/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mediation.ntask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.util.MiscellaneousUtil;
import org.apache.synapse.task.SynapseTaskException;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskManager;
import org.apache.synapse.task.TaskManagerObserver;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.mediation.ntask.internal.NtaskService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.impl.clustered.ClusteredTaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.utils.CarbonUtils;

import com.hazelcast.core.IExecutorService;

public class NTaskTaskManager implements TaskManager, TaskServiceObserver, ServerStartupHandler {
    private final Object lock = new Object();

    private static final Log logger = LogFactory.getLog(NTaskTaskManager.class.getName());

    /**
     * TODO ClusterGroupCommunicator NTASK_P2P_COMM_EXECUTOR is private in the carbon-commons-4.4.1 release, 
     * this should be changed to use ClusterGroupCommunicator.NTASK_P2P_COMM_EXECUTOR.
     */
    private static final String NTASK_P2P_COMM_EXECUTOR = "__NTASK_P2P_COMM_EXECUTOR__";

    private String name;

    private boolean initialized = false;

    private org.wso2.carbon.ntask.core.TaskManager taskManager;

    private final Map<String, Object> properties = new HashMap<String, Object>(5);

    protected final Properties configProperties = new Properties();

    private final List<TaskManagerObserver> observers = new ArrayList<TaskManagerObserver>();

    private final List<TaskDescription> taskQueue = new ArrayList<TaskDescription>();

    private final Object taskQueueLock = new Object();

