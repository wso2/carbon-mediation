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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.DdlAjaxProcessorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    DdlAjaxProcessorHelper ddlAjaxProcessorHelper = new DdlAjaxProcessorHelper(cookie, backendServerURL, configContext, request.getLocale());

    String action = request.getParameter("action");
    String serverProfilePath = request.getParameter("serverProfilePath");
    String serverIp = request.getParameter("ip");
    String serverPort = request.getParameter("port");
    String responseText = "";

    if(ddlAjaxProcessorHelper.isNotNullOrEmpty(action)){
        if(action.equals("getServerProfiles") && ddlAjaxProcessorHelper.isNotNullOrEmpty(serverProfilePath)){
            responseText = ddlAjaxProcessorHelper.getServerProfileNames(serverProfilePath);
            out.write(responseText);
        } else if(action.equals("testServer") && ddlAjaxProcessorHelper.isNotNullOrEmpty(serverIp)
                  && ddlAjaxProcessorHelper.isNotNullOrEmpty(serverPort)){
            responseText = ddlAjaxProcessorHelper.backendServerExists(serverIp, serverPort);
            out.write(responseText);
        } else {
            out.write(0);
        }
    }
%>