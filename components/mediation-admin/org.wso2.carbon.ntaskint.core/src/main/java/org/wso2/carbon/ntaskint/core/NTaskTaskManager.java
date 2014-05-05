package org.wso2.carbon.ntaskint.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.SynapseTaskException;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskManager;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntaskint.core.internal.NtaskService;

import java.util.*;

public class NTaskTaskManager implements TaskManager {
    private String name;

    private boolean initialized = false;

    private org.wso2.carbon.ntask.core.TaskManager taskManager;

    private Properties tmproperties;

    private static final Log logger = LogFactory.getLog(NTaskTaskManager.class.getName());

    private Map<String, Object> properties = new HashMap<String, Object>(5);

    public boolean schedule(TaskDescription taskDescription) {
        TaskInfo taskInfo;
        try {
            taskInfo = TaskBuilder.buildTaskInfo(taskDescription, properties);
        } catch (Exception e) {
            return false;
        }

        if (!isInitialized()) {
            return false;
        }
        try {
            if (taskDescription.getReceiverType() == TaskDescription.NOT_SET) {
                //logger.error("#schedule() Cannot schedule task [" + taskDescription.getName() + "]. Error: Receiver of the task not set.");
                //return false;
            } else if (taskDescription.getReceiverType() == TaskDescription.RECIPE) {
                String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                if (tenantDomain != null && !tenantDomain.equals("")
                        && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    //String tenantApiContext = apiData.getContext();
                    //apiData.setContext(TENANT_DELIMITER + tenantDomain + tenantApiContext);
                }
            }
            taskManager.registerTask(taskInfo);
            taskManager.scheduleTask(taskInfo.getName());
            if (logger.isDebugEnabled()) {
                logger.debug("#schedule() Scheduled task [" + taskInfo.getName() + "] SUCCESSFUL.");
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("#schedule() Scheduled task [" + taskDescription.getName() + "] FAILED. Error:" + e.getLocalizedMessage());
                logger.error(e);
            }
            return false;
        }
        return true;
    }

