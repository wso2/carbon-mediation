<%@ page import="org.wso2.carbon.task.ui.internal.ResponseInformation" %>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    String className = request.getParameter("taskClass");
    String states = "";
    String group = request.getParameter("taskGroup");

    if (className != null && !"".equals(className) && group != null && !"".equals(group)) {
        TaskManagementClient client;
        try {
            client = TaskManagementClient.getInstance(config, session);
            ResponseInformation responseInformation = client.loadTaskProperties(className, group);
            if (responseInformation.isFault()) {
                states = responseInformation.getMessage();
            }
        }
        catch (Throwable e) {
            states = e.getMessage();
        }
    }
%>
<%=states%>