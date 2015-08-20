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
package org.wso2.carbon.cloud.gateway.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.cloud.gateway.transport.server.CGThriftServer;
import org.wso2.carbon.cloud.gateway.transport.server.CGThriftServerHandler;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import java.net.SocketException;
import java.util.HashMap;

/**
 * @scr.component name="CGServiceComponent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setServerConfiguration"
 * unbind="unsetServerConfiguration"
 */
public class CGServiceComponent {
    private static Log log = LogFactory.getLog(CGServiceComponent.class);

    private ServerConfigurationService serverConfiguration;
    private RealmService realmService;

    protected void activate(ComponentContext ctxt) {
        // CG needs to know the key store location and also add the csg user etc..
        if (this.serverConfiguration == null || this.realmService == null) {
            log.error("Could not activated the CGServiceComponent. " +
                    (this.serverConfiguration == null ?
                            "ServerConfigurationService" : "RealmService") + "is null!");
            return;
        }
        try {
            // add the default cguser into the user store
            String csgRoleName = CGUtils.getStringProperty(CGConstant.CG_ROLE_NAME,
                    CGConstant.DEFAULT_CG_ROLE_NAME);

            addCGUser(
                    csgRoleName,
                    CGUtils.getPermissionsList(),
                    CGUtils.getStringProperty(CGConstant.CG_USER_NAME, CGConstant.DEFAULT_CG_USER),
                    CGUtils.getStringProperty(CGConstant.CG_USER_PASSWORD,
                            CGConstant.DEFAULT_CG_USER_PASSWORD));

        } catch (UserStoreException e) {
            log.error("Cloud not activated the CGServiceComponent.", e);
            return;
        }
        String hostName;
        try {
            hostName = CGUtils.getCGThriftServerHostName();
        } catch (SocketException e) {
            log.error("Could not activated the CGServiceComponent.", e);
            return;
        }

        int port = CGUtils.getCGThriftServerPort();
        int timeOut = CGUtils.getIntProperty(CGConstant.CG_THRIFT_CLIENT_TIMEOUT,
                CGConstant.DEFAULT_TIMEOUT);
        String keyStoreURL = CGUtils.getKeyStoreFilePath();
        if (keyStoreURL == null) {
            log.error("KeyStore is missing and required for mutual SSL");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Loading key store from the location '" + keyStoreURL + "'");
        }

        String keyStorePassWord = CGUtils.getKeyStorePassWord();
        if (keyStorePassWord == null) {
            log.error("KeyStore password is missing");
            return;
        }

        String trustStoreURL = CGUtils.getTrustStoreFilePath();
        if (trustStoreURL == null) {
            log.error("TrustStore is missing and required for mutual SSL");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Loading trust store from the location '" + trustStoreURL + "'");
        }

        String trustStorePassWord = CGUtils.getTrustStorePassWord();
        if (trustStorePassWord == null) {
            log.error("TrustStore password is missing");
            return;
        }

        WorkerPool workerPool =
                WorkerPoolFactory.getWorkerPool(
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_CORE, CGConstant.WORKERS_CORE_THREADS),
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_MAX, CGConstant.WORKERS_MAX_THREADS),
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_ALIVE, CGConstant.WORKER_KEEP_ALIVE),
                        CGUtils.getIntProperty(
                                CGConstant.CG_T_QLEN, CGConstant.WORKER_BLOCKING_QUEUE_LENGTH),
                        "CGThriftServerHandler-worker-thread-group",
                        "CGThriftServerHandler-worker");
        CGThriftServerHandler csgThriftServerHandler = new CGThriftServerHandler(workerPool);
        CGThriftServer server = new CGThriftServer(csgThriftServerHandler);
        try {
            server.start(
                    hostName,
                    port,
                    timeOut,
                    keyStoreURL,
                    keyStorePassWord,
                    trustStoreURL,
                    trustStorePassWord,
                    "Cloud-Gateway-ThriftServer-main-thread");
        } catch (AxisFault axisFault) {
            log.error("Unable to start thrift server", axisFault);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Activated the CGServiceComponent");
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

    protected void setServerConfiguration(ServerConfigurationService configuration) {
        serverConfiguration = configuration;
    }

    protected void unsetServerConfiguration(ServerConfigurationService configuration) {
        serverConfiguration = null;
    }

    private void addCGUser(String roleName,
                           String[] permissionList,
                           String csgUserName,
                           String passWord)
            throws UserStoreException {
        // add the required permission to the csg role
        String[] optimizedList = UserCoreUtil.optimizePermissions(permissionList);
        UserRealm realm = realmService.getBootstrapRealm();
        if (realm.getRealmConfiguration().getAdminRoleName().equals(roleName)) {
            throw new UserStoreException("UI permission of admin is not allowed to change!");
        }
        AuthorizationManager authorizationManager = realm.getAuthorizationManager();
        authorizationManager.clearRoleActionOnAllResources(roleName,
                UserMgtConstants.EXECUTE_ACTION);
        for (String permission : optimizedList) {
            authorizationManager.authorizeRole(roleName, permission,
                    UserMgtConstants.EXECUTE_ACTION);
        }

        // set required permission for csguser to put/get/delete WSDLs etc..
        authorizationManager.authorizeRole(roleName, "/", "add");
        authorizationManager.authorizeRole(roleName, "/", "get");
        authorizationManager.authorizeRole(roleName, "/", "delete");

        UserStoreManager manager = realm.getUserStoreManager();
        // register the cg role if not registered already and add the cguser
        

        if (!manager.isExistingUser(csgUserName)) {
            manager.addUser(
                    csgUserName,
                    passWord,
                    new String[]{},
                    new HashMap<String, String>(),
                    null,
                    false);
        }
        
        if (!manager.isExistingRole(roleName)) {
            manager.addRole(roleName, new String[]{csgUserName}, null);
        }
    }
}
