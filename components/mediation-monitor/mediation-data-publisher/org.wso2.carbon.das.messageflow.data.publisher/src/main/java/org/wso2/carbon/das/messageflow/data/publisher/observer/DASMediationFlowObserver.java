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
package org.wso2.carbon.das.messageflow.data.publisher.observer;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.wso2.carbon.das.messageflow.data.publisher.conf.PublisherProfile;
import org.wso2.carbon.das.messageflow.data.publisher.conf.PublisherProfileManager;
import org.wso2.carbon.das.messageflow.data.publisher.publish.StatisticsPublisher;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;


public class DASMediationFlowObserver implements MessageFlowObserver,
                                                 TenantInformation {

    private static final Log log = LogFactory.getLog(DASMediationFlowObserver.class);
    private int tenantId = -1234;

    private PublisherProfileManager publisherProfileManager;

    public DASMediationFlowObserver() {
        this.publisherProfileManager = new PublisherProfileManager();
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down the mediation statistics observer of DAS");
        }
    }

    @Override
    public void updateStatistics(PublishingFlow flow) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId,true);
            updateStatisticsInternal(flow);
        } catch (Exception e) {
            log.error("failed to update statics from DAS publisher", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void updateStatisticsInternal(PublishingFlow flow)
            throws Exception {
        int tenantID = getTenantId();
        List<PublisherProfile> publisherProfiles = publisherProfileManager.getTenantPublisherProfilesList(tenantID);

        if (publisherProfiles.isEmpty()) {
            return;
        }

        for (PublisherProfile aProfile : publisherProfiles) {
            StatisticsPublisher.process(flow, aProfile.getConfig());
        }
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(int i) {
        tenantId = i;
    }
}
