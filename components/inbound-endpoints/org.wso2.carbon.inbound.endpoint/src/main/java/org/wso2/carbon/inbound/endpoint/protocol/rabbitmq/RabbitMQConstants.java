/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.rabbitmq;

/**
 * Holds constants for RabbitMQ Inbound Endpoint Implementation
 */
public class RabbitMQConstants {
    public static final String CONTENT_TYPE_PROPERTY_PARAM = "rabbitmq.transport.ContentTypeProperty";
    public static final String RABBITMQ_REPLY_TO = "RABBITMQ_REPLY_TO";
    public static final String SOAP_ACTION = "SOAP_ACTION";
    public static final String RABBITMQ_PREFIX = "rabbitmq";
    public static final String SET_ROLLBACK_ONLY = "SET_ROLLBACK_ONLY";
    public static final String RABBITMQ_CON_FAC = "rabbitmq.connection.factory";

    public static final String SERVER_HOST_NAME = "rabbitmq.server.host.name";
    public static final String SERVER_PORT = "rabbitmq.server.port";
    public static final String SERVER_USER_NAME = "rabbitmq.server.user.name";
    public static final String SERVER_PASSWORD = "rabbitmq.server.password";
    public static final String SERVER_VIRTUAL_HOST = "rabbitmq.server.virtual.host";
    public static final String SERVER_RETRY_INTERVAL = "rabbitmq.server.retry.interval";

    public static final String HEARTBEAT = "rabbitmq.factory.heartbeat";
    public static final String CONNECTION_TIMEOUT = "rabbitmq.factory.connection.timeout";
    public static final String RETRY_INTERVAL = "rabbitmq.connection.retry.interval";
    public static final String RETRY_COUNT = "rabbitmq.connection.retry.count";
    public static final String RECEIVE_TIMEOUT = "rabbitmq.connection.receive.timeout";

    public static final String CORRELATION_ID = "rabbitmq.message.correlation.id";
    public static final String MESSAGE_ID = "rabbitmq.message.id";
    public static final String CONTENT_TYPE = "rabbitmq.message.content.type";
    public static final String CONTENT_ENCODING = "rabbitmq.message.content.encoding";

    public static final String REPLY_TO_NAME = "rabbitmq.replyto.name";
    public static final String REPLY_TO_TIMEOUT = "rabbitmq.replyto.timeout";
    public static final String REPLY_TO_CONTENT_TYPE = "rabbitmq.replyto.content.type";

    //SSL related properties
    public static final String SSL_ENABLED = "rabbitmq.connection.ssl.enabled";
    public static final String SSL_KEYSTORE_LOCATION = "rabbitmq.connection.ssl.keystore.location";
    public static final String SSL_KEYSTORE_TYPE = "rabbitmq.connection.ssl.keystore.type";
    public static final String SSL_KEYSTORE_PASSWORD = "rabbitmq.connection.ssl.keystore.password";
    public static final String SSL_TRUSTSTORE_LOCATION = "rabbitmq.connection.ssl.truststore.location";
    public static final String SSL_TRUSTSTORE_TYPE = "rabbitmq.connection.ssl.truststore.type";
    public static final String SSL_TRUSTSTORE_PASSWORD = "rabbitmq.connection.ssl.truststore.password";
    public static final String SSL_VERSION = "rabbitmq.connection.ssl.version";

    public static final String EXCHANGE_NAME = "rabbitmq.exchange.name";
    public static final String EXCHANGE_TYPE = "rabbitmq.exchange.type";
    public static final String EXCHANGE_DURABLE = "rabbitmq.exchange.durable";
    public static final String EXCHANGE_AUTODELETE = "rabbitmq.exchange.auto.delete";

    public static final String QUEUE_NAME = "rabbitmq.queue.name";
    public static final String QUEUE_DURABLE = "rabbitmq.queue.durable";
    public static final String QUEUE_EXCLUSIVE = "rabbitmq.queue.exclusive";
    public static final String QUEUE_AUTO_DELETE = "rabbitmq.queue.auto.delete";
    public static final String QUEUE_AUTO_ACK = "rabbitmq.queue.auto.ack";
    public static final String QUEUE_ROUTING_KEY = "rabbitmq.queue.routing.key";
    public static final String QUEUE_DELIVERY_MODE = "rabbitmq.queue.delivery.mode"; // 1=NonPersistent , 2=Persistent

    public static final String CONSUMER_QOS = "rabbitmq.channel.consumer.qos";
    public static final String CONSUMER_TAG = "rabbitmq.consumer.tag";

    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final int DEFAULT_RETRY_INTERVAL = 30000;
    public static final int DEFAULT_RETRY_COUNT = 3;
    public static final int DEFAULT_THREAD_COUNT = 20;
    public static final int DEFAULT_DELIVERY_MODE = 2; //Default is persistent
    public static final int DEFAULT_REPLY_TO_TIMEOUT = 30000;
}


