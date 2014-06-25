package org.wso2.carbon.mediation.ntask.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mediation.ntask.TaskServiceObserver;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.apache.synapse.task.TaskStartupObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @scr.component name="esbntask.taskservice" immediate="true"
 * @scr.reference name="tasks.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 **/
public class NtaskService {
    private static final Log logger = LogFactory.getLog(NtaskService.class);

    private static final List<TaskServiceObserver> observers = new ArrayList<TaskServiceObserver>();

    private static TaskService taskService;

    private static final List<TaskStartupObserver>startupObservers = new ArrayList<TaskStartupObserver>();

    private static ConfigurationContextService ccServiceInstance;

    private static final Object lock = new Object();

    protected void activate(ComponentContext context) {
        try {
            context.getBundleContext()
                    .registerService(this.getClass().getName(), new NtaskService(), null);
            logger.debug("ntask-integration bundle is activated.");
        } catch (Throwable e) {
            logger.error("Could not activate NTaskService. Error: " + e.getMessage(), e);
        }
    }    
    
    protected void deactivate(ComponentContext context) {
        logger.debug("ntask-integration bundle is deactivated.");
    }

    protected void setTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting the Task Service [" + taskService + "].");
        }
        NtaskService.taskService = taskService;
        updateAndCleanupObservers();
        deleteInboundEndpointAllTasks();
        //notifySubjects();
    }

    protected void unsetTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting the Task Service [" + taskService + "]");
        }
        NtaskService.taskService = null;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting Configuration Context Service [" + contextService + "]");
        }
        NtaskService.ccServiceInstance = contextService;
        updateAndCleanupObservers();
    }

    private void updateAndCleanupObservers() {
        Iterator<TaskServiceObserver> i = observers.iterator();
        while (i.hasNext()) {
            TaskServiceObserver observer = i.next();
            if (observer.update(null)) {
                i.remove();
            }
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting Configuration Context Service [" + contextService + "]");
        }
        NtaskService.ccServiceInstance = null;
    }

    public static ConfigurationContextService getCcServiceInstance() {
        return NtaskService.ccServiceInstance;
    }

    public static void addObserver(TaskServiceObserver o) {
        if (observers.contains(o)) {
            return;
        }
        observers.add(o);
    }

    public static TaskService getTaskService() {
        return NtaskService.taskService;
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
