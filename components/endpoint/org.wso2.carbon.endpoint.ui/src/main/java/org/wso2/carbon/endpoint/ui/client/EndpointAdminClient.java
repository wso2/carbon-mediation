/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.endpoint.ui.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.carbon.endpoint.stub.types.common.ConfigurationObject;
import org.wso2.carbon.endpoint.stub.types.service.EndpointMetaData;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointService;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointStore;
import org.wso2.carbon.sequences.common.to.SequenceInfo;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointAdminClient {

    public static final int ENDPOINT_PER_PAGE = 10;
    private EndpointAdminStub stub;
    private static final Log log = LogFactory.getLog(EndpointAdminClient.class);

    public EndpointAdminClient(String cookie,
                               String backendServerURL,
                               ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "EndpointAdmin";
        stub = new EndpointAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Add an Endpoint described by the given configuration
     *
     * @param endpointString - configuration representing the endpoint that needs
     *                 to be added
     * @return true if the endpoint was successfully added and false otherwise
     * @throws Exception in case of an error
     */
    public void addEndpoint(String endpointString) throws Exception {
        try {
            stub.addEndpoint(endpointString);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Set Endpoint status to Active
     *
     * @param name name of the endpoint
     * @throws Exception in case of an error
     */
    public void switchOn(String name) throws Exception {
        try {
            stub.switchOn(name);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Switch off Endpoint given by the name
     *
     * @param name name of the endpoint
     * @throws Exception in case of an error
     */
    public void switchOff(String name) throws Exception {
        try {
            stub.switchOff(name);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Delete an endpoint from the synapse configuration
     *
     * @param epName endpoint name
     * @throws Exception in case of an error
     */
    public void deleteEndpoint(String epName) throws Exception {
        try {
            stub.deleteEndpoint(epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }
    /**
     * Delete selected endpoints from synapse configuration
     * @param epNames
     * @throws Exception
     */
    public void deleteSelectedEndpoints(String[] epNames) throws Exception{
        try {
            stub.deleteSelectedEndpoint(epNames);
        }catch (Exception e){
            handleFault(e);
        }
    }
    /**
     *  Delete all endpoints in the synapse configuration
     * @throws Exception
     */
    public void deleteAllEndpointGroups() throws Exception{
        try {
            stub.deleteAllEndpointGroups();
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Delete an endpoint from the registry
     *
     * @param key key of the dynamic endpoint
     * @throws Exception in case of an error
     */
    public void deleteDynamicEndpoint(String key) throws Exception {
        try {
            stub.deleteDynamicEndpoint(key);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Get dependents of a particular endpoint.
     *
     * @param epName endpoint name
     * @return dependants of a endpoints
     */
    public ConfigurationObject[] getDependents(String epName) throws Exception {
        try {
            ConfigurationObject[] dependents = stub.getDependents(epName);
            if (dependents != null && dependents.length > 0 && dependents[0] != null) {
                return dependents;
            }
        } catch (RemoteException e) {
            handleFault(e);
        }
        return null;
    }

    /**
     * Update an existing dynamic endpoint
     *
     * @param key dynamic endpoint key
     * @param epConfiguration new endpoint configuration
     * @throws Exception in case of an error
     */
    public void updateDynamicEndpoint(String key, String epConfiguration) throws Exception {
        try {
            stub.deleteDynamicEndpoint(key);
            stub.addDynamicEndpoint(key, epConfiguration);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Gets the endpoint element as a string
     *
     * @param epName - name of the endpoint
     * @return String representing the endpoint with the given endpoint name
     * @throws Exception in case of an error
     */
    public String getEndpoint(String epName) throws Exception {
        try {
            return stub.getEndpointConfiguration(epName);
        } catch (Exception e) {
            handleFault(e);
        }
        return null;
    }

    /**
     * Get all endpoint configurations from the synapse configuration
     *
     * @return a list of all the endpoints
     * @throws Exception in case of an error
     */
    public String[] getEndpoints() throws Exception {
        try {
            return stub.getEndpoints();
        } catch (Exception e) {
            handleFault(e);
        }
        return null;
    }

    /**
     * Get a dynamic endpoint from the registry
     *
     * @param key dynamic endpoint key
     * @return dynamic endpoint configuration
     * @throws Exception on an error
     */
    public String getDynamicEndpoint(String key) throws Exception {
        String data = null;
        try {
            data = stub.getDynamicEndpoint(key);
        } catch (Exception e) {
            handleFault(e);
        }
        return data;
    }

    /**
     * Get all endpoints stored in the registry
     *
     * @param pageNumber page number
     * @param endpointsPerPage no of endpoints per page
     * @return endpoints in the registry
     * @throws Exception in case of an error
     */
    public String[] getDynamicEndpoints(int pageNumber, int endpointsPerPage) throws Exception {
        String[] result = null;
        try {
            String[] allDynamicEndpoints = stub.getDynamicEndpoints();
            if (allDynamicEndpoints != null) {
                if (allDynamicEndpoints.length >= (endpointsPerPage * pageNumber + endpointsPerPage)) {
                    result = new String[endpointsPerPage];
                } else if ((allDynamicEndpoints.length - (endpointsPerPage * pageNumber)) > 0) {
                    result = new String[allDynamicEndpoints.length - (endpointsPerPage * pageNumber)];
                } else {
                    pageNumber--;
                    result = new String[endpointsPerPage-1];
                }
                for (int i = 0; i < endpointsPerPage; ++i) {
                    if (result.length > i) {
                        result[i] = allDynamicEndpoints[endpointsPerPage * pageNumber + i];
                    }
                }
            }
        } catch (Exception e) {
            handleFault(e);
        }
        return result;
    }

    /**
     * Get Metadata of all the Searched Dynamic Endpoints in the Synapse configuration
     * @param pageNumber
     * @param endpointsPerPage
     * @param searchText
     * @return
     * @throws Exception
     */
    public String[] getDynamicEndpointsSearch(int pageNumber, int endpointsPerPage, String searchText) throws Exception {
        String[] result = null;
        try {
            String[] allDynamicEndpoints = stub.getDynamicEndpoints();
            if (allDynamicEndpoints != null) {
                if (allDynamicEndpoints.length >= (endpointsPerPage * pageNumber + endpointsPerPage)) {
                    result = new String[endpointsPerPage];
                } else if ((allDynamicEndpoints.length - (endpointsPerPage * pageNumber)) > 0) {
                    result = new String[allDynamicEndpoints.length - (endpointsPerPage * pageNumber)];
                } else {
                    pageNumber--;
                    result = new String[endpointsPerPage-1];
                }
                for (int i = 0; i < endpointsPerPage; ++i) {
                    if (result.length > i) {
                        result[i] = allDynamicEndpoints[endpointsPerPage * pageNumber + i];
                    }
                }
            }
        } catch (Exception e) {
            handleFault(e);
        }

        List<String> DynamicEndPoints = new ArrayList<String>();

        for (String  endpointInfoString : result ) {
            if (this.isServiceSatisfySearchString(searchText, endpointInfoString)) {
                DynamicEndPoints.add(endpointInfoString);
            }
        }
        if (DynamicEndPoints.size() > 0) {
            return DynamicEndPoints.toArray(new String[DynamicEndPoints.size()]);
        }
        return  null;
    }

    /**
     * Get the endpoint service for the given endpoint metadata
     *
     * @param metaData Metadata of an endpoint
     * @return endpoint service for the given endpoint metadata
     * @throws XMLStreamException in case of an error
     */
    public EndpointService getEndpointService(EndpointMetaData metaData)
            throws XMLStreamException {
        String endpointString = metaData.getEndpointString();
        OMElement endpointElement = AXIOMUtil.stringToOM(endpointString);
        EndpointService endpointService = EndpointStore.getInstance().getEndpointService(endpointElement);
        return endpointService;
    }

    /**
     * Get the endpoint service for the given endpoint configuration
     *
     * @param endpointString configuration of the endpoint
     * @return endpoint service for the given endpoint configuration
     * @throws XMLStreamException in case of an error
     */
    public EndpointService getEndpointService(String endpointString)
            throws XMLStreamException {
        OMElement endpointElement = AXIOMUtil.stringToOM(endpointString);
        EndpointService endpointService = EndpointStore.getInstance().getEndpointService(endpointElement);
        return endpointService;
    }

    /**
     * Get Metadata of all the Endpoints in the Synapse configuration
     *
     * @param pageNumber page number
     * @param endpointsPerPage no of endpoints per page
     * @return EndpointMetaData
     * @throws Exception in case of an error
     */
    public EndpointMetaData[] getEndpointMetaData(int pageNumber, int endpointsPerPage) throws Exception {
        EndpointMetaData[] result = null;
        try {
            EndpointMetaData[] allEndpointMetaData = stub.getEndpointsData();
            if (allEndpointMetaData != null) {
                if (allEndpointMetaData.length >= (endpointsPerPage * pageNumber + endpointsPerPage)) {
                    result = new EndpointMetaData[endpointsPerPage];
                } else if ((allEndpointMetaData.length - (endpointsPerPage * pageNumber)) > 0){
                    result = new EndpointMetaData[allEndpointMetaData.length - (endpointsPerPage * pageNumber)];
                } else {
                    pageNumber--;
                    result = new EndpointMetaData[endpointsPerPage-1];
                }
                for (int i = 0; i < endpointsPerPage; ++i) {
                    if (result.length > i) {
                        result[i] = allEndpointMetaData[endpointsPerPage * pageNumber + i];
                    }
                }
            }
        } catch (Exception e) {
            handleFault(e);
        }
        return result;
    }

    /**
     *  Method for match the pattern for endpoint Search
     * @param searchString
     * @param sequenceName
     * @return
     */
    private boolean isServiceSatisfySearchString(String searchString,String sequenceName) {
        if (searchString != null) {
            String regex = searchString.toLowerCase().
                    replace("..?", ".?").replace("..*", ".*").
                    replaceAll("\\?", ".?").replaceAll("\\*", ".*?");

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sequenceName.toLowerCase());

            return regex.trim().length() == 0 || matcher.find();
        }
        return false;
    }

    /**
     * Get Metadata of all the Searched Endpoints in the Synapse configuration
     * @param pageNumber
     * @param endpointsPerPage
     * @param searchText
     * @return
     * @throws Exception
     */
    public EndpointMetaData[] getEndpointMetaDataSearch(int pageNumber, int endpointsPerPage, String searchText) throws Exception {
        EndpointMetaData[] result = null;
        try {
            EndpointMetaData[] allEndpointMetaData = stub.getEndpointsData();
            if (allEndpointMetaData != null) {
               result =  allEndpointMetaData;
            }
        } catch (Exception e) {
            handleFault(e);
        }

        if (result == null || result.length == 0 || result[0] == null) {
               return null;
        }

        List<EndpointMetaData> endPoints = new ArrayList<EndpointMetaData>();

        for (EndpointMetaData endpointInfo : result ) {
            if (this.isServiceSatisfySearchString(searchText, endpointInfo.getName())) {
                endPoints.add(endpointInfo);
            }
        }
        if (endPoints.size() > 0) {
           return endPoints.toArray(new EndpointMetaData[endPoints.size()]);
        }

        return  null;
    }

    /**
     * Enable statistics collection for the specified endpoint
     *
     * @param name name of the endpoint
     * @throws Exception in case of an error
     */
    public void enableStatistics(String name) throws Exception {
        try {
            stub.enableStatistics(name);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Stop collecting statistics for a specified endpoint
     *
     * @param name name of the endpoint
     * @throws Exception in case of an error
     */
    public void disableStatistics(String name) throws Exception {
        try {
            stub.disableStatistics(name);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Add an endpoint to the Synapse registry
     *
     * @param key of the dynamic endpoint
     * @param epName endpoint name
     * @throws Exception in case of an error
     */
    public void addDynamicEndpoint(String key, String epName) throws Exception {
        try {
            if (!stub.addDynamicEndpoint(key, epName)) {
                throw new Exception("Endpoint \'" + key + "\' is already found in Registry");
            }
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Save an endpoint to the Synapse registry
     *
     * @param key    dynamic endpoint key
     * @param epName endpoint name
     * @throws Exception in case of an error
     */
    public void saveDynamicEndpoint(String key, String epName) throws Exception {
        try {
            stub.saveDynamicEndpoint(key, epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Exception handler
     *
     * @param e exception
     * @throws Exception in case of an error
     */
    private void handleFault(Exception e) throws Exception {
        log.error(e.getMessage(), e);
        throw e;
    }

    /**
     * Save an endpoint
     *
     * @param epName endpoint name
     * @throws Exception in case of an error
     */
    public void saveEndpoint(String epName) throws Exception {
        try {
            stub.saveEndpoint(epName);
        } catch (Exception e) {
            handleFault(e);
        }
    }

    /**
     * Get the endpoint count
     *
     * @return number of endpoints
     * @throws Exception
     */
    public int getEndpointCount() throws Exception {
        try {
            return stub.getEndpointCount();
        } catch (Exception e) {
            handleFault(e);
        }
        return 0;
    }

    /**
     * Get the dynamic endpoint count
     *
     * @return number of dynamic endpoints
     * @throws Exception
     */
    public int getDynamicEndpointCount() throws Exception {
        try {
            return stub.getDynamicEndpointCount();
        } catch (Exception e) {
            handleFault(e);
        }
        return 0;
    }

    /**
     * check the registry
     * @return true if register is null
     * @throws Exception
     */
    public boolean isRegisterNull() throws Exception {

        try {
            return stub.isRegisterNull();

        } catch (Exception e) {
            handleFault(e);
        }
        return false;

    }

}
