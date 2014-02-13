<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%
    String[] endpointGroups = null;
    String endpointString   =  request.getParameter("endpointGroupsString");
    if (endpointString != null && !endpointString.equalsIgnoreCase("") ) {
        endpointGroups = endpointString.split(":");
    }

    String pageNumber = request.getParameter("pageNumberEndpoint");
    String deleteAllEndpointGroups = request.getParameter("deleteAllEndpointGroups");
    int pageNumberInt = 0;
    if (pageNumber != null && !pageNumber.equals("")) {
         try {
              pageNumberInt = Integer.parseInt(pageNumber);
         } catch (Exception e) {

         }
    }
%>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client;
    try {
        client =  new EndpointAdminClient(cookie, serverURL, configContext);
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<script type="text/javascript">
    location.href = "../../admin/error.jsp";
</script>
<%
        return;
    }
    try {
        if (deleteAllEndpointGroups!= null && deleteAllEndpointGroups.equalsIgnoreCase("true")) {
            client.deleteAllEndpointGroups();
        } else if (endpointGroups!=null && endpointGroups.length>0) {
            client.deleteSelectedEndpoints(endpointGroups);
        }

} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
    }
%>