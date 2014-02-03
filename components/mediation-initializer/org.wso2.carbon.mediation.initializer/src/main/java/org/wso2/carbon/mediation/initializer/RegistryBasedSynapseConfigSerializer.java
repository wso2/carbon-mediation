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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.*;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.config.xml.eventing.EventSourceSerializer;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.mediation.initializer.persistence.registry.*;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.util.Map;

/**
 * Serializes the {@link org.apache.synapse.config.SynapseConfiguration} to a registry space,
 * which defaults to <em>/carbon/synapse-config</em></p>
 *
 * <p>The configuration element hierarchy used is;
 * <ul>
 *  <li>CONFIG_REGISTRY/repository/synapse/sequences</li>
 *  <li>CONFIG_REGISTRY/repository/synapse/endpoints</li>
 *  <li>CONFIG_REGISTRY/repository/synapse/local-entries</li>
 *  <li>CONFIG_REGISTRY/repository/synapse/proxy-services</li>
 *  <li>CONFIG_REGISTRY/repository/synapse/event-sources</li>
 *  <li>CONFIG_REGISTRY/repository/synapse/synapse-startups</li>
 *  <li>CONFIG_REGISTRY/repository/synapse/synapse-registry</li>
 * </ul>
 *
 * The above resource paths corresponds to the relevant elements</p>
 *
 * @see org.wso2.carbon.mediation.initializer.ServiceBusConstants.RegistryStore
 * @see org.wso2.carbon.mediation.initializer.RegistryBasedSynapseConfigBuilder
 */
public class RegistryBasedSynapseConfigSerializer {

    private static final Log log
            = LogFactory.getLog(RegistryBasedSynapseConfigSerializer.class);

    /**
     * Holds the registry instance to which the
     * {@link org.apache.synapse.config.SynapseConfiguration} will be persisted in to the
     * <em>/carbon/synapse-config</em> collection.
     */
    private UserRegistry registry;

    private String configName;

    /**
     * Constructs the serializer and sets the registry to be used
     *
     * @param registry to be used to serialize the configuration elements
     * @param configName cpnfiguration name
     */
    public RegistryBasedSynapseConfigSerializer(UserRegistry registry, String configName) {
        this.registry = registry;
        this.configName = configName;
    }

