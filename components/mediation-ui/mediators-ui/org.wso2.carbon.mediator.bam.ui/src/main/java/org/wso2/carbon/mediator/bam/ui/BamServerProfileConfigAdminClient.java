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

package org.wso2.carbon.mediator.bam.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediator.bam.config.stub.BAMMediatorConfigAdminStub;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Admin client uses the services from org.wso2.carbon.mediator.bam.config package
 */
public class BamServerProfileConfigAdminClient {

    private static final Log log = LogFactory.getLog(BamServerProfileConfigAdminClient.class);
	private static final String BUNDLE = "org.wso2.carbon.mediator.bam.ui.i18n.Resources";
	private BAMMediatorConfigAdminStub stub;
	private ResourceBundle bundle;

    public BamServerProfileConfigAdminClient(String cookie, String backendServerURL,
                                             ConfigurationContext configCtx, Locale locale) throws AxisFault {

        String serviceURL = backendServerURL + "BAMMediatorConfigAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new BAMMediatorConfigAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String getResourceString(String bamServerProfileLocation) throws RemoteException {
        try {
            return stub.getResourceString(bamServerProfileLocation);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.bam.server.location"), e);
        }
        return null;
    }

    public String[] getServerProfilePathList(String bamServerProfileLocation) throws RemoteException {
        return stub.getServerProfileNameList(bamServerProfileLocation);
    }

    private void handleException(String msg, Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }


}
