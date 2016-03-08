/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.das.messageflow.data.publisher.services;


import org.wso2.carbon.das.messageflow.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.das.messageflow.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.das.messageflow.data.publisher.util.PublisherUtils;

public class DASMessageFlowPublisherAdmin extends AbstractAdmin {

    private RegistryPersistenceManager registryPersistenceManager;

    public DASMessageFlowPublisherAdmin() {
        registryPersistenceManager = new RegistryPersistenceManager();
    }

    public void configureEventing(MediationStatConfig mediationStatConfig) {
        registryPersistenceManager.update(mediationStatConfig,
                                          CarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    public MediationStatConfig getEventingConfigData(String serverId) {
        return registryPersistenceManager.get(serverId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    public MediationStatConfig[] getAllPublisherNames() {
        return registryPersistenceManager.getAllPublisherNames(CarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    public boolean removeServer(String serverId) {
        return registryPersistenceManager.remove(serverId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    public boolean isCollectingEnabled() {
        return PublisherUtils.isTraceDataCollectingEnabled();
    }
}
