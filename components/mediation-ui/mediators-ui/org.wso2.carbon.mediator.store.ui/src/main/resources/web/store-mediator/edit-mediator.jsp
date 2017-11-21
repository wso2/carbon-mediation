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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediator.store.MessageStoreMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.mediator.store.MessageStoreAdminServiceClient " %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof MessageStoreMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    MessageStoreMediator storeMediator = (MessageStoreMediator) mediator;
    String messageStoreName = "";
    SynapsePath messageStoreExp = null;
    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
    boolean isExpression = false;
    if (storeMediator.getMessageStoreExp() != null) {
        messageStoreExp = storeMediator.getMessageStoreExp();
        isExpression = true;
        nmspRegistrar.registerNameSpaces(storeMediator.getMessageStoreExp(), "mediator.store.xpath", session);
    } else if (storeMediator.getMessageStoreName() != null) {
        messageStoreName = storeMediator.getMessageStoreName();
    }

    String onStoreSequence = "";
    if(storeMediator.getSequence() != null) {
        onStoreSequence = storeMediator.getSequence();
    }

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    org.wso2.carbon.mediator.store.MessageStoreAdminServiceClient  client = new org.wso2.carbon.mediator.store.MessageStoreAdminServiceClient (cookie, url, configContext);
    String messageStores [] = client.getMessageStoreNames(); /*Calling Message Store Admin service and get the available stores */
%>

<fmt:bundle basename="org.wso2.carbon.mediator.store.ui.i18n.Resources">
 <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.store.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="logi18n"/>
<div>
<script type="text/javascript" src="../store-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.store.header"/></h2>
    </td>
</tr>
    <tr>
        <td>
            <table class="normal">
                <tr>
                    <td>
                        <h3>
                            <fmt:message key="mediator.store.messageStore"/>
                            <span class="required">*</span>
                        </h3>
                    </td>
                </tr>

                <tr>
                    <td class="leftCol-small">
                        <fmt:message key="mediator.store.specifyAs"/> :
                    </td>
                    <td>
                        <input type="radio" id="specify"
                               onclick="javascript:displayElement('value', true); javascript:displayElement('xpath', false);"
                               name="specifyAs" <%=!isExpression ? "checked=\"checked\" value=\"Value\"" : "value=\"Value\""%>/>
                        <fmt:message key="mediator.store.value"/>
                        <input type="radio"
                               onclick="javascript:displayElement('value', false); javascript:displayElement('xpath', true);"
                               name="specifyAs" <%=isExpression ? "checked=\"checked\" value=\"Expression\"" : "value=\"Expression\""%>/>
                        <fmt:message key="mediator.store.xpath"/>
                    </td>
                </tr>

                <tr id="value" <%=isExpression ? "style=\"display:none\";" : ""%>>
                    <td>
                        <fmt:message key="mediator.store.value"/>
                    </td>
                    <td>
                        <select name="MessageStore">
                            <% if(messageStores != null && messageStores.length >0 )
                            {
                                for (String msn : messageStores) {
                                if (msn.equals(messageStoreName)) {
                            %>
                                <option selected="selected"
                                        value="<%=messageStoreName%>"><%=messageStoreName%>
                                </option>
                            <%
                                } else {
                            %>
                                <option
                                        value="<%=msn%>" ><%=msn%>
                                </option>
                            <%
                                }
                            %>
                            <%}
                            }%>
                        </select>
                    </td>
                </tr>

                <tr id="xpath" <%=!isExpression ? "style=\"display:none\";" : ""%>>
                    <td>
                        <fmt:message key="mediator.store.xpath"/>
                    </td>
                    <td><input type="text" name="mediator.store.xpath"
                               style="width:300px"
                               id="mediator.store.xpath"
                               value="<%=messageStoreExp!=null ? messageStoreExp.getExpression() : ""%>"/>
                    </td>
                    <td><a id="mediator.store.source.xpath_nmsp_button" href="#"
                           onclick="showNameSpaceEditor('mediator.store.xpath')"
                           class="nseditor-icon-link"
                           style="padding-left:40px">
                        <fmt:message key="mediator.store.namespace"/></a>
                    </td>
                </tr>

                <tr>
                    <td>
                        <h3><fmt:message key="mediator.store.sequence"/></h3>
                    </td>
                </tr>

                 <tr>
                    <td>
                        <fmt:message key="mediator.store.onStoreSequence"/>
                    </td>
                    <td>
                        <input class="longInput" type="text" value="<%=onStoreSequence%>"
                               name="onStoreSequence" id="onStoreSequence" readonly="true"/>
                    </td>
                    <td>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('onStoreSequence','/_system/config')"><fmt:message
                                key="mediator.store.conf.registry.browser"/></a>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('onStoreSequence','/_system/governance')"><fmt:message
                                key="mediator.store.gov.registry.browser"/></a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>