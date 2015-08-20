package org.wso2.carbon.mediation.initializer.handler;

import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.ProxyService;

/**
 * Created by nadeeshaan on 7/8/15.
 */
public class ProxyLogHandler extends AbstractSynapseHandler {
    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {
        handleLogAppenderSetter(messageContext);
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        handleLogAppenderSetter(messageContext);
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        handleLogAppenderSetter(messageContext);
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        handleLogAppenderSetter(messageContext);
        return true;
    }

    public void handleLogAppenderSetter (MessageContext synCtx) {
        String proxyName = (String) synCtx.getProperty(SynapseConstants.PROXY_SERVICE);

        if (proxyName != null) {
            ProxyService proxyService = synCtx.getConfiguration().getProxyService(proxyName);
            proxyService.setLogSetterValue();
        }
    }
}
