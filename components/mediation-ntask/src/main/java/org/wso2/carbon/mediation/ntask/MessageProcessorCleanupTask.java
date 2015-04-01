package org.wso2.carbon.mediation.ntask;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.synapse.message.processor.MessageProcessor;
import org.wso2.carbon.mediation.ntask.internal.SynapseEnvironmentServiceTracker;

public class MessageProcessorCleanupTask implements Callable<Void>, Serializable {
	private static final long serialVersionUID = 1L;
	private final String name;

	public MessageProcessorCleanupTask(String name) {
		this.name = name;
	}

	@Override
	public Void call() throws Exception {
		// TODO: How to get the message processor instance running inside this
		// worker node using an OSGI service/ synapse-env/ synapse-config.
		MessageProcessor messageProcessor =
		                                    SynapseEnvironmentServiceTracker.getSynapseEnvironmentService()
		                                                                    .getSynapseEnvironment()
		                                                                    .getSynapseConfiguration()
		                                                                    .getMessageProcessors()
		                                                                    .get(name);
		/*
		 * Get all the message processors run in this VM and filter using the
		 * name.
		 * Then deactivate that message processor and cleanup the resources.
		 */
		if (messageProcessor != null) {
			messageProcessor.deactivate();
		}

		return null;
	}

}
