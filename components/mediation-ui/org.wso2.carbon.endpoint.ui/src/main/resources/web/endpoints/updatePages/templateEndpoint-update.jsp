<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.template.TemplateEndpoint" %>

<%
    TemplateEndpoint endpoint = new TemplateEndpoint();
    String endpointName = request.getParameter("endpointName");
    String address = request.getParameter("address");
    String targetTemplate = request.getParameter("target.template");

    if (endpointName != null & !"".equals(endpointName.trim())) {
        endpoint.addParameter("name", endpointName.trim());
    }
    if (address != null) {
        endpoint.addParameter("uri", address.trim());
    }
    if (targetTemplate != null) {
        endpoint.setTargetTemplate(targetTemplate.trim());
    }
    int paramCount = Integer.parseInt(request.getParameter("propertyCount"));
    for (int i = 0; i < paramCount; i++) {
        String paramName = request.getParameter("propertyName" + i);
        String paramValue = request.getParameter("propertyValue" + i);
        if (paramName != null && paramValue != null && !"".equals(paramName.trim())) {
            endpoint.addParameter(paramName.trim(), paramValue.trim());
        }
    }

    OMElement endpointElement = endpoint.serialize(null);
    session.setAttribute("endpointConfiguration", endpointElement.toString());
%>
