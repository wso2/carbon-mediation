<%--
 ~ Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.localentry.ui.client.LocalEntryAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.localentry.stub.types.EntryData" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.localentry.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
               request="<%=request%>" />
<carbon:breadcrumb
        label="localentry"
        resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet" />
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>


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
        if(emtpyEntries){
           $myTabs.tabs('select', 1);
        }
    });
</script>

<script type="text/javascript">
    function deleteRow(i) {
        var table = document.getElementById("myTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var content = cell.firstChild.nodeValue;

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.entry"/>", function() {
            jQuery.ajax({
                type: "POST",
                url: "deleteEntriesHandler-ajaxprocessor.jsp",
                data: {"entryName": content},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("index.jsp");
                    }
                }
            });
        });
    }

    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, "");
    }

    String.prototype.ltrim = function() {
        return this.replace(/^\s+/, "");
    }

    String.prototype.rtrim = function() {
        return this.replace(/\s+$/, "");
    }

    function editRow(i, name) {
        var table = document.getElementById("myTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var type = row.cells[1];
        var content = cell.firstChild.nodeValue;
        var endType = type.firstChild.nodeValue;

        if (endType.trim() == 'Inline Text') {
            document.location.href = "inlinedText.jsp?" + "entryName=" + name;
        }
        else if (endType.trim() == 'Inline XML') {
            document.location.href = "inlinedXML.jsp?" + "entryName=" + name
        }
        else {
            document.location.href = "sourceURL.jsp?" + "entryName=" + name;
        }
    }

    function editCAppEntry(i,name) {
        var table = document.getElementById("myTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var type = row.cells[1];
        var content = cell.firstChild.nodeValue;
        var endType = type.firstChild.nodeValue;
        CARBON.showConfirmationDialog("The changes will not persist to the CAPP after restart or redeploy. Do you want to Edit?", function() {
            $.ajax({
                type: 'POST',
                success: function() {
                    if (endType.trim() == 'Inline Text') {
                        document.location.href = "inlinedText.jsp?" + "entryName=" + name;
                    } else if (endType.trim() == 'Inline XML') {
                        document.location.href = "inlinedXML.jsp?" + "entryName=" + name
                    } else {
                        document.location.href = "sourceURL.jsp?" + "entryName=" + name;
                    }
                }
            });
        });
    }

    function redirect(selNode) {
        var selected = selNode.options[selNode.selectedIndex].value;
        if (selected != "")window.location.href = selNode.options[selNode.selectedIndex].value;
    }

    function confirmForceDelete(entry, msg) {
        CARBON.showConfirmationDialog('<fmt:message key="dependency.mgt.warning"/><br/><br/>'
                + msg + '<br/><fmt:message key="force.delete"/>', function() {
            jQuery.ajax({
                type: "POST",
                url: "deleteEntriesHandler-ajaxprocessor.jsp",
                data: {"entryName": entry, "force": "true"},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("index.jsp");
                    }
                }
            });
        });
    }

</script>

