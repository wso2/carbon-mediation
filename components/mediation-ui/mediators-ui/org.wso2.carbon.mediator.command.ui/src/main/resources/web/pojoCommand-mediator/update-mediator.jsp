<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.command.CommandMediator" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.jaxen.JaxenException" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CommandMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CommandMediator pojoCommandMediator = (CommandMediator) mediator;

    pojoCommandMediator.getContextGetterProperties().clear(); // to avoid duplicates
    pojoCommandMediator.getContextSetterProperties().clear();
    pojoCommandMediator.getMessageGetterProperties().clear();
    pojoCommandMediator.getMessageSetterProperties().clear();
    pojoCommandMediator.getStaticSetterProperties().clear();

    XPathFactory xPathFactory = XPathFactory.getInstance();
    String propertyCountParameter = request.getParameter("propertyCount");

    if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
        int propertyCount = Integer.parseInt(propertyCountParameter.trim());
        for (int i = 0; i < propertyCount; i++) {
            String propName = request.getParameter("propertyName" + i);
            String readInfoSelect = request.getParameter("propertySelectReadType" + i);
            String updateInfoSelect = request.getParameter("propertySelectUpdateType" + i);

            String staticValue = request.getParameter("mediator.command.prop.value" + i);
            SynapseXPath xPath = null;
            String contextPropName = request.getParameter("mediator.command.prop.context" + i);

            // readInfo
            if ("value".equals(readInfoSelect)) {
                pojoCommandMediator.addStaticSetterProperty(propName, staticValue);
            } else if ("message".equals(readInfoSelect)) {
                xPath = xPathFactory.createSynapseXPath("mediator.command.prop.xpath" + i,
                        request, session);
                pojoCommandMediator.addMessageSetterProperty(propName, xPath);
            } else if ("context".equals(readInfoSelect)) {
                pojoCommandMediator.addContextSetterProperty(propName, contextPropName);
            }

            // updateInfo
            if ("message".equals(updateInfoSelect)) {
                if (xPath == null) {
                    xPath = xPathFactory.createSynapseXPath("mediator.command.prop.xpath" + i,
                            request, session);
                }
                pojoCommandMediator.addMessageGetterProperty(propName, xPath);
            } else if("context".equals(updateInfoSelect)) {
                pojoCommandMediator.addContextGetterProperty(propName, contextPropName);
            }
        }
    }
%>
