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

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.loadbalance.LoadBalanceEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
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
<script type="text/javascript" src="js/loadBalanceEndpoint-validate.js"></script>
<script type="text/javascript" src="js/common-tasks.js"></script>
<link rel="stylesheet" type="text/css" href="css/errorcode-editor.css"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        request="<%=request%>"/>

<carbon:breadcrumb
        label="load.balance.group"
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

    LoadBalanceEndpoint endpoint = null;

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
                endpoint = new LoadBalanceEndpoint();
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
            String msg = "Unable to get LoadBalance Endpoint data: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }
    } else if (origin != null && !"".equals(origin)) {
        String epString = (String) session.getAttribute("endpointConfiguration");
        epString = epString.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
        OMElement endpointElement = AXIOMUtil.stringToOM(epString);
        endpoint = new LoadBalanceEndpoint();
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
                endpoint = new LoadBalanceEndpoint();
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
            endpoint = new LoadBalanceEndpoint();
        }
    } else {
        endpoint = new LoadBalanceEndpoint();
        session.setAttribute("action", "add");
    }

    String[] sessionManOptions = {"SelectAValue", "http", "soap", "simpleClientSession"};
    String loadBalanceEndpointName = "";
    boolean isDefault = true, isTransport = false, isSOAP = false, isClientID = false;
    String algorithmClassName = EndpointConfigurationHelper.ROUNDROBIN_ALGO_CLASS_NAME; //default
    boolean isRoundRobin = true;
    long sessionTimeout = 0;
    String properties = "";

    if (endpoint != null) {
        if (endpoint.getName() != null) {
            loadBalanceEndpointName = endpoint.getName();
        }
        if (endpoint.getAlgorithmClassName() != null) {
            algorithmClassName = endpoint.getAlgorithmClassName();
        }
        if (!algorithmClassName.equals(EndpointConfigurationHelper.ROUNDROBIN_ALGO_CLASS_NAME)) {
            isRoundRobin = false;
        }
        String sessionType = endpoint.getSessionType();
        if (sessionType != null) {
            if (sessionType.equals("http")) {
                isTransport = true;
            } else if (sessionType.equals("soap")) {
                isSOAP = true;
            } else if (sessionType.equals("simpleClientSession")) {
                isClientID = true;
            }
        }
        sessionTimeout = endpoint.getSessionTimeout();
        if (endpoint.getProperties() != null) {
            properties = endpoint.getProperties();
        }
    }

    session.setAttribute("editingListEndpoint", endpoint);
    session.setAttribute("isEditingListEndpoint", "true");
%>

<script type="text/javascript">

    YAHOO.util.Event.onDOMReady(init);

    function init() {
        generateServiceParamTable();
    }

    function generateServiceParamTable() {
        var str = '<%=properties%>';
        if (str != '') {
            var params;
            params = str.split("::");
            var i, param;
            for (i = 0; i < params.length; i++) {
                param = params[i].split(",");
                addServiceParamRow(param[0], param[1], param[2], "headerTable");
            }
        }
    }

    function showHideAlgoInputDiv(selectNode) {
        var selectOption = selectNode.options[selectNode.selectedIndex].value;
        var algoInputDiv = document.getElementById('_algoInputDiv');
        if (selectOption != null && selectOption != undefined) {
            if (selectOption == 'other') { // show algoInputDiv
                if (algoInputDiv != null && algoInputDiv != undefined) {
                    document.getElementById('_algoClassName').value = '';
                    algoInputDiv.style.display = '';
                }
            } else { // hide algoInputDiv
                document.getElementById('_algoClassName').value = 'org.apache.synapse.endpoints.algorithms.RoundRobin';
                if (algoInputDiv != null && algoInputDiv != undefined) {
                    algoInputDiv.style.display = 'none';
                }
            }
        }
    }

    function activateManagementField(selectNode) {
        var selectOption = selectNode.options[selectNode.selectedIndex].value;
        var sessionTimeOut = document.getElementById('_sessionTimeOut');
        if (selectOption != null && selectOption != undefined) {
            if (selectOption == 'SelectAValue') {
                if (sessionTimeOut != null && sessionTimeOut != undefined) {
                    sessionTimeOut.disabled = 'disabled';
                    sessionTimeOut.value = 0;
                }
            } else {
                if (sessionTimeOut != null && sessionTimeOut != undefined) {
                    sessionTimeOut.disabled = '';
                }
            }
        }
    }
</script>

<div id="middle">
<h2>
    <fmt:message key="load.balance.group"/>
</h2>

<form action="updatePages/loadBalanceEndpoint-update.jsp" id="endpoint-editor-form"
      name="endpoint-editor-form">
