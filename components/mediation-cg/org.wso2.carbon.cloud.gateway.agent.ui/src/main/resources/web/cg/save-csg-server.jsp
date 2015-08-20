<%--

  Copyright (c) 20010-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
  Version 2.0 (the "License"); you may not use this file except
  in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.

--%>

<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGUtils" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGServerBean" %>

<%

    String serverURL = request.getParameter("csg_server_url_id");
    String hostName = CGUtils.getHostFromServerURL(serverURL);
    String port = CGUtils.getPortFromServerURL(serverURL);

    // a user name of the format 'admin@mydomain.org' for statos deployment
    // or just 'admin' for standalone deployment
    String tenantUserName = request.getParameter("csg_user_name_id");
    String userName = CGUtils.getUserNameFromTenantUserName(tenantUserName);
    String domain = CGUtils.getDomainNameFromTenantUserName(tenantUserName);
    String password = request.getParameter("csg_user_password_id");
    String name = request.getParameter("csg_server_name_id");

    String forwardTo = request.getParameter("forwardTo");
    if (forwardTo == null || "null".equals(forwardTo)) {
        forwardTo = "server-list.jsp";
    }

    String BUNDLE = "org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        CGAgentAdminClient csgAdminClient = new CGAgentAdminClient(
                cookie, backendServerURL, configContext, request.getLocale());

        if (hostName != null && port != null && userName != null && password != null) {
            CGServerBean csgServer = new CGServerBean();
            csgServer.setName(name);
            csgServer.setHost(hostName);
            csgServer.setPort(port);
            csgServer.setDomainName(domain);
            csgServer.setUserName(userName);
            csgServer.setPassWord(password);

            if (session.getAttribute("mode") != null && session.getAttribute("mode").equals("edit")) {
                csgServer.setName(request.getParameter("csg_server_name_id_hidden"));
                session.removeAttribute("mode");
                csgAdminClient.updateCGServer(csgServer);
            } else {
                csgAdminClient.addCGServer(csgServer);
            }
        }
    } catch (Exception e) {
        String message = MessageFormat.format(resourceBundle.getString("csg.error.cloud.not.add.server"),
                e.getMessage());
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>

<script type="text/javascript">
    location.href = "<%=forwardTo%>";
</script>