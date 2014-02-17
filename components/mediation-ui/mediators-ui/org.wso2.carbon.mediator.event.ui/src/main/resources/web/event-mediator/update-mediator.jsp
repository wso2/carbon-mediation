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

<%@ page import="org.wso2.carbon.mediator.event.ui.EventMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%
    try {
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof EventMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to update the mediator");
        }

        EventMediator eventMediator = (EventMediator) mediator;

        eventMediator.setTopic(null);
        eventMediator.setExpression(null);

        String keyGroup = request.getParameter("keygroup");
        XPathFactory xPathFactory = XPathFactory.getInstance();
        if (keyGroup != null) {
            if (keyGroup.equalsIgnoreCase("StaticKey")) {
                String value = request.getParameter("topicVal");
                if (value != null) {
                    eventMediator.setTopic(new Value(value));
                }
            } else {

                Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                        "topicVal", request.getParameter("topicVal"), session));
                eventMediator.setTopic(dynamicKey);
            }
        }

        String expression = request.getParameter("expression");
        if (expression != null) {
            eventMediator.setExpression(xPathFactory.createSynapseXPath(
                        "expression", request.getParameter("expression"), session));
        }
    } catch (Exception e) {
        session.setAttribute("sequence.error.message", e.getMessage());
        %>
        <script type="text/javascript">
            document.location.href = "../sequences/design_sequence.jsp?ordinal=1";
        </script>
        <%
    }
%>

