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

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.nio.reactor.ListeningIOReactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is responsible for monitor all IO events regarding to TCP Inbound endpoints
 */

public class InboundTCPIOReactor {

    private static final Log log = LogFactory.getLog(InboundTCPIOReactor.class);

    private static volatile boolean isStarted = false;

    private static ListeningIOReactor reactor;

    private static ConcurrentHashMap<Integer, ListenerEndpoint> endpointMap =
            new ConcurrentHashMap<Integer, ListenerEndpoint>();

    private static ConcurrentHashMap<Integer, TCPProcessor> processorMap =
            new ConcurrentHashMap<Integer, TCPProcessor>();

    public static boolean isStarted() {
        return isStarted;
    }

    public static void start() throws IOReactorException {
        if (reactor != null && reactor.getStatus().equals(IOReactorStatus.ACTIVE)) {
            return;
        }

        IOReactorConfig config = getDefaultReactorConfig();

        reactor = new DefaultListeningIOReactor(config);

        Thread reactorThread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    isStarted = true;
                    log.info("TCP Inbound Transport IO Reactor Started");
                    reactor.execute(new TCPMultiIOHandler(processorMap));
                } catch (IOException e) {
                    isStarted = false;
                    log.error("Error while starting the TCP Inbound Transport IO Reactor.", e);
                }
            }
        });

        reactorThread.start();
    }

    public static void stop() {
        try {
            reactor.shutdown();
            endpointMap.clear();
            isStarted = false;
        } catch (IOException e) {
            log.error("Error while shutting down TCP Inbound Transport IO Reactor. ", e);
        }
    }

    public static void pause() {
        try {
            reactor.pause();
        } catch (IOException e) {
            log.error("Error while pausing TCP Inbound Transport IO Reactor. ", e);
        }
    }

    public static boolean bind(int port, TCPProcessor processor) {
        if (!isPortAvailable(port)) {
            log.error("A service is already listening on port " +
                      port + ". Please select a different port for TCP Inbound endpoint.");
            return false;
        }

        ListenerEndpoint ep = reactor.listen(getSocketAddress(port));

        try {
            ep.waitFor();
            endpointMap.put(port, ep);
            processorMap.put(port, processor);
            return true;
        } catch (InterruptedException e) {
            log.error("Error while starting a new TCP Inbound Listener on port " + port + ". ", e);
            return false;
        }
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean unbind(int port) {
        ListenerEndpoint ep = endpointMap.get(port);
        endpointMap.remove(port);
        processorMap.remove(port);
        if (ep == null) {
            return false;
        }
        ep.close();
        return true;
    }

    public static boolean isEndpointRunning(int port) {
        return endpointMap.get(port) != null;
    }

    private static SocketAddress getSocketAddress(int port) {
        InetSocketAddress isa = new InetSocketAddress(port);
        return isa;
    }

    private static IOReactorConfig getDefaultReactorConfig() {
        IOReactorConfig.Builder builder = IOReactorConfig.custom();

        return builder.setSelectInterval(
                TCPConfiguration.getInstance().getIntProperty(InboundTCPConstants.TCPConstants.SELECT_INTERVAL, 1000))
                      .setShutdownGracePeriod(TCPConfiguration.getInstance().getIntProperty(
                              InboundTCPConstants.TCPConstants.SHUTDOWN_GRACE_PERIOD, 5000)).setIoThreadCount(
                        TCPConfiguration.getInstance().getIntProperty(InboundTCPConstants.TCPConstants.IO_THREAD_COUNT,
                                                                      Runtime.getRuntime().availableProcessors()))
                      .setSoTimeout(TCPConfiguration.getInstance()
                                                    .getIntProperty(InboundTCPConstants.TCPConstants.SO_TIMEOUT, 0))
                      .setSoKeepAlive(TCPConfiguration.getInstance().getBooleanProperty(
                              InboundTCPConstants.TCPConstants.SO_KEEP_ALIVE, true)).setTcpNoDelay(
                        TCPConfiguration.getInstance()
                                        .getBooleanProperty(InboundTCPConstants.TCPConstants.TCP_NO_DELAY, true))
                      .setConnectTimeout(TCPConfiguration.getInstance().getIntProperty(
                              InboundTCPConstants.TCPConstants.CONNECT_TIMEOUT, 0)).setRcvBufSize(
                        TCPConfiguration.getInstance().getIntProperty(InboundTCPConstants.TCPConstants.SO_RCVBUF, 0))
                      .setSndBufSize(TCPConfiguration.getInstance()
                                                     .getIntProperty(InboundTCPConstants.TCPConstants.SO_SNDBUF, 0))
                      .setInterestOpQueued(false).setSoReuseAddress(true).setSoLinger(-1).build();
    }
}
