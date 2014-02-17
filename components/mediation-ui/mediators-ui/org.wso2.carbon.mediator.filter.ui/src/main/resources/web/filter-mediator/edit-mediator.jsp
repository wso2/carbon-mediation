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

<%@ page import="org.wso2.carbon.mediator.filter.FilterMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    boolean isSourceXPath = false;
    String sourceVal = "", regEx = "", xpathVal = "";
    if (!(mediator instanceof FilterMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    FilterMediator filterMediator = (FilterMediator) mediator;
    if (filterMediator.getSource() != null && filterMediator.getRegex() != null) {
        isSourceXPath = true;
    } 
    if (filterMediator.getRegex() != null) {
        regEx = filterMediator.getRegex().toString();
    }
    if (filterMediator.getSource() != null) {
        NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
        nmspRegistrar.registerNameSpaces(filterMediator.getSource(), "mediator.filter.source_val", session);
        sourceVal = filterMediator.getSource().toString();
        sourceVal = sourceVal.replace("\"","&quot;");//replace quote sign with &quot;
    } else if (filterMediator.getXpath() != null) {
        NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
        nmspRegistrar.registerNameSpaces(filterMediator.getXpath(), "mediator.filter.xpath_val", session);
        xpathVal = filterMediator.getXpath().toString();
    }

%>
<fmt:bundle basename="org.wso2.carbon.mediator.filter.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.filter.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="filterMediatorJsi18n"/>    
<div>
    <script type="text/javascript" src="../filter-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
    <tr>
        <td><h2><fmt:message key="mediator.filter.header"/></h2></td>
    </tr>
    <tr>
        <td>
            <table class="normal">
                <tbody>
                    <tr>
                        <td style="width:80px;"><fmt:message key="mediator.filter.specify"/></td>
                        <td style="width:80px">
                            <input id="xpath" type="radio"
                                   name="mediator.filter.type" <%=!isSourceXPath ? "checked=\"checked\"" : ""%>
                                   onclick=" javascript:displayElement('mediator.filter.reg_ex', false); displayElement('mediator.filter.xpath', true); displayElement('mediator.filter.source', false);"
                                   value="xpath"/><fmt:message key="mediator.filter.xpath"/>
                        </td>
                        <td>
                            <input id="xpathRex" type="radio"
                                   name="mediator.filter.type" <%=isSourceXPath ? "checked=\"checked\"" : ""%>
                                   onclick=" javascript:displayElement('mediator.filter.reg_ex', true); displayElement('mediator.filter.xpath', false); displayElement('mediator.filter.source', true);"
                                   value="xpathRegx"/><fmt:message key="mediator.filter.sourceExpr"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table border="0" class="normal">
                <tbody>
                    <tr id="mediator.filter.xpath" <%=isSourceXPath ? "style=\"display:none\"" : ""%>>
                        <td style="width: 80px;">
                            <fmt:message key="mediator.filter.xpath"/>
                            <font style="color: red; font-size: 8pt;"> *</font>
                        </td>
                        <td style="width: 305px;">
                            <input id="filter_xpath" name="mediator.filter.xpath_val" type="text" value="<%=xpathVal%>"
                                   style="width: 300px;"/>
                        </td>
                        <td>
                            <a href="#"
                               id="mediator.callout.target.xpath_nmsp_button"
                               onclick="showNameSpaceEditor('mediator.filter.xpath_val');" class="nseditor-icon-link"
                                style="padding-left:40px">
                                <fmt:message key="mediator.filter.namespace"/></a>
                        </td>
                    </tr>
                    <tr id="mediator.filter.source" <%=!isSourceXPath ? "style=\"display:none\"" : ""%>>
                        <td style="width: 80px;">
                            <fmt:message key="mediator.filter.source"/>
                            <font style="color: red; font-size: 8pt;"> *</font>
                        </td>
                        <td style="width: 305px;">
                            <input id="filter_src" name="mediator.filter.source_val" type="text" value="<%=sourceVal%>"
                                   style="width: 300px;"/>
                        </td>
                        <td>
                            <a href="#"
                               id="mediator.callout.target.xpath_nmsp"
                               onclick="showNameSpaceEditor('mediator.filter.source_val');" class="nseditor-icon-link"
                                style="padding-left:40px">
                                <fmt:message key="mediator.filter.namespace"/></a>
                        </td>
                    </tr>
                    <tr id="mediator.filter.reg_ex" <%=!isSourceXPath ? "style=\"display:none\"" : ""%>>
                        <td>
                            <fmt:message key="mediator.filter.regex"/>
                            <font style="color: red; font-size: 8pt;"> *</font>
                        </td>
                        <td>
                            <input id="filter_regex" name="mediator.filter.regex_val" type="text" size="40"
                                   value="<%=regEx%>" style="width: 300px;"/>
                        </td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr style="display:none;">
        <td>
            <div id="nsEditor" style="display:none;"/>
        </td>
    </tr>
</table>
    <br/>
</div>
</fmt:bundle>