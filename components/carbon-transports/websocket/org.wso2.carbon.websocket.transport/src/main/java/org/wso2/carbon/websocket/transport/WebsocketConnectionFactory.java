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
import io.netty.channel.ChannelOption;
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
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.inbound.InboundResponseSender;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.websocket.transport.utils.SSLUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.xml.namespace.QName;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketConnectionFactory {

    private static final Log log = LogFactory.getLog(WebsocketConnectionFactory.class);

    private static final QName Q_PROFILE        = new QName("profile");
    private static final QName Q_TARGET_HOSTS   = new QName("targetHosts");
    private static final QName Q_PROXY_HOST_EL  = new QName("proxyHost");
    private static final QName Q_PROXY_PORT_EL  = new QName("proxyPort");
    private static final QName Q_PROXY_USER     = new QName("proxyUserName");
    private static final QName Q_PROXY_PASS     = new QName("proxyPassword");
    private static final QName Q_BYPASS         = new QName("bypass");

    private static class WsProxyProfileConfig {
        final String proxyHost;
        final int proxyPort;
        final String proxyUsername;
        final String proxyPassword;
        final Set<String> bypass;

        WsProxyProfileConfig(String proxyHost, int proxyPort,
                             String proxyUsername, String proxyPassword,
                             Set<String> bypass) {
            this.proxyHost    = proxyHost;
            this.proxyPort    = proxyPort;
            this.proxyUsername = proxyUsername;
            this.proxyPassword = proxyPassword;
            this.bypass       = bypass;
        }
    }

    private final EventLoopGroup sharedEventLoopGroup;
    private final TransportOutDescription transportOut;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketClientHandler>>
            channelHandlerPool = new ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketClientHandler>>();

    // proxyProfiles path — populated from ws.proxyProfiles parameter
    private Map<String, WsProxyProfileConfig> proxyProfileMap = new HashMap<>();
    private final Set<String> knownDirectHosts =
            Collections.synchronizedSet(new HashSet<>());
    private final Map<String, WsProxyProfileConfig> knownProxyConfigMap =
            new ConcurrentHashMap<>();

    // flat proxy path — only used when proxyProfileMap is empty
    private String proxyHost;
    private int proxyPort = -1;
    private String proxyUsername;
    private String proxyPassword;

    public WebsocketConnectionFactory(TransportOutDescription transportOut) throws AxisFault {
        this.transportOut = transportOut;
        int sharedEventLoopPoolSize = getMaxValueOrDefault(
                transportOut.getParameter(WebsocketConstants.WEBSOCKET_SHARED_EVENT_LOOP_POOL_SIZE), -1);
        if (sharedEventLoopPoolSize > 0) {
            this.sharedEventLoopGroup = new NioEventLoopGroup(sharedEventLoopPoolSize);
        } else {
            this.sharedEventLoopGroup = new NioEventLoopGroup();
        }
        boolean sslEnabled = WebsocketConstants.WSS.equalsIgnoreCase(transportOut.getName());
        if (sslEnabled) {
            Parameter trustParam = transportOut.getParameter(WebsocketConstants.TRUST_STORE_CONFIG_ELEMENT);
            if (trustParam != null) {
                OMElement trustStoreLocationElem = trustParam.getParameterElement().
                        getFirstChildWithName(new QName(WebsocketConstants.TRUST_STORE_LOCATION));
                OMElement trustStorePasswordElem = trustParam.getParameterElement().
                        getFirstChildWithName(new QName(WebsocketConstants.
                                TRUST_STORE_PASSWORD));

                if (trustStoreLocationElem == null || trustStorePasswordElem == null) {
                    handleWssTrustStoreParameterError("Unable to read parameter(s) "
                            + WebsocketConstants.TRUST_STORE_LOCATION + " and/or "
                            + WebsocketConstants.TRUST_STORE_PASSWORD + " from Transport configurations");
                }
            } else {
                handleWssTrustStoreParameterError("Unable to read parameter(s) "
                        + WebsocketConstants.TRUST_STORE_LOCATION + " and/or "
                        + WebsocketConstants.TRUST_STORE_PASSWORD + " from Transport configurations");
            }
        }

        loadProxyConfig(transportOut);
    }

    private void loadProxyConfig(TransportOutDescription transportOut) {
        Parameter profilesParam = transportOut.getParameter(WebsocketConstants.PROXY_PROFILES);
        if (profilesParam != null) {
            OMElement profilesElt = profilesParam.getParameterElement();
            Iterator<?> profiles = profilesElt.getChildrenWithName(Q_PROFILE);
            while (profiles.hasNext()) {
                OMElement profile = (OMElement) profiles.next();
                OMElement targetHostsElt = profile.getFirstChildWithName(Q_TARGET_HOSTS);
                if (targetHostsElt == null || targetHostsElt.getText().trim().isEmpty()) {
                    log.warn("Skipping ws proxy profile: missing or empty <targetHosts>");
                    continue;
                }
                OMElement proxyHostElt = profile.getFirstChildWithName(Q_PROXY_HOST_EL);
                OMElement proxyPortElt = profile.getFirstChildWithName(Q_PROXY_PORT_EL);
                if (proxyHostElt == null || proxyPortElt == null) {
                    log.warn("Skipping ws proxy profile for [" + targetHostsElt.getText()
                            + "]: missing <proxyHost> or <proxyPort>");
                    continue;
                }
                String pHost = proxyHostElt.getText().trim();
                int pPort = Integer.parseInt(proxyPortElt.getText().trim());
                String pUser = null;
                String pPass = null;
                OMElement userElt = profile.getFirstChildWithName(Q_PROXY_USER);
                OMElement passElt = profile.getFirstChildWithName(Q_PROXY_PASS);
                if (userElt != null) {
                    pUser = userElt.getText().trim();
                    pPass = passElt != null ? passElt.getText().trim() : "";
                }
                Set<String> bypassSet = new HashSet<>();
                OMElement bypassElt = profile.getFirstChildWithName(Q_BYPASS);
                if (bypassElt != null && !bypassElt.getText().trim().isEmpty()) {
                    for (String b : bypassElt.getText().split(",")) {
                        bypassSet.add(b.trim());
                    }
                }
                WsProxyProfileConfig config =
                        new WsProxyProfileConfig(pHost, pPort, pUser, pPass, bypassSet);
                for (String target : targetHostsElt.getText().split(",")) {
                    target = target.trim();
                    if (!proxyProfileMap.containsKey(target)) {
                        proxyProfileMap.put(target, config);
                    } else {
                        log.warn("Duplicate ws proxy profile for targetHost [" + target
                                + "] — ignoring");
                    }
                }
            }
            if (!proxyProfileMap.isEmpty()) {
                log.info(transportOut.getName() + " ws proxy profiles loaded for "
                        + proxyProfileMap.size() + " targetHost(s)");
                return;
            }
        }

        // No profiles — fall back to flat params
        Parameter proxyHostParam = transportOut.getParameter(WebsocketConstants.PROXY_HOST);
        if (proxyHostParam != null) {
            this.proxyHost = proxyHostParam.getParameterElement().getText();
            Parameter proxyPortParam = transportOut.getParameter(WebsocketConstants.PROXY_PORT);
            if (proxyPortParam != null) {
                this.proxyPort = Integer.parseInt(proxyPortParam.getParameterElement().getText());
            }
            Parameter proxyUsernameParam = transportOut.getParameter(WebsocketConstants.PROXY_USERNAME);
            if (proxyUsernameParam != null) {
                this.proxyUsername = proxyUsernameParam.getParameterElement().getText();
            }
            Parameter proxyPasswordParam = transportOut.getParameter(WebsocketConstants.PROXY_PASSWORD);
            if (proxyPasswordParam != null) {
                this.proxyPassword = proxyPasswordParam.getParameterElement().getText();
            }
            if (log.isDebugEnabled()) {
                log.debug(transportOut.getName()
                        + " ws flat proxy configured: " + this.proxyHost + ":" + this.proxyPort);
            }
        }
    }

    public WebSocketClientHandler getChannelHandler(final String tenantDomain,
                                                    final URI uri,
                                                    final String sourceIdentifier,
                                                    final boolean handshakePresent,
                                                    final String dispatchSequence,
                                                    final String dispatchErrorSequence,
                                                    final String contentType,
                                                    final String wsSubprotocol,
                                                    final boolean isConnectionTerminate,
                                                    final Map<String, Object> headers,
                                                    final InboundResponseSender inboundResponseSender,
                                                    final String responseDispatchSequence,
                                                    final String responseErrorSequence,
                                                    final String correlationId,
                                                    final Map<String, Object> apiProperties,
                                                    final SecretResolver resolver) throws InterruptedException {
        WebSocketClientHandler channelHandler = getChannelHandlerFromPool(sourceIdentifier,
                                                                          getClientHandlerIdentifier(uri));
        if (channelHandler == null) {
            synchronized (sourceIdentifier.intern()) {
                channelHandler = getChannelHandlerFromPool(sourceIdentifier, getClientHandlerIdentifier(uri));
                if (channelHandler == null) {
                    if (isConnectionTerminate) {
                        return null;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(correlationId + " -- Caching new connection with sourceIdentifier " + sourceIdentifier
                                + " in the Thread," + "ID: " + Thread.currentThread().getName() + "," + Thread
                                .currentThread().getId() + " API context: " + apiProperties
                                .get(WebsocketConstants.API_CONTEXT));
                    }
                    channelHandler = cacheNewConnection(tenantDomain, uri, sourceIdentifier, dispatchSequence, dispatchErrorSequence,
                                                        contentType, wsSubprotocol, headers, inboundResponseSender,
                                                        responseDispatchSequence, responseErrorSequence,
                                                        correlationId, apiProperties, resolver);
                }
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

    public WebSocketClientHandler cacheNewConnection(final String tenantDomain,
                                                     final URI uri,
                                                     final String sourceIdentifier,
                                                     String dispatchSequence,
                                                     String dispatchErrorSequence,
                                                     String contentType,
                                                     String wsSubprotocol,
                                                     Map<String, Object> headers,
                                                     InboundResponseSender inboundResponseSender,
                                                     String responseDispatchSequence,
                                                     String responseErrorSequence,
                                                     String correlationId,
                                                     Map<String, Object> apiProperties,
                                                     SecretResolver resolver) {
        if (log.isDebugEnabled()) {
            log.debug(correlationId + " -- Creating a Connection for the specified WS endpoint." + " API context: "
                    + apiProperties.get(WebsocketConstants.API_CONTEXT));
        }
        final WebSocketClientHandler handler;

        try {

            String scheme = uri.getScheme() == null ? WebsocketConstants.WS : uri.getScheme();
            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
            final int port;
            switch (scheme) {
                case WebsocketConstants.WS:
                    if (uri.getPort() < 0) {
                        // Setting default port for WS connection if port not defined in URI
                        port = WebsocketConstants.WEBSOCKET_DEFAULT_WS_PORT;
                        break;
                    }
                    port = uri.getPort();
                    break;
                case WebsocketConstants.WSS:
                    if (uri.getPort() < 0) {
                        // Setting default port for WSS connection if port not defined in URI
                        port = WebsocketConstants.WEBSOCKET_DEFAULT_WSS_PORT;
                        break;
                    }
                    port = uri.getPort();
                    break;
                default:
                    // If scheme does not belong to either ws or wss schemes, we return null
                    return null;
            }
            final boolean ssl = WebsocketConstants.WSS.equalsIgnoreCase(scheme);
            final SslContext sslCtx;
            if (ssl) {
                Parameter trustParam = transportOut.getParameter(WebsocketConstants.TRUST_STORE_CONFIG_ELEMENT);
                if (trustParam != null) {
                    OMElement trustStoreLocationElem = trustParam.getParameterElement().
                            getFirstChildWithName(new QName(WebsocketConstants.TRUST_STORE_LOCATION));
                    OMElement trustStorePasswordElem = trustParam.getParameterElement().
                            getFirstChildWithName(new QName(WebsocketConstants.
                                    TRUST_STORE_PASSWORD));

                    final String location = trustStoreLocationElem.getText();
                    String storePassword = trustStorePasswordElem.getText();
                    if (resolver != null) {
                        storePassword = MiscellaneousUtil.resolve(storePassword, resolver);
                    } else {
                        storePassword = MiscellaneousUtil.resolve(storePassword,
                                SecretResolverFactory.create(trustParam.getParameterElement(), false));
                    }
                    sslCtx = SslContextBuilder.forClient()
                            .trustManager(SSLUtil.createTrustmanager(location,
                                    storePassword))
                            .build();
                } else {
                    sslCtx = null;
                }
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

            DefaultHttpHeaders defaultHttpHeaders = new DefaultHttpHeaders();

            // If there are any custom headers, add them to the defaultHttpHeader.
            if (headers.size() > 0) {
                for (Map.Entry<String, Object> header : headers.entrySet()) {
                    defaultHttpHeaders.add(header.getKey(), header.getValue());
                }
            }

            // Initialize handler before Bootstrap configuration
            Parameter maxPayloadParam = transportOut
                    .getParameter(WebsocketConstants.WEBSOCKET_MAX_FRAME_PAYLOAD_LENGTH);
            if (maxPayloadParam != null) {
                int maxLength = Integer.parseInt(maxPayloadParam.getParameterElement().getText());
                handler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri,
                        WebSocketVersion.V13,
                        deriveSubprotocol(wsSubprotocol, contentType),
                        false, defaultHttpHeaders, maxLength, true, false, -1L, false, false));
            } else {
                handler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri,
                        WebSocketVersion.V13,
                        deriveSubprotocol(wsSubprotocol, contentType),
                        false, defaultHttpHeaders, 65536, true, false, -1L, false, false));
            }
            handler.setCorrelationId(correlationId);
            handler.setApiProperties(apiProperties);
            if (tenantDomain != null) {
                handler.setTenantDomain(tenantDomain);
            } else {
                handler.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            }

            if (!(WebsocketConstants.UNIVERSAL_SOURCE_IDENTIFIER).equals(sourceIdentifier)) {
                handler.registerWebsocketResponseSender(inboundResponseSender);
                handler.setDispatchSequence(responseDispatchSequence);
                handler.setDispatchErrorSequence(responseErrorSequence);
            }
            int maxInitLength = getMaxValueOrDefault(
                    transportOut.getParameter(WebsocketConstants.WEBSOCKET_MAX_HTTP_CODEC_INIT_LENGTH), 4096);
            int maxHeaderSize = getMaxValueOrDefault(
                    transportOut.getParameter(WebsocketConstants.WEBSOCKET_MAX_HTTP_CODEC_HEADER_SIZE), 8192);
            int maxChunkSize = getMaxValueOrDefault(
                    transportOut.getParameter(WebsocketConstants.WEBSOCKET_MAX_HTTP_CODEC_CHUNK_SIZE), 8192);
            int maxContentLength = getMaxValueOrDefault(
                    transportOut.getParameter(WebsocketConstants.WEBSOCKET_MAX_HTTP_AGGREGATOR_CONTENT_LENGTH), 8192);
            Bootstrap b = new Bootstrap();
            b.group(sharedEventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            if (log.isDebugEnabled()) {
                                log.debug(correlationId + " -- Using shared NioEventLoopGroup for connection - "
                                        + "Group: " + sharedEventLoopGroup.toString()
                                        + ", Source: " + sourceIdentifier
                                        + ", URI: " + uri
                                        + ", Thread: " + Thread.currentThread().getName()
                                        + ", ThreadID: " + Thread.currentThread().getId());
                            }
                            ChannelPipeline p = ch.pipeline();
                            HttpProxyHandler proxyHandler = resolveProxyHandler(host);
                            if (proxyHandler != null) {
                                p.addLast(proxyHandler);
                            }
                            if (sslCtx != null) {
                                SslHandler sslHandler = sslCtx.newHandler(ch.alloc(), host, port);
                                Parameter wsEnableHostnameVerification = transportOut
                                        .getParameter(WebsocketConstants.WEBSOCKET_HOSTNAME_VERIFICATION_CONFIG);
                                if (wsEnableHostnameVerification != null
                                        && wsEnableHostnameVerification.getValue() != null
                                        && !wsEnableHostnameVerification.getValue().toString().isEmpty()
                                        && Boolean.parseBoolean(wsEnableHostnameVerification.getValue().toString())) {
                                    SSLEngine sslEngine = sslHandler.engine();
                                    SSLParameters sslParams = sslEngine.getSSLParameters();
                                    sslParams.setEndpointIdentificationAlgorithm("HTTPS");
                                    sslEngine.setSSLParameters(sslParams);
                                }
                                p.addLast(sslHandler);                            }
                            p.addLast(new HttpClientCodec(maxInitLength, maxHeaderSize, maxChunkSize),
                                    new HttpObjectAggregator(maxContentLength),
                                    new WebSocketFrameAggregator(Integer.MAX_VALUE), handler);
                        }
                    });

            Channel ch = b.connect(uri.getHost(), port).sync().channel();
            ch.config().setOption(ChannelOption.ALLOW_HALF_CLOSURE, true);
            ch.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (log.isDebugEnabled()) {
                        log.debug("OperationComplete ChannelFuture triggered on sourceIdentifier: " + sourceIdentifier
                                          + ", clientIdentifier: " + getClientHandlerIdentifier(uri)
                                          + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                                          + Thread.currentThread().getId());
                    }
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

    private HttpProxyHandler resolveProxyHandler(String targetHost) {
        if (!proxyProfileMap.isEmpty()) {
            WsProxyProfileConfig profile = getProfileForHost(targetHost);
            if (profile != null) {
                return buildProxyHandler(profile.proxyHost, profile.proxyPort,
                        profile.proxyUsername, profile.proxyPassword);
            }
            return null;
        }
        if (proxyHost != null && proxyPort > 0) {
            return buildProxyHandler(proxyHost, proxyPort, proxyUsername, proxyPassword);
        }
        return null;
    }

    private WsProxyProfileConfig getProfileForHost(String targetHost) {
        if (knownProxyConfigMap.containsKey(targetHost)) {
            return knownProxyConfigMap.get(targetHost);
        }
        if (knownDirectHosts.contains(targetHost)) {
            return null;
        }
        boolean defaultProfile = false;
        for (String key : proxyProfileMap.keySet()) {
            if ("*".equals(key)) {
                defaultProfile = true;
                continue;
            }
            if (targetHost.matches(key)) {
                return resolveWithBypass(targetHost, key);
            }
        }
        if (defaultProfile) {
            return resolveWithBypass(targetHost, "*");
        }
        return null;
    }

    private WsProxyProfileConfig resolveWithBypass(String targetHost, String key) {
        WsProxyProfileConfig profile = proxyProfileMap.get(key);
        for (String bypass : profile.bypass) {
            if (targetHost.matches(bypass)) {
                knownDirectHosts.add(targetHost);
                if (log.isDebugEnabled()) {
                    log.debug("ws proxy bypass matched: host=" + targetHost
                            + " bypass=" + bypass);
                }
                return null;
            }
        }
        knownProxyConfigMap.put(targetHost, profile);
        return profile;
    }

    private HttpProxyHandler buildProxyHandler(String host, int port,
                                               String username, String password) {
        InetSocketAddress proxyAddress = new InetSocketAddress(host, port);
        if (username != null && !username.isEmpty()) {
            return new HttpProxyHandler(proxyAddress, username, password);
        }
        return new HttpProxyHandler(proxyAddress);
    }

    private String deriveSubprotocol(String wsSubprotocol, String contentType) {
        if (wsSubprotocol != null) {
            return wsSubprotocol;
        }
        return contentType != null ? SubprotocolBuilderUtil.contentTypeToSyanapeSubprotocol(contentType) : null;
    }

    private static void handleWssTrustStoreParameterError(String errorMsg) throws AxisFault {
        log.error(errorMsg);
        throw new AxisFault(errorMsg);
    }

    public void addChannelHandler(String sourceIdentifier,
                                  String clientIdentifier,
                                  WebSocketClientHandler clientHandler) {
        if (log.isDebugEnabled()) {
            log.debug("Adding channel for on sourceIdentifier: " + sourceIdentifier + ", clientIdentifier: "
                              + clientIdentifier + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                              + Thread.currentThread().getId());
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Fetching channel for on sourceIdentifier: " + sourceIdentifier + ", clientIdentifier: "
                              + clientIdentifier + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                              + Thread.currentThread().getId());
        }
        ConcurrentHashMap<String, WebSocketClientHandler> handlerMap =
                channelHandlerPool.get(sourceIdentifier);
        if (handlerMap == null) {
            return null;
        } else {
            if (log.isDebugEnabled()) {
                if (handlerMap.get(clientIdentifier) != null) {
                    log.debug(handlerMap.get(clientIdentifier).getCorrelationId()
                            + " -- Fetched channel for on sourceIdentifier: " + sourceIdentifier
                            + ", clientIdentifier: " + clientIdentifier + ", in the Thread,ID: " + Thread
                            .currentThread().getName() + "," + Thread.currentThread().getId() + "API Context"
                            + handlerMap.get(clientIdentifier).getApiProperties().get(WebsocketConstants.API_CONTEXT));
                }
            }
            return handlerMap.get(clientIdentifier);
        }
    }

    public void removeChannelHandler(String sourceIdentifier,
                                     String clientIdentifier) {
        if (log.isDebugEnabled()) {
            log.debug("Removing channel for on sourceIdentifier: " + sourceIdentifier + ", clientIdentifier: "
                              + clientIdentifier + ", in the Thread,ID: " + Thread.currentThread().getName() + ","
                              + Thread.currentThread().getId());
        }
        ConcurrentHashMap<String, WebSocketClientHandler> handlerMap =
                channelHandlerPool.get(sourceIdentifier);
        handlerMap.remove(clientIdentifier);
        if (handlerMap.isEmpty()) {
            handlerMap.clear();
            channelHandlerPool.remove(sourceIdentifier);
        }
    }

    private int getMaxValueOrDefault(Parameter parameter, int defaultValue) {

        if (parameter != null) {
            return Integer.parseInt(parameter.getParameterElement().getText());
        }
        return defaultValue;
    }

    public void shutdown() {
        if (sharedEventLoopGroup != null) {
            sharedEventLoopGroup.shutdownGracefully();
        }
    }
}
