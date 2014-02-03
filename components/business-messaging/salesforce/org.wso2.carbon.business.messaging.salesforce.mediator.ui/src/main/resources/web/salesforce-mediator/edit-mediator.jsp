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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@page import="org.apache.synapse.util.xpath.SynapseXPath" %>

<%@page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.SalesforceMediator" %>
<%@page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.factory.OperationBuilder" %>

<%@page import="java.util.List" %>
<%@page import="java.util.Queue" %>
<%@page import="java.util.LinkedList" %>

<%@page import="java.io.File" %>
<%@ page
        import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.config.SalesforceUIHandler" %>
<%@ page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.OperationType" %>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<script type="">
    YAHOO.util.Event.onDOMReady(function() {
        var selectElem = document.getElementById("mediator.salesforce.operation_name");
        loadConfigedInputs(selectElem[selectElem.selectedIndex].value);

    });
</script>
<!--

loadConfigedInputs(selectElem[selectElem.selectedIndex].value);
loadConfigedOutputs(selectElem[selectElem.selectedIndex].value);
-->
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();


    String repo = "";
    String axis2XML = "";

    if (!(mediator instanceof SalesforceMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SalesforceMediator salesforceMediator = (SalesforceMediator) mediator;
    SalesforceUIHandler handler = salesforceMediator.getHandler();

    String[] permittedOps = salesforceMediator.getHandler().getOperationParameters();
    if (salesforceMediator.getClientRepository() != null) {
        repo = salesforceMediator.getClientRepository();
    }
    if (salesforceMediator.getAxis2xml() != null) {
        axis2XML = salesforceMediator.getAxis2xml();
    }

%>

<fmt:bundle basename="org.wso2.carbon.business.messaging.salesforce.mediator.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.business.messaging.salesforce.mediator.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="salesforceMediatorJsi18n"/>
    <div>
        <script type="text/javascript" src="../salesforce-mediator/js/mediator-util.js"></script>
        <script type="text/javascript" src="../resources/js/resource_util.js"></script>
        <script type="text/javascript" src="../ajax/js/prototype.js"></script>

        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.salesforce.header"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <h3 class="mediator">
                        <fmt:message key="mediator.salesforce.operationDetails"/>
                    </h3>

                    <%
                        OperationType operation = salesforceMediator.getOperation();
                        String selectedOperation = null;
                        if (null == operation) {
                            selectedOperation = permittedOps[1];
                        } else {
                            selectedOperation = operation.getName();
                        }

                        int index = handler.getOperationPosition(selectedOperation, permittedOps);
                        /*System.out.println("OK..selected op");*/
                    %>
                    <table border="0" class="normal" width="100%">
                        <tr>
                            <td class="leftCol-small"><fmt:message
                                    key="mediator.salesforce.operationName"/></td>
                            <td>
                                <select id="mediator.salesforce.operation_name"
                                        name="mediator.salesforce.operation_name"
                                        onchange="loadConfigedInputs(this[this.selectedIndex].value)">
                                    <!--select id="mediator.salesforce.operation_name" name="mediator.salesforce.operation_name" -->

                                    <% int i = 0;
                                        for (String permittedOp : permittedOps) {
                                            if (i == index) {
                                    %>
                                    <option value="<%=permittedOp%>" selected="selected">
                                        <%=handler.getOperationDisplayName(permittedOp)%>
                                    </option>
                                    <%
                                    } else {%>
                                    <option value="<%=permittedOp%>">
                                        <%=handler.getOperationDisplayName(permittedOp)%>
                                    </option>
                                    <% }
                                        i++;
                                    }
                                    %>

                                        <%--<%--%>
                                        <%--if ("login".equals(selectedOperation)) {--%>
                                        <%--%>--%>
                                        <%--<option value="login" selected="selected">Login</option>--%>
                                        <%--<option value="query">Query</option>--%>
                                        <%--<option value="logout">Logout</option>--%>
                                        <%--<%--%>
                                        <%--} else if ("query".equals(selectedOperation)) {--%>
                                        <%--%>--%>
                                        <%--<option value="login">Login</option>--%>
                                        <%--<option value="query" selected="selected">Query</option>--%>
                                        <%--<option value="logout">Logout</option>--%>
                                        <%--<%--%>
                                        <%--} else if ("logout".equals(selectedOperation)) {--%>
                                        <%--%>--%>
                                        <%--<option value="login">Login</option>--%>
                                        <%--<option value="query">Query</option>--%>
                                        <%--<option value="logout" selected="selected">Logout</option>--%>
                                        <%--<%--%>
                                        <%--}--%>
                                        <%--%>--%>
                                </select>
                            </td>
                        </tr>
                    </table>
                    <div id="configInputs"></div>
                    <br>
                    <!--div id="configOutputs"></div-->
                </td>
            </tr>
            <tr>
                <td>
                    <h3 class="mediator"><fmt:message
                            key="mediator.salesforce.optionalAxis2Config"/></h3>
                    <table border="0" class="normal">
                        <tr>
                            <td class="leftCol-small">
                                <fmt:message key="mediator.salesforce.repo"/>
                            </td>
                            <td>
                                <input type="text" size="40" id="mediator.salesforce.repo"
                                       name="mediator.salesforce.repo" value="<%=repo%>"
                                       style="width:300px"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key="mediator.salesforce.axis2XML"/>
                            </td>
                            <td>
                                <input type="text" size="40" id="mediator.salesforce.axis2XML"
                                       name="mediator.salesforce.axis2XML" value="<%=axis2XML%>"
                                       style="width:300px"/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <span class="required">*</span> <fmt:message
                                    key="mediator.salesforce.defaultconfig"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>
</fmt:bundle>