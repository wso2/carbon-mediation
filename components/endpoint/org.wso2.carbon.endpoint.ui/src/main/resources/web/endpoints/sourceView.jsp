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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.synapse.config.xml.XMLConfigConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointService" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointStore" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">
<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript" src="js/endpoint-util.js"></script>
<script type="text/javascript" src="js/form.js"></script>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<%
    String configuration = (String) session.getAttribute("endpointConfiguration");

    String prettyPrintPayload = "";
    if (configuration != null) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(configuration.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(inputStream);
        prettyPrintPayload = xmlPrettyPrinter.xmlFormat();
    }

    OMElement epElement = AXIOMUtil.stringToOM(configuration);
    EndpointService epService;

    String templateAdd = (String) session.getAttribute("templateAdd");
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;
    boolean isTemplateAdd = templateAdd != null && "true".equals(templateAdd) ? true : false;
    if (isFromTemplateEditor) {
        epService = EndpointStore.getInstance().getEndpointService(epElement.getFirstChildWithName(new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "endpoint")));
    } else {
        epService = EndpointStore.getInstance().getEndpointService(epElement);
    }

    boolean isAnonymous = false;
    String endpointMode = (String) session.getAttribute("epMode");
    String anonymousOriginator = null;
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
        anonymousOriginator = (String) session.getAttribute("anonOriginator");
    }

    String designViewUrl = null;
    if (isTemplateAdd) {
        designViewUrl = epService.getUIPageName() + "Endpoint.jsp?templateAdd=true&origin=source";
    } else {
        designViewUrl = epService.getUIPageName() + "Endpoint.jsp?origin=source";
    }
%>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:breadcrumb
        label="source.of.endpoint"
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript">

    function submitEndpointData() {
        document.getElementById("xmlPay").value = editAreaLoader.getValue("xmlPay");
        var isValidXML = isValidXml(trim(document.getElementById('xmlPay').value));
        if (!isValidXML) {
            return false;
        }
        var epString = encodeCharacters(trim(document.getElementById('xmlPay').value));

        jQuery.ajax({
                        type: 'POST',
                        url: 'updatePages/sourceView-update.jsp',
                        data: 'endpointString=' + epString,
                        success: function(msg) {
                            var index = msg.toString().trim().indexOf('<div>');
                            if (msg.toString().trim().indexOf('<div>Error:') == index) {
                                CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                            } else {
                                directToSubmitPage();
                            }
                        }
                    });
    }

    function submitDynamicEndpointData() {
        document.getElementById("xmlPay").value = editAreaLoader.getValue("xmlPay");
        var isValidXML = isValidXml(trim(document.getElementById('xmlPay').value));
        if (!isValidXML) {
            return false;
        }
        var epString = encodeCharacters(trim(document.getElementById('xmlPay').value));

        jQuery.ajax({
                        type: 'POST',
                        url: 'updatePages/sourceView-update.jsp',
                        data: 'endpointString=' + epString,
                        success: function(msg) {
                            var index = msg.toString().trim().indexOf('<div>');
                            if (msg.toString().trim().indexOf('<div>Error:') == index) {
                                CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                            } else {
                                directToSubmitDynamicEndpointPage();
                            }
                        }
                    });
    }

    function directToSubmitDynamicEndpointPage() {
        var key = document.getElementById('synRegKey').value;
        var registry;
        if (document.getElementById("config_reg").checked == true) {
            registry = 'conf';
        } else {
            registry = 'gov';
        }

        jQuery.ajax({
                        type: 'POST',
                        url: 'ajaxprocessors/submitDynamicEndpoint-ajaxprocessor.jsp',
                        data: 'registry=' + registry + '&regKey=' + key,
                        success: function(msg) {
                            var index = msg.toString().trim().indexOf('<div>');
                            if (msg.toString().trim().indexOf('<div>Error:') == index) {
                                CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                            } else {
                                location.href = msg.toString().trim().substring(index + 17);
                            }
                        }
                    });
    }

    function directToSubmitPage() {
        jQuery.ajax({
                        type: 'POST',
                        url: 'ajaxprocessors/submitEndpoint-ajaxprocessor.jsp',
                        success: function(msg) {
                            var index = msg.toString().trim().indexOf('<div>');
                            if (msg.toString().trim().indexOf('<div>Error:') == index) {
                                CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                            } else {
                                location.href = msg.toString().trim().substring(index + 17);
                            }
                        }
                    });
    }

    function switchToDesign(url) {

        document.getElementById("xmlPay").value = editAreaLoader.getValue("xmlPay");
        var isValidXML = isValidXml(trim(document.getElementById('xmlPay').value));
        if (!isValidXML) {
            return false;
        }
        var epString = encodeCharacters(trim(document.getElementById('xmlPay').value));

        jQuery.ajax({
                        type: 'POST',
                        url: 'updatePages/sourceView-update.jsp',
                        data: 'endpointString=' + epString,
                        success: function(msg) {
                            var index = msg.toString().trim().indexOf('<div>');
                            if (msg.toString().trim().indexOf('<div>Error:') == index) {
                                CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                            } else {
                                location.href = url;
                            }
                        }
                    });
    }

    function showSaveAsForm(show) {
        var formElem = document.getElementById('saveAsForm');
        if (show) {
            formElem.style.display = "";
        } else {
            formElem.style.display = "none";
        }
    }


