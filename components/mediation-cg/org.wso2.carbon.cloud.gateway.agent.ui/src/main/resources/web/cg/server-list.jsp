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
<%@ page import="org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGServerBean" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGConstant" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String csgServerTableStyle = "display:none";
    String noCSGServerStyle = "";
    CGServerBean[] info = null;
    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        CGAgentAdminClient csgAdminClient = new CGAgentAdminClient(
                cookie, backendServerURL, configContext, request.getLocale());
        if (csgAdminClient.getCGServerList() != null && csgAdminClient.getCGServerList()[0] != null) {
            csgServerTableStyle = ""; // display the table
            noCSGServerStyle = "display:none";
            info = csgAdminClient.getCGServerList();

        }
    } catch (AxisFault axisFault) {
        String msg = "Cloud not retrieve the CSG server list";
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
            label="csg.menu.add.server"
            resourceBundle="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="csg.add.edit.server"/></h2>

        <div id="workArea">
            <%
                if(CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PERMISSION_STRING)){
            %>
            <table class="styledLeft" cellspacing="1" id="csg_server_table_id" style="<%=csgServerTableStyle%>">
                <thead>
                <tr class="tableEvenRow">
                    <th width="30%"><fmt:message key="csg.server.name"/></th>
                    <th><fmt:message key="csg.actions"/></th>
                </tr>
                </thead>
                <tbody>
                    <%
                        if(info != null){
                            for (CGServerBean anInfo : info) {
                                String serverName = anInfo.getName();
                    %>
                    <tr>
                    <td>
                            <%=serverName%>
                    </td>
                    <td>
                        <a href="add-edit-csg-server.jsp?serverName=<%=serverName%>&mode=edit" class="icon-link" style="background-image:url(../admin/images/edit.gif);"><fmt:message key="edit.server"/></a>
                        <a href="#" onclick="deleteServer('<%=serverName%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="csg.delete"/></a>
                    </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </tbody>
            </table>
            <table style="<%=noCSGServerStyle%>">
                <thead><tr>
                    <th><fmt:message key="csg.no.server"/></th>
                </tr></thead>
            </table>
            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td>
                        <a href="add-edit-csg-server.jsp" class="icon-link"
                           style="background-image:url(images/add.gif);"><fmt:message key="csg.add.server"/></a>
                    </td>
                </tr>
            </table>

            <script type="text/javascript">
                alternateTableRows('csg_server_table_id', 'tableEvenRow', 'tableOddRow');
            </script>
            <% }%>
        </div>
    </div>

    <script type="text/javascript">

        function validateAndDeleteServer(serverName, validationSucessCallback) {
            jQuery.get("../cg/has-services_ajaxprocessor.jsp", {'serverName':serverName},
                    function (data, status) {
                        var exists = false;

                        if (data.replace(/^\s+|\s+$/g, '') == 'true') {
                            exists = true;
                        }

                        if (exists) {
                            CARBON.showInfoDialog("<fmt:message key="csg.server.has.published.services"/>");
                        } else {
                            validationSucessCallback();
                        }
                    });
        }


        function deleteServer(serverName) {
        	var onValidationSuccess = function() {
        		CARBON.showConfirmationDialog("<fmt:message key="csg.delete.server"/> " + serverName + "?", function () {
                	location.href = "delete-server.jsp?&serverName=" + serverName;
            	});
        	};
        	
        	validateAndDeleteServer( serverName, onValidationSuccess);
        }

    </script>

</fmt:bundle>
