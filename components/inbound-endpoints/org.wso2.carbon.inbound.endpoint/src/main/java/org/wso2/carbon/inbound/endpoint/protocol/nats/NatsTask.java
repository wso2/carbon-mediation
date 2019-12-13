package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;

import java.util.Properties;

public class NatsTask extends InboundTask {

    private static final Log log = LogFactory.getLog(NatsTask.class.getName());
    private NatsPollingConsumer natsPollingConsumer;

    NatsTask(NatsPollingConsumer natsPollingConsumer, long interval) {
        printDebugLog("Initializing.");
        this.natsPollingConsumer = natsPollingConsumer;
        this.interval = interval;
    }

    public void init(SynapseEnvironment se) {
        printDebugLog("Initializing.");
    }

    public void destroy() {
        printDebugLog("Destroying.");
    }

    public void taskExecute() {
        printDebugLog("Executing.");
        natsPollingConsumer.execute();
    }

    @Override
    public Properties getInboundProperties() {
        return natsPollingConsumer.getInboundProperties();
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
