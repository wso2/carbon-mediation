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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Startup;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.XMLConfigurationBuilder;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.API;
import org.wso2.carbon.mediation.initializer.persistence.registry.*;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.Properties;

/**
 * Builds the {@link org.apache.synapse.config.SynapseConfiguration} by using a registry space,
 * which defaults to <em>/carbon/synapse-config</em></p>
 *
 * <p>The configuration element hierarchy has assumed to be;
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
 * The above resource paths corresponds to the relevant elements, and if there is no <em>main</em>
 * and <em>fault</em> sequences the builder will add a default sequence for those.</p>
 *
 * @see org.wso2.carbon.mediation.initializer.ServiceBusConstants.RegistryStore
 * @see org.wso2.carbon.mediation.initializer.RegistryBasedSynapseConfigSerializer
 */
public class RegistryBasedSynapseConfigBuilder {

    Log log = LogFactory.getLog(RegistryBasedSynapseConfigBuilder.class);

    /**
     * Holds the registry instance to build the
     * {@link org.apache.synapse.config.SynapseConfiguration} by looking at the configuration
     * elements in the <em>/carbon/synapse-config</em> collection.
     */
    private UserRegistry registry;

    /** QName that will be used to extract the filename from the registry */
    private static final QName FILE_NAME_ATTR
            = new QName(ServiceBusConstants.DEFINITION_FILE_NAME);

    private String configName;

    private String synapseXMLLocation;

    private Properties properties;

    private boolean failSafeConfigurationLoading;

    /**
     * Constructs the builder and sets the registry to be used
     * 
     * @param registry to be used to retrieve the configuration elements
     * @param configName configuration name
     * @param synapseXMLLocation location of the synapse xml
     * @param properties the properties from the synapse.properties file
     * @param failSafeConfigurationLoading to load configurations in a fail safe way
     */
    public RegistryBasedSynapseConfigBuilder(UserRegistry registry, String configName,
                                             String synapseXMLLocation, Properties properties,
                                             boolean failSafeConfigurationLoading) {
        this.registry = registry;
        this.configName = configName;
        this.synapseXMLLocation = synapseXMLLocation;
        this.properties = properties;

        this.failSafeConfigurationLoading = failSafeConfigurationLoading;
    }

    /**
     * Builds the {@link org.apache.synapse.config.SynapseConfiguration} from the registry space.
     * If the <b>synapse registry</b> has been specified and there are no content in the rest
     * of the config space, this method looks for the registry configured in the synapse-registry
     * to find a synapse.xml file....</p>
     * 
     * <p>If non of the above is able to provide the configuration fall back to the default
     * configuration
     *
     * @return configuration constructed from the registry config space
     */
    public SynapseConfiguration getConfiguration() {

        SynapseConfiguration configuration = SynapseConfigUtils.newConfiguration();
        configuration.setDefaultQName(XMLConfigConstants.DEFINITIONS_ELT);

        // load the configuration from the registry
        boolean transactionStarted = Transaction.isStarted();
        try {
            if (!transactionStarted) {
                registry.beginTransaction();
            }
            buildSynapseRegistryFromRegistry(configuration, properties);
            buildLocalEntriesFromRegistry(configuration, properties);
            buildEndpointsFromRegistry(configuration, properties);
            buildSequencesFromRegistry(configuration, properties);
            buildProxyServicesFromRegistry(configuration, properties);
            buildTasksFromRegistry(configuration, properties);
            buildEvenSourcesFromRegistry(configuration, properties);
            buildExecutorsFromRegistry(configuration, properties);
			buildAPIsFromRegistry(configuration, properties);
            buildMessageStoresFromRegistry(configuration, properties);
            buildMessageProcessorsFromRegistry(configuration, properties);
            if (!transactionStarted) {
                registry.commitTransaction();
            }
        } catch (Exception e) {
            try {
                 if (!transactionStarted) {
                    registry.rollbackTransaction();
                 }
            } catch (Exception ex) {
                throw new SynapseException("Unable to rollback transaction", ex);
            }
            throw new SynapseException("Unable to perform registry operation", e);
        }

        // if there are no content in the configuration unless for the registry declaration
        // try to find a synapse.xml file at the root of the declared synapse registry
        if (configuration.getLocalRegistry().isEmpty() && configuration.getProxyServices().isEmpty()
                && configuration.getRegistry() != null) {

            OMNode remoteConfigNode = configuration.getRegistry().lookup("synapse.xml");
            try {
                configuration = XMLConfigurationBuilder.getConfiguration(
                        SynapseConfigUtils.getStreamSource(remoteConfigNode).getInputStream(),
                        properties);
            } catch (XMLStreamException xse) {
                throw new SynapseException("Problem loading remote synapse.xml", xse);
            }
        }

        // after all, if there is no sequence named main defined attach a default one
        if (configuration.getMainSequence() == null) {
            SynapseConfigUtils.setDefaultMainSequence(configuration);
        }

        // after all, if there is no sequence named fault defined attach a default one
        if (configuration.getFaultSequence() == null) {
            SynapseConfigUtils.setDefaultFaultSequence(configuration);
        }

        return configuration;
    }

