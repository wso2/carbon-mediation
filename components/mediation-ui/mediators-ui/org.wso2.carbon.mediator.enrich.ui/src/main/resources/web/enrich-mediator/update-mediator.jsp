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

<%@ page import="org.wso2.carbon.mediator.enrich.ui.EnrichMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.SynapseJsonPathFactory" %>
<%@ page import="org.wso2.carbon.mediator.enrich.ui.EnrichUIConstants" %>


<%

    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof EnrichMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    EnrichMediator enrichMediator = (org.wso2.carbon.mediator.enrich.ui.EnrichMediator) mediator;

    // Source
    enrichMediator.setSourceClone(request.getParameter(EnrichUIConstants.SOURCE_CLONE));
    enrichMediator.setSourceType(request.getParameter(EnrichUIConstants.SOURCE_TYPE));

    if (request.getParameter(EnrichUIConstants.SOURCE_EXPRESSION_ID) != null &&
            !request.getParameter(EnrichUIConstants.SOURCE_EXPRESSION_ID).trim().equals("")) {

        if (request.getParameter(EnrichUIConstants.SOURCE_TYPE).equals("custom")) {
            String expression = request.getParameter(EnrichUIConstants.SOURCE_EXPRESSION_ID);
            if (expression.startsWith(EnrichUIConstants.JSON_PATH)) {
                int expLength = expression.length();
                String extractedExp = expression.substring(EnrichUIConstants.JSON_PATH.length(), expLength - 1);
                SynapseJsonPathFactory synapseJsonPathFactory = SynapseJsonPathFactory.getInstance();
                enrichMediator.setSourceExpression(synapseJsonPathFactory.createSynapseJsonPath(
                        EnrichUIConstants.TARGET_EXPRESSION_ID, extractedExp));
            } else {
                XPathFactory xPathFactory = XPathFactory.getInstance();
                enrichMediator.setSourceExpression(xPathFactory.createSynapseXPath(EnrichUIConstants.SOURCE_EXPRESSION_ID,
                        expression, session));
            }
            enrichMediator.setSourceProperty(null);
        } else if (request.getParameter(EnrichUIConstants.SOURCE_TYPE).equals("property")) {
            enrichMediator.setSourceType(request.getParameter(EnrichUIConstants.SOURCE_TYPE));
            enrichMediator.setSourceProperty(request.getParameter(EnrichUIConstants.SOURCE_EXPRESSION_ID));
            enrichMediator.setSourceExpression(null);
        }
    }

    String keyGroup = request.getParameter("keygroup");
    String keyVal = "";
    if (request.getParameter(EnrichUIConstants.SOURCE_TYPE).equals("inline")) {
        if (keyGroup != null && !keyGroup.equals("")) {
            if (keyGroup.equals("InlineXML")) {
                keyVal = request.getParameter("inlineEnrichText");
                enrichMediator.setSourceInlineElement(keyVal);
                enrichMediator.setInlineSourceRegKey("");
            } else if (keyGroup.equals("InlineRegKey")) {
                keyVal = request.getParameter("mediator.enrich.reg.key");
                enrichMediator.setInlineSourceRegKey(keyVal);
                enrichMediator.setSourceInlineElement("");
            }
        }
        enrichMediator.setSourceExpression(null);
        enrichMediator.setSourceProperty(null);
    }
            
    // Target
    enrichMediator.setTargetAction(request.getParameter(EnrichUIConstants.TARGET_ACTION));
    enrichMediator.setTargetType(request.getParameter(EnrichUIConstants.TARGET_TYPE));


    if (request.getParameter(EnrichUIConstants.TARGET_EXPRESSION_ID) != null &&
            !request.getParameter(EnrichUIConstants.TARGET_EXPRESSION_ID).trim().equals("")) {
        if (request.getParameter(EnrichUIConstants.TARGET_TYPE).equals("custom")) {
            String expression = request.getParameter(EnrichUIConstants.TARGET_EXPRESSION_ID);
            if (expression.startsWith(EnrichUIConstants.JSON_PATH)) {
                int expLength = expression.length();
                String extractedExp = expression.substring(EnrichUIConstants.JSON_PATH.length(), expLength - 1);
                SynapseJsonPathFactory synapseJsonPathFactory = SynapseJsonPathFactory.getInstance();
                enrichMediator.setTargetExpression(synapseJsonPathFactory.createSynapseJsonPath(
                        EnrichUIConstants.TARGET_EXPRESSION_ID, extractedExp));
            } else {
                XPathFactory xPathFactory2 = XPathFactory.getInstance();
                enrichMediator.setTargetExpression(xPathFactory2.createSynapseXPath(EnrichUIConstants.TARGET_EXPRESSION_ID,
                        expression, session));
            }
            enrichMediator.setTargetProperty(null);
        } else if (request.getParameter(EnrichUIConstants.TARGET_TYPE).equals("property")) {
            enrichMediator.setTargetProperty(request.getParameter(EnrichUIConstants.TARGET_EXPRESSION_ID));
            enrichMediator.setTargetExpression(null);
        }
    }
%>
