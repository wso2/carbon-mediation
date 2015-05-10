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

import kafka.consumer.*;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.apache.synapse.SynapseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KAFKAMessageListener extends AbstractKafkaMessageListener {

    public KAFKAMessageListener(int threadCount, List<String> topics,
                                Properties kafkaProperties, InjectHandler injectHandler)
            throws Exception {
        this.threadCount = threadCount;
        this.topics = topics;
        this.kafkaProperties = kafkaProperties;
        this.injectHandler = injectHandler;
    }

    /**
     * Create the connection with the zookeeper to consume the messages
     */
    public boolean createKafkaConsumerConnector() throws Exception {

        logger.debug("Create the connection and start to consume the streams");
        boolean isCreated;
        try {
            if (consumerConnector == null) {
                logger.info("Creating Kafka Consumer Connector...");
                consumerConnector = Consumer
                        .createJavaConsumerConnector(new ConsumerConfig(
                                kafkaProperties));
                logger.info("Kafka Consumer Connector is created");
                start();

            }
            isCreated = true;
        } catch (ZkTimeoutException toe) {
            logger.error(" Error in Creating Kafka Consumer Connector | ZkTimeout"
                    + toe.getMessage());
            throw new SynapseException(
                    " Error in Creating Kafka Consumer Connector| ZkTimeout",
                    toe);

        } catch (Exception e) {
            logger.error(" Error in Creating Kafka Consumer Connector "

                    + e.getMessage());
            throw new SynapseException(" Error in Creating Kafka Consumer Connector ",
                    e);
        }
        return isCreated;
    }

    /**
     * Starts topics consuming the messages,the message can be consumed by topic or topic filter which are white list and black list.
     */
    public void start() throws Exception {

        logger.debug("Start to consume the streams");
        try {
            logger.info("Starting KAFKA consumer listener...");
            Map<String, Integer> topicCount = new HashMap<String, Integer>();

            if (topics != null && topics.size() > 0) {
                // Define threadCount thread/s for topic
                for (String topic : topics) {
                    topicCount.put(topic, threadCount);
                }
                Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumerConnector
                        .createMessageStreams(topicCount);
                for (String topic : topics) {
                    List<KafkaStream<byte[], byte[]>> streams = consumerStreams
                            .get(topic);
                    startConsumers(streams);

                }
            } else if (kafkaProperties.getProperty(KAFKAConstants.TOPIC_FILTER) != null) {
                // Define #threadCount thread/s for topic filter
                List<KafkaStream<byte[], byte[]>> consumerStreams;
                boolean isFromWhiteList = (kafkaProperties
                        .getProperty(KAFKAConstants.FILTER_FROM_WHITE_LIST) == null || kafkaProperties
                        .getProperty(KAFKAConstants.FILTER_FROM_WHITE_LIST)
                        .isEmpty()) ? Boolean.TRUE
                        : Boolean
                        .parseBoolean(kafkaProperties
                                .getProperty(KAFKAConstants.FILTER_FROM_WHITE_LIST));
                if (isFromWhiteList) {
                    consumerStreams = consumerConnector
                            .createMessageStreamsByFilter(
                                    new Whitelist(
                                            kafkaProperties
                                                    .getProperty(KAFKAConstants.TOPIC_FILTER)),
                                    threadCount);
                } else {
                    consumerStreams = consumerConnector
                            .createMessageStreamsByFilter(
                                    new Blacklist(
                                            kafkaProperties
                                                    .getProperty(KAFKAConstants.TOPIC_FILTER)),
                                    threadCount);
                }

                startConsumers(consumerStreams);
            }

        } catch (Exception e) {
            logger.error("Error while Starting KAFKA consumer listener "
                    + e.getMessage());
            throw new SynapseException(
                    "Error while Starting KAFKA consumer listener ", e);
        }
    }

    /**
     * Use one stream from kafka stream iterator
     * @param streams
     */
    protected void startConsumers(List<KafkaStream<byte[], byte[]>> streams) {
        for (KafkaStream<byte[], byte[]> stream : streams) {
            consumerIte = stream.iterator();
            break;
        }
    }

    @Override
    public void injectMessageToESB() {
        byte[] msg = consumerIte.next().message();
        injectHandler.invoke(msg);
    }

    @Override
    public boolean hasNext() {
        return consumerIte.hasNext();
    }
}
