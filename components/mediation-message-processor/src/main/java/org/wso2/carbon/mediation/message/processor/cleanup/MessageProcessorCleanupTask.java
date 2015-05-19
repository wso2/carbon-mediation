package org.wso2.carbon.mediation.message.processor.cleanup;

import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.impl.ScheduledMessageProcessor;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;
import org.wso2.carbon.message.processor.util.ConfigHolder;
import org.apache.synapse.message.processor.MessageProcessorCleanupService;

public class MessageProcessorCleanupTask implements MessageProcessorCleanupService {
    private static final long serialVersionUID = 1L;

    private String name;

    public Void call() throws Exception {
        /*
         * Get the message processor instance running inside this worker node
         * using an OSGI service, synapse-env, synapse-config.
         */
        SynapseEnvironmentService synEnvService =
                                                  ConfigHolder.getInstance()
                                                              .getSynapseEnvironmentService(MultitenantConstants.SUPER_TENANT_ID);
        MessageProcessor messageProcessor =
                                            synEnvService.getSynapseEnvironment()
                                                         .getSynapseConfiguration()
                                                         .getMessageProcessors().get(name);

        /*
         * Get all the message processors run in this VM and filter using the
         * name.
         * Then cleanup the resources used by that message processor.
         */
        if (messageProcessor != null && messageProcessor instanceof ScheduledMessageProcessor) {
            ((ScheduledMessageProcessor) messageProcessor).cleanupLocalResources();
        }

        return null;
    }

    public void setName(String name) {
        this.name = name;

    }

}
