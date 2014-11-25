/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.inbound.endpoint.protocol.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.inbound.InboundRequestProcessor;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.Task;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskStartupObserver;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;


import java.util.Properties;

public class VFSProcessor implements InboundRequestProcessor, TaskStartupObserver {

	private FilePollingConsumer fileScanner;
    private String name;
    private Properties vfsProperties;
    private long interval;
    private String injectingSeq;
    private String onErrorSeq;
    private SynapseEnvironment synapseEnvironment;
    private static final Log log = LogFactory.getLog(VFSProcessor.class);
    private StartUpController startUpController;

    public VFSProcessor(InboundProcessorParams params) {
        this.name = params.getName();
        this.vfsProperties = params.getProperties();
        this.interval =
                Long.parseLong(vfsProperties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL));
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
    }

    public void init() {
    	log.info("Inbound file listener " + name + " starting ...");
    	fileScanner = new FilePollingConsumer(vfsProperties, name, synapseEnvironment, interval);
    	fileScanner.registerHandler(new FileInjectHandler(injectingSeq, onErrorSeq, synapseEnvironment, vfsProperties));
    	start();
    }
    
    public void destroy() {
        log.info("Inbound file listener " + name + " stoped.");
        startUpController.destroy();
    }
      
    /**
     * Register/start the schedule service
     * */
    public void start() {        	
        try {
        	Task task = new FileTask(fileScanner);
        	TaskDescription taskDescription = new TaskDescription();
        	taskDescription.setName(name + "-FILE-EP");
        	taskDescription.setTaskGroup("FILE-EP");
        	taskDescription.setInterval(interval);
        	taskDescription.setIntervalInMs(true);
        	taskDescription.addResource(TaskDescription.INSTANCE, task);
        	taskDescription.addResource(TaskDescription.CLASSNAME, task.getClass().getName());
        	startUpController = new StartUpController();
        	startUpController.setTaskDescription(taskDescription);
        	startUpController.init(synapseEnvironment);

        } catch (Exception e) {
            log.error("Could not start File Processor. Error starting up scheduler. Error: " + e.getLocalizedMessage());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public void update() {
		start();
	}
}


