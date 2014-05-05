<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.synapse.task.TaskDescription" %>
<%@ page import="org.wso2.carbon.task.ui.internal.ResponseInformation" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskClientConstants" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementHelper" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Set" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="taskcommon.js"></script>
<%
    String taskName = request.getParameter("taskName");
    if (taskName == null || "".equals(taskName)) {
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog("An error has been occurred !. Error Message : Task implementation class name is empty.");
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
        CARBON.showErrorDialog("An error has been occurred !. Error Message : Task group name is empty.");
    });
</script>
<%
        return;
    }
    String className = request.getParameter("taskClass");
    if (className == null || "".equals(className)) {
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog("An error has been occurred !. Error Message : Task Class name is empty.");
    });
</script>
<%
        return;
    }
    TaskManagementClient client;
    try {
        client = TaskManagementClient.getInstance(config, session);
        TaskDescription taskDescription = TaskManagementHelper.getTaskDescription(request, client, taskName, group);
        Set<OMElement> propertyValues = null;
        if (taskDescription != null) {
            propertyValues = taskDescription.getXmlProperties();
        }
        ResponseInformation responseInformation = client.loadTaskProperties(className, group);
        if (responseInformation.isFault()) {
%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog('<%=responseInformation.getMessage()%>');
    });
</script>

<% return;
} else {
    OMElement properties = (OMElement) responseInformation.getResult();
    Iterator propertyIterator = null;
    if (properties != null) {
        if (propertyValues != null) {
            propertyIterator = TaskManagementHelper.mergeProperties(properties, propertyValues).iterator();
        }
        if (propertyIterator == null) {
            propertyIterator = properties.getChildElements();
        }

        if (propertyIterator != null) {
%>

<fmt:bundle basename="org.wso2.carbon.task.ui.i18n.Resources">
    <table border="0" cellpadding="0" cellspacing="0" id="property_table"
           class="styledLeft noBorders" style="width:100%">
        <thead>
        <tr>
            <th><fmt:message key="task.property.name"/></th>
            <th><fmt:message key="task.property.type"/></th>
            <th><fmt:message key="task.property.value"/></th>
            <th><fmt:message key="task.action"/></th>
        </tr>
        </thead>
        <tbody>
        <%
            int i = 0;
            while (propertyIterator.hasNext()) {
                OMElement property = (OMElement) propertyIterator.next();
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
                       class="esb-edit small_textbox"
                       value="<%=propName%>" disabled="true"/>
                <input type="hidden" name="property_name_hidden<%=i%>"
                       id="property_name_hidden<%=i%>"
                       value="<%=propName%>"/>
            </td>

            <td align="left">
                <select class="esb-edit small_textbox" name="propertyTypeSelection<%=i%>"
                        id="propertyTypeSelection<%=i%>" onchange="onpropertyTypechange('<%=i%>');">
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
                <input id="textField<%=i%>" name="textField<%=i%>" type="text" value="<%=value%>"
                       style="<%=textFieldStyle%>" class="longInput"/>
                <textarea id="textArea<%=i%>" name="textArea<%=i%>" class="longInput"
                          style="<%=textAreaStyle%>"><%=xmlString%>
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
        </tbody>
    </table>
</fmt:bundle>
<% } %>
<% }
}
} catch (Throwable e) {
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
