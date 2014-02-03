/*
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mediation.configadmin;

import org.wso2.carbon.mediation.configadmin.util.ConfigHolder;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService;
import org.osgi.service.component.ComponentContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @scr.component name="org.wso2.carbon.sequences" immediate="true"
 * @scr.reference name="synapse.registrations.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
 * unbind="unsetSynapseRegistrationsService"
 * @scr.reference name="config.tracking.service"
 * interface="org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigTrackingService" unbind="unsetConfigTrackingService"
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class ConfigAdminServiceComponent {

    private static final Log log = LogFactory.getLog(ConfigAdminServiceComponent.class);

    protected void activate(ComponentContext cmpCtx) {
        ConfigHolder.getInstance().setBundleContext(cmpCtx.getBundleContext());
    }

    protected void setSynapseRegistrationsService(
            SynapseRegistrationsService synapseRegistrationsService) {
        ConfigHolder.getInstance().addSynapseRegistrationService(
                synapseRegistrationsService.getTenantId(), synapseRegistrationsService);       
    }

    protected void unsetSynapseRegistrationsService(
            SynapseRegistrationsService synapseRegistrationsService) {
        ConfigHolder.getInstance().removeSynapseRegistrationService(
                synapseRegistrationsService.getTenantId());
    }

    protected void setConfigTrackingService(ConfigurationTrackingService configTrackingService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationTrackingService bound to the ESB initialization process");
        }
        ConfigHolder.getInstance().setSynapseConfigTrackingService(configTrackingService);
    }

    protected void unsetConfigTrackingService(ConfigurationTrackingService configTrackingService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationTrackingService unbound from the ESB environment");
        }
        ConfigHolder.getInstance().setSynapseConfigTrackingService(null);
    }
}
