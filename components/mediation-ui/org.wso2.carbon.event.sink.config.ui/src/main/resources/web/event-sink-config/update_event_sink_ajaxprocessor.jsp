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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.sink.xsd.EventSink" %>
<%@ page import="org.wso2.carbon.event.sink.config.ui.PublishEventMediatorConfigAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    PublishEventMediatorConfigAdminClient publishEventMediatorConfigAdminClient =
            new PublishEventMediatorConfigAdminClient(cookie, backendServerURL, configContext, request.getLocale());

    String action = request.getParameter("action");
    String responseText="";
    String propertyCountParameter = request.getParameter("propertyCount");
    String serverIp = request.getParameter("ip");
    String serverPort = request.getParameter("port");
    if (action.equals("add")) {

        if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
            int propertyCount = 0;
            try {
                propertyCount = Integer.parseInt(propertyCountParameter.trim());
                for (int i = 0; i <= propertyCount; i++) {
                    EventSink eventSink = new EventSink();
                    String name = request.getParameter("propertyName" + i);

                    if (name != null && !"".equals(name)) {
                        eventSink.setName(name);
                        String valueId = "propertyUsername" + i;
                        String username = request.getParameter(valueId);
                        eventSink.setUsername(username);
                        String password = request.getParameter("propertyPassword" + i);
                        eventSink.setPassword(password);
                        String receiverUrl = request.getParameter("propertyReceiverUrl" + i);
                        eventSink.setReceiverUrlSet(receiverUrl);
                        String authenticatorUrl = request.getParameter("propertyAuthenticatorUrl" + i);
                        eventSink.setAuthenticationUrlSet(authenticatorUrl);
                    }
                    publishEventMediatorConfigAdminClient.writeEventSinkXml(eventSink);

                }
            } catch (NumberFormatException ignored) {
                throw new RuntimeException("Invalid number format");
            }
        }
    } else if (action.equals("delete")) {
        //ignore methods other than post
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        String name = request.getParameter("name");
        if (!(publishEventMediatorConfigAdminClient.deleteEventSink(name))) {
            responseText="false";
            out.write(responseText);
        }
    } else if (action.equals("edit")) {
        if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
            int propertyCount = 0;
            try {
                propertyCount = Integer.parseInt(propertyCountParameter.trim());
                for (int i = 0; i <= propertyCount; i++) {
                    EventSink eventSink = new EventSink();
                    String name = request.getParameter("propertyName" + i);

                    if (name != null && !"".equals(name)) {
                        eventSink.setName(name);
                        String valueId = "propertyUsername" + i;
                        String username = request.getParameter(valueId);
                        eventSink.setUsername(username);
                        String password = request.getParameter("propertyPassword" + i);
                        eventSink.setPassword(password);
                        String receiverUrl = request.getParameter("propertyReceiverUrl" + i);
                        eventSink.setReceiverUrlSet(receiverUrl);
                        String authenticatorUrl = request.getParameter("propertyAuthenticatorUrl" + i);
                        eventSink.setAuthenticationUrlSet(authenticatorUrl);
                    }
                    publishEventMediatorConfigAdminClient
                            .updateEventSink(name, eventSink.getUsername(), eventSink.getPassword(),
                                             eventSink.getReceiverUrlSet(), eventSink.getAuthenticationUrlSet());
                }

            } catch (NumberFormatException ignored) {
                throw new RuntimeException("Invalid number format");
            }
        }
    } else if (action.equals("testServer") && publishEventMediatorConfigAdminClient.isNotNullAndNotEmpty(serverIp)
            && publishEventMediatorConfigAdminClient.isNotNullAndNotEmpty(serverPort)) {
        responseText = publishEventMediatorConfigAdminClient.backendServerExists(serverIp, serverPort);
        out.write(responseText);
    }
%>