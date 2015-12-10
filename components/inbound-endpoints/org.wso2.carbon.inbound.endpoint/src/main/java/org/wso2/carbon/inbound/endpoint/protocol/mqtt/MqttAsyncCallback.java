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
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.inbound.endpoint.common.OneTimeTriggerAbstractCallback;

/**
 * MQTT Asynchronous call back handler
 */
public class MqttAsyncCallback extends OneTimeTriggerAbstractCallback implements MqttCallback {

    private static final Log log = LogFactory.getLog(MqttAsyncCallback.class);

    private String name;

    private MqttListener asycClient;

    private MqttInjectHandler injectHandler;
    private MqttConnectionFactory confac;
    private MqttAsyncClient mqttAsyncClient;
    private Properties mqttProperties;
    private MqttConnectOptions connectOptions;
    private MqttConnectionConsumer connectionConsumer;
    private MqttConnectionListener connectionListener;

    public MqttAsyncCallback(MqttAsyncClient mqttAsyncClient, MqttInjectHandler injectHandler,
                             MqttConnectionFactory confac, MqttConnectOptions connectOptions,
                             Properties mqttProperties) {
        this.injectHandler = injectHandler;
        this.mqttAsyncClient = mqttAsyncClient;
        this.confac = confac;
        this.connectOptions = connectOptions;
        this.mqttProperties = mqttProperties;

    }

    /**
     * Handle losing connection with the server. Here we just print it to the test console.
     *
     * @param throwable Throwable connection lost
     */
    @Override
    public void connectionLost(Throwable throwable) {
        log.info("Connection lost occurred to the remote server.");
        try {
            super.handleReconnection();
        } catch (InterruptedException ex) {
            log.error("Unable to suspend the callback reconnection", ex);
        }
    }

    protected void reConnect() {
        if (mqttAsyncClient != null) {
            try {
                connectionListener = new MqttConnectionListener(connectionConsumer);
                mqttAsyncClient.connect(connectOptions, connectionListener);

                connectionConsumer.acquireTaskSuspension();

                if (mqttAsyncClient.isConnected()) {
                    int qosLevel = Integer.parseInt(mqttProperties
                            .getProperty(MqttConstants.MQTT_QOS));
                    if (confac.getTopic() != null) {
                        mqttAsyncClient.subscribe(confac.getTopic(), qosLevel);
                    }
                    log.info("Re-Connected to the remote server.");
                }
            } catch (MqttException ex) {
                log.error("Error while trying to subscribe to the remote.", ex);
            } catch (InterruptedException ex) {
                log.error("Error while trying to subscribe to the remote.", ex);
            }
        }
    }

    /**
     * handles the incoming messages from broker and inject to Synapse mediation
     *
     * @param topic topic on which message received
     * @param mqttMessage mqtt message
     */
    public void messageArrived(String topic, MqttMessage mqttMessage) throws MqttException {
        if (log.isDebugEnabled()) {
            log.debug("Received Message: Topic:" + topic + "  Message: " + mqttMessage);
        }
        log.info("Received Message: Topic: " + topic);
        MqttClientManager clientManager = MqttClientManager.getInstance();
        String inboundIdentifier = clientManager.buildIdentifier
                (mqttAsyncClient.getClientId(), confac.getServerHost(), confac.getServerPort());
        if (super.isInboundRunnerMode()) {
            //register tenant loading flag for inbound identifier
            clientManager.registerInboundTenantLoadingFlag(inboundIdentifier);
            //this is a blocking call
            super.startInboundTenantLoading(inboundIdentifier);
            //un-register tenant loading flag for inbound identifier
            clientManager.unRegisterInboundTenantLoadingFlag(inboundIdentifier);

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantDomain(super.tenantDomain, true);
                injectHandler.invoke(mqttMessage, name);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            injectHandler.invoke(mqttMessage, name);
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("message delivered .. : " + iMqttDeliveryToken.toString());
    }

    public void setMqttConnectionConsumer(MqttConnectionConsumer connectionConsumer) {
        this.connectionConsumer = connectionConsumer;
    }

    /**
     * get the connection consumer reference
     * @return connectionConsumer reference
     */
    public MqttConnectionConsumer getMqttConnectionConsumer() {
        return this.connectionConsumer;
    }

    /**
     * get the options for the MQTT connection
     * @return connectOptions reference
     */
    public MqttConnectOptions getMqttConnectionOptions() {
        return this.connectOptions;
    }

    public void updateInjectHandler(MqttInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }

    /**
     * Shutdowns the connection listener.
     */
    public void shutdown() {
        super.shutdown();
        if (connectionListener != null) {
            this.connectionListener.shutdown();
        }
    }

    /**
     * Set the inbound endpoint name
     * @param name
     */
    public void setName (String name) {
        this.name = name;
    }

    /**
     * get the inbound endpoint name
     * @return name
     */
    public String getName () {
        return this.name;
    }
}
