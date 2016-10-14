/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.http2;

import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.http2.common.InboundHttp2Constants;
import org.wso2.carbon.inbound.endpoint.protocol.http2.management.Http2EndpointManager;

public class InboundHttp2Listener implements InboundRequestProcessor {

    private static final Logger log = Logger.getLogger(InboundHttp2Listener.class);

    private String name;
    private int port;
    private InboundProcessorParams processorParams;

    public InboundHttp2Listener(InboundProcessorParams params) {
        processorParams = params;
        String portParam = params.getProperties().getProperty(
                InboundHttp2Constants.INBOUND_PORT);
        try {
            port = Integer.parseInt(portParam);
        } catch (NumberFormatException e) {
            handleException("Validation failed for the port parameter " + portParam, e);
        }
        name = params.getName();
    }

    @Override
    public void init() {
        Http2EndpointManager.getInstance().startEndpoint(port, name, processorParams);
    }

    @Override
    public void destroy() {
        Http2EndpointManager.getInstance().closeEndpoint(port);
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

}
