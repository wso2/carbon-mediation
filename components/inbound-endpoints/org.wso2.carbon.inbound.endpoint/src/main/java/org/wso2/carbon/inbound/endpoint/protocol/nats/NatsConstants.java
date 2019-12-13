package org.wso2.carbon.inbound.endpoint.protocol.nats;

public class NatsConstants {

    private NatsConstants(){}

    public static final String SUBJECT = "subject";
    public static final String QUEUE_GROUP = "queue.group";
    public static final String BUFFER_SIZE = "buffer.size";
    public static final String TURN_ON_ADVANCED_STATS = "turn.on.advanced.stats";
    public static final String TRACE_CONNECTION = "trace.connection";
    public static final String TLS_PROTOCOL = "tls.protocol";
    public static final String TLS_KEYSTORE_TYPE = "tls.keystore.type";
    public static final String TLS_KEYSTORE_LOCATION = "tls.keystore.location";
    public static final String TLS_KEYSTORE_PASSWORD = "tls.keystore.password";
    public static final String TLS_TRUSTSTORE_TYPE = "tls.truststore.type";
    public static final String TLS_TRUSTSTORE_LOCATION = "tls.truststore.location";
    public static final String TLS_TRUSTSTORE_PASSWORD = "tls.truststore.password";
    public static final String TLS_KEY_MANAGER_ALGORITHM = "tls.key.manager.algorithm";
    public static final String TLS_TRUST_MANAGER_ALGORITHM = "tls.trust.manager.algorithm";
    public static final String CONTENT_TYPE = "content.type";

    // Default values
    static final String DEFAULT_TLS_ALGORITHM = "SunX509";
    static final String DEFAULT_STORE_TYPE = "JKS";
}
