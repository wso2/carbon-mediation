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
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.priority.executors.ui.PriorityAdminClient" %>
<%@ page import="java.util.List" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    PriorityAdminClient client = new PriorityAdminClient(getServletConfig(), session);

    List<String> executors = client.getExecutors();
%>

<script type="text/javascript" src="../carbon/global-params.js"></script>

<carbon:breadcrumb
            label="priority.menu.text"
            resourceBundle="org.wso2.carbon.priority.executors.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
<fmt:bundle basename="org.wso2.carbon.priority.executors.ui.i18n.Resources">

<script type="text/javascript">

    function deleteExecutor(name) {
        CARBON.showConfirmationDialog('Are you sure you want to delete the executor ' + escape(name) + '?', function () {
            jQuery.ajax({
                type: "POST",
                url: "delete_executor-ajaxprocessor.jsp",
                data: {"name": name},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("list_executors.jsp");
                    }
                }
            });
        });
    }

    function editExecutor(name) {
        window.location.href = "design_executors.jsp?name=" + name + "&mode=edit&action=none";
    }

    function addSequence() {
        window.location.href = "design_executors.jsp?mode=add&action=none";
    }
</script>

<style type="text/css">
    .inlineDiv div {
        float: left;
    }
</style>
    <div id="middle">
    <h2>
        <fmt:message key="priority.executers"/>
    </h2>

    <div id="workArea">
        <% if (executors.size() > 0) { %>
        <table class="styledLeft" cellspacing="1" id="sequencesTable">

            <thead>
            <tr>
                <th>
                    <fmt:message key="name"/>
                </th>
                <th colspan="2">
                    <fmt:message key="actions"/>
                </th>
            </tr>
            </thead>
            <tbody>
            <% for (String executor : executors) { %>
            <tr>
                <td>
                    <%= Encode.forHtmlContent(executor) %>
                </td>
                <td style="border-left:none;border-right:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="editExecutor('<%= executor %>')" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);">
                            <fmt:message key="edit"/></a>
                    </div>
                </td>
                <td style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="deleteExecutor('<%= executor %>')" class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);">
                            <fmt:message key="delete"/></a>
                    </div>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <% } else { %>

        <div style="height:25px;">
            <fmt:message key="priority.executors.are.not.defined"/>
        </div>
        <% } %>

        <script type="text/javascript">
            alternateTableRows('sequencesTable', 'tableEvenRow', 'tableOddRow');
        </script>

        <div style="height:25px;">
            <a class="icon-link" style="background-image: url(../admin/images/add.gif);"
               onclick="addSequence()"><fmt:message key="add.executor"/></a>
        </div>
    </div>
</fmt:bundle>
