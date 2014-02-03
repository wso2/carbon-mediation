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

import junit.framework.TestCase;
import org.apache.synapse.aspects.statistics.view.InOutStatisticsView;
import org.apache.synapse.aspects.statistics.ErrorLog;
import org.apache.synapse.aspects.ComponentType;

import java.util.Map;

public class StatisticsStoreTest extends TestCase {

    private ComponentType type = ComponentType.SEQUENCE;

    private String resource1 = "Entry1";
    private String resource2 = "Entry2";

    // Keep the values in the ascending order
    private long value1 = 25L;
    private long value2 = 55L;
    private long value3 = 80L;
    private long value4 = 100L;
    private long value5 = 112L;
    private long value6 = 198L;

    private static final MediationStatisticsStore store = new MediationStatisticsStore();

    public void testStatUpdateOperations() {
        TestStatisticsObserver observer = new TestStatisticsObserver(type);
        store.registerObserver(observer);

        runPhaseOne();
        runPhaseTwo();
        runPhaseThree();
        
        Map<String,Integer> totalCounts = store.getTotalCounts(type);
        assertEquals(2, totalCounts.get(resource1).intValue());
        assertEquals(4, totalCounts.get(resource2).intValue());

        // Test the observer API
        assertEquals(6, observer.getUpdateCount());
        assertEquals(1, observer.getErrorCount());
        double avg = ((double) value1 + (double) value3 + (double) value5)/3;
        assertEquals(avg, observer.getSeqInCategoryAvg());
        avg = ((double) value2 + (double) value4 + (double) value6)/3;
        assertEquals(avg, observer.getSeqOutCategoryAvg());

        assertEquals((double) value1, observer.getEntityAverage(resource1, true));
        avg = ((double) value3 + (double) value5) / 2;
        assertEquals(avg, observer.getEntityAverage(resource2, true));
        assertEquals((double) value2, observer.getEntityAverage(resource1, false));
        avg = ((double) value4 + (double) value6) / 2;
        assertEquals(avg, observer.getEntityAverage(resource2, false));

        store.unregisterObservers();
        assertEquals(false, observer.isInitialized());
    }

    private void runPhaseOne() {
        System.out.println("Starting phase 1");
        store.updateStatistics(getDataView(resource1, value1, value2));

        StatisticsRecord categoryInRecord  = store.getRecordByCategory(type, true);
        assertEquals(1, categoryInRecord.getTotalCount());
        assertEquals(0, categoryInRecord.getFaultCount());
        assertEquals(value1, categoryInRecord.getMinTime());
        assertEquals(value1, categoryInRecord.getMaxTime());
        assertEquals((double) value1, categoryInRecord.getAvgTime());

        StatisticsRecord categoryOutRecord  = store.getRecordByCategory(type, false);
        assertEquals(1, categoryOutRecord.getTotalCount());
        assertEquals(0, categoryOutRecord.getFaultCount());
        assertEquals(value2, categoryOutRecord.getMinTime());
        assertEquals(value2, categoryOutRecord.getMaxTime());
        assertEquals((double) value2, categoryOutRecord.getAvgTime());

        StatisticsRecord entityInRecord = store.getRecordByResource(resource1, type, true);
        assertEquals(1, entityInRecord.getTotalCount());
        assertEquals(0, entityInRecord.getFaultCount());
        assertEquals(value1, entityInRecord.getMinTime());
        assertEquals(value1, entityInRecord.getMaxTime());
        assertEquals((double) value1, entityInRecord.getAvgTime());

        StatisticsRecord entityOutRecord = store.getRecordByResource(resource1, type, false);
        assertEquals(1, entityOutRecord.getTotalCount());
        assertEquals(0, entityOutRecord.getFaultCount());
        assertEquals(value2, entityOutRecord.getMinTime());
        assertEquals(value2, entityOutRecord.getMaxTime());
        assertEquals((double) value2, entityOutRecord.getAvgTime());
    }

