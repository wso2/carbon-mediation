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
import org.apache.synapse.messageflowtracer.data.MessageFlowComponentEntry;
import org.apache.synapse.messageflowtracer.data.MessageFlowDataEntry;
import org.apache.synapse.messageflowtracer.data.MessageFlowTraceEntry;
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

import java.util.ArrayList;
import java.util.List;


public class Publisher {


    private static Log log = LogFactory.getLog(Publisher.class);
    private static boolean isStreamDefinitionAlreadyExist = false;

    public static void process(MessageFlowDataEntry dataEntry, MediationStatConfig mediationStatConfig) {
        List<String> metaDataKeyList = new ArrayList<String>();
        List<String> metaDataValueList = new ArrayList<String>();

        List<Object> eventData = new ArrayList<Object>();

        addMetaData(metaDataKeyList, metaDataValueList, mediationStatConfig);

        try {

            if (mediationStatConfig.isMessageFlowTracePublishingEnabled()) {
                if (dataEntry instanceof MessageFlowTraceEntry) {
                    addEventData(eventData, (MessageFlowTraceEntry) dataEntry);
                    StreamDefinition streamDef = getTraceStreamDefinition(mediationStatConfig, metaDataKeyList.toArray());
                    publishToAgent(eventData, metaDataKeyList, metaDataValueList, mediationStatConfig, streamDef);
                } else if (dataEntry instanceof MessageFlowComponentEntry) {
                    addEventData(eventData, (MessageFlowComponentEntry) dataEntry);
                    StreamDefinition streamDef = getComponentStreamDefinition(mediationStatConfig, metaDataKeyList.toArray());
                    publishToAgent(eventData, metaDataKeyList, metaDataValueList, mediationStatConfig, streamDef);
                } else {
                    log.error("Invalid entry type for update.");
                    return;
                }
            }

        } catch (MalformedStreamDefinitionException e) {
            log.error("Error while creating stream definition object", e);
            return;
        }

    }


    private static void addMetaData(List<String> metaDataKeyList, List<String> metaDataValueList,
                                    MediationStatConfig mediationStatConfig) {
        metaDataValueList.add(PublisherUtil.getHostAddress());
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


    private static void addEventData(List<Object> eventData, MessageFlowTraceEntry messageFlowTraceEntryData) {
        eventData.add(messageFlowTraceEntryData.getMessageId());
        eventData.add(messageFlowTraceEntryData.getEntryType());
        eventData.add(messageFlowTraceEntryData.getMessageFlow());
        eventData.add(messageFlowTraceEntryData.getTimeStamp());
    }


    private static void addEventData(List<Object> eventData, MessageFlowComponentEntry traceComponentData) {
        eventData.add(traceComponentData.getMessageId());
        eventData.add(traceComponentData.getComponentId());
        eventData.add(traceComponentData.getComponentName());
        eventData.add(traceComponentData.getTimestamp());
        eventData.add(traceComponentData.getPayload());
        eventData.add(traceComponentData.isResponse());
        eventData.add(traceComponentData.isStart());
        eventData.add(JSONObject.toJSONString(traceComponentData.getPropertyMap()));
        eventData.add(JSONObject.toJSONString(traceComponentData.getTransportPropertyMap()));
    }


    private static void publishToAgent(List<Object> eventData,
                                       List<String> metaDataKeyList,
                                       List<String> metaDataValueList,
                                       MediationStatConfig mediationStatConfig,
                                       StreamDefinition streamDef) {

        String streamId = null;

        String serverUrl = mediationStatConfig.getUrl();
        String userName = mediationStatConfig.getUserName();
        String passWord = mediationStatConfig.getPassword();
            /*
            String serverUrl = "tcp://127.0.0.1:7611";
            String userName = "admin";
            String passWord = "admin";
            */

        String key = serverUrl + "_" + userName
                     + "_" + passWord + "_" + streamDef.getName();
        EventPublisherConfig eventPublisherConfig = PublisherUtils.getEventPublisherConfig(key);
        if (!mediationStatConfig.isLoadBalancingEnabled()) {
            AsyncDataPublisher dataPublisher = null;
            try {
                if (eventPublisherConfig == null) {
                    synchronized (Publisher.class) {
                        eventPublisherConfig = new EventPublisherConfig();
                        AsyncDataPublisher asyncDataPublisher = new AsyncDataPublisher(serverUrl,
                                                                                       userName,
                                                                                       passWord);
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
                                DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), userName,
                                                                                    passWord);
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
            MediationStatConfig mediationStatConfig,
            Object[] metaData)
            throws MalformedStreamDefinitionException {
        StreamDefinition eventStreamDefinition = new StreamDefinition("esb-flow_tracer-component_entry-stream", "1.0.0");
        eventStreamDefinition.setNickName("");
        eventStreamDefinition.setDescription("This stream is use by WSO2 ESB to publish component specific data for tracing");
        eventStreamDefinition.addMetaData(DASDataPublisherConstants.DAS_HOST, AttributeType.STRING);
        for (int i = 0; i < metaData.length; i++) {
            eventStreamDefinition.addMetaData(metaData[i].toString(), AttributeType.STRING);
        }
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MESSAGE_ID,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.COMPONENT_ID,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.COMPONENT_NAME,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.TRACE_TIMESTAMP,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.PAYLOAD,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.RESPONSE,
                                             AttributeType.BOOL);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.START,
                                             AttributeType.BOOL);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.PROPERTY_MAP,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.TRANSPORT_PROPERTY_MAP,
                                             AttributeType.STRING);
        return eventStreamDefinition;
    }

    public static StreamDefinition getTraceStreamDefinition(
            MediationStatConfig mediationStatConfig,
            Object[] metaData)
            throws MalformedStreamDefinitionException {
        StreamDefinition eventStreamDefinition = new StreamDefinition("esb-flow_tracer-trace_entry-stream", "1.0.0");
        eventStreamDefinition.setNickName("");
        eventStreamDefinition.setDescription("WSO2 ESB publish message flow trace events through this");
        eventStreamDefinition.addMetaData(DASDataPublisherConstants.DAS_HOST, AttributeType.STRING);
        for (int i = 0; i < metaData.length; i++) {
            eventStreamDefinition.addMetaData(metaData[i].toString(), AttributeType.STRING);
        }
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MESSAGE_ID,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.ENTRY_TYPE,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MESSAGE_FLOW,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.TRACE_TIMESTAMP,
                                             AttributeType.STRING);
        return eventStreamDefinition;
    }

}
