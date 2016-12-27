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
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;

import javax.net.ssl.SSLException;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Creating new Connections to backend, storing initiated clientHandlers
 */
public class Http2ConnectionFactory {

	private static Http2ConnectionFactory factory;
	private static ConcurrentHashMap<String, Http2ClientHandler> clientConnections;
	private Log log = LogFactory.getLog(Http2ConnectionFactory.class);
	private TransportOutDescription trasportOut;
	private EventLoopGroup workerGroup;

	private Http2ConnectionFactory(TransportOutDescription transportOut) {
		this.trasportOut = transportOut;
		clientConnections = new ConcurrentHashMap<>();
		this.workerGroup = new NioEventLoopGroup();
	}

	public static Http2ConnectionFactory getInstance(TransportOutDescription transportOut) {
		if (factory == null) {
			factory = new Http2ConnectionFactory(transportOut);
		}
		return factory;
	}

	/**
	 * return client-handler for the given URI
	 *
	 * @param uri
	 * @return
	 * @throws AxisFault
	 */
	public synchronized Http2ClientHandler getChannelHandler(HttpHost uri) throws AxisFault {
		Http2ClientHandler handler = null;
		String key = generateKey(URI.create(uri.toURI()));
		handler = getClientHandlerFromPool(key);

		if (handler == null) {
			handler = cacheNewConnection(uri);
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

	/**
	 * Create new connection and return client handler
	 *
	 * @param uri
	 * @return Http2ClientHandler
	 * @throws AxisFault
	 */
	private Http2ClientHandler cacheNewConnection(HttpHost uri) throws AxisFault {

		final SslContext sslCtx;
		final boolean SSL;
		if (uri.getSchemeName().equalsIgnoreCase("https")) {
			SSL = true;
		} else
			SSL = false;
		try {
			// Handling SSL
			if (SSL) {
				Parameter trustParam =
						trasportOut.getParameter(Http2Constants.TRUST_STORE_CONFIG_ELEMENT);
				OMElement tsEle = null;
				if (trustParam != null) {
					tsEle = trustParam.getParameterElement();
				}
				final String location =
						tsEle.getFirstChildWithName(new QName(Http2Constants.TRUST_STORE_LOCATION))
						     .getText();
				final String storePassword =
						tsEle.getFirstChildWithName(new QName(Http2Constants.TRUST_STORE_PASSWORD))
						     .getText();

				SslProvider provider =
						OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
				sslCtx = SslContextBuilder.forClient().trustManager(
						SSLUtil.createTrustmanager(location, storePassword)).sslProvider(provider).ciphers(Http2SecurityUtil.CIPHERS,
				                                   SupportedCipherSuiteFilter.INSTANCE)
				                          .trustManager(InsecureTrustManagerFactory.INSTANCE)
				                          .applicationProtocolConfig(new ApplicationProtocolConfig(
						                          ApplicationProtocolConfig.Protocol.ALPN,
						                          ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
						                          ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
						                          ApplicationProtocolNames.HTTP_2,
						                          ApplicationProtocolNames.HTTP_1_1)).build();
			} else {
				sslCtx = null;
			}
			Http2ClientInitializer initializer =
					new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE);

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

			Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
			http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);

			final String key = generateKey(URI.create(uri.toURI()));
			Http2ClientHandler handler = initializer.responseHandler();

			clientConnections.put(key, handler);

			channel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					clientConnections.remove(key);
				}
			});
			return initializer.responseHandler();
		} catch (SSLException e) {
			throw new AxisFault("Error while connection establishment:", e);
		} catch (Exception e) {
			throw new AxisFault("Error while connection establishment:" + e);
		}
	}

	/**
	 * Fetch client handler from connection pool
	 *
	 * @param key
	 * @return Http2ClientHandler
	 */
	private Http2ClientHandler getClientHandlerFromPool(String key) {
		Http2ClientHandler handler;
		if (clientConnections.containsKey(key))
			handler = clientConnections.get(key);
		else
			handler = null;
		if (handler != null) {
			Channel c = handler.getChContext().channel();
			boolean canMakeNewStreams = handler.getConnection().local().canOpenStream();
			if (!c.isActive() || !canMakeNewStreams) {
				clientConnections.remove(key);
				handler = null;
			}
		}
		return handler;
	}

	/**
	 * Generate hash-key for each clientHandler using uri
	 *
	 * @param uri
	 * @return
	 */
	private String generateKey(URI uri) {
		String host = uri.getHost();
		int port = uri.getPort();
		String ssl;
		if (uri.getScheme().equalsIgnoreCase(Http2Constants.HTTPS2) ||
		    uri.getScheme().equalsIgnoreCase("https")) {
			ssl = "https://";
		} else
			ssl = "http://";
		return ssl + host + ":" + port;
	}

}
