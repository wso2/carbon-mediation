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
<%@ page import="org.apache.synapse.task.TaskDescription" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskClientConstants" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="org.wso2.carbon.task.stub.types.carbon.TaskData" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    boolean disableAddTask = new Boolean((String) config.getServletContext().getAttribute(
            CarbonConstants.PRODUCT_XML_WSO2CARBON + TaskClientConstants.DISABLE_ADD_TASK));
%>

<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="taskcommon.js"></script>

<fmt:bundle basename="org.wso2.carbon.task.ui.i18n.Resources">
    <carbon:jsi18n
        resourceBundle="org.wso2.carbon.task.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="taskjsi18n"/>
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.task.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="task.header"/>

    <div id="middle">
        <h2><fmt:message key="task.header"/></h2>

        <div id="workArea">
            <%
                TaskManagementClient client;
                TaskData[] data = null;
                try {
                    client = TaskManagementClient.getInstance(config, session);
                    List<TaskDescription> descriptions = client.getAllTaskDescriptions();
                    data = client.getAllTaskData();
                    if (descriptions != null && !descriptions.isEmpty()) {

            %>
            <p><fmt:message key="available.defined.scheduled.tasks"/></p>
            <br/>
            <table id="myTable" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="task.name"/></th>
                    <th><fmt:message key="task.action"/></th>
                </tr>
                </thead>
                <tbody>

                <%
                    Collections.sort(descriptions, new Comparator<TaskDescription>() {
                        public int compare(TaskDescription a, TaskDescription b) {
                            return a.getName().compareTo(b.getName());
                        }
                    });
                    if (data != null) {
                        for (TaskData taskData : data) {
                            if (taskData != null) {
                                String name = taskData.getName();
                                String group = taskData.getGroup();
                %>
                <tr id="tr_<%=name%>">

                    <td>
                        <% if (taskData.getArtifactContainerName() != null) { %>
                        <img src="images/applications.gif">
                        <%=Encode.forHtmlContent(name)%>
                        <% if (taskData.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                        <% } else { %>
                        <%=Encode.forHtmlContent(name)%>
                        <% } %>
                    </td>
                    <% if (taskData.getArtifactContainerName() != null) { %>
                    <td>
                        <a href="javascript:editCAppRow('<%=name%>','<%=group%>')" id="config_link"
                           class="edit-icon-link"><fmt:message key="task.edit"/></a>
                        <a href="#"
                           id="delete_link" class="delete-icon-link" style="color:gray;"><fmt:message
                                key="task.property.delete"/></a>
                    </td>
                    <% } else { %>
                    <td>
                        <a href="javascript:editRow('<%=name%>','<%=group%>')" id="config_link"
                           class="edit-icon-link"><fmt:message key="task.edit"/></a>
                        <a href="javascript:deleteRow('<%=name%>','<%=group%>')"
                           id="delete_link" class="delete-icon-link"><fmt:message
                                key="task.property.delete"/></a>
                    </td>
                    <% } %>

                </tr>
                <%
                            }
                        }
                    }
                %>
                </tbody>
            </table>
            <%} else {%>
            <p><fmt:message key="task.list.empty.text"/></p>
            <br/>
            <%}%>
            <%
                if(!disableAddTask) {
            %>
            <div style="height:30px;">
                <a href="javascript:document.location.href='newtask.jsp?ordinal=1'"
                   class="add-icon-link"><fmt:message key="task.button.add.text"/></a>
            </div>
            <%
                }
            %>
            <%

            } catch (Throwable e) {
                request.getSession().setAttribute(TaskClientConstants.EXCEPTION, e);
            %>
            <script type="text/javascript">
                jQuery(document).ready(function() {
                    CARBON.showErrorDialog('<%=e.getMessage()%>');
                });
            </script>
            <%
                }
            %>
        </div>
    </div>
</fmt:bundle>
<script type="text/javascript">
    alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
</script>
