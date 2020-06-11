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

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.registry.Registry;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

/**
 * This class can be used by connectors to refresh OAuth 2.0 access tokens by setting the following mandatory
 * properties in message context: uri.var.hostName, uri.var.refreshToken
 *
 * After refresh call this will set the uri.var.accessToken, and uri.var.apiUrl in the message context to be used by
 * subsequent calls
 */
public class RefreshAccessToken extends AbstractConnector {
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String PROPERTY_PREFIX = "uri.var.";

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        SynapseLog synLog = getLog(messageContext);
        Set propertyKeySet = messageContext.getPropertyKeySet();
        propertyKeySet.remove("Accept-Encoding");

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Salesforce Refresh Access Token mediator.");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        String refreshUrl;
        String customRefreshUrl = (String) messageContext.getProperty(PROPERTY_PREFIX + "customRefreshUrl");

        if (StringUtils.isNotEmpty(customRefreshUrl)) {
            refreshUrl = customRefreshUrl;
        } else {
            StringBuilder urlStringBuilder = new StringBuilder();
            urlStringBuilder.append(messageContext.getProperty(PROPERTY_PREFIX + "hostName"));
            urlStringBuilder.append("/services/oauth2/token?grant_type=refresh_token");
            String clientId = (String) messageContext.getProperty(PROPERTY_PREFIX + "clientId");
            if (StringUtils.isNotEmpty(clientId)) {
                urlStringBuilder.append("&client_id=").append(clientId);
            }
            String clientSecret = (String) messageContext.getProperty(PROPERTY_PREFIX + "clientSecret");
            if (StringUtils.isNotEmpty(clientSecret)) {
                urlStringBuilder.append("&client_secret=").append(clientSecret);
            }
            urlStringBuilder.append("&refresh_token=").append(messageContext.getProperty(PROPERTY_PREFIX + "refreshToken"));
            urlStringBuilder.append("&format=json");
            refreshUrl = urlStringBuilder.toString();
        }

        HttpGet httpget = new HttpGet(refreshUrl);
        CloseableHttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(httpget);
            if (httpResponse.getEntity() == null) {
                throw new ConnectException("Empty response received for refresh access token call");
            }
            Scanner scanner = new Scanner(httpResponse.getEntity().getContent());
            String jsonResponse = scanner.nextLine();
            JSONObject jsonObject = new JSONObject(jsonResponse);

            String accessToken = jsonObject.getString("access_token");
            messageContext.setProperty(PROPERTY_PREFIX + "accessToken", accessToken);

            String instanceUrl = jsonObject.getString("instance_url");
            messageContext.setProperty(PROPERTY_PREFIX + "apiUrl", instanceUrl);

            String systemTime = Long.toString(System.currentTimeMillis());
            String newAccessRegistryPath = (String) messageContext.getProperty(PROPERTY_PREFIX +
                    "accessTokenRegistryPath");
            String newTimeRegistryPath = (String) messageContext.getProperty(PROPERTY_PREFIX + "timeRegistryPath");
            Registry registry = messageContext.getConfiguration().getRegistry();

            if(StringUtils.isNotEmpty(accessToken)) {
                if (!registry.isResourceExists(newAccessRegistryPath)) {
                    registry.newResource(newAccessRegistryPath, false);
                    registry.updateResource(newAccessRegistryPath, accessToken);
                    registry.newResource(newTimeRegistryPath, false);
                    registry.updateResource(newTimeRegistryPath, systemTime);
                } else {
                    registry.updateResource(newAccessRegistryPath, accessToken);
                    registry.updateResource(newTimeRegistryPath, systemTime);
                }
            }
        } catch (IOException e) {
            synLog.error(e);
            throw new ConnectException(e, "Error while executing GET request to refresh the access token");
        } finally {
            propertyKeySet.remove("Cache-Control");
            propertyKeySet.remove("Pragma");
            httpget.releaseConnection();
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    synLog.error(e);
                }
            }
        }
    }
}
