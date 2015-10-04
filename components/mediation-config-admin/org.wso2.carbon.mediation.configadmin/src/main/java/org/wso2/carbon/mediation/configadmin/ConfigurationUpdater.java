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

package org.wso2.carbon.mediation.configadmin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerContextInformation;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.config.xml.XMLConfigurationBuilder;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.libraries.model.Library;
import org.apache.synapse.libraries.util.LibDeployerUtils;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.rest.API;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.synapse.task.TaskScheduler;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.configadmin.util.ConfigHolder;
import org.wso2.carbon.mediation.dependency.mgt.services.ConfigurationTrackingService;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationInitilizerException;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationManager;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.initializer.services.SynapseRegistrationsService;
import org.wso2.carbon.proxyadmin.observer.ProxyServiceParameterObserver;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Use an instance of this class to update the existing SynapseConfiguration of a server. This will
 * gracefully cleanup the existing configuration and apply the provided new configuration. In
 * case of an error, it will fallback to the old configuration. This class is not thread safe and
 * hence should not be used by multiple threads.
 */
public class ConfigurationUpdater {

    private static final Log log = LogFactory.getLog(ConfigurationUpdater.class);

    private ServerContextInformation serverContextInformation;
    private ConfigurationContext configurationContext;
    private MediationPersistenceManager persistenceManager;
    private UserRegistry configRegistry;

    private final SynapseConfiguration currentConfig;
    private SynapseEnvironment currentEnvironment;
    private AxisConfiguration axisCfg;

    private TaskDescriptionRepository taskRepository;
    private TaskScheduler taskScheduler;
    private static final String XML = ".xml";

    public ConfigurationUpdater(ServerContextInformation serverContextInformation,
                                ConfigurationContext configurationContext,
                                MediationPersistenceManager persistenceManager,
                                UserRegistry configRegistry) {

        this.serverContextInformation = serverContextInformation;
        this.configurationContext = configurationContext;
        this.persistenceManager = persistenceManager;
        this.configRegistry = configRegistry;

        currentConfig = serverContextInformation.getSynapseConfiguration();
        currentEnvironment = serverContextInformation.getSynapseEnvironment();
        axisCfg = currentConfig.getAxisConfiguration();
    }

    public void update(OMElement configElement) throws AxisFault {
        synchronized (currentConfig) {
            cleanupCurrentConfiguration();
        }

        SynapseConfiguration newConfig = null;
        try {
            newConfig = createNewConfiguration(configElement);
        } catch (Exception e) {
            // Something went wrong while constructing the new SynapseConfiguration -
            // Fall back to the old one...
            newConfig = currentConfig;
        } finally {
            synchronized (newConfig) {
                prepareNewConfiguration(newConfig);
            }
        }
    }

    /**
     * Stops the mediation persistence manager, cleans up the Synapse task scheduler and
     * destroys the current SynapseConfiguration. This method is considered to be failure
     * proof and hence does not throw any checked exceptions. Any faults encountered by
     * this method are due to programming errors and therefore they are translated into
     * instances of RuntimeException.
     */
    private void cleanupCurrentConfiguration() {
        persistenceManager.destroy();

        // We must get references to following task objects at this point
        // Once the SynapseConfiguration is destroyed they become unreachable
        if (currentEnvironment.getTaskManager().isInitialized()) {
            taskRepository = currentEnvironment.getTaskManager().getTaskDescriptionRepository();
            taskScheduler = currentEnvironment.getTaskManager().getTaskScheduler();
            TaskDescriptionRepository repository = currentEnvironment.getTaskManager()
                    .getTaskDescriptionRepository();
            if (repository != null) {
                repository.clear();
            }
            currentEnvironment.getTaskManager().cleanup();
        }

        log.trace("Stopping and undeploying proxy services");
        for (ProxyService proxyService : currentConfig.getProxyServices()) {
            proxyService.stop(currentConfig);
            safeRemoveService(proxyService.getName());
        }

        for (SequenceMediator sequence : currentConfig.getDefinedSequences().values()) {
            sequence.destroy();
        }

        for (Endpoint endpoint : currentConfig.getDefinedEndpoints().values()) {
            endpoint.destroy();
        }

        log.trace("Stopping active scheduled tasks");
        for (Startup startup : currentConfig.getStartups()) {
            startup.destroy();
        }

        for (SynapseEventSource eventSource : currentConfig.getEventSources()) {
            safeRemoveService(eventSource.getName());
        }

        for (PriorityExecutor executor : currentConfig.getPriorityExecutors().values()) {
            executor.destroy();
        }

        for (MessageStore ms : currentConfig.getMessageStores().values()) {
            ms.destroy();
        }

        for (MessageProcessor mp : currentConfig.getMessageProcessors().values()) {
            mp.destroy();
        }

        for (TemplateMediator tm : currentConfig.getSequenceTemplates().values()) {
            tm.destroy();
        }

        log.trace("Successfully cleaned up the current SynapseConfiguration");
    }

