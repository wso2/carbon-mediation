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
package org.wso2.carbon.business.messaging.salesforce.mediator;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.business.messaging.salesforce.mediator.handler.RequestHandler;
import org.wso2.carbon.business.messaging.salesforce.mediator.handler.ResponseHandler;

/**
 * <p>
 * Mediates the requests which are extracted from the
 * <code>MessageContext</code> using the specified particular source inputs and
 * maps the response back to the <code>MessageContext</code> using the outputs
 * specified.
 * </p>
 *
 * @see OperationType
 * @see org.apache.synapse.Mediator
 * @see org.apache.synapse.mediators.AbstractMediator
 */
public class SalesforceMediator extends AbstractMediator implements
                                                         ManagedLifecycle {

    private static final Log log = LogFactory.getLog(SalesforceMediator.class);

    /**
     * Holds the default location of the client repository* / public final
     */
    static String DEFAULT_CLIENT_REPO = "";//"./repository/deployment/client";
    /**
     * Holds the default location of the client axis2xml file
     */
    public final static String DEFAULT_AXIS2_XML = "";//"./repository/conf/axis2.xml";

    /**
     * Holds the location of the client repository.
     */
    private String clientRepository = DEFAULT_CLIENT_REPO;

    /**
     * Holds the location of the client axis2xml file
     */
    private String axis2xml = DEFAULT_AXIS2_XML;
    /**
     * The <code>Operation</code> to be invoked.
     *
     * @see org.wso2.carbon.business.messaging.salesforce.mediator.OperationType
     */
    private OperationType operation;

    private RequestHandler requestHandler;

    /**
     * <p>
     * This extracts the <code>Input</code>'s from the
     * <code>MessageContext</code>, invokes the <code>Salesforce</code> specified
     * <code>Operation</code> and formats the response to the specified format
     * in <code>Output</code>.
     * </p>
     *
     * @param synCtx message being passed by Synapse to this mediator
     * @return whether to continue further mediaiton or not as a boolean value
     * @see org.apache.synapse.Mediator#mediate(org.apache.synapse.MessageContext)
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Salesforce mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start dispatching operation: "
                                + operation.getName());
        }

        Object result = null;
        //delegate salesforce operation for this mediator
        try {
            result = requestHandler.handle(operation, synCtx);
        } catch (SynapseException e) {
            synLog.logSynapseException("Error Executing Salesforce Mediator.Exiting further processing..", e);
            return false;
        }

        if (synLog.isTraceTraceEnabled()) {
            synLog.traceTrace("Response payload received : " + result);
        }

        if (result != null) {
            try {
                new ResponseHandler().handle(operation, synCtx, result);
                if (log.isDebugEnabled()) {
                    log.debug("Salesforce Response : " + result);
                }
            } catch (SynapseException e) {
                synLog.logSynapseException("Error Executing Salesforce Mediator Response processing.", e);
                return true;
            }
        } else {
            synLog.traceOrDebug("Service returned a null response");
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End dispatching operation: "
                                + operation.getName());
        }

        if (synLog.isTraceOrDebugEnabled()) {

            synLog.traceOrDebug("End : Salesforce mediator");
        }

        return true;
    }

    public void init(SynapseEnvironment synEnv) {
        try {
            ConfigurationContext cfgCtx;
            if (!clientRepository.equals(DEFAULT_CLIENT_REPO) && !axis2xml.equals(DEFAULT_AXIS2_XML)) {
                cfgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        clientRepository, axis2xml);
            } else {
                cfgCtx = ((Axis2SynapseEnvironment) synEnv).getAxis2ConfigurationContext();
                if (log.isDebugEnabled()) {
                    log.debug("Using default synapse Axis2 Configuration");
                }
            }
            requestHandler = new RequestHandler(cfgCtx);
            if (log.isDebugEnabled()) {
                log.debug("Salesforce mediator was initialized successfully");
            }
        } catch (AxisFault e) {
            String msg = "Error initializing salesforce mediator : "
                         + e.getMessage();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }

    /**
     * Getter method for <code>operation</code>
     *
     * @return the operation the operation to invoke.
     */
    public OperationType getOperation() {
        return operation;
    }

    /**
     * Setter method for <code>operation</code>
     *
     * @param operation the operation to invoke
     */
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public void destroy() {
        log.debug("Salesforce mediator was destroyed successfully");

    }

    /**
     * @return the clientRepository
     */
    public String getClientRepository() {
        return clientRepository;
    }

    /**
     * @param clientRepository the clientRepository to set
     */
    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * @return the axis2xml
     */
    public String getAxis2xml() {
        return axis2xml;
    }

    /**
     * @param axis2xml the axis2xml to set
     */
    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

}
