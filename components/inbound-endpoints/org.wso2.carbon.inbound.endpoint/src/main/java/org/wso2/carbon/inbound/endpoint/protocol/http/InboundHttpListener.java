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

package org.wso2.carbon.inbound.endpoint.protocol.http;

import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.transport.passthru.api.PassThroughInboundEndpointHandler;
import org.wso2.carbon.inbound.endpoint.protocol.http.management.EndpointListenerManager;

import java.io.IOException;
import java.net.ServerSocket;


/**
 * Listener class for HttpInboundEndpoint which is trigger by inbound core and
 * responsible for start ListeningEndpoint related to given port
 */
public class InboundHttpListener implements InboundRequestProcessor {

    private static final Logger log = Logger.getLogger(InboundHttpListener.class);

    private String name;
    private int port;

    public InboundHttpListener(InboundProcessorParams params) {
        String portParam = params.getProperties().getProperty(
                InboundHttpConstants.INBOUND_ENDPOINT_PARAMETER_HTTP_PORT);
        try {
            port = Integer.parseInt(portParam);
        } catch (NumberFormatException e) {
            handleException("Please provide port number as integer  instead of  port  " + portParam, e);
        }
        name = params.getName();
    }

    @Override
    public void init() {
        if(isPortUsedByAnotherApplication(port)){
            log.warn("Port " + port + " used by inbound endpoint " + name + " is already used by another application " +
                     "hence undeploying inbound endpoint");
            this.destroy();
        }else {
            EndpointListenerManager.getInstance().startEndpoint(port, name);
        }
    }

    @Override
    public void destroy() {
        EndpointListenerManager.getInstance().closeEndpoint(port);
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }


    protected boolean isPortUsedByAnotherApplication(int port) {
        if (PassThroughInboundEndpointHandler.isEndpointRunning(port)) {
            return false;
        } else {
            try {
                ServerSocket srv = new ServerSocket(port);
                srv.close();
                srv = null;
                return false;
            } catch (IOException e) {
                return true;
            }
        }
    }
}
