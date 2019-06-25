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
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisError;
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

import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * {@code Axis2HttpTransportListener} is the Axis2 Transport Listener implementation for HTTP transport.
 */
public class Axis2HttpTransportListener implements TransportListener {

    private static final Logger LOG = LoggerFactory.getLogger(Axis2HttpTransportListener.class);

    private ServerConnector serverConnector;
    private WorkerPool workerPool;
    private ConfigurationContext configurationContext;

    @Override
    public void init(ConfigurationContext configurationContext, TransportInDescription transportInDescription) {

        LOG.info("Initializing Axis2HttpTransportListener");
        this.configurationContext = configurationContext;
        workerPool = WorkerPoolFactory.getWorkerPool(
                BridgeConstants.DEFAULT_WORKER_POOL_SIZE_CORE,
                BridgeConstants.DEFAULT_WORKER_POOL_SIZE_MAX,
                BridgeConstants.DEFAULT_WORKER_THREAD_KEEPALIVE_SEC,
                BridgeConstants.DEFAULT_WORKER_POOL_QUEUE_LENGTH,
                BridgeConstants.HTTP_WORKER_THREAD_GROUP_NAME,
                BridgeConstants.HTTP_WORKER_THREAD_ID);

        int portOffset = Integer.parseInt(System.getProperty("portOffset", "0"));
        Parameter portParam = transportInDescription.getParameter(TransportListener.PARAM_PORT);
        if (portParam == null) {
            throw new AxisError("Port parameter is not specified for Axis2HttpTransportListener");
        }
        int port = Integer.parseInt(portParam.getValue().toString());
        int operatingPort = port + portOffset;

        String host;
        Parameter hostParameter = transportInDescription.getParameter(TransportListener.HOST_ADDRESS);
        if (hostParameter != null) {
            host = ((String) hostParameter.getValue()).trim();
        } else {
            try {
                host = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                LOG.warn("Unable to lookup local host name, using 'localhost'");
                host = "localhost";
            }
        }

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(operatingPort);
        listenerConfiguration.setHost(host);

        HttpWsConnectorFactory httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpWsConnectorFactory
                .createServerConnector(new ServerBootstrapConfiguration(new HashMap<>()), listenerConfiguration);
    }

    @Override
    public void start() {

        LOG.info("Starting Axis2HttpTransportListener");
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(
                new ConnectorListenerToAxisBridge(configurationContext, workerPool));
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
            LOG.warn("{} Interrupted while waiting for server connector to start",
                     BridgeConstants.BRIDGE_LOG_PREFIX, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() {
        LOG.info("Stopping Axis2HttpTransportListener");
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
        LOG.info("Destroying Axis2HttpTransportListener");
    }
}
