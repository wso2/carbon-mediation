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
package org.wso2.carbon.endpoint.ext.wsrm;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.transport.TransportUtils;
import org.apache.cxf.continuations.Continuation;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.mediators.MediatorFaultHandler;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Request;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.inbound.endpoint.osgi.service.ServiceReferenceHolder;
import org.wso2.carbon.endpoint.ext.wsrm.utils.RMConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Extracts the message from the CXF Exchange
 */
public class RMRequestCallable implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(RMRequestCallable.class);

    private Exchange exchange;
    private Continuation continuation;
    private String injectingSequence;
    private String onErrorSequence;
    private Map<String, String> httpHeaders;
    private String receiver;
    private InboundRMResponseSender inboundRMResponseSender;


    public RMRequestCallable(Exchange exchange, Continuation continuation,
                             String injectingSequence, String onErrorSequence, InboundRMResponseSender inboundRMResponseSender) {
        this.setExchange(exchange);
        this.setContinuation(continuation);
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
        return injectToSynapse(request, bytes);
    }

    /**
     * Creates the SynapseMessageContext and injects the message in to Synapse for mediation
     * @param request The HttpServletRequest
     * @param bytes Request in bytes
     * @return success
     * @throws org.apache.axis2.AxisFault
     */
    private boolean injectToSynapse(Request request, byte[] bytes) throws AxisFault {

        String contentType = request.getContentType();
        MessageContext msgCtx = createMessageContext(request.getUri());
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
        boolean isSuccess;

        try {
            msgCtx.setWSAAction(getHttpHeaders().get(RMConstants.SOAP_ACTION));
            setMessageContextProperties(contentType, request.getCharacterEncoding() , msgCtx);

            SOAPEnvelope soapEnvelope = TransportUtils.createSOAPMessage(axis2MsgCtx, new ByteArrayInputStream(bytes), contentType);
            msgCtx.setEnvelope(soapEnvelope);
            msgCtx.getConfiguration();

            if (getInjectingSequence() == null || "".equals(getInjectingSequence())) {
                logger.error("Sequence name not specified. Sequence : " + getInjectingSequence());
                isSuccess = false;
            } else {
                SequenceMediator seq = (SequenceMediator) msgCtx.getSequence(getInjectingSequence());
                if (seq != null) {
                    SequenceMediator faultSequence = (SequenceMediator) msgCtx.getSequence(getOnErrorSequence());
                    MediatorFaultHandler mediatorFaultHandler = new MediatorFaultHandler(faultSequence);
                    msgCtx.pushFaultHandler(mediatorFaultHandler);
                    if (logger.isDebugEnabled()) {
                        logger.debug("injecting message to sequence : " + getInjectingSequence());
                    }
                    msgCtx.getEnvironment().injectMessage(msgCtx, seq);
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
     * @param uri the uri, the request was sent to
     */
    private MessageContext createMessageContext(HttpURI uri) throws AxisFault {

        String tenantDomain = getTenantDomain(uri.toString());
        // Create super tenant message context
        org.apache.axis2.context.MessageContext axis2MsgCtx = createAxis2MessageContext();
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);

        // If not super tenant, assign tenant configuration context
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            ConfigurationContext tenantConfigCtx =
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain, axis2MsgCtx.getConfigurationContext());
            axis2MsgCtx.setConfigurationContext(tenantConfigCtx);
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);
        }
        return MessageContextCreatorForAxis2.getSynapseMessageContext(axis2MsgCtx);
    }

    /**
     * Creates an Axis2 MessageContext instance
     * @return Axis2 Message Context
     */
    private static org.apache.axis2.context.MessageContext createAxis2MessageContext() {

        org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();
        axis2MsgCtx.setMessageID(UIDGenerator.generateURNString());

        axis2MsgCtx.setConfigurationContext(ServiceReferenceHolder.getInstance().getConfigurationContextService()
                                                                  .getServerConfigContext());
        // Axis2 spawns a new thread to send a message if this is TRUE
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING,
                                Boolean.FALSE);
        axis2MsgCtx.setServerSide(true);
        return axis2MsgCtx;
    }

    /**
     *
     * @param uri uri of the request
     * @return the tenant domain from the uri
     */
    private String getTenantDomain(String uri) {
        String tenant = MultitenantUtils.getTenantDomainFromUrl(uri);
        if (tenant.equals(uri)) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return tenant;
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