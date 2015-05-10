/**
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.message.processor;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.impl.ScheduledMessageProcessor;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.message.processor.util.ConfigHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 *
 * Cleans up the resources used by the message processor such as JMS consumers
 * etc especially in a clustered environment.
 *
 */
public class MessageProcessorCleanupTask implements Callable<Void>, Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;

    public MessageProcessorCleanupTask(String name) {
        this.name = name;
    }

    public Void call() throws Exception {
        /*
         * Get the message processor instance running inside this worker node
         * using an OSGI service, synapse-env, synapse-config.
         */
        SynapseEnvironmentService synEnvService =
                                                  ConfigHolder.getInstance()
                                                              .getSynapseEnvironmentService(MultitenantConstants.SUPER_TENANT_ID);
        MessageProcessor messageProcessor =
                                            synEnvService.getSynapseEnvironment()
                                                         .getSynapseConfiguration()
                                                         .getMessageProcessors().get(name);

        /*
         * Get all the message processors run in this VM and filter using the
         * name.
         * Then cleanup the resources used by that message processor.
         */
        if (messageProcessor != null && messageProcessor instanceof ScheduledMessageProcessor) {
            ((ScheduledMessageProcessor) messageProcessor).cleanupLocalResources();
        }

        return null;
    }

}
