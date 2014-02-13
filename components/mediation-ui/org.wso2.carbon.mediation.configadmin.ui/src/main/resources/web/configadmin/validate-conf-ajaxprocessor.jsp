<%@ page import="org.wso2.carbon.mediation.configadmin.ui.ConfigManagementClientUtils" %>
<%@ page import="org.wso2.carbon.mediation.configadmin.ui.ConfigManagementClient" %>
<%@ page import="org.wso2.carbon.mediation.configadmin.ui.ResponseInformation" %>
<%@ page import="org.wso2.carbon.mediation.configadmin.stub.types.carbon.ValidationError" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%
    ConfigManagementClient client;
    String result;
    try{
        client = ConfigManagementClient.getInstance(config, session);
        String synConfig = request.getParameter("synConfig");
        ResponseInformation responseInformation = client.validateConfiguration(synConfig.trim());
        ValidationError[] errors = (ValidationError[])responseInformation.getResult();

        if(responseInformation.isFault()){
            result="invalid";
        } else if(responseInformation.getResult()==null){
            result="valid";
        } else if (errors[0].getMessage().equals("WSDL URL is not accessible")) {
            result="valid";
        } else {
            result="invalid";
        }
    }   catch(Exception e){

        result="error";
    }
%>

<%=result.trim()%>
