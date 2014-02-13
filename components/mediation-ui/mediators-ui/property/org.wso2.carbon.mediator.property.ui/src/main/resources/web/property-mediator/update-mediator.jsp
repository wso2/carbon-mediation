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

<%@ page import="org.wso2.carbon.mediator.property.PropertyMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.util.AXIOMUtils" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String uri = "", prefix = "";
    if (!(mediator instanceof PropertyMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PropertyMediator propertyMediator = (org.wso2.carbon.mediator.property.PropertyMediator) mediator;

    propertyMediator.setName(request.getParameter("mediator.property.name"));
    propertyMediator.setAction(Integer.valueOf(request.getParameter("mediator.property.action")));
    propertyMediator.setScope(request.getParameter("mediator.property.scope"));    
    propertyMediator.setValue(null);
    propertyMediator.setExpression(null);
    if (propertyMediator.getAction() == org.apache.synapse.mediators.builtin.PropertyMediator.ACTION_SET) {
        String type = request.getParameter("type_select");
        if (type != null) {
            String value = request.getParameter("mediator.property.val_ex").trim();
            if (request.getParameter("mediator.property.type").equals("expression")) {
                SynapsePath path = null;
                if(value.startsWith("json-eval(")) {
                    path = new SynapseJsonPath(value.substring(10, value.length() - 1));
                } else {
                    XPathFactory xPathFactory = XPathFactory.getInstance();
                    path = xPathFactory.createSynapseXPath("mediator.property.val_ex", request, session);
                }
                propertyMediator.setExpression(path);
            } else if (request.getParameter("mediator.property.type").equals("value")) {
                if (type != null && type.equals("OM")) {
                    propertyMediator.setType(type);

                    String omValue = request.getParameter("om_text");
                    if (omValue != null) {
                        try {
                            propertyMediator.setValueElement(AXIOMUtil.stringToOM(omValue));
                        } catch (XMLStreamException e) {
                            throw new RuntimeException("Valid XML required");
                        }
                    }
                } else {
                    propertyMediator.setValue(value);
                }
            }

            if (type.equals("STRING")) {
                String pattern = request.getParameter("pattern");
                String group = request.getParameter("group");

                if (pattern != null && !"".equals(pattern)) {
                    propertyMediator.setPattern(pattern);
                    if (group != null && !"".equals(group)) {
                        propertyMediator.setGroup(Integer.parseInt(group));
                    }
                }
                propertyMediator.setType(type);
            } else {
                propertyMediator.setType(type);
            }
        }
    }
%>