<div id="middle">
    <h2><fmt:message key="manage.local.registry.entries"/></h2>

    <div id="workArea">
        <div id="tabs">
            <ul>
                <li><a href="#tabs-1"><fmt:message key="available.localentries.tab.title"/></a></li>
                <li><a href="#tabs-2"><fmt:message key="add.localentries.tab.title"/></a></li>

            </ul>

            <%
                String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
                        session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                LocalEntryAdminClient client;
                EntryData[] localEntries = null;

                int numberOfPages = 1;
                String pageNumber = request.getParameter("pageNumber");
                if (pageNumber == null) {
                    pageNumber = "0";
                }
                int pageNumberInt = 0;
                try {
                    pageNumberInt = Integer.parseInt(pageNumber);
                }
                catch (NumberFormatException ignored) {
                }

                try {
                    client = new LocalEntryAdminClient(cookie, url, configContext);
                    localEntries = client.getPaginatedEntryData(pageNumberInt);     // Using pagination
                    numberOfPages = (int) Math.ceil((double) client.getEntryDataCount() / LocalEntryAdminClient.LOCAL_ENTRIES_PER_PAGE);

                } catch (Exception e) {
                    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
            %>
            <script type="text/javascript">
                location.href = "../admin/error.jsp";
            </script>
            <%
                }
                if (localEntries == null) {
            %>
            <div id="tabs-1">
                <script type="text/javascript"> emtpyEntries=true</script>
                <fmt:message
                    key="no.entries.are.currently.defined.press.add.local.entry.to.define.a.new.entry.on.the.local.registry"/>
            </div>

            <%}%>

            <%if (localEntries != null) {%>
            <div id="tabs-1">
                <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                                  page="index.jsp" pageNumberParameterName="pageNumber"
                                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                                  prevKey="prev" nextKey="next"
                                  parameters=""/>
                <br/>
                <table id="myTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="enntry.name"/></th>
                        <th><fmt:message key="type"/></th>
                        <th><fmt:message key="description"/></th>
                        <th><fmt:message key="action"/></th>
                    </tr>
                    </thead>
                    <tbody>

                    <% for (EntryData dao : localEntries) {
                        EntryData entry = dao;
                    %>

                    <tr>
                        <td>
                            <% if (entry.getArtifactContainerName() != null) { %>
                                <img src="images/applications.gif">
                                <%= Encode.forHtmlContent(entry.getName())%>
                                <% if(entry.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                            <% } else { %>
                                <%= Encode.forHtmlContent(entry.getName())%>
                            <% } %>
                        </td>
                        <td><%= entry.getType()%>
                        </td>
                        <td><%= entry.getDescription() != null ? entry.getDescription() : "" %>
                        </td>

                        <td>
                            <% if (entry.getArtifactContainerName() != null) { %>
                                <a onclick="editCAppEntry(this.parentNode.parentNode.rowIndex,
                                        '<%=entry.getName()%>' )" href="#" class="icon-link"
                                        style="background-image:url(../admin/images/edit.gif);">
                                        <fmt:message key="edit"/></a>
                                <a href="#" onclick="#"
                                   id="delete_link" class="icon-link"
                                   style="color:gray;background-image:url(../admin/images/delete.gif);"><fmt:message
                                        key="delete"/></a>
                            <% } else { %>
                                <a onclick="editRow(this.parentNode.parentNode.rowIndex,
                                        '<%=entry.getName()%>' )" href="#" class="icon-link"
                                        style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                        key="edit"/></a>
                                <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                                   id="delete_link" class="icon-link"
                                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                        key="delete"/></a>
                            <% } %>
                        </td>
                    </tr>
                    <%}%>
                    </tbody>
                </table>
                <%}%>
                <br/>
                <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                                  page="index.jsp" pageNumberParameterName="pageNumber"
                                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                                  prevKey="prev" nextKey="next"
                                  parameters=""/>
                <br/>
                <br/>
            </div>
            <div id="tabs-2">

                <table id="localentryTypesTable" cellspacing="0" class="styledLeft" id="templatesTable">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="add.localentry.title"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td width="20%">
                            <a class="icon-link"
                               href="inlinedText.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="add.in.lined.text"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="add.in.lined.text.desc"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a class="icon-link"
                               href="inlinedXML.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="add.in.lined.xml"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="add.in.lined.xml.desc"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a class="icon-link"
                               href="sourceURL.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="add.source.url"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="add.source.url.desc"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <%
                    String dependencyMgtError = (String) session.getAttribute("d.mgt.error.msg");
                    if (dependencyMgtError != null) {
                        String entryToDelete = (String) session.getAttribute("d.mgt.error.entry.name");
                %>
                <script type="text/javascript">
                    confirmForceDelete('<%=entryToDelete%>', '<%=dependencyMgtError%>');
                </script>
                <%
                        session.removeAttribute("d.mgt.error.msg");
                        session.removeAttribute("d.mgt.error.entry.name");
                    }
                %>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('localentryTypesTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>