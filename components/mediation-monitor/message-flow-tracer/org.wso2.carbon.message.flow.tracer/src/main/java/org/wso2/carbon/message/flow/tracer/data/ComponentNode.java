package org.wso2.carbon.message.flow.tracer.data;

import org.apache.synapse.flowtracer.data.MessageFlowComponentEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
