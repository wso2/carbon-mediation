/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.gateway.agent.internal;


import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cloud.gateway.agent.observer.CGServiceObserver;
import org.wso2.carbon.cloud.gateway.agent.service.CGAgentAdminService;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGException;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="org.wso2.carbon.cloud.gateway.agent.internal.CGAgentServiceComponent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */

@SuppressWarnings({"UnusedDeclaration"})
public class CGAgentServiceComponent {

    private static Log log = LogFactory.getLog(CGAgentServiceComponent.class);

    private ConfigurationContextService configurationContextService;

    private RealmService realmService;

    private long initialReconnectDuration;

    private double reconnectionProgressionFactor;

    /**
     * Keep track of published services to re-publish
     */
    private List<String> pendingServices = new ArrayList<String>();

    protected void activate(ComponentContext context) {
        if (this.configurationContextService == null) {
            log.error("Cloud not activated the CGAgentServiceComponent. " +
                    "ConfigurationContextService is null!");
            return;
        }

        initialReconnectDuration = CGUtils.getLongProperty(CGConstant.INITIAL_RECONNECT_DURATION, 10000);
        reconnectionProgressionFactor = CGUtils.getDoubleProperty(CGConstant.PROGRESSION_FACTOR, 2.0);

        // register observers for automatic published services
        AxisConfiguration axisConfig =
                this.configurationContextService.getServerConfigContext().getAxisConfiguration();
        CGServiceObserver observer = new CGServiceObserver();
        axisConfig.addObservers(observer);

        String[] publishOptimizedList = UserCoreUtil.optimizePermissions(
                CGConstant.CG_PUBLISH_PERMISSION_LIST);

        String[] unpublishOptimizedList = UserCoreUtil.optimizePermissions(
                CGConstant.CG_UNPUBLISH_PERMISSION_LIST);

        try {
            // add the publish and un publish roles
            UserRealm realm = realmService.getBootstrapRealm();
            String publisherRole = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + CGConstant.CG_PUBLISH_ROLE_NAME;
            String unpublisherRole = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + CGConstant.CG_UNPUBLISH_ROLE_NAME;

            AuthorizationManager authorizationManager = realm.getAuthorizationManager();
/*            // Commenting the source to avoid database deadlock when clustered setup starts with shared database
            authorizationManager.clearRoleActionOnAllResources(publisherRole,
                    UserMgtConstants.EXECUTE_ACTION);
            authorizationManager.clearRoleActionOnAllResources(unpublisherRole,
                    UserMgtConstants.EXECUTE_ACTION);*/

            for (String permission : publishOptimizedList) {
                authorizationManager.authorizeRole(publisherRole, permission,
                        UserMgtConstants.EXECUTE_ACTION);
            }

            for (String permission : unpublishOptimizedList) {
                authorizationManager.authorizeRole(unpublisherRole, permission,
                        UserMgtConstants.EXECUTE_ACTION);
            }

            String cgUserName = CGUtils.getStringProperty(CGConstant.CG_USER_NAME, CGConstant.DEFAULT_CG_USER);
            String cgUserPassword = CGUtils.getStringProperty(CGConstant.CG_USER_PASSWORD,
                    CGConstant.DEFAULT_CG_USER_PASSWORD);

            UserStoreManager manager = realm.getUserStoreManager();

            if (!manager.isExistingRole(publisherRole)) {
                manager.addRole(publisherRole, new String[]{realm.getRealmConfiguration().getAdminUserName()}, null);
            }


            if (!manager.isExistingRole(unpublisherRole)) {
                manager.addRole(unpublisherRole, new String[]{realm.getRealmConfiguration().getAdminUserName()}, null);
            }

            // look for any published service and published them again
            for (Map.Entry<String, AxisService> entry : axisConfig.getServices().entrySet()) {
                AxisService axisService = entry.getValue();
                CGAgentAdminService service = new CGAgentAdminService();
                String status = service.getServiceStatus(axisService.getName());

                if (SystemFilter.isAdminService(axisService) || SystemFilter.isHiddenService(axisService) ||
                        axisService.isClientSide() || status.equals(CGConstant.CG_SERVICE_STATUS_UNPUBLISHED)) {
                    continue;
                }
                pendingServices.add(axisService.getName());
            }
            if (pendingServices.size() > 0) {
                new Thread(new ServiceRePublishingTask(), "Cloud-Gateway-re-publishing-thread").start();
            }
        } catch (Exception e) {
            log.error("Cloud not activated the CGAgentServiceComponent. ", e);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Activated the CGAgentServiceComponent");
        }
    }

    protected void deactivate(ComponentContext context) {

    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxService) {
        this.configurationContextService = configCtxService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (this.configurationContextService != null) {
            this.configurationContextService = null;
        }
    }

    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (this.realmService != null) {
            this.realmService = null;
        }
    }

    private class ServiceRePublishingTask implements Runnable {

        public void run() {
            long retryDuration = initialReconnectDuration;
            while (true) {
                if (CGUtils.isServerAlive("localhost",
                        CarbonUtils.getTransportPort(configurationContextService, "http"))) {
                    CGAgentAdminService adminService = new CGAgentAdminService();
                    for (String serviceName : pendingServices) {
                        try {
                            boolean isAutomatic = adminService.getServiceStatus(serviceName).equals(
                                    CGConstant.CG_SERVICE_STATUS_AUTO_MATIC);
                            String serverName = adminService.getPublishedServer(serviceName);
                            adminService.rePublishService(serviceName, serverName, isAutomatic);
                        } catch (CGException e) {
                            log.error("Error while re-publishing the previously published service '" + serviceName + "'," +
                                    " you will need to re-publish the service manually!", e);
                        }
                        log.info("Service '" + serviceName + "', re-published successfully");
                    }

                    break;
                } else {
                    // re-try until success
                    retryDuration = (long) (retryDuration * reconnectionProgressionFactor);
                    try {
                        Thread.sleep(retryDuration);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }
}
