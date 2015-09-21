/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.http.nio.reactor.IOReactorException;
import org.apache.log4j.Logger;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.management.TCPEndpointManager;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Listener class for TCP Inbound Endpoint.which is triggered by inbound core
 * and responsible for start listening for TCP connections on given port.
 */
public class InboundTCPListener implements InboundRequestProcessor {

    private static final Logger log = Logger.getLogger(InboundTCPListener.class);

    private int port;
    private InboundProcessorParams params;

    public InboundTCPListener(InboundProcessorParams params) {
        this.params = params;
        String tcpPort = null;
        try {
            tcpPort = params.getProperties().getProperty(InboundTCPConstants.INBOUND_TCP_PORT);
            this.port = Integer.parseInt(tcpPort);
        } catch (NumberFormatException numberFormatException) {
            log.error("The port number : " + tcpPort + " is not an integer. TCP inbound endpoint not started.",
                      numberFormatException);
        }
    }

    @Override public void init() {
        if (isPortAvailable(this.port)) {
            if (!InboundTCPIOReactor.isStarted()) {
                try {
                    InboundTCPIOReactor.start();
                } catch (IOReactorException ioReactorException) {
                    log.error("Inbound TCP IOReactor startup error: ", ioReactorException);
                    return;
                }
            }
            start();
        } else {
            this.destroy();
        }
    }

    public void start() {
        TCPEndpointManager.getInstance().startEndpoint(port, params.getName(), params);
    }

    @Override public void destroy() {
        TCPEndpointManager.getInstance().closeEndpoint(port);
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return true;
        } catch (IOException ioException) {
            log.error("Port : " + port + " is used by another application. Please select a different port for the " +
                      "TCP inbound endpoint.", ioException);
            return false;
        }
    }
}
