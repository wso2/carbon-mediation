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

import java.util.HashMap;
import java.util.Map;

/**
 * To hold the Artifact details deployed from CApp
 */
public class CAppArtifactsMap {

    /* Map to hold CApp artifact details for specific tenants*/
    Map<String, CAppArtifactData> cAppArtifactDataMap = new HashMap<String, CAppArtifactData>();

    public Map<String, CAppArtifactData> getcAppArtifactDataMap() {
        return cAppArtifactDataMap;
    }

    public void setcAppArtifactDataMap(String key, CAppArtifactData artifactData) {
        cAppArtifactDataMap.put(key, artifactData);
    }
}
