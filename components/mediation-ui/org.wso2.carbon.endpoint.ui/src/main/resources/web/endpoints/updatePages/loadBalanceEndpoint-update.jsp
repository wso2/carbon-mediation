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
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.loadbalance.LoadBalanceEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>

<%
    String updatePage = request.getParameter("updatePage");

    if (updatePage != null) {    // As a child element of list Endpoint
        LoadBalanceEndpoint editingEndpoint = (LoadBalanceEndpoint) ListEndpointDesignerHelper.getEditingEndpoint(request, session);
        String sessionType = request.getParameter("sessionManagementCombo");
        String sessionTimeout = request.getParameter("sessionTimeOutValue");
        if (sessionType != null && !sessionType.equals("SelectAValue")) {
            editingEndpoint.setSessionType(sessionType);
            if (sessionTimeout != null) {
                editingEndpoint.setSessionTimeout(Long.parseLong(sessionTimeout));
            }
        } else {
            editingEndpoint.setSessionType(null);
        }
    } else {
        LoadBalanceEndpoint endpoint = (LoadBalanceEndpoint) ListEndpointDesignerHelper.getEditingListEndpoint(session);

        String name = request.getParameter("listEndpointName");
        String algo = request.getParameter("algoCombo");
        String algoClassName = request.getParameter("algoClassName");
        String sessionType = request.getParameter("sessionManagement");
        String sessionTimeout = request.getParameter("sessionTimeOut");
        String properties = request.getParameter("endpointProperties");

        if (name != null) {
            endpoint.setName(name);
        }
        if (algo != null && algo.equals("other") && algoClassName != null) {
            endpoint.setAlgorithmClassName(algoClassName);
        } else {
            endpoint.setAlgorithmClassName(EndpointConfigurationHelper.ROUNDROBIN_ALGO_CLASS_NAME);
        }
        if (sessionType != null && !sessionType.equals("SelectAValue")) {
            endpoint.setSessionType(sessionType);
            if (sessionTimeout != null) {
                endpoint.setSessionTimeout(Long.parseLong(sessionTimeout));
            }
        } else {
            endpoint.setSessionType(null);
        }
        if (properties != null) {
            endpoint.setProperties(properties);
        } else {
           endpoint.setProperties(null);
        }

        OMElement endpointElement = endpoint.serialize(null);
        session.setAttribute("endpointConfiguration", endpointElement.toString());
    }
%>
