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
<%@page import="org.apache.synapse.mediators.bean.BeanConstants"%>
<%@page import="java.util.Map.Entry"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.wso2.carbon.mediator.bean.BeanMediator"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page import="org.apache.synapse.mediators.Value"%>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath"%>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.apache.axiom.om.xpath.AXIOMXPath" %>

<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%
	Mediator mediator = SequenceEditorHelper.getEditingMediator(
	request, session);
	if (!(mediator instanceof BeanMediator)) {
		// todo : proper error handling
		throw new RuntimeException("Unable to edit the mediator");
	}
	BeanMediator ejbMediator = (BeanMediator) mediator;

	String clazz = "";
	if (ejbMediator.getClazz() != null) {
		clazz = ejbMediator.getClazz();
	}

	String action = "";
	if (ejbMediator.getAction() != null) {
		action = ejbMediator.getAction();
	}

	String var = "";
	if (ejbMediator.getVar() != null) {
		var = ejbMediator.getVar();
	}

	String property = "";
	if (ejbMediator.getProperty() != null) {
		property = ejbMediator.getProperty();

	}


	 NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
	
	 if(ejbMediator.getValue() != null && ejbMediator.getValue().getExpression() != null){
		 nameSpacesRegistrar.registerNameSpaces(ejbMediator.getValue().getExpression(),"beanValue",session);
	 }
	 
	 if(ejbMediator.getTarget() != null && ejbMediator.getTarget().getExpression() != null){
		 nameSpacesRegistrar.registerNameSpaces(ejbMediator.getTarget().getExpression(),"targetValue",session);
	 }
	 
	 Value beanValue = ejbMediator.getValue();
	 Value targetValue = ejbMediator.getTarget();

	
%>

