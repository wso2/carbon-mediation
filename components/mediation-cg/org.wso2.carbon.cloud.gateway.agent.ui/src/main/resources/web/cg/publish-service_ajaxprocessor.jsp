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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGConstant" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%
    String serverName = request.getParameter("serverName");
    String serviceName = request.getParameter("serviceName");
    String action = request.getParameter("action");
    String autoMaticMode = request.getParameter("publishMode");
    String returnValue = null;

    try {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        CGAgentAdminClient client = new CGAgentAdminClient(cookie, backendServerURL,
                configContext, request.getLocale());
        if (CGConstant.CG_SERVICE_ACTION_PUBLISH.equals(action)) {  // starting the process
            if (autoMaticMode != null && !autoMaticMode.equals("")) {
                if (autoMaticMode.equals("automatic")) {
                    client.publish(serviceName, serverName, true);
                } else if (autoMaticMode.equals("manual")) {
                    client.publish(serviceName, serverName, false);
                } else {
                    throw new RuntimeException("Although the action is set to 'publish' the mode is neither manual nor " +
                            "automatic, it is '" + autoMaticMode + "'");
                }
            }
        } else if (CGConstant.CG_SERVICE_ACTION_UNPUBLISH.equals(action)) {  // just unpublish
            client.unPublish(serviceName, client.getPublishedServer(serviceName));
        } else if (CGConstant.CG_SERVICE_ACTION_MANUAL.equals(action)) { // manual publishing
            client.setServiceStatus(serviceName, CGConstant.CG_SERVICE_STATUS_PUBLISHED);
        } else if (CGConstant.CG_SERVICE_ACTION_AUTOMATIC.equals(action)) {
            client.setServiceStatus(serviceName, CGConstant.CG_SERVICE_STATUS_AUTO_MATIC);
        } else if (CGConstant.CG_SERVICE_ACTION_RESTART.equals(action)) {
            String publishedServer = client.getPublishedServer(serviceName);
            client.unPublish(serviceName, publishedServer);
            client.publish(serviceName, publishedServer, false);
        } else {
            throw new RuntimeException("Can not change the state of the service, '" + serviceName + "," +
                    "'unknown action '" + action + "' detected!");
        }
        returnValue = "successful";
    } catch (Exception e) {
        returnValue = e.getMessage();
    }
%>
<%=returnValue%>