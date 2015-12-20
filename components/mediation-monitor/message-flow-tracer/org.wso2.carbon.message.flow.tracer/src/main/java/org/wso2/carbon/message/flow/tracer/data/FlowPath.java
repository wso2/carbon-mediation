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
package org.wso2.carbon.message.flow.tracer.data;

import org.apache.synapse.messageflowtracer.data.MessageFlowComponentEntry;

import java.util.*;

/**
 * Represents the nodes and their connectivity in the graph structure
 */
public class FlowPath {

    private ComponentNode head;
    private Map<String,ComponentNode> nodeMap;

    public FlowPath(String[] flows, MessageFlowComponentEntry[] componentNodes){
        nodeMap = new HashMap<>();

        for (MessageFlowComponentEntry messageFlowComponentEntry:componentNodes){
            if(nodeMap.containsKey(messageFlowComponentEntry.getComponentId())){
                nodeMap.get(messageFlowComponentEntry.getComponentId()).getEntries().add(messageFlowComponentEntry);
            }
            else{
                nodeMap.put(messageFlowComponentEntry.getComponentId(),new ComponentNode(messageFlowComponentEntry.getComponentId(),messageFlowComponentEntry.getComponentName(),messageFlowComponentEntry));
            }
        }

        List<String>[] flowList = new ArrayList[flows.length];

        for (int i = 0; i < flows.length; i++) {
            if(flowList[i]==null){
                flowList[i] = new ArrayList<>();
            }

            String[] componentIds = flows[i].split("->");

            for (String c:componentIds){
                if(!c.trim().equals("")){
                    flowList[i].add(c.trim());
                }
            }
        }

        head = nodeMap.get(flowList[0].get(0));

        for (List<String> flow:flowList){
            processPath(flow,1,head);
        }
    }

    public void processPath(List<String> flow, int position, ComponentNode previous){
        if(position>=flow.size())
            return;

        previous.addNode(nodeMap.get(flow.get(position)));
        processPath(flow,position+1,nodeMap.get(flow.get(position)));
    }

    public void buildFlowWithLevels(Map<Integer,List<String>> map, int level, ComponentNode currentNode){
        for(ComponentNode node:currentNode.getNodeList()){
            if (map.containsKey(level)) {
                map.get(level).add(node.getEntries().get(0).getComponentName());
            } else {
                List<String> list = new ArrayList<>();
                list.add(node.getEntries().get(0).getComponentName());
                map.put(level, list);
            }
            buildFlowWithLevels(map,level+1,node);
        }
    }

    public Map<String, ComponentNode> getNodeMap() {
        return nodeMap;
    }

    public ComponentNode getHead() {
        return head;
    }
}
