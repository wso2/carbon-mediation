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
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.loadbalance.LoadBalanceEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>

<%
    LoadBalanceEndpoint endpoint = (LoadBalanceEndpoint) ListEndpointDesignerHelper.getEditingEndpoint(request, session);

    String[] sessionManOptions = {"SelectAValue", "http", "soap", "simpleClientSession"};
    boolean isDefault = true, isTransport = false, isSOAP = false, isClientID = false;
    long sessionTimeout = 0;

    if (endpoint != null) {
        String sessionType = endpoint.getSessionType();
        if (sessionType != null) {
            if (sessionType.equals("http")) {
                isTransport = true;
            } else if (sessionType.equals("soap")) {
                isSOAP = true;
            } else if (sessionType.equals("simpleClientSession")) {
                isClientID = true;
            }
        }
        sessionTimeout = endpoint.getSessionTimeout();
    }
%>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
            request="<%=request%>"/>

    <script type="text/javascript">

        function activateSessionManagementField(selectNode) {
            var selectOption = selectNode.options[selectNode.selectedIndex].value;
            var sessionTimeOut = document.getElementById('sessionTimeOutValue');
            if (selectOption != null && selectOption != undefined) {
                if (selectOption == 'SelectAValue') {
                    if (sessionTimeOut != null && sessionTimeOut != undefined) {
                        sessionTimeOut.disabled = 'disabled';
                        sessionTimeOut.value = 0;
                    }
                } else {
                    if (sessionTimeOut != null && sessionTimeOut != undefined) {
                        sessionTimeOut.disabled = '';
                    }
                }
            }
        }

        function validateForm() {
            return true;
        }

    </script>

    <table class="normal" width="100%">
        <tr>
            <td class="leftCol-small"><fmt:message
                    key="session.management"/></td>
            <td><select name="sessionManagementCombo" id="sesOptions"
                        onchange="activateSessionManagementField(this)">
                <option value="<%=sessionManOptions[0]%>" <%=isDefault ? "selected" : ""%>>
                    <fmt:message key="none"/></option>
                <option value="<%=sessionManOptions[1]%>" <%=isTransport ? "selected" : ""%>>
                    <fmt:message key="transport"/></option>
                <option value="<%=sessionManOptions[2]%>" <%=isSOAP ? "selected" : ""%>>
                    <fmt:message key="soap"/></option>
                <option value="<%=sessionManOptions[3]%>" <%=isClientID ? "selected" : ""%>>
                    <fmt:message key="client.id"/></option>
            </select></td>
        </tr>
        <tr>
            <td class="leftCol-small">
                <fmt:message key="endpoint.session.timeout"/>
            </td>
            <td>
                <input type="text" name="sessionTimeOutValue" id="sessionTimeOutValue"
                       value="<%=sessionTimeout%>"
                        <%=sessionTimeout == 0 ? "disabled=\"disabled\"" : ""%>
                       onkeypress="return validateText(event);"/>
            </td>
        </tr>
    </table>

</fmt:bundle>