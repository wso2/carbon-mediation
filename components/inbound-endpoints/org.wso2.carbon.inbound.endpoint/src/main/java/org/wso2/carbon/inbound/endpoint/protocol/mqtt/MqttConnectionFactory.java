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
import org.apache.synapse.SynapseException;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Hashtable;
import java.util.Properties;

/**
 * MQTT factory, return instances of asynchronous clients
 */
public class MqttConnectionFactory {

    private static final Log log = LogFactory.getLog(MqttConnectionFactory.class);

    private String factoryName;
    private Hashtable<String, String> parameters = new Hashtable<String, String>();
    private MqttDefaultFilePersistence dataStore;
    private static final int PORT_MIN_BOUND = 0;
    private static final int PORT_MAX_BOUND = 65535;

    public MqttConnectionFactory(Properties passedInParameter) {

        this.factoryName = passedInParameter.getProperty(MqttConstants.PARAM_MQTT_CONFAC);

        try {

            if (passedInParameter.getProperty(MqttConstants.MQTT_SERVER_HOST_NAME) != null) {
                parameters.put(MqttConstants.MQTT_SERVER_HOST_NAME,
                        passedInParameter.getProperty(MqttConstants.MQTT_SERVER_HOST_NAME));
            } else {
                String msg = "MQTT inbound listener Host Name cannot be empty";
                log.error(msg);
                throw new SynapseException(msg);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_TOPIC_NAME) != null) {
                parameters.put(MqttConstants.MQTT_TOPIC_NAME,
                        passedInParameter.getProperty(MqttConstants.MQTT_TOPIC_NAME));
            } else {
                String msg = "MQTT inbound listener Subscription Topic Name cannot be empty";
                log.error(msg);
                throw new SynapseException(msg);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_SERVER_PORT) != null) {
                validatePortField(passedInParameter.getProperty(MqttConstants.MQTT_SERVER_PORT));
                parameters.put(MqttConstants.MQTT_SERVER_PORT,
                        passedInParameter.getProperty(MqttConstants.MQTT_SERVER_PORT));
            } else {
                String msg = "MQTT inbound listener Port Number cannot be empty";
                log.error(msg);
                throw new SynapseException(msg);
            }

