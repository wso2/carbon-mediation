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
package org.wso2.carbon.das.messageflow.data.publisher.conf;


public class MediationStatConfig {

    private boolean isMessageFlowTracePublishingEnabled;
    private boolean isMessageFlowStatsPublishingEnabled;
    private String serverId = "";
    private String url = "";
    private String userName = "";
    private String password = "";

    private Property[] properties;
    private boolean isLoadBalancingEnabled = false;

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        String[] urls = this.url.split(",");
        isLoadBalancingEnabled = urls != null && urls.length > 1;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoadBalancingEnabled(){
        return isLoadBalancingEnabled;
    }

    public boolean isMessageFlowTracePublishingEnabled() {
        return isMessageFlowTracePublishingEnabled;
    }

    public void setMessageFlowTracePublishingEnabled(boolean messageFlowTracePublishingEnabled) {
        isMessageFlowTracePublishingEnabled = messageFlowTracePublishingEnabled;
    }

    public boolean isMessageFlowStatsPublishingEnabled() {
        return isMessageFlowStatsPublishingEnabled;
    }

    public void setMessageFlowStatsPublishingEnabled(boolean messageFlowStatsPublishingEnabled) {
        isMessageFlowStatsPublishingEnabled = messageFlowStatsPublishingEnabled;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

}
