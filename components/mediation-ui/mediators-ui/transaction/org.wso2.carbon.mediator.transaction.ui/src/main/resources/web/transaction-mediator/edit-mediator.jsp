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
<%@ page import="org.wso2.carbon.mediator.transaction.TransactionMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof TransactionMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    TransactionMediator transactionMediator = (TransactionMediator) mediator;
%>

<fmt:bundle basename="org.wso2.carbon.mediator.transaction.ui.i18n.Resources">
<div>
<script type="text/javascript" src="../transaction-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.transaction.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.transaction.action"/></td>
                <td>
                    <select id="mediator.transaction.transaction_action" name="mediator.transaction.transaction_action">
                    <%
                        if (TransactionMediator.ACTION_COMMIT.equals(transactionMediator.getAction())) {
                    %>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>" selected="true"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        } else if (TransactionMediator.ACTION_FAULT_IF_NO_TX.equals(transactionMediator.getAction())) {
                    %>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>" selected="true"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        } else if (TransactionMediator.ACTION_NEW.equals(transactionMediator.getAction())) {
                    %>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>" selected="true"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        } else if (TransactionMediator.ACTION_RESUME.equals(transactionMediator.getAction())) {
                    %>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>" selected="true"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        } else if (TransactionMediator.ACTION_ROLLBACK.equals(transactionMediator.getAction())) {
                    %>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>" selected="true"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        } else if (TransactionMediator.ACTION_SUSPEND.equals(transactionMediator.getAction())) {
                    %>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>" selected="true"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        } else if (TransactionMediator.ACTION_USE_EXISTING_OR_NEW.equals(transactionMediator.getAction())) {
                    %>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>" selected="true"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        } else {
                    %>
                        <option value="NONE"><fmt:message key="mediator.transaction.action.select"/></option>
                        <option value="<%=TransactionMediator.ACTION_COMMIT%>"><fmt:message key="mediator.transaction.action.commit"/></option>
                        <option value="<%=TransactionMediator.ACTION_FAULT_IF_NO_TX%>"><fmt:message key="mediator.transaction.action.fault"/></option>
                        <option value="<%=TransactionMediator.ACTION_NEW%>"><fmt:message key="mediator.transaction.action.new"/></option>
                        <option value="<%=TransactionMediator.ACTION_RESUME%>"><fmt:message key="mediator.transaction.action.resume"/></option>
                        <option value="<%=TransactionMediator.ACTION_ROLLBACK%>"><fmt:message key="mediator.transaction.action.rollback"/></option>
                        <option value="<%=TransactionMediator.ACTION_SUSPEND%>"><fmt:message key="mediator.transaction.action.suspend"/></option>
                        <option value="<%=TransactionMediator.ACTION_USE_EXISTING_OR_NEW%>"><fmt:message key="mediator.transaction.action.existornew"/></option>
                    <%
                        }
                    %>
                    </select>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</div>
</fmt:bundle>