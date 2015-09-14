<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%--
~  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  Licensed to the Apache Software Foundation (ASF) under one or more
~  contributor license agreements.  See the NOTICE file distributed with
~  this work for additional information regarding copyright ownership.
~
~  The ASF licenses this file to You under the Apache License, Version 2.0
~
~  (the "License"); you may not use this file except in compliance with
~  the License.  You may obtain a copy of the License at
~       http://www.apache.org/licenses/LICENSE-2.0
~  Unless required by applicable law or agreed to in writing, software
~  distributed under the License is distributed on an "AS IS" BASIS,
~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~  See the License for the specific language governing permissions and
~  limitations under the License.
--%>

<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.publishevent.ui.Property" %>
<%@ page import="org.wso2.carbon.mediator.publishevent.ui.PublishEventMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%

    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof PublishEventMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;

    publishEventMediator.setStreamName(request.getParameter("mediator.publishEvent.stream.name"));
    publishEventMediator.setStreamVersion(request.getParameter("mediator.publishEvent.stream.version"));
    publishEventMediator.setEventSink(request.getParameter("mediator.publishEvent.eventSink.select"));
    publishEventMediator.clearList("meta");
    publishEventMediator.clearList("correlation ");
    publishEventMediator.clearList("payload");
    publishEventMediator.clearList("arbitrary");

    XPathFactory xPathFactory = XPathFactory.getInstance();
    String metaPropertyCountParameter = request.getParameter("metaPropertyCount");
    if (metaPropertyCountParameter != null && !"".equals(metaPropertyCountParameter)) {
        Property currentProperty;
        List<Property> metaProperties = new ArrayList<Property>();

        try {
            int propertyCount = Integer.parseInt(metaPropertyCountParameter.trim());
            for (int i = 0; i <= propertyCount; i++) {
                String name = request.getParameter("metaPropertyName" + i);
                if (name != null && !"".equals(name)) {
                    String valueId = "metaPropertyValue" + i;
                    String value = request.getParameter(valueId);
                    currentProperty = new Property();
                    currentProperty.setName(name);

                    String expression = request.getParameter("metaPropertyTypeSelection" + i);
                    boolean isExpression = expression != null && "expression".equals(expression.trim());

                    if (value != null) {
                        if (isExpression) {
                            if (value.trim().startsWith("json-eval(")) {
                                SynapseXPath jsonPath =
                                        new SynapseXPath(value.trim().substring(10, value.length() - 1));
                                currentProperty.setExpression(jsonPath);
                            } else {
                                currentProperty
                                        .setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                            }
                        } else {
                            currentProperty.setValue(value);
                        }
                    }

                    String type = request.getParameter("metaPropertyValueTypeSelection" + i);
                    currentProperty.setType(type);

                    metaProperties.add(currentProperty);
                }
            }

            ((PublishEventMediator) mediator).setMetaProperties(metaProperties);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        } catch (Exception exception) {
            throw new RuntimeException("Invalid Path Expression");
        }
    }

    String correlationPropertyCountParameter = request.getParameter("correlationPropertyCount");
    if (correlationPropertyCountParameter != null && !"".equals(correlationPropertyCountParameter)) {
        Property currentProperty;
        List<Property> correlationProperties = new ArrayList<Property>();

        try {
            int propertyCount = Integer.parseInt(correlationPropertyCountParameter.trim());
            for (int i = 0; i <= propertyCount; i++) {
                String name = request.getParameter("correlationPropertyName" + i);
                if (name != null && !"".equals(name)) {
                    String valueId = "correlationPropertyValue" + i;
                    String value = request.getParameter(valueId);
                    currentProperty = new Property();
                    currentProperty.setName(name);

                    String expression = request.getParameter("correlationPropertyTypeSelection" + i);
                    boolean isExpression = expression != null && "expression".equals(expression.trim());

                    if (value != null) {
                        if (isExpression) {
                            if (value.trim().startsWith("json-eval(")) {
                                SynapseXPath jsonPath =
                                        new SynapseXPath(value.trim().substring(10, value.length() - 1));
                                currentProperty.setExpression(jsonPath);
                            } else {
                                currentProperty
                                        .setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                            }
                        } else {
                            currentProperty.setValue(value);
                        }
                    }

                    String type = request.getParameter("correlationPropertyValueTypeSelection" + i);
                    currentProperty.setType(type);

                    correlationProperties.add(currentProperty);
                }
            }

            ((PublishEventMediator) mediator).setCorrelationProperties(correlationProperties);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        } catch (Exception exception) {
            throw new RuntimeException("Invalid Path Expression");
        }
    }

    String payloadPropertyCountParameter = request.getParameter("payloadPropertyCount");
    if (payloadPropertyCountParameter != null && !"".equals(payloadPropertyCountParameter)) {
        Property currentProperty;
        List<Property> payloadProperties = new ArrayList<Property>();

        try {
            int propertyCount = Integer.parseInt(payloadPropertyCountParameter.trim());
            for (int i = 0; i <= propertyCount; i++) {
                String name = request.getParameter("payloadPropertyName" + i);
                if (name != null && !"".equals(name)) {
                    String valueId = "payloadPropertyValue" + i;
                    String value = request.getParameter(valueId);
                    currentProperty = new Property();
                    currentProperty.setName(name);

                    String expression = request.getParameter("payloadPropertyTypeSelection" + i);
                    boolean isExpression = expression != null && "expression".equals(expression.trim());

                    if (value != null) {
                        if (isExpression) {
                            if (value.trim().startsWith("json-eval(")) {
                                SynapseXPath jsonPath =
                                        new SynapseXPath(value.trim().substring(10, value.length() - 1));
                                currentProperty.setExpression(jsonPath);
                            } else {
                                currentProperty
                                        .setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                            }
                        } else {
                            currentProperty.setValue(value);
                        }
                    }

                    String type = request.getParameter("payloadPropertyValueTypeSelection" + i);
                    currentProperty.setType(type);

                    payloadProperties.add(currentProperty);
                }
            }

            ((PublishEventMediator) mediator).setPayloadProperties(payloadProperties);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        } catch (Exception exception) {
            throw new RuntimeException("Invalid Path Expression");
        }
    }

    String arbitraryPropertyCountParameter = request.getParameter("arbitraryPropertyCount");
    if (arbitraryPropertyCountParameter != null && !"".equals(arbitraryPropertyCountParameter)) {
        Property currentProperty;
        List<Property> arbitraryProperties = new ArrayList<Property>();

        try {
            int propertyCount = Integer.parseInt(arbitraryPropertyCountParameter.trim());
            for (int i = 0; i <= propertyCount; i++) {
                String name = request.getParameter("arbitraryPropertyName" + i);
                if (name != null && !"".equals(name)) {
                    String valueId = "arbitraryPropertyValue" + i;
                    String value = request.getParameter(valueId);
                    currentProperty = new Property();
                    currentProperty.setName(name);

                    String expression = request.getParameter("arbitraryPropertyTypeSelection" + i);
                    boolean isExpression = expression != null && "expression".equals(expression.trim());

                    if (value != null) {
                        if (isExpression) {
                            if (value.trim().startsWith("json-eval(")) {
                                SynapseXPath jsonPath =
                                        new SynapseXPath(value.trim().substring(10, value.length() - 1));
                                currentProperty.setExpression(jsonPath);
                            } else {
                                currentProperty
                                        .setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                            }
                        } else {
                            currentProperty.setValue(value);
                        }
                    }

                    String type = request.getParameter("arbitraryPropertyValueTypeSelection" + i);
                    currentProperty.setType(type);

                    arbitraryProperties.add(currentProperty);
                }
            }

            ((PublishEventMediator) mediator).setArbitraryProperties(arbitraryProperties);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        } catch (Exception exception) {
            throw new RuntimeException("Invalid Path Expression");
        }
    }


%>

