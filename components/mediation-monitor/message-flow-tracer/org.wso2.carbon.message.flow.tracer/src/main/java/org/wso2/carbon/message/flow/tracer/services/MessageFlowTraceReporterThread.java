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
package org.wso2.carbon.message.flow.tracer.services;

import org.apache.log4j.Logger;
import org.apache.synapse.messageflowtracer.data.MessageFlowDataEntry;
import org.apache.synapse.messageflowtracer.processors.MessageDataCollector;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.message.flow.tracer.datastore.MessageFlowTraceObserverStore;

public class MessageFlowTraceReporterThread extends Thread {
    private static Logger log = Logger.getLogger(MessageFlowTraceReporterThread.class);


    private boolean shutdownRequested = false;
    private boolean tracingEnabled = false;

    private MessageFlowTraceObserverStore messageFlowTraceObserverStore;

    /** The reference to the synapse environment service */
    private SynapseEnvironmentService synapseEnvironmentService;

    private long delay = 5 * 1000;


    public MessageFlowTraceReporterThread(SynapseEnvironmentService synEnvSvc,
                                          MessageFlowTraceObserverStore messageFlowTraceObserverStore) {
        this.synapseEnvironmentService = synEnvSvc;
        this.messageFlowTraceObserverStore = messageFlowTraceObserverStore;
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
        while (!shutdownRequested) {
            try {
                collectDataAndReport();
                delay();
            } catch (Throwable t) {
                log.error("Error while collecting and reporting mediation statistics", t);
            }
        }
    }

    private void collectDataAndReport(){
        if (log.isDebugEnabled()) {
            log.trace("Starting new mediation statistics collection cycle");
        }

        MessageDataCollector tracingStatisticsCollector =
                synapseEnvironmentService.getSynapseEnvironment().getMessageDataCollector();

        if (tracingStatisticsCollector == null) {
            if (log.isDebugEnabled()) {
                log.debug("Statistics collector is not available in the Synapse environment");
            }
            delay();
            return;
        }


        try {
            while (!tracingStatisticsCollector.isEmpty()){

                MessageFlowDataEntry dataEntry = tracingStatisticsCollector.deQueue();
                messageFlowTraceObserverStore.notifyObservers(dataEntry);
            }

        } catch (Exception e) {
            log.error("Error while obtaining tracing data.", e);
        }
    }

    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Statistics reporter thread is being stopped");
        }
        shutdownRequested = true;
    }
}
