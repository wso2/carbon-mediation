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

<%@ page import="org.apache.synapse.endpoints.Template" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>
<%@ page import="org.wso2.carbon.mediation.templates.common.factory.TemplateInfoFactory" %>

<%

    EndpointTemplateAdminClient endpointClient
            = new EndpointTemplateAdminClient(this.getServletConfig(), session);
    String templateName = request.getParameter("templateName");
    String type = request.getParameter("edittingType");
    Template templateObj = null;

    //removr any specific session vars
    session.removeAttribute("templateEdittingMode");
    session.removeAttribute("templateRegKey");

    if (templateName == null) {
        templateObj = new Template();
        templateObj.setName("");
        session.setAttribute("endpointTemplate", templateObj);
    }else if (type == null){
        templateObj = endpointClient.getTempalate(templateName);
        session.setAttribute("endpointTemplate", templateObj);
    }else{
        templateObj = endpointClient.getTempalate(templateName);
        session.setAttribute("endpointTemplate", templateObj);
        session.setAttribute("templateEdittingMode","registry");
        session.setAttribute("templateRegKey", templateName);
        String endpointType = TemplateInfoFactory.getEndpointTypeFromTemplate(templateObj.getElement());
        %>
    <%=endpointType%>;<%=templateName%>
    <%
    } /*else if (templateName == null) {
        templateObj = new Template();
        templateObj.setName("");
        session.setAttribute("endpointTemplate", templateObj);
    }*/


%>