/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.internal.logging.InternalLogger;
import org.apache.logging.log4j.ThreadContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to log the websocket access logs
 */
public class WebsocketAccessLogger {

    private final InternalLogger logger;
    Map<String, String> logFields = new HashMap<>();
    private static final String DEFAULT_LOG_FIELD_VALUE = "-";
    private static final String CHANNEL = "channel";
    private static final String METHOD = "method";
    private static final String URI_PATH = "uriPath";
    private static final String HTTP_PROTOCOL_VERSION = "httpProtocolVersion";
    private static final String HOST_PORT = "host-port";
    private static final String CONNECTION_HEADER = "connectionHeader";
    private static final String SEC_WEBSOCKET_VERSION = "secWebSocketVersion";
    private static final String UPGRADE = "upgrade";
    private static final String CONNECTION = "Connection";
    private static final String UPGRADE_HEADER = "upgradeHeader";
    private static final String USER_AGENT_HEADER = "userAgentHeader";
    private static final String HANDSHAKE_STATUS = "handshakeStatus";
    private static final String FRAME_LENGTH = "frameLength";
    private static final String HTTP_RESPONSE = "httpResponse";

    private static final String USER_AGENT = "User-Agent";

    public WebsocketAccessLogger(InternalLogger logger) {
        this.logger = logger;
        // add fields to map with default values
        logFields.put(CHANNEL, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(METHOD, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(URI_PATH, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(HTTP_PROTOCOL_VERSION, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(HOST_PORT, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(CONNECTION_HEADER, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(SEC_WEBSOCKET_VERSION, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(UPGRADE, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(USER_AGENT_HEADER, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(HANDSHAKE_STATUS, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(FRAME_LENGTH, DEFAULT_LOG_FIELD_VALUE);
        logFields.put(HTTP_RESPONSE, DEFAULT_LOG_FIELD_VALUE);

        // Set default values to all the fields
        ThreadContext.putAll(logFields);
    }

    /**
     * Log the channel registered event
     */
    public void logChannelRegistered(ChannelHandlerContext ctx) {
        String channel = ctx.channel().toString();
        logFields.put(CHANNEL, channel);
        ThreadContext.put(CHANNEL, logFields.get(CHANNEL));
        logger.debug("CHANNEL REGISTERED");

        logFields.replaceAll((k, v) -> DEFAULT_LOG_FIELD_VALUE);
        ThreadContext.clearAll();
        ThreadContext.putAll(logFields);
    }

    /**
     * Log the channel unregistered event
     */
    public void logChannelUnregistered(ChannelHandlerContext ctx) {
        String channel = ctx.channel().toString();
        logFields.put(CHANNEL, channel);
        ThreadContext.put(CHANNEL, logFields.get(CHANNEL));
        logger.debug("CHANNEL UNREGISTERED");

        logFields.replaceAll((k, v) -> DEFAULT_LOG_FIELD_VALUE);
        ThreadContext.clearAll();
        ThreadContext.putAll(logFields);
    }

    /**
     * Log the channel read event
     */
    public void logChannelRead(ChannelHandlerContext ctx, Object msg) {
        String channel = ctx.channel().toString();
        logFields.put(CHANNEL, channel);
        ThreadContext.put(CHANNEL, logFields.get(CHANNEL));

        String content = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);
        FullHttpRequest fullHttpRequest = convertToFullHttpRequest(content);

        if (fullHttpRequest != null) {
            // initial websocket connection http request
            logFields.put(METHOD, fullHttpRequest.method().name());
            logFields.put(URI_PATH, fullHttpRequest.uri());
            logFields.put(HTTP_PROTOCOL_VERSION, fullHttpRequest.protocolVersion().text());
            logFields.put(HOST_PORT, fullHttpRequest.headers().get("Host"));
            logFields.put(CONNECTION_HEADER, fullHttpRequest.headers().get(CONNECTION));
            logFields.put(SEC_WEBSOCKET_VERSION, fullHttpRequest.headers().get("Sec-WebSocket-Version"));
            logFields.put(UPGRADE_HEADER, fullHttpRequest.headers().get("Upgrade"));
            logFields.put(USER_AGENT_HEADER, fullHttpRequest.headers().get(USER_AGENT));

            ThreadContext.put(CHANNEL, logFields.get(CHANNEL));
            ThreadContext.put(METHOD, logFields.get(METHOD));
            ThreadContext.put(URI_PATH, logFields.get(URI_PATH));
            ThreadContext.put(HTTP_PROTOCOL_VERSION, logFields.get(HTTP_PROTOCOL_VERSION));
            ThreadContext.put(HOST_PORT, logFields.get(HOST_PORT));
            ThreadContext.put(CONNECTION_HEADER, logFields.get(CONNECTION_HEADER));
            ThreadContext.put(SEC_WEBSOCKET_VERSION, logFields.get(SEC_WEBSOCKET_VERSION));
            ThreadContext.put(UPGRADE_HEADER, logFields.get(UPGRADE_HEADER));
            ThreadContext.put(USER_AGENT_HEADER, logFields.get(USER_AGENT_HEADER));
            ThreadContext.put(HTTP_RESPONSE, logFields.get(HTTP_RESPONSE));

            logger.debug("HANDSHAKE REQUEST");
        } else {
            logFields.put(FRAME_LENGTH, String.valueOf(((ByteBuf) msg).readableBytes() - 6));
            ThreadContext.put(FRAME_LENGTH, logFields.get(FRAME_LENGTH));
            logger.debug("INBOUND FRAME");
        }

        logFields.replaceAll((k, v) -> DEFAULT_LOG_FIELD_VALUE);
        ThreadContext.clearAll();
        ThreadContext.putAll(logFields);
    }

    /**
     * Log the channel write event
     */
    public void logChannelWrite(ChannelHandlerContext ctx, Object msg) {
        String channel = ctx.channel().toString();
        logFields.put(CHANNEL, channel);
        ThreadContext.put(CHANNEL, logFields.get(CHANNEL));
        String content = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);

        FullHttpResponse fullHttpResponse = convertToFullHttpResponse(content);
        if (fullHttpResponse != null) {
            logFields.put(HTTP_RESPONSE, fullHttpResponse.status().toString());
            ThreadContext.put(HTTP_RESPONSE, logFields.get(HTTP_RESPONSE));
            String handshakeStatus = "failure";

            if (fullHttpResponse.status() == HttpResponseStatus.SWITCHING_PROTOCOLS) {
                // This is a Websocket handshake response
                logFields.put(HTTP_PROTOCOL_VERSION, fullHttpResponse.protocolVersion().toString());

                String upgradeHeader = fullHttpResponse.headers().get(UPGRADE);
                String connectionHeader = fullHttpResponse.headers().get(CONNECTION);

                logFields.put(UPGRADE_HEADER, fullHttpResponse.headers().get(UPGRADE));
                logFields.put(CONNECTION_HEADER, fullHttpResponse.headers().get(CONNECTION));

                String secWebSocketAccept = fullHttpResponse.headers().get("Sec-WebSocket-Accept");
                if (secWebSocketAccept != null && "websocket".equalsIgnoreCase(
                        upgradeHeader) && "upgrade".equalsIgnoreCase(connectionHeader)) {
                    // handshake is successful
                    handshakeStatus = "success";
                }
                logFields.put(HANDSHAKE_STATUS, handshakeStatus);

                ThreadContext.put(HTTP_PROTOCOL_VERSION, logFields.get(HTTP_PROTOCOL_VERSION));
                ThreadContext.put(UPGRADE_HEADER, logFields.get(UPGRADE_HEADER));
                ThreadContext.put(CONNECTION_HEADER, logFields.get(CONNECTION_HEADER));
                ThreadContext.put(HANDSHAKE_STATUS, logFields.get(HANDSHAKE_STATUS));

                logger.debug("HANDSHAKE RESPONSE");
            } else if (fullHttpResponse.status() == HttpResponseStatus.UNAUTHORIZED) {
                // Http response related to unauthorized request
                logger.debug("UNAUTHORIZED REQUEST");
            } else {
                // Other Http responses related to handshake failure
                logFields.put(HANDSHAKE_STATUS, HANDSHAKE_STATUS);
                ThreadContext.put(HANDSHAKE_STATUS, logFields.get(HANDSHAKE_STATUS));
                logger.debug("HANDSHAKE FAILURE");
            }
        } else {
            // WS Source frame
            logFields.put(FRAME_LENGTH, String.valueOf(((ByteBuf) msg).readableBytes() - 2));
            ThreadContext.put(FRAME_LENGTH, logFields.get(FRAME_LENGTH));
            logger.debug("OUTBOUND FRAME");
        }

        logFields.replaceAll((k, v) -> DEFAULT_LOG_FIELD_VALUE);
        ThreadContext.clearAll();
        ThreadContext.putAll(logFields);
    }

    /**
     * Convert raw HTTP request to FullHttpRequest
     */
    public static FullHttpRequest convertToFullHttpRequest(String rawHttpRequest) {
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestDecoder(), new HttpObjectAggregator(65536));
        // Write the raw HTTP request as a ByteBuf into the channel
        channel.writeInbound(Unpooled.copiedBuffer(rawHttpRequest.getBytes()));

        // Read the aggregated FullHttpRequest from the channel
        Object decodedObject = channel.readInbound();
        if (decodedObject instanceof FullHttpRequest) {
            return (FullHttpRequest) decodedObject;
        } else {
            return null;
        }
    }

    /**
     * Convert raw HTTP response to FullHttpResponse
     */
    private FullHttpResponse convertToFullHttpResponse(String rawHttpResponse) {
        EmbeddedChannel channel = new EmbeddedChannel(new HttpResponseDecoder(), new HttpObjectAggregator(65536));

        // Write the raw HTTP request as a ByteBuf into the channel
        channel.writeInbound(Unpooled.copiedBuffer(rawHttpResponse.getBytes()));

        // Read the aggregated FullHttpRequest from the channel
        Object decodedObject = channel.readInbound();
        if (decodedObject instanceof FullHttpResponse) {
            return (FullHttpResponse) decodedObject;
        } else {
            return null;
        }
    }
}
