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

package org.wso2.carbon.localentry.dos;

/**
 * <code>EntryData</code>, the place holder for entry data
 */
public class EntryData {

    private String name;
    private String value;
    private String type;
    private String description;
    private boolean isDeployedFromCApp = false;
    private boolean isEdited = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Check whether the local entry is deployed from CApp
     * @return true if local entry deployed from CApp, else false
     */
    public boolean getDeployedFromCApp() {
        return isDeployedFromCApp;
    }

    /**
     * Set whether the local entry is deployed from CApp
     * @param isDeployedFromCApp true if local entry deployed from CApp, else false
     */
    public void setDeployedFromCApp(boolean isDeployedFromCApp) {
        this.isDeployedFromCApp = isDeployedFromCApp;
    }

    /**
     * Check whether the v deployed from CApp is edited through management console
     * @return true if the local entry is edited, else false
     */
    public boolean getEdited() {
        return isEdited;
    }

    /**
     * Set whether the v deployed from CApp is edited through management console
     * @param isEdited true if the local entry is edited, else false
     */
    public void setEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }
}
