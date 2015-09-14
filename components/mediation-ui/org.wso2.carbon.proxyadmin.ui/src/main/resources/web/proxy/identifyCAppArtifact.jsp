<!--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~   http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<%@ page import="org.wso2.carbon.ui.CarbonSecuredHttpContext" %>
<%
    String serviceName = request.getParameter("serviceName");
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProxyServiceAdminClient client = new ProxyServiceAdminClient(
            configContext, backendServerURL, cookie, request.getLocale());
    ProxyData pd = client.getProxy(serviceName);
    String name = pd.getName();
    boolean loggedIn = session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null;
%>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="proxyi18n"
        />

<td width="200px">
    <% if(pd.getArtifactContainerName() != null) { %>
        <img src="images/applications.gif">
        <% if (loggedIn) { %>
            <a href="./service_info.jsp?serviceName=<%=serviceName%>"><%=name%>
            <% if(pd.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
            </a>
        <% } else { %>
            <%=name%>
        <% } %>
    <% } else { %>
        <% if (loggedIn) { %>
            <a href="./service_info.jsp?serviceName=<%=serviceName%>"><%=name%></a>
        <% } else { %>
            <%=name%>
        <% } %>
    <% } %>
</td>