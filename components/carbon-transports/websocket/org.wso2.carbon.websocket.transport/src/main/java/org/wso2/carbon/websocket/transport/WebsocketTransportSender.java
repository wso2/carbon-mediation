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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class WebsocketTransportSender extends AbstractTransportSender {

    private WebsocketConnectionFactory connectionFactory;

    private static final Log log = LogFactory.getLog(WebsocketTransportSender.class);

    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut)
            throws AxisFault {
        log.info("Initializing WS Connection Factory.");
        super.init(cfgCtx, transportOut);
        connectionFactory = WebsocketConnectionFactory.getInstance(transportOut);
    }

    public void sendMessage(MessageContext msgCtx, String targetEPR, OutTransportInfo trpOut)
            throws AxisFault {
        String sourceIdentier = null;
        boolean handshakePresent = false;
        String responceDispatchSequence = null;
        String responceErrorSequence = null;
        String messageType = null;

        InboundResponseSender responseSender = null;
        if (msgCtx.getProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER) != null) {
            responseSender = (InboundResponseSender)
                    msgCtx.getProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER);
            sourceIdentier = ((ChannelHandlerContext)msgCtx.getProperty(WebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT)).channel().toString();
        } else {
            sourceIdentier = WebsocketConstants.UNIVERSAL_SOURCE_IDENTIFIER;
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

        try {
            log.info("Fetching a Connection from the WS Connection Factory.");
            WebSocketClientHandler clientHandler = connectionFactory.getChannelHandler(new URI(targetEPR), sourceIdentier,
                    handshakePresent, responceDispatchSequence, responceErrorSequence, messageType);
            String tenantDomain = (String) msgCtx.getProperty(MultitenantConstants.TENANT_DOMAIN);
            clientHandler.setTenantDomain(tenantDomain);
            if (!sourceIdentier.equals(WebsocketConstants.UNIVERSAL_SOURCE_IDENTIFIER)) {
                clientHandler.registerWebsocketResponseSender(responseSender);
                clientHandler.setDispatchSequence(responceDispatchSequence);
                clientHandler.setDispatchErrorSequence(responceErrorSequence);
            }

            if (msgCtx.getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT) != null
                    && msgCtx.getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT).equals(true)) {
                WebSocketFrame frame = (BinaryWebSocketFrame) msgCtx.getProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME);
                log.info("Sending the message to the WS server.");
                clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame);
            } else {
                if (!handshakePresent) {
                    OMOutputFormat format = BaseUtils.getOMOutputFormat(msgCtx);
                    MessageFormatter messageFormatter =
                            MessageProcessorSelector.getMessageFormatter(msgCtx);
                    StringWriter sw = new StringWriter();
                    OutputStream out = new WriterOutputStream(sw, format.getCharSetEncoding());
                    messageFormatter.writeTo(msgCtx, format, out, true);
                    out.close();
                    final String msg = sw.toString();
                    WebSocketFrame frame = new TextWebSocketFrame(msg);
                    log.info("Sending the message to the WS server.");
                    clientHandler.getChannelHandlerContext().channel().writeAndFlush(frame);
                } else {
                    clientHandler.acknowledgeHandshake();
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error parsing the WS endpoint url", e);
        } catch (IOException e) {
            log.error("Error writting to the websocket channel", e);
        } catch (InterruptedException e) {
            log.error("Error writting to the websocket channel", e);
        }
    }
}
