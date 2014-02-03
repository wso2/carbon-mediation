<%--
  ~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.stub.types.common.ConfigurationObject" %>
<%@ page import="org.wso2.carbon.endpoint.stub.types.service.EndpointMetaData" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointService" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%
    String isloadPage = request.getParameter("loadpage");
    String pageNumberStr = request.getParameter("pageNumber");

    int endpointPageNumber = 0;
    if (pageNumberStr != null) {
        endpointPageNumber = Integer.parseInt(pageNumberStr);
    }
    int numberOfPages = 0;

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    EndpointAdminClient client;
    EndpointMetaData[] ePMetaData = null;
    try {
        client = new EndpointAdminClient(cookie, serverURL, configContext);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        return;
    }

    if (isloadPage == null || "".equals(isloadPage)) {
        String endpointName = request.getParameter("endpointName");
        if ("true".equals(request.getParameter("force"))) {
            doForceDelete(client, endpointName, request);
        } else {
            String result = deleteEndpoint(client, endpointName, request);
            if (!result.equals("")) {
%>
<div>Dep Error:</div>
<%=result%>
<%
            }
        }
        return;
    }
%>

<%! private String deleteEndpoint(EndpointAdminClient adminClient, String endpointName,
                                   HttpServletRequest request) {
    if ((endpointName != null) && (!"".equals(endpointName))) {
        try {
            ConfigurationObject[] dependents = adminClient.getDependents(endpointName);
            if (dependents != null) {
                String msg = "";
                ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.mediation.initializer.ui.i18n.Resources",
                                                                 request.getLocale());
                for (ConfigurationObject o : dependents) {
                    msg += "&ensp;&ensp;- " + o.getId();
                    if (bundle != null) {
                        msg += " (" + bundle.getString("dependency.mgt." + o.getType()) + ")";
                    }
                    msg += "<br/>";
                }
                return msg;
            } else {
                doForceDelete(adminClient, endpointName, request);
            }
        } catch (Exception e) {
            String msg = "Could not delete endpoint: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }
    }
    return "";
}
%>
<%!
    private void doForceDelete(EndpointAdminClient adminClient, String epr,
                               HttpServletRequest request) {
        try {
            adminClient.deleteEndpoint(epr);
        } catch (Exception e) {
            String msg = "Could not delete endpoint: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }
    }
%>
<%
    ePMetaData = client.getEndpointMetaData(endpointPageNumber,EndpointAdminClient.ENDPOINT_PER_PAGE);
    if (ePMetaData != null && ePMetaData.length == 0 && endpointPageNumber != 0 ) {
        endpointPageNumber--;
        ePMetaData = client.getEndpointMetaData(endpointPageNumber,EndpointAdminClient.ENDPOINT_PER_PAGE);
    }
    int epCount = client.getEndpointCount();

    if (epCount % EndpointAdminClient.ENDPOINT_PER_PAGE == 0) {
        numberOfPages = epCount / EndpointAdminClient.ENDPOINT_PER_PAGE;
    } else {
        numberOfPages = epCount / EndpointAdminClient.ENDPOINT_PER_PAGE + 1;
    }
%>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">

    <div id="noEpDiv" style="<%=ePMetaData!=null ?"display:none":""%>">
        <fmt:message
                key="no.endpoints.in.synapse.config"></fmt:message>

    </div>

    <% if (ePMetaData != null) {%>
    <script type="text/javascript">
        isDefinedSequenceFound = true;
    </script>
    <p><fmt:message key="endpoints.synapse.text"/></p>
    <br/>
    <carbon:paginator pageNumber="<%=endpointPageNumber%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=""%>" />
    <br/>
    <table class="styledLeft" cellpadding="1" id="endpointListTable">
        <thead>
        <tr>
            <th style="width:20%"><fmt:message key="endpoint.name"/></th>
            <th style="width:20%"><fmt:message key="type"/></th>
            <th colspan="4"><fmt:message key="action"/></th>
        </tr>
        </thead>

        <tbody>
        <%for (EndpointMetaData endpoint : ePMetaData) {%>
        <tr>
            <td><% if (endpoint.getDescription() != null) { %>
                    <span href="#">
                          <%= endpoint.getName()%>
                    </span>
                <%
                } else {
                %>
                <span href="#"><%= endpoint.getName()%></span>
                <%
                    }
                %>
            </td>
            <td>
                <%
                    EndpointService ePService = client.getEndpointService(endpoint);
                %>
                <%=ePService.getDisplayName()%>
            </td>
            <td style="border-right:none;border-left:none;width:100px">
                <div class="inlineDiv">
                    <% if (endpoint.getSwitchOn()) { %>
                    <div id="switchOff<%=endpoint.getName()%>">
                        <a href="#" onclick="switchOff('<%= endpoint.getName() %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-on.gif);"><fmt:message
                                key="switch.off"/></a>
                    </div>
                    <div id="switchOn<%=endpoint.getName()%>" style="display:none;">
                        <a href="#" onclick="switchOn('<%= endpoint.getName() %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-off.gif);"><fmt:message
                                key="switch.on"/></a>
                    </div>
                    <%} else {%>
                    <div id="switchOff<%=endpoint.getName()%>" style="display:none;">
                        <a href="#" onclick="switchOff('<%= endpoint.getName() %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-on.gif);"><fmt:message
                                key="switch.off"/></a>
                    </div>
                    <div id="switchOn<%=endpoint.getName()%>" style="">
                        <a href="#" onclick="switchOn('<%= endpoint.getName() %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-off.gif);"><fmt:message
                                key="switch.on"/></a>
                    </div>
                    <% }
                        if (ePService.isStatisticsAvailable()) {
                            if (endpoint.getEnableStatistics()) { %>
                    <td style="border-right:none;border-left:none;width:100px">
                        <div id="disableStat<%= endpoint.getName()%>">
                            <a href="#" onclick="disableStat('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="disable.statistics"/></a>
                        </div>
                        <div id="enableStat<%= endpoint.getName()%>" style="display:none;">
                            <a href="#" onclick="enableStat('<%= endpoint.getName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="enable.statistics"/></a>
                        </div>
                    </td>
                </div>
            </td>
            <%
            } else { %>
            <td style="border-right:none;border-left:none;width:100px">
                <div class="inlineDiv">
                    <div id="enableStat<%= endpoint.getName()%>">
                        <a href="#" onclick="enableStat('<%= endpoint.getName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="enable.statistics"/></a>
                    </div>
                    <div id="disableStat<%= endpoint.getName()%>" style="display:none">
                        <a href="#" onclick="disableStat('<%= endpoint.getName() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="disable.statistics"/></a>
                    </div>
                </div>
            </td>
            <% }
            } else {%>
            <td style="border-right:none;border-left:none;width:100px"></td>
            <%
                }
            %>
            <td style="border-left:none;border-right:none;width:100px">
                <div class="inlineDiv">
                    <a href="#"
                       class="icon-link"
                       onclick="editEndpoint('<%=ePService.getUIPageName()%>','<%= endpoint.getName() %>')"
                       style="background-image:url(../admin/images/edit.gif);"><fmt:message
                            key="edit"/></a>
                </div>
            </td>
            <td style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#"
                       onclick="deleteEndpoint('<%= endpoint.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"><fmt:message
                            key="delete"/></a>
                </div>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <br/>
    <carbon:paginator pageNumber="<%=endpointPageNumber%>" numberOfPages="<%=numberOfPages%>"
                          page="index.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>" />
    <% } %>

</fmt:bundle>