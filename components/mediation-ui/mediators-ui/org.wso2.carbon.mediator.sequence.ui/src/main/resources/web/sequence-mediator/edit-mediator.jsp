<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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

<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SequenceMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SequenceMediator sequenceMediator = (SequenceMediator) mediator;

    boolean isKeyDynamic = false;
    String keyVal = "";

    Value key = sequenceMediator.getKey();

    if (key != null) {

        if (key.getKeyValue() != null) {
            isKeyDynamic = false;
            keyVal = key.getKeyValue();
        } else if (key.getExpression() != null) {
            isKeyDynamic = true;
            keyVal = key.getExpression().toString();
            NameSpacesRegistrar nameSpacesRegistrarKey = NameSpacesRegistrar.getInstance();
            nameSpacesRegistrarKey.registerNameSpaces(key.getExpression(), "mediator.sequence.key.dynamic_val", session);
        }

    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.sequence.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.sequence.ui.i18n.JSResources"
		request="<%=request%>"
        i18nObjectName="sequencei18n"/>
    <div>
        <script type="text/javascript" src="../sequence-mediator/js/mediator-util.js"></script>
        <script type="text/javascript" src="../resources/js/resource_util.js"></script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.sequence.header"/></h2>
                </td>
            </tr>


            <tr>
                <td>
                    <table class="normal">

                        <tr>
                            <td>
                                <fmt:message key="sequence.key.type"/> :
                            </td>
                            <td>
                                <input type="radio"
                                       onclick="javascript:displayElement('mediator.sequence.key.dynamic', false); javascript:displayElement('mediator.sequence.key.static', true); displayElement('mediator.sequence.key.namespace.editor', false);"
                                       name="keygroup" <%=!isKeyDynamic ? "checked=\"checked\" value=\"StaticKey\"" : "value=\"StaticKey\""%>/>
                                <fmt:message key="sequence.key.static"/>
                                <input type="radio" id="keyGroupDynamic"
                                       onclick="javascript:displayElement('mediator.sequence.key.dynamic', true); displayElement('mediator.sequence.key.namespace.editor', true); displayElement('mediator.sequence.key.static', false);"
                                       name="keygroup" <%=isKeyDynamic ? "checked=\"checked\" value=\"DynamicKey\"" : "value=\"DynamicKey\""%>
                                "/>
                                <fmt:message key="sequence.key.dynamic"/>
                            </td>
                            <td></td>
                        </tr>

                        <tr id="mediator.sequence.key.static" <%=isKeyDynamic ? "style=\"display:none\";" : ""%>>
                            <td>
                                <fmt:message key="mediator.sequence.referring"/><span class="required">*</span>
                            </td>
                            <td>
                                <input class="longInput" type="text" value="<%=keyVal%>"
                                       id="mediator.sequence.key.static_val" name="mediator.sequence.key.static_val"
                                       readonly="true"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.sequence.key.static_val','/_system/config')"><fmt:message
                                        key="mediator.sequence.conf.registry.browser"/>
                                </a>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.sequence.key.static_val','/_system/governance')"><fmt:message
                                        key="mediator.sequence.gov.registry.browser"/>
                                </a>
                            </td>
                        </tr>

                        <tr id="mediator.sequence.key.dynamic" <%=!isKeyDynamic ? "style=\"display:none\";" : ""%>>
                            <td><fmt:message key="mediator.sequence.referring"/><span class="required">*</span></td>
                            <td><input class="longInput" type="text" name="mediator.sequence.key.dynamic_val"
                                       id="mediator.sequence.key.dynamic_val"
                                       value="<%=keyVal%>"/></td>
                            <td><a id="mediator.sequence.key.dynamic_nmsp_button" href="#nsEditorLink"
                                   class="nseditor-icon-link" style="padding-left:40px"
                                   onclick="showNameSpaceEditor('mediator.xslt.key.dynamic_val')">

                                <fmt:message key="mediator.sequence.namespaces"/></a>
                            </td>
                        </tr>


                    </table>
                </td>
            </tr>
        </table>
        <div id="registryBrowser" style="display:none;"></div>
        <a name="registryBrowserLink"></a>
        <div id="nsEditor" style="display:none;"></div>
        <a name="nsEditorLink"></a>

    </div>
</fmt:bundle>