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

import org.apache.log4j.Logger;
import org.apache.synapse.aspects.flow.statistics.publishing.PublishingFlow;
import org.apache.synapse.aspects.flow.statistics.store.CompletedStatisticStore;
import org.wso2.carbon.das.messageflow.data.publisher.data.MessageFlowObserverStore;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

import java.util.List;
import java.util.Queue;

public class MessageFlowReporterThread extends Thread {
    private static Logger log = Logger.getLogger(MessageFlowReporterThread.class);


    private boolean shutdownRequested = false;

    private MessageFlowObserverStore messageFlowObserverStore;

    /** The reference to the synapse environment service */
    private SynapseEnvironmentService synapseEnvironmentService;

	/**
	 * Reference to completed statistic entries in synapse side.
	 */
	Queue<PublishingFlow> completedStatisticEntries;

	private long delay = 1000;

	public MessageFlowReporterThread(SynapseEnvironmentService synEnvSvc,
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

	public void init() {
		if (synapseEnvironmentService.getSynapseEnvironment().getCompletedStatisticStore() != null) {
			completedStatisticEntries = synapseEnvironmentService.getSynapseEnvironment().getCompletedStatisticStore()
			                                                     .getCompletedStatisticEntries();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Statistics collector is not available in the Synapse environment");
			}
		}
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
                if(log.isDebugEnabled()) {
                    log.debug("Executing reporter thread - " + this.getName());
                }
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
	    if (completedStatisticEntries == null) {
		    if (log.isDebugEnabled()) {
			    log.debug("No completed statistics entries were found. Initializing the thread again.");
		    }
		    init();
		    return;
	    }
	    try {
		    int entriesToRead = completedStatisticEntries.size();
		    for (int i = 0; i < entriesToRead; i++) {
			    messageFlowObserverStore.notifyObservers(completedStatisticEntries.poll());
		    }
	    } catch (Exception e) {
		    log.error("Error while obtaining statistic data.", e);
	    }
    }

    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Statistics reporter thread is being stopped");
        }
        shutdownRequested = true;
    }
}
