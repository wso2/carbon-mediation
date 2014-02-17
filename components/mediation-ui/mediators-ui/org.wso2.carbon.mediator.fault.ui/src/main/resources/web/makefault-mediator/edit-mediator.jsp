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
<%@ page import="org.wso2.carbon.mediator.fault.FaultMediator" %>
<%@ page import="org.wso2.carbon.mediator.fault.ui.util.FaultUtil" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof FaultMediator)) {
        CarbonUIMessage.sendCarbonUIMessage("Unable to edit the mediator", CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }

    FaultMediator faultMediator = (FaultMediator) mediator;

    // Getting the fault code option to set it selected
    String faultcodeOption;
    if (faultMediator.getFaultCodeValue() != null && faultMediator.getFaultCodeValue().getLocalPart() != null) {
        faultcodeOption = faultMediator.getFaultCodeValue().getLocalPart();
    } else {
        faultcodeOption = "undefined";
    }

%>
<fmt:bundle basename="org.wso2.carbon.mediator.fault.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.fault.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="faulti18n"/>

<script type="text/javascript" src="../makefault-mediator/js/mediator-util.js"></script>
<div>

<table class="normal" width="100%">
<tr>
<td>
<h2><fmt:message key="mediator.fault.header"/></h2>
<table class="normal">
<tr>
    <td>
        <fmt:message key="mediator.fault.version"/>
    </td>
    <td>
        <%
            if (faultMediator.getSoapVersion() == 1 || faultMediator.getSoapVersion() == 0) {
        %>
        <input type="radio" id="soap_version" name="soap_version" checked="true"
               value="1" onclick="loadFaultCode('1');">SOAP 1.1
        <input type="radio" id="soap_version" name="soap_version" value="2"
               onclick="loadFaultCode('2');">SOAP 1.2
        <input type="radio" id="soap_version" name="soap_version" value="3"
               onclick="loadFaultCode('3');">POX     
        <%
        } else if (faultMediator.getSoapVersion() == 2) {

        %>
        <input type="radio" id="soap_version" name="soap_version" value="1"
               onclick="loadFaultCode('1');">SOAP 1.1
        <input type="radio" id="soap_version" name="soap_version" checked="true"
               value="2" onclick="loadFaultCode('2');">SOAP 1.2
        <input type="radio" id="soap_version" name="soap_version" value="3"
               onclick="loadFaultCode('3');">POX       
        <%
        } else if (faultMediator.getSoapVersion() == 3){
        %>
        <input type="radio" id="soap_version" name="soap_version" value="1"
               onclick="loadFaultCode('1');">SOAP 1.1
        <input type="radio" id="soap_version" name="soap_version" value="2" 
               onclick="loadFaultCode('2');">SOAP 1.2
        <input type="radio" id="soap_version" name="soap_version" value="3" checked="true"
               onclick="loadFaultCode('3');">POX
        <%}%>
    </td>
</tr>
<tr id="fault_code_11" <% if (faultMediator.getSoapVersion() == 2 || faultMediator.getSoapVersion() == 3) { %> style="display:none; "<% } %>>
    <td>
        <fmt:message key="mediator.fault.faultcode"/><span class="required">*</span>
    </td>
    <td>
        <select name="fault_code1" class="esb-edit small_textbox">
            <option value="VersionMismatch" <% if ("VersionMismatch".equals(faultcodeOption)) { %>
                    selected="selected" <%}%> >versionMismatch
            </option>
            <option value="MustUnderstand" <% if ("MustUnderstand".equals(faultcodeOption)) { %>
                    selected="selected" <%}%> >mustUnderstand
            </option>
            <option value="Client" <% if ("Client".equals(faultcodeOption)) { %> selected="selected" <%}%> >Client
            </option>
            <option value="Server" <% if ("Server".equals(faultcodeOption)) { %> selected="selected" <%}%> >Server
            </option>
        </select>
    </td>
</tr>
<tr id="fault_code_12" <% if (faultMediator.getSoapVersion() != 2) { %> style="display:none;" <% } %> >
    <td>
        <fmt:message key="mediator.fault.code"/><span class="required">*</span>
    </td>
    <td>
        <select name="fault_code2" class="esb-edit small_textbox">
            <option value="VersionMismatch" <% if ("VersionMismatch".equals(faultcodeOption)) { %>
                    selected="selected" <%}%> >versionMismatch
            </option>
            <option value="MustUnderstand" <% if ("MustUnderstand".equals(faultcodeOption)) { %>
                    selected="selected" <%}%> >mustUnderstand
            </option>
            <option value="DataEncodingUnknown" <% if ("DataEncodingUnknown".equals(faultcodeOption)) { %>
                    selected="selected" <%}%> >dataEncodingUnknown
            </option>
            <option value="Sender" <% if ("Sender".equals(faultcodeOption)) { %> selected="selected" <%}%> >Sender
            </option>
            <option value="Receiver" <% if ("Receiver".equals(faultcodeOption)) { %> selected="selected" <%}%> >Receiver
            </option>
        </select>
    </td>
</tr>
<tr>
    <td id="fault_string_row" <% if (faultMediator.getSoapVersion() !=1 && faultMediator.getSoapVersion() !=3) { %> style="display:none;" <% } %> >
        <fmt:message key="mediator.fault.string"/><span class="required">*</span>
    </td>
    <td id="reason" <% if (faultMediator.getSoapVersion() != 2) { %> style="display:none;" <% } %> >
        <fmt:message key="mediator.fault.reason"/><span class="required">*</span>
    </td>
    <td>
        <%
            if (faultMediator.getFaultReasonValue() != null) {
        %>
        <input type="radio" name="fault_string" value="value" checked="true"
               onclick="changeButton('value');">value
        <input type="radio" name="fault_string" value="expression"
               onclick="changeButton('expression');">expression
        <%
        } else if (faultMediator.getFaultReasonExpr() != null) {
        %>
        <input type="radio" name="fault_string" value="value"
               onclick="changeButton('value');">value
        <input type="radio" name="fault_string" value="expression" checked="true"
               onclick="changeButton('expression');">expression

        <%
        } else {
        %>
        <input type="radio" name="fault_string" value="value" checked="true"
               onclick="changeButton('value');">value
        <input type="radio" name="fault_string" value="expression"
               onclick="changeButton('expression');">expression
        <%
            }
        %>
        <br/>
        <%
            boolean isNSEditorRequired = false;
            NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
            nameSpacesRegistrar.registerNameSpaces(faultMediator.getFaultReasonExpr(), "name_space", session);
            if (faultMediator.getFaultReasonExpr() != null &&
                    faultMediator.getFaultReasonExpr().getRootExpr() != null &&
                    faultMediator.getFaultReasonExpr().getRootExpr().getText() != null) {
                isNSEditorRequired = true;
                //TODO: why do we need to set the expression as followings, the comment line
                //FaultUtil.repalceDoubleQuotation(faultMediator.getFaultReasonExpr().getRootExpr().getText());
                // setting the expression as below would be enough
        %>
        <input type="text" id="name_space" class="longInput" name="name_space"
               value="<%=FaultUtil.repalceDoubleQuotation(faultMediator.getFaultReasonExpr().toString())%>"
               style="float:left;"/>
        <%
        } else if (faultMediator.getFaultReasonValue() != null) {
        %>
        <input type="text" id="name_space" name="name_space" class="longInput"
               value="<%=faultMediator.getFaultReasonValue()%>" style="float:left;"/>
        <%
        } else {
        %>
        <input type="text" id="name_space" class="longInput" name="name_space" style="float:left;"/>
        <%
            }
        %>

    </td>
    <td valign="top" align="left">
        <br/>

        <a onclick="showNameSpaceEditor('name_space');"
           style="padding-left: 40px;<% if(!isNSEditorRequired) {%>display:none;<%} %>"
           class="nseditor-icon-link" href="#nsEditorLink" id="nmsp_button"><fmt:message
                key="mediator.fault.nameSpaces"/></a>
        <a name="string_nsEditorLink"/>

    </td>
</tr>
<tr id ="fault_actor_table_row" <% if (faultMediator.getSoapVersion() == 3) { %> style="display:none;" <% } %> >
    <td id="fault_actor_row"<% if (faultMediator.getSoapVersion() != 1) { %> style="display:none;" <% } %> > <fmt:message key="mediator.fault.actor"/></td>
    <td id="role_row" <% if (faultMediator.getSoapVersion() != 2) { %> style="display:none;" <% } %> ><fmt:message    key="mediator.fault.role"/></td>
    <td>
        <%
            if (faultMediator.getFaultRole() == null) {
        %>

        <input type="text" id="fault_actor" class="longInput" name="fault_actor"/>
        <%
        } else {

        %>
        <input type="text" id="fault_actor" class="longInput" name="fault_actor"
               value="<%=faultMediator.getFaultRole()%>"/>
        <%
            }
        %>
    </td>
</tr>
<tr id="node_row" <% if (faultMediator.getSoapVersion() != 2) { %> style="display:none;" <% } %> >
    <td><fmt:message key="mediator.fault.node"/></td>
    <td>
        <%
            if (faultMediator.getFaultNode() == null) {
        %>
        <input id="node" type="text" class="longInput" name="node"/>
        <%
        } else {
        %>
        <input id="node" type="text" class="longInput" name="node"
               value="<%=faultMediator.getFaultNode().getPath()%>"/>
        <%
            }
        %>
    </td>
</tr>
<tr>
    <td>
        <fmt:message key="mediator.fault.detail"/>
    </td>
    <td>

        <%
            if (faultMediator.getFaultDetail() != null) {
        %>
        <input type="radio" name="fault_detail" value="value" checked="true"
               onclick="changeButton('detail_value');">value
        <input type="radio" name="fault_detail" value="expression"
               onclick="changeButton('detail_expression');">expression
        <%
        } else if (faultMediator.getFaultDetailExpr() != null) {
        %>
        <input type="radio" name="fault_detail" value="value"
               onclick="changeButton('detail_value');">value
        <input type="radio" name="fault_detail" value="expression" checked="true"
               onclick="changeButton('detail_expression');">expression

        <%
        } else {
        %>

        <input type="radio" name="fault_detail" value="value" checked="true"
               onclick="changeButton('detail_value');">value
        <input type="radio" name="fault_detail" value="expression"
               onclick="changeButton('detail_expression');">expression
        <%
            }
        %>
        <br/>

        <%
            boolean isNSEditorRequiredForDetail = false;

            NameSpacesRegistrar nameSpacesRegistrarForDetail = NameSpacesRegistrar.getInstance();
            nameSpacesRegistrarForDetail.registerNameSpaces(faultMediator.getFaultDetailExpr(), "detail", session);
            if (faultMediator.getFaultDetailExpr() != null &&
                    faultMediator.getFaultDetailExpr().getRootExpr() != null &&
                    faultMediator.getFaultDetailExpr().getRootExpr().getText() != null) {
                isNSEditorRequiredForDetail = true;
                String detailXPathString = FaultUtil.repalceDoubleQuotation(faultMediator.getFaultDetailExpr().getRootExpr().getText());
                //TODO: why do we need to set the expression as followings, the comment line
                //FaultUtil.repalceDoubleQuotation(faultMediator.getFaultReasonExpr().getRootExpr().getText());
                // setting the expression as below would be enough

        %>
        <textarea cols="30" rows="8" class="longInput" type="text" id="detail"
                  name="detail"><%=detailXPathString%>
        </textarea>

        <%
        } else if (faultMediator.getFaultDetail() == null && faultMediator.getFaultDetailElements().size() == 0) {
        %>
        <textarea cols="30" rows="8" class="longInput" type="text" id="detail" name="detail"/>
        <%
        } else if (faultMediator.getFaultDetail() != null) {
        %>
        <textarea cols="30" rows="8" class="longInput" type="text" id="detail"
                  name="detail"><%=faultMediator.getFaultDetail()%>
        </textarea>
        <%
        } else if (faultMediator.getFaultDetailElements().size() > 0) {
            String detail = "";
            for (OMElement e : faultMediator.getFaultDetailElements()) {
                detail += e.toString();
            }
        %>                                                                     '

        <textarea cols="30" rows="8" class="longInput" type="text" id="detail"
                  name="detail"><%=detail%>
        </textarea>

        <%
            }
        %>

    </td>
    <td valign="top" align="left">
        <br/>

        <a onclick="showNameSpaceEditor('detail');"
           style="padding-left: 40px;<% if(!isNSEditorRequiredForDetail) {%>display:none;<%} %>"
           class="nseditor-icon-link" href="#nsEditorLink" id="detail_nmsp_button"><fmt:message
                key="mediator.fault.nameSpaces"/></a>
        <a name="detail_nsEditorLink"/>


    </td>
</tr>
</table>
</td>
</tr>
</table>
</div>
</fmt:bundle>
