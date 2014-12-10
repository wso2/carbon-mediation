package org.wso2.carbon.inbound.endpoint.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.Task;
import org.wso2.carbon.mediation.clustering.ClusteringAgentUtil;
import org.wso2.carbon.utils.CarbonUtils;

public class InboundRunner implements Runnable{

    private static final Log log = LogFactory.getLog(InboundRunner.class);
    
    private Task task;
    private long interval;
    private volatile boolean execute = true;
    private volatile boolean init = false;
    
    public InboundRunner(Task task, long interval){
        this.task = task;
        this.interval = interval;
    }
    
    protected void terminate(){
        execute = false;
    }
    
    @Override
    public void run() {
        //Wait for the clustering contect to be loaded
        while(!init){
            Boolean isSinglNode = ClusteringAgentUtil.isSingleNode();
            if(isSinglNode != null){
                if(!isSinglNode && !CarbonUtils.isWorkerNode()){
                    execute = false;
                    log.info("Inbound EP ignoring in the manager node.");           
                }
                init = true;
            }            
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.warn("Unable to sleep the inbound thread for interval of : " + interval + "ms.", e);
            }
        }        
        //Run the poll cycles
        while(execute){
            try{
                task.execute();
            }catch(Exception e){
                log.error("Error executing the inbound", e);
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.warn("Unable to sleep the inbound thread for interval of : " + interval + "ms.", e);
            }
        }
    }

}
