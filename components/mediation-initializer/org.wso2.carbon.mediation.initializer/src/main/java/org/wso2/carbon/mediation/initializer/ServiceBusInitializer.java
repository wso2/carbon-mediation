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

package org.wso2.carbon.mediation.initializer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.*;
import org.apache.synapse.commons.datasource.DataSourceConstants;
import org.apache.synapse.commons.datasource.DataSourceInformationRepository;
import org.apache.synapse.registry.Registry;
import org.apache.synapse.registry.RegistryEntry;
import org.apache.synapse.task.TaskConstants;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.synapse.task.TaskScheduler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.ServerShutdownHandler;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationManager;
import org.wso2.carbon.mediation.initializer.multitenancy.TenantServiceBusInitializer;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.*;
import org.wso2.carbon.mediation.initializer.utils.ConfigurationHolder;
import org.wso2.carbon.mediation.registry.ESBRegistryConstants;
import org.wso2.carbon.mediation.registry.services.SynapseRegistryService;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.task.services.TaskDescriptionRepositoryService;
import org.wso2.carbon.task.services.TaskSchedulerService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.securevault.SecurityConstants;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @scr.component name="esb.core.initializer" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="application.manager"
 * interface="org.wso2.carbon.application.deployer.service.ApplicationManagerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setAppManager" unbind="unsetAppManager"
 * @scr.reference name="synapse.registry.service"
 * interface="org.wso2.carbon.mediation.registry.services.SynapseRegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSynapseRegistryService" unbind="unsetSynapseRegistryService"
 * @scr.reference name="datasource.information.repository.service"
 * interface="org.wso2.carbon.datasource.DataSourceInformationRepositoryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setDataSourceInformationRepositoryService"
 * unbind="unsetDataSourceInformationRepositoryService"
 * @scr.reference name="task.description.repository.service"
 * interface="org.wso2.carbon.task.services.TaskDescriptionRepositoryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setTaskDescriptionRepositoryService" unbind="unsetTaskDescriptionRepositoryService"
 * @scr.reference name="task.scheduler.service"
 * interface="org.wso2.carbon.task.services.TaskSchedulerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setTaskSchedulerService" unbind="unsetTaskSchedulerService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 * @scr.reference name="config.tracking.service"
 * interface="org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigTrackingService" unbind="unsetConfigTrackingService"
 * @scr.reference name="eventbroker.service"
 * interface="org.wso2.carbon.event.core.EventBroker" cardinality="1..1"
 * policy="dynamic" bind="setEventBroker" unbind="unSetEventBroker"
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
public class ServiceBusInitializer {

    private static final Log log = LogFactory.getLog(ServiceBusInitializer.class);

    private static RegistryService registryService;
    private static ConfigurationTrackingService configTrackingService;
    private static ServerConfigurationInformation configurationInformation;
    private static ApplicationManagerService applicationManager;

    private static String configPath;
    private ConfigurationContextService configCtxSvc;
    private SynapseRegistryService synRegSvc;
    private DataSourceInformationRepositoryService dataSourceInformationRepositoryService;
    private TaskDescriptionRepositoryService repositoryService;
    private TaskSchedulerService taskSchedulerService;
    private SecretCallbackHandlerService secretCallbackHandlerService;
    private static EventBroker eventBroker;

    private ServerManager serverManager;

