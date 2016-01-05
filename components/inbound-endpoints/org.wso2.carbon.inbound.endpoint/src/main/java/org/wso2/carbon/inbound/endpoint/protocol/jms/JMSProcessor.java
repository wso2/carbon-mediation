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
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;
import org.wso2.carbon.inbound.endpoint.protocol.jms.factory.CachedJMSConnectionFactory;

import static org.wso2.carbon.inbound.endpoint.common.InboundTask.TASK_THRESHOLD_INTERVAL;

public class JMSProcessor extends InboundRequestProcessorImpl implements TaskStartupObserver {

    private static final Log log = LogFactory.getLog(JMSProcessor.class.getName());

    private static final String ENDPOINT_POSTFIX = "JMS" + COMMON_ENDPOINT_POSTFIX;

    private JMSPollingConsumer pollingConsumer;
    private Properties jmsProperties;
    private boolean sequential;
    private String injectingSeq;
    private String onErrorSeq;

    public JMSProcessor(InboundProcessorParams params) {
        this.name = params.getName();
        this.jmsProperties = params.getProperties();

        String inboundEndpointInterval = jmsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL);
        if(inboundEndpointInterval!=null){
            try {
                this.interval = Long.parseLong(inboundEndpointInterval);
            } catch (NumberFormatException nfe) {
                throw new SynapseException("Invalid numeric value for interval.", nfe);
            } catch (Exception e) {
                throw new SynapseException("Invalid value for interval.", e);
            }
        } else{
            this.cron = jmsProperties.getProperty(PollingConstants.INBOUND_CRON);;
        }
        this.sequential = true;
        String inboundEndpointSequential = jmsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL);
        if (inboundEndpointSequential != null) {
            this.sequential = Boolean.parseBoolean(inboundEndpointSequential);
        }
        this.coordination = true;
        String inboundCoordination = jmsProperties.getProperty(PollingConstants.INBOUND_COORDINATION);
        if (inboundCoordination != null) {
            this.coordination = Boolean.parseBoolean(inboundCoordination);
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
        pollingConsumer = new JMSPollingConsumer( jmsProperties, interval, name);
        pollingConsumer.registerHandler(new JMSInjectHandler(injectingSeq, onErrorSeq, sequential,
                synapseEnvironment, jmsProperties));
        start();
    }
    /**
     * Stop the inbound polling processor This will be called when inbound is
     * undeployed/redeployed or when server stop
     */
    public void destroy() {
        pollingConsumer.destroy();
        super.destroy();
    }
    /**
     * Register/start the schedule service
     * */
    public void start() {
        InboundTask task = new JMSTask(pollingConsumer, interval, cron);
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
