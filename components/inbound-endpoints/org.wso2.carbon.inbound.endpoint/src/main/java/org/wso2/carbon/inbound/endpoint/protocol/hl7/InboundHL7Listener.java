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
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class InboundHL7Listener implements InboundRequestProcessor {

    private static final Log log = LogFactory.getLog(InboundHL7Listener.class);

    private String port;
    private InboundProcessorParams inboundParams;

    public InboundHL7Listener(InboundProcessorParams params) {
        this.inboundParams = params;
        this.port = params.getProperties().getProperty(MLLPConstants.PARAM_HL7_PORT);
    }

    @Override
    public void init() {
        if (!InboundHL7IOReactor.isStarted()) {
            log.info("Starting MLLP Transport Reactor");
            try {
                InboundHL7IOReactor.start();
            } catch (IOException e) {
                log.error("MLLP Reactor startup error: " + e.getMessage());
                return;
            }
        }

        start();
    }

    public void start() {
        log.info("Starting HL7 Inbound Endpoint on port " + this.port);

        if (this.port == null) {
            log.error("The port specified is null");
            return;
        }

        try {
            int port = Integer.parseInt(this.port);
            InboundHL7IOReactor.bind(port, this.inboundParams);
        } catch (NumberFormatException e) {
            log.error("The port specified is of an invalid type: " + this.port + ". HL7 Inbound Endpoint not started");
        }
    }

    @Override
    public void destroy() {
        InboundHL7IOReactor.unbind(Integer.parseInt(port));
    }

}
