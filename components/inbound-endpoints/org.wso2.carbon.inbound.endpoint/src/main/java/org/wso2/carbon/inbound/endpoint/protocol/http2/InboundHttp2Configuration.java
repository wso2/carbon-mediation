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

import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundHttp2Constants;

import java.util.Properties;

public class InboundHttp2Configuration {
    private final int port;
    private final String name;
    private final String dispatchPattern;
    private final Properties properties;
    private String bossThreadPoolSize;
    private String workerThreadPoolSize;
    private String dispatchSequence;
    private String errorSequence;
    private boolean enableServerPush;

    private InboundHttp2Configuration(
            InboundHttp2Configuration.InboundHttp2ConfigurationBuilder builder) {
        this.port = builder.port;
        this.name = builder.name;
        this.properties = builder.properties;
        this.dispatchPattern = builder.dispatchPattern;
        this.workerThreadPoolSize = builder.workerThreadPoolSize;
        this.bossThreadPoolSize = builder.bossThreadPoolSize;
        this.dispatchSequence = builder.dispatchSequence;
        this.errorSequence = builder.errorSequence;
        this.enableServerPush = builder.enableServerPush;

    }

    public Properties getProperties() {
        return properties;
    }

    public String getBossThreadPoolSize() {
        return bossThreadPoolSize;
    }

    public String getWorkerThreadPoolSize() {
        return workerThreadPoolSize;
    }

    public String getDispatchSequence() {
        return dispatchSequence;
    }

    public String getErrorSequence() {
        return errorSequence;
    }

    public int getPort() {
        return port;
    }

    public boolean isEnableServerPush() {
        return enableServerPush;
    }

    public String getName() {
        return name;
    }

    public String getDispatchPattern() {
        return dispatchPattern;
    }

    public static class InboundHttp2ConfigurationBuilder {
        private final int port;
        private final String name;
        private String dispatchPattern;
        private Properties properties;
        private String bossThreadPoolSize;
        private String workerThreadPoolSize;
        private String dispatchSequence;
        private String errorSequence;
        private boolean enableServerPush;

        public InboundHttp2ConfigurationBuilder(int port, String name,
                InboundProcessorParams params) {
            properties = params.getProperties();

            this.name = ((name != null) || (name != "")) ? name : params.getName();
            this.port = (port > 0) ?
                    port :
                    Integer.parseInt(properties.getProperty(InboundHttp2Constants.INBOUND_PORT));

            if (properties.getProperty(
                    InboundHttp2Constants.INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN)
                    != null) {
                this.dispatchPattern = properties.getProperty(
                        InboundHttp2Constants.INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN);
            } else {
                this.dispatchPattern = null;
            }

            if (properties.getProperty(InboundHttp2Constants.INBOUND_BOSS_THREAD_POOL_SIZE)
                    != null) {
                this.bossThreadPoolSize = properties
                        .getProperty(InboundHttp2Constants.INBOUND_BOSS_THREAD_POOL_SIZE);
            } else {
                this.bossThreadPoolSize = "1";
            }
            if (properties.getProperty(InboundHttp2Constants.INBOUND_WORKER_THREAD_POOL_SIZE)
                    != null) {
                this.workerThreadPoolSize = properties
                        .getProperty(InboundHttp2Constants.INBOUND_WORKER_THREAD_POOL_SIZE);
            } else {
                this.workerThreadPoolSize = "1";
            }
            if (properties.getProperty(InboundHttp2Constants.INBOUND_DISPATCH_SEQUENCE) != null) {
                this.dispatchSequence = properties
                        .getProperty(InboundHttp2Constants.INBOUND_DISPATCH_SEQUENCE);
            } else {
                this.dispatchSequence = null;
            }
            if (properties.getProperty(InboundHttp2Constants.INBOUND_ERROR_SEQUENCE) != null) {
                this.errorSequence = properties
                        .getProperty(InboundHttp2Constants.INBOUND_ERROR_SEQUENCE);
            } else {
                this.errorSequence = null;
            }
            if (properties.getProperty(InboundHttp2Constants.INBOUND_SERVER_PUSH_ENABLED) != null) {
                this.enableServerPush = Boolean.parseBoolean(
                        properties.getProperty(InboundHttp2Constants.INBOUND_SERVER_PUSH_ENABLED));
            } else {
                this.enableServerPush = Boolean.parseBoolean("false");
            }
        }

        public InboundHttp2Configuration build() {
            return new InboundHttp2Configuration(this);
        }

    }

}
