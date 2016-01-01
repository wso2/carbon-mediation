/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.context;

import org.apache.http.nio.reactor.EventMask;
import org.apache.http.nio.reactor.IOSession;
import org.apache.log4j.Logger;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.codec.TCPCodec;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPConstants;

import java.io.ByteArrayOutputStream;
import java.nio.charset.CharsetDecoder;

/**
 * TCP message related parameters are stored here.
 */
public class TCPContext {

    private static final Logger log = Logger.getLogger(TCPContext.class);

    private InboundProcessorParams params;

    private IOSession session;
    private StringBuffer requestBuffer;
    private StringBuffer responseBuffer;

    //stores the tcp payload
    private ByteArrayOutputStream baos;

    private String tcpMessage;

    private byte[] tcpResponseMsg;

    private volatile TCPCodec codec;

    private long requestTime;
    private int expiry;

    private volatile boolean markForClose = false;

    private volatile String messageId;

    private BufferFactory bufferFactory;

    public TCPContext(IOSession session, CharsetDecoder decoder, BufferFactory bufferFactory,
                      InboundProcessorParams params) {
        this.session = session;
        this.codec = new TCPCodec(decoder);
        this.bufferFactory = bufferFactory;
        this.expiry = InboundTCPConstants.DEFAULT_TCP_TIMEOUT;
        this.requestBuffer = new StringBuffer();
        this.responseBuffer = new StringBuffer();
        this.baos = new ByteArrayOutputStream();
        this.params = params;
    }

    public byte[] getTcpResponseMsg() {
        return tcpResponseMsg;
    }

    public void setTcpResponseMsg(byte[] tcpResponseMsg) {
        this.tcpResponseMsg = tcpResponseMsg;
    }

    public ByteArrayOutputStream getBaos() {
        return baos;
    }

    public InboundProcessorParams getParams() {
        return params;
    }

    public void setParams(InboundProcessorParams params) {
        this.params = params;
    }

    public TCPCodec getCodec() {
        return codec;
    }

    public StringBuffer getRequestBuffer() {
        return this.requestBuffer;
    }

    public StringBuffer getResponseBuffer() {
        return responseBuffer;
    }

    public String getTCPMessage() {
        return this.tcpMessage;
    }

    public void setTCPMessage(String tcpMessage) {
        this.tcpMessage = tcpMessage;
    }

    /**
     * Activate writing mode
     */
    public void requestOutput() {
        session.clearEvent(EventMask.READ);
        session.setEvent(EventMask.WRITE);
    }

    /**
     * Activate reading mode
     */
    public void requestInput() {
        session.clearEvent(EventMask.WRITE);
        session.setEvent(EventMask.READ);
    }

    public void clearEventMaskRead() {
        session.clearEvent(EventMask.READ);
    }

    public void setRequestTime(long timeStamp) {
        this.requestTime = timeStamp;
    }

    public long getRequestTime() {
        return this.requestTime;
    }

    public void setExpiry(int milliseconds) {
        if (milliseconds < 1000) {
            milliseconds = 1000;
        }
        this.expiry = milliseconds;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > requestTime + expiry;
    }

    public BufferFactory getBufferFactory() {
        return bufferFactory;
    }

    public boolean isMarkForClose() {
        return markForClose;
    }

    public void setMarkForClose(boolean markForClose) {
        this.markForClose = markForClose;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * reset the tcp context. ready to read new message
     */
    public void reset() {
        //Resets TCP Context and TCPCodec to default states.
        this.getBaos().reset();
        this.responseBuffer.setLength(0);
        this.getCodec().setState(TCPCodec.READ_HEADER);
    }
}
