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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.http.HttpHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.SSLException;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class Http2ConnectionFactory {

    private static Http2ConnectionFactory factory;
    private static TreeMap<ChannelId,Map<String, Http2ClientHandler>> clientConnections;
   // private static TreeMap<String, Http2ClientHandler> connections;
    private Log log = LogFactory.getLog(Http2ConnectionFactory.class);
    private TransportOutDescription trasportOut;

    private Http2ConnectionFactory(TransportOutDescription transportOut) {
        this.trasportOut = transportOut;
        clientConnections = new TreeMap<>();
    }

    public static Http2ConnectionFactory getInstance(TransportOutDescription transportOut) {
        if (factory == null) {
            factory = new Http2ConnectionFactory(transportOut);
        }
        return factory;
    }

    public Http2ClientHandler getChannelHandler(HttpHost uri,ChannelId channelId) {

        Http2ClientHandler handler;
        Map conns=null;
        conns=clientConnections.get(channelId);
        if(conns==null){
            conns= new TreeMap<String, Http2ClientHandler>();
            clientConnections.put(channelId,conns);
        }
        handler = getClientHandlerFromPool(uri,conns);
        if (handler == null) {
            handler = cacheNewConnection(uri,conns);
            if (log.isDebugEnabled()) {
                if (handler != null) {
                    log.debug("New connection created for " + uri.toString());
                } else
                    log.debug("New connection establishment failed for " + uri.toString());
            }

        } else {
            if (log.isDebugEnabled()) {
                log.info("Get connection from pool");
            }
        }
        return handler;
    }

    public Http2ClientHandler cacheNewConnection(HttpHost uri,Map<String, Http2ClientHandler> map) {

        final SslContext sslCtx;
        final boolean SSL;
        if (uri.getSchemeName().equalsIgnoreCase("https")) {
            SSL = true;
        } else
            SSL = false;
        try {
            // Handling SSL
            if (SSL) {
                Parameter trustParam = trasportOut
                        .getParameter(Http2Constants.TRUST_STORE_CONFIG_ELEMENT);
                OMElement tsEle = null;
                if (trustParam != null) {
                    tsEle = trustParam.getParameterElement();
                }
                final String location = tsEle
                        .getFirstChildWithName(new QName(Http2Constants.TRUST_STORE_LOCATION))
                        .getText();
                final String storePassword = tsEle
                        .getFirstChildWithName(new QName(Http2Constants.TRUST_STORE_PASSWORD))
                        .getText();

                SslProvider provider = OpenSsl.isAlpnSupported() ?
                        SslProvider.OPENSSL :
                        SslProvider.JDK;
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(SSLUtil.createTrustmanager(location, storePassword))
                        .sslProvider(provider)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN,
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1))
                        .build();
            } else {
                sslCtx = null;
            }

            EventLoopGroup workerGroup = new NioEventLoopGroup();
            Http2ClientInitializer initializer = new Http2ClientInitializer(sslCtx,
                    Integer.MAX_VALUE);

            String HOST = uri.getHostName();
            Integer PORT = uri.getPort();
            // Configure the client.
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(HOST, PORT);
            b.handler(initializer);
            // Start the client.
            Channel channel = b.connect().syncUninterruptibly().channel();
            log.debug("Connected to [" + HOST + ':' + PORT + ']');
            // Wait for the HTTP/2 upgrade to occur.
            Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
            http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);

            String key = generateKey(URI.create(uri.toURI()));
            Http2ClientHandler handler = initializer.responseHandler();
            handler.setChannel(channel);
            map.put(key, handler);
            

            return initializer.responseHandler();
        } catch (SSLException e) {
            log.error("Error while connection establishment:" + e.fillInStackTrace());
            return null;
        } catch (Exception e) {
            log.error("Error while connection establishment:" + e.fillInStackTrace());
            return null;
        }
    }

    public Http2ClientHandler getClientHandlerFromPool(HttpHost uri,Map<String, Http2ClientHandler> map) {
        String key = generateKey(URI.create(uri.toURI()));
        Http2ClientHandler handler = map.get(key);
        if (handler != null) {
            Channel c = handler.getChannel();
            if (!c.isActive()) {
                map.remove(key);
                handler = cacheNewConnection(uri,map);
            } else if (handler.isStreamIdOverflow()) {
                c.close().syncUninterruptibly();
                map.remove(key);
                handler = cacheNewConnection(uri,map);
            }
        }
        return handler;
    }

    public String generateKey(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort();
        String ssl;
        if (uri.getScheme().equalsIgnoreCase(Http2Constants.HTTPS2) || uri.getScheme()
                .equalsIgnoreCase("https")) {
            ssl = "https://";
        } else
            ssl = "http://";
        return ssl + host + ":" + port;
    }
}
