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
package org.wso2.carbon.mediation.initializer.utils;

/**
 * This class holds the meta data of the artifacts deployed from CApp
 */

public class CAppArtifactData {

    /* Artifact name */
    private String name;

    /* Artifact type - ex: synapse/proxy-service */
    private String type;

    /* To identify the artifact is deployed from CApp */
    private boolean isDeployedFromCapp = false;

    /* To identify the CApp artifact is edited through management console */
    private boolean isEdited = false;


    public CAppArtifactData(String name, String type, boolean isDeployedFromCapp) {
        this.name = name;
        this.type = type;
        this.isDeployedFromCapp = isDeployedFromCapp;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public boolean isDeployedFromCapp() {
        return isDeployedFromCapp;
    }

    public void setDeployedFromCapp(boolean isDeployedFromCapp) {
        this.isDeployedFromCapp = isDeployedFromCapp;
    }

}
