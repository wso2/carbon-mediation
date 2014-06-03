/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskDescriptionRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Central controller for do Task management
 * Track 'TaskManagementService' service implementations
 * and delete management operation to them as appropriately
 */

public class TaskManager {

    private static final Log log = LogFactory.getLog(TaskManager.class);

    public static final String CARBON_TASK_MANAGER = "CARBON_TASK_MANAGER";

    public static final String CARBON_TASK_REPOSITORY = "CARBON_TASK_REPOSITORY";

    public static final String CARBON_TASK_JOB_METADATA_SERVICE = "CARBON_TASK_JOB_METADATA_SERVICE";

    public static final String CARBON_TASK_MANAGEMENT_SERVICE = "CARBON_TASK_MANAGEMENT_SERVICE";
    public static final String CARBON_TASK_SCHEDULER = "CARBON_TASK_SCHEDULER";    

    private JobMetaDataProviderServiceHandler jobMetaDataProviderServiceHandler;
    private TaskManagementServiceHandler taskManagementServiceHandler;
    private boolean initialized = false;
    private TaskDescriptionRepository repository;

    public TaskManager() {
    }

    public void init(JobMetaDataProviderServiceHandler jobMetaDataProviderServiceHandler,
                     TaskManagementServiceHandler taskManagementServiceHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Initiating a TaskManager");
        }
        this.jobMetaDataProviderServiceHandler = jobMetaDataProviderServiceHandler;
        this.taskManagementServiceHandler = taskManagementServiceHandler;
        this.initialized = true;

    }

    public void shutDown() {

        if (!initialized) {
            if (log.isDebugEnabled()) {
                log.debug("TaskManager has not been initilized.");
            }
            return;
        }

        this.initialized = false;
    }

    /**
     * Adding a TaskDescription
     * Delegates adding responsibility to underlying  each 'TaskManagementService' service
     *
     * @param taskDescription TaskDescription instance
     */
    public void addTaskDescription(TaskDescription taskDescription) {
        assetInitialized();
        String className =
                jobMetaDataProviderServiceHandler.getTaskManagementServiceImplementer(
                        taskDescription.getTaskGroup());
        if (className != null && !"".equals(className)) {
            taskManagementServiceHandler.addTaskDescription(taskDescription, className);
        }
    }

    /**
     * Deleting a TaskDescription
     * Delegates deleting responsibility to each 'TaskManagementService' service
     *
     * @param name  Name of the TaskDescription instance  to be deleted
     * @param group group of the task
     */
    public void deleteTaskDescription(String name, String group) {
        assetInitialized();
        String className =
                jobMetaDataProviderServiceHandler.getTaskManagementServiceImplementer(
                        group);
        if (className != null && !"".equals(className)) {
            taskManagementServiceHandler.deleteTaskDescription(name, className);
        }
    }

    /**
     * Editing a TaskDescription
     * Delegates Editing responsibility to each 'TaskManagementService' service
     *
     * @param taskDescription TaskDescription instance
     */
    public void editTaskDescription(TaskDescription taskDescription) {
        assetInitialized();
        String className =
                jobMetaDataProviderServiceHandler.getTaskManagementServiceImplementer(
                        taskDescription.getTaskGroup());
        if (className != null && !"".equals(className)) {
            taskManagementServiceHandler.editTaskDescription(taskDescription, className);
        }
    }

    /**
     * Get all TaskDescriptions across all 'TaskManagementService' service
     *
     * @return List of TaskDescription instances
     */
    public List<TaskDescription> getAllTaskDescriptions() {
        assetInitialized();
        List<TaskDescription> taskDescriptions = new ArrayList<TaskDescription>();
        if (repository == null) {
            return taskDescriptions;
        }
        Iterator<TaskDescription> iterator = repository.getAllTaskDescriptions();
        while (iterator.hasNext()) {
            TaskDescription taskDescription = iterator.next();
            if (taskDescription != null) {
                taskDescriptions.add(taskDescription);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("All available Task based Startup " + taskDescriptions);
        }
        return taskDescriptions;
    }

    /**
     * Looking up a TaskDescription
     * Delegates Looking up responsibility to each 'TaskManagementService' service
     *
     * @param name  Name of the TaskDescription to be returned
     * @param group group of the task
     * @return taskDescription TaskDescription instance
     */
    public TaskDescription getTaskDescription(String name, String group) {
        assetInitialized();
        String className =
                jobMetaDataProviderServiceHandler.getTaskManagementServiceImplementer(
                        group);
        if (className != null && !"".equals(className)) {
            return taskManagementServiceHandler.getTaskDescription(name, className);
        }
        return null;
    }

    /**
     * Explicit check for a TaskDescription with a particular name
     * Delegates responsibility to each 'TaskManagementService' service until any one find a TaskDescription
     * with the given name
     *
     * @param name  Name of the TaskDescription
     * @param group group of the task
     * @return true if there is a task description with given name
     */
    public boolean isContains(String name, String group) {
        assetInitialized();
        String className =
                jobMetaDataProviderServiceHandler.getTaskManagementServiceImplementer(
                        group);
        return className != null && !"".equals(className)
                && taskManagementServiceHandler.isContains(name, className);
    }

    public List<String> getPropertyNames(String taskClass, String group) {
        assetInitialized();
        String className =
                jobMetaDataProviderServiceHandler.getTaskManagementServiceImplementer(
                        group);
        if (className != null && !"".equals(className)) {
            return taskManagementServiceHandler.getPropertyNames(taskClass, className);
        }
        if (log.isDebugEnabled()) {
            log.debug("Cannot find a property name list of class : " + taskClass);
        }

        return new ArrayList<String>();
    }


    public List<String> getAllJobGroups() {
        assetInitialized();
        return jobMetaDataProviderServiceHandler.getJobGroups();
    }

    public void setTaskDescriptionRepository(TaskDescriptionRepository repository) {
        this.repository = repository;
    }

    private void assetInitialized() {
        if (!initialized) {
            String msg = "TaskManager has not been initilized." +
                    "Both of JobMetaDataProviderService and TaskManagementService should be " +
                    "provided to initiate the Task Manager";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
    }
}
