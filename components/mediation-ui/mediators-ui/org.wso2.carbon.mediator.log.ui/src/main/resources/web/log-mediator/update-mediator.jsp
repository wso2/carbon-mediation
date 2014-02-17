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
<%@ page import="org.wso2.carbon.mediator.log.LogMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.jaxen.JaxenException" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    MediatorProperty meditorProp = null;
    if (!(mediator instanceof LogMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    LogMediator logMediator = (LogMediator) mediator;
    logMediator.setSeparator(request.getParameter("mediator.log.log_separator"));
    logMediator.setLogLevel(Integer.parseInt(request.getParameter("mediator.log.log_level")));
    logMediator.setLogCategory(Integer.parseInt(request.getParameter("mediator.log.category")));
    logMediator.getProperties().clear(); // to avoid duplicates

    XPathFactory xPathFactory = XPathFactory.getInstance();
    String propertyCountParameter = request.getParameter("propertyCount");
    if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
        int propertyCount = 0;
        try {
            propertyCount = Integer.parseInt(propertyCountParameter.trim());
            for (int i = 0; i <= propertyCount; i++) {
                String name = request.getParameter("propertyName" + i);
                if (name != null && !"".equals(name)) {
                    String valueId = "propertyValue" + i;
                    String value = request.getParameter(valueId);
                    String expression = request.getParameter("propertyTypeSelection" + i);
                    boolean isExpression = expression != null && "expression".equals(expression.trim());
                    MediatorProperty mp = new MediatorProperty();
                    mp.setName(name.trim());
                    if (value != null) {
                        if (isExpression) {
                            if(value.trim().startsWith("json-eval(")) {
                                SynapsePath jsonPath = new SynapseJsonPath(value.trim().substring(10, value.length() - 1));
                                mp.setPathExpression(jsonPath);
                            } else {
                                mp.setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                            }
                        } else {
                            mp.setValue(value.trim());
                        }
                    }
                    logMediator.addProperty(mp);
                }
            }
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        } catch (JaxenException exception) {
            throw new RuntimeException("Invalid Path Expression");
        }
    }
%>

