package org.wso2.carbon.message.flow.tracer.services;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.message.flow.tracer.MessageFlowTraceDataStore;

public interface MessageFlowTraceService {

    public MessageFlowTraceDataStore getTraceDataStore();

    public int getTenantId();

    public ConfigurationContext getConfigurationContext();
}
