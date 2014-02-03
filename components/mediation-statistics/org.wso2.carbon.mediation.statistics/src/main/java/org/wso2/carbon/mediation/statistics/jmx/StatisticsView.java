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

package org.wso2.carbon.mediation.statistics.jmx;

import org.wso2.carbon.mediation.statistics.StatisticsRecord;

/**
 * MBean for exposing stats over JMX
 */
public class StatisticsView implements StatisticsViewMBean {
    private long totalCount = 0;
    private long faultCount = 0;
    private long maxTime = 0L;
    private long minTime = StatisticsRecord.DEFAULT_MIN_TIME;
    private double avgTime = 0.0D;

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setFaultCount(long faultCount) {
        this.faultCount = faultCount;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getFaultCount() {
        return faultCount;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public long getMinTime() {
        return minTime;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void reset() {
        totalCount = 0;
        faultCount = 0;
        maxTime = 0L;
        minTime = StatisticsRecord.DEFAULT_MIN_TIME;
        avgTime = 0.0D;
    }
}
