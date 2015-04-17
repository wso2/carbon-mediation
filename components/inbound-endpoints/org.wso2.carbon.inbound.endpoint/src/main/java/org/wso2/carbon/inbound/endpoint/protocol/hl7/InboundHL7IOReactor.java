package org.wso2.carbon.inbound.endpoint.protocol.hl7;

/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.synapse.inbound.InboundProcessorParams;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InboundHL7IOReactor {

    private static final Log log = LogFactory.getLog(InboundHL7IOReactor.class);

    private static ListeningIOReactor reactor;

    private static ConcurrentHashMap<Integer, ListenerEndpoint> endpointMap = new ConcurrentHashMap<Integer, ListenerEndpoint>();

    private static volatile boolean isStarted = false;

    private static ConcurrentHashMap<Integer, Map<String, Object>>
            parameterMap = new ConcurrentHashMap<Integer, Map<String, Object>>();

    public static void start() throws IOException {

        if (reactor != null && reactor.getStatus().equals(IOReactorStatus.ACTIVE)) {
            return;
        }

        IOReactorConfig config = getDefaultReactorConfig();

        reactor = new DefaultListeningIOReactor(config);

        Thread reactorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isStarted = true;
                    log.info("MLLP Transport IO Reactor Started");
                    reactor.execute(new MultiIOHandler(parameterMap));
                } catch (IOException e) {
                    isStarted = false;
                    log.error("Error while starting the MLLP Transport IO Reactor. " + e.getMessage());
                }
            }
        });

        reactorThread.start();
    }

    public static void stop() {
        log.info("LOG 1: stop() : isStarted = " + isStarted);

        try {
            reactor.shutdown();
            endpointMap.clear();
            isStarted = false;
        } catch (IOException e) {
            log.error("Error while shutting down MLLP Transport IO Reactor. " + e.getMessage());
        }
    }

    public static void pause() {
        try {
            reactor.pause();
        } catch (IOException e) {
            log.error("Error while pausing MLLP Transport IO Reactor. " + e.getMessage());
        }
    }

    public static boolean isStarted() {
        return isStarted;
    }

    public static boolean bind(int port, Map<String, Object> params) {
        ListenerEndpoint ep = reactor.listen(getSocketAddress(port));

        try {
            ep.waitFor();
            endpointMap.put(port, ep);
            parameterMap.put(port, params);
            return true;
        } catch (InterruptedException e) {
            log.error("Error while starting a new MLLP Listener on port " + port + ". " + e.getMessage());
            return false;
        }
    }

    public static boolean unbind(int port) {
        ListenerEndpoint ep = endpointMap.get(port);

        endpointMap.remove(port);
        parameterMap.remove(port);

        if (ep == null) {
            return false;
        }

        ep.close();

        return true;
    }

    private static SocketAddress getSocketAddress(int port) {
        InetSocketAddress isa = new InetSocketAddress(port);
        return isa;
    }


    private static IOReactorConfig getDefaultReactorConfig() {
        IOReactorConfig.Builder builder = IOReactorConfig.custom();
        return builder.setSelectInterval(1000)
                .setShutdownGracePeriod(500)
                .setInterestOpQueued(false)
                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .setSoTimeout(0)
                .setSoReuseAddress(true)
                .setSoLinger(-1)
                .setSoKeepAlive(true)
                .setTcpNoDelay(true)
                .setConnectTimeout(0).build();
                //.setSndBufSize(0)
                //.setRcvBufSize(0)
    }

}