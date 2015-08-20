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
package org.wso2.carbon.cloud.gateway.service;

import java.net.SocketException;

import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.cloud.gateway.common.*;
import org.wso2.carbon.cloud.gateway.transport.CGTransportSender;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.proxyadmin.ProxyAdminException;
import org.wso2.carbon.proxyadmin.ProxyData;
import org.wso2.carbon.proxyadmin.Entry;
import org.wso2.carbon.proxyadmin.service.ProxyServiceAdmin;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.service.mgt.ServiceMetaData;


/**
 * The class <code>CGAdminService</code> service provides the operations for deploying the proxies
 * for out sliders. These proxies are the actual proxies that represent the internal
 */
public class CGAdminService extends AbstractServiceBusAdmin {
    private static final Log log = LogFactory.getLog(CGAdminService.class);

    /**
     * Deploy the proxy service
     *
     * @param metaData meta data associated with this proxy
     * @throws org.wso2.carbon.cloud.gateway.common.CGException throws in case of an error
     */
    public void deployProxy(CGServiceMetaDataBean metaData) throws CGException {
        if (metaData == null) {
            handleException("CG Service meta data is null");
        }
        try {
            ProxyData proxyData = createProxyData(metaData);
            new ProxyServiceAdmin().addProxy(proxyData);

            // enable CG transport sender for this tenant if not already done so,
            // this has to done this way because Stratos has no mechanism to enable custom
            // transport senders, because all messages are supposed to go through ST out flow
            // we can get rid of this once Stratos has that capabilities
            if (getAxisConfig().getTransportOut(CGConstant.CG_TRANSPORT_NAME) == null) {
                enableCGTransportSender(getAxisConfig());
            }
        } catch (Exception e) {
            handleException("Could not deploy the CG service '" + metaData.getServiceName() + "'. "
                    + e.getMessage(), e);
        }
    }

