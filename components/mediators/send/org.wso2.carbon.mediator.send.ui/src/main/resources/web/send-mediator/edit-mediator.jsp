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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ page import="org.apache.synapse.config.xml.endpoints.EndpointSerializer" %>
<%@ page import="org.apache.synapse.endpoints.AbstractEndpoint" %>
<%@ page import="org.apache.synapse.endpoints.Endpoint" %>
<%@ page import="org.apache.synapse.endpoints.IndirectEndpoint" %>
<%@ page import="org.wso2.carbon.mediator.send.SendMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.synapse.endpoints.ResolvingEndpoint" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SendMediator)) {
        CarbonUIMessage.sendCarbonUIMessage("Unable to edit the mediator", CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
    SendMediator sendMediator = (SendMediator) mediator;
    // the varaibles holding which option of endpoints to be selected. If the endpoint is null then by default None
    String whichEP = "None";
    String anonEpXML = null;
    String key = "";
    String xpathVal = "";
    Value receivingSeqValue;
    String receivingSeq = "";
    boolean isDefaultRecSeq = true;
    boolean isKeyDynamic = false;

    // The endpoint already associated with send mediator
    Endpoint endpoint = sendMediator.getEndpoint();

    if ((anonEpXML = (String) session.getAttribute("anonEpXML")) != null && !"".equals(anonEpXML)) {
        whichEP = "Anon";
        session.removeAttribute("anonEpXML");
    } else if (endpoint != null) {
        // if an endpoint has a name then it is a defined endpoint. So the option is Imp
        if (endpoint instanceof IndirectEndpoint) {
            if ((key = ((IndirectEndpoint) endpoint).getKey()) != null && !"".equals(key)) {
                whichEP = "Reg";
            } else {
                // no key, no name means it is an anonymous endpoint
                anonEpXML = EndpointSerializer.getElementFromEndpoint(endpoint).toString();
                if (anonEpXML != null && !"".equals(anonEpXML)) {
                    whichEP = "Anon";
                }
            }
        } else if (endpoint instanceof ResolvingEndpoint) {
            SynapseXPath xp = ((ResolvingEndpoint) endpoint).getKeyExpression();
            if (xp != null) {
                whichEP = "XPath";
                NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
                nmspRegistrar.registerNameSpaces(xp, "mediator.send.xpath_val", session);
                xpathVal = xp.toString();
            }
        } else if (endpoint instanceof AbstractEndpoint) {
            // no key, no name means it is an anonymous endpoint
            anonEpXML = EndpointSerializer.getElementFromEndpoint(endpoint).toString();
            if (anonEpXML != null && !"".equals(anonEpXML)) {
                whichEP = "Anon";
            }
        }
    }

    if (anonEpXML != null && !"".equals(anonEpXML)) {
        session.setAttribute("endpointXML", anonEpXML);
    }

    if (sendMediator.getReceivingSeqValue() != null) {
        receivingSeqValue = sendMediator.getReceivingSeqValue();
        if (receivingSeqValue.getKeyValue() != null) {
            isKeyDynamic = false;
            receivingSeq = receivingSeqValue.getKeyValue();
            isDefaultRecSeq = false;
        } else if (receivingSeqValue.getExpression() != null) {
            isKeyDynamic = true;
            receivingSeq = receivingSeqValue.getExpression().toString();
            NameSpacesRegistrar nameSpacesRegistrarKey = NameSpacesRegistrar.getInstance();
            nameSpacesRegistrarKey.registerNameSpaces(receivingSeqValue.getExpression(), "mediator.send.key.dynamic_val", session);
            isDefaultRecSeq = false;
        }
    }

%>
<script type="text/javascript">
    var whichEP = '<%=whichEP%>';

    <%
      // Set the correct action for anonymous endpoint option
      if (anonEpXML != null && !"".equals(anonEpXML)) {
    %>
    var epAction = 'Edit';
    <%
        } else {
    %>
    var epAction = 'Add';
    <%
        }
    %>
</script>
<fmt:bundle basename="org.wso2.carbon.mediator.send.ui.i18n.Resources">
    <script type="text/javascript" src="../send-mediator/js/mediator-util.js"></script>

    <div>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="send.mediator"/></h2>
                    <label><fmt:message key="select.endpoint.type"/></label>
                    <table id="epOptionTable">
                        <tr>
                            <td>
                                <input id="epOpNone" type="radio" name="epOp" value="none"
                                       onclick="hideEpOps();"/><fmt:message key="select.ep.none"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="epOpAnon" type="radio" name="epOp" value="anon"
                                       onclick="showEpAddtionalOptions('epAnonAddEdit');"/><fmt:message
                                    key="anonymous"/>
                            </td>
                            <% if (anonEpXML != null && !"".equals(anonEpXML)) { %>
                            <td>
                                <a href="#" class="add-icon-link" id="epAnonAdd"
                                   onclick="anonEpAdd();"><fmt:message key="add"/></a>
                            </td>
                            <td>
                                <a href="#" class="edit-icon-link" id="epAnonEdit"
                                   onclick="anonEpEdit();" style="display:none;"><fmt:message
                                        key="edit"/></a>
                            </td>
                            <% } else { %>
                            <td>
                                <a href="#" class="add-icon-link" id="epAnonAdd"
                                   onclick="anonEpAdd();" style="display:none;"><fmt:message
                                        key="add"/></a>
                            </td>
                            <td>
                                <a href="#" class="edit-icon-link" id="epAnonEdit"
                                   onclick="anonEpEdit();"><fmt:message key="edit"/></a>
                            </td>
                            <% } %>
                            <td>
                                <a href="#" class="add-icon-link" id="epAnonClear"
                                   onclick="anonEpAdd();"><fmt:message key="new"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="epOpReg" type="radio" name="epOp" value="registry"
                                       onclick="showEpAddtionalOptions('registryEp');"/><fmt:message
                                    key="pick.from.registry"/>
                            </td>
                            <td>
                                <input type="text" id="registryKey" name="registryKey"
                                       value="<%=key%>" readonly="readonly"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink" id="confRegEpLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/config');">
                                    Configuration Registry</a>
                                <a href="#registryBrowserLink" id="govRegEpLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/governance');">
                                    Governance Registry</a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="epOpXPath" type="radio" name="epOp" value="xpath"
                                       onclick="showEpAddtionalOptions('xpath');"/><fmt:message
                                    key="xpath"/>
                            </td>
                            <td style="width: 305px;">
                                <input id="send_xpath" name="mediator.send.xpath_val" type="text"
                                       value="<%=xpathVal%>"
                                       style="width: 300px;"/>
                            </td>
                            <td>
                                <a href="#"
                                   id="mediator.send.xpath_nmsp"
                                   onclick="showNameSpaceEditor('mediator.send.xpath_val');"
                                   class="nseditor-icon-link"
                                   style="padding-left:40px">
                                    <fmt:message key="mediator.send.namespace"/></a>
                            </td>
                        </tr>
                    </table>
                    <table>

                        <tr>
                            <td>
                                <fmt:message key="receiving.seq.key.type"/> :
                            </td>
                            <td>
                                <input type="radio"
                                       onclick="javascript:displayElement('mediator.send.key.dynamic', false); javascript:displayElement('mediator.send.key.static', false); displayElement('mediator.send.key.namespace.editor', false);"
                                       name="keygroup" <%=isDefaultRecSeq ? "checked=\"checked\" value=\"DefaultKey\"" : "value=\"DefaultKey\""%>/>
                                <fmt:message key="send.rec.seq.default"/>
                                <input type="radio"
                                       onclick="javascript:displayElement('mediator.send.key.dynamic', false); javascript:displayElement('mediator.send.key.static', true); displayElement('mediator.send.key.namespace.editor', false);"
                                       name="keygroup" <%=!isKeyDynamic && !isDefaultRecSeq? "checked=\"checked\" value=\"StaticKey\"" : "value=\"StaticKey\""%>/>
                                <fmt:message key="send.key.static"/>
                                <input type="radio" id="keyGroupDynamic"
                                       onclick="javascript:displayElement('mediator.send.key.dynamic', true); displayElement('mediator.send.key.namespace.editor', true); displayElement('mediator.send.key.static', false);"
                                       name="keygroup" <%=isKeyDynamic && !isDefaultRecSeq ? "checked=\"checked\" value=\"DynamicKey\"" : "value=\"DynamicKey\""%> />
                                <fmt:message key="send.key.dynamic"/>
                            </td>
                            <td></td>
                        </tr>
                        <tr id="mediator.send.key.static" <%=isKeyDynamic || isDefaultRecSeq ? "style=\"display:none\";" : ""%>>
                            <td><fmt:message key="mediator.send.receiving.seq"/></td>
                            <td>
                                <input class="longInput" type="text"
                                       id="mediator.send.key.static_val"
                                       name="mediator.send.key.static_val"
                                       value="<%=receivingSeq%>" readonly="true"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.send.key.static_val','/_system/config')">
                                    <fmt:message key="conf.registry.keys"/></a>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.send.key.static_val','/_system/governance')"><fmt:message
                                        key="gov.registry.keys"/></a>
                            </td>
                        </tr>
                        <tr id="mediator.send.key.dynamic" <%=!isKeyDynamic || isDefaultRecSeq ? "style=\"display:none\";" : ""%>>
                            <td><fmt:message key="mediator.send.receiving.seq"/></td>
                            <td><input class="longInput" type="text"
                                       name="mediator.send.key.dynamic_val"
                                       id="mediator.send.key.dynamic_val"
                                       value="<%=receivingSeq%>"/></td>
                            <td><a id="mediator.send.key.dynamic_nmsp_button" href="#nsEditorLink"
                                   class="nseditor-icon-link" style="padding-left:40px"
                                   onclick="showNameSpaceEditor('mediator.send.key.dynamic_val')">
                                <fmt:message key="send.ns.editor.namespace"/></a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <table class="normal">
                    <tr>
                        <td>
                            <fmt:message key="build.message.before.sending"/>
                        </td>
                        <td>
                            <select name="build.message" id="build.message">
                                <option value="true" <%= sendMediator.isBuildMessage() ?
                                                         "selected=\"true\"" : ""%>>Yes
                                </option>
                                <option value="false" <%= !sendMediator.isBuildMessage() ?
                                                          "selected=\"true\"" : ""%>>No
                                </option>
                            </select>
                        </td>
                    </tr>
                </table>
            </tr>
        </table>
        <a name="registryBrowserLink"/>

        <div id="registryBrowser" style="display:none;"/>
    </div>
</fmt:bundle>
