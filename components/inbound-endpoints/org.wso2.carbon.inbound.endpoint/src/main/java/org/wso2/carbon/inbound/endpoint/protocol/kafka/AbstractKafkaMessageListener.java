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

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Properties;

public abstract class AbstractKafkaMessageListener {
    protected int threadCount;
    protected List<String> topics;
    protected ConsumerConnector consumerConnector;
    protected InjectHandler injectHandler;
    protected Properties kafkaProperties;
    protected ConsumerIterator<byte[], byte[]> consumerIte;
    protected static final Log logger = LogFactory
            .getLog(KAFKAMessageListener.class.getName());

    public static enum CONSUMER_TYPE {

        HIGHLEVEL("highlevel"), SIMPLE("simple");
        String name;

        private CONSUMER_TYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public abstract boolean createKafkaConsumerConnector() throws Exception;

	/*
     * Starts topics consuming
	 */

    public abstract void start() throws Exception;

    public void destroy() {
//		executerService.shutdownNow();
    }

    protected void startConsumers(List<KafkaStream<byte[], byte[]>> streams,
                                  int threadNo) {
    }

    public ConsumerConnector getConsumerConnector() {
        return consumerConnector;
    }

    public ConsumerIterator<byte[], byte[]> getConsumerIte() {
        return consumerIte;
    }

    public abstract void injectMessageToESB();

    public abstract boolean hasNext();
}
