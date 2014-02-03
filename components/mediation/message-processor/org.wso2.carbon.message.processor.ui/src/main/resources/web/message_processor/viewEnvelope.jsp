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
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageStoreAdminServiceClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.processor.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="Message Processor"
        resourceBundle="org.wso2.carbon.message.store.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript" src="../editarea/edit_area_full.js"></script>

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
        var dlcName = document.getElementById("dlcName").value;

        function delEp() {
            document.location.href = "deleteMessage.jsp?" + "dlcName=" + dlcName + "&messageId=" + content;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.message"/>", delEp);
    }


    function resendRow(i) {
        var table = document.getElementById("msgTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var content = cell.firstChild.nodeValue;
        var dlcName = document.getElementById("dlcName").value;

        function delEp() {
            document.location.href = "resendMessage.jsp?" + "dlcName=" + dlcName + "&messageId=" + content;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.the.message"/>", delEp);
    }

    function resendFirstMsg() {
        var processorName = document.getElementById("processorName").value;
        var messageId = document.getElementById("messageId").value;

        function delEp() {
            document.location.href = "resendFirstMessage.jsp?" + "processorName=" + processorName + "&messageId=" + messageId;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.first.message"/>", delEp);
    }

    function resendMsg() {
        var processorName = document.getElementById("processorName").value;
        var messageId = document.getElementById("messageId").value;
        var messageContent = document.getElementById("messageContent").value;

        function delEp() {
            document.editform.processorName = processorName;
            document.editform.messageId = messageId;
            document.editform.messageContent = messageContent;
            document.editform.submit();
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.the.message"/>", delEp);
    }


    function deleteMsg() {
        var mpName = document.getElementById("processorName").value;
        var messageId = document.getElementById("messageId").value;

        function delEp() {
            document.location.href = "deleteMessage.jsp?" + "processorName=" + mpName + "&messageId=" + messageId;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.message"/>", delEp);

    }

    function deleteFirstMsg() {
        var mpName = document.getElementById("processorName").value;

        function delEp() {
            document.location.href = "deleteFirstMessage.jsp?" + "processorName=" + mpName;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.first.messages"/>", delEp);

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
            document.location.href = "deleteDLCsHandler.jsp?" + "entryName=" + entry + "&force=true";
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
                String mpName = null;
                String messageId = null;
                String messageIndex = null;
                String xml = null;
                int xmlLines = 20;
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
                    messageStoreClient = new MessageStoreAdminServiceClient(cookie, url, configContext);

                    mpName = request.getParameter("processorName");
                    messageId = request.getParameter("messageId");
                    messageIndex = request.getParameter("messageIndex");


                } catch (Exception e) {
                    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
            %>


            <script type="text/javascript">
                location.href = "../admin/error.jsp";
            </script>
            <%
                }
                if (mpName == null || messageId == null) {
                    CarbonUIMessage.sendCarbonUIMessage("Error while accessing the Message Processor",
                            CarbonUIMessage.ERROR, request, new Exception("Error while accessing the Message Processor"));
                }
            %>


            <%if (mpName != null) {%>
            <table id="processorInfoTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">
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
                        size = client.getSize(mpName);
                        type = client.getClassName(mpName);
                        messageStoreName = messageStoreClient.getMessageStoreClass(client.getMessageProcessor(mpName).getMessageStore());
                    } catch (Exception e) {

                    }

                %>

                <tr>
                    <td><%= mpName%>
                    </td>
                    <td><%= type%>
                    </td>
                    <td><%= size%>
                    </td>
                </tr>
                </tbody>
            </table>
            <br/>
            <%
                if (messageId != null) {

                    try {
                        xml = client.getEnvelope(mpName, messageId);
                        XMLPrettyPrinter printer =
                                new XMLPrettyPrinter(new ByteArrayInputStream(xml.getBytes()));
                        xml = printer.xmlFormat();
                        String[] lineArray = xml.split("\n");

                        if (lineArray.length > 1) {
                            xmlLines = lineArray.length + 1;
                        }


                    } catch (Exception e) {
                        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
                    }
                }
            %>
            <h3><fmt:message key="message.content"/></h3>

            <form name="editform" action="resendMessage.jsp" method="POST">
                <table id="msgTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">

                    <tbody>
                    <tr>
                        <textarea id="messageContent" name="messageContent" cols="100" rows="18">
                            <%= xml%>
                        </textarea>
                        <input id="processorName" name="processorName" type="hidden" value="<%=mpName%>"/>
                        <input id="messageId" name="messageId" type="hidden" value="<%=messageId%>"/>
                    </tr>
                    <tr>
                        <td>
                            <%if (!(!messageIndex.trim().equals("1") && messageStoreName.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore"))) {%>
                            <a onclick="<%=messageStoreName.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")?"deleteFirstMsg();":"deleteMsg();"%>"
                               href="#" id="delete_all_link" class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                    key="delete"/></a>
                            <a onclick="<%=messageStoreName.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")?"resendFirstMsg();":"resendMsg();"%>"
                               href="#" id="resend_all_link" class="icon-link"
                               style="background-image:url(../admin/images/resend.gif);"><fmt:message
                                    key="resend"/></a>
                            <%}%>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
            <br/>

            <%}%>

        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('msgTable', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('processorInfoTable', 'tableEvenRow', 'tableOddRow');
    </script>
    <script type="text/javascript">
        editAreaLoader.init({
            id : "messageContent"        // textarea id
            ,syntax: "xml"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
        });
    </script>
</fmt:bundle>