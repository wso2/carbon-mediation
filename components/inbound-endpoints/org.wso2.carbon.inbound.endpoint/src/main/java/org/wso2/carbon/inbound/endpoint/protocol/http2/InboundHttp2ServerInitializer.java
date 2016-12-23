/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AsciiString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static io.netty.handler.logging.LogLevel.DEBUG;
import static io.netty.handler.logging.LogLevel.INFO;

/**
 * Initializing connection with a peer
 */
public class InboundHttp2ServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final Log log = LogFactory.getLog(InboundHttp2ServerInitializer.class);
    private static final Http2FrameLogger logger = new Http2FrameLogger(DEBUG, //Change mode into INFO to log frames
            InboundHttp2ServerInitializer.class);
    private final SslContext sslCtx;
    private final InboundHttp2Configuration config;
    private UpgradeCodecFactory upgradeCodecFactory;

    public InboundHttp2ServerInitializer(SslContext sslCtx, InboundHttp2Configuration config) {
        this(sslCtx, 16 * 1024, config);
    }

    public InboundHttp2ServerInitializer(SslContext sslCtx, int maxHttpContentLength,
            final InboundHttp2Configuration config) {
        if (maxHttpContentLength < 0) {
            throw new IllegalArgumentException(
                    "maxHttpContentLength (expected >= 0): " + maxHttpContentLength);
        }
        this.config = config;
        this.sslCtx = sslCtx;
    }

    /**
     * Channel initialization
     * @param ch
     */
    @Override
    public void initChannel(SocketChannel ch) {
        Http2Connection conn = new DefaultHttp2Connection(true);
        Http2FrameListenAdapter listenAdapter = new Http2FrameListenAdapter();
        Http2ConnectionHandler connectionHandler = new Http2ConnectionHandlerBuilder()
                .connection(conn).frameLogger(logger).frameListener(listenAdapter).build();
        InboundHttp2SourceHandler sourceHandler = new InboundHttp2SourceHandler(this.config, conn,
                connectionHandler.encoder());
        if (sslCtx != null) {
            configureSsl(ch, connectionHandler, sourceHandler);
        } else {
            configureClearText(ch, connectionHandler, sourceHandler);
        }
    }

    /**
     * start channel for HTTP/2 over TLS (https)
     * @param ch
     * @param connectionHandler
     * @param channelHanlder
     */
    private void configureSsl(SocketChannel ch, Http2ConnectionHandler connectionHandler,
            InboundHttp2SourceHandler channelHanlder) {
        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), connectionHandler, channelHanlder);
    }

    /**
     * start channel for HTTP/2 Cleartext
     * @param ch
     * @param connectionHandler
     * @param channelHandler
     */
    private void configureClearText(SocketChannel ch,
            final Http2ConnectionHandler connectionHandler,
            final InboundHttp2SourceHandler channelHandler) {
        final ChannelPipeline p = ch.pipeline();
        final HttpServerCodec sourceCodec = new HttpServerCodec();
        upgradeCodecFactory = new UpgradeCodecFactory() {

            public UpgradeCodec newUpgradeCodec(CharSequence protocol) {
                if (AsciiString
                        .contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                    return new Http2ServerUpgradeCodec(null, connectionHandler);
                } else {
                    return null;
                }
            }
        };

        p.addLast(sourceCodec);
        p.addLast(new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory));
        p.addLast(channelHandler);
        p.addLast(new UserEventLogger());
    }

    private static class UserEventLogger extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            ctx.fireUserEventTriggered(evt);
        }
    }

}
