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
package org.wso2.carbon.inbound.endpoint.protocol.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.ntask.core.impl.LocalTaskActionListener;

import java.util.Properties;

/**
 * 
 * JMSTask class is used to schedule the inbound execution when the coordination
 * is required
 * 
 */
public class JMSTask extends InboundTask implements LocalTaskActionListener {
    private static final Log logger = LogFactory.getLog(JMSTask.class.getName());

    private JMSPollingConsumer jmsPollingConsumer;

    public JMSTask(JMSPollingConsumer jmsPollingConsumer, long interval) {
        logger.debug("Initializing JMS Task.");
        this.jmsPollingConsumer = jmsPollingConsumer;
        this.interval = interval;
    }

    protected void taskExecute() {
        logger.debug("Executing JMS Task Execution.");
        jmsPollingConsumer.execute();
    }

    @Override
    public Properties getInboundProperties() {
        return jmsPollingConsumer.getInboundProperites();
    }

    public void init(SynapseEnvironment synapseEnvironment) {
        logger.debug("Initializing.");
    }

    public void destroy() {
        logger.debug("Destroying JMS Task.");
        jmsPollingConsumer.destroy();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Destroys the JMS task upon deletion of the local task.
     *
     * @param taskName the name of the task that was deleted
     */
    @Override
    public void notifyLocalTaskDeletion(String taskName) {
        destroy();
        logger.debug("Destroyed JMS task due to deletion of task: " + taskName);
    }
}
