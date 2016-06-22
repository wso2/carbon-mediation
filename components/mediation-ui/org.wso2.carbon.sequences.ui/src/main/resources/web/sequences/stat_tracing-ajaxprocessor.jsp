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

<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.EditorUIClient" %>

<%
    //ignore methods other than post
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String sequenceName = request.getParameter("sequenceName");
    String action = request.getParameter("action");    

    if (sequenceName != null && action != null) {
        EditorUIClient client = SequenceEditorHelper.getClientForEditor(getServletConfig(), session);//new SequenceAdminClient(getServletConfig(), session);
        if ("enableStat".equals(action)) {
            client.enableStatistics(sequenceName);
        } else if ("disableStat".equals(action)) {
            client.disableStatistics(sequenceName);
        } else if ("enableTracing".equals(action)) {
            client.enableTracing(sequenceName);
        } else if ("disableTracing".equals(action)) {
            client.disableTracing(sequenceName);
        }
    }

%>