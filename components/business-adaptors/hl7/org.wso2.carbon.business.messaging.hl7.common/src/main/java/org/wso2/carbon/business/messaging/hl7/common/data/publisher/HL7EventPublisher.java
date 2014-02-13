/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.business.messaging.hl7.common.data.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.business.messaging.hl7.common.data.EventPublishConfigHolder;
import org.wso2.carbon.business.messaging.hl7.common.data.MessageData;
import org.wso2.carbon.business.messaging.hl7.common.data.conf.EventPublisherConfig;
import org.wso2.carbon.business.messaging.hl7.common.data.conf.ServerConfig;
import org.wso2.carbon.business.messaging.hl7.common.data.utils.EventConfigUtil;
import org.wso2.carbon.business.messaging.hl7.common.data.utils.StreamDefUtil;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents HL7 data publisher
 */
public class HL7EventPublisher {

    public static final String UNDERSCORE = "_";

    private static Log log = LogFactory.getLog(HL7EventPublisher.class);

    private ServerConfig serverConfig;

    public HL7EventPublisher(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void publish(MessageData message) {

        List<Object> correlationData = EventConfigUtil.getCorrelationData(message);
        List<Object> metaData = EventConfigUtil.getMetaData(message);
        List<Object> payLoadData = EventConfigUtil.getEventData(message);
        Map<String, String> arbitraryDataMap = EventConfigUtil.getExtractedDataMap(message);

        StreamDefinition streamDef = null;

        try {
            streamDef = StreamDefUtil.getStreamDefinition();
        } catch (MalformedStreamDefinitionException e) {
            log.error("Unable to create HL7 StreamDefinition : " + e.getMessage(), e);
        }

        if (streamDef != null) {

            String key = serverConfig.getUrl() + UNDERSCORE + serverConfig.getUsername() + UNDERSCORE + serverConfig.getPassword();
            EventPublisherConfig eventPublisherConfig = EventPublishConfigHolder.getEventPublisherConfig(key);
            if (serverConfig.isLoadBalancingConfig()) {
                loadBalancerPublisher(eventPublisherConfig, streamDef,key, correlationData, metaData, payLoadData, arbitraryDataMap);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("single node receiver mode working.");
                }
                try {
                    if (eventPublisherConfig == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Newly creating publisher configuration.");
                        }
                        synchronized (HL7EventPublisher.class) {
                            eventPublisherConfig = new EventPublisherConfig();
                            AsyncDataPublisher asyncDataPublisher;
                            if (serverConfig.getSecureUrl() != null) {
                                asyncDataPublisher = new AsyncDataPublisher(serverConfig.getSecureUrl(), serverConfig.getUrl(),
                                        serverConfig.getUsername(),
                                        serverConfig.getPassword());
                            } else {
                                asyncDataPublisher = new AsyncDataPublisher(serverConfig.getUrl(),
                                        serverConfig.getUsername(),
                                        serverConfig.getPassword());
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Created stream definition.");
                            }
                            asyncDataPublisher.addStreamDefinition(streamDef);
                            eventPublisherConfig.setAsyncDataPublisher(asyncDataPublisher);
                            if (log.isDebugEnabled()) {
                                log.debug("Adding config info to map.");
                            }
                            EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                        }
                    }

                    AsyncDataPublisher asyncDataPublisher = eventPublisherConfig.getAsyncDataPublisher();

                    asyncDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData),
                            getObjectArray(correlationData),
                            getObjectArray(payLoadData), arbitraryDataMap);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully published data.");
                    }

                } catch (AgentException e) {
                    log.error("Error occurred while sending the event", e);
                }
            }
        }
    }

    private void loadBalancerPublisher(EventPublisherConfig eventPublisherConfig, StreamDefinition streamDef, String key, List<Object> correlationData, List<Object> metaData, List<Object> payLoadData, Map<String, String> arbitraryDataMap) {
        if (log.isDebugEnabled()) {
            log.debug("Load balancing receiver mode working.");
        }
        try {
            if (eventPublisherConfig == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Newly creating publisher configuration.");
                }
                synchronized (HL7EventPublisher.class) {
                    eventPublisherConfig = new EventPublisherConfig();
                    List<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
                    List<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(serverConfig.getUrl());

                    for (String aReceiverGroupURL : receiverGroupUrls) {
                    	List<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                        String[] urls = aReceiverGroupURL.split(ServerConfig.URL_SEPARATOR);
                        for (String aUrl : urls) {
                            if (log.isDebugEnabled()) {
                                log.debug("Adding node: " + aUrl);
                            }
                            DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), serverConfig.getUsername(),
                                    serverConfig.getPassword());
                            dataPublisherHolders.add(aNode);
                        }
                        ReceiverGroup group = new ReceiverGroup((ArrayList<DataPublisherHolder>) dataPublisherHolders);
                        allReceiverGroups.add(group);
                    }

                    LoadBalancingDataPublisher loadBalancingDataPublisher = new LoadBalancingDataPublisher((ArrayList<ReceiverGroup>) allReceiverGroups);

                    if (log.isDebugEnabled()) {
                        log.debug("Created stream definition.");
                    }
                    loadBalancingDataPublisher.addStreamDefinition(streamDef);
                    eventPublisherConfig.setLoadBalancingDataPublisher(loadBalancingDataPublisher);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding config info to map.");
                    }
                    EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                }
            }
            LoadBalancingDataPublisher loadBalancingDataPublisher = eventPublisherConfig.getLoadBalancingDataPublisher();

            loadBalancingDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData), getObjectArray(correlationData),
                    getObjectArray(payLoadData), arbitraryDataMap);
            if (log.isDebugEnabled()) {
                log.debug("Successfully published data.");
            }

        } catch (AgentException e) {
            log.error("Error occurred while sending the event", e);
        }
    }

    private Object[] getObjectArray(List<Object> list) {
        if (list.size() > 0) {
            return list.toArray();
        }
        return null;
    }
}
