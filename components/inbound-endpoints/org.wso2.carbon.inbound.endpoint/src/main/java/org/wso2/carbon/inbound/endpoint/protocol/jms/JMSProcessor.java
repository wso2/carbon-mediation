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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.task.Task;
import org.apache.synapse.task.TaskStartupObserver;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.factory.CachedJMSConnectionFactory;

public class JMSProcessor extends InboundRequestProcessorImpl implements TaskStartupObserver {

    private static final Log log = LogFactory.getLog(JMSProcessor.class.getName());

    private static final String ENDPOINT_POSTFIX = "JMS-EP";

    private CachedJMSConnectionFactory jmsConnectionFactory;
    private JMSPollingConsumer pollingConsumer;
    private Properties jmsProperties;
    private boolean sequential;
    private String injectingSeq;
    private String onErrorSeq;

    public JMSProcessor(InboundProcessorParams params) {
        this.name = params.getName();
        this.jmsProperties = params.getProperties();

        if (jmsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL) != null) {
            try {
                this.interval = Long.parseLong(jmsProperties
                        .getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL));
            } catch (NumberFormatException nfe) {
                throw new SynapseException("Invalid numeric value for interval.", nfe);
            }
        }
        this.sequential = true;
        if (jmsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL) != null) {
            this.sequential = Boolean.parseBoolean(jmsProperties
                    .getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL));
        }
        this.coordination = true;
        if (jmsProperties.getProperty(PollingConstants.INBOUND_COORDINATION) != null) {
            this.coordination = Boolean.parseBoolean(jmsProperties
                    .getProperty(PollingConstants.INBOUND_COORDINATION));
        }
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();

    }

    /**
     * This will be called at the time of synapse artifact deployment.
     */
    public void init() {
        log.info("Initializing inbound JMS listener for inbound endpoint " + name);
        jmsConnectionFactory = new CachedJMSConnectionFactory(this.jmsProperties);
        pollingConsumer = new JMSPollingConsumer(jmsConnectionFactory, jmsProperties, interval);
        pollingConsumer.registerHandler(new JMSInjectHandler(injectingSeq, onErrorSeq, sequential,
                synapseEnvironment, jmsProperties));
        start();
    }

    /**
     * Register/start the schedule service
     * */
    public void start() {
        Task task = new JMSTask(pollingConsumer);
        start(task, ENDPOINT_POSTFIX);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void update() {
        // This will not be called for inbound endpoints
    }
}
