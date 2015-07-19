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
import org.eclipse.paho.client.mqttv3.*;

/**
 * MQTT Asynchronous call back handler
 */
public class MqttAsyncCallback implements MqttCallback {

    //state variables
    static final int BEGIN = 0;
    static final int CONNECTED = 1;
    static final int PUBLISHED = 2;
    static final int SUBSCRIBED = 3;
    static final int DISCONNECTED = 4;
    static final int FINISH = 5;
    static final int ERROR = 6;
    static final int DISCONNECT = 7;
    private final Object waiter = new Object();
    int state = BEGIN;
    private MqttConnectOptions conOpt;
    private Log log = LogFactory.getLog(MqttAsyncCallback.class);
    // Private instance variables
    private MqttAsyncClient client;
    private Throwable ex = null;
    private boolean donext = false;

    private String name;

    private MqttListener asycClient;

    private MqttInjectHandler injectHandler;

    public MqttAsyncCallback(MqttAsyncClient clientAsync, MqttInjectHandler injectHandler) throws MqttException {

        client = clientAsync;
        // Set this wrapper as the callback handler
        client.setCallback(this);

        this.injectHandler = injectHandler;

    }

    public void setConOpt(MqttConnectOptions conOpt) {
        this.conOpt = conOpt;
    }

    /**
     * Wait for a maximum amount of time for a state change event to occur
     *
     * @param maxTTW maximum time to wait in milliseconds
     * @throws MqttException
     */
    private void waitForStateChange(int maxTTW) throws MqttException {
        synchronized (waiter) {
            if (!donext) {
                try {
                    waiter.wait(maxTTW);
                } catch (InterruptedException e) {
                    log.error("timed out");//TODO LOG.ERROR
                }

            }
            donext = false;
        }
    }

    /**
     * Subscribe to a topic on an MQTT server Once subscribed this method waits
     * for the messages to arrive from the server that match the subscription.
     *
     * @param topicName to subscribe to (can be wild carded)
     * @param qos       the maximum quality of service to receive messages at for this
     *                  subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws Throwable {
        // Use a state machine to decide which step to do next. State change
        // occurs
        // when a notification is received that an MQTT action has completed
        while (state != FINISH) {
            switch (state) {
                case BEGIN:
                    // Connect using a non-blocking connect
                    MqttConnector con = new MqttConnector();
                    con.doConnect();
                    break;
                case CONNECTED:
                    // Subscribe using a non-blocking subscribe
                    Subscriber sub = new Subscriber();
                    sub.doSubscribe(topicName, qos);
                    break;
                case SUBSCRIBED:
                    state = FINISH;
                    donext = true;
                    break;
                case ERROR:
                    throw ex;
            }
            waitForStateChange(10000);//TODO CHECK AVOID  //NEEDED checked with MB https://github.com/wso2/product-mb/blob/master/modules/integration/tests-common/admin-clients/src/main/java/org/wso2/mb/integration/common/clients/MQTTClientEngine.java
        }
    }

    public void disconnect() {
        Disconnector disc = new Disconnector();
        disc.doDisconnect();
    }

    /**
     * Handle losing connection with the server. Here we just print it to the test console.
     *
     * @param throwable Throwable connection lost
     */
    public void connectionLost(Throwable throwable) {
        log.error("Connection Lost - Client Disconnected");
    }// TODO ok

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {//TODO GIVE REASONABLE NAME S
        if (log.isDebugEnabled()) {
            log.debug("Received Message: Topic:" + topic + "  Message: " + mqttMessage);
        }
        log.info("Received Message: Topic: " + topic);
        injectHandler.invoke(mqttMessage, name);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("message delivered .. : " + iMqttDeliveryToken.toString());
    }

    /**
     * Connect in a non-blocking way and then sit back and wait to be notified
     * that the action has completed.
     */
    public class MqttConnector {

        public MqttConnector() {
        }

        public void doConnect() {
            // Connect to the server
            // Get a token and setup an asynchronous listener on the token which
            // will be notified once the connect completes
            log.info("Connecting to broker with client ID "
                    + client.getClientId());

            IMqttActionListener conListener = new IMqttActionListener() {
                public void onSuccess(IMqttToken asyncActionToken) {
                    log.info("Connected");
                    state = CONNECTED;
                    carryOn();
                }

                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    log.info("connect failed" + exception);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (waiter) {
                        donext = true;
                        waiter.notifyAll();
                    }
                }
            };

            try {
                // Connect using a non-blocking connect
                client.connect(conOpt, "Connect sample context", conListener);

            } catch (MqttException e) {
                // If though it is a non-blocking connect an exception can be
                // thrown if validation of parms fails or other checks such
                // as already connected fail.
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    /**
     * Subscribe in a non-blocking way and then sit back and wait to be notified
     * that the action has completed.
     */
    public class Subscriber {
        public void doSubscribe(String topicName, int qos) {
            // Make a subscription
            // Get a token and setup an asynchronous listener on the token which
            // will be notified once the subscription is in place.
            log.info("Subscribing to topic \"" + topicName + "\" qos " + qos);

            IMqttActionListener subListener = new IMqttActionListener() {
                public void onSuccess(IMqttToken asyncActionToken) {
                    log.info("Subscribe Completed");
                    state = SUBSCRIBED;
                    carryOn();
                }

                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    log.info("Subscribe failed" + exception);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (waiter) {
                        donext = true;
                        waiter.notifyAll();
                    }
                }
            };

            try {
                client.subscribe(topicName, qos, "Subscribe sample context",
                        subListener);
            } catch (MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
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

    /**
     * Disconnect in a non-blocking way and then sit back and wait to be
     * notified that the action has completed.
     */
    public class Disconnector {
        public void doDisconnect() {
            // Disconnect the client
            log.info("Disconnecting");

            IMqttActionListener discListener = new IMqttActionListener() {
                public void onSuccess(IMqttToken asyncActionToken) {
                    log.info("Disconnect Completed");
                    state = DISCONNECTED;
                    carryOn();
                }

                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    log.info("Disconnect failed" + exception);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (waiter) {
                        donext = true;
                        waiter.notifyAll();
                    }
                }
            };

            try {
                client.disconnect("Disconnect sample context", discListener);
            } catch (MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }
}
