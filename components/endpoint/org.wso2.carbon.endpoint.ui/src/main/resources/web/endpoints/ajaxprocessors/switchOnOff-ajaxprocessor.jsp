<%--
  ~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String endpointName = request.getParameter("endpointName");
    String action = request.getParameter("action");

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client = new EndpointAdminClient(cookie, url, configContext);

    if (endpointName != null && action != null) {
        try {
            if (!"".equals(action) && action.equals("switchOn")) {
                client.switchOn(endpointName);
            } else if (!"".equals(action) && action.equals("switchOff")) {
                client.switchOff(endpointName);
            }
        } catch (Exception ex) {
            String errMsg = ex.getMessage();
            errMsg = errMsg.replace("\'", ""); // this is to ensure that error message doesn't have ' or " marking unterminated strings
            errMsg = errMsg.replace("\"", "");
            errMsg = errMsg.replace("\n", "");

%>
<div>Error:</div>
<%=errMsg%>
<%
        }
    }
%>