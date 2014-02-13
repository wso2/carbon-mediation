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
import org.wso2.carbon.mediation.statistics.StatisticsRecord;
import org.apache.synapse.aspects.statistics.view.Statistics;
import org.apache.synapse.aspects.ComponentType;

public class StatisticsRecordTest extends TestCase {

    private String resourceId = "BogusEntity";
    private long value1 = 100L;
    private long value2 = 112L;
    private long value3 = 220L;

    public void testRecordCreation() {

        Statistics stats1 = new Statistics(resourceId);
        stats1.update(value1, false);

        Statistics stats2 = new Statistics(resourceId);
        stats2.update(value2, false);
        stats2.update(value3, true);

        StatisticsRecord record = new StatisticsRecord(resourceId,
                ComponentType.SEQUENCE, true, stats1);

        assertEquals(1, record.getTotalCount());
        assertEquals(0, record.getFaultCount());
        assertEquals(value1, record.getMaxTime());
        assertEquals(value1, record.getMinTime());
        assertEquals(Double.valueOf(value1), record.getAvgTime());
        assertEquals(true, record.isInStatistic());
        assertEquals(resourceId, record.getResourceId());
        assertEquals(ComponentType.SEQUENCE, record.getType());

        StatisticsRecord update = new StatisticsRecord(resourceId,
                ComponentType.SEQUENCE, true, stats2);

        assertEquals(2, update.getTotalCount());
        assertEquals(1, update.getFaultCount());
        assertEquals(value3, update.getMaxTime());
        assertEquals(value2, update.getMinTime());
        assertEquals(Double.valueOf((value2+value3)/2), update.getAvgTime());
    }

    public void testRecordUpdateOperations() {
        Statistics stats1 = new Statistics(resourceId);
        stats1.update(value1, false);

        Statistics stats2 = new Statistics(resourceId);
        stats2.update(value2, false);
        stats2.update(value3, true);

        StatisticsRecord record = new StatisticsRecord(resourceId,
                ComponentType.SEQUENCE, true, stats1);
        StatisticsRecord update = new StatisticsRecord(resourceId,
                ComponentType.SEQUENCE, true, stats2);

        record.updateRecord(update);

        assertEquals(3, record.getTotalCount());
        assertEquals(1, record.getFaultCount());
        assertEquals(value3, record.getMaxTime());
        assertEquals(value1, record.getMinTime());
        assertEquals(Double.valueOf((value1+value2+value3)/3), record.getAvgTime());
    }

}
