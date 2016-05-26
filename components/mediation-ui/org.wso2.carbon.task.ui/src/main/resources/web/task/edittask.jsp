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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.synapse.task.TaskDescription" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskClientConstants" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementHelper" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    boolean disableTaskClass = new Boolean((String) config.getServletContext().getAttribute(
            CarbonConstants.PRODUCT_XML_WSO2CARBON + TaskClientConstants.DISABLE_TASK_CLASS));
    boolean disableTaskProperties = new Boolean((String) config.getServletContext().getAttribute(
            CarbonConstants.PRODUCT_XML_WSO2CARBON + TaskClientConstants.DISABLE_TASK_PROPERTIES));
%>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="taskcommon.js"></script>
<script type="text/javascript">
    jQuery(document).ready(function() {
        jQuery("#loadClassButton").click(function() {
            jQuery("#modelDescription").load("classreload-ajaxprocessor.jsp", {taskClass: jQuery("#taskClass").val(),taskGroup: jQuery("#taskGroup").val(),taskName: jQuery("#taskName").val()},
                    function(res, status, t) {
                        if (status != "success") {
                            CARBON.showErrorDialog('<fmt:message key="task.error.state"/>');
                        }
                    });
            return false;
        });
    });
</script>
<%
    String name = request.getParameter("taskName");
    if (name == null || "".equals(name)) {
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog("An error has been occurred !. Error Message : Task name is empty.");
    });
</script>
<%
        return;
    }
    String group = request.getParameter("taskGroup");
    if (group == null || "".equals(group)) {

%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog("An error has been occurred !. Error Message : Task group is empty.");
    });
