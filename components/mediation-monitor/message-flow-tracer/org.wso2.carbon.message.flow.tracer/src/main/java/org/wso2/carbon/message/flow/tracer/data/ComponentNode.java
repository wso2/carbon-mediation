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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents node in the graph structure
 */
public class ComponentNode {

    private String componentId;
    private String componentName;
    private List<MessageFlowComponentEntry> entries;
    private Set<ComponentNode> nodeList;

    public ComponentNode(String componentId, String componentName, MessageFlowComponentEntry entry) {
        this.componentId = componentId;
        this.componentName = componentName;
        this.entries = new ArrayList<>();
        this.entries.add(entry);
        nodeList = new HashSet<>();
    }

    public void addNode(ComponentNode node){
        nodeList.add(node);
    }

    public Set<ComponentNode> getNodeList() {
        return nodeList;
    }

    public List<MessageFlowComponentEntry> getEntries() {
        return entries;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getComponentName() {
        return componentName;
    }
}
