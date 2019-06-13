/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mediation.throttle.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.mediation.throttle.ThrottlingAxis2ConfigurationContextObserver;
import org.wso2.carbon.mediation.throttle.ThrottlingUtils;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "throttle.services",
        immediate = true)
public class ThrottleServiceComponent {

    private static Log log = LogFactory.getLog(ThrottleServiceComponent.class);

    private static RegistryService registryServiceInstance;

    private static RealmService realmService;

    private static BundleContext bundleContext;

    private ServiceRegistration axisConfigContextObserverServiceReg;

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            bundleContext = ctxt.getBundleContext();
            // Save the template policies in the registry.
            saveTemplatePolicies(bundleContext);
            axisConfigContextObserverServiceReg = bundleContext.registerService(Axis2ConfigurationContextObserver
                    .class.getName(), new ThrottlingAxis2ConfigurationContextObserver(), null);
            log.debug("Throttle bundle is activated");
        } catch (Throwable e) {
            log.error("Failed to activate Throttle bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        axisConfigContextObserverServiceReg.unregister();
        log.debug("Throttle bundle is deactivated");
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {

        this.realmService = null;
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        registryServiceInstance = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        registryServiceInstance = null;
    }

    public static RegistryService getRegistryService() throws Exception {

        if (registryServiceInstance == null) {
            String msg = "Before activating Throttle Admin bundle, an instance of " + "RegistryService should be in " +
                    "existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return registryServiceInstance;
    }

    /**
     * Save the template policies in the registry. These will be used to
     * generate the policy file when the configuration data are sent
     * by the user
     *
     * @param bundleContext execution context of the throttle bundle
     * @throws java.io.IOException
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     */
    private void saveTemplatePolicies(BundleContext bundleContext) throws RegistryException, IOException {
        // Get the registry from the registry service
        Registry registry;
        if (registryServiceInstance != null) {
            registry = registryServiceInstance.getConfigSystemRegistry();
        } else {
            log.error("Failed to save template policies in throttling component");
            return;
        }
        try {
            registry.beginTransaction();
            ThrottlingUtils.saveTemplatePoliciesToRegistry(registry);
            registry.commitTransaction();
        } catch (Exception e) {
            registry.rollbackTransaction();
            String msg = "Failed to save template policies in throttling component";
            log.error(msg, e);
        }
    }

    public static BundleContext getBundleContext() {

        CarbonUtils.checkSecurity();
        return bundleContext;
    }
}