    /**
     * Parses the given XML configuration and creates a new SynapseConfiguration instance.
     * This method follows general Synapse fail-safe rules to handle faults that are encountered
     * while creating the SynapseConfiguration. If fail-safe mode is enabled (default), this
     * method will not throw any exceptions. Otherwise it may throw runtime exceptions. It may
     * also throw an XMLStreamException if the provided XML configuration is malformed.
     *
     * @param configElement OMElement containing the Synapse XML configuration
     * @return a new SynapseConfiguration instance
     * @throws XMLStreamException if the provided OMElement is malformed
     */
    private SynapseConfiguration createNewConfiguration(OMElement configElement)
            throws XMLStreamException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        configElement.serialize(stream);
        return XMLConfigurationBuilder.getConfiguration(
                new ByteArrayInputStream(stream.toByteArray()), currentConfig.getProperties());
    }

    private void prepareNewConfiguration(SynapseConfiguration newConfig) throws AxisFault {
        newConfig.setPathToConfigFile(currentConfig.getPathToConfigFile());
        newConfig.addEntry(SynapseConstants.SERVER_HOST,
                currentConfig.getEntryDefinition(SynapseConstants.SERVER_HOST));
        newConfig.addEntry(SynapseConstants.SERVER_IP,
                currentConfig.getEntryDefinition(SynapseConstants.SERVER_IP));

        // Check for the main sequence and add a default main sequence if not present
        if (newConfig.getMainSequence() == null) {
            SynapseConfigUtils.setDefaultMainSequence(newConfig);
        }

        // Check for the fault sequence and add a default fault sequence if not present
        if (newConfig.getFaultSequence() == null) {
            SynapseConfigUtils.setDefaultFaultSequence(newConfig);
        }

        // The show must go on - So set the file names to the new configuration
        setFileNames(newConfig);

        newConfig.setAxisConfiguration(axisCfg);
        Parameter synapseCtxParam = new Parameter(SynapseConstants.SYNAPSE_CONFIG, null);
        synapseCtxParam.setValue(newConfig);
        MessageContextCreatorForAxis2.setSynConfig(newConfig);

        //set up synapse env
        Axis2SynapseEnvironment synEnv = new Axis2SynapseEnvironment(
                configurationContext, newConfig, serverContextInformation);
        Parameter synapseEnvParam = new Parameter(SynapseConstants.SYNAPSE_ENV, null);
        synapseEnvParam.setValue(synEnv);
        MessageContextCreatorForAxis2.setSynEnv(synEnv);

        try {
            axisCfg.addParameter(synapseCtxParam);
            axisCfg.addParameter(synapseEnvParam);
        } catch (AxisFault axisFault) {
            // This condition should not occur unless there is a programming error
            handleException("Error while adding SynapseConfiguration and/or SynapseEnvironment " +
                    "to the AxisConfiguration", axisFault);
        }

        synEnv.getTaskManager().init(taskRepository, taskScheduler, newConfig.getTaskManager());
        Parameter suspendPersistence = new Parameter(ServiceBusConstants.SUSPEND_PERSISTENCE, "true");
        try {
            axisCfg.addParameter(suspendPersistence);
            deployServices(newConfig);
        } finally {
            serverContextInformation.setSynapseConfiguration(newConfig);
            serverContextInformation.setSynapseEnvironment(synEnv);
            newConfig.init(synEnv);
            synEnv.setInitialized(true);
            axisCfg.removeParameter(suspendPersistence);

            initPersistence(newConfig);
            publishConfiguration(newConfig, synEnv);
        }
    }

    /**
     * This method deploys the proxy services and event sources available in the provided
     * SynapseConfiguration. If an error occurs while deploying a service, this method will
     * follow the Synapse fail-safe rules. In case fail-safe is disabled, and an exception
     * occurs, no service beyond that point will be deployed.
     *
     * @param newConfig SynapseConfiguration with the services to be initialized
     * @throws AxisFault if an error occurs while deploying a service
     */
    private void deployServices(SynapseConfiguration newConfig) throws AxisFault {


        for (ProxyService proxyService : newConfig.getProxyServices()) {
            try {
                AxisService axisService = proxyService.buildAxisService(newConfig, axisCfg);
                ProxyServiceParameterObserver paramObserver =
                        new ProxyServiceParameterObserver(axisService);
                axisService.addParameterObserver(paramObserver);
                if (log.isDebugEnabled()) {
                    log.debug("Deployed Proxy service : " + proxyService.getName());
                }
                if (!proxyService.isStartOnLoad()) {
                    proxyService.stop(newConfig);
                }
            } catch (Exception e) {
                handleServiceInitializationError(proxyService.getName(),
                        SynapseConstants.FAIL_SAFE_MODE_PROXY_SERVICES, e);
            }
        }

        for (SynapseEventSource eventSource : newConfig.getEventSources()) {
            try {
                eventSource.buildService(axisCfg);
            } catch (Exception e) {
                handleServiceInitializationError(eventSource.getName(),
                        SynapseConstants.FAIL_SAFE_MODE_EVENT_SOURCE, e);
            }
        }
    }

    private void handleServiceInitializationError(String serviceName, String type,
                                                  Exception e) throws AxisFault {
        if (SynapseConfigUtils.isFailSafeEnabled(type)) {
            log.warn("Error while initializing the service: " + serviceName + " - " +
                    "Continue in fail-safe mode", e);
        } else {
            String msg = "Error while initializing the service: " + serviceName;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private void initPersistence(SynapseConfiguration newConfig) {
        ConfigurationManager configurationManager = (ConfigurationManager)
                configurationContext.getProperty(ConfigurationManager.CONFIGURATION_MANAGER);
        try {
            ConfigurationUtils.initPersistence(newConfig,
                    configRegistry,
                    newConfig.getPathToConfigFile(),
                    axisCfg, configurationManager.getTracker().getCurrentConfigurationName());
        } catch (ConfigurationInitilizerException e) {
            log.error("Error while initializing mediation persistence", e);
        }
    }

    private void addToDeploymentStore(String parent, String fileName, String artifactName, SynapseConfiguration config) {
        if (fileName != null) {
            File file = new File(serverContextInformation.getServerConfigurationInformation().
                    getSynapseXMLLocation(), parent);
            fileName = file.getAbsolutePath() + File.separator + fileName;
            SynapseArtifactDeploymentStore store = config.getArtifactDeploymentStore();
            if (!store.containsFileName(fileName)) {
                store.addArtifact(fileName, artifactName);
            }
            store.addRestoredArtifact(fileName);
        }
    }

    private void setFileNames(SynapseConfiguration newConfig) {
        Map<String, Endpoint> endpoints = newConfig.getDefinedEndpoints();
        for (String name : endpoints.keySet()) {
            Endpoint newEndpoint = endpoints.get(name);
            Endpoint oldEndpoint = currentConfig.getDefinedEndpoints().get(name);
            if (oldEndpoint != null) {
                newEndpoint.setFileName(oldEndpoint.getFileName());
                newEndpoint.setArtifactContainerName(oldEndpoint.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.ENDPOINTS_DIR, oldEndpoint.getFileName(), name, newConfig);
            } else {
                if (currentConfig.getDefinedEndpoints().size() >= newConfig.getDefinedEndpoints().size()) {
                    newConfig.removeEndpoint(name);
                    log.error("Unable to update the endpoints.");
                    Map<String, Endpoint> oldEndpoints = currentConfig.getDefinedEndpoints();
                    for (String oldName : oldEndpoints.keySet()) {
                        if (newConfig.getDefinedEndpoints().get(oldName) == null) {
                            newConfig.addEndpoint(oldName, oldEndpoints.get(oldName));
                        }
                    }
                } else {
                    Map<String, Endpoint> oldEndpoints = currentConfig.getDefinedEndpoints();
                    for (String oldName : oldEndpoints.keySet()) {
                        if (newConfig.getDefinedEndpoints().get(oldName) == null) {
                            newConfig.addEndpoint(oldName, oldEndpoints.get(oldName));
                        }
                    }
                    log.info("The endpoint which you are updating is created as a new endpoint: " + name);
                    newEndpoint.setFileName(name + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.ENDPOINTS_DIR, newEndpoint.getFileName(), name, newConfig);
                }
            }
        }

        Map<String, SequenceMediator> sequences = newConfig.getDefinedSequences();
        for (String name : sequences.keySet()) {
            SequenceMediator oldSequence = currentConfig.getDefinedSequences().get(name);
            if (oldSequence != null) {
                sequences.get(name).setFileName(oldSequence.getFileName());
                sequences.get(name).setArtifactContainerName(oldSequence.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.SEQUENCES_DIR, oldSequence.getFileName(), name, newConfig);
            } else {
                if (currentConfig.getDefinedSequences().size() >= newConfig.getDefinedSequences().size()) {
                    newConfig.removeSequence(name);
                    log.error("Unable to update the sequences.");
                    Map<String, SequenceMediator> oldSequences = currentConfig.getDefinedSequences();
                    for (String oldName : oldSequences.keySet()) {
                        if (newConfig.getDefinedSequences().get(oldName) == null) {
                            newConfig.addSequence(oldName, oldSequences.get(oldName));
                        }
                    }
                } else {
                    Map<String, SequenceMediator> oldSequences = currentConfig.getDefinedSequences();
                    for (String oldName : oldSequences.keySet()) {
                        if (newConfig.getDefinedSequences().get(oldName) == null) {
                            newConfig.addSequence(oldName, oldSequences.get(oldName));
                        }
                    }
                    log.info("The sequence which you are updating is created as a new sequence: " + name);
                    SequenceMediator newSequence = sequences.get(name);
                    newSequence.setFileName(name + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.SEQUENCES_DIR, newSequence.getFileName(), name, newConfig);
                }
            }
        }

        Collection<ProxyService> proxyServices = newConfig.getProxyServices();
        for (ProxyService proxy : proxyServices) {
            ProxyService oldProxy = currentConfig.getProxyService(proxy.getName());
            if (oldProxy != null) {
                proxy.setFileName(oldProxy.getFileName());
                proxy.setArtifactContainerName(oldProxy.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR, oldProxy.getFileName(), proxy.getName(), newConfig);
            } else {
                if (currentConfig.getProxyServices().size() >= newConfig.getProxyServices().size()) {
                    newConfig.removeProxyService(proxy.getName());
                    log.error("Unable to update the proxy services.");
                    Collection<ProxyService> oldProxies = currentConfig.getProxyServices();
                    for (ProxyService eachOldProxy : oldProxies) {
                        if (newConfig.getProxyService(eachOldProxy.getName()) == null) {
                            newConfig.addProxyService(eachOldProxy.getName(), eachOldProxy);
                        }
                    }
                } else {
                    Collection<ProxyService> oldProxies = currentConfig.getProxyServices();
                    for (ProxyService eachOldProxy : oldProxies) {
                        if (newConfig.getProxyService(eachOldProxy.getName()) == null) {
                            newConfig.addProxyService(eachOldProxy.getName(), eachOldProxy);
                        }
                    }
                    log.info("The proxy service which you are updating is created as a new proxy service: " + proxy.getName());
                    proxy.setFileName(proxy.getName() + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR, proxy.getFileName(), proxy.getName(), newConfig);
                }
            }
        }

        Map<String, Entry> localEntries = newConfig.getDefinedEntries();
        for (String name : localEntries.keySet()) {
            Entry newEntry = localEntries.get(name);
            Entry oldEntry = currentConfig.getDefinedEntries().get(name);
            if (oldEntry != null) {
                newEntry.setFileName(oldEntry.getFileName());
                newEntry.setArtifactContainerName(oldEntry.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.LOCAL_ENTRY_DIR, oldEntry.getFileName(), name, newConfig);
            } else {
                if (currentConfig.getDefinedEntries().size() >= newConfig.getDefinedEntries().size()) {
                    newConfig.removeEntry(name);
                    log.error("Unable to update the local entries.");
                    Map<String, Entry> oldEntries = currentConfig.getDefinedEntries();
                    for (String oldName : oldEntries.keySet()) {
                        if (newConfig.getDefinedEntries().get(oldName) == null) {
                            newConfig.addEntry(oldName, oldEntries.get(oldName));
                        }
                    }
                } else {
                    Map<String, Entry> oldEntries = currentConfig.getDefinedEntries();
                    for (String oldName : oldEntries.keySet()) {
                        if (newConfig.getDefinedEntries().get(oldName) == null) {
                            newConfig.addEntry(oldName, oldEntries.get(oldName));
                        }
                    }
                    log.info("The local entry which you are updating is created as a new local entry: " + name);
                    newEntry.setFileName(name + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.LOCAL_ENTRY_DIR, newEntry.getFileName(), name, newConfig);
                }
            }
        }

        Collection<Startup> tasks = newConfig.getStartups();
        for (Startup task : tasks) {
            Startup oldTask = currentConfig.getStartup(task.getName());
            if (oldTask != null) {
                task.setFileName(oldTask.getFileName());
                task.setArtifactContainerName(oldTask.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.TASKS_DIR, oldTask.getFileName(), task.getName(), newConfig);
            } else {
                task.setFileName(task.getName() + XML);
                addToDeploymentStore(MultiXMLConfigurationBuilder.TASKS_DIR, task.getFileName(), task.getName(), newConfig);
            }
        }

        Collection<SynapseEventSource> eventSources = newConfig.getEventSources();
        for (SynapseEventSource eventSource : eventSources) {
            SynapseEventSource oldEventSource = currentConfig.getEventSource(eventSource.getName());
            if (oldEventSource != null) {
                eventSource.setFileName(oldEventSource.getFileName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.EVENTS_DIR, oldEventSource.getFileName(), eventSource.getName(), newConfig);
            } else {
                if (currentConfig.getEventSources().size() >= newConfig.getEventSources().size()) {
                    newConfig.removeEventSource(eventSource.getName());
                    log.error("Unable to update the event sources.");
                    Collection<SynapseEventSource> oldEventSources = currentConfig.getEventSources();
                    for (SynapseEventSource eachOldEventSource : oldEventSources) {
                        if (newConfig.getEventSource(eachOldEventSource.getName()) == null) {
                            newConfig.addEventSource(eachOldEventSource.getName(), eachOldEventSource);
                        }
                    }
                } else {
                    Collection<SynapseEventSource> oldEventSources = currentConfig.getEventSources();
                    for (SynapseEventSource eachOldEventSource : oldEventSources) {
                        if (newConfig.getEventSource(eachOldEventSource.getName()) == null) {
                            newConfig.addEventSource(eachOldEventSource.getName(), eachOldEventSource);
                        }
                    }
                    log.info("The event source which you are updating is created as a new event source: " + eventSource.getName());
                    eventSource.setFileName(eventSource.getName() + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.EVENTS_DIR, eventSource.getFileName(), eventSource.getName(), newConfig);
                }
            }
        }

        Collection<PriorityExecutor> executors = newConfig.getPriorityExecutors().values();
        for (PriorityExecutor exec : executors) {
            PriorityExecutor oldExec = currentConfig.getPriorityExecutors().get(exec.getName());
            if (oldExec != null) {
                exec.setFileName(oldExec.getFileName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.EXECUTORS_DIR, oldExec.getFileName(), exec.getName(), newConfig);
            } else {
                exec.setFileName(exec.getName() + XML);
                addToDeploymentStore(MultiXMLConfigurationBuilder.EXECUTORS_DIR, exec.getFileName(), exec.getName(), newConfig);
            }
        }

        Collection<MessageStore> messageStores = newConfig.getMessageStores().values();

        for (MessageStore store : messageStores) {
            MessageStore oldStore = currentConfig.getMessageStore(store.getName());
            if (oldStore != null) {
                store.setFileName(oldStore.getFileName());
                store.setArtifactContainerName(oldStore.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.MESSAGE_STORE_DIR, oldStore.getFileName(), store.getName(), newConfig);
            } else {
                if (currentConfig.getMessageStores().values().size() >= newConfig.getMessageStores().values().size()) {
                    newConfig.removeMessageStore(store.getName());
                    log.error("Unable to update the message stores.");
                    Collection<MessageStore> oldMessageStores = currentConfig.getMessageStores().values();
                    for (MessageStore eachOldStore : oldMessageStores) {
                        if (newConfig.getMessageStore(eachOldStore.getName()) == null) {
                            newConfig.addMessageStore(eachOldStore.getName(), eachOldStore);
                        }
                    }
                } else {
                    Collection<MessageStore> oldMessageStores = currentConfig.getMessageStores().values();
                    for (MessageStore eachOldStore : oldMessageStores) {
                        if (newConfig.getMessageStore(eachOldStore.getName()) == null) {
                            newConfig.addMessageStore(eachOldStore.getName(), eachOldStore);
                        }
                    }
                    log.info("The message store which you are updating is created as a new message store: " + store.getName());
                    store.setFileName(store.getName() + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.MESSAGE_STORE_DIR, store.getFileName(), store.getName(), newConfig);
                }
            }
        }

        Collection<MessageProcessor> messageProcessors = newConfig.getMessageProcessors().values();

        for (MessageProcessor processor : messageProcessors) {
            MessageProcessor oldProcessor =
                    currentConfig.getMessageProcessors().get(processor.getName());
            if (oldProcessor != null) {
                processor.setFileName(oldProcessor.getFileName());
                processor.setArtifactContainerName(oldProcessor.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.MESSAGE_PROCESSOR_DIR, oldProcessor.getFileName(), processor.getName(), newConfig);
            } else {
                if (currentConfig.getMessageProcessors().values().size() >= newConfig.getMessageProcessors().values().size()) {
                    newConfig.removeMessageProcessor(processor.getName());
                    log.error("Unable to update the message processors.");
                    Collection<MessageProcessor> oldMessageProcessors = currentConfig.getMessageProcessors().values();
                    for (MessageProcessor eachOldProcessor : oldMessageProcessors) {
                        if (newConfig.getMessageProcessors().get(eachOldProcessor.getName()) == null) {
                            newConfig.addMessageProcessor(eachOldProcessor.getName(), eachOldProcessor);
                        }
                    }
                } else {
                    Collection<MessageProcessor> oldMessageProcessors = currentConfig.getMessageProcessors().values();
                    for (MessageProcessor eachOldProcessor : oldMessageProcessors) {
                        if (newConfig.getMessageProcessors().get(eachOldProcessor.getName()) == null) {
                            newConfig.addMessageProcessor(eachOldProcessor.getName(), eachOldProcessor);
                        }
                    }
                    log.info("The message processor which you are updating is created as a new message processor: " + processor.getName());
                    processor.setFileName(processor.getName() + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.MESSAGE_PROCESSOR_DIR, processor.getFileName(), processor.getName(), newConfig);
                }
            }
        }

        Map<String, TemplateMediator> sequenceTemplates = newConfig.getSequenceTemplates();
        for (String name : sequenceTemplates.keySet()) {
            TemplateMediator oldSequenceTempl = currentConfig.getSequenceTemplates().get(name);
            if (oldSequenceTempl != null) {
                sequenceTemplates.get(name).setFileName(oldSequenceTempl.getFileName());
                sequenceTemplates.get(name).setArtifactContainerName(oldSequenceTempl.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.TEMPLATES_DIR, oldSequenceTempl.getFileName(), name, newConfig);
            } else {
                if (currentConfig.getSequenceTemplates().size() >= newConfig.getSequenceTemplates().size()) {
                    newConfig.removeSequenceTemplate(name);
                    log.error("Unable to update the sequence templates.");
                    Map<String, TemplateMediator> oldSequenceTemplates = currentConfig.getSequenceTemplates();
                    for (String oldName : oldSequenceTemplates.keySet()) {
                        if (newConfig.getSequenceTemplates().get(oldName) == null) {
                            newConfig.addSequenceTemplate(oldName, oldSequenceTemplates.get(oldName));
                        }
                    }
                } else {
                    Map<String, TemplateMediator> oldSequenceTemplates = currentConfig.getSequenceTemplates();
                    for (String oldName : oldSequenceTemplates.keySet()) {
                        if (newConfig.getSequenceTemplates().get(oldName) == null) {
                            newConfig.addSequenceTemplate(oldName, oldSequenceTemplates.get(oldName));
                        }
                    }
                    log.info("The sequence template which you are updating is created as a new sequence template: " + name);
                    TemplateMediator newSeqTemplate = sequenceTemplates.get(name);
                    newSeqTemplate.setFileName(name + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.TEMPLATES_DIR, newSeqTemplate.getFileName(), name, newConfig);
                }
            }
        }

        Map<String, Template> endpointTemplates = newConfig.getEndpointTemplates();
        for (String name : endpointTemplates.keySet()) {
            Template oldEndpointTempl = currentConfig.getEndpointTemplates().get(name);
            if (oldEndpointTempl != null) {
                endpointTemplates.get(name).setFileName(oldEndpointTempl.getFileName());
                endpointTemplates.get(name).setArtifactContainerName(oldEndpointTempl.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.TEMPLATES_DIR, oldEndpointTempl.getFileName(), name, newConfig);
            } else {
                if (currentConfig.getEndpointTemplates().size() >= newConfig.getEndpointTemplates().size()) {
                    newConfig.removeEndpointTemplate(name);
                    log.error("Unable to update the endpoint templates.");
                    Map<String, Template> oldEndpointTemplates = currentConfig.getEndpointTemplates();
                    for (String oldName : oldEndpointTemplates.keySet()) {
                        if (newConfig.getEndpointTemplates().get(oldName) == null) {
                            newConfig.addEndpointTemplate(oldName, oldEndpointTemplates.get(oldName));
                        }
                    }
                } else {
                    Map<String, Template> oldEndpointTemplates = currentConfig.getEndpointTemplates();
                    for (String oldName : oldEndpointTemplates.keySet()) {
                        if (newConfig.getEndpointTemplates().get(oldName) == null) {
                            newConfig.addEndpointTemplate(oldName, oldEndpointTemplates.get(oldName));
                        }
                    }
                    log.info("The endpoint template which you are updating is created as a new endpoint template: " + name);
                    Template newTemplate = endpointTemplates.get(name);
                    newTemplate.setFileName(name + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.TEMPLATES_DIR, newTemplate.getFileName(), name, newConfig);
                }
            }
        }

        Collection<API> apiCollection = newConfig.getAPIs();
        for (API api : apiCollection) {
            API oldAPI = currentConfig.getAPI(api.getName());
            if (oldAPI != null) {
                api.setFileName(oldAPI.getFileName());
                api.setArtifactContainerName(oldAPI.getArtifactContainerName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.REST_API_DIR, api.getFileName(), api.getName(), newConfig);
            } else {
                if (currentConfig.getAPIs().size() >= newConfig.getAPIs().size()) {
                    newConfig.removeAPI(api.getName());
                    log.error("Unable to update the APIs.");
                    Collection<API> oldApiCollection = currentConfig.getAPIs();
                    for (API eachOldApi : oldApiCollection) {
                        if (newConfig.getAPI(eachOldApi.getName()) == null) {
                            newConfig.addAPI(eachOldApi.getName(), eachOldApi);
                        }
                    }
                } else {
                    Collection<API> oldApiCollection = currentConfig.getAPIs();
                    for (API eachOldApi : oldApiCollection) {
                        if (newConfig.getAPI(eachOldApi.getName()) == null) {
                            newConfig.addAPI(eachOldApi.getName(), eachOldApi);
                        }
                    }
                    log.info("The API which you are updating is created as a new API: " + api.getName());
                    api.setFileName(api.getName() + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.REST_API_DIR, api.getFileName(), api.getName(), newConfig);
                }
            }
        }

        Map<String, SynapseImport> imports = newConfig.getSynapseImports();
        for (String name : imports.keySet()) {
            SynapseImport oldImport = currentConfig.getSynapseImports().get(name);
            if (oldImport != null) {
                imports.get(name).setFileName(oldImport.getFileName());
                addToDeploymentStore(MultiXMLConfigurationBuilder.SYNAPSE_IMPORTS_DIR, oldImport.getFileName(), name, newConfig);
            } else {
                if (currentConfig.getSynapseImports().size() >= newConfig.getSynapseImports().size()) {
                    newConfig.removeSynapseImport(name);
                    log.error("Unable to update the synapse imports.");
                    Map<String, SynapseImport> oldImports = currentConfig.getSynapseImports();
                    for (String oldName : oldImports.keySet()) {
                        if (newConfig.getSynapseImports().get(oldName) == null) {
                            newConfig.addSynapseImport(oldName, oldImports.get(oldName));
                        }
                    }
                } else {
                    Map<String, SynapseImport> oldImports = currentConfig.getSynapseImports();
                    for (String oldName : oldImports.keySet()) {
                        if (newConfig.getSynapseImports().get(oldName) == null) {
                            newConfig.addSynapseImport(oldName, oldImports.get(oldName));
                        }
                    }
                    log.info("The synapse import which you are updating is created as a new synapse import: " + name);
                    SynapseImport newImport = imports.get(name);
                    newImport.setFileName(name + XML);
                    addToDeploymentStore(MultiXMLConfigurationBuilder.SYNAPSE_IMPORTS_DIR, newImport.getFileName(), name, newConfig);
                }
            }
        }

        //fix for persistence issue in mediation library (connector .zip files)
        Map<String, Library> libraryMap = currentConfig.getSynapseLibraries();
        for (String name : libraryMap.keySet()) {
            newConfig.getSynapseLibraries().put(name, libraryMap.get(name));
            String fileName = libraryMap.get(name).getFileName();
            SynapseArtifactDeploymentStore store = newConfig.getArtifactDeploymentStore();
            LibDeployerUtils.deployingLocalEntries(libraryMap.get(name), newConfig);
            if (!store.containsFileName(fileName)) {
                store.addArtifact(fileName, name);
            }
            store.addRestoredArtifact(fileName);
        }

        if (Boolean.valueOf(currentConfig.getProperty(
                MultiXMLConfigurationBuilder.SEPARATE_REGISTRY_DEFINITION))) {
            newConfig.getProperties().setProperty(
                    MultiXMLConfigurationBuilder.SEPARATE_REGISTRY_DEFINITION, "true");
        }

        if (Boolean.valueOf(currentConfig.getProperty(
                MultiXMLConfigurationBuilder.SEPARATE_TASK_MANAGER_DEFINITION))) {
            newConfig.getProperties().setProperty(
                    MultiXMLConfigurationBuilder.SEPARATE_TASK_MANAGER_DEFINITION, "true");
        }
    }

   private void safeRemoveService(String serviceName) {
        AxisService service = axisCfg.getServiceForActivation(serviceName);
        if (service != null) {
            try {
                //See https://wso2.org/jira/browse/ESBJAVA-1358
                service.getParent().addParameter(CarbonConstants.KEEP_SERVICE_HISTORY_PARAM,
                        "true");
                axisCfg.removeService(serviceName);
            } catch (AxisFault axisFault) {
                handleException("Error while removing the service: " + serviceName, axisFault);
            }
        }
    }

    private void publishConfiguration(SynapseConfiguration synCfg, Axis2SynapseEnvironment synEnv) {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        SynapseRegistrationsService registrationsService =
                ConfigHolder.getInstance().getSynapseRegistrationService(tenantId);

        if (registrationsService != null) {
            serverContextInformation.setSynapseConfiguration(synCfg);

            // populate the Synapse Configuration
            ServiceRegistration configRegistration =
                    registrationsService.getSynapseConfigurationServiceRegistration();
            SynapseConfigurationService synCfgSvc = (SynapseConfigurationService)
                    ConfigHolder.getInstance().getBundleContext().getService(
                            configRegistration.getReference());
            synCfgSvc.setSynapseConfiguration(synCfg);
            configRegistration.setProperties(new Properties());

            // populate the Synapse Environment
            ServiceRegistration synEnvSvcRegistration =
                    registrationsService.getSynapseEnvironmentServiceRegistration();
            SynapseEnvironmentService synEnvSvc = (SynapseEnvironmentService)
                    ConfigHolder.getInstance().getBundleContext().getService(
                            synEnvSvcRegistration.getReference());
            synEnvSvc.setSynapseEnvironment(synEnv);
            synEnvSvcRegistration.setProperties(new Properties());

            ConfigurationTrackingService trackingSvc = ConfigHolder.getInstance().
                    getSynapseConfigTrackingService();
            if (trackingSvc != null) {
                trackingSvc.setSynapseConfiguration(synCfg);
            }
        } else {
            String msg = "Couldn't find the OSGi service information about this " +
                    "ESB Configuration";
            log.error(msg);
            throw new ConfigAdminException(msg);
        }
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new ConfigAdminException(msg, e);
    }
}
