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

<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.mediators.Value" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SequenceMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SequenceMediator sequenceMediator = (SequenceMediator) mediator;

    sequenceMediator.setKey(null);
    String keyVal;
    String keyExp;
    XPathFactory xPathFactory = XPathFactory.getInstance();

    String keyGroup = request.getParameter("keygroup");
    if (keyGroup != null && !keyGroup.equals("")) {
        if (keyGroup.equals("StaticKey")) {
            keyVal = request.getParameter("mediator.sequence.key.static_val");
            if (keyVal != null && !keyVal.equals("")) {
                Value staticKey = new Value(keyVal);
                sequenceMediator.setKey(staticKey);
            }
        } else if (keyGroup.equals("DynamicKey")) {
            keyExp = request.getParameter("mediator.sequence.key.dynamic_val");


            if (keyExp != null && !keyExp.equals("")) {
                Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                        "mediator.sequence.key.dynamic_val", request.getParameter(
                                "mediator.sequence.key.dynamic_val"), session));
                sequenceMediator.setKey(dynamicKey);
            }
        }
    }


%>

