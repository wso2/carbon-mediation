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
    Value clientPrincipal = null;
    Value password = null;
    Value keytabPath = null;
    Value krb5ConfigRegistryPath = null;
    Value loginConfigRegistryPath = null;
    Value keyTabRegistryPath = null;

    try {
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof KerberosMediator)) {
            throw new RuntimeException("Unable to update the mediator");
        }
        KerberosMediator kerberosMediator = (KerberosMediator)mediator;
        loginContextName = kerberosMediator.getLoginContextName();
        loginConfig = kerberosMediator.getLoginConfig();
        krb5Config = kerberosMediator.getKrb5Config();
        spn = kerberosMediator.getSpn();
        clientPrincipal = kerberosMediator.getClientPrincipal();
        password = kerberosMediator.getPassword();
        keytabPath = kerberosMediator.getKeytabPath();
        krb5ConfigRegistryPath = kerberosMediator.getKrb5ConfigKey();
        loginConfigRegistryPath = kerberosMediator.getLoginConfigKey();
        keyTabRegistryPath = kerberosMediator.getRegistryKeyTabValue();

        if(loginContextName == null){
            loginContextName = "";
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

       <%
            if (krb5ConfigRegistryPath == null) {
       %>
            document.getElementById("krb5ConfigOptionAnon").checked = true;
            anonSelected();
       <%
            } else {
       %>
            document.getElementById("krb5ConfigOptionReference").checked = true;
            registrySelected();
       <%
            }
       %>

       <%
            if (loginConfigRegistryPath == null) {
       %>
           document.getElementById("loginConfigOptionAnon").checked = true;
           loginConfigOptionSelected();
       <%
           } else {
       %>
           document.getElementById("loginConfigOptionReference").checked = true;
           loginConfigRegistrySelected();
       <%
       }
       %>

       <%
           if (keyTabRegistryPath == null) {
       %>
           document.getElementById("keyTabPathOptionAnon").checked = true;
           keyTabOptionSelected();
       <%
            } else {
       %>
           document.getElementById("keyTabOptionReference").checked = true;
           keyTabRegistrySelected();
       <%
        }
       %>

        function anonSelected() {
            var krb5ConfigAnnonButton = document.getElementById("krb5ConfigOptionAnon");
            var krb5ConfigYes = document.getElementById("mediator.kerberos.krb5Config.txt.div");
            krb5ConfigYes.style.display = krb5ConfigAnnonButton.checked ? "block" : "none";
            document.getElementById("mediator.kerberos.txt.div").style.display='none';
            document.getElementById("mediator.kerberos.link.div").style.display='none';

        }

        function registrySelected() {
            var krb5ConfigRefButton = document.getElementById("krb5ConfigOptionReference");
            var krb5ConfigRegistry = document.getElementById("mediator.kerberos.txt.div");
            krb5ConfigRegistry.style.display = krb5ConfigRefButton.checked ? "block" : "none";
            var checkRegistry = document.getElementById("mediator.kerberos.link.div");
            checkRegistry.style.display = krb5ConfigRefButton.checked ? "block" : "none";
            document.getElementById("mediator.kerberos.krb5Config.txt.div").style.display='none';
        }

        function loginConfigOptionSelected() {
            var loginConfigOption = document.getElementById("loginConfigOptionAnon");
            var loginConfigAnonOption = document.getElementById("mediator.kerberos.loginConfig.txt.div");
            loginConfigAnonOption.style.display = loginConfigOption.checked ? "block" : "none";
            document.getElementById("mediator.kerberos.loginConfigKey.txt.div").style.display='none';
            document.getElementById("mediator.kerberos.loginConfigKey.link.div").style.display='none';

        }

        function loginConfigRegistrySelected() {
            var loginConfigReference = document.getElementById("loginConfigOptionReference");
            var loginText = document.getElementById("mediator.kerberos.loginConfigKey.txt.div");
            loginText.style.display = loginConfigReference.checked ? "block" : "none";
            var loginRegistry = document.getElementById("mediator.kerberos.loginConfigKey.link.div");
            loginRegistry.style.display = loginConfigReference.checked ? "block" : "none";
            document.getElementById("mediator.kerberos.loginConfig.txt.div").style.display='none';
        }

        function keyTabOptionSelected() {
            var keyTabOption= document.getElementById("keyTabPathOptionAnon");
            var keyTabText = document.getElementById("mediator.kerberos.keytab.txt.div");
            var keyTabSelection = document.getElementById("mediator.kerberos.keytabSelection.txt.div");
            keyTabText.style.display = keyTabOption.checked ? "block" : "none";
            keyTabSelection.style.display = keyTabOption.checked ? "block" : "none";
            document.getElementById("mediator.kerberos.keytabKey.txt.div").style.display='none';
            document.getElementById("mediator.kerberos.keytabKey.link.div").style.display='none';
        }

        function keyTabRegistrySelected() {
             var keyTabOptionRef = document.getElementById("keyTabOptionReference");
             var keyTabRegistryText = document.getElementById("mediator.kerberos.keytabKey.txt.div");
             keyTabRegistryText.style.display = keyTabOptionRef.checked ? "block" : "none";
             var registryConf = document.getElementById("mediator.kerberos.keytabKey.link.div");
             registryConf.style.display = keyTabOptionRef.checked ? "block" : "none";
             document.getElementById("mediator.kerberos.keytab.txt.div").style.display='none';
             document.getElementById("mediator.kerberos.keytabSelection.txt.div").style.display='none';
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
                    <tr>
                        <td class="leftCol-small" rowspan="3">
                            <fmt:message key="mediator.kerberos.krb5Config"/><span class="required">*</span>
                        </td>
                        <td>
                            <input type="radio" id="krb5ConfigOptionAnon" name="krb5Option" value="annon"
                            onclick="anonSelected()"/><fmt:message key="mediator.kerberos.anonymous"/>
                        </td>
                        <td>
                            <div id="mediator.kerberos.krb5Config.txt.div" style="display: none">
                                <input type="text" id="krb5Config" name="krb5Config" value="<%=krb5Config%>" />
                            </div>
                        </td>
                        <td></td>
                        <td></td>
                    </tr>

                    <tr>
                        <td>
                            <input type="radio" id="krb5ConfigOptionReference" name="krb5Option"
                            value="selectFromRegistry" onclick="registrySelected()"/>
                            <fmt:message key="mediator.kerberos.registry"/>
                        </td>
                        <td>
                            <div id="mediator.kerberos.txt.div" style="display: none">
                                <input type="text" id="krb5ConfigKey" name="krb5ConfigKey"
                                value="<%=(krb5ConfigRegistryPath != null)?krb5ConfigRegistryPath.getKeyValue():""%>"
                                readonly="true"/>
                            </div>
                        </td>
                        <td>
                            <div id="mediator.kerberos.link.div" style="display: none">
                                <a href="#registryBrowserLink"
                                class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('krb5ConfigKey','/_system/config')"><fmt:message
                                key="mediator.kerberos.conf.registry.browser"/></a>
                                <a href="#registryBrowserLink"
                                class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('krb5ConfigKey','/_system/governance')"><fmt:message
                                key="mediator.kerberos.gov.registry.browser"/></a>
                            </div>
                        </td>
                    </tr>
                    <tr></tr>
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.spn"/><span class="required">*</span>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="spn" name="spn" value="<%=spn%>" />
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.clientPrincipal"/><span class="required">*</span>
                        </td>
                        <%
                        NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
                        %>
                        <td>
                            <select name="clientPrincipalType" id="clientPrincipalType" onchange="onTypeSelectionChange('clientPrincipalType','nsCPEditorButtonTD')">
                                <%
                                if (clientPrincipal != null) {
                                    nameSpacesRegistrar.registerNameSpaces(clientPrincipal.getExpression(), "clientPrincipalType", session);
                                }
                                if (clientPrincipal != null && clientPrincipal.getExpression() != null) {
                                %>
                                    <option value="literal">Value</option>
                                    <option value="expression" selected="selected">Expression</option>
                                <%
                                } else if (clientPrincipal != null && clientPrincipal.getKeyValue() != null) {
                                %>
                                    <option value="literal" selected="selected">Value</option>
                                    <option value="expression">Expression</option>
                                <%
                                } else {
                                %>
                                    <option value="literal">Value</option>
                                    <option value="expression">Expression</option>
                                <%
                                }
                                %>
                            </select>
                        </td>
                        <td>
                            <%
                            if (clientPrincipal != null && clientPrincipal.getExpression() != null) {
                            %>
                                <input type="text" id="clientPrincipal" name="clientPrincipal" value="<%=clientPrincipal.getExpression().toString()%>" />
                            <%
                            } else if (clientPrincipal != null && clientPrincipal.getKeyValue() != null) {
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
                        <td id="nsCPEditorButtonTD" style="<%=(clientPrincipal == null || clientPrincipal.getExpression() == null)?"display:none;":""%>">
                            <a href="#nsEditorLink" class="nseditor-icon-link" style="padding-left:40px"
                                onclick="showNameSpaceEditor('clientPrincipalType')"><fmt:message key="mediator.kerberos.namespaces"/></a>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.password"/>
                        </td>
                        <td>
                            <select name="passwordType" id="passwordType" onchange="onTypeSelectionChange('passwordType','nsPasswordEditorButtonTD')">
                                <%
                                if (password != null) {
                                    nameSpacesRegistrar.registerNameSpaces(password.getExpression(), "passwordType", session);
                                }
                                if (password != null && password.getExpression() != null) {
                                %>
                                    <option value="literal">Value</option>
                                    <option value="expression" selected="selected">Expression</option>
                                <%
                                } else if (password != null && password.getKeyValue() != null) {
                                %>
                                    <option value="literal" selected="selected">Value</option>
                                    <option value="expression">Expression</option>
                                <%
                                } else {
                                %>
                                    <option value="literal">Value</option>
                                    <option value="expression">Expression</option>
                                <%
                                }
                                %>
                            </select>
                        </td>
                        <td>
                            <%
                            if (password != null && password.getExpression() != null) {
                            %>
                                <input type="text" id="password" name="password" value="<%=password.getExpression().toString()%>" />
                            <%
                            } else if (password != null && password.getKeyValue() != null) {
                            %>
                                <input type="text" id="password" name="password" value="<%=password.getKeyValue()%>" />
                            <%
                            } else {
                            %>
                                <input type="text" id="password" name="password" value="" />
                            <%
                            }
                            %>
                        </td>
                        <td id="nsPasswordEditorButtonTD" style="<%=(password == null || password.getExpression() == null)?"display:none;":""%>">
                            <a href="#nsEditorLink" class="nseditor-icon-link"
                            style="padding-left:40px"
                            onclick="showNameSpaceEditor('passwordType')"><fmt:message key="mediator.kerberos.namespaces"/></a>
                        </td>
                    </tr>
                     <tr>
                         <td class="leftCol-small" rowspan="3">
                            <fmt:message key="mediator.kerberos.keytabPath"/>
                         </td>
                         <td>
                            <input type="radio" id="keyTabPathOptionAnon" name="keyTabOption" value="annonKeytab"
                            onclick="keyTabOptionSelected()"/><fmt:message key="mediator.kerberos.anonymous"/>
                         </td>
                         <td>
                            <div id="mediator.kerberos.keytabSelection.txt.div" style="display: none">
                                 <select name="keytabPathType" id="keytabPathType" onchange="onTypeSelectionChange('keytabPathType','nsKeytabPathEditorButtonTD')">
                                    <%
                                     if (keytabPath != null) {
                                        nameSpacesRegistrar.registerNameSpaces(keytabPath.getExpression(), "keytabPathType", session);
                                     }
                                     if (keytabPath != null && keytabPath.getExpression() != null) {
                                    %>
                                         <option value="literal">Value</option>
                                         <option value="expression" selected="selected">Expression</option>
                                    <%
                                    } else if (keytabPath != null && keytabPath.getKeyValue() != null) {
                                    %>
                                         <option value="literal" selected="selected">Value</option>
                                         <option value="expression">Expression</option>
                                    <%
                                    } else {
                                    %>
                                         <option value="literal">Value</option>
                                         <option value="expression">Expression</option>
                                    <%
                                    }
                                    %>
                                 </select>
                            </div>
                         </td>
                         <td>
                             <div id="mediator.kerberos.keytab.txt.div" style="display: none">
                                 <%
                                 if (keytabPath != null && keytabPath.getExpression() != null) {
                                 %>
                                    <input type="text" id="keytabPath" name="keytabPath"
                                    value="<%=keytabPath.getExpression().toString()%>" />
                                 <%
                                 } else if (keytabPath != null && keytabPath.getKeyValue() != null) {
                                 %>
                                    <input type="text" id="keytabPath" name="keytabPath"
                                    value="<%=(keyTabRegistryPath == null)?keytabPath.getKeyValue():""%>"
                                 <%
                                 } else {
                                 %>
                                    <input type="text" id="keytabPath" name="keytabPath" value="" />
                                 <%
                                 }
                                 %>
                             </div>
                         </td>
                         <td id="nsKeytabPathEditorButtonTD" style="<%=(keytabPath == null || keytabPath.getExpression() == null)?"display:none;":""%>">
                             <a href="#nsEditorLink" class="nseditor-icon-link"
                             style="padding-left:40px"
                             onclick="showNameSpaceEditor('keytabPathType')"><fmt:message key="mediator.kerberos.namespaces"/></a>
                         </td>
                     </tr>
                     <tr>
                         <td>
                            <input type="radio" id="keyTabOptionReference" name="keyTabOption"
                            value="selectFromRegistryKeyTab" onclick="keyTabRegistrySelected()"/>
                            <fmt:message key="mediator.kerberos.registry"/>
                         </td>
                         <td>
                             <div id="mediator.kerberos.keytabKey.txt.div" style="display: none">
                                <input type="text" id="keyTabKey" name="keyTabKey"
                                value="<%=(keyTabRegistryPath != null)?keyTabRegistryPath.getKeyValue():""%>"
                                readonly="true"/>
                             </div>
                         </td>
                         <td>
                             <div id="mediator.kerberos.keytabKey.link.div" style="display: none">
                                <a href="#registryBrowserLink"
                                class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('keyTabKey','/_system/config')"><fmt:message
                                key="mediator.kerberos.conf.registry.browser"/></a>
                                <a href="#registryBrowserLink"
                                class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('keyTabKey','/_system/governance')"><fmt:message
                                key="mediator.kerberos.gov.registry.browser"/></a>
                             </div>
                         </td>
                     </tr>
                     <tr></tr>
                    <tr>
                        <td class="leftCol-small" rowspan="3">
                            <fmt:message key="mediator.kerberos.loginConfig"/>
                        </td>
                        <td>
                            <input type="radio" id="loginConfigOptionAnon" name="loginOption" value="annonLogin"
                            onclick="loginConfigOptionSelected()"/><fmt:message key="mediator.kerberos.anonymous"/>
                        </td>
                        <td>
                            <div id="mediator.kerberos.loginConfig.txt.div" style="display: none">
                                <input type="text" id="loginConfig" name="loginConfig" value="<%=loginConfig%>" />
                            </div>
                        </td>
                        <td></td>
                        <td></td>
                     </tr>

                    <tr>
                         <td>
                            <input type="radio" id="loginConfigOptionReference" name="loginOption"
                            value="selectFromRegistryLoginConfig" onclick="loginConfigRegistrySelected()"/>
                            <fmt:message key="mediator.kerberos.registry"/>
                         </td>
                         <td>
                            <div id="mediator.kerberos.loginConfigKey.txt.div" style="display: none">
                                <input type="text" id="loginConfigKey" name="loginConfigKey"
                                value="<%=(loginConfigRegistryPath != null)?loginConfigRegistryPath.getKeyValue():""%>"
                                readonly="true"/>
                            </div>
                         </td>
                         <td>
                            <div id="mediator.kerberos.loginConfigKey.link.div" style="display: none">
                                <a href="#registryBrowserLink"
                                class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('loginConfigKey','/_system/config')"><fmt:message
                                key="mediator.kerberos.conf.registry.browser"/></a>
                                <a href="#registryBrowserLink"
                                class="registry-picker-icon-link"
                                onclick="showRegistryBrowser('loginConfigKey','/_system/governance')"><fmt:message
                                key="mediator.kerberos.gov.registry.browser"/></a>
                            </div>
                         </td>
                    </tr>
                     <tr></tr>
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.loginContextName"/>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="loginContextName" name="loginContextName" value="<%=loginContextName%>" />
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>
</fmt:bundle>