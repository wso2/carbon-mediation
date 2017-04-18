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
<%@ page import="org.wso2.carbon.task.ui.internal.ResponseInformation" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="taskcommon.js"></script>
<fmt:bundle basename="org.wso2.carbon.task.ui.i18n.Resources">
    <carbon:breadcrumb label="task.header.new"
                       resourceBundle="org.wso2.carbon.task.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery("#loadClassButton").click(function() {
                jQuery("#modelDescription").load("classload-ajaxprocessor.jsp", {taskClass: jQuery("#taskClass").val(),taskGroup: jQuery("#taskGroup").val()},
                        function(res, status, t) {
                            if (status != "success") {
                                CARBON.showErrorDialog('<fmt:message key="task.error.state"/>');
                            }
                        });
                return false;
            });
        });
    </script>
    <% TaskManagementClient client;
        try {
            client = TaskManagementClient.getInstance(config, session);
            ResponseInformation responseInformation = client.getAllJobGroups();
            if (responseInformation.isFault()) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog('<%=responseInformation.getMessage()%>');
        });
    </script>

    <% return;
    } else {

    %>
    <form method="post" name="taskcreationform" id="taskcreationform"
          action="savetask-ajaxprocessor.jsp">

        <div id="middle">
            <h2><fmt:message key="task.header.new"/></h2>

            <div id="workArea">

                <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message key="task.header.new"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td style="width:150px"><fmt:message key="task.name"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="taskName" name="taskName" class="longInput" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="task.group"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <select id="taskGroup" name="taskGroup" class="longInput">
                                <%
                                    Object result = responseInformation.getResult();
                                    if (result instanceof String[]) {
                                        String[] groups = (String[]) result;
                                        for (String group : groups) { %>
                                <option value="<%=group%>">
                                    <%=group%>
                                </option>
                                <% }
                                }%>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="task.implementation"/><span class="required">*</span>
                        </td>
                        <td align="left">
                            <input onkeydown="onclassnamefieldchange('loadClassButton');"
                                   onkeypress="onclassnamefieldchange('loadClassButton');"
                                   onkeyup="onclassnamefieldchange('loadClassButton');"
                                   onchange="onclassnamefieldchange('loadClassButton');"
                                   id="taskClass" name="taskClass" class="longInput" type="text"
                                   value="org.apache.synapse.startup.tasks.MessageInjector"/>
                            <input style="" id="loadClassButton" class="button"
                                   name="loadClassButton" type="button"
                                   href="#"
                                   value="<fmt:message key="task.class.load.button.text"/>"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <div id="modelDescription"></div>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2" class="middle-header"><fmt:message
                                key="task.trigger.text"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="task.trigger.type"/></td>
                        <td>
                            <input type="radio" name="taskTrigger" id="taskTrigger" value="simple"
                                   onclick="settrigger('simple');"
                                   checked="true"/>
                            <fmt:message key="task.trigger.type.simple"/>

                            <input type="radio" name="taskTrigger" id="taskTrigger" value="cron"
                                   onclick="settrigger('cron');"/>
                            <fmt:message key="task.trigger.type.cron"/>
                            <input type="hidden" name="taskTrigger_hidden" id="taskTrigger_hidden"
                                   value="simple"/>
                        </td>
                    </tr>


                    <tr id="triggerCountTR">
                        <td><fmt:message key="task.trigger.count"/></td>
                        <td>
                            <input id="triggerCount" name="triggerCount" class="longInput"
                                   type="text" value=""/>
                        </td>
                    </tr>
                    <tr id="triggerIntervalTR">
                        <td><fmt:message key="task.trigger.interval"/><span
                                class="required">*</span></td>
                        <td>
                            <input id="triggerInterval" name="triggerInterval" class="longInput"
                                   type="text"
                                   value=""/>
                        </td>
                    </tr>
                    <tr id="triggerCronTR" style="display:none;">
                        <td><fmt:message key="task.trigger.type.cron"/><span
                                class="required">*</span></td>
                        <td>
                            <input id="triggerCron" name="triggerCron" type="text" class="longInput"
                                   value=""/>
                        </td>
                    </tr>

                    <tr>
                        <td colspan="2" class="middle-header"><fmt:message
                                key="task.miscellaneous.information"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="pinned.servers"/></td>
                        <td><input id="pinnedServers" name="pinnedServers" type="text"
                                   class="longInput" value=""/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3"><fmt:message key="task.separated.by.text"/></td>
                    </tr>


                    <tr>
                        <td class="buttonRow" colspan="3">
                            <input class="button" type="button"
                                   value="<fmt:message key="task.button.schedule.text"/>"
                                   onclick="tasksave('<fmt:message key="task.name.cannotfound.msg"/>',
                                           '<fmt:message key="task.classname.cannotfound.msg"/>',
                                           '<fmt:message key="task.cron.cannotfound.msg"/>',
                                           '<fmt:message key="task.count.cannotfound.msg"/>',
                                           '<fmt:message key="task.interval.cannotfound.msg"/>',
                                           '<fmt:message key="task.message.null.msg"/>',
                                           '<fmt:message key="task.property.table.error.msg"/>',
                                           document.taskcreationform); return false;"/>
                            <input class="button" type="button"
                                   value="<fmt:message key="task.canccel.button.text"/>"
                                   onclick="document.location.href='index.jsp?ordinal=0';"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <script type="text/javascript">
                    autoredioselect();
                </script>
            </div>
        </div>

    </form>
    <%
        }
    } catch (Throwable e) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog('<%=e.getMessage()%>');
        });
    </script>
    <%
            return;
        }
    %>
</fmt:bundle>

