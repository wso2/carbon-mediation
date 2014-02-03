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

<%@ page import="org.wso2.carbon.mediator.rmsequence.RMSequenceMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof RMSequenceMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    RMSequenceMediator rmsequenceMediator = (RMSequenceMediator) mediator;
    rmsequenceMediator.setVersion(request.getParameter("version"));
    if ("single".equals(request.getParameter("messageType"))) {
        rmsequenceMediator.setSingle(true);
        rmsequenceMediator.setCorrelation(null);
        rmsequenceMediator.setLastMessage(null);
    } else {
        XPathFactory xPathFactory = XPathFactory.getInstance();
        rmsequenceMediator.setCorrelation(xPathFactory.createSynapseXPath("correlation", request.getParameter("correlation"), session));
        if (request.getParameter("last-message") != null) {
            rmsequenceMediator.setLastMessage(xPathFactory.createSynapseXPath("last-message", request.getParameter("last-message"), session));
        }
        rmsequenceMediator.setSingle(false);
    }
%>

