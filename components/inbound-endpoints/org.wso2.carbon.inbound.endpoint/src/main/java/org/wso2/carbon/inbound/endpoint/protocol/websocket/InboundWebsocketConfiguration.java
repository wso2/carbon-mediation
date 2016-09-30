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

public class InboundWebsocketConfiguration {

    private final int port;
    private final String name;
    private String bossThreadPoolSize;
    private String workerThreadPoolSize;
    private int broadcastLevel;
    private String outFlowDispatchSequence;
    private String outFlowErrorSequence;
    private String subprotocolHandler;
    private String handshakeHandler;
    private String dispatchToCustomSequence;

    private InboundWebsocketConfiguration(InboundWebsocketConfigurationBuilder builder) {
        this.port = builder.port;
        this.name = builder.name;
        this.bossThreadPoolSize = builder.bossThreadPoolSize;
        this.workerThreadPoolSize = builder.workerThreadPoolSize;
        this.broadcastLevel = builder.broadcastLevel;
        this.outFlowDispatchSequence = builder.outFlowDispatchSequence;
        this.outFlowErrorSequence = builder.outFlowErrorSequence;
        this.subprotocolHandler = builder.subprotocolHandler;
        this.handshakeHandler = builder.handshakeHandler;
        this.dispatchToCustomSequence = builder.dispatchToCustomSequence;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return getName();
    }

    public String getBossThreadPoolSize() {
        return bossThreadPoolSize;
    }

    public String getWorkerThreadPoolSize() {
        return workerThreadPoolSize;
    }

    public int getBroadcastLevel() {
        return broadcastLevel;
    }

    public String getOutFlowDispatchSequence() {
        return outFlowDispatchSequence;
    }

    public String getOutFlowErrorSequence() {
        return outFlowErrorSequence;
    }

    public String getSubprotocolHandler() {
        return subprotocolHandler;
    }

    public String getHandshakeHandler() {
        return handshakeHandler;
    }

    public String getDispatchToCustomSequence() {
        return dispatchToCustomSequence;
    }

    public static class InboundWebsocketConfigurationBuilder {
        private final int port;
        private final String name;
        private String bossThreadPoolSize;
        private String workerThreadPoolSize;
        private int broadcastLevel;
        private String outFlowDispatchSequence;
        private String outFlowErrorSequence;
        private String subprotocolHandler;
        private String handshakeHandler;
        private String dispatchToCustomSequence;

        public InboundWebsocketConfigurationBuilder(int port, String name) {
            this.port = port;
            this.name = name;
        }

        public InboundWebsocketConfiguration build() {
            return new InboundWebsocketConfiguration(this);
        }

        public InboundWebsocketConfigurationBuilder bossThreadPoolSize(String bossThreadPoolSize) {
            this.bossThreadPoolSize = bossThreadPoolSize;
            return this;
        }

        public InboundWebsocketConfigurationBuilder workerThreadPoolSize(String workerThreadPoolSize) {
            this.workerThreadPoolSize = workerThreadPoolSize;
            return this;
        }

        public InboundWebsocketConfigurationBuilder broadcastLevel(int broadcastLevel) {
            this.broadcastLevel = broadcastLevel;
            return this;
        }

        public InboundWebsocketConfigurationBuilder outFlowDispatchSequence(String outFlowDispatchSequence) {
            this.outFlowDispatchSequence = outFlowDispatchSequence;
            return this;
        }

        public InboundWebsocketConfigurationBuilder outFlowErrorSequence(String outFlowErrorSequence) {
            this.outFlowErrorSequence = outFlowErrorSequence;
            return this;
        }

        public InboundWebsocketConfigurationBuilder subprotocolHandler(String subprotocolHandler) {
            this.subprotocolHandler = subprotocolHandler;
            return this;
        }

        public InboundWebsocketConfigurationBuilder handshakeHandler(String handshakeHandler) {
            this.handshakeHandler = handshakeHandler;
            return this;
        }

        public InboundWebsocketConfigurationBuilder dispatchToCustomSequence(String dispatchToCustomSequence) {
            this.dispatchToCustomSequence = dispatchToCustomSequence;
            return this;
        }
    }

}
