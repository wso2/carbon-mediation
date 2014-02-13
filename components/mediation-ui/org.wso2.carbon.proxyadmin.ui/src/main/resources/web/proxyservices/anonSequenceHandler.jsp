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
<%@ page import="org.apache.synapse.config.xml.MediatorFactoryFinder" %>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>

<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
            request.getLocale());
    String header = "";
    String sequence = request.getParameter("sequence");
    String seqXML = (String)session.getAttribute("seqXML");

    String forwardTo = "";
    if (request.getParameter("cancelled") != null && "true".equals(request.getParameter("cancelled"))) {
        forwardTo = "index.jsp?header=" + session.getAttribute("header") + "&fromdesign=true";
        // removes common attributes
        removeCommonSessionAttributes(session);
    } else {
        if (seqXML != null && !"".equals(seqXML)) {
            // return path from save_sequence.jsp
            header = (String)session.getAttribute("header");
            forwardTo = "index.jsp?header=" + header + "&fromdesign=true";
            seqXML = seqXML.replaceAll("&gt", ">");
            seqXML = seqXML.replaceAll("&lt", "<");
            ProxyData pd = (ProxyData) session.getAttribute("proxy");
            if ("in".equals(sequence)) {
                pd.setInSeqXML(seqXML);
            } else if ("out".equals(sequence)) {
                pd.setOutSeqXML(seqXML);
            } else if ("fault".equals(sequence)) {
                pd.setFaultSeqXML(seqXML);
            }
            session.setAttribute("proxy", pd);
            // removes the session attribute, seqXML
            session.removeAttribute("seqXML");
            // removes common session attributes
            removeCommonSessionAttributes(session);
        } else {
            // sets the anonOriginator to anonSequenceHandler.jsp. This will be the page to which result should be returned
            session.setAttribute("sequenceAnonOriginator", "../proxyservices/anonSequenceHandler.jsp");
            // send path to design_sequence.jsp
            header = request.getParameter("header");
            // exports the header in to the session
            session.setAttribute("header", header);
            ProxyData pd = (ProxyData) session.getAttribute("proxy");
            // sets pd as a session attribute to use in the return path
            forwardTo = "../sequences/design_sequence.jsp?serviceName=" + pd.getName();
            session.setAttribute("proxy", pd);
            // sets sequnce (i.e. in/out/fault) as a session attribute
            session.setAttribute("sequence", sequence);
            String xml = "";
            if ("in".equals(sequence)) {
                xml = pd.getInSeqXML();
            } else if ("out".equals(sequence)) {
                xml = pd.getOutSeqXML();
            } else if ("fault".equals(sequence)) {
                xml = pd.getFaultSeqXML();
            }
            SequenceMediator seq = null;
            if (xml != null && !"".equals(xml)) {
                try {
                	//spacial case verify any unwanted characters embedded with the xpath expression given
                	int xpathIndex = xml.indexOf("xpath=");
            		String xmlout = null;
            		if (xpathIndex > 0) {
            			String xpathBegin = xml.substring(xpathIndex);
            			int xpathBeginIndex = xpathBegin.indexOf("\"");
            			String xpathVariableStart = xpathBegin.substring(xpathBeginIndex + 1);
            			int xpathEndIndex = xpathVariableStart.indexOf("\"");
            			String xpath = xpathVariableStart.substring(0, xpathEndIndex);
            			int lenthXpath = xpath.length();

            			String xmlBxpath = xml.substring(0, xpathIndex + 7);
            			String xpathAxpathString = xml.substring(xpathIndex + 7 + lenthXpath, xml.length());
            			xmlout = xmlBxpath + xpath.replaceAll("<", "&lt").replaceAll(">", "&gt") + xpathAxpathString;
            		}else{
            			xmlout =xml;
            		}
                	
                    OMElement elem = new StAXOMBuilder(new ByteArrayInputStream(xmlout.getBytes())).getDocumentElement();
                    OMFactory fac = elem.getOMFactory();
                    elem.addAttribute("name", "__anonSequence__", fac.createOMNamespace("", ""));
                    // changes the name inSequence or outSequence or faultSequence to just sequence
                    elem.setLocalName("sequence");
                    seq = new SequenceMediator();
                    seq.build(elem);
                } catch (Exception e) {
                    removeCommonSessionAttributes(session);
                    CarbonUIMessage.sendCarbonUIMessage(bundle.getString(bundle.getString(
                            "unable.to.build.sequence.object.from.the.given.sequence.information")),
                            CarbonUIMessage.ERROR, request);
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
        window.location.href = 'index.jsp';
    }
</script>