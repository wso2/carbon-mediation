<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.mediator.throttle.client.ThrottleClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    //Obtaining the client-side ConfigurationContext instance.
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    //Server URL which is defined in the server.xml
    String serverBackendURL = CarbonUIUtil.getServerURL(config.getServletContext(),
            session);

    String service = request.getParameter("serviceName");
    String operation = request.getParameter("opName");
    String params = "";

    if (service != null) {
        if (operation == null) {
            params = "serviceName=" + service;
        } else {
            params = "serviceName=" + service + "&opName=" + operation;
        }
    }

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    try {
        ThrottleClient client = new ThrottleClient(cookie,
                serverBackendURL, configContext, request.getLocale());
        client.updateBackEnd(request);
    } catch (NumberFormatException e) {
%>
<script type="text/javascript">
    location.href = "index.jsp?<%=params%>";
</script>
<%
        return;
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
    String backURL = (String) session.getAttribute("backURL");
    session.removeAttribute("backURL");
%>

<script type="text/javascript">
    location.href = "<%=backURL%>";
</script>