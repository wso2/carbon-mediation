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
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.transport.passthru.SourceHandler;
import org.apache.synapse.transport.passthru.api.PassThroughInboundEndpointHandler;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;

import java.net.InetSocketAddress;


/**
 * Listener class for HttpInboundEndpoint which is trigger by inbound core and
 * responsible for start ListeningEndpoint related to given port
 */
public class InboundHttpListener implements InboundRequestProcessor {

    private static final Logger log = Logger.getLogger(InboundHttpListener.class);

    /**
     * Sequence that messages are injecting
     */
    private String injectingSequence;

    /**
     * Sequence which should trigger as fault handler
     */
    private String onErrorSequence;

    private SynapseEnvironment synapseEnvironment;
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
        injectingSequence = params.getInjectingSeq();
        onErrorSequence = params.getOnErrorSeq();
        synapseEnvironment = params.getSynapseEnvironment();
    }

    @Override
    public void init() {

        try {
            //Create wrapping object for Inbound Configuration
            InboundHttpConfiguration inboundHttpConfiguration =
                    new InboundHttpConfiguration(injectingSequence, onErrorSequence, synapseEnvironment);

            //Get registered source configuration of PassThrough Transport
            SourceConfiguration sourceConfiguration = PassThroughInboundEndpointHandler.getPassThroughSourceConfiguration();

            //Create Handler for handle Http Requests
            SourceHandler inboundSourceHandler = new InboundHttpSourceHandler(sourceConfiguration, inboundHttpConfiguration);

            //Start Endpoint in given port
            PassThroughInboundEndpointHandler.startEndpoint(new InetSocketAddress(port), inboundSourceHandler, name);

        } catch (Exception e) {
            handleException("Cannot init Inbound Endpoint " + name, e);
        }
    }

    @Override
    public void destroy() {
        PassThroughInboundEndpointHandler.closeEndpoint(port);
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
