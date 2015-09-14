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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGProxyToolsURLs" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String serviceName = request.getParameter("serviceName");
    CGAgentAdminClient csgAdminClient;
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        csgAdminClient = new CGAgentAdminClient(
                cookie, backendServerURL, configContext, request.getLocale());
        
        
    } catch (AxisFault axisFault) {
        String msg = "Cloud not retrieve the CSG service endpoint reference list";
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);

%>

<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>

<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources">
    <carbon:breadcrumb
            label="csg.epr"
            resourceBundle="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="csg.epr"/> (<%=serviceName%>)</h2>

        <div id="workArea">
            <table class="styledLeft" cellspacing="1" id="csg_epr_table_id" style="">
                <tbody>
                <%
                    CGProxyToolsURLs tools = csgAdminClient.getPublishedProxyToolsURLs(serviceName);
                    if(tools != null && tools.getEprArray() != null){
                        for (String epr : tools.getEprArray()) {
                %>
                <tr>
                    <td>
                        <%=epr%>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <span style="color: red; "><fmt:message key="csg.no.tool.reason"/></span>
                <%
                    }
                %>
                </tbody>
            </table>
            <script type="text/javascript">
                alternateTableRows('csg_epr_table_id', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </div>
</fmt:bundle>
