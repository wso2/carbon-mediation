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

package org.wso2.carbon.websocket.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.AttributeKey;
import java.util.Objects;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketResponseSender;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketSourceHandler;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.websocket.transport.utils.LogUtil;
import org.wso2.securevault.SecretResolver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

public class WebsocketTransportSender extends AbstractTransportSender {

    private WebsocketConnectionFactory connectionFactory;
    private final String API_PROPERTIES = "API_PROPERTIES";

    private static final Log log = LogFactory.getLog(WebsocketTransportSender.class);

    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Initializing WS Connection Factory.");
        }
        super.init(cfgCtx, transportOut);
        connectionFactory = new WebsocketConnectionFactory(transportOut);
    }

    public void sendMessage(MessageContext msgCtx, String targetEPR, OutTransportInfo trpOut)
            throws AxisFault {
        String sourceIdentifier = null;
        String correlationId = null;
        boolean handshakePresent = false;
        String responceDispatchSequence = null;
        String responceErrorSequence = null;
        String messageType = null;
        String wsSubProtocol = null;
        boolean isConnectionTerminate = false;
        Map<String, Object> customHeaders = new HashMap<>();
        Map<String, Object> apiProperties = new HashMap<>();

        if (log.isDebugEnabled()) {
            log.debug("Endpoint url: " + targetEPR);
        }

        // Store the target endpoint address in the channel attributes to make it available throughout the lifecycle of the connection
        AttributeKey<Map<String, Object>> WSO2_PROPERTIES = AttributeKey.valueOf(WebsocketConstants.WSO2_PROPERTIES);
        Map<String, Object> propertiesMap = ((ChannelHandlerContext) (msgCtx.getProperty(
                WebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT))).channel().attr(WSO2_PROPERTIES).get();
        if (propertiesMap == null) {
            propertiesMap = new HashMap<>();
        }
        propertiesMap.put(WebsocketConstants.TARGET_ENDPOINT_ADDRESS, targetEPR);
        ((ChannelHandlerContext) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT)).channel()
                .attr(WSO2_PROPERTIES).set(propertiesMap);

        InboundResponseSender responseSender = null;
        if (msgCtx.getProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER) != null) {
            responseSender = (InboundResponseSender)
                    msgCtx.getProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER);
            if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_CHANNEL_IDENTIFIER) != null) {
                sourceIdentifier = msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_CHANNEL_IDENTIFIER).toString();
            } else {
                sourceIdentifier = ((ChannelHandlerContext) msgCtx.
                        getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT)).channel().toString();
            }
        } else {
            sourceIdentifier = WebsocketConstants.UNIVERSAL_SOURCE_IDENTIFIER;
        }

        // The backend url is fixed at deployment time, so the caller's query string is merged in here.
        // Backend connections are pooled per source channel and the query is constant for the lifetime of
        // a channel, so a merged query cannot cross callers. The universal identifier is a shared pool and
        // is therefore skipped.
        if (!WebsocketConstants.UNIVERSAL_SOURCE_IDENTIFIER.equals(sourceIdentifier)
                && isForwardInboundQueryParamsEnabled(msgCtx)) {
            targetEPR = mergeInboundQueryParams(targetEPR, msgCtx);
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDSHAKE_PRESENT) != null
                && msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDSHAKE_PRESENT).equals(true)) {
            handshakePresent = true;
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE) != null) {
            responceDispatchSequence = (String) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE);
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE) != null) {
            responceErrorSequence = (String) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE);
        }

        if (msgCtx.getProperty(WebsocketConstants.CONTENT_TYPE) != null) {
            messageType = (String) msgCtx.getProperty(WebsocketConstants.CONTENT_TYPE);
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_CORRELATION_ID) != null) {
            correlationId = (String) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_CORRELATION_ID);
        } else {
            correlationId = sourceIdentifier;
        }

        if (((ChannelHandlerContext) (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT))).channel()
                .attr(AttributeKey.valueOf(API_PROPERTIES)).get() != null) {
            apiProperties = (Map<String, Object>) ((ChannelHandlerContext) (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT))).channel()
                    .attr(AttributeKey.valueOf(API_PROPERTIES)).get();
        }

        if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SUBPROTOCOL) != null) {
            wsSubProtocol = (String) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SUBPROTOCOL);
        }
        if (Boolean.valueOf(true).equals(msgCtx.getProperty(WebsocketConstants.CONNECTION_TERMINATE))) {
            isConnectionTerminate = true;
        }
        if (log.isDebugEnabled()) {
            log.debug(correlationId + " -- sendMessage triggered with sourceChannel: " + sourceIdentifier
                    + ", websocket sub protocol: " + wsSubProtocol + ", in the Thread,ID: " + Thread.currentThread()
                    .getName() + "," + Thread.currentThread().getId() + ", URL: " + targetEPR + " API context: "
                    + apiProperties.get(WebsocketConstants.API_CONTEXT));
        }
        String tenantDomain = (String) msgCtx.getProperty(MultitenantConstants.TENANT_DOMAIN);
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        /*
        * Preserve the specified headers from the original request by adding to the customHeaders map.
        * Headers present in this map won't be set at Netty level.
        */
        Parameter preserveWebSocketHeadersParameter = msgCtx.getTransportOut()
                .getParameter(WebsocketConstants.WEBSOCKET_HEADERS_PRESERVE_CONFIG);
        if (preserveWebSocketHeadersParameter != null && preserveWebSocketHeadersParameter.getValue() != null &&
                !preserveWebSocketHeadersParameter.getValue().toString().isEmpty()) {
            String preservableHeaders = preserveWebSocketHeadersParameter.getValue().toString();
            for (String header : preservableHeaders.split(",")) {
                Object headerValue = msgCtx.getProperty(header);
                Iterator<String> headerNames = msgCtx.getPropertyNames();
                while (headerValue == null && headerNames.hasNext()) {
                    String headerName = headerNames.next();
                    if (headerName.equalsIgnoreCase(header)) {
                        headerValue = msgCtx.getProperty(headerName);
                    }
                }
                if (headerValue != null) {
                    customHeaders.put(header, headerValue);
                }
            }
        }

        /*
        * Get all the message property names and check whether the properties with the websocket custom header
        * prefix are exist in the property map.
        *
        * This is used to add new headers to the handshake request. The property format
        * <prefix>.<header>
        *
        * If there is any property with the prefix, extract the header string from the property key and put to the
        * customHeaders map.
        */
        Iterator<String> propertyNames = msgCtx.getPropertyNames();
        String webSocketCustomHeaderPrefix;
        Parameter wsCustomHeaderParam =
                msgCtx.getTransportOut().getParameter(WebsocketConstants.WEBSOCKET_CUSTOM_HEADER_CONFIG);

        // avoid using org.apache.commons.lang.StringUtils due to osgi issue
        if (wsCustomHeaderParam == null || wsCustomHeaderParam.getValue() == null || wsCustomHeaderParam.getValue()
                .toString().isEmpty()) {
            webSocketCustomHeaderPrefix = WebsocketConstants.WEBSOCKET_CUSTOM_HEADER_PREFIX;
        } else {
            webSocketCustomHeaderPrefix = wsCustomHeaderParam.getValue().toString();
        }
        while (propertyNames.hasNext()) {
            String propertyName = propertyNames.next();
            if (propertyName.startsWith(webSocketCustomHeaderPrefix)) {
                Object value = msgCtx.getProperty(propertyName);
                String headerSplits[] = propertyName.split(webSocketCustomHeaderPrefix);
                if (headerSplits.length > 1) {
                    customHeaders.put(headerSplits[1], value);
                    if (log.isDebugEnabled()) {
                        log.debug(correlationId + " -- Adding Custom Header " + headerSplits[1] + ":" + value);
                    }
                } else {
                    log.warn("A header identified with having only the websocket custom-header prefix"
                            + " as the key (without a unique postfix). Hence dropping the header.");
                }
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug(correlationId
                        + " -- Fetching a Connection from the WS(WSS) Connection Factory with sourceChannel : "
                        + sourceIdentifier + ", in the Thread,ID: " + Thread.currentThread().getName() + "," + Thread
                        .currentThread().getId() + ", URL: " + targetEPR + " API context: " + apiProperties
                        .get(WebsocketConstants.API_CONTEXT));
            }
            ConfigurationContext configurationContext = msgCtx.getConfigurationContext();
            SecretResolver resolver = null;
            if (configurationContext != null && configurationContext.getAxisConfiguration() != null) {
                resolver = configurationContext.getAxisConfiguration().getSecretResolver();
            }
            WebSocketClientHandler clientHandler = connectionFactory.getChannelHandler(tenantDomain,
                                                                                       new URI(targetEPR),
                                                                                       sourceIdentifier, handshakePresent,
                                                                                       responceDispatchSequence,
                                                                                       responceErrorSequence,
                                                                                       messageType, wsSubProtocol,
                                                                                       isConnectionTerminate,
                                                                                       customHeaders, responseSender,
                                                                                       responceDispatchSequence,
                                                                                       responceErrorSequence,
                                                                                       correlationId,
                                                                                       apiProperties,
                                                                                       resolver);
            if (clientHandler == null && isConnectionTerminate) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            correlationId + " -- Backend connection does not exist. No need to send close frame to backend "
                                    + "with sourceChannel : " + sourceIdentifier + ", in the Thread,ID: " + Thread
                                    .currentThread().getName() + "," + Thread.currentThread().getId() + ", URL: "
                                    + targetEPR + " API context: " + apiProperties.get(WebsocketConstants.API_CONTEXT));
                }
                return;
            }

            if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT) != null
                    && msgCtx.getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT).equals(true)) {
                WebSocketFrame frame = (BinaryWebSocketFrame) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME);
                try {
                    if (log.isDebugEnabled()) {
                        log.debug(correlationId + " -- Sending the binary frame to the WS server on context id : "
                                          + clientHandler.getChannelHandlerContext().channel().toString());
                    }
                    if (clientHandler.getChannelHandlerContext().channel().isActive()) {
                        clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame.retain());
                        if (log.isDebugEnabled()) {
                            LogUtil.printWebSocketFrame(log, frame, clientHandler.getChannelHandlerContext(), false,
                                    correlationId);
                        }
                    }
                } finally {
                    ReferenceCountUtil.release(frame);
                }
            } else if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT) != null
                    && msgCtx.getProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT).equals(true)) {
                WebSocketFrame frame = (TextWebSocketFrame) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME);
                try {
                    if (log.isDebugEnabled()) {
                        log.debug(correlationId + " -- Sending the passthrough text frame to the WS server on context id: "
                                + clientHandler.getChannelHandlerContext().channel().toString() + ", "
                                + ", sourceIdentifier: " + sourceIdentifier + ", in the Thread,ID: " + Thread
                                .currentThread().getName() + "," + Thread.currentThread().getId() + ", URL: "
                                + targetEPR + " API context: " + apiProperties.get(WebsocketConstants.API_CONTEXT));
                    }
                    if (clientHandler.getChannelHandlerContext().channel().isActive()) {
                        clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame.retain());
                        if (log.isDebugEnabled()) {
                            LogUtil.printWebSocketFrame(log, frame, clientHandler.getChannelHandlerContext(), false,
                                    correlationId);
                        }
                    }
                } finally {
                    ReferenceCountUtil.release(frame);
                }
            } else if (Objects.equals(true, msgCtx.getProperty(WebsocketConstants.WEBSOCKET_PING_FRAME_PRESENT))) {
                WebSocketFrame frame = (PingWebSocketFrame) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_PING_FRAME);
                dispatchPingPongFrame(clientHandler, sourceIdentifier, frame, "ping");

            } else if (Objects.equals(true, msgCtx.getProperty(WebsocketConstants.WEBSOCKET_PONG_FRAME_PRESENT))) {
                WebSocketFrame frame = (PongWebSocketFrame) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_PONG_FRAME);
                dispatchPingPongFrame(clientHandler, sourceIdentifier, frame, "pong");
            } else if (isConnectionTerminate) {
                Channel channel = clientHandler.getChannelHandlerContext().channel();
                if (log.isDebugEnabled()) {
                    log.debug(correlationId + " -- Sending CloseWebsocketFrame to WS server on context id: " + clientHandler
                            .getChannelHandlerContext().channel().toString() + ", " + ", sourceIdentifier: "
                            + sourceIdentifier + ", in the Thread,ID: " + Thread.currentThread().getName() + "," + Thread
                            .currentThread().getId() + ", URL: " + targetEPR + " API context: " + apiProperties
                            .get(WebsocketConstants.API_CONTEXT));
                }
                if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_CLOSE_CODE) != null) {
                    int statusCode = (int) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_CLOSE_CODE);
                    String reasonText = (String) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_REASON_TEXT);
                    channel.writeAndFlush(new CloseWebSocketFrame(statusCode, reasonText));
                } else {
                    channel.writeAndFlush(new CloseWebSocketFrame());
                }
                channel.close();
            } else {
                if (!handshakePresent) {
                    RelayUtils.buildMessage(msgCtx, false);
                    OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
                    MessageFormatter messageFormatter =
                            MessageProcessorSelector.getMessageFormatter(msgCtx);
                    StringWriter sw = new StringWriter();
                    OutputStream out = new WriterOutputStream(sw, format.getCharSetEncoding());
                    messageFormatter.writeTo(msgCtx, format, out, true);
                    out.close();
                    final String msg = sw.toString();
                    WebSocketFrame frame = new TextWebSocketFrame(msg);
                    if (log.isDebugEnabled()) {
                        log.debug(correlationId + " -- Sending the text frame to the WS server on context id : "
                                + clientHandler.getChannelHandlerContext().channel().toString() + ", URL: " + targetEPR
                                + " API context: " + apiProperties.get(WebsocketConstants.API_CONTEXT));
                    }
                    if (clientHandler.getChannelHandlerContext().channel().isActive()) {
                        clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame.retain());
                        if (log.isDebugEnabled()) {
                            LogUtil.printWebSocketFrame(log, frame, clientHandler.getChannelHandlerContext(), false,
                                    correlationId);
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(correlationId + " -- AcknowledgeHandshake to WS server on context id: " + clientHandler
                                .getChannelHandlerContext().channel().toString() + ", " + ", sourceIdentifier: "
                                + sourceIdentifier + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                                + Thread.currentThread().getId() + ", URL: " + targetEPR + " API context: "
                                + apiProperties.get(WebsocketConstants.API_CONTEXT));
                    }
                    clientHandler.acknowledgeHandshake();
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error parsing the WS endpoint url", e);
        } catch (ConnectException e) {
            handleClientConnectionError(responseSender, e);
        } catch (IOException e) {
            log.error("Error writing to the websocket channel", e);
        } catch (InterruptedException e) {
            log.error("Error writing to the websocket channel", e);
        } catch (XMLStreamException e) {
            handleException("Error while building message", e);
        }
    }

    private void dispatchPingPongFrame(WebSocketClientHandler clientHandler, String sourceIdentifier,
                                       WebSocketFrame frame, String frameType) {
        try {
            if (clientHandler.getChannelHandlerContext().channel().isActive()) {
                if (log.isDebugEnabled()) {
                    log.debug("Sending the " + frameType + " frame to the WS server on context id: "
                                      + clientHandler.getChannelHandlerContext().channel().toString() + ", "
                                      + ", sourceIdentifier: " + sourceIdentifier + ", in the Thread,ID: "
                                      + Thread.currentThread().getName() + "," + Thread.currentThread().getId());
                }
                clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame.retain());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Disregarding the " + frameType + " frame on " + ", sourceIdentifier: " + sourceIdentifier
                                      + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                                      + Thread.currentThread().getId() + " due to the Channel being inactive");
                }
            }
        } finally {
            ReferenceCountUtil.release(frame);
        }
    }

    /**
     * Checks whether forwarding of inbound query parameters is enabled on this transport sender. Disabled
     * unless "ws.forward.query.params" is explicitly set to true.
     *
     * @param msgCtx axis2 message context of the outgoing message
     * @return true if the caller's query string should be merged into the backend url
     */
    private boolean isForwardInboundQueryParamsEnabled(MessageContext msgCtx) {

        if (msgCtx.getTransportOut() == null) {
            return false;
        }
        Parameter forwardQueryParams =
                msgCtx.getTransportOut().getParameter(WebsocketConstants.WEBSOCKET_FORWARD_QUERY_PARAMS_CONFIG);
        return forwardQueryParams != null && forwardQueryParams.getValue() != null
                && Boolean.parseBoolean(forwardQueryParams.getValue().toString().trim());
    }

    /**
     * Merges the query string of the inbound request into the backend url. The backend url is built at
     * deployment time from the endpoint url and the topic mapping, so it never carries the caller's query.
     * Pairs are appended verbatim, without decoding, so encoded values reach the backend exactly as the
     * caller sent them. A name already present on the backend url keeps its configured value.
     *
     * The merged url is discarded if it does not parse, because the query is caller controlled and
     * java.net.URI rejects characters that a caller can send.
     *
     * @param targetEPR backend url resolved from the endpoint definition
     * @param msgCtx    axis2 message context carrying the inbound request url
     * @return backend url with the caller's query merged in, or targetEPR unchanged
     */
    protected String mergeInboundQueryParams(String targetEPR, MessageContext msgCtx) {

        Object inboundUrl = msgCtx.getProperty(Constants.Configuration.TRANSPORT_IN_URL);
        if (targetEPR == null || inboundUrl == null) {
            return targetEPR;
        }
        String inbound = inboundUrl.toString();
        int queryStart = inbound.indexOf('?');
        if (queryStart < 0) {
            return targetEPR;
        }

        StringBuilder toAppend = new StringBuilder();
        for (String pair : inbound.substring(queryStart + 1).split("&")) {
            int equals = pair.indexOf('=');
            String name = equals < 0 ? pair : pair.substring(0, equals);
            if (!name.isEmpty() && !containsQueryParam(targetEPR, name)) {
                toAppend.append(toAppend.length() == 0 ? "" : "&").append(pair);
            }
        }
        if (toAppend.length() == 0) {
            return targetEPR;
        }

        String separator = targetEPR.indexOf('?') < 0 ? "?"
                : targetEPR.endsWith("?") || targetEPR.endsWith("&") ? "" : "&";
        String merged = targetEPR + separator + toAppend;
        try {
            new URI(merged);
        } catch (URISyntaxException e) {
            log.warn("Inbound query parameters were not merged into the backend url because the result is not a "
                             + "valid URI. Falling back to the configured endpoint url. Reason: " + e.getReason()
                             + " at index " + e.getIndex());
            return targetEPR;
        }
        if (log.isDebugEnabled()) {
            log.debug("Merged inbound query parameters into the backend url. Before: " + targetEPR
                              + ", after: " + merged);
        }
        return merged;
    }

    /**
     * Checks whether the backend url already defines a query parameter with the given name.
     *
     * @param targetEPR backend url resolved from the endpoint definition
     * @param name      query parameter name taken from the inbound request
     * @return true if targetEPR already carries that parameter
     */
    private boolean containsQueryParam(String targetEPR, String name) {

        int queryStart = targetEPR.indexOf('?');
        if (queryStart < 0) {
            return false;
        }
        for (String pair : targetEPR.substring(queryStart + 1).split("&")) {
            int equals = pair.indexOf('=');
            String existing = equals < 0 ? pair : pair.substring(0, equals);
            if (existing.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void handleClientConnectionError(InboundResponseSender responseSender, Exception e) {

        log.error("Error writing to the websocket channel", e);
        // we will close the client connection and notify with close frame
        InboundWebsocketSourceHandler sourceHandler = ((InboundWebsocketResponseSender) responseSender).getSourceHandler();
        CloseWebSocketFrame closeWebSocketFrame = new CloseWebSocketFrame(WebsocketConstants.WEBSOCKET_UPSTREAM_ERROR_SC,
                "Error connecting with the backend");
        try {
            sourceHandler.handleClientWebsocketChannelTermination(closeWebSocketFrame);
        } catch (AxisFault fault) {
            log.error("Error occurred while sending close frames", fault);
        }
    }
}
