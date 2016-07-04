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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.DdlAjaxProcessorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.BamServerProfilesHelper" %>

<%
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    String SERVER_PROFILE_LOCATION = "bamServerProfiles";
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    BamServerProfilesHelper bamServerProfilesHelper = new BamServerProfilesHelper(cookie, backendServerURL, configContext,
            request.getLocale());
    String action = request.getParameter("action");
    String profileName = request.getParameter("serverProfileName");
    if (bamServerProfilesHelper.isNotNullOrEmpty(action) && action.equals("remove") &&
            bamServerProfilesHelper.isNotNullOrEmpty(profileName)) {
        bamServerProfilesHelper.removeResource(SERVER_PROFILE_LOCATION + "/" + profileName);
    }

%>