<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.OMFactory" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.EditorUIClient" %>

<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.sequences.ui.i18n.Resources",
            request.getLocale());

    EditorUIClient sequenceAdminClient
            = SequenceEditorHelper.getClientForEditor(getServletConfig(), session);//new SequenceAdminClient(getServletConfig(), session);
    SequenceMediator seq = null;
    String action = request.getParameter("action");
    String header = "";
    String seqXML = (String)session.getAttribute("seqXML");
    String key = request.getParameter("key");

    String forwardTo = "";
    if (request.getParameter("cancelled") != null && "true".equals(request.getParameter("cancelled"))) {
        forwardTo = SequenceEditorHelper.getForwardToFrom(session);//"list_sequences.jsp";
        // removes common attributes
        removeCommonSessionAttributes(session);
    } else {
        if (seqXML != null && !"".equals(seqXML)) {            
            // return path from save_sequence.jsp
            header = (String)session.getAttribute("header");
            forwardTo = SequenceEditorHelper.getForwardToFrom(session);//"list_sequences.jsp";
            seqXML = seqXML.replaceAll("&gt", ">");
            seqXML = seqXML.replaceAll("&lt", "<");

            try {
                OMElement elem = new StAXOMBuilder(new ByteArrayInputStream(seqXML.getBytes())).getDocumentElement();
                String name = (String)session.getAttribute("registrySequenceName");
                OMFactory fac = elem.getOMFactory();
                if (name != null) {
                    elem.addAttribute("name", name, null);
                }
                seq = SequenceEditorHelper.getSequenceForEditor(session);
                seq.build(elem);
                key = (String) session.getAttribute("sequenceRegistryKey");
                if (key != null) {
                    sequenceAdminClient.updateDynamicSequence(key, seq);
                }
            } catch (Exception e) {
                removeCommonSessionAttributes(session);
                CarbonUIMessage.sendCarbonUIMessage(bundle.getString(bundle.getString(
                        "unable.to.build.sequence.object.from.the.given.sequence.information")),
                        CarbonUIMessage.ERROR, request);
                return;
            }
            // removes the session attribute, seqXML
            session.removeAttribute("seqXML");
            // removes common session attributes
            removeCommonSessionAttributes(session);
        } else if ("edit".equals(action) && key != null) {
            // sets the anonOriginator to anonSequenceHandler.jsp. This will be the page to which result should be returned
            session.setAttribute("sequenceAnonOriginator", "registry_sequence.jsp");
            // send path to design_sequence.jsp
            header = request.getParameter("header");
            // exports the header in to the session
            session.setAttribute("header", header);
            forwardTo = "./design_sequence.jsp";
            // sets sequnce (i.e. in/out/fault) as a session attribute
            session.setAttribute("sequenceRegistryKey", key);
            try {
                OMElement elem = sequenceAdminClient.getDynamicSequence(key);
                OMFactory fac = elem.getOMFactory();
                seq = SequenceEditorHelper.getSequenceForEditor(session);
                seq.build(elem.getFirstElement());
                session.setAttribute("registrySequenceName", seq.getName());
            } catch (Exception e) {
                session.setAttribute("dynamic_edit","fail");
                forwardTo = "./" + SequenceEditorHelper.getForwardToFrom(session);//"list_sequences.jsp";
            }
            session.setAttribute("editingSequence", seq);
            session.setAttribute("editingSequenceAction", "anonify");
            session.removeAttribute("mediator.position");
        }
    }
%>
<%!
    void removeCommonSessionAttributes(HttpSession session) {
        session.removeAttribute("seqXML");
        session.removeAttribute("sequenceAnonOriginator");
        session.removeAttribute("header");
        session.removeAttribute("editingSequence");
        session.removeAttribute("editingSequenceAction");
        session.removeAttribute("mediator.position");
        session.removeAttribute("registrySequenceName");
    }


%>

<script type="text/javascript">
    if (window.location.href.indexOf('originator') != -1) {
        window.location.href = "<%=forwardTo%>";
    } else {
        window.location.href = "<%=forwardTo%>";
    }
</script>

<%
    if ("fail".equals(session.getAttribute("dynamic_edit"))) {
        removeCommonSessionAttributes(session);
    }
%>
