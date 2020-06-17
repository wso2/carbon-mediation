<%--
  ~  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    String loginContextName = null;
    String loginConfig = "";
    String krb5Config = "";
    String spn = "";
    boolean keytabSelected = true;
    Value clientPrincipal = null;
    Value password = null;
    Value keytabConfig = null;
    Value spnConfigRegistryPath = null;

    try {
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof org.wso2.carbon.mediator.kerberos.ui.KerberosMediator)) {
            throw new RuntimeException("Unable to update the mediator");
        }
        org.wso2.carbon.mediator.kerberos.ui.KerberosMediator kerberosMediator =
                (org.wso2.carbon.mediator.kerberos.ui.KerberosMediator) mediator;
        loginContextName = kerberosMediator.getLoginContextName();
        loginConfig = kerberosMediator.getLoginConfig();
        krb5Config = kerberosMediator.getKrb5Config();
        spn = kerberosMediator.getSpn();
        clientPrincipal = kerberosMediator.getClientPrincipal();
        password = kerberosMediator.getPassword();
        keytabConfig = kerberosMediator.getKeytabConfig();
        spnConfigRegistryPath = kerberosMediator.getSpnConfigKey();

        if(loginContextName == null){
            loginContextName = "";
        }
        if(clientPrincipal != null || password != null){
            keytabSelected = false;
        }
        if(loginConfig == null){
            loginConfig = "";
        }
        if(krb5Config == null){
            krb5Config = "";
        }
        if(spn == null){
            spn = "";
        }
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
 %>

<%@page import="org.wso2.carbon.mediator.kerberos.KerberosMediator"%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.kerberos.ui.i18n.Resources">
    <carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.kerberos.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="enti18n"/>
