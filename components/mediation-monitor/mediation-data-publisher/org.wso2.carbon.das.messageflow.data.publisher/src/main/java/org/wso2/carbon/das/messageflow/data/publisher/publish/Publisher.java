/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.das.messageflow.data.publisher.publish;

import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.wso2.carbon.das.data.publisher.util.DASDataPublisherConstants;
import org.wso2.carbon.das.data.publisher.util.PublisherUtil;
import org.wso2.carbon.das.messageflow.data.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.das.messageflow.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.das.messageflow.data.publisher.conf.Property;
import org.wso2.carbon.das.messageflow.data.publisher.util.MediationDataPublisherConstants;
import org.wso2.carbon.das.messageflow.data.publisher.util.PublisherUtils;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;


public class Publisher {
    private static Log log = LogFactory.getLog(Publisher.class);

    public static void process(PublishingFlow publishingFlow, MediationStatConfig mediationStatConfig) {
        List<String> metaDataKeyList = new ArrayList<String>();
        List<String> metaDataValueList = new ArrayList<String>();

        List<Object> eventData = new ArrayList<Object>();

        addMetaData(metaDataKeyList, metaDataValueList, mediationStatConfig);

        try {

            if (mediationStatConfig.isMessageFlowPublishingEnabled()) {

                addEventData(eventData, publishingFlow);
                StreamDefinition streamDef = getComponentStreamDefinition(metaDataKeyList.toArray());
                publishToAgent(eventData, metaDataValueList, mediationStatConfig, streamDef);
            }

        } catch (MalformedStreamDefinitionException e) {
            log.error("Error while creating stream definition object", e);
        }

    }

    private static void addMetaData(List<String> metaDataKeyList, List<String> metaDataValueList,
                                    MediationStatConfig mediationStatConfig) {
//        metaDataValueList.add(PublisherUtil.getHostAddress());
        metaDataValueList.add("true"); // payload-data is in compressed form
        Property[] properties = mediationStatConfig.getProperties();
        if (properties != null) {
            for (Property property : properties) {
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaDataKeyList.add(property.getKey());
                    metaDataValueList.add(property.getValue());
                }
            }
        }
    }

    private static void addEventData(List<Object> eventData, PublishingFlow publishingFlow) {
        eventData.add(publishingFlow.getMessageFlowId());

        Map<String, Object> mapping = publishingFlow.getObjectAsMap();
        mapping.put("host", PublisherUtil.getHostAddress()); // Adding host

        String jsonString = JSONObject.toJSONString(mapping);

        eventData.add(compress(jsonString));
    }


    private static void publishToAgent(List<Object> eventData,
                                       List<String> metaDataValueList,
                                       MediationStatConfig mediationStatConfig,
                                       StreamDefinition streamDef) {

        String serverUrl = mediationStatConfig.getUrl();
        String userName = mediationStatConfig.getUserName();
        String password = mediationStatConfig.getPassword();

        String key = serverUrl + "_" + userName
                     + "_" + password + "_" + streamDef.getName();
        EventPublisherConfig eventPublisherConfig = PublisherUtils.getEventPublisherConfig(key);
        if (!mediationStatConfig.isLoadBalancingEnabled()) {
            AsyncDataPublisher dataPublisher = null;
            try {
                if (eventPublisherConfig == null) {
                    synchronized (Publisher.class) {
                        eventPublisherConfig = new EventPublisherConfig();
                        AsyncDataPublisher asyncDataPublisher = new AsyncDataPublisher(serverUrl, userName, password);
                        asyncDataPublisher.addStreamDefinition(streamDef);


                        eventPublisherConfig.setDataPublisher(asyncDataPublisher);
                        PublisherUtils.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                    }
                }
                dataPublisher = eventPublisherConfig.getDataPublisher();

                dataPublisher.publish(streamDef.getName(), streamDef.getVersion(), metaDataValueList.toArray(), null,
                                      eventData.toArray());

            } catch (AgentException e) {
                log.error("Error occurred while sending the event", e);
            }
        } else {
            try {
                LoadBalancingDataPublisher dataPublisher = null;
                if (eventPublisherConfig == null) {
                    synchronized (Publisher.class) {
                        eventPublisherConfig = new EventPublisherConfig();
                        ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
                        ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(serverUrl);

                        for (String aReceiverGroupURL : receiverGroupUrls) {
                            ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                            String[] urls = aReceiverGroupURL.split(",");
                            for (String aUrl : urls) {
                                DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), userName, password);
                                dataPublisherHolders.add(aNode);
                            }
                            ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                            allReceiverGroups.add(group);
                        }

                        LoadBalancingDataPublisher loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);

                        loadBalancingDataPublisher.addStreamDefinition(streamDef);
                        eventPublisherConfig.setLoadBalancingDataPublisher(loadBalancingDataPublisher);
                        PublisherUtils.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                    }
                }
                dataPublisher = eventPublisherConfig.getLoadBalancingDataPublisher();

                dataPublisher.publish(streamDef.getName(), streamDef.getVersion(), metaDataValueList.toArray(), null,
                                      eventData.toArray());
            } catch (AgentException e) {
                log.error("Error occurred while sending the event", e);
            }
        }
    }

    public static StreamDefinition getComponentStreamDefinition(
            Object[] metaData)
            throws MalformedStreamDefinitionException {
        StreamDefinition eventStreamDefinition = new StreamDefinition(
                MediationDataPublisherConstants.STREAM_NAME,
                MediationDataPublisherConstants.STREAM_VERSION);
        eventStreamDefinition.setNickName("");
        eventStreamDefinition.setDescription("This stream is use by WSO2 ESB to publish component specific data for tracing");
        eventStreamDefinition.addMetaData(DASDataPublisherConstants.DAS_COMPRESSED, AttributeType.STRING);
        for (Object aMetaData : metaData) {
            eventStreamDefinition.addMetaData(aMetaData.toString(), AttributeType.STRING);
        }

        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MESSAGE_ID,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.FLOW_DATA,
                                             AttributeType.STRING);
        return eventStreamDefinition;
    }

    /**
     * Compress the payload
     *
     * @param str
     * @return
     */
    private static String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
            return DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch (IOException e) {
            log.error("Unable to compress data", e);
        }

        return str;
    }
}
