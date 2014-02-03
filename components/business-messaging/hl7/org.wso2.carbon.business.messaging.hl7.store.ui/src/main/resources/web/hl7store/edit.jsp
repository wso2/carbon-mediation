<%--
 ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ page import="org.wso2.carbon.business.messaging.hl7.store.ui.HL7StoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.business.messaging.hl7.store.entity.xsd.TransferableHL7Message" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    HL7StoreAdminServiceClient client = new HL7StoreAdminServiceClient(cookie, url, configContext);

    String storeName = request.getParameter("store").trim();
    String uuid = request.getParameter("uuid").trim();

    TransferableHL7Message message = client.getMessage(storeName, uuid);

%>

<fmt:bundle basename="org.wso2.carbon.business.messaging.hl7.store.ui.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.business.messaging.hl7.store.ui.i18n.Resources"
                   request="<%=request%>"/>
    <carbon:breadcrumb
            label="Edit Message"
            resourceBundle="org.wso2.carbon.business.messaging.hl7.store.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
    <link type="text/css" href="css/style.css" rel="stylesheet"/>

    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>

    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

    <script type="text/javascript" src="js/jquery-1.10.2.min.js"></script>
    <script type="text/javascript">
        var jqNew = $.noConflict(true);
    </script>

    <script type="text/javascript">
        <%
        if (request.getParameter("store") != null) {
            out.write("var g_store = '" + storeName + "';");
        }
        %>
    </script>

    <script type="text/javascript" src="js/edit.js"></script>


    <script type="text/javascript">
        var allowTabChange = true;
        var emtpyEntries = false;

        $(function() {
            var $myTabs = $("#tabs");

            $myTabs.tabs({
                select: function(event, ui) {
                    if (!allowTabChange) {
                        alert("Tab selection is disabled, while you are in the middle of a workflow");
                    }
                    return allowTabChange;
                },

                show: function(event, ui) {
                    var selectedTab = $myTabs.tabs('option', 'selected');
                    allowTabChange = true;
                }
            });

            $myTabs.tabs('select', 0);
            if (emtpyEntries) {
                $myTabs.tabs('select', 1);
            }
        });
    </script>

    <script type="text/javascript">



    </script>


    <div id="middle">
        <h2><fmt:message key="manage.hl7.stores"/></h2>

        <div id="workArea" style="background-color:#FFFFFF;">

            <div id="controls">

                <table class="styledLeft" id="userTable" width="100%">
                    <tbody id="messageEdit">
                        <tr><td>Store</td><td><a href="index.jsp?store=<%=message.getStoreName()%>"><%=message.getStoreName()%></a></td><td></td></tr>
                        <tr><td>Timestamp</td><td><%=message.getDate()%></td><td></td></tr>
                        <tr class="shortRow"></tr>
                        <tr><td>Message ID</td><td><%=message.getMessageId()%></td><td></td></tr>
                        <tr><td>Control ID</td><td><%=message.getControlId()%></td><td></td></tr>
                        <tr class="shortRow"></tr>
                        <tr><td>ER7 Encoding</td><td><textarea id="rawMessage" class="er7TextArea"><%=message.getRawMessage()%></textarea></td><td><button id="btnRawMessage">Update</button></td></tr>
                        <tr><td>XML Encoding</td><td><textarea id="xmlMessage" class="xmlTextArea"><%=message.getEnvelope()%></textarea></td><td><button id="btnXmlMessage">Update</button></td></tr>
                    </tbody>
                </table>

                <table class="styledLeft" id="controlTable" width="100%">
                    <tr>
                        <td style="width: 118px;"></td>

                        <td>
                            Select Proxy: <select id="proxyList"></select>
                            <button id="btnSend">Send</button>
                        </td>
                    </tr>
                </table>

            </div>

        </div>
    </div>


    <script type="text/javascript">
        alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('dlcTable', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>