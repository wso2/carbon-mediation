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
    private static SynapseEnvironmentService synapseEnvironmentService;
    private static String serverName;

    private static final String TRANSPORT = "https"; // TODO: it is not ideal to assume https is always available

//    private static boolean isStatisticsReporterDisable = false;

    private static boolean isTraceDataCollectingEnabled = false;

//    public static void setStatisticsReporterDisable(boolean isStatisticsReporterDisabled) {
//        PublisherUtils.isStatisticsReporterDisable = isStatisticsReporterDisabled;
//    }
//
//    public static boolean getStatisticsReporterDisable() {
//        return isStatisticsReporterDisable;
//    }


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

//    public static void setSynapseEnvironmentService(
//            SynapseEnvironmentService synapseEnvironmentService) {
//        PublisherUtils.synapseEnvironmentService = synapseEnvironmentService;
//    }
//
//    public static SynapseEnvironmentService getSynapseEnvironmentService() {
//        return synapseEnvironmentService;
//    }


    /**
     * Get error counts
     *
     * @param mediationStatisticsSnapshot
     * @return
     */
    public static Map<String, Object> calculateErrorCounts(
            MediationStatisticsSnapshot mediationStatisticsSnapshot) {
        List<ErrorLog> errorLogs = mediationStatisticsSnapshot.getErrorLogs();
        Map<String, Object> errorMap = new HashMap<String, Object>();

        MediationSnapshotWrapper mediationSnapshotWrapper = new MediationSnapshotWrapper(
                mediationStatisticsSnapshot);

        // Iterate over error logs and create the errorMap with key-value pairs
        // as required by BAM
        if (errorLogs != null) {
            for (ErrorLog errorLog : errorLogs) {
                String key = mediationSnapshotWrapper.getStatTypePrefix()
                             + mediationSnapshotWrapper.getDirection() + "ErrorCount-Category-"
                             + errorLog.getErrorCode() + "-ResourceID-" + mediationSnapshotWrapper.getResId();

                Integer count = (Integer) errorMap.get(errorLog.getErrorCode());
                if (count == null) {
                    errorMap.put(key, 1);
                } else {
                    errorMap.put(key, count + 1);
                }
            }
        }

        return errorMap;
    }

    /**
     * Get and put the error logs which are retrived from synapse
     *
     * @param mediationStatisticsSnapshot
     * @param errorMap
     */
    public static void addErrorCategories(MediationStatisticsSnapshot mediationStatisticsSnapshot,
                                          Map<String, Object> errorMap) {
        List<ErrorLog> errorLogs = mediationStatisticsSnapshot.getErrorLogs();

        MediationSnapshotWrapper mediationSnapshotWrapper = new MediationSnapshotWrapper(mediationStatisticsSnapshot);

        if (errorLogs != null) {
            for (ErrorLog errorLog : errorLogs) {

                // Add errorID
                String idKey = mediationSnapshotWrapper.getStatTypePrefix() + mediationSnapshotWrapper.getDirection()
                               + mediationSnapshotWrapper.getResId();
                errorMap.put(idKey, errorLog.getErrorCode());
            }
        }
    }

    public static String updateServerName(AxisConfiguration axisConfiguration)
            throws MediationPublisherException {

        // Used in integration tests
        if (axisConfiguration == null) {
            return serverName;
        }

        String serverName = null;
        String hostName;

        try {
            hostName = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new MediationPublisherException("Error getting host name for the BAM event payload", e);
        }

        ConfigurationContextService confContextService = PublisherUtils.getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(confContextService.getServerConfigContext(), "https");
        String baseServerUrl = TRANSPORT + "://" + hostName + ":" + port;
        ConfigurationContext configurationContext = confContextService.getServerConfigContext();
        String context = configurationContext.getContextRoot();
        PrivilegedCarbonContext tenantCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = null;
        if (tenantCarbonContext != null) {
            tenantDomain = tenantCarbonContext.getTenantDomain();
        }

        if (tenantDomain != null) {
            serverName = baseServerUrl + context + "t/" + tenantDomain;
        } else if (tenantDomain == null && context.equals("/")) {
            serverName = baseServerUrl + "";
        } else if (tenantDomain == null && !context.equals("/")) {
            serverName = baseServerUrl + context;
        }

        return serverName;
    }

    public static void setServerName(String serverName) {
        PublisherUtils.serverName = serverName;
    }

    public static String getServerName() {
        return serverName;
    }

    private static class MediationSnapshotWrapper {
        private MediationStatisticsSnapshot mediationStatisticsSnapshot;
        private String statTypePrefix = "Any";
        private String direction;
        private String resId;

        public MediationSnapshotWrapper(MediationStatisticsSnapshot mediationStatisticsSnapshot) {
            this.mediationStatisticsSnapshot = mediationStatisticsSnapshot;
            updateFields();
        }

        public String getStatTypePrefix() {
            return statTypePrefix;
        }

        public String getDirection() {
            return direction;
        }

        public String getResId() {
            return resId;
        }

        private MediationSnapshotWrapper updateFields() {
            switch (mediationStatisticsSnapshot.getUpdate().getType()) {
                case PROXYSERVICE:
                    statTypePrefix = "Proxy";
                    break;
                case SEQUENCE:
                    statTypePrefix = "Sequence";
                    break;
                case ENDPOINT:
                    statTypePrefix = "Endpoint";
                    break;
                default:
                    statTypePrefix = "Any";
            }

            direction = mediationStatisticsSnapshot.getUpdate().isInStatistic() ? "In" : "Out";
            resId = mediationStatisticsSnapshot.getUpdate().getResourceId();
            return this;
        }

        public static ConfigurationContext getConfigurationContext() {
            return axisConfigurationContext;
        }

        public static void setConfigurationContext(ConfigurationContext axisConfigurationContext) {
            PublisherUtils.axisConfigurationContext = axisConfigurationContext;
        }
    }
}

