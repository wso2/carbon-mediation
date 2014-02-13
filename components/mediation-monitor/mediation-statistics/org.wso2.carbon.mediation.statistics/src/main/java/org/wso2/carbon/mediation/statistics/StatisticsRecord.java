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
import org.apache.synapse.aspects.statistics.view.Statistics;

/**
 * Bean class to represent a mediation statistics record. A record can represent
 * data associated with a particular entity (eg: a proxy service) or an entire
 * category of entities (eg: all proxy services). If the record represents an
 * individual entity, the resource ID field of the record is the name of the entity.
 * The type field represents the type of the entity. Types are defined in the
 * org.apache.synapse.aspects.ComponentType enumeration. In scenarios where the
 * record is for an entire category, the resource ID field will get some random
 * value and only the type field becomes significant.
 */
public class StatisticsRecord {

    /** Default minimum response time */
    public static final long DEFAULT_MIN_TIME = -1L;

    private int totalCount = 0;
    private int faultCount = 0;
    private long maxTime = 0L;
    private long minTime = DEFAULT_MIN_TIME;
    private double avgTime = 0.0D;

    private boolean inStatistic = false;
    private String resourceId = null;
    private ComponentType type = null;

    /**
     * Constructs a new statistics record instance
     *
     * @param resourceId The name of th entity
     * @param type Type of the entity
     * @param inStatistic true if this is an in-flow statistic
     * @param data Data to be stored in this record
     */
    public StatisticsRecord(String resourceId, ComponentType type,
                            boolean inStatistic, Statistics data) {
        this.resourceId = resourceId;
        this.type = type;
        this.inStatistic = inStatistic;

        if (data != null) {
            totalCount = data.getCount();
            faultCount = data.getFaultCount();
            maxTime = data.getMaxProcessingTime();
            minTime = data.getMinProcessingTime();
            avgTime = data.getAvgProcessingTime();
        }
    }

    /**
     * Copy constructor to create a copy of an existing record. It is recommended
     * that all statistics consumer implementation make use of this method to
     * obtain copies of records before making any changes to the values stored
     * in a record object. Otherwise changes made by one consumer will propagate
     * to the other consumers.
     *
     * @param record an existing statistics record
     */
    public StatisticsRecord(StatisticsRecord record) {
        this.resourceId = record.resourceId;
        this.type = record.type;
        this.inStatistic = record.inStatistic;
        this.totalCount = record.totalCount;
        this.faultCount = record.faultCount;
        this.maxTime = record.maxTime;
        this.minTime = record.minTime;
        this.avgTime = record.avgTime;
    }

    public String getResourceId() {
        return resourceId;
    }

    public ComponentType getType() {
        return type;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(int faultCount) {
        this.faultCount = faultCount;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public long getMinTime() {
        return minTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public boolean isInStatistic() {
        return inStatistic;
    }

    /**
     * Update this record by adding another StatisticsRecord instance
     * into it. The total count and the fault count fields will be
     * incremented. The minimum and maximum time fields will be updated
     * if required. Average time will be recalculated.
     *
     * @param record the record to be added to this record
     */
    public void updateRecord(StatisticsRecord record) {

        int updatedTotalCount = record.getTotalCount() + this.totalCount;

        if (updatedTotalCount > 0) {
            synchronized (this) {
                this.faultCount += record.getFaultCount();
                this.avgTime = (record.getAvgTime() * record.getTotalCount() +
                        this.avgTime * this.totalCount) / updatedTotalCount;

                if (record.getMaxTime() > this.maxTime) {
                    this.maxTime = record.getMaxTime();
                }

                if (this.minTime == DEFAULT_MIN_TIME || record.getMinTime() < this.minTime) {
                    this.minTime = record.getMinTime();
                }

                this.totalCount = updatedTotalCount;
            }
        }
    }
}