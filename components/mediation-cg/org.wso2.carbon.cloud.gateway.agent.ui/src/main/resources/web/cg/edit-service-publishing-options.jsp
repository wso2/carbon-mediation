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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGServerBean" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGConstant" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String serviceName = request.getParameter("serviceName");
    String action = request.getParameter("action");
    boolean isPublished = false;
    boolean isCSGServerAvilable = false;
    String remoteCSGServerTableStyle;
    String noCSGServerTableStyle;

    CGServerBean[] serverBeans = null;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServiceAdminClient client;
    CGAgentAdminClient csgAgentAdminClient;

    try {
        csgAgentAdminClient = new CGAgentAdminClient(
                cookie, backendServerURL, configContext, request.getLocale());
        serverBeans = csgAgentAdminClient.getCGServerList();
        if (serverBeans != null && serverBeans[0] != null) {
            isCSGServerAvilable = true;
            remoteCSGServerTableStyle = "";
            noCSGServerTableStyle = "display:none";

        } else {
            // no servers defined prompt the user to add servers
            remoteCSGServerTableStyle = "display:none";
            noCSGServerTableStyle = "";
        }
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
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
            label="csg.publish.options"
            resourceBundle="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="csg.publish.options"/>(<%=serviceName%>)</h2>

        <div id="workArea">
            <table class="styledLeft" cellpadding="0" id="service-publishing-option-table-id"
                   style="">
                <thead>
                <tr>
                    <th><fmt:message key="csg.publish.option"/></th>
                </tr>
                </thead>
                <%
                    if(CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PUBLISH_SERVICE_PERMISSION_STRING)){
                %>
                <tbody>
                <tr>
                    <td>
                        <table class="normal">
                            <tbody>
                            <tr>
                                <td style="width: 180px"><fmt:message key="csg.publish.mode"/></td>
                                <td>
                                    <input id="automatic-id" type="radio" name="publish-mode"
                                           value="true" checked="checked" onclick="setMode('automatic');"/><label
                                        for="automatic-id"><fmt:message
                                        key="csg.service.automatic"/></label>
                                    <input id="manul-id" type="radio" name="publish-mode"
                                           value="false"
                                           onclick="setMode('manual');"/><label
                                        for="manul-id"><fmt:message
                                        key="csg.service.manual"/></label>
                                </td>
                            </tr>
                            <tr>
                                <td style="width:180px"><fmt:message key="csg.remote.server"/></td>
                                <td>
                                    <%
                                        if (isCSGServerAvilable) {
                                    %>
                                    <label for="publish_to_csg_server_selector"></label>
                                    <select id="publish_to_csg_server_selector"
                                            style="margin-top: 2px ! important;">
                                        <option value="choose-server" id="default-value"
                                                selected="true">
                                            <fmt:message key="csg.select.server"/></option>
                                        <%
                                            for (CGServerBean serverBean : serverBeans) {
                                        %>
                                        <option value="<%=serverBean.getName()%>"><%=serverBean.getName() + " (https://" + serverBean.getHost() + ":" + serverBean.getPort() + ")"%>
                                        </option>
                                        <%
                                            }
                                        %>
                                    </select>
                                    <%
                                    } else {
                                    %>
                                    <fmt:message key="no.csg.server.defined"/>
                                    <a href="#" style="line-height:24px"
                                       onclick="addNewCSGServer();return false;"><fmt:message
                                            key="csg.add.short"/></a>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <%
                                    boolean isAuthorizedToManage =
                                            CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PUBLISH_SERVICE_PERMISSION_STRING);
                                    if (isAuthorizedToManage) {
                                %>
                                <td><input type="button" name="publish"
                                           value="<fmt:message key="csg.service.publish"/>"
                                           onclick="publishService('<%=serviceName%>', '<%=action%>');return false;"/>
                                </td>
                                <%
                                } else {
                                %>
                                <td><input type="button" name="publish"
                                           value="<fmt:message key="csg.service.publish"/>"
                                           disabled="disabled"/>
                                </td>
                                <%
                                    }
                                %>
                            </tr>
                            <tr id="rotating_indicator_id" style="display: none">
                                <td>
                                    <div style="position: absolute;margin-top:-5px;margin-left:-10px;padding-left:20px;background:transparent url(../cg/images/loading-small.gif) no-repeat left top;">
                                        <fmt:message key="csg.action.in.progress"/></div>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                </tbody>
                <% }%>
            </table>
        </div>
    </div>

    <script type="text/javascript">
        var publishingMode;

        function setMode(option) {
            publishingMode = option;
        }

        function publishService(serviceToPublish, serviceAction) {
            if (serviceAction == '<%=CGConstant.CG_SERVICE_ACTION_PUBLISH%>' ||
                serviceAction == '<%=CGConstant.CG_SERVICE_ACTION_AUTOMATIC%>') {
                var serverName = document.getElementById('publish_to_csg_server_selector').value;
                if (serverName == 'choose-server') {
                    CARBON.showWarningDialog("<fmt:message key="csg.invalid.selection.select.server"/>");
                    return;
                }
                if (publishingMode == 'undefined' || publishingMode == 'null' || publishingMode == null) {
                    // set default
                    publishingMode = 'automatic';
                }
                document.getElementById('rotating_indicator_id').style.display = "";
                jQuery.get("../cg/publish-service_ajaxprocessor.jsp", {'serviceName':serviceToPublish, 'serverName':serverName, 'action':serviceAction, 'publishMode':publishingMode},
                        function (data, status) {
                            handlePublishingCallBack(data, status, 'service-publishing-option-table-id');
                        });
            }
        }

        function handlePublishingCallBack(data, status, id) {
            document.getElementById('rotating_indicator_id').style.display = "none";

            if (data.replace(/^\s+|\s+$/g, '') != 'successful') {
                document.getElementById('publish_to_csg_server_selector').selectedIndex = 0;
                CARBON.showErrorDialog(data);
            } else {
                if (id != null) {
                    showHideIcon(id);
                }
                CARBON.showInfoDialog('<fmt:message key="service.published.sucessfully"/>',
                        function () {
                            location.href = 'forward-to.jsp';
                        },
                        null);
            }
        }

        function addNewCSGServer() {
            location.href = 'add-edit-csg-server.jsp?forwardTo=service-list.jsp';
        }

        function showHideIcon(id) {
            hideElem(id);
        }

        function showElem(id) {
            document.getElementById(id).style.display = "";
        }

        function hideElem(id) {
            document.getElementById(id).style.display = "none";
        }

    </script>
</fmt:bundle>