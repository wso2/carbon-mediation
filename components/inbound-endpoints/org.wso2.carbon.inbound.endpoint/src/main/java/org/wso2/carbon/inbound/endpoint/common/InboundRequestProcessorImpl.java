package org.wso2.carbon.inbound.endpoint.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.Task;
import org.apache.synapse.task.TaskDescription;
import org.wso2.carbon.utils.CarbonUtils;

public abstract class InboundRequestProcessorImpl implements InboundRequestProcessor {

    private static final Log log = LogFactory.getLog(InboundRequestProcessorImpl.class);

    protected StartUpController startUpController;
    protected SynapseEnvironment synapseEnvironment;
    protected long interval;
    protected String name;
    protected boolean coordination;
    
    private InboundRunner inboundRunner;    
    private Thread runningThread;
    
    protected void start(Task task, String endpointPostfix) {
        if(coordination){
            try {
                TaskDescription taskDescription = new TaskDescription();
                taskDescription.setName(name + "-" + endpointPostfix);
                taskDescription.setTaskGroup(endpointPostfix);
                taskDescription.setInterval(interval);
                taskDescription.setIntervalInMs(true);
                taskDescription.addResource(TaskDescription.INSTANCE, task);
                taskDescription.addResource(TaskDescription.CLASSNAME, task.getClass().getName());
                startUpController = new StartUpController();
                startUpController.setTaskDescription(taskDescription);
                startUpController.init(synapseEnvironment);

            } catch (Exception e) {
                log.error("Could not start the Processor. (" + endpointPostfix
                        + ") Error starting up scheduler. Error: " + e.getLocalizedMessage());
            }
        } else {
            inboundRunner = new InboundRunner(task, interval);
            runningThread = new Thread(inboundRunner);
            runningThread.start();
        }
    }
    
    public void destroy() {
        log.info("Inbound listener " + name + " stoped.");
        if(startUpController != null){
            startUpController.destroy();
        }else if(runningThread != null){
            inboundRunner.terminate();
            try {
                runningThread.join();
            } catch (InterruptedException e) {
                log.error("Error while stopping the inbound thread.");
            }
        }
    }
}