    private void buildSynapseRegistryFromRegistry(SynapseConfiguration configuration, Properties properties) {

        SynapseRegistriesRegistryStore synapseRegistryStore
                = new SynapseRegistriesRegistryStore(registry, configName);
        OMElement registryElem = synapseRegistryStore.getElement();

        if (registryElem != null) {
            if (log.isDebugEnabled()) {
                log.debug("Building the synapse registry declaration from the carbon registry");
            }
            SynapseXMLConfigurationFactory.defineRegistry(configuration, registryElem, properties);
        } else if (log.isDebugEnabled()) {
            log.debug("No synapse registry is defined in the carbon registry");
        }
    }

    private void buildSequencesFromRegistry(SynapseConfiguration configuration, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the sequences from the carbon registry");
        }
        SequenceRegistryStore sequenceStore = new SequenceRegistryStore(registry, configName);
        for (OMElement sequenceElem : sequenceStore.getElements()) {
            try {
                SequenceMediator seq = (SequenceMediator)
                        SynapseXMLConfigurationFactory.defineSequence(configuration, sequenceElem, properties);
                String fileName = sequenceElem.getAttributeValue(FILE_NAME_ATTR);
                if (fileName != null) {
                    seq.setFileName(fileName);
                    fileName = synapseXMLLocation
                            + File.separator + MultiXMLConfigurationBuilder.SEQUENCES_DIR
                            + File.separator + fileName;
                    configuration.getArtifactDeploymentStore().addArtifact(fileName, seq.getName());
                }
            } catch (SynapseException e) {
                handleErrorGracefully("sequence", e);
            }
        }
    }

    private void buildEndpointsFromRegistry(SynapseConfiguration configuration,
                                            Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the endpoints from the carbon registry");
        }
        EndpointRegistryStore endpointStore = new EndpointRegistryStore(registry, configName);
        for (OMElement endpointElem : endpointStore.getElements()) {
            try {
                Endpoint ep = SynapseXMLConfigurationFactory.defineEndpoint(
                        configuration, endpointElem, properties);

                String fileName = endpointElem.getAttributeValue(FILE_NAME_ATTR);
                if (fileName != null) {
                    ep.setFileName(fileName);
                    fileName = synapseXMLLocation
                            + File.separator + MultiXMLConfigurationBuilder.ENDPOINTS_DIR
                            + File.separator + fileName;
                    configuration.getArtifactDeploymentStore().addArtifact(fileName, ep.getName());
                }
            } catch (SynapseException e) {
                handleErrorGracefully("endpoint", e);
            }
        }
    }

    private void buildLocalEntriesFromRegistry(SynapseConfiguration configuration,
                                               Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the local entries from the carbon registry");
        }
        LocalEntryRegistryStore localEntryStore = new LocalEntryRegistryStore(registry, configName);
        for (OMElement localEntryElem : localEntryStore.getElements()) {
            try {
                Entry e = SynapseXMLConfigurationFactory.defineEntry(
                        configuration, localEntryElem, properties);

                String fileName = localEntryElem.getAttributeValue(FILE_NAME_ATTR);
                if (fileName != null) {
                    e.setFileName(fileName);
                    fileName = synapseXMLLocation
                            + File.separator + MultiXMLConfigurationBuilder.LOCAL_ENTRY_DIR
                            + File.separator + fileName;
                    configuration.getArtifactDeploymentStore().addArtifact(fileName, e.getKey());
                }
            } catch (SynapseException e) {
                handleErrorGracefully("local-entry", e);
            }
        }
    }

    private void buildProxyServicesFromRegistry(SynapseConfiguration configuration,
                                                Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the proxy services from the carbon registry");
        }
        ProxyServiceRegistryStore proxyServiceStore =
                new ProxyServiceRegistryStore(registry, configName);
        for (OMElement proxyServiceElem : proxyServiceStore.getElements()) {
            try {
                ProxyService proxy = SynapseXMLConfigurationFactory.defineProxy(
                        configuration, proxyServiceElem, properties);

                String fileName = proxyServiceElem.getAttributeValue(FILE_NAME_ATTR);
                if (fileName != null) {
                    proxy.setFileName(fileName);
                    fileName = synapseXMLLocation
                            + File.separator + MultiXMLConfigurationBuilder.PROXY_SERVICES_DIR
                            + File.separator + fileName;
                    configuration.getArtifactDeploymentStore().addArtifact(fileName, proxy.getName());
                }
            } catch (SynapseException e) {
                handleErrorGracefully("proxy", e);
            }
        }
    }

    private void buildTasksFromRegistry(SynapseConfiguration configuration,
                                        Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the startup tasks from the carbon registry");
        }
        StartupRegistryStore startupStore = new StartupRegistryStore(registry, configName);
        for (OMElement startupElem : startupStore.getElements()) {
            try {
                Startup st = SynapseXMLConfigurationFactory.defineStartup(
                        configuration, startupElem, properties);

                String fileName = startupElem.getAttributeValue(FILE_NAME_ATTR);
                if (fileName != null) {
                    st.setFileName(fileName);
                    fileName = synapseXMLLocation
                            + File.separator + MultiXMLConfigurationBuilder.TASKS_DIR
                            + File.separator + st.getFileName();
                    configuration.getArtifactDeploymentStore().addArtifact(fileName, st.getName());
                }
            } catch (SynapseException e) {
                handleErrorGracefully("task", e);
            }
        }
    }

    private void buildEvenSourcesFromRegistry(SynapseConfiguration configuration,
                                              Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the event sources from the carbon registry");
        }
        EventSourceRegistryStore eventSourceStore =
                new EventSourceRegistryStore(registry, configName);
        for (OMElement eventSourceElem : eventSourceStore.getElements()) {
            try {
                SynapseEventSource ses = SynapseXMLConfigurationFactory.defineEventSource(
                        configuration, eventSourceElem, properties);
                String fileName = eventSourceElem.getAttributeValue(FILE_NAME_ATTR);
                if (fileName != null) {
                    ses.setFileName(fileName);
                    fileName = synapseXMLLocation
                            + File.separator + MultiXMLConfigurationBuilder.EVENTS_DIR
                            + File.separator + ses.getFileName();
                    configuration.getArtifactDeploymentStore().addArtifact(fileName, ses.getName());
                }
            } catch (SynapseException e) {
                handleErrorGracefully("event source", e);
            }
        }
    }

    private void buildExecutorsFromRegistry(SynapseConfiguration configuration,
                                            Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the executors from the carbon registry");
        }
        ExecutorRegistryStore executorRegistryStore =
                new ExecutorRegistryStore(registry, configName);
        for (OMElement executorElem : executorRegistryStore.getElements()) {
            try {
                SynapseXMLConfigurationFactory.defineExecutor(configuration, executorElem, properties);
            } catch (SynapseException e) {
                handleErrorGracefully("executor", e);
            }
        }
    }

    private void buildAPIsFromRegistry(SynapseConfiguration configuration,
                                            Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Building the APIs from the carbon registry");
        }
        APIRegistryStore apiStore = new APIRegistryStore(registry, configName);
        for (OMElement apiElem : apiStore.getElements()) {
            try {
                API api = SynapseXMLConfigurationFactory.defineAPI(configuration, apiElem);

                String fileName = apiElem.getAttributeValue(FILE_NAME_ATTR);
                if (fileName != null) {
                    api.setFileName(fileName);
                    fileName = synapseXMLLocation
                            + File.separator + MultiXMLConfigurationBuilder.REST_API_DIR
                            + File.separator + fileName;
                    configuration.getArtifactDeploymentStore().addArtifact(fileName, api.getName());
                }
            } catch (SynapseException e) {
                handleErrorGracefully("API", e);
            }
        }
    }

    private void buildMessageStoresFromRegistry(SynapseConfiguration configuration,
                                                Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Build the message stores from the carbon registry");
        }
        MessageStoreRegistryStore messageStoreRegistryStore =
                new MessageStoreRegistryStore(registry, configName);
        for (OMElement messageStoreElement : messageStoreRegistryStore.getElements()) {
            try {
                SynapseXMLConfigurationFactory.defineMessageStore
                        (configuration, messageStoreElement, properties);
            } catch (SynapseException e) {
                handleErrorGracefully("messagestore", e);
            }
        }
    }

    private void buildMessageProcessorsFromRegistry(SynapseConfiguration configuration,
                                                    Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Build the message processors from the carbon registry");
        }
        MessageProcessorRegistryStore messageProcessorRegistryStore =
                new MessageProcessorRegistryStore(registry, configName);
        for (OMElement messageProcessorElement : messageProcessorRegistryStore.getElements()) {
            try {
                SynapseXMLConfigurationFactory.defineMessageProcessor(configuration,
                        messageProcessorElement, properties);
            } catch (SynapseException e) {
                handleErrorGracefully("messageprocessor", e);
            }
        }
    }

    public void handleErrorGracefully(String component, SynapseException e) {
        if (failSafeConfigurationLoading) {
            log.warn("Error occurred while loading a " + component +
                    " configuration from the registry.", e);
        } else {
            throw e;
        }
    }
}
