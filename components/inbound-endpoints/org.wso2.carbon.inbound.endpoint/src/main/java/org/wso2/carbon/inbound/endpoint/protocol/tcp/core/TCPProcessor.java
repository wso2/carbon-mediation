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

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.context.TCPContext;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.util.Axis2TCPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.util.TCPExecutorServiceFactory;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.util.TCPMessageUtils;

import java.math.BigInteger;
import java.nio.charset.CharsetDecoder;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Decoded inbound TCP messages are handled here. messages will inject to the sequence,
 * send back message encoding is also done here.
 */

public class TCPProcessor implements InboundResponseSender {
    private static final Log log = LogFactory.getLog(TCPProcessor.class);

    private ScheduledExecutorService executorService ;

    private Map<String, Object> parameters;
    private InboundProcessorParams params;

    //decoding parameters
    private int decodeMode = InboundTCPConstants.NOT_DECIDED_YET;
    private byte[] header;
    private byte[] trailer;
    private String tag;
    private int msgLength;

    public int getDecodeMode() {
        return decodeMode;
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getTrailer() {
        return trailer;
    }

    public String getTag() {
        return tag;
    }

    public int getMsgLength() {
        return msgLength;
    }

    private String inSequence;
    private String onErrorSequence;


    private int timeOut;

    public TCPProcessor(Map<String, Object> parameters) {
        this.parameters = parameters;

        params = (InboundProcessorParams) parameters.get(InboundTCPConstants.INBOUND_PARAMS);
        decideDecodeMode(params);
        inSequence = params.getInjectingSeq();
        onErrorSequence = params.getOnErrorSeq();

        timeOut = TCPMessageUtils.getInt(InboundTCPConstants.PARAM_TCP_TIMEOUT, params);
    }

    private void decideDecodeMode(InboundProcessorParams params) {

        //log.info("Deciding Decoding mode...");
        //we have to decide the decode Mode which are (DECODE_BY_HEADER_TRAILER/DECODE_BY_TAG/DECODE_BY_LENGTH)
        if (decodeMode == InboundTCPConstants.NOT_DECIDED_YET) {
            //header trailer mode
            String h = params.getProperties().getProperty(InboundTCPConstants.TCP_MSG_HEADER);
            String t1 = params.getProperties().getProperty(InboundTCPConstants.TCP_MSG_TRAILER_BYTE1);
            String t2 = params.getProperties().getProperty(InboundTCPConstants.TCP_MSG_TRAILER_BYTE2);

            if(!h.isEmpty() && !t1.isEmpty() && !t2.isEmpty()) {
                header = new BigInteger(h, 16).toByteArray();
                byte[] trailer1 = new BigInteger(t1, 16).toByteArray();
                byte[] trailer2 = new BigInteger(t2, 16).toByteArray();
                trailer = new byte[2];
                trailer[0] = trailer1[0];
                trailer[1] = trailer2[0];

                if (header[0] > 0 && trailer1[0] > 0 && trailer2[0] > 0) {
                    decodeMode = InboundTCPConstants.DECODE_BY_HEADER_TRAILER;
                    log.info("decode by header & trailer");
                }
            }
            //tag mode
            tag = params.getProperties().getProperty(InboundTCPConstants.TCP_MSG_TAG);
            if(!tag.isEmpty() && decodeMode==InboundTCPConstants.NOT_DECIDED_YET){
                decodeMode = InboundTCPConstants.DECODE_BY_TAG;
                log.info("decode by enclosure tag : "+tag);
            }

            //length mode
            try {
                msgLength = Integer.parseInt(
                        params.getProperties().getProperty(InboundTCPConstants.TCP_MSG_LENGTH));
                if(msgLength>0 && decodeMode==InboundTCPConstants.NOT_DECIDED_YET){
                    decodeMode = InboundTCPConstants.DECODE_BY_LENGTH;
                    log.info("decode by message length");
                }
            } catch (NumberFormatException e) {
                msgLength = -1;
            }

            //decide the decoding mode from the config
            if (decodeMode==InboundTCPConstants.NOT_DECIDED_YET) {
                log.error("Message decode mode not specified property in the TCP inbound point config file");
            }
        }
    }

    public Map<String, Object> getInboundParameterMap() {
        return parameters;
    }

    // Prepare Synapse Context for message injection
    // in tcpContext we have the decoded message as string,character encoding,tcp Message Content type
    public void processRequest(TCPContext tcpContext) {
        tcpContext.setRequestTime(System.currentTimeMillis());

        MessageContext synCtx = null;

        try {
            synCtx = TCPMessageUtils.createSynapseMessageContext(tcpContext, params);
        } catch (AxisFault e) {
            log.error("Could not generate Synapse Message Context", e);
        }

        tcpContext.setMessageId(synCtx.getMessageID());
        synCtx.setProperty(InboundTCPConstants.TCP_INBOUND_MSG_ID, synCtx.getMessageID());
        log.info("TCP inbound message ID : "+synCtx.getProperty(InboundTCPConstants.TCP_INBOUND_MSG_ID));


        //We need response invocation through this processor. set tcpContext and inbound response worker
        synCtx.setProperty(SynapseConstants.IS_INBOUND, true);
        synCtx.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER, this);
        synCtx.setProperty(InboundTCPConstants.TCP_CONTEXT, tcpContext);

        addProperties(synCtx, tcpContext);

        SequenceMediator injectSeq =
                (SequenceMediator) synCtx.getEnvironment().getSynapseConfiguration().getSequence(inSequence);
        injectSeq.setErrorHandler(onErrorSequence);

        executorService = TCPExecutorServiceFactory.getExecutorService();

        if (timeOut > 0) {
            executorService
                    .schedule(new TimeoutHandler(tcpContext, synCtx.getMessageID()), timeOut, TimeUnit.MILLISECONDS);
        }

        CallableTaskTCP task = new CallableTaskTCP(synCtx, injectSeq);
        log.info("message submitted to the inject sequence");
        executorService.submit(task);

    }

