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

<%@ page import="org.apache.axiom.om.OMAttribute" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.OMFactory" %>
<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.sequences.common.SequenceEditorException" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.sequences.ui.SequenceEditorConstants" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepositoryRegistrar" %>
<%@ page import="javax.xml.namespace.QName" %>
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

        String forwardTo = /*"list_sequences.jsp"*/ SequenceEditorHelper.getForwardToFrom(session)+"?tab=0&";
        String errorPage = "design_sequence.jsp?ordinal=1";
        try {
            String sequenceSource = request.getParameter("sequenceXML");
            String sequenceName = request.getParameter("sequenceName");
            String onErrorKey = request.getParameter("onErrorKey");
            String seqDescription = request.getParameter("seqDescription");
            if (sequenceSource != null) {
                if (SequenceEditorConstants.ACTION_PARAM_VALUE_ANONIFY.equals(
                        SequenceEditorHelper.getEditingSequenceAction(session))) {
                    OMElement elem = SequenceEditorHelper.parseStringToElement(sequenceSource.trim());
                    // changes the inSequence or outSequence or faultSequence to just sequence
                    if ("sequence".equals(SequenceEditorHelper.getEditorMode(session))) {
                        elem.setLocalName("sequence");
                    }
                    OMAttribute name = elem.getAttribute(new QName("name"));
                    if (name != null) {
                        name.setAttributeValue("__anonSequence__");
                    } else {
                        OMFactory fac = elem.getOMFactory();
                        elem.addAttribute("name", "__anonSequence__", fac.createOMNamespace("", ""));
                        sequenceSource = elem.toString().trim();
                    }
                }
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
            = SequenceEditorHelper.getClientForEditor(getServletConfig(), session) ; //new SequenceAdminClient(this.getServletConfig(), session);
    if (SequenceEditorHelper.getEditingSequence(session) != null) {
        if (onErrorKey != null && !"".equals(onErrorKey)) {
            SequenceEditorHelper.getEditingSequence(session).setErrorHandler(onErrorKey);
        }
        if (seqDescription != null && !seqDescription.equals("")) {
            SequenceEditorHelper.getEditingSequence(session).setDescription(seqDescription);
        }
        if (SequenceEditorConstants.ACTION_PARAM_VALUE_EDIT.equals(
                SequenceEditorHelper.getEditingSequenceAction(session))) {
            sequenceAdminClient.saveSequence(SequenceEditorHelper.getEditingActualSequence(session));
        } else if (SequenceEditorConstants.ACTION_PARAM_VALUE_ADD.equals(
                SequenceEditorHelper.getEditingSequenceAction(session))) {
            SequenceMediator sequence = SequenceEditorHelper.getEditingActualSequence(session);
            if (sequenceName != null && !"".equals(sequenceName)) {
                sequence.setName(sequenceName.trim());
            }
            if (sequenceName != null) {
                if (sequence.getName() == null || "".equals(sequence.getName()) || sequenceName.trim().contains(" ")) {
                    if (sequenceName.trim().contains(" ")) {
                        session.setAttribute("sequence.warn.message", bundle.getString("sequence.name.spaces.error"));
                    } else {
                        session.setAttribute("sequence.warn.message", bundle.getString("sequence.name.required"));
                    }
%>
<script type="text/javascript">
    document.location.href = '<%= errorPage %>';
</script>
<%
            return;
        }
    }
    try {
        sequenceAdminClient.addSequence(sequence);
        session.setAttribute("editingSequenceAction", "edit");
    } catch (SequenceEditorException e) {
        session.setAttribute("sequence.error.message", e.getMessage());
%>
<script type="text/javascript">
    document.location.href = '<%= errorPage %>';
</script>
<%
            }
        } else if (SequenceEditorConstants.ACTION_PARAM_VALUE_ANONIFY.equals(
                SequenceEditorHelper.getEditingSequenceAction(session))) {
            // gets the sequence, i.e. in/out/fault
            String sequence = (String) session.getAttribute("sequence");
            String xml = SequenceEditorHelper.parseAnonSequenceToString(SequenceEditorHelper.getEditingSequence(session),
                    (String) session.getAttribute("sequence"));
            // removes the editing sequence and editing sequence attribute from session
            if (request.getParameter("forwardTo") == null) {
                SequenceEditorHelper.removeEditingSequence(session);
                SequenceEditorHelper.removeEditingSequenceAction(session);
            }
            // sets the xml as a session attribute
            xml = xml.replaceAll(">", "&gt");
            xml = xml.replaceAll("<", "&lt");
            session.setAttribute("seqXML", xml);
            String originator = (String) session.getAttribute("sequenceAnonOriginator");
            if (originator != null) {
                forwardTo = originator + "?sequence=" + sequence + "&originator=save_sequence.jsp";
            } else {
                forwardTo = "../proxyservices/anonSequenceHandler.jsp?sequence=" + sequence + "&originator=save_sequence.jsp";
            }
        } else {
            //todo: unknown action error
        }
    } else {
    }
    SequenceEditorHelper.clearSessionCache(session);
    NameSpacesInformationRepositoryRegistrar.unRegisterNameSpacesInformationRepository(session);
    } catch (Exception e) {
       //getCause(), it explicitly returns null if the cause variable is the same as the throwable(e == cause)
       if (e.getCause() != null) {
           String errorMsg = e.getCause().toString().trim();
           errorMsg = errorMsg.substring(errorMsg.indexOf(":") + 2, errorMsg.length());
           session.setAttribute("sequence.error.message", errorMsg);
       }
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