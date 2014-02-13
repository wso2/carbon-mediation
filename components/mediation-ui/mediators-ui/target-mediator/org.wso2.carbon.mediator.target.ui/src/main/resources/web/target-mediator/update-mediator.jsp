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

<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.llom.util.AXIOMUtil" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.EndpointFactory" %>
<%@ page import="org.apache.synapse.endpoints.Endpoint" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.target.TargetMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.Properties" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof TargetMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    TargetMediator targetMediator = (TargetMediator) mediator;

    //targetMediator.getList().clear();
    targetMediator.setEndpoint(null);
    targetMediator.setEndpointRef(null);
    targetMediator.setSequenceRef(null);
    targetMediator.setSoapAction(null);
    targetMediator.setToAddress(null);

    String soapAction = request.getParameter("mediator.target.soapaction");
    String toAddr = request.getParameter("mediator.target.toaddress");
    if (soapAction != null && !"".equals(soapAction)) {
        targetMediator.setSoapAction(soapAction);
    }
    if (toAddr != null && !"".equals(toAddr)) {
        targetMediator.setToAddress(toAddr);
    }

    String seqValueType = request.getParameter("mediator.target.seq.type");
    if(seqValueType.equals("none")){
        targetMediator.getList().clear();
        targetMediator.setSequenceRef(null);
    } else if(seqValueType.equals("anonymous")){
        targetMediator.setSequenceRef("anon");
    } else if(seqValueType.equals("pickFromRegistry")){
        targetMediator.getList().clear();
        targetMediator.setSequenceRef(request.getParameter("mediator.target.seq.reg"));
    }

    // sets endpoint information
    String option = request.getParameter("epOp");
    if ("none".equals(option)) {
        targetMediator.setEndpoint(null);
        //removes session attribute if it is present
        session.removeAttribute("anonEpXML");
    } else if ("anon".equals(option)) {
        String anonEpXML = (String)session.getAttribute("endpointXML");
         if (anonEpXML != null && !"".equals(anonEpXML) && !"Add".equals(request.getParameter("anonEpAction"))) {
             OMElement anonEpXMLElem = AXIOMUtil.stringToOM(anonEpXML);
             Endpoint endpoint = EndpointFactory.getEndpointFromElement(anonEpXMLElem, true,
                                                                        new Properties());
             targetMediator.setEndpoint(endpoint);
             //removes session attribute after using it
             session.removeAttribute("anonEpXML");
          }
    } else if ("registry".equals(option)) {
        String key = request.getParameter("registryKey");
//        Endpoint endpoint = client.getEndpoint(key);
//        targetMediator.setEndpoint(endpoint);
        targetMediator.setEndpointRef(key);
        //removes session attribute if it is present
        session.removeAttribute("anonEpXML");
         session.removeAttribute("endpointXML");
    }


%>

