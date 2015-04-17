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
import ca.uhn.hl7v2.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class HL7Codec {
    private static final Log log = LogFactory.getLog(HL7Codec.class);


    public static final int READ_HEADER    = 0;
    public static final int READ_CONTENT   = 1;
    public static final int READ_TRAILER   = 2;
    public static final int READ_COMPLETE  = 3;

    public static final int WRITE_HEADER   = 4;
    public static final int WRITE_CONTENT  = 5;
    public static final int WRITE_TRAILER  = 6;
    public static final int WRITE_COMPLETE = 7;

    private final CharsetDecoder charsetDecoder;
    private final CharsetEncoder charsetEncoder;

    private int state;

    public HL7Codec() {
        this.state = READ_HEADER;
        this.charsetEncoder = MLLPConstants.UTF8_CHARSET.newEncoder();
        this.charsetDecoder = MLLPConstants.UTF8_CHARSET.newDecoder();
    }

    public HL7Codec(CharsetEncoder encoder, CharsetDecoder decoder) {
        this.state = READ_HEADER;
        this.charsetEncoder = encoder;
        this.charsetDecoder = decoder;
    }

    public int decode(ByteBuffer dst, MLLPContext context) throws IOException, MLLProtocolException, HL7Exception {

        if (this.state >= READ_COMPLETE) {
            return -1;
        }

        if (this.state == READ_HEADER) {
            if(dst.get(0) == MLLPConstants.HL7_HEADER[0]) {
                dst.position(1);
                this.state = READ_CONTENT;
            } else {
                throw new MLLProtocolException("Could not find header in incoming message.");
            }
        }

        if (this.state == READ_CONTENT) {

            int trailerIndex = findTrailer(dst);

            if(trailerIndex > -1) {
                dst.limit(trailerIndex);
                this.state = READ_TRAILER;
            }

            context.getRequestBuffer().append(charsetDecoder.decode(dst).toString());
        }

        if (this.state == READ_TRAILER) {
            this.state = READ_COMPLETE;
            try {
                if (context.isPreProcess()) {
                    context.setHl7Message(HL7MessageUtils.parse(context.getRequestBuffer().toString(),
                            context.getPreProcessParser()));
                } else {
                    context.setHl7Message(HL7MessageUtils.parse(context.getRequestBuffer().toString()));
                }
//                  System.out.println(context.getRequestBuffer().toString());
                context.setRequestBuffer(new StringBuffer());
            } catch (HL7Exception e) {
                throw e;
            }
        }

        return 0;

    }

    private int findTrailer(ByteBuffer dst) {
        for(int i=0; i<dst.limit(); i++) {
            if(dst.get(i) == MLLPConstants.HL7_TRAILER[0]) {
                if(dst.get(i+1) == MLLPConstants.HL7_TRAILER[1]) {
                    return i-1;
                }
            }
        }

        return -1;
    }

    public ByteBuffer encode(Message hl7Message, MLLPContext context) throws UnsupportedEncodingException,
            HL7Exception, IOException {

        if (this.state < READ_COMPLETE) {
            return null;
        }

        if (this.state == READ_COMPLETE) {

            StringBuffer response = new StringBuffer();
            response.append(new String(MLLPConstants.HL7_HEADER, "UTF-8"));
            if (context.isAutoAck()) {
                response.append(hl7Message.generateACK());
            } else {
                response.append(hl7Message.encode());
            }
            response.append(new String(MLLPConstants.HL7_TRAILER, "UTF-8"));
            context.setResponseBuffer(response);

            CharBuffer charBuffer = CharBuffer.allocate(context.getResponseBuffer().length());

            for (int i=0; i<context.getResponseBuffer().length(); i++) {
                charBuffer.put(context.getResponseBuffer().charAt(i));
            }

            charBuffer.flip();

            ByteBuffer buffer = ByteBuffer.allocate(charBuffer.length());

            charsetEncoder.encode(charBuffer, buffer, true);
            buffer.flip();

            return buffer;
        }

        return null;
    }

    public boolean isReadComplete() {
        if (this.state >= READ_COMPLETE) {
            return true;
        }

        return false;
    }

    public boolean isWriteComplete() {
        if (this.state == WRITE_COMPLETE) {
            return true;
        }

        return false;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public CharsetDecoder getCharsetDecoder() {
        return charsetDecoder;
    }

    public CharsetEncoder getCharsetEncoder() {
        return charsetEncoder;
    }
}
