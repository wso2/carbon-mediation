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
package org.wso2.transports.http.bridge.sender;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transports.http.bridge.BridgeConstants;

/**
 * {@code ResponseListener} listens for the response expected for the sent request.
 */
public class ResponseListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseListener.class);

    private MessageContext requestMsgCtx;
    private WorkerPool workerPool;

    ResponseListener(WorkerPool workerPool, MessageContext requestMsgContext) {
        this.workerPool = workerPool;
        this.requestMsgCtx = requestMsgContext;
    }

    @Override
    public void onMessage(HttpCarbonMessage httpResponse) {
        LOG.debug("{} Response received", BridgeConstants.BRIDGE_LOG_PREFIX);
        workerPool.execute(new HttpResponseWorker(requestMsgCtx, httpResponse));
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("{} Error while processing the response", BridgeConstants.BRIDGE_LOG_PREFIX, throwable);
    }

}
