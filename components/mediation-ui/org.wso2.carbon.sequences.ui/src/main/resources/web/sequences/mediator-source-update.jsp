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
<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.common.SequenceEditorException" %>

<%
    String mediatorSource = request.getParameter("mediatorSrc");
    Mediator newMediator = null;
    try {
        newMediator = SequenceEditorHelper.parseStringToMediator(mediatorSource);
    } catch (SequenceEditorException ignored) {
       // TODO 
    }
    session.setAttribute("editingMediator", newMediator);
    SequenceMediator editingSequence = SequenceEditorHelper.getEditingSequence(session);
    String editingMediatorPosition
            = SequenceEditorHelper.getEditingMediatorPosition(session).substring(9);
    SequenceEditorHelper.removeMediatorAt(editingSequence, editingMediatorPosition);
    SequenceEditorHelper.insertMediator(editingSequence, newMediator, editingMediatorPosition, 0);
%>

<% if (SequenceEditorHelper.isRedirected(request)) { %>
<script type="text/javascript">
    document.location.href = "design_sequence.jsp";
</script>
<% } %>
