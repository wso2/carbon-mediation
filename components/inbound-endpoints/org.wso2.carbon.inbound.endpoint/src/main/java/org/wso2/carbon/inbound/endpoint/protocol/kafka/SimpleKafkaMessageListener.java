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

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

import java.nio.ByteBuffer;
import java.util.*;

public class SimpleKafkaMessageListener extends AbstractKafkaMessageListener {
    private List<String> m_replicaBrokers;
    private boolean init;
    private String topic;
    private long maxReads = Long.MAX_VALUE;
    private int partition;
    private List<String> seedBrokers;
    private int port;
    private String leadBroker;
    private String clientName;
    private SimpleConsumer consumer;
    private long readOffset;

    public SimpleKafkaMessageListener(Properties kafkaProperties,
                                      InjectHandler injectHandler) throws Exception {
        this.kafkaProperties = kafkaProperties;
        this.injectHandler = injectHandler;
        m_replicaBrokers = new ArrayList<String>();
        validateInputParameters();
    }

    /**
     * Validate the input parameters for low level consumer
     */
    private void validateInputParameters() throws Exception {
        if (kafkaProperties.getProperty(KAFKAConstants.SIMPLE_TOPIC) == null) {
            throw new Exception("simple consumer topic is invalid");
        } else {
            this.topic = kafkaProperties
                    .getProperty(KAFKAConstants.SIMPLE_TOPIC);
        }
        if (kafkaProperties.getProperty(KAFKAConstants.SIMPLE_BROKERRS) == null) {
            throw new Exception("simple consumer brokers is invalid");
        } else {
            this.seedBrokers = getSeedBrokers(kafkaProperties
                    .getProperty(KAFKAConstants.SIMPLE_BROKERRS));
        }
        if (kafkaProperties.getProperty(KAFKAConstants.SIMPLE_PORT) == null) {
            throw new Exception("simple consumer port is invalid");
        } else {
            this.port = Integer.parseInt(kafkaProperties
                    .getProperty(KAFKAConstants.SIMPLE_PORT));
        }
        if (kafkaProperties.getProperty(KAFKAConstants.SIMPLE_PARTITION) == null) {
            throw new Exception("simple consumer partition is invalid");
        } else {
            this.partition = Integer.parseInt(kafkaProperties
                    .getProperty(KAFKAConstants.SIMPLE_PARTITION));
        }
        if (kafkaProperties.getProperty(KAFKAConstants.SIMPLE_MAX_MSGS_TO_READ) == null) {
            throw new Exception(
                    "simple consumer max.messages.to.read is invalid");
        } else {
            this.maxReads = Long.parseLong(kafkaProperties
                    .getProperty(KAFKAConstants.SIMPLE_MAX_MSGS_TO_READ));
        }

    }

    /**
     * Get the brokers from the broker list parameter
     * @param brokers
     * @return
     */
    private List<String> getSeedBrokers(String brokers) {
        return Arrays.asList(brokers.split(","));
    }

