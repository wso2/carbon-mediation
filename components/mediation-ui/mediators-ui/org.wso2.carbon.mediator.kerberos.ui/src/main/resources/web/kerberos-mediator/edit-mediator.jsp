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
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.krb5Config"/><span class="required">*</span>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="krb5Config" name="krb5Config" value="<%=krb5Config%>" />
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
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
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.keytabPath"/>
                        </td>
                        <td>
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
                        </td>
                        <td>
                            <%
                            if (keytabPath != null && keytabPath.getExpression() != null) {
                            %>
                                <input type="text" id="keytabPath" name="keytabPath" value="<%=keytabPath.getExpression().toString()%>" />
                            <%
                            } else if (keytabPath != null && keytabPath.getKeyValue() != null) {
                            %>
                                <input type="text" id="keytabPath" name="keytabPath" value="<%=keytabPath.getKeyValue()%>" />
                            <%
                            } else {
                            %>
                                <input type="text" id="keytabPath" name="keytabPath" value="" />
                            <%
                            }
                            %>
                        </td>
                        <td id="nsKeytabPathEditorButtonTD" style="<%=(keytabPath == null || keytabPath.getExpression() == null)?"display:none;":""%>">
                            <a href="#nsEditorLink" class="nseditor-icon-link"
                            style="padding-left:40px"
                            onclick="showNameSpaceEditor('keytabPathType')"><fmt:message key="mediator.kerberos.namespaces"/></a>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.kerberos.loginConfig"/>
                        </td>
                        <td class="text-box-big">
                            <input type="text" id="loginConfig" name="loginConfig" value="<%=loginConfig%>" />
                        </td>
                        <td></td>
                        <td></td>
                    </tr>
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