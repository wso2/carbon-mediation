<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
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

<%@ page import="org.wso2.carbon.mediator.conditionalrouter.ConditionalRouterMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.mediator.conditionalrouter.ConditionalRouterMediator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ConditionalRouterMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    ConditionalRouterMediator conditionalRouterMediator = (ConditionalRouterMediator) mediator;
%>
<fmt:bundle basename="org.wso2.carbon.mediator.conditionalrouter.ui.i18n.Resources">
    <div>
        <script type="text/javascript">
            function addRoute() {
                document.location.href = "../conditionalrouter-mediator/add_route.jsp";
            }
        </script>

        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="conditional.router.mediator"/></h2>
                </td>
            </tr>

            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td>
                                <fmt:message key="continue.after.routing"/>
                            </td>
                            <td>
                                <select name="mediator.conditionalrouter.continue"
                                        id="mediator.conditionalrouter.continue">
                                    <option value="true" <%= conditionalRouterMediator.isContinueAfter() ?
                                        "selected=\"true\"" : ""%>>Yes</option>
                                <option value="false" <%= !conditionalRouterMediator.isContinueAfter() ?
                                        "selected=\"true\"" : ""%>>No</option>
                                </select>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <a class="add-icon-link"
                       href="#addRouterLink"
                       onclick="addRoute()">Add Route</a>
                </td>
            </tr>
        </table>
        <a name="addRouterLink"></a>
    </div>
</fmt:bundle>