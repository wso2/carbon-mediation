/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.rabbitmq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.OneTimeTriggerInboundTask;

/**
 * The task implementation to trigger the RabbitMQ consumer.
 */
public class RabbitMQTask extends OneTimeTriggerInboundTask {

    private static final Log log = LogFactory.getLog(RabbitMQTask.class.getName());

    private RabbitMQConsumer rabbitMQConsumer;

    public RabbitMQTask(RabbitMQConsumer rabbitMQConsumer) {
        log.debug("RabbitMQ Task initialize.");
        this.rabbitMQConsumer = rabbitMQConsumer;
    }

    protected void taskExecute() {
        log.debug("Executing RabbitMQ Task Execution.");
        rabbitMQConsumer.execute();
    }

    public void init(SynapseEnvironment synapseEnvironment) {
        log.debug("Initializing.");
    }

    public void destroy() {
        log.debug("Destroying.");
    }
}