<fmt:bundle basename="org.wso2.carbon.mediator.bean.ui.i18n.Resources">
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.bean.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="beani18n" />
	<div>
		<script type="text/javascript"
			src="../bean-mediator/js/mediator-util.js"></script>
		<table class="normal" width="100%">
			<tr>
				<td>
					<h2>
						<fmt:message key="mediator.bean.header" />
					</h2></td>
			</tr>
			<tr>
				<td>
					<table class="normal">
						<tr>
							<td><fmt:message key="mediator.bean.clazz" /><span
								class="required">*</span>
							</td>
							<td><input class="longInput" type="text"
								value="<%=clazz%>" name="clazz" id="clazz" />
							</td>
						</tr>
						<tr>
							<td><fmt:message key="mediator.bean.action" /> <span
								class="required">*</span>
							</td>
							<td>
							  <select name="beanAction" id="beanAction">	
							  	  <%if(action != null && action.equals("CREATE")){ %>
							  	  <option value="CREATE" selected="selected"><fmt:message key="mediator.bean.action.create" /></option>
							  	  <option value="REMOVE"><fmt:message key="mediator.bean.action.remove" /></option>
							  	  <option value="SET_PROPERTY"><fmt:message key="mediator.bean.action.set" /></option>
							  	  <option value="GET_PROPERTY"><fmt:message key="mediator.bean.action.get" /></option>
							  	  <%}else if(action != null && action.equals("REMOVE")){%>
							  	  <option value="CREATE"><fmt:message key="mediator.bean.action.create" /></option>
							  	  <option value="REMOVE" selected="selected"><fmt:message key="mediator.bean.action.remove" /></option>
							  	  <option value="SET_PROPERTY"><fmt:message key="mediator.bean.action.set" /></option>
							  	  <option value="GET_PROPERTY"><fmt:message key="mediator.bean.action.get" /></option>
							  	  <%}else if(action != null && action.equals("SET_PROPERTY")){ %>
							  	  <option value="CREATE"><fmt:message key="mediator.bean.action.create" /></option>
							  	  <option value="REMOVE"><fmt:message key="mediator.bean.action.remove" /></option>
							  	  <option value="SET_PROPERTY" selected="selected"><fmt:message key="mediator.bean.action.set" /></option>
							  	  <option value="GET_PROPERTY"><fmt:message key="mediator.bean.action.get" /></option>
							  	  <%}else if(action != null && action.equals("GET_PROPERTY")){ %>
							  	  <option value="CREATE"><fmt:message key="mediator.bean.action.create" /></option>
							  	  <option value="REMOVE"><fmt:message key="mediator.bean.action.remove" /></option>
							  	  <option value="SET_PROPERTY"><fmt:message key="mediator.bean.action.set" /></option>
							  	  <option value="GET_PROPERTY" selected="selected"><fmt:message key="mediator.bean.action.get" /></option>
							  	  <%}else{ %>
							  	    <option value="CREATE"><fmt:message key="mediator.bean.action.create" /></option>
							  	    <option value="REMOVE"><fmt:message key="mediator.bean.action.remove" /></option>
							  	    <option value="SET_PROPERTY"><fmt:message key="mediator.bean.action.set" /></option>
							  	    <option value="GET_PROPERTY"><fmt:message key="mediator.bean.action.get" /></option>
							  	  <%}%>
							  </select>
							</td>
						</tr>
						 <tr>
							<td><fmt:message key="mediator.bean.var" /><span
								class="required">*</span></td>
							<td><input class="longInput" type="text" value="<%=var%>"
								name="beanVar" id="beanVar" /></td>
						  </tr>
						   <tr>
							<td><fmt:message key="mediator.bean.property" /></td>
							<td><input class="longInput" type="text" value="<%=property%>"
								name="property" id="property" /></td>
						  </tr>			
						  <tr>
							<td>
								<fmt:message key="mediator.bean.value" />
							</td>
							<td style="padding-left: 0px ! important;">
							  <table cellpadding=0 cellspacing=0>
							    <tr>
							      <td>
							        <select name="beanValueType" id="beanValueType" onchange="onTypeSelectionChange('beanValueType','nsBeanTypeEditorButtonTD')" >
							  			<%if(beanValue != null && beanValue.getExpression() != null){ %>
							  			<option value="literal">Value</option>
										<option value="expression" selected="selected">Expression</option>
										<%}else if(beanValue != null && beanValue.getKeyValue() !=null){ %>
										<option value="literal" selected="selected">Value</option>
										<option value="expression">Expression</option>
										<%}else{ %>
										<option value="literal">Value</option>
										<option value="expression">Expression</option>
										<%}%>
									</select>
							      </td>
							      <td>
							         <%if(beanValue != null && beanValue.getExpression() != null){ %>
							      	 	<input class="smallInput" type="text" value="<%=beanValue.getExpression().toString()%>"  name="beanValue" id="id" />
							      	 <%}else if(beanValue != null && beanValue.getKeyValue() != null){%>
							      	 	<input class="smallInput" type="text" value="<%=beanValue.getKeyValue()%>"  name="beanValue" id="beanValue" />
							      	 <%}else{%>
							      	 	<input class="smallInput" type="text" value=""  name="beanValue" id="beanValue" />
							      	 <%}%>
							      	 
							      </td>
							      <td id="nsBeanTypeEditorButtonTD"  style="<%=(beanValue ==null || beanValue.getExpression() ==null)?"display:none;":""%>">
							    	<a href="#nsEditorLink"
												class="nseditor-icon-link" style="padding-left: 40px"
												onclick="showNameSpaceEditor('beanValue')"> <fmt:message
														key="mediator.bean.namespace" /></a>
							      </td>
							    </tr>
							  </table>
					    	</td>
						</tr>
						<tr>
							<td>
								<fmt:message key="mediator.bean.target" />
							</td>
							<td style="padding-left: 0px ! important;">
							  <table>
							    <tr>
							      <td>
							        <select name="targetValueType" id="targetValueType" onchange="onTypeSelectionChange('targetValueType','nsTargetTypeEditorButtonTD')" >
							  			<%if(targetValue != null && targetValue.getExpression() != null){ %>
							  			<option value="literal">Value</option>
										<option value="expression" selected="selected">Expression</option>
										<%}else if(targetValue != null && targetValue.getKeyValue() !=null){ %>
										<option value="literal" selected="selected">Value</option>
										<option value="expression">Expression</option>
										<%}else{ %>
										<option value="literal">Value</option>
										<option value="expression">Expression</option>
										<%}%>
									</select>
							      </td>
							      <td>
							         <%if(targetValue != null && targetValue.getExpression() != null){ %>
							      	 	<input class="smallInput" type="text" value="<%=targetValue.getExpression().toString()%>"  name="targetValue" id="targetValue" />
							      	 <%}else if(targetValue != null && targetValue.getKeyValue() != null){%>
							      	 	<input class="smallInput" type="text" value="<%=targetValue.getKeyValue()%>"  name="targetValue" id="targetValue" />
							      	 <%}else{%>
							      	 	<input class="smallInput" type="text" value=""  name="targetValue" id="targetValue" />
							      	 <%}%>
							      	 
							      </td>
							      <td id="nsTargetTypeEditorButtonTD"  style="<%=(targetValue ==null || targetValue.getExpression() ==null)?"display:none;":""%>">
							    	<a href="#nsEditorLink"
												class="nseditor-icon-link" style="padding-left: 40px"
												onclick="showNameSpaceEditor('targetValue')"> <fmt:message
														key="mediator.bean.namespace" /></a>
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
		<div id="nsEditor" style="display: none;"></div>
	</div>
</fmt:bundle>