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

/**
 * A Bean class to keep the entire throttle policy configs
 */

public class ThrottlePolicy {

    /**
     * Maximum allowable number of concurrent accesses to the specified service
     */
    private int maxConcurrentAccesses;

    /**
     * User can add any number of configs to control different IP ranges.
     * This array keeps those data
     */
    private InternalData[] internalConfigs = new InternalData[0];

    /**
     * to indicate whether throttling is engaged or not
     */
    private boolean isEngaged;

    public int getMaxConcurrentAccesses() {
        return maxConcurrentAccesses;
    }

    public void setMaxConcurrentAccesses(int maxConcurrentAccesses) {
        this.maxConcurrentAccesses = maxConcurrentAccesses;
    }

    public InternalData[] getInternalConfigs() {
        return internalConfigs;
    }

    public void setInternalConfigs(InternalData[] internalConfigs) {
        this.internalConfigs = internalConfigs;
    }

    public boolean isEngaged() {
        return isEngaged;
    }

    public void setEngaged(boolean engaged) {
        isEngaged = engaged;
    }

}
