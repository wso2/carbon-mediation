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

public class SequenceStatView implements SequenceStatViewMBean {
    private long transactionsInLastMin;
    private long numberOfErrorsInLastMin;
    private double avgTimeInLastMin;
    private double avgMediationTimeInLastMin;
    private double errorPercentageInLastMin;

    public double getAvgMediationTimeInLastMin() {
        return avgMediationTimeInLastMin;
    }

    public void setAvgMediationTimeInLastMin(double avgMediationTimeInLastMin) {
        this.avgMediationTimeInLastMin = avgMediationTimeInLastMin;
    }

    public void setTransactionsInLastMin(long transactionsInLastMin) {
        this.transactionsInLastMin = transactionsInLastMin;
    }

    public void setNumberOfErrorsInLastMin(long numberOfErrorsInLastMin) {
        this.numberOfErrorsInLastMin = numberOfErrorsInLastMin;
    }

    public void setAvgTimeInLastMin(double avgTimeInLastMin) {
        this.avgTimeInLastMin = avgTimeInLastMin;
    }

    public long getTransactionsIn() {
        return transactionsInLastMin;
    }

    public long getNumberOfErrorsIn() {
        return numberOfErrorsInLastMin;
    }

    public double getAvgTimeIn() {
        return avgTimeInLastMin;
    }

    public double getErrorPercentageInLastMin() {
        return errorPercentageInLastMin;
    }

    public void setErrorPercentageInLastMin(double errorPercentageInLastMin) {
        this.errorPercentageInLastMin = errorPercentageInLastMin;
    }

    public void reset() {
        transactionsInLastMin = 0;
        numberOfErrorsInLastMin = 0;
        avgTimeInLastMin = 0.0D;
        errorPercentageInLastMin = 0.0D;
        avgMediationTimeInLastMin = 0.0D;
    }
}

