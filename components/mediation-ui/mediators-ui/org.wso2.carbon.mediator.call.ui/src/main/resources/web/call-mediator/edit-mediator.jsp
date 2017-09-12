<!--
~ Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.mediator.call.CallMediator" %>
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
    if (!(mediator instanceof CallMediator)) {
        CarbonUIMessage.sendCarbonUIMessage("Unable to edit the mediator", CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
    CallMediator callMediator = (CallMediator) mediator;
    // the varaibles holding which option of endpoints to be selected. If the endpoint is null then by default None
    String whichEP = "None";
    String anonEpXML = null;
    String key = "";
    String xpathVal = "";
    String repo = "", axis2XML = "";
    // The endpoint already associated with call mediator
    Endpoint endpoint = callMediator.getEndpoint();

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
                nmspRegistrar.registerNameSpaces(xp, "mediator.call.xpath_val", session);
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

    if (callMediator.getClientRepository() != null) {
        repo = callMediator.getClientRepository();
    }
    if (callMediator.getAxis2xml() != null) {
        axis2XML = callMediator.getAxis2xml();
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
<script type="text/javascript">
    function showBlockingOptions(elm) {
        var selectedElmValue = elm.options[elm.selectedIndex].value;
        if (selectedElmValue == "true") {
            displayElement('repo_row', true);
            displayElement('axis2xml_row', true);
            displayElement('init_row', true);
        }
        if (selectedElmValue == "false") {
            displayElement('repo_row', false);
            displayElement('axis2xml_row', false);
            displayElement('init_row', false);
        }
    }

</script>

<fmt:bundle basename="org.wso2.carbon.mediator.call.ui.i18n.Resources">
    <script type="text/javascript" src="../call-mediator/js/mediator-util.js"></script>

    <div>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="call.mediator"/></h2>
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
                                    <fmt:message key="conf.registry.keys"/></a>
                                <a href="#registryBrowserLink" id="govRegEpLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/governance');">
                                    <fmt:message key="gov.registry.keys"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="epOpXPath" type="radio" name="epOp" value="xpath"
                                       onclick="showEpAddtionalOptions('xpath');"/><fmt:message
                                    key="xpath"/>
                            </td>
                            <td style="width: 305px;">
                                <input id="call_xpath" name="mediator.call.xpath_val" type="text"
                                       value="<%=xpathVal%>"
                                       style="width: 300px;"/>
                            </td>
                            <td>
                                <a href="#"
                                   id="mediator.call.xpath_nmsp"
                                   onclick="showNameSpaceEditor('mediator.call.xpath_val');"
                                   class="nseditor-icon-link"
                                   style="padding-left:40px">
                                    <fmt:message key="mediator.call.namespace"/></a>
                            </td>
                        </tr>
                    </table>
                    <br>
                     <label><fmt:message key="enable.blocking.calls"/></label>
                     <table id="blockingOptionTable">
                         <tr>
                             <td><fmt:message key="blocking"/></td>
                             <td>
                                <select id="mediator.call.blocking" name="mediator.call.blocking"
                                onchange="showBlockingOptions(this)">
                                    <option value="false"
                                            <%=!callMediator.getBlocking() ?
                                               "selected=\"selected\"" :
                                               ""%>><fmt:message key="blocking.category.false"/>
                                    </option>
                                    <option value="true"
                                            <%=callMediator.getBlocking() ?
                                               "selected=\"selected\"" :
                                               ""%>><fmt:message key="blocking.category.true"/>
                                    </option>
                                </select>
                             </td>
                         </tr>
                         <tr id="repo_row" <%= !callMediator.getBlocking() ? "style=\"display:none;\"" : ""%>>
                              <td>
                                   <fmt:message key="mediator.call.repo"/>
                              </td>
                              <td>
                                   <input type="text" size="40" id="mediator.call.repo" name="mediator.call.repo"
                                    value="<%=repo%>" style="width:300px"/>
                              </td>
                         </tr>
                         <tr id="axis2xml_row" <%= !callMediator.getBlocking() ? "style=\"display:none;\"" : ""%>>
                              <td>
                                   <fmt:message key="mediator.call.axis2XML"/>
                              </td>
                              <td>
                                   <input type="text" size="40" id="mediator.call.axis2XML"
                                   name="mediator.call.axis2XML" value="<%=axis2XML%>" style="width:300px"/>
                              </td>
                         </tr>
                         <tr id="init_row" <%= !callMediator.getBlocking() ? "style=\"display:none;\"" : ""%>>
                              <td><fmt:message key="initAxis2ClientOptions"/></td>
                              <td>
                                  <select id="mediator.call.initAxis2ClientOptions"
                                  name="mediator.call.initAxis2ClientOptions">
                                      <option value="true"
                                          <%=callMediator.getInitAxis2ClientOptions() ?
                                              "selected=\"selected\"" :
                                               ""%>><fmt:message key="initAxis2ClientOptions.category.true"/>
                                      </option>
                                      <option value="false"
                                          <%=!callMediator.getInitAxis2ClientOptions() ?
                                              "selected=\"selected\"" :
                                              ""%>><fmt:message key="initAxis2ClientOptions.category.false"/>
                                      </option>
                                  </select>
                              </td>
                         </tr>
                     </table>

                </td>
            </tr>
        </table>
        <a name="registryBrowserLink"/>

        <div id="registryBrowser" style="display:none;"/>
    </div>
</fmt:bundle>
