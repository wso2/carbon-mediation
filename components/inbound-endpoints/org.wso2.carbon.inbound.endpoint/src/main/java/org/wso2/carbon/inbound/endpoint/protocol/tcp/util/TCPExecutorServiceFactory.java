/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.util;

import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.TCPConfiguration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Decoded messages will inject to the synapse and the reply from the synapse is getting via this.
 * Number of threads in the thread pool is 100, which specified in the tcp.properties file.
 * ESB-HOME/repository/conf/tcp.properties
 */

public class TCPExecutorServiceFactory {

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
            TCPConfiguration.getInstance().getIntProperty(InboundTCPConstants.TCPConstants.WORKER_THREADS_CORE,
                                                          InboundTCPConstants.TCPConstants.WORKER_THREADS_CORE_DEFAULT),
            TCPWorkerThreadFactory.getInstance());

    public static ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    private static class TCPWorkerThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        private static TCPWorkerThreadFactory instance = new TCPWorkerThreadFactory();

        private TCPWorkerThreadFactory() {
            group = new ThreadGroup("TCP-inbound-thread-group");
            namePrefix = "TCP-inbound-worker-";
        }

        public static TCPWorkerThreadFactory getInstance() {
            return instance;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}