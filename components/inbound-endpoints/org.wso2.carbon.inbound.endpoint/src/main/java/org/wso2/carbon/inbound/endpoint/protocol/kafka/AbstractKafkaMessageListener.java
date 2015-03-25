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
	protected KAFKAInjectHandler injectHandler;
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
		// executerService.shutdownNow();
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
