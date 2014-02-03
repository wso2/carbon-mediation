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
<%@ page import="org.wso2.carbon.sequences.common.SequenceEditorException" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.sequences.ui.SequenceEditorConstants" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepositoryRegistrar" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.EditorUIClient" %>


<%
    if (session.getAttribute("sequence.error.message") != null) {
%>
<script type="text/javascript">
    document.location.href = "design_sequence.jsp?ordinal=1";
</script>
<%
    } else {

        ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.sequences.ui.i18n.Resources", request.getLocale());

        String forwardTo = /*"list_sequences.jsp"*/ SequenceEditorHelper.getForwardToFrom(session) + "?tab=1&";
        String errorPage = "design_sequence.jsp?ordinal=1";
        try {
            String sequenceSource = request.getParameter("sequenceXML");
            String sequenceName = "";
            String givenName;
            String sequenceKey = "";
            String registry = request.getParameter("registry");
            if("conf".equals(registry)) {
               sequenceKey = "conf:" + request.getParameter("regKey");
            } else if("gov".equals(registry)) {
                sequenceKey = "gov:" + request.getParameter("regKey");
            }
            String onErrorKey = request.getParameter("onErrorKey");

            String seqDescription = request.getParameter("seqDescription");
            boolean updateOnSynapseRegistry = false;
            if ("true".equals(request.getParameter("updateSynapseRegistry"))) {
                updateOnSynapseRegistry = true;
            }

            //set name of sequence as registry path of the sequence
            sequenceName = sequenceKey;
            if (sequenceKey == null || "".equals(sequenceKey)) {
                session.setAttribute("sequence.warn.message", "Registry key value must not be null or empty");
            %>
                <script type="text/javascript">
                    document.location.href = '<%= errorPage %>';
                </script>
            <%
                return;
            }

            if (sequenceSource != null) {                
                errorPage = "source_sequence.jsp?ordinal=1&sequenceName=" + SequenceEditorHelper.getEditingSequence(session).getName();
                try {
                    session.setAttribute("editingSequence",
                            SequenceEditorHelper.parseStringToSequence(sequenceSource.trim()));
                } catch (SequenceEditorException seqe) {
                    session.setAttribute("sequence.error.message", bundle.getString("sequence.invalid.syntax.error"));
%>
<script type="text/javascript">
    document.location.href = '<%= errorPage %>';
</script>
<%
                    return;
                }
            }   

    EditorUIClient sequenceAdminClient
            = SequenceEditorHelper.getClientForEditor(getServletConfig(), session) ;//new SequenceAdminClient(this.getServletConfig(), session);
    if (onErrorKey != null && !"".equals(onErrorKey)) {
        SequenceEditorHelper.getEditingSequence(session).setErrorHandler(onErrorKey);
    }

    if (seqDescription != null && !seqDescription.equals("")) {
        SequenceEditorHelper.getEditingSequence(session).setDescription(seqDescription);
    }

    if (updateOnSynapseRegistry) {
            sequenceAdminClient.saveDynamicSequence(sequenceKey, SequenceEditorHelper.getEditingActualSequence(session));
    } else if (SequenceEditorConstants.ACTION_PARAM_VALUE_ADD.equals(
            SequenceEditorHelper.getEditingSequenceAction(session)) ||
                (SequenceEditorConstants.ACTION_PARAM_VALUE_EDIT.equals(
            SequenceEditorHelper.getEditingSequenceAction(session)))) {
        
        SequenceMediator sequence = SequenceEditorHelper.getEditingActualSequence(session);
        givenName = sequence.getName();
        sequence.setName(sequenceName.trim());
        
    try {
        sequenceAdminClient.addDynamicSequence(sequenceKey, sequence);
        session.setAttribute("editingSequenceAction", "edit");
    } catch (SequenceEditorException e) {
        try {
            if (givenName != null) {
                sequence.setName(givenName);
            }
            
            if (sequenceAdminClient.getDynamicSequence(sequenceKey) != null) {
                session.setAttribute("editingSequenceAction", "edit");
            }
        } catch (Exception ignore) {}
        session.setAttribute("sequence.error.message", e.getMessage());
%>
<script type="text/javascript">
    document.location.href = '<%= errorPage %>';
</script>
<%
        }
    }

    SequenceEditorHelper.clearSessionCache(session);
    NameSpacesInformationRepositoryRegistrar.unRegisterNameSpacesInformationRepository(session);
} catch (Exception e) {
            session.setAttribute("sequence.error.message", bundle.getString("sequence.invalid.syntax.error"));
%>
<script type="text/javascript">
    document.location.href = '<%= errorPage %>';
</script>
<%
    }
    if (request.getParameter("forwardTo") != null) {
        forwardTo = request.getParameter("forwardTo") + "?ordinal=1";
    }
%>

<script type="text/javascript">
    window.location.href = "<%=forwardTo%>";
</script>

<%
    }
%>