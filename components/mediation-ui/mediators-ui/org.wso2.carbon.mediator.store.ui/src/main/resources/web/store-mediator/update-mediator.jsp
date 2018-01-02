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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.store.MessageStoreMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePathFactory" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%! public boolean nullChecker(String strChecker) {

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
    if (!(mediator instanceof MessageStoreMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    MessageStoreMediator storeMediator = (MessageStoreMediator) mediator;
    String msName = "";
    String msExp = "";
    XPathFactory xPathFactory = XPathFactory.getInstance();
    String specifyAs = request.getParameter("specifyAs");
    if (specifyAs != null && !specifyAs.equals("")) {
        if (specifyAs.equals("Expression")) {
            msExp = request.getParameter("mediator.store.xpath");
            if (nullChecker(msExp)) {
                storeMediator.setMessageStoreExp(xPathFactory.createSynapseXPath("mediator.store.xpath", msExp, session));
            }

        } else if (specifyAs.equals("Value")) {
            msName = request.getParameter("MessageStore");
            if (nullChecker(msName)) {
                storeMediator.setMessageStoreName(msName);
                storeMediator.setMessageStoreExp(null);
            }
        }
    }
    String seqName = request.getParameter("onStoreSequence");

    if(nullChecker(seqName)) {
        storeMediator.setSequence(seqName);
    }

%>

