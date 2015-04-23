package org.wso2.carbon.inbound.endpoint.protocol.hl7;

/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.util.BufferFactory;

import java.nio.charset.CharsetDecoder;
import java.util.Map;

public class MLLPContext {
    private static final Log log = LogFactory.getLog(MLLPContext.class);

    private StringBuffer requestBuffer;
    private StringBuffer responseBuffer;
    private Message hl7Message;
    private final HL7Codec codec;
    private long requestTime;
    private int expiry;
    private boolean validateMessage;

    private volatile boolean autoAck = true;
    private volatile boolean ackReady = false;
    private volatile boolean preProcess = false;
    private volatile boolean nackMode = false;
    private volatile boolean transportError = false;

    private Map<String, Object> inboundParameters;
    private InboundProcessorParams inboundProcessorParams;

    public MLLPContext(Map<String, Object> inboundParameters) {
        this.inboundProcessorParams = (InboundProcessorParams) inboundParameters.get(MLLPConstants.INBOUND_PARAMS);
        this.codec = new HL7Codec((CharsetDecoder) inboundParameters.get(MLLPConstants.HL7_CHARSET_DECODER));
        this.validateMessage = Boolean.valueOf(inboundProcessorParams.getProperties().getProperty(MLLPConstants.PARAM_HL7_VALIDATE));
        this.requestBuffer = new StringBuffer();
        this.responseBuffer = new StringBuffer();
        this.expiry = MLLPConstants.DEFAULT_HL7_TIMEOUT;
        this.inboundParameters = inboundParameters;
    }

    public HL7Codec getCodec() {
        return codec;
    }

    public StringBuffer getRequestBuffer() {
        return this.requestBuffer;
    }

    public StringBuffer getResponseBuffer() {
        return responseBuffer;
    }

    public Message getHl7Message() {
        return this.hl7Message;
    }

    public void setHl7Message(Message hl7Message) {
        this.hl7Message = hl7Message;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    public boolean isAckReady() {
        return ackReady;
    }

    public void setAckReady(boolean ready) {
        this.ackReady = ready;
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
        if (System.currentTimeMillis() > requestTime + expiry) {
            return true;
        }

        return false;
    }

    public boolean isNackMode() {
        return nackMode;
    }

    public void setNackMode(boolean nackMode) {
        this.nackMode = nackMode;
    }

    public boolean isPreProcess() {
        return preProcess;
    }

    public void setPreProcess(boolean preProcess) {
        this.preProcess = preProcess;
    }

    public Parser getPreProcessParser() {
        return (Parser) this.inboundParameters.get(MLLPConstants.HL7_PRE_PROC_PARSER_CLASS);
    }

    public BufferFactory getBufferFactory() {
        return (BufferFactory) this.inboundParameters.get(MLLPConstants.INBOUND_HL7_BUFFER_FACTORY);
    }

    public boolean isValidateMessage() {
        return validateMessage;
    }

    public void setValidateMessage(boolean validateMessage) {
        this.validateMessage = validateMessage;
    }

    public boolean isTransportError() {
        return transportError;
    }

    public void setTransportError(boolean transportError) {
        this.transportError = transportError;
    }

    public void reset() {
        // Resets MLLP Context and HL7Codec to default states.
        this.responseBuffer.setLength(0);
        this.getCodec().setState(HL7Codec.READ_HEADER);
        this.setAckReady(false);
        this.setAutoAck(true);
        this.setPreProcess(false);
        this.setNackMode(false);
    }
}