package org.wso2.carbon.ntaskint.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.core.AbstractTask;

final class Task extends AbstractTask implements org.apache.synapse.task.Task, ManagedLifecycle {
    private static final Log logger = LogFactory.getLog(Task.class.getName());
    private static final int DEFAULT_CONNECTION_TIMEOUT = 20000;

    public Task() {
        System.out.println("##################################333");
    }

    public void execute() {
        System.out.println("org.wso2.carbon.ntaskint.core.Task Executing task---------------->" + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true));
        boolean systemTask = this.isSystemTask();
        if (!systemTask) {
            this.notifyTaskManager();
        }
    }

    private boolean isSystemTask() {
        String systemTaskFlag = this.getProperties().get(TaskBuilder.SYSTEM_TASK_FLAG);
        return (systemTaskFlag != null && Boolean.parseBoolean(systemTaskFlag));
    }

    /**
     * This notifies the task manager that this task was executed at this time, this can be used
     * for monitoring, billing etc.. requirements.
     */
    private void notifyTaskManager() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String taskName = this.getProperties().get(TaskBuilder.REMOTE_TASK_NAME);
    }

    public void init(SynapseEnvironment synapseEnvironment) {
        logger.info("Initializing Task...");
    }

    public void destroy() {
        logger.info("Destroying Task...");
    }
}
