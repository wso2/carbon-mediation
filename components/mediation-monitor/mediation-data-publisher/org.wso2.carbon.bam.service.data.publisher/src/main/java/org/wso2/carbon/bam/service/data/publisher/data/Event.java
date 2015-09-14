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
package org.wso2.carbon.bam.service.data.publisher.data;

import org.wso2.carbon.bam.service.data.publisher.util.StatisticsType;

import java.util.List;


public class Event {

    List<Object> correlationData;
    List<Object> metaData;
    List<Object> eventData;
    StatisticsType statisticsType;

    public StatisticsType getStatisticsType() {
        return statisticsType;
    }

    public void setStatisticsType(StatisticsType statisticsType) {
        this.statisticsType = statisticsType;
    }

    public List<Object> getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(List<Object> correlationData) {
        this.correlationData = correlationData;
    }

    public List<Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<Object> metaData) {
        this.metaData = metaData;
    }

    public List<Object> getEventData() {
        return eventData;
    }

    public void setEventData(List<Object> eventData) {
        this.eventData = eventData;
    }
}
