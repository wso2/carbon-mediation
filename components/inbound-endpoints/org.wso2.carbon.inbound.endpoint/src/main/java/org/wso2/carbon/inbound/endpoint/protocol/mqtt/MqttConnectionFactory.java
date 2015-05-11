/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

/**
 * MQTT factory, return instances of asynchronous and synchronous clients
 */
public class MqttConnectionFactory {

	private static final Log log = LogFactory.getLog(MqttConnectionFactory.class);

	private String factoryName;

	private Hashtable<String, String> parameters = new Hashtable<String, String>();

	public MqttConnectionFactory(Properties passedInParameter) {

		this.factoryName = passedInParameter.getProperty(MqttConstants.PARAM_MQTT_CONFAC);
		
		try {
			parameters.put(MqttConstants.MQTT_SERVER_HOST_NAME,
			               passedInParameter.getProperty(MqttConstants.MQTT_SERVER_HOST_NAME));
			parameters.put(MqttConstants.MQTT_TOPIC_NAME,
			               passedInParameter.getProperty(MqttConstants.MQTT_TOPIC_NAME));
			parameters.put(MqttConstants.MQTT_SERVER_PORT,
			               passedInParameter.getProperty(MqttConstants.MQTT_SERVER_PORT));
			parameters.put(MqttConstants.MQTT_QOS,
			               passedInParameter.getProperty(MqttConstants.MQTT_QOS));
			parameters.put(MqttConstants.MQTT_SESSION_CLEAN,
			               passedInParameter.getProperty(MqttConstants.MQTT_SESSION_CLEAN));
			parameters.put(MqttConstants.MQTT_SSL_ENABLE,
			               passedInParameter.getProperty(MqttConstants.MQTT_SSL_ENABLE));
			parameters.put(MqttConstants.MQTT_BLOCKING_SENDER,
			               passedInParameter.getProperty(MqttConstants.MQTT_BLOCKING_SENDER));
			parameters.put(MqttConstants.MQTT_TEMP_STORE,
			               passedInParameter.getProperty(MqttConstants.MQTT_TEMP_STORE));
			
			
		} catch (Exception e) {
			log.error("Error while reading properties for MQTT Connection Factory " + factoryName,
			          e);
		}

	}

	public String getName() {
		return factoryName;
	}

	public MqttClient getMqttClient() {
		return createMqttClient();
	}

	public MqttAsyncClient getMqttAsyncClient() {
		return createMqttAsyncClient();
	}

	public String getTopic() {
		return parameters.get(MqttConstants.MQTT_TOPIC_NAME);
	}

	/**
	 *  
	 * 
	 * @return a synchronous MQTT client
	 */
	private MqttClient createMqttClient() {

		String uniqueClientId = MqttClient.generateClientId()+new Random().nextInt(100);

		String sslEnable = parameters.get(MqttConstants.MQTT_SSL_ENABLE);

		// This sample stores in a temporary directory... where messages
		// temporarily
		// stored until the message has been delivered to the server.
		String tmpDir = parameters.get(MqttConstants.MQTT_TEMP_STORE);
		MqttDefaultFilePersistence dataStore = null;
		if (tmpDir != null) {
			dataStore = new MqttDefaultFilePersistence(tmpDir);
		} else {
			tmpDir = System.getProperty("java.io.tmpdir");
			dataStore = new MqttDefaultFilePersistence(tmpDir);
		}

		String mqttEndpointURL =
		                         "tcp://" + parameters.get(MqttConstants.MQTT_SERVER_HOST_NAME) +
		                                 ":" + parameters.get(MqttConstants.MQTT_SERVER_PORT);
		// If SSL is enabled in the config, Use SSL tranport
		if (sslEnable != null && sslEnable.equalsIgnoreCase("true")) {
			mqttEndpointURL =
			                  "ssl://" + parameters.get(MqttConstants.MQTT_SERVER_HOST_NAME) + ":" +
			                          parameters.get(MqttConstants.MQTT_SERVER_PORT);
		}

		MqttClient mqttClient = null;
        try {
	        mqttClient = new MqttClient(mqttEndpointURL, uniqueClientId, dataStore);
        } catch (MqttException e1) {
        	log.error("Error while creating the MQTT client", e1);
        }

		return mqttClient;
	}

	/**
	 * 
	 * 
	 * @return an asynchronous client
	 */
	private MqttAsyncClient createMqttAsyncClient() {

		String uniqueClientId = MqttAsyncClient.generateClientId()+new Random().nextInt(100);

		String sslEnable = parameters.get(MqttConstants.MQTT_SSL_ENABLE);

		// This sample stores in a temporary directory... where messages
		// temporarily
		// stored until the message has been delivered to the server.
		String tmpDir = parameters.get(MqttConstants.MQTT_TEMP_STORE);
		MqttDefaultFilePersistence dataStore = null;
		if (tmpDir != null) {
			dataStore = new MqttDefaultFilePersistence(tmpDir);
		} else {
			tmpDir = System.getProperty("java.io.tmpdir");
			dataStore = new MqttDefaultFilePersistence(tmpDir);
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
	        mqttClient = new MqttAsyncClient(mqttEndpointURL, uniqueClientId, dataStore);
        } catch (MqttException e1) {
        	log.error("Error while creating the MQTT client", e1);
        }

		return mqttClient;
	}

}