    /**
     * Serialize and persists the {@link org.apache.synapse.config.SynapseConfiguration} to the
     * registry space. If the <b>synapse.xml</b> has been loaded from the synapse registry then
     * any change will be local to the server instance and will not be persisted to the specified
     * synapse registry.
     *
     * @param configuration to be serialized
     */
    public void serializeConfiguration(SynapseConfiguration configuration) {

        log.info("Persisting the SynapseConfiguration to the registry...");

        try {
            boolean transactionStarted = Transaction.isStarted();
            if (!transactionStarted) {
                registry.beginTransaction();
            }
            // full config serialization should firs clear the space
            clearSynapseConfigRegistrySpace();

            if (configuration.getRegistry() != null) {
                String fileName = null;
                if (Boolean.valueOf(configuration.getProperty(
                        MultiXMLConfigurationBuilder.SEPARATE_REGISTRY_DEFINITION))) {

                    fileName = MultiXMLConfigurationBuilder.REGISTRY_FILE;
                }
                serializeSynapseRegistryToRegistry(configuration.getRegistry(), fileName);
            }

            for (ProxyService proxyService : configuration.getProxyServices()) {
                serializeProxyServiceToRegistry(proxyService);
            }

            for (SynapseEventSource eventSource : configuration.getEventSources()) {
                serializeEventSourceToRegistry(eventSource);
            }

            for (Startup startup : configuration.getStartups()) {
                serializeStartupToRegistry(startup);
            }

            for (Map.Entry<String, MessageStore> store :
                    configuration.getMessageStores().entrySet()) {
                serializeMessageStoreToRegistry(store.getValue());
            }

            for (Map.Entry<String, MessageProcessor> processor :
                    configuration.getMessageProcessors().entrySet()){
                serializeMessageProcessorToRegistry(processor.getValue());
            }


            for (Object o : configuration.getLocalRegistry().values()) {

                if (o instanceof SequenceMediator) {
                    // if this entry is a sequence
                    serializeSequenceToRegistry((SequenceMediator) o);
                } else if (o instanceof Endpoint) {
                    // if this entry is an endpoint
                    serializeEndpointToRegistry((Endpoint) o);
                } else if (o instanceof Entry) {
                    // if this is an real entry && this isn't the host and ip entries
                    Entry entry = (Entry) o;
                    if (!(SynapseConstants.SERVER_HOST.equals(entry.getKey())
                            || SynapseConstants.SERVER_IP.equals(entry.getKey()))
                            && entry.getType() != Entry.REMOTE_ENTRY) {
                        serializeEntryToRegistry(entry);
                    }
                }
            }
            if (!transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            try {
                registry.rollbackTransaction();
            } catch (Exception ex) {
                log.error("Unable to persist SynapseConfiguration to the registry", ex);
            }
            log.error("Unable to persist SynapseConfiguration to the registry", e);
        }
    }

    private void serializeSynapseRegistryToRegistry(Registry synapseRegistry, String fileName) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the synapse registry declaration");
        }
        SynapseRegistriesRegistryStore synapseRegistryStore
                = new SynapseRegistriesRegistryStore(registry, configName);
        synapseRegistryStore.persistElement(
                RegistrySerializer.serializeRegistry(null, synapseRegistry), fileName);
    }

    private void serializeProxyServiceToRegistry(ProxyService proxyService) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the proxy service " + proxyService.getName());
        }
        ProxyServiceRegistryStore proxyServiceStore =
                new ProxyServiceRegistryStore(registry, configName);
        proxyServiceStore.persistElement(proxyService.getName(), ProxyServiceSerializer
                .serializeProxy(null, proxyService), proxyService.getFileName());
    }

    private void serializeEventSourceToRegistry(SynapseEventSource eventSource) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the event source " + eventSource.getName());
        }
        EventSourceRegistryStore eventSourceStore =
                new EventSourceRegistryStore(registry, configName);
        eventSourceStore.persistElement(eventSource.getName(), EventSourceSerializer
                .serializeEventSource(null, eventSource), eventSource.getFileName());
    }

    private void serializeSequenceToRegistry(SequenceMediator sequence) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the sequence " + sequence.getName());
        }
        SequenceRegistryStore sequenceStore = new SequenceRegistryStore(registry, configName);
        sequenceStore.persistElement(sequence.getName(),
                MediatorSerializerFinder.getInstance().getSerializer(
                        sequence).serializeMediator(null, sequence), sequence.getFileName());
    }

    private void serializeEndpointToRegistry(Endpoint endpoint) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the endpoint " + endpoint.getName());
        }
        EndpointRegistryStore endpointStore =
                new EndpointRegistryStore(registry, configName);
        endpointStore.persistElement(endpoint.getName(), EndpointSerializer
                .getElementFromEndpoint(endpoint), endpoint.getFileName());
    }

    private void serializeEntryToRegistry(Entry entry) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the local entry " + entry.getKey());
        }
        LocalEntryRegistryStore localEntryStore =
                new LocalEntryRegistryStore(registry, configName);
        localEntryStore.persistElement(entry.getKey(),
                EntrySerializer.serializeEntry(entry, null), entry.getFileName());
    }

    private void serializeStartupToRegistry(Startup startup) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the startup task " + startup.getName());
        }
        StartupRegistryStore startupStore =
                new StartupRegistryStore(registry, configName);
        startupStore.persistElement(startup.getName(), StartupFinder.getInstance()
                .serializeStartup(null, startup), startup.getFileName());
    }

    private void serializeMessageStoreToRegistry(MessageStore messageStore) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the message store '" + messageStore.getName() + "'");
        }
        MessageStoreRegistryStore messageStoreRegistryStore =
                new MessageStoreRegistryStore(registry, configName);
        messageStoreRegistryStore.persistElement(messageStore.getName(), MessageStoreSerializer.
                serializeMessageStore(null, messageStore), messageStore.getFileName());
    }

    private void serializeMessageProcessorToRegistry(MessageProcessor messageProcessor) {
        if (log.isDebugEnabled()) {
            log.debug("Persisting the message processor '" + messageProcessor.getName() + "'");
        }
        MessageProcessorRegistryStore messageProcessorRegistryStore =
                new MessageProcessorRegistryStore(registry, configName);
        messageProcessorRegistryStore.persistElement(messageProcessor.getName(),
                MessageProcessorSerializer.serializeMessageProcessor(null, messageProcessor),
                messageProcessor.getFileName());
    }

    private void clearSynapseConfigRegistrySpace() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Clearing the registry space at path : "
                        + ServiceBusConstants.SYNAPSE_CONFIG_REGISTRY_SPACE);
            }
            if (registry.resourceExists(ServiceBusConstants.SYNAPSE_CONFIG_REGISTRY_SPACE +
                    RegistryConstants.PATH_SEPARATOR + configName)) {
                registry.delete(ServiceBusConstants.SYNAPSE_CONFIG_REGISTRY_SPACE +
                        RegistryConstants.PATH_SEPARATOR + configName);
            }
        } catch (RegistryException e) {
            log.warn("Couldn't clear the synapse configuration registry space");
        }
    }
}