    /**
     * Un deploy the proxy service
     *
     * @param serviceName the name of the proxy to un deploy
     * @throws org.wso2.carbon.cloud.gateway.common.CGException throws in case of an error
     */
    public void unDeployProxy(String serviceName) throws CGException {
        if (serviceName == null) {
            handleException("CG service(proxy service) name is null");
        }
        try {
            new ProxyServiceAdmin().deleteProxyService(serviceName);
            deleteWSDLResources(serviceName);// remove the wsdl document from the registry
        } catch (ProxyAdminException e) {
            handleException("Could not delete the CG service '" + serviceName + "'. "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the thrift server connection param
     *
     * @return the thriftServer connection url
     * @throws org.wso2.carbon.cloud.gateway.common.CGException In case the server is not running
     */
    public CGThriftServerBean getThriftServerConnectionBean() throws CGException {
        CGThriftServerBean bean;
        try {
            String hostName = CGUtils.getCGThriftServerHostName();
            int port = CGUtils.getCGThriftServerPort();
            int timeOut = CGUtils.getIntProperty(CGConstant.CG_THRIFT_CLIENT_TIMEOUT,
                    CGConstant.DEFAULT_TIMEOUT);

            if (!CGUtils.isServerAlive(hostName, port)) {
                handleException("Thrift server is not running on the host '" + hostName + "'" +
                        " in port '" + port + "'");
            }
            bean = new CGThriftServerBean();
            bean.setHostName(hostName);
            bean.setPort(port);
            bean.setTimeOut(timeOut);
        } catch (SocketException e) {
            throw new CGException(e);
        }
        return bean;
    }

    /**
     * Update the public proxy based on the new event of the back end service
     *
     * @param serviceName service
     * @param eventType   the new event type
     * @throws org.wso2.carbon.cloud.gateway.common.CGException throws in case of an error
     */
    public void updateProxy(String serviceName, int eventType) throws CGException {
        ProxyServiceAdmin proxyAdmin = new ProxyServiceAdmin();
        try {
            if (eventType == AxisEvent.SERVICE_REMOVE) {
                proxyAdmin.deleteProxyService(serviceName);
            } else if (eventType == AxisEvent.SERVICE_START) {
                proxyAdmin.startProxyService(serviceName);
            } else if (eventType == AxisEvent.SERVICE_STOP) {
                proxyAdmin.stopProxyService(serviceName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The event SERVICE_DEPLOY is supported for the service '" + serviceName + "'");
                }
            }
        } catch (ProxyAdminException e) {
            handleException("Could not update service proxy service '" + serviceName + "'", e);
        }
    }

    public CGProxyToolsURLs getPublishedProxyToolsURLs(String serviceName, String domainName)
            throws CGException {
        try {
            CGProxyToolsURLs tools = new CGProxyToolsURLs();
            ServiceMetaData data = new ServiceAdmin().getServiceData(serviceName);
            tools.setTryItURL(data.getTryitURL());
            tools.setWsdl11URL(data.getWsdlURLs()[0]);
            tools.setWsdl2URL(data.getWsdlURLs()[1]);
            tools.setEprArray(data.getEprs());
            return tools;
        } catch (Exception e) {
            handleException("Could not read the proxy service URL for the service '" + serviceName
                    + "', for the domain '" + domainName + "'", e);
        }
        return null;
    }

    private ProxyData createProxyData(CGServiceMetaDataBean proxyMetaData) throws CGException {

        if (log.isDebugEnabled()) {
            log.debug("Creating the proxy data with following metadata");
            log.debug("Service Name : " + proxyMetaData.getServiceName());
            log.debug("CGTransport endpoint : " + proxyMetaData.getEndpoint());
            log.debug("Has In Out operations? : " + proxyMetaData.isHasInOutMEP());
            log.debug("Enable MTOM? : " + proxyMetaData.isMTOMEnabled());
            log.debug("Has WS-Sec enabled ? : " + proxyMetaData.isWsSecEnabled());
            log.debug("Has WS-RM enabled ? : " + proxyMetaData.isWsRmEnabled());
            if (proxyMetaData.isWsSecEnabled()) {
                log.debug("WS-Sec policy : \n" + proxyMetaData.getSecPolicy());
            }
            if (proxyMetaData.isWsRmEnabled()) {
                log.debug("WS-RM policy : \n" + proxyMetaData.getRmPolicy());
            }
            log.debug("WSDL location : " + proxyMetaData.getWsdlLocation());
            log.debug("WSDL : \n" + proxyMetaData.getInLineWSDL());

            if (proxyMetaData.getServiceDependencies() != null &&
                    proxyMetaData.getServiceDependencies().length > 0) {

                    for (CGServiceDependencyBean dependencyBean : proxyMetaData.getServiceDependencies()) {

                        log.debug("Dependency Key : " + dependencyBean.getKey());
                        log.debug("Dependency Content : \n" + dependencyBean.getContent());

                    }

                }


        }

        ProxyData proxy = new ProxyData();
        proxy.setName(proxyMetaData.getServiceName());
        if (proxyMetaData.getInLineWSDL() != null) {
            String persistedWsdlPath = persistWSDL(proxyMetaData.getServiceName(),
            		proxyMetaData.getInLineWSDL(), proxyMetaData.getServiceDependencies());
            proxy.setWsdlKey(persistedWsdlPath);

            if (!ArrayUtils.isEmpty(proxyMetaData.getServiceDependencies())) {

                CGServiceDependencyBean[] dependencies = proxyMetaData.getServiceDependencies();
                Entry[] resourceEntries = new Entry[proxyMetaData.getServiceDependencies().length];
                for (int i = 0; i < resourceEntries.length; i++) {
                    resourceEntries[i] =
                                         new Entry(
                                                   dependencies[i].getKey(),
                                                   composeServiceResourcesPath(proxyMetaData.getServiceName()) +
                                                           dependencies[i].getKey());
                }

                proxy.setWsdlResources(resourceEntries);
            }


        }

        // FIXME - this is the workaround for https://issues.apache.org/jira/browse/SYNAPSE-527 and
        // https://issues.apache.org/jira/browse/AXIS2-4196
        String inSeq =
                "<inSequence xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                        "<class name=\"org.wso2.carbon.cloud.gateway.CGMEPHandlingMediator\"/>" +
                        "<property name=\"transportNonBlocking\" scope=\"axis2\" action=\"remove\"/>" +
                        "<property name=\"preserveProcessedHeaders\" value=\"true\"/>";

        if (proxyMetaData.isHasInOutMEP()) {
            proxy.setOutSeqXML(
                    "<outSequence xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                            "<send/>" +
                            "</outSequence>");
        } else {
            inSeq = inSeq + "<property name=\"OUT_ONLY\" scope=\"axis2\" action=\"set\" value=\"true\"/>";
        }
        inSeq = inSeq + "</inSequence>";
        proxy.setInSeqXML(inSeq);
        proxy.setFaultSeqXML(
                "<faultSequence xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                        "<log level=\"full\"/>" +
                        "<drop/>" +
                        "</faultSequence>");

        // add a dummy error code in order to make sure that we don't suspend the endpoint
        // for any type of error, the proxy is just a middle man and it has an in memory endpoint
        String endpointXML =
                "<endpoint xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                        "<address uri=\"" + proxyMetaData.getEndpoint() + "\">" +
                        "<suspendOnFailure>" +
                        "<errorCodes>400207</errorCodes>" +
                        "<initialDuration>1000</initialDuration>" +
                        "<progressionFactor>2</progressionFactor>" +
                        "<maximumDuration>64000</maximumDuration>" +
                        "</suspendOnFailure>" +
                        "</address>" +
                        "</endpoint>";
        proxy.setEndpointXML(endpointXML);

        return proxy;
    }

    private void handleException(String msg) throws CGException {
        log.error(msg);
        throw new CGException(msg);
    }

    private void handleException(String msg, Throwable t) throws CGException {
        log.error(msg, t);
        throw new CGException(msg, t);
    }

    private void enableCGTransportSender(AxisConfiguration axisConfig)
            throws Exception {
        CGTransportSender sender = new CGTransportSender();
        TransportOutDescription transportOut =
                new TransportOutDescription(CGConstant.CG_TRANSPORT_NAME);
        transportOut.setSender(sender);
        axisConfig.addTransportOut(transportOut);
        transportOut.getSender().init(getConfigContext(), transportOut);
    }

    /**
     * Saves the Wsdl of service published onto this server in Governance user
     * registry
     *
     * @param serviceName Name of the service
     * @param wsdl        content of the Wsdl document.
     * @return returns the key of the stored WSDL
     * @throws org.wso2.carbon.cloud.gateway.common.CGException in case of an error.
     */
    private String persistWSDL(String serviceName, String wsdl, CGServiceDependencyBean[] dependencies) throws CGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;
        Registry registry = getGovernanceUserRegistry();

        String serviceResourcesPath = composeServiceResourcesPath(serviceName);
        String wsdlPath = serviceResourcesPath + serviceName + ".wsdl";

        try {

            if (!isTransactionAlreadyStarted) {
                registry.beginTransaction(); // start a transaction if none
                // exists currently.
            }

            if (registry.resourceExists(serviceResourcesPath)) {
                // delete the resource and add it again
                if (log.isDebugEnabled()) {
                    log.debug("Replacing the Wsdl for the service: " + serviceName);
                }
                registry.delete(serviceResourcesPath);
            }

            Resource resource = registry.newResource();
            resource.setContent(wsdl);

            if (log.isDebugEnabled()) {
                log.debug("Adding wsdl to registry. Service name: " + serviceName);
            }

            registry.put(wsdlPath , resource);

            if (!ArrayUtils.isEmpty(dependencies)) {

                for (CGServiceDependencyBean dependency : dependencies) {
                    Resource dependencyResource = registry.newResource();
                    dependencyResource.setContent(dependency.getContent());
                    registry.put(serviceResourcesPath + dependency.getKey(), dependencyResource);
                }

            }

        } catch (RegistryException e) {
            isTransactionSuccess = false;
            handleException("Error occurred while saving the wsdl into registry", e);
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
                    handleException("Error occurred while trying to rollback or commit the " +
                            "transaction", re);
                }
            }
        }

        return wsdlPath;
    }


