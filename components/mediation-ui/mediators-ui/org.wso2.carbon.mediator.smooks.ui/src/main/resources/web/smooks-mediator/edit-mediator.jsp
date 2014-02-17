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

<%@ page import="org.wso2.carbon.mediator.smooks.ui.SmooksMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    
    if (!(mediator instanceof SmooksMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SmooksMediator smooksMediator = (SmooksMediator) mediator;

    String inputType = smooksMediator.getInputType();
    String outputType = smooksMediator.getOutputType();

    String inputExpr = "";
    if (smooksMediator.getInputExpression() != null) {
        inputExpr = smooksMediator.getInputExpression().toString();
    }

    String outputExpr = "";
    if (smooksMediator.getOutputExpression() != null) {
        outputExpr = smooksMediator.getOutputExpression().toString();
    }

    String outputProperty = "";
    if (smooksMediator.getOutputProperty() != null) {
        outputProperty = smooksMediator.getOutputProperty();
    }

    String outputAction = "";
    if (smooksMediator.getOutputAction() != null) {
        outputAction = smooksMediator.getOutputAction();
    }

    boolean isExpression = smooksMediator.getOutputProperty() == null;

    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    nameSpacesRegistrar.registerNameSpaces(smooksMediator.getInputExpression(), "inputExpr", session);

    nameSpacesRegistrar.registerNameSpaces(smooksMediator.getOutputExpression(), "outputExpr", session);
%>

<fmt:bundle basename="org.wso2.carbon.mediator.smooks.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.smooks.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="propertyMediatorJsi18n"/>
    <div>
        <script type="text/javascript" src="../smooks-mediator/js/mediator-util.js"></script>

        <table class="normal" width="100%">
            <tbody>
            <tr><td colspan="4"><h2><fmt:message key="smooks.mediator"/></h2></td></tr>
            <tr>
                <td>
                    <fmt:message key="config.key"/><span class="required">*</span>
                </td>
                <td>
                    <input class="longInput" type="text"
                           value="<%= smooksMediator.getConfigKey() != null ? smooksMediator.getConfigKey() : "" %>"
                           id="seq_ref" name="seq_ref" readonly="true"/>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('seq_ref','/_system/config')"><fmt:message key="conf.registry.browser"/>
                    </a>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('seq_ref','/_system/governance')"><fmt:message key="gov.registry.browser"/>
                    </a>
                </td>
            </tr>
            <tr>
                <td>Input</td>
                <td colspan="3">
                    <select name="inputTypeSelect" id="inputTypeSelect">
                        <option value="xml" <%=inputType.equals("xml") ? "selected=\"selected\"": ""%>>XML</option>
                        <option value="text" <%=inputType.equals("text") ? "selected=\"selected\"": ""%>>Text</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td style="width: 80px;">
                    <label>Expression</label>
                </td>
                <td style="width: 305px;">
                    <input id="inputExpr" name="inputExpr" type="text"
                           value="<%=inputExpr%>"
                           style="width: 300px;"/>
                </td>
                <td>
                    <a href="#"
                       id="inputExprBttn"
                       onclick="showNameSpaceEditor('inputExpr');"
                       class="nseditor-icon-link"
                       style="padding-left:40px">
                        Namespaces</a>
                </td>
                <td></td>
            </tr>
            <tr>
                <td><label>Output</label></td>
                <td colspan="3">
                    <select name="outputTypeSelect" id="outputTypeSelect">
                        <option value="xml" <%=outputType.equals("xml") ? "selected=\"selected\"": ""%>>XML</option>
                        <option value="text" <%=outputType.equals("text") ? "selected=\"selected\"": ""%>>Text</option>
                         <option value="java" <%=outputType.equals("java") ? "selected=\"selected\"": ""%>>Java</option>
                    </select>
                </td>
            </tr>
            <tr>
                <% if (smooksMediator.getOutputProperty() == null) { %>
                <td>
                    <select name="outputExprSelect" id="exprTypeSelect" onchange="typeChanged()">
                        <option selected="selected" value="expression">Expression</option>
                        <option value="property">Property</option>
                    </select>
                </td>
                <% } else { %>
                <td>
                    <select name="outputExprSelect" id="exprTypeSelect" onchange="typeChanged()">
                        <option value="expression">Expression</option>
                        <option selected="selected" value="property">Property</option>
                    </select>
                </td>
                <% } %>
                <% if (smooksMediator.getOutputProperty() == null) { %>
                <td style="width: 305px;">
                    <input id="outputExpr" name="outputExpr" type="text"
                           value="<%=outputExpr%>"
                           style="width: 300px;"/>
                </td>
                <% } else { %>
                <td style="width: 305px;">
                    <input id="outputExpr" name="outputExpr" type="text"
                           value="<%=outputProperty%>"
                           style="width: 300px;"/>
                </td>
                <% } %>
                <td <%=!isExpression ? "style=\"display:none;\" id=\"namespaceEditor\"" : "id=\"namespaceEditor\""%>>
                    <a href="#"
                       onclick="showNameSpaceEditor('inputExpr');"
                       class="nseditor-icon-link"
                       style="padding-left:40px">
                        Namespaces</a>
                </td>
                <td <%=!isExpression ? "style=\"display:none;\" id=\"actionSelect\" " : "id=\"actionSelect\" "%>>
                    <select name="outputActionSelect">
                        <option value="add" <%=outputAction.equals("add") ? "selected=\"selected\"": ""%>>Add</option>
                        <option value="replace" <%=outputAction.equals("replace") ? "selected=\"selected\"": ""%>>Replace</option>
                        <option value="sibling" <%=outputAction.equals("sibling") ? "selected=\"selected\"": ""%>>Sibling</option>
                    </select>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</fmt:bundle>