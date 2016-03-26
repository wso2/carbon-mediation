/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.common;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.Task;
import org.apache.synapse.task.TaskDescription;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.osgi.service.ServiceReferenceHolder;
import org.wso2.carbon.inbound.endpoint.persistence.InboundEndpointsDataStore;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * 
 * This class provides the common implementation for polling protocol processors
 * 
 */
public abstract class InboundRequestProcessorImpl implements InboundRequestProcessor {

    protected StartUpController startUpController;
    protected SynapseEnvironment synapseEnvironment;
    protected long interval;
    protected String name;
    protected boolean coordination;

    private InboundRunner inboundRunner;
    private Thread runningThread;
    private static final Log log = LogFactory.getLog(InboundRequestProcessorImpl.class);
    private InboundEndpointsDataStore dataStore;
    
    protected final static String COMMON_ENDPOINT_POSTFIX = "--SYNAPSE_INBOUND_ENDPOINT";
    
    public InboundRequestProcessorImpl(){
   	 dataStore = InboundEndpointsDataStore.getInstance();
    }
    
    /**
     * 
     * Based on the coordination option schedule the task with NTASK or run as a
     * background thread
     * 
     * @param task
     * @param endpointPostfix
     */
    protected void start(InboundTask task, String endpointPostfix) {
        log.info("Starting the inbound endpoint " + name + ", with coordination " + coordination
                + ". Interval : " + interval + ". Type : " + endpointPostfix);
        if (coordination) {
            try {
                TaskDescription taskDescription = new TaskDescription();
                taskDescription.setName(name + "-" + endpointPostfix);
                taskDescription.setTaskGroup(endpointPostfix);
                if (interval < InboundTask.TASK_THRESHOLD_INTERVAL) {
                    taskDescription.setInterval(InboundTask.TASK_THRESHOLD_INTERVAL);
                } else {
                    taskDescription.setInterval(interval);
                }
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
			   	 if(!dataStore.isPollingEndpointRegistered(tenantDomain, name)){
			   		 dataStore.registerPollingingEndpoint(tenantDomain, name);
			   	 }
			   }

            // When coordination is false, we sometimes require pinned server concept ex: when clustering
            // is not available.
            if (isPinnedServerEnabled(task.getInboundProperties())) {
                if (isPinnedServer(task.getInboundProperties())) {
                    startInboundRunnerThread(task, tenantDomain, true);
                } else {
                    log.info("Inbound Endpoint " + name +
                            " not started as it is not pinned to this server");
                }
            } else {
                startInboundRunnerThread(task, tenantDomain, false);
            }
        }
    }

    private void startInboundRunnerThread(InboundTask task, String tenantDomain, boolean mgrOverride) {
        inboundRunner = new InboundRunner(task, interval, tenantDomain, mgrOverride);
        runningThread = new Thread(inboundRunner);
        runningThread.start();
    }

    /**
     * Stop the inbound polling processor This will be called when inbound is
     * undeployed/redeployed or when server stop
     */
    public void destroy() {
        log.info("Inbound endpoint " + name + " stopping.");
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        if(tenantId != MultitenantConstants.SUPER_TENANT_ID){                      
            dataStore.unregisterPollingEndpoint(carbonContext.getTenantDomain(), name);
        }         
        if (startUpController != null) {
            startUpController.destroy();
        } else if (runningThread != null) {
            inboundRunner.terminate();
            try {
                runningThread.join();
            } catch (InterruptedException e) {
                log.error("Error while stopping the inbound thread.");
            }
        }
    }

    protected static boolean isPinnedServerEnabled(Properties inboundProperties) {
        if (inboundProperties != null &&
                inboundProperties.getProperty(PollingConstants.INBOUND_PINNED_SERVER) != null) {
            return true;
        }

        return false;
    }

    protected boolean isPinnedServer(Properties inboundProperties) {
        String thisServerName = System.getProperty("pinServerName", null);
        if (thisServerName == null || "".equals(thisServerName)) {
            thisServerName = getAxis2ParameterValue(ServiceReferenceHolder.getInstance()
                            .getConfigurationContextService().getServerConfigContext().getAxisConfiguration(),
                    SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME);
            if (thisServerName == null || "".equals(thisServerName)) {
                thisServerName = System.getProperty("SynapseServerName", null);
                if (thisServerName == null || "".equals(thisServerName)) {
                    thisServerName = getServerHost();
                    if (thisServerName == null || "".equals(thisServerName)) {
                        thisServerName = "localhost";
                    }
                }
            }
        }

        String pinnedServersValue = inboundProperties.getProperty(
                PollingConstants.INBOUND_PINNED_SERVER, null);

        List<String> pinnedServers = getPinnedServers(pinnedServersValue);
        if (pinnedServers != null && !pinnedServers.isEmpty()) {
            if (pinnedServers.contains(thisServerName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper method to get a value of a parameters in the AxisConfiguration
     *
     * @param axisConfiguration AxisConfiguration instance
     * @param paramKey The name / key of the parameter
     * @return The value of the parameter
     */
    private static String getAxis2ParameterValue(AxisConfiguration axisConfiguration,
                                                 String paramKey) {
        Parameter parameter = axisConfiguration.getParameter(paramKey);
        if (parameter == null) {
            return null;
        }
        Object value = parameter.getValue();
        if (value != null && value instanceof String) {
            return (String) parameter.getValue();
        } else {
            return null;
        }
    }

    private List<String> getPinnedServers(String pinnedServersValue) {
        StringTokenizer st = new StringTokenizer(pinnedServersValue, " ,");
        List<String> pinnedServersList = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() != 0) {
                pinnedServersList.add(token);
            }
        }
        return pinnedServersList;
    }

    private String getServerHost() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            if (addr != null) {
                return addr.getHostName();
            }
        } catch (UnknownHostException e) {
            log.warn("Unable to get the hostName or IP address of the server", e);
        }

        return null;
    }
}
