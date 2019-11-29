package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;

import java.util.Properties;

public class NatsTask extends InboundTask {

    private static final Log logger = LogFactory.getLog(NatsTask.class.getName());

    private NatsPollingConsumer natsPollingConsumer;

    NatsTask(NatsPollingConsumer natsPollingConsumer, long interval) {
        logger.debug("Initializing.");
        this.natsPollingConsumer = natsPollingConsumer;
        this.interval = interval;
    }

    public void init(SynapseEnvironment se) {
        logger.debug("Initializing.");
    }

    public void destroy() {
        logger.debug("Destroying.");
    }

    public void taskExecute() {
        logger.debug("Executing.");
        natsPollingConsumer.execute();
    }

    @Override
    public Properties getInboundProperties() {
        return natsPollingConsumer.getInboundProperties();
    }
}
