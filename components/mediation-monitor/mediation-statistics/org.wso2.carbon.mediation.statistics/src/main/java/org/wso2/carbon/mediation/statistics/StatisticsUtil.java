/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediation.statistics;

import org.apache.log4j.Logger;
import org.apache.synapse.aspects.ComponentType;

import java.util.List;

public class StatisticsUtil {

    private static Logger log = Logger.getLogger(StatisticsUtil.class);
    public static final String ENDPOINT_CATEGORY = "endpoint";
    public static final String PROXY_SERVICE_CATEGORY = "proxy-service";
    public static final String SEQUENCE_CATEGORY = "sequence";

    public static final String IN_DIRECTION = "in";
    public static final String OUT_DIRECTION = "out";

    public static StatisticsRecord getCombinedRecord(List<StatisticsRecord> records) {
        if (records == null || records.size() == 0) {
            return null;
        }

        StatisticsRecord combinedRecord = new StatisticsRecord(records.get(0));
        records.remove(0);

        for (StatisticsRecord r : records) {
            combinedRecord.updateRecord(r);
        }
        return combinedRecord;
    }

    public static ComponentType getComponentType(int type) {
        switch (type) {
            case StatisticsConstants.ENDPOINT_STATISTICS : return ComponentType.ENDPOINT;
            case StatisticsConstants.PROXY_SERVICE_STATISTICS: return ComponentType.PROXYSERVICE;
            case StatisticsConstants.SEQUENCE_STATISTICS: return ComponentType.SEQUENCE;
            default: return null;
        }
    }

    public static String getCategory(long category) throws MediationStatisticsException {
        switch ((int) category) {
            case StatisticsConstants.ENDPOINT_STATISTICS:
                return ENDPOINT_CATEGORY;
            case StatisticsConstants.PROXY_SERVICE_STATISTICS:
                return PROXY_SERVICE_CATEGORY;
            case StatisticsConstants.SEQUENCE_STATISTICS:
                return SEQUENCE_CATEGORY;
            default:
                handleException("Unknown category");
        }
        return "";
    }

    public static String getDirection(long direction) throws MediationStatisticsException {
        switch ((int) direction) {
            case StatisticsConstants.IN:
                return IN_DIRECTION;
            case StatisticsConstants.OUT:
                return OUT_DIRECTION;
            default:
                handleException("Unknown direction");
        }
        return "";
    }

    public static long getCategory(String catetory) throws MediationStatisticsException {

        if (ENDPOINT_CATEGORY.equals(catetory)) {
            return StatisticsConstants.ENDPOINT_STATISTICS;
        } else if (PROXY_SERVICE_CATEGORY.equals(catetory)) {
            return StatisticsConstants.PROXY_SERVICE_STATISTICS;
        } else if (SEQUENCE_CATEGORY.equals(catetory)) {
            return StatisticsConstants.SEQUENCE_STATISTICS;
        } else {
            handleException("Unknow category " + catetory);
        }
        return -1;
    }

    public static long getDirection(String direction) throws MediationStatisticsException {
        if (IN_DIRECTION.equals(direction)) {
            return StatisticsConstants.IN;
        } else if (OUT_DIRECTION.equals(direction)) {
            return StatisticsConstants.OUT;
        } else {
            handleException("Unknown direction " + direction);
        }
        return -1;
    }

    private static void handleException(String msg) throws MediationStatisticsException {
        log.error(msg);
        throw new MediationStatisticsException(msg);
    }

}