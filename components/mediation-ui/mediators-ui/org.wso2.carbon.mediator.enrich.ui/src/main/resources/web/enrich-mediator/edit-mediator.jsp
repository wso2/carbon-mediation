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

<%@ page import="org.wso2.carbon.mediator.enrich.ui.EnrichMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    String sourceProperty = "";
    String targetProperty = "";
    String sourceValue = "";
    String targetValue = "";
    String inlineXMLStr = "";
    String inlineKey = "";
    boolean isInlineRegResource = false;

    final int CUSTOM = 0;
    final int ENVELOPE = 1;
    final int BODY = 2;
    final int PROPERTY = 3;
    final int INLINE = 4;


    if (!(mediator instanceof org.wso2.carbon.mediator.enrich.ui.EnrichMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    EnrichMediator enrichMediator = (org.wso2.carbon.mediator.enrich.ui.EnrichMediator) mediator;

    int sourceType = CUSTOM;

    if (enrichMediator.getSourceType().equals("body")) {
        sourceType = BODY;
    } else if (enrichMediator.getSourceType().equals("envelope")) {
        sourceType = ENVELOPE;
    } else if (enrichMediator.getSourceType().equals("property")) {
        sourceType = PROPERTY;
        sourceValue = enrichMediator.getSourceProperty();
    } else if (enrichMediator.getSourceType().equals("custom")) {
        sourceType = CUSTOM;
        if (enrichMediator.getSourceExpression() != null) {
            SynapseXPath sourceXPath = enrichMediator.getSourceExpression();
            NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
            if (sourceXPath != null) {
                nameSpacesRegistrar.registerNameSpaces(sourceXPath, "mediator.enrich.source.val_ex", session);
                sourceValue = sourceXPath.toString();
            }
        }
    } else if (enrichMediator.getSourceType().equals("inline")) {
        sourceType = INLINE;
        if (enrichMediator.getSourceInlineXML() != null && !enrichMediator.getSourceInlineXML().equals("")) {
            inlineXMLStr = enrichMediator.getSourceInlineXML();
        } else if (enrichMediator.getInlineSourceRegKey() != null
                && !enrichMediator.getInlineSourceRegKey().equals("")) {
            isInlineRegResource = true;
            inlineKey = enrichMediator.getInlineSourceRegKey();
        }
    }




    int targetType = CUSTOM;
    if (enrichMediator.getTargetType().equals("body")) {
        targetType = BODY;
    } else if (enrichMediator.getTargetType().equals("envelope")) {
        targetType = ENVELOPE;
    } else if (enrichMediator.getTargetType().equals("property")) {
        targetType = PROPERTY;
        targetValue = enrichMediator.getTargetProperty();
    } else if (enrichMediator.getTargetType().equals("custom")) {
        targetType = CUSTOM;
        if (enrichMediator.getTargetExpression() != null) {
            SynapseXPath targetXPath = enrichMediator.getTargetExpression();
            NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
            if (targetXPath != null) {
                nameSpacesRegistrar.registerNameSpaces(targetXPath, "mediator.enrich.target.val_ex", session);
                targetValue = targetXPath.toString();
            }
        }
    }

%>

<fmt:bundle basename="org.wso2.carbon.mediator.enrich.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.enrich.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="enrichMediatorJsi18n"/>

<div>
<script type="text/javascript" src="../enrich-mediator/js/mediator-util.js"></script>


<script type="text/javascript">
    function changeSourceElements(elm) {
        var selectedElmValue = elm.options[elm.selectedIndex].value;
        if (selectedElmValue == "custom") {
            displayElement('sourceXpathExpLabel', true);
            displayElement('mediator.enrich.source.nmsp_button', true);
            displayElement('mediator.enrich.source.val_ex', true);
            displayElement('sourcePropertyLabel', false);
            displayElement('sourceInlineLabel', false);
            displayElement('inlineEnrichText', false);
            displayElement('inlineXMLRadio', false);
            displayElement('inlineRegRadio', false);
            displayElement('registryInlineLabel', false);
            displayElement('mediator.enrich.reg.key', false);
            displayElement('inlineRegBrowser', true);
            displayElement('inlineRegBrowser', false);
        } else if (selectedElmValue == "envelope") {
            displayElement('sourceXpathExpLabel', false);
            displayElement('mediator.enrich.source.nmsp_button', false);
            displayElement('mediator.enrich.source.val_ex', false);
            displayElement('sourcePropertyLabel', false);
            displayElement('sourceInlineLabel', false);
            displayElement('inlineEnrichText', false);
            displayElement('inlineXMLRadio', false);
            displayElement('inlineRegRadio', false);
            displayElement('registryInlineLabel', false);
            displayElement('mediator.enrich.reg.key', false);
            displayElement('inlineRegBrowser', false);
        } else if (selectedElmValue == "body") {
            displayElement('sourceXpathExpLabel', false);
            displayElement('mediator.enrich.source.nmsp_button', false);
            displayElement('mediator.enrich.source.val_ex', false);
            displayElement('sourcePropertyLabel', false);
            displayElement('sourceInlineLabel', false);
            displayElement('inlineEnrichText', false);
            displayElement('inlineXMLRadio', false);
            displayElement('inlineRegRadio', false);
            displayElement('registryInlineLabel', false);
            displayElement('mediator.enrich.reg.key', false);
            displayElement('inlineRegBrowser', false);
        } else if (selectedElmValue == "property") {
            displayElement('sourceXpathExpLabel', false);
            displayElement('mediator.enrich.source.nmsp_button', false);
            displayElement('mediator.enrich.source.val_ex', true);
            displayElement('sourcePropertyLabel', true);
            displayElement('sourceInlineLabel', false);
            displayElement('inlineEnrichText', false);
            displayElement('inlineXMLRadio', false);
            displayElement('inlineRegRadio', false);
            displayElement('registryInlineLabel', false);
            displayElement('mediator.enrich.reg.key', false);
            displayElement('inlineRegBrowser', false);
        } else if (selectedElmValue == "inline") {
            displayElement('sourceXpathExpLabel', false);
            displayElement('mediator.enrich.source.nmsp_button', false);
            displayElement('mediator.enrich.source.val_ex', false);
            displayElement('sourcePropertyLabel', false);
            displayElement('sourceInlineLabel', true);
            displayElement('inlineEnrichText', true);
            displayElement('inlineXMLRadio', true);
            displayElement('inlineRegRadio', true);
            displayElement('registryInlineLabel', false);
            displayElement('mediator.enrich.reg.key', false);
            displayElement('inlineRegBrowser', false);


        }
    }

    function changeTargetElements(elm) {
        var selectedElmValue = elm.options[elm.selectedIndex].value;
        if (selectedElmValue == "custom") {
            displayElement('mediator.enrich.target.val_ex', true);
            displayElement('targetXpathExpLabel', true);
            displayElement('mediator.enrich.target.nmsp_button', true);
            displayElement('targetPropertyLabel', false);
            displayElement('targetInlineLabel', false);
        } else if (selectedElmValue == "envelope") {
            displayElement('mediator.enrich.target.val_ex', false);
            displayElement('targetXpathExpLabel', false);
            displayElement('mediator.enrich.target.nmsp_button', false);
            displayElement('targetPropertyLabel', false);
            displayElement('targetInlineLabel', false);
        } else if (selectedElmValue == "body") {
            displayElement('mediator.enrich.target.val_ex', false);
            displayElement('targetXpathExpLabel', false);
            displayElement('mediator.enrich.target.nmsp_button', false);
            displayElement('targetPropertyLabel', false);
            displayElement('targetInlineLabel', false);
        } else if (selectedElmValue == "property") {
            displayElement('mediator.enrich.target.val_ex', true);
            displayElement('targetXpathExpLabel', false);
            displayElement('mediator.enrich.target.nmsp_button', false);
            displayElement('targetPropertyLabel', true);
            displayElement('targetInlineLabel', false);
        }
    }

    function validateCombinations(currElem){
        var elm = document.getElementById('mediator.enrich.target.type');
        var selectedElmValueForType = elm.options[elm.selectedIndex].value;

        var elemAction = document.getElementById('mediator.enrich.target.action');
        var selectedElmValueForAction = elemAction.options[elemAction.selectedIndex].value;

        var thisElemValue = currElem.options[currElem.selectedIndex].value;
        <%--alert("on validate : type :" + selectedElmValueForType + "  action : " + selectedElmValueForAction--%>
            <%--+ "  this : " + thisElemValue);--%>


        if(selectedElmValueForAction == "sibling" && selectedElmValueForType == "envelope" ){
            if(thisElemValue == "sibling"){
                //force option to 'replace'
                elm.value = 'body';
            }
        }

        if (selectedElmValueForType == "envelope") {
            elemAction.value = 'replace';
            elemAction.disabled = true;
        }
        else {
            elemAction.disabled = false;
        }
    }

    // here the past parameter is just not important.
    window.onload = validateCombinations(document.getElementById('mediator.enrich.target.action'));

</script>



<table class="normal" width="100%">
<tbody>
<tr>
    <td colspan="3"><h2><fmt:message key="mediator.enrich.header"/></h2></td>
</tr>
<tr>
    <td colspan="3"><h3><fmt:message key="mediator.enrich.sourceheader"/></h3></td>
</tr>
<tr>
    <td style="width:130px;"><fmt:message key="mediator.enrich.clone"/><font
            style="color: red; font-size: 8pt;"> *</font>
    </td>
    <td>
        <select id="mediator.enrich.source.clone" name="mediator.enrich.source.clone"
                style="width:150px;">
            <option value="true" <%=enrichMediator.getSourceClone() != null && enrichMediator.getSourceClone().equals("true") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.true"/>
            </option>
            <option value="false" <%=enrichMediator.getSourceClone() != null && enrichMediator.getSourceClone().equals("false") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.false"/>
            </option>
        </select>
    </td>
</tr>
<tr>
    <td style="width:130px;"><fmt:message key="mediator.enrich.type"/><font
            style="color: red; font-size: 8pt;"> *</font>
    </td>
    <td>
        <select id="mediator.enrich.source.type" name="mediator.enrich.source.type2"
                style="width:150px;" onchange="changeSourceElements(this)">
            <option value="custom" <%=enrichMediator.getSourceType() != null && enrichMediator.getSourceType().equals("custom") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.custom"/>
            </option>
            <option value="envelope" <%=enrichMediator.getSourceType() != null && enrichMediator.getSourceType().equals("envelope") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.envelope"/>
            </option>
            <option value="body" <%=enrichMediator.getSourceType() != null && enrichMediator.getSourceType().equals("body") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.body"/>
            </option>
            <option value="property" <%=enrichMediator.getSourceType() != null && enrichMediator.getSourceType().equals("property") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.property"/>
            </option>
            <option value="inline" <%=enrichMediator.getSourceType() != null && enrichMediator.getSourceType().equals("inline") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.inline"/>
            </option>
        </select>
    </td>
</tr>
<tr id="sourceExpressionRowId">
    <td>
        <label id="sourceXpathExpLabel" <%= sourceType != CUSTOM ? "style=\"display:none\"" : ""%>><fmt:message
                key="mediator.enrich.xpath.expression"/>
            <font style="color: red; font-size: 8pt;"> *</font>
        </label>
        <label <%= sourceType != PROPERTY ? "id=\"sourcePropertyLabel\" style=\"display:none\"" : "id=\"sourcePropertyLabel\"" %>><fmt:message
                key="mediator.enrich.property"/>
            <font style="color: red; font-size: 8pt;"> *</font>
        </label>

    </td>
    <td style="width:320px;">
        <input name="mediator.enrich.source.val_ex"
               type="text" id="mediator.enrich.source.val_ex"
                <%= (sourceType == ENVELOPE || sourceType == BODY || sourceType == INLINE) ? "style=\"display:none\"" : "id=\"mediator.enrich.source.val_ex\"" %>
               value="<%=sourceValue%>" style="width:300px;"/>
    </td>

    <td>
        <a href="#nsEditorLink" id="mediator.enrich.source.nmsp_button"
           onclick="showNameSpaceEditor('mediator.enrich.source.val_ex')"
           class="nseditor-icon-link"
                <%= sourceType != CUSTOM ? "style=\"display:none;padding-left:40px\"" : "style=\"padding-left:40px;\"" %>>
            <fmt:message key="mediator.enrich.namespace"/></a>
        <a name="nsEditorLink"></a>
    </td>
</tr>
<tr>
    <td id="inlineXMLRadio" <%= sourceType != INLINE ? "id=\"inlineXMLRadio\" style=\"display:none\"" : "id=\"inlineXMLRadio\"" %>>
        <input type="radio"
               onclick="javascript:displayElement('inlineEnrichText', true);
                        javascript:displayElement('sourceInlineLabel', true);
                        javascript:displayElement('inlineRegBrowser', false);
                        javascript:displayElement('registryInlineLabel', false);
                        javascript:displayElement('keyId', false);
                        javascript:displayElement('mediator.enrich.reg.key', false);"
               name="keygroup" <%=!isInlineRegResource ? "checked=\"checked\" value=\"InlineXML\"" : "value=\"InlineXML\""%>/>
        <fmt:message key="mediator.enrich.target.inline.content"/>
    </td>
    <td id="inlineRegRadio" <%= sourceType != INLINE ? "id=\"inlineRegRadio\" style=\"display:none\"" : "id=\"inlineRegRadio\"" %>>
        <input type="radio"
               onclick="javascript:displayElement('inlineEnrichText', false);
                        javascript:displayElement('inlineRegBrowser', true);
                        javascript:displayElement('registryInlineLabel', true);
                        javascript:displayElement('keyId', true);
                        javascript:displayElement('sourceInlineLabel', false);
                        javascript:displayElement('mediator.enrich.reg.key', true);"
               name="keygroup" <%=isInlineRegResource ? "checked=\"checked\" value=\"InlineRegKey\"" : "value=\"InlineRegKey\""%> />
        <fmt:message key="mediator.enrich.target.inline.reg"/>
    </td>

</tr>
<tr>
    <td>
        <label <%= ((sourceType != INLINE ) || (!isInlineRegResource))  ? "id=\"registryInlineLabel\" style=\"display:none\"" : "id=\"registryInlineLabel\"" %>>
            Registry Key</label>
    </td>

    <td <%= ((sourceType != INLINE) || (!isInlineRegResource)) ? "id=\"keyId\" style=\"display:none\"" : "id=\"keyId\"" %>>
        <input class="longInput" type="text"
               id="mediator.enrich.reg.key"
               name="mediator.enrich.reg.key"
               value="<%= inlineKey %>" readonly="readonly"/>
    </td>
    <td  <%= ((sourceType != INLINE) || (!isInlineRegResource)) ? "id=\"inlineRegBrowser\" style=\"display:none\"" : "id=\"inlineRegBrowser\"" %>>
        <a href="#registryBrowserLink"
           class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.enrich.reg.key','/_system/config')"><fmt:message key="conf.registry.keys"/></a>
        <a href="#registryBrowserLink"
           class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.enrich.reg.key','/_system/governance')"><fmt:message key="gov.registry.keys"/></a>
    </td>

</tr>
<tr>
    <td>
        <label <%= ((sourceType != INLINE) || isInlineRegResource) ? "id=\"sourceInlineLabel\" style=\"display:none\"" : "id=\"sourceInlineLabel\"" %>><fmt:message
                key="mediator.enrich.inline"/><font
                style="color: red; font-size: 8pt;"> *</font></label>
    </td>
    <td>
        <textarea name="inlineEnrichText" id="inlineEnrichText" title="In-lined XML"
                  cols="70" rows="18"
                <%= (sourceType == ENVELOPE || sourceType == BODY || sourceType == PROPERTY || sourceType == CUSTOM) || isInlineRegResource ? "style=\"display:none\"" :
                        "id=\"inlineEnrichText\" name=\"inlineEnrichText\" " %>><%=inlineXMLStr.trim()%></textarea>
    </td>
</tr>

<tr>
    <td colspan="3"><h3><fmt:message key="mediator.enrich.targetheader"/></h3></td>
</tr>
<tr>
    <td style="width:130px;"><fmt:message key="mediator.enrich.target.action"/><font
            style="color: red; font-size: 8pt;"> *</font>
    </td>
    <td>
        <select id="mediator.enrich.target.action" name="mediator.enrich.target.action"
                style="width:150px;" onchange="validateCombinations(this)">
            <option value="replace" <%=enrichMediator.getTargetAction() != null && enrichMediator.getTargetAction().equals("replace") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.target.action.replace"/>
            </option>
            <option value="child" <%=enrichMediator.getTargetAction() != null && enrichMediator.getTargetAction().equals("child") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.target.action.child"/>
            </option>
            <option id="mediator.enrich.action.sibling" value="sibling" <%=enrichMediator.getTargetAction() != null && enrichMediator.getTargetAction().equals("sibling") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.target.action.sibling"/>
            </option>
        </select>
    </td>
</tr>
<tr>
    <td style="width:130px;"><fmt:message key="mediator.enrich.type"/><font
            style="color: red; font-size: 8pt;"> *</font>
    </td>
    <td>
        <select id="mediator.enrich.target.type" name="mediator.enrich.target.type"
                style="width:150px;" onchange="changeTargetElements(this);validateCombinations(this);">
            <option value="custom" <%=enrichMediator.getTargetType() != null && enrichMediator.getTargetType().equals("custom") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.custom"/>
            </option>
            <option id="mediator.enrich.type.envelope" value="envelope" <%=enrichMediator.getTargetType() != null && enrichMediator.getTargetType().equals("envelope") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.envelope"/>
            </option>
            <option value="body" <%=enrichMediator.getTargetType() != null && enrichMediator.getTargetType().equals("body") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.body"/>
            </option>
            <option value="property" <%=enrichMediator.getTargetType() != null && enrichMediator.getTargetType().equals("property") ? "selected=\"selected\"" : ""%>>
                <fmt:message key="mediator.enrich.type.property"/>
            </option>
        </select>
    </td>
</tr>
<tr id="targetExpressionRowId">
    <td>
        <label id="targetXpathExpLabel" <%= targetType != CUSTOM ? "style=\"display:none\"" : ""%>><fmt:message
                key="mediator.enrich.xpath.expression"/>
            <font style="color: red; font-size: 8pt;"> *</font>
        </label>
        <label <%= targetType != PROPERTY ? "id=\"targetPropertyLabel\" style=\"display:none\"" : "id=\"targetPropertyLabel\"" %>><fmt:message
                key="mediator.enrich.property"/>
            <font style="color: red; font-size: 8pt;"> *</font>
        </label>

    </td>
    <td style="width:320px;">
        <input name="mediator.enrich.target.val_ex"
               type="text" id="mediator.enrich.target.val_ex"
                <%= (targetType == ENVELOPE || targetType == BODY || targetType == INLINE) ? "style=\"display:none\"" : "id=\"mediator.enrich.target.val_ex\"" %>
               value="<%=targetValue%>" style="width:300px;"/>
    </td>
    <td>
        <a href="#nsEditorLinkTarget" id="mediator.enrich.target.nmsp_button"
           onclick="showNameSpaceEditor('mediator.enrich.target.val_ex')"
           class="nseditor-icon-link"
                <%= targetType != CUSTOM ? "style=\"display:none;padding-left:40px\"" : "style=\"padding-left:40px;\"" %>>
            <fmt:message key="mediator.enrich.namespace"/></a>
        <a name="nsEditorLinkTarget"></a>
    </td>
</tr>
</tbody>
</table>
<a name="registryBrowserLink"></a>

<div id="registryBrowser" style="display:none;"></div>

</div>
</fmt:bundle>