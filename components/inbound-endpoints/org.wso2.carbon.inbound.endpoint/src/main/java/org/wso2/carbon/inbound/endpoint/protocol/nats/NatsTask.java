/*
 * Copyright 2020 The Apache Software Foundation.
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
package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;

import java.util.Properties;

/**
 * NatsTask class is used to schedule tasks for inbound NATS processor when required (coordination==true).
 */
public class NatsTask extends InboundTask {

    private static final Log log = LogFactory.getLog(NatsTask.class.getName());
    private NatsPollingConsumer natsPollingConsumer;

    NatsTask(NatsPollingConsumer natsPollingConsumer, long interval) {
        printDebugLog("Initializing NATS Task.");
        this.natsPollingConsumer = natsPollingConsumer;
        this.interval = interval;
    }

    public void init(SynapseEnvironment se) {
        printDebugLog("Initializing NATS Task.");
    }

    public void destroy() {
        printDebugLog("Destroying NATS Task.");
    }

    public void taskExecute() {
        printDebugLog("Executing NATS Task.");
        natsPollingConsumer.execute();
    }

    @Override
    public Properties getInboundProperties() {
        return natsPollingConsumer.getInboundProperties();
    }

    /**
     * Check if debug is enabled for logging.
     *
     * @param text log text
     */
    private void printDebugLog(String text) {
        if (log.isDebugEnabled()) {
            log.debug(text);
        }
    }
}
