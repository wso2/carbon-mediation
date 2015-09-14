/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.task.ui.internal;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskDescriptionFactory;
import org.apache.synapse.task.TaskDescriptionSerializer;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.task.stub.TaskAdminStub;
import org.wso2.carbon.task.stub.TaskManagementException;
import org.wso2.carbon.task.stub.types.carbon.TaskData;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 *
 */
public class TaskManagementClient {

    private static final Log log = LogFactory.getLog(TaskManagementClient.class);


    private static final String TASK_EXTENSION_NS = "http://www.wso2.org/products/wso2commons/tasks";

    private static final OMNamespace TASK_OM_NAMESPACE =
            OMAbstractFactory.getOMFactory().createOMNamespace(TASK_EXTENSION_NS, "task");

    private static final QName ROOT_QNAME = new QName(TASK_EXTENSION_NS, "taskExtension", "task");


    private TaskAdminStub stub;

    private TaskManagementClient(String cookie,
                                 String backendServerURL,
                                 ConfigurationContext configCtx)
            throws AxisFault, TaskManagementException {

        String serviceURL = backendServerURL + "TaskAdmin";
        stub = new TaskAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }

    public static TaskManagementClient getInstance(ServletConfig config,
                                                   HttpSession session)
            throws TaskManagementException, AxisFault {

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new TaskManagementClient(cookie, backendServerURL, configContext);
    }

    public ResponseInformation addTaskDescription(TaskDescription taskDescription) {

        validateTaskDescription(taskDescription);
        if (log.isDebugEnabled()) {
            log.debug("Going to add TaskDescription :" + taskDescription);
        }

        OMElement taskElement = TaskDescriptionSerializer.serializeTaskDescription(
                TASK_OM_NAMESPACE, taskDescription);
        validateTaskElement(taskElement);
        if (log.isDebugEnabled()) {
            log.debug("TasK configuration :" + taskElement);
        }
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.addTaskDescription(taskElement);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public ResponseInformation deleteTaskDescription(String name, String group) {

        validateName(name);
        validateGroup(group);
        if (log.isDebugEnabled()) {
            log.debug("Going to delete a TaskDescription with name : " + name);
        }
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.deleteTaskDescription(name, group);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;

    }

    public ResponseInformation editTaskDescription(TaskDescription taskDescription) {

        validateTaskDescription(taskDescription);
        if (log.isDebugEnabled()) {
            log.debug("Going to Edit TaskDescription :" + taskDescription);
        }
        OMElement taskElement = TaskDescriptionSerializer.serializeTaskDescription(
                TASK_OM_NAMESPACE, taskDescription);
        validateTaskElement(taskElement);
        if (log.isDebugEnabled()) {
            log.debug("TasK configuration :" + taskElement);
        }
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            stub.editTaskDescription(taskElement);
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;
    }

    public List<TaskDescription> getAllTaskDescriptions() throws Exception {

        OMElement element = stub.getAllTaskDescriptions(null);
        if (log.isDebugEnabled()) {
            log.debug("All TasKs configurations :" + element);
        }
        List<TaskDescription> descriptions = new ArrayList<TaskDescription>();
        if (element == null) {
            return descriptions;
        }

        OMElement taskRoot = element.getFirstChildWithName(ROOT_QNAME);
        if (taskRoot == null) {
            return descriptions;
        }
        Iterator iterator = taskRoot.getChildElements();
        while (iterator.hasNext()) {
            OMElement taskElement = (OMElement) iterator.next();
            if (taskElement != null) {
                TaskDescription taskDescription = TaskDescriptionFactory.createTaskDescription(
                        taskElement, TASK_OM_NAMESPACE);
                if (taskDescription != null) {
                    descriptions.add(taskDescription);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("All TasKs Descriptions :" + descriptions);
        }
        return descriptions;
    }

    public TaskData[] getAllTaskData() throws Exception {
        TaskData[] taskData = null;
        try {
            taskData = stub.getAllTaskData();
        } catch (Exception e) {
            handleException(e.getLocalizedMessage());
        }
        return taskData;
    }

    public TaskDescription getTaskDescription(String name, String group) throws Exception {

        validateName(name);
        validateGroup(group);
        if (log.isDebugEnabled()) {
            log.debug("Going to retrieve a Task Description for give name :" + name);
        }

        OMElement returnElement = stub.getTaskDescription(name, group);
        validateTaskElement(returnElement);

        OMElement taskElement = returnElement.getFirstElement();
        validateTaskElement(taskElement);

        if (log.isDebugEnabled()) {
            log.debug("Retrieved Task Configuration : " + returnElement);
        }
        TaskDescription taskDescription = TaskDescriptionFactory.createTaskDescription(taskElement,
                TASK_OM_NAMESPACE);
        if (log.isDebugEnabled()) {
            log.debug("Retrieved Task : " + taskDescription);
        }
        validateTaskDescription(taskDescription);
        long interval = taskDescription.getInterval();
        if (taskDescription.getIntervalInMs()) {
            interval = interval / 1000;
        }
        taskDescription.setInterval(interval);
        taskDescription.setIntervalInMs(false);
        return taskDescription;
    }

    public boolean isContains(String name, String group) throws Exception {

        validateName(name);
        validateGroup(group);
        return stub.isContains(name, group);
    }

    public ResponseInformation loadTaskProperties(String className, String group) {

        validateName(className);
        validateGroup(group);
        if (log.isDebugEnabled()) {
            log.debug("Going to retrieve properties of the Task implementation : " + className);
        }
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            OMElement returnElement = (OMElement) stub.loadTaskClassProperties(className.trim(), group);
            if (log.isDebugEnabled()) {
                log.debug("Loaded class properties as XML : " + returnElement);
            }
            if (returnElement != null) {
                OMElement properties =
                        returnElement.getFirstChildWithName(new QName(TASK_EXTENSION_NS,
                                "properties", "task"));
                if (properties != null) {
                    if (log.isDebugEnabled()) {
                        log.debug(" Returning properties : " + properties);
                    }
                    responseInformation.setResult(properties);
                }
            }
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;

    }

    public ResponseInformation getAllJobGroups() {

        if (log.isDebugEnabled()) {
            log.debug("Going to retrieve all JOBGroup names");
        }
        ResponseInformation responseInformation = new ResponseInformation();
        try {
            String[] jobGroups = stub.getAllJobGroups();
            if (jobGroups != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Loaded JOBGroup names : " + Arrays.toString(jobGroups));
                }
                responseInformation.setResult(jobGroups);
            }
        } catch (Exception e) {
            responseInformation.setFault(true);
            responseInformation.setMessage(e.getMessage());
        }
        return responseInformation;

    }

    private static void validateTaskDescription(TaskDescription description) {

        if (description == null) {
            handleException("Task Description can not be found.");
        }
    }

    private static void validateTaskElement(OMElement taskElement) {

        if (taskElement == null) {
            handleException("Task Description OMElement can not be found.");
        }
    }

    private static void validateName(String name) {
        if (name == null || "".equals(name)) {
            handleException("Name is null or empty");
        }
    }

    private static void validateGroup(String name) {
        if (name == null || "".equals(name)) {
            handleException("Group is null or empty");
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }
}
