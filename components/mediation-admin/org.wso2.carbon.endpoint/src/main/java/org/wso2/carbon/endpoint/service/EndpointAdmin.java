package org.wso2.carbon.endpoint.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointContext;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.registry.Registry;
import org.wso2.carbon.endpoint.EndpointAdminException;
import org.wso2.carbon.endpoint.util.ConfigHolder;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;
import org.wso2.carbon.mediation.dependency.mgt.services.DependencyManagementService;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class EndpointAdmin extends AbstractServiceBusAdmin {

    private static final Log log = LogFactory.getLog(EndpointAdmin.class);
    public static final String WSO2_ENDPOINT_MEDIA_TYPE = "application/vnd.wso2.esb.endpoint";
    private static final String artifactType = ServiceBusConstants.ENDPOINT_TYPE;

    /**
     * Set Endpoint status to Active
     *
     * @param endpointName name of the endpoint
     * @throws EndpointAdminException in case of an error
     */
    public void switchOn(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);
            ep.getContext().switchOn();
            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, endpointName))) {
                persistEndpoint(ep);
            }

            if (log.isDebugEnabled()) {
                log.debug("Endpoint " + ep.getName() + " switched on");
            }

        } catch (SynapseException ex) {
            handleFault("Error switch on endpoint : " + endpointName, ex);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Switch off Endpoint
     *
     * @param endpointName name of the endpoint
     * @throws EndpointAdminException in case of an error
     */
    public void switchOff(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);
            ep.getContext().switchOff();
            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, endpointName))) {
                persistEndpoint(ep);
            }

            if (log.isDebugEnabled()) {
                log.debug("Endpoint " + ep.getName() + " switched off");
            }
        } catch (SynapseException ex) {
            handleFault("Error switch off endpoint : " + endpointName, ex);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add an Endpoint described by the given configuration
     *
     * @param epString - configuration representing the endpoint that needs
     *                 to be added
     * @return true if the endpoint was successfully added and false otherwise
     * @throws EndpointAdminException if the element is not an endpoint or if an endpoint
     *                                with the same name exists
     */
    public boolean addEndpoint(String epString) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            OMElement endpointElement;
            try {
                endpointElement = AXIOMUtil.stringToOM(epString);
            } catch (XMLStreamException e) {
                return false;
            }
            if (endpointElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.ENDPOINT_ELT.getLocalPart())) {

                String endpointName = endpointElement.getAttributeValue(new QName("name"));
                assertNameNotEmpty(endpointName);
                endpointName = endpointName.trim();
                log.debug("Adding endpoint : " + endpointName + " to the configuration");

                if (getSynapseConfiguration().getLocalRegistry()
                            .get(endpointName) != null) {
                    handleFault("The name " + endpointName +
                                " is already used within the configuration", null);
                } else {
                    SynapseConfiguration config = getSynapseConfiguration();
                    if (config.getEndpoint(endpointName) != null) {
                        handleFault("A endpoint with name "
                                    + endpointName + " is already there.", null);
                    }
                    SynapseXMLConfigurationFactory.defineEndpoint(
                            config, endpointElement, config.getProperties());
                    Endpoint endpoint = config.getEndpoint(endpointName);
                    if (endpoint != null) {
                        if (endpoint instanceof AbstractEndpoint) {
                            endpoint.setFileName(
                                    ServiceBusUtils.generateFileName(endpoint.getName()));
                        }
                        endpoint.init(getSynapseEnvironment());
                        persistEndpoint(endpoint);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Added endpoint : " + endpointName + " to the configuration");
                }
                return true;
            } else {
                handleFault("Unable to create endpoint. Invalid XML definition", null);
            }
        } catch (SynapseException syne) {
            handleFault("Unable to add Endpoint ", syne);
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Deletes the endpoint from the SynapseConfiguration
     *
     * @param endpointName - name of the endpoint to be deleted
     * @return true if the endpoint was successfully deleted and false otherwise
     * @throws EndpointAdminException if the proxy service name given is not existent in the
     *                                synapse configuration
     */
    public boolean deleteEndpoint(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (log.isDebugEnabled()) {
                log.debug("Deleting endpoint : " + endpointName + " from the configuration");
            }
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Endpoint endpoint = synapseConfiguration.getDefinedEndpoints().get(endpointName);
            synapseConfiguration.removeEndpoint(endpointName);
            MediationPersistenceManager pm = getMediationPersistenceManager();
            String fileName = null;
            if (endpoint instanceof AbstractEndpoint) {
                fileName = endpoint.getFileName();
            }
            pm.deleteItem(endpointName, fileName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
            if (log.isDebugEnabled()) {
                log.debug("Endpoint : " + endpointName + " removed from the configuration");
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * Delete selected endpoints
     * @param endpointNames
     * @throws EndpointAdminException
     */
    public void deleteSelectedEndpoint(String [] endpointNames) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            for(String endpointName : endpointNames){
                assertNameNotEmpty(endpointName);
                endpointName = endpointName.trim();
                if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, endpointName))) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting endpoint : " + endpointName + " from the configuration");
                }
                SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
                Endpoint endpoint = synapseConfiguration.getDefinedEndpoints().get(endpointName);
                synapseConfiguration.removeEndpoint(endpointName);
                MediationPersistenceManager pm = getMediationPersistenceManager();
                String fileName = null;
                if (endpoint instanceof AbstractEndpoint) {
                    fileName = endpoint.getFileName();
                }
                pm.deleteItem(endpointName, fileName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint : " + endpointName + " removed from the configuration");
                }
            }
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Delete
     * @throws EndpointAdminException
     */
    public void deleteAllEndpointGroups() throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map<String, Endpoint> namedEndpointMap = synapseConfiguration.getDefinedEndpoints();
            Collection<String> namedEndpointCollection = namedEndpointMap.keySet();
            if (namedEndpointCollection.size()>0) {
                CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                        getcAppArtifactDataService();
                for (String endpointName : namedEndpointCollection) {
                    assertNameNotEmpty(endpointName);
                    endpointName = endpointName.trim();
                    if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, endpointName))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting endpoint : " + endpointName + " from the configuration");
                    }
                    Endpoint endpoint = synapseConfiguration.getDefinedEndpoints().get(endpointName);
                    synapseConfiguration.removeEndpoint(endpointName);
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    String fileName = null;
                    if (endpoint instanceof AbstractEndpoint) {
                        fileName = endpoint.getFileName();
                    }
                    pm.deleteItem(endpointName, fileName, ServiceBusConstants.ITEM_TYPE_ENDPOINT);
                    if (log.isDebugEnabled()) {
                        log.debug("Endpoint : " + endpointName + " removed from the configuration");
                    }
                }
            }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the endpoint element as a string
     *
     * @param endpointName - name of the endpoint
     * @return String representing the endpoint with the given endpoint name
     * @throws Exception in case of an error
     */
    public String getEndpointConfiguration(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            if (synapseConfiguration.getEndpoint(endpointName) != null) {
                OMElement ele = EndpointSerializer.getElementFromEndpoint(
                        synapseConfiguration.getEndpoint(endpointName));
                return ele.toString();
            } else {
                handleFault("The endpoint named " + endpointName + " does not exist", null);
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Get all endpoint configurations from the synapse configuration
     *
     * @return a list of all the endpoints
     * @throws EndpointAdminException in case of an error
     */
    public String[] getEndpoints() throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map<String, Endpoint> namedEndpointMap = synapseConfiguration.getDefinedEndpoints();
            Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();

            List<String> epList = new ArrayList<String>();
            for (Endpoint ep : namedEndpointCollection) {
                OMElement ele = EndpointSerializer.getElementFromEndpoint(ep);
                epList.add(ele.toString());
            }
            return epList.toArray(new String[epList.size()]);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get Metadata of all Endpoints of Synapse configuration
     *
     * @return EndpointMetaData
     */
    public EndpointMetaData[] getEndpointsData() {

        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map<String, Endpoint> namedEndpointMap = synapseConfiguration.getDefinedEndpoints();
            Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();

            List<Endpoint> epList = new ArrayList<Endpoint>();
            for (Endpoint ep : namedEndpointCollection) {
                epList.add(ep);
            }

            Collections.sort(epList, new Comparator<Endpoint>() {
                public int compare(Endpoint o1, Endpoint o2) {
                    return (o1).getName().compareToIgnoreCase((o2).getName());
                }
            });

            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            List<EndpointMetaData> metaDatas = new ArrayList<EndpointMetaData>();
            for (Endpoint ep : epList) {
                EndpointMetaData data = new EndpointMetaData();
                data.setName(ep.getName());
                data.setDescription(ep.getDescription());

                if (cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, ep.getName()))) {
                    data.setDeployedFromCApp(true);
                }
                if (cAppArtifactDataService.isArtifactEdited(getTenantId(), getArtifactName(artifactType, ep.getName()))) {
                    data.setEdited(true);
                }
                // Statistics
                EndpointDefinition def = ((AbstractEndpoint) ep).getDefinition();
                if (null != def) {
                    if (def.isStatisticsEnable()) {
                        data.setEnableStatistics(true);
                    } else {
                        data.setEnableStatistics(false);
                    }
                }

                if (ep.getContext().isState(EndpointContext.ST_ACTIVE)) {
                    data.setSwitchOn(true);
                } else {
                    data.setSwitchOn(false);
                }
                data.setEndpointString(EndpointSerializer.getElementFromEndpoint(ep).toString());
                metaDatas.add(data);
            }
            return metaDatas.toArray(new EndpointMetaData[metaDatas.size()]);

        } finally {
            lock.unlock();
        }
    }

    /**
     * Enable statistics collection for the specified endpoint
     *
     * @param endpointName name of the endpoint
     * @throws EndpointAdminException in case of an error
     */
    public void enableStatistics(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);

            ((AbstractEndpoint) ep).getDefinition().enableStatistics();

            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, endpointName))) {
                persistEndpoint(ep);
            }
            if (log.isDebugEnabled()) {
                log.debug("Statistics enabled on endpoint : " + endpointName);
            }
        } catch (SynapseException syne) {
            handleFault("Error enabling statistics for the endpoint : " + endpointName, syne);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Stop collecting statistics for a specified endpoint
     *
     * @param endpointName name of the endpoint
     * @throws EndpointAdminException on error
     */
    public void disableStatistics(String endpointName) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            assertNameNotEmpty(endpointName);
            endpointName = endpointName.trim();
            Endpoint ep = getSynapseConfiguration().getEndpoint(endpointName);
            ((AbstractEndpoint) ep).getDefinition().disableStatistics();
            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, endpointName))) {
                persistEndpoint(ep);
            }
            if (log.isDebugEnabled()) {
                log.debug("Statistics disabled on endpoint : " + endpointName);
            }
        } catch (SynapseException syne) {
            handleFault("Error disabling statistics for the endpoint : " + endpointName, syne);
        } finally {
            lock.unlock();
        }
    }

    private void persistEndpoint(Endpoint ep) throws EndpointAdminException {
        MediationPersistenceManager pm = getMediationPersistenceManager();
        pm.saveItem(ep.getName(), ServiceBusConstants.ITEM_TYPE_ENDPOINT);
    }

    private void assertNameNotEmpty(String endpointName) throws EndpointAdminException {
        if (endpointName == null || "".equals(endpointName.trim())) {
            handleFault("Invalid name : Name is empty.", null);
        }
    }

    private void handleFault(String message, Exception e) throws EndpointAdminException {
        if (e != null) {
            log.error(message, e);
            throw new EndpointAdminException(e.getMessage(), e);
        } else {
            log.error(message);
            throw new EndpointAdminException(message);
        }
    }

    /**
     * Update an existing endpoint from the given String representation of the XML
     *
     * @param epString - String representing the XML which describes the
     *                 Endpoint element
     * @return true if the endpoint was saved successfully and false otherwise
     * @throws EndpointAdminException if the endpoint does not exists in the
     *                                SynapseConfiguration
     */
    public boolean saveEndpoint(String epString) throws EndpointAdminException {
        OMElement endpointElement;
        final Lock lock = getLock();
        try {
            lock.lock();
            try {
                endpointElement = AXIOMUtil.stringToOM(epString);
            } catch (XMLStreamException e) {
                return false;
            }

            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (endpointElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.ENDPOINT_ELT.getLocalPart())) {

                String endpointName = endpointElement.getAttributeValue(new QName("name"));
                assertNameNotEmpty(endpointName);
                endpointName = endpointName.trim();
                if (log.isDebugEnabled()) {
                    log.debug("Updating the definition of the endpoint : " + endpointName);
                }

                Endpoint previousEndpoint = getSynapseConfiguration().getEndpoint(
                        endpointName.trim());

                String artifactName = getArtifactName(artifactType, previousEndpoint.getName());
                if (previousEndpoint == null) {
                    addEndpoint(epString);
                }

                boolean statisticsState = false;

                EndpointDefinition def = null;
                if (null != previousEndpoint) {
                    def = ((AbstractEndpoint) previousEndpoint).getDefinition();
                }
                if (null != def && def.isStatisticsEnable()) {
                    statisticsState = true;
                }

                String fileName = null;
                if (null != previousEndpoint && previousEndpoint instanceof AbstractEndpoint) {
                    fileName = previousEndpoint.getFileName();
                }

                Endpoint endpoint = EndpointFactory.getEndpointFromElement(
                        endpointElement, false, getSynapseConfiguration().getProperties());
                if (endpoint == null) {
                    handleFault("Newly created endpoint is null ", null);
                }
                if (null != def) {
                    if (statisticsState) {
                        ((AbstractEndpoint) endpoint).getDefinition()
                                .enableStatistics();
                    }
                }

                if (fileName != null && endpoint instanceof AbstractEndpoint) {
                    endpoint.setFileName(fileName);
                }

                endpoint.init(getSynapseEnvironment());
                endpointName = endpointName.trim();
                getSynapseConfiguration().removeEndpoint(endpointName);
                getSynapseConfiguration().addEndpoint(endpointName, endpoint);
                if (cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                    cAppArtifactDataService.setEdited(getTenantId(), artifactName);
                } else {
                    persistEndpoint(endpoint);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Updated the definition of the endpoint : " + endpointName);
                }
                return true;
            } else {
                handleFault("Unable to update endpoint. Invalid XML definition", null);
            }
        } catch (SynapseException syne) {
            handleFault("Unable to edit Endpoint ", syne);
        } catch (Exception e) {
            handleFault("Unable to edit Endpoint ", e);
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Update an existing Endpoint endpoint in the registry
     *
     * @param key    dynamic endpoint key
     * @param epName endpoint name
     * @return whether operation is successful
     * @throws EndpointAdminException on Error
     */
    public boolean saveDynamicEndpoint(String key, String epName) throws EndpointAdminException {
        OMElement endpointElement;
        final Lock lock = getLock();
        try {
            lock.lock();
            endpointElement = AXIOMUtil.stringToOM(epName);

            if (endpointElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.ENDPOINT_ELT.getLocalPart())) {

                String endpointName = "dynamicEndpoint";
                if (log.isDebugEnabled()) {
                    log.debug("Updating endpoint : " + endpointName + " in the Synapse registry");
                }

                Registry registry = getSynapseConfiguration().getRegistry();
                if (registry != null) {
                    if (registry.getRegistryEntry(key).getType() == null) {
                        handleFault("No resource exists by the key '" + key + "'", null);
                    }

                    registry.updateResource(key, endpointElement);

                    if (log.isDebugEnabled()) {
                        log.debug("Updated endpoint : " + endpointName + " in the Synapse registry");
                    }
                    return true;
                }
            } else {
                handleFault("Unable to create endpoint. Invalid XML definition", null);
            }
        } catch (XMLStreamException e) {
            return false;
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Add an endpoint to the Synapse registry
     *
     * @param key    of the dynamic endpoint
     * @param epConfiguration endpoint configuration
     * @throws EndpointAdminException in case of an error
     */
    public boolean addDynamicEndpoint(String key, String epConfiguration) throws EndpointAdminException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.reset();
        String originalKey = key;
        try {
            org.wso2.carbon.registry.core.Registry registry;
            if (key.startsWith("conf:")) {
                registry = getConfigSystemRegistry();
                key = key.replace("conf:", "");
            } else {
                registry = getGovernanceRegistry();
                key = key.replace("gov:", "");
            }
            if (!registry.resourceExists(key)) {

                try {
                    OMElement endpointElement = AXIOMUtil.stringToOM(epConfiguration);
                    OMFactory fac = OMAbstractFactory.getOMFactory();
                    String name = originalKey.replace(":", "/");
                    endpointElement.addAttribute(fac.createOMAttribute("name",
                                                                       fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, ""),
                                                                       name));
                    XMLPrettyPrinter.prettify(endpointElement, stream);
                } catch (Exception e) {
                    handleFault("Unable to pretty print configuration", e);
                }
                epConfiguration = new String(stream.toByteArray()).trim();

                Resource resource = registry.newResource();
                resource.setMediaType(WSO2_ENDPOINT_MEDIA_TYPE);
                resource.setContent(epConfiguration);
                registry.put(key, resource);
            } else {
                log.warn("Resource is already exists");
                return false;
            }
        } catch (RegistryException e) {
            handleFault("WSO2 Registry Exception", e);
            return false;
        }
        return true;
    }

    /**
     * Get a dynamic endpoint from the registry
     *
     * @param key dynamic endpoint key
     * @return dynamic endpoint configuration
     * @throws EndpointAdminException on an error
     */
    public String getDynamicEndpoint(String key) throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synConfig = getSynapseConfiguration();
            Registry registry = synConfig.getRegistry();
            if (registry != null) {
                if (registry.getRegistryEntry(key).getType() == null) {
                    handleFault("No resource is available by the key '" + key + "'", null);
                }
            } else {
                handleFault("Unable to access the registry instance for the ESB", null);
            }
            OMElement e = null;
            if (registry != null) {
				Object obj = null;
				try {
					obj = registry.getResource(new Entry(key),
							synConfig.getProperties());
				} catch (Exception e1) {
					log.error("Invalid endpoint configuration", e1);
					return null;
				}
				if (obj instanceof OMElement) {
					e = (OMElement) obj;
				} else {
					log.error("Invalid endpoint configuration");
				}
				// e = (OMElement) registry.getResource(new Entry(key),
				// synConfig.getProperties());
			}
            if (e != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found dynamic endpoint " + key);
                }
                return e.toString();
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Check registry is available or not
     * @return boolean
     * @throws EndpointAdminException
     */

    public boolean isRegisterNull() throws EndpointAdminException{
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synConfig = getSynapseConfiguration();
            Registry registry = synConfig.getRegistry();
            if (registry == null) {
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Get all endpoints stored in the registry
     *
     * @return endpoints in the registry
     * @throws EndpointAdminException in case of an error
     */
    public String[] getDynamicEndpoints() throws EndpointAdminException {
        org.wso2.carbon.registry.core.Registry registry;
        final Lock lock = getLock();
        try {
            lock.lock();
            String[] configInfo = getConfigSystemRegistry() != null ? getMimeTypeResult(getConfigSystemRegistry()):new String[0];
            String[] govInfo = getGovernanceRegistry() !=null ?getMimeTypeResult(getGovernanceRegistry()):new String[0];
            String[] info = new String[configInfo.length + govInfo.length];

            int ptr = 0;
            for (String aConfigInfo : configInfo) {
                info[ptr] = "conf:" + aConfigInfo;
                ++ptr;
            }
            for (String aGovInfo : govInfo) {
                info[ptr] = "gov:" + aGovInfo;
                ++ptr;
            }
            Arrays.sort(info);
            if (log.isDebugEnabled()) {
                log.debug("Found " + info.length + " dynamic endpoints");
            }
            return info;
        } catch (RegistryException e) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove a endpoint form the registry
     *
     * @param key dynamic endpoint key
     * @return whether operation is successful
     * @throws EndpointAdminException in case of an error
     */
    public boolean deleteDynamicEndpoint(String key) throws EndpointAdminException {
        Lock lock = getLock();
        try {
            lock.lock();
            Registry registry = getSynapseConfiguration().getRegistry();
            if (registry != null) {
                if (registry.getRegistryEntry(key).getType() == null) {
                    handleFault("The key '" + key +
                            "' cannot be found within the configuration", null);
                }
                registry.delete(key);

                if (log.isDebugEnabled()) {
                    log.debug("Deleted endpoint with key: " + key + " from the Synapse registry");
                }
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    private String[] getMimeTypeResult(org.wso2.carbon.registry.core.Registry targetRegistry)
            throws EndpointAdminException, RegistryException {
        String sql = "SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE REG_MEDIA_TYPE = ?";
        Map parameters = new HashMap();
        parameters.put("query", sql);
        parameters.put("1", WSO2_ENDPOINT_MEDIA_TYPE);
        Resource result = targetRegistry.executeQuery(null, parameters);
        return (String[]) result.getContent();
    }

    /**
     * Get dependents of a particular endpoint.
     *
     * @param endpointName endpoint name
     * @return dependants of a endpoints
     */
    public ConfigurationObject[] getDependents(String endpointName) {
        DependencyManagementService dependencyMgr = ConfigHolder.getInstance().
                getDependencyManager();
        if (dependencyMgr != null) {
            ConfigurationObject[] dependents = dependencyMgr.getDependents(
                    ConfigurationObject.TYPE_ENDPOINT, endpointName);
            if (dependents != null && dependents.length > 0) {
                List<ConfigurationObject> deps = new ArrayList<ConfigurationObject>();
                for (ConfigurationObject o : dependents) {
                    if (o.getType() != ConfigurationObject.TYPE_UNKNOWN) {
                        deps.add(o);
                    }
                }

                if (deps.size() > 0) {
                    return deps.toArray(new ConfigurationObject[deps.size()]);
                }
            }
        }
        return null;
    }

    public String[] getEndPointsNames() throws EndpointAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            Map<String, Endpoint> namedEndpointMap = synapseConfiguration.getDefinedEndpoints();
            Collection<Endpoint> namedEndpointCollection = namedEndpointMap.values();
            return listToNames(namedEndpointCollection.toArray(
                    new Endpoint[namedEndpointCollection.size()]));
        } finally {
            lock.unlock();
        }
    }

    private String[] listToNames(Endpoint[] eps) {
        if (eps == null) {
            return null;
        } else {
            String[] datas = new String[eps.length];
            for (int i = 0; i < eps.length; i++) {
                Endpoint ep = eps[i];
                datas[i] = ep.getName();
            }
            return datas;
        }
    }

    public int getEndpointCount() throws EndpointAdminException {
        final Lock lock = getLock();
        try{
            lock.lock();
            return getSynapseConfiguration().getDefinedEndpoints().size();
        } catch (Exception e) {
            handleFault("Error while retrieving Endpoint count", e);
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public int getDynamicEndpointCount() throws EndpointAdminException {
        org.wso2.carbon.registry.core.Registry registry;
        try {
            String[] govList = getGovernanceRegistry() != null? getMimeTypeResult(getGovernanceRegistry()) :new String[0];
            String[] confList = getConfigSystemRegistry() != null ? getMimeTypeResult(getConfigSystemRegistry()):new String [0];
            return confList.length + govList.length;
        } catch (Exception e) {
            handleFault("Error while retrieving dynamic endpoint count", e);
        }
        return 0;
    }
}