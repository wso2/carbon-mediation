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

package org.wso2.carbon.inbound.endpoint.protocol.tcp.codec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.context.TCPContext;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.TCPContextException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;

/**
 * After reading the bytes from the stream. decoding the tcp message from bytes is done here.
 */

public class TCPCodec {
    private static final Log log = LogFactory.getLog(TCPCodec.class);

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
    private boolean oneWayMessaging = false;

    private int currentPosition = 0;
    private int initialBufferLimit;

    public TCPCodec() {
        this.state = READ_HEADER;
        this.charsetDecoder = InboundTCPConstants.UTF8_CHARSET.newDecoder();
    }

    public TCPCodec(CharsetDecoder charsetDecoder) {
        this.state = READ_HEADER;
        setCharsetDecoder(charsetDecoder);
    }

    //here we decode the byte stream
    public int decode(ByteBuffer dst, TCPContext context) throws IOException, TCPContextException {

        initialBufferLimit = dst.limit();
        log.info("Initial buffer limit : " + initialBufferLimit);
        log.info("position : " + dst.position() + " limit : " + dst.limit());

        switch (decodeMode) {

            case InboundTCPConstants.DECODE_BY_HEADER_TRAILER: {
                //decode the first message of the buffer
                if (this.state >= READ_COMPLETE || dst.position() < 0) {
                    return -1;
                }

                if (this.state == READ_HEADER) {
                    if (dst.get(0) == header[0]) {
                        dst.position(1);
                        this.state = READ_CONTENT;
                    } else {
                        throw new TCPContextException("Could not find header in incoming message.");
                    }
                }

                if (this.state == READ_CONTENT) {

                    int trailerIndex = findTrailer(dst);

                    if (trailerIndex > -1) {
                        dst.limit(trailerIndex);
                        this.state = READ_TRAILER;
                    }

                    if (dst.hasArray()) {
                        byte[] ar = new byte[dst.remaining()];
                        dst.get(ar, 0, ar.length);
                        context.getBaos().write(ar);
                        log.info("length of the byte array is : " + context.getBaos().toByteArray().length);
                        log.info("bytes " + ar[0] + " " + ar[1]);
                    }

                    //context.getRequestBuffer().append(charsetDecoder.decode(dst).toString());
                    //set the buffer position and limit for the rest of the buffer
                    dst.limit(initialBufferLimit);
                    dst.position(trailerIndex + 2);
                    dst.compact();
                }

                if (this.state == READ_TRAILER) {
                    this.state = READ_COMPLETE;

                    //here we have the TCP message as a sting buffer we store the message in TCP message context.
                    //context.setTCPMessage(context.getRequestBuffer().toString());
                    //context.getRequestBuffer().setLength(0);
                    //context.setTCPMessage(new String(context.getBaos().toByteArray(), charsetDecoder.charset()));
                    return InboundTCPConstants.ONE_TCP_MESSAGE_IS_DECODED;
                }

            }
            case InboundTCPConstants.DECODE_BY_TAG: {
                //
                byte[] input = new byte[dst.remaining()];
                dst.get(input, 0, input.length);

                //decode the first message of the buffer
                log.info("input length : " + input.length);

                if (this.state >= READ_COMPLETE || dst.position() < 0) {
                    return -1;
                }

                if (this.state == READ_HEADER) {
                    // log.info(byteString);

                    int headerIndex = findTagIndex(input, delimiterTag, 0);
                    log.info("header index : " + headerIndex);

                    if (headerIndex > 0) {
                        dst.position(headerIndex - 1);
                        this.state = READ_CONTENT;
                    } else {
                        throw new TCPContextException("Could not find header tag in incoming message.");
                    }
                }

                if (this.state == READ_CONTENT) {

                    int trailerIndex = findTagIndex(input, delimiterTag, dst.position() + delimiterTag.length);
                    log.info("Trailer index : " + trailerIndex);

                    if (trailerIndex > -1) {
                        dst.limit(trailerIndex + delimiterTag.length + 1);
                        this.state = READ_TRAILER;
                    }

                    if (dst.hasArray()) {
                        byte[] ar = new byte[dst.remaining()];
                        dst.get(ar, 0, ar.length);
                        context.getBaos().write(ar);
                        log.info("length of the byte array is : " + context.getBaos().toByteArray().length);
                        log.info("bytes " + ar[0] + " " + ar[1]);
                    }

                    //set the buffer position and limit for the rest of the buffer
                    log.info("position : " + dst.position() + " limit : " + dst.limit());
                    dst.limit(initialBufferLimit);
                    dst.position(trailerIndex + tag.length() + 1);
                    log.info("position : " + dst.position() + " limit : " + dst.limit());
                    dst.compact();
                    log.info("position : " + dst.position() + " limit : " + dst.limit());

                }

                if (this.state == READ_TRAILER) {
                    this.state = READ_COMPLETE;

                    //here we have the TCP message as a sting buffer we store the message in TCP message context.
                    //context.setTCPMessage(context.getRequestBuffer().toString());
                    // log.info(context.getTCPMessage());
                    //context.getRequestBuffer().setLength(0);
                    context.setTCPMessage(new String(context.getBaos().toByteArray(), charsetDecoder.charset()));
                    return InboundTCPConstants.ONE_TCP_MESSAGE_IS_DECODED;
                }

                //
/*
                //decode the first message of the buffer
                String byteString = charsetDecoder.decode(dst).toString();
                log.info("string length : " + byteString.length());

                if (this.state >= READ_COMPLETE || dst.position() < 0) {
                    return -1;
                }

                if (this.state == READ_HEADER) {
                    // log.info(byteString);

                    int headerIndex = byteString.indexOf(tag);
                    log.info("header index : " + headerIndex);

                    if (headerIndex > 0) {
                        dst.position(headerIndex + tag.length() + 1);
                        this.state = READ_CONTENT;
                    } else {
                        throw new TCPContextException("Could not find header tag in incoming message.");
                    }
                }

                if (this.state == READ_CONTENT) {

                    int trailerIndex = byteString.indexOf(tag, tag.length());
                    log.info("Trailer index : " + trailerIndex);

                    if (trailerIndex > -1) {
                        dst.limit(trailerIndex - 2);
                        this.state = READ_TRAILER;
                    }

                    context.getRequestBuffer().append(charsetDecoder.decode(dst).toString());

                    //set the buffer position and limit for the rest of the buffer
                    log.info("position : " + dst.position() + " limit : " + dst.limit());
                    dst.limit(initialBufferLimit);
                    dst.position(trailerIndex + tag.length() + 1);
                    log.info("position : " + dst.position() + " limit : " + dst.limit());
                    dst.compact();
                    log.info("position : " + dst.position() + " limit : " + dst.limit());

                }

                if (this.state == READ_TRAILER) {
                    this.state = READ_COMPLETE;

                    //here we have the TCP message as a sting buffer we store the message in TCP message context.
                    context.setTCPMessage(context.getRequestBuffer().toString());
                    // log.info(context.getTCPMessage());
                    context.getRequestBuffer().setLength(0);
                    return InboundTCPConstants.ONE_TCP_MESSAGE_IS_DECODED;
                }
*/
            }
            case InboundTCPConstants.DECODE_BY_LENGTH: {

                if (this.state >= READ_COMPLETE || dst.position() < 0) {
                    return -1;
                }

                if ((msgLength - currentPosition) > dst.capacity()) {
                    currentPosition += dst.capacity();
                    context.getRequestBuffer().append(charsetDecoder.decode(dst).toString());
                    this.state = READ_CONTENT;
                } else {
                    dst.limit((msgLength - currentPosition) + 1);
                    context.getRequestBuffer().append(charsetDecoder.decode(dst).toString());
                    this.state = READ_COMPLETE;

                    context.setTCPMessage(context.getRequestBuffer().toString());
                    context.getRequestBuffer().setLength(0);

                    //need to set the position and limit

                    return InboundTCPConstants.ONE_TCP_MESSAGE_IS_DECODED;
                }
            }
        }

        return 0;

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
            if (found)
                return i;
        }
        return -1;
    }

    //get the message from TCP context and convert it byte stream to send
    public int encode(ByteBuffer outBuf, TCPContext context) {
        log.info("state : " + this.state);

        if (this.state < READ_COMPLETE) {
            return 0;
        }

        if (this.state == READ_COMPLETE) {
            if (decodeMode != InboundTCPConstants.DECODE_BY_TAG) {
                responseBytes = context.getTCPMessage().getBytes(charsetDecoder.charset());
            } else {
                String taggedResponse = "<" + tag + ">" + context.getTCPMessage() + "</" + tag + ">";
                responseBytes = taggedResponse.getBytes(charsetDecoder.charset());
            }
            this.state = WRITE_HEADER;
        }

        if (this.state >= WRITE_HEADER) {
            return fillBuffer(outBuf, responseBytes);
        }

        return 0;

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
            }
            case InboundTCPConstants.DECODE_BY_TAG: {
                if (this.state == WRITE_HEADER) {
                    byteBuffer.put(responseBytes);
                    this.state = WRITE_COMPLETE;
                    log.info("state = write complete");
                }
            }
            case InboundTCPConstants.DECODE_BY_LENGTH: {
                if (this.state == WRITE_HEADER) {
                    byteBuffer.put(responseBytes);
                    count = responseBytes.length;
                    this.state = WRITE_COMPLETE;
                }
            }
        }

        byteBuffer.flip();
        return count;
    }

    public void resetState(){
        this.state = READ_HEADER;

    }

    public boolean isReadComplete() {
        if (this.state >= READ_COMPLETE) {
            return true;
        }

        return false;
    }

    public boolean isWriteTrailer() {
        if (this.state == WRITE_TRAILER) {
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

    public void setOneWayMessaging(boolean oneWayMessaging) {
        this.oneWayMessaging = oneWayMessaging;
    }
}