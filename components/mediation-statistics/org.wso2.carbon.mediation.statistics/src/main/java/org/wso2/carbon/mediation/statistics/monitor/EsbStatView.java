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

public class EsbStatView implements EsbStatViewMBean {

    private long transactionsInLastMin;
    private long errorsInLastMin;
    private double errorPercentageInLastMin;

    public long getTransactionsIn() {
        return transactionsInLastMin;
    }

    public long getErrorsIn() {
        return errorsInLastMin;
    }

    public double getErrorPercentageInLastMin() {
        return errorPercentageInLastMin;
    }

    public void setTransactionsInLastMin(long transactionsInLastMin) {
        this.transactionsInLastMin = transactionsInLastMin;
    }

    public void setErrorsInLastMin(long errorsInLastMin) {
        this.errorsInLastMin = errorsInLastMin;
    }

    public void setErrorPercentageInLastMin(double errorPercentageInLastMin) {
        this.errorPercentageInLastMin = errorPercentageInLastMin;
    }

    public void reset() {
        transactionsInLastMin = 0;
        errorsInLastMin = 0;
        errorPercentageInLastMin = 0.0D;
    }
}