    private void addProperties(MessageContext synCtx, TCPContext context) {

        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((org.apache.synapse.core.axis2.Axis2MessageContext) synCtx).getAxis2MessageContext();

        axis2MsgCtx.setProperty(Axis2TCPConstants.TCP_MESSAGE_OBJECT, context.getTCPMessage());

        if (parameters.get(InboundTCPConstants.TCP_CHARSET_DECODER) != null) {
            axis2MsgCtx.setProperty(Axis2TCPConstants.TCP_MESSAGE_CHARSET,
                                    ((CharsetDecoder) parameters.get(InboundTCPConstants.TCP_CHARSET_DECODER)).charset()
                                                                                                              .displayName());
        }
    }

    @Override public void sendBack(MessageContext messageContext) {
        TCPContext tcpContext = (TCPContext) messageContext.getProperty(InboundTCPConstants.TCP_CONTEXT);
        log.info("send Back method was called");

        if (messageContext.getProperty(InboundTCPConstants.TCP_INBOUND_MSG_ID) != null &&
            !tcpContext.getMessageId().equals(messageContext.getProperty(InboundTCPConstants.TCP_INBOUND_MSG_ID))) {
            log.warn("TCP Response ID does not match request ID. Response may have been received after timeout.");
            return;
        }

        tcpContext.setTCPMessage(TCPMessageUtils.payloadToTCPMessage(messageContext, params));

        //log.info(tcpContext.getTCPMessage());

        tcpContext.requestOutput();

    }

    //if we need to send a message about request timeout
    private class TimeoutHandler implements Runnable {
        private TCPContext context;
        private String messageId;

        public TimeoutHandler(TCPContext context, String messageId) {
            this.context = context;
            this.messageId = messageId;
        }

        public void run() {
            if (messageId.equals(context.getMessageId())) {

                log.warn("Timed out while waiting for TCP Response to be generated.");
/*
                context.setTCPMessage("");
                context.setMessageId("TIMEOUT");
                context.requestOutput();
*/
            }
        }
    }
}