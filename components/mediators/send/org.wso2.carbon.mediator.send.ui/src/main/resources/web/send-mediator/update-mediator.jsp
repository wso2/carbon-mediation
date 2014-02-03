<%--
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
 --%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.send.SendMediator" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.llom.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.EndpointFactory" %>
<%@ page import="org.apache.synapse.endpoints.Endpoint" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.send.ui.client.SendClient" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.apache.synapse.endpoints.ResolvingEndpoint" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.apache.synapse.mediators.Value" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SendMediator)) {
        CarbonUIMessage.sendCarbonUIMessage("Unable to edit the mediator", CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }
    SendMediator sendMediator = (SendMediator) mediator;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    SendClient client = new SendClient(
            configContext, backendServerURL, cookie, request.getLocale());

    // sets endpoint information
    String option = request.getParameter("epOp");
    if ("none".equals(option)) {
        sendMediator.setEndpoint(null);
    } else if ("anon".equals(option)) {
        String anonEpXML = (String)session.getAttribute("endpointXML");
        session.removeAttribute("endpointXML");
         if (anonEpXML != null && !"".equals(anonEpXML) && !"Add".equals(request.getParameter("anonEpAction"))) {
             OMElement anonEpXMLElem = AXIOMUtil.stringToOM(anonEpXML);
             Endpoint endpoint = EndpointFactory.getEndpointFromElement(anonEpXMLElem, true, new Properties());
             sendMediator.setEndpoint(endpoint);
          }
    } else if ("registry".equals(option)) {
        String key = request.getParameter("registryKey");
        Endpoint endpoint = client.getEndpoint(key);
        sendMediator.setEndpoint(endpoint);
    } else if ("xpath".equals(option)) {
        XPathFactory xPathFactory = XPathFactory.getInstance();
        ResolvingEndpoint ep = new ResolvingEndpoint();
        SynapseXPath xpath = xPathFactory.createSynapseXPath("mediator.send.xpath_val", request, session);
        ep.setKeyExpression(xpath);
        sendMediator.setEndpoint(ep);
    }

    String keyVal;
    String keyExp;
    String keyGroup = request.getParameter("keygroup");
    if (keyGroup != null && !keyGroup.equals("")) {
        if (keyGroup.equals("StaticKey")) {
            keyVal = request.getParameter("mediator.send.key.static_val");
            if (keyVal != null && !keyVal.equals("")) {
                Value staticKey = new Value(keyVal);
                sendMediator.setReceivingSeqValue(staticKey);
            }
        } else if (keyGroup.equals("DynamicKey")) {
            keyExp = request.getParameter("mediator.send.key.dynamic_val");
            if (keyExp != null && !keyExp.equals("")) {
                XPathFactory xPathFactory = XPathFactory.getInstance();
                Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                        "mediator.xslt.key.dynamic_val", request.getParameter("mediator.send.key.dynamic_val"), session));
                sendMediator.setReceivingSeqValue(dynamicKey);
            }
        } else if (keyGroup.equals("DefaultKey")) {
            sendMediator.setReceivingSeqValue(null);
        }
    }

    String buildMessage = request.getParameter("build.message");
    if (buildMessage != null && "true".equalsIgnoreCase(buildMessage)) {
        sendMediator.setBuildMessage(true);
    } else {
        sendMediator.setBuildMessage(false);
    }

    session.removeAttribute("anonEpXML");
    session.removeAttribute("endpointXML");
%>

