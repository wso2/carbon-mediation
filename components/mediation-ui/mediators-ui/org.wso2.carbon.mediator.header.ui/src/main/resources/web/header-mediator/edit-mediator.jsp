<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
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

<%@ page import="org.wso2.carbon.mediator.header.HeaderMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="javax.xml.XMLConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    boolean isExpression = false;
    int type = 1;
    String prefix = "", uri = "";
    String val = "";
    String xml = "";
    String scope = "";
    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
    if (!(mediator instanceof HeaderMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    HeaderMediator headerMediator = (HeaderMediator) mediator;
    if (headerMediator.getQName() != null) {
        QName qname = headerMediator.getQName();
        prefix = qname.getPrefix();
        uri = qname.getNamespaceURI();
        if (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX )) {
            nmspRegistrar.registerNameSpaces(qname, "mediator.header.name", session);
        }
    }
    if (headerMediator.getValue() != null) {
        isExpression = false;
        type = 1;
        val = headerMediator.getValue();
    } else if (headerMediator.getExpression() != null) {
        isExpression = true;
        type = 2;
        val = headerMediator.getExpression().toString();
        nmspRegistrar.registerNameSpaces(headerMediator.getExpression(), "mediator.header.val_ex", session);
    } else if (headerMediator.getXml() != null) {
        type = 3;
        xml = headerMediator.getXml().toString();
    } else if (headerMediator.getScope() != null) {
    	scope = headerMediator.getScope();
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.header.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.header.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="headerMediatorJsi18n"/>
<div>
    <script type="text/javascript" src="../header-mediator/js/mediator-util.js"></script>
    <table class="normal" width="100%">
    <tr>
        <td>
            <h2><fmt:message key="mediator.header.header"/></h2>
        </td>
    </tr>
    <tr>
    <td>
    <table class="styledLeft">
        <tbody>
            <tr id="mediator.header.name.row" style="<%=(type == 1 || type == 2) ? "": "display:none;"%> ">
                <td width="20%">
                    <fmt:message key="mediator.header.name"/>
                    <font style="color: red; font-size: 8pt;"> *</font></td>
                <td width="5%">
                    <input type="text" size="40" id="mediator.header.name" name="mediator.header.name"
                           value="<%=headerMediator.getQName() != null ? headerMediator.getQName().getLocalPart() : ""%>"/>
                </td>
                <td width="75%" align="left">
                    <a id="mediator.header.name.namespace_button" href="#"
                           onclick="javascript:showSingleNameSpaceEditor('mediator.header.name');" class="nseditor-icon-link"
                           style="padding-left:40px">
                        <fmt:message key="mediator.header.namespace"/></a>
                </td>
            </tr>
            <tr>
                <td width="20%"><fmt:message key="mediator.header.action"/> :</td>
                <td width="80%" colspan="2">
                    <input type="radio" id="set" name="mediator.header.action" value="set"
                                        onclick="javascript: displayElement('mediator.header.value_row', true); displayElement('mediator.header.inlinexmltext_row', true);"
                        <%=headerMediator.getAction() == HeaderMediator.ACTION_SET ? "checked=\"checked\"": "" %>/>
                    <fmt:message key="mediator.header.set"/>
                    <input type="radio" id="remove" name="mediator.header.action" value="remove"
                           onclick="javascript: displayElement('mediator.header.value_row', false);
                           displayElement('mediator.header.name.expression', false);
                           displayElement('mediator.header.inlinexmltext_row', false);
                           displayElement('mediator.header.name.row', true)"
                        <%=headerMediator.getAction() == HeaderMediator.ACTION_REMOVE ? "checked=\"checked\"": "" %>/>
                    <fmt:message key="mediator.header.remove"/>
                </td>
            </tr>
            <tr id="mediator.header.value_row" <%=headerMediator.getAction() == HeaderMediator.ACTION_REMOVE ? "style=\"display:none\"" : ""%>>
                <td width="20%"><input type="radio" name="mediator.header.actionType" id="value" value="value"
                           onclick="javascript: displayElement('mediator.header.val_ex', true); displayElement('mediator.header.expression.namespace_button', false);
                           displayElement('mediator.header.expression.nmsp', false);displayElement('mediator.header.inlinexmltext', false);
                           displayElement('mediator.header.name.row', true)"
                           <%=(type == 1) ? "checked=\"checked\"": "" %>/>
                    <fmt:message key="mediator.header.value"/>
                    <input type="radio" name="mediator.header.actionType" id="expression" value="expression"
                           onclick="javascript:  displayElement('mediator.header.val_ex', true); displayElement('mediator.header.expression.namespace_button', true);
                           displayElement('mediator.header.inlinexmltext', false);displayElement('mediator.header.name.row', true)"
                           <%=(type == 2) ? "checked=\"checked\"": "" %>/>
                    <fmt:message key="mediator.header.expression"/><font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td width="30%">
                    <input type="text" id="mediator.header.val_ex" size="40" name="mediator.header.val_ex" value="<%=val%>" style="<%=(type == 3) ? "display:none" : ""%>"/>
                </td>
                <td width="50%" align="left"><a id="mediator.header.expression.namespace_button" href="#"
                           onclick="javascript:showNameSpaceEditor('mediator.header.val_ex');" class="nseditor-icon-link"
                           style="padding-left:40px;<%=(type == 1 || type == 3) ? "display:none;" : ""%> ">
                        <fmt:message key="mediator.header.namespace"/></a>
                </td>
            </tr>
            <tr id="mediator.header.inlinexmltext_row">
                <td width="20%">
                    <input type="radio" name="mediator.header.actionType" id="inlineXML" value="inlineXML"
                           onclick="javascript: displayElement('mediator.header.expression.namespace_button', false);displayElement('mediator.header.expression.nmsp', false);
                           displayElement('mediator.header.inlinexmltext', true);displayElement('mediator.header.val_ex', false);
                           displayElement('mediator.header.name.row', false)"
                           <%=(type == 3) ? "checked=\"checked\"": "" %>/>
                    <fmt:message key="mediator.header.inlinexml"/><font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td width="80%" colspan="2">
                    <textarea rows="8" cols="50" id="mediator.header.inlinexmltext" name="mediator.header.inlinexmltext"  style="<%=(type == 3) ? "": "display:none;"%> " ><%=xml%></textarea>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.header.scope"/></td>
                <td>
                    <select id="mediator.header.scope" name="mediator.header.scope"
                            style="width:150px;">
                        <option value="default" <%=headerMediator.getScope() != null && headerMediator.getScope().equals("default") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="synapse"/>
                        </option>
                        <option value="transport" <%=headerMediator.getScope() != null && headerMediator.getScope().equals("transport") ? "selected=\"selected\"" : ""%>>
                            <fmt:message key="transport"/>
                        </option>
                    </select>
                </td>
            </tr>               
        </tbody>
    </table>
    </td>
    </tr>
    </table>
    <div id="nsEditor" style="display:none;"/>
</div>
</fmt:bundle>


