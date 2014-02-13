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
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.Endpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointService" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointStore" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String endpointPosition = request.getParameter("childEndpointID");
    Endpoint endpoint = ListEndpointDesignerHelper.getEditingEndpoint(request, session);
    String logicalName = endpoint.getTagLocalName();
    EndpointStore store = EndpointStore.getInstance();
    EndpointService endpointService = store.getEndpointService(logicalName);
    String editPage = "../forms/"+endpointService.getUIPageName() + "Endpoint-form.jsp";
    String updatePage = "../updatePages/"+endpointService.getUIPageName() + "Endpoint-update.jsp";
    // set the endpoint position to the session
    session.setAttribute("endpoint.position", endpointPosition);

    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
%>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
    <div>
        <% if (endpointService.isChildEndpointFormAvailable()) { %>
        <form action="listEndpointDesigner/childEndpoint-update-ajaxprocessor.jsp" id="childEndpoint-editor-form"
              name="childEndpoint-editor-form">
            <jsp:include page="<%= editPage %>" flush="true">
                <jsp:param name="endpointPosition" value="<%=endpointPosition%>"/>
            </jsp:include>

            <table class="styledLeft" cellspacing="0" style="margin-left: 0px !important;">
                <tr class="buttonRow" style="border-top: solid 1px #ccc;">
                    <td>
                        <input type="hidden" id="childEndpointID" name="childEndpointID"
                               value="<%=endpointPosition%>"/>
                        <input type="hidden" id="updatePage" name="updatePage"
                               value="<%=updatePage%>"/>
                        <input type="hidden" name="random" value="<%=Math.random()%>"/>
                        <input type="button" class="button"
                               onclick="javascript: updateChildEndpoint('<%= endpointService.getUIPageName() %>', false); return false;"
                               value="Update"/>
                    </td>
                    <td id="whileUpload" style="display:none">
                        <img align="top" src="../resources/images/ajax-loader.gif"/>
                        <span><fmt:message key="listendpointdesigner.childform.update"/></span>
                    </td>
                </tr>
            </table>
        </form>
        <% } else {%>
        <script type="text/javascript">
            hideChildEndpointTab();
        </script>
        <% } %>
    </div>
</fmt:bundle>