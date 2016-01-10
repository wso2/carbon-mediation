/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.message.flow.tracer.services;

import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.synapse.messageflowtracer.data.MessageFlowComponentEntry;
import org.apache.synapse.messageflowtracer.data.MessageFlowTraceEntry;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.message.flow.tracer.data.*;
import org.wso2.carbon.message.flow.tracer.datastore.MessageFlowTraceDataStore;
import org.wso2.carbon.message.flow.tracer.util.MessageFlowTraceConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageFlowTracerAdminService extends AbstractServiceBusAdmin {


    public MessageFlowTraceEntry[] getMessageFlows() {
        MessageFlowTraceDataStore messageFlowTraceDataStore = (MessageFlowTraceDataStore) getConfigContext().getProperty(MessageFlowTraceConstants
                                                                                                                                 .MESSAGE_FLOW_TRACE_STORE);

        List<MessageFlowTraceEntry> entries = new ArrayList<>();
        Map<String, List<MessageFlowTraceEntry>> messageFlows = messageFlowTraceDataStore.getMessageFlowDataHolder().getMessageFlows();
        for (Map.Entry<String, List<MessageFlowTraceEntry>> entry : messageFlows.entrySet()) {
            if (entry.getValue() != null && entry.getValue().size() > 0) {
                entries.add(entry.getValue().get(0));
            }
        }

        if (messageFlows.values().size() == 0) {
            return new MessageFlowTraceEntry[1];
        }

        // Sort MessageFlowTraceEntry by date
        Collections.sort(entries, new Comparator<MessageFlowTraceEntry>() {

            private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

            public int compare(MessageFlowTraceEntry o1, MessageFlowTraceEntry o2) {
                if (o1.getTimeStamp() == null || o2.getTimeStamp() == null)
                    return 0;

                try {
                    return dateFormatter.parse(o1.getTimeStamp()).compareTo(dateFormatter.parse(o2.getTimeStamp()));
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        return entries.toArray(new MessageFlowTraceEntry[entries.size()]);
    }

    public MessageFlowComponentEntry[] getComponentInfo(String messageId, String componentId) {
        return new MessageFlowComponentEntry[2];
    }

    public String[] getMessageFlowInLevels(String messageId) {
        MessageFlowTraceDataStore mediationTraceDataStore = (MessageFlowTraceDataStore) getConfigContext().getProperty(MessageFlowTraceConstants
                                                                                                                               .MESSAGE_FLOW_TRACE_STORE);

        String[] messageFlows = mediationTraceDataStore.getMessageFlowDataHolder().getMessageFlowTrace(messageId);
        MessageFlowComponentEntry[] messageFlowComponentEntries = mediationTraceDataStore.getMessageFlowDataHolder().getComponentInfo(messageId);
        FlowPath flowPath = new FlowPath(messageFlows, messageFlowComponentEntries);
        Map<Integer, List<String>> levelMap = new TreeMap<>();
        List<String> initialList = new ArrayList<>();
        initialList.add(flowPath.getHead().getEntries().get(0).getComponentName());
        levelMap.put(0, initialList);
        flowPath.buildFlowWithLevels(levelMap, 1, flowPath.getHead());
        String[] levels = new String[levelMap.size()];

        for (Integer i : levelMap.keySet()) {
            levels[i] = levelMap.get(i).toString();
        }
        return levels;
    }

    public Edge[] getAllEdges(String messageId) {
        MessageFlowTraceDataStore messageFlowTraceDataStore = (MessageFlowTraceDataStore) getConfigContext().getProperty(MessageFlowTraceConstants
                                                                                                                                 .MESSAGE_FLOW_TRACE_STORE);

        String[] messageFlows = messageFlowTraceDataStore.getMessageFlowDataHolder().getMessageFlowTrace(messageId);
        MessageFlowComponentEntry[] messageFlowComponentEntries = messageFlowTraceDataStore.getMessageFlowDataHolder().getComponentInfo(messageId);
        FlowPath flowPath = new FlowPath(messageFlows, messageFlowComponentEntries);

        Map<String, ComponentNode> componentNodeHashMap = flowPath.getNodeMap();

        Set<Edge> edges = new HashSet<>();

        for (ComponentNode node1 : componentNodeHashMap.values()) {
            for (ComponentNode node2 : node1.getNodeList()) {
                edges.add(new Edge(node1.getComponentId(), node2.getComponentId()));
            }
        }
        return edges.toArray(new Edge[edges.size()]);
    }

    public String getAllComponents(String messageId) {
        MessageFlowTraceDataStore messageFlowTraceDataStore = (MessageFlowTraceDataStore) getConfigContext().getProperty(MessageFlowTraceConstants
                                                                                                                                 .MESSAGE_FLOW_TRACE_STORE);

        String[] messageFlows = messageFlowTraceDataStore.getMessageFlowDataHolder().getMessageFlowTrace(messageId);
        MessageFlowComponentEntry[] messageFlowComponentEntries = messageFlowTraceDataStore.getMessageFlowDataHolder().getComponentInfo(messageId);
        FlowPath flowPath = new FlowPath(messageFlows, messageFlowComponentEntries);
        Map<String, ComponentNode> componentNodeHashMap = flowPath.getNodeMap();
        Map<String, Map<String, String>> hoverNodeMap = new HashMap<>();

        for (ComponentNode node : componentNodeHashMap.values()) {
            Map<String, String> propertyMap = new HashMap<>();
            propertyMap.put("label", node.getComponentName());
            if (node.getEntries().size() > 1) {
                propertyMap.put("description", node.getEntries().get(0).isStart() + "<br>" + node.getEntries().get(1)
                        .isStart());
                propertyMap.put("beforepayload", StringEscapeUtils.escapeHtml((node.getEntries().get(0).isStart() ? node.getEntries().get(0)
                        .getPayload() : node.getEntries().get(1).getPayload())));
                propertyMap.put("afterpayload", StringEscapeUtils.escapeHtml((node.getEntries().get(0).isStart() ? node.getEntries().get(1)
                        .getPayload() : node.getEntries().get(0).getPayload())));

                propertyMap.put("beforeproperties", (node.getEntries().get(0).isStart() ? constructPropertyString(
                        node.getEntries().get(0).getPropertyMap(), node.getEntries().get(0).getTransportPropertyMap()) :
                                                     constructPropertyString(
                                                             node.getEntries().get(1).getPropertyMap(), node.getEntries().get(1).getTransportPropertyMap())));
                propertyMap.put("afterproperties", (node.getEntries().get(0).isStart() ? constructPropertyString(
                        node.getEntries().get(1).getPropertyMap(), node.getEntries().get(1).getTransportPropertyMap()) :
                                                    constructPropertyString(
                                                            node.getEntries().get(0).getPropertyMap(), node.getEntries().get(0).getTransportPropertyMap())));
            } else if (node.getEntries().size() == 1) {
                propertyMap.put("description", node.getEntries().get(0).isStart() + "");
                propertyMap.put("beforepayload", node.getEntries().get(0).getPayload());

                propertyMap.put("beforeproperties", constructPropertyString(
                        node.getEntries().get(0).getPropertyMap(), node.getEntries().get(0).getTransportPropertyMap()));
            } else {
                propertyMap.put("description", "N/A");
            }

            if (node.getEntries().size() == 0 || node.getEntries().size() == 1) {
                propertyMap.put("style", "fill: #F00");
            } else {
                if (!node.getEntries().get(0).isResponse()) {
                    propertyMap.put("style", "fill: #D3D3D3");
                } else {
                    propertyMap.put("style", "fill: #D3D3E3");
                }
            }
            hoverNodeMap.put(node.getComponentId(), propertyMap);
        }

        return JSONObject.toJSONString(hoverNodeMap);
    }

    public void clearAll() {
        MessageFlowTraceDataStore messageFlowTraceDataStore = (MessageFlowTraceDataStore) getConfigContext().getProperty
                (MessageFlowTraceConstants.MESSAGE_FLOW_TRACE_STORE);
        messageFlowTraceDataStore.getMessageFlowDataHolder().clearDataStores();
    }

    private String constructPropertyString(Map<String, Object> propertyMap, Map<String, Object> transportPropertyMap) {
        StringBuilder properties = new StringBuilder();
        //transport properties
        for (String transportProperty : transportPropertyMap.keySet()) {
            if (transportPropertyMap.get(transportProperty) instanceof String) {
                properties.append(transportProperty)
                        .append("=")
                        .append(transportPropertyMap.get(transportProperty))
                        .append(",");
            }
        }
        properties.append("=,");
        //general properties
        for (String property : propertyMap.keySet()) {
            properties.append(property)
                    .append("=")
                    .append(propertyMap.get(property))
                    .append(",");
        }
        return properties.toString();
    }
}