    @Override
    public boolean createKafkaConsumerConnector() throws Exception {
        return run();
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void injectMessageToESB() {
        startDataFetching();
    }

    @Override
    public boolean hasNext() {
        if (maxReads == Long.MAX_VALUE) {
            return true;
        }
        return maxReads > 0;
    }

    public boolean run() throws Exception {

        if (init) {
            return init;
        }
        // find the meta data about the topic and partition we are interested in
        PartitionMetadata metadata = findLeader(seedBrokers, port, topic,
                partition);
        if (metadata == null) {
            throw new Exception(
                    "Can't find metadata for Topic and Partition. Exiting");
        }
        if (metadata.leader() == null) {
            throw new Exception(
                    "Can't find Leader for Topic and Partition. Exiting");
        }
        this.leadBroker = metadata.leader().host();
        this.clientName = "Client_" + topic + "_" + partition;

        this.consumer = new SimpleConsumer(leadBroker, port,
                KAFKAConstants.BUFFER_SIZE, KAFKAConstants.SO_TIMEOUT,
                clientName);
        this.readOffset = getLastOffset(consumer, topic, partition,
                kafka.api.OffsetRequest.EarliestTime(), clientName);
        init = true;

        return init;
    }

    private void startDataFetching() {

        if (maxReads > 0) {
            if (consumer == null) {
                consumer = new SimpleConsumer(leadBroker, port,
                        KAFKAConstants.SO_TIMEOUT,
                        KAFKAConstants.BUFFER_SIZE, clientName);
            }
            FetchRequest req = new FetchRequestBuilder()
                    .clientId(clientName)
                    .addFetch(topic, partition, readOffset,
                            KAFKAConstants.SO_TIMEOUT) // Note: this
                            // fetchSize of
                            // 100000 might need
                            // to be increased
                            // if large batches
                            // are written to
                            // Kafka
                    .build();
            FetchResponse fetchResponse = consumer.fetch(req);

            if (fetchResponse.hasError()) {
                // Something went wrong!
                short code = fetchResponse.errorCode(topic, partition);
                logger.error("Error fetching data from the Broker:"
                        + leadBroker + " Reason: " + code);
                if (code == ErrorMapping.OffsetOutOfRangeCode()) {
                    // We asked for an invalid offset. For simple case ask
                    // for the last element to reset
                    readOffset = getLastOffset(consumer, topic, partition,
                            kafka.api.OffsetRequest.LatestTime(),
                            clientName);
                }
                consumer.close();
                consumer = null;
                try {
                    leadBroker = findNewLeader(leadBroker, topic,
                            partition, port);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            for (MessageAndOffset messageAndOffset : fetchResponse
                    .messageSet(topic, partition)) {
                long currentOffset = messageAndOffset.offset();
                if (currentOffset < readOffset) {
                    logger.info("Found an old offset: " + currentOffset
                            + " Expecting: " + readOffset);
                    continue;
                }
                readOffset = messageAndOffset.nextOffset();
                ByteBuffer payload = messageAndOffset.message().payload();

                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                try {
                    logger.info("Add to injectHandler.invoke");
                    injectHandler.invoke(bytes);
                    logger.info("End of Add to injectHandler.invoke");

                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("0 - added to queue!");
                }

                maxReads--;
                if (maxReads < 0) {
                    break;
                }

            }
            consumer.close();
        }

    }

    public static long getLastOffset(SimpleConsumer consumer, String topic,
                                     int partition, long whichTime, String clientName) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic,
                partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(
                whichTime, 1));
        OffsetRequest request = new OffsetRequest(
                requestInfo, kafka.api.OffsetRequest.CurrentVersion(),
                clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            logger.error("Error fetching data Offset Data the Broker. Reason: "
                    + response.errorCode(topic, partition));
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }

    private String findNewLeader(String a_oldLeader, String a_topic,
                                 int a_partition, int a_port) throws Exception {
        for (int i = 0; i < 3; i++) {
            boolean goToSleep = false;
            PartitionMetadata metadata = findLeader(m_replicaBrokers, a_port,
                    a_topic, a_partition);
            if (metadata == null) {
                goToSleep = true;
            } else if (metadata.leader() == null) {
                goToSleep = true;
            } else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host())
                    && i == 0) {
                // first time through if the leader hasn't changed give
                // ZooKeeper a second to recover
                // second time, assume the broker did recover before fail over,
                // or it was a non-Broker issue
                goToSleep = true;
            } else {
                return metadata.leader().host();
            }
            if (goToSleep) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
        throw new Exception(
                "Unable to find new leader after Broker failure. Exiting");
    }

    private PartitionMetadata findLeader(List<String> a_seedBrokers,
                                         int a_port, String a_topic, int a_partition) throws Exception {
        PartitionMetadata returnMetaData = null;
        loop:
        for (String seed : a_seedBrokers) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(seed, a_port,
                        KAFKAConstants.SO_TIMEOUT, KAFKAConstants.BUFFER_SIZE,
                        "leaderLookup");
                List<String> topics = Collections.singletonList(a_topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                throw new Exception("Error communicating with Broker [" + seed
                        + "] to find Leader for [" + a_topic + ", "
                        + a_partition + "] Reason: ", e);
            } finally {
                if (consumer != null)
                    consumer.close();
            }
        }
        if (returnMetaData != null) {
            m_replicaBrokers.clear();
            for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
                m_replicaBrokers.add(replica.host());
            }
        }
        return returnMetaData;
    }

}
