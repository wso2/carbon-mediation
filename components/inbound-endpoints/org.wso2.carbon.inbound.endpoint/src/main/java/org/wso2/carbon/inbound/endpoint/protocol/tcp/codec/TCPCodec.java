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

package org.wso2.carbon.inbound.endpoint.protocol.tcp.codec;

import org.apache.log4j.Logger;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.context.TCPContext;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.TCPContextException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;

/**
 * Class responsible for tcp message decoding.
 */
public class TCPCodec {

    private static final Logger log = Logger.getLogger(TCPCodec.class);

    public static final int READ_HEADER = 0;
    public static final int READ_CONTENT = 1;
    public static final int READ_TRAILER = 2;
    public static final int READ_COMPLETE = 3;

    public static final int WRITE_HEADER = 4;
    public static final int WRITE_CONTENT = 5;
    public static final int WRITE_TRAILER = 6;
    public static final int WRITE_COMPLETE = 7;

    private CharsetDecoder charsetDecoder;

    private volatile int state;

    private int responseReadPosition = 0;
    private byte[] responseBytes = null;

    //decoding parameters
    private int decodeMode;
    private byte[] header;
    private byte[] trailer;
    private String tag;
    private byte[] delimiterTag;
    private int msgLength;

    public TCPCodec(CharsetDecoder charsetDecoder) {
        state = READ_HEADER;
        setCharsetDecoder(charsetDecoder);
    }

    /**
     * decoding messasges from byte stream
     *
     * @param byteBuffer received byte buffer from
     * @param context    tcp message context
     * @return if one tcp message is decoded return 1
     * @throws IOException
     * @throws TCPContextException
     */
    public int decode(ByteBuffer byteBuffer, TCPContext context) throws IOException, TCPContextException {
        int initialBufferLimit = byteBuffer.limit();

        switch (decodeMode) {
            case InboundTCPConstants.DECODE_BY_HEADER_TRAILER: {
                //decode the first message of the buffer
                if (this.state >= READ_COMPLETE || byteBuffer.position() < 0) {
                    return InboundTCPConstants.NOTHING_TO_DECODE;
                }
                if (this.state == READ_HEADER) {
                    if (byteBuffer.get(0) == header[0]) {
                        byteBuffer.position(1);
                        this.state = READ_CONTENT;
                    } else {
                        throw new TCPContextException("Could not find header in incoming message.");
                    }
                }
                if (this.state == READ_CONTENT) {
                    int trailerIndex = findTrailer(byteBuffer);
                    if (trailerIndex > -1) {
                        byteBuffer.limit(trailerIndex);
                        this.state = READ_TRAILER;
                    }
                    if (byteBuffer.hasArray()) {
                        byte[] ar = new byte[byteBuffer.remaining()];
                        byteBuffer.get(ar, 0, ar.length);
                        context.getBaos().write(ar);
                    }
                    //set the buffer position and limit for the rest of the buffer
                    byteBuffer.limit(initialBufferLimit);
                    byteBuffer.position(trailerIndex + 2);
                    byteBuffer.compact();
                }
                if (this.state == READ_TRAILER) {
                    this.state = READ_COMPLETE;
                    return InboundTCPConstants.ONE_TCP_MESSAGE_IS_DECODED;
                }
                break;
            }
            case InboundTCPConstants.DECODE_BY_TAG: {
                byte[] input = new byte[byteBuffer.remaining()];
                byteBuffer.get(input, 0, input.length);
                //decode the first message of the buffer
                if (this.state >= READ_COMPLETE || byteBuffer.position() < 0) {
                    return InboundTCPConstants.NOTHING_TO_DECODE;
                }
                if (this.state == READ_HEADER) {
                    int headerIndex = findTagIndex(input, delimiterTag, 0);
                    if (headerIndex > 0) {
                        byteBuffer.position(headerIndex - 1);
                        this.state = READ_CONTENT;
                    } else {
                        throw new TCPContextException("Could not find header tag in incoming message.");
                    }
                }
                if (this.state == READ_CONTENT) {
                    int trailerIndex = findTagIndex(input, delimiterTag, byteBuffer.position() + delimiterTag.length);
                    if (trailerIndex > -1) {
                        byteBuffer.limit(trailerIndex + delimiterTag.length + 1);
                        this.state = READ_TRAILER;
                    }
                    if (byteBuffer.hasArray()) {
                        byte[] ar = new byte[byteBuffer.remaining()];
                        byteBuffer.get(ar, 0, ar.length);
                        context.getBaos().write(ar);
                    }
                    //set the buffer position and limit for the rest of the buffer
                    byteBuffer.limit(initialBufferLimit);
                    byteBuffer.position(trailerIndex + tag.length() + 1);
                    byteBuffer.compact();
                }
                if (this.state == READ_TRAILER) {
                    this.state = READ_COMPLETE;
                    return InboundTCPConstants.ONE_TCP_MESSAGE_IS_DECODED;
                }
                break;
            }
            default: {
                log.warn("Decoding method not specified");
                break;
            }
        }
        return InboundTCPConstants.STILL_WORKING_ON_MESSAGE;
    }

