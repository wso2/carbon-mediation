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
<%@ page import="org.wso2.carbon.mediator.throttle.OnAcceptMediator" %>
<%@ page import="org.wso2.carbon.mediator.throttle.OnRejectMediator" %>
<%@ page import="org.wso2.carbon.mediator.throttle.ThrottleMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.throttle.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.throttle.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="thottleMediatorJsi18n"/>
<%
    String throttleGroup = "";
    boolean inLine = false;
    boolean acceptInline = false;
    boolean rejectInline = false;
    String inLinePolicy = "", acceptKey = "", rejectKey = "";
    String key = "";
    String policyID = SequenceEditorHelper.getEditingMediatorPosition(session) + "_throttle_policy";
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ThrottleMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ThrottleMediator throttleMediator = (ThrottleMediator) mediator;
    if (throttleMediator.getId() != null) {
        throttleGroup = throttleMediator.getId();
    }
    if (throttleMediator.getInLinePolicy() != null) {
        inLine = true;
        inLinePolicy = throttleMediator.getInLinePolicy().toString();
        Map policyXMLMap = (Map) request.getSession().getAttribute("throttle_policy_map");
        if (policyXMLMap == null) {
            policyXMLMap = new HashMap();
        }
        policyXMLMap.put(policyID, inLinePolicy);
        request.getSession().setAttribute("throttle_policy_map", policyXMLMap);
    } else if (throttleMediator.getPolicyKey() != null) {
        key = throttleMediator.getPolicyKey();
    }

    for (Mediator m : throttleMediator.getList()) {
        if (m instanceof OnAcceptMediator) {
            acceptInline = true;
        } else if (m instanceof OnRejectMediator) {
            rejectInline = true;
        }
    }

    if (!acceptInline && throttleMediator.getOnAcceptSeqKey() != null) {
        acceptKey = throttleMediator.getOnAcceptSeqKey();
    }
    if (!rejectInline && throttleMediator.getOnRejectSeqKey() != null) {
        rejectKey = throttleMediator.getOnRejectSeqKey();
    }
%>

<div>
<script type="text/javascript" src="../throttle-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
    <tbody>
        <tr>
            <td colspan="3">
                <h2><fmt:message key="throttle.mediator"/></h2>
            </td>
        </tr>
        <tr>
            <td style="width:150px"><fmt:message key="thottle.group.id"/><span class="required">*</span>
            </td>
            <td>
                <input class="longInput" type="text" id="throttle_id1" name="throttle_id1"  value="<%=throttleGroup%>"/>
            </td>
            <td></td>
        </tr>
    </tbody>
</table>
<table class="normal" width="100%">
<tbody>
<tr>
    <td colspan="3">
        <h3 class="mediator"><fmt:message key="throttle.policy"/></h3>
    <td>
</tr>

<tr>
    <td style="width:150px"><fmt:message key="specify.as"/></td>
    <td colspan="2">
        <% if (inLine) {%>
        <input type="radio"
               onclick="javascript:displayElement('refer_policy', false);displayElement('inline_policy_input', true);"
               name="policygroup"
               id="policygroupInlineId"
               value="InLinePolicy" checked="checked"/>
        <fmt:message key="in.lined.policy"/>
        <input type="radio"
               onclick="javascript:displayElement('refer_policy', true);displayElement('inline_policy_input', false);"
               name="policygroup"
               id="policygroupValueId"
               value="PolicyKey"/>
        <fmt:message key="referencing.policy"/>
        <%} else {%>
        <input type="radio"
               onclick="javascript:displayElement('refer_policy', false);displayElement('inline_policy_input', true);"
               name="policygroup"
               id="policygroupInlineId"
               value="InLinePolicy"/>
        <fmt:message key="in.lined.policy"/>
        <input type="radio"
               onclick="javascript:displayElement('refer_policy', true);displayElement('inline_policy_input', false);"
               name="policygroup" id="policygroupValueId"   checked="checked"
               value="PolicyKey"/>
        <fmt:message key="referencing.policy"/>
        <%}%>
    </td>
</tr>
<tr id="refer_policy" style="<%=inLine?"display:none" : ""%>">
    <td><fmt:message key="referring.policy"/></td>
    <td><input class="longInput" type="text" name="mediator.throttle.regPolicy"
               id="mediator.throttle.regPolicy" value="<%=key%>"
               readonly="disabled" style="float:left"/>
    <!--</td>-->
    <!--<td>-->
        <a href="#registryBrowserLink" class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.throttle.regPolicy','/_system/config')"><fmt:message
            key="conf.key"/></a>
        <a href="#registryBrowserLink" class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.throttle.regPolicy','/_system/governance')"><fmt:message
            key="gov.key"/></a>
    </td>
