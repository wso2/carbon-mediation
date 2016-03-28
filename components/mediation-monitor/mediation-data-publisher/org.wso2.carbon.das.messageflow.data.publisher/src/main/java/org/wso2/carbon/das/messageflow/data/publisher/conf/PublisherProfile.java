/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.das.messageflow.data.publisher.conf;

import org.apache.synapse.aspects.flow.statistics.structuring.StructuringArtifact;

import java.util.HashMap;
import java.util.Map;

public class PublisherProfile {

    private Map<String, Integer> synapseConfigRecords = new HashMap<String, Integer>();

    private PublisherConfig config = new PublisherConfig();


    public PublisherProfile(PublisherConfig config) {
        this.config = config;
    }

    public PublisherConfig getConfig() {
        return config;
    }

    public void setConfig(PublisherConfig config) {
        this.config = config;
    }

    public boolean isAlreadyPublished(StructuringArtifact structuringArtifact) {

        String name = structuringArtifact.getName();
        int hashcode = structuringArtifact.getHashcode();

        if (synapseConfigRecords.get(name) == null) {
            // Completely new configuration
            synapseConfigRecords.put(name, hashcode);
            return false;
        } else if (synapseConfigRecords.get(name) != hashcode) {
            // Updated configuration
            synapseConfigRecords.put(name, hashcode);
            return false;
        } else {
            // Already published
            return true;
        }
    }
}