</script>
<%
        return;
    }

    try {
        TaskManagementClient client = TaskManagementClient.getInstance(config, session);
        TaskDescription taskDescription = TaskManagementHelper.getTaskDescription(request, client, name, group);
%>
<fmt:bundle basename="org.wso2.carbon.task.ui.i18n.Resources">
<carbon:breadcrumb label="task.edit.header" resourceBundle="org.wso2.carbon.task.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<form method="post" name="taskcreationform" id="taskcreationform"
      action="savetask.jsp">

<div id="middle">
<h2><fmt:message key="task.edit.header"/> : <%=name%>
</h2>

<div id="workArea">


<table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
<thead>
<tr>
    <th colspan="2"><fmt:message key="task.edit.header"/></th>
</tr>
</thead>
<tbody>
<tr>
    <td style="width:150px"><fmt:message key="task.name"/><span class="required">*</span></td>
    <td align="left">
        <input id="taskName" name="taskName" class="longInput" type="text"
               value="<%=taskDescription.getName()%>" disabled="true"/>
        <input type="hidden" name="taskName_hidden" id="taskName_hidden"
               value="<%=taskDescription.getName()%>"/>
    </td>
</tr>
<tr>
    <td style="width:150px"><fmt:message key="task.group"/><span class="required">*</span></td>
    <td align="left">
        <input id="taskGroup" name="taskGroup" class="longInput" type="text"
               value="<%=taskDescription.getTaskGroup()%>" disabled="true"/>
        <input type="hidden" name="taskGroup_hidden" id="taskGroup_hidden"
               value="<%=taskDescription.getTaskGroup()%>"/>
    </td>
</tr>
<%
    if (!disableTaskClass) {
%>
<tr>
    <td><fmt:message key="task.implementation"/><span class="required">*</span></td>
    <td align="left">
        <input onkeydown="onclassnamefieldchange('loadClassButton');"
               onkeypress="onclassnamefieldchange('loadClassButton');"
               onkeyup="onclassnamefieldchange('loadClassButton');"
               onchange="onclassnamefieldchange('loadClassButton');" id="taskClass" name="taskClass"
               class="longInput" type="text"
               value="<%=taskDescription.getTaskImplClassName()%>"/>
        <input id="loadClassButton" name="loadClassButton" class="button" type="button"
               href="#"
               value="<fmt:message key="task.class.reload.button.text"/>"/>
    </td>
</tr>
<%
} else { %>
<input id="taskClass" name="taskClass" type="hidden"
       value="<%=taskDescription.getTaskImplClassName()%>"/>
<% } %>
<%
    if (!disableTaskProperties) {
%>
<tr>
    <td colspan="2">

        <div id="modelDescription">
            <% Set<OMElement> properties = taskDescription.getXmlProperties();
                if (properties != null && !properties.isEmpty()) {
            %>
            <table border="0" cellpadding="0" cellspacing="0" class="styledLeft noBorders"
                   id="property_table" style="width:100%">
                <thead>
                <tr>
                    <th><fmt:message key="task.property.name"/></th>
                    <th><fmt:message key="task.property.type"/></th>
                    <th><fmt:message key="task.property.value"/></th>
                    <th><fmt:message key="task.action"/></th>
                </tr>
                </thead>
                <%
                    int i = 0;
                    for (OMElement property : properties) {

                        if (property != null) {
                            String propName = property.getAttributeValue(new QName("", "name", ""));
                            if (propName != null && !"".equals(propName)) {
                                String value = property.getAttributeValue((new QName("", "value", "")));
                                boolean isLiteral = value != null && !"".equals(value);
                                OMElement omValue = property.getFirstElement();
                                String textFieldStyle = "";
                                String textAreaStyle = "display:none;";
                                if (value == null) {
                                    value = "";
                                }
                                String xmlString = "";
                                if (omValue != null) {
                                    xmlString = omValue.toString();
                                    textAreaStyle = "";
                                    textFieldStyle = "display:none;";
                                }

                %>
                <tr id="pr<%=i%>">
                    <td align="left">
                        <input type="text" name="property_name<%=i%>" id="property_name<%=i%>"
                               value="<%=propName%>" disabled="true"/>
                        <input type="hidden" name="property_name_hidden<%=i%>"
                               id="property_name_hidden<%=i%>"
                               value="<%=propName%>"/>
                    </td>

                    <td align="left">
                        <select class="esb-edit small_textbox" name="propertyTypeSelection<%=i%>"
                                id="propertyTypeSelection<%=i%>"
                                onchange="onpropertyTypechange('<%=i%>');">
                            <% if (isLiteral) {%>
                            <option value="literal">
                                <fmt:message key="task.property.type.literal"/>
                            </option>
                            <option value="xml">
                                <fmt:message key="task.property.type.xml"/>
                            </option>
                            <%} else if (omValue != null) {%>
                            <option value="xml">
                                <fmt:message key="task.property.type.xml"/>
                            </option>
                            <option value="literal">
                                <fmt:message key="task.property.type.literal"/>
                            </option>
                            <%} else { %>
                            <option value="literal">
                                <fmt:message key="task.property.type.literal"/>
                            </option>
                            <option value="xml">
                                <fmt:message key="task.property.type.xml"/>
                            </option>
                            <% }%>
                        </select>
                    </td>
                    <td>
                        <input id="textField<%=i%>" name="textField<%=i%>" type="text"
                               value="<%=value%>"
                               style="<%=textFieldStyle%>" class="longInput" for-label="<%=propName%>"/>
                        <textarea id="textArea<%=i%>" name="textArea<%=i%>" class="longInput"
                                  style="<%=textAreaStyle%>" for-label="<%=propName%>"><%=xmlString%>
                        </textarea>
                    </td>
                    <td valign="top"><a href="#" class="delete-icon-link-nofloat"
                                        onclick="deleteproperty('<%=i%>');"><fmt:message
                            key="task.property.delete"/></a></td>
                </tr>
                <% i++;
                }
                }

                } %>
                <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
            </table>
            <% } %></div>
    </td>
</tr>
<%
} else {
    Set<OMElement> properties = taskDescription.getXmlProperties();
    if (properties != null && !properties.isEmpty()) {
        int i = 0;
        for (OMElement property : properties) {

            if (property != null) {
                String propName = property.getAttributeValue(new QName("", "name", ""));
                if (propName != null && !"".equals(propName)) {
                    String value = property.getAttributeValue((new QName("", "value", "")));
                    boolean isLiteral = value != null && !"".equals(value);
                    OMElement omValue = property.getFirstElement();
                    String textFieldStyle = "";
                    String textAreaStyle = "display:none;";
                    if (value == null) {
                        value = "";
                    }
                    String xmlString = "";
                    if (omValue != null) {
                        xmlString = omValue.toString();
                        textAreaStyle = "";
                        textFieldStyle = "display:none;";
                    }

%>
<input type="hidden" name="property_name_hidden<%=i%>"
       id="property_name_hidden<%=i%>" value="<%=propName%>"/>
<input id="textField<%=i%>" name="textField<%=i%>" type="hidden"
       value="<%=value%>" style="<%=textFieldStyle%>" class="longInput"/>
<input id="textArea<%=i%>" name="textArea<%=i%>" class="longInput"
       type="hidden" style="<%=textAreaStyle%>" value="<%=xmlString%>"/>
<input name="propertyTypeSelection<%=i%>" id="propertyTypeSelection<%=i%>"
       type="hidden" value="<%if(isLiteral){%>literal<%}else{%>xml<%}%>"/>
<% i++;
}
}

} %>
<input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
<% }
}
%>
<tr>
    <td colspan="2" class="middle-header"><fmt:message key="task.trigger.text"/></td>
</tr>

<%
    String cron = taskDescription.getCronExpression();
    int count = taskDescription.getCount();
    long interval = taskDescription.getInterval();
    if (taskDescription.getIntervalInMs()) {
        interval = interval / 1000;
    }
    boolean simpleTrigger = cron == null || "".equals(cron);

%>

<tr>
    <td><fmt:message key="task.trigger.type"/></td>
    <% if (simpleTrigger) {%>
    <td>
        <input type="radio" id="taskTrigger" name="taskTrigger" value="simple"
               onclick="settrigger('simple');"
               checked="true"/>
        <fmt:message key="task.trigger.type.simple"/>

        <input type="radio" id="taskTrigger" name="taskTrigger" value="cron"
               onclick="settrigger('cron');"/>
        <fmt:message key="task.trigger.type.cron"/>
        <input type="hidden" name="taskTrigger_hidden" id="taskTrigger_hidden" value="simple"/>
    </td>
    <% } else {%>
    <td>
        <input type="radio" name="taskTrigger" id="taskTrigger" value="simple"
               onclick="settrigger('simple');"
               checked="true"/>
        <fmt:message key="task.trigger.type.simple"/>

        <input type="radio" name="taskTrigger" id="taskTrigger" value="cron"
               onclick="settrigger('cron');" checked="true"/>
        <fmt:message key="task.trigger.type.cron"/>
        <input type="hidden" name="taskTrigger_hidden" id="taskTrigger_hidden" value="cron"/>
    </td>
    <%} %>
</tr>


<% if (simpleTrigger) {%>
<tr id="triggerCountTR">
    <td><fmt:message key="task.trigger.count"/></td>
    <td>
        <input id="triggerCount" class="longInput" name="triggerCount" type="text"
               value="<%=count%>"/>
    </td>
</tr>
<tr id="triggerIntervalTR">
    <td><fmt:message key="task.trigger.interval"/><span class="required">*</span></td>
    <td align="left">
        <input id="triggerInterval" name="triggerInterval" class="longInput" type="text"
               value="<%=interval%>"/>
    </td>
</tr>
<tr id="triggerCronTR" style="display:none;">
    <td><fmt:message key="task.trigger.type.cron"/><span class="required">*</span></td>
    <td>
        <input id="triggerCron" name="triggerCron" class="longInput" type="text" value=""/>
    </td>
</tr>
<% } else {%>
<tr id="triggerCountTR" style="display:none;">
    <td><fmt:message key="task.trigger.count"/></td>
    <td>
        <input id="triggerCount" name="triggerCount" class="longInput" type="text" value=""/>
    </td>
</tr>
<tr id="triggerIntervalTR" style="display:none;">
    <td><fmt:message key="task.trigger.interval"/><span class="required">*</span></td>
    <td align="left">
        <input id="triggerInterval" name="triggerInterval" class="longInput" type="text"
               value=""/>
    </td>
</tr>
<tr id="triggerCronTR">
    <td><fmt:message key="task.trigger.type.cron"/><span class="required">*</span></td>
    <td>
        <input id="triggerCron" name="triggerCron" class="longInput" type="text" value="<%=cron%>"/>
    </td>
</tr>
<%} %>

<%
    List<String> pinnedServerList = taskDescription.getPinnedServers();

    String pinnedServerStr = "";
    if (pinnedServerList != null && !pinnedServerList.isEmpty()) {
        for (String pinnedServer : pinnedServerList) {
            if (pinnedServer != null && !"".equals(pinnedServer)) {
                pinnedServerStr += pinnedServer + " ";
            }
        }
        pinnedServerStr = pinnedServerStr.trim();
    }

%>
<%
   if (!disableTaskProperties) {
%>
<tr>
    <td colspan="2" class="middle-header"><fmt:message key="task.miscellaneous.information"/></td>
</tr>
<tr>
    <td><fmt:message key="task.pinned.servers"/></td>
    <td>
        <input id="pinnedServers" name="pinnedServers" class="longInput" type="text"
               value="<%=pinnedServerStr%>"/>
    </td>
</tr>
<% } else { %>
    <input id="pinnedServers" name="pinnedServers" type="hidden" value="<%=pinnedServerStr%>"/>
<% } %>

<tr>
    <td class="buttonRow" colspan="2">
        <input type="hidden" name="saveMode" id="saveMode" value="edit"/>
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
        <input class="button" type="button" value="<fmt:message key="task.canccel.button.text"/>"
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
</fmt:bundle>
<% } catch (Throwable e) {
    request.getSession().setAttribute(TaskClientConstants.EXCEPTION, e);
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
