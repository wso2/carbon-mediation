/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.rest.api.service;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.rest.APIFactory;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.rest.api.APIData;
import org.wso2.carbon.rest.api.APIException;
import org.wso2.carbon.rest.api.ConfigHolder;
import org.wso2.carbon.rest.api.RestApiAdminUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RestApiAdminService {

    private static final Log log = LogFactory.getLog(RestApiAdminService.class);
    private static final String TENANT_DELIMITER = "/t/";

    public boolean addApi(APIData apiData) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            if (tenantDomain != null && !tenantDomain.isEmpty()
                    && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                String tenantApiContext = apiData.getContext();
                apiData.setContext(TENANT_DELIMITER + tenantDomain + tenantApiContext);
            }
            addApi(RestApiAdminUtils.retrieveAPIOMElement(apiData), null, false);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean addApiFromString(String apiData) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            OMElement apiElement = AXIOMUtil.stringToOM(apiData);
            addApi(apiElement, null, false);
            return true;
        } catch (XMLStreamException e) {
            handleException(log, "Could not parse String to OMElement", e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set the tenant domain when a publisher publishes his API in MT mode. When publisher publishes
     * the API, we login the gateway as super tenant. But we need to publish the API in the particular
     * tenant domain.
     *
     * @param apiData
     * @param tenantDomain
     * @return
     * @throws APIException
     */
    public boolean addApiForTenant(String apiData, String tenantDomain) throws APIException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
                    true);
            return addApiFromString(apiData);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Set the tenant domain when a publisher tries to retrieve API his API in MT mode. When
     * publisher gets
     * the API, we login the gateway as super tenant. But we need to get the
     * API,which is in the particular tenant domain.
     *
     * @param apiName
     * @param tenantDomain
     * @return
     * @throws APIException
     */
    public APIData getApiForTenant(String apiName, String tenantDomain) throws APIException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
                    true);
            return getApiByName(apiName);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public APIData getApiByName(String apiName) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = RestApiAdminUtils.getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(apiName);
            return RestApiAdminUtils.convertApiToAPIData(api);
        } finally {
            lock.unlock();
        }
    }

    public boolean updateApiFromString(String apiName, String apiData) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(apiName);

            OMElement apiElement = AXIOMUtil.stringToOM(apiData);

            //Set API name to old value since we do not allow editing the API name.
            OMAttribute nameAttribute = apiElement.getAttribute(new QName("name"));
            if (nameAttribute == null || "".equals(nameAttribute.getAttributeValue().trim())) {
                apiElement.addAttribute("name", apiName, null);
            }

            API oldAPI = null;
            API api = APIFactory.createAPI(apiElement);

            SynapseConfiguration synapseConfiguration = RestApiAdminUtils.getSynapseConfiguration();

            oldAPI = synapseConfiguration.getAPI(apiName);
            if (oldAPI != null) {
                oldAPI.destroy();
                api.setFileName(oldAPI.getFileName());
            }

            synapseConfiguration.removeAPI(apiName);
            synapseConfiguration.addAPI(api.getName(), api);
            api.init(RestApiAdminUtils.getSynapseEnvironment());

            if ((oldAPI != null ? oldAPI.getArtifactContainerName() : null) != null) {
                api.setArtifactContainerName(oldAPI.getArtifactContainerName());
                api.setIsEdited(true);
                getApiByName(apiName).setIsEdited(true);
            } else {
                MediationPersistenceManager pm = RestApiAdminUtils.getMediationPersistenceManager();
                String fileName = api.getFileName();
                pm.deleteItem(apiName, fileName, ServiceBusConstants.ITEM_TYPE_REST_API);
                pm.saveItem(apiName, ServiceBusConstants.ITEM_TYPE_REST_API);
            }

            return true;
        } catch (XMLStreamException e) {
            handleException(log, "Could not parse String to OMElement", e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Set the tenant domain when a publisher updates his API in MT mode. When
     * publisher updates the API, we login the gateway as super tenant. But we need to update the
     * API,which is in the particular tenant domain.
     *
     * @param apiName
     * @param apiData
     * @return
     * @throws APIException
     */
    public boolean updateApiForTenant(String apiName, String apiData, String tenantDomain) throws APIException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
                    true);
            return updateApiFromString(apiName, apiData);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public boolean deleteApi(String apiName) throws APIException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(apiName);
            apiName = apiName.trim();

            SynapseConfiguration synapseConfiguration = RestApiAdminUtils.getSynapseConfiguration();
            API api = synapseConfiguration.getAPI(apiName);

            if (api.getArtifactContainerName() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting API : " + apiName + " from the configuration");
                }
                api.destroy();
                synapseConfiguration.removeAPI(apiName);

                MediationPersistenceManager pm = RestApiAdminUtils.getMediationPersistenceManager();
                String fileName = api.getFileName();
                pm.deleteItem(apiName, fileName, ServiceBusConstants.ITEM_TYPE_REST_API);

                if (log.isDebugEnabled()) {
                    log.debug("Api : " + apiName + " removed from the configuration");
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    private void handleException(Log log, String message, Exception e) throws APIException {
        if (e == null) {
            APIException apiException = new APIException(message);
            log.error(message, apiException);
            throw apiException;
        } else {
            message = message + " :: " + e.getMessage();
            log.error(message, e);
            throw new APIException(message, e);
        }
    }

    protected Lock getLock() throws APIException {
        AxisConfiguration axisConfig = ConfigHolder.getInstance().getAxisConfiguration();
        Parameter p = axisConfig.getParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
        if (p != null) {
            return (Lock) p.getValue();
        } else {
            log.warn(ServiceBusConstants.SYNAPSE_CONFIG_LOCK + " is null, Recreating a new lock");
            Lock lock = new ReentrantLock();
            try {
                axisConfig.addParameter(ServiceBusConstants.SYNAPSE_CONFIG_LOCK, lock);
                return lock;
            } catch (AxisFault axisFault) {
                log.error("Error while setting " + ServiceBusConstants.SYNAPSE_CONFIG_LOCK);
            }
        }
        return null;
    }

    private void assertNameNotEmpty(String apiName) throws APIException {
        if (apiName == null || "".equals(apiName.trim())) {
            handleFault("Invalid name : Name is empty.", null);
        }
    }

    /**
     * Add an api described by the given OMElement
     *
     * @param apiElement configuration of the api which needs to be added
     * @param fileName   Name of the file in which this configuration should be saved or null
     * @throws APIException if the element is not an api or if an api with the
     *                      same name exists
     */
    private void addApi(OMElement apiElement,
                        String fileName, boolean updateMode) throws APIException {

        try {
            if (apiElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.API_ELT.getLocalPart())) {

                String apiName = apiElement.getAttributeValue(new QName("name"));
                String apiTransports = apiElement.getAttributeValue(new QName("transports"));

                if (RestApiAdminUtils.getSynapseConfiguration().getAxisConfiguration().getService(
                        apiName) != null) {
                    handleException(log, "A service named " + apiName + " already exists", null);
                } else {
                    API api = APIFactory.createAPI(apiElement);

                    try {
                        RestApiAdminUtils.getSynapseConfiguration().addAPI(api.getName(), api);

                        //addParameterObserver(api.getName());

                        if (log.isDebugEnabled()) {
                            log.debug("Added API : " + apiName);
                            log.debug("Authorized Transports : " + apiTransports);
                        }

                        if (apiTransports != null) {
                            if (Constants.TRANSPORT_HTTP.equalsIgnoreCase(apiTransports)) {
                                api.setProtocol(RESTConstants.PROTOCOL_HTTP_ONLY);
                            } else if (Constants.TRANSPORT_HTTPS.equalsIgnoreCase(apiTransports)) {
                                api.setProtocol(RESTConstants.PROTOCOL_HTTPS_ONLY);
                            }
                        }

                        if (updateMode) {
                            api.setFileName(fileName);
                        } else {
                            if (fileName != null) {
                                api.setFileName(fileName);
                            } else {
                                api.setFileName(ServiceBusUtils.generateFileName(api.getName()));
                            }
                        }
                        api.init(RestApiAdminUtils.getSynapseEnvironment());
                        RestApiAdminUtils.persistApi(api);

                    } catch (Exception e) {
                        api.destroy();
                        RestApiAdminUtils.getSynapseConfiguration().removeAPI(api.getName());
                        try {
                            AxisConfiguration axisConfig = ConfigHolder.getInstance().getAxisConfiguration();
                            if (axisConfig.getService(api.getName()) != null) {
                                axisConfig.removeService(api.getName());
                            }
                        } catch (Exception ignore) {
                        }
                        handleException(log, "Error trying to add the API to the ESB " +
                                "configuration : " + api.getName(), e);
                    }
                }
            } else {
                handleException(log, "Invalid API definition", null);
            }
        } catch (AxisFault af) {
            handleException(log, "Invalid API definition", af);
        }
    }

    private void handleFault(String message, Exception e) throws APIException {
        if (e != null) {
            log.error(message, e);
            throw new APIException(e.getMessage(), e);
        } else {
            log.error(message);
            throw new APIException(message);
        }
    }

}
