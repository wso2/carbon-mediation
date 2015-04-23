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
import ca.uhn.hl7v2.HL7Exception;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.EventMask;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.transport.passthru.util.BufferFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

//TODO Abstract SourceHandler
public class HL7ServerIOEventDispatch implements IOEventDispatch {
    private static final Log log = LogFactory.getLog(HL7ServerIOEventDispatch.class);

    private Map<String, String> sessionIdToPort;
    private volatile HL7RequestProcessor hl7RequestProcessor;
    private BufferFactory bufferFactory;
    private ByteBuffer requestBuffer;
    private ByteBuffer responseBuffer;

    public HL7ServerIOEventDispatch() { /* default constructor */ }

    public HL7ServerIOEventDispatch(HL7RequestProcessor hl7RequestProcessor) {
        super();
        sessionIdToPort = new HashMap<String, String>();
        this.hl7RequestProcessor = hl7RequestProcessor;
        this.bufferFactory = (BufferFactory) hl7RequestProcessor.getInboundParameterMap().get(MLLPConstants.INBOUND_HL7_BUFFER_FACTORY);
    }

    private String getRemoteAddress(int hashCode) {
        return sessionIdToPort.get(String.valueOf(hashCode));
    }

    @Override
    public void connected(IOSession session) {
//        System.out.println("connected: " + session.getRemoteAddress() + " hashCode " + this.hashCode() + " session.hashCode() " + session.hashCode());
        sessionIdToPort.put(String.valueOf(session.hashCode()), String.valueOf(session.getRemoteAddress()));

        if (session.getAttribute(MLLPConstants.MLLP_CONTEXT) == null) {
            session.setAttribute(MLLPConstants.MLLP_CONTEXT,
                    new MLLPContext(hl7RequestProcessor.getInboundParameterMap()));
        }

        requestBuffer = bufferFactory.getBuffer();
        responseBuffer = bufferFactory.getBuffer();

    }

    // TODO: stop setting event mask here to give control to outputReady do that only when necessary
    @Override
    public void inputReady(IOSession session) {
//        System.out.println("input ready " + getRemoteAddress(session.hashCode()));
//        System.out.println(session.getStatus());
        ReadableByteChannel ch = (ReadableByteChannel) session.channel();

        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);

        try {
            int read;
            while ((read = ch.read(requestBuffer)) > 0) {
                requestBuffer.flip();
                try {
                    mllpContext.getCodec().decode(requestBuffer, mllpContext);
                } catch (MLLProtocolException e) {
                    shutdownConnection(session, mllpContext, e);
                } catch (HL7Exception e) {
                    shutdownConnection(session, mllpContext, e);
                } catch (IOException e) {
                    shutdownConnection(session, mllpContext, e);
                }
            }

            bufferFactory.release(requestBuffer);

            if (mllpContext.getCodec().isReadComplete())  {
                hl7RequestProcessor.processRequest(mllpContext);
                session.clearEvent(EventMask.READ);
                session.setEvent(EventMask.WRITE);
            }

            if (read < 0) {
                session.close();
            }

        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

    }

    @Override
    public void outputReady(IOSession session) {
//        System.out.println("output ready " + getRemoteAddress(session.hashCode()));

        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);

        if (mllpContext.isAutoAck()) {
            writeOut(session, mllpContext);
            return;
        } else {

            if (mllpContext.isAckReady()) {
                writeOut(session, mllpContext);
//                bufferFactory.release(requestBuffer);
                return;
            }

            // if message timeout expired
            if (mllpContext.isExpired()) {
                log.warn("Timed out while waiting for HL7 Response to be generated. Enable inbound.hl7.AutoAck to auto generate ACK response.");
                try {
                    mllpContext.setHl7Message(HL7MessageUtils.createNack(mllpContext.getHl7Message(),
                            "Timed out while waiting for HL7 Response to be generated."));
                    writeOut(session, mllpContext);
                } catch (HL7Exception e) {
                    log.error("Exception while generating NACK response on timeout. ", e);
                    session.clearEvent(EventMask.WRITE);
                    session.setEvent(EventMask.READ);
                    mllpContext.reset();
                }

            }
        }
    }

    @Override
    public void timeout(IOSession session) {
        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);
        shutdownConnection(session, mllpContext, null);
    }

    @Override
    public void disconnected(IOSession session) {
//        System.out.println("disconnected " + getRemoteAddress(session.hashCode()));
        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);
        shutdownConnection(session, mllpContext, null);
    }

    private void writeOut(IOSession session, MLLPContext mllpContext) {

        try {
            mllpContext.getCodec().encode(responseBuffer, mllpContext);
        } catch (HL7Exception e) {
            shutdownConnection(session, mllpContext, e);
        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

        if (responseBuffer == null) {
            handleException(session, mllpContext, new MLLProtocolException("HL7 Codec is in an inconsistent state: "
                    + mllpContext.getCodec().getState() + ". Shutting down connection."));
            session.close();
            return;
        }

        try {
            session.channel().write(responseBuffer);
            if (mllpContext.getCodec().isWriteTrailer()) {
                session.channel().write(MLLPConstants.HL7_TRAILER_BBUF);
                MLLPConstants.HL7_TRAILER_BBUF.flip();
                mllpContext.getCodec().setState(HL7Codec.WRITE_COMPLETE);
            }
            bufferFactory.release(responseBuffer);
        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

        if (mllpContext.getCodec().isWriteComplete()) {
            mllpContext.getBufferFactory().release(responseBuffer);
            session.clearEvent(EventMask.WRITE);
            session.setEvent(EventMask.READ);
            mllpContext.reset();
        }

    }

    private void shutdownConnection(IOSession session, MLLPContext mllpContext, Exception e) {
        if (session.isClosed()) {
            return;
        }

        if (e != null) {
            handleException(session, mllpContext, e);
        }

        bufferFactory.release(requestBuffer);
        bufferFactory.release(responseBuffer);
        session.close();
    }

    private void handleException(IOSession session, MLLPContext mllpContext, Exception e) {
        log.error("Exception caught while in IO handler. Cause: ", e);
        try {
            mllpContext.setTransportError(true);
            session.clearEvent(EventMask.READ);
            session.setEvent(EventMask.WRITE);
            mllpContext.getCodec().setState(HL7Codec.READ_COMPLETE);
            mllpContext.setHl7Message(HL7MessageUtils.createNack(null, "Transport error: " + e));
        } catch (HL7Exception e2) {
            log.error("Exception while generating NACK on error. Connection will be shutdown.", e);
            session.clearEvent(EventMask.WRITE);
            session.setEvent(EventMask.READ);
            mllpContext.reset();
        }
    }
}
