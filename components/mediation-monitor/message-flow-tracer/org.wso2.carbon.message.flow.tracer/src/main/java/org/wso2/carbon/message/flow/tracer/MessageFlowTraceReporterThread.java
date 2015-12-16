package org.wso2.carbon.message.flow.tracer;

import org.apache.log4j.Logger;
import org.wso2.carbon.mediation.initializer.services.SynapseEnvironmentService;

public class MessageFlowTraceReporterThread extends Thread {
    private static Logger log = Logger.getLogger(MessageFlowTraceReporterThread.class);

    private boolean shutdownRequested = false;

    private boolean tracingEnabled = false;

    private MessageFlowTraceDataStore messageFlowTraceDataStore;

    /** The reference to the synapse environment service */
    private SynapseEnvironmentService synapseEnvironmentService;

    private long delay = 5 * 1000;



    /**
     * This flag will be updated according to the carbon.xml defined value, if
     * setup as 'true' the statistic collector will be disabled.
     */
    private boolean statisticsReporterDisabled =false;

    public MessageFlowTraceReporterThread(SynapseEnvironmentService synEnvSvc,
                                          MessageFlowTraceDataStore messageFlowTraceDataStore) {
        this.synapseEnvironmentService = synEnvSvc;
        this.messageFlowTraceDataStore = messageFlowTraceDataStore;
    }

    public void setDelay(long delay) {
        if (log.isDebugEnabled()) {
            log.debug("Mediation statistics reporter delay set to " + delay + " ms");
        }
        this.delay = delay;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    private void delay() {
        if (delay <= 0) {
            return;
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignore) {

        }
    }

    public void run() {
        while (!shutdownRequested) {
            try {
//                collectDataAndReport();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {

                }
            } catch (Throwable t) {
                // catch all possible errors to prevent the thread from dying
                log.error("Error while collecting and reporting mediation statistics", t);
            }
        }
    }



    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Statistics reporter thread is being stopped");
        }
        shutdownRequested = true;
    }
}
