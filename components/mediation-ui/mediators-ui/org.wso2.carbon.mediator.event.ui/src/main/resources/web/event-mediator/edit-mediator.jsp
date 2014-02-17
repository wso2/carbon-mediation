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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.event.ui.EventingSourceAdminClient" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.event.ui.EventMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediator.event.stub.service.xsd.dto.EventSourceDTO" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    boolean isKeyDynamic = false;

    if (!(mediator instanceof EventMediator)) {
        throw new RuntimeException("EventMediator instance required");
    }

    EventMediator eventMediator = (EventMediator) mediator;
    String topicValue = "";
    String expressionValue = "";

    if (eventMediator.getTopic() != null) {
        if (eventMediator.getTopic().getKeyValue() != null) {
            isKeyDynamic = false;
            topicValue = eventMediator.getTopic().getKeyValue();
        } else if (eventMediator.getTopic().getExpression() != null){
            topicValue = eventMediator.getTopic().getExpression().toString();
            isKeyDynamic = true;
        }
    }

    if (eventMediator.getExpression() != null) {
        expressionValue = eventMediator.getExpression().toString();
    }

    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    nameSpacesRegistrar.registerNameSpaces(eventMediator.getExpression(), "expression", session);
    if (eventMediator.getTopic() != null && eventMediator.getTopic().getExpression() != null) {
        nameSpacesRegistrar.registerNameSpaces(eventMediator.getTopic().getExpression(), "topicVal", session);
    }

%>

<fmt:bundle basename="org.wso2.carbon.mediator.event.ui.i18n.Resources">
    <carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.event.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="eventi18n"/>    
<div>
    <script type="text/javascript" src="../event-mediator/js/mediator-util.js"></script>

    <table class="normal" width="100%">
        <tr>
            <td>
                <h2><fmt:message key="mediator.event.header"/></h2>
            </td>
        </tr>
        <tr>
            <table>
                <tbody>
                <tr>
                    <td>
                        <fmt:message key="topic.type"/>
                    </td>
                    <td>
                        <input type="radio"
                               onclick="javascript:displayElement('topicValNamespace', false);"
                               name="keygroup" <%=!isKeyDynamic ? "checked=\"checked\" value=\"StaticKey\"" : "value=\"StaticKey\""%>/>
                        <fmt:message key="static"/>
                        <input type="radio" id="keyGroupDynamic"
                               onclick="javascript:displayElement('topicValNamespace', true);"
                               name="keygroup" <%=isKeyDynamic ? "checked=\"checked\" value=\"DynamicKey\"" : "value=\"DynamicKey\""%>/>
                        <fmt:message key="dynamic"/>
                    </td>
                    <td></td>
                </tr>
                <tr>
                    <td>Topic<span class="required">*</span></td>
                    <td><input class="longInput" type="text" id="topicVal"
                               name="topicVal"
                               value="<%=topicValue%>"/></td>
                    <td>
                        <% if (!isKeyDynamic) { %>
                        <a href="#nsEditorLink"
                           class="nseditor-icon-link" style="padding-left:40px;display:none" id="topicValNamespace"
                           onclick="showNameSpaceEditor('topicVal')"><fmt:message
                                key="namespace"/></a>
                        <% } else { %>
                        <a href="#nsEditorLink"
                           class="nseditor-icon-link" style="padding-left:40px"
                           id="topicValNamespace"%>
                        onclick="showNameSpaceEditor('topicVal')"><fmt:message key="namespace"/></a>
                        <% } %>
                    </td>
                </tr>
                <tr>
                    <td>Expression</td>
                    <td><input class="longInput" type="text" id="expression"
                               name="expression"
                               value="<%=expressionValue%>"/></td>
                    <td><a href="#nsEditorLink"
                           class="nseditor-icon-link" style="padding-left:40px"
                            <%=!isKeyDynamic ? "style=\"display:none\" id=\"expressionNMSP\"" : "id=\"expressionNMSP\""%>
                           onclick="showNameSpaceEditor('expression')"><fmt:message key="namespace"/></a>
                    </td>
                </tr>
                </tbody>
            </table>
        </tr>
    </table>
</div>
</fmt:bundle>