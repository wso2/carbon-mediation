/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.kafka;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class KAFKAPollingConsumer {
    private static final Log logger = LogFactory
            .getLog(KAFKAPollingConsumer.class.getName());

    private InjectHandler injectHandler;
    private Properties kafkaProperties;
    private int threadCount;
    private List<String> topics;
    private AbstractKafkaMessageListener messageListener;
    private long scanInterval;

    /**
     * Initialize the kafka properties and the polling interval
     */
    public KAFKAPollingConsumer(Properties kafkaProperties, long interval) throws Exception {

        this.kafkaProperties = kafkaProperties;
        this.scanInterval = interval;
        if (kafkaProperties.getProperty(KAFKAConstants.THREAD_COUNT) == null
                || kafkaProperties.getProperty(KAFKAConstants.THREAD_COUNT)
                .equals("")
                || Integer.parseInt(kafkaProperties
                .getProperty(KAFKAConstants.THREAD_COUNT)) <= 0) {
            this.threadCount = 1;
        } else {
            this.threadCount = Integer.parseInt(kafkaProperties
                    .getProperty(KAFKAConstants.THREAD_COUNT));
        }
        if (kafkaProperties.getProperty(KAFKAConstants.TOPICS) != null) {
            this.topics = Arrays.asList(kafkaProperties.getProperty(
                    KAFKAConstants.TOPICS).split(","));
        }
    }

    /**
     * Start the listener to listen when new messages come to the esb,the listener can be high level or low level.
     */
    public void startsMessageListener() throws Exception {
        if (messageListener == null) {
            //Start a high level listener
            if (kafkaProperties.getProperty(KAFKAConstants.CONSUMER_TYPE) == null
                    || kafkaProperties
                    .getProperty(KAFKAConstants.CONSUMER_TYPE)
                    .isEmpty()
                    || kafkaProperties
                    .getProperty(KAFKAConstants.CONSUMER_TYPE)
                    .equalsIgnoreCase(
                            AbstractKafkaMessageListener.CONSUMER_TYPE.HIGHLEVEL
                                    .getName())) {
                messageListener = new KAFKAMessageListener(threadCount, topics,
                        kafkaProperties, injectHandler);
                //Start a low level listener
            } else if (kafkaProperties
                    .getProperty(KAFKAConstants.CONSUMER_TYPE)
                    .equalsIgnoreCase(
                            AbstractKafkaMessageListener.CONSUMER_TYPE.SIMPLE
                                    .getName())) {
                messageListener = new SimpleKafkaMessageListener(
                        kafkaProperties, injectHandler);
            }
        }

    }

    /**
     * Polling the messages according to the polling interval
     */
    public void execute() {
        try {
            logger.debug("Executing : KAFKA Inbound EP : ");
            //Poll the messages when the polling interval is less than threshold value 1000ms
            if (this.scanInterval > 0 && this.scanInterval < KAFKAConstants.THRESHOLD_INTERVAL) {
                while (true) {
                    try {
                        Thread.sleep(this.scanInterval);
                    } catch (InterruptedException e) {
                        logger.debug("Current Thread was interrupted while it is sleeping.");
                    }
                    poll();
                }
            }
            //Poll the messages when the polling interval is grater than threshold value 1000ms
            if (this.scanInterval > 0 && this.scanInterval >= KAFKAConstants.THRESHOLD_INTERVAL) {
                poll();
            }

        } catch (Exception e) {
            logger.error("Error while retrieving or injecting Kafka message. " + e.getMessage(), e);
        }
    }

    /**
     * Register a handler to implement injection of the retrieved message
     * @param processingHandler
     */
    public void registerHandler(InjectHandler processingHandler) {
        injectHandler = processingHandler;
    }

    /**
     * Create the connection with the zookeeper and inject the messages to the sequence
     */
    public Object poll() {
        if (logger.isDebugEnabled()) {
            logger.debug("run() - polling messages");
        }
        //Create the connection to the zookeeper
        try {
            if (!messageListener.createKafkaConsumerConnector()) {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        //Inject the messages to the sequence
        try {
            if (injectHandler != null && messageListener.hasNext()) {
                messageListener.injectMessageToESB();
            } else {
                return null;
            }

        } catch (Exception e) {
            logger.error("Error while receiving KAFKA message. "
                    + e.getMessage());
        }
        return null;
    }

    /**
     * Stop to consume the messages
     */
    public void destroy() throws Exception {
        messageListener.destroy();
    }
}
