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

<%@ page import="org.wso2.carbon.mediator.rmsequence.RMSequenceMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof RMSequenceMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    RMSequenceMediator rmsequenceMediator = (RMSequenceMediator) mediator;
    String showCorrValues;
    if (rmsequenceMediator.isSingle()) {
        showCorrValues = "display:none";
    } else {
        showCorrValues = "";
    }
    boolean isVersion11 = false;
    if (rmsequenceMediator.getVersion() == null) {
        // assume default
        isVersion11 = false;
    } else if (rmsequenceMediator.getVersion() != null) {
        if (rmsequenceMediator.getVersion().equals("1.0")) {
            isVersion11 = false;
        } else if (rmsequenceMediator.getVersion().equals("1.1")) {
            isVersion11 = true;
        }
    } else {
        // assume default
        isVersion11 = false;
    }
    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    if (rmsequenceMediator.getCorrelation() != null) {
        nameSpacesRegistrar.registerNameSpaces(rmsequenceMediator.getCorrelation(), "correlation", session);
    }
    if (rmsequenceMediator.getLastMessage() != null) {
        nameSpacesRegistrar.registerNameSpaces(rmsequenceMediator.getLastMessage(), "last-message", session);
    }

%>
<fmt:bundle basename="org.wso2.carbon.mediator.rmsequence.ui.i18n.Resources">
    <div>
        <script type="text/javascript" src="../rmsequence-mediator/js/mediator-util.js"></script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.rmsequence.header"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td><fmt:message key="mediator.rmsequence.version"/></td>
                            <td><input <%=isVersion11?"":"checked"%> name="version" id="v1.0" type="radio" value="1.0">1.0
                            </td>
                            <td><input <%=isVersion11?"checked":""%>  name="version" id="v1.1" type="radio" value="1.1">1.1
                            </td>

                        </tr>
                        <tr>
                            <td><fmt:message key="mediator.rmsequence.msgsequence"/></td>
                            <%
                                if (rmsequenceMediator.isSingle()) {
                            %>
                            <td><input onclick="javascript:hideCorrelationPanel();" checked="true" name="messageType"
                                       id="singleRadio" type="radio" value="single"><fmt:message
                                    key="mediator.rmsequence.singlemsg"/></td>
                            <td><input onclick="javascript:showCorrelationPanel();" name="messageType"
                                       id="correlationRadio" type="radio" value="correlated"><fmt:message
                                    key="mediator.rmsequence.correlatedseq"/></td>
                            <%
                            } else {
                            %>
                            <td><input onclick="javascript:hideCorrelationPanel();" name="messageType" id="singleRadio"
                                       type="radio" value="single"><fmt:message key="mediator.rmsequence.singlemsg"/>
                            </td>
                            <td><input onclick="javascript:showCorrelationPanel();" checked="true" name="messageType"
                                       id="correlationRadio" type="radio" value="correlated"><fmt:message
                                    key="mediator.rmsequence.correlatedseq"/></td>
                            <%
                                }
                            %>
                        </tr>
                        <tr id="correlation_row" style="<%=showCorrValues%>">
                            <td></td>
                            <td></td>
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                            <fmt:message key="mediator.rmsequence.xpath"/><span
                                                class="required">*</span>
                                        </td>
                                        <td>
                                            <input name="correlation" id="correlation" type="text"
                                                   value="<%=rmsequenceMediator.getCorrelation()!=null?rmsequenceMediator.getCorrelation():""%>">
                                        </td>
                                        <td><a href="#nsEditorLink" class="nseditor-icon-link"
                                               style="padding-left:40px"
                                               onclick="showNameSpaceEditor('correlation')"><fmt:message
                                                key="mediator.rmsequence.nameSpaces"/></a>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="mediator.rmsequence.lastxpath"/></td>
                                        <td><input id="last-message" name="last-message" type="text"
                                                   value="<%=rmsequenceMediator.getLastMessage()!=null?rmsequenceMediator.getLastMessage():""%>">
                                        </td>
                                        <td>
                                            <a href="#nsEditorLink" class="nseditor-icon-link"
                                               style="padding-left:40px"
                                               onclick="showNameSpaceEditor('last-message')"><fmt:message
                                                    key="mediator.rmsequence.nameSpaces"/></a>
                                        </td>
                                    </tr>
                                </table>
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