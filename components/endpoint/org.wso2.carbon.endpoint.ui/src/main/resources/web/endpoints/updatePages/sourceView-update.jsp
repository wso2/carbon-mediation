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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.EndpointFactory" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.TemplateSerializer" %>
<%@ page import="org.apache.synapse.endpoints.Template" %>
<%@ page import="java.util.Properties" %>
<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<%
    String configuration = request.getParameter("endpointString");
    configuration = configuration.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
    configuration = configuration.replace("&amp;","&");
    configuration = configuration.replace("&", "&amp;"); // this is to ensure that url is properly encoded

    session.setAttribute("endpointConfiguration", configuration);
    OMElement endpointElement = AXIOMUtil.stringToOM(configuration);

    if (endpointElement.getLocalName().equalsIgnoreCase("endpoint")) {
        try {
            EndpointFactory.getEndpointFromElement(endpointElement, false, new Properties());
            %>
            <div>Success:</div>
            <%
        } catch (Exception endpointEx) {
             %>
            <div>Error:</div><%=endpointEx.getMessage()%>
            <%
        }
    } else if (endpointElement.getLocalName().equalsIgnoreCase("template")) {
        try {
            Template template = new Template();
            template.setElement(endpointElement.getFirstElement());
            new TemplateSerializer().serializeEndpointTemplate(template, null);
            %>
            <div>Success:</div>
            <%
        } catch (Exception templateEx) {
            %>
            <div>Error:</div><%=templateEx.getMessage()%>
            <%
        }
    } else {
         %>
            <div>Error:</div><fmt:message key="source.view.invalid.configuration"/>
         <%
    }
%>
</fmt:bundle>