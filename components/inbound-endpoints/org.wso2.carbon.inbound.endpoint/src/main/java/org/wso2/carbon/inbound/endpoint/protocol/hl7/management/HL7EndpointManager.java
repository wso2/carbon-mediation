package org.wso2.carbon.inbound.endpoint.protocol.hl7.management;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.inboundfactory.InboundRequestProcessorFactoryImpl;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.*;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.HL7Processor;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.InboundHL7IOReactor;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.core.MLLPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.hl7.util.Axis2HL7Constants;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

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

public class HL7EndpointManager extends AbstractInboundEndpointManager {
    private static final Logger log = Logger.getLogger(HL7EndpointManager.class);

    private static HL7EndpointManager instance = new HL7EndpointManager();

    private HL7EndpointManager() {
        super();
    }

    public static HL7EndpointManager getInstance() {
        return instance;
    }

    @Override
    public boolean startListener(int port, String name) {
        return true;
    }

    public void startListener(int port, String name, InboundProcessorParams params) {
        log.info("Starting HL7 Inbound Endpoint on port " + port);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
        if (params.getProperties().getProperty(MLLPConstants.HL7_INBOUND_TENANT_DOMAIN) == null) {
            params.getProperties().put(MLLPConstants.HL7_INBOUND_TENANT_DOMAIN, tenantDomain);
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(MLLPConstants.INBOUND_PARAMS, params);
        parameters.put(MLLPConstants.INBOUND_HL7_BUFFER_FACTORY,
                new BufferFactory(8 * 1024, new HeapByteBufferAllocator(), 1024));
        validateParameters(params, parameters);

        HL7Processor hl7Processor = new HL7Processor(parameters);
        parameters.put(MLLPConstants.HL7_REQ_PROC, hl7Processor);

        InboundHL7IOReactor.bind(port, hl7Processor);
    }

    public void startEndpoint(int port, String name, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();

        params.getProperties().setProperty(MLLPConstants.HL7_INBOUND_TENANT_DOMAIN, tenantDomain);

        String epName = dataStore.getListeningEndpointName(port, tenantDomain);
        if (epName != null) {
            if (epName.equalsIgnoreCase(name)) {
                log.info(epName + " Endpoint is already started in port : " + port);
            } else {
                String msg = "Another endpoint named : " + epName + " is currently using this port: " + port;
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            dataStore.registerListeningEndpoint(port, tenantDomain,
                    InboundRequestProcessorFactoryImpl.Protocols.hl7.toString(), name, params);
            startListener(port, name, params);
        }
    }

    @Override
    public boolean startEndpoint(int port, String name) {
        return true;
    }

    @Override
    public void closeEndpoint(int port) {
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = cc.getTenantDomain();
        dataStore.unregisterListeningEndpoint(port, tenantDomain);

        if (!InboundHL7IOReactor.isEndpointRunning(port)) {
            log.info("Listener Endpoint is not started");
            return ;
        } else if (dataStore.isEndpointRegistryEmpty(port)) {
            // if no other endpoint is working on this port. close the listening endpoint
            InboundHL7IOReactor.unbind(port);
        }
    }

    private void validateParameters(InboundProcessorParams params, Map<String, Object> parameters) {
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
                        message = preProcessor.process(message, Axis2HL7Constants.MessageType.V2X,
                                Axis2HL7Constants.MessageEncoding.ER7);
                        return super.parse(message);
                    }
                };

                parameters.put(MLLPConstants.HL7_PRE_PROC_PARSER_CLASS, preProcParser);
            }
        } catch (Exception e) {
            log.error("Error creating message preprocessor: ", e);
        }

        try {
            if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_CHARSET) == null) {
                params.getProperties().setProperty(MLLPConstants.PARAM_HL7_CHARSET, MLLPConstants.UTF8_CHARSET.displayName());
                parameters.put(MLLPConstants.HL7_CHARSET_DECODER, MLLPConstants.UTF8_CHARSET.newDecoder());
            } else {
                parameters.put(MLLPConstants.HL7_CHARSET_DECODER, Charset
                        .forName(params.getProperties().getProperty(MLLPConstants.PARAM_HL7_CHARSET)).newDecoder());
            }
        } catch (UnsupportedCharsetException e) {
            parameters.put(MLLPConstants.HL7_CHARSET_DECODER, MLLPConstants.UTF8_CHARSET.newDecoder());
            log.error("Unsupported charset '" + params.getProperties()
                    .getProperty(MLLPConstants.PARAM_HL7_CHARSET) + "' specified. Default UTF-8 will be used instead.");
        }

        if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_VALIDATE) == null) {
            params.getProperties().setProperty(MLLPConstants.PARAM_HL7_VALIDATE, "true");
        }

        if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE) == null) {
            params.getProperties().setProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE, "false");
        } else {
            if (!params.getProperties().getProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE).equalsIgnoreCase("true") &&
                    !params.getProperties().getProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE).equalsIgnoreCase("false")) {
                params.getProperties().setProperty(MLLPConstants.PARAM_HL7_BUILD_RAW_MESSAGE, "false");
            }
        }

        if (params.getProperties().getProperty(MLLPConstants.PARAM_HL7_PASS_THROUGH_INVALID_MESSAGES) == null) {
            params.getProperties().setProperty(MLLPConstants.PARAM_HL7_PASS_THROUGH_INVALID_MESSAGES, "false");
        } else {
            if (!params.getProperties().getProperty(MLLPConstants.PARAM_HL7_PASS_THROUGH_INVALID_MESSAGES).equalsIgnoreCase("true") &&
                    !params.getProperties().getProperty(MLLPConstants.PARAM_HL7_PASS_THROUGH_INVALID_MESSAGES).equalsIgnoreCase("false")) {
                params.getProperties().setProperty(MLLPConstants.PARAM_HL7_PASS_THROUGH_INVALID_MESSAGES, "false");
            }
        }
    }
}
