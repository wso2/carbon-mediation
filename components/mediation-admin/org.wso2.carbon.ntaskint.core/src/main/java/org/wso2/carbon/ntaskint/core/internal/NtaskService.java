package org.wso2.carbon.ntaskint.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntaskint.core.TaskBuilder;

/**
 * @scr.component name="esbntask.taskservice" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 **/
public class NtaskService {
    private static final Log logger = LogFactory.getLog(NtaskService.class);

    private static TaskService taskService;

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
}
