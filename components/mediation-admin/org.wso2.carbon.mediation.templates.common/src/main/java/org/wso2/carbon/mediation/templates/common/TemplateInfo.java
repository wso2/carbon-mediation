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
package org.wso2.carbon.mediation.templates.common;

public class TemplateInfo {

    private String name;
    private boolean enableStatistics;
    private boolean enableTracing;
    private String description;
    private boolean isDeployedFromCApp;
    private boolean isEdited;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Check whether the template is deployed from CApp
     * @return true if v deployed from CApp, else false
     */
    public boolean getDeployedFromCApp() {
        return isDeployedFromCApp;
    }

    /**
     * Set whether the template is deployed from CApp
     * @param isDeployedFromCApp true if template deployed from CApp, else false
     */
    public void setDeployedFromCApp(boolean isDeployedFromCApp) {
        this.isDeployedFromCApp = isDeployedFromCApp;
    }

    /**
     * Check whether the template deployed from CApp is edited through management console
     * @return true if the template is edited, else false
     */
    public boolean getEdited() {
        return isEdited;
    }

    /**
     * Set whether the template deployed from CApp is edited through management console
     * @param isEdited true if the template is edited, else false
     */
    public void setEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

}
