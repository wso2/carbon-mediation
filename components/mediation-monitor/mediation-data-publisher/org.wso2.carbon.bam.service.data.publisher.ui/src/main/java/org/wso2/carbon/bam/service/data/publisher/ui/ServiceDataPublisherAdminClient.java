/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bam.service.data.publisher.ui;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.service.data.publisher.stub.ServiceDataPublisherAdminStub;
import org.wso2.carbon.bam.service.data.publisher.stub.conf.EventingConfigData;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ServiceDataPublisherAdminClient {

    private static final Log log = LogFactory.getLog(ServiceDataPublisherAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.bam.service.data.publisher.ui.i18n.Resources";
    private ServiceDataPublisherAdminStub stub;
    private ResourceBundle bundle;

    public ServiceDataPublisherAdminClient(String cookie, String backendServerURL,
                                           ConfigurationContext configContext, Locale locale)
            throws AxisFault {
        String serviceURL = backendServerURL + "ServiceDataPublisherAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ServiceDataPublisherAdminStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void setEventingConfigData(EventingConfigData eventingConfigData) throws
                                                                             RemoteException {
        try {
            stub.configureEventing(eventingConfigData);
        } catch (Exception e) {
            handleException(bundle.getString("cannot.set.eventing.config"), e);
        }
    }

    public EventingConfigData getEventingConfigData() throws RemoteException {
        try {
            return stub.getEventingConfigData();
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.eventing.config"), e);
        }
        return null;
    }

    public boolean isCloudDeployment() throws RemoteException {
        try {
            return stub.isCloudDeployment();
        } catch (RemoteException e) {
            handleException(bundle.getString("backend.server.unavailable"), e);
        }
        return false;
    }

    public String getBAMServerURL() throws RemoteException {
        try {
            return stub.getServerConfigBAMServerURL();
        } catch (RemoteException e) {
            handleException(bundle.getString("backend.server.unavailable"), e);
        }
        return "";
    }

    private void handleException(String msg, java.lang.Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }
}
