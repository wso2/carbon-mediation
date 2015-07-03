/*
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

package org.wso2.carbon.proxyadmin.service;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.config.xml.ProxyServiceFactory;
import org.apache.synapse.config.xml.ProxyServiceSerializer;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.config.xml.endpoints.EndpointSerializer;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.util.PolicyInfo;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.carbon.mediation.initializer.services.CAppArtifactDataService;
import org.wso2.carbon.proxyadmin.*;
import org.wso2.carbon.proxyadmin.observer.ProxyServiceParameterObserver;
import org.wso2.carbon.proxyadmin.util.ConfigHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * The class <code>ProxyServiceAdmin</code> provides the administration service to configure
 * proxy services.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ProxyServiceAdmin extends AbstractServiceBusAdmin {

    private static String SUCCESSFUL = "successful";
    private static String FAILED = "failed";
    private static Log log = LogFactory.getLog(ProxyServiceAdmin.class);
    private static final String artifactType = ServiceBusConstants.PROXY_SERVICE_TYPE;

    /**
     * Enables statistics for the specified proxy service
     *
     * @param proxyName name of the proxy service name of which the statistics need to be enabled
     * @throws ProxyAdminException in case of a failure in enabling statistics
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String enableStatistics(String proxyName) throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            if (proxy != null) {
                if (proxy.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(proxyName);
                    config.enableStatistics();
                    proxy.configure(config);
                } else {
                    proxy.getAspectConfiguration().enableStatistics();
                }
                CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                        getcAppArtifactDataService();
                if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(),getArtifactName(artifactType, proxyName))) {
                    persistProxyService(proxy);
                }

            } else {
                log.error("Couldn't find the proxy service with name "
                        + proxyName + " to enable statistics");
                return FAILED;
            }
            if(log.isDebugEnabled()) {
                log.debug("Enabled statistics on proxy service : " + proxyName);
            }
            return SUCCESSFUL;
        } catch (Exception e) {
            handleException(log, "Unable to enable statistics for proxy service " + proxyName, e);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    /**
     * Disables statistics for the specified proxy service
     *
     * @param proxyName name of the proxy service of which statistics need to be disabled
     * @throws ProxyAdminException in case of a failure in disabling statistics
     * @return <code>successful</code> on success or <code>failed</code> if unsuccessful
     */
    public String disableStatistics(String proxyName) throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            if (proxy != null) {
                if (proxy.getAspectConfiguration() == null) {
                    AspectConfiguration config = new AspectConfiguration(proxyName);
                    config.disableStatistics();
                    proxy.configure(config);
                } else {
                    proxy.getAspectConfiguration().disableStatistics();
                }
                CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                        getcAppArtifactDataService();
                if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, proxyName))) {
                    persistProxyService(proxy);
                }
            } else {
                log.error("Couldn't find the proxy service with name "
                        + proxyName + " to disable statistics");
                return FAILED;
            }
            if(log.isDebugEnabled()) {
                log.debug("Disabled statistics on proxy service : " + proxyName);
            }
            return SUCCESSFUL;
        } catch (Exception e) {
            handleException(log, "Unable to disable statistics for proxy service " + proxyName, e);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    /**
     * Enables tracing for the specified proxy service
     *
     * @param proxyName name of the the proxy service of which tracing needs to be enabled
     * @throws ProxyAdminException in case of a failure in enabling tracing
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String enableTracing(String proxyName) throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            proxy.setTraceState(SynapseConstants.TRACING_ON);
            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, proxyName))) {
                persistProxyService(proxy);
            }
            if(log.isDebugEnabled()) {
                log.debug("Enabled tracing on proxy service : " + proxyName);
            }
            return SUCCESSFUL;
        } catch (Exception e) {
            handleException(log, "Unable to enable tracing for proxy service " + proxyName, e);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    /**
     * Disables tracing for the specified proxy service
     *
     * @param proxyName name of the proxy service of which tracing needs to be disabled
     * @throws ProxyAdminException in case of a failure in disabling tracing
     * @return SUCCESSFUL is the operation is successful and FAILED if it is failed
     */
    public String disableTracing(String proxyName) throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            proxy.setTraceState(SynapseConstants.TRACING_OFF);
            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (!cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), getArtifactName(artifactType, proxyName))) {
                persistProxyService(proxy);
            }
            if(log.isDebugEnabled()) {
                log.debug("Disabled tracing on proxy service : " + proxyName);
            }
            return SUCCESSFUL;
        } catch (Exception e) {
            handleException(log, "Unable to disable tracing for proxy service " + proxyName, e);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    /**
     * Add a proxy service described by the given OMElement
     *
     * @param proxyServiceElement configuration of the proxy service which needs to be added
     * @param fileName Name of the file in which this configuration should be saved or null
     * @throws ProxyAdminException if the element is not an proxy service or if a proxy service with the
     *                   same name exists
     */
    private void addProxyService(OMElement proxyServiceElement,
                                 String fileName, boolean updateMode) throws ProxyAdminException {
        try {
            if (proxyServiceElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.PROXY_ELT.getLocalPart())) {
                String proxyName = proxyServiceElement.getAttributeValue(new QName("name"));

                if (getSynapseConfiguration().getProxyService(proxyName) != null ||
                        getSynapseConfiguration().getAxisConfiguration().getService(
                                proxyName) != null) {
                    handleException(log, "A service named " + proxyName + " already exists", null);
                } else {
                    ProxyService proxy = ProxyServiceFactory.createProxy(proxyServiceElement,
                            getSynapseConfiguration().getProperties());

                    if (updateMode) {
                        proxy.setFileName(fileName);
                    } else {
                        if (fileName != null) {
                            proxy.setFileName(fileName);
                        } else {
                            proxy.setFileName(ServiceBusUtils.generateFileName(proxy.getName()));
                        }
                    }

                    String artifactName = getArtifactName(artifactType, proxyName);
                    try {
                        getSynapseConfiguration().addProxyService(
                                proxy.getName(), proxy);
                        proxy.buildAxisService(getSynapseConfiguration(), getAxisConfig());
                        addParameterObserver(proxy.getName());

                        if(log.isDebugEnabled()) {
                            log.debug("Added proxy service : " + proxyName);
                        }

                        if (!proxy.isStartOnLoad()) {
                            proxy.stop(getSynapseConfiguration());
                        }

                        if (proxy.getTargetInLineInSequence() != null) {
                            proxy.getTargetInLineInSequence().init(getSynapseEnvironment());
                        }
                        if (proxy.getTargetInLineOutSequence() != null) {
                            proxy.getTargetInLineOutSequence().init(getSynapseEnvironment());
                        }
                        if (proxy.getTargetInLineFaultSequence() != null) {
                            proxy.getTargetInLineFaultSequence().init(getSynapseEnvironment());
                        }
                        if (proxy.getTargetInLineEndpoint() != null) {
                            proxy.getTargetInLineEndpoint().init(getSynapseEnvironment());
                        }

                        CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                                getcAppArtifactDataService();

                        if (cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                            cAppArtifactDataService.setEdited(getTenantId(), artifactName);
                        } else {
                            persistProxyService(proxy);
                        }

                    } catch (Exception e) {
                        getSynapseConfiguration().removeProxyService(proxyName);
                        try{
                            if (getAxisConfig().getService(proxy.getName()) != null) {
                                getAxisConfig().removeService(proxy.getName());
                            }
                        } catch (Exception ignore) {}
                        handleException(log, "Error trying to add the proxy service to the ESB " +
                                "configuration : " + proxy.getName(), e);
                    }
                }
            } else {
                handleException(log, "Invalid proxy service definition", null);
            }
        } catch (AxisFault af) {
            handleException(log, "Invalid proxy service definition", af);
        }
    }

    /**
     * Alter and saves the proxy service to the SynapseConfiguration as specified by the
     * given OMElement configuration
     *
     * @param proxyServiceElement configuration of the proxy service which needs to be altered
     * @throws ProxyAdminException if the service not present or the configuration is wrong or
     *                   in case of a failure in building the axis service
     */
    private void modifyProxyService(OMElement proxyServiceElement) throws ProxyAdminException{
        try {
            if (proxyServiceElement.getQName().getLocalPart()
                    .equals(XMLConfigConstants.PROXY_ELT.getLocalPart())) {

                String proxyName = proxyServiceElement.getAttributeValue(new QName("name"));
                SynapseConfiguration synapseConfig = getSynapseConfiguration();

                ProxyService currentProxy = synapseConfig.getProxyService(proxyName);
                boolean wasRunning = false;
                if (currentProxy == null) {
                    handleException(log, "A proxy service named "
                            + proxyName + " does not exist", null);

                } else {
                    log.debug("Deleting existing proxy service : " + proxyName);
                    AxisService axisService = synapseConfig.getAxisConfiguration().
                            getService(proxyName);
                    CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                            getcAppArtifactDataService();
                    if (axisService != null) {
                        wasRunning = axisService.isActive();
                        axisService.getParent().addParameter(
                                CarbonConstants.KEEP_SERVICE_HISTORY_PARAM, "true");
                        updateAndSyncServiceParameters(currentProxy,axisService);
                    }
                    deleteProxyService(proxyName);

                    try {
                        log.debug("Adding proxy service : " + proxyName);
                        addProxyService(proxyServiceElement, currentProxy.getFileName(), true);
                        if(log.isDebugEnabled()) {
                            log.debug("Modified proxy service : " + proxyName);
                        }

                        if (!wasRunning &&
                                synapseConfig.getProxyService(proxyName).isRunning()) {
                            synapseConfig.getProxyService(proxyName).stop(synapseConfig);
                        } else if (wasRunning &&
                                !synapseConfig.getProxyService(proxyName).isRunning()) {
                            synapseConfig.getProxyService(proxyName).start(synapseConfig);
                        }

                        ProxyService proxy = synapseConfig.getProxyService(proxyName);
                        if (proxy != null) {
                            if (proxy.getTargetInLineInSequence() != null) {
                                proxy.getTargetInLineInSequence().init(getSynapseEnvironment());
                            }
                            if (proxy.getTargetInLineOutSequence() != null) {
                                proxy.getTargetInLineOutSequence().init(getSynapseEnvironment());
                            }
                            if (proxy.getTargetInLineFaultSequence() != null) {
                                proxy.getTargetInLineFaultSequence().init(getSynapseEnvironment());
                            }
                            if (proxy.getTargetInLineEndpoint() != null) {
                                proxy.getTargetInLineEndpoint().init(getSynapseEnvironment());
                            }
                        }
                    } catch (Exception e) {

                        log.error("Unable to save changes made for the proxy service : "
                                + proxyName + ". Restoring the existing proxy..");

                        try {
                            synapseConfig.addProxyService(proxyName, currentProxy);
                            String artifactName = getArtifactName(artifactType, proxyName);
                            if (cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
                                cAppArtifactDataService.setEdited(getTenantId(), artifactName);
                            } else {
                                persistProxyService(currentProxy);
                            }

                            currentProxy.buildAxisService(synapseConfig, getAxisConfig());
                            addParameterObserver(currentProxy.getName());

                            if (!wasRunning) {
                                currentProxy.stop(synapseConfig);
                            } else {
                                currentProxy.start(synapseConfig);
                            }
                        } catch (Exception af) {
                            handleException(log, "Unable to restore the existing proxy", af);
                        }

                        handleException(log, "Unable to save changes made for the proxy service : "
                                + proxyName + ". Restored the existing proxy...", e);
                    }
                }
            } else {
                handleException(log, "Invalid proxy service definition", null);
            }

        } catch (AxisFault af) {
            handleException(log, "Invalid proxy service definition", af);
        }
    }

    /**
     * Deletes a proxy service from the synapse configuration
     *
     * @param proxyName name of the proxy service which needs to be deleted
     * @throws ProxyAdminException if the proxy service name given is not existent in the
     *                   synapse configuration
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String deleteProxyService(String proxyName) throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();

            if (log.isDebugEnabled()) {
                log.debug("Deleting proxy service : " + proxyName);
            }
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            ProxyService proxy = synapseConfiguration.getProxyService(proxyName);
            CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                    getcAppArtifactDataService();
            if (proxy != null) {
                synapseConfiguration.removeProxyService(proxyName);
                MediationPersistenceManager pm = getMediationPersistenceManager();
                pm.deleteItem(proxyName, proxy.getFileName(),
                        ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
                if(log.isDebugEnabled()) {
                    log.debug("Proxy service : " + proxyName + " deleted");
                }
                return SUCCESSFUL;

            } else {
                log.warn("No proxy service exists by the name : " + proxyName);
                return FAILED;
            }
        } catch (Exception e) {
            handleException(log, "Unable to delete proxy service : " + proxyName, e);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    /**
     * Get the available transport names from the AxisConfiguration
     *
     * @return String array of available transport names
     */
    public String[] getAvailableTransports() throws ProxyAdminException {
        Object[] transports = getAxisConfig().getTransportsIn().keySet().toArray();
        String[] ret = new String[transports.length];
        for (int i = 0; i < transports.length; i++) {
            ret[i] = (String) transports[i];
        }
        return ret;
    }

    /**
     * Get the available sequences from the SynapseConfiguration
     *
     * @return String array of available sequence names
     * @throws ProxyAdminException if there is an error
     */
    public String[] getAvailableSequences() throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            Object[] sequences = getSynapseConfiguration().getDefinedSequences().keySet().toArray();
            String[] ret = new String[sequences.length];
            for (int i = 0; i < sequences.length; i++) {
                ret[i] = (String) sequences[i];
            }
            return ret;
        } catch (Exception af) {
            handleException(log, "Unable to get available sequences", af);
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Get the available endpoints from the SynapseConfiguration
     *
     * @return String array of available endpoint names
     * @throws ProxyAdminException if there is an error
     */
    public String[] getAvailableEndpoints() throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            Object[] endpoints = getSynapseConfiguration().getDefinedEndpoints().keySet().toArray();
            String[] ret = new String[endpoints.length];
            for (int i = 0; i < endpoints.length; i++) {
                ret[i] = (String) endpoints[i];
            }
            return ret;
        } catch (Exception af) {
            handleException(log, "Unable to get available endpoints", af);
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Gets the endpoint object defined under the given name
     *
     * @param name the name of the endpoint
     * @return endpoint configuration related with the name
     * @throws ProxyAdminException if the endpoint is not found for the given name
     */
    public String getEndpoint(String name) throws ProxyAdminException{
        String epXML = null;
        final Lock lock = getLock();
        try {
            lock.lock();
            Endpoint ep = getSynapseConfiguration().getDefinedEndpoints().get(name);
            epXML = EndpointSerializer.getElementFromEndpoint(ep).toString();
        } catch (Exception axisFault) {
            handleException(log, "No endpoint defined by the name: " + name, axisFault);
        } finally {
            lock.unlock();
        }
        return epXML;
    }

    /**
     * Encapsulates the available transports, endpoints, and sequences into a single two dimensional array
     * @return  A two dimensional array containing the set of transports, endpoints, and sequences
     * under 0,1, and 2 indices.
     * @throws ProxyAdminException
     */
    public MetaData getMetaData() throws ProxyAdminException{
        final Lock lock = getLock();
        try {
            lock.lock();
            MetaData metaData = new MetaData();
            String[] arr = getAvailableTransports();
            if (arr.length != 0) {
                metaData.setTransportsAvailable(true);
                metaData.setTransports(arr);
            }
            arr = getAvailableEndpoints();
            if (arr.length != 0) {
                metaData.setEndpointsAvailable(true);
                metaData.setEndpoints(arr);
            }
            arr = getAvailableSequences();
            if (arr.length != 0) {
                metaData.setSequencesAvailable(true);
                metaData.setSequences(arr);
            }
            return metaData;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Starts the service specified by the name
     *
     * @param proxyName name of the proxy service which needs to be started
     * @throws ProxyAdminException in case of a failure in starting the service
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String startProxyService(String proxyName) throws ProxyAdminException {
        log.debug("Starting/Re-starting proxy service : " + proxyName);
        final Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            List pinnedServers = proxy.getPinnedServers();

            if (pinnedServers.isEmpty() ||
                    pinnedServers.contains(getServerConfigurationInformation().getServerName())) {

                proxy.start(getSynapseConfiguration());
            }
            if(log.isDebugEnabled()) {
                log.debug("Started/Re-started proxy service : " + proxyName);
            }
            return SUCCESSFUL;
        } catch (Exception af) {
            handleException(log, "Unable to start/re-start proxy service: " + proxyName, af);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    /**
     * Stops the service specified by the name
     *
     * @param proxyName name of the proxy service which needs to be stoped
     * @throws ProxyAdminException in case of a failure in stopping the service
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String stopProxyService(String proxyName) throws ProxyAdminException {
        log.debug("Stopping proxy service : " + proxyName);
        final Lock lock = getLock();
        try {
            lock.lock();

            ProxyService proxy = getSynapseConfiguration().getProxyService(proxyName);
            List pinnedServers = proxy.getPinnedServers();

            if (pinnedServers.isEmpty() || pinnedServers.contains(
                    getServerConfigurationInformation().getSynapseXMLLocation())) {

                proxy.stop(getSynapseConfiguration());
            }
            if(log.isDebugEnabled()) {
                log.debug("Stopped proxy service : " + proxyName);
            }
            return SUCCESSFUL;
        } catch (Exception af) {
            handleException(log, "Unable to stop proxy service : " + proxyName, af);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    /**
     * Redeploying service
     * Removes an existing one,Adds a new one
     *
     * @param proxyName name of the proxy service which needs to be redeployed
     * @throws ProxyAdminException in case of a failure in redeploying the service
     * @return <code>successful</code> on success or <code>failed</code> otherwise
     */
    public String redeployProxyService(String proxyName) throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();

            ProxyService currentProxy = getSynapseConfiguration().getProxyService(proxyName);
            if (currentProxy != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Redeploying proxy service : " + proxyName);
                }
                OMElement proxyElement = ProxyServiceSerializer.serializeProxy(null, currentProxy);
                modifyProxyService(proxyElement);
                if(log.isDebugEnabled()) {
                    log.debug("Redeployed proxy service : " + proxyName);
                }
                return SUCCESSFUL;
            }
        } catch (Exception af) {
            handleException(log, "Unable to redeploy proxy service : " + proxyName, af);
        } finally {
            lock.unlock();
        }
        return FAILED;
    }

    public String getSourceView(ProxyData pd) throws ProxyAdminException {
        return pd.retrieveOM().toString();
    }

    public ProxyData getProxy(String proxyName) throws ProxyAdminException {
        final Lock lock = getLock();
        try {
            lock.lock();
            ProxyService ps = proxyForName(proxyName);
            return generateProxyDataFor(ps);
        } finally {
            lock.unlock();
        }
    }

    public String addProxy(ProxyData pd) throws ProxyAdminException {
        // todo - at the moment I get the OMElement from the pd and asks the private method to build the proxy service
        // todo - but I could improve this by creating a proxy service from the pd itself. Not for this release :)
        final Lock lock = getLock();
        try {
            lock.lock();

            addProxyService(pd.retrieveOM(), null, false);
            return SUCCESSFUL;
        } finally {
            lock.unlock();
        }
    }

    public String modifyProxy(ProxyData pd) throws ProxyAdminException {
        // todo - see todo of addProxy
        final Lock lock = getLock();
        try {
            lock.lock();

            modifyProxyService(pd.retrieveOM());
            return SUCCESSFUL;
        } finally {
            lock.unlock();
        }
    }

    private ProxyService proxyForName(String proxyName) throws ProxyAdminException {
        try {
            ProxyService ps = getSynapseConfiguration().getProxyService(proxyName);
            if (ps != null) {
                return ps;
            } else {
                handleException(log, "A proxy service named : "
                        + proxyName + " does not exist", null);
            }
        } catch (Exception af) {
            handleException(log, "Unable to get the proxy service definition for : "
                    + proxyName, af);
        }
        return null;
    }

    private ProxyData generateProxyDataFor(ProxyService ps) throws ProxyAdminException {
        ProxyData pd = new ProxyData();
        pd.setName(ps.getName());

        // sets status, i.e. whether running/stop, statistics on/off, tracing on/off,
        // wsdl available/unavilable, startOnLoad true/false
        pd.setRunning(ps.isRunning());
        if (ps.getAspectConfiguration() != null
                && ps.getAspectConfiguration().isStatisticsEnable()) {
            pd.setEnableStatistics(true);
        } else {
            pd.setEnableStatistics(false);
        }
        if (ps.getTraceState() == SynapseConstants.TRACING_ON) {
            pd.setEnableTracing(true);
        } else if (ps.getTraceState() == SynapseConstants.TRACING_OFF) {
            pd.setEnableTracing(false);
        }
        if (ps.getWsdlURI() != null ||
                ps.getWSDLKey() != null || ps.getInLineWSDL() != null || ps.getPublishWSDLEndpoint() != null) {
            pd.setWsdlAvailable(true);
        } else {
            pd.setWsdlAvailable(false);
        }
        if (ps.isStartOnLoad()) {
            pd.setStartOnLoad(true);
        } else {
            pd.setStartOnLoad(false);
        }

        String artifactName = getArtifactName(artifactType, ps.getName());
        CAppArtifactDataService cAppArtifactDataService = ConfigHolder.getInstance().
                getcAppArtifactDataService();

        if (cAppArtifactDataService.isArtifactDeployedFromCApp(getTenantId(), artifactName)) {
            pd.setDeployedFromCApp(true);
        }
        if (cAppArtifactDataService.isArtifactEdited(getTenantId(), artifactName)) {
            pd.setEdited(true);
        }

        // sets transports
        List list;
        if ((list = ps.getTransports()) != null && !list.isEmpty()) {
            String [] arr = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = (String)list.get(i);
            }
            pd.setTransports(arr);
        }

        // sets pinned servers (if any)
        if ((list = ps.getPinnedServers()) != null && !list.isEmpty()) {
            String [] arr = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = (String)list.get(i);
            }
            pd.setPinnedServers(arr);
        }

        if (ps.getServiceGroup() != null) {
            pd.setServiceGroup(ps.getServiceGroup());
        }

        if (ps.getDescription() != null) {
            pd.setDescription(ps.getDescription());
        }

        SequenceMediatorSerializer seqMedSerializer = new SequenceMediatorSerializer();
        // sets target in sequence
        if (ps.getTargetInSequence() != null) {
            pd.setInSeqKey(ps.getTargetInSequence());
        } else if (ps.getTargetInLineInSequence() != null) {
            OMElement inSeq = seqMedSerializer.serializeAnonymousSequence(
                    null, ps.getTargetInLineInSequence());
            inSeq.setLocalName("inSequence");
            pd.setInSeqXML(inSeq.toString());
        }

        // sets target out sequence
        if (ps.getTargetOutSequence() != null) {
            pd.setOutSeqKey(ps.getTargetOutSequence());
        } else if (ps.getTargetInLineOutSequence() != null) {
            OMElement outSeq = seqMedSerializer.serializeAnonymousSequence(
                    null, ps.getTargetInLineOutSequence());
            outSeq.setLocalName("outSequence");
            pd.setOutSeqXML(outSeq.toString());
        }

        // sets fault sequence
        if (ps.getTargetFaultSequence() != null) {
            pd.setFaultSeqKey(ps.getTargetFaultSequence());
        } else if (ps.getTargetInLineFaultSequence() != null) {
            OMElement faultSeq = seqMedSerializer.serializeAnonymousSequence(
                    null, ps.getTargetInLineFaultSequence());
            faultSeq.setLocalName("faultSequence");
            pd.setFaultSeqXML(faultSeq.toString());
        }

        // sets endpoint
        if (ps.getTargetEndpoint() != null) {
            pd.setEndpointKey(ps.getTargetEndpoint());
        } else if (ps.getTargetInLineEndpoint() != null) {
            pd.setEndpointXML(EndpointSerializer.getElementFromEndpoint(
                    ps.getTargetInLineEndpoint()).toString());
        }

        // sets publish WSDL
        if (pd.isWsdlAvailable()) {
            if (ps.getWSDLKey() != null) {
                pd.setWsdlKey(ps.getWSDLKey());
            } else if (ps.getWsdlURI() != null) {
                pd.setWsdlURI(ps.getWsdlURI().toString());
            } else if (ps.getInLineWSDL() != null) {
                pd.setWsdlDef(ps.getInLineWSDL().toString());
            } else if (ps.getPublishWSDLEndpoint() != null) {
                pd.setPublishWSDLEndpoint(ps.getPublishWSDLEndpoint().toString());
            }
            Map <String, String> map;
            if (ps.getResourceMap() != null
                    && (map = ps.getResourceMap().getResources()) != null && !map.isEmpty()) {
                Entry [] entries = new Entry[map.size()];
                int i = 0;
                for (Map.Entry<String,String> key : map.entrySet()) {
                    entries[i] = new Entry(key.getKey(), key.getValue());
                    i++;
                }
                pd.setWsdlResources(entries);
            }
        }

        // sets additional service parameters
        Map <String, Object> map;
        if ((map = ps.getParameterMap()) != null && !map.isEmpty()) {
            Entry [] entries = new Entry[map.size()];
            int i = 0;
            Object o;
            for (Map.Entry<String,Object> key : map.entrySet()){
                o = key.getValue();
                if (o instanceof String) {
                    entries[i] = new Entry(key.getKey(), (String)o);
                    i++;
                } else if (o instanceof OMElement) {
                    entries[i] = new Entry(key.getKey(), o.toString());
                    i++;
                }
            }
            pd.setServiceParams(entries);
        }

        if (ps.isWsSecEnabled()) {
            pd.setEnableSecurity(true);
        }

        if (ps.getPolicies() != null && ps.getPolicies().size() > 0) {
            List<ProxyServicePolicyInfo> policies = new ArrayList<ProxyServicePolicyInfo>();
            for (PolicyInfo policyInfo : ps.getPolicies()) {
                if (policyInfo.getPolicyKey() != null) {
                    ProxyServicePolicyInfo policy = new ProxyServicePolicyInfo();
                    policy.setKey(policyInfo.getPolicyKey());
                    if (policyInfo.getType() != 0) {
                        policy.setType(policyInfo.getMessageLable());
                    }
                    if (policyInfo.getOperation() != null) {
                        policy.setOperationName(policyInfo.getOperation().getLocalPart());
                        if (policyInfo.getOperation().getNamespaceURI() != null) {
                            policy.setOperationNS(policyInfo.getOperation().getNamespaceURI());
                        }
                    }
                    policies.add(policy);

                } else {
                    throw new ProxyAdminException("A policy without a key was found on the " +
                            "proxy service : " + ps.getName());
                }
            }
            pd.setPolicies(policies.toArray(new ProxyServicePolicyInfo[policies.size()]));
        }
        return pd;
    }

    /**
     * Register a ProxyServiceParameterObserver for the given AxisService
     * @param serviceName
     * @throws AxisFault
     */
    private void addParameterObserver(String serviceName) throws AxisFault {
        AxisService service = getAxisConfig().getService(serviceName);
        ProxyServiceParameterObserver paramObserver =
                new ProxyServiceParameterObserver(service);
        service.addParameterObserver(paramObserver);
    }

    public void persistProxyService(ProxyService proxy) throws ProxyAdminException {
        MediationPersistenceManager pm = getMediationPersistenceManager();
        if (pm != null) {
            pm.saveItem(proxy.getName(), ServiceBusConstants.ITEM_TYPE_PROXY_SERVICE);
        }
    }



    /**
       * This will keep the Synapse proxy params and Axis2 service params in Sync with the registry
       * @param service
       * @param axisService
       * @throws ProxyAdminException
       * @throws AxisFault
       */
      private void updateAndSyncServiceParameters(
              ProxyService service,
              AxisService axisService) throws ProxyAdminException, AxisFault{

          String servicePath = RegistryResources.ROOT + "axis2" +
                  RegistryConstants.PATH_SEPARATOR + "service-groups" +
                  RegistryConstants.PATH_SEPARATOR +
                  axisService.getAxisServiceGroup().getServiceGroupName() +
                  RegistryConstants.PATH_SEPARATOR + "services" +
                  RegistryConstants.PATH_SEPARATOR + axisService.getName();

        String serviceParametersPath = servicePath + RegistryConstants.PATH_SEPARATOR + "parameters";

        Registry registry = null;
        try {
            registry = ConfigHolder.getInstance().getRegistryService().getConfigSystemRegistry();
        } catch (RegistryException e) {
            handleException(log,"Error while accessing the Registry",e);
        }

        try {
              // delete the persisted parameters
              Map<String, Object> params = service.getParameterMap();

              if (registry.resourceExists(serviceParametersPath)) {
                  // there are service level parameters
                  Resource serviceParamsResource = registry.get(serviceParametersPath);
                  if (serviceParamsResource instanceof Collection) {
                      Collection serviceParamsCollection =
                              (Collection) serviceParamsResource;
                      for (String serviceParamResourcePath :
                              serviceParamsCollection.getChildren()) {

                          String[] elems = serviceParamResourcePath.split(
                                  RegistryConstants.PATH_SEPARATOR);
                          String name = elems[elems.length - 1];
                          if (!SynapseConstants.SERVICE_TYPE_PARAM_NAME.equals(name)) {
                              registry.delete(serviceParamResourcePath);
                          }

                      }
                  }
              }

              //set the proxy params to the axis2 service
            Iterator<Map.Entry<String,Object>> it = params.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String,Object> entry = it.next();
                axisService.addParameter(entry.getKey(),entry.getValue());
            }

          } catch (RegistryException e) {
              handleException(log,"Error while accessing the Registry" ,e);
          }
      }


    private void handleException(Log log, String message, Exception e) throws ProxyAdminException {
        if (e == null) {
            ProxyAdminException paf = new ProxyAdminException(message);
            log.error(message, paf);
            throw paf;
        } else {
            message = message + " :: " + e.getMessage();
            log.error(message, e);
            throw new ProxyAdminException(message, e);
        }
    }

}
