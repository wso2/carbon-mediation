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

package org.wso2.carbon.http2.transport.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;

import static io.netty.handler.logging.LogLevel.DEBUG;

/**
 * Initializing a connection with the back-end server
 */
public class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {
	private static final Http2FrameLogger logger =
			new Http2FrameLogger(DEBUG,  //change mode to INFO for logging frames
			                     Http2ClientInitializer.class);

	private final SslContext sslCtx;
	private final int maxContentLength;
	private Http2ClientHandler responseHandler;
	private Http2ConnectionHandler connectionHandler;
	private Http2SettingsHandler settingsHandler;
	private Http2FrameListener listener;

	public Http2ClientInitializer(SslContext sslCtx, int maxContentLength) {
		this.sslCtx = sslCtx;
		this.maxContentLength = maxContentLength;
	}

	/**
	 * Initiate Http2 connection as clearText or TCP secured
	 *
	 * @param ch
	 * @throws Exception
	 */
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		final Http2Connection connection = new DefaultHttp2Connection(false);
		Http2FrameListenAdapter clientFrameListener = new Http2FrameListenAdapter();

		listener = new DelegatingDecompressorFrameListener(connection, clientFrameListener);
		connectionHandler =
				new Http2ConnectionHandlerBuilder().connection(connection).frameLogger(logger)
				                                   .frameListener(listener).build();
		responseHandler = new Http2ClientHandler(connection);
		responseHandler.setEncoder(connectionHandler.encoder());
		settingsHandler = new Http2SettingsHandler(ch.newPromise(), responseHandler);
		if (sslCtx != null) {
			configureSsl(ch);
		} else {
			configureClearText(ch);
		}
	}

	public Http2ClientHandler responseHandler() {
		return responseHandler;
	}

	public Http2SettingsHandler settingsHandler() {
		return settingsHandler;
	}

	private void configureEndOfPipeline(ChannelPipeline pipeline) {
		pipeline.addLast(settingsHandler, responseHandler);
	}

	private void configureSsl(SocketChannel ch) {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
			@Override
			protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
				if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
					ChannelPipeline p = ctx.pipeline();
					p.addLast(connectionHandler);
					configureEndOfPipeline(p);
					return;
				}
				ctx.close();
				throw new IllegalStateException("unknown protocol: " + protocol);
			}
		});
	}

	private void configureClearText(SocketChannel ch) {
		HttpClientCodec sourceCodec = new HttpClientCodec();
		Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler);
		HttpClientUpgradeHandler upgradeHandler =
				new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, 65536);

		ch.pipeline()
		  .addLast(sourceCodec, upgradeHandler, new UpgradeRequestHandler(), new UserEventLogger());
	}

	private static class UserEventLogger extends ChannelInboundHandlerAdapter {
		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			ctx.fireUserEventTriggered(evt);
		}
	}

	private final class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			DefaultFullHttpRequest upgradeRequest =
					new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
			ctx.writeAndFlush(upgradeRequest);
			ctx.fireChannelActive();
			ctx.pipeline().remove(this);
			configureEndOfPipeline(ctx.pipeline());
		}
	}
}
