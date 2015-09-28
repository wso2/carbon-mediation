/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is responsible for monitor all IO events regarding to TCP Inbound endpoints
 * each IO event is notified to TCPMultiIOHandler.
 */
public class InboundTCPIOReactor {

    private static final Logger log = Logger.getLogger(InboundTCPIOReactor.class);

    private static volatile boolean isStarted = false;

    private static ListeningIOReactor reactor;

    private static ConcurrentHashMap<Integer, ListenerEndpoint> endpointMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<Integer, TCPProcessor> processorMap = new ConcurrentHashMap<>();

    public static boolean isStarted() {
        return isStarted;
    }

    /**
     * Start IOReactor for TCPInbound endpoint
     *
     * @throws IOReactorException
     */
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
                    reactor.execute(new TCPMultiIOHandler(processorMap));
                } catch (IOException ioException) {
                    isStarted = false;
                    log.error("Error while starting the TCP Inbound Transport IO Reactor.", ioException);
                }
            }
        });
        reactorThread.start();
    }

    /**
     * Stop IOReactor for TCPInbound endpoint
     */
    public static void stop() {
        try {
            reactor.shutdown();
            endpointMap.clear();
            isStarted = false;
        } catch (IOException ioException) {
            log.error("Error while shutting down TCP Inbound Transport IO Reactor. ", ioException);
        }
    }

    public static void pause() {
        try {
            reactor.pause();
        } catch (IOException ioException) {
            log.error("Error while pausing TCP Inbound Transport IO Reactor. ", ioException);
        }
    }

    /**
     * Bind TCPProcessor to the Inbound endpoint port
     *
     * @param port
     * @param processor TCPProcessor which is unique to a EndPoint
     * @return true if starting endpoint is successful
     */
    public static boolean bind(int port, TCPProcessor processor) {
        if (!isPortAvailable(port)) {
            log.error("A service is already listening on port " +
                      port + ". Please select a different port for TCP Inbound endpoint.");
            return false;
        }
        ListenerEndpoint listenerEndpoint = reactor.listen(getSocketAddress(port));
        try {
            listenerEndpoint.waitFor();
            endpointMap.put(port, listenerEndpoint);
            processorMap.put(port, processor);
            return true;
        } catch (InterruptedException interruptedException) {
            log.error("Error while starting a new TCP Inbound Listener on port " + port + ". ", interruptedException);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.close();
            return true;
        } catch (IOException ioException) {
            return false;
        }
    }

    public static boolean unbind(int port) {
        ListenerEndpoint listenerEndpoint = endpointMap.get(port);
        endpointMap.remove(port);
        processorMap.remove(port);
        if (listenerEndpoint == null) {
            return false;
        }
        listenerEndpoint.close();
        return true;
    }

    public static boolean isEndpointRunning(int port) {
        return endpointMap.get(port) != null;
    }

    private static SocketAddress getSocketAddress(int port) {
        return new InetSocketAddress(port);
    }

    private static IOReactorConfig getDefaultReactorConfig() {
        IOReactorConfig.Builder builder = IOReactorConfig.custom();

        return builder
                .setSelectInterval(TCPConfiguration.getInstance().getIntProperty(
                        InboundTCPConstants.TCPConstants.SELECT_INTERVAL, 1000))
                .setShutdownGracePeriod(TCPConfiguration.getInstance().getIntProperty(
                        InboundTCPConstants.TCPConstants.SHUTDOWN_GRACE_PERIOD, 500))
                .setIoThreadCount(TCPConfiguration.getInstance().getIntProperty(
                        InboundTCPConstants.TCPConstants.IO_THREAD_COUNT, Runtime.getRuntime().availableProcessors()))
                .setSoTimeout(TCPConfiguration.getInstance().getIntProperty(
                        InboundTCPConstants.TCPConstants.SO_TIMEOUT, 0))
                .setSoKeepAlive(TCPConfiguration.getInstance().getBooleanProperty(
                        InboundTCPConstants.TCPConstants.SO_KEEP_ALIVE, true))
                .setTcpNoDelay(TCPConfiguration.getInstance().getBooleanProperty(
                        InboundTCPConstants.TCPConstants.TCP_NO_DELAY, true))
                .setConnectTimeout(TCPConfiguration.getInstance().getIntProperty(
                        InboundTCPConstants.TCPConstants.CONNECT_TIMEOUT, 0))
                .setRcvBufSize(TCPConfiguration.getInstance().getIntProperty(
                        InboundTCPConstants.TCPConstants.SO_RCVBUF, 0))
                .setSndBufSize(TCPConfiguration.getInstance().getIntProperty(
                        InboundTCPConstants.TCPConstants.SO_SNDBUF, 0))
                .setInterestOpQueued(false)
                .setSoReuseAddress(false)
                .setSoLinger(-1)
                .build();
    }
}
