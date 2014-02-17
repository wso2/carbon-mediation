<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.switchm.SwitchDefaultMediator" %>
<%@ page import="org.wso2.carbon.mediator.switchm.SwitchMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.mediator.switchm.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.switchm.ui.i18n.JSResources"
		request="<%=request%>"
        i18nObjectName="switchi18n"/>

    <%
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof SwitchMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }
        SwitchMediator switchMediator = (SwitchMediator) mediator;

        boolean defaultPresent = false;
        List<Mediator> mediatorList = switchMediator.getList();
        for (Mediator aMediator : mediatorList) {
            if (aMediator instanceof SwitchDefaultMediator) {
                defaultPresent = true;
                break;
            }
        }

        int noOfCases = mediatorList.size();
        if (defaultPresent) {
            noOfCases = noOfCases - 1;
        }
    %>

    <div>
        <script type="text/javascript" src="../switch-mediator/js/mediator-util.js"></script>
        <script type="text/javascript">
            function addCase() {
                if (!updateEditingMediator()) {
                    return;
                }
                document.location.href = "../switch-mediator/add_case.jsp";
            }
            function addDefault() {
                if (!updateEditingMediator()) {
                    return;
                }
                document.location.href = "../switch-mediator/add_default.jsp";
            }
        </script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="switch.mediator"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td>
                                <fmt:message key="source.xpath"/> <span class="required">*</span>
                            </td>
                            <td>
                                <%
                                    NameSpacesRegistrar nameSpacesRegistrar =
                                            NameSpacesRegistrar.getInstance();
                                    nameSpacesRegistrar.registerNameSpaces(
                                            switchMediator.getSource(), "sourceXPath", session);
                                    if (switchMediator.getSource() != null) {
                                %>
                                <input id="sourceXPath" type="text"
                                       value="<%=switchMediator.getSource().toString()%>"
                                       name="sourceXPath"
                                       size="40"/>
                                <%} else {%>
                                <input type="text" value="" id="sourceXPath" name="sourceXPath"
                                       size="40"/>
                                <%}%>
                            </td>
                            <td>
                                <a href="#nsEditorLink" id="mediator.switch.nmsp_button"
                                   onclick="showNameSpaceEditor('sourceXPath')"
                                   class="nseditor-icon-link"
                                   style="padding-left:40px">
                                    <fmt:message key="namespaces"/>
                                </a>
                            </td>
                        </tr>
                        <tr>
                            <td >
                                <fmt:message key="number.of.cases"/>
                            </td>
                            <td>
                                <%= noOfCases %> <fmt:message key="case"/><%= noOfCases != 1 ? "s" : "" %>
                            </td>
                            <td></td>
                        </tr>
                        <tr>
                            <td >
                            </td>
                            <td>
                                <a class="add-icon-link"
                                   href="#caseTarget"
                                   onclick="addCase()"><fmt:message key="add.case"/></a>
                            </td>
                            <td></td>
                        </tr>
                        <%
                            if (!defaultPresent) {
                        %>
                        <tr>
                            <td>
                                <a class="add-icon-link"
                                   href="#defaultTarget"
                                   onclick="addDefault()"><fmt:message key="default.case"/>
                            </td>
                            <td></td>
                            <td></td>
                        </tr>
                        <%
                            }
                        %>
                    </table>
                </td>
            </tr>
        </table>

        <a name="caseTarget"></a>

        <a name="defaultTarget"></a>

        <a name="nsEditorLink"></a>

        <div id="nsEditor" style="display:none;"></div>

    </div>
</fmt:bundle>
