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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.service.mgt.ui.ServiceAdminClient" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper" %>
<%@ page import="org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGServerBean" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGConstant" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGProxyToolsURLs" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String csgServiceTableStyle = "", noCSGServiceStyle = "";

    response.setHeader("Cache-Control", "no-cache");
    String action = request.getParameter("action");
    String remoteCSGServerTableStyle;
    String noCSGServerTableStyle;
    String serverName = null;
    String publishedStatus;
    boolean isPublished = false;
    boolean isCSGServerAvilable = false;

    CGServerBean[] serverBeans = null;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServiceAdminClient client;
    CGAgentAdminClient csgAgentAdminClient;

    ServiceMetaData[] serviceData;
    int numberOfPages;
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }
    ServiceMetaDataWrapper servicesInfo;

    String serviceTypeFilter = request.getParameter("serviceTypeFilter");
    if (serviceTypeFilter == null) {
        serviceTypeFilter = "ALL";
    }
    String serviceSearchString = request.getParameter("serviceSearchString");
    if (serviceSearchString == null) {
        serviceSearchString = "";
    }
    String parameters = "serviceTypeFilter=" + serviceTypeFilter +
            "&serviceSearchString=" + serviceSearchString;
    boolean isAuthorizedToManage =
            CarbonUIUtil.isUserAuthorized(request, CGConstant.MANAGE_SERVICE_PERMISSION_STRING);
    try {
        client = new ServiceAdminClient(cookie, backendServerURL, configContext, request.getLocale());
        csgAgentAdminClient = new CGAgentAdminClient(
                cookie, backendServerURL, configContext, request.getLocale());
        servicesInfo = client.getAllServices(serviceTypeFilter,
                serviceSearchString,
                Integer.parseInt(pageNumber));
        numberOfPages = servicesInfo.getNumberOfPages();
        serviceData = servicesInfo.getServices();
        csgServiceTableStyle = isAuthorizedToManage ? "" : "display:none";
        noCSGServiceStyle = isAuthorizedToManage ? "display:none" : "";
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
        label="csg.menu.pubsub.service"
        resourceBundle="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<div id="middle">
    <h2><fmt:message key="csg.publish.unpublish"/></h2>

    <div id="workArea">
    <%
        if(CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PUBLISH_SERVICE_PERMISSION_STRING) ||
                CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING)) {
    %>
        <form action="service-list.jsp" name="searchForm">

        <table class="styledLeft">
            <tbody>
            <tr>
                <td>
                    <table style="border: 0; !important">
                        <tbody>
                        <tr style="border: 0; !important">
                            <td style="border:0; !important">
                                <nobr>
                                    <fmt:message key="service.type"/>
                                    <select name="serviceTypeFilter">
                                        <%
                                            if (serviceTypeFilter.equals("ALL")) {
                                        %>
                                        <option value="ALL" selected="selected"><fmt:message key="all"/></option>
                                        <%
                                        } else {
                                        %>
                                        <option value="ALL"><fmt:message key="all"/></option>
                                        <%
                                            }
                                            for (String serviceType : servicesInfo.getServiceTypes()) {
                                                if (serviceTypeFilter.equals(serviceType)) {
                                        %>
                                        <option value="<%= serviceType%>" selected="selected"><%= serviceType%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%= serviceType%>"><%= serviceType%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                    &nbsp;&nbsp;&nbsp;
                                    <fmt:message key="search.service"/>
                                    <input type="text" name="serviceSearchString"
                                           value="<%= serviceSearchString != null? serviceSearchString : ""%>"/>&nbsp;
                                </nobr>
                            </td>
                            <td style="border:0; !important">
                                <a class="icon-link" href="#" style="background-image: url(../service-mgt/images/search.gif);"
                                   onclick="javascript:searchServices(); return false;"
                                   alt="<fmt:message key="search"/>"></a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>

        <br/>

        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          page="index.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=parameters%>"/>

        <table class="styledLeft" cellspacing="1" id="csg_service_table_id"
               style="<%=csgServiceTableStyle%>">
            <thead>
            <tr class="tableEvenRow">
                <th colspan="3" width="30%"><fmt:message key="csg.service.name"/></th>
                <th width="10%"><fmt:message key="csg.service.status"/></th>
                <th width="27%"><fmt:message key="csg.actions"/></th>
                <th><fmt:message key="csg.tools"/></th>
            </tr>
            </thead>
            <tbody>
            <%
                if (serviceData != null) {
                    for (ServiceMetaData service : serviceData) {
                        if (service != null) {
                            String serviceName = service.getName();
                            if (serviceName != null) {
                                String serviceStatus = csgAgentAdminClient.getServiceStatus(serviceName);
                                if (serviceStatus != null) {
            %>
            <tr>
                <td><%=serviceName%>
                </td>
                <td width="20px" style="text-align:left;">
                    <nobr>
                        <img src="../<%= service.getServiceType()%>/images/type.gif"
                             title="<%= service.getServiceType()%>"
                             alt="<%= service.getServiceType()%>"/>
                        <%= service.getServiceType() %>
                    </nobr>
                </td>
                <td width="20px" style="text-align:left;">
                    <nobr>
                        <%
                            if(service.getSecurityScenarioId() != null) {
                         %>
                        <img src="../service-mgt/images/secured.gif"/><fmt:message key="csg.secured"/>
                        <%
                            } else {
                        %>
                        <img src="../service-mgt/images/unsecured.gif"/><fmt:message key="csg.not.secured"/>
                        <%
                            }
                        %>
                    </nobr>
                </td>
                <%
                    if (serviceStatus.equals(CGConstant.CG_SERVICE_STATUS_UNPUBLISHED) &&
                        CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PUBLISH_SERVICE_PERMISSION_STRING)) {
                        // service is un published mode, let the user to publish
                %>
                <td><%=CGConstant.CG_SERVICE_STATUS_UNPUBLISHED%>
                </td>
                <td>
                    <a href="#"
                       onclick="goToPublishingOptions('<%=serviceName%>', '<%=CGConstant.CG_SERVICE_ACTION_PUBLISH%>');return false;"
                       class="icon-link" style="background-image:url(../cg/images/publish.png);">
                        <fmt:message key="csg.service.publish"/></a>
                </td>
                <td></td>
                <%
                } else if (serviceStatus.equals(CGConstant.CG_SERVICE_STATUS_PUBLISHED) &&
                        CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING)) {
                    // service in published stats, but user has select manual mode of operation
                %>
                <td><%=CGConstant.CG_SERVICE_STATUS_PUBLISHED%>
                </td>
                <td>
                    <a href="#"
                       onclick="changeServiceStatus('<%=serviceName%>', '<%=CGConstant.CG_SERVICE_ACTION_UNPUBLISH%>', false);return false;"
                       class="icon-link"
                       style="background-image:url(../cg/images/unpublish.png);">
                        <fmt:message key="csg.service.unpublish"/></a>
                    <a href="#"
                       onclick="changeServiceStatus('<%=serviceName%>','<%=CGConstant.CG_SERVICE_ACTION_RESTART%>', false);return false;"
                       class="icon-link"
                       style="background-image:url(../cg/images/restart.png);">
                        <fmt:message key="csg.service.restart"/></a>
                    <a href="#"
                       onclick="changeServiceStatus('<%=serviceName%>', '<%=CGConstant.CG_SERVICE_ACTION_AUTOMATIC%>', false);return false;"
                       class="icon-link"
                       style="background-image:url(../cg/images/switch_to_automatic.png);">
                        <fmt:message key="csg.service.switch.atuomatic"/></a>
                </td>
                <td>
                    <%
                        try {
                            CGProxyToolsURLs tools = csgAgentAdminClient.getPublishedProxyToolsURLs(serviceName);
                            if (tools != null) {
                    %>
                    <a href="<%=CGUtils.getTryItURLFromWSDLURL(tools.getWsdl11URL())%>" class="icon-link" style="background-image:url(../service-mgt/images/tryit.gif);" target="_blank">
                        <fmt:message key="try.this.service"/>
                    </a>
                    <a href="<%=tools.getWsdl11URL()%>" class="icon-link"
                       style="background-image:url(../service-mgt/images/wsdl.gif);" target="_blank">
                        <fmt:message key="wsdl11"/>
                    </a>
                    <a href="<%=tools.getWsdl2URL()%>" class="icon-link"
                       style="background-image:url(../service-mgt/images/wsdl.gif);" target="_blank">
                        <fmt:message key="wsdl2"/>
                    </a>
                    <a href="#" class="icon-link" style="background-image:url(../cg/images/endpoints-icon.gif);" onclick="goToEPRPage('<%=serviceName%>');"><fmt:message key="csg.epr"/></a>
                    <%
                        } else {
                    %>
                    <span style="color: red; "><fmt:message key="csg.no.tool"/></span>
                    <%
                        }
                    } catch (Exception e) {
                        String reason = e.getMessage();
                        if (reason != null && !reason.equals("")) {
                            // FIXME - due to some reason not the original log was here
                            // so used the following general error
                    %>
                    <span style="color: red; "><fmt:message
                            key="csg.no.tool.reason"/></span>
                    <%
                    } else {
                    %>
                    <span style="color: red; "><fmt:message
                            key="csg.no.tool.connection.refused"/></span>
                    <%
                            }

                        }
                    %>
                </td>
                <%
                } else if (serviceStatus.equals(CGConstant.CG_SERVICE_STATUS_AUTO_MATIC)) {
                    // services is published, but user has selected automatic operation
                    if (CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING)) {
                %>
                <td><%=CGConstant.CG_SERVICE_STATUS_PUBLISHED%>
                </td>
                <td>
                    <a href="#"
                    onclick="changeServiceStatus('<%=serviceName%>', '<%=CGConstant.CG_SERVICE_ACTION_UNPUBLISH%>', true);return false;"
                    class="icon-link"
                    style="background-image:url(../cg/images/unpublish.png);">
                        <fmt:message key="csg.service.unpublish"/></a>
                    <a href="#"
                       onclick="changeServiceStatusToManual('<%=serviceName%>', '<%=CGConstant.CG_SERVICE_ACTION_MANUAL%>', true);return false;"
                       class="icon-link"
                       style="background-image:url(../cg/images/move-to-manual.png);">
                        <fmt:message key="csg.service.switch.manual"/></a>
                </td>
                <td>
                    <%
                        try {
                            CGProxyToolsURLs tools = csgAgentAdminClient.getPublishedProxyToolsURLs(serviceName);
                            if (tools != null) {
                    %>
                    <a href="<%=CGUtils.getTryItURLFromWSDLURL(tools.getWsdl11URL())%>" class="icon-link" style="background-image:url(../service-mgt/images/tryit.gif);" target="_blank">
                        <fmt:message key="try.this.service"/>
                    </a>
                    <a href="<%=tools.getWsdl11URL()%>" class="icon-link"
                       style="background-image:url(../service-mgt/images/wsdl.gif);" target="_blank">
                        <fmt:message key="wsdl11"/>
                    </a>
                    <a href="<%=tools.getWsdl2URL()%>" class="icon-link"
                       style="background-image:url(../service-mgt/images/wsdl.gif);" target="_blank">
                        <fmt:message key="wsdl2"/>
                    </a>
                    <a href="#" class="icon-link" style="background-image:url(../cg/images/endpoints-icon.gif);"
                       onclick="goToEPRPage('<%=serviceName%>');"><fmt:message key="csg.epr"/></a>
                    <%
                    } else {
                    %>
                    <span style="color: red; "><fmt:message key="csg.no.tool.reason"/></span>
                    <%
                        }
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (msg != null && !msg.equals("")) {
                        // FIXME - due to some reason original error was not available so used the
                        // following general message
                    %>
                    <span style="color: red; "><fmt:message key="csg.no.tool.reason"/>
                    </span>
                    <%
                    } else {
                    %>
                    <span style="color: red; "><fmt:message key="csg.no.tool.connection.refused"/></span>
                    <%
                            }
                        }
                    %>
                </td>
                <%
                        }
                    }
                %>
            </tr>
            <%
            } else {
                if(CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PUBLISH_SERVICE_PERMISSION_STRING)){
            %>
            <tr>
                <td>
                    <%=serviceName%>
                </td>
                <td><%=CGConstant.CG_SERVICE_STATUS_UNPUBLISHED%>
                </td>
                <td>
                    <a href="#" class="icon-link"
                       style="background-image:url(../cg/images/publish.png);"
                       onclick="goToPublishingOptions('<%=serviceName%>', '<%=CGConstant.CG_SERVICE_ACTION_AUTOMATIC%>')">
                        <fmt:message key="csg.service.publish"/></a>
                </td>
                <td></td>
            </tr>
            <%
                                }
                            }
                        }
                        }
                    }
                }
            %>
            <tr id="rotating_indicator_id" style="border: 0; !important;display: none">
                <td style="border: 0; !important">
                    <div style="position: absolute;margin-top:-5px;margin-left:-10px;padding-left:20px;background:transparent url(../cg/images/loading-small.gif) no-repeat left top;">
                        <fmt:message key="csg.action.in.progress"/></div>
                </td>
            </tr>
            </tbody>
        </table>

        <table style="<%=noCSGServiceStyle%>">
            <thead>
            <tr>
                <th><fmt:message key="csg.no.permission"/></th>
            </tr>
            </thead>
        </table>

        <script type="text/javascript">
            alternateTableRows('csg_service_table_id', 'tableEvenRow', 'tableOddRow');
        </script>
        </form>
    <%
        }
    %>
    </div>
