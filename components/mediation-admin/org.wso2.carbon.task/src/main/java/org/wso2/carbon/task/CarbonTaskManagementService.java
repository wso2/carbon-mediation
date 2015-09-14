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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.*;
import org.wso2.carbon.core.AbstractAdmin;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Web service for Task Management
 */

public class CarbonTaskManagementService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(CarbonTaskManagementService.class);

    private static final String TASK_EXTENSION_NS =
            "http://www.wso2.org/products/wso2commons/tasks";

    private static final OMFactory FACTORY = OMAbstractFactory.getOMFactory();

    private final static String COMMON_ENDPOINT_POSTFIX = "--SYNAPSE_INBOUND_ENDPOINT";
    
    private static final OMNamespace TASK_OM_NAMESPACE = FACTORY.createOMNamespace(
            TASK_EXTENSION_NS, "task");

    public CarbonTaskManagementService() {
    }

    public boolean addTaskDescription(OMElement taskElement) throws TaskManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Add TaskDescription - Get a Task configuration  :" + taskElement);
        }
        TaskDescription taskDescription = validateAndCreate(taskElement);
        if (isContains(taskDescription.getName(), taskDescription.getTaskGroup())) {
            throw new TaskManagementException("Task with name " + taskDescription.getName() +
                    " is already there.");
        }
        try {
            getTaskManager().addTaskDescription(taskDescription);
        } catch (Exception e) {
            try {
                getTaskManager().deleteTaskDescription(taskDescription.getName(),
                        taskDescription.getTaskGroup());
            } catch (Exception ignored) {
            }
            handleException("Error creating a task : " + e.getMessage(), e);            
        }
        return true;
    }

    public boolean deleteTaskDescription(String s, String group) throws TaskManagementException {

        validateName(s);
        if (log.isDebugEnabled()) {
            log.debug("Delete TaskDescription - Get a name of the TaskDescription " +
                    "to be deleted : " + s);
        }
        try {
            getTaskManager().deleteTaskDescription(s, group);
        } catch (Exception e) {
            handleException("Error deleting a task with name : " + s + " : " +
                    e.getMessage(), e);
        }
        return true;
    }

    public void editTaskDescription(OMElement taskElement) throws TaskManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Edit TaskDescription - Get a Task configuration  :" + taskElement);
        }
        try {
            getTaskManager().editTaskDescription(validateAndCreate(taskElement), getAxisConfig());
        } catch (Exception e) {
            handleException("Error editing a task : " + e.getMessage(), e);
        }
    }

    public OMElement getAllTaskDescriptions() throws TaskManagementException {

        OMElement root =
                OMAbstractFactory.getOMFactory().createOMElement(new QName(TASK_EXTENSION_NS,
                        "taskExtension", "task"));
        try {
            List<TaskDescription> descriptions = getTaskManager().getAllTaskDescriptions();
            for (TaskDescription taskDescription : descriptions) {
                if (taskDescription != null && taskDescription.getName() != null
                        && !taskDescription.getName().endsWith(COMMON_ENDPOINT_POSTFIX)) {
                    OMElement taskElement =
                            TaskDescriptionSerializer.serializeTaskDescription(TASK_OM_NAMESPACE,
                                    taskDescription);
                    validateTaskElement(taskElement);
                    root.addChild(taskElement);
                }
            }
        } catch (Exception e) {
            handleException("Error loading all tasks : " + e.getMessage(), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Returning all TaskDescriptions as  :" + root);
        }
        return root;
    }

    public OMElement getTaskDescription(String s, String group) throws TaskManagementException {

        validateName(s);
        if (log.isDebugEnabled()) {
            log.debug("Get TaskDescription - Get a name of the TaskDescription to be" +
                    " returned : " + s);
        }
        try {
            TaskDescription taskDescription = getTaskManager().getTaskDescription(s, group);
            if (taskDescription != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved a TaskDescription : " + taskDescription);
                }
                OMElement taskElement =
                        TaskDescriptionSerializer.serializeTaskDescription(TASK_OM_NAMESPACE,
                                taskDescription);
                validateTaskElement(taskElement);
                if (log.isDebugEnabled()) {
                    log.debug("Task Configuration : " + taskElement);
                }
                return taskElement;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There is no TaskDescription with name :" + s);
                }
            }
        } catch (Exception e) {
            handleException("Error loading a task with name " + s + " : " +
                    e.getMessage(), e);
        }
        return null;
    }

    public TaskData[] getAllTaskData() throws TaskManagementException {
        TaskData[] taskData = null;
        if (getTaskManager().getAllTaskData(getAxisConfig()) != null) {
            taskData = getTaskManager().getAllTaskData(getAxisConfig());
        }
        return taskData;
    }

    public boolean isContains(String s, String group) throws TaskManagementException {

        validateName(s);

        return getTaskManager().isContains(s, group);
    }

    public OMElement loadTaskClassProperties(String className,
                                             String group) throws TaskManagementException {

        validateName(className);
        try {
            List<String> names = getTaskManager().getPropertyNames(className.trim(), group);
            OMNamespace nullNS = FACTORY.createOMNamespace("", "");
            OMElement propertiesElement = FACTORY.createOMElement("properties", TASK_OM_NAMESPACE);
            if (names == null || names.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Empty Property name list for class : " + className);
                }
                return propertiesElement;
            }
            for (String name : names) {
                if (name != null && !"".equals(name)) {
                    OMElement propElem = FACTORY.createOMElement("property", TASK_OM_NAMESPACE);
                    propElem.addAttribute("name", name, nullNS);
                    propElem.addAttribute("value", "", nullNS);
                    propertiesElement.addChild(propElem);
                }

            }

            return propertiesElement;
        } catch (Exception e) {
            handleException("Error loading task implementation : " +
                    e.getMessage(), e);
            return null;
        }
    }

    public String[] getAllJobGroups() {
        List<String> strings = getTaskManager().getAllJobGroups();
        return strings.toArray(new String[strings.size()]);
    }

    //************* HELPER METHODS  ************// 

    private static TaskDescription validateAndCreate(
            OMElement taskElement) throws TaskManagementException {

        validateTaskElement(taskElement);
        TaskDescription taskDescription =
                TaskDescriptionFactory.createTaskDescription(taskElement, TASK_OM_NAMESPACE);
        validateTaskDescription(taskDescription);
        if (log.isDebugEnabled()) {
            log.debug("Task Description : " + taskDescription);
        }
        return taskDescription;
    }


    private static void validateTaskDescription(
            TaskDescription description) throws TaskManagementException {
        if (description == null) {
            handleException("Task Description can not be found.");
        }
    }

    private static void validateTaskElement(
            OMElement taskElement) throws TaskManagementException {
        if (taskElement == null) {
            handleException("Task Description OMElement can not be found.");
        }
    }

    private static void validateName(String name) throws TaskManagementException {
        if (name == null || "".equals(name)) {
            handleException("Name is null or empty");
        }
    }

    private static void handleException(String msg) throws TaskManagementException {
        log.error(msg);
        throw new TaskManagementException(msg);
    }

    private static void handleException(String msg, Exception e) throws TaskManagementException {
        log.error(msg, e);
        throw new TaskManagementException(msg, e);
    }

    private synchronized TaskManager getTaskManager() {
        return (TaskManager) getConfigContext().getProperty(
                TaskManager.CARBON_TASK_MANAGER);
    }
}

