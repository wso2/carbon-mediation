/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.http.netty.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundHttpConstants;
import org.wso2.carbon.inbound.endpoint.protocol.http.utils.InboundThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * keeps inbound requests for processing
 */
public class InboundRequestQueue {

    private static final Log log = LogFactory.getLog(InboundRequestQueue.class);

    private BlockingQueue<InboundSourceRequest> eventQueue;
    private ExecutorService executorService;

    public InboundRequestQueue() {
        executorService = Executors.newFixedThreadPool(InboundHttpConstants.WORKER_POOL_SIZE, new InboundThreadFactory("request"));
        eventQueue = new ArrayBlockingQueue<InboundSourceRequest>(InboundHttpConstants.REQUEST_BUFFER_CAPACITY);
    }

    public void publish(InboundSourceRequest inboundSourceRequest) {
        try {
            eventQueue.put(inboundSourceRequest);
        } catch (InterruptedException e) {
            String logMessage = "Failure to insert request into queue";
            log.warn(logMessage);
        }
        executorService.submit(new InboundSourceRequestWorker(eventQueue));
    }

    @Override
    protected void finalize() throws Throwable {
        executorService.shutdown();
        super.finalize();
    }

}
