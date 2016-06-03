/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.das.messageflow.data.publisher.conf;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.das.data.publisher.util.DASDataPublisherConstants;
import org.wso2.carbon.das.messageflow.data.publisher.internal.MessageFlowDataPublisherDataHolder;
import org.wso2.carbon.das.messageflow.data.publisher.util.MediationDataPublisherConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RegistryPersistenceManager {

    private static Log log = LogFactory.getLog(RegistryPersistenceManager.class);
    public static final String EMPTY_STRING = "";

    private static String IDS = "ids";

    /**
     * Get a DAS server information
     *
     * @param serverId
     * @param tenantId
     * @return
     */
    public PublisherConfig get(String serverId, int tenantId) {
        PublisherConfig PublisherConfig = new PublisherConfig();
        RegistryService dasRegistryService = MessageFlowDataPublisherDataHolder.getInstance().getRegistryService();

        // First set it to defaults, but do not persist
        PublisherConfig.setMessageFlowPublishingEnabled(false);
        PublisherConfig.setUrl(EMPTY_STRING);
        PublisherConfig.setUserName(EMPTY_STRING);
        PublisherConfig.setPassword(EMPTY_STRING);

        try {
            Registry registry = dasRegistryService.getConfigSystemRegistry(tenantId);
            String resourcePath = MediationDataPublisherConstants.DAS_MEDIATION_MESSAGE_FLOW_REG_PATH + serverId;
            Properties configs = null;

            if (registry != null && registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                configs = resource.getProperties();
            } else {
                log.error("Resource not found from registry: " + resourcePath);
                return null;
            }

            if (configs != null) {
                String serverIdRecorded = getPropertyFromList(DASDataPublisherConstants.DAS_SERVER_ID, configs);
                String url = getPropertyFromList(DASDataPublisherConstants.DAS_URL, configs);
                String userName = getPropertyFromList(DASDataPublisherConstants.DAS_USER_NAME, configs);
                String password = getPropertyFromList(DASDataPublisherConstants.DAS_PASSWORD, configs);
                String tracePublishingEnable = getPropertyFromList(DASDataPublisherConstants.DAS_PUBLISHING_ENABLED, configs);

                if (url != null && userName != null && password != null) {
                    PublisherConfig.setMessageFlowPublishingEnabled(Boolean.parseBoolean(tracePublishingEnable));
                    PublisherConfig.setServerId(serverIdRecorded);
                    PublisherConfig.setUrl(url);
                    PublisherConfig.setUserName(userName);
                    PublisherConfig.setPassword(password);
                }
            }
        } catch (Exception e) {
            log.error("Could not load values from registry", e);
        }

        return PublisherConfig;
    }

    /**
     * Update a DAS server information
     *
     * @param tenantId
     * @param config
     */
    public void update(int tenantId, PublisherConfig config) {
        RegistryService dasRegistryService = MessageFlowDataPublisherDataHolder.getInstance().getRegistryService();

        try {
            Registry registry = dasRegistryService.getConfigSystemRegistry(tenantId);
            String serverId = config.getServerId();
            String resourcePath = MediationDataPublisherConstants.DAS_MEDIATION_MESSAGE_FLOW_REG_PATH + serverId;
            Resource resource;

            if (registry != null) {
                if (registry.resourceExists(resourcePath)) {
                    resource = registry.get(resourcePath);
                } else {
                    resource = registry.newResource();
                }

                resource.setProperty(DASDataPublisherConstants.DAS_SERVER_ID, config.getServerId());
                resource.setProperty(DASDataPublisherConstants.DAS_URL, config.getUrl());
                resource.setProperty(DASDataPublisherConstants.DAS_USER_NAME, config.getUserName());
                resource.setProperty(DASDataPublisherConstants.DAS_PASSWORD, config.getPassword());
                resource.setProperty(DASDataPublisherConstants.DAS_PUBLISHING_ENABLED, String.valueOf(config.isMessageFlowPublishingEnabled()));

                // update registry at the end
                registry.put(resourcePath, resource);

                // update the list of server-IDs
                String serverListPath = MediationDataPublisherConstants.DAS_SERVER_LIST_REG_PATH;
                if (registry.resourceExists(serverListPath)) {
                    Resource listResource = registry.get(serverListPath);
                    List<String> idList = listResource.getPropertyValues(IDS);
                    if (idList == null) {
                        idList = new ArrayList<>();
                    }

                    if (!idList.contains(serverId)) {
                        idList.add(serverId);
                    }

                    listResource.setProperty(IDS, idList);
                    registry.put(serverListPath, listResource);
                }
            } else {
                log.error("Resource not found from registry: " + resourcePath);
                return;
            }
        } catch (Exception e) {
            log.error("Could not load values from registry", e);
        }
    }

    /**
     * Get all DAS servers belongs to a tenant
     *
     * @param tenantId
     * @return
     */
    public List<PublisherConfig> load(int tenantId) {
        List<PublisherConfig> publisherConfigList = new ArrayList<>();
        RegistryService dasRegistryService = MessageFlowDataPublisherDataHolder.getInstance().getRegistryService();

        try {
            Registry registry = dasRegistryService.getConfigSystemRegistry(tenantId);
            String serverListPath = MediationDataPublisherConstants.DAS_SERVER_LIST_REG_PATH;
            Resource resource;

            if (registry != null) {
                if (registry.resourceExists(serverListPath)) {
                    resource = registry.get(serverListPath);

                    List<String> idList = resource.getPropertyValues(IDS);

                    if (idList != null) {
                        for (String id : idList) {
                            publisherConfigList.add(this.get(id, tenantId));
                        }
                    }

                } else {
                    resource = registry.newResource();
                    resource.setProperty(IDS, new ArrayList<String>());
                    registry.put(serverListPath, resource);
                }

            }


        } catch (Exception e) {
            log.error("Could not load values from registry", e);
        }

        return publisherConfigList;
    }

    // Get list of DAS servers
    public PublisherConfig[] getAllPublisherNames(int tenantId) {
        List<PublisherConfig> configList = load(tenantId);
        return configList.toArray(new PublisherConfig[configList.size()]);
    }

    /**
     * Remove a DAS server
     *
     * @param tenantId
     * @param serverId
     * @return
     */
    public boolean remove(int tenantId, String serverId) {
        RegistryService dasRegistryService = MessageFlowDataPublisherDataHolder.getInstance().getRegistryService();
        try {
            Registry registry = dasRegistryService.getConfigSystemRegistry(tenantId);
            String resourcePath = MediationDataPublisherConstants.DAS_MEDIATION_MESSAGE_FLOW_REG_PATH + serverId;
            String serverListPath = MediationDataPublisherConstants.DAS_SERVER_LIST_REG_PATH;

            if (registry != null) {
                if (registry.resourceExists(resourcePath)) {
                    registry.delete(resourcePath);
                }

                if (registry.resourceExists(serverListPath)) {
                    Resource listResource = registry.get(serverListPath);
                    List<String> idList = listResource.getPropertyValues(IDS);
                    idList.remove(serverId);
                    listResource.setProperty(IDS, idList);
                    registry.put(serverListPath, listResource);
                }
            } else {
                return false;
            }

        } catch (Exception e) {
            log.error("Could not load values from registry", e);
            return false;
        }

        return true;
    }

    private String getPropertyFromList(String name, Properties configs) {
        String value;
        try {
            value = ((List<String>) configs.get(name)).get(0);
        } catch (Exception e) {
            throw new NullPointerException("Unable to get property : " + name);
        }
        return value;
    }

}
