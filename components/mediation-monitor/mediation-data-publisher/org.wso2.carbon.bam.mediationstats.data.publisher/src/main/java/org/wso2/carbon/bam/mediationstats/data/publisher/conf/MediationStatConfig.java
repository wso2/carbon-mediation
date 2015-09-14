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
package org.wso2.carbon.bam.mediationstats.data.publisher.conf;


public class MediationStatConfig {

    private boolean isPublishingEnable;
    private boolean enableMediationStats;
    private String url = "";
    private String userName = "";
    private String password = "";
    private Property[] properties;

    private String streamName = "bam_mediation_stats_data_publisher";
    private String version = "1.0.0";
    private String nickName = "MediationStatsDataAgent";
    private String description = "Publish Mediation statistics events";
    private boolean isLoadBalancingEnabled = false;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

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

    public boolean isEnableMediationStats() {
        return enableMediationStats;
    }

    public void setEnableMediationStats(boolean enableMediationStats) {
        this.enableMediationStats = enableMediationStats;
    }

    public boolean isLoadBalancingEnabled(){
        return isLoadBalancingEnabled;
    }

    public void setStatisticsReporterDisable(boolean isPublishingEnabled) {
        isPublishingEnable = isPublishingEnabled;
    }

    public boolean getStatisticsReporterDisable() {
        return isPublishingEnable;
    }

}
