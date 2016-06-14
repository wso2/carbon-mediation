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

<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<carbon:jsi18n resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
               request="<%=request%>" />

<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
    private HttpServletRequest req = null;
    private HttpSession ses = null;
    private static final String gt = ">";
    private static final String lt = "<";
%>
<script type="text/javascript">

    function forward() {
        location.href = 'index.jsp';
    }

</script>



<%
    String messageProcessorName = request.getParameter("processorName").trim();
    String messageId = request.getParameter("messageId").trim();
    req = request;
    ses = session;
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MessageProcessorAdminServiceClient client = new MessageProcessorAdminServiceClient(cookie, url, configContext);
    int error =0;

    if(messageProcessorName != null && messageId != null) {
        try{
            client.resendMessage(messageProcessorName,messageId);
        }catch(Exception e) {

            error = 1;
            String msg = e.getMessage();
            String errMsg = msg.replaceAll("\\'", " ");
            String pageName = request.getParameter("pageName");
            %>

     <script type="text/javascript">
    //function backtoForm(){

    jQuery(document).ready(function() {
        function gotoPage() {

            history.go(-1);
        }
        CARBON.showErrorDialog('Can not resend Message' + '<%=errMsg%>', gotoPage);
    });
</script>

<%
            return;
        }

    } else {


    }
%>


<%if (error == 0) {%>
<script type="text/javascript">
    forward();
</script>
<%}%>