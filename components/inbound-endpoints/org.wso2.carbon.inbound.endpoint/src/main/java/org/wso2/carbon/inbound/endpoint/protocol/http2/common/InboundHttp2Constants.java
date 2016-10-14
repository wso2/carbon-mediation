package org.wso2.carbon.inbound.endpoint.protocol.http2.common;

/**
 * Created by chanakabalasooriya on 8/29/16.
 */
public class InboundHttp2Constants {


    public static final String HTTP2="http2";
    public static final String HTTPS2="https2";


    public static final String INBOUND_PORT = "inbound.http2.port";
    public static final String INBOUND_ENDPOINT_PARAMETER_DISPATCH_FILTER_PATTERN = "dispatch.filter.pattern";

    public static final String INBOUND_BOSS_THREAD_POOL_SIZE = "http2.boss.thread.pool.size";
    public static final String INBOUND_WORKER_THREAD_POOL_SIZE = "http2.worker.thread.pool.size";
    public static final String INBOUND_SO_BACKLOG="http2.so.backlog";

    public static final String INBOUND_SSL_KEY_STORE_FILE = "https2.ssl.key.store.file";
    public static final String INBOUND_SSL_KEY_STORE_PASS = "https2.ssl.key.store.pass";
    public static final String INBOUND_SSL_TRUST_STORE_FILE = "https2.ssl.trust.store.file";
    public static final String INBOUND_SSL_TRUST_STORE_PASS = "https2.ssl.trust.store.pass";
    public static final String INBOUND_SSL_CERT_PASS = "https2.ssl.cert.pass";

}
