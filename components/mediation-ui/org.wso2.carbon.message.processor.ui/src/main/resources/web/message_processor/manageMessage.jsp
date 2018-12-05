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
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.processor.service.xsd.MessageProcessorMetaData" %>

<%@ page import="org.wso2.carbon.message.processor.ui.utils.MessageProcessorData" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageStoreAdminServiceClient" %>


<%@ page import="org.owasp.encoder.Encode" %>
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

    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript" src="localentrycommons.js"></script>


    <link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>


    <%

        String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),session);
        ConfigurationContext configContext = (ConfigurationContext)config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        MessageProcessorAdminServiceClient client = new MessageProcessorAdminServiceClient(cookie,url,configContext);
        String processorName = request.getParameter("messageProcessorName");
        String msg = null;

        if( processorName != null )
        {
            try{
                msg = client.getMessage(processorName);

            } catch (Throwable e ) {
                msg = "ERROR : " + e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(msg,CarbonUIMessage.ERROR, request);
            }
        }

        if(msg!=null) {
            msg = msg.replace("<","&lt");
            msg = msg.replace(">","&gt");
        }
    %>

    <div id = "middle">
        <h2><%=processorName%> : Message Management</h2>

        <% if(msg == null) { %>
        <div id="workArea" style="background-color:#F4F4F4;">
            <pre> <h3>No message in the subscribed Queue</h3> </pre> <br>
        </div>
        <input type="button" class="button" value = "Pop Message" disabled>
       
        <% } else if(msg.contains("ERROR")) { %>
        <div id="workArea" style="background-color:#F4F4F4;">
            <pre> 
                <h3><%=msg%></h3>
                <h4>Message Broker may be inactive</h4>
            </pre> <br>
        </div>
        <input type="button" class="button" value = "Pop Message" disabled>
          
        <% } else { %>
        <div id="workArea" style="background-color:#F4F4F4;">
            <pre> <h3><%=msg%></h3> </pre> <br>
        </div>
        <input type="button" class="button" onclick="popMessage('<%=processorName%>')" value = "Pop Message">
        <% } %>
        
        
    </div>

    <script type="text/javascript">
        function popMessage(name)
        {
            CARBON.showConfirmationDialog("Do you want to pop the message?", function () {
                jQuery.ajax({
                    type: "POST",
                    url: "popMessageFromQueue.jsp",
                    data: {"processorName": name},
                    success: function (result, status, xhr) {
                        alert("Message has been sucessfully popped");
                    }
                });
            });
        }

    </script>


</div>

</fmt:bundle>

