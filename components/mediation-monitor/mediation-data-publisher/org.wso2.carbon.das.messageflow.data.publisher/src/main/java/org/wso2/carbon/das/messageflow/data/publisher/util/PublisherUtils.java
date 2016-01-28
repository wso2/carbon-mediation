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
package org.wso2.carbon.das.messageflow.data.publisher.util;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.aspects.statistics.ErrorLog;
import org.wso2.carbon.das.messageflow.data.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.das.messageflow.data.publisher.services.DASMessageFlowPublisherAdmin;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.mediation.statistics.MediationStatisticsSnapshot;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublisherUtils {

    private static ConfigurationContext axisConfigurationContext;
    private static ConfigurationContextService configurationContextService;
    private static DASMessageFlowPublisherAdmin dasMessageFlowPublisherAdmin;

    private static final String TRANSPORT = "https"; // TODO: it is not ideal to assume https is always available

    private static boolean isTraceDataCollectingEnabled = false;

    public static boolean isTraceDataCollectingEnabled() {
        return isTraceDataCollectingEnabled;
    }

    public static void setTraceDataCollectingEnabled(boolean isTraceDataCollectingEnabled) {
        PublisherUtils.isTraceDataCollectingEnabled = isTraceDataCollectingEnabled;
    }

    private static Map<String,EventPublisherConfig> eventPublisherConfigMap =
            new HashMap<String, EventPublisherConfig>();

    public static EventPublisherConfig getEventPublisherConfig(String key) {
        return eventPublisherConfigMap.get(key);
    }

    public static Map<String,EventPublisherConfig> getEventPublisherConfigMap(){
        return eventPublisherConfigMap;
    }

    public static void setConfigurationContext(ConfigurationContext axisConfigContext) {
        axisConfigurationContext = axisConfigContext;
    }


    public static ConfigurationContext getConfigurationContext() {
        return axisConfigurationContext;
    }

    public static void setConfigurationContextService(ConfigurationContextService contextService) {
        configurationContextService = contextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }


    public static DASMessageFlowPublisherAdmin getMediationStatPublisherAdmin() {
        return dasMessageFlowPublisherAdmin;
    }

    public static void setMediationStatPublisherAdmin(
            DASMessageFlowPublisherAdmin mediationStatsPublisherAdmin) {
        dasMessageFlowPublisherAdmin = mediationStatsPublisherAdmin;
    }

}

