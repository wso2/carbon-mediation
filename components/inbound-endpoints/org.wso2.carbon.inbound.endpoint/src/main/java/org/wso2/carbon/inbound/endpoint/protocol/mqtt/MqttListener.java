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
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.wso2.carbon.inbound.endpoint.common.InboundOneTimeTriggerRequestProcessor;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Properties;


/**
 * Listener to get a mqtt client from the factory and subscribe to a given topic
 */
public class MqttListener extends InboundOneTimeTriggerRequestProcessor {

    private static final String ENDPOINT_POSTFIX = "MQTT" + COMMON_ENDPOINT_POSTFIX;
    private static final Log log = LogFactory.getLog(MqttListener.class);

    private String injectingSeq;
    private String onErrorSeq;

    private Properties mqttProperties;
    private String contentType;
    private boolean sequential;

    private MqttConnectionFactory confac;
    private MqttAsyncClient mqttAsyncClient;
    private MqttAsyncCallback mqttAsyncCallback;
    private MqttConnectOptions connectOptions;
    private MqttConnectionConsumer connectionConsumer;
    private MqttInjectHandler injectHandler;

    protected String userName;
    protected String password;

    protected boolean cleanSession;

    private InboundProcessorParams params;


    /**
     * constructor
     *
     * @param params
     */
    public MqttListener(InboundProcessorParams params) {
        this.name = params.getName();
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
        this.mqttProperties = params.getProperties();
        this.params = params;

        this.sequential = true;
        if (mqttProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL) != null) {
            this.sequential =
                    Boolean.parseBoolean(mqttProperties.getProperty
                            (PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL));
        }

        this.coordination = true;
        if (mqttProperties.getProperty(PollingConstants.INBOUND_COORDINATION) != null) {
            this.coordination =
                    Boolean.parseBoolean(mqttProperties.getProperty
                            (PollingConstants.INBOUND_COORDINATION));
        }

        confac = new MqttConnectionFactory(mqttProperties);
        contentType = confac.getContent();

        injectHandler =
                new MqttInjectHandler(injectingSeq, onErrorSeq, sequential,
                        synapseEnvironment, contentType);
        this.synapseEnvironment = params.getSynapseEnvironment();

        if (mqttProperties.getProperty(MqttConstants.MQTT_USERNAME) != null) {
            userName = mqttProperties.getProperty(MqttConstants.MQTT_USERNAME);
        }

        if (mqttProperties.getProperty(MqttConstants.MQTT_PASSWORD) != null) {
            password = mqttProperties.getProperty(MqttConstants.MQTT_PASSWORD);
        }

        cleanSession =
                Boolean.parseBoolean(mqttProperties.getProperty(MqttConstants.MQTT_SESSION_CLEAN));
    }

    @Override
    public void destroy() {
        //release the thread from suspension
        //this is need since Thread.join() causes issues
        //this will release thread suspended thread for completion
        super.destroy();
        connectionConsumer.shutdown();
        mqttAsyncCallback.shutdown();
        if (CarbonUtils.isWorkerNode()) {
            confac.shutdown();
        }
        try {
            if (mqttAsyncClient.isConnected()) {
                mqttAsyncClient.disconnect();
            }
            mqttAsyncClient.close();
            log.info("Disconnected from the remote MQTT server.");
        } catch (MqttException e) {
            log.error("Error while disconnecting from the remote server.");
        }


    }

    @Override
    public void init() {
        log.info("MQTT inbound endpoint " + name + " initializing ...");
        initAsyncClient();
        start();
    }

    public void initAsyncClient() {
        mqttAsyncClient = confac.getMqttAsyncClient();
        connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(cleanSession);
        if (userName != null && password != null) {
            connectOptions.setUserName(userName);
            connectOptions.setPassword(password.toCharArray());
        }
        mqttAsyncCallback = new MqttAsyncCallback(mqttAsyncClient, injectHandler,
                confac, connectOptions, mqttProperties);
        connectionConsumer = new MqttConnectionConsumer(connectOptions, mqttAsyncClient,
                confac, mqttProperties);
        mqttAsyncCallback.setMqttConnectionConsumer(connectionConsumer);
        mqttAsyncClient.setCallback(mqttAsyncCallback);
    }

    public void start() {
        MqttTask mqttTask = new MqttTask(connectionConsumer);
        mqttTask.setCallback(mqttAsyncCallback);
        start(mqttTask, ENDPOINT_POSTFIX);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
