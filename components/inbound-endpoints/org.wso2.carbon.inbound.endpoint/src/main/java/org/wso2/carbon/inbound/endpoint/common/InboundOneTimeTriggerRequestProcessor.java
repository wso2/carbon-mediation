/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.TaskDescription;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointsDataStore;

/**
 * This class provides the common implementation for one time trigger protocol processors
 * Implemented the support if message injection happens in a separate thread. ( using Callbacks )
 * One such requirement is loading the tenant when message is injected if at that moment tenant
 * is unloaded.
 */
public abstract class InboundOneTimeTriggerRequestProcessor implements InboundRequestProcessor {

    protected StartUpController startUpController;
    protected SynapseEnvironment synapseEnvironment;
    protected String name;
    protected boolean coordination;

    private OneTimeTriggerInboundRunner inboundRunner;
    private Thread runningThread;
    private static final Log log = LogFactory.getLog(InboundOneTimeTriggerRequestProcessor.class);
    private InboundEndpointsDataStore dataStore;

    protected final static String COMMON_ENDPOINT_POSTFIX = "--SYNAPSE_INBOUND_ENDPOINT";
    public static final int TASK_THRESHOLD_INTERVAL = 1000;

    public InboundOneTimeTriggerRequestProcessor() {
        dataStore = InboundEndpointsDataStore.getInstance();
    }

    /**
     * Based on the coordination option schedule the task with NTASK or run as a
     * background thread
     *
     * @param task
     * @param endpointPostfix
     */
    protected void start(OneTimeTriggerInboundTask task, String endpointPostfix) {
        log.info("Starting the inbound endpoint " + name + ", with coordination " + coordination
                + ". Type : " + endpointPostfix);
        if (coordination) {
            try {
                TaskDescription taskDescription = new TaskDescription();
                taskDescription.setName(name + "-" + endpointPostfix);
                taskDescription.setTaskGroup(endpointPostfix);
                taskDescription.setInterval(TASK_THRESHOLD_INTERVAL);
                taskDescription.setIntervalInMs(true);
                taskDescription.addResource(TaskDescription.INSTANCE, task);
                taskDescription.addResource(TaskDescription.CLASSNAME, task.getClass().getName());
                startUpController = new StartUpController();
                startUpController.setTaskDescription(taskDescription);
                startUpController.init(synapseEnvironment);
            } catch (Exception e) {
                log.error("Error starting the inbound endpoint " + name
                        + ". Unable to schedule the task. " + e.getLocalizedMessage(), e);
            }
        } else {
            PrivilegedCarbonContext carbonContext =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext();
            int tenantId = carbonContext.getTenantId();
            String tenantDomain = null;
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                tenantDomain = carbonContext.getTenantDomain();
                if (!dataStore.isPollingEndpointRegistered(tenantDomain, name)) {
                    dataStore.registerPollingingEndpoint(tenantDomain, name);
                }
            }
            inboundRunner = new OneTimeTriggerInboundRunner(task, tenantDomain);
            if(task.getCallback() != null){
                task.getCallback().setInboundRunnerMode(true);
            }
            if (task.getCallback() != null) {
                //this logic introduced if message injection happens in different thread than this
                //where we do not have access to the carbon context, this is the case for all
                //inbound endpoints where message injection happens in a different thread
                // ( callbacks ) but this is not the case for polling based inbound endpoints
                //later this tenantDomain is used for tenant loading
                task.getCallback().setTenantDomain(tenantDomain);
            }
            runningThread = new Thread(inboundRunner);
            runningThread.start();
        }
    }

    /**
     * Stop the inbound polling processor This will be called when inbound is
     * undeployed/redeployed or when server stop
     */
    public void destroy() {
        log.info("Inbound endpoint " + name + " stopping.");
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            dataStore.unregisterPollingEndpoint(carbonContext.getTenantDomain(), name);
        }
        if (startUpController != null) {
            startUpController.destroy();
        } else if (runningThread != null) {
            try {
                //this is introduced where the the thread is suspended due to external server is not
                //up and running and waiting connection to be completed.
                //thread join waits until that suspension is removed where inbound endpoint
                //is un deployed that will eventually lead to completion of this thread
                runningThread.join();
            } catch (InterruptedException e) {
                log.error("Error while stopping the inbound thread.");
            }
        }
    }
}
