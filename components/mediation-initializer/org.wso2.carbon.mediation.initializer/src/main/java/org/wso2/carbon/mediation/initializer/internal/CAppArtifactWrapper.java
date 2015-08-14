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

package org.wso2.carbon.mediation.initializer.internal;

import org.apache.synapse.config.SynapseConfiguration;

/**
 * Wrapper class to hold synapse configuration with and without CApp artifact details
 */

public class CAppArtifactWrapper {

    /* To hold the synapse config without CApp artifact details */
    private SynapseConfiguration newConfig;

    /* To hold the synapse config of CApp artifact details */
    private SynapseConfiguration cAppArtifactConfig;

    /**
     * To get synapse config without CApp artifact details
     *
     * @return SynapseConfiguration without CApp details
     */
    public SynapseConfiguration getNewConfig() {
        return newConfig;
    }

    /**
     * Set synapse config without CApp artifact details
     *
     * @param newConfig SynapseConfiguration without CApp details
     */
    public void setNewConfig(SynapseConfiguration newConfig) {
        this.newConfig = newConfig;
    }

    /**
     * To get synapse config with CApp artifact details
     *
     * @return SynapseConfiguration holding CApp details
     */
    public SynapseConfiguration getcAppArtifactConfig() {
        return cAppArtifactConfig;
    }

    /**
     * Set synapse config of CApp artifact details
     *
     * @param cAppArtifactConfig SynapseConfiguration holding CApp details
     */
    public void setcAppArtifactConfig(SynapseConfiguration cAppArtifactConfig) {
        this.cAppArtifactConfig = cAppArtifactConfig;
    }
}
