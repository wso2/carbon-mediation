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
<%@ page import="org.wso2.carbon.mediator.target.TargetMediator" %>

<%
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
    iterateMediator.setExpression(xPathFactory.createSynapseXPath("itr_expression", request, session));
    iterateMediator.setAttachPath(xPathFactory.createSynapseXPath("attach_path", request, session));

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

