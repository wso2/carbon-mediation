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
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">

    <%
        EndpointTemplateAdminClient templateAdminClient
                = new EndpointTemplateAdminClient(this.getServletConfig(), session);

        String templateName = request.getParameter("templateSelect");
        Template template = templateAdminClient.getTempalate(templateName);
        List<String> paramSet = template.getParameters();
    %>

    <%
        int i = 0;
        for (String paramName : paramSet) {
            String paramValue = "";
            if (paramName.equals("name") || paramName.equals("uri")) { // hide default parameters
                continue;
            }
    %>
    <tr id="propertyRaw<%=i%>">
        <td><input type="text" name="propertyName<%=i%>"
                   id="propertyName<%=i%>"
                   class="esb-edit small_textbox"
                   value="<%=paramName%>"/>
        </td>
        <td><input type="text" name="propertyValue<%=i%>"
                   id="propertyValue<%=i%>"
                   value="<%=paramValue%>"/>
        </td>
        <td><a href="#" class="delete-icon-link"
               onclick="deleteProperty(<%=i%>)"><fmt:message
                key="template.parameter.delete"/></a></td>
    </tr>
    <%
            i++;
        }%>
    <input type="hidden" name="propertyCount" id="propertyCount"
           value="<%=i%>"/>

</fmt:bundle>