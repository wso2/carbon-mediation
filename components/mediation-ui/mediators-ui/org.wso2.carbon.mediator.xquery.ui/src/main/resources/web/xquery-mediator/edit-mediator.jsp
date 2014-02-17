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

<%@ page import="org.apache.axiom.om.xpath.AXIOMXPath" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.xquery.Variable" %>
<%@ page import="org.wso2.carbon.mediator.xquery.XQueryMediator" %>
<%@ page import="org.wso2.carbon.mediator.xquery.internal.XQueryMediatorClientHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.xquery.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.xquery.i18n.JSResources"
        request="<%=request%>" i18nObjectName="xqueryjsi18n"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof XQueryMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    XQueryMediator xqueryMediator = (XQueryMediator) mediator;

    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();

    boolean isKeyDynamic = false;
    String keyVal = "";

    Value key = xqueryMediator.getQueryKey();

    if (key != null) {

        if (key.getKeyValue() != null) {
            isKeyDynamic = false;
            keyVal = key.getKeyValue();
        } else if (key.getExpression() != null) {
            isKeyDynamic = true;
            keyVal = key.getExpression().toString();
            nameSpacesRegistrar.registerNameSpaces(key.getExpression(), "mediator.xquery.key.dynamic_val", session);
        }

    }

    AXIOMXPath sourceXPath = xqueryMediator.getTarget();
    String source = "";
    if (sourceXPath != null) {
        nameSpacesRegistrar.registerNameSpaces(sourceXPath, "mediator.xquery.target", session);
        source = sourceXPath.toString();
    }
    List<Variable> variableList = xqueryMediator.getVariables();

    boolean isExpressionAvailable = false;
    String variableTableStyle = variableList.isEmpty() ? "display:none;" : "";
    for (Variable v : variableList) {
        if (v.getVariableType() == Variable.CUSTOM_VARIABLE) {
            isExpressionAvailable = true;
        }
    }
    
%>

<div>
<script type="text/javascript" src="../xquery-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="xquery.mediator.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">
             <tr>
                <td>
                    <fmt:message key="xquery.key.type"/> :
                </td>
                <td>
                    <input type="radio"
                           onclick="javascript:displayElement('mediator.xquery.key.dynamic', false); javascript:displayElement('mediator.xquery.key.static', true); displayElement('mediator.xquery.key.namespace.editor', false);"
                           name="keygroup" <%=!isKeyDynamic ? "checked=\"checked\" value=\"StaticKey\"" : "value=\"StaticKey\""%>/>
                    <fmt:message key="xquery.key.static"/>
                    <input type="radio" id="keyGroupDynamic"
                           onclick="javascript:displayElement('mediator.xquery.key.dynamic', true); displayElement('mediator.xquery.key.namespace.editor', true); displayElement('mediator.xquery.key.static', false);"
                           name="keygroup" <%=isKeyDynamic ? "checked=\"checked\" value=\"DynamicKey\"" : "value=\"DynamicKey\""%>
                    "/>
                    <fmt:message key="xquery.key.dynamic"/>
                </td>
                <td></td>
            </tr>

            <tr id="mediator.xquery.key.static" <%=isKeyDynamic ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.xquery.key"/> <font style='color: red;font-size: 8pt;'>&#160;*</font>
                </td>
                <td>
                    <input type="text" class="longInput" id="mediator.xquery.key.static_val"
                           name="mediator.xquery.key.static_val" value="<%=keyVal%>"
                           readonly="true"/>
                </td>
                <td>
                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('mediator.xquery.key.static_val','/_system/config')"><fmt:message
                            key="conf.registry.keys"/></a>
                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('mediator.xquery.key.static_val','/_system/governance')"><fmt:message
                            key="gov.registry.keys"/></a>                    
                </td>
            </tr>

            <tr id="mediator.xquery.key.dynamic" <%=!isKeyDynamic ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.xquery.key"/><span class="required">*</span></td>
                <td><input class="longInput" type="text" name="mediator.xquery.key.dynamic_val"
                           id="mediator.xquery.key.dynamic_val"
                           value="<%=keyVal%>"/></td>
                <td><a id="mediator.xquery.key.dynamic_nmsp_button" href="#nsEditorLink"
                       class="nseditor-icon-link" style="padding-left:40px"
                       onclick="showNameSpaceEditor('mediator.xquery.key.dynamic_val')">

                    <fmt:message key="namespaces"/></a>
                </td>
            </tr>

            <tr>
                <td><fmt:message key="mediator.xquery.target"/></td>
                <td>
                    <input type="text" class="longInput" id="mediator.xquery.target" name="mediator.xquery.target" value="<%=source%>"/>
                </td>
                <td><a href="#nsEditorLink" class="nseditor-icon-link"
                       style="padding-left:40px"
                       onclick="showNameSpaceEditor('mediator.xquery.target')"><fmt:message key="namespaces"/></a></td>
            </tr>
        </table>
    </td>
