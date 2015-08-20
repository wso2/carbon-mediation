/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediation.throttle;


import org.apache.synapse.commons.throttle.core.ThrottleConstants;

/**
 * A Bean class to keep configs for a particular IP range. Corresponds to a single
 * row of the table in the UI
 */

public class InternalData {

    /**
     * range to control access
     */
    private String range;

    /**
     * rangeType can be IP or DOMAIN. default is set to IP
     */
    private String rangeType = "IP";

    /**
     * three parameters to set if the access is constrained
     */
    private int maxRequestCount;
    private int unitTime;
    private int prohibitTimePeriod;

    /**
     * variable to indicate whether access is allowed for the specified range.
     * If allowed, no need to set the above three parameters as access is not
     * controlled
     */
    private int accessLevel = ThrottleConstants.ACCESS_ALLOWED;

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangeType) {
        this.rangeType = rangeType;
    }

    public int getMaxRequestCount() {
        return maxRequestCount;
    }

    public void setMaxRequestCount(int maxRequestCount) {
        this.maxRequestCount = maxRequestCount;
    }

    public int getUnitTime() {
        return unitTime;
    }

    public void setUnitTime(int unitTime) {
        this.unitTime = unitTime;
    }

    public int getProhibitTimePeriod() {
        return prohibitTimePeriod;
    }

    public void setProhibitTimePeriod(int prohibitTimePeriod) {
        this.prohibitTimePeriod = prohibitTimePeriod;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
}
