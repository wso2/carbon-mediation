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
package org.wso2.carbon.bam.mediationstats.data.publisher.util;


import org.wso2.carbon.core.RegistryResources;

public class MediationDataPublisherConstants {

    public static final String MEDIATION_STATISTICS_REG_PATH = RegistryResources.COMPONENTS
                                                               + "org.wso2.carbon.bam.mediationstats.data.publisher/mediationstats";

    public static final String MEDIATION_STATISTICS_PROPERTIES_REG_PATH = RegistryResources.COMPONENTS
                                                             + "org.wso2.carbon.bam.mediationstats.data.publisher/properties";


    // key, value constants
    public static final String MAX_PROCESS_TIME = "max_processing_time";
    public static final String MIN_PROCESS_TIME = "min_processing_time";
    public static final String AVG_PROCESS_TIME = "avg_processing_time";
    public static final String COUNT = "count";
    public static final String CUMULATIVE_COUNT = "cumulative_count";
    public static final String FAULT_COUNT = "fault_count";
    public static final String BAM_ID = "ID";

    public static final String IN_STATISTIC = "In";
    public static final String OUT_STATISTIC = "Out";

    public static final String DIRECTION = "direction";
    public static final String TIMESTAMP = "timestamp";
    public static final String RESOURCE_ID = "resource_id";
    public static final String STATS_TYPE = "stats_type";


    public static final String ENABLE_MEDIATION_STATS = "EnableMediationStats";

    public static final String CLOUD_DEPLOYMENT_PROP = "IsCloudDeployment";

    public static final String SERVER_CONFIG_BAM_URL = "BamServerURL";

    public static final String DEFAULT_BAM_SERVER_URL = "tcp://127.0.0.1:7611";

}
