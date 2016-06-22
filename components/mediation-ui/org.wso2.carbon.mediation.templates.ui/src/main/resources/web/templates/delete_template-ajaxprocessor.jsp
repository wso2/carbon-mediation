<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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

<%@ page import="org.wso2.carbon.sequences.common.to.ConfigurationObject" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.TemplateAdminClientAdapter" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>

<%

    //ignore methods other than post
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String templateName = request.getParameter("sequenceName");
    String locationType = request.getParameter("type");
    String templateType = request.getParameter("templateType");

    if (templateType == null) {
        TemplateAdminClientAdapter templateAdminClient
                = new TemplateAdminClientAdapter(this.getServletConfig(), session);
        if (templateName != null && !"".equals(templateName) && locationType ==null) {
            doForceDelete(templateAdminClient, templateName, request);
        } else {
            templateAdminClient.deleteDynamicTemplate(templateName);
            // TODO: error handling
        }
    }else{
        EndpointTemplateAdminClient endpointAdminClient
                = new EndpointTemplateAdminClient(this.getServletConfig(), session);
        if (templateName != null && !"".equals(templateName) && locationType ==null) {
            doForceDelete(endpointAdminClient, templateName, request);
        } else {
            endpointAdminClient.deleteDynamicTemplate(templateName);
            // TODO: error handling
        }

    }
%>

<%!
    private void doForceDelete(TemplateAdminClientAdapter adminClient, String templateName,
                             HttpServletRequest request) {
        try {
            adminClient.deleteTemplate(templateName);
        } catch (Exception e) {
            String msg = "Could not delete template: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }
    }

    private void doForceDelete(EndpointTemplateAdminClient adminClient, String templateName,
                             HttpServletRequest request) {
        try {
            adminClient.deleteTemplate(templateName);
        } catch (Exception e) {
            String msg = "Could not delete template: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }
    }
%>
