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

package org.wso2.carbon.mediation.initializer.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.MultiXMLConfigurationSerializer;
import org.apache.synapse.config.xml.XMLConfigurationSerializer;
import org.wso2.carbon.mediation.initializer.RegistryBasedSynapseConfigSerializer;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Manages all persistence activities related to mediation configuration. Takes care
 * of saving configurations to the file system, registry and updating such configurations
 * as and when necessary. All the mediation components (proxy services, sequences etc)
 * should use this implementation to handle their persistence requirements. This class
 * does not immediately carry out persistence requests. Requests are first queued up and
 * then executed as batch jobs. Therefore admin services which initiate persistence
 * requests does not have to 'wait' for disk and network I/O often associated with
 * persistence activities. This improves the UI response times and system usability
 * in a great deal.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MediationPersistenceManager {

    private static final Log log = LogFactory.getLog(MediationPersistenceManager.class);

    private UserRegistry registry;
    private boolean initialized = false;
    private String configPath;
    private boolean flatFileMode;    
    private SynapseConfiguration synapseConfiguration;
    private String configName;

    /** Queue to hold persistence requests - Make sure all accesses are synchronized */
    private final LinkedList<PersistenceRequest> requestQueue = new LinkedList<PersistenceRequest>();

    private MediationPersistenceWorker worker;
    private boolean acceptRequests;
    private long interval = 5000L;

    private Map<Integer, AbstractStore> dataStores;

    /**
     * Initialize the mediation persistence manager instance and start accepting
     * and processing persistence requests. Persistence requests are carried out
     * on the local file system and if required on the registry as well.
     *
     * @param registry The UserRegistry instance to be used for persistence or null
     * @param configPath Path to the file/directory where configuration should be saved in
     * @param synapseConfiguration synapse configuration to be used
     * @param interval The wait time for the mediation persistence worker thread
     * @param configName Name of the configuration to be used
     */
    public MediationPersistenceManager(UserRegistry registry, String configPath,
                     SynapseConfiguration synapseConfiguration,
                     long interval, String configName) {

        if (initialized) {
            log.warn("Mediation persistence manager is already initialized");
            return;
        }

        if (configPath == null) {
            log.warn("Synapse configuration location is not provided - Unable to initialize " +
                    "the mediation persistence manager.");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Initializing the mediation persistence manager");
        }

        this.registry = registry;
        this.configPath = configPath;        
        this.synapseConfiguration = synapseConfiguration;
        this.configName = configName;
        if (interval > 0) {
            this.interval = interval;
        } else {
            log.warn("Invalid interval value " + interval + " for the mediation persistence " +
                    "worker, Using defaults");
        }

        File file = new File(configPath);
        flatFileMode = file.exists() && file.isFile();

        initDataStores();
        
        worker = new MediationPersistenceWorker();
        worker.start();

        // and we are ready to launch....
        acceptRequests = true;
        initialized = true;
    }

    private void initDataStores() {
        dataStores = new HashMap<Integer, AbstractStore>();
        dataStores.put(ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE,
                new ProxyServiceStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_SEQUENCE,
                new SequenceStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_ENDPOINT,
                new EndpointStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_TASK,
                new StartupStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_EVENT_SRC,
                new EventSourceStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_ENTRY,
                new LocalEntryStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_REGISTRY,
                new SynapseRegistryStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_EXECUTOR,
                new ExecutorStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_TEMPLATE,
                new TemplateStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_TEMPLATE_ENDPOINTS,
                new EndpointTemplateStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_MESSAGE_STORE,
                new MessageStoreStore(configPath,registry,configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_MESSAGE_PROCESSOR,
                new MessageProcessorStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_REST_API,
                new APIStore(configPath, registry, configName));
        dataStores.put(ServiceBusConstants.ITEM_TYPE_IMPORT,
                new ImportStore(configPath, registry, configName));
    }

    public void destroy() {
        if (!initialized) {
            return;
        }

        // Stop accepting any more persistence requests
        acceptRequests = false;

        if (log.isDebugEnabled()) {
            log.debug("Shutting down mediation persistence manager");
        }

        // Wait till the jobs already in the queue are done
        while (!requestQueue.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) { }
        }

        // Halt the persistence worker thread
        worker.proceed = false;
        if (worker.isAlive()) {
            // If the worker is asleep, wake him up
            worker.interrupt();
        }
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Save changes made to a particular item of the mediation configuration. Changes are
     * saved to the local file system and if required to the registry as well. Types of
     * supported mediation configuration items are defined in ServiceBusConstants. If the
     * item to be saved should be written to its own configuration file, the file name
     * attribute must be set on the item (in the SynapseConfiguration). Leaving the file
     * name as null will cause the item to be persisted into the top level synapse.xml
     * file.
     *
     * @param name Name/ID of the configuration item
     * @param itemType type of the configuration item
     */
    public void saveItem(String name, int itemType) {
        if (!initialized || !acceptRequests) {
            log.warn("Mediation persistence manager is either not initialized or not in the " +
                    "'accepting' mode. Ignoring the save request.");
            return;
        }

        if (itemType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            saveFullConfiguration(false);
            return;
        }

        PersistenceRequest request = new PersistenceRequest(name, itemType, true);
        synchronized (requestQueue) {
            addToQueue(request);
        }
    }

    /**
     * Delete a particular item in the saved mediation configuration. Item is removed from the
     * local file system and if required to the registry as well. Types of
     * supported mediation configuration items are defined in ServiceBusConstants.
     *
     * @param name Name/ID of the configuration item
     * @param fileName Name of the file where the item is currently saved in
     * @param itemType Type of the configuration item
     */
    public void deleteItem(String name, String fileName, int itemType) {
        if (!initialized || !acceptRequests) {
            log.warn("Mediation persistence manager is either not initialized or not in the " +
                    "'accepting' mode. Ignoring the delete request.");
            return;
        }

        if (itemType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            return;
        }

        PersistenceRequest request = new PersistenceRequest(name, fileName, itemType, false);
        synchronized (requestQueue) {
            addToQueue(request);
        }
    }

    /**
     * Save the specified item to the registry only. This method accepts request if and only
     * if registry persistence is enabled for mediation configuration items. It is not
     * recommended to use this method unless it is required to save something to the registry
     * only. To save an item properly one must use the saveItem method.
     *
     * @param name Name/ID of the configuration item
     * @param itemType Type of the configuration item
     */
    public void saveItemToRegistry(String name, int itemType) {

        if (registry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Registry persistence is disabled for mediation configuration. " +
                        "Ignoring the persistence request for " + name);
            }
            return;
        }

        if (!initialized || !acceptRequests) {
            log.warn("Mediation persistence manager is either not initialized or not in the " +
                    "'accepting' mode. Ignoring the save request.");
            return;
        }

        if (itemType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            saveFullConfiguration(true);
            return;
        }

        PersistenceRequest request = new PersistenceRequest(name, itemType, true);
        request.registryOnly = true;

        synchronized (requestQueue) {
            addToQueue(request);
        }
    }

    /**
     * Delete the specified item from the registry only. This method accepts request if and only
     * if registry persistence is enabled for mediation configuration items. It is not
     * recommended to use this method unless it is required to delete something from the registry
     * only. To delete an item properly one must use the deleteItem method.
     *
     * @param name Name/ID of the configuration item
     * @param itemType Type of the configuration item
     */
    public void deleteItemFromRegistry(String name, int itemType) {

        if (registry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Registry persistence is disabled for mediation configuration. " +
                        "Ignoring the persistence request for " + name);
            }
            return;
        }

        if (!initialized || !acceptRequests) {
            log.warn("Mediation persistence manager is either not initialized or not in the " +
                    "'accepting' mode. Ignoring the delete request.");
            return;
        }

        if (itemType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            return;
        }

        PersistenceRequest request = new PersistenceRequest(name, null, itemType, false);
        request.registryOnly = true;
        synchronized (requestQueue) {
            addToQueue(request);
        }
    }

    /**
     * This method ensures that only the latest persistence request for a particular object
     * will remain in the request queue. This helps reduce I/O overhead during persistence
     * operations by merging multiple save requests to one and giving delete requests priority
     * over save requests.
     *
     * @param request The latest request to be added to queue
     */
    private void addToQueue(PersistenceRequest request) {

        int i = 0;
        boolean matchFound = false;
        for (; i < requestQueue.size(); i++) {
            PersistenceRequest oldRequest = requestQueue.get(i);
            if (oldRequest.subjectType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
                // if a request to save the full configuration is already in the queue
                // we can ignore the current request - Configuration will get saved
                // to the disk anyway
                return;
            }

            if (oldRequest.subjectType == request.subjectType &&
                    oldRequest.subjectId.equals(request.subjectId)) {
                matchFound = true;
                break;
            }
        }

        if (matchFound) {
            // If an older request was found for the same item overwrite it
            requestQueue.remove(i);
            requestQueue.add(i, request);
        } else {
            // Otherwise add the current request to the tail of the queue
            requestQueue.offer(request);
        }
    }

    /**
     * Make a request to save the complete mediation configuration (the entire
     * SynapseConfiguration) to be saved to the file system and the registry.
     * This will remove all the existing requests already in the job queue and add
     * a single new entry.
     *
     * @param registryOnly Whether or not to save the configuration to the registry only
     */
    public void saveFullConfiguration(boolean registryOnly) {
        if (log.isDebugEnabled()) {
            log.debug("Received request to save full mediation configuration");
        }

        PersistenceRequest request = new PersistenceRequest(null,
                ServiceBusConstants.ITEM_TYPE_FULL_CONFIG, true);
        request.registryOnly = registryOnly;
        synchronized (requestQueue) {
            requestQueue.clear();
            requestQueue.offer(request);
        }
    }

    private void handleException(String msg, Throwable t) {
        log.error(msg, t);
        throw new ServiceBusPersistenceException(msg, t);
    }

    private class MediationPersistenceWorker extends Thread {

        boolean proceed = true;

        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Starting the mediation persistence worker thread");
            }

            if(CarbonUtils.isWorkerNode()){
                log.info("This is a worker node, Mediation persistance manager will be disabled.");
            }
            while (proceed) {
                PersistenceRequest request;

                synchronized (requestQueue) {
                   request = requestQueue.poll();
                }

                if (request == null) {
                    try {
                        sleep(interval);
                    } catch (InterruptedException ignore) {
                        // This condition could occur only during system shutdown.
                        // We can safely ignore this.
                    }

                    // Simply go to the next iteration
                    continue;
                }
                if(CarbonUtils.isWorkerNode()){
                    log.debug("Ignoring persist request because this is a worker node");
                    continue;
                }
                try {
                    //SynapseConfiguration config = synapseConfiguration;
                    if (request.registryOnly && registry != null) {
                        if (request.save) {
                            persistElementToRegistry(synapseConfiguration, request);
                        } else {
                            deleteElementFromRegistry(request);
                        }
                    } else {
                        if (flatFileMode) {
                            saveToFlatFile(synapseConfiguration);
                        } else if (request.save) {
                            persistElement(synapseConfiguration, request);
                        } else {
                            deleteElement(synapseConfiguration, request);
                        }
                    }

                } catch (Throwable t) {
                    // Just log the error and continue
                    // DO NOT throw the error since that will kill the worker thread
                    log.error("Error while saving mediation configuration changes", t);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Stopping the mediation persistence worker thread");
            }
        }
    }

    private void persistElement(SynapseConfiguration config, PersistenceRequest request) {
        if (request.subjectType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            saveFullConfiguration(config);
        } else {
            AbstractStore dataStore = dataStores.get(request.subjectType);
            dataStore.save(request.subjectId, config);
        }
    }

    private void deleteElement(SynapseConfiguration config, PersistenceRequest request) {
        AbstractStore dataStore = dataStores.get(request.subjectType);
        dataStore.delete(request.subjectId, request.fileName, config);
    }

    private void persistElementToRegistry(SynapseConfiguration config, PersistenceRequest request) {
        if (request.subjectType == ServiceBusConstants.ITEM_TYPE_FULL_CONFIG) {
            if (log.isDebugEnabled()) {
                log.debug("Serializing full mediation configuration to the registry");
            }

            RegistryBasedSynapseConfigSerializer registrySerializer =
                    new RegistryBasedSynapseConfigSerializer(registry, configName);
            registrySerializer.serializeConfiguration(config);

        } else {
            AbstractStore dataStore = dataStores.get(request.subjectType);
            dataStore.saveItemToRegistry(request.subjectId, config);
        }
    }

    private void deleteElementFromRegistry(PersistenceRequest request) {
        AbstractStore dataStore = dataStores.get(request.subjectType);
        dataStore.deleteItemFromRegistry(request.subjectId);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void saveToFlatFile(SynapseConfiguration config) throws IOException,
            XMLStreamException {

        File outputFile = new File(configPath);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(outputFile);
        XMLConfigurationSerializer.serializeConfiguration(config, fos);
        fos.flush();
        fos.close();
    }

    private void saveFullConfiguration(SynapseConfiguration config) {
        if (log.isDebugEnabled()) {
            log.debug("Serializing full mediation configuration to the file system");
        }

        MultiXMLConfigurationSerializer serializer = new MultiXMLConfigurationSerializer(configPath);
        serializer.serialize(config);

        if (registry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Serializing full mediation configuration to the registry");    
            }

            RegistryBasedSynapseConfigSerializer registrySerializer =
                    new RegistryBasedSynapseConfigSerializer(registry, configName);
            registrySerializer.serializeConfiguration(config);
        }
    }

    /**
     * Bean to store details of a persistence request
     */
    private class PersistenceRequest {

        private boolean save;
        private int subjectType;
        private String subjectId;
        private String fileName;
        private boolean registryOnly;

        public PersistenceRequest(String subjectId, int subjectType, boolean save) {
            this.save = save;
            this.subjectId = subjectId;
            this.subjectType = subjectType;
        }

        public PersistenceRequest(String subjectId, String fileName, int subjectType, boolean save) {
            this.save = save;
            this.subjectId = subjectId;
            this.subjectType = subjectType;
            this.fileName = fileName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PersistenceRequest pr = (PersistenceRequest) o;
            return pr.subjectType == this.subjectType &&
                    pr.save == this.save &&
                    pr.subjectId.equals(this.subjectId);
        }

        @Override
        public int hashCode() {
            int result = (save ? 1 : 0);
            result = 31 * result + subjectType;
            result = 31 * result + (subjectId != null ? subjectId.hashCode() : 0);
            return result;
        }
    }

}
