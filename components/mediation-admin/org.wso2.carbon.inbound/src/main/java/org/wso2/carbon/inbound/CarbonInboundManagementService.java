/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.SynapseXMLConfigurationFactory;
import org.apache.synapse.inbound.InboundEndpoint;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
/**
 * Admin service related to inbound endpoint
 * */
public class CarbonInboundManagementService extends AbstractServiceBusAdmin {

    private static Log log = LogFactory.getLog(CarbonInboundManagementService.class);

    /**
     * Get all the inbound endpoins available.
     * 
     * @return List<InboundEndpointDTO> (This contains all the inbound endpoints)
     * */
    public InboundEndpointDTO[] getAllInboundEndpointNames() throws InboundManagementException {        
        Collection<InboundEndpoint> inboundEndpoints = getSynapseConfiguration().getInboundEndpoints();
        InboundEndpointDTO[]lInboundEndpoints = new InboundEndpointDTO[inboundEndpoints.size()];
        int i = 0;
        for (InboundEndpoint inboundEndpoint : inboundEndpoints) {
            lInboundEndpoints[i++] = new InboundEndpointDTO(inboundEndpoint);
        }
        return lInboundEndpoints;
    }

    /**
     * 
     * Get specific inbound ep by name
     * 
     * @param endointName
     * @return
     * @throws InboundManagementException
     */
    public InboundEndpointDTO getInboundEndpointbyName(String endointName) throws InboundManagementException {
        InboundEndpoint inboundEndpoint = getInboundEndpoint(endointName);
        if (inboundEndpoint != null) {
            return new InboundEndpointDTO(inboundEndpoint);
        }
        return null;
    }

