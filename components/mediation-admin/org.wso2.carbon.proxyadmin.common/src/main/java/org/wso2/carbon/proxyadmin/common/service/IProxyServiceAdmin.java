/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.proxyadmin.common.service;

import org.wso2.carbon.proxyadmin.common.MetaData;
import org.wso2.carbon.proxyadmin.common.ProxyAdminException;
import org.wso2.carbon.proxyadmin.common.ProxyData;



/**
 * The class <code>IProxyServiceAdmin</code> provides the administration service to configure
 * proxy services.
 */
public interface IProxyServiceAdmin {

    /**
     * Enables statistics for the specified proxy service
     *
     * @param proxyName name of the proxy service name of which the statistics need to be enabled
     * @throws ProxyAdminException in case of a failure in enabling statistics
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String enableStatistics(String proxyName) throws ProxyAdminException;

    /**
     * Disables statistics for the specified proxy servivce
     *
     * @param proxyName name of the proxy service of which statistics need to be disabled
     * @throws ProxyAdminException in case of a failure in disabling statistics
     * @return <code>successful</code> on success or <code>failed</code> if unsuccessful
     */
    public String disableStatistics(String proxyName) throws ProxyAdminException;
    
    /**
     * Enables tracing for the specified proxy service
     *
     * @param proxyName name of the the proxy service of which tracing needs to be enabled
     * @throws ProxyAdminException in case of a failure in enabling tracing
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String enableTracing(String proxyName) throws ProxyAdminException;

    /**
     * Disables tracing for the specified proxy service
     *
     * @param proxyName name of the proxy service of which tracing needs to be disabled
     * @throws ProxyAdminException in case of a failure in disabling tracing
     * @return SUCCESSFUL is the operation is successful and FAILED if it is failed
     */
    public String disableTracing(String proxyName) throws ProxyAdminException;

    /**
     * Deletes a proxy service from the synapse configuration
     *
     * @param proxyName name of the proxy service which needs to be deleted
     * @throws ProxyAdminException if the proxy service name given is not existent in the
     *                   synapse configuration
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String deleteProxyService(String proxyName) throws ProxyAdminException;

    /**
     * Get the available transport names from the AxisConfiguration
     *
     * @return String array of available transport names
     */
    public String[] getAvailableTransports() throws ProxyAdminException;
    
    /**
     * Get the available sequences from the SynapseConfiguration
     *
     * @return String array of available sequence names
     * @throws ProxyAdminException if there is an error
     */
    public String[] getAvailableSequences() throws ProxyAdminException;
    
    /**
     * Get the available endpoints from the SynapseConfiguration
     *
     * @return String array of available endpoint names
     * @throws ProxyAdminException if there is an error
     */
    public String[] getAvailableEndpoints() throws ProxyAdminException;

    /**
     * Gets the endpoint object defined under the given name
     *
     * @param name the name of the endpoint
     * @return endpoint configuration related with the name
     * @throws ProxyAdminException if the endpoint is not found for the given name
     */
    public String getEndpoint(String name) throws ProxyAdminException;
    
    /**
     * Encapsulates the available transports, endpoints, and sequences into a single two dimensional array
     * @return  A two dimensional array containing the set of transports, endpoints, and sequences
     * under 0,1, and 2 indices.
     * @throws ProxyAdminException
     */
    public MetaData getMetaData() throws ProxyAdminException;

    /**
     * Starts the service specified by the name
     *
     * @param proxyName name of the proxy service which needs to be started
     * @throws ProxyAdminException incase of a failure in starting the service
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String startProxyService(String proxyName) throws ProxyAdminException;

    /**
     * Stops the service specified by the name
     *
     * @param proxyName name of the proxy service which needs to be stoped
     * @throws ProxyAdminException in case of a failure in stopping the service
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String stopProxyService(String proxyName) throws ProxyAdminException;
    
    /**
     * Redeploying service
     * Removes an existing one,Adds a new one
     *
     * @param proxyName name of the proxy service which needs to be redeployed
     * @throws ProxyAdminException in case of a failure in redeploying the service
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String redeployProxyService(String proxyName) throws ProxyAdminException;
    public String getSourceView(ProxyData pd) throws ProxyAdminException;

    public ProxyData getProxy(String proxyName) throws ProxyAdminException;

    public String addProxy(ProxyData pd) throws ProxyAdminException;

    public String modifyProxy(ProxyData pd) throws ProxyAdminException;

}
