package org.wso2.carbon.inbound.endpoint.protocol.kafka;

public class KAFKAConstants {

    public static final String ZOOKEEPER_CONNECT = "zookeeper.connect";
    public static final String GROUP_ID = "group.id";
    public static final String ZOOKEEPER_SESSION_TIMEOUT_MS = "zookeeper.session.timeout.ms";
    public static final String ZOOKEEPER_SYNC_TIME_MS = "zookeeper.sync.time.ms";
    public static final String ZOOKEEPER_COMMIT_INTERVAL_MS = "auto.commit.interval.ms";
    public static final String THREAD_COUNT = "thread.count";
    public static final String TOPICS = "topics";
    public static final String CONTENT_TYPE = "content.type";
    public static final String TOPIC_FILTER = "topic.filter";
    public static final String FILTER_FROM_WHITELIST = "filter.from.whitelist";
    public static final String SIMPLE_TOPIC = "simple.topic";
    public static final String SIMPLE_BROKERRS = "simple.brokers";
    public static final String SIMPLE_PORT = "simple.port";
    public static final String SIMPLE_PARTITION = "simple.partition";
    public static final String SIMPLE_MAX_MSGS_TO_READ = "simple.max.messages.to.read";
    public static final String CONSUMER_TYPE = "consumer.type";
    public static final int SO_TIMEOUT = 100000;
    public static final int BUFFER_SIZE = 64 * 1024;

}
