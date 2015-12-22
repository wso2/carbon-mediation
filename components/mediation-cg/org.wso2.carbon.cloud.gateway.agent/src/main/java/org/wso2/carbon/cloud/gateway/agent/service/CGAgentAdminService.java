/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.gateway.agent.service;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cloud.gateway.agent.CGAgentPollingTaskFlags;
import org.wso2.carbon.cloud.gateway.agent.CGAgentUtils;
import org.wso2.carbon.cloud.gateway.agent.client.AuthenticationClient;
import org.wso2.carbon.cloud.gateway.agent.client.CGAdminClient;
import org.wso2.carbon.cloud.gateway.agent.transport.CGPollingTransportReceiver;
import org.wso2.carbon.cloud.gateway.agent.transport.CGPollingTransportSender;
import org.wso2.carbon.cloud.gateway.common.CGConstant;
import org.wso2.carbon.cloud.gateway.common.CGException;
import org.wso2.carbon.cloud.gateway.common.CGServerBean;
import org.wso2.carbon.cloud.gateway.common.CGUtils;
import org.wso2.carbon.cloud.gateway.common.thrift.CGThriftClient;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGProxyToolsURLs;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGServiceMetaDataBean;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGThriftServerBean;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.ServicePersistenceManager;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.core.transports.util.TransportSummary;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.cloud.gateway.agent.CGAgentWsdlDependencyResolver;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGServiceDependencyBean;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The class <code>CGAgentAdminService</code> provides the admin service to manipulate the
 * CGAgent remotely
 */
