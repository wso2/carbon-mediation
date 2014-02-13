/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.endpoint.ui.endpoints.http;

import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointService;

public class HttpEndpointService implements EndpointService {

    public Endpoint getEndpoint() {
        return new HttpEndpoint();
    }

    public String getType() {
        return "http";
    }

    public String getUIPageName() {
        return "http";
    }

    public String getDescription() {
        return "Defines a URI Template based REST Service endpoint";
    }

    public String getDisplayName() {
        return "HTTP Endpoint";
    }

    public boolean isStatisticsAvailable() {
        return true;
    }

    public boolean canAddAsChild() {
        return true;
    }

    public boolean canAddAsTemplate() {
        return true;
    }

    public boolean isChildEndpointFormAvailable() {
        return true;
    }

}
