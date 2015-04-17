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
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InboundHL7Listener implements InboundRequestProcessor {

    private static final Log log = LogFactory.getLog(InboundHL7Listener.class);

    private String port;
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public InboundHL7Listener(InboundProcessorParams params) {
        parameters.put(MLLPConstants.INBOUND_PARAMS, params);
        this.port = params.getProperties().getProperty(MLLPConstants.PARAM_HL7_PORT);
        validateParameters(params);
    }

    private void validateParameters(InboundProcessorParams params) {
        if (!params.getProperties().getProperty(MLLPConstants.PARAM_HL7_AUTO_ACK).equalsIgnoreCase("true")
                && !params.getProperties().getProperty(MLLPConstants.PARAM_HL7_AUTO_ACK).equalsIgnoreCase("false")) {
            log.warn("Parameter inbound.hl7.AutoAck is not valid. Default value of true will be used.");
            params.getProperties().setProperty(MLLPConstants.PARAM_HL7_AUTO_ACK, "true");
        }

        try {
            Integer.valueOf(params.getProperties().getProperty(MLLPConstants.PARAM_HL7_TIMEOUT));
        } catch (NumberFormatException e) {
            log.warn("Parameter inbound.hl7.TimeOut is not valid. Default timeout " +
                    "of " + MLLPConstants.DEFAULT_HL7_TIMEOUT + " milliseconds will be used.");
            params.getProperties().setProperty(MLLPConstants.PARAM_HL7_TIMEOUT,
                    String.valueOf(MLLPConstants.DEFAULT_HL7_TIMEOUT));
        }

        try {
            if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_PRE_PROC) != null) {
                final HL7MessagePreprocessor preProcessor = (HL7MessagePreprocessor) Class.forName(params.getProperties()
                        .getProperty(MLLPConstants.PARAM_HL7_PRE_PROC)).newInstance();

                Parser preProcParser = new PipeParser() {
                    public Message parse(String message) throws HL7Exception {
                        message = preProcessor.process(message, HL7Constants.MessageType.V2X,
                                HL7Constants.MessageEncoding.ER7);
                        return super.parse(message);
                    }
                };

                parameters.put(MLLPConstants.HL7_PRE_PROC_PARSER_CLASS, preProcParser);
            }
        } catch (Exception e) {
            log.error("Error creating message preprocessor: " + e.getMessage(), e);
        }

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
            InboundHL7IOReactor.bind(port, this.parameters);
        } catch (NumberFormatException e) {
            log.error("The port specified is of an invalid type: " + this.port + ". HL7 Inbound Endpoint not started");
        }
    }

    @Override
    public void destroy() {
        InboundHL7IOReactor.unbind(Integer.parseInt(port));
    }

}
