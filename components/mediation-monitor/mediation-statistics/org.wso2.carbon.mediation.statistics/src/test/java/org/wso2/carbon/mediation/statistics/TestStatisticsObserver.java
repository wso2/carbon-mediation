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

package org.wso2.carbon.mediation.statistics;

import org.apache.synapse.aspects.ComponentType;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class TestStatisticsObserver implements MediationStatisticsObserver {

    private ComponentType type;
    private boolean initialized = true;
    private int updateCount = 0;
    private int errorCount = 0;
    private Map<String,Double> entityInAverageTimes = new HashMap<String,Double>();
    private Map<String,Double> entityOutAverageTimes = new HashMap<String,Double>();
    private double seqInCategoryAvg = 0.0D;
    private double seqOutCategoryAvg = 0.0D;

    public TestStatisticsObserver(ComponentType type) {
        this.type = type;
    }

    public void destroy() {
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public double getSeqInCategoryAvg() {
        return seqInCategoryAvg;
    }

    public double getSeqOutCategoryAvg() {
        return seqOutCategoryAvg;
    }

    public double getEntityAverage(String resourceId, boolean in) {
        Map<String,Double> entityAverages = in ? entityInAverageTimes : entityOutAverageTimes;
        return entityAverages.get(resourceId);
    }

    public void updateStatistics(MediationStatisticsSnapshot snapshot) {

        updateCount++;

        if (snapshot.getErrorLogs() != null) {
            errorCount += snapshot.getErrorLogs().size();
        }

        StatisticsRecord entitySnapshotCopy, categorySnapshotCopy;
        if (snapshot.getEntitySnapshot() != null) {
            entitySnapshotCopy = new StatisticsRecord(snapshot.getEntitySnapshot());
            entitySnapshotCopy.updateRecord(snapshot.getUpdate());
        } else {
            entitySnapshotCopy = snapshot.getUpdate();
        }

        if (snapshot.getCategorySnapshot() != null) {
            categorySnapshotCopy = new StatisticsRecord(snapshot.getCategorySnapshot());
            categorySnapshotCopy.updateRecord(snapshot.getUpdate());
        } else {
            categorySnapshotCopy = snapshot.getUpdate();
        }

        if (snapshot.getUpdate().isInStatistic()) {
            entityInAverageTimes.put(entitySnapshotCopy.getResourceId(),
                    entitySnapshotCopy.getAvgTime());
            if (snapshot.getCategorySnapshot().getType() == type) {
                seqInCategoryAvg = categorySnapshotCopy.getAvgTime();
            }
        } else {
            entityOutAverageTimes.put(entitySnapshotCopy.getResourceId(),
                    entitySnapshotCopy.getAvgTime());
            if (snapshot.getCategorySnapshot().getType() == type) {
                seqOutCategoryAvg = categorySnapshotCopy.getAvgTime();
            }
        }
    }

    public void notifyTraceLogs(MessageTraceLog[] logs) {
        
    }
}
