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
package org.wso2.carbon.cloud.gateway.agent.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.gateway.common.CGException;
import org.wso2.carbon.cloud.gateway.stub.CGAdminServiceStub;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGProxyToolsURLs;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGServiceMetaDataBean;
import org.wso2.carbon.cloud.gateway.stub.types.common.CGThriftServerBean;

/**
 * <code>CGAdminClient </code> provides the admin client for CSGAdmin service
 */
public class CGAdminClient {
    private CGAdminServiceStub stub;

    private static final Log log = LogFactory.getLog(CGAdminClient.class);

    public CGAdminClient(String cookie, String backendServerUrl) throws CGException {
        String serviceURL = backendServerUrl + "CGAdminService";
        try {
            stub = new CGAdminServiceStub(serviceURL);
        } catch (AxisFault axisFault) {
            throw new CGException(axisFault);
        }
        Options options = stub._getServiceClient().getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    public CGAdminClient(String cookie,
                         String backendServerURL,
                         ConfigurationContext configCtx) throws CGException {
        String serviceURL = backendServerURL + "CGAdminService";
        try {
            stub = new CGAdminServiceStub(configCtx, serviceURL);
        } catch (AxisFault axisFault) {
            throw new CGException(axisFault);
        }
        Options options = stub._getServiceClient().getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    public void deployProxy(CGServiceMetaDataBean serviceMetaData) throws CGException {
        try {
            stub.deployProxy(serviceMetaData);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void unDeployProxy(String serviceName) throws CGException {
        try {
            stub.unDeployProxy(serviceName);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public CGThriftServerBean getThriftServerConnectionBean() throws CGException {
        try {
            return stub.getThriftServerConnectionBean();
        } catch (Exception e) {
            throw new CGException(e);
        }
    }

    public void updateProxy(String serviceName, int eventType) throws CGException {
        try {
            stub.updateProxy(serviceName, eventType);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public CGProxyToolsURLs getPublishedProxyToolsURLs(String serviceName, String domainName)
            throws CGException {
        try {
            return stub.getPublishedProxyToolsURLs(serviceName, domainName);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    private void handleException(Throwable t) throws CGException {
        log.error(t);
        throw new CGException(t);
    }
}
