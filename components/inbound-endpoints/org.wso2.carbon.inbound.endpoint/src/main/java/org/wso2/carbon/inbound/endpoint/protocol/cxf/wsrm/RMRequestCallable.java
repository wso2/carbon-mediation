/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.transport.TransportUtils;
import org.apache.cxf.continuations.Continuation;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.eclipse.jetty.server.Request;
import org.wso2.carbon.inbound.endpoint.protocol.cxf.wsrm.utils.RMConstants;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Extracts the message from the CXF Exchange
 */
public class RMRequestCallable implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(RMRequestCallable.class);

    private Exchange exchange;
    private Continuation continuation;
    private SynapseEnvironment synapseEnvironment;
    private String injectingSequence;
    private String onErrorSequence;
    private Map<String, String> httpHeaders;
    private String receiver;
    private InboundRMResponseSender inboundRMResponseSender;


    public RMRequestCallable(Exchange exchange, Continuation continuation, SynapseEnvironment synapseEnvironment,
                             String injectingSequence, String onErrorSequence, InboundRMResponseSender inboundRMResponseSender) {
        this.setExchange(exchange);
        this.setContinuation(continuation);
        this.setSynapseEnvironment(synapseEnvironment);
        this.setInjectingSequence(injectingSequence);
        this.setOnErrorSequence(onErrorSequence);
        this.inboundRMResponseSender = inboundRMResponseSender;
        this.httpHeaders = new HashMap<String, String>();
    }

    @Override
    public Boolean call() throws Exception {

        Message message = getExchange().getInMessage();
        //Extract the HttpServletRequest from the message
        Request request = (Request) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        Enumeration headerNames = request.getHeaderNames();

        //Extract the HTTP headers
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            getHttpHeaders().put(headerName, headerValue);
        }

        setReceiver(request.getRequestURL().toString());
        byte[] bytes = (byte[]) message.get(RMConstants.CXF_RM_MESSAGE_PAYLOAD);
        return injectToSynapse(request.getContentType(), request.getCharacterEncoding(), bytes);
    }

    /**
     * Creates the SynapseMessageContext and injects the message in to Synapse for mediation
     *
     * @param contentType       Content type of the request
     * @param characterEncoding Character encoding used
     * @param bytes             Request in bytes
     */
    private boolean injectToSynapse(String contentType, String characterEncoding, byte[] bytes) {

        MessageContext msgCtx = createMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
        boolean isSuccess;

        try {
            msgCtx.setWSAAction(getHttpHeaders().get(RMConstants.SOAP_ACTION));
            setMessageContextProperties(contentType, characterEncoding, msgCtx);

            SOAPEnvelope soapEnvelope = TransportUtils.createSOAPMessage(axis2MsgCtx, new ByteArrayInputStream(bytes), contentType);
            msgCtx.setEnvelope(soapEnvelope);

            if (getInjectingSequence() == null || "".equals(getInjectingSequence())) {
                logger.error("Sequence name not specified. Sequence : " + getInjectingSequence());
                isSuccess = false;
            } else {
                SequenceMediator seq = (SequenceMediator) getSynapseEnvironment().getSynapseConfiguration().
                        getSequence(getInjectingSequence());

                if (seq != null) {
                    seq.setErrorHandler(getOnErrorSequence());
                    if (logger.isDebugEnabled()) {
                        logger.debug("injecting message to sequence : " + getInjectingSequence());
                    }
                    getSynapseEnvironment().injectAsync(msgCtx, seq);
                    isSuccess = true;
                } else {
                    logger.error("Sequence: " + getInjectingSequence() + " not found");
                    isSuccess = false;
                }
            }
            return isSuccess;
        } catch (AxisFault axisFault) {
            logger.error("Error occurred when injecting the incoming request to Synapse", axisFault);
            return false;
        } catch (XMLStreamException e) {
            logger.error("Error occurred when extracting the SOAPEnvelope from the request", e);
            return false;
        }
    }

    /**
     * Sets properties in the MessageContext
     *
     * @param contentType       request content type
     * @param characterEncoding character encoding of the request
     * @param msgCtx            MessageContext
     */
    private void setMessageContextProperties(String contentType, String characterEncoding, MessageContext msgCtx) {
        msgCtx.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
        msgCtx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, characterEncoding);
        msgCtx.setProperty(SynapseConstants.IS_CXF_WS_RM, Boolean.TRUE);
        msgCtx.setProperty(RMConstants.CXF_CONTINUATION, getContinuation());
        msgCtx.setProperty(RMConstants.CXF_EXCHANGE, getExchange());
        msgCtx.setProperty(SynapseConstants.IS_INBOUND, Boolean.TRUE);
        msgCtx.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER, inboundRMResponseSender);
    }

    /**
     * Creates a Synapse MessageContext from the request details
     *
     * @return Synapse MessageContext
     */
    private MessageContext createMessageContext() {

        MessageContext messageContext = this.getSynapseEnvironment().createMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) messageContext).getAxis2MessageContext();

        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUID.randomUUID().toString());

        String restUrlPostfix = NhttpUtil
                .getRestUrlPostfix(getReceiver(), axis2MsgCtx.getConfigurationContext().getServicePath());
        String servicePrefix = getReceiver().substring(0, getReceiver().indexOf(restUrlPostfix));
        axis2MsgCtx.setProperty(PassThroughConstants.SERVICE_PREFIX, servicePrefix);
        axis2MsgCtx.setProperty(PassThroughConstants.REST_URL_POSTFIX, restUrlPostfix);

        messageContext.setTo(new EndpointReference(getReceiver()));
        messageContext.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING, Boolean.FALSE);

        return messageContext;
    }


    public Exchange getExchange() {
        return exchange;
    }

    public final void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public Continuation getContinuation() {
        return continuation;
    }

    public final void setContinuation(Continuation continuation) {
        this.continuation = continuation;
    }

    public SynapseEnvironment getSynapseEnvironment() {
        return synapseEnvironment;
    }

    public final void setSynapseEnvironment(SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
    }

    public String getInjectingSequence() {
        return injectingSequence;
    }

    public final void setInjectingSequence(String injectingSequence) {
        this.injectingSequence = injectingSequence;
    }

    public String getOnErrorSequence() {
        return onErrorSequence;
    }

    public final void setOnErrorSequence(String onErrorSequence) {
        this.onErrorSequence = onErrorSequence;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public String getReceiver() {
        return receiver;
    }

    public final void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
