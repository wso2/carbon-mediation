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

<%@ page import="org.wso2.carbon.mediator.iterate.IterateMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof IterateMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    IterateMediator iterateMediator = (IterateMediator) mediator;
%>

<fmt:bundle basename="org.wso2.carbon.mediator.iterate.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.iterate.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="iteratei18n"/>

<div>
    <script type="text/javascript" src="../iterate-mediator/js/mediator-util.js"></script>
    <table class="normal" width="100%">
        <tr>
            <td>
                <h2><fmt:message key="mediator.iterate.header"/></h2>
            </td>
        </tr>
        <tr>
            <td>
                <table class="normal">
                <tr>
                    <td>
                    <fmt:message key="mediator.iterate.id"/>
                    </td>
                    <td><input type="text" id="mediator.iterate.id" name="mediator.iterate.id"
						value='<%= iterateMediator.getId() != null ? 
								iterateMediator.getId(): ""%>' />
					</td>
					<td></td>				
                </tr>
                   <tr>
                        <td>
                            <fmt:message key="mediator.iterate.sequential"/>
                        </td>
                        <td>
                            <select name="sequentialMed" id="sequentialMed"
                                    class="esb-edit small_textbox">
                                <%
                                    if (iterateMediator.isSequential()) {
                                %>
                                <option selected="true" value="true">True</option>
                                <option value="false">False</option>
                                <%
                                } else {
                                %>
                                <option value="true">True</option>
                                <option selected="true" value="false">False</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="mediator.iterate.parent"/>
                        </td>
                        <td>
                            <select name="continueParent" id="continueParent"
                                    class="esb-edit small_textbox">
                                <%
                                    if (iterateMediator.isContinueParent()) {
                                %>
                                <option selected="true" value="true">True</option>
                                <option value="false">False</option>
                                <%
                                } else {
                                %>
                                <option value="true">True</option>
                                <option selected="true" value="false">False</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="mediator.iterate.payload"/>
                        </td>
                        <td>
                            <select name="preservePayload" id="preservePayload"
                                    class="esb-edit small_textbox">
                                <%
                                    if (iterateMediator.isPreservePayload()) {
                                %>
                                <option selected="true" value="true">True</option>
                                <option value="false">False</option>
                                <%
                                } else {
                                %>
                                <option value="true">True</option>
                                <option selected="true" value="false">False</option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="mediator.iterate.expression"/><span class="required">*</span>
                        </td>
                        <td>
                            <%
                                NameSpacesRegistrar nameSpacesRegistrar =
                                        NameSpacesRegistrar.getInstance();
                                nameSpacesRegistrar.registerNameSpaces(
                                        iterateMediator.getExpression(), "itr_expression", session);
                                if (iterateMediator.getExpression() == null) {
                            %>
                            <input value="" id="itr_expression" name="itr_expression" type="text">
                            <%
                            } else {
                            %>
                            <input value="<%=iterateMediator.getExpression().toString()%>"
                                   id="itr_expression" name="itr_expression" type="text">
                            <%
                                }
                            %>
                        </td>
                        <td>
                            <a href="#nsEditorLink" class="nseditor-icon-link"
                               style="padding-left:40px"
                               onclick="showNameSpaceEditor('itr_expression')"><fmt:message key="mediator.iterate.nameSpaces"/></a>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <fmt:message key="mediator.iterate.attachpath"/>
                        </td>
                        <td>
                            <%
                                nameSpacesRegistrar.registerNameSpaces(
                                        iterateMediator.getAttachPath(), "attach_path", session);
                                if (iterateMediator.getAttachPath() == null) {
                            %>
                            <input value="" id="attach_path" name="attach_path" type="text">
                            <%
                            } else {
                            %>
                            <input value="<%=iterateMediator.getAttachPath().toString()%>"
                                   id="attach_path" name="attach_path" type="text">
                            <%
                                }
                            %>
                        </td>
                        <td>
                            <a href="#nsEditorLink" class="nseditor-icon-link"
                               style="padding-left:40px"
                               onclick="showNameSpaceEditor('attach_path')"><fmt:message key="mediator.iterate.nameSpaces"/></a>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="update.for.target"/>
            </td>
        </tr>
    </table>
    <a name="nsEditorLink"></a>
    
    <div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>