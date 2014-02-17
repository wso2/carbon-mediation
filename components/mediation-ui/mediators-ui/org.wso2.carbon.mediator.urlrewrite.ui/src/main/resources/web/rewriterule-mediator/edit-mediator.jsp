<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%--
  ~  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.apache.axiom.om.xpath.AXIOMXPath" %>
<%@ page import="org.wso2.carbon.mediator.urlrewrite.URLRewriteMediator"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page
	import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar"%>
<%@ page import="java.util.List"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@page import="org.wso2.carbon.mediator.urlrewrite.URLRewriteActions"%>

<%@page import="org.wso2.carbon.mediator.urlrewrite.URLRulesMediator"%>
<%@ page import="java.io.ByteArrayInputStream" %>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp" />

<fmt:bundle
	basename="org.wso2.carbon.mediator.urlrewrite.ui.i18n.Resources">
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.urlrewrite.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="urlrewritejsi18n" />

	<%
		Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
			if (!(mediator instanceof URLRulesMediator)) {
				throw new RuntimeException("Unable to edit the mediator");
			}
			URLRulesMediator urlRulesMediator = (URLRulesMediator) mediator;
			List<URLRewriteActions> actions = urlRulesMediator.getActions();	
			NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();		
			// rule list			
			String actionTableStyle = actions.isEmpty() ? "display:none;" : "";	
			
			String condition = "";
			if ((urlRulesMediator.getEvaluator() != null)) {
		        condition = urlRulesMediator.getCondition();
		        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(new ByteArrayInputStream(condition.getBytes()));
		        condition= xmlPrettyPrinter.xmlFormat();
		    }
			 
	%>


	<div><script type="text/javascript"
		src="../rewriterule-mediator/js/mediator-util.js"></script>

	<table class="normal">

		<tr>
			<td>
			<h2><fmt:message key="mediator.urlrewriterule.header" /></h2>
			</td>
		</tr>
		<table class="normal">
			<tr id="mediator.urlrewrite.condition_row">
				<td style="padding-right: 10px;"><fmt:message
					key="mediator.urlrewrite.condition" /></td>
				<td style="padding-right: 5px;">
				<textarea cols="60" rows="10" id="mediator.urlrewrite.condition"
					name="mediator.urlrewrite.condition" >
					<%=condition%>
					</textarea>
					 </td>
			</tr>
		</table>
		<tr>
			<td>
			<h3 class="mediator"><fmt:message
				key="mediator.urlrewrite.rules" /></h3>
			<div style="margin-top: 10px;">
			<table id="actiontable" class="styledInner"
				style="<%=actionTableStyle%>;">
				<thead>

					<tr>
						<th width="20%"><fmt:message key="mediator.urlrewrite.action" /></th>
						<th width="20%"><fmt:message
							key="mediator.urlrewrite.fragment" /></th>
						<th width="10%"><fmt:message
							key="mediator.urlrewrite.option" /></th>
						<th id="value-th" width="20%"><fmt:message
							key="mediator.urlrewrite.valueexpression" /></th>
						<th id="ns-edior-th" width="20%"><fmt:message
							key="mediator.urlrewrite.namespaceeditor" /></th>
						<th id="regex-th" width="20%"><fmt:message
							key="mediator.urlrewrite.regex" /></th>
						<th><fmt:message key="mediator.urlrewrite.delete" /></th>
					</tr>
					<tbody id="actiontbody">
				<%
							int i = 0;
								for (URLRewriteActions urlRewriteAction : actions) {
									String value = null;
									boolean isLiteral=false;									
									AXIOMXPath expr = null;
										value=urlRewriteAction.getValue();
						                expr = urlRewriteAction.getXpath();
						                if (expr != null) {
						                    nameSpacesRegistrar.registerNameSpaces(expr, "mediator.urlrewrite.valuetxt" + i, session);						                 
						                }
						         isLiteral = value != null && !"".equals(value);
								
						%>

						<tr id="actionRow<%=i%>">


							<td><select id="action_select<%=i%>"
								name="action_select<%=i%>" style="width: 150px;" onchange="onActionTypeSelectionChange('action_select<%=i%>',<%=i %>);">
								<option value="set" id="actionSet<%=i%>" name="actionSet<%=i%>"
									<%=urlRewriteAction.getAction() != null &&
					          urlRewriteAction.getAction().equals("set") ? "selected=\"selected\""
					                                                    : ""%>>
								<fmt:message key="mediator.urlrewrite.set" /></option>

								<option value="remove" id="actionRemove<%=i%>" name="actionRemove<%=i%>"
									<%=urlRewriteAction.getAction() != null &&
					          urlRewriteAction.getAction().equals("remove")
					                                                       ? "selected=\"selected\""
					                                                       : ""%>>
								<fmt:message key="mediator.urlrewrite.remove" /></option>

								<option value="append" id="actionAppend<%=i%>" name="actionAppend<%=i%>"
									<%=urlRewriteAction.getAction() != null &&
					          urlRewriteAction.getAction().equals("append")
					                                                       ? "selected=\"selected\""
					                                                       : ""%>>
								<fmt:message key="mediator.urlrewrite.append" /></option>

								<option value="prepend" id="actionPrepend<%=i%>" name="actionPrepend<%=i%>"
									<%=urlRewriteAction.getAction() != null &&
					          urlRewriteAction.getAction().equals("prepend")
					                                                        ? "selected=\"selected\""
					                                                        : ""%>>
								<fmt:message key="mediator.urlrewrite.prepend" /></option>

								<option value="replace" id="actionReplace<%=i%>" name="actionReplace<%=i%>"
									<%=urlRewriteAction.getAction() != null &&
					          urlRewriteAction.getAction().equals("replace")
					                                                        ? "selected=\"selected\""
					                                                        : ""%>>
								<fmt:message key="mediator.urlrewrite.replace" /></option>
							</select></td>

							<td><select  id="fragment_select<%=i%>"
								name="fragment_select<%=i%>" style="width: 150px;" onchange="onFragmentTypeSelectionChange('fragment_select<%=i%>',<%=i %>);" >
								<option value="protocol"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("protocol")
					                                                           ? "selected=\"selected\""
					                                                           : ""%>>
								<fmt:message key="protocol" /></option>

								<option value="host"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("host")
					                                                       ? "selected=\"selected\""
					                                                       : ""%>>
								<fmt:message key="host" /></option>

								<option value="port"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("port")
					                                                       ? "selected=\"selected\""
					                                                       : ""%>>
								<fmt:message key="port" /></option>

								<option value="path"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("path")
					                                                       ? "selected=\"selected\""
					                                                       : ""%>>
								<fmt:message key="path" /></option>

								<option value="query"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("query")
					                                                        ? "selected=\"selected\""
					                                                        : ""%>>
								<fmt:message key="query" /></option>

								<option value="ref"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("ref")
					                                                      ? "selected=\"selected\""
					                                                      : ""%>>
								<fmt:message key="ref" /></option>

								<option value="user"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("user")
					                                                       ? "selected=\"selected\""
					                                                       : ""%>>
								<fmt:message key="user" /></option>

								<option value="full"
									<%=urlRewriteAction.getFragment() != null &&
					          urlRewriteAction.getFragment().equals("full")
					                                                       ? "selected=\"selected\""
					                                                       : ""%>>
								<fmt:message key="full" /></option>
							</select>
                            <script type="text/javascript">
                                    onFragmentTypeSelectionChange('fragment_select<%=i%>', <%=i%>)
                                </script>
                            </td>

							<td><select class="esb-edit small_textbox"
								name="optionTypeSelection<%=i%>"
								id="optionTypeSelection<%=i%>"
								onchange="onOptionTypeSelectionChange('<%=i%>')">
								<%
									if (isLiteral) {
								%>
								<option value="literal"><fmt:message
									key="mediator.urlrewrite.value" /></option>
								<option value="expression"><fmt:message
									key="mediator.urlrewrite.expression" /></option>
								<%
									} else if (expr != null) {
								%>
								<option value="expression"><fmt:message
									key="mediator.urlrewrite.expression" /></option>
								<option value="literal"><fmt:message
									key="mediator.urlrewrite.value" /></option>
								<%
									} else {									
								%>								
								<option value="literal"><fmt:message
									key="mediator.urlrewrite.value" /></option>
								<option value="expression"><fmt:message
									key="mediator.urlrewrite.expression" /></option>
								<%
									}
								%>
							</select></td>

							<td>
							<%
								if (value != null && !"".equals(value)) {
							%> <input
								id="mediator.urlrewrite.valuetxt<%=i%>" name="mediator.urlrewrite.valuetxt<%=i%>" type="text"
								value="<%=value%>" class="esb-edit" /> <%
 								}else if (expr != null) {
 							%>
							<input id="mediator.urlrewrite.valuetxt<%=i%>" name="mediator.urlrewrite.valuetxt<%=i%>"
								type="text" value="<%=expr.toString()%>"
								class="esb-edit" /> <%
 							} else {
 							%>	<input	style="width: 300px; <%="display:none;"%>" id="mediator.urlrewrite.valuetxt<%=i%>" name="mediator.urlrewrite.valuetxt<%=i%>" type="text"
 								 class="esb-edit" />	
 							<%} %>
 							
							</td>						
							<% 
							if(isLiteral){
							%>								
								<td id="nsEditorButtonTD<%=i%>" style='display:""'>
							
							 <script type="text/javascript">
                                    document.getElementById("ns-edior-th").style.display = "";
                               </script>
							<% 
							}
							%>
							</td>
							
							<td id="nsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
							
							<%
								if (!isLiteral && expr != null) {									
							%> 
							   <script type="text/javascript">
                                    document.getElementById("ns-edior-th").style.display = "";
                                </script>
							<a
								href="#nsEditorLink" id="nsEditorButtonTD<%=i%>"
								class="nseditor-icon-link" style="padding-left: 40px"
								onclick="showNameSpaceEditor('mediator.urlrewrite.valuetxt<%=i%>')"> 
								</a> <%
 								}
 							%>
							</td>

							<td style="padding-right: 5px;" >
							
							<input type="text"
								style="width: 300px; <%=urlRewriteAction.getRegex() == null?"display:none;":""%>" id="mediator.urlrewrite.regex<%=i%>"
								name="mediator.urlrewrite.regex<%=i%>" type="text"
								value='<%=urlRewriteAction.getRegex() != null ? urlRewriteAction.getRegex()
					                                             : ""%>'
								class="esb-edit" />
								
							</td>

							<td id="deleteButtonTD<%=i%>"><a href="#" href="#"
								class="delete-icon-link"
								onclick="deleteAction('<%=i%>');return false;"><fmt:message
								key="mediator.urlrewrite.delete" /></a></td>
						</tr>

						<%
							i++;
								}
						%>
						<input type="hidden" name="actionCount" id="actionCount"
							value="<%=i%>" />
							 <script type="text/javascript">
                            if (isRemainPropertyExpressions()) {
                                resetDisplayStyle("");
                            }
                        </script>
					</tbody>
				</thead>
			</table>
			</div>
			</td>
		</tr>

		<tr>
			<td>
			<div style="margin-top: 0px;"><a name="addActionsLink"></a> <a
				class="add-icon-link" href="#addRulesLink"
				onclick="addAction('<fmt:message key="actionemptyerror"/>','<fmt:message key="fragmentemptyerror"/>')">
			<fmt:message key="mediator.urlrewrite.addaction" /></a></div>
			</td>
		</tr>
	</table>
	</div>
</fmt:bundle>