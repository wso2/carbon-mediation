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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<!-- Dependencies -->
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>

<!-- Connection handling lib -->
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<!-- Source File -->
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">

    <carbon:breadcrumb
            label="service.proxy.menu.text"
            resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="proxyi18n"/>

    <script type="text/javascript" src="inc/utils.js"></script>
    <script type="text/javascript" src="js/proxyservices.js"></script>

    <script type="text/javascript">
        function createProxy() {
            if (validateProxyName() && validateWSDLOptions() &&
                validateTransports() && validateWsdlEndpoint()) {
                proxy_form.action = "template_wsdl-based.jsp";
                proxy_form.submit();
                return true;
            }
            return false;
        }

        function testWsdlURIForEndpoint() {
            var btn = document.getElementById('testMainConnBtn');
            btn.disabled = 'true';
            var wsdlUri = document.getElementById('main_wsdl_url').value;
            if (wsdlUri == '') {
                CARBON.showWarningDialog(proxyi18n["invalid.wsdl.uri"]);
                btn.removeAttribute('disabled');
            } else {
                jQuery.get("testConnection-ajaxprocessor.jsp", {'url' : wsdlUri},
                        function(data,status) {
                            if (data.replace(/^\s+|\s+$/g, '') != 'success') {
                                CARBON.showErrorDialog(proxyi18n["invalid.wsdl.uri2"]);
                            } else {
                                CARBON.showInfoDialog(proxyi18n["wsdl.uri.ok"]);
                            }
                            btn.removeAttribute('disabled');
                        });
            }
        }

        function validateWsdlEndpoint() {
            var val = document.getElementById('main_wsdl_url').value;
            if (val == null || val == '') {
                CARBON.showErrorDialog(proxyi18n['no.wsdl.url']);
                return false;
            }

            val = document.getElementById('wsdl_service').value;
            if (val == null || val == '') {
                CARBON.showErrorDialog(proxyi18n['no.wsdl.service']);
                return false;
            }

            val = document.getElementById('wsdl_port').value;
            if (val == null || val == '') {
                CARBON.showErrorDialog(proxyi18n['no.wsdl.port']);
                return false;
            }
            return true;
        }

        function publishSameWSDL() {
            var chk = document.getElementById('publish_same');
            var combo = document.getElementById('publishWsdlCombo');
            var txtBox = document.getElementById('wsdlUriText');

            if (chk.checked) {
                combo.selectedIndex = 2;
                txtBox.value = document.getElementById('main_wsdl_url').value;
                hideElem('wsdlInline');
                showElem('wsdlUri');
                hideElem('wsdlReg');
                showElem('wsdlResourceTr');

                disableItem('publishWsdlCombo');
                disableItem('wsdlInlineText');
                disableItem('wsdlUriText');
                disableItem('regBrowserLink');
                disableItem('testConnBtn');
            } else {
                combo.selectedIndex = 0;
                txtBox.value = '';
                hideElem('wsdlInline');
                hideElem('wsdlUri');
                hideElem('wsdlReg');
                hideElem('wsdlResourceTr');

                enableItem('publishWsdlCombo');
                enableItem('wsdlInlineText');
                enableItem('wsdlUriText');
                enableItem('regBrowserLink');
                enableItem('testConnBtn');
            }
        }

        function enableItem(id) {
            document.getElementById(id).removeAttribute('disabled');
        }

        function disableItem(id) {
            document.getElementById(id).disabled = 'true';
        }
    </script>

    <%
        String proxyName = null;
        String wsdlURL = null;
        String wsdlService = null;
        String wsdlPort = null;
        boolean publishSame = false;

        boolean submitted = "true".equals(request.getParameter("formSubmitted"));

        if (submitted) {
            try {
                proxyName = request.getParameter("proxyName");
                if (proxyName == null || "".equals(proxyName)) {
                    throw new Exception("The proxy service name has not been specified");
                }

                wsdlURL = request.getParameter("mainWsdlURL");
                if (wsdlURL == null || "".equals(wsdlURL)) {
                    throw new Exception("The WSDL URL has not been specified");
                }

                URL url = new URL(wsdlURL);

                wsdlService = request.getParameter("wsdlService");
                if (wsdlService == null || "".equals(wsdlService)) {
                    throw new Exception("The WSDL service has not been specified");
                }

                wsdlPort = request.getParameter("wsdlPort");
                if (wsdlPort == null || "".equals(wsdlPort)) {
                    throw new Exception("The WSDL port has not been specified");
                }

                publishSame = request.getParameter("publishSameWsdl") != null;

                String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                ProxyServiceAdminClient client = new ProxyServiceAdminClient(
                        configContext, backendServerURL, cookie, request.getLocale());

                ProxyData proxy = new ProxyData();
                proxy.setStartOnLoad(true);
                proxy.setName(proxyName);
                proxy.setEndpointXML("<endpoint xmlns=\"http://ws.apache.org/ns/synapse\"><wsdl uri=\"" + url.toString() + "\" " +
                        "service=\"" + wsdlService + "\" port=\"" + wsdlPort + "\"/></endpoint>");
                proxy.setOutSeqXML("<outSequence xmlns=\"http://ws.apache.org/ns/synapse\"><send/></outSequence>");

                if (publishSame) {
                    request.setAttribute("processResourcesOnly", "true");
                    proxy.setWsdlAvailable(true);
                    proxy.setWsdlURI(wsdlURL);
                }

                request.setAttribute("proxyDataObject", proxy);
    %>
    <jsp:include page="inc/publish_wsdl_processer.jsp"/>
    <%
        if (request.getAttribute("proxyCreationError") != null) {
            throw new Exception((String) request.getAttribute("proxyCreationError"));
        }
    %>
    <jsp:include page="inc/transports_processer.jsp"/>
    <%
        if (request.getAttribute("proxyCreationError") != null) {
            throw new Exception((String) request.getAttribute("proxyCreationError"));
        }
        client.addProxy(proxy);
        ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources");
        CarbonUIMessage.sendCarbonUIMessage(
                bundle.getString("proxy.add.success") + " " + proxy.getName(),
                CarbonUIMessage.INFO, request);
    %>
    <script type="text/javascript">
        proxyCreated('<%=proxyName%>');
    </script>
    <%
    } catch (Exception e) {
        String cause;
        if (e.getCause() != null) {
            cause = e.getCause().getMessage();
            cause = cause.replaceAll("\n|\\r|\\t|\\f", "");
        } else {
            cause = e.getMessage();
        }
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog("<%=cause%>");
    </script>
    <%
            }
        }
    %>

    <jsp:include page="inc/metadata.jsp"/>

    <div id="middle">
        <h2><fmt:message key="wsdl-based"/></h2>
        <div id="workArea">
            <p>
                <fmt:message key="wsdl-based.desc"/>
            </p>
            &nbsp;
            <form id="proxy_form" method="POST" action="">
                <input type="hidden" name="formSubmitted" value="true"/>
                <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="proxy.settings"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td colspan="2" style="padding-bottom:10px;">
                            <table>
                                <tr>
                                    <td style="width:130px;"><fmt:message key="proxy.service.name"/><span class="required">*</span></td>
                                    <td><input id="proxy_name" type="text" name="proxyName" size="40" onkeypress="return validateProxyNameText(event)"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="wsdl.uri"/><span class="required">*</span></td>
                                    <td>
                                        <input id="main_wsdl_url" type="text" name="mainWsdlURL" size="60"/>
                                        <button id="testMainConnBtn" onclick="testWsdlURIForEndpoint(); return false;" class="button">Test URI</button>
                                    </td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="wsdl.service"/><span class="required">*</span></td>
                                    <td><input id="wsdl_service" name="wsdlService" type="text" size="40"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="wsdl.port"/><span class="required">*</span></td>
                                    <td><input id="wsdl_port" name="wsdlPort" type="text" size="40"/></td>
                                </tr>
                                <tr>
                                    <td><fmt:message key="publish.same.wsdl"/></td>
                                    <td><input type="checkbox" name="publishSameWsdl" id="publish_same" onchange="publishSameWSDL();"/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="inc/publish_wsdl.jsp?templateType=wsdl"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <jsp:include page="inc/transports.jsp"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <button class="button" onclick="createProxy(); return false;"><fmt:message key="create"/></button>
                            <button class="button" onclick="templatesHome(); return false;"><fmt:message key="cancel"/></button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>

    <%
        if (submitted) {
            if (proxyName != null) {
    %>
        <script type="text/javascript">
            document.getElementById('proxy_name').value = '<%=proxyName%>';
        </script>
    <%
            }

            if (wsdlURL != null) {
    %>
        <script type="text/javascript">
            document.getElementById('main_wsdl_url').value = '<%=wsdlURL%>';
        </script>
    <%
            }

            if (wsdlPort != null) {
    %>
        <script type="text/javascript">
            document.getElementById('wsdl_port').value = '<%=wsdlPort%>';
        </script>
    <%
            }

            if (wsdlService != null) {
    %>
        <script type="text/javascript">
            document.getElementById('wsdl_service').value = '<%=wsdlService%>';
        </script>
    <%
            }

            if (publishSame) {
    %>
        <script type="text/javascript">
            document.getElementById('publish_same').checked = 'true';
            publishSameWSDL();
        </script>
    <%
            }
        }
    %>

</fmt:bundle>