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
package org.wso2.carbon.mediator.bam.config.ui;

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
 * Admin client that uses the backend persistence and cryptographic facilities
 */
public class BamServerProfileConfigAdminClient {

    private static final Log log = LogFactory.getLog(BamServerProfileConfigAdminClient.class);
	private static final String BUNDLE = "org.wso2.carbon.mediator.bam.config.ui.i18n.Resources";
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

    public void saveResourceString(String resourceString, String bamServerProfileLocation) throws RemoteException {
        try {
            stub.saveResourceString(resourceString, bamServerProfileLocation);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.save.resource.string"), e);
        }
    }

    public String getResourceString(String bamServerProfileLocation) throws RemoteException {
        try {
            return stub.getResourceString(bamServerProfileLocation);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.get.resource.string"), e);
        }
        return null;
    }

    public boolean resourceAlreadyExists(String bamServerProfileLocation) throws RemoteException {
        try {
            return stub.resourceAlreadyExists(bamServerProfileLocation);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.check.resource.exists"), e);
        }
        return true;
    }

    public boolean addCollection(String path) throws RemoteException {
        try {
            return stub.addCollection(path);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.add.collection"), e);
        }
        return false;
    }
    
    public boolean removeResource(String path) throws RemoteException {
        try {
            return stub.removeResource(path);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.remove.resource"), e);
        }
        return false;
    }

    public String[] getServerProfilePathList(String bamServerProfileLocation) throws RemoteException {
        return stub.getServerProfileNameList(bamServerProfileLocation);
    }

    public String encryptAndBase64Encode(String plainText) throws RemoteException {
        try {
            return stub.encryptAndBase64Encode(plainText);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.encrypt.encode.string"), e);
        }
        return "";
    }

    public String base64DecodeAndDecrypt(String cipherText) throws RemoteException {
        try {
            return stub.base64DecodeAndDecrypt(cipherText);
        } catch (RemoteException e) {
            handleException(bundle.getString("cannot.decode.decrypt.string"), e);
        }
        return "";
    }

    private void handleException(String msg, Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }

}
