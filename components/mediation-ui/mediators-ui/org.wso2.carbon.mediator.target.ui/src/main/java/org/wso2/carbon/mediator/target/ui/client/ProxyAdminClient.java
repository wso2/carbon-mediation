/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.mediator.target.ui.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.IndirectEndpoint;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;

import java.util.ResourceBundle;
import java.util.Locale;
import java.rmi.RemoteException;

public class ProxyAdminClient {
    private static final Log log = LogFactory.getLog(ProxyAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.mediator.target.ui.i18n.Resources";
    private ResourceBundle bundle;
    public ProxyServiceAdminStub stub;

    public ProxyAdminClient(ConfigurationContext configCtx, String backendServerURL,
                      String cookie, Locale locale) throws AxisFault {
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        String serviceURL = backendServerURL + "ProxyServiceAdmin";
        stub = new ProxyServiceAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getAvailableEndpoints() throws AxisFault {
        try {
            return stub.getAvailableEndpoints();
        } catch (Exception e) {
            handleException(bundle.getString("unable.to.get.declared.endpoints"), e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public Endpoint getEndpoint(String name){
        IndirectEndpoint iep = new IndirectEndpoint();
        iep.setKey(name);
        return iep;
    }
}
