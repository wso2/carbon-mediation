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

<%@ page import="org.wso2.carbon.mediator.aggregate.AggregateMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.aggregate.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.aggregate.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="aggregatejsi18n"/>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof AggregateMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    AggregateMediator aggregateMediator = (AggregateMediator) mediator;

    String sequenceRef = aggregateMediator.getOnCompleteSequenceRef();
%>

<div>
<script type="text/javascript" src="../aggregate-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
    <tr>
        <td>
            <h2><fmt:message key="mediator.aggregate.header"/></h2>
        </td>
    </tr>
    <tr>
        <td>
            <table class="normal">
              <tr>
                    <td>
                    <fmt:message key="mediator.aggregate.id"/>
                    </td>
                    <td><input type="text" id="mediator.aggregate.id" name="mediator.aggregate.id"
						value='<%= aggregateMediator.getId() != null ? 
								aggregateMediator.getId(): ""%>' />
					</td>
					<td></td>
					<td></td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mediator.aggregate.expression"/> <span class="required">*</span>
                    </td>
                    <td>
                        <%
                            NameSpacesRegistrar nameSpacesRegistrar =
                                    NameSpacesRegistrar.getInstance();
                            nameSpacesRegistrar.registerNameSpaces(
                                    aggregateMediator.getAggregationExpression(), "aggregate_expr",
                                    session);
                            if (aggregateMediator.getAggregationExpression() != null) {
                        %>
                        <input value="<%=aggregateMediator.getAggregationExpression().toString()%>"
                               id="aggregate_expr" name="aggregate_expr" type="text">
                        <%
                        } else {
                        %>
                        <input value="" id="aggregate_expr" name="aggregate_expr" type="text">
                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('aggregate_expr')"><fmt:message key="mediator.aggregate.nameSpaces"/></a>
                    </td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mediator.aggregate.timeout"/>
                    </td>
                    <td>
                        <%
                            if (aggregateMediator.getCompletionTimeoutSec() == 0) {
                        %>
                        <input value="" name="complete_time" id="complete_time" type="text">
                        <%
                        } else {
                        %>
                        <input value="<%=aggregateMediator.getCompletionTimeoutSec()%>"
                               name="complete_time" id="complete_time" type="text">
                        <%
                            }
                        %>
                    </td>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mediator.aggregate.maxmessages"/>
                    </td>
                    <td><select name="msgMaxType" id="msgMaxType" onchange="onMessageTypeSelectionChange('msgMaxType','nsMaxEditorButtonTD')">
												<%
												if(aggregateMediator.getMaxMessagesToComplete() != null){
												nameSpacesRegistrar.registerNameSpaces(
												                                       aggregateMediator.getMaxMessagesToComplete().getExpression(), "msgMaxType",
												                                       session);
												}
												if(aggregateMediator.getMaxMessagesToComplete() != null && aggregateMediator.getMaxMessagesToComplete().getExpression() !=null){
												%>
												<option value="literal">Value</option>
												<option value="expression" selected="selected">Expression</option>
												<%
													}else if(aggregateMediator.getMaxMessagesToComplete() != null && aggregateMediator.getMaxMessagesToComplete().getKeyValue() !=null){
												%>
												<option value="literal" selected="selected">Value</option>
												<option value="expression">Expression</option>
												<%
													}else{
												%>
												<option value="literal">Value</option>
												<option value="expression">Expression</option>
												<%
													}
												%>
										</select>
										</td>
                    <td>
                        <%
                            if (aggregateMediator.getMaxMessagesToComplete() != null && aggregateMediator.getMaxMessagesToComplete().getExpression() !=null ) {
                        %>
                        <input value="<%=aggregateMediator.getMaxMessagesToComplete().getExpression().toString()%>" name="complete_max" id="complete_max" type="text">
                        <%
                        } else if(aggregateMediator.getMaxMessagesToComplete() != null && aggregateMediator.getMaxMessagesToComplete().getKeyValue() !=null){
                        %>
                        <input value="<%=aggregateMediator.getMaxMessagesToComplete().getKeyValue()%>"
                               name="complete_max" id="complete_max" type="text">
                        <%
                            }else{
                        %>
                        <input value="-1"
                               name="complete_max" id="complete_max" type="text">
                        <%
                            }
                        %>
                    </td>
                     <td id="nsMaxEditorButtonTD" style="<%=(aggregateMediator.getMaxMessagesToComplete() ==null || aggregateMediator.getMaxMessagesToComplete().getExpression() ==null)?"display:none;":""%>">
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('msgMaxType')"><fmt:message key="mediator.aggregate.nameSpaces"/></a>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mediator.aggregate.minmessages"/>
                    </td>
                    </td>
                    <td><select name="msgMinType" id="msgMinType" onchange="onMessageTypeSelectionChange('msgMinType','nsMinEditorButtonTD')">
												<%
												if(aggregateMediator.getMinMessagesToComplete() != null){
												nameSpacesRegistrar.registerNameSpaces(
												                                       aggregateMediator.getMinMessagesToComplete().getExpression(), "msgMinType",
												                                       session);
												}
												
													if(aggregateMediator.getMinMessagesToComplete() != null && aggregateMediator.getMinMessagesToComplete().getExpression() != null){
												%>
												<option value="literal">Value</option>
												<option value="expression" selected="selected">Expression</option>
												<%
													}else if(aggregateMediator.getMinMessagesToComplete() != null && aggregateMediator.getMinMessagesToComplete().getKeyValue() != null){
												%>
												<option value="literal" selected="selected">Value</option>
												<option value="expression">Expression</option>
												<%
													}else{
												%>
												<option value="literal">Value</option>
												<option value="expression">Expression</option>
												<%
													}
												%>
										</select>
										</td>
                    <td>
                       <%
                            if (aggregateMediator.getMinMessagesToComplete() != null && aggregateMediator.getMinMessagesToComplete().getExpression() != null) {
                        %>
                        <input value="<%=aggregateMediator.getMinMessagesToComplete().getExpression().toString()%>" name="complete_min" id="complete_min" type="text">
                        <%
                        } else if(aggregateMediator.getMinMessagesToComplete() != null && aggregateMediator.getMinMessagesToComplete().getKeyValue() != null){
                        %>
                        <input value="<%=aggregateMediator.getMinMessagesToComplete().getKeyValue() %>"
                               name="complete_min" id="complete_min" type="text">
                        <%
                            }else{
                        %>
                         <input value="-1"
                               name="complete_min" id="complete_min" type="text">
                        <%
                            }
                        %>
                    </td>
                    <td id="nsMinEditorButtonTD" style="<%=(aggregateMediator.getMinMessagesToComplete() ==null || aggregateMediator.getMinMessagesToComplete().getExpression() ==null)?"display:none;":""%>">
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('msgMinType')"><fmt:message key="mediator.aggregate.nameSpaces"/></a>
                    </td>
                
                </tr>
                <tr>
                    <td>
                        <fmt:message key="mediator.aggregate.correlation"/>
                    </td>
                    <td>
                        <%
                            nameSpacesRegistrar.registerNameSpaces(
                                    aggregateMediator.getCorrelateExpression(), "correlate_expr",
                                    session);
                            if (aggregateMediator.getCorrelateExpression() != null) {
                        %>
                        <input value="<%=aggregateMediator.getCorrelateExpression().toString()%>"
                               id="correlate_expr" name="correlate_expr" type="text">
                        <%
                        } else {
                        %>
                        <input value="" id="correlate_expr" name="correlate_expr" type="text">
                        <%
                            }
                        %>
                    </td>
                    <td>
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('correlate_expr')"><fmt:message key="mediator.aggregate.nameSpaces"/></a>
                    </td>
                    <td></td>
                </tr>


                <tr>
                   <td>
                      <fmt:message key="mediator.aggregate.enclose.element.property"/>
                   </td>
                   <td><input type="text" id="mediator.aggregate.enclose.element.property.name" name="mediator.aggregate.enclose.element.property.name"
                             value='<%= aggregateMediator.getEnclosingElementPropertyName() != null ?
              								aggregateMediator.getEnclosingElementPropertyName(): ""%>'/>
                   </td>
                   <td></td>
                   <td></td>
                </tr>


                <tr>
                    <td rowspan="3">
                        <b><fmt:message key="mediator.aggregate.complete"/></b>
                    </td>
                    <td>
                        <input type="radio" id="sequenceOptionAnon" name="sequenceOption" value="annon" onclick="anonSelected()"/><fmt:message key="mediator.aggregate.anonymous"/>
                    </td>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <input type="radio" id="sequenceOptionReference" name="sequenceOption" value="selectFromRegistry" onclick="registrySelected()"/><fmt:message key="mediator.aggregate.registry"/>
                    </td>
                    <td>
                        <div id="mediator.sequence.txt.div">
                        <input type="text" id="mediator.sequence" name="mediator.sequence" readonly="true"/>
                        </div>
                    </td>
                    <td>
                        <div id="mediator.sequence.link.div">
                            <a href="#registryBrowserLink"
                               class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.sequence','/_system/config')"><fmt:message
                                    key="mediator.aggregate.conf.registry.browser"/></a>
                            <a href="#registryBrowserLink"
                               class="registry-picker-icon-link"
                               onclick="showRegistryBrowser('mediator.sequence','/_system/governance')"><fmt:message
                                    key="mediator.aggregate.gov.registry.browser"/></a>
                        </div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<script type="text/javascript">
    <%
    if (sequenceRef == null) {
    %>
    document.getElementById("sequenceOptionAnon").checked = true;
    anonSelected();
    <%

    } else {
   %>
    document.getElementById("sequenceOptionReference").checked = true;
    document.getElementById("mediator.sequence").value = "<%=sequenceRef%>";
    registrySelected();
    <%
    }
    %>

    function anonSelected() {
        document.getElementById("mediator.sequence.txt.div").style.display = "none";
        document.getElementById("mediator.sequence.link.div").style.display = "none";
    }

    function registrySelected() {
        document.getElementById("mediator.sequence.txt.div").style.display = "";
        document.getElementById("mediator.sequence.link.div").style.display = "";
    }
</script>

  <a name="nsEditorLink"></a>

  <div id="nsEditor" style="display:none;"></div>

  <a name="registryBrowserLink"></a>

  <div id="registryBrowser" style="display:none;"></div>
</div>
</fmt:bundle>