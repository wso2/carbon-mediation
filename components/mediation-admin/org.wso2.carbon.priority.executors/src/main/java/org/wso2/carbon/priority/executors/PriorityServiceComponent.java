/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.priority.executors;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.priority.executors.services.PriorityExeDeployerService;
import org.wso2.carbon.priority.executors.services.PriorityExeDeployerServiceImpl;
import org.wso2.carbon.priority.executors.util.ConfigHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.priority", 
         immediate = true)
public class PriorityServiceComponent extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(PriorityServiceComponent.class);

    private boolean activated = false;

    private Map<Integer, SynapseEnvironmentService> synapseEnvironmentServices = new HashMap<Integer, SynapseEnvironmentService>();

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bndCtx = ctxt.getBundleContext();
            bndCtx.registerService(Axis2ConfigurationContextObserver.class.getName(), this, null);
            bndCtx.registerService(PriorityExeDeployerService.class.getName(), new PriorityExeDeployerServiceImpl(), null);
            SynapseEnvironmentService synEnvService = synapseEnvironmentServices.get(MultitenantConstants.SUPER_TENANT_ID);
            registerDeployer(ConfigHolder.getInstance().getAxisConfiguration(), synEnvService.getSynapseEnvironment());
            if (log.isDebugEnabled()) {
                log.debug("Endpoint Admin bundle is activated ");
            }
            activated = true;
            if (log.isDebugEnabled()) {
                log.debug("Sequence Admin bundle is activated ");
            }
        } catch (Throwable e) {
            log.error("Failed to activate Sequence Admin bundle ", e);
        }
    }

    /**
     * Un-registers the Endpoint deployer.
     *
     * @param axisConfig         AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void unRegisterDeployer(AxisConfiguration axisConfig, SynapseEnvironment synapseEnvironment) {
        if (axisConfig != null) {
            DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
            String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(synapseEnvironment.getServerContextInformation());
            String endpointDirPath = synapseConfigPath + File.separator + MultiXMLConfigurationBuilder.EXECUTORS_DIR;
            deploymentEngine.removeDeployer(endpointDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
    }

    /**
     * Registers the Endpoint deployer.
     *
     * @param axisConfig         AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void registerDeployer(AxisConfiguration axisConfig, SynapseEnvironment synapseEnvironment) {
        SynapseConfiguration synCfg = synapseEnvironment.getSynapseConfiguration();
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        SynapseArtifactDeploymentStore deploymentStore = synCfg.getArtifactDeploymentStore();
        String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(synapseEnvironment.getServerContextInformation());
        String endpointDirPath = synapseConfigPath + File.separator + MultiXMLConfigurationBuilder.EXECUTORS_DIR;
        for (PriorityExecutor ep : synCfg.getPriorityExecutors().values()) {
            if (ep.getFileName() != null) {
                deploymentStore.addRestoredArtifact(endpointDirPath + File.separator + ep.getFileName());
            }
        }
        synchronized (axisConfig) {
            deploymentEngine.addDeployer(new ExecutorDeployer(), endpointDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
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

    @Reference(
             name = "synapse.env.service", 
             service = org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService.class, 
             cardinality = ReferenceCardinality.AT_LEAST_ONE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSynapseEnvironmentService")
    protected void setSynapseEnvironmentService(SynapseEnvironmentService synEnvSvc) {
        boolean alreadyCreated = synapseEnvironmentServices.containsKey(synEnvSvc.getTenantId());
        synapseEnvironmentServices.put(synEnvSvc.getTenantId(), synEnvSvc);
        if (activated) {
            if (!alreadyCreated) {
                try {
                    registerDeployer(synEnvSvc.getConfigurationContext().getAxisConfiguration(), synEnvSvc.getSynapseEnvironment());
                    if (log.isDebugEnabled()) {
                        log.debug("Endpoint Admin bundle is activated ");
                    }
                } catch (Throwable e) {
                    log.error("Failed to activate Endpoint Admin bundle ", e);
                }
            }
        }
    }

    protected void unsetSynapseEnvironmentService(SynapseEnvironmentService synEnvSvc) {
        synapseEnvironmentServices.remove(synEnvSvc.getTenantId());
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.OPTIONAL, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the ESB initialization process");
        }
        try {
            ConfigHolder.getInstance().setRegistry(regService.getConfigSystemRegistry());
        } catch (RegistryException e) {
            log.error("Couldn't retrieve the registry from the registry service");
        }
    }

    protected void unsetRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the ESB environment");
        }
        ConfigHolder.getInstance().setRegistry(null);
    }

    @Reference(
             name = "synapse.registrations.service", 
             service = org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService.class, 
             cardinality = ReferenceCardinality.AT_LEAST_ONE, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetSynapseRegistrationsService")
    protected void setSynapseRegistrationsService(SynapseRegistrationsService synapseRegistrationsService) {
    }

    protected void unsetSynapseRegistrationsService(SynapseRegistrationsService synapseRegistrationsService) {
        int tenantId = synapseRegistrationsService.getTenantId();
        if (synapseEnvironmentServices.containsKey(tenantId)) {
            SynapseEnvironment env = synapseEnvironmentServices.get(tenantId).getSynapseEnvironment();
            synapseEnvironmentServices.remove(synapseRegistrationsService.getTenantId());
            AxisConfiguration axisConfig = synapseRegistrationsService.getConfigurationContext().getAxisConfiguration();
            if (axisConfig != null) {
                unRegisterDeployer(axisConfig, env);
            }
        }
    }

    public void createdConfigurationContext(ConfigurationContext configContext) {
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (axisConfig != null) {
            SynapseEnvironmentService synEnvService = synapseEnvironmentServices.get(tenantId);
            if (synEnvService != null) {
                try {
                    registerDeployer(axisConfig, synEnvService.getSynapseEnvironment());
                } catch (Exception e) {
                    log.error("Error while initializing PriorityExecutor Admin", e);
                }
            }
        }
    }
}
