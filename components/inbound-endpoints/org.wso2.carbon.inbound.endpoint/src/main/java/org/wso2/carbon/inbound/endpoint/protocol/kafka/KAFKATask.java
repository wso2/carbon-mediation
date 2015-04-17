package org.wso2.carbon.inbound.endpoint.protocol.kafka;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;

public class KAFKATask implements org.apache.synapse.task.Task,
        ManagedLifecycle {
    private static final Log logger = LogFactory.getLog(KAFKATask.class
            .getName());
    private KAFKAPollingConsumer kafkaPollingConsumer;

    public KAFKATask(KAFKAPollingConsumer kafkaPollingConsumer) {
        logger.debug("Initializing.");
        this.kafkaPollingConsumer = kafkaPollingConsumer;
    }

    public void init(SynapseEnvironment se) {
        logger.debug("Initializing.");
    }

    public void destroy() {
        logger.debug("Destroying.");
    }

    public void execute() {
        logger.debug("Executing.");
        kafkaPollingConsumer.execute();
    }
}
