
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
package org.wso2.carbon.endpoint.ui.endpoints;

/**
 * This provides the basic structure of a endpoint service for the endpoint editor ui,
 */
public interface EndpointService {

    /**
     * Get the Endpoint related to this Endpoint service
     *
     * @return Endpoint
     */
    public Endpoint getEndpoint();

    /**
     * Get the Endpoint Type of the Endpoint Service. Ex: Address, WSDL
     *
     * @return Endpoint Type
     */
    public String getType();

    /**
     * Get the common name of the UI pages for this endpoint service
     *
     * @return UI page name
     */
    public String getUIPageName();

    /**
     * Get the description of the Endpoint Type. The description should include the the functionality of the the Endpoint type
     *
     * @return description of the Endpoint type
     */
    public String getDescription();

    /**
     * This gives the display name for the Endpoint.This can
     * be any {@link String}. It is recommended to put a meaning full descriptive short name
     * as the display name
     *
     * @return display name of the Endpoint Type
     */
    public String getDisplayName();

    /**
     * Defines whether we can enable the statistics for this Endpoint Type.
     * Enable/Disable Statistics option will be available only if this is set to true
     *
     * @return whether we can enable the statistics for this Endpoint Type
     */
    public boolean isStatisticsAvailable();

    /**
     * Defines whether this Endpoint Type can be add as a child Endpoint of a List Endpoint like LoadBalance Endpoint.
     * If this is set to false you will not see a menu item for this Endpoint type in the AddMenu of the listEndpoint designer.
     *
     * @return whether this Endpoint Type can be add as a child Endpoint of a List Endpoint
     */
    public boolean canAddAsChild();

    /**
     * Defines whether we can create a template from this Endpoint Type.
     * If this is set to false you will not see a entry for this endpoint type in the Endpoint Template designer
     *
     * @return whether we can create a template from this Endpoint Type
     */
    public boolean canAddAsTemplate();

    /**
     * Defines whether this Endpoint Type has a child endpoint form for the list endpoint designer.
     * This is used by the <code>listEndpointDesigner</code>
     *
     * @return whether child endpoint form exists for this endpoint type
     */
    public boolean isChildEndpointFormAvailable();
}
