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
<%@ page import="org.wso2.carbon.message.store.ui.utils.MessageStoreData" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.store.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.store.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="Message Stores"
        resourceBundle="org.wso2.carbon.message.store.ui.i18n.Resources"
        topPage="false"
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
    function deleteRow(i, name) {
        var table = document.getElementById("myTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var content = cell.firstElementChild.innerHTML;

        function delEp() {
            document.location.href = "deleteMessageStoresHandler.jsp?" + "messageStoreName=" + name;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.message.store"/>", delEp);
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

    function editRow(i, storeType, name) {
        var table = document.getElementById("myTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var type = row.cells[1];
        if (storeType == "org.apache.synapse.message.store.impl.jms.JmsStore") {
            document.location.href = "jmsMessageStore.jsp?" + "messageStoreName=" + name;
        } else if (storeType == "org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore") {
            document.location.href = "rabbitmqMessageStore.jsp?" + "messageStoreName=" + name;
        } else if (storeType == "org.apache.synapse.message.store.impl.memory.InMemoryStore") {
            document.location.href = "inMemoryMessageStore.jsp?" + "messageStoreName=" + name;
        } else if (storeType == "org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore") {
            document.location.href = "jdbcMessageStore.jsp?" + "messageStoreName=" + name;
        } else {
            document.location.href = "customMessageStore.jsp?" + "messageStoreName=" + name;
        }
    }

    function editCAppStore(i, storeType,name) {
        CARBON.showConfirmationDialog('<fmt:message key="edit.artifactContainer.store.on.page.prompt"/>', function() {
            $.ajax({
                type: 'POST',
                success: function() {
                    if (storeType == "org.apache.synapse.message.store.impl.jms.JmsStore") {
                        document.location.href = "jmsMessageStore.jsp?" + "messageStoreName=" + name;
                    } else if (storeType == "org.apache.synapse.message.store.impl.memory.InMemoryStore") {
                        document.location.href = "inMemoryMessageStore.jsp?" + "messageStoreName=" + name;
                    } else {
                        document.location.href = "customMessageStore.jsp?" + "messageStoreName=" + name;
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
            document.location.href = "deleteMessageStoresHandler.jsp?" + "entryName=" + entry + "&force=true";
        });
    }

</script>


<div id="middle">
    <h2><fmt:message key="manage.message.stores"/></h2>

    <div id="workArea" style="background-color:#F4F4F4;">

        <div id="tabs">
            <ul>

                <li><a href="#tabs-1"><fmt:message key="available.messageStores.tab.title"/></a></li>
                <li><a href="#tabs-2"><fmt:message key="add.messageStores.tab.title"/></a></li>

            </ul>
            <%
                String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
                        session);
                ConfigurationContext configContext =
                        (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                MessageStoreAdminServiceClient client = null;
                MessageStoreData[] messageStoreData = null;
                int numberOfMessageStores = 0;
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
                    messageStoreData = client.getPaginatedMessageStoreData(pageNumberInt);

                    if (messageStoreData != null) {
                        if (client.getMessageStoreData() != null) {
                            numberOfMessageStores = client.getMessageStoreData().length;
                        }
                    }

                    numberOfPages = (int) Math.ceil((double) numberOfMessageStores /
                            MessageStoreAdminServiceClient.MESSAGE_STORES_PER_PAGE);


                } catch (Exception e) {
                    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
            %>
            <script type="text/javascript">
                location.href = "../admin/error.jsp";
            </script>
            <%
                }
                if (messageStoreData == null) {
            %>
            <div id="tabs-1" class="ui-tabs-panel">
                <script type="text/javascript"> emtpyEntries = true</script>
                <fmt:message
                        key="no.messageStores.are.currently.defined"/>
            </div>

            <%}%>

            <%if (messageStoreData != null) {%>
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
                        <th><fmt:message key="messageStore.name"/></th>
                        <th><fmt:message key="type"/></th>
                        <th><fmt:message key="size"/></th>
                        <th><fmt:message key="action"/></th>
                    </tr>
                    </thead>
                    <tbody>

                    <%
                        for (MessageStoreData msData : messageStoreData) {
                            String name = msData.getName();
                            String type = "Not defined";
                            int size = 0;
                            try {
                                size = client.getSize(name);
                                type = client.getClassName(name);
                            } catch (Exception e) {

                            }

                    %>

                    <tr>
                        <td> <%if (!type.trim().equals("org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore") && !type.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore") && !type.trim().equals("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore")) { %>
                            <a href="viewMessageStore.jsp?messageStoreName=<%=name%>">
                                <% if (msData.getArtifactContainerName() != null) { %>
                                    <img src="images/applications.gif">
                                    <%=name%>
                                    <% if(msData.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                                <% } else {%>
                                    <%=name%>
                                <%}%>
                            </a>
                        <%} else {%>
                            <a><%=name%>
                            </a>
                        <%}%>
                        </td>
                        <td><%= type%>
                        </td>
                        <%if (!type.trim().equals("org.apache.synapse.message.store.impl.jms.JmsStore") && !type.trim().equals("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore")) { %>
                        <td><%= size%>
                        </td>
                        <%} else {%>
                        <td>Not Applicable</td>
                        <%}%>

                        <td>
                            <% if (msData.getArtifactContainerName() != null) { %>
                            <a onclick="<%=("org.apache.synapse.message.store.impl.memory.InMemoryStore".equals(type.trim()))?"return false":"editCAppStore(this.parentNode.parentNode.rowIndex,"+"'" + type + "',"+"'" + name + "');"%>"
                               class="<%=("org.apache.synapse.message.store.impl.memory.InMemoryStore".equals(type.trim()))?"icon-link-disabled":"icon-link"%>"
                               style="background-image:url(../admin/images/edit.gif);" href="#"><fmt:message
                                    key="edit"/></a>

                            <a href="#" onclick="#"
                               id="delete_link" class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                    key="delete"/></a>
                            <% } else {%>
                            <a onclick="<%=("org.apache.synapse.message.store.impl.memory.InMemoryStore".equals(type.trim()))?"return false":"editRow(this.parentNode.parentNode.rowIndex,"+"'" + type + "',"+"'" + name + "');"%>"
                               class="<%=("org.apache.synapse.message.store.impl.memory.InMemoryStore".equals(type.trim()))?"icon-link-disabled":"icon-link"%>"
                               style="background-image:url(../admin/images/edit.gif);" href="#"><fmt:message
                                    key="edit"/></a>

                            <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex,'<%= name %>')"
                               id="delete_link" class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                    key="delete"/></a>
                            <% } %>

                        </td>
                    </tr>
                    <%}%>
                    </tbody>
                </table>
                <br/>


                <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=numberOfPages%>"
                                  page="index.jsp" pageNumberParameterName="pageNumber"
                                  resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                                  prevKey="prev" nextKey="next"
                                  parameters=""/>
            </div>
            <%}%>


            <div id="tabs-2">
                <table id="MessageStoreOptionTable" class="styledLeft" cellpadding="1">
                    <thead>
                    <tr>
                        <th colspan="2">
                            <fmt:message key="add.messageStore.title"/>
                        </th>
                    </tr>
                    </thead>

                    <tr>
                        <td width="155px">
                            <a class="icon-link"
                               href="inMemoryMessageStore.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="in.memory.message.store"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="in.memory.message.store.desc"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a class="icon-link"
                               href="jmsMessageStore.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="jms.message.store"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="jms.message.store.desc"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a class="icon-link"
                               href="rabbitmqMessageStore.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="rabbitmq.message.store"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="rabbitmq.message.store.desc"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a class="icon-link"
                               href="jdbcMessageStore.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="jdbc.message.store"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="jdbc.message.store.desc"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <a class="icon-link"
                               href="customMessageStore.jsp"
                               style="background-image: url(../admin/images/add.gif);">
                                <fmt:message key="custom.message.store"/>
                            </a>
                        </td>
                        <td>
                            <fmt:message key="custom.message.store.desc"/>
                        </td>
                    </tr>

                </table>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
    alternateTableRows('dlcTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>