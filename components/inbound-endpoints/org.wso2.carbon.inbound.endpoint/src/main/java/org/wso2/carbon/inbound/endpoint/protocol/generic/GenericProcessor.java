package org.wso2.carbon.inbound.endpoint.protocol.generic;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.inbound.InboundProcessorParams;
import org.apache.synapse.startup.quartz.StartUpController;
import org.apache.synapse.task.Task;
import org.apache.synapse.task.TaskStartupObserver;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;
import org.wso2.carbon.inbound.endpoint.protocol.PollingConstants;

public class GenericProcessor extends InboundRequestProcessorImpl implements TaskStartupObserver {


	private GenericPollingConsumer pollingConsumer;
    private Properties properties;
    private String injectingSeq;
    private String onErrorSeq;
    private static final Log log = LogFactory.getLog(GenericProcessor.class);
    private StartUpController startUpController;
    private String classImpl;
    private boolean sequential;
    
    private static final String ENDPOINT_POSTFIX = "CLASS-EP";
    
    public GenericProcessor(String name, String classImpl, Properties properties,
            long scanInterval, String injectingSeq, String onErrorSeq,
            SynapseEnvironment synapseEnvironment, boolean coordination, boolean sequential) {
        this.name = name;
        this.properties = properties;
        this.interval = scanInterval;
        this.injectingSeq = injectingSeq;
        this.onErrorSeq = onErrorSeq;
        this.synapseEnvironment = synapseEnvironment;
        this.classImpl = classImpl;
        this.coordination = coordination;
        this.sequential = sequential;
    }

    public GenericProcessor(InboundProcessorParams params) {
        this.name = params.getName();
        this.properties = params.getProperties();
        this.interval =
                Long.parseLong(properties.getProperty(PollingConstants.INBOUND_ENDPOINT_INTERVAL));
        this.coordination = true;
        if (properties.getProperty(PollingConstants.INBOUND_COORDINATION) != null) {
            this.coordination = Boolean.parseBoolean(properties
                    .getProperty(PollingConstants.INBOUND_COORDINATION));
        }
        this.sequential = true;
        if (properties.getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL) != null) {
            this.sequential = Boolean.parseBoolean(properties
                    .getProperty(PollingConstants.INBOUND_ENDPOINT_SEQUENTIAL));
        }        
        this.injectingSeq = params.getInjectingSeq();
        this.onErrorSeq = params.getOnErrorSeq();
        this.synapseEnvironment = params.getSynapseEnvironment();
        this.classImpl = params.getClassImpl();
    }

    public void init() {
    	log.info("Inbound listener " + name + " for class " + classImpl + " starting ...");
		try{
			Class c = Class.forName(classImpl);
			Constructor cons = c.getConstructor(Properties.class, String.class, SynapseEnvironment.class, long.class, String.class, String.class, boolean.class, boolean.class);
			pollingConsumer = (GenericPollingConsumer)cons.newInstance(properties, name, synapseEnvironment, interval, injectingSeq, onErrorSeq, coordination, sequential);
		}catch(ClassNotFoundException e){
			log.error("Class " + classImpl + " not found. Please check the required class is added to the classpath.");
			log.error(e);
		}catch(NoSuchMethodException e){
			log.error("Required constructor is not implemented.");
			log.error(e);
		}catch(InvocationTargetException e){
			log.error("Unable to create the consumer");
			log.error(e);
		}catch(Exception e){
			log.error("Unable to create the consumer");
			log.error(e);					
		}    	
    	start();
    }
         
    
    public void start() {        	
        try {
        	Task task = new GenericTask(pollingConsumer);
        	start(task, ENDPOINT_POSTFIX);
        } catch (Exception e) {
            log.error("Could not start Generic Processor. Error starting up scheduler. Error: " + e.getLocalizedMessage());
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
