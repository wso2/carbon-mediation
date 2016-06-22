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

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    //ignore methods other than post
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String serverName = request.getParameter("serverName");
    try {
        if (serverName != null) {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            CGAgentAdminClient csgAdminClient = new CGAgentAdminClient(
                    cookie, backendServerURL, configContext, request.getLocale());

            csgAdminClient.removeCGServer(serverName);
        }
    } catch (AxisFault axisFault) {
        String msg = "Cloud not delete the CSG server '" + serverName + "'";
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>

<%
        return;
    }
%>

<script type="text/javascript">
    document.location.href = "server-list.jsp";
</script>