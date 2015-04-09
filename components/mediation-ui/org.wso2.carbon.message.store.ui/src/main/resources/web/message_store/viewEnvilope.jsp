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
<%@ page import="org.wso2.carbon.message.store.ui.MessageStoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.wso2.carbon.message.store.stub.MessageStoreAdminService" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.store.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.store.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="Message Store"
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

    function resendMsg() {
        var dlcName = document.getElementById("dlcName").value;
        var messageId = document.getElementById("messageId").value;
        var messageContent = document.getElementById("messageContent").value;

        function delEp() {
            document.editform.dlcName = dlcName;
            document.editform.messageId = messageId;
            document.editform.messageContent = messageContent;
            document.editform.submit();
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.the.message"/>", delEp);
    }

    function deleteMsg() {
        var msName = document.getElementById("msName").value;
        var messageId = document.getElementById("messageId").value;

        function delEp() {
            document.location.href = "deleteMessage.jsp?" + "messageStoreName=" + msName + "&messageId=" + messageId;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.message"/>", delEp);

    }

    function deleteFirstMsg() {
        var msName = document.getElementById("msName").value;

        function delEp() {
            document.location.href = "deleteFirstMessage.jsp?" + "messageStoreName=" + msName;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.first.messages"/>", delEp);

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
    <h2><fmt:message key="manage.message.store"/></h2>

    <div id="workArea" style="background-color:#F4F4F4;">
        <div id="tabs">
            <%
                String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
                        session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                MessageStoreAdminServiceClient client = null;
                String msName = null;
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
                    client = new MessageStoreAdminServiceClient(cookie, url, configContext);

                    msName = request.getParameter("messageStoreName");
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
                if (msName == null || messageId == null) {
                    CarbonUIMessage.sendCarbonUIMessage("Error while accessing the Message Store",
                            CarbonUIMessage.ERROR, request, new Exception("Error while accessing the Message Store"));
                }
            %>


            <%if (msName != null) {%>
            <table id="dlcInfoTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="messageStore.name"/></th>
                    <th><fmt:message key="type"/></th>
                    <%if (!client.getClassName(msName).trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore") && !client.getClassName(msName).trim().equals("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore")) {%>
                        <th><fmt:message key="size"/></th>
                    <%}%>
                </tr>
                </thead>
                <tbody>
                <%
                    String type = "Not defined";
                    int size = 0;
                    try {
                        size = client.getSize(msName);
                        type = client.getClassName(msName);
                    } catch (Exception e) {

                    }

                %>

                <tr>
                    <td><%= msName%>
                    </td>
                    <td><%= type%>
                    </td>
                    <%if (!type.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore") && !type.trim().equals("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore")) {%>
                        <td><%= size%>
                        </td>
                    <%}%>
                </tr>
                </tbody>
            </table>
            <br/>
            <%
                if (messageId != null) {

                    try {
                        xml = client.getEnvelope(msName, messageId);
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

            <div id="workArea">
                <form name="editform" action="resendMessage.jsp" method="POST">
                    <table id="msgTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">

                        <tbody>
                        <tr>
                            <textarea id="messageContent" name="messageContent" cols="100" rows="18">
                                <%= xml%>
                            </textarea>
                            <input id="msName" name="msName" type="hidden" value="<%=msName%>"/>
                            <input id="messageId" name="messageId" type="hidden" value="<%=messageId%>"/>
                        </tr>
                        <tr>
                            <td>
                                <%if (!type.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore") && !type.trim().equals("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore")) {%>
                                <input type="button" value="<fmt:message key="delete"/>"
                                       onclick="<%="deleteMsg();"%>"
                                       class="button"/>
                                <%}%>
                                <input type="button" value="<fmt:message key="cancel"/>"
                                       onclick="javascript:document.location.href='viewMessageStore.jsp?messageStoreName=<%=msName%>'"
                                       class="button"/>
                            </td>
                        </tr>

                        </tbody>
                    </table>
                </form>
            </div>
            <br/>

            <%}%>

        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('msgTable', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('dlcInfoTable', 'tableEvenRow', 'tableOddRow');
    </script>
    <script type="text/javascript">
        editAreaLoader.init({
            id : "messageContent"        // textarea id
            ,syntax: "xml"            // syntax to be uses for highgliting
            ,start_highlight: true        // to display with highlight mode on start-up
            ,is_editable :false // make it readonly
            ,allow_toggle:false // disable toggle view
        });
    </script>
</fmt:bundle>