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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

public class HL7ServerIOEventDispatch implements IOEventDispatch {
    private static final Log log = LogFactory.getLog(HL7ServerIOEventDispatch.class);

    private Map<String, String> sessionIdToPort;
    private volatile HL7RequestProcessor HL7RequestProcessor;

    public HL7ServerIOEventDispatch() { /* default constructor */ }

    public HL7ServerIOEventDispatch(HL7RequestProcessor HL7RequestProcessor) {
        super();
        sessionIdToPort = new HashMap<String, String>();
        this.HL7RequestProcessor = HL7RequestProcessor;
    }

    private String getRemoteAddress(int hashCode) {
        return sessionIdToPort.get(String.valueOf(hashCode));
    }

    @Override
    public void connected(IOSession session) {
//        System.out.println("connected: " + session.getRemoteAddress() + " hashCode " + this.hashCode() + " session.hashCode() " + session.hashCode());
        sessionIdToPort.put(String.valueOf(session.hashCode()), String.valueOf(session.getRemoteAddress()));

        if (session.getAttribute(MLLPConstants.MLLP_CONTEXT) == null) {
            session.setAttribute(MLLPConstants.MLLP_CONTEXT, new MLLPContext());
        }
    }

    @Override
    public void inputReady(IOSession session) {
//        System.out.println("input ready " + getRemoteAddress(session.hashCode()));
//        System.out.println(session.getStatus());
        ReadableByteChannel ch = (ReadableByteChannel) session.channel();
        ByteBuffer dst = ByteBuffer.allocate(4096);

        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);

        try {
            int read;
            while ((read = ch.read(dst)) > 0) {
                dst.flip();
                try {
                    mllpContext.getCodec().decode(dst, mllpContext);
                } catch (MLLProtocolException e) {
                    shutdownConnection(session, e);
                } catch (HL7Exception e) {
                    shutdownConnection(session, e);
                } catch (IOException e) {
                    shutdownConnection(session, e);
                }
            }

            if (mllpContext.getCodec().isReadComplete())  {
                HL7RequestProcessor.processRequest(mllpContext);
                session.clearEvent(EventMask.READ);
                session.setEvent(EventMask.WRITE);
            }

            if (read < 0) {
                session.close();
            }

        } catch (IOException e) {
            shutdownConnection(session, e);
        }

    }

    @Override
    public void outputReady(IOSession session) {
//        System.out.println("output ready " + getRemoteAddress(session.hashCode()));

        MLLPContext mllpContext = (MLLPContext) session.getAttribute(MLLPConstants.MLLP_CONTEXT);

        if (mllpContext.isAutoAck()) {
            writeOut(session, mllpContext);
        } else {

            if (mllpContext.isAckReady()) {
                writeOut(session, mllpContext);
            }
            // if message timeout expired
//            if (mllpContext.isExpired()) {
//                session.clearEvent(EventMask.WRITE);
//                session.setEvent(EventMask.READ);
//                mllpContext.getCodec().setState(HL7Codec.READ_HEADER);
//            }
        }
    }

    @Override
    public void timeout(IOSession session) {
//        System.out.println("timeout " + getRemoteAddress(session.hashCode()));
        session.close();
    }

    @Override
    public void disconnected(IOSession session) {
//        System.out.println("disconnected " + getRemoteAddress(session.hashCode()));

        session.close();
    }

    private void writeOut(IOSession session, MLLPContext mllpContext) {
        ByteBuffer outBuf = null;
        try {
            outBuf = mllpContext.getCodec().encode(mllpContext.getHl7Message(), mllpContext);
        } catch (HL7Exception e) {
            shutdownConnection(session, e);
        } catch (IOException e) {
            shutdownConnection(session, e);
        }

        if (outBuf == null) {
            handleException(new MLLProtocolException("HL7 Codec is in an inconsistent state: "
                    + mllpContext.getCodec().getState() + ". Shutting down connection."));
            session.close();
            return;
        }

        try {
            session.channel().write(outBuf);
            mllpContext.getCodec().setState(HL7Codec.WRITE_COMPLETE);
        } catch (IOException e) {
            shutdownConnection(session, e);
        }

        if (mllpContext.getCodec().isWriteComplete()) {
            session.clearEvent(EventMask.WRITE);
            session.setEvent(EventMask.READ);
            mllpContext.reset();
        }

    }

    private void shutdownConnection(IOSession session, Exception e) {
        handleException(e);
        session.close();
    }

    private void handleException(Exception e) {
        System.out.println(e.getMessage());
    }
}