    private int findTrailer(ByteBuffer dst) {
        for (int i = 0; i < dst.limit(); i++) {
            if (dst.get(i) == trailer[0]) {
                if (dst.get(i + 1) == trailer[1]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findTagIndex(byte[] largeBuffer, byte[] searchTag, int offset) {
        for (int i = offset; i < largeBuffer.length - searchTag.length + 1; i++) {
            boolean found = true;
            for (int j = 0; j < searchTag.length; j++) {
                if (largeBuffer[i + j] != searchTag[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the message from TCP context and convert it byte stream to send
     *
     * @param outBuf  output buffer
     * @param context tcpMessage context
     * @return
     */
    public int encode(ByteBuffer outBuf, TCPContext context) {
        if (this.state < READ_COMPLETE) {
            return 0;
        } else if (this.state == READ_COMPLETE) {
            responseBytes = context.getTcpResponseMsg();
            this.state = WRITE_HEADER;
        } else if (this.state >= WRITE_HEADER) {
            return fillBuffer(outBuf, responseBytes);
        }
        return InboundTCPConstants.STILL_WORKING_ON_MESSAGE;
    }

    //here we fill the byteBuffer with the response
    private int fillBuffer(ByteBuffer byteBuffer, byte[] responseBytes) {
        if (responseBytes == null) {
            return 0;
        }
        int count = 0;
        switch (decodeMode) {
            case InboundTCPConstants.DECODE_BY_HEADER_TRAILER: {
                byte b;
                int headerPosition = 0;
                if (this.state == WRITE_HEADER) {
                    byteBuffer.put(header[0]);
                    headerPosition = 1;
                    this.state = WRITE_CONTENT;
                }
                int MAX = byteBuffer.capacity();
                if (byteBuffer.capacity() - (responseBytes.length - responseReadPosition + headerPosition) > 0) {
                    MAX = responseBytes.length - responseReadPosition + headerPosition;
                }
                for (int i = responseReadPosition; i < MAX + responseReadPosition - headerPosition; i++) {
                    count++;
                    b = responseBytes[i];
                    byteBuffer.put(b);
                }
                responseReadPosition += count;
                if (responseReadPosition == responseBytes.length) {
                    this.state = WRITE_TRAILER;
                    responseReadPosition = 0;
                }
                if (this.state == WRITE_TRAILER) {
                    byteBuffer.put(trailer);
                    this.state = WRITE_COMPLETE;
                }
                break;
            }
            case InboundTCPConstants.DECODE_BY_TAG: {
                if (this.state == WRITE_HEADER) {
                    byteBuffer.put(responseBytes);
                    this.state = WRITE_COMPLETE;
                }
                break;
            }
            default: {
                log.warn("Decoding method not specified");
                break;
            }
        }
        byteBuffer.flip();
        return count;
    }

    public void resetState() {
        this.state = READ_HEADER;
    }

    public boolean isReadComplete() {
        return this.state >= READ_COMPLETE;
    }

    public boolean isWriteTrailer() {
        return this.state == WRITE_TRAILER;
    }

    public boolean isWriteComplete() {
        return this.state == WRITE_COMPLETE;
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

    private void setCharsetDecoder(CharsetDecoder charsetDecoder) {
        this.charsetDecoder = charsetDecoder;
    }

    public void setDecodeMode(int decodeMode) {
        this.decodeMode = decodeMode;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public void setTrailer(byte[] trailer) {
        this.trailer = trailer;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setDelimiterTag(byte[] delimiterTag) {
        this.delimiterTag = delimiterTag;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }
}
