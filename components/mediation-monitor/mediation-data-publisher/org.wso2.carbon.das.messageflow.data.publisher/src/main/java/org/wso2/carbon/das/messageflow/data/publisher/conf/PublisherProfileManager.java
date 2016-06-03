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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.structuring.StructuringArtifact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublisherProfileManager {

    private static Log log = LogFactory.getLog(PublisherProfileManager.class);

    // Uses to persists changes to registry
    private RegistryPersistenceManager registryPersistenceManager;

    // Common place for storing all PublisherProfiles
    private static Map<Integer, HashMap<String, PublisherProfile>> publisherProfiles =
            new HashMap<Integer, HashMap<String, PublisherProfile>>();

    public PublisherProfileManager() {
        registryPersistenceManager = new RegistryPersistenceManager();
    }

    public PublisherProfile getPublisherProfiles(int tenantId, String serverId) {
        return publisherProfiles.get(tenantId).get(serverId);
    }

    public List<PublisherProfile> getTenantPublisherProfilesList (int tenantId) {
        if (publisherProfiles.get(tenantId) == null) {
            List<PublisherConfig> publisherConfigs = registryPersistenceManager.load(tenantId);
            List <PublisherProfile> profileList = new ArrayList<PublisherProfile>();

            HashMap<String, PublisherProfile> profileMap = new HashMap<String, PublisherProfile>();


            for (PublisherConfig config : publisherConfigs) {
                profileList.add(new PublisherProfile(config));
                profileMap.put(config.getServerId(), new PublisherProfile(config));
            }

            publisherProfiles.put(tenantId, profileMap);
        }
        if (publisherProfiles.get(tenantId).values() == null){
            return new ArrayList<PublisherProfile>();
        } else {
            return new ArrayList<PublisherProfile>(publisherProfiles.get(tenantId).values());
        }
    }

    public void addPublisherProfile(int tenantId, String serverId, PublisherProfile profile) {
        publisherProfiles.get(tenantId).put(serverId, profile);

        registryPersistenceManager.update(tenantId, profile.getConfig());
    }

    public boolean removePublisherProfile(int tenantId, String serverId) {
        return (publisherProfiles.get(tenantId).remove(serverId) != null)
                && registryPersistenceManager.remove(tenantId, serverId);
    }


    /**
     * This will load publishing-server-configurations from the registry.
     * This wil be called when the component gets activated
     *
     * @param tenantId  Tenant ID to load
     */
    public void loadTenantPublisherProfilesFromRegistry (int tenantId) {
        List<PublisherConfig> configList = registryPersistenceManager.load(tenantId);
        HashMap<String, PublisherProfile>  profiles = new HashMap<String, PublisherProfile>();

        for(PublisherConfig aConfig : configList) {
            profiles.put(aConfig.getServerId(), new PublisherProfile(aConfig));
        }

        publisherProfiles.put(tenantId, profiles);
    }
}
