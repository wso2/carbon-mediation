package org.wso2.carbon.message.flow.tracer;

import org.apache.synapse.flowtracer.data.MessageFlowComponentEntry;
import org.apache.synapse.flowtracer.data.MessageFlowTraceEntry;
import org.apache.synapse.flowtracer.MessageFlowDbConnector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.wso2.carbon.message.flow.tracer.data.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class MessageFlowTracerService {

    private MessageFlowDbConnector messageFlowDbConnector;

    public MessageFlowTracerService() {
        messageFlowDbConnector = MessageFlowDbConnector.getInstance();
    }

    public MessageFlowTraceEntry[] getMessageFlows(){
        Map<String,MessageFlowTraceEntry> messageFlows = messageFlowDbConnector.getMessageFlows();

        if(messageFlows.values().size()==0)
            return new MessageFlowTraceEntry[1];

        return messageFlows.values().toArray(new MessageFlowTraceEntry[messageFlows.values().size()]);
    }

    public MessageFlowComponentEntry[] getComponentInfo(String messageId, String componentId){
        return new MessageFlowComponentEntry[2];
    }

    public String[] getMessageFlowInLevels(String messageId){
        String[] messageFlows = messageFlowDbConnector.getMessageFlowTrace(messageId);
        MessageFlowComponentEntry[] messageFlowComponentEntries = messageFlowDbConnector.getComponentInfo(messageId);
        FlowPath flowPath = new FlowPath(messageFlows,messageFlowComponentEntries);
        Map<Integer,List<String>> levelMap = new TreeMap<>();
        List<String> initialList = new ArrayList<>();
        initialList.add(flowPath.getHead().getEntries().get(0).getComponentName());
        levelMap.put(0,initialList);
        flowPath.buildFlowWithLevels(levelMap,1,flowPath.getHead());
        String[] levels = new String[levelMap.size()];

        for(Integer i:levelMap.keySet()){
            levels[i] = levelMap.get(i).toString();
        }

        return levels;
    }

    public Edge[] getAllEdges(String messageId){

        String[] messageFlows = messageFlowDbConnector.getMessageFlowTrace(messageId);
        MessageFlowComponentEntry[] messageFlowComponentEntries = messageFlowDbConnector.getComponentInfo(messageId);
        FlowPath flowPath = new FlowPath(messageFlows,messageFlowComponentEntries);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;

        Map<String, ComponentNode> componentNodeHashMap = flowPath.getNodeMap();

        Set<Edge> edges = new HashSet<>();

        for(ComponentNode node1:componentNodeHashMap.values()){
            for(ComponentNode node2:node1.getNodeList()){
                edges.add(new Edge(node1.getComponentId(),node2.getComponentId()));
            }
        }

        return edges.toArray(new Edge[edges.size()]);
    }

    public String getAllComponents(String messageId){
        String[] messageFlows = messageFlowDbConnector.getMessageFlowTrace(messageId);
        MessageFlowComponentEntry[] messageFlowComponentEntries = messageFlowDbConnector.getComponentInfo(messageId);
        FlowPath flowPath = new FlowPath(messageFlows,messageFlowComponentEntries);
        String jsonString = null;
        Map<String, ComponentNode> componentNodeHashMap = flowPath.getNodeMap();
        Map<String, Map<String, String>> hoverNodeMap = new HashMap<>();

        for(ComponentNode node:componentNodeHashMap.values()){
            Map<String, String> propertyMap = new HashMap<>();
            propertyMap.put("label",node.getComponentName());
            if(node.getEntries().size()>1) {
                propertyMap.put("description", node.getEntries().get(0).isStart() + "<br>" + node.getEntries().get(1).isStart());
                propertyMap.put("beforepayload", (node.getEntries().get(0).isStart()? node.getEntries().get(0).getPayload():node.getEntries().get(1).getPayload()));
                propertyMap.put("afterpayload", (node.getEntries().get(0).isStart()? node.getEntries().get(1).getPayload():node.getEntries().get(0).getPayload()));

                propertyMap.put("beforeproperties", (node.getEntries().get(0).isStart()? node.getEntries().get(0).getPropertySet():node.getEntries().get(1).getPropertySet()));
                propertyMap.put("afterproperties", (node.getEntries().get(0).isStart()? node.getEntries().get(1).getPropertySet():node.getEntries().get(0).getPropertySet()));
            }
            else if(node.getEntries().size() == 1){
                propertyMap.put("description", node.getEntries().get(0).isStart()+"");
                propertyMap.put("beforepayload", node.getEntries().get(0).getPayload());

                propertyMap.put("beforeproperties", node.getEntries().get(0).getPropertySet());
            }
            else{
                propertyMap.put("description", "N/A");
            }

            if(node.getEntries().size()==0 || node.getEntries().size()==1){
                propertyMap.put("style","fill: #F00");
            }
            else{
                if(!node.getEntries().get(0).isResponse()){
                    propertyMap.put("style","fill: #0FF");
                }
                else{
                    propertyMap.put("style","fill: #0F0");
                }
            }

            hoverNodeMap.put(node.getComponentId(), propertyMap);
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            jsonString = mapper.writeValueAsString(hoverNodeMap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    public void clearAll(){
        messageFlowDbConnector.clearTables();
    }
}