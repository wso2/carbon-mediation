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
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.wso2.carbon.inbound.endpoint.inboundfactory.InboundRequestProcessorFactoryImpl;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

import java.util.Properties;


/**
 * Listener to get a mqtt client from the factory and subscribe to a given topic
 */
public class MqttListener implements InboundRequestProcessor {

    private static final Log log = LogFactory.getLog(MqttListener.class);

    private String name;
    private SynapseEnvironment synapseEnvironment;

    private String injectingSeq;
    private String onErrorSeq;

    private Properties mqttProperties;
    private String contentType;
    private boolean sequential;

    private MqttConnectionFactory confac;

    private MqttAsyncCallback mqttAsyncCallback;

    private MqttClient mqttClient;
    private MqttAsyncClient mqttAsyncClient;

    private MqttInjectHandler injectHandler;

    protected String userName;
    protected String password;

    protected boolean cleanSession;

    private boolean mqttBlockingSenderEnable;
    private int retryInterval = 1000;
    private int retryCount = 50;

    private InboundProcessorParams params;
    private InboundRequestProcessorFactoryImpl listner;

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
                    Boolean.parseBoolean(mqttProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL));
        }

        confac = new MqttConnectionFactory(mqttProperties);
        contentType = confac.getContent();

        injectHandler =
                new MqttInjectHandler(injectingSeq, onErrorSeq, sequential,
                        synapseEnvironment, contentType);

        if (mqttProperties.getProperty(MqttConstants.MQTT_USERNAME) != null) {
            userName = mqttProperties.getProperty(MqttConstants.MQTT_USERNAME);
        }

        if (mqttProperties.getProperty(MqttConstants.MQTT_PASSWORD) != null) {
            password = mqttProperties.getProperty(MqttConstants.MQTT_PASSWORD);
        }

        cleanSession =
                Boolean.parseBoolean(mqttProperties.getProperty(MqttConstants.MQTT_SESSION_CLEAN));
        mqttBlockingSenderEnable =
                Boolean.parseBoolean(mqttProperties.getProperty(MqttConstants.MQTT_BLOCKING_SENDER));

    }

    public InboundRequestProcessorFactoryImpl getListner() {
        return listner;
    }

    @Override
    public void destroy() {

        try {
            if (mqttAsyncCallback != null) {
                mqttAsyncCallback.disconnect();
            } else
                mqttClient.disconnect();
            log.info("Disconnected.");
        } catch (MqttException e) {
            log.error("Error while disconnecting from the remote server...", e);
        }

    }

    @Override
    public void init() {

        log.info("MQTT inbound endpoint: " + name + " initializing ...");

        if (mqttBlockingSenderEnable){
            initSyncClient();
        }
        else{
            initAsyncClient();
        }
    }

    /**
     * MQTT synchrounous client
     */
    public void initSyncClient() {
        mqttClient = confac.getMqttClient();

        MqttSyncCallback mqttSyncCallback = new MqttSyncCallback(injectHandler);//changed
        mqttClient.setCallback(mqttSyncCallback);

        if (confac != null) {
            mqttSyncCallback.setMqttSyncCallback(confac);
        }
        if (params != null) {
            mqttSyncCallback.setParams(params);
            mqttSyncCallback.setName(params.getName());
        }

        MqttConnectOptions opt = new MqttConnectOptions();
        opt.setCleanSession(cleanSession);
        if (opt != null) {
            mqttSyncCallback.setOpt(opt);
        }

        if (userName != null && password != null) {
            opt.setUserName(userName);
            opt.setPassword(password.toCharArray());
        }

        try {
            mqttClient.connect();
            if (confac.getTopic() != null) {
                mqttClient.subscribe(confac.getTopic());
            }
            log.info("Connected to the remote server.");
        } catch (MqttException e) {
            if (!mqttClient.isConnected()) {
                int retryC = 0;
                while ((retryC < retryCount)) {
                    retryC++;
                    log.info("Attempting to reconnect" + " in " + retryInterval + " ms");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignore) {
                    }
                    try {
                        mqttClient.connect();
                        if (mqttClient.isConnected()) {
                            if (confac.getTopic() != null) {
                                mqttClient.subscribe(confac.getTopic());
                                log.info("Subscribed to the remote server.");
                            }
                            break;
                        }
                        log.info("Re-connected to the remote server.");
                    } catch (MqttException e1) {
                        log.error("Error while trying to retry", e1);
                    }
                }
            }
        }

    }

    /**
     * MQTT Asynchrounous client
     */
    public void initAsyncClient() {
        mqttAsyncClient = confac.getMqttAsyncClient();

        MqttConnectOptions opt = new MqttConnectOptions();
        opt.setCleanSession(cleanSession);

        if (userName != null && password != null) {
            opt.setUserName(userName);
            opt.setPassword(password.toCharArray());
        }

        try {
            mqttAsyncCallback = new MqttAsyncCallback(mqttAsyncClient, injectHandler);

            mqttAsyncCallback.setConOpt(opt);

            mqttAsyncCallback.setName(params.getName());

            mqttAsyncCallback.subscribe(confac.getTopic(),
                    Integer.parseInt(mqttProperties.getProperty(MqttConstants.MQTT_QOS)));

        } catch (MqttException e) {

            if (!mqttAsyncClient.isConnected()) {
                int retryC = 0;
                while ((retryC < retryCount)) {
                    retryC++;
                    log.info("Attempting to reconnect" + " in " + retryInterval + " ms");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                    try {
                        mqttAsyncCallback = new MqttAsyncCallback(mqttAsyncClient, injectHandler);

                        mqttAsyncCallback.setConOpt(opt);

                        if (confac.getTopic() != null) {
                            try {
                                mqttAsyncCallback.subscribe(confac.getTopic(),
                                        Integer.parseInt(mqttProperties.getProperty(MqttConstants.MQTT_QOS)));
                                if (mqttAsyncClient.isConnected()) {
                                    break;
                                }
                            } catch (Throwable throwable) {
                                log.info("Re-connected to the remote server.");
                            }

                        }

                    } catch (MqttException e1) {
                        log.error("Error while trying to retry", e1);
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.error("Error in qos level", e);

        } catch (Throwable e) {
            log.error("Error while subscribing to topic", e);
        }
    }

    public void start() {
    }


}
