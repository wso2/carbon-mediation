/*
 * Copyright 2020 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * Polling consumer for NATS to initialize connection and poll messages.
 */
public class NatsPollingConsumer {

    private static final Log log = LogFactory.getLog(NatsPollingConsumer.class.getName());
    private NatsInjectHandler injectHandler;
    private Properties natsProperties;
    private NatsMessageListener natsMessageListener;
    private String subject;
    private long scanInterval;
    private Long lastRanTime;
    private String name;

    NatsPollingConsumer(Properties natsProperties, long scanInterval, String name) {
        this.natsProperties = natsProperties;
        this.scanInterval = scanInterval;
        this.name = name;
        this.subject = natsProperties.getProperty(NatsConstants.SUBJECT);
        if (subject == null) throw new SynapseException("NATS subject cannot be null.");
    }

    NatsPollingConsumer() {}

    /**
     * Initialize the message listener to use (Core NATS or NATS Streaming).
     */
    void initializeMessageListener() {
        printDebugLog("Create the NATS message listener.");
        if (Boolean.parseBoolean(natsProperties.getProperty(NatsConstants.NATS_STREAMING))) {
            natsMessageListener = new StreamingListener(subject, injectHandler, natsProperties);
            return;
        }
        natsMessageListener = new CoreListener(subject, injectHandler, natsProperties);
    }

    /**
     * Called at taskExecute to execute the NATS Task.
     */
    public void execute() {
        try {
            printDebugLog("Executing : NATS Inbound EP : ");
            // Check if the cycles are running in correct interval and start scan
            long currentTime = (new Date()).getTime();
            if (lastRanTime == null || ((lastRanTime + (scanInterval)) <= currentTime)) {
                lastRanTime = currentTime;
                poll();
            } else {
                printDebugLog("Skip cycle since concurrent rate is higher than the scan interval : NATS Inbound EP ");
            }
            printDebugLog("End : NATS Inbound EP : ");
        } catch (IOException | InterruptedException e) {
            log.error("An error occurred while connecting to NATS server or consuming the message. " + e);
            natsMessageListener.closeConnection();
        } catch (Exception e) {
            log.error("Error while retrieving or injecting NATS message. " + e.getMessage(), e);
            natsMessageListener.closeConnection();
        }
    }

    /**
     * Create the NATS connection and poll messages.
     */
    public Object poll() throws IOException, InterruptedException, TimeoutException {
        if (natsMessageListener.createConnection() && injectHandler != null) {
            natsMessageListener.consumeMessage(name);
        }
        return null;
    }

    /**
     *
     * Register a handler to implement injection of the retrieved message.
     *
     * @param injectHandler the injectHandler
     */
    void registerHandler(NatsInjectHandler injectHandler) {
        this.injectHandler = injectHandler;
    }

    public Properties getInboundProperties() {
        return natsProperties;
    }

    public long getScanInterval() {
        return scanInterval;
    }

    /**
     * Check if debug is enabled for logging.
     *
     * @param text log text
     */
    private void printDebugLog(String text) {
        if (log.isDebugEnabled()) {
            log.debug(text);
        }
    }
}
