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
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.SourceHandler;
import org.apache.synapse.transport.passthru.api.PassThroughInboundEndpointHandler;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointInfoDTO;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpConfiguration;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpConstants;
import org.wso2.carbon.inbound.endpoint.protocol.http.InboundHttpSourceHandler;
import org.wso2.carbon.inbound.endpoint.protocol.http.config.WorkerPoolConfiguration;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Manager which handles Http Listeners activities for Inbound Endpoints, coordinating
 * with Pass-through APIs and registry etc. This is the central place to mange Http Listeners
 * for Inbound endpoints
 */
public class HTTPEndpointManager extends AbstractInboundEndpointManager {

    private static HTTPEndpointManager instance = new HTTPEndpointManager();

    private static final Logger log = Logger.getLogger(HTTPEndpointManager.class);

    private ConcurrentHashMap<String,ConcurrentHashMap<Integer, WorkerPoolConfiguration>> workerPoolMap =
                                  new ConcurrentHashMap<String, ConcurrentHashMap<Integer, WorkerPoolConfiguration>>();

    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Pattern>> dispatchPatternMap =
            new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Pattern>>();

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
     * @param params inbound endpoint params
     */
    public boolean startEndpoint(int port, String name, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
        InboundHttpConfiguration config = buildConfiguration(port, name, params);

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
            dataStore.registerListeningEndpoint(port, tenantDomain, InboundHttpConstants.HTTP, name, null);
            boolean start = startListener(port, name, params);

            if (start) {
                if (config.getCoresize() != null && config.getMaxSize() != null && config.getKeepAlive() != null
                        && config.getQueueLength() != null) {
                    WorkerPoolConfiguration workerPoolConfiguration = new WorkerPoolConfiguration(
                            config.getCoresize(),
                            config.getMaxSize(),
                            config.getKeepAlive(),
                            config.getQueueLength(),
                            config.getThreadGroup(),
                            config.getThreadID());

                    addWorkerPool(tenantDomain, port, workerPoolConfiguration);
                }
                if (config.getDispatchPattern() != null) {
                    Pattern pattern = compilePattern(config.getDispatchPattern());
                    addDispatchPattern(tenantDomain, port, pattern);
                }
            } else {
                dataStore.unregisterListeningEndpoint(port, tenantDomain);
                return false;
            }
        }
        return true;

    }


    /**
     * Start Https Inbound endpoint in a particular port
     * @param port  port
     * @param name  endpoint name
     */
    public boolean startSSLEndpoint(int port , String name, SSLConfiguration sslConfiguration, InboundProcessorParams params) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
        InboundHttpConfiguration config = buildConfiguration(port, name, params);

        String epName = dataStore.getListeningEndpointName(port, tenantDomain);

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
                dataStore.registerSSLListeningEndpoint(port, tenantDomain, InboundHttpConstants.HTTPS, name, sslConfiguration);
            }
            boolean start = startSSLListener(port, name, sslConfiguration, params);
            if (start) {
                if (config.getCoresize() == null && config.getMaxSize() == null && config.getKeepAlive() == null
                        && config.getQueueLength() == null) {
                    WorkerPoolConfiguration workerPoolConfiguration = new WorkerPoolConfiguration(config.getCoresize(),
                            config.getMaxSize(),
                            config.getKeepAlive(),
                            config.getQueueLength(),
                            config.getThreadGroup(),
                            config.getThreadID());
                    addWorkerPool(tenantDomain, port, workerPoolConfiguration);
                }
                if (config.getDispatchPattern() != null) {
                    Pattern pattern = compilePattern(config.getDispatchPattern());
                    addDispatchPattern(tenantDomain, port, pattern);
                }
            } else {
                dataStore.unregisterListeningEndpoint(port, tenantDomain);
                return false;
            }
        }
        return true;
    }


    /**
     * Start Http Listener in a particular port
     * @param port  port
     * @param name  endpoint name
     * @param params inbound endpoint params
     */
    public boolean startListener(int port, String name, InboundProcessorParams params) {
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
    public boolean startSSLListener(int port, String name, SSLConfiguration sslConfiguration, InboundProcessorParams params) {
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
        dataStore.unregisterListeningEndpoint(port, tenantDomain);
        removeWorkerPoolConfiguration(tenantDomain, port);
        removeDispatchPattern(tenantDomain, port);

        if (!PassThroughInboundEndpointHandler.isEndpointRunning(port)) {
            log.info("Listener Endpoint is not started");
            return ;
        } else if (dataStore.isEndpointRegistryEmpty(port)) {
            // if no other endpoint is working on this port. close the http listening endpoint
            PassThroughInboundEndpointHandler.closeEndpoint(port);
        }

    }

    public InboundHttpConfiguration buildConfiguration(int port, String name, InboundProcessorParams params) {
        return new InboundHttpConfiguration.InboundHttpConfigurationBuilder(port, name)
                .workerPoolCoreSize(params.getProperties().getProperty(
                        InboundHttpConstants.INBOUND_WORKER_POOL_SIZE_CORE))
                .workerPoolMaxSize(params.getProperties().getProperty(
                        InboundHttpConstants.INBOUND_WORKER_POOL_SIZE_MAX))
                .workerPoolKeepAlive(params.getProperties().getProperty(
                        InboundHttpConstants.INBOUND_WORKER_THREAD_KEEP_ALIVE_SEC))
                .workerPoolQueueLength(params.getProperties().getProperty(
                        InboundHttpConstants.INBOUND_WORKER_POOL_QUEUE_LENGTH))
                .workerPoolThreadGroup(params.getProperties().getProperty(
                        InboundHttpConstants.INBOUND_THREAD_GROUP_ID))
                .workerPoolThreadId(params.getProperties().getProperty(
                        InboundHttpConstants.INBOUND_THREAD_ID))
                .dispatchPattern(params.getProperties().getProperty(
                        InboundHttpConstants.INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN))
                .build();
    }

    /**
     * Start Http listeners for all the Inbound Endpoints. This should be called in the
     * server startup to load all the required listeners for endpoints in all tenants
     */
    public void loadEndpointListeners() {
        Map<Integer, List<InboundEndpointInfoDTO>> tenantData = dataStore.getAllListeningEndpointData();
        for (Map.Entry tenantInfoEntry : tenantData.entrySet()) {
            int port = (Integer) tenantInfoEntry.getKey();

            InboundEndpointInfoDTO inboundEndpointInfoDTO =
                       (InboundEndpointInfoDTO) ((ArrayList) tenantInfoEntry.getValue()).get(0);

            if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTP)) {
                startListener(port, inboundEndpointInfoDTO.getEndpointName(), inboundEndpointInfoDTO.getInboundParams());
            } else if (inboundEndpointInfoDTO.getProtocol().equals(InboundHttpConstants.HTTPS)) {
                startSSLListener(port, inboundEndpointInfoDTO.getEndpointName(), inboundEndpointInfoDTO.getSslConfiguration(),
                        inboundEndpointInfoDTO.getInboundParams());
            }

        }
    }

    /**
     *Method for add worker pool configs
     * @param tenantDomain
     * @param port
     * @param workerPoolConfiguration
     */
    public void addWorkerPool(String tenantDomain,int port ,WorkerPoolConfiguration workerPoolConfiguration){
        ConcurrentHashMap concurrentHashMap = workerPoolMap.get(tenantDomain);
        if(concurrentHashMap == null){
            concurrentHashMap = new ConcurrentHashMap<Integer, WorkerPoolConfiguration>();
            concurrentHashMap.put(port,workerPoolConfiguration);
            workerPoolMap.put(tenantDomain,concurrentHashMap);
        } else {
            concurrentHashMap.put(port,workerPoolConfiguration);
        }
    }

    /**
     * Method for get WorkerPool Config
     * @param tenantDomain
     * @param port
     * @return
     */
    public WorkerPoolConfiguration getWorkerPoolConfiguration(String tenantDomain, int port){
        ConcurrentHashMap concurrentHashMap = workerPoolMap.get(tenantDomain);
        if(concurrentHashMap != null){
          Object val = concurrentHashMap.get(port);
            if(val instanceof WorkerPoolConfiguration){
                return (WorkerPoolConfiguration)val;
            }
        }
        return  null;
    }

    /**
     * Remove Worker Pool
     * @param tenantDomian Tenant Domain
     * @param port Port
     */
    public void removeWorkerPoolConfiguration(String tenantDomian, int port ){
        ConcurrentHashMap concurrentHashMap = workerPoolMap.get(tenantDomian);
        if(concurrentHashMap != null){
           if(concurrentHashMap.containsKey(port)){
               concurrentHashMap.remove(port);
           }
        }
    }

    /**
     * Adds a dispatch pattern to pattern map.
     * @param tenantDomain
     * @param port
     */
    public void addDispatchPattern(String tenantDomain, int port, Pattern pattern) {
        ConcurrentHashMap concurrentHashMap = dispatchPatternMap.get(tenantDomain);
        if(concurrentHashMap == null){
            concurrentHashMap = new ConcurrentHashMap<Integer, Pattern>();
            concurrentHashMap.put(port, pattern);
            dispatchPatternMap.put(tenantDomain, concurrentHashMap);
        } else {
            concurrentHashMap.put(port, pattern);
        }
    }

    /**
     * Removes a dispatch pattern from pattern map.
     * @param tenantDomain
     * @param port
     */
    public void removeDispatchPattern(String tenantDomain, int port) {
        ConcurrentHashMap concurrentHashMap = dispatchPatternMap.get(tenantDomain);
        if(concurrentHashMap != null){
            if(concurrentHashMap.containsKey(port)){
                concurrentHashMap.remove(port);
            }
        }
    }

    /**
     * Method to get pattern for tenant and port.
     * @param tenantDomain
     * @param port
     * @return
     */
    public Pattern getPattern(String tenantDomain, int port){
        ConcurrentHashMap concurrentHashMap = dispatchPatternMap.get(tenantDomain);
        if(concurrentHashMap != null){
            Object val = concurrentHashMap.get(port);
            if(val instanceof Pattern){
                return (Pattern) val;
            }
        }
        return  null;
    }

    protected Pattern compilePattern(String dispatchPattern) {
        try {
            return Pattern.compile(dispatchPattern, Pattern.COMMENTS | Pattern.DOTALL);
        } catch (PatternSyntaxException e) {
            log.error("Dispatch pattern " + dispatchPattern + " is an invalid pattern.");
            throw new SynapseException(e);
        }
    }

}
