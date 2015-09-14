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
package org.wso2.carbon.bam.service.data.publisher.util;

import org.wso2.carbon.core.RegistryResources;

public final class ServiceStatisticsPublisherConstants {

    public static final String BAM_SERVICE_STATISTICS_PUBLISHER_MODULE_NAME = "wso2bampublisherservicestats";

    public static final String RESPONSE_TIME = "response_time";
    public static final String REQUEST_COUNT = "request_count";
    public static final String RESPONSE_COUNT = "response_count";
    public static final String FAULT_COUNT = "fault_count";


    // Registry persistence related constants
    public static final String SERVICE_STATISTICS_REG_PATH = RegistryResources.COMPONENTS
                                                             + "org.wso2.carbon.bam.service.data.publisher/service_stats/";
    public static final String ENABLE_SERVICE_STATS_EVENTING = "EnableServiceStats";



}
