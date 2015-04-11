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
<%@ page import="org.wso2.carbon.message.store.stub.MessageInfo" %>
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
            var msName = document.getElementById("messageStoreName_elem").value;

            function delEp() {
                document.location.href = "deleteMessage.jsp?" + "messageStoreName=" + msName + "&messageId=" + content;
            }

            CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.message"/>", delEp);
        }

        function deleteFirstRow() {
            var msName = document.getElementById("messageStoreName_elem").value;

            function delEp() {
                document.location.href = "deleteFirstMessage.jsp?" + "messageStoreName=" + msName;
            }

            CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.first.messages"/>", delEp);

        }


        function resendRow(i) {
            var table = document.getElementById("msgTable");
            var row = table.rows[i];
            var cell = row.cells[0];
            var content = cell.firstChild.nodeValue;
            var dlcName = document.getElementById("messageStoreName_elem").value;

            function delEp() {
                document.location.href = "resendMessage.jsp?" + "messageStoreName=" + dlcName + "&messageId=" + content;
            }

            CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.resend.the.message"/>", delEp);
        }

        function viewEnvRow(i) {
            var table = document.getElementById("msgTable");
            var index = i;
            var row = table.rows[i];
            var cell = row.cells[0];
            var type = row.cells[1];
            var content = cell.firstChild.nodeValue;
            var msName = document.getElementById("messageStoreName_elem").value;
            document.location.href = "viewEnvilope.jsp?" + "messageStoreName=" + msName + "&messageIndex=" + index + "&messageId=" + content ;

        }

        function deleteAll() {
            var msName = document.getElementById("messageStoreName_elem").value;

            function delEp() {
                document.location.href = "deleteAll.jsp?" + "messageStoreName=" + msName;
            }

            CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.all.messages"/>", delEp);

        }

        function resendAll() {

            var dlcName = document.getElementById("messageStoreName_elem").value;

            function delEp() {
                document.location.href = "resendAll.jsp?" + "dlcName=" + dlcName;
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
                document.location.href = "deleteMessageStoresHandler.jsp?" + "entryName=" + entry + "&force=true";
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
                    String[] messageStoreNames = null;
                    String messageStoreName = null;
                    MessageInfo[] messageInfos = null;
                    int numberOfMessageStores = 0;
                    boolean displayMessageInDetail = true;

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
                        client = new MessageStoreAdminServiceClient(cookie, url, configContext);
                        messageStoreNames = client.getMessageStoreNames();

                        String name = request.getParameter("messageStoreName");
                        if (messageStoreNames != null && name != null) {
                            for (String n : messageStoreNames) {
                                if (name.equals(n)) {
                                    messageStoreName = n;
                                }
                            }


                        } else {
                            throw new Exception("Error while accessing Message Stores ");
                        }

                        // Hide messages in details div when JDBC Message Store is used
                        if(client.getClassName(messageStoreName).trim().equals("org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore")){
                            displayMessageInDetail = false;
                        }

                        // Hide messages in details div when RabbitMQ is used
                        if(client.getClassName(messageStoreName).trim().equals("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore")){
                            displayMessageInDetail = false;
                        }

                        messageInfos = client.getPaginatedMessages(messageStoreName, pageNumberInt);
                        numberOfPages = (int) Math.ceil((double)
                                                                client.getSize(messageStoreName) / MessageStoreAdminServiceClient.
                                                                MESSAGE_STORES_PER_PAGE);

                    } catch (Exception e) {
                        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
                %>


                <script type="text/javascript">
                    location.href = "../admin/error.jsp";
                </script>
                <%
                    }
                    if (messageStoreName == null) {
                        CarbonUIMessage.sendCarbonUIMessage("Error while accessing the Message Store",
                                                            CarbonUIMessage.ERROR, request,
                                                            new Exception("Error while accessing the Message Store"));
                    }
                %>


                <%
                    if (messageStoreName != null) {
                        String myUrl = "messageStoreName=" + messageStoreName;
                %>
                <input id="messageStoreName_elem" type="hidden" value="<%=messageStoreName%>"/>
                <table id="messageStoreInfoTable" border="0" cellspacing="0" cellpadding="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="messageStore.name"/></th>
                        <th><fmt:message key="type"/></th>
                        <%if (!client.getClassName(messageStoreName).trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")) { %>
                        <th><fmt:message key="size"/></th>
                        <%}%>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        String type = "Not defined";
                        int size = 0;
                        try {
                            size = client.getSize(messageStoreName);
                            type = client.getClassName(messageStoreName);
                        } catch (Exception e) {

                        }

                    %>

                    <tr>
                        <td><%= messageStoreName%>
                        </td>
                        <td><%= type%>
                        </td>
                        <%if (!client.getClassName(messageStoreName).trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")) { %>
                        <td><%= size%>
                        </td>
                        <%}%>
                    </tr>
                    </tbody>
                </table>
                <br/>
                <div id="messages_in_detail" <%=!displayMessageInDetail ? "style=\"display:none\";" : ""%>>
                    <h3><fmt:message key="stored.messages"/></h3>
                    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                                      page="viewMessageStore.jsp"
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
                            if (messageInfos == null || messageInfos.length == 0) {
                        %>
                        <tr>
                            <td colspan="2"><fmt:message key="messsage.store.empty"/></td>
                        </tr>
                        <%
                            }
                        %>

                        <%
                            try {

                                for (MessageInfo mi : messageInfos) {
                        %>

                        <tr>
                            <td><%= mi.getMessageId()%>
                            </td>


                            <td><a onclick="viewEnvRow(this.parentNode.parentNode.rowIndex)" href="#"
                                   class="icon-link"
                                   style="background-image:url(../message_store/images/envelop.gif);"><fmt:message
                                    key="view.envelope"/></a>
                                <%if (!type.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")) { %>
                                <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                                   id="delete_link" class="icon-link"
                                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                        key="delete"/></a>
                                <%}%>
                            </td>
                        </tr>
                        <%
                                }
                            } catch (Exception e) {
                                CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
                            }

                        %>
                        <%if (!type.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore")) { %>
                        <tr>
                            <td colspan="2">
                                <a onclick="<%=(messageInfos == null || messageInfos.length == 0)?"return false":"deleteAll();"%>"
                                   href="#" id="delete_all_link" class="<%=(messageInfos == null || messageInfos.length == 0)?"icon-link-disabled":"icon-link"%>"
                                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                        key="deleteAll"/></a>
                            </td>
                        </tr>
                        <%}%>
                        </tbody>
                    </table>
                    <br/>
                    <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                                      page="viewMessageStore.jsp"
                                      pageNumberParameterName="pageNumber"
                                      resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                                      prevKey="prev" nextKey="next"
                                      parameters="<%=myUrl%>" showPageNumbers="false"/>

                    <%}%>
                </div>
            </div>
        </div>
        <script type="text/javascript">
            alternateTableRows('msgTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('messageStoreInfoTable', 'tableEvenRow', 'tableOddRow');
        </script>
</fmt:bundle>