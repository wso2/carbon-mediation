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
<%@ page import="org.wso2.carbon.message.processor.service.xsd.MessageProcessorMetaData" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.processor.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
               request="<%=request%>"/>
<carbon:breadcrumb
        label="Message Processors"
        resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
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
    function deleteRow(i, linked) {
        var table = document.getElementById("myTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var content = cell.innerHTML;

        if (linked) {
            content = cell.firstChild.innerHTML;
        }

        function delEp() {
            document.location.href = "deleteMessageProcessorHandler.jsp?" + "processorName=" + content;
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.processor"/>", delEp);
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

    function editRow(processorType, i, linked) {
        var table = document.getElementById("myTable");
        var row = table.rows[i];
        var cell = row.cells[0];
        var type = row.cells[1];
        var content = cell.innerHTML;

        if (linked) {
            content = cell.firstChild.innerHTML;
        }

        if (processorType == "Scheduled Message Forwarding Processor") {
            document.location.href = "manageMessageForwardingProcessor.jsp?" + "messageProcessorName=" + content;
        } else if(processorType == "Scheduled Failover Message Forwarding Processor") {
            document.location.href = "manageFailoverMessageForwardingProcessor.jsp?" + "messageProcessorName=" + content;
        } else if (processorType == "Message Sampling Processor") {
            document.location.href = "manageMessageSamplingProcessor.jsp?" + "messageProcessorName=" + content;
        } else {
            document.location.href = "manageCustomMessageProcessor.jsp?" + "messageProcessorName=" + content;
        }

    }

     function editCAppProcessor(processorType,name) {
         CARBON.showConfirmationDialog('<fmt:message key="edit.cApp.processor.on.page.prompt"/>', function() {
               $.ajax({
                     type: 'POST',
                     success: function() {
                     if (processorType == "Scheduled Message Forwarding Processor") {
                            document.location.href = "manageMessageForwardingProcessor.jsp?" + "messageProcessorName=" + name;
                     } else if (processorType == "Message Sampling Processor") {
                            document.location.href = "manageMessageSamplingProcessor.jsp?" + "messageProcessorName=" + name;
                     } else {
                            document.location.href = "manageCustomMessageProcessor.jsp?" + "messageProcessorName=" + name;
                     }
                     }
               });
         });
     }

    function deactivateRow(name) {

        function deacEp() {
            document.location.href = "ScheduledProcessorActionHandler.jsp?" + "processorName=" + name +
                    "&action=Deactivate";
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.deactivate.the.processor"/>", deacEp);
    }

    function activateRow(name) {

        function deacEp() {
            document.location.href = "ScheduledProcessorActionHandler.jsp?" + "processorName=" + name +
                    "&action=Activate";
        }

        CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.activate.the.processor"/>", deacEp);
    }

    function redirect(selNode) {
        var selected = selNode.options[selNode.selectedIndex].value;
        if (selected != "")window.location.href = selNode.options[selNode.selectedIndex].value;
    }

    function confirmForceDelete(entry, msg) {
        CARBON.showConfirmationDialog('<fmt:message key="dependency.mgt.warning"/><br/><br/>'
                + msg + '<br/><fmt:message key="force.delete"/>', function() {
            document.location.href = "deleteMessageProcessorHandler.jsp?" + "entryName=" + entry + "&force=true";
        });
    }

</script>

<div id="middle">
<h2><fmt:message key="manage.message.Processors"/></h2>

<div id="workArea" style="background-color:#F4F4F4;">

<div id="tabs">
<ul>
    <li><a href="#tabs-1"><fmt:message key="available.messageProcessors.tab.title"/></a></li>
    <li><a href="#tabs-2"><fmt:message key="add.messageProcessors.tab.title"/></a></li>
</ul>
<%
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MessageProcessorAdminServiceClient client = null;
    MessageProcessorMetaData[] messageProcessors = null;
    int numberOfMessageProcessors = 0;
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
        messageProcessors = client.getPaginatedMessageProcessorData(pageNumberInt);
        String[] processorNameList = client.getMessageProcessorNames();

        if (processorNameList != null) {
            numberOfMessageProcessors = processorNameList.length;
        }

        numberOfPages = (int) Math.ceil((double) numberOfMessageProcessors /
                MessageProcessorAdminServiceClient.MESSAGE_PROCESSORS_PER_PAGE);


    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
    }
    if (messageProcessors == null) {
%>
<div id="tabs-1">
    <script type="text/javascript"> emtpyEntries = true</script>
    <fmt:message key="no.messageProcessors.are.currently.defined"/>
</div>

<%}%>

