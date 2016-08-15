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
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.apache.axis2.util.XMLPrettyPrinter" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.apache.axis2.util.XMLUtils" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="javax.xml.XMLConstants" %>
<%@ page import="org.xml.sax.EntityResolver" %>
<%@ page import="org.xml.sax.InputSource" %>
<%@ page import="org.xml.sax.SAXException" %>
<%@ page import="java.io.IOException" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyAdminClientUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<link type="text/css" rel="stylesheet" href="../proxyservices/css/proxyservices.css"/>
<!-- Dependencies -->
<script type="text/javascript" src="../proxyservices/js/proxyservices.js"></script>
    <%
        ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
                request.getLocale());
        String header = request.getParameter("header");
        ProxyData pd = (ProxyData)session.getAttribute("proxy");

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ProxyServiceAdminClient client = new ProxyServiceAdminClient(
                configContext, backendServerURL, cookie, request.getLocale());

        String forwardTo;
        String source;
        if (pd != null) {
            try {
                source = prettyPrint(client.getSourceView(pd).trim());
                // removes the proxy attribute after use
                session.removeAttribute("proxy");
            } catch (RemoteException e) {
                forwardTo = "index.jsp?header=" + header;
                CarbonUIMessage.sendCarbonUIMessage(bundle.getString("unable.to.get.source.from.the.design"),
                        CarbonUIMessage.ERROR, request);
                %>
                <script type="text/javascript">
                    window.location.href = '<%=Encode.forJavaScriptBlock(forwardTo)%>';
                </script>
                <%
                return;
            }
        } else if ((source = (String)session.getAttribute("proxyXML")) != null && !"".equals(source)){
            // this means that we came here from sourceToData causing an exception
            String ppSource = prettyPrint(source);
            if(ppSource.length() > 0) {
                source = ppSource;
            }
            // removes the session attribute of proxyXML
            session.removeAttribute("proxyXML");
        }
        if (source != null) {
            source = source.replace("&", "&amp;");
        }

        String saveOrModify = "add";
        if (bundle.getString("header.add").equals(header)) {
            saveOrModify = "add";
        } else if (bundle.getString("header.modify").equals(header)) {
            saveOrModify = "modify";
        }
    %>
    <%!
        String prettyPrint(String source) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OMElement elem;
            try {
                elem = XMLUtils.toOM((ProxyAdminClientUtils.getSecuredDocumentBuilder(true)).
                        parse(new ByteArrayInputStream(source.getBytes())).getDocumentElement());
                XMLPrettyPrinter.prettify(elem,stream);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new String(stream.toByteArray()).trim();
        }
    %>

<fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">
<carbon:breadcrumb
		label="proxy.service.source"
		resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />

    <script type="text/javascript">
        function designView(){
            document.getElementById("srcTextArea").value = editAreaLoader.getValue("srcTextArea");            
            document.sourceForm.action = "sourceToData.jsp?return=index.jsp&header=<%=Encode.forJavaScriptBlock(header)%>&originator=source.jsp";
            document.sourceForm.submit();
        }

        function saveData() {
            document.getElementById("srcTextArea").value = editAreaLoader.getValue("srcTextArea");
            document.sourceForm.action = "sourceToData.jsp?submit=<%=saveOrModify%>&header=<%=Encode.forJavaScriptBlock(header)%>&forwardTo=../service-mgt/index.jsp&originator=source.jsp";
            document.sourceForm.submit();
        }

        function cancelData() {
            window.location.href="../service-mgt/index.jsp";
        }
    </script>
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript">
    YAHOO.util.Event.onDOMReady(function() {
       editAreaLoader.init({
           id : "srcTextArea"		// textarea id
           ,syntax: "xml"			// syntax to be uses for highgliting
           ,start_highlight: true		// to display with highlight mode on start-up
       });
    });
    </script>
    <div id="middle">
        <h2><%=Encode.forHtmlContent(header)%> Proxy Service</h2>
        <div id="workArea">
            <form id="form1" name="sourceForm" method="post" action="">
                <table cellspacing="0" cellpadding="0" border="0" class="styledLeft noBorders">
                    <thead>
                        <tr>
                            <th>
                                <span style="float: left; position: relative; margin-top: 2px;"><fmt:message key="proxy.service.source"/></span><a
                                    style="background-image: url(images/design-view.gif);" class="icon-link"
                                    onclick="designView();" href="#"><fmt:message key="proxy.source.switch"/></a>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>
                                <textarea
                                        style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                        id="srcTextArea" name="srcTextArea" rows="30"><%=source%>
                                </textarea>
                            </td>
                        </tr>
                        <tr>
                            <td class="buttonRow">
                                <input id="saveBtn" type="submit" value="<fmt:message key="proxy.source.save"/>" class="button"
                                       onclick="saveData();return false;"/>
                                <input id="cancelBtn" type="button" value="<fmt:message key="proxy.source.cancel"/>" class="button"
                                       onclick="cancelData();return false;"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
