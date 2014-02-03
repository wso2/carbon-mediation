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

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.Endpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.ListEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
    <%
        String childEndpointAction = request.getParameter("childEndpointAction");
        ListEndpoint editingListEndpoint = ListEndpointDesignerHelper.getEditingListEndpoint(session);
        String childEndpointPosition = "";
        if (childEndpointAction.equals("add")) {
            String childEndpointName = request.getParameter("childEndpointName");
            String position = request.getParameter("position");

            Endpoint newEndpoint = ListEndpointDesignerHelper.getNewEndpoint(childEndpointName);
            ListEndpoint parent = (ListEndpoint) ListEndpointDesignerHelper.getEndpointAt(
                    editingListEndpoint, position.substring(14)
            );
            parent.addChild(newEndpoint);

            if (position.equals("childEndpoint-00")) {
                childEndpointPosition = "childEndpoint-" + (parent.getList().size() - 1);
            } else {
                childEndpointPosition = position + "." + (parent.getList().size() - 1);
            }
        } else {
            String endpointPosition = request.getParameter("childEndpointID");
            if ("delete".equals(childEndpointAction)) {
                ListEndpointDesignerHelper.deleteEndpointAt(endpointPosition, session);
                ListEndpointDesignerHelper.clearSessionCache(session);
            }
        }
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers.
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
    %>
    <div style="position:absolute;padding:20px;">
        <ul class="root-list" id="endpointTree">
            <li>
                <div class="minus-icon"
                     onclick="treeColapse(this)"
                     id="treeColapser"></div>
                <div class="childEndpoints" id="childEndpoint-00">
                    <a class="root-endpoint">root</a>
                    <div class="endpointToolbar"
                         style="width:100px;">
                        <div>
                            <a class="addChildStyle"><fmt:message key="listendpointdesigner.add.child"/></a>
                        </div>
                    </div>
                </div>
                <%
                    int count = editingListEndpoint.getList().size();
                    if (count != 0) {
                %>
                <div class="branch-node"></div>
                <ul class="child-list">
                    <%
                        int position = 0;
                        for (Endpoint endpoint : editingListEndpoint.getList()) {
                            count--;
                    %>
                    <%=ListEndpointDesignerHelper.getEndpointHTML(endpoint, count == 0, String.valueOf(position), config, request.getLocale())%>
                    <%
                            position++;
                        }
                    %>
                </ul>
                <%
                    }
                %>
            </li>
        </ul>
        <% if (childEndpointAction.equals("add")) { %>
        <input id="addChildEndpointPosition" type="hidden" value="<%=childEndpointPosition%>"/>
        <% } %>
    </div>
</fmt:bundle>

