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
package org.wso2.carbon.mediation.initializer.services;

import org.apache.synapse.config.SynapseConfiguration;
import org.wso2.carbon.mediation.initializer.internal.CAppArtifactWrapper;
import org.wso2.carbon.mediation.initializer.internal.CAppDataHolder;
import org.wso2.carbon.mediation.initializer.utils.CAppArtifactsMap;

public class CAppArtifactDataServiceImpl implements CAppArtifactDataService {

    @Override
    public boolean isArtifactDeployedFromCApp(int tenantId, String name) {
        return CAppDataHolder.getInstance().isDeployedFromCApp(tenantId, name);
    }

    @Override
    public boolean isArtifactEdited(int tenantId, String name) {
        return CAppDataHolder.getInstance().isCAppArtifactEdited(tenantId, name);
    }

    @Override
    public void setEdited(int tenantId, String name) {
        CAppDataHolder.getInstance().setEdited(tenantId, name);
    }

    @Override
    public CAppArtifactWrapper removeCAppArtifactsBeforePersist(int tenantId, SynapseConfiguration synapseConfiguration) {
        return CAppDataHolder.getInstance().removeCAppArtifactsBeforePersist(tenantId, synapseConfiguration);
    }

    @Override
    public void addCAppArtifactData(int tenantId, CAppArtifactsMap cAppArtifactsMap) {
        CAppDataHolder.getInstance().addCAppArtifactData(tenantId, cAppArtifactsMap);
    }


    @Override
    public void removeCappArtifactData(int tenantId) {
        CAppDataHolder.getInstance().removeCappArtifactData(tenantId);
    }

}
