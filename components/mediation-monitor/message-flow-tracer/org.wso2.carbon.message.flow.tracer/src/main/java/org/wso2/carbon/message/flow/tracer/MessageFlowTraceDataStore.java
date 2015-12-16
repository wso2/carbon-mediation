package org.wso2.carbon.message.flow.tracer;

import org.apache.synapse.flowtracer.MessageFlowDataHolder;

public class MessageFlowTraceDataStore {

    private MessageFlowDataHolder messageFlowDataHolder;

    public MessageFlowTraceDataStore(MessageFlowDataHolder messageFlowDataHolder) {
        this.messageFlowDataHolder = messageFlowDataHolder;
    }

    public MessageFlowDataHolder getMessageFlowDataHolder(){
        return messageFlowDataHolder;
    }
}