public class CGAgentAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(CGAgentAdminService.class);

    /**
     * Deploy the proxy service
     *
     * @param serviceName the service to deploy
     * @param serverName  the serverName to publish
     * @param isAutomatic the mode of service publishing
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public void publishService(String serviceName, String serverName, boolean isAutomatic) throws CGException {
        try {
            CGServerBean csgServer = getCGServerBean(serverName);
            if (csgServer == null) {
                handleException("No persist information found for the server'" + serverName + "'");
            }

            if (getAxisConfig().getService(serviceName) == null) {
                handleException("AxisService is null for service '" + serviceName + "'");
            }

            CGAdminClient csgAdminClient = getCGAdminClient(csgServer);
            if (csgAdminClient == null) {
                handleException("CGAdminClient is null");
            }
            handleServicePublishing(serviceName, serverName, isAutomatic, csgAdminClient, csgServer);
            // deploy proxy
            CGServiceMetaDataBean cgServiceMetaData = getCGServiceMetaData(getAxisConfig().getService(serviceName),
                                                                           csgServer.getDomainName(), serverName);
            csgAdminClient.deployProxy(cgServiceMetaData);
            flagServiceStatus(serviceName, serverName, cgServiceMetaData, true, isAutomatic);
            AxisService service = getAxisConfig().getService(serviceName);
            service.removeExposedTransport(CGConstant.CG_POLLING_TRANSPORT_NAME);
        } catch (Exception e) {
            handleException("Could not publish service '" + serviceName + "'. " + e.getMessage(), e);
        }
    }

    public void rePublishService(String serviceName, String serverName, boolean isAutomatic) throws CGException {
        try {
            CGServerBean csgServer = getCGServerBean(serverName);
            if (csgServer == null) {
                handleException("No persist information found for the server'" + serverName + "'");
            }

            CGAdminClient csgAdminClient = getCGAdminClient(csgServer);
            if (csgAdminClient == null) {
                handleException("CGAdminClient is null");
            }
            handleServicePublishing(serviceName, serverName, isAutomatic, csgAdminClient, csgServer);
            flagServiceStatus(serviceName, serverName, null, true, isAutomatic);
        } catch (CGException e) {
            handleException("Cloud not republish service '" + serviceName + "'", e);
        }
    }

    /**
     * Un-deploy the proxy service
     *
     * @param serviceName the service to un-deploy
     * @param serverName  the server name to publish
     * @param isCheckBackend check the backend availability
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public void unPublishService(String serviceName, String serverName, boolean isCheckBackend) throws CGException {
        if (serviceName == null) {
            handleException("The service name is not supplied for un-publishing");
        }
        try {
            AxisService service = getAxisConfig().getService(serviceName);
            if (service == null) {
                handleException("No service is found with the name '" + serviceName + "'");
            }
            service.removeExposedTransport(CGConstant.CG_POLLING_TRANSPORT_NAME);
            CGServerBean csgServer = getCGServerBean(serverName);
            if (csgServer == null) {
                throw new CGException("No CG server information found with the name '" +
                        serverName + "'");
            }
            CGAdminClient csgAdminClient = getCGAdminClient(csgServer);
            if (csgAdminClient == null && isCheckBackend) {
                handleException("CGAdminClient is null");
            }
            // flag this service's polling task for shutdown
            CGAgentPollingTaskFlags.flagForShutDown(serviceName, true);
            if (csgAdminClient != null) {
                csgAdminClient.unDeployProxy(serviceName);
            } else {
                log.warn("Could not un-publish the service in the remote CG server, You need to manually un-publish ");
            }
            flagServiceStatus(serviceName, serverName, null, false, false);
        } catch (Exception e) {
            handleException("Could not un-publish the service '" + serviceName + "'", e);
        }
    }

    /**
     * Add a new CG server and store it in registry
     *
     * @param csgServer new csg server instance
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public void addCGServer(CGServerBean csgServer) throws CGException {
        try {
            // authenticate using provided credentials and if logged in persist the server
            loggingToRemoteCGServer(csgServer);

            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            CGAgentUtils.persistServer(registry, csgServer);
        } catch (Exception e) {
            handleException("Could not add CG server '" + csgServer.getName() + "'. Error is " +
                    e.getMessage(), e);
        }
    }

    /**
     * Get the CG server given by the name
     *
     * @param csgServerName csg server name
     * @return the csg server instance
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public CGServerBean getCGServer(String csgServerName) throws CGException {
        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
        String resourcePath = CGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServerName;
        try {
            if (registry != null && registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                return CGAgentUtils.getCGServerBean(resource);
            }
        } catch (RegistryException e) {
            handleException("Could not read the registry resource '" + resourcePath + "'. Error is " +
                    e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get the set of CSG servers
     *
     * @return the list of CG_TRANSPORT_NAME servers
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public CGServerBean[] getCGServerList() throws CGException {
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            if (registry != null && registry.resourceExists(CGConstant.REGISTRY_SERVER_RESOURCE_PATH)) {
                Resource resource = registry.get(CGConstant.REGISTRY_SERVER_RESOURCE_PATH);
                if (resource instanceof Collection) {
                    Collection collection = (Collection) resource;
                    int size = collection.getChildCount();
                    CGServerBean[] beanInfo = new CGServerBean[size];
                    String[] child = collection.getChildren();
                    for (int i = 0; child.length > i; i++) {
                        String s = child[i]; // returns the set of path
                        Resource childResource = registry.get(s);
                        beanInfo[i] = CGAgentUtils.getCGServerBean(childResource);
                    }
                    return beanInfo;
                }
            }
        } catch (RegistryException e) {
            handleException("Could not retrieve the CSG server list. Error is " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Update the CSG server and persist new information into registry
     *
     * @param csgServer new csg server information
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public void updateCGServer(CGServerBean csgServer) throws CGException {

        // check if the new user can log in
        loggingToRemoteCGServer(csgServer);

        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;

        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
        String resource = CGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServer.getName();
        try {
            if (!isTransactionAlreadyStarted) {
                // start a transaction only if we are not in one.
                registry.beginTransaction();
            }

            if (registry != null && registry.resourceExists(resource)) {
                // delete the resource and add it again
                registry.delete(resource);
                CGAgentUtils.persistServer(registry, csgServer);
            }

        } catch (RegistryException e) {
            isTransactionSuccess = false;
            handleException("Could not read the registry resource '" + resource + "'. Error is "
                    + e.getMessage(), e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                // commit or rollback the transaction since we started it.
                try {
                    if (isTransactionSuccess) {
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }

                } catch (RegistryException re) {
                    handleException("Error occurred while trying to rollback or commit the " +
                            "transaction", re);
                }
            }
        }
    }

    /**
     * Remove the CSG server given by the name
     *
     * @param csgServerName the csg server name
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public void removeCGServer(String csgServerName) throws CGException {

        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;

        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();

        try {

            if (!isTransactionAlreadyStarted) {
                // start a transaction only if we are not in one.
                registry.beginTransaction();
            }

            String resource = CGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServerName;
            if (registry != null && registry.resourceExists(resource)) {
                if (!isHasPublishedServices(csgServerName)) {
                    registry.delete(resource);
                } else {
                    handleException(csgServerName + " has services published onto it.");
                }

            } else {
                log.error("The resource '" + resource + "' does not exist!");
            }

        } catch (Exception e) {
            isTransactionSuccess = false;
            handleException("Could not remove the CSG server: " + csgServerName + ". Error is " +
                    e.getMessage(), e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        // commit the transaction since we started it.
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException re) {
                    handleException("Error occurred while trying to rollback or commit " +
                            "the transaction", re);
                }
            }
        }
    }

    /**
     * Returns the status of the service
     *
     * @param serviceName service name
     * @return a string states representing the service status
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public String getServiceStatus(String serviceName) throws CGException {
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            String resourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName +
                    ".flag";
            if (registry != null && registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                return new String((byte[]) resource.getContent());
            }
        } catch (Exception e) {
            handleException("Could not retrieve the service publish flag for service '" +
                    serviceName + "'", e);
        }
        return CGConstant.CG_SERVICE_STATUS_UNPUBLISHED;
    }

    public void setServiceStatus(String serviceName, String status) throws CGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;

        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();

        try {
            if (!isTransactionAlreadyStarted) {
                // start a new transaction if there exists none.
                registry.beginTransaction();
            }

            org.wso2.carbon.registry.core.Resource resource = registry.newResource();
            resource.setContent(status);
            registry.put(CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".flag",
                    resource);
        } catch (Exception e) {
            isTransactionSuccess = false;
            handleException("Could not retrieve the service publish flag for service '" +
                    serviceName + "'", e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        // commit the transaction since we started it.
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException re) {
                    handleException("Error occurred while trying to rollback or " +
                            "commit the transaction", re);
                }
            }
        }
    }

    public void doServiceUpdate(String serviceName, int eventType) throws CGException {
        String publishedServer = getPublishedServer(serviceName);
        if (publishedServer != null) {
            CGServerBean csgServer = getCGServerBean(publishedServer);
            if (csgServer == null) {
                handleException("No persist information found for the server'" + publishedServer + "'");
            }
            try {
                if (eventType == AxisEvent.SERVICE_REMOVE) {
                    CGAdminClient csgAdminClient = getCGAdminClient(csgServer);
                    if (csgAdminClient == null) {
                        handleException("CGAdminClient is null");
                    }
                    // flag this service's polling task for shutdown
                    CGAgentPollingTaskFlags.flagForShutDown(serviceName, true);
                    csgAdminClient.unDeployProxy(serviceName);
                    flagServiceStatus(serviceName, publishedServer, null, false, false);
                }
            } catch (Exception e) {
                handleException("Cloud not update service the service '" + serviceName + "'");
            }
        }
    }

    /**
     * Get the server that this service has published to
     *
     * @param serviceName service name
     * @return the server that this service has published to
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          throws in case of an error
     */
    public String getPublishedServer(String serviceName) throws CGException {
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            String serverResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName
                    + ".server";
            if (registry != null && registry.resourceExists(serverResourcePath)) {
                Resource serverResource = registry.get(serverResourcePath);
                if (serverResource != null && serverResource.getContent() != null) {
                    return new String((byte[]) serverResource.getContent());
                }
            }
        } catch (RegistryException e) {
            handleException("Could not retrieve the published server list. Error is " +
                    e.getMessage(), e);
        }
        return null;
    }

    public CGProxyToolsURLs getPublishedProxyToolsURLs(String serviceName) throws CGException {
        try {
            CGServerBean bean = getCGServer(getPublishedServer(serviceName));
            if (bean == null) {
                handleException("No persist server information found for the published service '"
                        + serviceName + "'");
            }
            String domainName = bean.getDomainName();
            CGAdminClient csgAdminClient = getCGAdminClient(bean);
            if (csgAdminClient == null) {
                handleException("CGAdminClient is null");
            }
            CGProxyToolsURLs tools =
                    csgAdminClient.getPublishedProxyToolsURLs(serviceName, domainName);
            CGProxyToolsURLs tempTools = new CGProxyToolsURLs();
            if (tools != null) {
                tempTools.setTryItURL(tools.getTryItURL());
                tempTools.setWsdl11URL(tools.getWsdl11URL());
                tempTools.setWsdl2URL(tools.getWsdl2URL());
                tempTools.setEprArray(tools.getEprArray());

                return tempTools;
            }
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Determines whether at least a one service has been published to specified CSG server.
     *
     * @param csgServerName Name of the CSG sever
     * @return true if there is at least a one service published to the CSG server or false otherwise.
     * @throws org.wso2.carbon.cloud.gateway.common.CGException
     *          in case of an error
     */
    public boolean isHasPublishedServices(String csgServerName) throws CGException {

        boolean isHasServices = false;
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            if (registry != null && registry.resourceExists(CGConstant.REGISTRY_FLAG_RESOURCE_PATH)) {
                Resource resource = registry.get(CGConstant.REGISTRY_FLAG_RESOURCE_PATH);

                if (resource instanceof Collection) {
                    Collection serviceFlagCollection = (Collection) resource;
                    String[] flags = serviceFlagCollection.getChildren();

                    List<String> serverFlagPaths = filterForServiceFlags(flags);

                    for (String serverFlagPath : serverFlagPaths) {

                        Resource serverFlag = registry.get(serverFlagPath);
                        String publishedServer =
                                serverFlag.getContent() != null ?
                                        new String((byte[]) serverFlag.getContent())
                                        : "";
                        if (csgServerName.equals(publishedServer)) {
                            // found at least one service, published to specified server
                            isHasServices = true;
                            break;
                        }
                    }

                }

            }

        } catch (Exception e) {
            handleException("Unable to retrieve CSG services configurations", e);
        }
        return isHasServices;
    }

    private void handleServicePublishing(String serviceName,
                                         String serverName,
                                         boolean isAutomatic,
                                         CGAdminClient csgAdminClient,
                                         CGServerBean csgServer) throws CGException {
        if (serviceName == null) {
            handleException("Service name is null!");
        }
        try {
            AxisService service = getAxisConfig().getService(serviceName);
            if (service == null) {
                handleException("No service found with the name '" + serviceName + "'");
            }

            service.addExposedTransport(CGConstant.CG_POLLING_TRANSPORT_NAME);
            String domainName = csgServer.getDomainName();
            String passWord = csgServer.getPassWord();
            String userName = CGUtils.getFullUserName(csgServer.getUserName(), domainName);

            CGThriftServerBean bean = csgAdminClient.getThriftServerConnectionBean();

            // thrift server either bind to loop back address(most of the time "localhost") or it can bound to
            // ip or host name but not both so below is used
            String hostName = csgServer.getHost();
            if ("localhost".equals(hostName) || "127.0.0.1".equals(hostName)) {
                hostName = bean.getHostName(); // bean is the remote thrift server information
            } else {
                bean.setHostName(hostName);
            }
            int port = bean.getPort();
            int timeOut = bean.getTimeOut();

            String trustStorePath = CGUtils.getTrustStoreFilePath();
            String trustStorePassword = CGUtils.getTrustStorePassWord();
            if (log.isDebugEnabled()) {
                log.debug("Loading the trust store from the location '" + trustStorePath + "'");
            }
            // get a token for this service
            CGThriftClient csgThriftClient =
                    new CGThriftClient(CGUtils.getCGThriftClient(
                            hostName, port, timeOut, trustStorePath, trustStorePassword));
            // we use the CSG EPR as the key of the buffer
            String queueName = CGUtils.getCGEPR(domainName, serverName, serviceName);
            String token = csgThriftClient.login(userName, passWord, queueName);

            // encrypt and embed, so nobody can steal
            persistToken(serviceName, token);
            persistCGServerDetails(serviceName, bean);

            CGAgentPollingTaskFlags.flagForShutDown(serviceName, false);
            if (hasInOutOperations(service)) {
                // enable CSG Thrift transport sender as well
                // FIXME - need to persist the configured transport
                enableCGPollingTransportSender(getAxisConfig());
            }
            // enable CSG transport receiver for this service
            // FIXME - need to persist the configured transport
            enableCGPollingTransportReceiver(getAxisConfig());
        } catch (Exception e) {
            throw new CGException(e);
        }
    }

    private void handleException(String msg) throws CGException {
        log.error(msg);
        throw new CGException(msg);
    }

    private void handleException(String msg, Throwable t) throws CGException {
        log.error(msg, t);
        throw new CGException(msg, t);
    }

    private CGServerBean getCGServerBean(String csgServerName) throws CGException {
        CGServerBean bean = null;
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            String resourceName = CGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServerName;
            if (registry != null && registry.resourceExists(resourceName)) {
                org.wso2.carbon.registry.core.Resource resource = registry.get(resourceName);
                try {
                    bean = new CGServerBean();
                    bean.setHost(resource.getProperty(CGConstant.CG_SERVER_HOST));
                    bean.setName(resource.getProperty(CGConstant.CG_SERVER_NAME));
                    bean.setUserName(resource.getProperty(CGConstant.CG_SERVER_USER_NAME));
                    bean.setPort(resource.getProperty(CGConstant.CG_SERVER_PORT));
                    bean.setDomainName(resource.getProperty(CGConstant.CG_SERVER_DOMAIN_NAME));

                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

                    bean.setPassWord(new String(cryptoUtil.base64DecodeAndDecrypt(
                            resource.getProperty("password"))));
                } catch (CryptoException e) {
                    handleException("Could not convert into an AXIOM element");
                }
            } else {
                throw new CGException("Resource :" + resourceName + " does not exist");
            }
        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            handleException("Could not retrieve the server information for server: " +
                    csgServerName, e);
        }
        return bean;
    }

    private static String getAuthServiceURL(CGServerBean csgServer) {
        return "https://" + csgServer.getHost() + ":" + csgServer.getPort() +
                "/services/AuthenticationAdmin";
    }

    private static String getProxyURL(CGServerBean csgServer) {
        return "https://" + csgServer.getHost() + ":" + csgServer.getPort() + "/services/";
    }

    private static boolean hasInOutOperations(AxisService service) {
        for (Iterator<AxisOperation> axisOpItr = service.getOperations(); axisOpItr.hasNext(); ) {
            AxisOperation axisOp = axisOpItr.next();
            if (axisOp.getAxisSpecificMEPConstant() == WSDLConstants.MEP_CONSTANT_IN_OUT) {
                return true;
            }
        }
        return false;
    }

    private void flagServiceStatus(String serviceName, String serverName, CGServiceMetaDataBean cgServiceMetaData,
                                   boolean isPublished,
                                   boolean isAutoMatic)
            throws CGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;
        Registry registry = getConfigSystemRegistry();
        try {

            if (!isTransactionAlreadyStarted) {
                registry.beginTransaction(); // start a transaction if none exists currently.
            }

            if (registry != null && !registry.resourceExists(CGConstant.REGISTRY_CG_RESOURCE_PATH)) {
                org.wso2.carbon.registry.core.Collection collection = registry.newCollection();
                registry.put(CGConstant.REGISTRY_CG_RESOURCE_PATH, collection);
            }

            org.wso2.carbon.registry.core.Resource resource = registry.newResource();
            org.wso2.carbon.registry.core.Resource serverResource = registry.newResource();
            String serverResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".server";
            String wsdlResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".wsdl";
            String cgServerResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".cgserver";
            String tokenResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".token";

            if (isPublished) {
                if (isAutoMatic) {
                    resource.setContent(CGConstant.CG_SERVICE_STATUS_AUTO_MATIC);
                } else {
                    resource.setContent(CGConstant.CG_SERVICE_STATUS_PUBLISHED);
                }

                serverResource.setContent(serverName);
                if (cgServiceMetaData != null && cgServiceMetaData.getInLineWSDL() != null) {
                    org.wso2.carbon.registry.core.Resource wsdlResource = registry.newResource();
                    wsdlResource.setContent(cgServiceMetaData.getInLineWSDL());
                    registry.put(wsdlResourcePath, wsdlResource);
                }
            } else {
                resource.setContent(CGConstant.CG_SERVICE_STATUS_UNPUBLISHED);
                // remove the published server from the list
                if (registry.resourceExists(serverResourcePath)) {
                    registry.delete(serverResourcePath);
                }
                if (registry.resourceExists(wsdlResourcePath)) {
                    registry.delete(wsdlResourcePath);
                }
                if (registry.resourceExists(cgServerResourcePath)) {
                    registry.delete(cgServerResourcePath);
                }
                if (registry.resourceExists(tokenResourcePath)) {
                    registry.delete(tokenResourcePath);
                }
            }
            registry.put(CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".flag",
                    resource);
            registry.put(serverResourcePath, serverResource);

        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            isTransactionSuccess = false;
            handleException("Could not flag the service '" + serviceName + "'", e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (Exception exception) {
                    handleException("Error occurred while trying to rollback or commit the " +
                            "transaction", exception);
                }
            }
        }
    }

    private CGServiceMetaDataBean
    getCGServiceMetaData(AxisService service, String tenantName, String serverName)
            throws CGException {
        try {
            CGServiceMetaDataBean privateServiceMetaData
                    = new org.wso2.carbon.cloud.gateway.stub.types.common.CGServiceMetaDataBean();
            privateServiceMetaData.setServiceName(service.getName());

            privateServiceMetaData.setEndpoint(
                    CGUtils.getCGEPR(tenantName, serverName, service.getName()));

            ServiceAdmin serviceAdmin = new ServiceAdmin(getAxisConfig());
            org.wso2.carbon.service.mgt.ServiceMetaData serviceAdminMetaData =
                    serviceAdmin.getServiceData(service.getName());

            if (serviceAdminMetaData.isActive()) {
                // Transmit dependencies to CSG server if this service has any.
                List<CGServiceDependencyBean> dependencies = new ArrayList<CGServiceDependencyBean>();
                CGAgentWsdlDependencyResolver dependencyResolver =
                        new CGAgentWsdlDependencyResolver(service, serviceAdminMetaData.getWsdlURLs()[0]);
                OMElement adjustedWsdl = null;
                String persistedWsdlContent = null;
                try {
                    adjustedWsdl = dependencyResolver.parseWsdlDependencies(dependencies);
                } catch (CGException e) {
                    persistedWsdlContent = getPersistedWsdlContent(service.getName());
                }

                if (!dependencies.isEmpty()) {
                    // Service has dependencies
                    privateServiceMetaData.setServiceDependencies(
                            dependencies.toArray(new CGServiceDependencyBean[dependencies.size()]));
                }

                if (adjustedWsdl == null && persistedWsdlContent == null) {
                    privateServiceMetaData.setInLineWSDL(null);
                } else if (adjustedWsdl != null && persistedWsdlContent == null) {
                    String wsdlString = adjustedWsdl.toStringWithConsume();
                    if (log.isDebugEnabled()) {
                        log.debug("Adjusted wsdl : " + wsdlString);
                    }
                    privateServiceMetaData.setInLineWSDL(wsdlString);
                } else {
                    privateServiceMetaData.setInLineWSDL(persistedWsdlContent);
                }
            }

            populateExposedTransports(service, privateServiceMetaData);
            if (hasInOutOperations(service)) {
                privateServiceMetaData.setHasInOutMEP(true);
            }
            return privateServiceMetaData;
        } catch (Exception e) {
            handleException("Error while retrieving the meta data of the service '" +
                    service.getName() + "'", e);
        }
        return null;
    }

    private void populateExposedTransports(AxisService service, CGServiceMetaDataBean privateServiceMetaData) {
        if (service.getExposedTransports() != null && service.getExposedTransports().size() > 0) {
            List<String> exposedTransports = service.getExposedTransports();
            exposedTransports.remove(CGConstant.CG_POLLING_TRANSPORT_NAME);
            privateServiceMetaData.setEnabledTransports(exposedTransports.toArray(new String[exposedTransports.size()]));
        }
    }

    private String getPersistedWsdlContent(String serviceName) throws Exception {
        Registry registry = getConfigSystemRegistry();
        String wsdlResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".wsdl";
        if (registry.resourceExists(wsdlResourcePath)) {
            Resource resource = registry.get(wsdlResourcePath);

            if (resource != null && resource.getContent() != null) {
                return IOUtils.toString(resource.getContentStream());
            }
        }
        return null;
    }

    private void persistCGServerDetails(String serviceName, CGThriftServerBean thriftConnectionBean) throws Exception {
        if (thriftConnectionBean == null) {
            handleException("Remote CG server information not found");
        }
        Registry registry = getConfigSystemRegistry();
        if (!registry.resourceExists(CGConstant.REGISTRY_CG_RESOURCE_PATH)) {
            org.wso2.carbon.registry.core.Collection collection = registry.newCollection();
            registry.put(CGConstant.REGISTRY_CG_RESOURCE_PATH, collection);
        }
        String cgServerResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".cgserver";
        org.wso2.carbon.registry.core.Resource cgServerResource = registry.newResource();
        cgServerResource.setContent(toString(thriftConnectionBean));
        registry.put(cgServerResourcePath, cgServerResource);
    }

    private void persistToken(String serviceName, String token) throws Exception {
        if (token == null) {
            handleException("Token cannot be null");
        }

        Registry registry = getConfigSystemRegistry();
        if (!registry.resourceExists(CGConstant.REGISTRY_CG_RESOURCE_PATH)) {
            org.wso2.carbon.registry.core.Collection collection = registry.newCollection();
            registry.put(CGConstant.REGISTRY_CG_RESOURCE_PATH, collection);
        }
        String tokenResourcePath = CGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".token";
        org.wso2.carbon.registry.core.Resource tokenResource = registry.newResource();
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        tokenResource.setContent(cryptoUtil.encryptAndBase64Encode(token.getBytes()));
        registry.put(tokenResourcePath, tokenResource);
    }

    private CGAdminClient getCGAdminClient(CGServerBean bean) throws CGException {
        try {
            String domainName = bean.getDomainName();
            String passWord = bean.getPassWord();
            String sessionCookie = CGAgentUtils.getSessionCookie(getAuthServiceURL(bean),
                    bean.getUserName(), passWord, domainName, bean.getHost());
            CGAdminClient csgAdminClient;
            if (CGAgentUtils.isClientAxis2XMLExists()) {
                ConfigurationContext configCtx = ConfigurationContextFactory.
                        createConfigurationContextFromFileSystem(null, CGConstant.CLIENT_AXIS2_XML);
                csgAdminClient = new CGAdminClient(sessionCookie, getProxyURL(bean), configCtx);
            } else {
                csgAdminClient = new CGAdminClient(sessionCookie, getProxyURL(bean));
            }
            return csgAdminClient;
        } catch (Exception e) {
            log.error("Exception occurred while construct the CGAgentAdmin client", e);
        }
        return null;
    }

    private void enableCGPollingTransportSender(AxisConfiguration axisConfig) throws AxisFault {
        TransportOutDescription transportOut =
                new TransportOutDescription(CGConstant.CG_POLLING_TRANSPORT_NAME);
        CGPollingTransportSender txSender = new CGPollingTransportSender();
        transportOut.setSender(txSender);
        axisConfig.addTransportOut(transportOut);
        transportOut.getSender().init(getConfigContext(), transportOut);
    }

    private void enableCGPollingTransportReceiver(AxisConfiguration axisConfig) throws AxisFault {

        // FIXME:https://wso2.org/jira/browse/CG-23, Services of the type RegistryAdminService1348830151341_12,
        // ServiceAdmin1348830151485_3, CGAgentAdminService1348830155933_11 are added into axis2 config
        // which are not admin services
        for (AxisService service : axisConfig.getServices().values()) {
            if (!SystemFilter.isAdminService(service) && !SystemFilter.isHiddenService(service) &&
                    service.getExposedTransports().size() == 0 && service.isEnableAllTransports() &&
                    service.getName().matches("(\\w)*Admin(\\w)*(\\d)*_(\\d)*")) {
                service.setEnableAllTransports(false);
                if (log.isDebugEnabled()) {
                    log.debug("The non admin service '" + service.getName() + "' has zero exposed transports but has" +
                            " marked to enable all transports. So setting enable all transport to false");
                }
            }
        }

        TransportInDescription transportIn =
                new TransportInDescription(CGConstant.CG_POLLING_TRANSPORT_NAME);
        CGPollingTransportReceiver receiver = new CGPollingTransportReceiver();
        transportIn.setReceiver(receiver);
        axisConfig.addTransportIn(transportIn);
        transportIn.getReceiver().init(getConfigContext(), transportIn);
        transportIn.getReceiver().start();
    }

    private List<String> filterForServiceFlags(String[] flags) {
        List<String> filtered = new ArrayList<String>();
        for (String flag : flags) {
            if (flag.endsWith(".server")) {
                filtered.add(flag);
            }
        }
        return filtered;
    }


    private void loggingToRemoteCGServer(CGServerBean csgServer) throws CGException {
        String authServerUrl = "https://" + csgServer.getHost() + ":" + csgServer.getPort() +
                "/services/AuthenticationAdmin";
        AuthenticationClient authClient = new AuthenticationClient();
        authClient.getLoggedAuthAdminStub(
                authServerUrl,
                csgServer.getUserName(),
                csgServer.getPassWord(),
                csgServer.getHost(),
                csgServer.getDomainName());

    }

    protected void addExposedTransports(String serviceName, String transport) throws Exception {
        ServiceAdmin admin;

        if (serviceName == null) {
            handleException("Invalid service name: Service name must not be null");
        }

        if (transport == null) {
            handleException("Invalid transport name: Transport name must not be null");
        }

        try {
            admin = new ServiceAdmin(getAxisConfig());
            admin.addTransportBinding(serviceName, transport);
        } catch (Exception e) {
            handleException("Error while adding exposed transport " + transport, e);
        }
    }

    protected void removeExposedTransports(String serviceName, String transportProtocol)
            throws Exception {

        TransportSummary[] transports;
        PersistenceFactory pf = PersistenceFactory.getInstance(getAxisConfig());
        ServicePersistenceManager pm;
        AxisService axisService;

        if (serviceName == null) {
            handleException("Invalid service name");
        }

        if (transportProtocol == null) {
            handleException("Invalid transport name");
        }

        axisService = getAxisConfig().getServiceForActivation(serviceName);
        if (axisService == null) {
            handleException("No service exists by the name : " + serviceName);
        }

        try {
            if (isUTEnabled(serviceName)) {
                // If UT enabled, you can't remove HTTPS transport from this service.
                if (ServerConstants.HTTPS_TRANSPORT.equalsIgnoreCase(transportProtocol)) {
                    throw new Exception("Cannot remove HTTPS transport binding for Service ["
                            + serviceName + "] since a security scenario which requires the "
                            + "service to contain only the HTTPS transport binding"
                            + " has been applied to this service.");
                }
            }

            if (!axisService.isEnableAllTransports()) {
                if (axisService.getExposedTransports().size() == 1) {
                    log.warn("At least one transport binding must exist for a service. No bindings " +
                            "will be removed.");
                    return;
                }

                // Simply remove the transport from the list of exposed transport
                axisService.removeExposedTransport(transportProtocol);

            } else {
                // This returns all the available transports - not just active ones.
                transports = listTransports();

                // populate the exposed transports list with the other transports
                for (TransportSummary transport : transports) {
                    if (transport.isListenerActive() &&
                            !transport.getProtocol().equals(transportProtocol)) {
                        axisService.addExposedTransport(transport.getProtocol());
                    }
                }
                axisService.setEnableAllTransports(false);
            }

            pm = pf.getServicePM();
            pm.removeExposedTransports(serviceName, transportProtocol);

            getAxisConfig().notifyObservers(
                    new AxisEvent(CarbonConstants.AxisEvent.TRANSPORT_BINDING_REMOVED, axisService),
                    axisService);

        } catch (Exception e) {
            handleException("Error while removing exposed transport : " + transportProtocol, e);
        }
    }

    protected boolean isUTEnabled(String serviceName) throws AxisFault {
        AxisService axisService;
        OMElement serviceElement;

        axisService = getAxisConfig().getServiceForActivation(serviceName);

        try {
            ServicePersistenceManager pm = PersistenceFactory.getInstance(getAxisConfig()).getServicePM();
            serviceElement = pm.getService(axisService);

            if (serviceElement == null) {
                pm.handleNewServiceAddition(axisService);
                serviceElement = pm.getService(axisService);
            }
            if (serviceElement.getAttributeValue(new QName(Resources.ServiceProperties.IS_UT_ENABLED)) != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("Error occurred while checking whether UT being enabled for service "
                    + serviceName, e);
            return false;
        }
        return false;
    }

    protected TransportSummary[] listTransports() {
        Map<String, TransportService> transports;
        ArrayList<TransportSummary> transCollection;
        TransportStore transportStore;

        // TransportStore already being created - so we pass null.
        transportStore = TransportStore.getInstance();
        // All transport bundles update the TransportStore - with the corresponding transports
        // supported by those.
        transports = transportStore.getAvailableTransports();
        transCollection = new ArrayList<TransportSummary>();

        for (Iterator<TransportService> iter = transports.values().iterator(); iter.hasNext();) {
            TransportService transportService;
            TransportSummary summary;

            transportService = iter.next();
            // TransportSummary only needs a subset of information from TransportInfo.
            summary = new TransportSummary();
            summary.setProtocol(transportService.getName());
            // All transports already loaded in to axis2configuration are considered as active.
            // Inactive transports still available in the management UI so the user can enable
            // those.
            summary.setListenerActive(transportService.isEnabled(true, getAxisConfig()));
            summary.setSenderActive(transportService.isEnabled(false, getAxisConfig()));
            transCollection.add(summary);
        }

        return transCollection.toArray(new TransportSummary[transCollection.size()]);
    }

    /**
     * Write the object to a Base64 string.
     */
    private static String toString(Serializable object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            return Base64.encode(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Exception occurred while writing object to the base64 String", e);
        }
        return null;
    }
}
