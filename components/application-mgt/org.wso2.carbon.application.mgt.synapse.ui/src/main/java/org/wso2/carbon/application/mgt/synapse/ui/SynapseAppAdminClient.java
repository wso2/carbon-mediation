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
package org.wso2.carbon.application.mgt.synapse.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.wso2.carbon.application.mgt.synapse.stub.SynapseApplicationAdminStub;
import org.wso2.carbon.application.mgt.synapse.stub.types.carbon.SynapseApplicationMetadata;

import java.util.ResourceBundle;
import java.util.Locale;
import java.lang.*;


public class SynapseAppAdminClient {
    private static final Log log = LogFactory.getLog(SynapseAppAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.application.mgt.synapse.ui.i18n.Resources";
    private ResourceBundle bundle;
    public SynapseApplicationAdminStub stub;

    public SynapseAppAdminClient(String cookie,
                                 String backendServerURL,
                                 ConfigurationContext configCtx,
                                 Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "SynapseApplicationAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new SynapseApplicationAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public SynapseApplicationMetadata getSynapseAppData(String appName) throws AxisFault {
        try {
            return stub.getSynapseAppData(appName);
        } catch (Exception e) {
            handleException(bundle.getString("cannot.get.service.data"), e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
