<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.mediation.configadmin.ui.ConfigManagementClient" %>
<%@ page import="org.wso2.carbon.mediation.configadmin.ui.ResponseInformation" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.Date" %>

<link href="../styles/main.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="configcommon.js"></script>

<fmt:bundle basename="org.wso2.carbon.mediation.configadmin.ui.i18n.Resources">

    <%
        Date date = new Date();
        ConfigManagementClient client;

        try {
            String name = request.getParameter("name");
            String action = request.getParameter("action");
            ResponseInformation responseInfo = null;
            if (action.equals("activate")) {
                client = ConfigManagementClient.getInstance(config, session);
                responseInfo = client.loadConfiguration(name);
            } else if (action.equals("delete")) {
                client = ConfigManagementClient.getInstance(config, session);
                responseInfo = client.deleteConfiguration(name);                
            }
            if (responseInfo.isFault()) {
    %>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=updateFailed&rand=<%=date.getTime()%>&tab=1';
    </script>
    <%
    } else {
        if (action.equals("activate")) {
    %>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=activated&rand=<%=date.getTime()%>&tab=0';
    </script>
    <%} else { %>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=deleted&rand=<%=date.getTime()%>&tab=0';
    </script>
    <% }
    }
    } catch (Throwable t) {
    %>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=failed&rand=<%=date.getTime()%>&tab=1';
    </script>
    <%
        }
    %>

</fmt:bundle>