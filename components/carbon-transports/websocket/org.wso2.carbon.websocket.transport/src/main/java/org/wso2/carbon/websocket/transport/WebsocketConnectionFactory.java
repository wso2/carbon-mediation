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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.websocket.transport.utils.SSLUtil;

import javax.net.ssl.SSLException;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketConnectionFactory {

    private static final Log log = LogFactory.getLog(WebsocketConnectionFactory.class);
    private static WebsocketConnectionFactory instance = null;

    private final TransportOutDescription transportOut;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketClientHandler>>
            channelHandlerPool = new ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketClientHandler>>();

    public WebsocketConnectionFactory(TransportOutDescription transportOut) {
        this.transportOut = transportOut;
    }

    public static WebsocketConnectionFactory getInstance(TransportOutDescription transportOut) {
        if (instance == null) {
            instance = new WebsocketConnectionFactory(transportOut);
        }
        return instance;
    }

    public WebSocketClientHandler getChannelHandler(final URI uri,
                                                    final String sourceIdentifier,
                                                    final boolean handshakePresent,
                                                    final String dispatchSequence,
                                                    final String dispatchErrorSequence,
                                                    final String contentType) throws InterruptedException {
        WebSocketClientHandler channelHandler;
        if (handshakePresent) {
            channelHandler = cacheNewConnection(uri, sourceIdentifier, dispatchSequence, dispatchErrorSequence, contentType);
        } else {
            channelHandler = getChannelHandlerFromPool(sourceIdentifier, getClientHandlerIdentifier(uri));
            if (channelHandler == null) {
                channelHandler = cacheNewConnection(uri, sourceIdentifier, dispatchSequence, dispatchErrorSequence, contentType);
            }
        }
        channelHandler.handshakeFuture().sync();
        return channelHandler;
    }

    public String getClientHandlerIdentifier(final URI uri) {
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port = uri.getPort();
        final String subscriberPath = uri.getPath();
        return host.concat(String.valueOf(port)).concat(subscriberPath);
    }

    public WebSocketClientHandler cacheNewConnection(final URI uri,
                                                     final String sourceIdentifier,
                                                     String dispatchSequence,
                                                     String dispatchErrorSequence,
                                                     String contentType) {
        if (log.isDebugEnabled()) {
            log.debug("Creating a Connection for the specified WS endpoint.");
        }
        final WebSocketClientHandler handler;

        try {

            String scheme = uri.getScheme() == null ? WebsocketConstants.WS : uri.getScheme();
            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
            final int port = uri.getPort();
            if (!WebsocketConstants.WS.equalsIgnoreCase(scheme) && !WebsocketConstants.WSS.equalsIgnoreCase(scheme)) {
                return null;
            }
            final boolean ssl = WebsocketConstants.WSS.equalsIgnoreCase(scheme);
            final SslContext sslCtx;
            if (ssl) {
                Parameter trustParam = transportOut.getParameter(WebsocketConstants.TRUST_STORE_CONFIG_ELEMENT);
                OMElement tsEle = null;
                if (trustParam != null) {
                    tsEle = trustParam.getParameterElement().getFirstElement();
                }
                final String location =
                        tsEle.getFirstChildWithName(new QName(WebsocketConstants.TRUST_STORE_LOCATION))
                                .getText();
                final String storePassword =
                        tsEle.getFirstChildWithName(new QName(WebsocketConstants.TRUST_STORE_PASSWORD))
                                .getText();
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(SSLUtil.createTrustmanager(location,
                                storePassword))
                        .build();
            } else {
                sslCtx = null;
            }

            if (sourceIdentifier.equals(WebsocketConstants.UNIVERSAL_SOURCE_IDENTIFIER)) {
                Parameter dispatchParam = transportOut.getParameter(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE);
                if (dispatchParam != null) {
                    dispatchSequence = dispatchParam.getParameterElement().getText();
                }
                Parameter errorParam = transportOut.getParameter(WebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE);
                if (errorParam != null) {
                    dispatchErrorSequence = errorParam.getParameterElement().getText();
                }

            }

            final EventLoopGroup group = new NioEventLoopGroup();
            handler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri,
                    WebSocketVersion.V13,
                    contentType != null ? SubprotocolBuilderUtil.contentTypeToSyanapeSubprotocol(contentType) : null,
                    false,
                    new DefaultHttpHeaders()));
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            }
                            p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192),
                                    new WebSocketFrameAggregator(Integer.MAX_VALUE), handler);
                        }
                    });

            Channel ch = b.connect(uri.getHost(), port).sync().channel();
            ch.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    group.shutdownGracefully();
                    removeChannelHandler(sourceIdentifier, getClientHandlerIdentifier(uri));
                }
            });
            handler.setDispatchSequence(dispatchSequence);
            handler.setDispatchErrorSequence(dispatchErrorSequence);
            addChannelHandler(sourceIdentifier, getClientHandlerIdentifier(uri), handler);
            return handler;

        } catch (InterruptedException e) {
            log.error("Interruption occured while connecting to the remote WS endpoint", e);
        } catch (SSLException e) {
            log.error("Error occurred while building the SSL context fo WSS endpoint", e);
        }

        return null;
    }

    public void addChannelHandler(String sourceIdentifier,
                                  String clientIdentifier,
                                  WebSocketClientHandler clientHandler) {
        ConcurrentHashMap<String, WebSocketClientHandler> handlerMap =
                channelHandlerPool.get(sourceIdentifier);
        if (handlerMap == null) {
            handlerMap = new ConcurrentHashMap<String, WebSocketClientHandler>();
            handlerMap.put(clientIdentifier, clientHandler);
            channelHandlerPool.put(sourceIdentifier, handlerMap);
        } else {
            handlerMap.put(clientIdentifier, clientHandler);
        }
    }

    public WebSocketClientHandler getChannelHandlerFromPool(String sourceIdentifier,
                                                            String clientIdentifier) {
        ConcurrentHashMap<String, WebSocketClientHandler> handlerMap =
                channelHandlerPool.get(sourceIdentifier);
        if (handlerMap == null) {
            return null;
        } else {
            return handlerMap.get(clientIdentifier);
        }
    }

    public void removeChannelHandler(String sourceIdentifier,
                                     String clientIdentifier) {
        ConcurrentHashMap<String, WebSocketClientHandler> handlerMap =
                channelHandlerPool.get(sourceIdentifier);
        handlerMap.remove(clientIdentifier);
    }


}