/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.websocket.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketConstants;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketSourceHandler;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.management.WebsocketEndpointManager;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.websocket.transport.service.ServiceReferenceHolder;

import java.net.URI;
import java.util.HashMap;

/**
 * Test class for WebSocketClientHandler
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceReferenceHolder.class,
        MessageContextCreatorForAxis2.class, WebSocketClientHandler.class, WebsocketEndpointManager.class })
public class WebSocketClientHandlerTest {

    private static final Log log = LogFactory.getLog(WebSocketClientHandlerTest.class);
    private ChannelHandlerContext channelHandlerContext;
    private WebSocketClientHandler webSocketClientHandler;
    private WebSocketClientHandshaker webSocketClientHandshaker;
    private MessageContext synCtx;
    private HttpHeaders httpHeaders;
    private Channel channel;

    @Before
    public void setup() throws Exception {
        channelHandlerContext = Mockito.mock(ChannelHandlerContext.class);
        channel = Mockito.mock(Channel.class);
        Mockito.when(channelHandlerContext.channel()).thenReturn(channel);
        httpHeaders = Mockito.mock(HttpHeaders.class);
        synCtx = createSynapaseMessageContext();
        WebsocketConnectionFactory webSocketClientHandshakerFactory;
        webSocketClientHandshaker = WebSocketClientHandshakerFactory.newHandshaker(new URI("ws://localhost:8000"),
                WebSocketVersion.V13, null, false, httpHeaders);
        WebsocketEndpointManager websocketEndpointManager = Mockito.mock(WebsocketEndpointManager.class);
        PowerMockito.mockStatic(WebsocketEndpointManager.class);
        Mockito.when(WebsocketEndpointManager.getInstance()).thenReturn(websocketEndpointManager);
        InboundWebsocketSourceHandler inboundWebsocketSourceHandler = Mockito.mock(InboundWebsocketSourceHandler.class);
        inboundWebsocketSourceHandler.setPassThroughControlFrames(false);
        Mockito.when(WebsocketEndpointManager.getInstance().getSourceHandler()).thenReturn(
                inboundWebsocketSourceHandler);
        webSocketClientHandler = new WebSocketClientHandler(webSocketClientHandshaker);
        webSocketClientHandler.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        ConfigurationContextService cfgCtxService = Mockito.mock(ConfigurationContextService.class);
        Mockito.when(serviceReferenceHolder.getConfigurationContextService()).thenReturn(cfgCtxService);
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        Mockito.when(cfgCtxService.getServerConfigContext()).thenReturn(configurationContext);
        org.apache.axis2.context.MessageContext messageContext = new org.apache.axis2.context.MessageContext();
        // This is to eliminate the null pointer during setProperty in createAxis2MessageContext()
        messageContext.setProperty("test", "value");
        PowerMockito.whenNew(org.apache.axis2.context.MessageContext.class).withNoArguments()
                .thenReturn(messageContext);
        PowerMockito.mockStatic(MessageContextCreatorForAxis2.class);
        Mockito.when(MessageContextCreatorForAxis2.getSynapseMessageContext(Mockito.any())).thenReturn(synCtx);
        SequenceMediator mediator = Mockito.mock(SequenceMediator.class);
    }

    @Test
    public void exceptionCaughtTest() throws AxisFault {
        Throwable cause = new CorruptedWebSocketFrameException(WebSocketCloseStatus.MESSAGE_TOO_BIG,
                "Max frame length of 65536 has been exceeded.");
        webSocketClientHandler.handlerAdded(channelHandlerContext);
        ChannelPromise channelPromise = Mockito.mock(ChannelPromise.class);
        channelPromise.setSuccess();
        Mockito.when(channelHandlerContext.newPromise()).thenReturn(channelPromise);
        // Setting the handshakeFuture as success before calling exceptionCaught
        webSocketClientHandler.handlerAdded(channelHandlerContext);

        webSocketClientHandler.exceptionCaught(channelHandlerContext, cause);
        Assert.assertEquals(synCtx.getProperty(InboundWebsocketConstants.WEB_SOCKET_CLOSE_CODE),
                WebSocketCloseStatus.MESSAGE_TOO_BIG.code());
        Assert.assertEquals(synCtx.getProperty(InboundWebsocketConstants.WEB_SOCKET_REASON_TEXT),
                WebSocketCloseStatus.MESSAGE_TOO_BIG.reasonText());
    }

    private MessageContext createSynapaseMessageContext() {
        Axis2SynapseEnvironment synapseEnvironment = new Axis2SynapseEnvironment(new SynapseConfiguration());
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        MessageContext mc = new Axis2MessageContext(axis2MC, new SynapseConfiguration(), synapseEnvironment);
        SequenceMediator mediator = Mockito.mock(SequenceMediator.class);
        HashMap contextEntries = new HashMap<>();
        contextEntries.put("fault", mediator);
        mc.setContextEntries(contextEntries);
        return mc;
    }
}
