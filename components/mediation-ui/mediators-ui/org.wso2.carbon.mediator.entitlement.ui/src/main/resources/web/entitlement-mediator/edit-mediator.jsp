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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediator.entitlement.EntitlementMediator"%>
<%@ page import="org.wso2.carbon.mediator.entitlement.OnAcceptMediator" %>
<%@ page import="org.wso2.carbon.mediator.entitlement.OnRejectMediator" %>
<%@ page import="org.wso2.carbon.mediator.entitlement.AdviceMediator" %>
<%@ page import="org.wso2.carbon.mediator.entitlement.ObligationsMediator" %>

<%
        String remoteServiceUserName = null;
		String remoteServicePassword = null;
		String remoteServiceUrl = null;
        boolean utCBH = false, x509CBH = false, samlCBH = false, kerberosCBH = false, customCBH = false;
        String callbackClass = null;
        String client = null;
        String thriftHost = null;
        String thriftPort = null;
        boolean acceptInline = false;
        boolean rejectInline = false;
        boolean obligationsInline = false;
        boolean adviceInline = false;
        String acceptKey = "", rejectKey = "", obligationsKey = "", adviceKey = "";

		try {
            Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
            if (!(mediator instanceof EntitlementMediator)) {
                throw new RuntimeException("Unable to update the mediator");
            }
            EntitlementMediator entMediator = (EntitlementMediator)mediator;
            remoteServiceUrl = entMediator.getRemoteServiceUrl();
            if(remoteServiceUrl==null){
            	remoteServiceUrl ="";
            }
            remoteServiceUserName = entMediator.getRemoteServiceUserName();
            if(remoteServiceUserName==null){
            	remoteServiceUserName ="";
            }
            remoteServicePassword = entMediator.getRemoteServicePassword();
            if(remoteServicePassword==null){
            	remoteServicePassword ="";
            }
            callbackClass = entMediator.getCallbackClass();
            if(callbackClass == null  ||
                    callbackClass.equals("org.wso2.carbon.identity.entitlement.mediator.callback.UTEntitlementCallbackHandler")){
                callbackClass = "";
                utCBH = true;
            } else if (callbackClass.equals("org.wso2.carbon.identity.entitlement.mediator.callback.X509EntitlementCallbackHandler")){
                callbackClass = "";
                x509CBH = true;
            } else if (callbackClass.equals("org.wso2.carbon.identity.entitlement.mediator.callback.SAMLEntitlementCallbackHandler")){
                callbackClass = "";
                samlCBH = true;
            } else if (callbackClass.equals("org.wso2.carbon.identity.entitlement.mediator.callback.KerberosEntitlementCallbackHandler")){
                callbackClass = "";
                kerberosCBH = true;
            } else {
                customCBH = true;
            }
            client = entMediator.getClient();
            if(client == null){
                client = "basicAuth";
            }
            thriftHost = entMediator.getThriftHost();
            if(thriftHost == null){
                thriftHost = "";
            }
            thriftPort = entMediator.getThriftPort();
            if(thriftPort == null){
                thriftPort = "";
            }
            for (Mediator m : entMediator.getList()) {
                if (m instanceof OnAcceptMediator) {
                    acceptInline = true;
                } else if (m instanceof OnRejectMediator) {
                    rejectInline = true;
                } else if (m instanceof ObligationsMediator) {
                    obligationsInline = true;
                } else if (m instanceof AdviceMediator) {
                    adviceInline = true;
                }
            }

            if (!acceptInline && entMediator.getOnAcceptSeqKey() != null) {
                acceptKey = entMediator.getOnAcceptSeqKey();
            }
            if (!rejectInline && entMediator.getOnRejectSeqKey() != null) {
                rejectKey = entMediator.getOnRejectSeqKey();
            }
            if (!obligationsInline && entMediator.getObligationsSeqKey() != null) {
                obligationsKey = entMediator.getObligationsSeqKey();
            }
            if (!adviceInline && entMediator.getAdviceSeqKey() != null) {
                adviceKey = entMediator.getAdviceSeqKey();
            }
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
            

            <script type="text/javascript">
                   location.href = "../admin/error.jsp";
            </script>
<%
            return;
        }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.entitlement.ui.i18n.Resources">
    <carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.entitlement.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="enti18n"/>
