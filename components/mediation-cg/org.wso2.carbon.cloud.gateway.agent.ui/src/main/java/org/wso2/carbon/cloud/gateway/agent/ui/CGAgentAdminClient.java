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
package org.wso2.carbon.cloud.gateway.agent.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.gateway.agent.stub.CGAgentAdminServiceCGException;
import org.wso2.carbon.cloud.gateway.agent.stub.CGAgentAdminServiceStub;
import org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGProxyToolsURLs;
import org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGServerBean;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class <code>CGAgentAdminClient</code> provides the admin client for CSGAgent.
 */
public class CGAgentAdminClient {
    private static final Log log = LogFactory.getLog(CGAgentAdminClient.class);

    private static final String BUNDLE = "org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources";

    private ResourceBundle bundle;

    private CGAgentAdminServiceStub stub;


    public CGAgentAdminClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx,
                              Locale locale) throws AxisFault {

        String serviceURL = backendServerURL + "CGAgentAdminService";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        stub = new CGAgentAdminServiceStub(configCtx, serviceURL);
        Options options = stub._getServiceClient().getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Publish the service
     *
     * @param serviceName name of the service to publish
     * @param serverName  name of the server from which to unpublished
     * @param isAutoMatic the mode of publishing
     * @throws AxisFault in case of an error
     */
    public void publish(String serviceName, String serverName, boolean isAutoMatic) throws AxisFault {
        try {
            stub.publishService(serviceName, serverName, isAutoMatic);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.publish.service", e), e);
        }
    }

    /**
     * Un publish the service
     *
     * @param serviceName name of the service to unpublished
     * @param serverName  name of the server from which to unpublished
     * @throws AxisFault in case of an error
     */
    public void unPublish(String serviceName, String serverName) throws AxisFault {
        try {
            stub.unPublishService(serviceName, serverName);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.unpublish.service", e), e);
        }
    }

    /**
     * Add a CSG server bean
     *
     * @param csgServer the csg server bean with server information
     * @throws AxisFault throws in case of an error
     */
    public void addCGServer(CGServerBean csgServer) throws AxisFault {
        try {
            stub.addCGServer(csgServer);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.add.the.csg.server", e), e);
        }
    }

    /**
     * Get the available list of csg servers
     *
     * @return the csg server list
     * @throws AxisFault throws in case of an error
     */
    public CGServerBean[] getCGServerList() throws AxisFault {
        try {
            return stub.getCGServerList();
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.retrieve.csg.servers", e), e);
        }
        return null;
    }

    /**
     * Remove this csg server instance
     *
     * @param serverName the unique csg server name to delete
     * @throws AxisFault throws in case of an error
     */
    public void removeCGServer(String serverName) throws AxisFault {
        try {
            stub.removeCGServer(serverName);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.remove.csg.server", e), e);
        }
    }

    /**
     * Get the CSG server bean given the server name
     *
     * @param serverName the csg server name
     * @return the csg server instance given by this name
     * @throws AxisFault throws in case of an error
     */
    public CGServerBean getCGServer(String serverName) throws AxisFault {
        try {
            return stub.getCGServer(serverName);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.retrieve.csg.servers", e), e);
        }
        return null;
    }

    /**
     * Update an already existing csg server
     *
     * @param bean the new csg server information
     * @throws AxisFault throws in case of an error
     */
    public void updateCGServer(CGServerBean bean) throws AxisFault {
        try {
            stub.updateCGServer(bean);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.update.csg.server", e), e);
        }
    }

    /**
     * Is the given service already published
     *
     * @param serviceName service
     * @return status of the csg service
     * @throws AxisFault throws in case of an error
     */
    public String getServiceStatus(String serviceName) throws AxisFault {
        try {
            return stub.getServiceStatus(serviceName);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.get.published.flag", e), e);
        }
        return null;
    }

    public void setServiceStatus(String serviceName, String status) throws AxisFault {
        try {
            stub.setServiceStatus(serviceName, status);
        } catch (Exception e) {
            handleException("Could not set the new status for the service '" + serviceName + "'", e);
        }
    }

    /**
     * Get the server that this service has published
     *
     * @param serviceName the service
     * @return the list of servers
     * @throws AxisFault throws in case of an error
     */
    public String getPublishedServer(String serviceName) throws AxisFault {
        try {
            return stub.getPublishedServer(serviceName);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (CGAgentAdminServiceCGException e) {
            handleException(getI18nString("cannot.get.published.server.list", e), e);
        }
        return null;
    }

    public CGProxyToolsURLs getPublishedProxyToolsURLs(String serviceName) throws AxisFault {
        try {
            return stub.getPublishedProxyToolsURLs(serviceName);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Determines whether at least one service has been published to specified CSG server.
     *
     * @param serverName Name of the CSG sever
     * @return if there is at least a one service published to the CSG server or false otherwise.
     * @throws AxisFault in case of an error
     */
    public boolean isHasPublishedServices(String serverName) throws AxisFault {

        boolean isPublished = false;
        try {
            isPublished = stub.isHasPublishedServices(serverName);
        } catch (RemoteException e) {
            handleException(getI18nString("connect.error", e), e);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }

        return isPublished;
    }

    private void handleException(String msg, Throwable t) throws AxisFault {
        log.error(msg, t);
        throw new AxisFault(msg, t);
    }

    private String getI18nString(String rowString, Throwable t) {
        return MessageFormat.format(bundle.getString(rowString), t.getMessage());
    }
}
