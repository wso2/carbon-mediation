<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.apache.synapse.mediators.Value" %>
<%@page import="org.wso2.carbon.mediator.fastXSLT.ui.FastXSLTMediator" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.fastXSLT.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.fastXSLT.i18n.JSResources"
            request="<%=request%>" i18nObjectName="fastXSLTjsi18n"/>
    <%
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof FastXSLTMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }
        FastXSLTMediator fastXSLTMediator = (FastXSLTMediator) mediator;

        boolean isKeyDynamic = false;
        String keyVal = "";

        Value key = fastXSLTMediator.getXsltKey();

        if (key != null) {

            if (key.getKeyValue() != null) {
                isKeyDynamic = false;
                keyVal = key.getKeyValue();
            } else if (key.getExpression() != null) {
                isKeyDynamic = true;
                keyVal = key.getExpression().toString();
                NameSpacesRegistrar nameSpacesRegistrarKey = NameSpacesRegistrar.getInstance();
                nameSpacesRegistrarKey.registerNameSpaces(key.getExpression(), "mediator.fastXSLT.key.dynamic_val", session);
            }

        }
    %>
    <div>
        <script type="text/javascript" src="../fastXSLT-mediator/js/mediator-util.js"></script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="fastXSLT.mediator.header"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">

                        <tr>
                            <td>
                                <fmt:message key="fastXSLT.key.type"/> :
                            </td>
                            <td>
                                <input type="radio"
                                       onclick="javascript:displayElement('mediator.fastXSLT.key.dynamic', false); javascript:displayElement('mediator.fastXSLT.key.static', true); displayElement('mediator.fastXSLT.key.namespace.editor', false);"
                                       name="keygroup" <%=!isKeyDynamic ? "checked=\"checked\" value=\"StaticKey\"" : "value=\"StaticKey\""%>/>
                                <fmt:message key="fastXSLT.key.static"/>
                                <input type="radio" id="keyGroupDynamic"
                                       onclick="javascript:displayElement('mediator.fastXSLT.key.dynamic', true); displayElement('mediator.fastXSLT.key.namespace.editor', true); displayElement('mediator.fastXSLT.key.static', false);"
                                       name="keygroup" <%=isKeyDynamic ? "checked=\"checked\" value=\"DynamicKey\"" : "value=\"DynamicKey\""%>/>
                                <fmt:message key="fastXSLT.key.dynamic"/>
                            </td>
                            <td></td>
                        </tr>
                        <tr id="mediator.fastXSLT.key.static" <%=isKeyDynamic ? "style=\"display:none\";" : ""%>>
                            <td><fmt:message key="mediator.fastXSLT.key"/><span class="required">*</span></td>
                            <td>
                                <input class="longInput" type="text" id="mediator.fastXSLT.key.static_val"
                                       name="mediator.fastXSLT.key.static_val"
                                       value="<%=keyVal%>" readonly="true"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.fastXSLT.key.static_val','/_system/config')"><fmt:message
                                        key="conf.registry.keys"/></a>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.fastXSLT.key.static_val','/_system/governance')"><fmt:message
                                        key="gov.registry.keys"/></a>
                            </td>
                        </tr>
                        <tr id="mediator.fastXSLT.key.dynamic" <%=!isKeyDynamic ? "style=\"display:none\";" : ""%>>
                            <td><fmt:message key="mediator.fastXSLT.key"/><span class="required">*</span></td>
                            <td><input class="longInput" type="text" name="mediator.fastXSLT.key.dynamic_val"
                                       id="mediator.fastXSLT.key.dynamic_val"
                                       value="<%=keyVal%>"/></td>
                            <td><a id="mediator.fastXSLT.key.dynamic_nmsp_button" href="#nsEditorLink"
                                   class="nseditor-icon-link" style="padding-left:40px"
                                   onclick="showNameSpaceEditor('mediator.fastXSLT.key.dynamic_val')">

                                <fmt:message key="namespaces"/></a>
                            </td>
                        </tr>
                    </table>

                </td>
            </tr>
        </table>
        <a name="nsEditorLink"></a>

        <div id="nsEditor" style="display:none;"></div>

        <a name="registryBrowserLink"></a>

        <div id="registryBrowser" style="display:none;"></div>
    </div>
</fmt:bundle> 