    private void runPhaseTwo() {
        System.out.println("Starting phase 2");
        store.updateStatistics(getDataView(resource2, value3, value4));

        StatisticsRecord categoryInRecord  = store.getRecordByCategory(type, true);
        assertEquals(2, categoryInRecord.getTotalCount());
        assertEquals(0, categoryInRecord.getFaultCount());
        assertEquals(value1, categoryInRecord.getMinTime());
        assertEquals(value3, categoryInRecord.getMaxTime());
        double avg = ((double) value1 + (double) value3)/2;
        assertEquals(avg, categoryInRecord.getAvgTime());

        StatisticsRecord categoryOutRecord  = store.getRecordByCategory(type, false);
        assertEquals(2, categoryOutRecord.getTotalCount());
        assertEquals(0, categoryOutRecord.getFaultCount());
        assertEquals(value2, categoryOutRecord.getMinTime());
        assertEquals(value4, categoryOutRecord.getMaxTime());
        avg = ((double) value2 + (double) value4)/2;
        assertEquals(avg, categoryOutRecord.getAvgTime());

        StatisticsRecord entityInRecord = store.getRecordByResource(resource2, type, true);
        assertEquals(1, entityInRecord.getTotalCount());
        assertEquals(0, entityInRecord.getFaultCount());
        assertEquals(value3, entityInRecord.getMinTime());
        assertEquals(value3, entityInRecord.getMaxTime());
        assertEquals((double) value3, entityInRecord.getAvgTime());

        StatisticsRecord entityOutRecord = store.getRecordByResource(resource2, type, false);
        assertEquals(1, entityOutRecord.getTotalCount());
        assertEquals(0, entityOutRecord.getFaultCount());
        assertEquals(value4, entityOutRecord.getMinTime());
        assertEquals(value4, entityOutRecord.getMaxTime());
        assertEquals((double) value4, entityOutRecord.getAvgTime());
    }

    private void runPhaseThree() {
        System.out.println("Starting phase 3");
        InOutStatisticsView view = getDataView(resource2, value5, value6);
        ErrorLog log = new ErrorLog("1001");
        view.getInStatistics().addErrorLog(log);
        store.updateStatistics(view);

        StatisticsRecord categoryInRecord  = store.getRecordByCategory(type, true);
        assertEquals(3, categoryInRecord.getTotalCount());
        assertEquals(0, categoryInRecord.getFaultCount());
        assertEquals(value1, categoryInRecord.getMinTime());
        assertEquals(value5, categoryInRecord.getMaxTime());
        double avg = ((double) value1 + (double) value3 + (double) value5)/3;
        assertEquals(avg, categoryInRecord.getAvgTime());

        StatisticsRecord categoryOutRecord  = store.getRecordByCategory(type, false);
        assertEquals(3, categoryOutRecord.getTotalCount());
        assertEquals(0, categoryOutRecord.getFaultCount());
        assertEquals(value2, categoryOutRecord.getMinTime());
        assertEquals(value6, categoryOutRecord.getMaxTime());
        avg = ((double) value2 + (double) value4 + (double) value6)/3;
        assertEquals(avg, categoryOutRecord.getAvgTime());

        StatisticsRecord entityInRecord = store.getRecordByResource(resource2, type, true);
        assertEquals(2, entityInRecord.getTotalCount());
        assertEquals(0, entityInRecord.getFaultCount());
        assertEquals(value3, entityInRecord.getMinTime());
        assertEquals(value5, entityInRecord.getMaxTime());
        avg = ((double) value3 + (double) value5)/2;
        assertEquals(avg, entityInRecord.getAvgTime());

        StatisticsRecord entityOutRecord = store.getRecordByResource(resource2, type, false);
        assertEquals(2, entityOutRecord.getTotalCount());
        assertEquals(0, entityOutRecord.getFaultCount());
        assertEquals(value4, entityOutRecord.getMinTime());
        assertEquals(value6, entityOutRecord.getMaxTime());
        avg = ((double) value4 + (double) value6)/2;
        assertEquals(avg, entityOutRecord.getAvgTime());
    }

    private InOutStatisticsView getDataView(String resourceId, long in, long out) {
        InOutStatisticsView view = new InOutStatisticsView(resourceId, "test.wso2.org", type);
        view.getInStatistics().update(in, false);
        view.getOutStatistics().update(out, false);
        return view;
    }

}
