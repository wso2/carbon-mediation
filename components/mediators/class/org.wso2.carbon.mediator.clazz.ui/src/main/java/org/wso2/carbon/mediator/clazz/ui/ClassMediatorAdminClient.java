/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.clazz.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.mediator.clazz.stub.types.ClassMediatorAdminStub;


import java.util.ResourceBundle;
import java.util.Locale;
import java.rmi.RemoteException;

public class ClassMediatorAdminClient {
    private static final Log log = LogFactory.getLog(ClassMediatorAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.mediator.clazz.ui.i18n.Resources";   // TODO, add packages for i18n
    private ClassMediatorAdminStub stub;
    private ResourceBundle bundle;

    public ClassMediatorAdminClient(String cookie,
                                    String backEndServerURL,
                                    ConfigurationContext configCtx,
                                    Locale locale) throws AxisFault {

        String serviceURL = backEndServerURL + "ClassMediatorAdmin";
//        bundle = ResourceBundle.getBundle(BUNDLE, locale);         // TODO, fix i18n
        stub = new ClassMediatorAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public String[] getClassAttributes(String className) throws RemoteException {
        try {
            return stub.getClassSetProps(className);
        } catch (RemoteException ex) {
            handleException("A remote exception", ex); 
        }
        return null;
    }

    private void handleException(String msg, Exception ex) throws RemoteException {
        log.error(msg, ex);
        throw new RemoteException(msg, ex);

    }

}