</tr>

<tr id="inline_policy_input" style="<%=!inLine?"display:none" : ""%>">
    <td><fmt:message key="in.lined.policy"/></td>
    <td><a href="#throttleBrowserLink" class="policie-icon-link"
           style="padding-left:40px"
           onclick="showInLinedThrottlePolicyEditor('<%=policyID%>')"><fmt:message
            key="throttle.policy.editor"/></a>
    </td>
</tr>

<tr>
    <td colspan="3">
        <h3 class="mediator"><fmt:message key="on.acceptance"/></h3>
    </td>
</tr>

<tr>
    <td><fmt:message key="specify.as"/></td>
    <td colspan="2">
        <% if (acceptInline) {%>
        <input type="radio"
               onclick="javascript:displayElement('onaccept_refer_seq', false);"
               name="onacceptgroup" value="onAcceptSequence" checked="checked"/>
        <label><fmt:message key="in.lined.sequence"/></label>
        <input type="radio"
               onclick="javascript:displayElement('onaccept_refer_seq', true);"
               name="onacceptgroup" value="onAcceptSequenceKey"/>
        <label><fmt:message key="referring.sequence"/></label>
        <% } else {%>
        <input type="radio"
               onclick="javascript:displayElement('onaccept_refer_seq', false);"
               name="onacceptgroup" value="onAcceptSequence"/>
        <label><fmt:message key="in.lined.sequence"/></label>
        <input type="radio"
               onclick="javascript:displayElement('onaccept_refer_seq', true);"
               name="onacceptgroup" value="onAcceptSequenceKey" checked="checked"/>
        <label><fmt:message key="referring.sequence"/></label>
        <%}%>
    </td>
</tr>
<tr id="onaccept_refer_seq" style="<%=acceptInline?"display:none" : ""%>">
    <td><fmt:message key="referring.sequence"/></td>
    <td><input class="longInput" type="text" name="mediator.throttle.acceptKey"
               id="mediator.throttle.acceptKey" value="<%=acceptKey%>" style="float:left" readonly="readonly"/>
    <!--</td>-->
    <!--<td>-->
        <a href="#registryBrowserLink" class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.throttle.acceptKey','/_system/config')"><fmt:message
            key="conf.key"/></a>
        <a href="#registryBrowserLink" class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.throttle.acceptKey','/_system/governance')"><fmt:message
            key="gov.key"/></a>
    </td>
</tr>

<tr>
    <td colspan="3">
        <h3 class="mediator"><fmt:message key="on.rejection"/></h3>
    </td>
</tr>

<tr>
    <td><fmt:message key="specify.as"/></td>
    <td colspan="2">
        <% if (rejectInline) {%>
        <input type="radio"
               onclick="javascript:displayElement('onareject_refer_seq', false);"
               name="onrejectgroup"
               value="onRejectSequence" checked="checked"/>
        <label><fmt:message key="in.lined.sequence"/></label>
        <input type="radio"
               onclick="javascript:displayElement('onareject_refer_seq', true);"
               name="onrejectgroup"
               value="onRejectSequenceKey"/>
        <label><fmt:message key="referring.sequence"/></label>
        <% } else {%>
        <input type="radio"
               onclick="javascript:displayElement('onareject_refer_seq', false);"
               name="onrejectgroup"
               value="onRejectSequence"/>
        <label><fmt:message key="in.lined.sequence"/></label>
        <input type="radio"
               onclick="javascript:displayElement('onareject_refer_seq', true);"
               name="onrejectgroup"
               value="onRejectSequenceKey" checked="checked"/>
        <label><fmt:message key="referring.sequence"/></label>
        <%}%>
    </td>
</tr>
<tr id="onareject_refer_seq" style="<%=rejectInline?"display:none" : ""%>">
    <td><fmt:message key="referring.sequence"/></td>
    <td><input class="longInput" type="text" name="mediator.throttle.rejectKey"
               id="mediator.throttle.rejectKey" value="<%=rejectKey%>" style="float:left" readonly="readonly"/>
    <!--</td>-->
    <!--<td>-->
        <a href="#registryBrowserLink" class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.throttle.rejectKey','/_system/config')"><fmt:message
            key="conf.key"/></a>
        <a href="#registryBrowserLink" class="registry-picker-icon-link"
           onclick="showRegistryBrowser('mediator.throttle.rejectKey','/_system/governance')"><fmt:message
            key="gov.key"/></a>
    </td>
</tr>
</tbody>
</table>
<a name="registryBrowserLink"></a>

<div id="registryBrowser" style="display:none;"></div>

<a name="throttleBrowserLink"></a>

<div id="throttleBrowser" style="display:none;"></div>
</div>
</fmt:bundle>