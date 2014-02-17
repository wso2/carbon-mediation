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


<%@ page import="org.wso2.carbon.mediator.conditionalrouter.ConditionalRouterMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    if (!(mediator instanceof ConditionalRouterMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }

    ConditionalRouterMediator conditionalRouterMediator = (ConditionalRouterMediator) mediator;
    String continueAfter = request.getParameter("mediator.conditionalrouter.continue");
    if (continueAfter != null && continueAfter.equalsIgnoreCase("true")) {
        conditionalRouterMediator.setContinueAfter(true);
    } else {
        conditionalRouterMediator.setContinueAfter(false);
    }
    /*if (!(mediator instanceof RouterMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    RouterMediator routerMediator = (RouterMediator) mediator;

    String continueAfter = request.getParameter("mediator.router.continue");
    if (continueAfter != null && "true".equalsIgnoreCase(continueAfter)) {
        routerMediator.setContinueAfter(true);
    } else {
        routerMediator.setContinueAfter(false);
    }*/
%>
