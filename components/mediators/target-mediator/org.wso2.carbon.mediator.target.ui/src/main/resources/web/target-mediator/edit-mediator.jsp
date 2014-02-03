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

<%@ page import="org.apache.synapse.config.xml.endpoints.EndpointSerializer" %>
<%@ page import="org.apache.synapse.endpoints.AbstractEndpoint" %>
<%@ page import="org.apache.synapse.endpoints.Endpoint" %>
<%@ page import="org.apache.synapse.endpoints.IndirectEndpoint" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.target.TargetMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    // the varaibles holding which option of endpoints to be selected. If the endpoint is null then by default None
    String whichSeq = "none";
    String whichEP = "None";
    String anonEpXML = null;
    String  key = "";
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    if (!(mediator instanceof TargetMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    TargetMediator targetMediator = (TargetMediator) mediator;

    String soapAction = targetMediator.getSoapAction();
    if(soapAction == null){
        soapAction = "";
    }

    String toAddress = targetMediator.getToAddress();
    if(toAddress == null){
        toAddress = "";
    }

    String sequenceStr = targetMediator.getSequenceRef();
    if(sequenceStr == null){
        sequenceStr = "";
    }

    if(targetMediator.getList().isEmpty() && targetMediator.getSequenceRef() == null){
        whichSeq = "none";
    } else if(targetMediator.getSequenceRef() != null && !"anon".equals(targetMediator.getSequenceRef())) {
        whichSeq = "reg";
    } else if(targetMediator.getSequenceRef() != null    && "anon".equals(targetMediator.getSequenceRef())){
        whichSeq = "anon";
        //if the sequence is anonymous we have to clear the sequence string
        //in order to avoid it displaying in the registry textbox
        sequenceStr = "";
    }

    // The endpoint already associated with send mediator
    Endpoint endpoint = targetMediator.getEndpoint();

    if ((anonEpXML = (String)session.getAttribute("anonEpXML")) != null && !"".equals(anonEpXML)) {
        whichEP = "Anon";
        session.removeAttribute("anonEpXML");
    } else if (endpoint != null) {
        // if an endpoint has a name then it is an defined endpoint. So the option is Imp
        if (endpoint instanceof IndirectEndpoint) {
            if ((key = ((IndirectEndpoint)endpoint).getKey()) != null && !"".equals(key)) {
                whichEP = "Reg";
            } else {
                //TODO can an IndirectEndpoint be an anon endpoint ? do we need to remove the code below
                // no key, no name means it is an anonymous endpoint
                anonEpXML = EndpointSerializer.getElementFromEndpoint(endpoint).toString();
                if (anonEpXML != null && !"".equals(anonEpXML)) {
                     whichEP = "Anon";
                }
            }
        } else if (endpoint instanceof AbstractEndpoint) {
            // no key, no name means it is an anonymous endpoint
            anonEpXML = EndpointSerializer.getElementFromEndpoint(endpoint).toString();
            if (anonEpXML != null && !"".equals(anonEpXML)) {
                whichEP = "Anon";
            }
        }
    } else if (targetMediator.getEndpointRef() != null) {
        whichEP = "Reg";
        key = targetMediator.getEndpointRef();
    }
    if (anonEpXML != null && !"".equals(anonEpXML)) {
        session.setAttribute("endpointXML", anonEpXML);
    }

%>

<script type="text/javascript">
    var whichEP = '<%=whichEP%>';
    var whichSeq = '<%=whichSeq%>'
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

<fmt:bundle basename="org.wso2.carbon.mediator.target.ui.i18n.Resources">
<carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.target.ui.i18n.JSResources"
		request="<%=request%>"
        i18nObjectName="targeti18n"/>
    <div>
        <script type="text/javascript" src="../target-mediator/js/mediator-util.js"></script>

        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2>Target Configuration</h2>
                </td>
            </tr>

            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td>
                                SOAP Action
                            </td>
                            <td>
                                <input type="text" name="mediator.target.soapaction" id="mediator.target.soapaction"
                                       value="<%=soapAction%>"/>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                To Address
                            </td>
                            <td>
                                <input type="text" name="mediator.target.toaddress" id="mediator.target.toaddress"
                                       value="<%=toAddress%>"/>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <h3 class="mediator">Sequence</h3>
                                <input type="hidden" name="mediator.target.seq.type"
                                       id="mediator.target.seq.type" value="none"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="mediator.target.seq.radio.none" name="mediator.target.seq.radio"  type="radio" value="none"
                                      onclick="hideSeqRegistryOption(); seqNoneClicked();"/>None
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="mediator.target.seq.radio.anon" name="mediator.target.seq.radio" type="radio" onclick="hideSeqRegistryOption()"/>
                                Anonymous
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="mediator.target.seq.radio.reg" name="mediator.target.seq.radio" type="radio" onclick="showSeqRegistryOption()"/>
                                Pick From Registry

                            </td>
                            <td>
                                <input type="text" name="mediator.target.seq.reg"
                                       id="mediator.target.seq.reg" value="<%=sequenceStr%>"
                                       style="width:300px;display:none;"
                                       readonly="disabled" />
                            </td>
                            <td>
                                <a href="#registryBrowserLink" id="mediator.target.seq.reg.link_1"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.target.seq.reg','/_system/config')">
                                    <fmt:message key="conf.registry.keys"/></a>
                                <a href="#registryBrowserLink" id="mediator.target.seq.reg.link_2"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.target.seq.reg','/_system/governance')"><fmt:message
                                        key="gov.registry.keys"/></a>
                            </td>
                        </tr>


                        <tr>
                            <td><h3 class="mediator">Endpoint</h3></td>
                        </tr>
                        <tr>
                            <td>
                                <input id="epOpNone" type="radio" name="epOp" value="none"
                                       onclick="hideEpOps();"/>None
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="epOpAnon" type="radio" name="epOp" value="anon"
                                       onclick="showEpAddtionalOptions('epAnonAddEdit');"/>Anonymous
                            </td>
                            <%

                            %>
                            <% if( anonEpXML != null && !"".equals(anonEpXML)) { %>
                                <td>
                                    <a href="#" class="add-icon-link" id="epAnonAdd" onclick="anonEpAdd();" style="display:none;">Add</a>
                                </td>
                                <td>
                                    <a href="#" class="edit-icon-link" id="epAnonEdit" onclick="anonEpEdit();">Edit</a>
                                </td>

                                <td>
                                    <input type="hidden" id="anonAddEdit" name="anonAddEdit" value="Edit"/>
                                </td>
                            <% } else { %>

                                <td>
                                    <a href="#" class="add-icon-link" id="epAnonAdd" onclick="anonEpAdd();">Add</a>
                                </td>
                                <td>
                                    <a href="#" class="edit-icon-link" id="epAnonEdit" onclick="anonEpEdit();"  style="display:none;">Edit</a>
                                </td>
                                <td>
                                    <input type="hidden" id="anonAddEdit" name="anonAddEdit" value="Add"/>
                                </td>
                            <% } %>
                                <td>
                                    <a href="#"  class="delete-icon-link" id="epAnonClear" onclick="anonEpClear();">Clear</a>
                                </td>
                        </tr>
                        <tr>
                            <td>
                                <input id="epOpReg" type="radio" name="epOp" value="registry"
                                       onclick="showEpAddtionalOptions('registryEp');"/>Pick From Registry
                            </td>
                            <td>
                                <input type="text" id="registryKey" name="registryKey"
                                       value="<%=key%>" readonly="readonly" style="width:300px;"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink" id="regEpLink_1"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/config')">
                                    <fmt:message key="conf.registry.keys"/></a>
                                <a href="#registryBrowserLink" id="regEpLink_2"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/governance')"><fmt:message
                                        key="gov.registry.keys"/></a>
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