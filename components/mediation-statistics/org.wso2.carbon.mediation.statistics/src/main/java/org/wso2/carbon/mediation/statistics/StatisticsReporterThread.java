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

package org.wso2.carbon.mediation.statistics;

import org.apache.log4j.Logger;
import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.statistics.StatisticsCollector;
import org.apache.synapse.aspects.statistics.StatisticsRecord;
import org.apache.synapse.aspects.statistics.StatisticsLog;
import org.apache.synapse.aspects.statistics.view.InOutStatisticsView;
import org.apache.synapse.aspects.statistics.view.StatisticsViewStrategy;
import org.apache.synapse.aspects.statistics.view.SystemViewStrategy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.*;

/**
 * A thread that collects and processes statistics collected in-memory by
 * Synapse. Ideally this should be the only consumer to directly access Synapse
 * statistics. Any other potential consumers should use the MediationStatisticsObserver
 * API to obtain statistics from the MediationStatisticsStore.
 */
public class StatisticsReporterThread extends Thread {

    private static Logger log = Logger.getLogger(StatisticsReporterThread.class);

    private boolean shutdownRequested = false;

    private boolean tracingEnabled = false;

    private MediationStatisticsStore mediationStatisticsStore;

    /** The reference to the synapse environment service */
    private SynapseEnvironmentService synapseEnvironmentService;

    private long delay = 5 * 1000;

    private final StatisticsViewStrategy systemViewStrategy = new SystemViewStrategy();
    
	/**
	 * This flag will be updated according to the carbon.xml defined value, if
	 * setup as'true' the statistic collector will be disabled.
	 */
     private boolean statisticsReporterDisabled =false;

    public StatisticsReporterThread(SynapseEnvironmentService synEnvSvc,
                                    MediationStatisticsStore mediationStatStore) {
        this.synapseEnvironmentService = synEnvSvc;
        this.mediationStatisticsStore = mediationStatStore;
     }

    public void setDelay(long delay) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics reporter delay set to " + delay + " ms");
        }
        this.delay = delay;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    private void reportStatistics(Map<String, Map<String, InOutStatisticsView>> statsMap) {
        for (Map<String, InOutStatisticsView> viewMap : statsMap.values()) {
            for (InOutStatisticsView view : viewMap.values()) {
                if (view != null) {
                    mediationStatisticsStore.updateStatistics(view);
                }
            }
        }
    }

    private void delay() {
        if (delay <= 0) {
            return;
        }

        try {
            sleep(delay);
        } catch (InterruptedException ignore) {

        }
    }

    public void run() {
        while (!shutdownRequested) {
            try {
                collectDataAndReport();
            } catch (Throwable t) {
                // catch all possible errors to prevent the thread from dying
                log.error("Error while collecting and reporting mediation statistics", t);
            }
        }
    }

    private void collectDataAndReport() {
        if (log.isDebugEnabled()) {
            log.trace("Starting new mediation statistics collection cycle");
        }

        StatisticsCollector statisticsCollector =
                synapseEnvironmentService.getSynapseEnvironment().getStatisticsCollector();

        if (statisticsCollector == null) {
            if (log.isDebugEnabled()) {
                log.debug("Statistics collector is not available in the Synapse environment");
            }
            delay();
            return;
        }

        List<StatisticsRecord> records =
                statisticsCollector.getAndClearStatisticsRecords();
        if (records == null || records.size() == 0) {
            // If no records are collected take a nap and try again later
            delay();
            return;
        }

        if (tracingEnabled) {
            List<MessageTraceLog> traceLogs = getTraceLogs(records);
            if (traceLogs != null) {
                mediationStatisticsStore.notifyTraceLogs(traceLogs.toArray(
                        new MessageTraceLog[traceLogs.size()]));
            }
        }

        // report sequence statistics
        reportStatistics(systemViewStrategy.determineView(records, ComponentType.SEQUENCE));

        // report endpoint statistics to database
        reportStatistics(systemViewStrategy.determineView(records, ComponentType.ENDPOINT));

        // report proxy service statistics to database
        reportStatistics(systemViewStrategy.determineView(records, ComponentType.PROXYSERVICE));
    }

    private List<MessageTraceLog> getTraceLogs(List<StatisticsRecord> records) {
        List<MessageTraceLog> traceLogs = new ArrayList<MessageTraceLog>();
        for (StatisticsRecord record : records) {
            if (record == null) {
                continue;
            }

            MessageTraceLog traceLog = new MessageTraceLog(record.getId());

            List<StatisticsLog> logs = record.getAllStatisticsLogs();
            StatisticsLog startLog = null;
            StatisticsLog endLog = null;
            for (StatisticsLog log : logs) {
                if (log == null) {
                    continue;
                }

                if (startLog == null && log.getComponentType() != ComponentType.ANY) {
                    startLog = log;
                } else if (startLog != null) {
                    endLog = log;
                    break;
                }
            }

            if (startLog == null || endLog == null) {
                continue;
            }

            traceLog.setType(startLog.getComponentType());
            traceLog.setResourceId(startLog.getId());

            switch (startLog.getComponentType()) {
                case PROXYSERVICE:
                    traceLog.setRequestFaultStatus(startLog.isFault() ?
                            MessageTraceLog.FAULT_STATUS_TRUE : MessageTraceLog.FAULT_STATUS_FALSE);
                    if (!endLog.isEndAnyLog()) {
                        StatisticsLog lastLog = logs.get(logs.size() - 1);
                        traceLog.setResponseFaultStatus(lastLog.isFault() ?
                                MessageTraceLog.FAULT_STATUS_TRUE : MessageTraceLog.FAULT_STATUS_FALSE);
                    }
                    break;

                case SEQUENCE:
                    if (endLog.isResponse()) {
                        traceLog.setResponseFaultStatus(endLog.isFault() ?
                                MessageTraceLog.FAULT_STATUS_TRUE : MessageTraceLog.FAULT_STATUS_FALSE);
                        if (!startLog.isResponse()) {
                            traceLog.setRequestFaultStatus(startLog.isFault() ?
                                    MessageTraceLog.FAULT_STATUS_TRUE : MessageTraceLog.FAULT_STATUS_FALSE);
                        }
                    } else {
                        traceLog.setRequestFaultStatus(endLog.isFault() ?
                                MessageTraceLog.FAULT_STATUS_TRUE : MessageTraceLog.FAULT_STATUS_FALSE);
                    }
                    break;

                case ENDPOINT:
                    traceLog.setRequestFaultStatus(endLog.isFault() ?
                            MessageTraceLog.FAULT_STATUS_TRUE : MessageTraceLog.FAULT_STATUS_FALSE);
                    break;
            }

            traceLogs.add(traceLog);
        }

        if (traceLogs.size() > 0) {
            return traceLogs;
        }
        return null;
    }

    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Statistics reporter thread is being stopped");
        }
        shutdownRequested = true;
    }
}
