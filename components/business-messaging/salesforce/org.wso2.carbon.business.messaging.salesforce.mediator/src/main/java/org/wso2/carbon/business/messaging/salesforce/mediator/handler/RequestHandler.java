/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.business.messaging.salesforce.mediator.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.business.messaging.salesforce.core.SalesforceProxy;
import org.wso2.carbon.business.messaging.salesforce.mediator.InputType;
import org.wso2.carbon.business.messaging.salesforce.mediator.OperationType;

/**
 * <p>
 * Hold the <code>Operation</code> and constructs the request header and the
 * payload of the soap envilope based on the configuratoin in the
 * <code>SalesforceMedaitor</code>.
 * </p>
 *
 * @see org.wso2.carbon.business.messaging.salesforce.mediator.OperationType
 * @see org.wso2.carbon.business.messaging.salesforce.mediator.InputType
 * @see org.wso2.carbon.business.messaging.salesforce.mediator.OutputType
 */
public class RequestHandler {

    /**
     * <p>
     * Holds the log4j based log for the login purposes
     * </p>
     */
    private static final Log log = LogFactory.getLog(RequestHandler.class);

    /**
     * <p>
     * Holds the configuration context to be handled.
     * </p>
     */
    private org.apache.axis2.context.ConfigurationContext configurationContext;
    private static final String SALESFORCE_PROXY_SESSION = "salesforce.proxy.session";

    //private final static Map<String, String[]> parameters = new HashMap<String, String[]>();

    /**
     * Constructor accepting a <code>Operation</code>
     *
     * @param configurationContext Axis Configuration used to invoke client calls to salesforce API  .
     */
    public RequestHandler(
            org.apache.axis2.context.ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

/*
    static {
        parameters.put("login", new String[]{"username", "password"});
        parameters.put("logout", new String[]{});
        parameters.put("query", new String[]{"queryString"});
    }
*/

    /**
     * <p>
     * If both the <code>expression</code> provided then the evaluated string
     * value of the <code>expression</code> over the message will be returned.
     * </p>
     *
     * @param synCtx message to be evaluated.
     * @return the evaluated string value of the <code>expression</code>
     */
    public Object handle(OperationType operation, MessageContext synCtx) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Start %s operation", operation.getName()));
        }
        Object result = null;
        /*Object[] params = new Object[parameters.get(operation.getName()).length];*/
        Object[] params = new Object[operation.getInputs().size()];

        if (!operation.getInputs().isEmpty()) {
            Object obj = operation.evaluate(synCtx);
            int index = 0;
            for (InputType input : operation.getInputs()) {
                String param = input.getName();
                params[index++] = PropertyHandler.getInstanceProperty(param, obj);
            }
        }

        try {
            SalesforceProxy proxyInstance;
            if (synCtx.getProperty(SALESFORCE_PROXY_SESSION) == null) {
                proxyInstance = SalesforceProxyFactory.getSalesforceProxyInstance(configurationContext);
                synCtx.setProperty(SALESFORCE_PROXY_SESSION, proxyInstance);
            } else {
                proxyInstance = (SalesforceProxy) synCtx.getProperty(SALESFORCE_PROXY_SESSION);
            }
            //dispatch operation
            result = PropertyHandler.invoke(proxyInstance, operation.getName(), params);

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error invoking %s operation", operation
                        .getName()));
            }
            handleException(String.format("Error invoking %s operation",
                                          operation.getName()), e);
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("End %s operation", operation
                    .getName()));
            log.debug("Response result: " + result);
        }

        return result;
    }

    /**
     * Logs the exception and wraps the source message into a
     * <code>SynapseException</code> exception.
     *
     * @param msg the source message
     * @param e   the exception
     */
    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