    public boolean reschedule(String taskName, TaskDescription taskDescription) {
        if (!isInitialized()) {
            return false;
        }
        try {
            TaskInfo taskInfo = taskManager.getTask(taskName);
            TaskDescription description = TaskBuilder.buildTaskDescription(taskInfo);
            taskInfo = TaskBuilder.buildTaskInfo(description, properties);
            taskManager.registerTask(taskInfo);
            taskManager.rescheduleTask(taskInfo.getName());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean delete(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        if (taskName == null) {
            return false;
        }
        String list[] = taskName.split("::");
        String name = list[0];
        String group = list[1];
        if (name == null || "".equals(name)) {
            throw new SynapseTaskException("Task Name can not be null", logger);
        }
        if (group == null || "".equals(group)) {
            group = TaskDescription.DEFAULT_GROUP;
            if (logger.isDebugEnabled()) {
                logger.debug("Task group is null or empty , using default group :"
                        + TaskDescription.DEFAULT_GROUP);
            }
        }
        try {
            boolean deleted = taskManager.deleteTask(name);
            if (deleted) {
                NTaskAdapter.removeProperty(taskName);
            }
            return deleted;
        } catch (Exception e) {
            logger.error("#delete() Cannot delete task [" + taskName + "]. Error:" + e.getLocalizedMessage());
            logger.error(e);
            return false;
        }
    }

    public boolean pause(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        try {
            taskManager.pauseTask(taskName);
            return true;
        } catch (Exception e) {
            logger.error("#pause() Cannot pause task [" + taskName + "]. Error:" + e.getLocalizedMessage());
            logger.error(e);
        }
        return false;
    }

    public boolean pauseAll() {
        if (!isInitialized()) {
            return false;
        }
        try {
            List<TaskInfo> taskList = taskManager.getAllTasks();
            for (TaskInfo taskInfo : taskList) {
                taskManager.pauseTask(taskInfo.getName());
            }
            return true;
        } catch (Exception e) {
            logger.error("#pauseAll() Cannot pause all tasks. Error:" + e.getLocalizedMessage());
            logger.error(e);
        }
        return false;
    }

    public boolean resume(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        if (taskName == null) {
            return false;
        }
        try {
            taskManager.resumeTask(taskName);
        } catch (Exception e) {
            logger.error("#resume() Cannot resume task [" + taskName + "]. Error:" + e.getLocalizedMessage());
            logger.error(e);
            return false;
        }
        return true;
    }

    public boolean resumeAll() {
        if (!isInitialized()) {
            return false;
        }
        try {
            List<TaskInfo> taskList = taskManager.getAllTasks();
            for (TaskInfo taskInfo : taskList) {
                taskManager.resumeTask(taskInfo.getName());
            }
            return true;
        } catch (Exception e) {
            logger.error("#resumeAll() Cannot resume all tasks. Error:" + e.getLocalizedMessage());
            logger.error(e);
        }
        return false;
    }

    public TaskDescription getTask(String taskName) {
        if (!isInitialized()) {
            return null;
        }
        try {
            TaskInfo taskInfo = taskManager.getTask(taskName);
            return TaskBuilder.buildTaskDescription(taskInfo);
        } catch (Exception e) {
            logger.error("#getTask() Cannot return task [" + taskName + "]. Error:" + e.getLocalizedMessage());
            logger.error(e);
            return null;
        }
    }

    public String[] getTaskNames() {
        if (!isInitialized()) {
            return new String[0];
        }
        try {
            List<TaskInfo> taskList = taskManager.getAllTasks();
            List<String> result = new ArrayList<String>();
            for (TaskInfo taskInfo : taskList) {
                result.add(taskInfo.getName());
            }
            return result.toArray(new String[result.size()]);
        } catch (Exception e) {
            logger.error("#getTaskNames() Cannot return task list. Error:" + e.getLocalizedMessage());
            logger.error(e);
        }
        return new String[0];
    }

    public boolean init(Properties properties) {
        try {
            if (this.tmproperties == null && properties != null) {
                this.tmproperties = properties;
            }
            taskManager = this.getTaskManager(false);
            if (taskManager == null) {
                return false;
            }
            taskManager.initStartupTasks();
            initialized = true;
            return true;
        } catch (Exception e) {
            logger.error("#init() Cannot initialize task manager. Error:" + e.getLocalizedMessage());
            logger.error(e);
            initialized = false;
        }
        return false;
    }

    public boolean isInitialized() {
        long duration = 100;
        return initialized || init(null);
    }

    public boolean start() {
        if (!isInitialized()) {
            return false;
        }
        return true;
    }

    public boolean stop() {
        if (!isInitialized()) {
            return false;
        }
        return false;
    }

    public int getRunningTaskCount() {
        if (!isInitialized()) {
            return -1;
        }
        String[] names = getTaskNames();
        int count = 0;
        try {
            for (String name : names) {
                if (taskManager.getTaskState(name).equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.NORMAL)) {
                    ++count;
                }
            }
        } catch (Exception e) {
            logger.error("#getRunningTaskCount() Cannot return running task count. Error:" + e.getLocalizedMessage());
            logger.error(e);
        }
        return count;
    }

    public boolean isTaskRunning(Object o) {
        if (!isInitialized()) {
            return false;
        }
        String taskName;
        if (o instanceof String) {
            taskName = (String) o;
        } else {
            return false;
        }
        try {
            return taskManager.getTaskState(taskName).equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.NORMAL);
        } catch (Exception e) {
            logger.error("#isTaskRunning() Cannot return task status [" + taskName + "]. Error:" + e.getLocalizedMessage());
            logger.error(e);
        }
        return false;
    }

    public boolean setProperties(Map<String, Object> properties) {
        if (properties == null) {
            return false;
        }
        Iterator i = properties.keySet().iterator();
        while (i.hasNext()) {
            String k = (String) i.next();
            Object v = properties.get(k);
            this.properties.put(k, v);
        }
        return true;
    }

    public boolean setProperty(String name, Object property) {
        if (name == null) {
            return false;
        }
        properties.put(name, property);
        return true;
    }

    public Object getProperty(String name) {
        if (name == null) {
            return null;
        }
        return properties.get(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getProviderClass() {
        return this.getClass().getName();
    }
    protected final Properties configProperties = new Properties();

    public Properties getConfigurationProperties() {
        return configProperties;
    }

    public void setConfigurationProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        configProperties.putAll(properties);
    }

    private org.wso2.carbon.ntask.core.TaskManager getTaskManager(boolean system) throws Exception {
        TaskService taskService = NtaskService.getTaskService();
        if (taskService == null) {
            return null;
        }
        return taskService.getTaskManager(
                system ? TaskBuilder.TASK_TYPE_SYSTEM :
                        TaskBuilder.TASK_TYPE_USER);
    }

    private int getCurrentTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private void checkSystemRequest() throws Exception {
        if (this.getCurrentTenantId() != MultitenantConstants.SUPER_TENANT_ID) {
            throw new Exception("System request verification failed, " +
                            "only Super-Tenant can make this type of requests");
        }
    }
}

