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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Maintain the common methods used by inbound RabbitMQ protocol.
 */
public class RabbitMQUtils {

    private static final Log log = LogFactory.getLog(RabbitMQUtils.class);

    public static Connection createConnection(ConnectionFactory factory) throws IOException {
        Connection connection = factory.newConnection();
        return connection;
    }

    public static String getProperty(MessageContext mc, String key) {
        return (String) mc.getProperty(key);
    }

    public static boolean isDurableQueue(Hashtable<String, String> properties) {
        String durable = properties.get(RabbitMQConstants.QUEUE_DURABLE);
        return durable != null && Boolean.parseBoolean(durable);
    }

    public static boolean isExclusiveQueue(Hashtable<String, String> properties) {
        String exclusive = properties.get(RabbitMQConstants.QUEUE_EXCLUSIVE);
        return exclusive != null && Boolean.parseBoolean(exclusive);
    }

    public static boolean isAutoDeleteQueue(Hashtable<String, String> properties) {
        String autoDelete = properties.get(RabbitMQConstants.QUEUE_AUTO_DELETE);
        return autoDelete != null && Boolean.parseBoolean(autoDelete);
    }

    public static boolean isQueueAvailable(Connection connection, String queueName) throws IOException {
        Channel channel = connection.createChannel();
        try {
            // check availability of the named queue.
            // if an error is encountered, including if the queue does not exist and if the
            // queue is exclusively owned by another connection.
            channel.queueDeclarePassive(queueName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Declare a queue.
     * @param connection   the rabbitmq connection
     * @param queueName    the name of the queue
     * @param isDurable    true if we are declaring a durable queue
     * @param isExclusive  true if we are declaring an exclusive queue
     * @param isAutoDelete true if we are declaring an autodelete queue
     * @throws IOException
     */
    public static void declareQueue(Connection connection, String queueName, boolean isDurable,
                                    boolean isExclusive, boolean isAutoDelete) throws IOException {
        boolean queueAvailable = isQueueAvailable(connection, queueName);
        Channel channel = connection.createChannel();

        if (!queueAvailable) {
            if (log.isDebugEnabled()) {
                log.debug("Queue :" + queueName + " not found or already declared exclusive. Declaring the queue.");
            }
            // Declare the named queue if it does not exists.
            if (!channel.isOpen()) {
                channel = connection.createChannel();
                log.debug("Channel is not open. Creating a new channel.");
            }
            try {
                channel.queueDeclare(queueName, isDurable, isExclusive, isAutoDelete, null);
            } catch (IOException e) {
                handleException("Error while creating queue: " + queueName, e);
            }
        }
    }

    public static void declareQueue(Connection connection, String queueName,
                                    Hashtable<String, String> properties) throws IOException {
        Boolean queueAvailable = isQueueAvailable(connection, queueName);
        Channel channel = connection.createChannel();

        if (!queueAvailable) {
            // Declare the named queue if it does not exists.
            if (!channel.isOpen()) {
                channel = connection.createChannel();
                log.debug("Channel is not open. Creating a new channel.");
            }
            try {
                channel.queueDeclare(queueName, isDurableQueue(properties),
                        isExclusiveQueue(properties), isAutoDeleteQueue(properties), null);

            } catch (IOException e) {
                handleException("Error while creating queue: " + queueName, e);
            }
        }
    }


    public static void declareExchange(Connection connection, String exchangeName, Hashtable<String, String> properties) throws IOException {
        Boolean exchangeAvailable = false;
        Channel channel = connection.createChannel();
        String exchangeType = properties
                .get(RabbitMQConstants.EXCHANGE_TYPE);
        String durable = properties.get(RabbitMQConstants.EXCHANGE_DURABLE);
        try {
            // check availability of the named exchange.
            // The server will raise an IOException.
            // if the named exchange already exists.
            channel.exchangeDeclarePassive(exchangeName);
            exchangeAvailable = true;
        } catch (IOException e) {
            log.info("Exchange :" + exchangeName + " not found.Declaring exchange.");
        }

        if (!exchangeAvailable) {
            // Declare the named exchange if it does not exists.
            if (!channel.isOpen()) {
                channel = connection.createChannel();
                log.debug("Channel is not open. Creating a new channel.");
            }
            try {
                if (exchangeType != null
                        && !exchangeType.equals("")) {
                    if (durable != null && !durable.equals("")) {
                        channel.exchangeDeclare(exchangeName,
                                exchangeType,
                                Boolean.parseBoolean(durable));
                    } else {
                        channel.exchangeDeclare(exchangeName,
                                exchangeType, true);
                    }
                } else {
                    channel.exchangeDeclare(exchangeName, "direct", true);
                }
            } catch (IOException e) {
                handleException("Error occurred while declaring exchange.", e);
            }
        }
        channel.close();
    }

    public static void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RabbitMQException(message, e);
    }
}