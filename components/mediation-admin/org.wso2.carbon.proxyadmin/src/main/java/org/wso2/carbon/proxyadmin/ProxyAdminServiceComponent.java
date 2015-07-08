/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.proxyadmin;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.proxyadmin.observer.ProxyObserver;
import org.wso2.carbon.proxyadmin.observer.ProxyServiceParameterObserver;
import org.wso2.carbon.proxyadmin.service.ProxyDeployerService;
import org.wso2.carbon.proxyadmin.service.ProxyDeployerServiceImpl;
import org.wso2.carbon.proxyadmin.util.ConfigHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @scr.component name="org.wso2.carbon.proxyadmin" immediate="true"
 * @scr.reference name="synapse.env.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService"
 * cardinality="1..n" policy="dynamic"
 * bind="setSynapseEnvironmentService" unbind="unsetSynapseEnvironmentService"
 * @scr.reference name="service.admin.service" interface="org.wso2.carbon.service.mgt.ServiceAdmin"
 *  cardinality="1..1" policy="dynamic"
 *  bind="setServiceadminService" unbind="unsetServiceAdminService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="synapse.registrations.service"
 * interface="org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService"
 * cardinality="1..n" policy="dynamic" bind="setSynapseRegistrationsService"
 * unbind="unsetSynapseRegistrationsService"
 * @scr.reference name="synapse.capp.deployment.service"
 * interface="org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService"
 * cardinality="1..n" policy="dynamic" bind="setCAppArtifactDataService"
 * unbind="unsetCAppArtifactDataService"
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ProxyAdminServiceComponent extends AbstractAxis2ConfigurationContextObserver{

    private static final Log log = LogFactory.getLog(ProxyAdminServiceComponent.class);

    private boolean initialized = false;
    
    private boolean registerDeployers =true;

    private static final String SERVICE_TYPE = "serviceType";
    private static final String PROXY_SERVICE_TYPE = "proxy";

    protected void activate(ComponentContext context) {
        try {
            initialized = true;
            BundleContext bndCtx = context.getBundleContext();
            
            bndCtx.registerService(
                                   Axis2ConfigurationContextObserver.class.getName(), this, null);

            bndCtx.registerService(ProxyDeployerService.class.getName(),
                                   new ProxyDeployerServiceImpl(), null);
            
            SynapseEnvironmentService synEnvService =
                    ConfigHolder.getInstance().getSynapseEnvironmentService(
                            MultitenantConstants.SUPER_TENANT_ID);
            if (synEnvService != null) {
                AxisConfiguration axisConf = synEnvService.getConfigurationContext().
                        getAxisConfiguration();
                
                try {
                    ProxyObserver proxyObserver = new ProxyObserver(synEnvService,
                            ConfigHolder.getInstance().getRegistryService().
                                    getConfigSystemRegistry());

                    ConfigHolder.getInstance().addProxyObserver(
                            MultitenantConstants.SUPER_TENANT_ID, proxyObserver);

                    axisConf.addObservers(proxyObserver);

                    registerDeployer(synEnvService.getConfigurationContext().getAxisConfiguration(),
                            synEnvService.getSynapseEnvironment());
                    proxyObserver.setSynapseEnvironmentService(synEnvService);

                    bindProxyParameterObserver(axisConf);
                } catch (ProxyAdminException e) {
                    log.error("Error while initializing the proxy service observer. " +
                            "Proxy admin component may be unstable.", e);
                }
            } else {
                log.warn("Cannot register the proxy service observer. The axis service " +
                        "representing the proxy service might not be in sync with the proxy");
            }            
        } catch (Throwable t) {
            log.fatal("Error occurred while activating the Proxy Admin", t);
        }
    }

     private void bindProxyParameterObserver(AxisConfiguration axisConfiguration) {
       HashMap<String, AxisService> axisServices = axisConfiguration.getServices();
        if (null != axisServices) {
            for (Map.Entry<String, AxisService> aAxisService : axisServices.entrySet()) {
               AxisService service = aAxisService.getValue();
                Object type = service.getParameterValue(SERVICE_TYPE);
                if (null != type) {
                    if (type.toString().equalsIgnoreCase(PROXY_SERVICE_TYPE)){
                        ProxyServiceParameterObserver parameterObserver =
                                new ProxyServiceParameterObserver(service);
                        service.addParameterObserver(parameterObserver);
                    }
                }
            }
        }
    }

    private void registerDeployer(AxisConfiguration axisConfig, SynapseEnvironment synEnv)
            throws ProxyAdminException {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        SynapseArtifactDeploymentStore deploymentStore =
                synEnv.getSynapseConfiguration().getArtifactDeploymentStore();

        String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                synEnv.getServerContextInformation());

        String proxyDirPath = synapseConfigPath
                + File.separator + MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR;

        for (ProxyService proxyService : synEnv.getSynapseConfiguration().getProxyServices()) {
            if (proxyService.getFileName() != null) {
                deploymentStore.addRestoredArtifact(
                        proxyDirPath + File.separator + proxyService.getFileName());
            }
        }
        synchronized (axisConfig) {
            deploymentEngine.addDeployer(
                    new ProxyServiceDeployer(), proxyDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
        }
     }

    protected void deactivate(ComponentContext context) {
        Set<Map.Entry<Integer, SynapseEnvironmentService>> entrySet =
                ConfigHolder.getInstance().getSynapseEnvironmentServices().entrySet();
        for (Map.Entry<Integer, SynapseEnvironmentService> entry : entrySet) {
            unregistryDeployer(
                    entry.getValue().getConfigurationContext().getAxisConfiguration(),
                    entry.getValue().getSynapseEnvironment());
        }
    }

    /**
     * Here we receive an event about the creation of a SynapseEnvironment. If this is
     * SuperTenant we have to wait until all the other constraints are met and actual
     * initialization is done in the activate method. Otherwise we have to do the activation here.
     *
     * @param synapseEnvironmentService SynapseEnvironmentService which contains information
     * about the new Synapse Instance
     */
    protected void setSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        boolean alreadyCreated = ConfigHolder.getInstance().getSynapseEnvironmentServices().
                containsKey(synapseEnvironmentService.getTenantId());
        
        ConfigHolder.getInstance().addSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId(),
                synapseEnvironmentService);
        if (initialized) {
            int tenantId = synapseEnvironmentService.getTenantId();
            AxisConfiguration axisConfiguration = synapseEnvironmentService.
                    getConfigurationContext().getAxisConfiguration();

            ProxyObserver observer;
            if (!alreadyCreated) {
                try {
                    registerDeployer(
                            synapseEnvironmentService.getConfigurationContext().getAxisConfiguration(),
                            synapseEnvironmentService.getSynapseEnvironment());
                    
                   observer = new ProxyObserver(synapseEnvironmentService,
                            ConfigHolder.getInstance().getRegistryService().
                                    getConfigSystemRegistry(tenantId));
                    axisConfiguration.addObservers(observer);
                    ConfigHolder.getInstance().addProxyObserver(tenantId, observer);
                } catch (ProxyAdminException e) {
                    log.error("Error while initializing the proxy admin.", e);
                } catch (RegistryException e) {
                    log.error("Error while initializing the proxy admin.", e);
                }
            } else {
                observer = ConfigHolder.getInstance().getProxyObsever(tenantId);
                if (observer != null) {
                    observer.setSynapseEnvironmentService(synapseEnvironmentService);
                }
            }
        }
    }

    /**
     * Here we receive an event about Destroying a SynapseEnvironment. This can be the super tenant
     * destruction or a tenant destruction. But in this method we don't do anything
     *
     * @param synapseEnvironmentService synapseEnvironment
     */
    protected void unsetSynapseEnvironmentService(
            SynapseEnvironmentService synapseEnvironmentService) {
        ConfigHolder.getInstance().removeSynapseEnvironmentService(
                synapseEnvironmentService.getTenantId());
    }

    protected void setSynapseRegistrationsService(
            SynapseRegistrationsService synapseRegistrationsService) {
        
    }

    protected void setCAppArtifactDataService(
            CAppArtifactDataService cAppArtifactDataService) {
        ConfigHolder.getInstance().setcAppArtifactDataService(cAppArtifactDataService);
    }

    protected void unsetCAppArtifactDataService(
            CAppArtifactDataService cAppArtifactDataService) {
        ConfigHolder.getInstance().setcAppArtifactDataService(null);
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
                unregistryDeployer(axisConfig, env);                
            }
        }
    }

    /**
     * Un-registers the Proxy Deployer.
     *
     * @param axisConfig AxisConfiguration to which this deployer belongs
     * @param synapseEnvironment SynapseEnvironment to which this deployer belongs
     */
    private void unregistryDeployer(AxisConfiguration axisConfig,
                                    SynapseEnvironment synapseEnvironment) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        String synapseConfigPath = ServiceBusUtils.getSynapseConfigAbsPath(
                synapseEnvironment.getServerContextInformation());
        String proxyDirPath = synapseConfigPath
                + File.separator + MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR;
        deploymentEngine.removeDeployer(
                proxyDirPath, ServiceBusConstants.ARTIFACT_EXTENSION);
    }

    protected void setServiceadminService(ServiceAdmin serviceAdmin) {}
    protected void unsetServiceAdminService(ServiceAdmin serviceAdmin) {}

    protected void setRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the ESB initialization process");
        }
        ConfigHolder.getInstance().setRegistryService(regService);        
    }

    protected void unsetRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the ESB environment");
        }
        ConfigHolder.getInstance().setRegistryService(null);
    }
    
    
	public void createdConfigurationContext(ConfigurationContext configContext) {
		AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
	    if (!CarbonUtils.isWorkerNode()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Proxy Observer tenant reinitialization");
                }
               
                if (axisConfig != null) {
                     SynapseEnvironmentService synEnvService = ConfigHolder.getInstance().getSynapseEnvironmentService(tenantId);
                    if (synEnvService != null) {
                        ProxyObserver proxyObserver = new ProxyObserver(synEnvService, ConfigHolder.getInstance().getRegistryService()
                                .getConfigSystemRegistry(tenantId));

                        ConfigHolder.getInstance().addProxyObserver(tenantId, proxyObserver);
                        axisConfig.addObservers(proxyObserver);
                        registerDeployer(axisConfig, synEnvService.getSynapseEnvironment());
                        proxyObserver.setSynapseEnvironmentService(synEnvService);
                    }

                } else {
                    log.error("Error while initialzing AxisConfiguration", null);
                }

            } catch (Throwable t) {
                log.error("Error while initializing the proxy admin.", t);
            }
        }else{
        	  SynapseEnvironmentService synEnvService = ConfigHolder.getInstance().getSynapseEnvironmentService(tenantId);
        	  try {
				if (synEnvService != null && axisConfig != null) {
					this.registerDeployer(axisConfig, synEnvService.getSynapseEnvironment());
				}
              } catch (ProxyAdminException e) {
            	 log.error("Error while initializing the proxy admin.", e);
              }
        }

    }
    
}
