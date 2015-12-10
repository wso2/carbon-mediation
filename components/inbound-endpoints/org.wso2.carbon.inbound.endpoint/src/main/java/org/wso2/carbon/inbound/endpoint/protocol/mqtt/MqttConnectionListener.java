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
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * MQTT connection Listener bounded per MQTT client, this the listener registered per client
 * when mqtt connection is being made. Delegates logic per successful and failed connection scenarios
 */
public class MqttConnectionListener implements IMqttActionListener {

    private static final Log log = LogFactory.getLog(MqttConnectionListener.class);
    private MqttConnectionConsumer mqttConnectionConsumer;
    private boolean execute = true;
    private static final int DEFAULT_RECONNECTION_INTERVAL = 10000;

    public MqttConnectionListener(MqttConnectionConsumer mqttConnectionConsumer) {
        this.mqttConnectionConsumer = mqttConnectionConsumer;
    }

    public void onSuccess(IMqttToken token) {
        mqttConnectionConsumer.releaseTaskSuspension();
    }

    public void onFailure(IMqttToken token, Throwable exception) {
        try {

            int retryInterval = mqttConnectionConsumer.getMqttConnectionFactory().
                    getReconnectionInterval();
            if (retryInterval != -1) {
                Thread.sleep(retryInterval);
            } else {
                Thread.sleep(DEFAULT_RECONNECTION_INTERVAL);
            }

            if (execute) {
                mqttConnectionConsumer.getMqttAsyncClient()
                        .connect(mqttConnectionConsumer.getConnectOptions(), this);
            }
        } catch (MqttException ex) {
            log.error("Error while trying to subscribe to the remote", ex);
        } catch (InterruptedException ex) {
            log.error("Error while trying to subscribe to the remote", ex);
        }
    }

    /**
     * shutdowns the connection listener from issuing reconnection attempts
     */
    public void shutdown() {
        this.execute = false;
    }
}
