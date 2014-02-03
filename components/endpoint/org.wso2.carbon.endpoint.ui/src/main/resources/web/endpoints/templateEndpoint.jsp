<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.template.TemplateEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/registry-browser.js"></script>

<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript" src="js/tabs.js"></script>

<script type="text/javascript" src="js/form.js"></script>
<script type="text/javascript" src="js/endpoint-util.js"></script>
<script type="text/javascript" src="js/template-param.js"></script>
<script type="text/javascript" src="js/endpoint-params.js"></script>
<script type="text/javascript" src="js/templateEndpoint-validate.js"></script>
<script type="text/javascript" src="js/common-tasks.js"></script>


<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        request="<%=request%>"/>

<carbon:breadcrumb
        label="template.endpoint"
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    String endpointName = request.getParameter("endpointName");
    String endpointAction = request.getParameter("endpointAction");
    String origin = request.getParameter("origin");

    boolean isAnonymous = false;
    String anonymousEndpointXML = null;
    String anonymousOriginator = null;
    String endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
    }

    TemplateEndpoint endpoint = null;

    if (endpointAction != null && !"".equals(endpointAction) && endpointAction.equals("edit")) {
        try {
            session.setAttribute("action", "edit"); // uses when saving the endpoint

            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            EndpointAdminClient client;
            String endpointString;
            try {
                client = new EndpointAdminClient(cookie, serverURL, configContext);
                endpointString = client.getEndpoint(endpointName);
                OMElement endpointElement = AXIOMUtil.stringToOM(endpointString);
                endpoint = new TemplateEndpoint();
                endpoint.build(endpointElement, false);
            } catch (Exception e) {
                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
            return;
        }
    } catch (Exception e) {
        String msg = "Unable to get Address Endpoint data: " + e.getMessage();
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
    }
} else if (origin != null && !"".equals(origin)) {
    String epString = (String) session.getAttribute("endpointConfiguration");
    epString = epString.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
    OMElement endpointElement = AXIOMUtil.stringToOM(epString);
    endpoint = new TemplateEndpoint();
    endpoint.build(endpointElement, isAnonymous);
} else if (isAnonymous) {
    // coming through using either send mediator or proxy services by adding an anonymous endpoint
    // we are in anonymous mode
    anonymousEndpointXML = (String) session.getAttribute("anonEpXML");
    anonymousOriginator = (String) session.getAttribute("anonOriginator");
    if (anonymousEndpointXML != null && !"".equals(anonymousEndpointXML)) {
        // if a user is here that mean user is trying to edit an existing anonymous endpoint
        try {
            OMElement endpointElement = AXIOMUtil.stringToOM(anonymousEndpointXML);
            endpoint = new TemplateEndpoint();
            endpoint.build(endpointElement, true);
        } catch (XMLStreamException e) {
            session.removeAttribute("anonEpXML");
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Unable to create endpoint with given data");
    window.location.href = "loadBalanceEndpoint.jsp";
</script>
<%
            }
        } else {
            endpoint = new TemplateEndpoint();
        }
    } else {
        endpoint = new TemplateEndpoint();
        session.setAttribute("action", "add");
    }

    String templateEpName = "";
    String templateEndpointAddress = "";
    String validAddressURL = "";
    String target = "";
    Map<String, String> parameterMap = new HashMap<String, String>();

    if (endpoint != null) {
        parameterMap = endpoint.getParameters();
        // Endpoint Name
        if (parameterMap.containsKey("name")) {
            templateEpName = parameterMap.get("name");
        }
        // Endpoint Address
        if (parameterMap.containsKey(("uri"))) {
            templateEndpointAddress = parameterMap.get("uri");
            validAddressURL = EndpointConfigurationHelper.getValidXMLString(templateEndpointAddress);
        }
        // target Template
        if (endpoint.getTargetTemplate() != null) {
            target = endpoint.getTargetTemplate();
        }
    }

    Set<String> paramSet = parameterMap.keySet();
    String propertyTableStyle = parameterMap.size() == 0 ? "display:none;" : "";

    if (paramSet.size() == 1 || paramSet.size() == 2) {
        propertyTableStyle = "display:none;";
        for (String param : paramSet) {
            if (!param.equals("name") && !param.equals("uri")) {
                propertyTableStyle = "";
            }
        }
    }

    EndpointTemplateAdminClient templateAdminClient = new EndpointTemplateAdminClient(config, session);
    String[] templateNameList = templateAdminClient.getAllTempalateNames();

%>

<div id="middle">
<h2>
    <% if (request.getParameter("serviceName") != null) {
    %><%=request.getParameter("serviceName")%>:&nbsp;<%
    }
    if ("edit".equals(endpointAction)) {
%><fmt:message key="edit.endpoint"/><%
} else {
%><fmt:message key="template.endpoint"/><%
    }
%>
</h2>
<div id="workArea">
<form action="updatePages/templateEndpoint-update.jsp" id="endpoint-editor-form"
      name="endpoint-editor-form">
<table class="styledLeft">
<thead>
<tr>
    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="design.view.of.the.template.endpoint"/></span>
        <a class="icon-link"
           style="background-image: url(images/source-view.gif);"
           onclick="switchToSource('Template','<%=isAnonymous%>','false');"
           href="#"><fmt:message key="switch.to.source.view"/></a>
    </th>
