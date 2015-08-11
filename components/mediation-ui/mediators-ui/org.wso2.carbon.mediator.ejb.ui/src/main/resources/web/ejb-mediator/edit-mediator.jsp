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
<%@page import="java.util.Map.Entry"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>
<%@ page import="org.wso2.carbon.mediator.ejb.EJBMediator"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page import="org.apache.synapse.mediators.Value"%>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath"%>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar"%>
<%@ page import="org.apache.axiom.om.xpath.AXIOMXPath"%>

<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%
	Mediator mediator = SequenceEditorHelper.getEditingMediator(
	request, session);
	if (!(mediator instanceof EJBMediator)) {
		// todo : proper error handling
		throw new RuntimeException("Unable to edit the mediator");
	}
	EJBMediator ejbMediator = (EJBMediator) mediator;

	String clazz = "";
	if (ejbMediator.getClazz() != null) {
		clazz = ejbMediator.getClazz();
	}

	String beanstalk = "";
	if (ejbMediator.getBeanstalk() != null) {
		beanstalk = ejbMediator.getBeanstalk();
	}

	String method = "";
	if (ejbMediator.getMethod() != null) {
		method = ejbMediator.getMethod();
	}

	String target = "";
	if (ejbMediator.getTarget() != null) {
		target = ejbMediator.getTarget();

	}

	String jndiName = "";
	if (ejbMediator.getJndiName() != null) {
		jndiName = ejbMediator.getJndiName();

	}

		
	Boolean stateful = null;
	if(ejbMediator.getStateful() != null){
		stateful = ejbMediator.getStateful();
	}
	
	Boolean remove = null;
	if(ejbMediator.getRemove() != null){
		remove = ejbMediator.getRemove();
	}
	
	Value beanId = null;
	if(ejbMediator.getId() != null){
		beanId = ejbMediator.getId();
	}
	
	 NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
	 int m = 0;
	 for(Value argumentVal : ejbMediator.getArguments()){
		 if(argumentVal.getExpression() != null){
	 SynapseXPath xpathExpr = (SynapseXPath) argumentVal.getExpression();
	 nameSpacesRegistrar.registerNameSpaces(xpathExpr,"argumentValue"+m,session);
	   }
		 m++;
	 }
	 
	 if(ejbMediator.getId() != null && ejbMediator.getId().getExpression() != null){
		 nameSpacesRegistrar.registerNameSpaces(ejbMediator.getId().getExpression(),"beanIdType",session);
	 }
	

	// feature list
	List<Value> argumentList = ejbMediator.getArguments();
	String argumentTableStyle = argumentList.isEmpty() ? "display:none;"
	: "";
%>

