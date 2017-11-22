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

package org.wso2.carbon.inbound.endpoint.protocol.tcp.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.management.TCPEndpointManager;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 */

public class InboundTCPListener implements InboundRequestProcessor {

    private static final Log log = LogFactory.getLog(InboundTCPListener.class);
    private int port;
    private InboundProcessorParams params;

    public InboundTCPListener(InboundProcessorParams params) {
        this.params = params;
        String tcpPort = null;
        try {
            tcpPort = params.getProperties().getProperty(InboundTCPConstants.INBOUND_TCP_PORT);
            this.port = Integer.parseInt(tcpPort);
        } catch (NumberFormatException e) {
            log.error("The port number : " + tcpPort + " is not an integer. TCP inbound endpoint not started.", e);
        }
    }

    @Override public void init() {
        if (isPortAvailable(this.port)) {
            if (!InboundTCPIOReactor.isStarted()) {
                //log.info("Starting TCP Inbound IO Reactor...");
                try {
                    InboundTCPIOReactor.start();
                } catch (IOReactorException e) {
                    log.error("Inbound TCP IOReactor startup error: ", e);
                    return;
                }
            }
            start();
        } else {
            this.destroy();
        }
    }

    public void start() {
        //log.info("Starting TCP Inbound Endpoint on port " + this.port);
        TCPEndpointManager.getInstance().startEndpoint(port, params.getName(), params);
    }

    @Override public void destroy() {
        TCPEndpointManager.getInstance().closeEndpoint(port);
    }

    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            ss = null;
            return true;
        } catch (IOException e) {
            log.error("Port : " + port + " is used by another application. Please select a different port for the " +
                      "TCP inbound endpoint.", e);
            return false;
        }
    }
}
