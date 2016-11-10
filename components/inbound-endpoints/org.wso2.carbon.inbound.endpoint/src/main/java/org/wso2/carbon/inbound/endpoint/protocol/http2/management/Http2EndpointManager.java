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

package org.wso2.carbon.inbound.endpoint.protocol.http2.management;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.protocol.http2.InboundHttp2Configuration;
import org.wso2.carbon.inbound.endpoint.protocol.http2.InboundHttp2EventExecutor;
import org.wso2.carbon.inbound.endpoint.protocol.http2.InboundHttp2ServerInitializer;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundHttp2Constants;
import org.wso2.carbon.inbound.endpoint.protocol.http2.configuration.NettyThreadPoolConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http2.ssl.SSLHandlerFactory;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.ssl.InboundWebsocketSSLConfiguration;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class Http2EndpointManager extends AbstractInboundEndpointManager {

    private static org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EndpointManager instance = null;

    private static final Logger log = Logger.getLogger(
            org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EndpointManager.class);

    protected Http2EndpointManager() {
        super();
    }

    public static org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EndpointManager getInstance() {
        if (instance == null) {
            instance = new org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EndpointManager();
        }
        return instance;
    }

    public boolean startEndpoint(int port, String name, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();

        String epName = dataStore.getListeningEndpointName(port, tenantDomain);
        if (epName != null) {
            if (epName.equalsIgnoreCase(name)) {
                log.info(epName + " Endpoint is already started in port : " + port);
            } else {
                String msg =
                        "Another endpoint named : " + epName + " is currently using this port: "
                                + port;
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            dataStore.registerListeningEndpoint(port, tenantDomain, InboundHttp2Constants.HTTP2,
                    name, params);
            boolean start = startListener(port, name, params);

            if (!start) {
                dataStore.unregisterListeningEndpoint(port, tenantDomain);
                return false;
            }
        }
        return true;

    }

    public boolean startSSLEndpoint(int port, String name, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
        String epName = dataStore.getListeningEndpointName(port, tenantDomain);

        if (epName != null) {
            if (epName.equalsIgnoreCase(name)) {
                log.info(epName + " Endpoint is already started in port : " + port);
            } else {
                String msg =
                        "Another endpoint named : " + epName + " is currently using this port: "
                                + port;
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            dataStore.registerListeningEndpoint(port, tenantDomain, InboundHttp2Constants.HTTPS2,
                    name, params);
            boolean start = startSSLListener(port, name, params);
            if (start) {
                //do nothing
            } else {
                dataStore.unregisterListeningEndpoint(port, tenantDomain);
                return false;
            }
        }
        return true;
    }

    public boolean startListener(int port, String name, InboundProcessorParams params) {
        if (org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EventExecutorManager
                .getInstance().isRegisteredExecutor(port)) {
            log.info("Netty Listener already started on port " + port);
            return true;
        }

        InboundHttp2Configuration config = buildConfiguration(port, name, params);
        NettyThreadPoolConfiguration threadPoolConfig = new NettyThreadPoolConfiguration(
                config.getBossThreadPoolSize(), config.getWorkerThreadPoolSize());
        InboundHttp2EventExecutor eventExecutor = new InboundHttp2EventExecutor(threadPoolConfig);

        org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EventExecutorManager
                .getInstance().registerEventExecutor(port, eventExecutor);
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, config.getSoBacklog());
        b.group(eventExecutor.getBossGroupThreadPool(), eventExecutor.getWorkerGroupThreadPool())
                .channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new InboundHttp2ServerInitializer(null, config));
        try {

            b.bind(config.getPort()).sync().channel();
            log.info("Http2 Inbound started on Port : " + config.getPort());
            return true;
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean startSSLListener(int port, String name, InboundProcessorParams params) {
        if (org.wso2.carbon.inbound.endpoint.protocol.websocket.management.WebsocketEventExecutorManager
                .getInstance().isRegisteredExecutor(port)) {
            log.info("Netty Listener already started on port " + port);
            return true;
        }

        InboundHttp2Configuration config = buildConfiguration(port, name, params);
        InboundWebsocketSSLConfiguration SslConfig = buildSSLConfiguration(params);
        NettyThreadPoolConfiguration threadPoolConfig = new NettyThreadPoolConfiguration(
                config.getBossThreadPoolSize(), config.getWorkerThreadPoolSize());
        InboundHttp2EventExecutor eventExecutor = new InboundHttp2EventExecutor(threadPoolConfig);
        org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EventExecutorManager
                .getInstance().registerEventExecutor(port, eventExecutor);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(eventExecutor.getBossGroupThreadPool(),
                    eventExecutor.getWorkerGroupThreadPool()).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(
                    new InboundHttp2ServerInitializer(getSSLContext(SslConfig), config));

            b.bind(config.getPort()).sync().channel();

            log.info("Http2-secure Inbound started on Port : " + config.getPort());

        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public void closeEndpoint(int port) {

        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = cc.getTenantDomain();
        dataStore.unregisterListeningEndpoint(port, tenantDomain);

        if (!org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EventExecutorManager
                .getInstance().isRegisteredExecutor(port)) {
            log.info("Listener Endpoint is not started");
            return;
        } else if (dataStore.isEndpointRegistryEmpty(port)) {
            Http2EventExecutorManager.getInstance().shutdownExecutor(port);
        }

    }

    public InboundHttp2Configuration buildConfiguration(int port, String name,
            InboundProcessorParams params) {
        return new InboundHttp2Configuration.InboundHttp2ConfigurationBuilder(port, name, params)
                .build();

    }

    public SslContext getSSLContext(InboundWebsocketSSLConfiguration sslconfig) {
        SslContext sslContext = null;
        SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SSLHandlerFactory handlerFactory = new SSLHandlerFactory(sslconfig);
            sslContext = SslContextBuilder.forServer(handlerFactory.getKeyStoreFactory())
                    .trustManager(handlerFactory.getTrustStoreFactory()).sslProvider(provider)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(
                            new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                                    // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                    // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                    ApplicationProtocolNames.HTTP_2,
                                    ApplicationProtocolNames.HTTP_1_1)).build();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (SSLException e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    public InboundWebsocketSSLConfiguration buildSSLConfiguration(InboundProcessorParams params) {
        return new InboundWebsocketSSLConfiguration.SSLConfigurationBuilder(params.getProperties()
                .getProperty(InboundHttp2Constants.INBOUND_SSL_KEY_STORE_FILE),
                params.getProperties()
                        .getProperty(InboundHttp2Constants.INBOUND_SSL_KEY_STORE_PASS),
                params.getProperties()
                        .getProperty(InboundHttp2Constants.INBOUND_SSL_TRUST_STORE_FILE),
                params.getProperties()
                        .getProperty(InboundHttp2Constants.INBOUND_SSL_TRUST_STORE_PASS),
                params.getProperties().getProperty(InboundHttp2Constants.INBOUND_SSL_CERT_PASS))
                .build();
    }

}
