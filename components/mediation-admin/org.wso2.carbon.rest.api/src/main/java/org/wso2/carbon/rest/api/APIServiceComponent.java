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

package org.wso2.carbon.rest.api;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.deployers.APIDeployer;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.apache.synapse.rest.API;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.rest.api.service.RestAPIDeployerService;
import org.wso2.carbon.rest.api.service.RestAPIDeployerServiceImpl;
import org.wso2.carbon.rest.api.service.RestApiAdmin;
import org.wso2.carbon.rest.api.service.RestApiAdminService;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * @scr.component name="org.wso2.carbon.rest.api" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="synapse.config.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setSynapseConfigurationService"
 * unbind="unsetSynapseConfigurationService"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseEnvironmentService"
 * unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="0..1" policy="dynamic"
 * bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="dependency.mgt.service"
 * interface="org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService"
 * cardinality="0..1" policy="dynamic"
 * bind="setDependencyManager" unbind="unsetDependencyManager"
 * @scr.reference name="synapse.registrations.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
 * unbind="unsetSynapseRegistrationsService"
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class APIServiceComponent extends AbstractAxis2ConfigurationContextObserver{

    private static final Log log = LogFactory.getLog(APIServiceComponent.class);

    private boolean activated = false;

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bndCtx = ctxt.getBundleContext();
            bndCtx.registerService(Axis2ConfigurationContextObserver.class.getName(), this, null);
            bndCtx.registerService(RestAPIDeployerService.class.getName(),
                                   new RestAPIDeployerServiceImpl(), null);
            bndCtx.registerService(RestApiAdminService.class.getName(), new RestApiAdminService(), null);

            SynapseEnvironmentService synEnvService =
                    ConfigHolder.getInstance().getSynapseEnvironmentService(
                            MultitenantConstants.SUPER_TENANT_ID);

            registerDeployer(ConfigHolder.getInstance().getAxisConfiguration(),
                    synEnvService.getSynapseEnvironment());
            if (log.isDebugEnabled()) {
                log.debug("Rest API Admin bundle is activated ");
            }
            activated = true;
        } catch (Throwable e) {
            log.error("Failed to activate Rest API Admin bundle ", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        try {
            Set<Map.Entry<Integer, SynapseEnvironmentService>> entrySet =
                    ConfigHolder.getInstance().getSynapseEnvironmentServices().entrySet();
            for (Map.Entry<Integer, SynapseEnvironmentService> entry : entrySet) {
                unregisterDeployer(
                        entry.getValue().getConfigurationContext().getAxisConfiguration(),
                        entry.getValue().getSynapseEnvironment());
            }
        } catch (APIException e) {
            log.warn("Couldn't remove the APIDeployer");
        }
    }

    /**
     * Un-registers the API deployer.
     *
     * @param axisConfig         AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void unregisterDeployer(AxisConfiguration axisConfig, SynapseEnvironment synapseEnvironment)
            throws APIException {
        if (axisConfig != null) {
            DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
            String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                    synapseEnvironment.getServerContextInformation());
            String apiDirPath = synapseConfigPath
                    + File.separator + MultiXMLConfigurationBuilder.REST_API_DIR;
            deploymentEngine.removeDeployer(
                    apiDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
    }

    /**
     * Registers the API deployer.
     *
     * @param axisConfig         AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void registerDeployer(AxisConfiguration axisConfig, SynapseEnvironment synapseEnvironment)
            throws APIException {
        SynapseConfiguration synCfg = synapseEnvironment.getSynapseConfiguration();
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        SynapseArtifactDeploymentStore deploymentStore = synCfg.getArtifactDeploymentStore();

        String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                synapseEnvironment.getServerContextInformation());
        String apiDirPath = synapseConfigPath
                + File.separator + MultiXMLConfigurationBuilder.REST_API_DIR;

        for (API api : synCfg.getAPIs()) {
            if (api.getFileName() != null) {
                deploymentStore.addRestoredArtifact(
                        apiDirPath + File.separator + api.getFileName());
            }
        }
        synchronized (axisConfig) {
            deploymentEngine.addDeployer(
                    new ApiDeployer(), apiDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        ConfigHolder.getInstance().setAxisConfiguration(
                cfgCtxService.getServerConfigContext().getAxisConfiguration());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {
        ConfigHolder.getInstance().setAxisConfiguration(null);
    }

    protected void setSynapseConfigurationService(
            SynapseConfigurationService synapseConfigurationService) {

        ConfigHolder.getInstance().setSynapseConfiguration(
                synapseConfigurationService.getSynapseConfiguration());
    }

    protected void unsetSynapseConfigurationService(
            SynapseConfigurationService synapseConfigurationService) {

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
    protected void setSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        boolean alreadyCreated = ConfigHolder.getInstance().getSynapseEnvironmentServices().
                containsKey(synapseEnvironmentService.getTenantId());

        ConfigHolder.getInstance().addSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId(),
                synapseEnvironmentService);
        if (activated) {
            if (!alreadyCreated) {
                try {
                    registerDeployer(synapseEnvironmentService.getConfigurationContext().getAxisConfiguration(),
                            synapseEnvironmentService.getSynapseEnvironment());
                    if (log.isDebugEnabled()) {
                        log.debug("Rest API bundle is activated ");
                    }
                } catch (Throwable e) {
                    log.error("Failed to activate Rest API Admin bundle ", e);
                }
            }
        }
    }

    /**
     * Here we receive an event about Destroying a SynapseEnvironment. This can be the super tenant
     * destruction or a tenant destruction.
     *
     * @param synapseEnvironmentService synapseEnvironment
     */
    protected void unsetSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        ConfigHolder.getInstance().removeSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId());
    }

    protected void setRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the restapi component");
        }
        try {
            ConfigHolder.getInstance().setConfigRegistry(regService.getConfigSystemRegistry());
            ConfigHolder.getInstance().setGovernanceRegistry(regService.getGovernanceSystemRegistry());
        } catch (RegistryException e) {
            log.error("Couldn't retrieve the registry from the registry service");
        }
    }

    protected void unsetRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the restapi component");
        }
        ConfigHolder.getInstance().setConfigRegistry(null);
    }

    protected void setDependencyManager(DependencyManagementService dependencyMgr) {
        if (log.isDebugEnabled()) {
            log.debug("Dependency management service bound to the restapi component");
        }
        ConfigHolder.getInstance().setDependencyManager(dependencyMgr);
    }

    protected void unsetDependencyManager(DependencyManagementService dependencyMgr) {
        if (log.isDebugEnabled()) {
            log.debug("Dependency management service unbound from the restapi component");
        }
        ConfigHolder.getInstance().setDependencyManager(null);
    }

    protected void setSynapseRegistrationsService(
            SynapseRegistrationsService synapseRegistrationsService) {

    }

    public void createdConfigurationContext(ConfigurationContext configContext) {
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (axisConfig != null) {
            SynapseEnvironmentService synEnvService = ConfigHolder.getInstance().getSynapseEnvironmentService(tenantId);
            if (synEnvService != null) {
                try {
                    registerDeployer(axisConfig, synEnvService.getSynapseEnvironment());
                } catch (APIException e) {
                    log.error("Error while initializing API admin",e);
                }
            }
        }
    }

    protected void unsetSynapseRegistrationsService(
            SynapseRegistrationsService synapseRegistrationsService) {
        int tenantId = synapseRegistrationsService.getTenantId();
        if (ConfigHolder.getInstance().getSynapseEnvironmentServices().containsKey(tenantId)) {
            SynapseEnvironment env = ConfigHolder.getInstance().
                    getSynapseEnvironmentService(tenantId).getSynapseEnvironment();

            ConfigHolder.getInstance().removeSynapseEnvironmentService(
                    synapseRegistrationsService.getTenantId());

            AxisConfiguration axisConfig = synapseRegistrationsService.getConfigurationContext().
                    getAxisConfiguration();
            if (axisConfig != null) {
                try {
                    unregisterDeployer(axisConfig, env);
                } catch (APIException e) {
                    log.warn("Couldn't remove the APIDeployer");
                }
            }
        }
    }
}
