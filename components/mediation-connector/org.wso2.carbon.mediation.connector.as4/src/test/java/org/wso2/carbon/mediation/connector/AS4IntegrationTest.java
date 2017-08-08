/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediation.connector;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * AS4 connector integration test
 */
public class AS4IntegrationTest extends ConnectorIntegrationTestBase {

    private Map<String, String> esbRequestHeadersMap = new HashMap<String, String>();
    private Map<String, String> apiRequestHeadersMap = new HashMap<String, String>();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init("as4-connector-1.0.0");
        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "application/json");
    }

    @Test(groups = {"wso2.esb"}, description = "as4 send test case")
    public void testAS4Send() throws Exception {
        log.info("AS4 connector test send operation");

        esbRequestHeadersMap.put("Action", "urn:send");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "sendRequest.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 202, "Invalid status code received.");
    }

    @Test(groups = {"wso2.esb"}, description = "as4 receive test case")
    public void testAS4Receive() throws Exception {
        log.info("AS4 connector test receive operation");

        esbRequestHeadersMap.put("Action", "urn:receive");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "receiveRequest.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 202, "Invalid status code received.");
    }
}