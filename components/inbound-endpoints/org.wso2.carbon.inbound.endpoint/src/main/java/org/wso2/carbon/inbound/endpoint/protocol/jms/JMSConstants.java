/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.inbound.endpoint.protocol.jms;

import javax.xml.namespace.QName;

/**
 * 
 * Common constants used by inbound JMS protocol
 * 
 */
public class JMSConstants {

    public static enum JMSDestinationType {
        QUEUE, TOPIC
    };

    public static final String TOPIC_PREFIX = "topic.";
    public static final String QUEUE_PREFIX = "queue.";

    public static String JAVA_INITIAL_NAMING_FACTORY = "java.naming.factory.initial";
    public static String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";

    public static String CONNECTION_FACTORY_JNDI_NAME = "transport.jms.ConnectionFactoryJNDIName";
    public static String CONNECTION_FACTORY_TYPE = "transport.jms.ConnectionFactoryType";
    public static String DESTINATION_NAME = "transport.jms.Destination";
    public static String DESTINATION_TYPE = "transport.jms.DestinationType";
    public static final String DESTINATION_TYPE_QUEUE = "queue";
    public static final String DESTINATION_TYPE_TOPIC = "topic";
    public static String SESSION_TRANSACTED = "transport.jms.SessionTransacted";
    public static String SESSION_ACK = "transport.jms.SessionAcknowledgement";
    public static String RECEIVER_TIMEOUT = "transport.jms.ReceiveTimeout";    
    public static String CONTENT_TYPE = "transport.jms.ContentType";
    /** Namespace for JMS map payload representation */
    public static final String JMS_MAP_NS = "http://axis.apache.org/axis2/java/transports/jms/map-payload";
    /** Root element name of JMS Map message payload representation */
    public static final String JMS_MAP_ELEMENT_NAME = "JMSMap";
    public static final String SET_ROLLBACK_ONLY = "SET_ROLLBACK_ONLY";
    public static final QName JMS_MAP_QNAME = new QName(JMS_MAP_NS, JMS_MAP_ELEMENT_NAME, "");

    public static final String PARAM_CACHE_LEVEL = "transport.jms.CacheLevel";

    public static final String PARAM_JMS_USERNAME = "transport.jms.UserName";
    /** The password to use when obtaining a JMS Connection */
    public static final String PARAM_JMS_PASSWORD = "transport.jms.Password";
    /**
     * The parameter indicating the JMS API specification to be used - if this
     * is "1.1" the JMS 1.1 API would be used, else the JMS 1.0.2B
     */
    public static final String PARAM_JMS_SPEC_VER = "transport.jms.JMSSpecVersion";
    /**
     * Maximum number of shared JMS Connections when sending messages out
     * */
    public static final String MAX_JMS_CONNECTIONS = "transport.jms.MaxJMSConnections";

    public static final String MAX_JMS_SESSIONS = "transport.jms.MaxJMSSessions";

    public static final String PARAM_SUB_DURABLE = "transport.jms.SubscriptionDurable";
    /** The name for the durable subscription See {@link PARAM_SUB_DURABLE} */
    public static final String PARAM_DURABLE_SUB_NAME = "transport.jms.DurableSubscriberName";
    public static final String PARAM_DURABLE_SUB_CLIENT_ID = "transport.jms.DurableSubscriberClientID";

    /** A message selector to be used when messages are sought for this service */
    public static final String PARAM_MSG_SELECTOR = "transport.jms.MessageSelector";

    /**
     * Do not cache any JMS resources between tasks (when sending) or JMS CF's
     * (when sending)
     */
    public static final int CACHE_NONE = 0;
    /**
     * Cache only the JMS connection between tasks (when receiving), or JMS CF's
     * (when sending)
     */
    public static final int CACHE_CONNECTION = 1;
    /**
     * Cache only the JMS connection and Session between tasks (receiving), or
     * JMS CF's (sending)
     */
    public static final int CACHE_SESSION = 2;
    /**
     * Cache the JMS connection, Session and Consumer between tasks when
     * receiving
     */
    public static final int CACHE_CONSUMER = 3;
    /**
     * Cache the JMS connection, Session and Producer within a
     * JMSConnectionFactory when sending
     */
    public static final int CACHE_PRODUCER = 4;
    /**
     * automatic choice of an appropriate caching level (depending on the
     * transaction strategy)
     */
    public static final int CACHE_AUTO = 5;

}
