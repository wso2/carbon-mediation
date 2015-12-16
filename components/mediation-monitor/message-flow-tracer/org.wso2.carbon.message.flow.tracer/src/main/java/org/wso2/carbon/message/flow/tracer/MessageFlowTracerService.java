package org.wso2.carbon.message.flow.tracer;

import net.minidev.json.JSONObject;
import org.apache.synapse.flowtracer.data.MessageFlowComponentEntry;
import org.apache.synapse.flowtracer.data.MessageFlowTraceEntry;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.message.flow.tracer.data.*;

import java.util.*;

public class MessageFlowTracerService extends AbstractServiceBusAdmin {


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
        String jsonString = null;
        Map<String, ComponentNode> componentNodeHashMap = flowPath.getNodeMap();
        Map<String, Map<String, String>> hoverNodeMap = new HashMap<>();

        for (ComponentNode node : componentNodeHashMap.values()) {
            Map<String, String> propertyMap = new HashMap<>();
            propertyMap.put("label", node.getComponentName());
            if (node.getEntries().size() > 1) {
                propertyMap.put("description", node.getEntries().get(0).isStart() + "<br>" + node.getEntries().get(1)
                        .isStart());
                propertyMap.put("beforepayload", (node.getEntries().get(0).isStart() ? node.getEntries().get(0)
                        .getPayload() : node.getEntries().get(1).getPayload()));
                propertyMap.put("afterpayload", (node.getEntries().get(0).isStart() ? node.getEntries().get(1)
                        .getPayload() : node.getEntries().get(0).getPayload()));

                propertyMap.put("beforeproperties", (node.getEntries().get(0).isStart() ? constructPropertyString(
                        node.getEntries().get(0).getPropertyMap()): constructPropertyString(
                        node.getEntries().get(1).getPropertyMap())));
                propertyMap.put("afterproperties", (node.getEntries().get(0).isStart() ? constructPropertyString(
                        node.getEntries().get(1).getPropertyMap()) : constructPropertyString(
                        node.getEntries().get(0).getPropertyMap())));
            } else if (node.getEntries().size() == 1) {
                propertyMap.put("description", node.getEntries().get(0).isStart() + "");
                propertyMap.put("beforepayload", node.getEntries().get(0).getPayload());

                propertyMap.put("beforeproperties", constructPropertyString(
                        node.getEntries().get(0).getPropertyMap()));
            } else {
                propertyMap.put("description", "N/A");
            }

            if (node.getEntries().size() == 0 || node.getEntries().size() == 1) {
                propertyMap.put("style", "fill: #F00");
            } else {
                if (!node.getEntries().get(0).isResponse()) {
                    propertyMap.put("style", "fill: #0FF");
                } else {
                    propertyMap.put("style", "fill: #0F0");
                }
            }
            hoverNodeMap.put(node.getComponentId(), propertyMap);
        }

        jsonString = JSONObject.toJSONString(hoverNodeMap);
        return jsonString;
    }

    public void clearAll() {
        MessageFlowTraceDataStore messageFlowTraceDataStore = (MessageFlowTraceDataStore) getConfigContext().getProperty
                (MessageFlowTraceConstants.MESSAGE_FLOW_TRACE_STORE);
        messageFlowTraceDataStore.getMessageFlowDataHolder().clearDataStores();
    }

    private String constructPropertyString(Map<String, String> propertyMap){
        StringBuilder properties = new StringBuilder();
        for(String property : propertyMap.keySet()){
            properties.append(property)
                    .append("=")
                    .append(propertyMap.get(property))
                    .append(",");
        }
        return properties.toString();
    }
}