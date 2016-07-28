/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.das.messageflow.data.publisher.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.synapse.aspects.flow.statistics.data.raw.BasicStatisticDataUnit;
import org.apache.synapse.aspects.flow.statistics.data.raw.CallbackDataUnit;
import org.apache.synapse.aspects.flow.statistics.data.raw.StatisticDataUnit;
import org.apache.synapse.aspects.flow.statistics.data.raw.StatisticsLog;
import org.apache.synapse.aspects.flow.statistics.log.MessageFlowProcessorInterface;
import org.apache.synapse.aspects.flow.statistics.log.StatisticsReportingEvent;
import org.apache.synapse.aspects.flow.statistics.log.StatisticsReportingEventHolder;
import org.apache.synapse.aspects.flow.statistics.log.templates.AbstractStatisticEvent;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.apache.synapse.aspects.flow.statistics.store.CompletedStatisticStore;
import org.apache.synapse.aspects.flow.statistics.util.TracingDataCollectionHelper;
import org.wso2.carbon.das.messageflow.data.publisher.data.MessageFlowObserverStore;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageFlowReporterThread2 extends Thread {
    private static Logger log = Logger.getLogger(MessageFlowReporterThread2.class);


    private volatile boolean shutdownRequested = false;

    private MessageFlowObserverStore messageFlowObserverStore;

    /**
     * The reference to the synapse environment service
     */
    private SynapseEnvironmentService synapseEnvironmentService;

    private long delay = 5000;


    public MessageFlowReporterThread2(SynapseEnvironmentService synEnvSvc,
                                      MessageFlowObserverStore messageFlowObserverStore) {
        this.synapseEnvironmentService = synEnvSvc;
        this.messageFlowObserverStore = messageFlowObserverStore;
    }

    public void setDelay(long delay) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics reporter delay set to " + delay + " ms");
        }
        this.delay = delay;
    }

    private void delay() {
        if (delay <= 0) {
            return;
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignore) {

        }
    }

    public void run() {
        StatisticsReportingEventHolder statisticsReportingEventHolder;
        while (!shutdownRequested) {
            try {
                statisticsReportingEventHolder = synapseEnvironmentService.getSynapseEnvironment().getMessageDataStore().dequeue();
                if (statisticsReportingEventHolder != null) {

                    log.info("******* event list size - " + statisticsReportingEventHolder.getQueueSize() + " holder " +
                            "count - " + statisticsReportingEventHolder.countHolder.getStatCount() + " callback - " + statisticsReportingEventHolder.countHolder.getCallBackCount());
                    processAndPublishEventList(statisticsReportingEventHolder.getEventList());
                } else {
                    Thread.sleep(1000);
                }
            } catch (Exception exception) {
                log.error("Error in mediation flow statistic data consumer while consuming data", exception);
            }
        }
    }

    private void processAndPublishEventList(List<StatisticsReportingEvent> eventList) {


        List<StatisticsReportingEvent> remainingEvents = new ArrayList<>();
        List<StatisticsLog> messageFlowLogs = new ArrayList<>();

        for (StatisticsReportingEvent event : eventList) {
            if (event.getEventType() == AbstractStatisticEvent.EventType.STATISTICS_OPEN_EVENT) {
                StatisticsLog statisticsLog = new StatisticsLog((StatisticDataUnit) event.getDataUnit());
                messageFlowLogs.add(statisticsLog.getCurrentIndex(), statisticsLog);
            } else {
                remainingEvents.add(event);
            }
        }

        for (StatisticsReportingEvent event : remainingEvents) {

            switch (event.getEventType()) {
                case STATISTICS_CLOSE_EVENT:

                    StatisticDataUnit dataUnit = (StatisticDataUnit) event.getDataUnit();

                    StatisticsLog statisticsLog = messageFlowLogs.get(dataUnit.getCurrentIndex());

                    int parentIndex = statisticsLog.getParentIndex();
                    if (parentIndex == -1 || messageFlowLogs.get(parentIndex).isFlowSplittingMediator()) {
                        statisticsLog.setParentIndex(parentIndex);
                    } else {
                        statisticsLog.setParentIndex(getParent(messageFlowLogs, parentIndex));
                    }

                    if (statisticsLog.getHashCode() == null) {
                        statisticsLog.setHashCode(messageFlowLogs.get(parentIndex).getHashCode());
                    }

                    statisticsLog.setEndTime(dataUnit.getTime());
                    statisticsLog.setAfterPayload(dataUnit.getPayload());
                    updateParents(messageFlowLogs, statisticsLog.getParentIndex(), dataUnit.getTime());
                    break;
                case CALLBACK_COMPLETION_EVENT:
                    CallbackDataUnit callbackDataUnit = (CallbackDataUnit) event.getDataUnit();
                    if (!callbackDataUnit.isOutOnlyFlow()) {
                        updateParents(messageFlowLogs, callbackDataUnit.getCurrentIndex(), callbackDataUnit.getTime());
                    }
                    break;
                case CALLBACK_RECEIVED_EVENT:
                    callbackDataUnit = (CallbackDataUnit) event.getDataUnit();
                    if (!callbackDataUnit.isOutOnlyFlow()) {
                        updateParents(messageFlowLogs, callbackDataUnit.getCurrentIndex(), callbackDataUnit.getTime());
                    }
                    break;
                case ENDFLOW_EVENT:
                    break;
                case FAULT_EVENT:
                    BasicStatisticDataUnit basicDataUnit = event.getDataUnit();
                    addFaultsToParents(messageFlowLogs, basicDataUnit.getCurrentIndex());
                    break;
                case PARENT_REOPEN_EVENT:
                    break;
                default:
                    break;
            }
            //messageFlowLogs.get(0).setEndTime(eventList.get(event.));


        }

        PublishingFlow publishingFlow = TracingDataCollectionHelper.createPublishingFlow(messageFlowLogs);
        logEvent(publishingFlow);
        messageFlowObserverStore.notifyObservers(publishingFlow);


        log.info("came");


        //messageFlowObserverStore.notifyObservers(statisticsEntry.getMessageFlowLogs());
    }

    void updateParents(List<StatisticsLog> messageFlowLogs, int index, long endTime) {
        while (index > -1) {
            StatisticsLog dataUnit = messageFlowLogs.get(index);
            dataUnit.setEndTime(endTime);
            index = dataUnit.getParentIndex();
        }
    }

    private int getParent(List<StatisticsLog> messageFlowLogs, int parentIndex) {
        int trueParentIndex = 0;
        while (parentIndex > -1) {
            StatisticsLog updatingLog = messageFlowLogs.get(parentIndex);
            if (updatingLog.getEndTime() == -1) {
                trueParentIndex = updatingLog.getCurrentIndex();
                break;
            }
            parentIndex = updatingLog.getParentIndex();
        }
        return trueParentIndex;
    }

    void addFaultsToParents(List<StatisticsLog> messageFlowLogs, int index) {
        while (index > -1) {
            StatisticsLog updatingLog = messageFlowLogs.get(index);
            updatingLog.incrementNoOfFaults();
            index = updatingLog.getParentIndex();
        }

    }

    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Statistics reporter thread is being stopped");
        }
        shutdownRequested = true;
    }

    //todo this is only for test, please remove this before committing - rajith
    private static void logEvent(PublishingFlow publishingFlow) {
        Map<String, Object> mapping = publishingFlow.getObjectAsMap();
        mapping.put("host", "localhost"); // Adding host
        mapping.put("tenantId", "1234");

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = mapper.writeValueAsString(mapping);
        } catch (JsonProcessingException e) {
            log.error("Unable to convert", e);
        }
        log.info("Uncompressed data -------------------------- : " + jsonString);
    }
}
