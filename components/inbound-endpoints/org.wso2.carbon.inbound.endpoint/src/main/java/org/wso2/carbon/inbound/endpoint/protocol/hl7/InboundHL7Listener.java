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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.transport.passthru.util.BufferFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

public class InboundHL7Listener implements InboundRequestProcessor {

    private static final Log log = LogFactory.getLog(InboundHL7Listener.class);

    private int port;
    private InboundProcessorParams params;

    public InboundHL7Listener(InboundProcessorParams params) {
        this.params = params;
    }

    @Override
    public void init() {
        if (!InboundHL7IOReactor.isStarted()) {
            log.info("Starting MLLP Transport Reactor");
            try {
                InboundHL7IOReactor.start();
            } catch (IOException e) {
                log.error("MLLP Reactor startup error: ", e);
                return;
            }
        }

        start();
    }

    public void start() {
        log.info("Starting HL7 Inbound Endpoint on port " + this.port);

        try {
            this.port = Integer.parseInt(params.getProperties().getProperty(MLLPConstants.PARAM_HL7_PORT));
        } catch (NumberFormatException e) {
            log.error("The port specified is of an invalid type: " + this.port + ". Endpoint not started.");
            return;
        }
        HL7EndpointManager.getInstance().startEndpoint(port, params.getName(), params);

    }

    @Override
    public void destroy() {
        HL7EndpointManager.getInstance().closeEndpoint(port);
    }

}
