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
import org.apache.synapse.inbound.InboundProcessorParams;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.wso2.carbon.inbound.endpoint.inboundfactory.InboundRequestProcessorFactoryImpl;

/**
 * MQTT Synchronous call back handler
 */
public class MqttSyncCallback implements MqttCallback {

    private static final Log log = LogFactory.getLog(MqttSyncCallback.class);

    private MqttInjectHandler injectHandler;
    private int retryInterval = 1000;
    private int retryCount = 50;
    private MqttConnectionFactory confac;
    private MqttClient mqttClient;
    private InboundProcessorParams params;
    private MqttConnectOptions opt;
    InboundRequestProcessorFactoryImpl listener1;
    MqttListener listener;
    private MqttMessage msg;

    public MqttSyncCallback(MqttInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }

    /**
     * Handle losing connection with the server. Here we just print it to the test console.
     *
     * @param throwable Throwable connection lost
     */
    @Override
    public void connectionLost(Throwable throwable) { 
        connect();
        log.info("Connection reconnected.");
    }

    private void pause() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Error handling goes here...
        }
    }

    private void connect() {
        boolean tryConnecting = true;
        while (tryConnecting) {
            try {
                MqttClient mqttClient = confac.getMqttClient();
                mqttClient.connect();
                setParams(params);
                setMsg(msg);
                setMqttSyncCallback(confac);
                listener1 = new InboundRequestProcessorFactoryImpl();

                if (mqttClient.isConnected()) {
                    if (confac.getTopic() != null) {
                        mqttClient.subscribe(confac.getTopic());
                        log.info("Subscribed to the remote server.");
                    }
                    tryConnecting = false;
                    injectHandler.invoke(msg);
                    return;
                }
            } catch (Exception e1) {
                log.error("Connection attempt failed with '" + e1.getCause() +
                        "'. Retrying.");
                pause();

            }
        }
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws MqttException {
        if (log.isDebugEnabled()) {
            log.debug("Received Message: Topic:" + topic + "  Message: " + mqttMessage);
        }
        log.info("Received Message: Topic: " + topic);
        injectHandler.invoke(mqttMessage);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("message delivered .. : " + iMqttDeliveryToken.toString());
    }

    public void setMqttSyncCallback(MqttConnectionFactory confac) {
        this.confac = confac;
    }

    public void setParams(InboundProcessorParams params) {
        this.params = params;
    }

    public void setMsg(MqttMessage msg) {
        this.msg = msg;
    }

    public MqttMessage getMsg() {
        return msg;
    }

    public MqttConnectOptions getOpt() {
        return opt;
    }

    public void setOpt(MqttConnectOptions opt) {
        this.opt = opt;
    }
}