</div>

<script type="text/javascript">

    function goToEPRPage(serviceName) {
        location.href = 'display-epr-list.jsp?serviceName=' + serviceName;
    }

    function searchServices() {
        document.searchForm.submit();
    }

    function changeServiceStatus(serviceName, action, isAutomatic) {
        if (action == '<%=CGConstant.CG_SERVICE_ACTION_UNPUBLISH%>' || action == '<%=CGConstant.CG_SERVICE_ACTION_RESTART%>' ||
                action == '<%=CGConstant.CG_SERVICE_ACTION_AUTOMATIC%>') {
            CARBON.showConfirmationDialog("<fmt:message key="csg.are.you.sure"/> ", function () {
                document.getElementById('rotating_indicator_id').style.display = "";
                jQuery.get("../cg/publish-service_ajaxprocessor.jsp", {'serviceName':serviceName, 'action':action},
                        function (data, status) {
                            handleCallBack(data, status, action, isAutomatic);
                        });
            });

        } else {
            CARBON.showWarningDialog("<fmt:message key="csg.invalid.selection"/>");
        }
    }

    function changeServiceStatusToManual(serviceName, action) {
        if (action == '<%=CGConstant.CG_SERVICE_ACTION_MANUAL%>') {
            CARBON.showConfirmationDialog("<fmt:message key="csg.are.you.sure"/> ", function () {
                document.getElementById('rotating_indicator_id').style.display = "";
                jQuery.get("../cg/publish-service_ajaxprocessor.jsp", {'serviceName':serviceName, 'action':action},
                        function (data, status) {
                            handleCallBack(data, status, action, false);
                        });
            });
        } else {
            CARBON.showWarningDialog("<fmt:message key="csg.invalid.selection"/>");
        }
    }


    function handleCallBack(data, status, action, isAutomatic) {
        if (action == '<%=CGConstant.CG_SERVICE_ACTION_UNPUBLISH%>' ||
                action == '<%=CGConstant.CG_SERVICE_ACTION_RESTART%>' ||
                action == '<%=CGConstant.CG_SERVICE_ACTION_AUTOMATIC%>') {
            if (isAutomatic) {
                document.getElementById('rotating_indicator_id').style.display = "none";
            } else {
                document.getElementById('rotating_indicator_id').style.display = "none";
            }
        } else if (action == '<%=CGConstant.CG_SERVICE_ACTION_MANUAL%>') {
            document.getElementById('rotating_indicator_id').style.display = "none";
        }

        if (data.replace(/^\s+|\s+$/g, '') != 'successful') {
            CARBON.showErrorDialog(data);
        } else {
            if (action == '<%=CGConstant.CG_SERVICE_ACTION_UNPUBLISH%>') {
                CARBON.showInfoDialog('<fmt:message key="service.unpublished.sucessfully"/>',
                        function () {
                            location.href = 'forward-to.jsp';
                        },
                        null);
            } else if (action == '<%=CGConstant.CG_SERVICE_ACTION_RESTART%>') {
                CARBON.showInfoDialog('<fmt:message key="service.restarted.sucessfully"/>',
                        function () {
                            location.href = 'forward-to.jsp';
                        },
                        null);

            } else if (action == '<%=CGConstant.CG_SERVICE_ACTION_MANUAL%>') {
                CARBON.showInfoDialog('<fmt:message key="service.moved.manual"/>',
                        function () {
                            location.href = 'forward-to.jsp';
                        },
                        null);

            } else if (action == '<%=CGConstant.CG_SERVICE_ACTION_AUTOMATIC%>') {
                CARBON.showInfoDialog('<fmt:message key="service.moved.auto"/>',
                        function () {
                            location.href = 'forward-to.jsp';
                        },
                        null);
            }
        }
    }

    function goToPublishingOptions(currentServiceName, action) {
        location.href = 'edit-service-publishing-options.jsp?forwardTo=service-list.jsp&serviceName=' + currentServiceName + '&action=' + action;
    }
</script>

<carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%= parameters%>"/>

</fmt:bundle>
