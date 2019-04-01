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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transports.http.bridge.BridgeConstants;

/**
 * {@code ConnectorListenerToAxisBridge} receives the {@code HttpCarbonMessage} coming from the Netty HTTP transport,
 * converts them to {@code MessageContext} and finally deliver them to the axis engine.
 *
 */
public class ConnectorListenerToAxisBridge implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorListenerToAxisBridge.class);

    private ConfigurationContext configurationContext;
    private WorkerPool workerPool;

    public ConnectorListenerToAxisBridge(ConfigurationContext configurationContext, WorkerPool workerPool) {
        this.configurationContext = configurationContext;
        this.workerPool = workerPool;
    }

    public void onMessage(HttpCarbonMessage httpCarbonMessage) {
        LOG.debug(BridgeConstants.BRIDGE_LOG_PREFIX + "Message received to HTTP transport, submitting a worker to the pool to process");
        workerPool.execute(new HttpRequestWorker(httpCarbonMessage, configurationContext));
    }

    public void onError(Throwable throwable) {
    }


}
