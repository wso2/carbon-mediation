<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.mediator.router.RouteMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String val = "";
    if (!(mediator instanceof RouteMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    RouteMediator routeMediator = (RouteMediator) mediator;

    if(routeMediator.getExpression() != null){
        val = routeMediator.getExpression().toString();
        NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
        nameSpacesRegistrar.registerNameSpaces(routeMediator.getExpression(), "mediator.route.expression", session);    
    }
%>
<fmt:bundle basename="org.wso2.carbon.mediator.router.ui.i18n.Resources">
<div>
    <script type="text/javascript">
        function addRoute() {
            document.location.href = "../router-mediator/add_route.jsp";
        }
    </script>

    <table class="normal" width="100%">
        <tr>
            <td>
                <h2><fmt:message key="route.configuration"/></h2>
            </td>
        </tr>

        <tr>
            <td>
                <table class="normal">
                    <tr>
                        <td>
                            <fmt:message key="break.after.route"/>
                        </td>
                        <td>
                            <select name="mediator.route.break" id="mediator.route.break">
                                <option value="true" <%= routeMediator.isBreakRouter() ?
                                        "selected=\"true\"" : ""%>>Yes</option>
                                <option value="false" <%= !routeMediator.isBreakRouter() ?
                                        "selected=\"true\"" : ""%>>No</option>
                            </select>
                        </td>
                    </tr>

                    <tr>
                        <td>
                            <fmt:message key="route.expression"/><span class="required">*</span>
                        </td>
                        <td>
                           <input type="text" value="<%=val%>" name="mediator.route.expression" id="mediator.route.expression"/>
                        </td>
                        <td><a href="#nsEditorLink" id="mediator.route.expression.xpath_nmsp_button"
                           onclick="showNameSpaceEditor('mediator.route.expression')" class="nseditor-icon-link"
                           style="padding-left:40px">
                        <fmt:message key="namespaces"/></a>
                    </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="route.pattern"/></td>
                        <td>
                            <input type="text" value="<%= routeMediator.getMatch() != null ? routeMediator.getMatch() : "" %>" name="mediator.route.pattern" id="mediator.route.pattern"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
    <a name="nsEditorLink"></a>
</div>
</fmt:bundle>