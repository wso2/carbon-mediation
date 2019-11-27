<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%-- 
 ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~ 
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~ 
 ~ http://www.apache.org/licenses/LICENSE-2.0
 ~ 
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied. See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
--%>

<%@ page import="org.wso2.carbon.mediator.foreach.ForEachMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.SynapseJsonPathFactory" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%
    final String JSON_EVAL_START_STRING = "json-eval(";
    final String ITR_EXPRESSION_KEY = "itr_expression";
    
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    if (!(mediator instanceof ForEachMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    ForEachMediator foreachMediator = (ForEachMediator) mediator;

    XPathFactory xPathFactory = XPathFactory.getInstance();
    SynapseJsonPathFactory jsonPathFactory = SynapseJsonPathFactory.getInstance();
    
    String iterateExpression = request.getParameter(ITR_EXPRESSION_KEY);
    if (iterateExpression.startsWith(JSON_EVAL_START_STRING)) {
        foreachMediator.setExpression(
                jsonPathFactory.createSynapseJsonPath(ITR_EXPRESSION_KEY, iterateExpression.trim().substring(
                                JSON_EVAL_START_STRING.length(), iterateExpression.length() - 1)));
    } else {
        foreachMediator.setExpression(xPathFactory.createSynapseXPath(ITR_EXPRESSION_KEY, iterateExpression, session));
    }

    foreachMediator.setSequenceRef(null);

    String seqValueType = request.getParameter("mediator.foreach.seq.type");
    if (seqValueType.equals("anon")) {
        foreachMediator.setSequenceRef(null);
    } else if (seqValueType.equals("pickFromRegistry")) {
        foreachMediator.getList().clear();
        foreachMediator.setSequenceRef(request.getParameter("mediator.foreach.seq.reg"));
    }

    if (request.getParameter("mediator.foreach.id") != null &&
             !request.getParameter("mediator.foreach.id").trim().equals("")) {
        foreachMediator.setId(request.getParameter("mediator.foreach.id"));
    }

%>