</tr>
</thead>
<tbody>
<tr>
    <td>
        <table class="normal-nopadding">
            <tbody>

            <tr style="<%=!isAnonymous?"":"display:none"%>">
                <td width="180px"><fmt:message key="template.endpointName"/> <span
                        class="required">*</span></td>
                <td><input name="endpointName" id="endpointName"
                           value="<%=templateEpName%>"
                        <%=(!"".equals(templateEpName)) ? "disabled=\"disabled\"" : ""%>
                           onkeypress="return validateText(event);"/>
                    <input type="hidden" name="endpointName"
                           value="<%=templateEpName%>"/>
                </td>
            </tr>
            <tr>
                <td class="leftCol-small"><fmt:message key="template.endpoint.address"/>
                </td>
                <td><input id="address" name="address" type="text"
                           value="<%=validAddressURL%>" size="75"/>
                    <input id="testAddress" name="testAddress" type="button"
                           class="button"
                           onclick="testURL(document.getElementById('address').value)"
                           value="<fmt:message key="test.url"/>"/>
                </td>
            </tr>
            <tr>
                <td width="180px"><fmt:message key="template.endpoint.target"/> <span
                        class="required">*</span>
                </td>
                <td>
                    <input class="longInput" type="text" id="target.template"
                           name="target.template"
                           value="<%=target%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="template.available.templates"/>
                </td>
                <td>
                    <select name="templateSelector" id="templateSelector" onchange="onTemplateSelectionChange()">
                        <option value="default">Select From Templates</option>
                        <%
                            if (templateNameList != null) {
                                for (String templateName : templateNameList) {%>
                                    <option value="<%=templateName%>"><%=templateName%></option>
                                    <%
                                }
                            }
                            %>
                    </select>
                </td>
            </tr>
            <tr>
                <td colspan="2">

                    <div style="margin-top:0px;">
                        <table id="propertytable" style="<%=propertyTableStyle%>;" name="propertytable"
                               class="styledLeft">
                            <thead>
                            <tr>
                                <td colspan="3" class="sub-header"><fmt:message
                                        key="parameters"/></td>
                            </tr>

                            <tr>
                                <th width="15%"><fmt:message
                                        key="th.parameter.name"/></th>
                                <th width="15%"><fmt:message
                                        key="th.parameter.value"/></th>
                                <th><fmt:message key="th.action"/></th>
                            </tr>
                            <tbody id="propertytbody">
                            <%
                                int i = 0;
                                for (String param : paramSet) {
                                    String paramName = param;
                                    String paramValue = parameterMap.get(paramName);
                                    if (paramName.equals("name") || paramName.equals("uri")) { // hide default parameters
                                        continue;
                                    }
                            %>
                            <tr id="propertyRaw<%=i%>">
                                <td><input type="text" name="propertyName<%=i%>"
                                           id="propertyName<%=i%>"
                                           class="esb-edit small_textbox"
                                           value="<%=paramName%>"/>
                                </td>
                                <td><input type="text" name="propertyValue<%=i%>"
                                           id="propertyValue<%=i%>"
                                           value="<%=paramValue%>"/>
                                </td>
                                <td><a href="#" class="delete-icon-link"
                                       onclick="deleteProperty(<%=i%>)"><fmt:message
                                        key="template.parameter.delete"/></a></td>
                            </tr>
                            <%
                                    i++;
                                }%>
                            <input type="hidden" name="propertyCount" id="propertyCount"
                                   value="<%=i%>"/>
                            </tbody>
                            </thead>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div style="margin-top:10px;">
                        <a name="addNameLink"></a>
                        <a class="add-icon-link"
                           href="#addNameLink"
                           onclick="addParameter(true)"><fmt:message
                                key="template.parameter.add"/></a>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
        <table class="normal-nopadding">
            <tbody>
            <tr>
                <td class="buttonRow" colspan="2">
                    <input type="button" value="<fmt:message key="save"/>"
                           class="button" name="save"
                           onclick="javascript:submitEndpointData('Template','<%=isAnonymous%>','false');"/>
                    <%
                        if (!isAnonymous) {
                    %>
                    <input type="button" value="<fmt:message key="saveas"/>"
                           class="button" name="save"
                           onclick="javascript:showSaveAsForm(true,false);"/>
                    <%
                        }
                    %>
                    <input type="button" value="<fmt:message key="cancel"/>"
                           name="cancel" class="button"
                           onclick="javascript:cancelEndpointData('<%=anonymousOriginator%>');"/>
                </td>
            </tr>
            </tbody>
        </table>
        <div style="display:none;" id="saveAsForm">
            <p>&nbsp;</p>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th colspan="2">
                                    <span style="float:left; position:relative; margin-top:2px;"><fmt:message
                                            key="save.as.title"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <table class="normal">
                            <tr>
                                <td><fmt:message key="save.in"/></td>
                                <td>
                                    <fmt:message key="config.registry"/> <input
                                        type="radio" name="registry" id="config_reg"
                                        value="conf:"  checked="checked"
                                        onclick="document.getElementById('reg').innerHTML='conf:';"/>
                                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                    <fmt:message key="gov.registry"/> <input
                                        type="radio" name="registry" id="gov_reg"
                                        value="gov:"
                                        onclick="document.getElementById('reg').innerHTML='gov:';"/>
                                </td>
                            </tr>
                            <tr>
                                <td>Key
                                	<span class="required">*</span>
                        		</td>
                                <td><span id="reg">conf:</span><input type="text"
                                                                     size="75"
                                                                     id="synRegKey"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" class="button"
                               value="<fmt:message key="save"/>" id="saveSynRegButton"
                               onclick="javascript:submitDynamicEndpointData('Template','false'); return false;"/>
                        <input type="button" class="button"
                               value="<fmt:message key="cancel"/>"
                               id="cancelSynRegButton"
                               onclick="javascript:showSaveAsForm(false,false); return false;">
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </td>
</tr>
</tbody>
</table>
</form>
</div>
</div>
<a name="registryBrowserLink"></a>

</fmt:bundle>