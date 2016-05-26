<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.task.ui.internal.ResponseInformation" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskClientConstants" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="taskcommon.js"></script>
<%
    String className = request.getParameter("taskClass");
    if (className == null || "".equals(className)) {
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
        CARBON.showErrorDialog("An error has been occurred !. Error Message :  Task group is empty.");
    });
</script>
<%
        return;
    }

    TaskManagementClient client;
    try {
        client = TaskManagementClient.getInstance(config, session);
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
    if (properties != null) {
        Iterator propertyIterator = properties.getChildElements();
        if (propertyIterator != null && propertyIterator.hasNext()) {
%>

<fmt:bundle basename="org.wso2.carbon.task.ui.i18n.Resources">

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
        <tbody>
        <%
            int i = 0;

            while (propertyIterator.hasNext()) {

                OMElement property = (OMElement) propertyIterator.next();
                if (property != null) {
                    String name = property.getAttributeValue(new QName("", "name", ""));
                    if (name != null && !"".equals(name)) {
        %>

        <tr id="pr<%=i%>">
            <td align="left">
                <input type="text" name="property_name<%=i%>" id="property_name<%=i%>"
                       value="<%=name%>" disabled="true"/>
                <input type="hidden" name="property_name_hidden<%=i%>"
                       id="property_name_hidden<%=i%>"
                       value="<%=name%>"/>
            </td>

            <td align="left">
                <select name="propertyTypeSelection<%=i%>"
                        id="propertyTypeSelection<%=i%>" onchange="onpropertyTypechange('<%=i%>');">
                    <option value="literal">
                        <fmt:message key="task.property.type.literal"/>
                    </option>
                    <option value="xml">
                        <fmt:message key="task.property.type.xml"/>
                    </option>
                </select>
            </td>
            <td>
                <input id="textField<%=i%>" name="textField<%=i%>" type="text" class="longInput" for-label="<%=name%>"/>
                <textarea id="textArea<%=i%>" name="textArea<%=i%>" class="longInput"
                          style="display:none;" for-label="<%=name%>"></textarea>
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

<% }
}
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
