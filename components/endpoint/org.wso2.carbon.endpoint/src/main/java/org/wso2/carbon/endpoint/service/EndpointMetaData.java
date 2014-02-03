/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.endpoint.service;

/**
 * Instances of this class represents the MetaData of a Endpoint
 */
public class EndpointMetaData {

    private String name;
    private boolean enableStatistics;
    private String description;
    private boolean switchOn;
    private String endpointString;

    /**
     * Get the configuration of an Endpoint
     * @return endpoint configuration
     */
    public String getEndpointString() {
        return endpointString;
    }

    /**
     * Set the configuration of the endpoint as a metadata
     * @param endpointString endpoint configuration
     */
    public void setEndpointString(String endpointString) {
        this.endpointString = endpointString;
    }

    /**
     * Check whether statistics are enabled for the endpoint
     * @return true if statistics are enabled, otherwise false
     */
    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    /**
     * Set whether statistics are enabled for the endpoint
     * @param enableStatistics true if statistics are enabled, otherwise false
     */
    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    /**
     * Get whether endpoint is switched on
     * @return true if endpoint is switched on, otherwise false
     */
    public boolean isSwitchOn() {
        return switchOn;
    }

    /**
     * Set whether endpoint is switched on
     * @param switchOn true if endpoint is switched on, otherwise false
     */
    public void setSwitchOn(boolean switchOn) {
        this.switchOn = switchOn;
    }

    /**
     * Get the name of the endpoint
     * @return endpoint name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the endpoint name as a metadata
     * @param name endpoint name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of the endpoint
     * @return endpoint description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the endpoint description as a metadata
     * @param description endpoint description
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
