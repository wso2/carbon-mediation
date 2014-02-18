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
        client = ConfigManagementClient.getInstance(config, session);
        String xml = request.getParameter("rawConfig");
        boolean force = Boolean.parseBoolean(request.getParameter("force"));
        if (xml != null && !"".equals(xml)) {
            if (force) {
                ResponseInformation responseInfo = client.updateConfiguration(xml.trim(), session);
                if (responseInfo.isFault()) {
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=updateFailed&rand=<%=date.getTime()%>';
    </script>
<%
                } else {
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=updated&rand=<%=date.getTime()%>';
    </script>
<%
                }
            } else {
                ResponseInformation responseInfo = client.validateConfiguration(xml.trim());
                if (responseInfo.isFault()) {
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=updateFailed&rand=<%=date.getTime()%>';
    </script>
<%
                } else if (responseInfo.getResult() != null) {
                    session.setAttribute("validation.errors", responseInfo.getResult());
                    session.setAttribute("input.config", xml);
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?configInvalid=true&rand=<%=date.getTime()%>';
    </script>
<%
                } else {
                    responseInfo = client.updateConfiguration(xml.trim(), session);
                    if (responseInfo.isFault()) {
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=updateFailed&rand=<%=date.getTime()%>';
    </script>
<%
                    } else {
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=updated&rand=<%=date.getTime()%>';
    </script>
<%
                    }
                }
            }
} else {
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=session&rand=<%=date.getTime()%>';
    </script>
    <%
        }
    } catch (Throwable t) {
%>
    <script type="text/javascript">
        window.location.href = 'index.jsp?status=updateFailed&rand=<%=date.getTime()%>';
    </script>
<%
    }
%>

</fmt:bundle>  