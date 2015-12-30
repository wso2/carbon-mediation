/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.statistics.persistence;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.statistics.*;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.core.RegistryResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Queue;
import java.util.LinkedList;

/**
 * This MediationStatisticsObserver implementation persists the statistics records
 * to the registry. A resource is created for each record and data values are stored
 * as resource properties. All the records and updates sent by the statistics reporting
 * logic are put into a queue and a separate worker thread is used to process them one
 * by one. This prevents the statistics reporting logic getting unnecessarily delayed
 * by expensive registry operations.
 */
public final class PersistingStatisticsObserver implements MediationStatisticsObserver {

    private static final Log log = LogFactory.getLog(MediationStatisticsObserver.class);

    private Queue<StatisticsRecord> dataQueue = new LinkedList<StatisticsRecord>();
    private RegistryWriter writerTask;
    private Registry registry;
    private boolean proceed;

    private final String rootPath;

    private static final String PROXY = "/proxy-services";
    private static final String SEQUENCES = "/sequences";
    private static final String ENDPOINTS = "/endpoints";

    public PersistingStatisticsObserver(String rootPath) {
        if (rootPath == null) {
            this.rootPath = RegistryResources.ROOT + "mediation-stats/";
        } else {
            this.rootPath = rootPath;
        }

        proceed = true;

        try {
            registry = ServiceReferenceHolder.getInstance().getGovernanceRegistry();
            writerTask = new RegistryWriter();
            writerTask.start();
        } catch (RegistryException e) {
            log.error("Error while obtaining a registry instance. The persisting statistics " +
                    "observer will not function.", e);
        }
    }

    public void destroy() {
        proceed = false;
    }

    public void updateStatistics(MediationStatisticsSnapshot snapshot) {

        if (!proceed) {
            // If we are about to shutdown do not accept any more updates.
            // If everything goes alright this method will not be called when the
            // system is going down....
            return;
        }

        StatisticsRecord latestRecord;
        if (snapshot.getEntitySnapshot() != null) {
            latestRecord = new StatisticsRecord(snapshot.getEntitySnapshot());
            latestRecord.updateRecord(snapshot.getUpdate());
        } else {
            latestRecord = snapshot.getUpdate();
        }

        // Simple store this in the queue for now and return...
        dataQueue.add(latestRecord);
    }

    public void notifyTraceLogs(MessageTraceLog[] logs) {
        
    }

    private void persistRecord(StatisticsRecord record) throws RegistryException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            String path = calculatePath(record);
            Resource recordResource = registry.newResource();
            recordResource.setProperty("totalCount", String.valueOf(record.getTotalCount()));
            recordResource.setProperty("faultCount", String.valueOf(record.getFaultCount()));
            recordResource.setProperty("maxTime", String.valueOf(record.getMaxTime()));
            recordResource.setProperty("minTime", String.valueOf(record.getMinTime()));
            recordResource.setProperty("avgTime", String.valueOf(record.getAvgTime()));
            registry.put(path, recordResource);
            recordResource.discard();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private String calculatePath(StatisticsRecord entityRecord) {
        String type = "";
        switch (entityRecord.getType()) {
            case PROXYSERVICE:
                type = PROXY;
                break;

            case SEQUENCE:
                type = SEQUENCES;
                break;

            case ENDPOINT:
                type = ENDPOINTS;
                break;
        }

        String direction = "/in";
        if (!entityRecord.isInStatistic()) {
            direction = "/out";
        }

        return rootPath + type + "/" + entityRecord.getResourceId() + direction;
    }

    private class RegistryWriter extends Thread {

        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Mediation statistics persisting thread has started");
            }

            while (proceed || dataQueue.size() > 0) {
                StatisticsRecord record = dataQueue.poll();
                if (record == null) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {  }
                    continue;
                }

                try {
                    persistRecord(record);
                } catch (Throwable t) {
                    log.error("Error while accessing the registry for statistics " +
                            "persistence", t);
                }
            }
        }
    }
}