package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpInetConnection;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.nhttp.HttpCoreRequestResponseTransport;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.ProtocolState;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundHttpConstants;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class InboundHttpSourceRequestWorker implements Runnable {

    private static final Log log = LogFactory.getLog(InboundHttpSourceRequestWorker.class);

    /**
     * the http request
     */
    private InboundHttpSourceRequest request = null;
    /**
     * The configuration of the receiver
     */
    private InboundConfiguration sourceConfiguration = null;

    private static final String SOAP_ACTION_HEADER = "SOAPAction";

    private SynapseEnvironment synapseEnvironment;

    public InboundHttpSourceRequestWorker(InboundHttpSourceRequest inboundSourceRequest, InboundConfiguration inboundConfiguration, SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
        this.request = inboundSourceRequest;
        this.sourceConfiguration = inboundConfiguration;
    }


    public void run() {
        if (request != null) {
            org.apache.synapse.MessageContext msgCtx = createMessageContext(request);
            MessageContext messageContext = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
            messageContext.setProperty(
                    InboundHttpConstants.HTTP_INBOUND_SOURCE_REQUEST, request);
            messageContext.setProperty(
                    InboundHttpConstants.HTTP_INBOUND_SOURCE_CONFIGURATION, sourceConfiguration);
            messageContext.setProperty(InboundHttpConstants.HTTP_INBOUND_SOURCE_CONNECTION,
                    request.getConnection());

            if (request.isEntityEnclosing()) {
                processEntityEnclosingRequest(messageContext);
                msgCtx.setProperty(SynapseConstants.IS_INBOUND, "true");
                msgCtx.setProperty(SynapseConstants.OUT_SEQUENCE, request.getOutSeq());
                msgCtx.setWSAAction(request.getHeaders().get(InboundHttpConstants.SOAP_ACTION));
                SequenceMediator seq = (SequenceMediator) synapseEnvironment.getSynapseConfiguration().getSequence(request.getInjectSeq());
                seq.setErrorHandler(request.getFaultSeq());
                if (seq != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("injecting message to sequence : " + request.getInjectSeq());
                    }
                    synapseEnvironment.injectAsync(msgCtx, seq);
                } else {
                    log.error("Sequence: " + request.getInjectSeq() + " not found");
                }
            } else {
                log.error("cannot handle Non Entity Enclosing Request");
            }
        } else {
            log.error("InboundSourceRequest cannot be null");
        }
    }


    private org.apache.synapse.MessageContext createMessageContext(InboundHttpSourceRequest inboundSourceRequest) {
        org.apache.synapse.MessageContext msgCtx = synapseEnvironment.createMessageContext();
        MessageContext axis2MsgCtx = ((org.apache.synapse.core.axis2.Axis2MessageContext) msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());
        String oriUri = inboundSourceRequest.getUri();
        String restUrlPostfix = NhttpUtil.getRestUrlPostfix(oriUri, axis2MsgCtx.getConfigurationContext().getServicePath());
        String servicePrefix = oriUri.substring(0, oriUri.indexOf(restUrlPostfix));

        axis2MsgCtx.setTo(new EndpointReference(oriUri));
        axis2MsgCtx.setProperty(MessageContext.CLIENT_API_NON_BLOCKING, false);
        axis2MsgCtx.setProperty(PassThroughConstants.SERVICE_PREFIX, servicePrefix);

        // msgContext.setTo(new EndpointReference(restUrlPostfix));
        axis2MsgCtx.setProperty(PassThroughConstants.REST_URL_POSTFIX, restUrlPostfix);


        return msgCtx;
    }

    private void processEntityEnclosingRequest(MessageContext msgContext) {
        try {
            String contentTypeHeader = request.getHeaders().get(HTTP.CONTENT_TYPE);
            contentTypeHeader = contentTypeHeader != null ? contentTypeHeader : inferContentType();

            String charSetEncoding = null;
            String contentType = null;

            if (contentTypeHeader != null) {
                charSetEncoding = BuilderUtil.getCharSetEncoding(contentTypeHeader);
                contentType = TransportUtils.getContentType(contentTypeHeader, msgContext);
            }
            // get the contentType of char encoding
            if (charSetEncoding == null) {
                charSetEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            String method = request.getRequest() != null ? request.getRequest().getRequestLine().getMethod().toUpperCase() : "";
            msgContext.setTo(new EndpointReference(request.getUri()));
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);
            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEncoding);
            msgContext.setServerSide(true);

            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentTypeHeader);
            msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);


            String soapAction = request.getHeaders().get(SOAP_ACTION_HEADER);

            int soapVersion = HTTPTransportUtils.
                    initializeMessageContext(msgContext, soapAction,
                            request.getUri(), contentTypeHeader);
            SOAPEnvelope envelope;

            if (soapVersion == 1) {
                SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
                envelope = fac.getDefaultEnvelope();
            } else if (soapVersion == 2) {
                SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
                envelope = fac.getDefaultEnvelope();
            } else {
                SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
                envelope = fac.getDefaultEnvelope();
            }

            msgContext.setEnvelope(envelope);


            msgContext.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, request.getPipe());
            Map excessHeaders = request.getExcessHeaders();

            msgContext.setMessageID(UIDGenerator.generateURNString());

            // Axis2 spawns a new threads to send a message if this is TRUE - and it has to
            // be the other way
            msgContext.setProperty(MessageContext.CLIENT_API_NON_BLOCKING,
                    Boolean.FALSE);


            NHttpServerConnection conn = request.getConnection();


            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, this);
            msgContext.setServerSide(true);
            msgContext.setProperty(
                    Constants.Configuration.TRANSPORT_IN_URL, request.getUri());

            // http transport header names are case insensitive
            Map<String, String> headers = new TreeMap<String, String>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });

            Set<Map.Entry<String, String>> entries = request.getHeaders().entrySet();
            for (Map.Entry<String, String> entry : entries) {
                headers.put(entry.getKey(), entry.getValue());
            }
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headers);
            msgContext.setProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS, excessHeaders);

            // Following section is required for throttling to work

            if (conn instanceof HttpInetConnection) {
                HttpInetConnection netConn = (HttpInetConnection) conn;
                InetAddress remoteAddress = netConn.getRemoteAddress();
                if (remoteAddress != null) {
                    msgContext.setProperty(
                            MessageContext.REMOTE_ADDR, remoteAddress.getHostAddress());
                    msgContext.setProperty(
                            NhttpConstants.REMOTE_HOST, NhttpUtil.getHostName(remoteAddress));
                }
            }

            msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                    new HttpCoreRequestResponseTransport(msgContext));


        } catch (AxisFault axisFault) {
            log.error(axisFault.getMessage(), axisFault);
        }
    }

    private String inferContentType() {
        Map<String, String> headers = request.getHeaders();
        for (String header : headers.keySet()) {
            if (HTTP.CONTENT_TYPE.equalsIgnoreCase(header)) {
                return headers.get(header);
            }
        }
        Parameter param = sourceConfiguration.getConfigurationContext().getAxisConfiguration().
                getParameter(PassThroughConstants.REQUEST_CONTENT_TYPE);
        if (param != null) {
            return param.getValue().toString();
        }
        return null;
    }

    private void sendAck(MessageContext msgContext) {
        String respWritten = "";
        if (msgContext.getOperationContext() != null) {
            respWritten = (String) msgContext.getOperationContext().getProperty(
                    Constants.RESPONSE_WRITTEN);
        }

        if (msgContext.getProperty(PassThroughConstants.FORCE_SOAP_FAULT) != null) {
            respWritten = "SKIP";
        }

        boolean respWillFollow = !Constants.VALUE_TRUE.equals(respWritten)
                && !"SKIP".equals(respWritten);
        boolean ack = (((RequestResponseTransport) msgContext.getProperty(
                RequestResponseTransport.TRANSPORT_CONTROL)).getStatus()
                == RequestResponseTransport.RequestResponseTransportStatus.ACKED);
        boolean forced = msgContext.isPropertyTrue(NhttpConstants.FORCE_SC_ACCEPTED);
        boolean nioAck = msgContext.isPropertyTrue("NIO-ACK-Requested", false);
        if (respWillFollow || ack || forced || nioAck) {
            NHttpServerConnection conn = request.getConnection();
            //SourceResponse sourceResponse;
            if (!nioAck) {
                msgContext.removeProperty(MessageContext.TRANSPORT_HEADERS);
//                sourceResponse = SourceResponseFactory.create(msgContext,
//                        request, sourceConfiguration);
//                sourceResponse.setStatus(HttpStatus.SC_ACCEPTED);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Sending ACK response with status "
                            + msgContext.getProperty(NhttpConstants.HTTP_SC)
                            + ", for MessageID : " + msgContext.getMessageID());
                }
//                sourceResponse = SourceResponseFactory.create(msgContext,
//                        request, sourceConfiguration);
//                sourceResponse.setStatus(Integer.parseInt(
//                        msgContext.getProperty(NhttpConstants.HTTP_SC).toString()));
            }

            //    SourceContext.setResponse(conn, sourceResponse);
            ProtocolState state = InboundSourceContext.getState(conn);
            if (state != null && state.compareTo(ProtocolState.REQUEST_DONE) <= 0) {
                conn.requestOutput();
            } else {
                InboundSourceContext.updateState(conn, ProtocolState.CLOSED);
                sourceConfiguration.getSourceConnections().shutDownConnection(conn);
            }
        }
    }
}
