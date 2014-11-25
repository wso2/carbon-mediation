package org.wso2.carbon.inbound.endpoint.protocol.generic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;

public class GenericTask implements org.apache.synapse.task.Task, ManagedLifecycle {
    private static final Log logger = LogFactory.getLog(GenericTask.class.getName());

    private GenericPollingConsumer pollingConsumer;
    
    public GenericTask(GenericPollingConsumer pollingConsumer) {
    	logger.debug("Generic Task initalize.");
    	this.pollingConsumer = pollingConsumer;
    }

    public void execute() {
    	logger.debug("File Task executing.");
    	pollingConsumer.poll();
    }


    public void init(SynapseEnvironment synapseEnvironment) {
        logger.debug("Initializing Task.");
    }

    public void destroy() {
        logger.debug("Destroying Task. ");
    }
}
