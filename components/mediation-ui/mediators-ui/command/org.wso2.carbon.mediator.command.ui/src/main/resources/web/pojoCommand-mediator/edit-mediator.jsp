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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.command.CommandMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CommandMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CommandMediator pojoCommandMediator = (CommandMediator) mediator;
    String commandClassName = pojoCommandMediator.getCommand();
%>
<fmt:bundle basename="org.wso2.carbon.mediator.command.ui.i18n.Resources">
<carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.command.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="commandi18n"/>
<div>
<script type="text/javascript" src="../pojoCommand-mediator/js/mediator-util.js"></script>
<script type="text/javascript">
    var val;
    jQuery('#actionID').click(function() {
        val = document.getElementById('mediatorInputId').value;
        var url = '../pojoCommand-mediator/command-ajaxprocessor.jsp';
        jQuery('#attribDescription').load(url, {mediatorInput: val, clearAll : 'true'},
                function(res, status, t) {
                    if (status != "success") {
                        CARBON.showErrorDialog('<fmt:message key="mediator.command.errmsg"/>');
                    }
                })
        return false;
    });
</script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.command.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.command.className"/><span class="required">*</span>
                </td>
                <td align="left">
                    <input type="text" id="mediatorInputId" name="mediatorInput" size="35"
                           value="<%= commandClassName !=null ? commandClassName : ""%>"/>
                </td>
                <td>
                    <input id="actionID" type="button" value="<fmt:message key="mediator.command.loadClass"/>"
                           class="button"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
<td>
<div class="Mediator" id="attribDescription">
    <% if (commandClassName != null && commandClassName.length() > 0) { %>
        <jsp:include page="../pojoCommand-mediator/command-ajaxprocessor.jsp">
            <jsp:param name="mediatorInput" value="<%= commandClassName%>"/>
        </jsp:include>
    <% } %>
</div>
</td>
</tr>
</table>
</div>
</fmt:bundle>
