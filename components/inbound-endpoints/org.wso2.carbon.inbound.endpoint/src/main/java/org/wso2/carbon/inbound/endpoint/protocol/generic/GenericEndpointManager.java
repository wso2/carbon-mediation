/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.generic;

import org.apache.log4j.Logger;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.AbstractInboundEndpointManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GenericEndpointManager extends AbstractInboundEndpointManager {

    private static final Logger log = Logger.getLogger(GenericEndpointManager.class);
    protected ConcurrentHashMap<Integer, GenericInboundListener> genericInboundEndpointMap = new ConcurrentHashMap<Integer, GenericInboundListener>();
    private static ConcurrentHashMap<String, GenericEndpointManager> managerInstances = new ConcurrentHashMap<String, GenericEndpointManager>();

    /**
     * This is to return GenericEndpointManager for given set of parameters
     *
     * @param inboundParams Inbound parameters
     * @return
     */
    public static synchronized GenericEndpointManager getInstance(InboundProcessorParams inboundParams) {
        String classImpl = inboundParams.getClassImpl();
        String name = inboundParams.getName();

        if (null == classImpl) {
            String msg = "GenericEndpointManager class not found";
            log.error(msg);
            throw new SynapseException(msg);
        }

        GenericEndpointManager managerInstance = null;

        if (managerInstances.containsKey(classImpl)){
            if (log.isDebugEnabled())
                log.debug("Using already existing generic endpoint manager for " + classImpl);
            managerInstance = managerInstances.get(classImpl);
        }
        else {
            log.info("Inbound listener " + name + " for class " + classImpl + " starting ...");
            try {
                // Dynamically load GenericEndpointManager from given classpath
                Class c = Class.forName(classImpl);
                managerInstance = (GenericEndpointManager) c.newInstance();
            } catch (ClassNotFoundException e) {
                handleException("Class " + classImpl + " not found. Please check the required class is added to the classpath.", e);
            } catch (Exception e) {
                handleException("Unable to create the consumer", e);
            }

            managerInstances.put(classImpl, managerInstance);
        }

        return managerInstance;
    }

    /**
     * This must be implemented to give the respective listener implementation
     *
     * @param params Inbound Parameters
     * @return GenericInboundListener implemented object
     */
    public abstract GenericInboundListener getEndpoint(InboundProcessorParams params);

    /**
     * Starts the listener
     *
     * @param port  port Listening port number
     * @param name  endpoint name
     * @param inboundParameters Inbound endpoint parameters
     * @return boolean states the success of listener starting
     */
    @Override
    public boolean startListener(int port, String name, InboundProcessorParams inboundParameters) {
        boolean success = false;
        if (!genericInboundEndpointMap.containsKey(port)) {
            GenericInboundListener endpoint = this.getEndpoint(inboundParameters);
            success = endpoint.startListener();

            if (success) {
                genericInboundEndpointMap.put(port, endpoint);
            }
        }

        return success;
    }

    /**
     * Authorize the port availability
     * If port available ; return true
     * If port already in-use ; return false
     *
     * @param port  port
     * @param name  endpoint name
     * @param inboundParameters Inbound endpoint parameters
     * @return
     */
    @Override // TODO: name is not matching with the job
    public boolean startEndpoint(int port, String name, InboundProcessorParams inboundParameters) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = carbonContext.getTenantDomain();
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
            dataStore.registerListeningEndpoint(port, tenantDomain, GenericConstants.GENERIC_INBOUND_NAME, name, inboundParameters);
            return startListener(port, name, inboundParameters);
        }

        return false;
    }

    /**
     * Unregister listener
     *
     * @param port  port of the endpoint
     */
    @Override
    public void closeEndpoint(int port) {
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = cc.getTenantDomain();
        dataStore.unregisterListeningEndpoint(port, tenantDomain);
        genericInboundEndpointMap.remove(port);
    }

    /**
     * States whether generic endpoint is a listening
     * Return true; if listening
     *
     * @param inboundParameters Inbound Parameters for endpoint
     * @return boolean
     */
    public static boolean isListeningInboundEndpoint(InboundProcessorParams inboundParameters){
        return inboundParameters.getProperties().containsKey(GenericConstants.PARAM_INBOUND_ENDPOINT_LISTENING)
               && "true".equals(inboundParameters.getProperties().getProperty(GenericConstants.PARAM_INBOUND_ENDPOINT_LISTENING));
    }

    /**
     * Returns the Manager instance for given Manager classpath
     *
     * @param classImpl classpath of Manager class
     * @return GenericEndpointManager if already defined, else null
     */
    public static GenericEndpointManager getManagerInstance(String classImpl) {
        if (!managerInstances.containsKey(classImpl)){
            String msg = "Instance of class " + classImpl + " is not registered. Use GenericEndpointManager.getInstance() to register an instance";
            handleException(msg, new NullPointerException(msg));
        }
        return managerInstances.get(classImpl);
    }

    protected static synchronized void handleException(String msg, Exception ex) {
        log.error(msg, ex);
        throw new SynapseException(ex);
    }
}
