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
package org.wso2.carbon.bam.service.data.publisher.data;


import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;
import org.wso2.carbon.statistics.services.util.SystemStatistics;

import java.sql.Timestamp;

public class StatisticData {

    private SystemStatistics systemStatistics;
    private ServiceStatistics serviceStatistics;
    private OperationStatistics operationStatistics;
    private String serviceName;
    private String operationName;
    private Timestamp timestamp;

    private String userAgent;
    private String remoteAddress;
    private String host;
    private String contentType;
    private String referer;
    private String requestURL;

    public String getRequestURL() {
        return requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public SystemStatistics getSystemStatistics() {
        return systemStatistics;
    }

    public void setSystemStatistics(SystemStatistics systemStatistics) {
        this.systemStatistics = systemStatistics;
    }

    public ServiceStatistics getServiceStatistics() {
        return serviceStatistics;
    }

    public void setServiceStatistics(ServiceStatistics serviceStatistics) {
        this.serviceStatistics = serviceStatistics;
    }

    public OperationStatistics getOperationStatistics() {
        return operationStatistics;
    }

    public void setOperationStatistics(OperationStatistics operationStatistics) {
        this.operationStatistics = operationStatistics;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