<div id="workArea">
<table class="styledLeft" cellspacing="0">
    <thead>
    <tr>
        <th>
            <span style="float:left; position:relative; margin-top:2px;">Switch to source</span><a
                href="#" onclick="switchToSource('LoadBalance','<%=isAnonymous%>','false');"
                class="icon-link"
                style="background-image:url(images/source-view.gif);"><fmt:message
                key="switch.to.source.view"/></a>
        </th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <table class="normal" width="100%">
                <tr style="<%=!isAnonymous?"":"display:none"%>">
                    <td class="leftCol-small">
                        <fmt:message key="endpoint.name"/> <span
                            class="required">*</span>
                    </td>
                    <td>
                        <input type="text" id="listEndpointName" name="listEndpointName"
                               value="<%=loadBalanceEndpointName %>" <%= !"".equals(loadBalanceEndpointName) ? "disabled=\"disabled\"" : "" %>
                               onkeypress="return validateText(event);"/>
                        <input type="hidden" name="listEndpointName"
                               value="<%=loadBalanceEndpointName%>"/>
                        <input type="hidden" name="isAnnonEndpointID"
                               id="isAnnonEndpointID" value="<%=isAnonymous%>"/>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-small"><fmt:message key="algorithm"/></td>
                    <td>
                        <select name="algoCombo" id="_algoCombo"
                                onchange="showHideAlgoInputDiv(this)">
                            <option value="roundrobin"
                                    <%=isRoundRobin ? "selected" : ""%>>
                                <fmt:message key="roundrobin"/></option>
                            <option value="other" <%=!isRoundRobin ? "selected" : ""%>>
                                <fmt:message key="other"/></option>
                        </select>
                    </td>
                </tr>
                <tr id="_algoInputDiv"
                        <%=isRoundRobin ? "style=\"display:none\"" : ""%>>
                    <td></td>
                    <td>
                        <fmt:message key="algorithm.class.name"/>
                        <input name="algoClassName" type="text" id="_algoClassName"
                               style="width:450px;"
                               value="<%=algorithmClassName%>"/>
                    </td>
                </tr>
                <tr>
                    <td class="leftCol-small"><fmt:message
                            key="session.management"/></td>
                    <td><select name="sessionManagement" id="_sesOptions"
                                onchange="activateManagementField(this)">
                        <option value="<%=sessionManOptions[0]%>" <%=isDefault ? "selected" : ""%>>
                            <fmt:message key="none"/></option>
                        <option value="<%=sessionManOptions[1]%>" <%=isTransport ? "selected" : ""%>>
                            <fmt:message key="transport"/></option>
                        <option value="<%=sessionManOptions[2]%>" <%=isSOAP ? "selected" : ""%>>
                            <fmt:message key="soap"/></option>
                        <option value="<%=sessionManOptions[3]%>" <%=isClientID ? "selected" : ""%>>
                            <fmt:message key="client.id"/></option>
                    </select></td>
                </tr>
                <tr>
                    <td class="leftCol-small">
                        <fmt:message key="endpoint.session.timeout"/>
                    </td>
                    <td>
                        <input type="text" id="_sessionTimeOut" name="sessionTimeOut"
                               value="<%= sessionTimeout%>"
                                <%=sessionTimeout == 0 ? "disabled=\"disabled\"" : ""%>
                               onkeypress="return validateText(event);"/>
                    </td>
                </tr>
            </table>
            <div id="listEndpointDesigner">
                <jsp:include page="listEndpointDesigner/listEndpointDesigner.jsp" flush="true">
                    <jsp:param name="request" value="<%=request%>"/>
                </jsp:include>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <table class="normal-nopadding">
                <tbody>
                <tr>
                    <td colspan="2" class="sub-header"><fmt:message
                            key="lb.endpoint.property.header"/></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <a href="#" onclick="addServiceParams('headerTable')"
                           style="background-image: url('../admin/images/add.gif');"
                           class="icon-link">Add
                                             Property</a>
                        <input type="hidden" name="endpointProperties"
                               id="endpointProperties"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <table cellpadding="0" cellspacing="0" border="0"
                               class="styledLeft"
                               id="headerTable"
                               style="display:none;">
                            <thead>
                            <tr>
                                <th style="width:25%"><fmt:message
                                        key="param.name"/></th>
                                <th style="width:25%"><fmt:message
                                        key="param.value"/></th>
                                <th style="width:25%"><fmt:message
                                        key="param.scope"/></th>
                                <th style="width:25%"><fmt:message
                                        key="param.action"/></th>
                            </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    <tr>
        <td class="buttonRow">
            <input type="button" value="<fmt:message key="save"/>"
                   class="button"
                   name="save"
                   onclick="submitEndpointData('LoadBalance','<%=isAnonymous%>','true');"/>
            <%
                if (!isAnonymous) {
            %>
            <input type="button" value="<fmt:message key="saveas"/>" class="button" name="save"
                   onclick="javascript:showSaveAsForm(true,false);"/>
            <%
                }
            %>
            <input type="button" value="<fmt:message key="cancel"/>"
                   name="cancel"
                   class="button"
                   onclick="cancelEndpointData('<%=anonymousOriginator%>');"/>
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
                    <td><fmt:message key="save.in"/></td>
                    <td>
                        <fmt:message key="config.registry"/> <input type="radio" name="registry"
                                                                    id="config_reg"
                                                                    value="conf:"
                                                                    checked="checked"
                                                                    onclick="document.getElementById('reg').innerHTML='conf:';"/>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <fmt:message key="gov.registry"/> <input type="radio" name="registry"
                                                                 id="gov_reg"
                                                                 value="gov:"
                                                                 onclick="document.getElementById('reg').innerHTML='gov:';"/>
                    </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="registry.key"/>
                            <span class="required">*</span>
                        </td>
                        <td><span id="reg">conf:</span><input type="text" size="75" id="synRegKey"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input type="button" class="button" value="<fmt:message key="save"/>"
                       id="saveSynRegButton"
                       onclick="javascript:submitDynamicEndpointData('LoadBalance','true'); return false;"/>
                <input type="button" class="button" value="<fmt:message key="cancel"/>"
                       id="cancelSynRegButton"
                       onclick="javascript:showSaveAsForm(false,false); return false;">
            </td>
        </tr>
        </tbody>
    </table>
</div>
</div>
</form>
</div>
<a name="registryBrowserLink"></a>

</fmt:bundle>