/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.websocket;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
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
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.inbound.InboundEndpoint;
import org.apache.synapse.inbound.InboundEndpointConstants;
import org.apache.synapse.mediators.MediatorFaultHandler;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.transport.customlogsetter.CustomLogSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelInboundHandlerAdapter;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.inbound.endpoint.osgi.service.ServiceReferenceHolder;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.management.WebsocketEndpointManager;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.management.WebsocketSubscriberPathManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;


public class InboundWebsocketSourceHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(InboundWebsocketSourceHandler.class);

    private InboundWebsocketChannelContext wrappedContext;
    private WebSocketServerHandshaker handshaker;
    private boolean isSSLEnabled;
    private URI subscriberPath;
    private String tenantDomain;
    private int port;
    private boolean dispatchToCustomSequence;
    private InboundWebsocketResponseSender responseSender;
    private static ArrayList<String> contentTypes = new ArrayList<>();
    private static ArrayList<String> otherSubprotocols = new ArrayList<>();
    private int clientBroadcastLevel;
    private String outflowDispatchSequence;
    private String outflowErrorSequence;
    private ChannelPromise handshakeFuture;
    private ArrayList<AbstractSubprotocolHandler> subprotocolHandlers;

    static {
        contentTypes.add("application/xml");
        contentTypes.add("application/json");
        contentTypes.add("text/xml");
    }

    public InboundWebsocketSourceHandler() throws Exception {
    }

    public void setSubprotocolHandlers(ArrayList<AbstractSubprotocolHandler> subprotocolHandlers) {
        this.subprotocolHandlers = subprotocolHandlers;
        for (AbstractSubprotocolHandler handler : subprotocolHandlers) {
            otherSubprotocols.add(handler.getSubprotocolIdentifier());
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        this.isSSLEnabled = ctx.channel().pipeline().get("ssl") != null ? true : false;
        this.wrappedContext = new InboundWebsocketChannelContext(ctx);
        this.port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
        this.responseSender = new InboundWebsocketResponseSender(this);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHandshake(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
    }

    private void handleHandshake(ChannelHandlerContext ctx, FullHttpRequest req) throws URISyntaxException, AxisFault {

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), SubprotocolBuilderUtil.buildSubprotocolString(contentTypes, otherSubprotocols), true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            ChannelFuture future = handshaker.handshake(ctx.channel(), req);
            future.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        handshakeFuture.setSuccess();
                    }
                }
            });
        }

        tenantDomain = MultitenantUtils.getTenantDomainFromUrl(req.getUri());
        if (tenantDomain.equals(req.getUri())) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        String endpointName =
                WebsocketEndpointManager.getInstance().getEndpointName(port, tenantDomain);
        if (endpointName == null) {
            handleException("Endpoint not found for port : " + port + "" +
                    " tenant domain : " + tenantDomain);
        }

        WebsocketSubscriberPathManager.getInstance()
                .addChannelContext(endpointName, subscriberPath.getPath(), wrappedContext);
        MessageContext synCtx = getSynapseMessageContext(tenantDomain);
        InboundEndpoint endpoint = synCtx.getConfiguration().getInboundEndpoint(endpointName);
        if (endpoint == null) {
            log.error("Cannot find deployed inbound endpoint " + endpointName + "for process request");
            return;
        }

        synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_SOURCE_HANDSHAKE_PRESENT, new Boolean(true));
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty(InboundWebsocketConstants.WEBSOCKET_SOURCE_HANDSHAKE_PRESENT, new Boolean(true));
        injectToSequence(synCtx, endpoint);

    }

    private String getWebSocketLocation(FullHttpRequest req) throws URISyntaxException {
        String location = req.headers().get(HOST) + req.getUri();
        subscriberPath = new URI(req.getUri());
        if (isSSLEnabled) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

    private boolean interceptWebsocketMessageFlow(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (handshaker.selectedSubprotocol() == null || subprotocolHandlers == null ||
                (subprotocolHandlers != null && subprotocolHandlers.isEmpty())) {
            return false;
        }
        boolean continueFlow = false;
        for (AbstractSubprotocolHandler handler : subprotocolHandlers) {
            if (handshaker.selectedSubprotocol() != null &&
                    handshaker.selectedSubprotocol().contains(handler.getSubprotocolIdentifier())) {
                continueFlow = handler.handle(ctx, frame, subscriberPath.toString());
                break;
            }
        }
        return !continueFlow;
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        try {

            if (handshakeFuture.isSuccess()) {

                String endpointName =
                        WebsocketEndpointManager.getInstance().getEndpointName(port, tenantDomain);
                MessageContext synCtx = getSynapseMessageContext(tenantDomain);
                InboundEndpoint endpoint = synCtx.getConfiguration().getInboundEndpoint(endpointName);

                if (endpoint == null) {
                    log.error("Cannot find deployed inbound endpoint " + endpointName + "for process request");
                    return;
                }

                if (interceptWebsocketMessageFlow(ctx, frame)) {
                    return;
                }

                if (frame instanceof CloseWebSocketFrame) {
                    handleClientWebsocketChannelTermination(frame);
                    return;
                } else if ((frame instanceof BinaryWebSocketFrame) && ((handshaker.selectedSubprotocol() == null) ||
                        (handshaker.selectedSubprotocol() != null
                                && !handshaker.selectedSubprotocol().contains(InboundWebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX)))) {
                    handleWebsocketBinaryFrame(frame);
                    return;
                } else if ((frame instanceof TextWebSocketFrame) && ((handshaker.selectedSubprotocol() == null) ||
                        (handshaker.selectedSubprotocol() != null
                                && !handshaker.selectedSubprotocol().contains(InboundWebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX)))) {
                    handleWebsocketPassthroughTextFrame(frame);
                    return;
                } else if ((frame instanceof TextWebSocketFrame) && handshaker.selectedSubprotocol() != null
                        && handshaker.selectedSubprotocol().contains(InboundWebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX)) {

                    CustomLogSetter.getInstance().setLogAppender(endpoint.getArtifactContainerName());

                    String message = ((TextWebSocketFrame) frame).text();
                    String contentType = SubprotocolBuilderUtil
                            .syanapeSubprotocolToContentType(SubprotocolBuilderUtil
                                    .extractSynapseSubprotocol(handshaker.selectedSubprotocol()));

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
                    injectToSequence(synCtx, endpoint);
                } else if (frame instanceof PingWebSocketFrame) {
                    ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                    return;
                }

            } else {
                log.error("Handshake incomplete at source handler. Failed to inject websocket frames to Synapse engine");
            }
        } catch (Exception e) {
            log.error("Exception occured while injecting websocket frames to the Synapse engine", e);
        }

    }

    public void handleClientWebsocketChannelTermination(WebSocketFrame frame) throws AxisFault {

        handshaker.close(wrappedContext.getChannelHandlerContext().channel(), (CloseWebSocketFrame) frame.retain());
        String endpointName =
                WebsocketEndpointManager.getInstance().getEndpointName(port, tenantDomain);
        WebsocketSubscriberPathManager.getInstance()
                .removeChannelContext(endpointName, subscriberPath.getPath(), wrappedContext);

    }

    protected void handleWebsocketBinaryFrame(WebSocketFrame frame) throws AxisFault {
        String endpointName =
                WebsocketEndpointManager.getInstance().getEndpointName(port, tenantDomain);

        MessageContext synCtx = getSynapseMessageContext(tenantDomain);
        InboundEndpoint endpoint = synCtx.getConfiguration().getInboundEndpoint(endpointName);

        if (endpoint == null) {
            log.error("Cannot find deployed inbound endpoint " + endpointName + "for process request");
            return;
        }

        synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT, new Boolean(true));
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty(InboundWebsocketConstants.WEBSOCKET_BINARY_FRAME_PRESENT, new Boolean(true));
        synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_BINARY_FRAME, frame);
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty(InboundWebsocketConstants.WEBSOCKET_BINARY_FRAME, frame);
        injectToSequence(synCtx, endpoint);

    }

    protected void handleWebsocketPassthroughTextFrame(WebSocketFrame frame) throws AxisFault {
        String endpointName =
                WebsocketEndpointManager.getInstance().getEndpointName(port, tenantDomain);

        MessageContext synCtx = getSynapseMessageContext(tenantDomain);
        InboundEndpoint endpoint = synCtx.getConfiguration().getInboundEndpoint(endpointName);

        if (endpoint == null) {
            log.error("Cannot find deployed inbound endpoint " + endpointName + "for process request");
            return;
        }

        synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT, new Boolean(true));
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty(InboundWebsocketConstants.WEBSOCKET_TEXT_FRAME_PRESENT, new Boolean(true));
        synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_TEXT_FRAME, frame);
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty(InboundWebsocketConstants.WEBSOCKET_TEXT_FRAME, frame);
        injectToSequence(synCtx, endpoint);

    }

    public InboundWebsocketChannelContext getChannelHandlerContext() {
        return wrappedContext;
    }

    public String getSubscriberPath() {
        return subscriberPath.getPath();
    }

    public int getClientBroadcastLevel() {
        return clientBroadcastLevel;
    }

    public void setOutflowDispatchSequence(String outflowDispatchSequence){
        this.outflowDispatchSequence = outflowDispatchSequence;
    }

    public void setOutflowErrorSequence(String outflowErrorSequence) {
        this.outflowErrorSequence = outflowErrorSequence;
    }

    public void setClientBroadcastLevel(int clientBroadcastLevel) {
        this.clientBroadcastLevel = clientBroadcastLevel;
    }

    protected void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public int getPort() {
        return port;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    private org.apache.synapse.MessageContext getSynapseMessageContext(String tenantDomain) throws AxisFault {
        MessageContext synCtx = createSynapseMessageContext(tenantDomain);
        synCtx.setProperty(SynapseConstants.IS_INBOUND, true);
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().setProperty(SynapseConstants.IS_INBOUND, true);
        synCtx.setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER, responseSender);
        ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .setProperty(InboundEndpointConstants.INBOUND_ENDPOINT_RESPONSE_WORKER, responseSender);
        synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT, wrappedContext.getChannelHandlerContext());
        ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .setProperty(InboundWebsocketConstants.WEBSOCKET_SOURCE_HANDLER_CONTEXT, wrappedContext.getChannelHandlerContext());
        if (outflowDispatchSequence != null) {
            synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE, outflowDispatchSequence);
            ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                    .setProperty(InboundWebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE, outflowDispatchSequence);
        }
        if (outflowErrorSequence != null) {
            synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE, outflowErrorSequence);
            ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                    .setProperty(InboundWebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE, outflowErrorSequence);
        }
        synCtx.setProperty(InboundWebsocketConstants.WEBSOCKET_SUBSCRIBER_PATH, subscriberPath.toString());
        return synCtx;
    }

    private static org.apache.synapse.MessageContext createSynapseMessageContext(String tenantDomain) throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MsgCtx = createAxis2MessageContext();
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MsgCtx.setServiceContext(svcCtx);
        axis2MsgCtx.setOperationContext(opCtx);
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            ConfigurationContext tenantConfigCtx =
                    TenantAxisUtils.getTenantConfigurationContext(tenantDomain,
                            axis2MsgCtx.getConfigurationContext());
            axis2MsgCtx.setConfigurationContext(tenantConfigCtx);
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN, tenantDomain);
        } else {
            axis2MsgCtx.setProperty(MultitenantConstants.TENANT_DOMAIN,
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
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
                                  InboundEndpoint endpoint) {
        SequenceMediator injectingSequence = null;
        if (endpoint.getInjectingSeq() != null) {
            injectingSequence = (SequenceMediator) synCtx.getSequence(endpoint.getInjectingSeq());
        }
        if (injectingSequence == null) {
            injectingSequence = (SequenceMediator) synCtx.getMainSequence();
        }
        SequenceMediator faultSequence = getFaultSequence(synCtx, endpoint);
        MediatorFaultHandler mediatorFaultHandler = new MediatorFaultHandler(faultSequence);
        synCtx.pushFaultHandler(mediatorFaultHandler);
        if (log.isDebugEnabled()) {
            log.debug("injecting message to sequence : " + endpoint.getInjectingSeq());
        }
        synCtx.setProperty("inbound.endpoint.name", endpoint.getName());
        if (dispatchToCustomSequence) {
            String context = (subscriberPath.getPath()).substring(1);
            context = context.replace('/', '-');
            if (synCtx.getConfiguration().getDefinedSequences().containsKey(context))
                injectingSequence = (SequenceMediator) synCtx.getSequence(context);

        }
        synCtx.getEnvironment().injectMessage(synCtx, injectingSequence);
    }

    private SequenceMediator getFaultSequence(org.apache.synapse.MessageContext synCtx,
                                              InboundEndpoint endpoint) {
        SequenceMediator faultSequence = null;
        if (endpoint.getOnErrorSeq() != null) {
            faultSequence = (SequenceMediator) synCtx.getSequence(endpoint.getOnErrorSeq());
        }
        if (faultSequence == null) {
            faultSequence = (SequenceMediator) synCtx.getFaultSequence();
        }
        return faultSequence;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    public void setDispatchToCustomSequence(boolean dispatchToCustomSequence) {
        this.dispatchToCustomSequence = dispatchToCustomSequence;
    }


}