</script>

<div id="middle">
    <h2><fmt:message key="source.of.endpoint"/></h2>
    <div id="workArea">
        <form name="sourceViewForm" action="" method="POST">
            <input type="hidden" id="endpointName" name="endpointName" value=""/>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th>
                            <span style="float: left; position: relative; margin-top: 2px;">
                                <fmt:message key="source.of.endpoint"/>
                            </span>
                        <a href="#" class="icon-link"
                           style="background-image: url(images/design-view.gif);"
                           onclick="switchToDesign('<%=designViewUrl%>')"> <fmt:message
                                key="switch.to.design.view"/></a>
                    </th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <textarea id="xmlPay" name="design"
                                  style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                  rows="30"><%=prettyPrintPayload%>
                        </textarea>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" value="<fmt:message key="save"/>" class="button"
                               name="save" onclick="submitEndpointData();"/>
                        <%
                            if (!isAnonymous) {
                        %>
                        <input type="button" value="<fmt:message key="saveas"/>" class="button"
                               name="save"
                               onclick="javascript:showSaveAsForm(true);"/>
                        <%
                            }
                        %>
                        <input type="button" value="<fmt:message key="cancel"/>" name="cancel"
                               class="button"
                               onclick="javascript:cancelEndpointData('<%=anonymousOriginator%>','<%=isFromTemplateEditor%>');"/>
                    </td>
                </tr>
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
                                    <td><fmt:message key="config.registry"/> <input type="radio"
                                                                                    name="registry"
                                                                                    id="config_reg"
                                                                                    value="conf:"
                                                                                    checked="checked"
                                                                                    onclick="document.getElementById('reg').innerHTML='conf:';"/>
                                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                        <fmt:message key="gov.registry"/> <input type="radio"
                                                                                 name="registry"
                                                                                 id="gov_reg"
                                                                                 value="gov:"
                                                                                 onclick="document.getElementById('reg').innerHTML='gov:';"/>


                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="registry.key"/></td>
                                    <td><span id="reg">conf:</span><input type="text" size="75"
                                                                         id="synRegKey"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button"
                                   value="<fmt:message key="save"/>" id="saveSynRegButton"
                                   onclick="javascript:submitDynamicEndpointData(); return false;"/>
                            <input type="button" class="button"
                                   value="<fmt:message key="cancel"/>" id="cancelSynRegButton"
                                   onclick="javascript:showSaveAsForm(false); return false;">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </form>
    </div>
</div>

<script type="text/javascript">
    editAreaLoader.init({
                            id : "xmlPay"
                            ,syntax: "xml"
                            ,start_highlight: true
                        });
</script>
</fmt:bundle>