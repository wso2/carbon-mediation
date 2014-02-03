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
package org.wso2.carbon.rest.api.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;

import javax.servlet.http.HttpSession;

public class AuthAdminServiceClient {
    public static final String CLIENT_TRUST_STORE_PATH = "/home/usw/install/wso2-svn/branch/3_2_0/3.2.0/components/rest-api/simple-front-end/security/client-truststore.jks";

    public static final String HOST_NAME = "localhost";
    public static final String HTTPS_PORT = "9443";
//    public static final String HTTPS_PORT = "9445";
    public static final String SERVICE_URL = "https://" + HOST_NAME + ":" + HTTPS_PORT + "/services/";
//    public static final String SERVICE_URL = "http://" + HOST_NAME + ":" + HTTPS_PORT + "/services/";
    public static final String USER_NAME = "admin";
    public static final String PASSWORD = "admin";
    public static final String KEY_STORE_PASSWORD = "wso2carbon";
    public static final String KEY_STORE_TYPE = "jks";

    private static AuthenticationAdminStub authenticationAdminStub;

    public AuthAdminServiceClient() throws AxisFault {
        init(SERVICE_URL + "AuthenticationAdmin");
    }

    public static void init(String backEndServerURL) throws AxisFault {
        setSystemProperties(CLIENT_TRUST_STORE_PATH, KEY_STORE_TYPE, KEY_STORE_PASSWORD);
        authenticationAdminStub = new AuthenticationAdminStub(null, backEndServerURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
    }

    public static String login(String hostName, String userName, String password) throws Exception {

        authenticationAdminStub.login(userName, password, hostName);
//        authenticationAdminStub.login(userName, password, hostName);
        ServiceContext serviceContext = authenticationAdminStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        return sessionCookie;
    }

    public static void setSystemProperties(String keyStorePath, String keyStoreType, String keyStorePassword) {
        System.setProperty("javax.net.ssl.trustStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", keyStoreType);
    }

}
