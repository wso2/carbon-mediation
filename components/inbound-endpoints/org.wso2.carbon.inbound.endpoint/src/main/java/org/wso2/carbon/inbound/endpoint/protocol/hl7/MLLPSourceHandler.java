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
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.transport.passthru.util.BufferFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

public class MLLPSourceHandler implements IOEventDispatch {
    private static final Log log = LogFactory.getLog(MLLPSourceHandler.class);

    private Map<String, String> sessionIdToPort;
    private volatile HL7Processor hl7Processor;

    private final ByteBuffer hl7TrailerBuf = ByteBuffer.wrap(MLLPConstants.HL7_TRAILER);
    private BufferFactory bufferFactory;
    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;

    public MLLPSourceHandler() { /* default constructor */ }

    public MLLPSourceHandler(HL7Processor hl7Processor) {
        super();
        sessionIdToPort = new HashMap<String, String>();
        this.hl7Processor = hl7Processor;
        this.bufferFactory = (BufferFactory) hl7Processor.getInboundParameterMap().get(
                MLLPConstants.INBOUND_HL7_BUFFER_FACTORY);
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
                    MLLPContextFactory.createMLLPContext(session, hl7Processor));
        }

        inputBuffer = bufferFactory.getBuffer();
        outputBuffer = bufferFactory.getBuffer();
//        inputBuffer = ByteBuffer.allocate(8 * 1024);
//        outputBuffer = ByteBuffer.allocate(8 * 1024);
    }

    @Override
    public void inputReady(IOSession session) {
//        System.out.println("input ready " + getRemoteAddress(session.hashCode()));
//        System.out.println(session.getStatus());
        ReadableByteChannel ch = (ReadableByteChannel) session.channel();

        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);

        inputBuffer.clear();
        try {
            int read;
            while ((read = ch.read(inputBuffer)) > 0) {
                inputBuffer.flip();
                try {
                    mllpContext.getCodec().decode(inputBuffer, mllpContext);
                } catch (MLLProtocolException e) {
                    shutdownConnection(session, mllpContext, e);
                } catch (HL7Exception e) {
                    shutdownConnection(session, mllpContext, e);
                } catch (IOException e) {
                    shutdownConnection(session, mllpContext, e);
                }
            }

            if (mllpContext.getCodec().isReadComplete())  {
                mllpContext.setResponded(false);
                if (mllpContext.isAutoAck()) {
                    mllpContext.requestOutput();
                    bufferFactory.release(inputBuffer);
                }
                hl7Processor.processRequest(mllpContext);
            }

            if (read < 0) {
                bufferFactory.release(inputBuffer);
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

        writeOut(session, mllpContext);

//        if (hl7Processor.isAutoAck()) {
//            writeOut(session, mllpContext);
//            return;
//        } else {
//
//            if (mllpContext.isAckReady()) {
//                writeOut(session, mllpContext);
////                bufferFactory.release(inputBuffer);
//                return;
//            }
//
//            // if message timeout expired
//            if (mllpContext.isExpired()) {
//                log.warn("Timed out while waiting for HL7 Response to be generated. Enable inbound.hl7.AutoAck to auto generate ACK response.");
//                try {
//                    mllpContext.setHl7Message(HL7MessageUtils.createNack(mllpContext.getHl7Message(),
//                            "Timed out while waiting for HL7 Response to be generated."));
//                    writeOut(session, mllpContext);
//                } catch (HL7Exception e) {
//                    log.error("Exception while generating NACK response on timeout. ", e);
//                    session.clearEvent(EventMask.WRITE);
//                    session.setEvent(EventMask.READ);
//                    mllpContext.reset();
//                }
//
//            }
//        }
    }

    private void writeOut(IOSession session, MLLPContext mllpContext) {

        outputBuffer.clear();
        try {
            mllpContext.getCodec().encode(outputBuffer, mllpContext);
        } catch (HL7Exception e) {
            shutdownConnection(session, mllpContext, e);
        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

        if (outputBuffer == null) {
            handleException(session, mllpContext, new MLLProtocolException("HL7 Codec is in an inconsistent state: "
                    + mllpContext.getCodec().getState() + ". Shutting down connection."));
            return;
        }

        try {
            session.channel().write(outputBuffer);
            if (mllpContext.getCodec().isWriteTrailer()) {
                session.channel().write(hl7TrailerBuf);
                hl7TrailerBuf.flip();
                mllpContext.getCodec().setState(HL7Codec.WRITE_COMPLETE);
            }
            bufferFactory.release(outputBuffer);
        } catch (IOException e) {
            shutdownConnection(session, mllpContext, e);
        }

        if (mllpContext.getCodec().isWriteComplete()) {
            if (mllpContext.isMarkForClose()) {
                shutdownConnection(session, mllpContext, null);
            } else {
                bufferFactory.release(outputBuffer);
                mllpContext.setResponded(true);
                mllpContext.reset();
                mllpContext.requestInput();
            }
        }

    }

    @Override
    public void timeout(IOSession session) {
        System.out.println("timeout " + getRemoteAddress(session.hashCode()));
        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);
        shutdownConnection(session, mllpContext, null);
    }

    @Override
    public void disconnected(IOSession session) {
        System.out.println("disconnected " + getRemoteAddress(session.hashCode()));
        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);
        shutdownConnection(session, mllpContext, null);
    }

    private void shutdownConnection(IOSession session, MLLPContext mllpContext, Exception e) {
        if (e != null) {
            handleException(session, mllpContext, e);
        }

        bufferFactory.release(inputBuffer);
        bufferFactory.release(outputBuffer);
        session.close();
    }

    private void handleException(IOSession session, MLLPContext mllpContext, Exception e) {
        log.error("Exception caught while in IO handler. Cause: ", e);
    }
}
