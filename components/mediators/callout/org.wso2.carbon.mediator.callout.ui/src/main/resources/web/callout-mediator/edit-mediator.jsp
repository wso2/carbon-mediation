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

<%@ page import="org.wso2.carbon.mediator.callout.CalloutMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String serviceURL = "", action = "", endpointKey = "" ;
    String targetVal = "", sourceVal = "";
    String repo = "", axis2XML = "" , initAxis2ClientOptions="";
    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
    boolean isTargetXpath = false, isSourceXpath = false, isSourceEnvelope = false, isSourceKey = false;
    boolean isEnableWSSec = false;
    String secPolicy = "";
    boolean useDifferentPoliciesForInAndOut = false;
    String secOutboundPolicy = "";
    String secInboundPolicy = "";
    boolean isServiceUrlEndpoint = false; //Whether Service URL is specifies as a Endpoint
    if (!(mediator instanceof CalloutMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CalloutMediator calloutMediator = (CalloutMediator) mediator;
    if (calloutMediator.getServiceURL() != null) {
        serviceURL = calloutMediator.getServiceURL();
    }
    if (calloutMediator.getAction() != null) {
        action = calloutMediator.getAction();
    }
    if (calloutMediator.getClientRepository() != null) {
        repo = calloutMediator.getClientRepository();
    }
    if (calloutMediator.getAxis2xml() != null) {
        axis2XML = calloutMediator.getAxis2xml();
    }

    if(calloutMediator.getInitAxis2ClientOptions() != null) {
        initAxis2ClientOptions = calloutMediator.getInitAxis2ClientOptions();
    }

    if (calloutMediator.isUseEnvelopeAsSource()) {
        isSourceEnvelope = true;
        isSourceXpath = false;
        isSourceKey=false;
    } else if (calloutMediator.getRequestKey() != null) {
        isSourceXpath = false;
        isSourceKey=true;
        sourceVal = calloutMediator.getRequestKey();
    } else if (calloutMediator.getRequestXPath() != null){
        isSourceXpath = true;
        isSourceKey=false;
        sourceVal = calloutMediator.getRequestXPath().toString();
        nmspRegistrar.registerNameSpaces(calloutMediator.getRequestXPath(), "mediator.callout.source.xpath_val", session);
    }

    if (calloutMediator.getTargetKey() != null) {
        isTargetXpath = false;
        targetVal = calloutMediator.getTargetKey();
    } else if (calloutMediator.getTargetXPath() != null){
        isTargetXpath = true;
        targetVal = calloutMediator.getTargetXPath().toString();
        nmspRegistrar.registerNameSpaces(calloutMediator.getTargetXPath(), "mediator.callout.target.xpath_val", session);
    }

    if(calloutMediator.getEndpointKey() != null) {
        isServiceUrlEndpoint =true;
        endpointKey = calloutMediator.getEndpointKey();
    } else if (calloutMediator.getServiceURL() != null) {
        isServiceUrlEndpoint =false;
        serviceURL = calloutMediator.getServiceURL();
    }

    if (calloutMediator.isSecurityOn()) {
        isEnableWSSec = true;
        // Sec. policy
        if (calloutMediator.getWsSecPolicyKey() != null) {
            secPolicy = calloutMediator.getWsSecPolicyKey();
        } else {
            useDifferentPoliciesForInAndOut = true;
            if (calloutMediator.getOutboundWsSecPolicyKey() != null) {
                secOutboundPolicy = calloutMediator.getOutboundWsSecPolicyKey();
            }
            if (calloutMediator.getInboundWsSecPolicyKey() != null) {
                secInboundPolicy = calloutMediator.getInboundWsSecPolicyKey();
            }
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.callout.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.callout.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="calloutMediatorJsi18n"/>
<div>
    <script type="text/javascript" src="../callout-mediator/js/mediator-util.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <table class="normal" width="100%">
    <tr>
        <td>
            <h2><fmt:message key="mediator.callout.header"/></h2>
        </td>
    </tr>
    <tr>
        <td>
        <h3 class="mediator"><fmt:message key="mediator.callout.serviceurl"/></h3>
        <table class="normal">
                <tr>
                    <td class="leftCol-small">
                        <fmt:message key="mediator.callout.specifyas"/> :
                    </td>
                    <td>
                        <input type="radio" id="serviceURL"
                               onclick="javascript:displayElement('mediator.callout.serviceurl.url', true); javascript:displayElement('mediator.callout.serviceurl.key', false);"
                               name="serviceURLGroup" <%=!isServiceUrlEndpoint ? "checked=\"checked\" value=\"URL\"" : "value=\"URL\""%>/>
                        <fmt:message key="mediator.callout.serviceurl.url"/>
                        <input type="radio"
                               onclick="javascript:displayElement('mediator.callout.serviceurl.url', false); javascript:displayElement('mediator.callout.serviceurl.key', true);"
                               name="serviceURLGroup" <%=isServiceUrlEndpoint ? "checked=\"checked\" value=\"Endpoint\"" : "value=\"Endpoint\""%>/>
                        <fmt:message key="mediator.callout.serviceurl.key"/>
                    </td>
                </tr>
                <tr id="mediator.callout.serviceurl.url" <%=isServiceUrlEndpoint ? "style=\"display:none\";" : ""%> >
                    <td><fmt:message key="mediator.callout.serviceurl.url"/></td>
                    <td><input type="text" id="mediator.callout.serviceurl.url.value" name="mediator.callout.serviceurl.url.value" style="width:300px"
                               value="<%=serviceURL%>"/></td>
                </tr>
                <tr id="mediator.callout.serviceurl.key" <%=!isServiceUrlEndpoint ? "style=\"display:none\";" : ""%> >
                    <td><fmt:message key="mediator.callout.serviceurl.key"/></td>
                    <td><input type="text" id="mediator.callout.serviceurl.key.value" name="mediator.callout.serviceurl.key.value" style="width:300px"
                                value="<%=endpointKey%>" readonly="readonly"/>
                    </td>
                    <td>
                        <a id="confRegEpLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('mediator.callout.serviceurl.key.value','/_system/config');">
                            Configuration Registry</a>
                    </td>
                    <td>
                        <a id="govRegEpLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('mediator.callout.serviceurl.key.value','/_system/governance');">
                            Governance Registry</a>
                    </td>
                </tr>
        </table>
    </td>
    </tr>
    <tr>
    <td>
    <table border="0" class="normal" >
            <tr>
                <td>
                    <fmt:message key="mediator.callout.action"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.callout.action" name="mediator.callout.action" value="<%=action%>" style="width:300px"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="mediator.callout.repo"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.callout.repo" name="mediator.callout.repo" value="<%=repo%>" style="width:300px"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="mediator.callout.axis2XML"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.callout.axis2XML" name="mediator.callout.axis2XML" value="<%=axis2XML%>" style="width:300px"/>
                </td>
            </tr>
         <tr>
                <td>
                    <fmt:message key="mediator.callout.initAxis2ClientOptions"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.callout.initAxis2ClientOptions" name="mediator.callout.initAxis2ClientOptions" value="<%=initAxis2ClientOptions%>" style="width:300px"/>
                </td>
            </tr>
    </table>
    </td>
    </tr>
    <tr>
        <td>
        <h3 class="mediator"><fmt:message key="mediator.callout.source"/> <span class="required">*</span></h3>
        <table class="normal">
            <tr>
                <td class="leftCol-small">
                    <fmt:message key="mediator.callout.specifyas"/> :
                </td>
                <td>
                    <input type="radio" id="sourceGroupXPath"
                           onclick="javascript:displayElement('mediator.callout.source.xpath', true); displayElement('mediator.callout.source.namespace.editor', true); displayElement('mediator.callout.source.key', false);"
                           name="sourcegroup" <%=isSourceXpath ? "checked=\"checked\" value=\"XPath\"" : "value=\"XPath\""%>
                    "/>
                    <fmt:message key="mediator.callout.xpath"/>
                    <input type="radio"
                           onclick="javascript:displayElement('mediator.callout.source.xpath', false); javascript:displayElement('mediator.callout.source.key', true); displayElement('mediator.callout.source.namespace.editor', false);"
                           name="sourcegroup" <%=isSourceKey ? "checked=\"checked\" value=\"Key\"" : "value=\"Key\""%>/>
                    <fmt:message key="property.th"/>
                    <input type="radio" id="sourceGroupEnvelope"
                           onclick="javascript:displayElement('mediator.callout.source.xpath', false); javascript:displayElement('mediator.callout.source.key', false); displayElement('mediator.callout.source.namespace.editor', false);"
                           name="sourcegroup" <%=isSourceEnvelope ? "checked=\"checked\" value=\"Envelope\"" : "value=\"Envelope\""%>/>
                    <fmt:message key="source.envelope"/>
                </td>
                <td></td>
            </tr>
            <tr id="mediator.callout.source.xpath" <%=!isSourceXpath ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.callout.xpath"/></td>
                <td><input type="text" name="mediator.callout.source.xpath_val"
                           style="width:300px"
                           id="mediator.callout.source.xpath_val"
                           value="<%=sourceVal%>"/></td>
                <td><a id="mediator.callout.source.xpath_nmsp_button" href="#"
                       onclick="showNameSpaceEditor('mediator.callout.source.xpath_val')"
                       class="nseditor-icon-link"
                       style="padding-left:40px">
                    <fmt:message key="mediator.callout.namespace"/></a>
                </td>
            </tr>
            <tr id="mediator.callout.source.key" <%=!isSourceKey ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.callout.key"/></td>
                <td><input type="text" name="mediator.callout.source.key_val" style="width:300px"
                           id="mediator.callout.source.key_val" value="<%=sourceVal%>"/>
                </td>
                    <%--<td>--%>
                    <%--<a href="#" class="registry-picker-icon-link"--%>
                    <%--onclick="showRegistryBrowser('mediator.callout.source.key_val','/_system/config')"><fmt:message key="registry.conf.browser"/></a>--%>
                    <%--<a href="#" class="registry-picker-icon-link"--%>
                    <%--onclick="showRegistryBrowser('mediator.callout.source.key_val','/_system/governance')"><fmt:message key="registry.gov.browser"/></a>--%>
                    <%--</td>--%>
            </tr>
        </table>
    </td>
    </tr>
    <tr>
    <td>
        <h3 class="mediator">Target <span class="required">*</span></h3>
        <table class="normal">
                <tr>
                    <td class="leftCol-small">
                        <fmt:message key="mediator.callout.specifyas"/> :
                    </td>
                    <td>
                        <input type="radio" id="targetGroupXPath"
                               onclick="displayElement('mediator.callout.target.xpath', true); displayElement('mediator.callout.target.namespace.editor', true); displayElement('mediator.callout.target.key', false);"
                               name="targetgroup" <%=isTargetXpath ? "checked=\"checked\" value=\"XPath\"" : "value=\"XPath\""%>/>
                        <fmt:message key="mediator.callout.xpath"/>
                        <input type="radio"
                               onclick="displayElement('mediator.callout.target.xpath', false); displayElement('mediator.callout.target.namespace.editor', false); displayElement('mediator.callout.target.key', true);"
                               name="targetgroup" <%=!isTargetXpath ? "checked=\"checked\" value=\"Key\"" : "value=\"Key\""%>/>
                        <fmt:message key="property.th"/>
                    </td>
                    <td/>
                </tr>
                <tr id="mediator.callout.target.xpath" <%=!isTargetXpath ? "style=\"display:none\";" : ""%>>
                    <td><fmt:message key="mediator.callout.xpath"/></td>
                    <td><input type="text" name="mediator.callout.target.xpath_val" style="width:300px"
                               id="mediator.callout.target.xpath_val"
                               value="<%=targetVal%>"/></td>
                    <td><a id="mediator.callout.target.xpath_nmsp_button" href="#"
                           onclick="showNameSpaceEditor('mediator.callout.target.xpath_val')" class="nseditor-icon-link"
                                   style="padding-left:40px">
                        <fmt:message key="mediator.callout.namespace"/></a>
                    </td>
                </tr>
                <tr id="mediator.callout.target.key" <%=isTargetXpath ? "style=\"display:none\";" : ""%>>
                    <td><fmt:message key="mediator.callout.key"/></td>
                    <td><input type="text" name="mediator.callout.target.key_val"
                               id="mediator.callout.target.key_val" value="<%=targetVal%>" style="width:300px"/>
                    </td>
                    <%--<td>--%>
                        <%--<a href="#" class="registry-picker-icon-link"--%>
                        <%--onclick="showRegistryBrowser('mediator.callout.target.key_val','/_system/config')">--%>
                            <%--<fmt:message key="registry.conf.browser"/></a>--%>
                        <%--<a href="#" class="registry-picker-icon-link"--%>
                        <%--onclick="showRegistryBrowser('mediator.callout.target.key_val','/_system/governance')">--%>
                            <%--<fmt:message key="registry.gov.browser"/></a>--%>
                    <%--</td>--%>
                </tr>
        </table>
    </td>
    </tr>
    <tr>
        <td>
            <h3 class="mediator">WS-Security</h3>
            <table class="normal">
                <tr>
                    <td><fmt:message key="ws.security"/></td>
                    <td><input type="checkbox"
                               onclick="showHideWSSecRows()"
                               id="wsSecurity" name="wsSecurity" value="wsSecurity"
                            <%=isEnableWSSec ? "checked=\"checked\"" : ""%> />
                    </td>
                </tr>
                <tr id="tr_ws_use_different_policies" style="display:<%=isEnableWSSec?"":"none" %>">
                    <td><fmt:message key="use.different.policies"/></td>
                    <td><input type="checkbox"
                               onclick="showHideInOutWSSecRows()"
                               id="wsSecurityUseDifferentPolicies"
                               name="wsSecurityUseDifferentPolicies"
                               value="wsSecurityUseDifferentPolicies"
                            <%=useDifferentPoliciesForInAndOut ? "checked=\"checked\"" : ""%> />
                    </td>
                </tr>
                <tr id="tr_ws_sec_policy_key" style="display:<%=isEnableWSSec && !useDifferentPoliciesForInAndOut?"":"none" %>">
                    <td>
                        <div class="indented"><fmt:message key="policy.key"/></div>
                    </td>
                    <td>
                        <table class="normal">
                            <tr>
                                <td>
                                    <input class="longInput" type="text" id="wsSecPolicyKeyID"
                                           name="wsSecPolicyKeyID"
                                           value="<%=secPolicy%>" />
                                </td>
                                <td>
                                    <a href="#registryBrowserLink"
                                       class="registry-picker-icon-link"
                                       style="padding-left:20px;padding-right:20px"
                                       onclick="showRegistryBrowser('wsSecPolicyKeyID', '/_system/config')"><fmt:message
                                            key="registry.conf.keys"/></a>
                                    <a href="#registryBrowserLink"
                                       class="registry-picker-icon-link"
                                       style="padding-left:20px"
                                       onclick="showRegistryBrowser('wsSecPolicyKeyID', '/_system/governance')"><fmt:message
                                            key="registry.gov.keys"/></a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr id="tr_ws_sec_outbound_policy_key" style="display:<%=useDifferentPoliciesForInAndOut?"":"none" %>">
                    <td>
                        <div class="indented"><fmt:message key="outbound.policy.key"/></div>
                    </td>
                    <td>
                        <table class="normal">
                            <tr>
                                <td>
                                    <input class="longInput" type="text" id="wsSecOutboundPolicyKeyID"
                                           name="wsSecOutboundPolicyKeyID"
                                           value="<%=secOutboundPolicy%>" />
                                </td>
                                <td>
                                    <a href="#registryBrowserLink"
                                       class="registry-picker-icon-link"
                                       style="padding-left:20px;padding-right:20px"
                                       onclick="showRegistryBrowser('wsSecOutboundPolicyKeyID', '/_system/config')"><fmt:message
                                            key="registry.conf.keys"/></a>
                                    <a href="#registryBrowserLink"
                                       class="registry-picker-icon-link"
                                       style="padding-left:20px"
                                       onclick="showRegistryBrowser('wsSecOutboundPolicyKeyID', '/_system/governance')"><fmt:message
                                            key="registry.gov.keys"/></a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr id="tr_ws_sec_inbound_policy_key" style="display:<%=useDifferentPoliciesForInAndOut?"":"none" %>">
                    <td>
                        <div class="indented"><fmt:message key="inbound.policy.key"/></div>
                    </td>
                    <td>
                        <table class="normal">
                            <tr>
                                <td>
                                    <input class="longInput" type="text" id="wsSecInboundPolicyKeyID"
                                           name="wsSecInboundPolicyKeyID"
                                           value="<%=secInboundPolicy%>" />
                                </td>
                                <td>
                                    <a href="#registryBrowserLink"
                                       class="registry-picker-icon-link"
                                       style="padding-left:20px;padding-right:20px"
                                       onclick="showRegistryBrowser('wsSecInboundPolicyKeyID', '/_system/config')"><fmt:message
                                            key="registry.conf.keys"/></a>
                                    <a href="#registryBrowserLink"
                                       class="registry-picker-icon-link"
                                       style="padding-left:20px"
                                       onclick="showRegistryBrowser('wsSecInboundPolicyKeyID', '/_system/governance')"><fmt:message
                                            key="registry.gov.keys"/></a>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    </table>
</div>
</fmt:bundle>
