/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.inbound.endpoint.protocol.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.Hashtable;
import java.util.Properties;

/**
 * MQTT factory, return instances of asynchronous and synchronous clients
 */
public class MqttConnectionFactory {

    private static final Log log = LogFactory.getLog(MqttConnectionFactory.class);

    private String factoryName;
    private Hashtable<String, String> parameters = new Hashtable<String, String>();
    MqttDefaultFilePersistence dataStore;


    public MqttConnectionFactory(Properties passedInParameter) {

        this.factoryName = passedInParameter.getProperty(MqttConstants.PARAM_MQTT_CONFAC);

        try {

            if (passedInParameter.getProperty(MqttConstants.MQTT_SERVER_HOST_NAME) != null) {
                parameters.put(MqttConstants.MQTT_SERVER_HOST_NAME,
                        passedInParameter.getProperty(MqttConstants.MQTT_SERVER_HOST_NAME));
            } else {
                log.error("Host name cannot be empty");
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_TOPIC_NAME) != null) {
                parameters.put(MqttConstants.MQTT_TOPIC_NAME,
                        passedInParameter.getProperty(MqttConstants.MQTT_TOPIC_NAME));
            } else {
                log.error("Specify the topic name to be subscribed");
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_SERVER_PORT) != null) {
                parameters.put(MqttConstants.MQTT_SERVER_PORT,
                        passedInParameter.getProperty(MqttConstants.MQTT_SERVER_PORT));
            } else {
                log.error("Port number cannot be empty");
            }

            if (passedInParameter.getProperty(MqttConstants.CONTENT_TYPE) != null) {
                parameters.put(MqttConstants.CONTENT_TYPE,
                        passedInParameter.getProperty(MqttConstants.CONTENT_TYPE));
            } else {
                log.error("Specify the content type of the message");
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_QOS) == null) {
                parameters.put(MqttConstants.MQTT_QOS, "1");
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_QOS) != null) {

                int qos = Integer.parseInt(passedInParameter.getProperty(MqttConstants.MQTT_QOS));

                if (qos == 2 || qos == 1 || qos == 0) {
                    parameters.put(MqttConstants.MQTT_QOS,
                            passedInParameter.getProperty(MqttConstants.MQTT_QOS));
                } else {
                    log.error("QOS must be either 0 or 1 or 2");
                }
            }

            parameters.put(MqttConstants.MQTT_TEMP_STORE,
                    passedInParameter.getProperty(MqttConstants.MQTT_TEMP_STORE));

            parameters.put(MqttConstants.MQTT_SESSION_CLEAN,
                    passedInParameter.getProperty(MqttConstants.MQTT_SESSION_CLEAN));

            parameters.put(MqttConstants.MQTT_SSL_ENABLE,
                    passedInParameter.getProperty(MqttConstants.MQTT_SSL_ENABLE));

            parameters.put(MqttConstants.MQTT_CLIENT_ID,
                    passedInParameter.getProperty(MqttConstants.MQTT_CLIENT_ID));

        } catch (Exception e) {
            log.error("Error while reading properties for MQTT connection factory " + factoryName);
        }

    }

    public String getName() {
        return factoryName;
    }

    public MqttAsyncClient getMqttAsyncClient() {
        return createMqttAsyncClient();
    }

    public String getTopic() {
        return parameters.get(MqttConstants.MQTT_TOPIC_NAME);
    }

    public String getContent() {
        return parameters.get(MqttConstants.CONTENT_TYPE);
    }

    private MqttAsyncClient createMqttAsyncClient() {

        String uniqueClientId;
        if (parameters.get(MqttConstants.MQTT_CLIENT_ID) != null) {
            uniqueClientId = parameters.get(MqttConstants.MQTT_CLIENT_ID);
        } else {
            uniqueClientId = MqttAsyncClient.generateClientId();
        }

        String sslEnable = parameters.get(MqttConstants.MQTT_SSL_ENABLE);

        // This sample stores in a temporary directory... where messages
        // temporarily
        // stored until the message has been delivered to the server.
        String tmpDir = parameters.get(MqttConstants.MQTT_TEMP_STORE);

        dataStore = null;

        int qos = Integer.parseInt(parameters.get(MqttConstants.MQTT_QOS.toString()));
        if (qos == 2 || qos == 1) {
            if (tmpDir != null) {
                dataStore = new MqttDefaultFilePersistence(tmpDir);
            } else {
                tmpDir = System.getProperty("java.io.tmpdir");
                dataStore = new MqttDefaultFilePersistence(tmpDir);
            }
        } else {
            dataStore = null;
        }

        String mqttEndpointURL =
                "tcp://" + parameters.get(MqttConstants.MQTT_SERVER_HOST_NAME) +
                        ":" + parameters.get(MqttConstants.MQTT_SERVER_PORT);
        // If SSL is enabled in the config, Use SSL transport
        if (sslEnable != null && sslEnable.equalsIgnoreCase("true")) {
            mqttEndpointURL =
                    "ssl://" + parameters.get(MqttConstants.MQTT_SERVER_HOST_NAME) + ":" +
                            parameters.get(MqttConstants.MQTT_SERVER_PORT);
        }

        MqttAsyncClient mqttClient = null;
        try {
            if (dataStore != null) {
                mqttClient = new MqttAsyncClient(mqttEndpointURL, uniqueClientId, dataStore);
            } else {
                mqttClient = new MqttAsyncClient(mqttEndpointURL, uniqueClientId);
            }
            log.info("Successfully created to mqtt client");
        } catch (MqttException ex) {
            log.error("Error while creating the MQTT asynchronous client");
        }
        return mqttClient;
    }

    public void shutdown() {
        if (dataStore != null) {
            try {
                dataStore.clear();
                dataStore.close();
            } catch (MqttPersistenceException ex) {
                log.error("Error while releasing the resources for data store");
            }
        }
    }
}
