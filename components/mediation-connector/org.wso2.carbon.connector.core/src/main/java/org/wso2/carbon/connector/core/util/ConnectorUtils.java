package org.wso2.carbon.connector.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.template.TemplateContext;
import org.wso2.carbon.connector.core.pool.Configuration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

public class ConnectorUtils {

    public static Object lookupTemplateParamater(MessageContext ctxt, String paramName) {
        Stack<TemplateContext> funcStack = (Stack) ctxt.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
        TemplateContext currentFuncHolder = funcStack.peek();
        Object paramValue =  currentFuncHolder.getParameterValue(paramName);
        return paramValue;
    }

    public static Object lookupTemplateParamater(MessageContext ctxt, int index) {
        Stack<TemplateContext> funcStack = (Stack) ctxt.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
        TemplateContext currentFuncHolder = funcStack.peek();
        Collection paramList = currentFuncHolder.getParameters();
        Iterator it = paramList.iterator();
        int i = 0;
        while (it.hasNext()) {
            String param = (String) it.next();
            if (i == index) {
                return param;
            }
            i++;
        }
        return null;
    }

    /**
     * Get pool configuration from template parameters
     *
     * @param messageContext Message Context
     * @return Pool Configuration
     */
    public static Configuration getPoolConfiguration(MessageContext messageContext) {

        Configuration configuration = new Configuration();
        String maxActiveConnections = (String) lookupTemplateParamater(messageContext,
                Constants.MAX_ACTIVE_CONNECTIONS);
        String maxIdleConnections = (String) lookupTemplateParamater(messageContext,
                Constants.MAX_IDLE_CONNECTIONS);
        String maxWaitTime = (String) lookupTemplateParamater(messageContext,
                Constants.MAX_WAIT_TIME);
        String minEvictionTime = (String) lookupTemplateParamater(messageContext,
                Constants.MAX_EVICTION_TIME);
        String evictionCheckInterval = (String) lookupTemplateParamater(messageContext,
                Constants.EVICTION_CHECK_INTERVAL);
        String exhaustedAction = (String) lookupTemplateParamater(messageContext,
                Constants.EXHAUSTED_ACTION);

        if (!StringUtils.isEmpty(maxActiveConnections)) {
            configuration.setMaxActiveConnections(Integer.parseInt(maxActiveConnections));
        }
        if (!StringUtils.isEmpty(maxWaitTime)) {
            configuration.setMaxWaitTime(Long.parseLong(maxWaitTime));
        }
        if (!StringUtils.isEmpty(maxIdleConnections)) {
            configuration.setMaxIdleConnections(Integer.parseInt(maxIdleConnections));
        }
        if (!StringUtils.isEmpty(minEvictionTime)) {
            configuration.setMinEvictionTime(Long.parseLong(minEvictionTime));
        }
        if (!StringUtils.isEmpty(evictionCheckInterval)) {
            configuration.setEvictionCheckInterval(Long.parseLong(evictionCheckInterval));
        }
        if (!StringUtils.isEmpty(exhaustedAction)) {
            configuration.setExhaustedAction(exhaustedAction);
        }
        return configuration;
    }
}
