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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.throttle.OnAcceptMediator" %>
<%@ page import="org.wso2.carbon.mediator.throttle.OnRejectMediator" %>
<%@ page import="org.wso2.carbon.mediator.throttle.ThrottleMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="java.util.Map" %>

<%
    try {
    String param = null;
    boolean isInline = false;
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ThrottleMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ThrottleMediator throttleMediator = (ThrottleMediator) mediator;

    param = request.getParameter("throttle_id1");
    if (param != null && !param.equals("")) {
        throttleMediator.setId(param);
    }
    throttleMediator.setPolicyKey(null);
    throttleMediator.setInLinePolicy(null);
    param = request.getParameter("policygroup");
    if (param != null && !param.equals("")) {
        if (param.equals("InLinePolicy")) {
            String policyID = SequenceEditorHelper.getEditingMediatorPosition(session) + "_throttle_policy";
            Map policyXMLMap = (Map) request.getSession().getAttribute("throttle_policy_map");
            if (policyXMLMap != null) {
                String policyXML = (String) policyXMLMap.get(policyID);
                if (policyXML != null && !"".equals(policyXML)) {
                    try {
                        OMElement ele = AXIOMUtil.stringToOM(policyXML);
                        throttleMediator.setInLinePolicy(ele);
                    } catch (XMLStreamException e) {
                        throw new RuntimeException("Invalid Policy File");
                    }
                }
            }
        } else if (param.equals("PolicyKey")) {
            String key = request.getParameter("mediator.throttle.regPolicy");
            if (key != null && !key.equals("")) {
                throttleMediator.setPolicyKey(key);
            }
        }
    }

    param = request.getParameter("onacceptgroup");
    if (param != null && !param.equals("")) {
        if (param.equals("onAcceptSequenceKey")) {
            String key = request.getParameter("mediator.throttle.acceptKey");
            System.out.println(key);
            throttleMediator.setOnAcceptSeqKey(key);
            for (int i = 0; i < throttleMediator.getList().size(); i++) {
                Mediator m = throttleMediator.getChild(i);
                if (m instanceof OnAcceptMediator) {
                    throttleMediator.removeChild(i);
                }
            }
        } else if (param.equals("onAcceptSequence")) {
            boolean onAcceptPresent = false;
            throttleMediator.setOnAcceptSeqKey(null);
            for (int i = 0; i < throttleMediator.getList().size(); i++) {
                Mediator m = throttleMediator.getChild(i);
                if (m instanceof OnAcceptMediator) {
                    onAcceptPresent = true;
                }
            }
            if (!onAcceptPresent) {
                throttleMediator.addChild(new OnAcceptMediator());
            }
        }
    }

    param = request.getParameter("onrejectgroup");
    if (param != null && !param.equals("")) {
        if (param.equals("onRejectSequenceKey")) {
            String key = request.getParameter("mediator.throttle.rejectKey");
            throttleMediator.setOnRejectSeqKey(key);
            for (int i = 0; i < throttleMediator.getList().size(); i++) {
                Mediator m = throttleMediator.getChild(i);
                if (m instanceof OnRejectMediator) {
                    throttleMediator.removeChild(i);
                }
            }
        } else if (param.equals("onRejectSequence")) {
            boolean onRejectPresent = false;
            throttleMediator.setOnRejectSeqKey(null);
            for (int i = 0; i < throttleMediator.getList().size(); i++) {
                Mediator m = throttleMediator.getChild(i);
                if (m instanceof OnRejectMediator) {
                    onRejectPresent = true;
                }
            }
            if (!onRejectPresent) {
                throttleMediator.addChild(new OnRejectMediator());
            }
        }
    }

    } catch (Exception e) {
        %>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>");
</script>
<%
    }
%>


