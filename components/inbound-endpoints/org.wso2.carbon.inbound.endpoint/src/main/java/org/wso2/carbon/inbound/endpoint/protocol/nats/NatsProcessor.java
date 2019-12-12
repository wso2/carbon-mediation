package org.wso2.carbon.inbound.endpoint.protocol.nats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundTaskProcessor;
import org.apache.synapse.task.TaskStartupObserver;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.common.InboundTask;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

import java.util.Properties;

public class NatsProcessor extends InboundRequestProcessorImpl implements TaskStartupObserver, InboundTaskProcessor {

    private static final Log log = LogFactory.getLog(NatsProcessor.class.getName());

    private static final String ENDPOINT_POSTFIX = "NATS" + COMMON_ENDPOINT_POSTFIX;

    private NatsPollingConsumer pollingConsumer;
    private Properties natsProperties;
    private boolean sequential;
    private String injectingSeq;
    private String onErrorSeq;

    public NatsProcessor(InboundProcessorParams params) {
        this.name = params.getName();
        this.natsProperties = params.getProperties();

        String inboundEndpointInterval = natsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL);
        if (inboundEndpointInterval != null) {
            try {
                this.interval = Long.parseLong(inboundEndpointInterval);
            } catch (NumberFormatException nfe) {
                throw new SynapseException("Invalid numeric value for interval.", nfe);
            }
        }
        this.sequential = true;
        String inboundEndpointSequential = natsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL);
        if (inboundEndpointSequential != null) {
            this.sequential = Boolean.parseBoolean(inboundEndpointSequential);
        }
        this.coordination = true;
        String inboundCoordination = natsProperties.getProperty(PollingConstants.INBOUND_COORDINATION);
        if (inboundCoordination != null) {
            this.coordination = Boolean.parseBoolean(inboundCoordination);
        }
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
    }

    @Override public void init() {
        log.info("Initializing inbound NATS listener for inbound endpoint " + name);
        pollingConsumer = new NatsPollingConsumer(natsProperties, interval, name);
        pollingConsumer.registerHandler(new NatsInjectHandler(injectingSeq, onErrorSeq, sequential, synapseEnvironment,
                natsProperties.getProperty(NatsConstants.CONTENT_TYPE)));
        pollingConsumer.initializeMessageListener();
        start();
    }

    @Override public void destroy() {
        super.destroy();
    }

    public void start() {
        InboundTask task = new NatsTask(pollingConsumer, interval);
        start(task, ENDPOINT_POSTFIX);
    }

    public void update() {
        throw new UnsupportedOperationException();
    }

    @Override public void destroy(boolean removeTask) {
        if (removeTask) {
            destroy();
        }
    }
}
