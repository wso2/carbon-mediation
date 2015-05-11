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
import org.eclipse.paho.client.mqttv3.*;

/**
 * MQTT Synchronous call back handler
 */
public class MqttSyncCallback implements MqttCallback {

	private static final Log log = LogFactory.getLog(MqttSyncCallback.class);

	private MqttInjectHandler injectHandler;

	public MqttSyncCallback(MqttInjectHandler injectHandler) {
		this.injectHandler = injectHandler;
	}

	public void connectionLost(Throwable throwable) {
		throw new IllegalStateException();
	}

	public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		log.debug("Received Message: Topic:" + s + "  Message: " + mqttMessage);
		injectHandler.invoke(mqttMessage);
	}

	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		throw new IllegalStateException();
	}

	public void messageArrived(MqttTopic mqttTopic, MqttMessage mqttMessage) throws Exception {
		log.debug("Received Message: Topic:" + mqttTopic + "  Message: " + mqttMessage);
		injectHandler.invoke(mqttMessage);
	}

	public void deliveryComplete(MqttDeliveryToken mqttDeliveryToken) {
		log.info("message delivered .. : " + mqttDeliveryToken.toString());
	}
}
