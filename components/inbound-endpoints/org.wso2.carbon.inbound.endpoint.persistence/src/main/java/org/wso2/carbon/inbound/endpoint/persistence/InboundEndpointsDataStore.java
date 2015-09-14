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

package org.wso2.carbon.inbound.endpoint.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.log4j.Logger;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.transport.passthru.core.ssl.SSLConfiguration;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InboundEndpointsDataStore {

    private static final Logger log = Logger.getLogger(InboundEndpointsDataStore.class);

    private Map<Integer,List<InboundEndpointInfoDTO>> endpointListeningInfo;
    //Store polling endpoints with <TenantId<Endpoint_Name>> format
    private Map<String,Set<String>> endpointPollingInfo;
    private Registry registry = null;
    private final String rootPath = RegistryResources.ROOT + "esb/inbound/inbound-endpoints/";

    private static InboundEndpointsDataStore instance = new InboundEndpointsDataStore();

    public static InboundEndpointsDataStore getInstance() {
        return instance;
    }

    private InboundEndpointsDataStore() {
        try {
            registry = ServiceReferenceHolder.getInstance().getRegistry();
        } catch (RegistryException e) {
            handleException("Error while obtaining a registry instance", e);
        }

        try {
            Resource fetchedResource = registry.get(rootPath);
            if (fetchedResource != null) {
                String fetchedData=null;
               if(fetchedResource.getContent() instanceof byte[]){
                   fetchedData = new String((byte[])fetchedResource.getContent());
               };
                OMElement fetchedOM = null;
                try {
                    fetchedOM = AXIOMUtil.stringToOM(fetchedData);
                } catch (XMLStreamException e) {
                    handleException("Error while converting fetched registry data to a OM", e);
                }
                endpointListeningInfo = PersistenceUtils.convertOMToEndpointListeningInfo(fetchedOM);
                endpointPollingInfo = PersistenceUtils.convertOMToEndpointPollingInfo(fetchedOM);
            }
        } catch (ResourceNotFoundException ex) {
            log.info("Inbound endpoint registry data not found, so re-initializing registry data");
            initRegistryData();
        } catch (RegistryException e) {
            handleException("Error occurred while fetching inbound endpoint data from registry", e);
        }
    }

    /**
     * Initialize registry data
     */
    private void initRegistryData() {

        endpointListeningInfo = new ConcurrentHashMap<Integer, List<InboundEndpointInfoDTO>>();
        endpointPollingInfo = new ConcurrentHashMap<String, Set<String>>();
        try {
            Resource resource = registry.newResource();
            resource.setContent(PersistenceUtils.convertEndpointInfoToOM(endpointListeningInfo, endpointPollingInfo).toString());
            registry.put(rootPath, resource);
        } catch (RegistryException e) {
            handleException("Initializing registry data.Error while creating registry resource", e);
        }
    }

    /**
     * Register endpoint in the InboundEndpointsDataStore
     *
     * @param port         listener port
     * @param tenantDomain tenant domain
     * @param protocol     protocol
     * @param name         endpoint name
     */
    public void registerListeningEndpoint(int port, String tenantDomain, String protocol, String name, InboundProcessorParams params) {

        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.get(port);
        if (tenantList == null) {
            // If there is no existing listeners in the port, create a new list
            tenantList = new ArrayList<InboundEndpointInfoDTO>();
            endpointListeningInfo.put(port, tenantList);
        }
        tenantList.add(new InboundEndpointInfoDTO(tenantDomain, protocol, name, params));
        updateRegistry();
    }

    /**
     * Register endpoint in the InboundEndpointsDataStore
     *
     * @param tenantDomain tenant domain
     * @param name         endpoint name
     */
    public void registerPollingingEndpoint(String tenantDomain, String name) {

        Set<String> lNames = endpointPollingInfo.get(tenantDomain);
        if (lNames == null) {
      	   lNames = new HashSet<String>();           
        }
        lNames.add(name);
        endpointPollingInfo.put(tenantDomain, lNames);
        updateRegistry();
    }
    
    /**
     * Register SSL endpoint in the InboundEndpointsDataStore
     *
     * @param port         listener port
     * @param tenantDomain tenant domain
     * @param protocol     protocol
     * @param name         endpoint name
     */
    public void registerSSLListeningEndpoint(int port, String tenantDomain, String protocol, String name,
                                             SSLConfiguration sslConfiguration, InboundProcessorParams params) {

        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.get(port);
        if (tenantList == null) {
            // If there is no existing listeners in the port, create a new list
            tenantList = new ArrayList<InboundEndpointInfoDTO>();
            endpointListeningInfo.put(port, tenantList);
        }
        InboundEndpointInfoDTO inboundEndpointInfoDTO = new InboundEndpointInfoDTO(tenantDomain, protocol, name, params);
        inboundEndpointInfoDTO.setSslConfiguration(sslConfiguration);
        tenantList.add(inboundEndpointInfoDTO);

        updateRegistry();
    }

    /**
     * Get endpoint name for given port and domain
     *
     * @param port         port
     * @param tenantDomain tenant domain
     * @return endpoint name
     */
    public String getListeningEndpointName(int port, String tenantDomain) {
        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.get(port);
        if (tenantList != null) {
            for (InboundEndpointInfoDTO tenantInfo : tenantList) {
                if (tenantInfo.getTenantDomain().equals(tenantDomain)) {
                    return tenantInfo.getEndpointName();
                }
            }
        }
        return null;
    }

    /**
     * Unregister an endpoint from data store
     *
     * @param port         port
     * @param tenantDomain tenant domain name
     */
    public void unregisterListeningEndpoint(int port, String tenantDomain) {
        List<InboundEndpointInfoDTO> tenantList = endpointListeningInfo.get(port);
        if (tenantList != null) {
            for (InboundEndpointInfoDTO tenantInfo : tenantList) {
                if (tenantInfo.getTenantDomain().equals(tenantDomain)) {
                    tenantList.remove(tenantInfo);
                    break;
                }
            }
        }
        if (endpointListeningInfo.get(port) != null && endpointListeningInfo.get(port).size() == 0) {
      	  endpointListeningInfo.remove(port);
        }
        updateRegistry();
    }

    /**
     * Unregister an endpoint from data store
     *
     * @param tenantId        
     * @param name 
     */
    public void unregisterPollingEndpoint(String tenantDomain, String name) {
        Set<String> lNames = endpointPollingInfo.get(tenantDomain);
        if (lNames != null && !lNames.isEmpty()) {
            for (String strName : lNames) {
                if (strName.equals(name)) {
               	  lNames.remove(strName);
                    break;
                }
            }
            if(lNames.isEmpty()){
            	endpointPollingInfo.remove(tenantDomain);
            }
        }        
        updateRegistry();
    }    

    /**
     * Check polling endpoint from data store
     *
     * @param tenantDomain      
     * @param name 
     */
    public boolean isPollingEndpointRegistered(String tenantDomain, String name) {
        Set<String> lNames = endpointPollingInfo.get(tenantDomain);
        if (lNames != null && !lNames.isEmpty()) {
            for (String strName : lNames) {
                if (strName.equals(name)) {
               	  return true;
                }
            }
        }        
        return false;
    }   
    
    /**
     * Check whether endpoint registry is empty for a particular port
     *
     * @param port port
     * @return whether no endpoint is registered for a port
     */
    public boolean isEndpointRegistryEmpty(int port) {
        return endpointListeningInfo.get(port) == null;
    }

    /**
     * Get details of all endpoints
     *
     * @return information of all endpoints
     */
    public  Map<Integer,List<InboundEndpointInfoDTO>> getAllListeningEndpointData() {
        return endpointListeningInfo;
    }

    /**
     * Get details of all polling endpoints
     *
     * @return information of all polling endpoints
     */
    public  Map<String,Set<String>> getAllPollingingEndpointData() {
        return endpointPollingInfo;
    }
    
    /**
     * Synchronize in memory endpoint data with registry
     */
    private synchronized void updateRegistry() {
        OMElement dataOM = PersistenceUtils.convertEndpointInfoToOM(endpointListeningInfo, endpointPollingInfo);
        try {
            Resource resource = registry.get(rootPath);
            resource.setContent(dataOM.toString());
            registry.put(rootPath, resource);
        } catch (RegistryException e) {
            handleException("Exception occurred while updating registry data", e);
        }
    }

    private void handleException(String msg, Exception ex) {
        //TODO: check whether we need more error handling here
        log.error(msg, ex);
    }


}
