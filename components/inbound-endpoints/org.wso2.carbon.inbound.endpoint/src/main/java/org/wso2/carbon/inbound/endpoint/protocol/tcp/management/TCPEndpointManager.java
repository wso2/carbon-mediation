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

package org.wso2.carbon.inbound.endpoint.protocol.tcp.management;

import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.util.BufferFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.inboundfactory.InboundRequestProcessorFactoryImpl;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPConstants;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.InboundTCPIOReactor;
import org.wso2.carbon.inbound.endpoint.protocol.tcp.core.TCPProcessor;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is responsible to manage Inbound end points individually each end point is bind to InboundTCPIOReactor
 */

public class TCPEndpointManager extends AbstractInboundEndpointManager {

    private static final Logger log = Logger.getLogger(TCPEndpointManager.class);

    private static TCPEndpointManager instance = new TCPEndpointManager();

    public TCPEndpointManager() {
        super();
    }

    public static TCPEndpointManager getInstance() {
        return instance;
    }

    //new endpoints will be bound to IO Reactor
    public boolean startListener(int port, String name, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
        if (params.getProperties().getProperty(InboundTCPConstants.TCP_INBOUND_TENANT_DOMAIN) == null) {
            params.getProperties().put(InboundTCPConstants.TCP_INBOUND_TENANT_DOMAIN, tenantDomain);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(InboundTCPConstants.INBOUND_PARAMS, params);
        parameters.put(InboundTCPConstants.INBOUND_TCP_BUFFER_FACTORY,
                       new BufferFactory(8 * 1024, new HeapByteBufferAllocator(), 1024));
        validateParameters(params, parameters);
        TCPProcessor tcpProcessor = new TCPProcessor(parameters);
        parameters.put(InboundTCPConstants.TCP_REQ_PROC, tcpProcessor);
        return InboundTCPIOReactor.bind(port, tcpProcessor);

    }

    public boolean startEndpoint(int port, String name, InboundProcessorParams params) {
        log.info("Starting TCP Inbound Endpoint on port " + port);

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();

        params.getProperties().setProperty(InboundTCPConstants.TCP_INBOUND_TENANT_DOMAIN, tenantDomain);

        String epName = dataStore.getListeningEndpointName(port, tenantDomain);
        if (epName != null) {
            if (epName.equalsIgnoreCase(name)) {
                log.info(epName + ": TCP Inbound Endpoint is already started in port : " + port);
            } else {
                String msg = "Another endpoint named : " + epName + " is currently using this port: " + port;
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            dataStore.registerListeningEndpoint(port, tenantDomain,
                                                InboundRequestProcessorFactoryImpl.Protocols.tcp.toString(), name,
                                                params);
            return startListener(port, name, params);
        }

        return false;
    }

    @Override public void closeEndpoint(int port) {
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = cc.getTenantDomain();
        dataStore.unregisterListeningEndpoint(port, tenantDomain);

        if (!InboundTCPIOReactor.isEndpointRunning(port)) {
            log.info("Listener Endpoint is not started");
        } else if (dataStore.isEndpointRegistryEmpty(port)) {
            // if no other endpoint is working on this port. close the listening endpoint
            InboundTCPIOReactor.unbind(port);
        }
    }

    private void validateParameters(InboundProcessorParams params, Map<String, Object> parameters) {
        //auto ack is not using in TCP
        try {
            Integer.valueOf(params.getProperties().getProperty(InboundTCPConstants.PARAM_TCP_TIMEOUT));
        } catch (NumberFormatException e) {
            log.warn("Parameter inbound.tcp.TimeOut is not valid. Default timeout " +
                     "of " + InboundTCPConstants.DEFAULT_TCP_TIMEOUT + " milliseconds will be used.");
            params.getProperties().setProperty(InboundTCPConstants.PARAM_TCP_TIMEOUT,
                                               String.valueOf(InboundTCPConstants.DEFAULT_TCP_TIMEOUT));
        }

        try {
            if (params.getProperties().getProperty(InboundTCPConstants.PARAM_TCP_CHARSET) == null) {
                params.getProperties().setProperty(InboundTCPConstants.PARAM_TCP_CHARSET,
                                                   InboundTCPConstants.UTF8_CHARSET.displayName());
                parameters.put(InboundTCPConstants.TCP_CHARSET_DECODER, InboundTCPConstants.UTF8_CHARSET.newDecoder());
            } else {
                parameters.put(InboundTCPConstants.TCP_CHARSET_DECODER, Charset.forName(
                        params.getProperties().getProperty(InboundTCPConstants.PARAM_TCP_CHARSET)).newDecoder());
            }
        } catch (UnsupportedCharsetException e) {
            parameters.put(InboundTCPConstants.TCP_CHARSET_DECODER, InboundTCPConstants.UTF8_CHARSET.newDecoder());
            log.error("Unsupported charset '" +
                      params.getProperties().getProperty(InboundTCPConstants.PARAM_TCP_CHARSET) + "' " +
                      "specified. Default " +
                      "UTF-8 will be used instead.", e);
        }
    }

}