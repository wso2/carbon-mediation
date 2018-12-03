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

<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.wso2.carbon.message.processor.ui.utils.MessageProcessorData" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>

<body>
    
<%  
    //ignore methods other than post
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }
    
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),session);
    ConfigurationContext configContext =(ConfigurationContext)config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String msg = null; 
    
    MessageProcessorAdminServiceClient client = new MessageProcessorAdminServiceClient(cookie,url,configContext);
    String processorName = request.getParameter("processorName");
    
    if( processorName != null ) {
        try{
            client.popMessage(processorName);
        }catch (Throwable e){
            msg = "Couldnt pop the Message :" + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg,CarbonUIMessage.ERROR, request);     
        }
    }
    else {
        msg = "Message Processor name is null";
        CarbonUIMessage.sendCarbonUIMessage(msg,CarbonUIMessage.ERROR, request);     
    }
    
%>    
</body>
