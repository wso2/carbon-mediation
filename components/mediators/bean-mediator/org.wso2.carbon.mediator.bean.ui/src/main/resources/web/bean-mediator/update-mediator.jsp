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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.bean.BeanMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%!public boolean nullChecker(String strChecker) {

//     if (strChecker == null) {
//         return false;
//     } else if (strChecker != null && (!(strChecker.equalsIgnoreCase("")))) {
//         return false;
//     } else {
//         return true;
//     }
    if(strChecker == null){
    	return false;
    }else if(strChecker != null && strChecker.equalsIgnoreCase("")){
    	return false;
    }else{
    	return true;
    }
    //return false;
}%>

<%
	Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
	XPathFactory xPathFactory = XPathFactory.getInstance();
	
    if (!(mediator instanceof BeanMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    BeanMediator ejbMediator = (BeanMediator) mediator;
    String clazz = request.getParameter("clazz");
    if(nullChecker(clazz)) {
    	ejbMediator.setClazz(clazz);
    }

    String action = request.getParameter("beanAction");
    if(nullChecker(action)) {
    	ejbMediator.setAction(action);
    }
    
    String var = request.getParameter("beanVar");
    if(nullChecker(var)) {
    	ejbMediator.setVar(var);
    }
    
    String property = request.getParameter("property");
    if(nullChecker(property)) {
    	ejbMediator.setProperty(property);
    }
    
    
    
	String beanTypeExpression = request.getParameter("beanValueType");
	boolean isBeanExpression = beanTypeExpression != null
	&& "expression".equals(beanTypeExpression.trim());
	String beanVal = request.getParameter("beanValue");
	if (nullChecker(beanVal)) {
		Value beanTypeVal = null;
		if (isBeanExpression) {
		beanTypeVal = new Value(xPathFactory.createSynapseXPath(
			"beanValue", beanVal.trim(), session));
		} else if (nullChecker(beanVal)) {
		beanTypeVal = new Value(beanVal);
		}
		ejbMediator.setValue(beanTypeVal);
	}

	

	String targetTypeExpression = request
			.getParameter("targetValueType");
	boolean isTaregetExpression = targetTypeExpression != null
			&& "expression".equals(targetTypeExpression.trim());
	Value targetTypeVal = null;
	String targetVal = request.getParameter("targetValue");
	if (nullChecker(targetVal)) {
		if (isTaregetExpression) {
			targetTypeVal = new Value(xPathFactory.createSynapseXPath(
					"targetValue", targetVal.trim(), session));
		} else if (nullChecker(targetVal)) {
			targetTypeVal = new Value(targetVal);
		}
		ejbMediator.setTarget(targetTypeVal);
	}
	
%>

