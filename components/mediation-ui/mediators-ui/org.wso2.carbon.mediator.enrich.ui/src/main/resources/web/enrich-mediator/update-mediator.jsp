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


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof EnrichMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    EnrichMediator enrichMediator = (org.wso2.carbon.mediator.enrich.ui.EnrichMediator) mediator;

    // Source
    enrichMediator.setSourceClone(request.getParameter("mediator.enrich.source.clone"));
    enrichMediator.setSourceType(request.getParameter("mediator.enrich.source.type2"));

    if (request.getParameter("mediator.enrich.source.val_ex") != null && !request.getParameter("mediator.enrich.source.val_ex").trim().equals("")) {

        if (request.getParameter("mediator.enrich.source.type2").equals("custom")) {
            String expression = request.getParameter("mediator.enrich.source.val_ex");
            if (expression.startsWith("json-eval(")) {
                int expLength = expression.length();
                String extractedExp = expression.substring(10, expLength - 1);
                SynapseJsonPathFactory synapseJsonPathFactory = SynapseJsonPathFactory.getInstance();
                enrichMediator.setSourceExpression(synapseJsonPathFactory.createSynapseJsonPath(
                        "mediator.enrich.target.val_ex", extractedExp));
            } else {
                XPathFactory xPathFactory = XPathFactory.getInstance();
                enrichMediator.setSourceExpression(xPathFactory.createSynapseXPath("mediator.enrich.source.val_ex",
                        expression, session));
            }
            enrichMediator.setTargetProperty(null);
        } else if (request.getParameter("mediator.enrich.source.type2").equals("property")) {
            enrichMediator.setSourceType(request.getParameter("mediator.enrich.source.type2"));
            enrichMediator.setSourceProperty(request.getParameter("mediator.enrich.source.val_ex"));
            enrichMediator.setSourceExpression(null);
        }
    }

    String keyGroup = request.getParameter("keygroup");
    String keyVal = "";
    if (request.getParameter("mediator.enrich.source.type2").equals("inline")) {
        if (keyGroup != null && !keyGroup.equals("")) {
            if (keyGroup.equals("InlineXML")) {
                keyVal = request.getParameter("inlineEnrichText");
                enrichMediator.setSourceInlineXML(keyVal);
                enrichMediator.setInlineSourceRegKey("");
            } else if (keyGroup.equals("InlineRegKey")) {
                keyVal = request.getParameter("mediator.enrich.reg.key");
                enrichMediator.setInlineSourceRegKey(keyVal);
                enrichMediator.setSourceInlineXML("");
            }
        }
    }
            
    // Target
    enrichMediator.setTargetAction(request.getParameter("mediator.enrich.target.action"));
    enrichMediator.setTargetType(request.getParameter("mediator.enrich.target.type"));


    if (request.getParameter("mediator.enrich.target.val_ex") != null && !request.getParameter("mediator.enrich.target.val_ex").trim().equals("")) {
        if (request.getParameter("mediator.enrich.target.type").equals("custom")) {
            String expression = request.getParameter("mediator.enrich.target.val_ex");
            if (expression.startsWith("json-eval(")) {
                int expLength = expression.length();
                String extractedExp = expression.substring(10, expLength - 1);
                SynapseJsonPathFactory synapseJsonPathFactory = SynapseJsonPathFactory.getInstance();
                enrichMediator.setTargetExpression(synapseJsonPathFactory.createSynapseJsonPath(
                        "mediator.enrich.target.val_ex", extractedExp));
            } else {
                XPathFactory xPathFactory2 = XPathFactory.getInstance();
                enrichMediator.setTargetExpression(xPathFactory2.createSynapseXPath("mediator.enrich.target.val_ex",
                        expression, session));
            }
            enrichMediator.setTargetProperty(null);
        } else if (request.getParameter("mediator.enrich.target.type").equals("property")) {
            enrichMediator.setTargetProperty(request.getParameter("mediator.enrich.target.val_ex"));
            enrichMediator.setTargetExpression(null);
        }       
    }



%>

