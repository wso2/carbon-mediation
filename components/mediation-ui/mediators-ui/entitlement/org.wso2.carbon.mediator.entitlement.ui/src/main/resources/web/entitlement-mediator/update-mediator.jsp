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
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<%
    try {       
        String remoteServiceUserName = null;
		String remoteServicePassword = null;
		String remoteServiceUrl = null;
        String callbackClass = null;
        String thriftHost = null;
        String thriftPort = null;
        String client = null;
        String param = null;

		remoteServiceUserName = request.getParameter("remoteServiceUserName");
		remoteServicePassword = request.getParameter("remoteServicePassword");
		remoteServiceUrl = request.getParameter("remoteServiceUrl");
        callbackClass = request.getParameter("callbackClass");
        thriftHost = request.getParameter("thriftHost");
        thriftPort = request.getParameter("thriftPort");
        client = request.getParameter("client");
		 
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof EntitlementMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to update the mediator");
        }
        EntitlementMediator entMediator = (EntitlementMediator) mediator;
        entMediator.setRemoteServiceUrl(remoteServiceUrl);
        entMediator.setRemoteServiceUserName(remoteServiceUserName);
        entMediator.setRemoteServicePassword(remoteServicePassword);
        entMediator.setCallbackClass(callbackClass);
        entMediator.setThriftHost(thriftHost);
        entMediator.setThriftPort(thriftPort);
        entMediator.setClient(client);

        param = request.getParameter("onacceptgroup");
        if (param != null && !param.equals("")) {
            if (param.equals("onAcceptSequenceKey")) {
                String key = request.getParameter("mediator.entitlement.acceptKey");
                entMediator.setOnAcceptSeqKey(key);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof OnAcceptMediator) {
                        entMediator.removeChild(i);
                    }
                }
            } else if (param.equals("onAcceptSequence")) {
                boolean onAcceptPresent = false;
                entMediator.setOnAcceptSeqKey(null);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof OnAcceptMediator) {
                        onAcceptPresent = true;
                    }
                }
                if (!onAcceptPresent) {
                    entMediator.addChild(new OnAcceptMediator());
                }
            }
        }

        param = request.getParameter("onrejectgroup");
        if (param != null && !param.equals("")) {
            if (param.equals("onRejectSequenceKey")) {
                String key = request.getParameter("mediator.entitlement.rejectKey");
                entMediator.setOnRejectSeqKey(key);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof OnRejectMediator) {
                        entMediator.removeChild(i);
                    }
                }
            } else if (param.equals("onRejectSequence")) {
                boolean onRejectPresent = false;
                entMediator.setOnRejectSeqKey(null);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof OnRejectMediator) {
                        onRejectPresent = true;
                    }
                }
                if (!onRejectPresent) {
                    entMediator.addChild(new OnRejectMediator());
                }
            }
        }

        param = request.getParameter("obligationsgroup");
        if (param != null && !param.equals("")) {
            if (param.equals("obligationsSequenceKey")) {
                String key = request.getParameter("mediator.entitlement.obligationsKey");
                entMediator.setObligationsSeqKey(key);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof ObligationsMediator) {
                        entMediator.removeChild(i);
                    }
                }
            } else if (param.equals("obligationsSequence")) {
                boolean obligationsPresent = false;
                entMediator.setObligationsSeqKey(null);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof ObligationsMediator) {
                        obligationsPresent = true;
                    }
                }
                if (!obligationsPresent) {
                    entMediator.addChild(new ObligationsMediator());
                }
            }
        }

        param = request.getParameter("advicegroup");
        if (param != null && !param.equals("")) {
            if (param.equals("adviceSequenceKey")) {
                String key = request.getParameter("mediator.entitlement.adviceKey");
                entMediator.setAdviceSeqKey(key);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof AdviceMediator) {
                        entMediator.removeChild(i);
                    }
                }
            } else if (param.equals("adviceSequence")) {
                boolean advicePresent = false;
                entMediator.setAdviceSeqKey(null);
                for (int i = 0; i < entMediator.getList().size(); i++) {
                    Mediator m = entMediator.getChild(i);
                    if (m instanceof AdviceMediator) {
                        advicePresent = true;
                    }
                }
                if (!advicePresent) {
                    entMediator.addChild(new AdviceMediator());
                }
            }
        }

    } catch (Exception e) {
        session.setAttribute("sequence.error.message", e.getMessage());
%>
        
<%@page import="org.wso2.carbon.mediator.entitlement.EntitlementMediator"%>
<%@ page import="org.wso2.carbon.mediator.entitlement.OnAcceptMediator" %>
<%@ page import="org.wso2.carbon.mediator.entitlement.OnRejectMediator" %>
<%@ page import="org.wso2.carbon.mediator.entitlement.ObligationsMediator" %>
<%@ page import="org.wso2.carbon.mediator.entitlement.AdviceMediator" %>
<script type="text/javascript">
            document.location.href = "../sequences/design_sequence.jsp?ordinal=1";
        </script>
        <%
    }
%>