    /**
     * 
     * Create inbound EP based on the given parameters
     * 
     * @param name
     * @param sequence
     * @param onError
     * @param protocol
     * @param classImpl
     * @param sParams
     * @throws InboundManagementException
     */
    public void addInboundEndpoint(String name, String sequence, String onError, String protocol, String classImpl, String suspend, ParameterDTO[]lParameterDTOs) throws InboundManagementException {

        try {
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/ns/synapse", "syn");
            OMElement elem = fac.createOMElement("inboundEndpoint", omNs);
            elem.addAttribute(fac.createOMAttribute("name", null, name));
            if (sequence != null && sequence != "") {
                elem.addAttribute(fac.createOMAttribute("sequence", null, sequence));
            }
            elem.addAttribute(fac.createOMAttribute("suspend", null, suspend));
            if (onError != null && onError != "") {
                elem.addAttribute(fac.createOMAttribute("onError", null, onError));
            }
            if (protocol != null) {
                elem.addAttribute(fac.createOMAttribute("protocol", null, protocol));
            } else {
                elem.addAttribute(fac.createOMAttribute("class", null, classImpl));
            }
            OMElement params = fac.createOMElement("parameters", omNs);
            for (ParameterDTO parameterDTO : lParameterDTOs) {
                OMElement param = fac.createOMElement("parameter", omNs);
                param.addAttribute(fac.createOMAttribute("name", null, parameterDTO.getName()));
                if (parameterDTO.getKey() != null) {
                    param.addAttribute(fac.createOMAttribute("key", null, parameterDTO.getKey()));
                } else if (parameterDTO.getValue() != null) {
                    param.setText(parameterDTO.getValue());
                }
                params.addChild(param);
            }
            elem.addChild(params);
            SynapseXMLConfigurationFactory.defineInboundEndpoint(synapseConfiguration, elem, synapseConfiguration.getProperties());
            InboundEndpoint inboundEndpoint = getInboundEndpoint(name);
            if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                persistInboundEndpoint(inboundEndpoint);
            }
            inboundEndpoint.init(getSynapseEnvironment());
        } catch (Exception ex) {
            log.error("Error adding inbound Endpoint", ex);
            removeInboundEndpoint(name);
            throw ex;
        }
    }

    /**
     * 
     * Adds new inbound endpoint from XML Config
     * 
     * @param inboundElement
     */
    public void addInboundEndpointFromXMLString(String inboundElement) {
        XMLStreamReader reader = null;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(inboundElement));
        } catch (XMLStreamException e) {
            log.error(e.getMessage());
        }
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement omElement = builder.getDocumentElement();
        SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
        SynapseXMLConfigurationFactory.defineInboundEndpoint(synapseConfiguration, omElement, synapseConfiguration.getProperties());
        String name = omElement.getAttributeValue(new QName("name"));
        InboundEndpoint inboundEndpoint = null;
        try {
            inboundEndpoint = getInboundEndpoint(name);
        } catch (InboundManagementException e) {
            log.error(e.getMessage());
        }
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            persistInboundEndpoint(inboundEndpoint);
        }
        inboundEndpoint.init(getSynapseEnvironment());
    }

    /**
     * 
     * Update inbound endpoint with given parameters
     * 
     * @param name
     * @param sequence
     * @param onError
     * @param protocol
     * @param classImpl
     * @param lParameterDTOs
     * @throws InboundManagementException
     */
    public void updateInboundEndpoint(String name, String sequence, String onError, String protocol, String classImpl, String suspend, ParameterDTO[]lParameterDTOs) throws InboundManagementException {
        SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/ns/synapse", "syn");
        OMElement elem = fac.createOMElement("inboundEndpoint", omNs);
        elem.addAttribute(fac.createOMAttribute("name", null, name));
        if (sequence != null && sequence != "") {
            elem.addAttribute(fac.createOMAttribute("sequence", null, sequence));
        }
        elem.addAttribute(fac.createOMAttribute("suspend", null, suspend));
        if (onError != null && onError != "") {
            elem.addAttribute(fac.createOMAttribute("onError", null, onError));
        }
        if (protocol != null) {
            elem.addAttribute(fac.createOMAttribute("protocol", null, protocol));
        } else {
            elem.addAttribute(fac.createOMAttribute("class", null, classImpl));
        }
        OMElement params = fac.createOMElement("parameters", omNs);
        for (ParameterDTO lParameterDTO : lParameterDTOs) {
            OMElement param = fac.createOMElement("parameter", omNs);
            param.addAttribute(fac.createOMAttribute("name", null, lParameterDTO.getName()));            
            if (lParameterDTO.getKey() != null) {
           	    param.addAttribute(fac.createOMAttribute("key", null, lParameterDTO.getKey()));
            }else if (lParameterDTO.getValue() != null) {
                param.setText(lParameterDTO.getValue());
            }            
            params.addChild(param);
        }
        elem.addChild(params);

        InboundEndpoint oldInboundEndpoint = synapseConfiguration.getInboundEndpoint(name);
        if (oldInboundEndpoint != null) {
            oldInboundEndpoint.destroy();
            synapseConfiguration.removeInboundEndpoint(name);
        }

        SynapseXMLConfigurationFactory.defineInboundEndpoint(synapseConfiguration, elem, synapseConfiguration.getProperties());
        InboundEndpoint inboundEndpoint = getInboundEndpoint(name);

        /** If the inbound service deployed from artifact container, set the isEdited variable to true */
        if (oldInboundEndpoint.getArtifactContainerName() != null) {
            inboundEndpoint.setArtifactContainerName(oldInboundEndpoint.getArtifactContainerName());
            inboundEndpoint.setIsEdited(true);
        }
        if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
            persistInboundEndpoint(inboundEndpoint);
        }
        try {
            inboundEndpoint.init(getSynapseEnvironment());
        } catch (Exception e) {
            inboundEndpoint.destroy();
            synapseConfiguration.removeInboundEndpoint(name);
            synapseConfiguration.addInboundEndpoint(oldInboundEndpoint.getName(), oldInboundEndpoint);
            if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                persistInboundEndpoint(oldInboundEndpoint);
            }
            oldInboundEndpoint.init(getSynapseEnvironment());
            throw e;
        }
    }

    /**
     * 
     * Removes the given inbound endpoint
     * 
     * @param name
     * @throws InboundManagementException
     */
    public void removeInboundEndpoint(String name) throws InboundManagementException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting inbound service : " + name);
            }
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            InboundEndpoint inboundEndpoint = synapseConfiguration.getInboundEndpoint(name);
            if (inboundEndpoint != null && inboundEndpoint.getArtifactContainerName() == null) {
                synapseConfiguration.removeInboundEndpoint(name);
                inboundEndpoint.destroy();
                if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                    MediationPersistenceManager pm = getMediationPersistenceManager();
                    pm.deleteItem(name, inboundEndpoint.getFileName(),
                            ServiceBusConstants.ITEM_TYPE_INBOUND);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Inbound service : " + name + " deleted");
                }
            } else {
                if (inboundEndpoint.getArtifactContainerName() != null) {
                    log.warn("Inbound service" + name + " deployed from artifact container. Will not be deleted.");
                } else {
                    log.warn("No Inbound service exists by the name : " + name);
                }
            }
        } catch (Exception e) {
            log.error("Unable to delete inbound service : " + name, e);
        }

    }

    public String enableStatistics(String inboundEndpointName) throws InboundManagementException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            InboundEndpoint inboundEndpoint = synapseConfiguration.getInboundEndpoint(inboundEndpointName);
            if (inboundEndpoint != null) {
                if (inboundEndpoint.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(inboundEndpointName);
                    config.enableStatistics();
                    inboundEndpoint.configure(config);
                } else {
                    inboundEndpoint.getAspectConfiguration().enableStatistics();
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (inboundEndpoint.getArtifactContainerName() == null) {
                    if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                        persistInboundEndpoint(inboundEndpoint);
                    }
                }
                return inboundEndpointName;
            } else {
                handleException(log, "No defined Inbound Endpoint with name " + inboundEndpointName +
                                     " found to enable statistics in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't enable statistics of the Inbound Endpoint " + inboundEndpointName + " : " +
                                 fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableStatistics(String inboundEndpointName) throws InboundManagementException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            InboundEndpoint inboundEndpoint = synapseConfiguration.getInboundEndpoint(inboundEndpointName);
            if (inboundEndpoint != null) {
                if (inboundEndpoint.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(inboundEndpointName);
                    config.disableStatistics();
                    inboundEndpoint.configure(config);
                } else {
                    inboundEndpoint.getAspectConfiguration().disableStatistics();
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (inboundEndpoint.getArtifactContainerName() == null) {
                    if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                        persistInboundEndpoint(inboundEndpoint);
                    }
                }
                return inboundEndpointName;
            } else {
                handleException(log, "No defined Inbound Endpoint with name " + inboundEndpointName +
                                     " found to disable statistics in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't disable statistics of the Inbound Endpoint " + inboundEndpointName + " : " +
                                 fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String enableTracing(String inboundEndpointName) throws InboundManagementException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            InboundEndpoint inboundEndpoint = synapseConfiguration.getInboundEndpoint(inboundEndpointName);
            if (inboundEndpoint != null) {
                if (inboundEndpoint.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(inboundEndpointName);
                    config.enableTracing();
                    config.enableStatistics(); // Tracing need statistics to be enabled
                    inboundEndpoint.configure(config);
                } else {
                    inboundEndpoint.getAspectConfiguration().enableTracing();
                    inboundEndpoint.getAspectConfiguration().enableStatistics(); // Tracing need statistics to be enabled
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (inboundEndpoint.getArtifactContainerName() == null) {
                    if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                        persistInboundEndpoint(inboundEndpoint);
                    }
                }
                return inboundEndpointName;
            } else {
                handleException(log, "No defined Inbound Endpoint with name " + inboundEndpointName +
                                     " found to enable tracing in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't enable tracing of the Inbound Endpoint " + inboundEndpointName + " : " +
                                 fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public String disableTracing(String inboundEndpointName) throws InboundManagementException {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            InboundEndpoint inboundEndpoint = synapseConfiguration.getInboundEndpoint(inboundEndpointName);
            if (inboundEndpoint != null) {
                if (inboundEndpoint.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(inboundEndpointName);
                    config.disableTracing();
                    inboundEndpoint.configure(config);
                } else {
                    inboundEndpoint.getAspectConfiguration().disableTracing();
                }

                /** Persist the api service if it is not deployed via an artifact container */
                if (inboundEndpoint.getArtifactContainerName() == null) {
                    if(!Boolean.parseBoolean(System.getProperty("NonRegistryMode"))) {
                        persistInboundEndpoint(inboundEndpoint);
                    }
                }
                return inboundEndpointName;
            } else {
                handleException(log, "No defined Inbound Endpoint with name " + inboundEndpointName +
                                     " found to disable tracing in the Synapse configuration", null);
            }
        } catch (Exception fault) {
            handleException(log, "Couldn't disable tracing of the Inbound Endpoint " + inboundEndpointName + " : " +
                                 fault.getMessage(), fault);
        } finally {
            lock.unlock();
        }
        return null;
    }

    private void persistInboundEndpoint(InboundEndpoint inboundEndpoint) {
        MediationPersistenceManager pm = getMediationPersistenceManager();
        if (pm == null) {
            log.error("Cannot Persist sequence because persistence manager is null, "
                    + "probably persistence is disabled");
        } else if (inboundEndpoint.getArtifactContainerName() == null) {
            pm.saveItem(inboundEndpoint.getName(), ServiceBusConstants.ITEM_TYPE_INBOUND);
        }
    }

    private InboundEndpoint getInboundEndpoint(String endointName) throws InboundManagementException {
        Collection<InboundEndpoint> inboundEndpoints = getSynapseConfiguration().getInboundEndpoints();
        for (InboundEndpoint inboundEndpoint : inboundEndpoints) {
            if (endointName.equals(inboundEndpoint.getName())) {
                return (inboundEndpoint);
            }
        }
        return null;
    }

    private void handleException(Log log, String message, Exception e) throws InboundManagementException {
        if (e == null) {
            InboundManagementException inboundManagementException = new InboundManagementException(message);
            log.error(message, inboundManagementException);
            throw inboundManagementException;
        } else {
            message = message + " :: " + e.getMessage();
            log.error(message, e);
            throw new InboundManagementException(message, e);
        }
    }
}
