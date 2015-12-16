package org.wso2.carbon.message.flow.tracer.services;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.message.flow.tracer.MessageFlowTraceDataStore;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class MessageFlowTraceServiceImpl implements MessageFlowTraceService {

    private MessageFlowTraceDataStore tenantTraceStore;

    private int tenantId = MultitenantConstants.SUPER_TENANT_ID;

    private ConfigurationContext configurationContext;

    public MessageFlowTraceServiceImpl(MessageFlowTraceDataStore messageFlowTraceDataStore,
                                       int tenantId, ConfigurationContext configurationContext) {
        tenantTraceStore = messageFlowTraceDataStore;
        this.tenantId = tenantId;
        this.configurationContext = configurationContext;
    }

    public MessageFlowTraceDataStore getTraceDataStore() {
        return tenantTraceStore;
    }

    public int getTenantId() {
        return tenantId;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }
}
