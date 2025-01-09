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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.inbound.endpoint.internal.http.api.Constants;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.ssl.InboundWebsocketSSLConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.ssl.SSLHandlerFactory;

import java.util.ArrayList;

public class InboundWebsocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger log = LoggerFactory.getLogger(InboundWebsocketChannelInitializer.class);

    private InboundWebsocketSSLConfiguration sslConfiguration;
    private int clientBroadcastLevel;
    private String outflowDispatchSequence;
    private String outflowErrorSequence;
    private ChannelHandler pipelineHandler;
    private boolean dispatchToCustomSequence;
    private ArrayList<AbstractSubprotocolHandler> subprotocolHandlers;
    private int portOffset;
    private int inflowIdleTime;
    private int outflowIdleTime;
    private boolean passThroughControlFrames;

    public InboundWebsocketChannelInitializer() {
    }

    public void setSslConfiguration(InboundWebsocketSSLConfiguration sslConfiguration) {
        this.sslConfiguration = sslConfiguration;
    }

    public void setPipelineHandler(ChannelHandler name) {
        this.pipelineHandler = name;
    }

    public void setDispatchToCustomSequence(String dispatchToCustomSequence) {
        this.dispatchToCustomSequence = Boolean.parseBoolean(dispatchToCustomSequence);
    }

    public void setClientBroadcastLevel(int clientBroadcastLevel) {
        this.clientBroadcastLevel = clientBroadcastLevel;
    }

    public void setOutflowDispatchSequence(String outflowDispatchSequence) {
        this.outflowDispatchSequence = outflowDispatchSequence;
    }

    public void setOutflowErrorSequence(String outflowErrorSequence) {
        this.outflowErrorSequence = outflowErrorSequence;
    }

    public void setSubprotocolHandlers(ArrayList<AbstractSubprotocolHandler> subprotocolHandlers) {
        this.subprotocolHandlers = subprotocolHandlers;
    }

    public void setInflowIdleTime(int inflowIdleTime) {
        this.inflowIdleTime = inflowIdleTime;
    }

    public void setOutflowIdleTime(int outflowIdleTime) {
        this.outflowIdleTime = outflowIdleTime;
    }

    public void setPassThroughControlFrames(boolean passThroughControlFrames) {
        this.passThroughControlFrames = passThroughControlFrames;
    }

    @Override
    protected void initChannel(SocketChannel websocketChannel) throws Exception {

        if (sslConfiguration != null) {
            SslHandler sslHandler = new SSLHandlerFactory(sslConfiguration).create();
            websocketChannel.pipeline().addLast("ssl", sslHandler);
        }

        ChannelPipeline p = websocketChannel.pipeline();
        int maxInitLength = Integer.parseInt(SynapsePropertiesLoader.getPropertyValue(
                Constants.WEBSOCKET_TRANSPORT_MAX_HTTP_CODEC_INIT_LENGTH, "4096"));
        int maxHeaderSize = Integer.parseInt(SynapsePropertiesLoader.getPropertyValue(
                Constants.WEBSOCKET_TRANSPORT_MAX_HTTP_CODEC_HEADER_SIZE, "8192"));
        int maxChunkSize = Integer.parseInt(SynapsePropertiesLoader.getPropertyValue(
                Constants.WEBSOCKET_TRANSPORT_MAX_HTTP_CODEC_CHUNK_SIZE, "8192"));
        int maxContentLength = Integer.parseInt(SynapsePropertiesLoader.getPropertyValue(
                Constants.WEBSOCKET_TRANSPORT_MAX_HTTP_AGGREGATOR_CONTENT_LENGTH, "65536"));
        p.addLast(new WebSocketAccessLoggingHandler(LogLevel.DEBUG));
        p.addLast("codec", new HttpServerCodec(maxInitLength, maxHeaderSize, maxChunkSize));
        p.addLast("aggregator", new HttpObjectAggregator(maxContentLength));
        p.addLast("frameAggregator", new WebSocketFrameAggregator(Integer.MAX_VALUE));
        p.addLast("idleStateHandler", new IdleStateHandler(inflowIdleTime, outflowIdleTime, 0));
        InboundWebsocketSourceHandler sourceHandler = new InboundWebsocketSourceHandler();
        sourceHandler.setClientBroadcastLevel(clientBroadcastLevel);
        sourceHandler.setDispatchToCustomSequence(dispatchToCustomSequence);
        sourceHandler.setPortOffset(portOffset);
        sourceHandler.setPassThroughControlFrames(passThroughControlFrames);
        if (outflowDispatchSequence != null)
            sourceHandler.setOutflowDispatchSequence(outflowDispatchSequence);
        if (outflowErrorSequence != null)
            sourceHandler.setOutflowErrorSequence(outflowErrorSequence);
        if (subprotocolHandlers != null)
            sourceHandler.setSubprotocolHandlers(subprotocolHandlers);
        if (pipelineHandler != null)
            p.addLast("pipelineHandler", pipelineHandler.getClass().getConstructor().newInstance());
        p.addLast("handler", sourceHandler);
    }

    public void setPortOffset(int portOffset) {
        this.portOffset = portOffset;
    }
}