/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.connector.core;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

public class RefreshAccessToken extends AbstractConnector {
    CloseableHttpClient httpclient = HttpClients.createDefault();

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        SynapseLog synLog = getLog(messageContext);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Salesforce Refresh Access Token mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        String tokenRefreshUrl = "";
        tokenRefreshUrl += messageContext.getProperty("uri.var.hostName");
        tokenRefreshUrl += "/services/oauth2/token?grant_type=refresh_token";
        tokenRefreshUrl += "&client_id=" + messageContext.getProperty("uri.var.clientId");
        tokenRefreshUrl += "&client_secret=" + messageContext.getProperty("uri.var.clientSecret");
        tokenRefreshUrl += "&refresh_token=" + messageContext.getProperty("uri.var.refreshToken");
        tokenRefreshUrl += "&format=json";

        HttpGet httpget = new HttpGet(tokenRefreshUrl);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(httpget);
            Scanner scanner = new Scanner(httpResponse.getEntity().getContent());
            String jsonResponse = scanner.nextLine();
            JSONObject jsonObject = new JSONObject(jsonResponse);

            String accessToken = jsonObject.getString("access_token");
            messageContext.setProperty("uri.var.accessToken", accessToken);

            String instanceUrl = jsonObject.getString("instance_url");
            messageContext.setProperty("uri.var.apiUrl", instanceUrl);

            String systemTime = Long.toString(System.currentTimeMillis());
            String newAccessRegistryPath = (String) messageContext.getProperty("uri.var.accessTokenRegistryPath");
            String newTimeRegistryPath = (String) messageContext.getProperty("uri.var.timeRegistryPath");

            if((accessToken != null) && (!accessToken.equals(""))){
                if (!messageContext.getConfiguration().getRegistry().isResourceExists(newAccessRegistryPath)) {
                    messageContext.getConfiguration().getRegistry().newResource(newAccessRegistryPath, false);
                    messageContext.getConfiguration().getRegistry().updateResource(newAccessRegistryPath, accessToken);
                    messageContext.getConfiguration().getRegistry().newResource(newTimeRegistryPath, false);
                    messageContext.getConfiguration().getRegistry().updateResource(newTimeRegistryPath, systemTime);
                } else {
                    messageContext.getConfiguration().getRegistry().updateResource(newAccessRegistryPath, accessToken);
                    messageContext.getConfiguration().getRegistry().updateResource(newTimeRegistryPath, systemTime);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            httpget.releaseConnection();
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
