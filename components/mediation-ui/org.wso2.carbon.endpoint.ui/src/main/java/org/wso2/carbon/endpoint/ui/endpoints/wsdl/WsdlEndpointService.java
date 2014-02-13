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

package org.wso2.carbon.endpoint.ui.endpoints.wsdl;

import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointService;

public class WsdlEndpointService implements EndpointService {

    public Endpoint getEndpoint() {
        return new WsdlEndpoint();
    }

    public String getType() {
        return "wsdl";
    }

    public String getUIPageName() {
        return "wsdl";
    }

    public String getDescription() {
        return "Defines the WSDL, Service and Port ";
    }

    public String getDisplayName() {
        return "WSDL Endpoint";
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
