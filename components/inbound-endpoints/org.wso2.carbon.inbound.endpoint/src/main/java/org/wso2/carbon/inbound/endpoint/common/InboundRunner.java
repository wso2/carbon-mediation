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

import java.util.Date;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.Task;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.inbound.endpoint.persistence.service.InboundEndpointPersistenceServiceDSComponent;
import org.wso2.carbon.mediation.clustering.ClusteringAgentUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * 
 * InboundRunner class is used to run the non coordinated processors in
 * background according to the scheduled interval
 * 
 */
public class InboundRunner implements Runnable {

    private InboundTask task;
    private long interval;

    private volatile boolean execute = true;
    private volatile boolean init = false;
    // Following will be used to calculate the sleeping interval
    private long lastRuntime;
    private long currentRuntime;
    private long cycleInterval;
    private String tenantDomain;

    private static final Log log = LogFactory.getLog(InboundRunner.class);

    public InboundRunner(InboundTask task, long interval, String tenantDomain) {
        this.task = task;
        this.interval = interval;
        this.tenantDomain = tenantDomain;
    }

    /**
     * Exit the running while loop and terminate the thread
     */
    protected void terminate() {
        execute = false;
    }

    @Override
    public void run() {
        log.debug("Starting the Inbound Endpoint.");
        // Wait for the clustering configuration to be loaded.
        while (!init) {
            log.debug("Waiting for the configuration context to be loaded to run Inbound Endpoint.");
            Boolean isSinglNode = ClusteringAgentUtil.isSingleNode();
            if (isSinglNode != null) {
                if (!isSinglNode && !CarbonUtils.isWorkerNode()) {
                    // Given node is the manager in the cluster, and not
                    // required to run the service
                    execute = false;
                    log.info("Inbound EP will not run in manager node. Same will run on worker(s).");
                }
                init = true;
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.warn(
                        "Unable to sleep the inbound thread for interval of : " + interval + "ms.",
                        e);
            }
        }

        log.debug("Configuration context loaded. Running the Inbound Endpoint.");
        // Run the poll cycles
        while (execute) {
            log.debug("Executing the Inbound Endpoint.");
            lastRuntime = getTime();
            try {
                task.taskExecute();
            } catch (Exception e) {
                log.error("Error executing the inbound endpoint polling cycle.", e);
            }
            currentRuntime = getTime();
            cycleInterval = interval - (currentRuntime - lastRuntime);
            if (cycleInterval > 0) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    log.warn("Unable to sleep the inbound thread for interval of : " + interval
                            + "ms.", e);
                }
            }
            //Keep the tenant loaded
    		   if (tenantDomain != null) {
    		        ConfigurationContextService configurationContext = InboundEndpointPersistenceServiceDSComponent.getConfigContextService();
    		        ConfigurationContext mainConfigCtx = configurationContext.getServerConfigContext();
    		        TenantAxisUtils.getTenantConfigurationContext(tenantDomain, mainConfigCtx);
    		   }           
        }
        log.debug("Exit the Inbound Endpoint running loop.");
    }

    private Long getTime() {
        return new Date().getTime();
    }
}