            if (passedInParameter.getProperty(MqttConstants.CONTENT_TYPE) != null) {
                parameters.put(MqttConstants.CONTENT_TYPE,
                        passedInParameter.getProperty(MqttConstants.CONTENT_TYPE));
            } else {
                log.warn("Default value is used for the parameter : "
                        + MqttConstants.CONTENT_TYPE);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_QOS) == null) {
                parameters.put(MqttConstants.MQTT_QOS, "1");
                log.warn("Default value is used for the parameter : "
                        + MqttConstants.MQTT_QOS);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_QOS) != null) {

                int qos = Integer.parseInt(passedInParameter.getProperty(MqttConstants.MQTT_QOS));

                if (qos == 2 || qos == 1 || qos == 0) {
                    parameters.put(MqttConstants.MQTT_QOS,
                            passedInParameter.getProperty(MqttConstants.MQTT_QOS));
                } else {
                    parameters.put(MqttConstants.MQTT_QOS, "1");
                    log.warn("Default value is used for the parameter : "
                            + MqttConstants.MQTT_QOS);
                }
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_TEMP_STORE) != null) {
                parameters.put(MqttConstants.MQTT_TEMP_STORE,
                        passedInParameter.getProperty(MqttConstants.MQTT_TEMP_STORE));
            } else {
                log.warn("Default value is used for the parameter : "
                        + MqttConstants.MQTT_TEMP_STORE);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_SESSION_CLEAN) != null) {
                parameters.put(MqttConstants.MQTT_SESSION_CLEAN,
                        passedInParameter.getProperty(MqttConstants.MQTT_SESSION_CLEAN));
            } else {
                log.warn("Default value is used for the parameter : "
                        + MqttConstants.MQTT_SESSION_CLEAN);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_SSL_ENABLE) != null) {
                parameters.put(MqttConstants.MQTT_SSL_ENABLE,
                        passedInParameter.getProperty(MqttConstants.MQTT_SSL_ENABLE));
            } else {
                log.warn("Default value is used for the parameter : "
                        + MqttConstants.MQTT_SSL_ENABLE);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_CLIENT_ID) != null) {
                parameters.put(MqttConstants.MQTT_CLIENT_ID,
                        passedInParameter.getProperty(MqttConstants.MQTT_CLIENT_ID));
            } else {
                log.warn("Default value is used for the parameter : "
                        + MqttConstants.MQTT_CLIENT_ID);
            }

            if (passedInParameter.getProperty(MqttConstants.MQTT_RECONNECTION_INTERVAL) != null) {
                parameters.put(MqttConstants.MQTT_RECONNECTION_INTERVAL,
                        passedInParameter.getProperty(MqttConstants.MQTT_RECONNECTION_INTERVAL));
            } else {
                log.warn("Default value is used for the parameter : "
                        + MqttConstants.MQTT_RECONNECTION_INTERVAL);
            }

        } catch (Exception ex) {
            log.error("MQTT connection factory : " + factoryName + " failed to initialize " +
                    "the MQTT Inbound configuration properties", ex);
            //this will prevent the inbound from deployment if anything goes bad and exception
            //thrown at this point will be thrown to the higher layer
            throw new SynapseException(ex.getMessage());
        }

    }

    public String getName() {
        return factoryName;
    }

    public MqttAsyncClient getMqttAsyncClient(String name) {
        return createMqttAsyncClient(name);
    }

    public String getTopic() {
        return parameters.get(MqttConstants.MQTT_TOPIC_NAME);
    }

    public String getContent() {
        return parameters.get(MqttConstants.CONTENT_TYPE);
    }

    public String getServerHost() {
        return parameters.get(MqttConstants.MQTT_SERVER_HOST_NAME);
    }

    public String getServerPort() {
        return parameters.get(MqttConstants.MQTT_SERVER_PORT);
    }

    /**
     * returns the interval which reconnection attempts make if broker is down
     *
     * @return interval reconnection interval in milliseconds
     */
    public int getReconnectionInterval() {
        if (parameters.get(MqttConstants.MQTT_RECONNECTION_INTERVAL) != null) {
            return Integer.parseInt(parameters.get(MqttConstants.MQTT_RECONNECTION_INTERVAL));
        } else {
            return -1;
        }
    }

    /**
     * creates a MQTT asynchronous client object for the inbound endpoint
     *
     * @param name inbound name
     * @return MqttAsyncClient client
     */
    private MqttAsyncClient createMqttAsyncClient(String name) {

        MqttClientManager clientManager = MqttClientManager.getInstance();
        String uniqueClientId;
        String inboundIdentifier;


        if (parameters.get(MqttConstants.MQTT_CLIENT_ID) != null) {
            uniqueClientId = parameters.get(MqttConstants.MQTT_CLIENT_ID);
        } else {
            uniqueClientId = MqttAsyncClient.generateClientId();
        }


        PrivilegedCarbonContext carbonContext =
                PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        name = clientManager.buildNameIdentifier(name, String.valueOf(tenantId));

        if (clientManager.hasInboundEndpoint(name)) {
            inboundIdentifier = clientManager.getInboundEndpointIdentifier(name);
        } else {
            inboundIdentifier = clientManager.
                    buildIdentifier(uniqueClientId, getServerHost(), getServerPort());
        }

        if (clientManager.hasMqttClient(inboundIdentifier)) {
            //update data store reference
            if (clientManager.hasClientDataStore(inboundIdentifier)) {
                dataStore = clientManager.getMqttClientDataStore(inboundIdentifier);
            }
            return clientManager.getMqttClient(inboundIdentifier);
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
            log.info("Successfully created MQTT client");
        } catch (MqttException ex) {
            log.error("Error while creating the MQTT asynchronous client", ex);
        }
        //here we register the created client in Mqtt clientManager
        clientManager.registerInboundEndpoint(name, inboundIdentifier);
        clientManager.registerMqttClient(inboundIdentifier, mqttClient);
        //register dataStore for Client
        if (dataStore != null) {
            clientManager.registerClientDataStore(inboundIdentifier, dataStore);
        }

        return mqttClient;
    }

    /**
     * validates a given port number whether it contains special characters or bounded to valid port
     * range (0-65535)  in operating system
     *
     * @param port port number
     */
    protected void validatePortField(String port) {
        try {
            int portInteger = Integer.parseInt(port);
            if ((PORT_MIN_BOUND < portInteger) && (portInteger < PORT_MAX_BOUND)) {
                //this is a valid port integer so just return
                return;
            } else {
                //in this case port number is not bounded to min and max, throwing synapse
                //exception will prevent the inbound from deployment
                String msg = "Server Port number should be bounded to min integer value: "
                        + PORT_MIN_BOUND + " and max integer value: " + PORT_MAX_BOUND;
                log.error(msg);
                throw new SynapseException(msg);
            }
        } catch (NumberFormatException ex) {
            //in this case port string contain any special characters, throwing synapse
            //exception will prevent the inbound from deployment
            String msg = "Server Port number should not contain any special characters";
            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    /**
     * clears the all related state maintained in data store files
     *
     * @param isClientConnected whether client has connected previously or not
     */
    public void shutdown(boolean isClientConnected) {
        //need to clear the resources if and only if client holds the lock for the resource
        //that is client has made a successful connection to the server
        //this will clear the persistence resources and releases the the lock bound to that resource
        if (dataStore != null && isClientConnected) {
            try {
                dataStore.clear();
                dataStore.close();
            } catch (MqttPersistenceException ex) {
                log.error("Error while releasing the resources for data store", ex);
            }
        }
    }
}
