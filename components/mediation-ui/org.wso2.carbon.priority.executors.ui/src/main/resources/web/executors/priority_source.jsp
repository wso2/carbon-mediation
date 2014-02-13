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
<%@ page import="org.wso2.carbon.priority.executors.ui.PriorityAdminClient" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.priority.executors.ui.i18n.Resources", request.getLocale());
    String forwardTo = request.getParameter("forwardTo");
    String action = request.getParameter("action");
    String mode = request.getParameter("mode");

    Executor executor = (Executor) session.getAttribute("source_executor");

    String sourceXML = executor.serialize().toString();

    ByteArrayInputStream byteArrayInputStream
                = new ByteArrayInputStream(sourceXML.getBytes());
    XMLPrettyPrinter printer = new XMLPrettyPrinter(byteArrayInputStream);
    sourceXML = printer.xmlFormat();
%>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<script type="text/javascript">
    function cancelExecutor() {
        document.location.href = "list_executors.jsp";
    }

    function saveExecutor() {
        var ele = document.getElementById("forwardTo");
        ele.value = "list_executors.jsp";
        ele = document.getElementById("action");
        ele.value = "save";
        ele = document.getElementById("mode");
        ele.value = "<%=mode%>";

        document.getElementById('sourceXML_Hidden').innerHTML = editAreaLoader.getValue("source");
        //document.source_form.action = "save_source.jsp";
        document.source_form.submit();
    }

    function designView() {
        var ele = document.getElementById("forwardTo");
        ele.value = "design_executors.jsp";
        ele = document.getElementById("action");
        ele.value = "design";
        ele = document.getElementById("mode");
        ele.value = "<%=mode%>";
        //document.source_form.action = "save_source.jsp";
        document.source_form.submit();
    }
</script>

<fmt:bundle basename="org.wso2.carbon.priority.executors.ui.i18n.Resources">


<carbon:breadcrumb
        label="priority.executor.source"
        resourceBundle="org.wso2.carbon.priority.executors.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

    <div id="middle">
    <h2><fmt:message key="priority.executor.source"/></h2>

    <div id="workArea">
        <form action="save_source.jsp" method="post" id="source_form" name="source_form">
            <table class="styledLeft" cellspacing="0" cellpadding="0">
                <thead>
                <tr>
                    <th>
                        <span style="float:left; position:relative; margin-top:2px;">
                            <fmt:message key="priority.executor.source.view"/></span>
                        <a href="#" onclick="designView()" class="icon-link" style="background-image:url(images/design-view.gif);">
                            <fmt:message key="priority.executor.design.view"/></a>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <textarea rows="30" id="source"
                                  style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;">
                            <%= sourceXML %>
                        </textarea>
                        <textarea style="display:none" name="sourceXML" id="sourceXML_Hidden">
                            <%= sourceXML %>
                        </textarea>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" class="button" onclick="saveExecutor();"
                               value="<fmt:message key="save"/>"/>
                        <input type="button" class="button" onclick="cancelExecutor(); return false;"
                               value="<fmt:message key="cancel"/>"/>
                    </td>
                </tr>
                </tbody>
            </table>
            <input type="hidden" name="forwardTo" id="forwardTo" value="<%=forwardTo%>"/>
            <input type="hidden" name="action" id="action" value="<%=action%>"/>
            <input type="hidden" name="mode" id="mode" value="design"/>
        </form>
    </div>
</div>
     <script type="text/javascript">
        editAreaLoader.init({
            id : "source"		// textarea id
            ,syntax: "xml"			// syntax to be uses for highgliting
            ,start_highlight: true		// to display with highlight mode on start-up
        });
    </script>
</fmt:bundle>