<fmt:bundle basename="org.wso2.carbon.mediator.ejb.ui.i18n.Resources">
	<carbon:jsi18n resourceBundle="org.wso2.carbon.mediator.ejb.ui.i18n.JSResources" request="<%=request%>" i18nObjectName="ejbjsi18n" />
	<div>
		<script type="text/javascript" src="../ejb-mediator/js/mediator-util.js"></script>
		<table class="normal" width="100%">
			<tr>
				<td>
					<h2>
						<fmt:message key="mediator.ejb.header" />
					</h2></td>
			</tr>
			<tr>
				<td>
					<table class="normal">
						<tr>
							<td><fmt:message key="mediator.ejb.beanstalk" /><span class="required">*</span>
							</td>
							<td><input class="longInput" type="text" value="<%=beanstalk%>" name="beanstalk" id="beanstalk" />
							</td>
						</tr>
						<tr>
							<td><fmt:message key="mediator.ejb.clazz" /> <span class="required">*</span>
							</td>
							<td><input class="longInput" type="text" value="<%=clazz%>" name="clazz" id="clazz" />
							</td>
						</tr>
						<tr>
							<td><fmt:message key="mediator.ejb.method" /><span class="required">*</span></td>
							<td><input class="longInput" type="text" value="<%=method%>" name="ejbMethod" id="ejbMethod" /></td>
						<tr>
							<td><fmt:message key="mediator.ejb.beanId" />
							</td>
							<td style="padding-left: 0px ! important;">
								<table>
									<tr>
										<td><select name="beanIdType" id="beanIdType" onchange="onBeanTypeSelectionChange('beanIdType')">
												<%
													if(beanId != null && beanId.getExpression() != null){
												%>
												<option value="literal">Value</option>
												<option value="expression" selected="selected">Expression</option>
												<%
													}else if(beanId != null && beanId.getKeyValue() !=null){
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
												if(beanId != null && beanId.getExpression() != null){
											%> <input class="smallInput" type="text" value="<%=beanId.getExpression().toString()%>" name="id" id="id" /> <%
 	}else if(beanId != null && beanId.getKeyValue() != null){
 %> <input class="smallInput" type="text" value="<%=beanId.getKeyValue()%>" name="id" id="id" /> <%
 	}else{
 %> <input class="smallInput" type="text" value="" name="id"
											id="id" /> <%
 	}
 %>
										</td>
										<td id="nsBeanTypeEditorButtonTD" style="<%=(beanId ==null || beanId.getExpression() ==null)?"display:none;":""%>"><a href="#nsEditorLink" class="nseditor-icon-link" style="padding-left: 40px" onclick="showNameSpaceEditor('beanIdType')"> <fmt:message key="mediator.ejb.namespace" /> </a>
										</td>
									</tr>
								</table>
							</td>
						</tr>
						<tr>
							<td><fmt:message key="mediator.ejb.remove" />
							</td>
							<td><select name="remove" id="remove">
									<%
										if(remove != null && remove) {
									%>
									<option value="true" selected="selected">
										<fmt:message key="mediator.ejb.remove.YES" />
									</option>
									<option value="false">
										<fmt:message key="mediator.ejb.remove.NO" />
									</option>
									<%
										}else if(remove != null && !remove){
									%>
									<option value="true">
										<fmt:message key="mediator.ejb.remove.YES" />
									</option>
									<option value="false" selected="selected">
										<fmt:message key="mediator.ejb.remove.NO" />
									</option>
									<%
										}else{
									%>
									<option value="true">
										<fmt:message key="mediator.ejb.remove.YES" />
									</option>
									<option value="false" selected="selected">
										<fmt:message key="mediator.ejb.remove.NO" />
									</option>
									<%
										}
									%>
							</select></td>
						</tr>
						<tr>
							<td><fmt:message key="mediator.ejb.target" />
							</td>
							<td><input class="longInput" type="text" value="<%=target%>" name="target" id="target" />
							</td>
						</tr>
						<tr>
							<td><fmt:message key="mediator.ejb.jndiName" /><span class="required">*</span>
							</td>
							<td><input class="longInput" type="text" value="<%=jndiName%>" name="jndiName" id="jndiName" />
							</td>
						</tr>
						<tr>
							<td></td>
							<td>
								<h3 class="mediator">
									<fmt:message key="mediator.ejb.arguments" />
								</h3>
								<table id="argumenttable" style="<%=argumentTableStyle%>;" class="styledInner">
									<thead>
										<tr>
											<th width="15%"><fmt:message key="mediator.ejb.argumentType" /></th>
											<th width="15%"><fmt:message key="mediator.ejb.argument.name" /></th>
											<th id="ns-edior-th" style="display: none;" width="25%"><fmt:message key="mediator.esb.nsEditor" /></th>
											<th></th>
										</tr>
									<tbody id="argumenttbody">
										<%
											int l = 0;
																		    for (Value arg : argumentList) {
											                                if (arg != null) {
											                                	String value = arg.getKeyValue();
											                                	SynapseXPath expression = (SynapseXPath) arg.getExpression();
										%>
										<tr id="argumentRaw<%=l%>">
											<td><select class="esb-edit small_textbox" name="propertyTypeSelection<%=l%>" id="propertyTypeSelection<%=l%>" onchange="onPropertyTypeSelectionChange('<%=l%>','<fmt:message key="mediator.ejb.namespace"/>')">

													<%
														if(value != null){
													%>
													<option value="literal" selected="selected">Value</option>
													<option value="expression">Expression</option>
													<%
														}else if( expression != null){
													%>
													<option value="literal">Value</option>
													<option value="expression" selected="selected">Expression</option>
													<%
														}else{
													%>
													<option value="literal">Value</option>
													<option value="expression">Expression</option>
													<%
														}
													%>
											</select></td>
											<td>
												<%
													if(expression != null){
												%> <input type="text" name="argumentValue<%=l%>" id="argumentValue<%=l%>" value="<%=expression.toString()%>" /> <%
 	}else{
 %> <input type="text" name="argumentValue<%=l%>" id="argumentValue<%=l%>" value="<%=value%>" /> <%
 	}
 %>
											</td>
											<td id="nsEditorButtonTD<%=l%>" style="<%=expression == null?"display:none;":""%>">
												<%
													if(expression != null){
												%> <script type="text/javascript">
                                    					document.getElementById("ns-edior-th").style.display = "";
                                					 </script> <a href="#nsEditorLink" class="nseditor-icon-link" style="padding-left: 40px" onclick="showNameSpaceEditor('argumentValue<%=l%>')"> <fmt:message key="mediator.ejb.namespace" /> </a> <%
 	}
 %>
											</td>
											<td><a href="#" class="delete-icon-link" onclick="deleteArgument(<%=l%>);return false;"><fmt:message key="mediator.ejb.argument.delete" /> </a>
											</td>
										</tr>
										<%
											l++;
													}
												}
										%>
									</tbody>
								</table>
							</td>
						</tr>
						<tr style="display: none">
							<td><input type="hidden" name="argumentCount" id="argumentCount" value="<%=l%>" /></td>
						</tr>
						<tr id="addProp">
							<td>
								<div style="margin-top: 0px;">
									<a name="addArgumentLink"></a> <a class="add-icon-link" href="#addArgumentLink" onclick="addArgument('<fmt:message key="mediator.ejb.emptyerror"/>')"> <fmt:message key="mediator.ejb.addArgument" /> </a>
								</div>
							</td>
						</tr>
					</table></td>
			</tr>
		</table>
		<a name="nsEditorLink"></a>
		<div id="nsEditor" style="display: none;"></div>
	</div>
</fmt:bundle>