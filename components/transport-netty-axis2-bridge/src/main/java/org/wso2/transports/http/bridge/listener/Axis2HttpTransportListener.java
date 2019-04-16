/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.transports.http.bridge.listener;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transports.http.bridge.BridgeConstants;

import java.util.HashMap;

/**
 * {@code Axis2HttpTransportListener} is the Axis2 Transport Listener implementation for HTTP transport.
 *
 */
public class Axis2HttpTransportListener implements TransportListener {

    private static final Logger LOG = LoggerFactory.getLogger(Axis2HttpTransportListener.class);

    @Override
    public void init(ConfigurationContext configurationContext, TransportInDescription transportInDescription) {

        WorkerPool workerPool = WorkerPoolFactory.getWorkerPool(BridgeConstants.DEFAULT_WORKER_POOL_SIZE_CORE,
                BridgeConstants.DEFAULT_WORKER_POOL_SIZE_MAX,
                BridgeConstants.DEFAULT_WORKER_THREAD_KEEPALIVE_SEC,
                BridgeConstants.DEFAULT_WORKER_POOL_QUEUE_LENGTH,
                BridgeConstants.HTTP_WORKER_THREAD_GROUP_NAME,
                BridgeConstants.HTTP_WORKER_THREAD_ID);

        HttpWsConnectorFactory httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        // TODO: Make host and port configurable from axis2.xml
        listenerConfiguration.setPort(8280);
        listenerConfiguration.setHost("localhost");
        ServerConnector serverConnector = httpWsConnectorFactory
                .createServerConnector(new ServerBootstrapConfiguration(new HashMap<>()), listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(
                new ConnectorListenerToAxisBridge(configurationContext, workerPool));
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            LOG.warn(BridgeConstants.BRIDGE_LOG_PREFIX + "Interrupted while waiting for server connector to start", e);
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public EndpointReference getEPRForService(String s, String s1) {
        return null;
    }

    @Override
    public EndpointReference[] getEPRsForService(String s, String s1) {
        return new EndpointReference[0];
    }

    @Override
    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    @Override
    public void destroy() {

    }
}
