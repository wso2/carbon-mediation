package org.wso2.carbon.mediation.ntask.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskStartupSubject;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mediation.ntask.TaskServiceObserver;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.apache.synapse.task.TaskStartupObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="esbntask.taskservice" immediate="true"
 * @scr.reference name="tasks.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 **/
public class NtaskService implements TaskStartupSubject {
    private static final Log logger = LogFactory.getLog(NtaskService.class);

    private static final List<TaskServiceObserver> observers = new ArrayList<TaskServiceObserver>();

    private static TaskService taskService;

    private static final List<TaskStartupObserver>startupObservers = new ArrayList<TaskStartupObserver>();
    
    protected void activate(ComponentContext context) {
        try {
            context.getBundleContext()
                    .registerService(this.getClass().getName(), new NtaskService(), null);
            if (logger.isDebugEnabled()) {
                logger.debug("ntask-integration bundle is activated ");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
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
        if (taskService != null) {
            synchronized (observers) {
                for (TaskServiceObserver o : observers) {
                    if (o.update(null)) {
                        observers.remove(o);
                    }
                }
            }
        } else {
            logger.error("Could not notify observers. TaskService is null.");
        }
        deleteInboundEndpointAllTasks();
        notifySubjects();
    }

    protected void unsetTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting the Task Service [" + taskService + "].");
        }
        NtaskService.taskService = null;
    }

    public static void addObserver(TaskServiceObserver o) {
        synchronized (observers) {
            if (observers.contains(o)) {
                return;
            }
            observers.add(o);
        }
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
    
	public static void attachObserver(TaskStartupObserver startupObserver) {
		synchronized (startupObservers) {
			startupObservers.add(startupObserver);
		}
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
		}
	}

    // TODO
    public boolean deleteInboundEndpointAllTasks() {
//        if (!isInitialized()) {
//            return false;
//        }
//        try {
//            List<TaskInfo> taskList;
//            synchronized (lock) {
//                taskList = taskManager.getAllTasks();
//            }
//            List<String> result = new ArrayList<String>();
//            for (TaskInfo taskInfo : taskList) {
//                String strTaskName = taskInfo.getName();
//                if(strTaskName.endsWith("-EP")){
//                    delete(strTaskName);
//                }
//                result.add(taskInfo.getName());
//            }
//        } catch (Exception e) {
//            logger.error("#getTaskNames() Cannot return task list. Error:" + e.getLocalizedMessage());
//            logger.error(e);
//            return false;
//        }
        return true;
    }
}
