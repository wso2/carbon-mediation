/*
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
package org.wso2.carbon.inbound.endpoint.protocol.http.management;

import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.transport.passthru.SourceHandler;
import org.apache.synapse.transport.passthru.api.PassThroughInboundEndpointHandler;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointsDataStore;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointInfoDTO;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpConstants;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpSourceHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manager which handles Http Listeners activities for Inbound Endpoints, coordinating
 * with Pass-through APIs and registry etc. This is the central place to mange Http Listeners
 * for Inbound endpoints
 */
public class HTTPEndpointManager extends AbstractInboundEndpointManager {

    private static HTTPEndpointManager instance = new HTTPEndpointManager();

    private static final Logger log = Logger.getLogger(HTTPEndpointManager.class);

    private HTTPEndpointManager() {
        super();
    }

    public static HTTPEndpointManager getInstance() {
        return instance;
    }

    /**
     * Start Http Inbound endpoint in a particular port
     * @param port  port
     * @param name  endpoint name
     */
    public void startEndpoint(int port, String name) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();

        String epName = dataStore.getEndpointName(port, tenantDomain);
        if (epName != null) {
            if (epName.equalsIgnoreCase(name)) {
                log.info(epName + " Endpoint is already started in port : " + port);
            } else {
                String msg = "Another endpoint named : " + epName + " is currently using this port: " + port;
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            dataStore.registerEndpoint(port, tenantDomain, InboundHttpConstants.HTTP, name, null);
            boolean start =  startListener(port, name);
            if(!start){
                dataStore.unregisterEndpoint(port,tenantDomain);
            }
        }

    }

    /**
     * Start Https Inbound endpoint in a particular port
     * @param port  port
     * @param name  endpoint name
     */
    public void startSSLEndpoint(int port , String name, SSLConfiguration sslConfiguration){
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
        String epName = dataStore.getEndpointName(port, tenantDomain);

        if (PassThroughInboundEndpointHandler.isEndpointRunning(port)) {
            if(epName != null && epName.equalsIgnoreCase(name) ){
                log.info(epName + " Endpoint is already started in port : " + port);
            }else{
                String msg = "Cannot Start Endpoint "+ name+ " Already occupied port " + port + " by another Endpoint ";
                log.warn(msg);
                throw new SynapseException(msg);
            }
        } else {
            if(epName != null && epName.equalsIgnoreCase(name)){
                log.info(epName + " Endpoint is already registered in registry");
            }else{
                dataStore.registerSSLEndpoint(port, tenantDomain, InboundHttpConstants.HTTPS, name, sslConfiguration);
            }
            boolean start = startSSLListener(port, name, sslConfiguration);
            if (!start) {
               dataStore.unregisterEndpoint(port,tenantDomain);
            }
        }
    }



    /**
     * Start Http Listener in a particular port
     * @param port  port
     * @param name  endpoint name
     */
    public boolean startListener(int port, String name) {
        if (PassThroughInboundEndpointHandler.isEndpointRunning(port)) {
            log.info("Listener is already started for port : " + port);
            return true;
        }

        SourceConfiguration sourceConfiguration = null;
        try {
            sourceConfiguration = PassThroughInboundEndpointHandler.getPassThroughSourceConfiguration();
        } catch (Exception e) {
            log.warn("Cannot get PassThroughSourceConfiguration ", e);
            return false;
        }
        if (sourceConfiguration != null) {
            //Create Handler for handle Http Requests
            SourceHandler inboundSourceHandler = new InboundHttpSourceHandler(port, sourceConfiguration);
            try {
                //Start Endpoint in given port
                PassThroughInboundEndpointHandler.startEndpoint(new InetSocketAddress(port),
                        inboundSourceHandler, name);
            } catch (NumberFormatException e) {
                log.error("Exception occurred while starting listener for endpoint : "
                             + name + " ,port " + port, e);
            }
        } else {
            log.warn("SourceConfiguration is not registered in PassThrough Transport hence not start inbound endpoint " + name);
            return false;
        }
        return true;
    }

    /**
     * Start Http Listener in a particular port
     * @param port  port
     * @param name  endpoint name
     */
    public boolean startSSLListener(int port, String name, SSLConfiguration sslConfiguration) {
        if (PassThroughInboundEndpointHandler.isEndpointRunning(port)) {
            log.info("Listener is already started for port : " + port);
            return true;
        }
        SourceConfiguration sourceConfiguration = null;
        try {
            sourceConfiguration = PassThroughInboundEndpointHandler.getPassThroughSSLSourceConfiguration();
        } catch (Exception e) {
            log.warn("Cannot get PassThroughSSLSourceConfiguration ", e);
            return false;
        }
        if (sourceConfiguration != null) {
            //Create Handler for handle Http Requests
            SourceHandler inboundSourceHandler = new InboundHttpSourceHandler(port, sourceConfiguration);
            try {
                //Start Endpoint in given port
                PassThroughInboundEndpointHandler.startSSLEndpoint(new InetSocketAddress(port),
                                                                   inboundSourceHandler, name, sslConfiguration);
            } catch (NumberFormatException e) {
                log.error("Exception occurred while starting listener for endpoint : "
                          + name + " ,port " + port, e);
                return false;
            }
        } else {
            log.warn("SourceConfiguration is not registered in PassThrough Transport hence not start inbound endpoint " + name);
            return false;
        }
        return true;
    }

    /**
     * Stop Inbound Endpoint
     * @param port  port of the endpoint
     */
    public void closeEndpoint(int port) {

        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = cc.getTenantDomain();
        dataStore.unregisterEndpoint(port, tenantDomain);

        if (!PassThroughInboundEndpointHandler.isEndpointRunning(port)) {
            log.info("Listener Endpoint is not started");
            return;
        } else if (dataStore.isEndpointRegistryEmpty(port)) {
            // if no other endpoint is working on this port. close the http listening endpoint
            PassThroughInboundEndpointHandler.closeEndpoint(port);
        }

    }

    /**
     * Start Http listeners for all the Inbound Endpoints. This should be called in the
     * server startup to load all the required listeners for endpoints in all tenants
     */
    public void loadEndpointListeners() {
        Map<Integer, List<InboundEndpointInfoDTO>> tenantData = dataStore.getAllEndpointData();
        for (Map.Entry tenantInfoEntry : tenantData.entrySet()) {
            int port = (Integer) tenantInfoEntry.getKey();

            InboundEndpointInfoDTO inboundEndpointInfoDTO =
                       (InboundEndpointInfoDTO) ((ArrayList) tenantInfoEntry.getValue()).get(0);

            if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTP)) {
                startListener(port, inboundEndpointInfoDTO.getEndpointName());
            } else if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTPS)) {
                startSSLListener(port, inboundEndpointInfoDTO.getEndpointName(), inboundEndpointInfoDTO.getSslConfiguration());
            }

        }
    }

}
