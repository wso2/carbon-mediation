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

<%@ page import="org.wso2.carbon.mediator.script.ScriptMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ScriptMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ScriptMediator scriptMediator = (ScriptMediator) mediator;

    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();

    // scriptTyep
    String scriptType = null;

    boolean isKeyDynamic = false;
    String keyVal = "";

    Value key = scriptMediator.getKey();

    if (key != null) {

        if (key.getKeyValue() != null) {
            isKeyDynamic = false;
            keyVal = key.getKeyValue();
            scriptType = key.getKeyValue();
        } else if (key.getExpression() != null) {
            isKeyDynamic = true;
            keyVal = key.getExpression().toString();
            scriptType = key.getExpression().toString();
            nameSpacesRegistrar.registerNameSpaces(key.getExpression(), "mediator.script.key.dynamic_val", session);
        }

    }
    String scriptTypeStyle = "";
    String sourceScriptStyle = "";
    String functionStyle = "";
    String keyStyle = "";
    String keyTypeStyle = "display:none";
    String includeKeyTableStyle = "";

    Map includes = null;
    if (scriptType != null && !"".equals(scriptType)) {
        functionStyle = "";
        keyStyle = "";
        sourceScriptStyle = "display:none";
        includes = scriptMediator.getIncludes();
        includeKeyTableStyle = includes.isEmpty() ? "display:none" : "";
        scriptTypeStyle = "";
        keyTypeStyle = "";
    } else if (scriptType == null) {
        functionStyle = "display:none";
        keyStyle = "display:none";
        includeKeyTableStyle = "display:none";
        sourceScriptStyle = "";
        scriptTypeStyle = "display:none";
        keyTypeStyle = "display:none";
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.script.ui.i18n.Resources">
<carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.script.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="scripti18n"/>
<div>
<script type="text/javascript" src="../script-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.script.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.script.type"/></td>
                <td><select style="width:110px;" id="script_type" name="script_type"
                            onchange="javascript:changeFields(this.value);">
                    <%
                        if (scriptMediator.getKey() == null) {
                    %>
                    <option id="inline" value="inline" selected="true">Inline</option>
                    <option id="registry_key" value="regKey">Registry Key</option>
                    <%
                    } else {
                    %>
                    <option id="inline" value="inline">Inline</option>
                    <option id="registry_key" value="regKey" selected="true">Registry Key</option>
                    <%
                        }
                    %>
                </select>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.script.language"/><span class="required">*</span></td>
                <td><select style="width:110px;" id="mediator.script.language" name="mediator.script.language">
                    <%
                        if ("js".equals(scriptMediator.getLanguage())) {
                    %>
                    <option id="javaScript" value="js" selected="true">Javascript</option>
                    <option id="nashornScript" value="nashornJs">NashornJavascript</option>
                    <option id="ruby" value="rb">Ruby</option>
                    <option id="groovy" value="groovy">Groovy</option>
                    <%
                    } else if ("rb".equals(scriptMediator.getLanguage())) {
                    %>
                    <option id="javaScript" value="js">Javascript</option>
                    <option id="nashornScript" value="nashornJs">NashornJavascript</option>
                    <option id="ruby" value="rb" selected="true">Ruby</option>
                    <option id="groovy" value="groovy">Groovy</option>
                    <%
                    } else if ("groovy".equals(scriptMediator.getLanguage())) {
                    %>
                    <option id="javaScript" value="js">Javascript</option>
                    <option id="nashornScript" value="nashornJs">NashornJavascript</option>
                    <option id="ruby" value="rb">Ruby</option>
                    <option id="groovy" value="groovy" selected="true">Groovy</option>
                    <%
                    } else if ("nashornJs".equals(scriptMediator.getLanguage())) {
                    %>
                    <option id="javaScript" value="js">Javascript</option>
                    <option id="nashornScript" value="nashornJs" selected="true">NashornJavascript</option>
                    <option id="ruby" value="rb">Ruby</option>
                    <option id="groovy" value="groovy">Groovy</option>
                    <%
                    } else {
                    %>
                    <option id="javaScript" value="js" selected="true">Javascript</option>
                    <option id="nashornScript" value="nashornJs">NashornJavascript</option>
                    <option id="ruby" value="rb">Ruby</option>
                    <option id="groovy" value="groovy">Groovy</option>
                    <%
                        }
                    %>
                </select>
                </td>
            </tr>

            <tr id="source_script" style="<%=sourceScriptStyle%>">
                <td><fmt:message key="mediator.script.source"/><span class="required">*</span></td>
                <td>
                    <textarea rows="20" cols="40" id="mediator.script.source_script"
                              name="mediator.script.source_script"><%= scriptMediator.getScriptSourceCode() != null ? scriptMediator.getScriptSourceCode() : "var sample" %>
                    </textarea>
            </tr>

            <tr id="function_row" style="<%=functionStyle%>">
                <td><fmt:message key="mediator.script.function"/><span class="required">*</span></td>
                <td>
                    <input class="longInput" type="text" id="mediator.script.function" name="mediator.script.function"
                           value="<%= scriptMediator.getFunction() != null ? scriptMediator.getFunction() : "" %>"/>
                </td>
            </tr>

            <tr id="key_type_raw" style="<%=keyTypeStyle%>">
                <td>
                    <fmt:message key="mediator.script.key.type"/> :
                </td>
                <td>
                    <input type="radio" id="keyGroupStatic"
                           onclick="javascript:displayElement('mediator.script.key.dynamic', false); javascript:displayElement('mediator.script.key.static', true); document.getElementById('mediator.script.key.static_val').value=''; displayElement('mediator.script.key.namespace.editor', false);"
                           name="keygroup" <%=!isKeyDynamic ? "checked=\"checked\" value=\"StaticKey\"" : "value=\"StaticKey\""%>/>
                    <fmt:message key="mediator.script.key.static"/>
                    <input type="radio" id="keyGroupDynamic"
                           onclick="javascript:displayElement('mediator.script.key.dynamic', true); displayElement('mediator.script.key.namespace.editor', true); document.getElementById('mediator.script.key.dynamic_val').value = ''; displayElement('mediator.script.key.static', false);"
                           name="keygroup" <%=isKeyDynamic ? "checked=\"checked\" value=\"DynamicKey\"" : "value=\"DynamicKey\""%>
                    "/>
                    <fmt:message key="mediator.script.key.dynamic"/>
                </td>
                <td></td>
            </tr>

            <tr id="mediator.script.key.static" <%=(isKeyDynamic | "display:none".equals(keyTypeStyle)) ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.script.key"/><span class="required">*</span></td>
                <td>
                    <input class="longInput" id="mediator.script.key.static_val" name="mediator.script.key.static_val"
                           type="text"
                           value="<%=scriptMediator.getKey()!=null?keyVal:""%>" readonly="true">
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('mediator.script.key.static_val','/_system/config');return false;">
                    <fmt:message key="mediator.script.conf.regkey"/></a>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('mediator.script.key.static_val','/_system/governance');return false;">
                    <fmt:message key="mediator.script.gov.regkey"/></a>
                </td>
            </tr>

            <tr id="mediator.script.key.dynamic" <%=!isKeyDynamic | "display:none".equals(keyTypeStyle) ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.script.key"/><span class="required">*</span></td>
                <td><input class="longInput" type="text" name="mediator.script.key.dynamic_val"
                           id="mediator.script.key.dynamic_val"
                           value="<%=keyVal%>"/></td>
                <td><a id="mediator.script.key.dynamic_nmsp_button" href="#nsEditorLink"
                       class="nseditor-icon-link" style="padding-left:40px"
                       onclick="showNameSpaceEditor('mediator.script.key.dynamic_val')">

                    <fmt:message key="mediator.script.namespaces"/></a>
                </td>
            </tr>

        </table>
    </td>
</tr>
<tr id="include_key" style="<%=scriptTypeStyle%>">
    <td>
        <h3 class="mediator">
            <fmt:message key="mediator.script.includekeys"/>
        </h3>

        <div style="margin-top:0px;">
            <table id="includeKeytable" style="<%=includeKeyTableStyle%>;" class="styledInner">
                <tbody id="includeKeytbody">
                    <%
                        int i = 0;
                        if (includes != null && !includes.isEmpty()) {
                            Iterator values = includes.values().iterator();
                            Iterator keyValueFairs = includes.entrySet().iterator();

                            for (i = 0; i < includes.size();) {
                                Map.Entry entry = (Map.Entry) keyValueFairs.next();
//                                Object val = entry.getValue();
                                Object val = entry.getKey();
                                if (val != null) {
                    %>
                    <tr id="includeKeyRaw<%=i%>">
                        <td width="6%">Key</td>
                        <td width="10%"><input class="longInput" type="text" name="includeKey<%=i%>"
                                               id="includeKey<%=i%>"
                                               value="<%=val%>" readonly="true">
                        </td>
                        <td width="15%">
                            <a href="#registryBrowserLink"
                               class="registry-picker-icon-link"                               
                               onclick="showRegistryBrowser('includeKey<%=i%>','/_system/config')"><fmt:message
                                    key="mediator.script.conf.regkey"/></a>
                            <a href="#registryBrowserLink"
                               class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('includeKey<%=i%>','/_system/governance')"><fmt:message
                                    key="mediator.script.gov.regkey"/></a>
                        </td>
                        <td>
                            <a href="#" class="delete-icon-link" 
                               onclick="deleteIncludeKey('<%=i%>')"><fmt:message key="mediator.script.delete"/></a>
                        </td>
                    </tr>
                    <%
                                }                                                               
                                i++;
                            }
                        }
                    %>
                    <input type="hidden" name="includeKeyCount" id="includeKeyCount" value="<%=i%>"/>
                </tbody>
            </table>
        </div>
    </td>
</tr>
<tr id="include_key_link" style="<%=scriptTypeStyle%>">
    <td>
        <div style="margin-top:0px;">
            <a name="addNameLink"></a>
            <a class="add-icon-link"
               href="#addNameLink"
               onclick="addIncludeKey('<fmt:message key="mediator.script.emptykey"/>')"><fmt:message
                    key="mediator.script.add"/></a>
        </div>
    </td>
</tr>
</table>
<a name="registryBrowserLink"></a>
</div>
</fmt:bundle>