    protected void activate(ComponentContext ctxt) {

        log.info("Starting ESB...");

        // FIXME: this is a hack to get rid of the https port retrieval from the axis2
        // configuration returning the non blocking https transport. Ideally we should be able
        // to fix this by making it possible to let the authentication of carbon be done through
        // the non blocking https transport
        setHttpsProtForConsole();
        
      //clean up temp folder created for connector class loader reference
        String javaTempDir = System.getProperty("java.io.tmpdir");
        String APP_UNZIP_DIR = javaTempDir.endsWith(File.separator) ?
                        javaTempDir + "libs" :
                        javaTempDir + File.separator + "libs";
        cleanupTempDirectory(APP_UNZIP_DIR);
        
        try {
            BundleContext bndCtx = ctxt.getBundleContext();
            ConfigurationHolder.getInstance().setBundleContext(bndCtx);

            TenantServiceBusInitializer listener = new TenantServiceBusInitializer();
            bndCtx.registerService(
                    Axis2ConfigurationContextObserver.class.getName(), listener, null);

            // initialize the lock
            Lock lock = new ReentrantLock();
            configCtxSvc.getServerConfigContext().getAxisConfiguration().addParameter(
                    ServiceBusConstants.SYNAPSE_CONFIG_LOCK, lock);

            // first check which configuration should be active
            UserRegistry registry = registryService.getConfigSystemRegistry();

            // init the multiple configuration tracker
            ConfigurationManager configurationManager =
                    new ConfigurationManager(registry, configCtxSvc.getServerConfigContext());
            configurationManager.init();

            // set the event broker as a property
            if (eventBroker != null) {
                configCtxSvc.getServerConfigContext().setProperty("mediation.event.broker", eventBroker);
            }

            // Initialize Synapse
            ServerContextInformation contextInfo = initESB(configurationManager.
                    getTracker().getCurrentConfigurationName());

            ServiceRegistration synCfgRegistration = null;
            ServiceRegistration synEnvRegistration = null;

            if (contextInfo.getSynapseConfiguration() != null) {

                //Properties props = new Properties();
                SynapseConfigurationService synCfgSvc
                        = new SynapseConfigurationServiceImpl(contextInfo.getSynapseConfiguration(),
                        MultitenantConstants.SUPER_TENANT_ID, configCtxSvc.getServerConfigContext());
                synCfgRegistration = bndCtx.registerService(
                        SynapseConfigurationService.class.getName(), synCfgSvc, null);

                initPersistence(synCfgSvc,
                        configurationManager.getTracker().getCurrentConfigurationName());
                bndCtx.registerService(
                        ServerShutdownHandler.class.getName(),
                        new MPMShutdownHandler(
                                synCfgSvc.getSynapseConfiguration().getAxisConfiguration()),
                        null);

                if (log.isDebugEnabled()) {
                    log.debug("SynapseConfigurationService Registered");
                }

                if (configTrackingService != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Publishing the SynapseConfiguration to the " +
                                "ConfigurationTrackingService");
                    }
                    configTrackingService.setSynapseConfiguration(contextInfo.
                            getSynapseConfiguration());
                }

            } else {
                handleFatal("Couldn't register the SynapseConfigurationService, " +
                        "SynapseConfiguration not found");
            }

            if (contextInfo.getSynapseEnvironment() != null) {

                //Properties props = new Properties();
                SynapseEnvironmentService synEnvSvc
                        = new SynapseEnvironmentServiceImpl(contextInfo.getSynapseEnvironment(),
                        MultitenantConstants.SUPER_TENANT_ID, configCtxSvc.getServerConfigContext());
                synEnvRegistration = bndCtx.registerService(
                        SynapseEnvironmentService.class.getName(), synEnvSvc, null);

                if (log.isDebugEnabled()) {
                    log.debug("SynapseEnvironmentService Registered");
                }

            } else {
                handleFatal("Couldn't register the SynapseEnvironmentService, " +
                        "SynapseEnvironment not found");
            }

            //Properties props = new Properties();
            SynapseRegistrationsService synRegistrationsSvc
                    = new SynapseRegistrationsServiceImpl(synCfgRegistration, synEnvRegistration,
                    MultitenantConstants.SUPER_TENANT_ID, configCtxSvc.getServerConfigContext());
            bndCtx.registerService(SynapseRegistrationsService.class.getName(),
                    synRegistrationsSvc, null);