<div>
    <script type="text/javascript" src="../kerberos-mediator/js/mediator-util.js"></script>
    <script type="text/javascript">
        <% if (spnConfigRegistryPath == null) { %>
            document.getElementById("spnConfigOptionAnon").checked = true;
            spnConfigOptionAnonSelected();
        <% } else { %>
            document.getElementById("spnConfigOptionReference").checked = true;
            spnConfigOptionReferenceSelected()();
        <% } %>

    function spnConfigOptionAnonSelected() {
        var spnConfigAnnonButton = document.getElementById("spnConfigOptionAnon");
        var spnConfig = document.getElementById("mediator.kerberos.spn.txt.div");
        spnConfig.style.display = spnConfigAnnonButton.checked ? "block" : "none";
        document.getElementById("mediator.kerberos.spnKey.txt.div").style.display='none';
        document.getElementById("mediator.kerberos.spn.link.div").style.display='none';
    }

    function spnConfigOptionReferenceSelected() {
        var spnConfigRefButton = document.getElementById("spnConfigOptionReference");
        var spnConfigRegistry = document.getElementById("mediator.kerberos.spnKey.txt.div");
        spnConfigRegistry.style.display = spnConfigRefButton.checked ? "block" : "none";
        var loadSpnFromRegistry = document.getElementById("mediator.kerberos.spn.link.div");
        loadSpnFromRegistry.style.display = spnConfigRefButton.checked ? "block" : "none";
        document.getElementById("mediator.kerberos.spn.txt.div").style.display='none';
    }
    </script>
    <table class="normal" width="100%">
        <tr>
            <td>
                <h2><fmt:message key="mediator.kerberos.header"/></h2>
            </td>
        </tr>
        <tr>
            <td>
                <table class="normal">
                    <tr></tr>
                    <tr>
                        <td class="leftCol-small" rowspan="3">
                            <fmt:message key="mediator.kerberos.spn"/><span class="required">*</span>
                        </td>
                        <td>
                            <input type="radio" id="spnConfigOptionAnon" name="spnOption" value="annonSpn"
                            onclick="spnConfigOptionAnonSelected()"/><fmt:message key="mediator.kerberos.anonymous"/>
                        </td>
                        <td>
                            <div id="mediator.kerberos.spn.txt.div" style="display: none">
                                <input type="text" id="spn" name="spn" value="<%=spn%>" />
                            </div>
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>
                            <input type="radio" id="spnConfigOptionReference" name="spnOption"
                            value="spnSelectFromRegistry" onclick="spnConfigOptionReferenceSelected()"/>
                            <fmt:message key="mediator.kerberos.registry"/>
                        </td>
                        <td>
                            <div id="mediator.kerberos.spnKey.txt.div" style="display: none">
                                <input type="text" id="spnConfigKey" name="spnConfigKey"
                                value="<%=(spnConfigRegistryPath != null)?spnConfigRegistryPath.getKeyValue():""%>"
                                readonly="true"/>
                            </div>
                        </td>
                        <td>
                            <div id="mediator.kerberos.spn.link.div" style="display: none">
                                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('spnConfigKey','/_system/config')"><fmt:message
                                key="mediator.kerberos.conf.registry.browser"/></a>
                                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('spnConfigKey','/_system/governance')"><fmt:message
                                key="mediator.kerberos.gov.registry.browser"/></a>
                        </div>
                        </td>
                    </tr>
                    <tr></tr>
                    <tr>
                        <td><fmt:message key="mediator.kerberos.AuthMethod"/></td>
                        <td colspan="2">
                            <input type="radio"
                                   onclick="javascript:displayElement('usernameauthprincipal', false);displayElement('usernameauthpassword', false);displayElement('keytabauthkrb', true);displayElement('keytabauthlogin', true);displayElement('keytabauthcontext', true);clearValues('keytab');"
                                   name="authgroup" id="keytabauthgroup" value="keytabauth" <% if (keytabSelected) { %> checked="checked" <% } %> />
                            <label>Keytab Auth</label>
                            <input type="radio"
                                   onclick="javascript:displayElement('usernameauthprincipal', true);displayElement('usernameauthpassword', true);displayElement('keytabauthkrb', false);displayElement('keytabauthlogin', false);displayElement('keytabauthcontext', false);clearValues('username');"
                                   name="authgroup" id="usernameauthgroup" value="usernameauth" <% if (!keytabSelected) { %> checked="checked" <% } %> />
                            <label>Username Auth</label>
                        </td>
                    </tr>
                    <tr id="keytabauthkrb" style="<%=keytabSelected ? "" : "display:none"%>">
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.krb5Config"/>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="krb5Config" name="krb5Config" value="<%=krb5Config%>" />
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr id="keytabauthlogin" style="<%=keytabSelected ? "" : "display:none"%>">
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.loginConfig"/>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="loginConfig" name="loginConfig" value="<%=loginConfig%>" />
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr id="keytabauthcontext" style="<%=keytabSelected ? "" : "display:none"%>">
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.loginContextName"/><span class="required">*</span>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="loginContextName" name="loginContextName" value="<%=loginContextName%>" />
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
                    </div>
                    <tr id="usernameauthprincipal" style="<%=keytabSelected ? "display:none" : ""%>">
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.clientPrincipal"/><span class="required">*</span>
                        </td>
                        <td class="text-box-big">
                            <%
                            if (clientPrincipal != null && clientPrincipal.getKeyValue() != null) {
                            %>
                                <input type="text" id="clientPrincipal" name="clientPrincipal" value="<%=clientPrincipal.getKeyValue()%>" />
                            <%
                            } else {
                            %>
                                <input type="text" id="clientPrincipal" name="clientPrincipal" value="" />
                            <%
                            }
                            %>
                        </td>
                    </tr>
                    <tr id="usernameauthpassword" style="<%=keytabSelected ? "display:none" : ""%>">
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.password"/><span class="required">*</span>
                        </td>
                        <td class="text-box-big">
                            <%
                            if (password != null && password.getKeyValue() != null) {
                            %>
                                <input type="password" id="password" name="password" value="<%=password.getKeyValue()%>" />
                            <%
                            } else {
                            %>
                                <input type="password" id="password" name="password" value="" />
                            <%
                            }
                            %>
                        </td>
                    </tr>
                    </div>
                </table>
            </td>
        </tr>
    </table>
</div>
</fmt:bundle>