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
package org.wso2.carbon.message.processor.ui;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.message.store.stub.MessageStoreAdminServiceStub;

/**
 * Client that is used to access Message Store
 */
public class MessageStoreAdminServiceClient {

    private MessageStoreAdminServiceStub stub;

    private static final String adminServiceName = "MessageStoreAdminService";


    private static Log log = LogFactory.getLog(MessageStoreAdminServiceClient.class);


    public MessageStoreAdminServiceClient(String cookie, String backendServerUrl,
                                          ConfigurationContext configurationContext) throws AxisFault {

        String serviceURL = backendServerUrl + adminServiceName;
        stub = new MessageStoreAdminServiceStub(configurationContext, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }


    public String[] getMessageStoreNames() throws Exception {
        String[] messageStoreNames = null;
        try {
            messageStoreNames = stub.getMessageStoreNames();
        } catch (Exception e) {
            String message = "Error While Accessing Message Store admin Service "
                    + e.getMessage();
            log.error(message, e);
            throw e;
        }

        return messageStoreNames;
    }

    public String getMessageStoreClass(String messageStoreName) throws Exception {
        String messageStoreClassName = null;
        try {
            messageStoreClassName = stub.getClassName(messageStoreName);
        } catch (Exception e) {
            String message = "Error While Accessing Message Store admin Service "
                    + e.getMessage();
            log.error(message, e);
            throw e;
        }

        return messageStoreClassName;
    }


}
