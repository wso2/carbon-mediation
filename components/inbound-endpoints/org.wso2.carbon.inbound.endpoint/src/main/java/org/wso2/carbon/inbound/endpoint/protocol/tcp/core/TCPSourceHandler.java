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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.context.TCPContext;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.context.TCPContextFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * IO event handler
 */

public class TCPSourceHandler implements IOEventDispatch {
    private static final Log log = LogFactory.getLog(TCPSourceHandler.class);

    private Map<String, String> sessionIdToPort;
    private volatile TCPProcessor tcpProcessor;

    private final ByteBuffer tcpTrailerBuf = ByteBuffer.wrap(InboundTCPConstants.TCP_TRAILER);
    private BufferFactory bufferFactory;
    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;

    public TCPSourceHandler() {
    }

    public TCPSourceHandler(TCPProcessor tcpProcessor) {
        super();
        sessionIdToPort = new HashMap<String, String>();
        this.tcpProcessor = tcpProcessor;
        this.bufferFactory = (BufferFactory) tcpProcessor.getInboundParameterMap()
                                                         .get(InboundTCPConstants.INBOUND_TCP_BUFFER_FACTORY);
    }

    private String getRemoteAddress(int hashCode) {
        return sessionIdToPort.get(String.valueOf(hashCode));
    }

    //we bind a TCP Context to session so that we can encode decode messages using TCP Context parameters
    @Override public void connected(IOSession session) {

        sessionIdToPort.put(String.valueOf(session.hashCode()), String.valueOf(session.getRemoteAddress()));

        if (session.getAttribute(InboundTCPConstants.TCP_CONTEXT) == null) {
            session.setAttribute(InboundTCPConstants.TCP_CONTEXT,
                                 TCPContextFactory.createTCPContext(session, tcpProcessor));
        }

        inputBuffer = bufferFactory.getBuffer();
        outputBuffer = bufferFactory.getBuffer();
    }

    @Override public void inputReady(IOSession session) {

        ReadableByteChannel ch = (ReadableByteChannel) session.channel();

        TCPContext tcpContext = (TCPContext) session.getAttribute(InboundTCPConstants.TCP_CONTEXT);
        log.info("input ready. position : " + inputBuffer.position() + " limit : " + inputBuffer.limit());
/*
        if(inputBuffer.position()==0){
            inputBuffer.clear();
        }else{
            inputBuffer.compact();
        }
*/
        try {
            // how many number of Bytes reads from the channel
            int read;

            //buffer size = 8192
            //here we read data from the channel and write in to the inputBuffer+there can be existing data in buffer
            while ((read = ch.read(inputBuffer)) > 0) {
                log.info("position : " + inputBuffer.position() + " limit : " + inputBuffer.limit());
                inputBuffer.flip();
                log.info("position : " + inputBuffer.position() + " limit : " + inputBuffer.limit());
                try {
                    //here we send the input byte buffer, tcpContext, inboundPoint parameters to decode the message
                    int status = tcpContext.getCodec().decode(inputBuffer, tcpContext);

                    if (status == InboundTCPConstants.ONE_TCP_MESSAGE_IS_DECODED) {
                        log.info("one message is decoded : " + tcpContext.getMessageId() + " : " +
                                 tcpContext.getTCPMessage());
                        break;
                    }
                    //log.info("Received TCP message : "+tcpContext.getTCPMessage());
                } catch (TCPContextException e) {
                    shutdownConnection(session, tcpContext, e);
                } catch (IOException e) {
                    shutdownConnection(session, tcpContext, e);
                }
            }

            //now we have the decoded TCP message as a String and the TCPContext
            if (tcpContext.getCodec().isReadComplete()) {
                //bufferFactory.release(inputBuffer);
                //inputBuffer = bufferFactory.getBuffer();

                //log.info("Clear Event Mask READ : now there is no event mask");
                //tcpContext.clearEventMask();
                tcpProcessor.processRequest(tcpContext);
            }

            log.info("read : " + read);
            if (read <= 0) {

                bufferFactory.release(inputBuffer);
                inputBuffer = bufferFactory.getBuffer();
                session.close();
                log.info("session closed...");
            }

        } catch (IOException e) {
            shutdownConnection(session, tcpContext, e);
        }
    }

    @Override public void outputReady(IOSession session) {
        log.info("output ready called");
        TCPContext tcpContext = (TCPContext) session.getAttribute(InboundTCPConstants.TCP_CONTEXT);
        writeOut(session, tcpContext);
    }

    //write the response from tcp endpoint to client
    private void writeOut(IOSession session, TCPContext tcpContext) {

        outputBuffer.clear();

        tcpContext.getCodec().encode(outputBuffer, tcpContext);

        if (outputBuffer == null) {
            handleException(session, tcpContext, new TCPContextException(
                    "Error in tcp outbound message. Could not get the byte buffer from tcp context: " +
                    tcpContext.getCodec().getState() + "." +
                    " " +
                    "Shutting down connection."));
            return;
        }

        try {
            session.channel().write(outputBuffer);

        } catch (IOException e) {
            shutdownConnection(session, tcpContext, e);
        }

        if (tcpContext.getCodec().isWriteComplete()) {
            log.info("input buffer position : " + inputBuffer.position());
            if (tcpContext.isMarkForClose()) {
                log.info("TCP Connection shutdown...");
                shutdownConnection(session, tcpContext, null);
            } else {
                bufferFactory.release(outputBuffer);
                outputBuffer = bufferFactory.getBuffer();
                tcpContext.setMessageId("RESPONDED");
                tcpContext.reset();
                tcpContext.requestInput();
                log.info("request input set");
            }
        }
    }

    @Override public void timeout(IOSession session) {
        TCPContext tcpContext = (TCPContext) session.getAttribute(InboundTCPConstants.TCP_CONTEXT);
        shutdownConnection(session, tcpContext, null);
    }

    @Override public void disconnected(IOSession session) {
        TCPContext tcpContext = (TCPContext) session.getAttribute(InboundTCPConstants.TCP_CONTEXT);
        shutdownConnection(session, tcpContext, null);
    }

    private void shutdownConnection(IOSession session, TCPContext tcpContext, Exception e) {
        if (e != null) {
            handleException(session, tcpContext, e);
        }
        bufferFactory.release(inputBuffer);
        bufferFactory.release(outputBuffer);
        session.close();
    }

    private void handleException(IOSession session, TCPContext tcpContext, Exception e) {
        log.error("Exception caught while in Inbound TCP IO handler. Cause: ", e);
    }
}