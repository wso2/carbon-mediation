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
package org.wso2.carbon.endpoint.ui.endpoints.recipientlist;

import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointService;

public class RecipientlistEndpointService implements EndpointService {

    public Endpoint getEndpoint() {
        return new RecipientlistEndpoint();
    }

    public String getType() {
        return "recipientlist";
    }

    public String getUIPageName() {
        return "recipientlist";
    }

    public String getDescription() {
        return "Defines the list of endpoints a message will be routed to";
    }

    public String getDisplayName() {
        return "Recipient List Group";
    }

    public boolean isStatisticsAvailable() {
        return false;
    }

    public boolean canAddAsChild() {
        return false;
    }

    public boolean canAddAsTemplate() {
        return false;
    }

    public boolean isChildEndpointFormAvailable() {
        return false;
    }

}