            configCtxSvc.getServerConfigContext().setProperty(
                    ConfigurationManager.CONFIGURATION_MANAGER, configurationManager);
            
                        
        } catch (Exception e) {
            handleFatal("Couldn't initialize the ESB...", e);
        } catch (Throwable t) {
            log.fatal("Failed to initialize ESB due to a fatal error", t);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        serverManager.stop();
        serverManager.shutdown();
    }

    private void initPersistence(SynapseConfigurationService synCfgSvc, String configName)
            throws RegistryException, AxisFault {
        // Initialize the mediation persistence manager if required
        ServerConfiguration serverConf = ServerConfiguration.getInstance();
        String persistence = serverConf.getFirstProperty(ServiceBusConstants.PERSISTENCE);

        // Check whether persistence is disabled
        if (!ServiceBusConstants.DISABLED.equals(persistence)) {
            // Check registry persistence is disabled or not
            String regPersistence = serverConf.getFirstProperty(
                    ServiceBusConstants.REGISTRY_PERSISTENCE);
            UserRegistry registry = ServiceBusConstants.ENABLED.equals(regPersistence) ?
                    registryService.getConfigSystemRegistry() : null;

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

            // Finally init the persistence manager
            MediationPersistenceManager pm = new MediationPersistenceManager(registry,
                    configurationInformation.getSynapseXMLLocation(),
                    synCfgSvc.getSynapseConfiguration(), intervalInMillis, configName);

            configCtxSvc.getServerConfigContext().getAxisConfiguration().addParameter(new Parameter(
                    ServiceBusConstants.PERSISTENCE_MANAGER, pm));
        } else {
            log.info("Persistence for mediation configuration is disabled");
        }
    }

    private void setHttpsProtForConsole() {

        ServerConfiguration config = ServerConfiguration.getInstance();
        if (CarbonUtils.isRunningInStandaloneMode()) {
            // Try to get the port information from the Carbon TransportManager
            // -- Standalone Mode --
            final String TRANSPORT_MANAGER
                    = "org.wso2.carbon.tomcat.ext.transport.ServletTransportManager";
            try {
                Class transportManagerClass = Class.forName(TRANSPORT_MANAGER);
                Object transportManager = transportManagerClass.newInstance();
                Method method = transportManagerClass.getMethod("getPort", String.class);
                int httpsPort = (Integer) method.invoke(transportManager, "https");
                int httpPort = (Integer) method.invoke(transportManager, "http");


                // required to properly log the management console URL
                System.setProperty("carbon.https.port", Integer.toString(httpsPort));
                System.setProperty("carbon.http.port", Integer.toString(httpPort));

                System.setProperty("httpPort", Integer.toString(httpPort));
                System.setProperty("httpsPort", Integer.toString(httpsPort));
                // this is required for the dashboard to work
                config.setConfigurationProperty("RegistryHttpPort", Integer.toString(httpPort));
            } catch (ClassNotFoundException e) {
                log.error("Failed to load the transport manager class using reflection", e);
            } catch (Exception e) {
                log.error("failed to set ports http/https",e);
            }
        } else {
            // Try to get the port information from the carbon.xml
            // -- Webapp Deployment Mode --
            if (log.isDebugEnabled()) {
                log.debug("TransportManager implementation not found. Switching to " +
                        "webapp deployment mode. Reading HTTPS port from the carbon.xml.");
            }

            String serverURL = config.getFirstProperty("ServerURL");
            if (serverURL != null) {
                try {
                    URL url = new URL(serverURL);
                    if ("https".equals(url.getProtocol())) {
                        System.setProperty("carbon.https.port", String.valueOf(url.getPort()));
                    } else {
                        log.warn("Invalid protocol " + url.getProtocol() + " in Carbon server URL");
                    }
                } catch (MalformedURLException ex) {
                    log.error("Error while parsing the server URL " + serverURL, ex);
                }
            } else {
                log.warn("Server URL is not specified in the carbon.xml. Unable to " +
                        "set the HTTPS port as a system property");
            }

        }
    }

    private ServerContextInformation initESB(String name) throws AxisFault {

        if (configCtxSvc != null && synRegSvc != null) {
            ConfigurationContext configContext = configCtxSvc.getServerConfigContext();

            log.info("Initializing Apache Synapse...");
            configurationInformation =
                    ServerConfigurationInformationFactory.createServerConfigurationInformation(
                            configContext.getAxisConfiguration());
            // ability to specify the SynapseServerName as a system property
            if (System.getProperty("SynapseServerName") != null) {
                configurationInformation.setServerName(System.getProperty("SynapseServerName"));
            }

            // for now we override the default configuration location with the value in registry
            String synapseConfigsLocation = configurationInformation.getSynapseXMLLocation();
            if (synapseConfigsLocation != null) {
                configurationInformation.setSynapseXMLLocation(
                        synapseConfigsLocation + File.separator + name);
            } else {
                configurationInformation.setSynapseXMLLocation(
                        ServiceBusConstants.DEFAULT_SYNAPSE_CONFIGS_LOCATION + name);
            }

            configurationInformation.setCreateNewInstance(false);
            configurationInformation.setServerControllerProvider(
                    CarbonSynapseController.class.getName());
            if (isRunningSamplesMode()) {
                configurationInformation.setSynapseXMLLocation("repository" + File.separator
                        + "samples" + File.separator + "synapse_sample_" + System.getProperty(
                        ServiceBusConstants.ESB_SAMPLE_SYSTEM_PROPERTY) + ".xml");
            }

            serverManager = new ServerManager();
            ServerContextInformation contextInfo = new ServerContextInformation(configContext,
                    configurationInformation);

            if (dataSourceInformationRepositoryService != null) {
                DataSourceInformationRepository repository =
                        dataSourceInformationRepositoryService.getDataSourceInformationRepository();
                contextInfo.addProperty(DataSourceConstants.DATA_SOURCE_INFORMATION_REPOSITORY,
                        repository);
            }

            if (taskSchedulerService != null) {
                TaskScheduler scheduler = taskSchedulerService.getTaskScheduler();
                contextInfo.addProperty(TaskConstants.TASK_SCHEDULER, scheduler);
            }

            if (repositoryService != null) {
                TaskDescriptionRepository repository
                        = repositoryService.getTaskDescriptionRepository();
                contextInfo.addProperty(TaskConstants.TASK_DESCRIPTION_REPOSITORY, repository);
            }

            if (secretCallbackHandlerService != null) {
                contextInfo.addProperty(SecurityConstants.PROP_SECRET_CALLBACK_HANDLER,
                        secretCallbackHandlerService.getSecretCallbackHandler());
            }

            AxisConfiguration axisConf = configContext.getAxisConfiguration();
            axisConf.addParameter(
                    new Parameter(ServiceBusConstants.SYNAPSE_CURRENT_CONFIGURATION, name));

            serverManager.init(configurationInformation, contextInfo);
            serverManager.start();


            AxisServiceGroup serviceGroup = axisConf.getServiceGroup(
                    SynapseConstants.SYNAPSE_SERVICE_NAME);
            serviceGroup.addParameter("hiddenService", "true");

            return contextInfo;

        } else {
            handleFatal("Couldn't initialize Synapse, " +
                    "ConfigurationContext service or SynapseRegistryService is not available");
        }

        // never executes, but keeps the compiler happy
        return null;
    }

    private boolean isDefaultRegistryStructureCreated() {
        if (registryService != null) {
            try {
                UserRegistry registry = registryService.getConfigSystemRegistry();
                if (registry.resourceExists(ServiceBusConstants.META_INF_REGISTRY_PATH)) {
                    Resource resource = registry.get(ServiceBusConstants.META_INF_REGISTRY_PATH);
                    if (resource != null && ServiceBusConstants.STRUCTURE_CREATED.equals(resource
                            .getProperty(ServiceBusConstants.DEFAULT_COLLECTIONS_PROPERTY))) {
                        if (log.isDebugEnabled()) {
                            log.debug("Default Registry structure of ESB has already been created");
                        }
                        return true;
                    }
                }
            } catch (RegistryException rege) {
                log.warn("Cannot determine whether the default registry structure " +
                        "of the ESB is created or not.", rege);
                return true;
            }
        }
        return false;
    }

    public static boolean isRunningSamplesMode() {
        return System.getProperty(ServiceBusConstants.ESB_SAMPLE_SYSTEM_PROPERTY) != null;
    }

    private void createDefaultRegistryStructure(Registry registry) {

        String[] defaultCollections = ServerConfiguration.getInstance().getProperties(
                ServiceBusConstants.DEFAULT_ESBREGISTRY_ITEM);
        if (defaultCollections.length == 0) {
            defaultCollections = new String[]{"esb-resources/endpoints", "esb-resources/sequences",
                    "esb-resources/policy", "esb-resources/schema", "esb-resources/scripts",
                    "esb-resources/wsdl", "esb-resources/xslt"};
        }

        for (String collectionName : defaultCollections) {
            RegistryEntry collectionEntry = registry.getRegistryEntry(collectionName);
            if (collectionEntry != null
                    && ESBRegistryConstants.FOLDER.equals(collectionEntry.getType())) {
                if (log.isDebugEnabled()) {
                    log.debug("Collection named " + collectionName + " is there on " +
                            "the ESB registry, collection creation skipped");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Creating the collection "
                            + collectionName + " on the ESB registry");
                }
                registry.newResource(collectionName, true);
            }
        }

        try {
            UserRegistry systemRegistry = registryService.getConfigSystemRegistry();
            if (systemRegistry.resourceExists(ServiceBusConstants.META_INF_REGISTRY_PATH)) {
                Resource resource = systemRegistry.get(ServiceBusConstants.META_INF_REGISTRY_PATH);
                resource.addProperty(ServiceBusConstants.DEFAULT_COLLECTIONS_PROPERTY,
                        ServiceBusConstants.STRUCTURE_CREATED);
            } else {
                CollectionImpl collection = new CollectionImpl();
                collection.setPath(ServiceBusConstants.META_INF_REGISTRY_PATH);
                collection.setProperty(ServiceBusConstants.DEFAULT_COLLECTIONS_PROPERTY,
                        ServiceBusConstants.STRUCTURE_CREATED);
                systemRegistry.put(ServiceBusConstants.META_INF_REGISTRY_PATH, collection);
            }
        } catch (RegistryException rege) {
            log.warn("Couldn't persist the default ESB collections structure created action", rege);
        }
    }

    protected void setRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the ESB initialization process");
        }
        registryService = regService;
    }

    protected void unsetRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the ESB environment");
        }
        registryService = null;
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService bound to the ESB initialization process");
        }
        this.configCtxSvc = configurationContextService;
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unbound from the ESB environment");
        }
        this.configCtxSvc = null;
    }

    protected void setSynapseRegistryService(SynapseRegistryService synapseRegistryService) {
        if (log.isDebugEnabled()) {
            log.debug("SynapseRegistryService bound to the ESB initialization process");
        }
        this.synRegSvc = synapseRegistryService;
    }

    protected void unsetSynapseRegistryService(SynapseRegistryService synapseRegistryService) {
        if (log.isDebugEnabled()) {
            log.debug("SynapseRegistryService unbound from the ESB environment");
        }
        this.synRegSvc = null;
    }

    protected void setDataSourceInformationRepositoryService(
            DataSourceInformationRepositoryService repositoryService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService " +
                    "bound to the ESB initialization process");
        }
        this.dataSourceInformationRepositoryService = repositoryService;
    }

    protected void unsetDataSourceInformationRepositoryService(
            DataSourceInformationRepositoryService repositoryService) {

        if (log.isDebugEnabled()) {
            log.debug("DataSourceInformationRepositoryService unbound from the ESB environment");
        }
        this.dataSourceInformationRepositoryService = null;
    }

    protected void setTaskDescriptionRepositoryService(
            TaskDescriptionRepositoryService repositoryService) {
        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService bound to the ESB initialization process");
        }
        this.repositoryService = repositoryService;
    }

    protected void unsetTaskDescriptionRepositoryService(
            TaskDescriptionRepositoryService repositoryService) {
        if (log.isDebugEnabled()) {
            log.debug("TaskDescriptionRepositoryService unbound from the ESB environment");
        }
        this.repositoryService = null;
    }

    protected void setTaskSchedulerService(
            TaskSchedulerService schedulerService) {
        if (log.isDebugEnabled()) {
            log.debug("TaskSchedulerService bound to the ESB initialization process");
        }
        this.taskSchedulerService = schedulerService;
    }

    protected void unsetTaskSchedulerService(
            TaskSchedulerService schedulerService) {
        if (log.isDebugEnabled()) {
            log.debug("TaskSchedulerService unbound from the ESB environment");
        }
        this.taskSchedulerService = null;
    }

    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService bound to the ESB initialization process");
        }
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService unbound from the ESB environment");
        }
        this.secretCallbackHandlerService = null;
    }

    protected void setAppManager(ApplicationManagerService appService) {
        if (log.isDebugEnabled()) {
            log.debug("CarbonApplicationService bound to the ESB initialization process");
        }
        applicationManager = appService;
    }

    protected void unsetAppManager(ApplicationManagerService appService) {
        if (log.isDebugEnabled()) {
            log.debug("CarbonApplicationService unbound from the ESB environment");
        }
        applicationManager = null;
    }

    protected void setConfigTrackingService(ConfigurationTrackingService configTrackingService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationTrackingService bound to the ESB initialization process");
        }
        ServiceBusInitializer.configTrackingService = configTrackingService;
    }

    protected void unsetConfigTrackingService(ConfigurationTrackingService configTrackingService) {
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationTrackingService unbound from the ESB environment");
        }
        ServiceBusInitializer.configTrackingService = null;
    }

    protected void setEventBroker(EventBroker eventBroker) {
        ServiceBusInitializer.eventBroker = eventBroker;
    }

    protected void unSetEventBroker(EventBroker eventBroker) {
        ServiceBusInitializer.eventBroker = null;
    }

    public static EventBroker getEventBroker() {
        return eventBroker;
    }

    public static ConfigurationTrackingService getConfigurationTrackingService() {
        return configTrackingService;
    }

    public static ApplicationManagerService getAppManager() {
        if (applicationManager == null) {
            String msg = "Before activating Mediation initializer service bundle, an instance of "
                    + "CarbonApplicationService should be in existance";
            log.error(msg);
        }
        return applicationManager;
    }

    public static ServerConfigurationInformation getConfigurationInformation() {
        return configurationInformation;
    }

    protected static RegistryService getRegistryService() {
        return registryService;
    }

    protected static ServerConfigurationInformation getServerConfigurationInformation() {
        return configurationInformation;
    }

    private void handleFatal(String message) {
        log.fatal(message);
        // Do not do this -- throw new RuntimeException(message);
        // it causes the OSGi environment to reinitialize synapse which will result in a looping
    }

    private void handleFatal(String message, Exception e) {
        log.fatal(message, e);
        // Do not do this -- throw new RuntimeException(message, e);
        // it causes the OSGi environment to reinitialize synapse which will result in a looping
    }

    public class MPMShutdownHandler implements ServerShutdownHandler {
        private AxisConfiguration configuration;

        public MPMShutdownHandler(AxisConfiguration configuration) {
            this.configuration = configuration;
        }

        public void invoke() {
            Parameter p = configuration.getParameter(
                    ServiceBusConstants.PERSISTENCE_MANAGER);
            if (p != null && p.getValue() instanceof MediationPersistenceManager) {
                ((MediationPersistenceManager) p.getValue()).destroy();
            }
        }
    }
    
    /**
	 * Clean up temp files
	 * 
	 * @param appUnzipDir
	 */
	private static void cleanupTempDirectory(String appUnzipDir) {
	    File tempDirector = new File(appUnzipDir);
	    if (tempDirector.isDirectory()) {
			File[] entries = tempDirector.listFiles();
			int size = entries.length;
			for(int i = 0 ; i<size ; i++){
				try {
	                FileUtils.deleteDirectory(entries[i]);
                } catch (IOException e) {
                	log.warn("Could not build lib artifact for path : " + entries[i].getAbsolutePath());
                }
			}
	    }
    }
}
