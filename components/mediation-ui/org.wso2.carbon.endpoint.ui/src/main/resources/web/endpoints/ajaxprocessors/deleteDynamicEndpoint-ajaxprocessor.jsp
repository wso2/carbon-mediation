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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointService" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String endpointName = request.getParameter("endpointName");
    String dynamicPageNumberStr = request.getParameter("dynamicPageNumber");

    int dynamicEnpointPageNumber = 0;
    if(dynamicPageNumberStr!=null){
        dynamicEnpointPageNumber = Integer.parseInt(dynamicPageNumberStr);
    }
    int numberOfDynamicPages = 0;

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    EndpointAdminClient client;
    String[] dynamicEndpoints = null;
    try {
        client = new EndpointAdminClient(cookie, serverURL, configContext);
        client.deleteDynamicEndpoint(endpointName);
        dynamicEndpoints = client.getDynamicEndpoints(dynamicEnpointPageNumber, EndpointAdminClient.ENDPOINT_PER_PAGE);
        if (dynamicEndpoints != null && dynamicEndpoints.length == 0 && dynamicEnpointPageNumber != 0) {
            dynamicEnpointPageNumber--;
            dynamicEndpoints = client.getDynamicEndpoints(dynamicEnpointPageNumber, EndpointAdminClient.ENDPOINT_PER_PAGE);
        }

        int dynamicEpCount = client.getDynamicEndpointCount();
        if (dynamicEpCount % EndpointAdminClient.ENDPOINT_PER_PAGE == 0) {
            numberOfDynamicPages = dynamicEpCount / EndpointAdminClient.ENDPOINT_PER_PAGE;
        } else {
            numberOfDynamicPages = dynamicEpCount / EndpointAdminClient.ENDPOINT_PER_PAGE + 1;
        }
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
    <div id="noEpDiv"
         style="<%=dynamicEndpoints!=null ?"display:none":""%>">
        <fmt:message
                key="no.endpoints.in.registry"></fmt:message>

    </div>

    <%
        if ((dynamicEndpoints != null) && (dynamicEndpoints.length > 0)) {
    %>
    <script type="text/javascript">
        isDynamicSequenceFound = true;
    </script>
    <p><fmt:message key="endpoints.dynamic.text"/></p>
    <br/>
    <carbon:paginator pageNumber="<%=dynamicEnpointPageNumber%>"
                      numberOfPages="<%=numberOfDynamicPages%>"
                      page="index.jsp" pageNumberParameterName="dynamicPageNumber"
                      resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=""%>"/>
    <br/>
    <table class="styledLeft" cellspacing="1" id="dynamicEndpointsTable">
        <thead>
        <tr>
            <th style="width:30%">
                <fmt:message key="endpoint.name"/>
            </th>
            <th style="width:20%">
                <fmt:message key="type"/>
            </th>
            <th colspan="2">
                <fmt:message key="action"/>
            </th>
        </tr>
        </thead>
        <tbody>
        <% for (String endpoint : dynamicEndpoints) { %>
        <tr>
            <td>
                <%=endpoint %>
            </td>
            <td>
                <%
                    String epXML = client.getDynamicEndpoint(endpoint);
                    EndpointService epService = client.getEndpointService(epXML);
                %>

                <%=epService.getDisplayName()%>
            </td>
            <td style="border-right:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="editDynamicEndpoint('<%=endpoint%>')" class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"><fmt:message
                            key="edit"/></a>
                </div>
            </td>
            <td style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="deleteDynamicEndpoint('<%= endpoint %>')"
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
    <carbon:paginator pageNumber="<%=dynamicEnpointPageNumber%>" numberOfPages="<%=numberOfDynamicPages%>"
                          page="index.jsp" pageNumberParameterName="dynamicPageNumber"
                          resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>"/>
    <%
        }
    %>
    <br/>
</fmt:bundle>