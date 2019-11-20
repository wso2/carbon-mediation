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
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.SynapseJsonPathFactory" %>
<%@ page import="org.wso2.carbon.mediator.target.TargetMediator" %>
<%@ page import="org.apache.synapse.config.xml.SynapseXPathFactory" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%
    final String JSON_EVAL_START_STRING = "json-eval(";
    final String ITR_EXPRESSION_KEY = "itr_expression";
    final String ATTACH_PTH_KEY = "attach_path";

    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof IterateMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    IterateMediator iterateMediator = (IterateMediator) mediator;
    String continueParent =  request.getParameter("continueParent");
    String preservePayload = request.getParameter("preservePayload");
    String sequentialMed = request.getParameter("sequentialMed");

    XPathFactory xPathFactory = XPathFactory.getInstance();
    SynapseJsonPathFactory jsonPathFactory = SynapseJsonPathFactory.getInstance();

    String iterateExpression = request.getParameter(ITR_EXPRESSION_KEY);
    if (iterateExpression.startsWith(JSON_EVAL_START_STRING)) {
        iterateMediator.setExpression(
                jsonPathFactory.createSynapseJsonPath(ITR_EXPRESSION_KEY,
                        iterateExpression.trim().substring(
                                JSON_EVAL_START_STRING.length(), iterateExpression.length() - 1)));
    } else {
        iterateMediator.setExpression(xPathFactory.createSynapseXPath(ITR_EXPRESSION_KEY, iterateExpression, session));
    }

    String attachPathExpression = request.getParameter(ATTACH_PTH_KEY);
    if (attachPathExpression == null || StringUtils.isEmpty(attachPathExpression)) {
        iterateMediator.setAttachPathPresent(false);
    } else {
        iterateMediator.setAttachPathPresent(true);
        if (attachPathExpression.startsWith(JSON_EVAL_START_STRING)) {
            iterateMediator.setAttachPath(
                    jsonPathFactory.createSynapseJsonPath(ATTACH_PTH_KEY, attachPathExpression.trim().substring(
                            JSON_EVAL_START_STRING.length(), attachPathExpression.length() - 1)));
        } else {
            iterateMediator.setAttachPath(xPathFactory.createSynapseXPath(ATTACH_PTH_KEY, request, session));
        }
    }

    boolean cont_parent = Boolean.parseBoolean(continueParent);
    iterateMediator.setContinueParent(cont_parent);
    boolean pres_payload = Boolean.parseBoolean(preservePayload);
    iterateMediator.setPreservePayload(pres_payload);

    boolean seq_med=Boolean.parseBoolean(sequentialMed);
    iterateMediator.setSequential(seq_med);
    
    if (iterateMediator.getList().size() == 0) {
        iterateMediator.addChild(new TargetMediator());
    }
    
    if (request.getParameter("mediator.iterate.id") != null && 
  				  !request.getParameter("mediator.iterate.id").trim().
						equals("")) {
    		iterateMediator.setId(request.getParameter("mediator.iterate.id"));
	  }   
      
%>

