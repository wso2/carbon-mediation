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

package org.wso2.carbon.inbound.endpoint.protocol.https2;

import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundHttp2Constants;
import org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EndpointManager;

public class InboundHttps2Listener implements InboundRequestProcessor {

    private static final Logger log = Logger.getLogger(InboundHttps2Listener.class);
    private int port;
    private String name;
    private InboundProcessorParams processorParams;

    public InboundHttps2Listener(InboundProcessorParams params) {
        processorParams = params;
        String portParam = params.getProperties().getProperty(InboundHttp2Constants.INBOUND_PORT);
        try {
            port = Integer.parseInt(portParam);
        } catch (NumberFormatException e) {
            handleException("Please provide port number as integer  instead of  port  " + portParam,
                    e);
        }
        name = params.getName();
    }

    @Override
    public void init() {
        Http2EndpointManager.getInstance().startSSLEndpoint(port, name, processorParams);
    }

    public void destroy() {
        Http2EndpointManager.getInstance().closeEndpoint(port);
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

}