<%if (messageProcessors != null) {%>
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
            <th><fmt:message key="messageProcessor.name"/></th>
            <th><fmt:message key="type"/></th>
            <th><fmt:message key="action"/></th>
        </tr>
        </thead>
        <tbody>

        <%
            for (MessageProcessorMetaData data : messageProcessors) {

                String type = "";
                try {
                    type = client.getClassName(data.getName());

                    if (type != null) {

                        if ("org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor".
                                equals(type.trim())) {
                            type = "Scheduled Message Forwarding Processor";
                         } else if ("org.apache.synapse.message.processor.impl.failover.FailoverScheduledMessageForwardingProcessor".
                                equals(type.trim())) {
                            type = "Scheduled Failover Message Forwarding Processor";

                        } else if ("org.apache.synapse.message.processor.impl.sampler.SamplingProcessor".
                                equals(type.trim())) {
                            type = "Message Sampling Processor";
                        }

                    } else {
                        type = "Custom Message Processor";
                    }
                } catch (Exception e) {

                }

        %>

        <tr>
            <%--<%--%>
                <%--if ("Scheduled Message Forwarding Processor".--%>
                        <%--equalsIgnoreCase(type) && !client.isActive(name)) {--%>
            <%--%>--%>
            <%--<td><a href="ScheduledForwardingProcessorView.jsp?processorName=<%=name%>"><%=name%>--%>
            <%--</a>--%>
            <%--</td>--%>
            <%--<%} else { %>--%>
            <%--<td><%=name%>--%>
            <%--</td>--%>
            <%--<%} %>--%>
            <td>
            <% if (data.getDeployedFromCApp()) { %>
                <img src="images/applications.gif">
                <%=data.getName()%>
                <% if(data.getEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
            <% } else { %>
                <%=data.getName()%>
            <% } %>
            </td>
            <td><%=type%>
            </td>
            <%
                if (("Scheduled Message Forwarding Processor".
                        equalsIgnoreCase(type) || "Message Sampling Processor".equals(type) || "Scheduled Failover Message Forwarding Processor".equalsIgnoreCase(type))
                        && client.isActive(name)) {
            %>
            <td>
                 <% if (data.getDeployedFromCApp()) { %>
                        <a onclick="editCAppProcessor('<%= type%>','<%= data.getName()%>')" href="#"
                                     class="icon-link"
                                     style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                     key="edit"/></a>
                        <a href="#" onclick="#"
                                     id="delete_link" class="icon-link"
                                     style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                     key="delete"/></a>
                 <% } else { %>
                        <a onclick="editRow('<%= type%>',this.parentNode.parentNode.rowIndex)" href="#"
                                     class="icon-link"
                                     style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                     key="edit"/></a>
                        <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                                     id="delete_link" class="icon-link"
                                     style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                     key="delete"/></a>
                 <% } %>


                <span class="icon-text" style="background-image:url(../message_processor/images/activate.gif);">
                    <fmt:message key="active"/>&nbsp;[</span>
                <a href="#" class="icon-link" id="deactivate_link"
                   style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;"
                   onclick="deactivateRow('<%= data.getName()%>')"><fmt:message key="deactivate"/></a>
                <span class="icon-text"
                      style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;">]</span>

            </td>
            <%
            } else if ("Scheduled Message Forwarding Processor".
                    equalsIgnoreCase(type)) {
            %>
            <td>
                <% if (data.getDeployedFromCApp()) { %>
                <a onclick="editCAppProcessor('<%= type%>','<%= data.getName()%>')" href="#"
                   class="icon-link"
                   style="background-image:url(../admin/images/edit.gif);"><fmt:message
                    key="edit"/></a>
                <a href="#" onclick="#"
                   id="delete_link" class="icon-link"
                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                   key="delete"/></a>
                <% } else { %>
                <a onclick="editRow('<%= type%>', this.parentNode.parentNode.rowIndex)" href="#"
                   class="icon-link"
                   style="background-image:url(../admin/images/edit.gif);"><fmt:message
                   key="edit"/></a>
                <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                   id="delete_link" class="icon-link"
                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                        key="delete"/></a>
                <% } %>

                <span class="icon-text" style="background-image:url(../message_processor/images/deactivate.gif);">
                    <fmt:message key="inactive"/>&nbsp;[</span>
                <a href="#" class="icon-link" id="activate_link"
                   style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;"
                   onclick="activateRow('<%= data.getName()%>')"><fmt:message key="activate"/></a>
                <span class="icon-text"
                      style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;">]</span>

            </td>
            <%
             } else if ("Scheduled Failover Message Forwarding Processor".
                                equalsIgnoreCase(type)) {
             %>
                <td><a onclick="editRow('<%= type%>', this.parentNode.parentNode.rowIndex)" href="#"
                        class="icon-link"
                        style="background-image:url(../admin/images/edit.gif);"><fmt:message
                        key="edit"/></a>
                    <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                       id="delete_link" class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"><fmt:message
                       key="delete"/></a>
                    <span class="icon-text" style="background-image:url(../message_processor/images/deactivate.gif);">
                    <fmt:message key="inactive"/>&nbsp;[</span>
                    <a href="#" class="icon-link" id="activate_link"
                       style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;"
                       onclick="activateRow(this.parentNode.parentNode.rowIndex)"><fmt:message key="activate"/></a>
                    <span class="icon-text"
                          style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;">]</span>
                </td>
            <%
            } else if ("Message Sampling Processor".
                    equalsIgnoreCase(type)) {
            %>
            <td>
                <% if (data.getDeployedFromCApp()) { %>
                <a onclick="editRow('<%= type%>', '<%= data.getName()%>')" href="#"
                   class="icon-link"
                   style="background-image:url(../admin/images/edit.gif);"><fmt:message
                    key="edit"/></a>
                <a href="#" onclick="#"
                      id="delete_link" class="icon-link"
                      style="background-image:url(../admin/images/delete.gif);"><fmt:message
                      key="delete"/></a>
                <% } else { %>
                <a onclick="editRow('<%= type%>', this.parentNode.parentNode.rowIndex)" href="#"
                      class="icon-link"
                      style="background-image:url(../admin/images/edit.gif);"><fmt:message
                      key="edit"/></a>
                <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                   id="delete_link" class="icon-link"
                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                        key="delete"/></a>
                <% } %>

                 <span class="icon-text"
                       style="background-image:url(../message_processor/images/deactivate.gif);">
                    <fmt:message key="inactive"/>&nbsp;[</span>
                <a href="#" class="icon-link" id="activate_link"
                   style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;"
                   onclick="activateRow('<%= data.getName()%>')"><fmt:message key="activate"/></a>
                <span class="icon-text"
                      style="background-image:none !important; margin-left: 0px !important; padding-left: 0px !important;">]</span>

            </td>
            <%} else { %>
            <td><a onclick="editRow('<%= type%>', this.parentNode.parentNode.rowIndex)" href="#"
                   class="icon-link"
                   style="background-image:url(../admin/images/edit.gif);"><fmt:message
                    key="edit"/></a>
                <a href="#" onclick="deleteRow(this.parentNode.parentNode.rowIndex)"
                   id="delete_link" class="icon-link"
                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                        key="delete"/></a>
            </td>
            <%} %>


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
                <fmt:message key="add.messageProcessor.title"/>
            </th>
        </tr>
        </thead>

        <tr>
            <td style="width:155px;">
                <a 
                   href="manageMessageForwardingProcessor.jsp"  class="icon-link"
                   style="background: url(../admin/images/add.gif)  no-repeat;">
                    <fmt:message key="scheduled.message.forwarding.processor"/>
                </a>
            </td>
            <td>
                <fmt:message key="scheduled.message.forwarding.processor.desc"/>
            </td>
        </tr>
        <tr>
            <td style="width:155px;">
                <a  href="manageFailoverMessageForwardingProcessor.jsp"  class="icon-link"
                    style="background: url(../admin/images/add.gif)  no-repeat;">
                    <fmt:message key="scheduled.failover.message.forwarding.processor"/>
                </a>
            </td>
            <td>
                <fmt:message key="scheduled.failover.message.forwarding.processor.desc"/>
            </td>
        </tr>

        <tr>
            <td style="width:155px;">
                <a 
                   href="manageMessageSamplingProcessor.jsp" class="icon-link"
                   style="background: url(../admin/images/add.gif)  no-repeat;">
                    <fmt:message key="message.sampling.processor"/>
                </a>
            </td>
            <td>
                <fmt:message key="message.sampling.processor.desc"/>
            </td>
        </tr>
        <tr>
            <td style="width:155px;">
                <a 
                   href="manageCustomMessageProcessor.jsp" class="icon-link"
                   style="background: url(../admin/images/add.gif) no-repeat;">
                    <fmt:message key="custom.message.processor"/>
                </a>
            </td>
            <td>
                <fmt:message key="custom.message.processor.desc"/>
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
