<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.OMFactory" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.rest.api.ui.i18n.Resources",
                                                     request.getLocale());
    String header = "";
    String sequence = request.getParameter("sequence");
    String seqXML = (String) session.getAttribute("seqXML");
    APIData apiName = (APIData) session.getAttribute("apiData");
    ResourceData resourceData = (ResourceData) session.getAttribute("resource");

    String forwardTo = "";
    if (request.getParameter("cancelled") != null && "true".equals(request.getParameter("cancelled"))) {
        forwardTo = "manageAPI.jsp?" + "mode=" + session.getAttribute("mode") + "&apiName=" + apiName.getName()
                    + "&resourceIndex=" + session.getAttribute("index");
        // removes common attributes
        session.setAttribute("fromSourceView", "true");
        removeCommonSessionAttributes(session);
    } else {
        if (seqXML != null && !"".equals(seqXML)) {  // coming from the seq. editor.
            header = (String) session.getAttribute("header");
            forwardTo = "designToData.jsp?" + "sequence=" + sequence + "&index=" + session.getAttribute("index") + "&resourceIndex=" + session.getAttribute("index");
            seqXML = seqXML.replaceAll("&gt", ">");
            seqXML = seqXML.replaceAll("&lt", "<");
            if ("in".equals(sequence)) {
                resourceData.setInSeqXml(seqXML);
            } else if ("out".equals(sequence)) {
                resourceData.setOutSeqXml(seqXML);
            } else if ("fault".equals(sequence)) {
                resourceData.setFaultSeqXml(seqXML);
            }
            session.removeAttribute("seqXML");
            removeCommonSessionAttributes(session);
        } else {   // coming from designToData.jsp
            session.setAttribute("index", request.getParameter("index"));
            session.setAttribute("sequenceAnonOriginator", "../api/anonSequenceHandler.jsp");
            header = request.getParameter("header");
            session.setAttribute("header", header);
            forwardTo = "../sequences/design_sequence.jsp?serviceName=" + "API-Resource";
            session.setAttribute("sequence", sequence);
            String xml = "";
            if ("in".equals(sequence)) {
                xml = resourceData.getInSeqXml();
            } else if ("out".equals(sequence)) {
                xml = resourceData.getOutSeqXml();
            } else if ("fault".equals(sequence)) {
                xml = resourceData.getFaultSeqXml();
            }
            SequenceMediator seq;
            if (xml != null && !"".equals(xml)) {
                try {
                    xml = xml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<")
                            .replaceAll("\n", "").replaceAll("\t", " ");
                    OMElement elem = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes())).getDocumentElement();
                    OMFactory fac = elem.getOMFactory();
                    elem.addAttribute("name", "__anonSequence__", fac.createOMNamespace("", ""));
                    // changes the name inSequence or outSequence or faultSequence to just sequence
                    elem.setLocalName("sequence");
                    seq = new SequenceMediator();
                    seq.build(elem);
                } catch (Exception e) {
                    removeCommonSessionAttributes(session);
                    CarbonUIMessage.sendCarbonUIMessage(
                            bundle.getString("unable.to.build.sequence.object.from.the.given.sequence.information"),
                            CarbonUIMessage.ERROR, request, e);
%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
                    response.sendRedirect("../admin/error.jsp");
                    return;
                }
            } else {
                seq = new SequenceMediator();
                seq.setName("__anonSequence__");
            }
            session.setAttribute("editingSequence", seq);
            session.setAttribute("editingSequenceAction", "anonify");
            session.removeAttribute("mediator.position");
            session.removeAttribute("editorClientFactory");
        }
    }
%>
<%!
    void removeCommonSessionAttributes(HttpSession session) {
        session.removeAttribute("anonOriginator");
        session.removeAttribute("sequenceAnonOriginator");
        session.removeAttribute("header");
        session.removeAttribute("editingSequence");
        session.removeAttribute("editingSequenceAction");
        session.removeAttribute("mediator.position");
    }
%>

<script type="text/javascript">
    if (window.location.href.indexOf('originator') != -1 ||
        window.location.href.indexOf('cancelled') != -1) {
        window.location.href = "<%=forwardTo%>";
    } else {
        window.location.href = 'manageAPI.jsp';
    }
</script>
