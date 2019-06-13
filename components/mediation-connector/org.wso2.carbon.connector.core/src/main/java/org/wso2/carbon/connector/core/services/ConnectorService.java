/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector.core.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.connector.core.util.ConfigHolder;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "org.wso2.carbon.connector.api",
        immediate = true)
public class ConnectorService {

    private static final Log log = LogFactory.getLog(ConnectorService.class);

    private boolean activated = false;

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Connector Core bundle is activated ");
            }
            activated = true;
        } catch (Throwable e) {
            log.error("Failed to activate connectors core bundle ", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

    }

    @Reference(
            name = "configuration.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {

        ConfigHolder.getInstance().setAxisConfiguration(cfgCtxService.getServerConfigContext().getAxisConfiguration());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {

        ConfigHolder.getInstance().setAxisConfiguration(null);
    }

    @Reference(
            name = "synapse.config.service",
            service = org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSynapseConfigurationService")
    protected void setSynapseConfigurationService(SynapseConfigurationService synapseConfigurationService) {

        ConfigHolder.getInstance().setSynapseConfiguration(synapseConfigurationService.getSynapseConfiguration());
    }

    protected void unsetSynapseConfigurationService(SynapseConfigurationService synapseConfigurationService) {

        ConfigHolder.getInstance().setSynapseConfiguration(null);
    }

    /**
     * Here we receive an event about the creation of a SynapseEnvironment. If this is
     * SuperTenant we have to wait until all the other constraints are met and actual
     * initialization is done in the activate method. Otherwise we have to do the activation here.
     *
     * @param synapseEnvironmentService SynapseEnvironmentService which contains information
     *                                  about the new Synapse Instance
     */
    @Reference(
            name = "synapse.env.service",
            service = org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSynapseEnvironmentService")
    protected void setSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {

        boolean alreadyCreated = ConfigHolder.getInstance().getSynapseEnvironmentServices().containsKey
                (synapseEnvironmentService.getTenantId());
        ConfigHolder.getInstance().addSynapseEnvironmentService(synapseEnvironmentService.getTenantId(),
                synapseEnvironmentService);
    }

    /**
     * Here we receive an event about Destroying a SynapseEnvironment. This can be the super tenant
     * destruction or a tenant destruction.
     *
     * @param synapseEnvironmentService synapseEnvironment
     */
    protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synapseEnvironmentService) {

        ConfigHolder.getInstance().removeSynapseEnvironmentService(synapseEnvironmentService.getTenantId());
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService regService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the connectors core component");
        }
        try {
            ConfigHolder.getInstance().setRegistryService(regService);
            ConfigHolder.getInstance().setConfigRegistry(regService.getConfigSystemRegistry());
            ConfigHolder.getInstance().setGovernanceRegistry(regService.getGovernanceSystemRegistry());
        } catch (RegistryException e) {
            log.error("Couldn't retrieve the registry from the registry service");
        }
    }

    protected void unsetRegistryService(RegistryService regService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the connectors core component");
        }
        ConfigHolder.getInstance().setConfigRegistry(null);
        ConfigHolder.getInstance().setRegistryService(null);
    }

    /**
     * @param realmservice
     */
    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    public void setRealmService(RealmService realmservice) {

        ConfigHolder.getInstance().setRealmservice(realmservice);
    }

    public void unsetRealmService(RealmService realmservice) {

        ConfigHolder.getInstance().setRealmservice(null);
    }
}
