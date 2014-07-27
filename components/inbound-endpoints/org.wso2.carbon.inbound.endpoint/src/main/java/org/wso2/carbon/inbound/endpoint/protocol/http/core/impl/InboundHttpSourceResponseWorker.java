package org.wso2.carbon.inbound.endpoint.protocol.http.core.impl;


import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.http.HttpStatus;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.inbound.InboundMessageContextQueue;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;

import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.apache.synapse.transport.passthru.ProtocolState;
import org.apache.synapse.transport.passthru.util.PassThroughTransportUtils;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundHttpConstants;

import java.io.IOException;
import java.io.OutputStream;

public class InboundHttpSourceResponseWorker implements Runnable {
    private Logger logger = Logger.getLogger(InboundHttpSourceResponseWorker.class);

    @Override
    public void run() {
        while (true) {
            try {
                MessageContext messageContext = InboundMessageContextQueue.getInstance().getMessageContextQueue().take();
                if (messageContext != null) {

                    org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

                    InboundConfiguration sourceConfiguration = (InboundConfiguration) msgContext.getProperty(
                            InboundHttpConstants.HTTP_INBOUND_SOURCE_CONFIGURATION);

                    NHttpServerConnection conn = (NHttpServerConnection) msgContext.getProperty(
                            InboundHttpConstants.HTTP_INBOUND_SOURCE_CONNECTION);
                    if (conn == null) {
                        logger.error("Unable to correlate the response to a request");
                        throw new IllegalStateException("Unable to correlate the response to a request");

                    }

                    InboundHttpSourceRequest sourceRequest = InboundSourceContext.getRequest(conn);


                    InboundHttpSourceResponse sourceResponse = InboundSourceResponseFactory.create(msgContext,
                            sourceRequest, sourceConfiguration);
                    //  sourceResponse.checkResponseChunkDisable(msgContext);

                    InboundSourceContext.setResponse(conn, sourceResponse);

                    Boolean noEntityBody = (Boolean) msgContext.getProperty(PassThroughConstants.NO_ENTITY_BODY);
                    Pipe pipe = (Pipe) msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
                    if ((noEntityBody == null || !noEntityBody) || pipe != null) {
                        if (pipe == null) {
                            pipe = new Pipe(sourceConfiguration.getBufferFactory().getBuffer(),
                                    "Test", sourceConfiguration);
                            msgContext.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, pipe);
                            msgContext.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
                        }

                        pipe.attachConsumer(conn);
                        sourceResponse.connect(pipe);
                    }

                    Integer errorCode = (Integer) msgContext.getProperty(PassThroughConstants.ERROR_CODE);
                    if (errorCode != null) {
                        sourceResponse.setStatus(HttpStatus.SC_BAD_GATEWAY);
                        InboundSourceContext.get(conn).setShutDown(true);
                    }

                    ProtocolState state = InboundSourceContext.getState(conn);
                    if (state != null && state.compareTo(ProtocolState.REQUEST_DONE) <= 0) {
                        // start sending the response if we

                        boolean noEntityBodyResponse = false;
                        if (noEntityBody != null && Boolean.TRUE == noEntityBody
                                && pipe != null) {
                            OutputStream out = pipe.getOutputStream();
                            out.write(new byte[0]);
                            pipe.setRawSerializationComplete(true);
                            out.close();
                            noEntityBodyResponse = true;
                        }

                        if (!noEntityBodyResponse && msgContext.isPropertyTrue(PassThroughConstants.MESSAGE_BUILDER_INVOKED) && pipe != null) {
                            OutputStream out = pipe.getOutputStream();
                /*if (msgContext.isPropertyTrue(NhttpConstants.SC_ACCEPTED)) {
                    out.write(new byte[0]);
                }else {*/

                            //This is to support MTOM in response path for requests sent without a SOAPAction. The reason is
                            //axis2 selects application/xml formatter as the formatter for formatting the ESB to client response
                            //when there is no SOAPAction.
                            if (Constants.VALUE_TRUE.equals(msgContext.getProperty(Constants.Configuration.ENABLE_MTOM))) {
                                msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED);
                                msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED);
                            }

                            MessageFormatter formatter = MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
                            OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgContext);
                            Object contentTypeInMsgCtx =
                                    msgContext.getProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE);
                            boolean isContentTypeSetFromMsgCtx = false;

                            // If ContentType header is set in the axis2 message context, use it.
                            if (contentTypeInMsgCtx != null) {
                                String contentTypeValueInMsgCtx = contentTypeInMsgCtx.toString();
                                // Skip multipart/related as it should be taken from formatter.
                                if (!contentTypeValueInMsgCtx.contains(
                                        PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED)) {

                                    if (format != null) {
                                        String encoding = format.getCharSetEncoding();
                                        if (encoding != null) {
                                            sourceResponse.removeHeader(HTTP.CONTENT_TYPE);
                                            contentTypeValueInMsgCtx += "; charset=" + encoding;
                                        }
                                    }
                                    sourceResponse.addHeader(HTTP.CONTENT_TYPE, contentTypeValueInMsgCtx);
                                    isContentTypeSetFromMsgCtx = true;
                                }
                            }
                            // If ContentType is not set from msg context, get the formatter ContentType
                            if (!isContentTypeSetFromMsgCtx) {
                                sourceResponse.removeHeader(HTTP.CONTENT_TYPE);
                                sourceResponse.addHeader(HTTP.CONTENT_TYPE,
                                        formatter.getContentType(
                                                msgContext, format, msgContext.getSoapAction())
                                );
                            }

                            formatter.writeTo(msgContext, format, out, false);
                /*}*/
                            pipe.setSerializationComplete(true);
                            out.close();
                        }
                        conn.requestOutput();
                    } else {
                        // nothing much to do as we have started the response already
                        if (errorCode != null) {
                            if (logger.isDebugEnabled()) {
                                logger.warn("A Source connection is closed because of an " +
                                        "error in target: " + conn);
                            }
                        } else {
                            logger.debug("A Source Connection is closed, because Inbound Http source handler " +
                                    "is already in the process of writing a response while " +
                                    "another response is submitted: " + conn);
                        }
                        InboundSourceContext.updateState(conn, ProtocolState.CLOSED);
                        sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
                    }
                }
            } catch (AxisFault axisFault) {
                logger.error(axisFault.getMessage(), axisFault);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
