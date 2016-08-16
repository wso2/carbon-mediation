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
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskDescription;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Provide utilities for Task Description Management
 */
public class TaskManagementHelper {

    private static final Log log = LogFactory.getLog(TaskManagementHelper.class);

    private static final String TASK_EXTENSION_NS = "http://www.wso2.org/products/wso2commons/tasks";

    private static final OMFactory FACTORY = OMAbstractFactory.getOMFactory();

    private static final OMNamespace TASK_OM_NAMESPACE = FACTORY.createOMNamespace(
            TASK_EXTENSION_NS, "task");

    /**
     * Factory method to create a Task Description from HttpServletResuet
     *
     * @param request HttpServletRequest instance
     * @return A Task Description
     * @throws ServletException Throws for any error during Task Description creation
     */
    public static TaskDescription createTaskDescription(
            HttpServletRequest request) throws ServletException {

        String name = request.getParameter("taskName");
        if (name == null || "".equals(name)) {
            name = request.getParameter("taskName_hidden");
            if (name == null || "".equals(name)) {
                handleException("Name cannot be null or empty");
            }
        }

        String group = request.getParameter("taskGroup");
        if (group == null || "".equals(group)) {
            group = request.getParameter("taskGroup_hidden");
            if (group == null || "".equals(group)) {
                handleException("Task group cannot be null or empty");
            }
        }

        String taskClass = request.getParameter("taskClass");
        if (taskClass == null || "".equals(taskClass)) {
            handleException("Task Class cannot be null or empty");
        }

        TaskDescription taskDescription = new TaskDescription();
        taskDescription.setName(name.trim());
        taskDescription.setTaskGroup(group.trim());
        taskDescription.setTaskImplClassName(taskClass.trim());

        String trigger = request.getParameter("taskTrigger");

        if (trigger != null && !"".equals(trigger)) {
            if ("simple".equals(trigger)) {


                String interval = request.getParameter("triggerInterval");
                if (interval != null && !"".equals(interval)) {
                    try {
                        taskDescription.setInterval(Long.parseLong(interval.trim()) * 1000);
                        taskDescription.setIntervalInMs(true);
                    } catch (NumberFormatException e) {
                        handleException("Invalid value for interval (Expected type is long) : " +
                                interval);
                    }

                }

                String count = request.getParameter("triggerCount");
                if (count != null && !"".equals(count)) {
                    try {
                        taskDescription.setCount(Integer.parseInt(count.trim()));
                    } catch (NumberFormatException e) {
                        handleException("Invalid value for Count (Expected type is int) : " +
                                count);
                    }
                }

            } else if ("cron".equals(trigger)) {

                String cron = request.getParameter("triggerCron");
                if (cron != null && !"".equals(cron)) {
                    taskDescription.setCronExpression(cron.trim());
                } else {
                    handleException("Cron expression cannot be empty for cron trigger");
                }
            }
        } else {
            handleException("No Trigger has been selected");
        }

        String pinnedServers = request.getParameter("pinnedServers");
        if (pinnedServers != null || !"".equals(pinnedServers)) {
            StringTokenizer st = new StringTokenizer(pinnedServers, " ,");
            List<String> pinnedServersList = new ArrayList<String>();
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.length() != 0) {
                    pinnedServersList.add(token);
                }
            }
            taskDescription.setPinnedServers(pinnedServersList);
        }

        String propertyCount = request.getParameter("propertyCount");
        if (propertyCount != null && !"".equals(propertyCount)) {

            try {

                int propCount = Integer.parseInt(propertyCount.trim());
                if (log.isDebugEnabled()) {
                    log.debug("Number of properties : " + propCount);
                }

                for (int i = 0; i < propCount; i++) {

                    String id = "property_name_hidden" + String.valueOf(i);
                    String propName = request.getParameter(id);

                    if (propName != null && !"".equals(propName)) {

                        String propertyType = request.getParameter("propertyTypeSelection" +
                                String.valueOf(i));

                        if ("literal".equals(propertyType)) {

                            String value = request.getParameter("textField" + String.valueOf(i));

                            if (log.isDebugEnabled()) {
                                log.debug("[ Property Name : " + name + " ]" +
                                        "[ Property Value : " + value + " ]");
                            }

                            if (value != null && !"".equals(value)) {

                                OMElement propElem = FACTORY.createOMElement("property",
                                        TASK_OM_NAMESPACE);
                                OMNamespace nullNS = FACTORY.createOMNamespace("", "");
                                propElem.addAttribute("name", propName.trim(), nullNS);
                                propElem.addAttribute("value", value.trim(), nullNS);
                                taskDescription.setXmlProperty(propElem);
                            }

                        } else if ("xml".equals(propertyType)) {

                            String value = request.getParameter("textArea" + String.valueOf(i));

                            if (log.isDebugEnabled()) {
                                log.debug("[ Property Name : " + name + " ][ Property Value : " +
                                        value + " ]");
                            }

                            if (value != null && !"".equals(value)) {

                                OMElement propElem = FACTORY.createOMElement("property",
                                        TASK_OM_NAMESPACE);
                                OMNamespace nullNS = FACTORY.createOMNamespace("", "");
                                propElem.addAttribute("name", propName.trim(), nullNS);
                                try {
                                    propElem.addChild(createOMElement(value.trim()));
                                } catch (Throwable e) {
                                    handleException("Invalid XML has been provided " +
                                            "for property : " + propName);
                                }
                                taskDescription.setXmlProperty(propElem);
                            }
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                handleException("Invalid number of properties " + propertyCount);
            }
        }
        return taskDescription;
    }

    private static OMElement createOMElement(String xml) throws Exception {
        return XMLUtils.toOM(getSecuredDocumentBuilder(true).
                parse(new ByteArrayInputStream(xml.getBytes())).getDocumentElement());
    }

    private static void handleException(String msg) throws ServletException {
        log.error(msg);
        throw new ServletException(msg);
    }

    public static TaskDescription getTaskDescription(HttpServletRequest request,
                                                     TaskManagementClient client,
                                                     String name, String group) throws Exception {
        if (name == null || "".equals(name)) {
            handleException("Task Name cannot be empty");
        } 
        name = name.trim();
        TaskDescription taskDescription = client.getTaskDescription(name, group);
        if (taskDescription == null) {
            handleException("No task description for name :" + name);
        }
        return taskDescription;
    }

    public static List<OMElement> mergeProperties(OMElement allPropertyElement,
                                                  Set<OMElement> propertySources) {

        List<OMElement> toBeReturn = new ArrayList<OMElement>();
        if (allPropertyElement == null || propertySources == null) {
            return toBeReturn;
        }

        Iterator propertyIterator = allPropertyElement.getChildElements();
        while (propertyIterator.hasNext()) {

            OMElement property = (OMElement) propertyIterator.next();
            if (property != null) {

                String name = property.getAttributeValue(new QName("", "name", ""));
                if (name != null && !"".equals(name)) {

                    OMElement element = getValueElement(propertySources, name);
                    if (element != null) {
                        toBeReturn.add(element.cloneOMElement());
                    } else {
                        toBeReturn.add(property.cloneOMElement());
                    }
                }
            }
        }
        return toBeReturn;
    }

    private static OMElement getValueElement(Set<OMElement> sources, String name) {

        for (OMElement element : sources) {
            if (element != null) {

                String elementName = element.getAttributeValue(new QName("", "name", ""));
                if (elementName != null && !"".equals(elementName)) {

                    if (elementName.trim().equals(name.trim())) {
                        return element;
                    }
                }

            }
        }
        return null;
    }

    /**
     * This method provides a secured document builder which will secure XXE attacks.
     *
     * @param setIgnoreComments whether to set setIgnoringComments in DocumentBuilderFactory.
     * @return DocumentBuilder
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public static DocumentBuilder getSecuredDocumentBuilder(boolean setIgnoreComments) throws
            ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringComments(setIgnoreComments);
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        org.apache.xerces.util.SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(0);
        documentBuilderFactory.setAttribute(Constants.XERCES_PROPERTY_PREFIX +
                Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                throw new SAXException("Possible XML External Entity (XXE) attack. Skipping entity resolving");
            }
        });
        return documentBuilder;
    }
}
