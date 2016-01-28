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
package org.wso2.carbon.das.messageflow.data.publisher.observer;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.messageflowtracer.data.MessageFlowDataEntry;
import org.wso2.carbon.das.messageflow.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.das.messageflow.data.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.das.messageflow.data.publisher.publish.Publisher;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.statistics.*;

import java.util.List;


public class DASMediationStatisticsObserver implements MessageFlowTracingObserver,
                                                       TenantInformation {

    private static final Log log = LogFactory.getLog(DASMediationStatisticsObserver.class);
    private int tenantId = -1234;

    public DASMediationStatisticsObserver() {
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down the mediation statistics observer of DAS");
        }
    }

    @Override
    public void updateStatistics(Object traceEntry) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId,true);
            updateStatisticsInternal(traceEntry);
        } catch (Exception e) {
            log.error("failed to update statics from DAS publisher", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void updateStatisticsInternal(Object traceEntry)
            throws Exception {
        int tenantID = getTenantId();
        List<MediationStatConfig> mediationStatConfigList = new RegistryPersistenceManager().load(tenantID);

        if (mediationStatConfigList.isEmpty()) {
            return;
        }

        for (MediationStatConfig mediationStatConfig:mediationStatConfigList) {
            Publisher.process((MessageFlowDataEntry) traceEntry, mediationStatConfig);
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
