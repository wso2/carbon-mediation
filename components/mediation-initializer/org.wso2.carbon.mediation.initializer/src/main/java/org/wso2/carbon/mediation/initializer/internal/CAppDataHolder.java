/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.initializer.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.API;
import org.apache.synapse.Startup;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.Template;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.template.TemplateMediator;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.store.MessageStore;

import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.utils.CAppArtifactData;
import org.wso2.carbon.mediation.initializer.utils.CAppArtifactsMap;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CAppDataHolder {

    private static final Log log = LogFactory.getLog(CAppDataHolder.class);

    private static CAppDataHolder instance;

    /* Map to hold Artifact details deployed from CApp of tenants*/
    private Map<Integer, Map<String, CAppArtifactData>> cAppArtifactDataForTenants = new ConcurrentHashMap<Integer, Map<String, CAppArtifactData>>();

    public static CAppDataHolder getInstance() {
        if (instance == null) {
            instance = new CAppDataHolder();
        }
        return instance;
    }

    /**
     * Add CApp artifacts data of the tenants to the Map
     *
     * @param tenantId         Current tenant id
     * @param cAppArtifactsMap CApp artifact details
     */
    public void addCAppArtifactData(int tenantId, CAppArtifactsMap cAppArtifactsMap) {
        if (cAppArtifactDataForTenants.containsKey(tenantId)) {
            Map<String, CAppArtifactData> artifactDataMap = getCAppArtifactDataMap(tenantId);
            Map<String, CAppArtifactData> newArtifactDataMap = new HashMap<String, CAppArtifactData>();
            newArtifactDataMap.putAll(artifactDataMap);
            newArtifactDataMap.putAll(cAppArtifactsMap.getcAppArtifactDataMap());
            cAppArtifactDataForTenants.put(tenantId, newArtifactDataMap);
        } else {
            cAppArtifactDataForTenants.put(tenantId, cAppArtifactsMap.getcAppArtifactDataMap());
        }
    }

    /**
     * Get the CAPP deployed artifact map of the current tenant
     *
     * @param tenantId Current tenant id
     * @return CApp Artifact Data Map
     */
    public Map<String, CAppArtifactData> getCAppArtifactDataMap(int tenantId) {
        if (cAppArtifactDataForTenants.containsKey(tenantId)) {
            return cAppArtifactDataForTenants.get(tenantId);
        }
        return null;
    }

    /**
     * Get the CAPP deployed artifact details of the tenant by artifact name
     *
     * @param tenantId     Current tenant id
     * @param artifactName Name of the Artifact - artifact type + artifact name
     * @return CApp Artifact Data
     */
    public CAppArtifactData getCAppArtifactDataByName(int tenantId, String artifactName) {
        Map<String, CAppArtifactData> artifactDataMap = getCAppArtifactDataMap(tenantId);
        if (artifactDataMap != null) {
            if (artifactDataMap.containsKey(artifactName)) {
                return artifactDataMap.get(artifactName);
            }
        }
        return null;
    }

    /**
     * Remove the artifact data for the tenant from the map
     *
     * @param tenantId Current tenant id
     */
    public void removeCappArtifactData(int tenantId, String name) {
        if (cAppArtifactDataForTenants.containsKey(tenantId)) {
            Map<String, CAppArtifactData> artifactDataMap = getCAppArtifactDataMap(tenantId);
            if (artifactDataMap.containsKey(name)) {
                artifactDataMap.remove(name);
            }
        }
    }

    /**
     * Check whether the artifact is deployed from CApp
     *
     * @param tenantId     Current tenant id
     * @param artifactName Name of the Artifact - artifact type + artifact name
     * @return true if artifact deployed from CApp, else false
     */
    public boolean isDeployedFromCApp(int tenantId, String artifactName) {
        CAppArtifactData cAppArtifactData = getCAppArtifactDataByName(tenantId, artifactName);
        if (cAppArtifactData != null) {
            return cAppArtifactData.isDeployedFromCapp();
        }
        return false;
    }

    /**
     * Check whether the CApp artifact is edited through management console
     *
     * @param tenantId     Current tenant id
     * @param artifactName Name of the Artifact - artifact type + artifact name
     * @return true if artifact is edited from management console, else false
     */
    public boolean isCAppArtifactEdited(int tenantId, String artifactName) {
        CAppArtifactData cAppArtifactData = getCAppArtifactDataByName(tenantId, artifactName);
        if (cAppArtifactData != null) {
            return cAppArtifactData.isEdited();
        }
        return false;
    }

    /**
     * Set the artifact data as edited through management console
     *
     * @param tenantId     Current tenant id
     * @param artifactName Name of the Artifact - artifact type + artifact name
     */
    public void setEdited(int tenantId, String artifactName) {
        CAppArtifactData cAppArtifactData = getCAppArtifactDataByName(tenantId, artifactName);
        if (cAppArtifactData != null && cAppArtifactData.isDeployedFromCapp()) {
            cAppArtifactData.setEdited(true);
        }
    }

    /**
     * @param synapseConfiguration Contains all the configuration includes CApp artifact configs
     * @return new Configuration which removes all the CApp related configs
     */
    public CAppArtifactWrapper removeCAppArtifactsBeforePersist(int tenantId, SynapseConfiguration synapseConfiguration) {

        final Lock lock = getLock(synapseConfiguration.getAxisConfiguration());
        SynapseConfiguration cAppArtifactConfig = new SynapseConfiguration();
        CAppArtifactWrapper cAppArtifactWrapper = new CAppArtifactWrapper();

        try {
            lock.lock();
            Map<String, Endpoint> endpoints = synapseConfiguration.getDefinedEndpoints();
            for (String name : endpoints.keySet()) {
                CAppArtifactData cAppEndpointData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.ENDPOINT_TYPE + File.separator + name);
                if (cAppEndpointData != null)
                    if (cAppEndpointData != null) {
                        Endpoint newEndpoint = endpoints.get(name);
                        cAppArtifactConfig.addEndpoint(name, newEndpoint);
                        cAppEndpointData.setEdited(true);
                        synapseConfiguration.removeEndpoint(name);
                    }
            }

            Map<String, SequenceMediator> sequences = synapseConfiguration.getDefinedSequences();
            for (String name : sequences.keySet()) {
                CAppArtifactData cAppSequenceData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.SEQUENCE_TYPE + File.separator + name);
                if (cAppSequenceData != null) {
                    SequenceMediator newSequences = sequences.get(name);
                    cAppArtifactConfig.addSequence(name, newSequences);
                    cAppSequenceData.setEdited(true);
                    synapseConfiguration.removeSequence(name);
                }
            }

            Collection<ProxyService> proxyServices = synapseConfiguration.getProxyServices();
            for (ProxyService proxy : proxyServices) {
                CAppArtifactData cAppProxyData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.PROXY_SERVICE_TYPE + File.separator + proxy.getName());
                if (cAppProxyData != null) {
                    cAppArtifactConfig.addProxyService(proxy.getName(), proxy);
                    cAppProxyData.setEdited(true);
                    synapseConfiguration.removeProxyService(proxy.getName());
                }
            }

            Map<String, Entry> localEntries = synapseConfiguration.getDefinedEntries();
            for (String name : localEntries.keySet()) {
                CAppArtifactData cAppLocalEntryData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.LOCAL_ENTRY_TYPE + File.separator + name);
                if (cAppLocalEntryData != null) {
                    Entry newEntry = localEntries.get(name);
                    cAppArtifactConfig.addEntry(name, newEntry);
                    cAppLocalEntryData.setEdited(true);
                    synapseConfiguration.removeEntry(name);
                }
            }

            Collection<MessageStore> messageStores = synapseConfiguration.getMessageStores().values();
            for (MessageStore store : messageStores) {
                CAppArtifactData cAppStoreData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.MESSAGE_STORE_TYPE + File.separator + store.getName());
                if (cAppStoreData != null) {
                    cAppArtifactConfig.addMessageStore(store.getName(), store);
                    cAppStoreData.setEdited(true);
                    synapseConfiguration.removeMessageStore(store.getName());
                }
            }

            Collection<MessageProcessor> messageProcessors = synapseConfiguration.getMessageProcessors().values();
            for (MessageProcessor processor : messageProcessors) {
                CAppArtifactData cAppProcessorData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.MESSAGE_PROCESSOR_TYPE + File.separator + processor.getName());
                if (cAppProcessorData != null) {
                    cAppArtifactConfig.addMessageProcessor(processor.getName(), processor);
                    cAppProcessorData.setEdited(true);
                    synapseConfiguration.removeMessageProcessor(processor.getName());
                }
            }

            Map<String, TemplateMediator> sequenceTemplates = synapseConfiguration.getSequenceTemplates();
            for (String name : sequenceTemplates.keySet()) {
                CAppArtifactData cAppSequenceTemplateData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.TEMPLATE_TYPE + File.separator + name);
                if (cAppSequenceTemplateData != null) {
                    TemplateMediator newTemplate = sequenceTemplates.get(name);
                    cAppArtifactConfig.addSequenceTemplate(name, newTemplate);
                    cAppSequenceTemplateData.setEdited(true);
                    synapseConfiguration.removeSequenceTemplate(name);
                }
            }

            Map<String, Template> endpointTemplates = synapseConfiguration.getEndpointTemplates();
            for (String name : endpointTemplates.keySet()) {
                CAppArtifactData cAppEndpointTemplateData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.TEMPLATE_TYPE + File.separator + name);
                if (cAppEndpointTemplateData != null) {
                    Template newEndpointTemplate = endpointTemplates.get(name);
                    cAppArtifactConfig.addEndpointTemplate(name, newEndpointTemplate);
                    cAppEndpointTemplateData.setEdited(true);
                    synapseConfiguration.removeEndpointTemplate(name);
                }
            }

            Collection<API> apiCollection = synapseConfiguration.getAPIs();
            for (API api : apiCollection) {
                CAppArtifactData cAppApiData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.API_TYPE + File.separator + api.getName());
                if (cAppApiData != null) {
                    API newApi = api;
                    cAppArtifactConfig.addAPI(api.getName(), newApi);
                    cAppApiData.setEdited(true);
                    synapseConfiguration.removeAPI(api.getName());
                }
            }

            Collection<Startup> tasks = synapseConfiguration.getStartups();
            for (Startup task : tasks) {
                CAppArtifactData cAppApiData = getCAppArtifactDataByName(tenantId, ServiceBusConstants.TASK_TYPE + File.separator + task.getName());
                if (cAppApiData != null) {
                    Startup newTask = task;
                    cAppArtifactConfig.addStartup(newTask);
                    cAppApiData.setEdited(true);
                    synapseConfiguration.removeStartup(task.getName());
                }

            }

            cAppArtifactWrapper.setcAppArtifactConfig(cAppArtifactConfig);
            cAppArtifactWrapper.setNewConfig(synapseConfiguration);
            return cAppArtifactWrapper;
        } finally {
            lock.unlock();
        }
    }

    protected Lock getLock(AxisConfiguration axisConfig) {
        Parameter p = axisConfig.getParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
        if (p != null) {
            return (Lock) p.getValue();
        } else {
            log.warn(ServiceBusConstants.SYNAPSE_CONFIG_LOCK + " is null, Recreating a new lock");
            Lock lock = new ReentrantLock();
            try {
                axisConfig.addParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK, lock);
                return lock;
            } catch (AxisFault axisFault) {
                log.error("Error while setting " + ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
            }
        }

        return null;
    }

}