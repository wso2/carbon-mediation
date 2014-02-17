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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.mediator.ejb.EJBMediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page import="org.apache.synapse.mediators.Value"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory"%>

<%!public boolean nullChecker(String strChecker) {

    if (strChecker == null) {
        return false;
    } else if (strChecker != null) {
        return true;
    } else if ((!(strChecker.equalsIgnoreCase("")))) {
        return true;
    }
    return false;
}%>

<%
	Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
	XPathFactory xPathFactory = XPathFactory.getInstance();
	
    if (!(mediator instanceof EJBMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    EJBMediator ejbMediator = (EJBMediator) mediator;
    String clazz = request.getParameter("clazz");
    if(nullChecker(clazz)) {
    	ejbMediator.setClazz(clazz);
    }

    String beanstalk = request.getParameter("beanstalk");
    if(nullChecker(beanstalk)) {
    	ejbMediator.setBeanstalk(beanstalk);
    }
    
    String method = request.getParameter("ejbMethod");
    if(nullChecker(method)) {
    	ejbMediator.setMethod(method);
    }
    
    String target = request.getParameter("target");
    if(nullChecker(target)) {
    	ejbMediator.setTarget(target);
    }
    
    String jndiName = request.getParameter("jndiName");
    if(nullChecker(jndiName)) {
    	ejbMediator.setJndiName(jndiName);
    }

    String beanTypeExpression = request.getParameter("beanIdType");
    boolean isBeanExpression = beanTypeExpression != null && "expression".equals(beanTypeExpression.trim());
    Value beanTypeVal =null;
    String beanVal = request.getParameter("id");
    if(isBeanExpression){
    	 beanTypeVal = new Value(xPathFactory.createSynapseXPath("beanIdType", beanVal.trim(), session));
     }else{
    	 beanTypeVal = new Value(beanVal);
    }
    ejbMediator.setId(beanTypeVal);
    
    String stateful = request.getParameter("stateful");
    if(nullChecker(stateful)) {
    	ejbMediator.setStateful(Boolean.valueOf(stateful));
    }
    
    
    String remove = request.getParameter("remove");
    if(nullChecker(remove)) {
    	ejbMediator.setRemove(Boolean.valueOf(remove));
    }
    
//    mapping arguments inputs
 	 
	 ejbMediator.getArguments().clear();
     String argumentCountParameter = request.getParameter("argumentCount");
     if (argumentCountParameter != null && !"".equals(argumentCountParameter)) {
         int argumentCount = 0;
         try {
         	argumentCount = Integer.parseInt(argumentCountParameter.trim());
              for (int i = 0; i <= argumentCount; i++) {
                  String name = request.getParameter("argumentValue" + i);
                  if (name != null && !"".equals(name)) {
                      String valueId = "argumentValue" + i;
                      String value = request.getParameter(valueId);
                      String expression = request.getParameter("propertyTypeSelection" + i);
                      boolean isExpression = expression != null && "expression".equals(expression.trim());
                      Value mp =null;
		      		  if (value != null) {
                         if (isExpression) {
                              mp = new Value(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                         }else{
                        	 mp = new Value(value);
                         }
                     }
                     ejbMediator.addArguments(mp);
                 }
             }
         } catch (NumberFormatException ignored) {
               throw new RuntimeException("Invalid number format");
         }
      }
%>

