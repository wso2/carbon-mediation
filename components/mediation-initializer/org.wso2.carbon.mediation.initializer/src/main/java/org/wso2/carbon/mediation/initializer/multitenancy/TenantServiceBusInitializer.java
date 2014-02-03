/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.initializer.multitenancy;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerConfigurationInformationFactory;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.ServerManager;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.SynapseConfigurationBuilder;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.MultiXMLConfigurationSerializer;
import org.apache.synapse.deployers.ExtensionDeployer;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.registry.Registry;
import org.apache.synapse.task.TaskConstants;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.synapse.task.TaskScheduler;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService;
import org.wso2.carbon.mediation.initializer.CarbonSynapseController;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusInitializer;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationManager;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationServiceImpl;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentServiceImpl;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsServiceImpl;
import org.wso2.carbon.mediation.initializer.utils.ConfigurationHolder;
import org.wso2.carbon.mediation.registry.WSO2Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This creates the {@link org.apache.synapse.config.SynapseConfiguration}
 * for the respective tenants.
 */
public class TenantServiceBusInitializer extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(TenantServiceBusInitializer.class);    

//    private Map<Integer, ServiceRegistration> tenantRegistrations =
//            new HashMap<Integer, ServiceRegistration>();

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        ServerContextInformation contextInfo;
        String tenantDomain =
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        
        log.info("Intializing the ESB Configuration for the tenant domain : " + tenantDomain);

        try {
            // first check which configuration should be active
            org.wso2.carbon.registry.core.Registry registry =
                    (org.wso2.carbon.registry.core.Registry) PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().
                                    getRegistry(RegistryType.SYSTEM_CONFIGURATION);

            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

            // initialize the lock
            Lock lock = new ReentrantLock();
            axisConfig.addParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK, lock);

            // creates the synapse configuration directory hierarchy if not exists
            // useful at the initial tenant creation
            File tenantAxis2Repo = new File(
                    configurationContext.getAxisConfiguration().getRepository().getFile());
            File synapseConfigsDir = new File(tenantAxis2Repo, ServiceBusConstants.SYNAPSE_CONFIGS);
            if (!synapseConfigsDir.exists()) {
                if (!synapseConfigsDir.mkdir()) {
                    log.fatal("Couldn't create the synapse-config root on the file system " +
                            "for the tenant domain : " + tenantDomain);
                    return;
                }
            }

            String synapseConfigsDirLocation = synapseConfigsDir.getAbsolutePath();

            // set the required configuration parameters to initialize the ESB
            axisConfig.addParameter(SynapseConstants.Axis2Param.SYNAPSE_CONFIG_LOCATION,
                    synapseConfigsDirLocation);

            // init the multiple configuration tracker
            ConfigurationManager manger = new ConfigurationManager((UserRegistry) registry,
                    configurationContext);
            manger.init();

            File synapseConfigDir = new File(synapseConfigsDir,
                    manger.getTracker().getCurrentConfigurationName());
            if (!synapseConfigDir.exists()) {
                createTenantSynapseConfigHierarchy(synapseConfigDir, tenantDomain);
            }

            axisConfig.addParameter(SynapseConstants.Axis2Param.SYNAPSE_HOME,
                    tenantAxis2Repo.getAbsolutePath());
            axisConfig.addParameter(SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME,
                    "synapse." + tenantDomain);
            axisConfig.addParameter(SynapseConstants.Axis2Param.SYNAPSE_RESOLVE_ROOT,
                    tenantAxis2Repo.getAbsolutePath());

            // Initialize Synapse
            contextInfo = initESB(manger.getTracker().getCurrentConfigurationName(),
                    configurationContext);

            if (contextInfo == null) {
                handleFatal("Failed to intilize the ESB for tenent:" + tenantDomain);
            }
            
            initPersistence(manger.getTracker().getCurrentConfigurationName(),
                    configurationContext,
                    contextInfo);

            configurationContext.setProperty(
                    ConfigurationManager.CONFIGURATION_MANAGER, manger);

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            // populate the SynapseEnv service and SynapseConfig OSGI Services so that other
            // components get to know about the availability of the new synapse configuration            
            //Properties props = new Properties();
            SynapseConfigurationService synCfgSvc
                        = new SynapseConfigurationServiceImpl(contextInfo.getSynapseConfiguration(),
                        tenantId, configurationContext);

            ServiceRegistration confRegistration =
                    ConfigurationHolder.getInstance().getBundleContext().registerService(
                        SynapseConfigurationService.class.getName(), synCfgSvc, null);

            //props = new Properties();
            SynapseEnvironmentService synEnvSvc
                        = new SynapseEnvironmentServiceImpl(contextInfo.getSynapseEnvironment(),
                        tenantId, configurationContext);
            ServiceRegistration envRegistration =
                    ConfigurationHolder.getInstance().getBundleContext().registerService(
                        SynapseEnvironmentService.class.getName(), synEnvSvc, null);

            //props = new Properties();
            SynapseRegistrationsService synRegistrationsSvc
                    = new SynapseRegistrationsServiceImpl(
                    confRegistration, envRegistration, tenantId, configurationContext);
            ServiceRegistration synapseRegistration =
                    ConfigurationHolder.getInstance().getBundleContext().registerService(
                    SynapseRegistrationsService.class.getName(),
                    synRegistrationsSvc, null);
            
            //creating secure-vault specific location
            if (!isRepoExists(registry)) {
    			org.wso2.carbon.registry.core.Collection secureVaultCollection = registry
    					.newCollection();
    			registry.put(ServiceBusConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY,
    					secureVaultCollection);
    		
    		}
            

            ConfigurationTrackingService trackingService = ServiceBusInitializer.
                    getConfigurationTrackingService();
            if (trackingService != null) {
                trackingService.setSynapseConfiguration(contextInfo.getSynapseConfiguration());
            }

	        // set the event broker as a property for tenants
            EventBroker eventBroker = ServiceBusInitializer.getEventBroker();
            if (eventBroker != null) {
               configurationContext.setProperty("mediation.event.broker", eventBroker);
            }

            ConfigurationHolder.getInstance().addSynapseRegistration(tenantId, synapseRegistration);
            
        } catch (Exception e) {
            handleFatal("Couldn't initialize the ESB for tenant:" + tenantDomain, e);
        } catch (Throwable t) {
            log.fatal("Failed to initialize ESB for tenant:"
                    + tenantDomain + "due to a fatal error", t);
        }
    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain =
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        log.info("Shutting down the persistence manager for the tenant: " + tenantDomain);

        Parameter p = configurationContext.getAxisConfiguration().getParameter(
                ServiceBusConstants.PERSISTENCE_MANAGER);
        if (p != null && p.getValue() instanceof MediationPersistenceManager) {
            ((MediationPersistenceManager) p.getValue()).destroy();
        }

        // unregister the service so that components get to know about the tenant termination
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ServiceRegistration tenantRegistration = ConfigurationHolder.getInstance().
                getSynapseRegistration(tenantId);

        if (tenantRegistration != null) {
            ConfigurationHolder.getInstance().getBundleContext().ungetService(
                    tenantRegistration.getReference());
        }
    }

    private void initPersistence(String configName,
                                 ConfigurationContext configurationContext,
                                 ServerContextInformation contextInfo)
            throws RegistryException, AxisFault {
        // Initialize the mediation persistence manager if required
        ServerConfiguration serverConf = ServerConfiguration.getInstance();
        String persistence = serverConf.getFirstProperty(ServiceBusConstants.PERSISTENCE);
        org.wso2.carbon.registry.core.Registry configRegistry =
                (org.wso2.carbon.registry.core.Registry) PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().
                                getRegistry(RegistryType.SYSTEM_CONFIGURATION);

        // Check whether persistence is disabled
        if (!ServiceBusConstants.DISABLED.equals(persistence)) {


            // Check registry persistence is disabled or not
            String regPersistence = serverConf.getFirstProperty(
                    ServiceBusConstants.REGISTRY_PERSISTENCE);
            UserRegistry registry = ServiceBusConstants.ENABLED.equals(regPersistence) ?
                    (UserRegistry) configRegistry : null;

            // Check the worker interval is set or not
            String interval = serverConf.getFirstProperty(ServiceBusConstants.WORKER_INTERVAL);
            long intervalInMillis = 5000L;
            if (interval != null && !"".equals(interval)) {
                try {
                    intervalInMillis = Long.parseLong(interval);
                } catch (NumberFormatException e) {
                    log.error("Invalid value " + interval + " specified for the mediation " +
                            "persistence worker interval, Using defaults", e);
                }
            }

            MediationPersistenceManager pm = new MediationPersistenceManager(registry,
                    contextInfo.getServerConfigurationInformation().getSynapseXMLLocation(),
                    contextInfo.getSynapseConfiguration(), intervalInMillis, configName);

            configurationContext.getAxisConfiguration().addParameter(
                    new Parameter(ServiceBusConstants.PERSISTENCE_MANAGER, pm));
       } else {
            log.info("Persistence for mediation configuration is disabled");
        }
    }

    private ServerContextInformation initESB(String configurationName,
                                             ConfigurationContext configurationContext)
            throws AxisFault {
        ServerConfigurationInformation configurationInformation =
                ServerConfigurationInformationFactory.createServerConfigurationInformation(
                        configurationContext.getAxisConfiguration());
        // ability to specify the SynapseServerName as a system property
        if (System.getProperty("SynapseServerName") != null) {
            configurationInformation.setServerName(System.getProperty("SynapseServerName"));
        }

        // for now we override the default configuration location with the value in registry
        configurationInformation.setSynapseXMLLocation(
                configurationInformation.getSynapseXMLLocation() + File.separator + configurationName);


        configurationInformation.setCreateNewInstance(false);
        configurationInformation.setServerControllerProvider(
                CarbonSynapseController.class.getName());
        if (isRunningSamplesMode()) {
            configurationInformation.setSynapseXMLLocation("repository" + File.separator
                    + "samples" + File.separator + "synapse_sample_" + System.getProperty(
                    ServiceBusConstants.ESB_SAMPLE_SYSTEM_PROPERTY) + ".xml");
        }

        ServerManager serverManager = new ServerManager();
        ServerContextInformation contextInfo = new ServerContextInformation(configurationContext,
                configurationInformation);

        /*if (dataSourceInformationRepositoryService != null) {
            DataSourceInformationRepository repository =
                    dataSourceInformationRepositoryService.getDataSourceInformationRepository();
            contextInfo.addProperty(DataSourceConstants.DATA_SOURCE_INFORMATION_REPOSITORY,
                    repository);
        }*/

        TaskScheduler scheduler;
        if (configurationContext.getProperty(ServiceBusConstants.CARBON_TASK_SCHEDULER) == null) {
            scheduler = new TaskScheduler(TaskConstants.TASK_SCHEDULER);
            configurationContext.setProperty(ServiceBusConstants.CARBON_TASK_SCHEDULER, scheduler);
        } else {
            scheduler = (TaskScheduler) configurationContext.getProperty(
                    ServiceBusConstants.CARBON_TASK_SCHEDULER);
        }
        contextInfo.addProperty(TaskConstants.TASK_SCHEDULER, scheduler);

        TaskDescriptionRepository repository;                
        if (configurationContext.getProperty(ServiceBusConstants.CARBON_TASK_REPOSITORY) == null) {
            repository = new TaskDescriptionRepository();
            configurationContext.setProperty(
                    ServiceBusConstants.CARBON_TASK_REPOSITORY, repository);
        } else {
            repository = (TaskDescriptionRepository)
                    configurationContext.getProperty(ServiceBusConstants.CARBON_TASK_REPOSITORY);
        }
        contextInfo.addProperty(TaskConstants.TASK_DESCRIPTION_REPOSITORY, repository);

        /* if (secretCallbackHandlerService != null) {
            contextInfo.addProperty(SecurityConstants.PROP_SECRET_CALLBACK_HANDLER,
                    secretCallbackHandlerService.getSecretCallbackHandler());
        }*/

        AxisConfiguration axisConf = configurationContext.getAxisConfiguration();
            axisConf.addParameter(new Parameter(
                    ServiceBusConstants.SYNAPSE_CURRENT_CONFIGURATION,
                    configurationName));

        serverManager.init(configurationInformation, contextInfo);
        serverManager.start();

        AxisServiceGroup serviceGroup = axisConf.getServiceGroup(
                SynapseConstants.SYNAPSE_SERVICE_NAME);
        serviceGroup.addParameter("hiddenService", "true");

        addDeployers(configurationContext,contextInfo);

        return contextInfo;
    }

    /**
     * Create the file system for holding the synapse configuration for a new tanent.
     * @param synapseConfigDir configuration directory where synapse configuration is created
     * @param tenantDomain name of the tenent
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void createTenantSynapseConfigHierarchy(File synapseConfigDir, String tenantDomain) {

        if (!synapseConfigDir.mkdir()) {
            log.fatal("Couldn't create the synapse-config root on the file system " +
                    "for the tenant domain : " + tenantDomain);
            return;
        }

        File sequencesDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.SEQUENCES_DIR);
        File endpointsDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.ENDPOINTS_DIR);
        File entriesDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.LOCAL_ENTRY_DIR);
        File proxyServicesDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR);
        File eventSourcesDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.EVENTS_DIR);
        File tasksDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.TASKS_DIR);
        File executorsDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.EXECUTORS_DIR);

        if(!sequencesDir.mkdir()) {
            log.warn("Could not create " + sequencesDir);
        }
        if(!endpointsDir.mkdir()) {
            log.warn("Could not create " + endpointsDir);
        }
        if(!entriesDir.mkdir()) {
            log.warn("Could not create " + entriesDir);
        }
        if(!proxyServicesDir.mkdir()) {
            log.warn("Could not create " + proxyServicesDir);
        }
        if(!eventSourcesDir.mkdir()) {
            log.warn("Could not create " + eventSourcesDir);
        }
        if(!tasksDir.mkdir()) {
            log.warn("Could not create " + tasksDir);
        }
        if(!executorsDir.mkdir()) {
            log.warn("Could not create " + executorsDir);
        }

        SynapseConfiguration initialSynCfg = SynapseConfigurationBuilder.getDefaultConfiguration();
        SequenceMediator mainSequence = (SequenceMediator) initialSynCfg.getMainSequence();
        SequenceMediator faultSequence = (SequenceMediator) initialSynCfg.getFaultSequence();
        mainSequence.setFileName(SynapseConstants.MAIN_SEQUENCE_KEY + ".xml");
        faultSequence.setFileName(SynapseConstants.FAULT_SEQUENCE_KEY + ".xml");
        Registry registry = new WSO2Registry();
        initialSynCfg.setRegistry(registry);

        MultiXMLConfigurationSerializer serializer
                = new MultiXMLConfigurationSerializer(synapseConfigDir.getAbsolutePath());
        try {
            serializer.serializeSequence(mainSequence, initialSynCfg, null);
            serializer.serializeSequence(faultSequence, initialSynCfg, null);
            serializer.serializeSynapseRegistry(registry, initialSynCfg, null);
        } catch (Exception e) {
            handleException("Couldn't serialise the initial synapse configuration " +
                    "for the domain : " + tenantDomain, e);
        }
    }

    private void addDeployers(ConfigurationContext configurationContext,ServerContextInformation contextInfo) {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        String carbonRepoPath = configurationContext.getAxisConfiguration().getRepository().getFile();
       
        String mediatorsPath = carbonRepoPath + File.separator + "mediators";
        String extensionsPath = carbonRepoPath + File.separator + "extensions";
        ExtensionDeployer deployer = new ExtensionDeployer();
        deploymentEngine.addDeployer(deployer, mediatorsPath, "xar");
        deploymentEngine.addDeployer(deployer, extensionsPath, "xar");
        deploymentEngine.addDeployer(deployer, mediatorsPath, "jar");
        deploymentEngine.addDeployer(deployer, extensionsPath, "jar");
     }
    
    
    /**
	 * Checks whether the given repository already existing.
	 * 
	 * @return
	 */
	protected boolean isRepoExists(org.wso2.carbon.registry.core.Registry registry) {
		try {
			registry.get(ServiceBusConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY);
		} catch (RegistryException e) {
			return false;
		}
		return true;
	}

	public String getProviderClass() {
		return this.getClass().getName();
	}
	

    public static boolean isRunningSamplesMode() {
        return System.getProperty(ServiceBusConstants.ESB_SAMPLE_SYSTEM_PROPERTY) != null;
    }

    private void handleFatal(String message) {
        log.fatal(message);
    }

    private void handleFatal(String message, Exception e) {
        log.fatal(message, e);
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
    }
}
