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

<%@ page import="org.wso2.carbon.mediator.conditionalrouter.ConditionalRouteMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="org.wso2.carbon.mediator.conditionalrouter.ConditionalRouterMediator" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<%--<script type="text/javascript">
    editAreaLoader.init({
        id : "conditionalRouteConfig"                // text area id
        ,syntax: "xml"                  // syntax to be uses for highlighting
        ,start_highlight: true  // to display with highlight mode on start-up
    });
</script>--%>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ConditionalRouteMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ConditionalRouteMediator conditionalRouteMediator = (ConditionalRouteMediator) mediator;
    String conditionString = "";
    boolean isAsynchronous = conditionalRouteMediator.isAsynchronous();
    boolean isBreakAfter = conditionalRouteMediator.isBreakAfter();

    if (conditionalRouteMediator.getEvaluator() != null) {
        conditionString = conditionalRouteMediator.getConditionString();
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(new ByteArrayInputStream(conditionString.getBytes()));
        conditionString = xmlPrettyPrinter.xmlFormat();
    }

    String targetSeq = conditionalRouteMediator.getTargetSeq();

    /*if(conditionalRouteMediator.getExpression() != null){
        val = conditionalRouteMediator.getExpression().toString();
        NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
        nameSpacesRegistrar.registerNameSpaces(conditionalRouteMediator.getExpression(), "mediator.route.expression", session);
    }*/
%>
<fmt:bundle basename="org.wso2.carbon.mediator.conditionalrouter.ui.i18n.Resources">
    <div>
        <script type="text/javascript">
            function addRoute() {
                document.location.href = "../conditionalrouter-mediator/add_route.jsp";
            }
        </script>

        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="conditional.route.condifuration"/></h2>
                </td>
            </tr>

            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td>
                                <fmt:message key="break.after.route"/>
                            </td>
                            <td>
                                <select name="mediator.conditionalroute.break" id="mediator.conditionalroute.break">
                                    <option value="true"
                                            <%= isBreakAfter ? "selected=\"true\"" : ""%>>Yes</option>
                                    <option value="false"
                                            <%= !isBreakAfter ? "selected=\"true\"" : ""%>>No</option>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <fmt:message key="evaluator.configuration"/><span class="required">*</span>
                            </td>
                            <td>
                                <textarea name="conditionalRouteConfig" id="conditionalRouteConfig"
                                          title="Configuration"
                                          cols="60" rows="10"><%= conditionString != null ? conditionString : "" %></textarea>
                            </td>

                        </tr>
                        <tr>
                            <td>
                                <fmt:message key="route.target"/><span class="required">*</span>
                            </td>
                            <td>
                                <input name="seq.target"
                                       type="text" id="seq.target"
                                       style="width:300px;" value="<%= targetSeq != null ? targetSeq : "" %>"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink" id="confRegSeqLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('seq.target','/_system/config');">
                                    Configuration Registry</a>
                                <a href="#registryBrowserLink" id="govRegSeqLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('seq.target','/_system/governance');">
                                    Governance Registry</a>
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