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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.inbound.InboundResponseSender;
import org.apache.synapse.mediators.MediatorFaultHandler;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.websocket.transport.service.ServiceReferenceHolder;
import org.wso2.carbon.websocket.transport.utils.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private static final Log log = LogFactory.getLog(WebSocketClientHandler.class);
    private String dispatchSequence;
    private String dispatchErrorSequence;
    private ChannelHandlerContext ctx;
    private InboundResponseSender responseSender;
    private String tenantDomain;

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public void setDispatchSequence(String dispatchSequence) {
        this.dispatchSequence = dispatchSequence;
    }

    public void setDispatchErrorSequence(String dispatchErrorSequence) {
        this.dispatchErrorSequence = dispatchErrorSequence;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return this.ctx;
    }

    public void registerWebsocketResponseSender(InboundResponseSender responseSender) {
        this.responseSender = responseSender;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("WebSocket client disconnected on context id : " + ctx.channel().toString());
        }
    }

    public void handleHandshake(ChannelHandlerContext ctx, FullHttpResponse msg) {
        if (!handshaker.isHandshakeComplete()) {
            if (log.isDebugEnabled()) {
                LogUtil.printHeaders(log, msg, ctx);
            }
            handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
            if (log.isDebugEnabled()) {
                log.debug("WebSocket client connected to remote WS endpoint on context id : " + ctx.channel().toString());
            }
            handshakeFuture.setSuccess();
            return;
        }
    }

    public void acknowledgeHandshake() {
        try {
            if (handshaker.isHandshakeComplete()) {
                if (responseSender != null) {
                    org.apache.synapse.MessageContext synCtx = getSynapseMessageContext(tenantDomain);
                    synCtx.setProperty(WebsocketConstants.WEBSOCKET_TARGET_HANDSHAKE_PRESENT, true);
                    synCtx.setProperty(WebsocketConstants.WEBSOCKET_TARGET_HANDLER_CONTEXT, ctx);
                    injectToSequence(synCtx, dispatchSequence, dispatchErrorSequence);
                }
            }
        } catch (Exception e) {
            log.error("Exception occured while injecting websocket frames to the Synapse engine", e);
        }
    }

    public void handleTargetWebsocketChannelTermination(WebSocketFrame frame) throws AxisFault {
        handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain()).addListener(ChannelFutureListener.CLOSE);
    }

    public void handleWebsocketBinaryFrame(WebSocketFrame frame) throws AxisFault {
        org.apache.synapse.MessageContext synCtx = getSynapseMessageContext(tenantDomain);
        synCtx.setProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT, true);
        synCtx.setProperty(WebsocketConstants.WEBSOCKET_BINARY_FRAME, frame);
        injectToSequence(synCtx, dispatchSequence, dispatchErrorSequence);
    }

    public void handlePassthroughTextFrame(WebSocketFrame frame) throws AxisFault {
        org.apache.synapse.MessageContext synCtx = getSynapseMessageContext(tenantDomain);
        synCtx.setProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT, true);
        synCtx.setProperty(WebsocketConstants.WEBSOCKET_TEXT_FRAME, frame);
        injectToSequence(synCtx, dispatchSequence, dispatchErrorSequence);
    }

    public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws AxisFault {

        try {
            if (log.isDebugEnabled()) {
                LogUtil.printWebSocketFrame(log, frame, ctx, true);
            }
            if (handshaker.isHandshakeComplete()) {

                if (frame instanceof CloseWebSocketFrame) {
                    handleTargetWebsocketChannelTermination(frame);
                    return;
                } else if ((frame instanceof BinaryWebSocketFrame) && ((handshaker.actualSubprotocol() == null) ||
                        ((handshaker.actualSubprotocol() != null) &&
                                !handshaker.actualSubprotocol().contains(WebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX)))) {
                    handleWebsocketBinaryFrame(frame);
                    return;
                } else if ((frame instanceof PingWebSocketFrame) && ((handshaker.actualSubprotocol() == null) ||
                        ((handshaker.actualSubprotocol() != null) &&
                                !handshaker.actualSubprotocol().contains(WebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX)))) {
                    ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
                    return;
                } else if ((frame instanceof TextWebSocketFrame) && ((handshaker.actualSubprotocol() == null) ||
                        ((handshaker.actualSubprotocol() != null) &&
                                !handshaker.actualSubprotocol().contains(WebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX)))) {
                    handlePassthroughTextFrame(frame);
                    return;
                } else if ((frame instanceof TextWebSocketFrame) &&
                        ((handshaker.actualSubprotocol() != null) &&
                                handshaker.actualSubprotocol().contains(WebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX))) {

                    org.apache.synapse.MessageContext synCtx = getSynapseMessageContext(tenantDomain);

                    String message = ((TextWebSocketFrame) frame).text();
                    String contentType = SubprotocolBuilderUtil.syanapeSubprotocolToContentType(handshaker.actualSubprotocol());

                    org.apache.axis2.context.MessageContext axis2MsgCtx =
                            ((org.apache.synapse.core.axis2.Axis2MessageContext) synCtx)
                                    .getAxis2MessageContext();

                    Builder builder = null;
                    if (contentType == null) {
                        log.debug("No content type specified. Using SOAP builder.");
                        builder = new SOAPBuilder();
                    } else {
                        int index = contentType.indexOf(';');
                        String type = index > 0 ? contentType.substring(0, index)
                                : contentType;
                        try {
                            builder = BuilderUtil.getBuilderFromSelector(type, axis2MsgCtx);
                        } catch (AxisFault axisFault) {
                            log.error("Error while creating message builder :: "
                                    + axisFault.getMessage());
                        }
                        if (builder == null) {
                            if (log.isDebugEnabled()) {
                                log.debug("No message builder found for type '" + type
                                        + "'. Falling back to SOAP.");
                            }
                            builder = new SOAPBuilder();
                        }
                    }

                    OMElement documentElement = null;
                    InputStream in = new AutoCloseInputStream(new ByteArrayInputStream(message.getBytes()));
                    documentElement = builder.processDocument(in, contentType, axis2MsgCtx);
                    synCtx.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
                    injectToSequence(synCtx, dispatchSequence, dispatchErrorSequence);
                }

            } else {
                log.error("Handshake incomplete at target handler. Failed to inject websocket frames to Synapse engine");
            }
        } catch (Exception e) {
            log.error("Exception occured while injecting websocket frames to the Synapse engine", e);
        }

    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            handleHandshake(ctx, (FullHttpResponse) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error encountered while processing the response", cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    private org.apache.synapse.MessageContext getSynapseMessageContext(String tenantDomain) throws AxisFault {
        org.apache.synapse.MessageContext synCtx = createSynapseMessageContext(tenantDomain);
        if (responseSender != null) {
            synCtx.setProperty(SynapseConstants.IS_INBOUND, true);
            synCtx.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER, responseSender);
        }
        synCtx.setProperty(WebsocketConstants.WEBSOCKET_SUBSCRIBER_PATH, handshaker.uri().toString());
        return synCtx;
    }

    private static MessageContext createSynapseMessageContext(String tenantDomain) throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MsgCtx = createAxis2MessageContext();
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            ConfigurationContext tenantConfigCtx =
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain,
                            axis2MsgCtx.getConfigurationContext());
            axis2MsgCtx.setConfigurationContext(tenantConfigCtx);
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);
        } else {
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        }
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        axis2MsgCtx.setEnvelope(envelope);
        return MessageContextCreatorForAxis2.getSynapseMessageContext(axis2MsgCtx);
    }


    private static org.apache.axis2.context.MessageContext createAxis2MessageContext() {
        org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();
        axis2MsgCtx.setMessageID(UIDGenerator.generateURNString());
        axis2MsgCtx.setConfigurationContext(ServiceReferenceHolder.getInstance().getConfigurationContextService()
                .getServerConfigContext());
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING,
                Boolean.FALSE);
        axis2MsgCtx.setServerSide(true);
        return axis2MsgCtx;
    }

    private void injectToSequence(org.apache.synapse.MessageContext synCtx,
                                  String dispatchSequence, String dispatchErrorSequence) {
        SequenceMediator injectingSequence = null;
        if (dispatchSequence != null) {
            injectingSequence = (SequenceMediator) synCtx.getSequence(dispatchSequence);
        }
        if (injectingSequence == null) {
            injectingSequence = (SequenceMediator) synCtx.getMainSequence();
        }
        SequenceMediator faultSequence = getFaultSequence(synCtx, dispatchErrorSequence);
        MediatorFaultHandler mediatorFaultHandler = new MediatorFaultHandler(faultSequence);
        synCtx.pushFaultHandler(mediatorFaultHandler);
        if (log.isDebugEnabled()) {
            log.debug("injecting message to sequence : " + dispatchSequence);
        }
        synCtx.getEnvironment().injectMessage(synCtx, injectingSequence);
    }

    private SequenceMediator getFaultSequence(org.apache.synapse.MessageContext synCtx,
                                              String dispatchErrorSequence) {
        SequenceMediator faultSequence = null;
        if (dispatchErrorSequence != null) {
            faultSequence = (SequenceMediator) synCtx.getSequence(dispatchErrorSequence);
        }
        if (faultSequence == null) {
            faultSequence = (SequenceMediator) synCtx.getFaultSequence();
        }
        return faultSequence;
    }

}