<div>
    <script type="text/javascript" src="../entitlement-mediator/js/mediator-util.js"></script>

    <table class="normal" width="100%">
        <tr>
            <td>
                <h2><fmt:message key="mediator.ent.header"/></h2>
            </td>
        </tr>

        <tr>
            <td>
                <table style="width: 100%">
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.ent.remoteservice"/>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="remoteServiceUrl" name="remoteServiceUrl" value="<%=remoteServiceUrl%>" />
                        </td>
                    </tr>
                      <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.ent.remoteservice.user"/>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="remoteServiceUserName" name="remoteServiceUserName" value="<%=remoteServiceUserName%>" />
                        </td>
                    </tr>
                      <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.ent.remoteservice.password"/>
                        </td>
                        <td class="text-box-big">
                            <input type="password" id="remoteServicePassword" name="remoteServicePassword" value="<%=remoteServicePassword%>" />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.ent.callback"/>
                        </td>
                        <td>
                            <input type="radio"
                                   onclick="javascript:toggleCallback(this);"
                                   name="callbackClassOption" value="org.wso2.carbon.identity.entitlement.mediator.callback.UTEntitlementCallbackHandler"
                                    <% if (utCBH) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.callback.ut"/></label>
                            <input type="radio"
                                   onclick="javascript:toggleCallback(this);"
                                   name="callbackClassOption" value="org.wso2.carbon.identity.entitlement.mediator.callback.X509EntitlementCallbackHandler"
                                    <% if (x509CBH) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.callback.x509"/></label>
                            <input type="radio"
                                   onclick="javascript:toggleCallback(this);"
                                   name="callbackClassOption" value="org.wso2.carbon.identity.entitlement.mediator.callback.SAMLEntitlementCallbackHandler"
                                    <% if (samlCBH) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.callback.saml"/></label>
                            <input type="radio"
                                   onclick="javascript:toggleCallback(this);"
                                   name="callbackClassOption" value="org.wso2.carbon.identity.entitlement.mediator.callback.KerberosEntitlementCallbackHandler"
                                    <% if (kerberosCBH) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.callback.kerberos"/></label>
                            <input type="radio"
                                   onclick="javascript:toggleCallback(this);"
                                   name="callbackClassOption" value="<%=callbackClass%>"
                                   id="callbackClassOptionCustom"
                                    <% if (customCBH) { %> checked="checked" <% } %> />
                            <label><fmt:message key="custom"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-small"/>
                        <td class="text-box-big">
                            <label><fmt:message key="mediator.ent.callback.custom"/></label>
                            <input type="text" id="callbackClass" name="callbackClass" value="<%=callbackClass%>" <% if (!customCBH) {%> disabled="disabled" <% } %> />
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.ent.client.type"/>
                        </td>
                        <td>
                            <input type="radio"
                                   onclick="javascript:toggleClient(this);"
                                   name="client" value="basicAuth"
                                    <% if (client.equals("basicAuth")) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.client.type.basicAuth"/></label>
                            <input type="radio"
                                   onclick="javascript:toggleClient(this);"
                                   name="client" value="thrift"
                                   id="thrift"
                                    <% if (client.equals("thrift")) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.client.type.thrift"/></label>
                            <input type="radio"
                                   onclick="javascript:toggleClient(this);"
                                   name="client" value="soap"
                                    <% if (client.equals("soap")) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.client.type.soap"/></label>
                            <input type="radio"
                                   onclick="javascript:toggleClient(this);"
                                   name="client" value="wsXacml"
                                    <% if (client.equals("wsXacml")) { %> checked="checked" <% } %> />
                            <label><fmt:message key="mediator.ent.client.type.wsXacml"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-small"/>
                        <td class="text-box-big">
                            <label><fmt:message key="mediator.ent.thriftHost"/></label>
                            <input type="text" id="thriftHost" name="thriftHost" value="<%=thriftHost%>" <% if (!client.equals("thrift")) {%> disabled="disabled" <% } %> />
                            <fmt:message key="mediator.ent.thriftPort"/>
                            <input type="text" id="thriftPort" name="thriftPort" value="<%=thriftPort%>" <% if (!client.equals("thrift")) {%> disabled="disabled" <% } %> />
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
                            <input type="radio"
                                   onclick="javascript:displayElement('onaccept_refer_seq', false);"
                                   name="onacceptgroup" value="onAcceptSequence" <% if (acceptInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="in.lined.sequence"/></label>
                            <input type="radio"
                                   onclick="javascript:displayElement('onaccept_refer_seq', true);"
                                   name="onacceptgroup" value="onAcceptSequenceKey" <% if (!acceptInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="referring.sequence"/></label>
                        </td>
                    </tr>
                    <tr id="onaccept_refer_seq" style="<%=acceptInline ? "display:none" : ""%>">
                        <td><fmt:message key="referring.sequence"/></td>
                        <td><input class="longInput" type="text" name="mediator.entitlement.acceptKey"
                                   id="mediator.entitlement.acceptKey" value="<%=acceptKey%>" style="float:left" readonly="readonly"/>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.acceptKey','/_system/config')"><fmt:message
                                key="conf.key"/></a>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.acceptKey','/_system/governance')"><fmt:message
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
                            <input type="radio"
                                   onclick="javascript:displayElement('onareject_refer_seq', false);"
                                   name="onrejectgroup"
                                   value="onRejectSequence" <% if (rejectInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="in.lined.sequence"/></label>
                            <input type="radio"
                                   onclick="javascript:displayElement('onareject_refer_seq', true);"
                                   name="onrejectgroup"
                                   value="onRejectSequenceKey" <% if (!rejectInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="referring.sequence"/></label>
                        </td>
                    </tr>
                    <tr id="onareject_refer_seq" style="<%=rejectInline ? "display:none" : ""%>">
                        <td><fmt:message key="referring.sequence"/></td>
                        <td><input class="longInput" type="text" name="mediator.entitlement.rejectKey"
                                   id="mediator.entitlement.rejectKey" value="<%=rejectKey%>" style="float:left" readonly="readonly"/>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.rejectKey','/_system/config')"><fmt:message
                                key="conf.key"/></a>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.rejectKey','/_system/governance')"><fmt:message
                                key="gov.key"/></a>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="3">
                            <h3 class="mediator"><fmt:message key="obligations"/></h3>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="specify.as"/></td>
                        <td colspan="2">
                            <input type="radio"
                                   onclick="javascript:displayElement('obligations_refer_seq', false);"
                                   name="obligationsgroup" value="obligationsSequence" <% if (obligationsInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="in.lined.sequence"/></label>
                            <input type="radio"
                                   onclick="javascript:displayElement('obligations_refer_seq', true);"
                                   name="obligationsgroup" value="obligationsSequenceKey" <% if (!obligationsInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="referring.sequence"/></label>
                        </td>
                    </tr>
                    <tr id="obligations_refer_seq" style="<%=obligationsInline ? "display:none" : ""%>">
                        <td><fmt:message key="referring.sequence"/></td>
                        <td><input class="longInput" type="text" name="mediator.entitlement.obligationsKey"
                                   id="mediator.entitlement.obligationsKey" value="<%=obligationsKey%>" style="float:left" readonly="readonly"/>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.obligationsKey','/_system/config')"><fmt:message
                                key="conf.key"/></a>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.obligationsKey','/_system/governance')"><fmt:message
                                key="gov.key"/></a>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="3">
                            <h3 class="mediator"><fmt:message key="advice"/></h3>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="specify.as"/></td>
                        <td colspan="2">
                            <input type="radio"
                                   onclick="javascript:displayElement('advice_refer_seq', false);"
                                   name="advicegroup" value="adviceSequence" <% if (adviceInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="in.lined.sequence"/></label>
                            <input type="radio"
                                   onclick="javascript:displayElement('advice_refer_seq', true);"
                                   name="advicegroup" value="adviceSequenceKey" <% if (!adviceInline) { %> checked="checked" <% } %> />
                            <label><fmt:message key="referring.sequence"/></label>
                        </td>
                    </tr>
                    <tr id="advice_refer_seq" style="<%=adviceInline ? "display:none" : ""%>">
                        <td><fmt:message key="referring.sequence"/></td>
                        <td><input class="longInput" type="text" name="mediator.entitlement.adviceKey"
                                   id="mediator.entitlement.adviceKey" value="<%=adviceKey%>" style="float:left" readonly="readonly"/>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.adviceKey','/_system/config')"><fmt:message
                                key="conf.key"/></a>
                            <a href="#registryBrowserLink" class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.entitlement.adviceKey','/_system/governance')"><fmt:message
                                key="gov.key"/></a>
                        </td>
                    </tr>

                </table>
            </td>
        </tr>
    </table>
</div>
</fmt:bundle>