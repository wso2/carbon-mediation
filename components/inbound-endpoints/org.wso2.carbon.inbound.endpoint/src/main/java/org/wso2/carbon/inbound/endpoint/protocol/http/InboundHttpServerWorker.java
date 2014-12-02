/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http;


import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.passthru.ServerWorker;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;

import java.io.OutputStream;

/**
 * Create SynapseMessageContext from HTTP Request and inject it to the sequence in a synchronous manner
 * This is the worker for HTTP inbound related requests.
 */
public class InboundHttpServerWorker extends ServerWorker {

    private static final Log log = LogFactory.getLog(InboundHttpServerWorker.class);

    private SourceRequest request = null;

    private InboundHttpConfiguration inboundHttpConfiguration;


    public InboundHttpServerWorker(SourceRequest sourceRequest, SourceConfiguration sourceConfiguration,
                                   InboundHttpConfiguration inboundHttpConfiguration, OutputStream outputStream) {
        super(sourceRequest, sourceConfiguration, outputStream);
        this.inboundHttpConfiguration = inboundHttpConfiguration;
        this.request = sourceRequest;
    }

    public void run() {
        if (request != null) {
            try {
                //create Synapse Message Context
                org.apache.synapse.MessageContext msgCtx = createSynapseMessageContext(request);
                MessageContext messageContext = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();

                // setting Inbound related properties
                setInboundProperties(msgCtx);

                String method = request.getRequest() != null ? request.getRequest().getRequestLine().getMethod().
                        toUpperCase() : "";
                processHttpRequestUri(messageContext, method);

                // Get injecting sequence for synapse engine
                SequenceMediator injectingSequence = (SequenceMediator) inboundHttpConfiguration.
                        getSynapseEnvironment().getSynapseConfiguration().
                        getSequence(inboundHttpConfiguration.getInjectSeq());

                if (injectingSequence != null) {
                    injectingSequence.setErrorHandler(inboundHttpConfiguration.getFaultSeq());
                    if (log.isDebugEnabled()) {
                        log.debug("injecting message to sequence : " + inboundHttpConfiguration.getInjectSeq());
                    }
                } else {
                    log.error("Sequence: " + inboundHttpConfiguration.getInjectSeq() + " not found");
                }

                if (!isRESTRequest(messageContext, method)) {
                    if (request.isEntityEnclosing()) {
                        processEntityEnclosingRequest(messageContext, false);
                    } else {
                        processNonEntityEnclosingRESTHandler(null, messageContext, false);
                    }
                }
                // handover synapse message context to synapse environment for inject it to given sequence in
                //synchronous manner
                inboundHttpConfiguration.getSynapseEnvironment().injectMessage(msgCtx, injectingSequence);

                // send ack for client if needed
                sendAck(messageContext);
            } catch (Exception e) {
                log.error("Exception occurred when running " + InboundHttpServerWorker.class.getName(), e);
            }
        } else {
            log.error("InboundSourceRequest cannot be null");
        }
    }


    // Create Synapse Message Context
    private org.apache.synapse.MessageContext createSynapseMessageContext(SourceRequest inboundSourceRequest) {
        org.apache.synapse.MessageContext msgCtx = inboundHttpConfiguration.getSynapseEnvironment().createMessageContext();
        MessageContext messageContext = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        createMessageContext(messageContext, inboundSourceRequest);
        return msgCtx;
    }

    // Setting Inbound Related Properties
    private void setInboundProperties(org.apache.synapse.MessageContext msgContext) {
        msgContext.setProperty(SynapseConstants.IS_INBOUND, true);
        msgContext.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER,
                new InboundHttpResponseSender());
        msgContext.setWSAAction(request.getHeaders().get(InboundHttpConstants.SOAP_ACTION));
    }

}
