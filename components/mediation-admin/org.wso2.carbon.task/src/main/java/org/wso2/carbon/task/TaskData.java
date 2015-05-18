/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.task;

import org.apache.synapse.task.TaskDescription;

public class TaskData {

//    private TaskDescription taskDescription;
    private boolean isDeployedFromCApp = false;
    private boolean isEdited = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private String name;
    private String group;

    /**
     * Check whether the task is deployed from CApp
     * @return true if task deployed from CApp, else false
     */
    public boolean getDeployedFromCApp() {
        return isDeployedFromCApp;
    }

    /**
     * Set whether the task is deployed from CApp
     * @param isDeployedFromCApp true if task deployed from CApp, else false
     */
    public void setDeployedFromCApp(boolean isDeployedFromCApp) {
        this.isDeployedFromCApp = isDeployedFromCApp;
    }

    /**
     * Check whether the task deployed from CApp is edited through management console
     * @return true if the task is edited, else false
     */
    public boolean getEdited() {
        return isEdited;
    }

    /**
     * Set whether the task deployed from CApp is edited through management console
     * @param isEdited true if the task is edited, else false
     */
    public void setEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }


}
