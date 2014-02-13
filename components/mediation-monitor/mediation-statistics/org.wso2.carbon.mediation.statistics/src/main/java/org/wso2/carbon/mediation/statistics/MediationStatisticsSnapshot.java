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

import org.apache.synapse.aspects.statistics.ErrorLog;

import java.util.List;

public class MediationStatisticsSnapshot {

    private StatisticsRecord update;
    private StatisticsRecord entitySnapshot;
    private StatisticsRecord categorySnapshot;
    private List<ErrorLog> errorLogs;

    /**
     * Get the cumulative record for the entire category to which the resource belongs
     *
     * @return A StatisticsRecord object or null
     */
    public StatisticsRecord getCategorySnapshot() {
        return categorySnapshot;
    }

    void setCategorySnapshot(StatisticsRecord categorySnapshot) {
        this.categorySnapshot = categorySnapshot;
    }

    /**
     * Get the cumulative record for the resource
     *
     * @return A StatisticsRecord object or null
     */
    public StatisticsRecord getEntitySnapshot() {
        return entitySnapshot;
    }

    void setEntitySnapshot(StatisticsRecord entitySnapshot) {
        this.entitySnapshot = entitySnapshot;
    }

    /**
     * Get the list of error logs generated on the resource since the last update
     *
     * @return a list of ErrorLog objects or null
     */
    public List<ErrorLog> getErrorLogs() {
        return errorLogs;
    }

    void setErrorLogs(List<ErrorLog> errorLogs) {
        this.errorLogs = errorLogs;
    }

    /**
     * Get the latest instance update on the resource. This contains data gathered
     * since the last update.
     *
     * @return A StatisticsRecord object
     */
    public StatisticsRecord getUpdate() {
        return update;
    }

    void setUpdate(StatisticsRecord update) {
        this.update = update;
    }
}