    /**
     * Deletes the Wsdl of service published onto this server
     *
     * @param serviceName Name of the service
     * @throws org.wso2.carbon.cloud.gateway.common.CGException in case of an error.
     */
    private void deleteWSDLResources(String serviceName) throws CGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;
        Registry registry = getGovernanceUserRegistry();

        String resourcePath = composeServiceResourcesPath(serviceName);

        try {

            if (!isTransactionAlreadyStarted) {
                // start a transaction if none exists currently.
                registry.beginTransaction();
            }

            if (registry.resourceExists(resourcePath)) {
                // delete the resource ( - Wsdl document)
                registry.delete(resourcePath);
            }
        } catch (RegistryException e) {
            isTransactionSuccess = false;
            handleException("Error occurred while deleting the wsdl from registry", e);
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


    /**
     * Constructs the registry path to persist a wsdl document ( - belonging to
     * service published on CG server)
     *
     * @param serviceName Name of the service
     * @return Designated path in registry
     */
    public String composeServiceResourcesPath(String serviceName) {

        return new StringBuilder().append(CGConstant.REGISTRY_CG_WSDL_RESOURCE_PATH)
                                  .append(RegistryConstants.PATH_SEPARATOR).append(serviceName)
                                  .append(RegistryConstants.PATH_SEPARATOR).toString();
    }
}
