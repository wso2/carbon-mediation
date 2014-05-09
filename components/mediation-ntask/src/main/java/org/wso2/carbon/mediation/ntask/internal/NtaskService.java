package org.wso2.carbon.mediation.ntask.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.mediation.ntask.TaskBuilder;
import org.apache.synapse.task.TaskStartupSubject;
import org.apache.synapse.task.TaskStartupObserver;


/**
 * @scr.component name="esbntask.taskservice" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 **/
public class NtaskService implements TaskStartupSubject{
    private static final Log logger = LogFactory.getLog(NtaskService.class);

    private static TaskService taskService;
    private static List<TaskStartupObserver>startupObservers = new ArrayList<TaskStartupObserver>();
    
    protected void activate(ComponentContext context) {
        try {
            /* register the remote-tasks task types */
            getTaskService().registerTaskType(TaskBuilder.TASK_TYPE_USER);
            getTaskService().registerTaskType(TaskBuilder.TASK_TYPE_SYSTEM);
            if (logger.isDebugEnabled()) {
                logger.debug("ntask-integration bundle is activated ");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            /* don't throw exception */
        }
    }    
    
    protected void deactivate(ComponentContext context) {
        logger.debug("ntask-integration bundle is deactivated ");
    }

    protected void setTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting the Task Service [" + taskService + "].");
        }
        NtaskService.taskService = taskService;
        notifySubjects();
    }

    protected void unsetTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting the Task Service [" + taskService + "].");
        }
        NtaskService.taskService = null;
    }

    public static TaskService getTaskService() {
        return NtaskService.taskService;
    }

    public static TaskService getTaskService(TaskStartupObserver startupObserver) {
    	if(NtaskService.taskService == null && startupObserver != null){
    		synchronized (startupObservers) {
    			startupObservers.add(startupObserver);
    		}
    	}
        return NtaskService.taskService;
    }

	@Override
	public void attach(TaskStartupObserver startupObserver) {
		synchronized (startupObservers) {
			startupObservers.add(startupObserver);
		}
	}

	@Override
	public void notifySubjects() {
		synchronized (startupObservers) {
			for(TaskStartupObserver inboundObserver:startupObservers){
				inboundObserver.update();
			} 
			startupObservers.clear();
			startupObservers = null;
		}
	}
}
