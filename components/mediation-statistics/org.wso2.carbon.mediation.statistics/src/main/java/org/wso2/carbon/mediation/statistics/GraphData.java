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
package org.wso2.carbon.mediation.statistics;

public class GraphData {

    private String serverData;
    private String sequenceData;
    private String proxyServiceData;
    private String endPointData;

    public String getServerData() {
        return serverData;
    }

    public void setServerData(String serverData) {
        this.serverData = serverData;
    }

    public String getSequenceData() {
        return sequenceData;
    }

    public void setSequenceData(String sequenceData) {
        this.sequenceData = sequenceData;
    }

    public String getProxyServiceData() {
        return proxyServiceData;
    }

    public void setProxyServiceData(String proxyServiceData) {
        this.proxyServiceData = proxyServiceData;
    }

    public String getEndPointData() {
        return endPointData;
    }

    public void setEndPointData(String endPointData) {
        this.endPointData = endPointData;
    }
}