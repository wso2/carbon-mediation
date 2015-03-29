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
    private String strUserName;
    private String strPassword;
    private int threadCount;
    private List<String> topics;
    private AbstractKafkaMessageListener messageListener;
    private static int msgCounter;// uses for logging purpose
    private static final String POISON_OBJ = "poison object";


    public KAFKAPollingConsumer(Properties kafkaProperties) throws Exception {

        this.kafkaProperties = kafkaProperties;
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
        this.msgCounter = 1;

    }

    public void startsMessageListener() throws Exception {
        if (messageListener == null) {
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

    public void execute() {
        poll();
    }

    public void registerHandler(InjectHandler processingHandler) {
        injectHandler = processingHandler;
    }

    public Object poll() {
        if (logger.isDebugEnabled()) {
            logger.debug("run() - polling messages");
        }
        try {
            if (!messageListener.createKafkaConsumerConnector()) {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
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

    public AbstractKafkaMessageListener getMessageListener() {
        return messageListener;
    }

    public void destroy() throws Exception {

        messageListener.destroy();
    }
}
