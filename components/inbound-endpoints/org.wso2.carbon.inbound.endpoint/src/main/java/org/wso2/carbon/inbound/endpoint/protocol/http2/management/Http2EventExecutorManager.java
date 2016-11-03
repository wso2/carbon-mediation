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

import org.wso2.carbon.inbound.endpoint.protocol.http2.InboundHttp2EventExecutor;

import java.util.concurrent.ConcurrentHashMap;

public class Http2EventExecutorManager {

    private ConcurrentHashMap<Integer, InboundHttp2EventExecutor> executorPoolMap = new ConcurrentHashMap<Integer, InboundHttp2EventExecutor>();

    private static org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EventExecutorManager instance = null;

    public static org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EventExecutorManager getInstance() {
        if (instance == null) {
            instance = new org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EventExecutorManager();
        }
        return instance;
    }

    public void shutdownExecutor(int port) {
        executorPoolMap.get(port).shutdownEventExecutor();
        executorPoolMap.remove(port);
    }

    public void registerEventExecutor(int port, InboundHttp2EventExecutor eventExecutor) {
        executorPoolMap.put(port, eventExecutor);
    }

    public boolean isRegisteredExecutor(int port) {
        return executorPoolMap.containsKey(port);
    }

}