    @Override
    public boolean schedule(TaskDescription taskDescription) {
		logger.debug("#schedule Scheduling task : " + taskId(taskDescription));
		TaskInfo taskInfo;
		try {
			taskInfo = TaskBuilder.buildTaskInfo(taskDescription, properties);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("#schedule Could not build task info object of task : " +taskId(taskDescription)+ ". Error: " +
				                     e.getLocalizedMessage(), e);
			}
			synchronized (lock) {
                queueTask(taskDescription);
			}
			return false;
		}
		if (!isInitialized()) {
			// if cannot schedule yet, put in the pending tasks list.
			synchronized (lock) {
				logger.debug("#schedule Added pending task : " + taskId(taskDescription));
                queueTask(taskDescription);
			}
			return false;
		}
		try {
			synchronized (lock) {
				if (taskManager == null) {
					logger.debug("#schedule Could not schedule task " + taskId(taskDescription) +
					             ". Task manager is not available.");
                    queueTask(taskDescription);
					return false;
				}
                taskManager.registerTask(taskInfo);
                taskManager.scheduleTask(taskInfo.getName());
                removeTask(taskDescription);
			}
			logger.info("Scheduled task " + taskId(taskDescription));
		} catch (Exception e) {
			logger.error("Scheduling task [" + taskId(taskDescription) + "::" +
			                     taskDescription.getTaskGroup() + "] FAILED. Error: " +
			                     e.getLocalizedMessage(), e);
			return false;
		}
		return true;
	}

    @Override
    public boolean reschedule(String taskName, TaskDescription taskDescription) {
		if (!isInitialized()) {
			return false;
		}
		try {
			synchronized (lock) {
				if (taskManager == null) {
					logger.warn("#reschedule Could not reschedule task [" + taskName +
					            "]. Task manager is not available.");
					return false;
				}
				TaskInfo taskInfo = taskManager.getTask(taskName);
				TaskDescription description = TaskBuilder.buildTaskDescription(taskInfo);
				taskInfo = TaskBuilder.buildTaskInfo(description, properties);
				taskManager.registerTask(taskInfo);
				taskManager.rescheduleTask(taskInfo.getName());
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

    @Override
    public boolean delete(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        if (taskName == null) {
            return false;
        }
        String list[] = taskName.split("::");
        String name = list[0];
        if (name == null || "".equals(name)) {
            throw new SynapseTaskException("Task name is null. ", logger);
        }

        String group = null;
        if (list.length > 1) {
            group = list[1];
        }
        if (group == null || "".equals(group)) {
            group = TaskDescription.DEFAULT_GROUP;
            if (logger.isDebugEnabled()) {
                logger.debug("#delete Task group is null or empty , using default group :"
                        + TaskDescription.DEFAULT_GROUP);
            }
        }
        try {
            boolean deleted;
            synchronized (lock) {
                if (taskManager == null) {
                    logger.warn("#delete Could not delete task [" + taskName + "]. Task manager is not available.");
                    return false;
                }
                deleted = taskManager.deleteTask(name);
                NTaskAdapter.removeProperty(taskName);
            }

            logger.debug("Deleted task [" + name + "] [" + deleted + "]");
            return deleted;
        } catch (Exception e) {
            logger.error("Cannot delete task [" + taskName + "::" + group + "]. Error: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public boolean pause(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        try {
            synchronized (lock) {
                if (taskManager == null) {
                    logger.warn("#pause Could not pause task [" + taskName + "]. Task manager is not available.");
                    return false;
                }
                taskManager.pauseTask(taskName);
            }
            return true;
        } catch (Exception e) {
            logger.error("Cannot pause task [" + taskName + "]. Error: " + e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean pauseAll() {
        if (!isInitialized()) {
            return false;
        }
        try {
            synchronized (lock) {
                if (taskManager == null) {
                    logger.warn("#pauseAll Could not pause any task. Task manager is not available.");
                    return false;
                }
                List<TaskInfo> taskList = taskManager.getAllTasks();
                for (TaskInfo taskInfo : taskList) {
                    taskManager.pauseTask(taskInfo.getName());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Cannot pause all tasks. Error: " + e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public boolean resume(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        if (taskName == null) {
            return false;
        }
        try {
            synchronized (lock) {
                if (taskManager == null) {
                    logger.warn("#resume Could not resume task [" + taskName + "]. Task manager is not available.");
                    return false;
                }
                taskManager.resumeTask(taskName);
            }
        } catch (Exception e) {
            logger.error("Cannot resume task [" + taskName + "]. Error: " + e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean resumeAll() {
        if (!isInitialized()) {
            return false;
        }
        try {
            synchronized (lock) {
                if (taskManager == null) {
                    logger.warn("#resumeAll Could not resume any task. Task manager is not available.");
                    return false;
                }
                List<TaskInfo> taskList = taskManager.getAllTasks();
                for (TaskInfo taskInfo : taskList) {
                    taskManager.resumeTask(taskInfo.getName());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Cannot resume all tasks. Error: " + e.getLocalizedMessage(), e);
        }
        return false;
    }

    @Override
    public TaskDescription getTask(String taskName) {
        if (!isInitialized()) {
            return null;
        }
        try {
            TaskInfo taskInfo;
            synchronized (lock) {
                if (taskManager == null) {
                    logger.warn("#getTask Could not retrieve task [" + taskName + "]. Task manager is not available.");
                    return null;
                }
                taskInfo = taskManager.getTask(taskName);
            }
            return TaskBuilder.buildTaskDescription(taskInfo);
        } catch (Exception e) {
            logger.error("Cannot return task [" + taskName + "]. Error: " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public String[] getTaskNames() {
        if (!isInitialized()) {
            return new String[0];
        }
        try {
            List<TaskInfo> taskList;
            synchronized (lock) {
                if (taskManager == null) {
                    logger.warn("#getTaskNames Could not query task names. Task manager is not available.");
                    return new String[0];
                }
                taskList = taskManager.getAllTasks();
            }
            List<String> result = new ArrayList<String>();
            for (TaskInfo taskInfo : taskList) {
                result.add(taskInfo.getName());
            }
            return result.toArray(new String[result.size()]);
        } catch (Exception e) {
            logger.error("Cannot return task list. Error: " + e.getLocalizedMessage(), e);
        }
        return new String[0];
    }

    @Override
    public boolean init(Properties properties) {
        synchronized (lock) {
            try {
                TaskService taskService = NtaskService.getTaskService();
                if (taskService == null || NtaskService.getCcServiceInstance() == null) {
                    NtaskService.addObserver(this);
                    return false;
                }
                if ((taskManager = getTaskManager(false)) == null) {
                    logger.debug("#init Could not initialize task manager. " + managerId());
                    return false;
                } else {
                    logger.debug("#init Obtained Carbon task manager " + managerId());
                }

                initialized = true;
                if (isTaskRunningNode()) {
                    taskService.registerTaskType(Constants.TASK_TYPE_ESB);
                    updateAndCleanupObservers();
                }

                logger.info("Initialized task manager. Tenant [" + getCurrentTenantId() + "]");
                if (logger.isDebugEnabled()) {
                    logger.debug("#init Initialized task manager : " + managerId());
                    logger.debug("#init Scheduling existing tasks if any. : " + managerId());
                }
                Object[] taskDescriptions = pendingTasks();
                for (Object d : taskDescriptions) {
                    schedule((TaskDescription) d);
                }
                return true;
            } catch (Exception e) {
                logger.error("Cannot initialize task manager. Error: " + e.getLocalizedMessage(), e);
                initialized = false;
            }
        }
        return false;
    }

    /**
     * Helper method to decide whether this node is task running node or not.
     *
     * @return true if this node supposed to run tasks, false otherwise
     */
    private boolean isTaskRunningNode() {
        boolean isStandaloneNode = NtaskService.getCcServiceInstance().getServerConfigContext()
                                           .getAxisConfiguration().getClusteringAgent() == null;
        boolean isWorkerNode = !isStandaloneNode && CarbonUtils.isWorkerNode();
        if (logger.isDebugEnabled()) {
            logger.debug("#init standalone node: [" + isStandaloneNode + "] worker node: [" + isWorkerNode + "] " + managerId());
        }
        if (isStandaloneNode || isWorkerNode) {
            return true;
        }
        /**
         * If this is not a worker node node in a cluster, then use "clusteringPattern" parameter in axis2.xml
         * clustering configs to decide whether to run tasks in this node or not, and that defaults to not
         * running tasks(defaulting to "WorkerManager" clustering pattern and this node is a manager node, hence
         * not running tasks in this node)
         */
        Parameter parameter = NtaskService.getCcServiceInstance().getServerConfigContext()
                .getAxisConfiguration().getClusteringAgent().getParameter(Constants.CLUSTERING_PATTERN);
        if (parameter == null || parameter.getValue() == null || parameter.getValue().toString().isEmpty()) {
            logger.warn("clusteringPattern parameter not configured correctly in clustering configuration, " +
                        "hence defaults to worker manager clustering pattern, and since this node is a manager node, " +
                        "skips running tasks in this node" );
            return false;
        } else if (parameter.getValue().toString().equals(Constants.CLUSTERING_PATTERN_WORKER_MANAGER)) {
            if (logger.isDebugEnabled()) {
                logger.debug("clustering pattern is worker manager clustering pattern, and this node is a " +
                             "manager node, hence skip running tasks");
            }
            return false;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Non worker manager clustering pattern mentioned, hence running tasks in this node");
            }
            return true;
        }
    }

    @Override
    public boolean update(Map<String, Object> parameters) {
        return init(parameters == null || !parameters.containsKey("init.properties") ? null
                : (Properties) parameters.get("init.properties"));
    }

    @Override
    public boolean isInitialized() {
        synchronized (lock) {
            return initialized;
        }
    }

    @Override
    public boolean start() {
        return isInitialized();
    }

    @Override
    public boolean stop() {
        // Nothing to do here.
        return true;
    }

    @Override
    public int getRunningTaskCount() {
        if (!isInitialized()) {
            return -1;
        }
        String[] names = getTaskNames();
        int count = 0;
        try {
            for (String name : names) {
                synchronized (lock) {
                    if (taskManager == null) {
                        logger.warn("#getRunningTaskCount Could not determine the number of running tasks. Task manager is not available.");
                        return -1;
                    }
                    if (taskManager.getTaskState(name)
                            .equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.NORMAL)) {
                        ++count;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot return running task count. Error: " + e.getLocalizedMessage(), e);
        }
        return count;
    }

    public List<String> getRunningTaskList() {
        if (!isInitialized()) {
            return null;
        }
        String[] names = getTaskNames();
        List<String> runningTaskList = new ArrayList<String>();
        try {
            for (String name : names) {
                synchronized (lock) {
                    if (taskManager.getTaskState(name)
                            .equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.NORMAL)) {
                        runningTaskList.add(name);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot return running task list. Error: " + e.getLocalizedMessage(), e);
        }
        return runningTaskList;
    }


    @Override
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
        synchronized (lock) {
            if (taskManager == null) {
                logger.warn("#isTaskRunning Could not determine the state of the task [" + taskName + "]. Task manager is not available.");
                return false;
            }
            try {
                return taskManager.getTaskState(taskName)
                        .equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.NORMAL);
            } catch (Exception e) {
                logger.error("Cannot return task status [" + taskName
                        + "]. Error: " + e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    @Override
    public boolean setProperties(Map<String, Object> properties) {
        if (properties == null) {
            return false;
        }
        for (String key : properties.keySet()) {
            synchronized (lock) {
                this.properties.put(key, properties.get(key));
            }
        }
        return true;
    }

    @Override
    public boolean setProperty(String name, Object property) {
        if (name == null) {
            return false;
        }
        synchronized (lock) {
            properties.put(name, property);
        }
        return true;
    }

    @Override
    public Object getProperty(String name) {
        if (name == null) {
            return null;
        }
        synchronized (lock) {
            return properties.get(name);
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProviderClass() {
        return this.getClass().getName();
    }

    @Override
    public Properties getConfigurationProperties() {
        synchronized (lock) {
            return configProperties;
        }
    }

    @Override
    public void setConfigurationProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        synchronized (lock) {
            configProperties.putAll(properties);
        }
    }

    private org.wso2.carbon.ntask.core.TaskManager getTaskManager(boolean system) throws Exception {
        TaskService taskService = NtaskService.getTaskService();
        if (taskService == null) {
            return null;
        }
        return taskService.getTaskManager(Constants.TASK_TYPE_ESB);
    }

    private int getCurrentTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    public static int tenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    @Override
    public void invoke() {
        //Initialize the Task Manager after server is started
        init(null);
    }

    private String taskId(TaskDescription description) {
        return "[NTask::" + getCurrentTenantId() + "::" + description.getName() + "]";
    }

    private String managerId() {
        return "[NTaskTaskManager::" + getCurrentTenantId() + " ::" + this.hashCode() + "]";
    }

    @Override
    public void addObserver(TaskManagerObserver observer) {
        if (observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    @Override
    public boolean isTaskDeactivated(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        synchronized (lock) {
            if (taskManager == null) {
                logger.warn("#isTaskRunning Could not determine the state of the task [" +
                            taskName + "]. Task manager is not available.");
                return false;
            }
            try {
                return taskManager.getTaskState(taskName)
                                  .equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.PAUSED);
            } catch (Exception e) {
                /*
                 * This fix was given to avoid error messages printing
                 * while server shutdowns in cluster mode when MP is running.
                 * This is related to the issue ESBJAVA-4061.
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot return task status [" + taskName + "]. Error: " +
                            e.getLocalizedMessage(), e);
                }
            }
        }
        return false;
    }

    @Override
    public boolean isTaskBlocked(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        synchronized (lock) {
            if (taskManager == null) {
                logger.warn("#isTaskRunning Could not determine the state of the task [" +
                            taskName + "]. Task manager is not available.");
                return false;
            }
            try {
                return taskManager.getTaskState(taskName)
                                  .equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.BLOCKED);
            } catch (Exception e) {
                logger.error("Cannot return task status [" + taskName + "]. Error: " +
                                     e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    @Override
    public boolean isTaskRunning(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        synchronized (lock) {
            if (taskManager == null) {
                logger.warn("#isTaskRunning Could not determine the state of the task [" +
                            taskName + "]. Task manager is not available.");
                return false;
            }
            try {
                return taskManager.getTaskState(taskName)
                                  .equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.NORMAL);
            } catch (Exception e) {
                logger.error("Cannot return task status [" + taskName + "]. Error: " +
                                     e.getLocalizedMessage(), e);
            }
        }
        return false;
    }
    

    public boolean isTaskExist(String taskName) {
        if (!isInitialized()) {
            return false;
        }
        synchronized (lock) {
            if (taskManager == null) {
                logger.warn("#isTaskExist Could not determine the state of the task [" + taskName +
                            "]. Task manager is not available.");
                return false;
            }
            try {
                return !taskManager.getTaskState(taskName)
                                   .equals(org.wso2.carbon.ntask.core.TaskManager.TaskState.NONE);
            } catch (Exception e) {
                logger.error("Cannot return task status [" + taskName + "]. Error: " +
                                     e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    private void updateAndCleanupObservers() {
        Iterator<TaskManagerObserver> iter = observers.iterator();
        while (iter.hasNext()) {
            TaskManagerObserver observer = iter.next();
            observer.update();
            iter.remove();
        }
    }

    private boolean queueTask(TaskDescription description) {
        synchronized (taskQueueLock) {
            logger.debug("#queueTask Queuing task " + taskId(description)) ;
            if (!taskQueue.contains(description)) {
                return taskQueue.add(description);
            }
        }
        return false;
    }

    private boolean removeTask(TaskDescription description) {
        synchronized (taskQueueLock) {
            logger.debug("#removeTask removing task " + taskId(description)) ;
            return taskQueue.remove(description);
        }
    }

    private Object[] pendingTasks() {
        synchronized (taskQueueLock) {
            return taskQueue.toArray();
        }
    }

    @Override
    public void sendClusterMessage(Callable<Void> callable) {
        if (taskManager instanceof ClusteredTaskManager) {
            try {
                IExecutorService executorService =
                                                   ((ClusteredTaskManager) taskManager).getClusterComm()
                                                                                       .getHazelcast()
                                                                                       .getExecutorService(NTASK_P2P_COMM_EXECUTOR);
                executorService.submitToAllMembers(callable);
            } catch (TaskException e) {
                logger.error("Can not submit a cluster message.", e);
            }
        }

    }

}

