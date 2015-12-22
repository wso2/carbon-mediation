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

<%@ page import="org.wso2.carbon.mediator.property.PropertyMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    boolean isExpression = false;
    String val = "";

    if (!(mediator instanceof org.wso2.carbon.mediator.property.PropertyMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PropertyMediator propertyMediator = (org.wso2.carbon.mediator.property.PropertyMediator) mediator;
    boolean isOMValue = false;
    boolean displayPatternAndGroup = true;
    String type = "STRING";
    String omValue = "";
    if (propertyMediator.getType() != null) {
        type = propertyMediator.getType();
        if (!type.equals("STRING") || PropertyMediator.ACTION_REMOVE
                == propertyMediator.getAction()) {
            displayPatternAndGroup = false;
        }
    }
    if (propertyMediator.getValue() != null) {
        isExpression = false;
        val = propertyMediator.getValue();
        val = val.replace("\"","&quot;");//replace quote sign with &quot;
    } else if (propertyMediator.getExpression() != null) {
        isExpression = true;
        val = propertyMediator.getExpression().toString();
        val = val.replace("\"","&quot;");//replace quote sign with &quot;
        NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
        nameSpacesRegistrar.registerNameSpaces(propertyMediator.getExpression(), "mediator.property.val_ex", session);
    } else if (propertyMediator.getValueElement() != null) {
        omValue = propertyMediator.getValueElement().toString();
        isOMValue = true;
    }
    String pattern = "";
    String group = "";

    if (type.equals("STRING")) {
        if (propertyMediator.getPattern() != null) {
            pattern = propertyMediator.getPattern();
        }

        if (propertyMediator.getGroup() != 0) {
            group = Integer.toString(propertyMediator.getGroup());
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.property.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.property.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="propertyMediatorJsi18n"/>
    <div>
        <script type="text/javascript" src="../property-mediator/js/mediator-util.js"></script>

        <table class="normal" width="100%">
            <tbody>
            <tr><td colspan="3"><h2><fmt:message key="mediator.property.header"/></h2></td></tr>
            <tr>
                <td style="width:130px;"><fmt:message key="name"/><font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td>
                    <input type="text" id="mediator.property.name" name="mediator.property.name"
                           style="width:300px;"
                           value='<%=propertyMediator.getName() != null ? propertyMediator.getName() : ""%>'/>
                </td>
                <td></td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.property.action"/></td>
                <td>
                    <input id="set" type="radio" name="mediator.property.action"
                           value="<%=org.apache.synapse.mediators.builtin.PropertyMediator.ACTION_SET%>"
                           onclick="displaySetProperties(true); "
                            <%=propertyMediator.getAction() == org.apache.synapse.mediators.builtin.PropertyMediator.ACTION_SET ? "checked=\"checked\"" : ""%>/>
                    <fmt:message key="mediator.property.set"/>
                    &nbsp;
                    <input id="remove" type="radio" name="mediator.property.action"
                           value="<%=org.apache.synapse.mediators.builtin.PropertyMediator.ACTION_REMOVE%>"
                           onclick="displaySetProperties(false);"
                            <%=propertyMediator.getAction() == org.apache.synapse.mediators.builtin.PropertyMediator.ACTION_REMOVE ? "checked=\"checked\"" : ""%>/>
                    <fmt:message key="mediator.property.remove"/>
                </td>
            </tr>

            <tr id="mediator.property.action_row" <%=org.apache.synapse.mediators.builtin.PropertyMediator.ACTION_REMOVE == propertyMediator.getAction() ? "style=\"display:none;\"" : ""%>>
                <td style="padding-right: 10px;">
                    <fmt:message key="mediator.property.setAction"/>
                </td>
                <td style="padding-right: 5px;">
                    <% if (!isExpression) { %>
                    <input type="radio" id="value" name="mediator.property.type"
                           onclick="expressionChanged()"
                           value="value" checked="checked"/>
                    <fmt:message key="mediator.property.value"/>
                    <input type="radio" id="expression" name="mediator.property.type"
                           onclick="expressionChanged()"
                           value="expression"/>
                    <fmt:message key="mediator.property.expression"/>
                    <% } else { %>
                    <input type="radio" id="value" name="mediator.property.type"
                           onclick="expressionChanged()"
                           value="value"/>
                    <fmt:message key="mediator.property.value"/>
                    <input type="radio" id="expression" name="mediator.property.type"
                           onclick="expressionChanged()"
                           value="expression" checked="checked"/>
                    <fmt:message key="mediator.property.expression"/>
                    <% } %>
                </td>
            </tr>
            <tr id="type_row">
                <td><fmt:message key="type"/></td>
                <td>
                    <select id="type_select" name="type_select" style="width:150px;" onchange="typeChanged()">
                        <option value="STRING" <%=type.equals("STRING") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="string"/>
                        </option>
                        <option value="INTEGER" <%=type.equals("INTEGER") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="integer"/>
                        </option>
                        <option value="BOOLEAN" <%=type.equals("BOOLEAN") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="boolean"/>
                        </option>
                        <option value="DOUBLE" <%=type.equals("DOUBLE") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="double"/>
                        </option>
                        <option value="FLOAT" <%=type.equals("FLOAT") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="float"/>
                        </option>
                        <option value="LONG" <%=type.equals("LONG") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="long"/>
                        </option>
                        <option value="SHORT" <%=type.equals("SHORT") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="short"/>
                        </option>
                        <option value="OM" <%=isOMValue || type.equals("OM") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="om"/>
                        </option>
                    </select>
                </td>
            </tr>
            <tr id="mediator.property.value_row" <%=org.apache.synapse.mediators.builtin.PropertyMediator.ACTION_REMOVE == propertyMediator.getAction() ? "style=\"display:none;\"" : ""%>>
                <td>
                    <label id="expressionLabel" <%=!isExpression ? "style=\"display:none\"" : ""%>><fmt:message key="mediator.property.expression"/></label>
                    <label <%=isExpression ? "id=\"valueLabel\" style=\"display:none\"" : "id=\"valueLabel\"" %>><fmt:message key="mediator.property.value"/></label><font
                        style="color: red; font-size: 8pt;"> *</font>
                </td>
                <% if (!isOMValue) { %>
                <td style="width:320px;" id="value_col">
                    <input name="mediator.property.val_ex"
                           type="text" id="mediator.property.val_ex"
                           value="<%=val%>" style="width:300px;"/>
                </td>
                <td style="width:320px;display:none" id="om_text_td" colspan="2">
                    <textarea name="om_text" rows="10" cols="20"
                           id="om_text"
                           style="width:300px;"><%=omValue%></textarea>
                </td>
                <td id="namespace_col">
                    <a href="#nsEditorLink" id="mediator.property.nmsp_button"
                       onclick="showNameSpaceEditor('mediator.property.val_ex')" class="nseditor-icon-link"
                       style="padding-left:40px;<%=!isExpression ? "display:none;" : ""%>">
                        <fmt:message key="mediator.property.namespace"/></a>
                    <a name="nsEditorLink"></a>
                </td>
                <% } else { %>
                <td style="width:320px;display:none" id="value_col">
                    <input name="mediator.property.val_ex"
                           type="text" id="mediator.property.val_ex"
                           value="<%=val%>" style="width:300px;"/>
                </td>
                <td style="width:320px;" id="om_text_td" colspan="2">
                    <textarea name="om_text" rows="10" cols="20"
                           id="om_text"
                           style="width:300px;"><%=omValue%></textarea>
                </td>
                <td id="namespace_col" style="display:none">
                    <a href="#nsEditorLink" id="mediator.property.nmsp_button"
                       onclick="showNameSpaceEditor('mediator.property.val_ex')" class="nseditor-icon-link"
                       style="padding-left:40px;<%=!isExpression ? "display:none;" : ""%>">
                        <fmt:message key="mediator.property.namespace"/></a>
                    <a name="nsEditorLink"></a>
                </td>
                <% } %>
            </tr>
            <tr id="pattern_row" <%= !displayPatternAndGroup ? "style=\"display:none;\"" : ""%>>
                <td>
                    <fmt:message key="pattern"/>
                </td>
                <td style="width:320px;" <%=isOMValue ? "style=\"display:none\"" : ""%>>
                    <input name="pattern"
                           type="text" id="pattern"
                           value="<%=pattern%>" style="width:300px;"/>
                </td>
            </tr>
            <tr id="group_row" <%= !displayPatternAndGroup ? "style=\"display:none;\"" : ""%>>
                <td>
                    <fmt:message key="group"/>
                </td>
                <td style="width:320px;" <%=isOMValue ? "style=\"display:none\"" : ""%>>
                    <input name="group"
                           type="text" id="group"
                           value="<%=group%>" style="width:300px;"/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.property.scope"/></td>
                <td>
                    <select id="mediator.property.scope" name="mediator.property.scope"
                            style="width:150px;">
                        <option value="default" <%=propertyMediator.getScope() != null && propertyMediator.getScope().equals("default") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="synapse"/>
                        </option>
                        <option value="transport" <%=propertyMediator.getScope() != null && propertyMediator.getScope().equals("transport") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="transport"/>
                        </option>
                        <option value="axis2" <%=propertyMediator.getScope() != null && propertyMediator.getScope().equals("axis2") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="axis2"/>
                        </option>
                        <option value="axis2-client" <%=propertyMediator.getScope() != null && propertyMediator.getScope().equals("axis2-client") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="axis2.client"/>
                        </option>
                        <option value="operation" <%=propertyMediator.getScope() != null && propertyMediator.getScope().equals("operation") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="operation"/>
                        </option>
                        <option value="registry" <%=propertyMediator.getScope() != null && propertyMediator.getScope().equals("registry") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="registry"/>
                        </option>
                    </select>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</fmt:bundle>