</tr>
<tr>
<td>
<h3 class="mediator"><fmt:message key="variables"/></h3>

<div style="margin-top:10px;">
<table id="variabletable" style="<%=variableTableStyle%>" class="styledInner">
<thead>
<tr>
    <th><fmt:message key="th.variable.type"/></th>
    <th><fmt:message key="th.variable.name"/></th>
    <th><fmt:message key="th.value.type"/></th>
    <th><fmt:message key="th.value.expression"/></th>
    <th id="reg-key-th" style="<%=!isExpressionAvailable? "display:none;" : ""%>"><fmt:message key="th.registry.key"/></th>
    <th id="reg-browser-th" style="<%=!isExpressionAvailable? "display:none;" : ""%>"><fmt:message key="registry.browser"/></th>
    <th id="ns-edior-th" style="<%=!isExpressionAvailable? "display:none;" : ""%>"><fmt:message key="namespaceeditor"/></th>
    <th><fmt:message key="th.action"/></th>
</tr>
<tbody id="variabletbody">
<%
    int i = 0;
    for (Variable variable : variableList) {
        if (variable != null) {
            String name = null;
            String value = "";
            boolean isLiteral = true;
            String regKey = "";
            AXIOMXPath expr = null;
            if (variable.getVariableType() == Variable.BASE_VARIABLE) {
                QName qName = variable.getName();
                Object variableValue = variable.getValue();
                if (variableValue != null) {
                    value = (String) variable.getValue();
                }
                if (qName != null) {
                    name = qName.getLocalPart();
                }
            } else if (variable.getVariableType() == Variable.CUSTOM_VARIABLE) {
                isLiteral = false;
                QName qName = variable.getName();
                if (qName != null) {
                    name = qName.getLocalPart();
                }
                expr = variable.getExpression();
                if (expr != null) {
                    nameSpacesRegistrar.registerNameSpaces(expr, "variableValue" + i, session);
                    value = expr.toString();
                }
                String variableRegKey = variable.getRegKey();
                if (variableRegKey != null) {
                    regKey = variableRegKey;
                }
            }
            String type = XQueryMediatorClientHelper.getType(variable);
            if (name != null && !"".equals(name)) {

%>
<tr id="variableRaw<%=i%>">
<td>
<select id="variableType<%=i%>" name="variableType<%=i%>">
<% if (value == null) {%>
<option value="Select-A-Value" selected="true"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("DOCUMENT".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT" selected="true">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("DOCUMENT_ELEMENT".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT" selected="true">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("ELEMENT".equals(type)) {%>
<option value="Select-A-Value" selected="true"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT" selected="true">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("INT".equals(type)) {%>
<option value="Select-A-Value" selected="true"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT" selected="true">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("INTEGER".equals(type)) {%>
<option value="Select-A-Value" selected="true"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER" selected="true">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("BYTE".equals(type)) {%>
<option value="Select-A-Value" selected="true"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE" selected="true">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("DOUBLE".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE" selected="true">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("SHORT".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT" selected="true">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("LONG".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG" selected="true">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("FLOAT".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT" selected="true">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("BOOLEAN".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN" selected="true">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING">STRING</option>
<%} else if ("STRING".equals(type)) {%>
<option value="Select-A-Value"><fmt:message key="leave.as.is"/></option>
<option value="DOCUMENT">DOCUMENT</option>
<option value="DOCUMENT_ELEMENT">DOCUMENT_ELEMENT</option>
<option value="ELEMENT">ELEMENT</option>
<option value="INT">INT</option>
<option value="INTEGER">INTEGER</option>
<option value="BOOLEAN">BOOLEAN</option>
<option value="BYTE">BYTE</option>
<option value="DOUBLE">DOUBLE</option>
<option value="SHORT">SHORT</option>
<option value="LONG">LONG</option>
<option value="FLOAT">FLOAT</option>
<option value="STRING" selected="true">STRING</option>
<%}%>
</select>
</td>

<td><input type="text" name="variableName<%=i%>" id="variableName<%=i%>" value="<%=name%>"/></td>
<td>
    <select name="variableTypeSelection<%=i%>"
            id="variableTypeSelection<%=i%>"
            onchange="onvariableTypeSelectionChange('<%=i%>','<fmt:message key="namespaces"/>','<fmt:message key="registry.browser"/>')">
        <% if (isLiteral) {%>
        <option value="literal" selected="selected">
            <fmt:message key="value"/>
        </option>
        <option value="expression">
            <fmt:message key="expression"/>
        </option>
        <%} else if (variable.getVariableType() == Variable.CUSTOM_VARIABLE) {%>
        <option value="expression" selected="selected">
            <fmt:message key="expression"/>
        </option>
        <option value="literal">
            <fmt:message key="value"/>
        </option>
        <%} else { %>
        <option value="literal" selected="selected">
            <fmt:message key="value"/>
        </option>
        <option value="expression">
            <fmt:message key="expression"/>
        </option>
        <% }%>
    </select>
</td>
<td>
    <input id="variableValue<%=i%>" name="variableValue<%=i%>" type="text" value="<%=value%>"/>
</td>
<td id="registrykeyTD<%=i%>" style="<%=!isExpressionAvailable? "display:none;" : ""%>" >
    <% if (!isLiteral && variable.getVariableType() == Variable.CUSTOM_VARIABLE) {%>
    <input id="registryKey<%=i%>" name="registryKey<%=i%>" type="text" value="<%=regKey%>" readonly="true"/>
    <% }%>
</td>
<td id="registryBrowserButtonTD<%=i%>" style="<%=!isExpressionAvailable? "display:none;" : ""%>">
    <% if (!isLiteral && variable.getVariableType() == Variable.CUSTOM_VARIABLE) {%>
    <a href="#registryBrowserLink" class="registry-picker-icon-link"
       onclick="showRegistryBrowser('registryKey<%=i%>','/_system/config')"><fmt:message key="conf.registry.keys"/></a>
    <a href="#registryBrowserLink" class="registry-picker-icon-link"
       onclick="showRegistryBrowser('registryKey<%=i%>','/_system/governance')"><fmt:message key="gov.registry.keys"/></a>    
    <% }%>
</td>
<td id="nsEditorButtonTD<%=i%>" style="<%=!isExpressionAvailable? "display:none;" : ""%>">
     <% if (!isLiteral && variable.getVariableType() == Variable.CUSTOM_VARIABLE) {%>
    <a href="#nsEditorLink" class="nseditor-icon-link"
       style="padding-left:40px"
       onclick="showNameSpaceEditor('variableValue<%=i%>')"><fmt:message key="namespaces"/></a>
    <% }%>
</td>

<td><a href="#" class="delete-icon-link" onclick="deletevariable('<%=i%>')"><fmt:message
        key="delete"/></a></td>
</tr>
<% }
}
    i++;
} %>
<input type="hidden" name="variableCount" id="variableCount" value="<%=i%>"/>
<script type="text/javascript">
    if (isRemainVariableExpressions()) {
        resetDisplayStyle("");
    }
</script>
</tbody>
</thead>
</table>
</div>
</td>
</tr>
<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addvariableLink"></a>
            <a class="add-icon-link"
               href="#addvariableLink"
               onclick="addvariable('<fmt:message key="namespaces"/>','<fmt:message key="registry.keys"/>','<fmt:message key="nameemptyerror"/>','<fmt:message key="valueemptyerror"/>','<fmt:message key="typeemptyerror"/>','<fmt:message key="leave.as.is"/>')"><fmt:message
                    key="add.variable"/></a>
        </div>
    </td>
</tr>

</table>
<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>

<a name="registryBrowserLink"></a>

<div id="registryBrowser" style="display:none;"></div>
</div>
</fmt:bundle>