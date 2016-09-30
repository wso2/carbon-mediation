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

package org.wso2.carbon.inbound.endpoint.protocol.websocket.management;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.HandshakeHandlerBuilderUtil;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.SubprotocolBuilderUtil;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.ssl.InboundWebsocketSSLConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointInfoDTO;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketChannelInitializer;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketConstants;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.InboundWebsocketEventExecutor;
import org.wso2.carbon.inbound.endpoint.protocol.websocket.configuration.NettyThreadPoolConfiguration;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebsocketEndpointManager extends AbstractInboundEndpointManager {

    private static WebsocketEndpointManager instance = null;

    private static final Logger log = Logger.getLogger(WebsocketEndpointManager.class);

    protected WebsocketEndpointManager() {
        super();
    }

    public static WebsocketEndpointManager getInstance() {
        if (instance == null) {
            instance = new WebsocketEndpointManager();
        }
        return instance;
    }

    public boolean startEndpoint(int port, String name, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();

        String epName = dataStore.getListeningEndpointName(port, tenantDomain);
        if (epName != null) {
            if (epName.equalsIgnoreCase(name)) {
                log.info(epName + " Endpoint is already started in port : " + port);
            } else {
                String msg = "Another endpoint named : " + epName + " is currently using this port: " + port;
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            dataStore.registerListeningEndpoint(port, tenantDomain, InboundWebsocketConstants.WS, name, params);
            boolean start = startListener(port, name, params);

            if (start) {
                //do nothing
            } else {
                dataStore.unregisterListeningEndpoint(port, tenantDomain);
                return false;
            }
        }
        return true;

    }

    public boolean startSSLEndpoint(int port, String name, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
        String epName = dataStore.getListeningEndpointName(port, tenantDomain);

        if (epName != null) {
            if (epName.equalsIgnoreCase(name)) {
                log.info(epName + " Endpoint is already started in port : " + port);
            } else {
                String msg = "Another endpoint named : " + epName + " is currently using this port: " + port;
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            dataStore.registerListeningEndpoint(port, tenantDomain, InboundWebsocketConstants.WSS, name,
                    params);
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
        if (WebsocketEventExecutorManager.getInstance().isRegisteredExecutor(port)) {
            log.info("Netty Listener already started on port " + port);
            return true;
        }

        InboundWebsocketConfiguration config = buildConfiguration(port, name, params);
        NettyThreadPoolConfiguration threadPoolConfig =
                new NettyThreadPoolConfiguration(config.getBossThreadPoolSize(),
                        config.getWorkerThreadPoolSize());
        InboundWebsocketEventExecutor eventExecutor = new InboundWebsocketEventExecutor(threadPoolConfig);
        WebsocketEventExecutorManager.getInstance().registerEventExecutor(port, eventExecutor);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(eventExecutor.getBossGroupThreadPool(), eventExecutor.getWorkerGroupThreadPool())
                .channel(NioServerSocketChannel.class);
        InboundWebsocketChannelInitializer handler = new InboundWebsocketChannelInitializer();
        handler.setClientBroadcastLevel(config.getBroadcastLevel());
        handler.setOutflowDispatchSequence(config.getOutFlowDispatchSequence());
        handler.setOutflowErrorSequence(config.getOutFlowErrorSequence());
        handler.setSubprotocolHandlers(SubprotocolBuilderUtil.stringToSubprotocolHandlers(config.getSubprotocolHandler()));
        handler.sethandshakeHandler(HandshakeHandlerBuilderUtil.stringToHandshakeHandlers(config.getHandshakeHandler()));
        handler.setDispatchToCustomSequence(config.getDispatchToCustomSequence());
        bootstrap.childHandler(handler);
        try {
            bootstrap.bind(new InetSocketAddress(port)).sync();
            log.info("Netty Listener starting on port " + port);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public boolean startSSLListener(int port, String name, InboundProcessorParams params) {
        if (WebsocketEventExecutorManager.getInstance().isRegisteredExecutor(port)) {
            log.info("Netty Listener already started on port " + port);
            return true;
        }

        InboundWebsocketConfiguration config = buildConfiguration(port, name, params);
        InboundWebsocketSSLConfiguration sslConfiguration = buildSSLConfiguration(params);
        NettyThreadPoolConfiguration threadPoolConfig =
                new NettyThreadPoolConfiguration(config.getBossThreadPoolSize(),
                        config.getWorkerThreadPoolSize());
        InboundWebsocketEventExecutor eventExecutor = new InboundWebsocketEventExecutor(threadPoolConfig);
        WebsocketEventExecutorManager.getInstance().registerEventExecutor(port, eventExecutor);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(eventExecutor.getBossGroupThreadPool(), eventExecutor.getWorkerGroupThreadPool())
                .channel(NioServerSocketChannel.class);
        InboundWebsocketChannelInitializer handler = new InboundWebsocketChannelInitializer();
        handler.setSslConfiguration(sslConfiguration);
        handler.setClientBroadcastLevel(config.getBroadcastLevel());
        handler.setOutflowDispatchSequence(config.getOutFlowDispatchSequence());
        handler.setOutflowErrorSequence(config.getOutFlowErrorSequence());
        handler.setSubprotocolHandlers(SubprotocolBuilderUtil.stringToSubprotocolHandlers(config.getSubprotocolHandler()));
        handler.sethandshakeHandler(HandshakeHandlerBuilderUtil.stringToHandshakeHandlers(config.getHandshakeHandler()));
        handler.setDispatchToCustomSequence(config.getDispatchToCustomSequence());
        bootstrap.childHandler(handler);
        try {
            bootstrap.bind(new InetSocketAddress(port)).sync();
            log.info("Netty SSL Listener starting on port " + port);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public void closeEndpoint(int port) {

        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = cc.getTenantDomain();
        dataStore.unregisterListeningEndpoint(port, tenantDomain);

        if (!WebsocketEventExecutorManager.getInstance().isRegisteredExecutor(port)) {
            log.info("Listener Endpoint is not started");
            return;
        } else if (dataStore.isEndpointRegistryEmpty(port)) {
            WebsocketEventExecutorManager.getInstance().shutdownExecutor(port);
        }

    }

    public InboundWebsocketConfiguration buildConfiguration(int port, String name, InboundProcessorParams params) {
        return new InboundWebsocketConfiguration.InboundWebsocketConfigurationBuilder(port, name)
                .bossThreadPoolSize(params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_BOSS_THREAD_POOL_SIZE))
                .workerThreadPoolSize(params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_WORKER_THREAD_POOL_SIZE))
                .broadcastLevel(validateBroadcastLevelParam(params.getProperties().getProperty(
                        InboundWebsocketConstants.WEBSOCKET_CLIENT_SIDE_BROADCAST_LEVEL)))
                .outFlowDispatchSequence(params.getProperties().getProperty(
                        InboundWebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_SEQUENCE))
                .outFlowErrorSequence(params.getProperties().getProperty(
                        InboundWebsocketConstants.WEBSOCKET_OUTFLOW_DISPATCH_FAULT_SEQUENCE))
                .subprotocolHandler(params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_SUBPROTOCOL_HANDLER_CLASS))
                .handshakeHandler(params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_HANDSHAKE_HANDLER_CLASS))
                .dispatchToCustomSequence(params.getProperties().getProperty(
                        InboundWebsocketConstants.CUSTOM_SEQUENCE))
                .build();
    }

    protected int validateBroadcastLevelParam(String broadcastLevelParam) {
        int broadcastLevel = 0;
        try {
            if (broadcastLevelParam != null && broadcastLevelParam.trim() != "") {
                broadcastLevel = Integer.parseInt(broadcastLevelParam);
                if (broadcastLevel < 0 || broadcastLevel > 2) {
                    String msg = "Validation failed. Unknown client broadcast level.";
                    log.error(msg);
                    throw new SynapseException(msg);
                }
            }
        } catch (NumberFormatException e) {
            String msg = "Validation failed. Broadcast level parameter should not contain any special characters";
            log.error(msg);
            throw new SynapseException(msg, e);
        }
        return broadcastLevel;
    }

    public InboundWebsocketSSLConfiguration buildSSLConfiguration(InboundProcessorParams params) {
        return new InboundWebsocketSSLConfiguration.SSLConfigurationBuilder(
                params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_SSL_KEY_STORE_FILE),
                params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_SSL_KEY_STORE_PASS),
                params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_SSL_TRUST_STORE_FILE),
                params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_SSL_TRUST_STORE_PASS),
                params.getProperties().getProperty(
                        InboundWebsocketConstants.INBOUND_SSL_CERT_PASS)).build();
    }

    public void loadEndpointListeners() {
        Map<Integer, List<InboundEndpointInfoDTO>> tenantData = dataStore.getAllListeningEndpointData();
        for (Map.Entry tenantInfoEntry : tenantData.entrySet()) {
            int port = (Integer) tenantInfoEntry.getKey();

            InboundEndpointInfoDTO inboundEndpointInfoDTO =
                    (InboundEndpointInfoDTO) ((ArrayList) tenantInfoEntry.getValue()).get(0);

            if (inboundEndpointInfoDTO.getProtocol().equals(InboundWebsocketConstants.WS)) {
                startListener(port, inboundEndpointInfoDTO.getEndpointName(), inboundEndpointInfoDTO.getInboundParams());
            } else if (inboundEndpointInfoDTO.getProtocol().equals(InboundWebsocketConstants.WSS)) {
                startSSLListener(port, inboundEndpointInfoDTO.getEndpointName(), inboundEndpointInfoDTO.getInboundParams());
            }

        }
    }

}
