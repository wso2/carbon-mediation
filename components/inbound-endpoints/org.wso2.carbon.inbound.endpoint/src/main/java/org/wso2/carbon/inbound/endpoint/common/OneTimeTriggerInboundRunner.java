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
import org.wso2.carbon.mediation.clustering.ClusteringAgentUtil;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * OnetimeTriggerInboundRunner class is used to run the non coordinated processors in
 * background. This Runner is only active for one task execution iteration ( One time trigger ) 
 */
public class OneTimeTriggerInboundRunner implements Runnable {

    private OneTimeTriggerInboundTask task;
    private volatile boolean execute = true;
    private volatile boolean init = false;
    private String tenantDomain;
    private static final long CLUSTER_CONFIGURATION_CHECK_INTERVAL = 1000;

    private static final Log log = LogFactory.getLog(OneTimeTriggerInboundRunner.class);

    public OneTimeTriggerInboundRunner(OneTimeTriggerInboundTask task, String tenantDomain) {
        this.task = task;
        this.tenantDomain = tenantDomain;
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Starting the Inbound Endpoint.");
        }
        // Wait for the clustering configuration to be loaded.
        while (!init) {
            if (log.isDebugEnabled()) {
                log.debug("Waiting for the configuration context to be loaded to run Inbound Endpoint.");
            }
            Boolean isSinglNode = ClusteringAgentUtil.isSingleNode();
            if (isSinglNode != null) {
                if (!isSinglNode && !CarbonUtils.isWorkerNode()) {
                    // Given node is the manager in the cluster, and not
                    // required to run the service
                    execute = false;
                    log.info("Inbound Endpoint will not run in manager node. Same will run on worker(s).");
                }
                init = true;
            }
            try {
                Thread.sleep(CLUSTER_CONFIGURATION_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to sleep the Inbound thread for interval of : " +
                            CLUSTER_CONFIGURATION_CHECK_INTERVAL + "ms.");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Configuration context loaded. Ready to Run the Inbound Endpoint.");
        }
        if (log.isDebugEnabled()) {
            log.debug("Executing One-Time trigger connection consumer task in Inbound Endpoint.");
        }

        try {
            if (execute) {
                task.taskExecute();
            }
        } catch (Exception e) {
            log.error("Error executing the Inbound Endpoint once time execution", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exiting the Inbound Endpoint.");
        }
    }
}
