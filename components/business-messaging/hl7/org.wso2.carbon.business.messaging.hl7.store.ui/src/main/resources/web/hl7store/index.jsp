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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    HL7StoreAdminServiceClient client = new HL7StoreAdminServiceClient(cookie, url, configContext);

    String[] storeNames = client.getStoreNames();

%>

<fmt:bundle basename="org.wso2.carbon.business.messaging.hl7.store.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.business.messaging.hl7.store.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="HL7 Console"
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
        String storeName = request.getParameter("store").trim();
        out.write("var g_store = '" + storeName + "';");
    }
    if(storeNames == null) {
        out.write("var g_avail = false;");
    } else {
        out.write("var g_avail = true;");
    }
    %>
</script>

<script type="text/javascript" src="js/index.js"></script>


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


<div id="middle">
    <h2><fmt:message key="manage.hl7.stores"/></h2>

    <div id="workArea" style="background-color:#FFFFFF;">

        <% if(storeNames == null) { %>
        There are no HL7 message stores.
        <% } else { %>
        <div id="controls">
            <table class="styledLeft" id="controlTable" width="100%">
                <tbody>
                    <tr>
                        <td style="border-right:0px;">
                            <div class="select">
                                <label>Store:
                                <select id="store">
                                    <%
                                        for(String name: storeNames) {
                                    %>
                                    <option><%=name%></option>
                                    <%
                                        }
                                    %>
                                </select>
                                </label>
                                <button id="btnStore">View</button>
                            </div>
                        </td>
                        <td style="border-right:0px;border-left:0px;">
                            <div class="search">
                                <label>Search: <input id="txtSearch" type="text" class="search" /></label><button id="btnSearch">Search</button>
                            </div>
                        </td>
                        <td class="rightAlign" style="border-left:0px;"><span id="purgeMessages" class="pageLink">Purge Messages</span></td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div id="storeTable">
            <div id="loadingDiv" style="text-align:center; padding:20px;">
                Loading...  <img src="images/load.gif" />
            </div>
            <table class="styledLeft" id="messageTable" width="100%">
                <thead>
                <tr><th>Timestamp</th><th>Message ID</th><th>Control ID</th><th>Raw Message</th><th>Actions</th></tr>
                </thead>
                <tbody id="storeTableBody"></tbody>
            </table>
        </div>

        <div id="pagination">
            <table class="styledLeft" id="paginationTable" width="100%">
                <tbody>
                <tr>
                    <td style="border-right:0px;">
                        <div class="search">
                            <label>Filter: <input id="txtFilter" type="text" class="search" /></label><button id="btnFilter">Filter</button>
                        </div>
                    </td>
                    <td class="rightAlign" style="border-left:0px;"> <span id="prevPage" class="pageLink"><< Previous Page</span> | <span id="currentPage">1</span> of <span id="totalPages"></span> | <span id="nextPage" class="pageLink">Next Page >></span></td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <% } %>
</div>


<script type="text/javascript">
    alternateTableRows('storeTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>