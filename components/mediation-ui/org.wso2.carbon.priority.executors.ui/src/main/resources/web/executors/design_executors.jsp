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
<%@ page import="org.wso2.carbon.priority.executors.ui.Executor" %>
<%@ page import="org.wso2.carbon.priority.executors.ui.Queue" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.priority.executors.ui.PriorityAdminClient" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%
    String executorName = request.getParameter("name");
    String mode = request.getParameter("mode");
    String action = request.getParameter("action");
    PriorityAdminClient client = new PriorityAdminClient(getServletConfig(), session);

    Executor executor = null;
    if (action.equals("none")) {
        if (mode.equals("add")) {
            executor = new Executor();
        } else if (mode.equals("edit")) {
            executor = client.getExecutor(executorName);
        }
    } else if (action.equals("design")) {
        // in this case we take the executor from the session
        executor = (Executor) session.getAttribute("source_executor");
    }
%>

<link type="text/css" rel="stylesheet" href="css/tree-styles.css"/>
<link type="text/css" rel="stylesheet" href="css/menu-styles.css"/>

<style type="text/css">

</style>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<fmt:bundle basename="org.wso2.carbon.priority.executors.ui.i18n.Resources">

<script type="text/javascript">

    var ROW_BASE = 1; // first number (for display)

    function dbrDeleteCurrentRow(obj) {
        var delRow = obj.parentNode.parentNode;
        var tbl = delRow.parentNode.parentNode;
        var rIndex = delRow.sectionRowIndex;
        var rowArray = new Array(delRow);
        dbrDeleteRows(rowArray);
        if (tbl.tBodies[0].rows.length == 0) {
            tbl.style.display = 'none';
        }
    }

    function dbrDeleteRows(rowObjArray) {
        for (var i = 0; i < rowObjArray.length; i++) {
            var rIndex = rowObjArray[i].sectionRowIndex;
            rowObjArray[i].parentNode.deleteRow(rIndex);
        }
    }

    function dbrAddStmtResultsTableRow(tableName, num) {
        var tbl = document.getElementById(tableName);
        var nextRow = tbl.tBodies[0].rows.length;
        var noTwo = 0;
        var hidden = document.getElementById(("hidden_queues"));
        var val = hidden.value;
        if (val != null && val != "") {
            noTwo = parseInt(val);
        }
        if (tbl.tBodies[0].rows.length == 0) {
            tbl.style.display = '';
        }

        if (num == null) {
            num = nextRow;
        }

        var isFixed = true;
        var ele = document.getElementById('isFixed');
        if (ele && ele.value == "false") {
            isFixed = false;
        } else if (ele && ele.value == "true") {
            isFixed = true;
        }

        var row = tbl.tBodies[0].insertRow(num);

        var cell0 = row.insertCell(0);
        var txtInp = document.createElement('input');
        txtInp.setAttribute('type', 'text');
        txtInp.setAttribute('name', ('priority' + noTwo));
        txtInp.setAttribute('id', ('priority' + noTwo));
        txtInp.setAttribute('value', "");
        cell0.appendChild(txtInp);

        var cell1 = row.insertCell(1);
        YAHOO.util.Dom.addClass(cell1, 'capacityCol');
        if (!isFixed) {
            cell1.style.display = "none";
        }
        txtInp = document.createElement('input');
        txtInp.setAttribute('type', 'text');
        txtInp.setAttribute('value', "");
        txtInp.setAttribute('name', ('capacity' + noTwo));
        txtInp.setAttribute('id', ('capacity' + noTwo));
        cell1.appendChild(txtInp);

        var cell2 = row.insertCell(2);
        cell2.innerHTML = "<a onclick=\"dbrDeleteCurrentRow(this);return false;\" class=\"delete-icon-link\">Delete</a>";

        hidden.setAttribute("value", (noTwo + 1));
    }

    function sourceView() {
        if (!validate()) {
            return;
        }

        var ele = document.getElementById("forwardTo");
        ele.value = "priority_source.jsp";
        ele = document.getElementById("action");
        ele.value = "source";
        ele = document.getElementById("mode");
        ele.value = "<%=mode%>";

        ele = document.getElementById("executor.name_1");
        var ele_1 = document.getElementById("executor.name");
        ele_1.value = ele.value;

        //document.main_form.action = "save_design.jsp";
        document.main_form.submit();
    }

    function cancelExecutor() {
        window.location.href = "list_executors.jsp";
    }

    function saveExecutor() {
        if (!validate()) {
            return;
        }

        var ele = document.getElementById("forwardTo");
        ele.value = "list_executors.jsp";
        ele = document.getElementById("action");
        ele.value = "none";
        ele = document.getElementById("mode");
        ele.value = "<%=mode%>";

        ele = document.getElementById("executor.name_1");
        var ele_1 = document.getElementById("executor.name");
        ele_1.value = ele.value;

//        document.main_form.action = "save_design.jsp";
        document.main_form.submit();
    }

    function applyExecutor() {
        if (!validate()) {
            return;
        }
        var ele = document.getElementById("forwardTo");
        ele.value = "list_executors.jsp";
        ele = document.getElementById("action");
        ele.value = "none";
        ele = document.getElementById("mode");
        ele.value = "<%=mode%>";

        ele = document.getElementById("executor.name_1");
        var ele_1 = document.getElementById("executor.name");
        ele_1.value = ele.value;

//        document.main_form.action = "save_design.jsp";
        document.main_form.submit();
    }

    function validate() {
        var regEx = /[~!@#$%^&*()\\\/+=\:;<>'"?[\]{}|\s,]|^$/;
        var ele = document.getElementById("executor.name_1");
        if (ele && regEx.test(ele.value)) {
            CARBON.showErrorDialog("Name is empty or contains invalid characters");
            return false;
        }

        var isFixed = true;
        ele = document.getElementById('isFixed');
        if (ele && ele.value == "false") {
            isFixed = false;
        } else if (ele && ele.value == "true") {
            isFixed = true;
        }

        ele = document.getElementById("hidden_queues");
        var size = parseInt(ele.value);
        var count = 0;
        for (var i = 1; i < size; i++) {
            var e1 = document.getElementById("priority" + i);
            if (e1 && e1.value == "") {
                CARBON.showErrorDialog("Priority must be specified for a queue");
                return false;
            } else if (e1) {
                count++;
                if (isNaN(e1.value)) {
                    CARBON.showErrorDialog("Priority must be a number");
                    return false;
                }
            }

            if (isFixed) {
                e1 = document.getElementById("capacity" + i);
                if (e1 && e1.value == "") {
                    CARBON.showErrorDialog("Capacity must be specified for a queue");
                    return false;
                } else if (e1) {
                    if (isNaN(e1.value)) {
                        CARBON.showErrorDialog("Capacity must be a number");
                        return false;
                    }
                }
            }
        }

        if (count <= 1) {
            CARBON.showErrorDialog("Two or more queues must be specified");
            return false;
        }

        ele = document.getElementById("max");
        if (ele && ele.value == "") {
            CARBON.showErrorDialog("Max thread count is required");
            return false;
        }

        ele = document.getElementById("core");
        if (ele && ele.value == "") {
            CARBON.showErrorDialog("Core thread count is required");
            return false;
        }

        ele = document.getElementById("keep_alive");
        if (ele && ele.value == "") {
            CARBON.showErrorDialog("Keep-Alive thread count is required");
            return false;
        }

        return true;
    }

    function optionChanged() {
        var elms = YAHOO.util.Dom.getElementsByClassName('capacityCol', 'td');
        var isFixed = true;
        var ele = document.getElementById('isFixed');
        if (ele && ele.value == "false") {
            isFixed = false;
        } else if (ele && ele.value == "true") {
            isFixed = true;
        }

        if (isFixed) {
            ele = document.getElementById("size_th");
            ele.style.display = "";
            for(var i=0;i<elms.length;i++){
                elms[i].style.display = "";
            }

        } else {
            ele = document.getElementById("size_th");
            ele.style.display = "none";
            for(var i=0;i<elms.length;i++){
                elms[i].style.display = "none";
            }
        }
    }

</script>

    <carbon:breadcrumb
		label="priority.design.text"
		resourceBundle="org.wso2.carbon.priority.executors.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />

<div id="middle">
    <h2><fmt:message key="priority.design.text"/></h2>

    <div id="workArea">
        <form name="main_form" id="main_form" action="save_design.jsp">
        <table class="styledLeft noBorders" cellspacing="0" style="width:100%">
            <thead>
            <tr>
                <th colspan="2">
                    <span style="float:left; position:relative; margin-top:2px;">
                            <fmt:message key="priority.executor.design.view"/></span>
                    <a href="#" onclick="sourceView()" class="icon-link"
                       style="background-image:url(images/source-view.gif);">
                        <fmt:message key="swith.to.source.view"/></a>
                </th>
            </tr>
            </thead>
            <tbody>

            <tr>
                <td class="leftCol-small" style="white-space:nowrap;">
                    <fmt:message key="executor.name"/><span class="required">*</span>
                </td>
                <td align="left">
                    <input type="text" id="executor.name_1" name="executor.name_1"
                           value="<%= executor.getName() != null ? executor.getName() : "" %>" <%= "edit".equals(mode) ? "disabled=\"disabled\"" : "" %>
                            />
                    <input type="hidden" id="executor.name" name="executor.name"
                           value="<%= executor.getName() != null ? executor.getName() : "" %>"/>
                </td>
            </tr>

            <tr>

                <td class="leftCol-small" style="white-space:nowrap;">
                    <fmt:message key="fixed.size.queues"/><span class="required">*</span>
                </td>
                <td align="left">
                    <select name="isFixed" id="isFixed" onchange="optionChanged()">
                        <option value="true" <%=executor.isFixedSize() ? "selected=\"selected\"" : ""%>>true</option>
                        <option value="false" <%=!executor.isFixedSize() ? "selected=\"selected\"" : ""%>>false</option>
                    </select>
                </td>

            </tr>
            <tr>
                <td colspan="2" class="middle-header"><fmt:message key="queues"/></td>
            </tr>
            <tr>
                <td colspan="2" style="padding-top:10px;">
                    <table class="styledInner" width="100%" id="queues" >
                        <thead>
                            <tr>
                                <th><fmt:message key="priority"/></th>
                                <th id="size_th" <%=!executor.isFixedSize()? "style=\"display:none\"" : ""%>><fmt:message key="size"/></th>
                                <th><fmt:message key="action"/></th>
                            </tr>
                        </thead>
                        <tbody>
                        <% for (int i = 1; i <= executor.getQueues().size(); i++) { %>
                        <tr id="">
                            <td width="5%" style="white-space:nowrap;">
                               <input id="priority<%=i%>" name="priority<%=i%>"
                                      value="<%=executor.getQueues().get(i - 1).getPriority()%>"/>
                            </td>
                            <td width="5%" class="capacityCol" <%=!executor.isFixedSize()? "style=\"display:none;white-space:nowrap;\"" : "white-space:nowrap;"%>>
                               <input id="capacity<%=i%>" name="capacity<%=i%>"
                                      value="<%=executor.getQueues().get(i - 1).getCapacity()%>"/>
                            </td>
                            <td><a onclick="dbrDeleteCurrentRow(this);" class="delete-icon-link" href="#"><fmt:message key="delete"/></a></td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                    <div>
                        <a class="icon-link" style="background-image: url(../admin/images/add.gif);"
                           onclick="dbrAddStmtResultsTableRow('queues', null)">Add Queue</a>
                    </div>
                </td>
            </tr>
            <input type="hidden" value="<%=executor.getQueues().size() + 1%>" id="hidden_queues" name="hidden_queues"/>
            <tr>
                <td class="middle-header" colspan="2"><fmt:message key="next.queue.algoritm"/></td>
            </tr>
            <tr>
                <td><fmt:message key="class"/></td>
                <td><input id="nextQueue" name="nextQueue"
                           value="<%=executor.getAlgorithm() != null ? executor.getAlgorithm() : ""%>"/></td>
            </tr>
            <tr>
                <td class="middle-header" colspan="2"><fmt:message key="threads"/></td>
            </tr>

            <tr>
                <td width="5%" style="white-space:nowrap;">
                    <fmt:message key="core"/>
                </td>
                <td align="left">
                    <input type="text" id="core" name="core"
                           value="<%=executor.getCore()%>"/>
                </td>
            </tr>
            <tr>
                <td width="5%" style="white-space:nowrap;">
                    <fmt:message key="max"/>
                </td>
                <td align="left">
                    <input type="text" id="max" name="max"
                           value="<%=executor.getMax()%>"/>
                </td>
            </tr>
            <tr>
                <td width="5%" style="white-space:nowrap;">
                    <fmt:message key="keep.alive"/>
                </td>
                <td align="left">
                    <input type="text" id="keep_alive" name="keep_alive"
                           value="<%=executor.getKeepAlive()%>"/>
                </td>

            </tr>

            <tr>
                <td class="buttonRow" colspan="2">
                    <input type="button" class="button" value="<fmt:message key="save"/>"
                           id="saveButton" onclick="saveExecutor(); return false;"/>
                    <%--<input type="button" class="button" value="<fmt:message key="apply"/>"--%>
                           <%--id="applyButton" onclick="applyExecutor(); return false;"/>--%>
                    <input type="button" class="button" value="<fmt:message key="cancel"/>"
                           onclick="cancelExecutor(); return false;"/>
                </td>
            </tr>
            </tbody>
        </table>
            <input type="hidden" name="forwardTo" id="forwardTo" value=""/>
            <input type="hidden" name="action" id="action" value=""/>
            <input type="hidden" name="mode" id="mode" value=""/>
        </form>
    </div>
</div>

</fmt:bundle>


