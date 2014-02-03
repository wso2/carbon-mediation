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

package org.wso2.carbon.mediation.statistics.monitor;

public class MemoryStatView implements MemoryStatViewMBean {
    private float totalMemory;
    private float freeMemory;
    private float usedMemory;
    private float maxMemory;

    public float getTotalMemory() {
        return totalMemory;
    }

    public float getFreeMemory() {
        return freeMemory;
    }

    public float getUsedMemory() {
        return usedMemory;
    }

    public float getMaxMemory() {
        return maxMemory;
    }

    public void setTotalMemory(float totalMemory) {
        this.totalMemory = totalMemory;
    }

    public void setUsedMemory(float usedMemory) {
        this.usedMemory = usedMemory;
    }

    public void setFreeMemory(float freeMemory) {
        this.freeMemory = freeMemory;
    }

    public void setMaxMemory(float maxMemory) {
        this.maxMemory = maxMemory;
    }
}

