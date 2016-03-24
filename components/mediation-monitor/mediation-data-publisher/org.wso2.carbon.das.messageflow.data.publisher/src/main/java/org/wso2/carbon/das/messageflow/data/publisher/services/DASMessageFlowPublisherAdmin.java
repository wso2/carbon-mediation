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
package org.wso2.carbon.das.messageflow.data.publisher.services;

import org.apache.synapse.aspects.flow.statistics.structuring.StructuringArtifact;
import org.wso2.carbon.das.messageflow.data.publisher.conf.PublisherConfig;
import org.wso2.carbon.das.messageflow.data.publisher.conf.PublisherProfile;
import org.wso2.carbon.das.messageflow.data.publisher.conf.PublisherProfileManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.das.messageflow.data.publisher.publish.ConfigurationPublisher;
import org.wso2.carbon.das.messageflow.data.publisher.util.PublisherUtils;

import java.util.ArrayList;
import java.util.List;

public class DASMessageFlowPublisherAdmin extends AbstractAdmin {

    private PublisherProfileManager publisherProfileManager;

    public DASMessageFlowPublisherAdmin() {
        this.publisherProfileManager = new PublisherProfileManager();
    }

    public void configureEventing(PublisherConfig config) {
        publisherProfileManager.addPublisherProfile(CarbonContext.getThreadLocalCarbonContext().getTenantId(), config.getServerId(), new PublisherProfile(config));

        // Publish previous configs belongs to a tenant
        if (config.isMessageFlowPublishingEnabled()) {
            List<StructuringArtifact> artifactList = publisherProfileManager.getSynapseArtifactList(CarbonContext.getThreadLocalCarbonContext().getTenantId());

            if (artifactList == null) return;

            for (StructuringArtifact artifact : artifactList) {
                ConfigurationPublisher.process(artifact, config);
            }
        }
    }

    public PublisherConfig getEventingConfigData(String serverId) {
        return  publisherProfileManager.getPublisherProfiles(CarbonContext.getThreadLocalCarbonContext().getTenantId(), serverId).getConfig();
    }

    public PublisherConfig[] getAllPublisherNames() {
        List<PublisherProfile> profileArrayList = publisherProfileManager.getTenantPublisherProfilesList(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        ArrayList<PublisherConfig> publisherConfigs = new ArrayList<PublisherConfig>();

        for (PublisherProfile profile : profileArrayList) {
            publisherConfigs.add(profile.getConfig());
        }

        return publisherConfigs.toArray(new PublisherConfig[publisherConfigs.size()]);
    }

    public boolean removeServer(String serverId) {
        return publisherProfileManager.removePublisherProfile(CarbonContext.getThreadLocalCarbonContext().getTenantId(), serverId);
    }

    public boolean isCollectingEnabled() {
        return PublisherUtils.isTraceDataCollectingEnabled();
    }
}
