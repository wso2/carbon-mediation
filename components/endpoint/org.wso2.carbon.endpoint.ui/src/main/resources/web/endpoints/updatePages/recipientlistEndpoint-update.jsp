<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.recipientlist.RecipientlistEndpoint" %>

<%
    String updatePage = request.getParameter("updatePage");

    if (updatePage == null) {
        RecipientlistEndpoint endpoint = (RecipientlistEndpoint) ListEndpointDesignerHelper.getEditingListEndpoint(session);

        String name = request.getParameter("listEndpointName");
        String properties = request.getParameter("endpointProperties");

        if (name != null) {
            endpoint.setName(name);
        }
        if (properties != null) {
            endpoint.setProperties(properties);
        }

        OMElement endpointElement = endpoint.serialize(null);
        session.setAttribute("endpointConfiguration", endpointElement.toString());
    }
%>
