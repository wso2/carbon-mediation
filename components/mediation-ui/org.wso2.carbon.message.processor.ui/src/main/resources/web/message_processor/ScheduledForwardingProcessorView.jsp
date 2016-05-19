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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageStoreAdminServiceClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.processor.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="Message Processor"
        resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<link type="text/css" href="css/customStyle.css" rel="stylesheet"/>
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
        if (emtpyEntries) {
            $myTabs.tabs('select', 1);
        }
    });
</script>

<script type="text/javascript">
    function deleteRow(i) {
        var table = document.getElementById("msgTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var content = cell.firstChild.nodeValue;
        var msName = document.getElementById("messageProcessorName_elem").value;

        function delEp() {
            $.ajax({
                type: 'POST',
                url: 'deleteMessage.jsp',
                data: "processorName=" + msName + "&messageId=" + content
            });
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.message"/>", delEp);
    }


    function resendRow(i) {
        var table = document.getElementById("msgTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var content = cell.firstChild.nodeValue;
        var dlcName = document.getElementById("messageProcessorName_elem").value;

        function delEp() {
            $.ajax({
                type: 'POST',
                url: 'resendMessage.jsp',
                data: "processorName=" + dlcName + "&messageId=" + content
            });
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.the.message"/>", delEp);
    }

    function resendFirstRow() {
        var table = document.getElementById("msgTable");
        var row = table.rows[1];
        var cell = row.cells[0];
        var content = cell.firstChild.nodeValue;
        var dlcName = document.getElementById("messageProcessorName_elem").value;

        function delEp() {
            $.ajax({
                type: 'POST',
                url: 'resendFirstMessage.jsp',
                data: "processorName=" + dlcName + "&messageId=" + content
            });
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.first.message"/>", delEp);
    }

    function viewEnvRow(i) {
        var table = document.getElementById("msgTable");
        var index = i;
        var row = table.rows[i];
        var cell = row.cells[0];
        var type = row.cells[1];
        var content = cell.firstChild.nodeValue;
        var msName = document.getElementById("messageProcessorName_elem").value;
        document.location.href = "viewEnvelope.jsp?" + "processorName=" + msName + "&messageIndex=" + index + "&messageId=" + content;

    }

    function deleteAll() {
        var msName = document.getElementById("messageProcessorName_elem").value;

        function delEp() {
            $.ajax({
                type: 'POST',
                url: 'deleteAll.jsp',
                data: "processorName=" + msName
            });
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.all.messages"/>", delEp);

    }

    function deleteFirst() {
        var msName = document.getElementById("messageProcessorName_elem").value;

        function delEp() {
            $.ajax({
                type: 'POST',
                url: 'deleteFirstMessage.jsp',
                data: "processorName=" + msName
            });
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.first.messages"/>", delEp);

    }

    function resendAll() {

        var pName = document.getElementById("messageProcessorName_elem").value;

        function delEp() {
            $.ajax({
                type: 'POST',
                url: 'resendAll.jsp',
                data: "processorName=" + pName
            });
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.all.messages"/>", delEp);
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


    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, "");
    }

    String.prototype.ltrim = function() {
        return this.replace(/^\s+/, "");
    }

    String.prototype.rtrim = function() {
        return this.replace(/\s+$/, "");
    }


    function redirect(selNode) {
        var selected = selNode.options[selNode.selectedIndex].value;
        if (selected != "")window.location.href = selNode.options[selNode.selectedIndex].value;
    }

    function confirmForceDelete(entry, msg) {
        CARBON.showConfirmationDialog('<fmt:message key="dependency.mgt.warning"/><br/><br/>'
                + msg + '<br/><fmt:message key="force.delete"/>', function() {
            $.ajax({
                type: 'POST',
                url: 'deleteMessageStoresHandler.jsp',
                data: "entryName=" + entry + "&force=true"
            });
        });
    }

</script>

<div id="middle">
<h2><fmt:message key="manage.message.Processor"/></h2>

<div id="workArea" style="background-color:#F4F4F4;">
    <div id="tabs">
        <%
            String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
                    session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            MessageProcessorAdminServiceClient client = null;
            MessageStoreAdminServiceClient messageStoreClient = null;
            String[] messageProcessorNames = null;
            String messageProcessorName = null;
            String[] messageIds = null;

            int numberOfPages = 1;
            String pageNumber = request.getParameter("pageNumber");
            if (pageNumber == null) {
                pageNumber = "0";
            }
            int pageNumberInt = 0;
            try {
                pageNumberInt = Integer.parseInt(pageNumber);
            } catch (NumberFormatException ignored) {
            }

            try {
                client = new MessageProcessorAdminServiceClient(cookie, url, configContext);
                messageProcessorNames = client.getMessageProcessorNames();
                messageStoreClient = new MessageStoreAdminServiceClient(cookie, url, configContext);

                String name = request.getParameter("processorName");
                if (messageProcessorNames != null && name != null) {
                    for (String n : messageProcessorNames) {
                        if (name.equals(n)) {
                            messageProcessorName = n;
                        }
                    }


                } else {
                    throw new Exception("Error while accessing Message Processors ");
                }

                messageIds = client.getPaginatedMessages(messageProcessorName, pageNumberInt);
                numberOfPages = (int) Math.ceil((double)
                        client.getSize(messageProcessorName) / MessageProcessorAdminServiceClient.
                        MESSAGE_PROCESSORS_PER_PAGE);

            } catch (Exception e) {
                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        %>


        <script type="text/javascript">
            location.href = "../admin/error.jsp";
        </script>
        <%
            }
            if (messageProcessorName == null) {
                CarbonUIMessage.sendCarbonUIMessage("Error while accessing the Message Processor",
                        CarbonUIMessage.ERROR, request,
                        new Exception("Error while accessing the Message Processor"));
            }
        %>


        <%
            if (messageProcessorName != null) {
                String myUrl = "processorName=" + messageProcessorName;
        %>
        <input id="messageProcessorName_elem" type="hidden" value="<%=messageProcessorName%>"/>
        <table id="messageProcessorInfoTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="name"/></th>
                <th><fmt:message key="type"/></th>
                <th><fmt:message key="size"/></th>
            </tr>
            </thead>
            <tbody>
            <%
                String type = "Not defined";
                int size = 0;
                String messageStoreName = null;
                try {
                    size = client.getSize(messageProcessorName);
                    type = client.getClassName(messageProcessorName);
                    messageStoreName = messageStoreClient.getMessageStoreClass(client.getMessageProcessor(messageProcessorName).getMessageStore());
                } catch (Exception e) {

                }

            %>

            <tr>
                <td><%= messageProcessorName%>
                </td>
                <td><%= type%>
                </td>
                <td><%= size%>
                </td>
            </tr>
            </tbody>
        </table>
        <br/>

        <h3><fmt:message key="stored.messages"/></h3>
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          page="ScheduledForwardingProcessorView.jsp"
                          pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=myUrl%>" showPageNumbers="false"/>

        <table id="msgTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">
            <thead>
            <tr>
                <th><fmt:message key="message.id"/></th>
                <th><fmt:message key="action"/></th>
            </tr>
            </thead>
            <tbody>
            <%
                try {
                    for (String mi : messageIds) {
            %>

            <tr>
                <td><%=mi%>
                </td>
                <td><a onclick="viewEnvRow(this.parentNode.parentNode.rowIndex)" href="#"
                       class="icon-link"
                       style="background-image:url(../message_processor/images/envelop.gif);"><fmt:message
                        key="view.envelope"/></a>
                    <%if (!messageStoreName.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")) { %>
                    <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                       id="delete_link" class="icon-link"
                       style="background-image:url(../message_processor/images/delete_22.gif);"><fmt:message
                            key="delete"/></a>
                    <a href="#" onclick="resendRow(this.parentNode.parentNode.rowIndex)"
                       id="resend_link" class="icon-link"
                       style="background-image:url(../message_processor/images/resend.gif);"><fmt:message
                            key="resend"/></a>
                    <%}%>
                </td>
            </tr>
            <%
                    }
                } catch (Exception e) {
                    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
                }

            %>
            <tr>
                <td>
                    <%if (messageStoreName.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")) { %>
                    <a onclick="<%=(messageIds == null || messageIds.length == 0)?"return false":"deleteFirst();"%>"
                       href="#" id="delete_first_link"
                       class="<%=(messageIds == null || messageIds.length == 0)?"icon-link-disabled":"icon-link"%>"
                       style="background-image:url(../message_processor/images/delete_22.gif);"><fmt:message
                            key="deleteFirst"/></a>
                    <a onclick="<%=(messageIds == null || messageIds.length == 0)?"return false":"resendFirstRow();"%>"
                       href="#" id="resend_First_link"
                       class="<%=(messageIds == null || messageIds.length == 0)?"icon-link-disabled":"icon-link"%>"
                       style="background-image:url(../message_processor/images/resend.gif);"><fmt:message
                            key="resendFirst"/></a>
                    <%}%>
                    <a onclick="<%=(messageIds == null || messageIds.length == 0)?"return false":"deleteAll();"%>"
                       href="#" id="delete_all_link"
                       class="<%=(messageIds == null || messageIds.length == 0)?"icon-link-disabled":"icon-link"%>"
                       style="background-image:url(../message_processor/images/delete_22.gif);"><fmt:message
                            key="deleteAll"/></a>
                    <a onclick="<%=(messageIds == null || messageIds.length == 0)?"return false":"resendAll();"%>"
                       href="#" id="resend_all_link"
                       class="<%=(messageIds == null || messageIds.length == 0)?"icon-link-disabled":"icon-link"%>"
                       style="background-image:url(../message_processor/images/resend.gif);"><fmt:message
                            key="resendAll"/></a>
                </td>
            </tr>

            </tbody>
        </table>
        <br/>
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                          page="ScheduledForwardingProcessorView.jsp"
                          pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=myUrl%>" showPageNumbers="false"/>

        <%}%>

    </div>
</div>
<script type="text/javascript">
    alternateTableRows('msgTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('messageStoreInfoTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>