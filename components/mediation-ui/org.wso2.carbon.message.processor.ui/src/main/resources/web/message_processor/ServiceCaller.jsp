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
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<carbon:jsi18n resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
               request="<%=request%>" />

<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
    private HttpServletRequest req = null;
    private HttpSession ses = null;
    private static final String gt = ">";
    private static final String lt = "<";
    private MessageProcessorAdminServiceClient client = null;
%>
<script type="text/javascript">

    function forward() {
        location.href = 'index.jsp'
    }

</script>

<%!


    private String getMessageStoreXML() throws Exception {
        String name = req.getParameter("Name").trim();
        String targetEndpoint = req.getParameter("TargetEndpoint");
        String provider = req.getParameter("Provider");
        String store = req.getParameter("MessageStore");
        String addedParams = req.getParameter("addedParams");
        String removedParams = req.getParameter("removedParams");
        String params = req.getParameter("tableParams");
        if("custom.processor".equals(provider)) {
            provider = req.getParameter("custom_provider_class");
        }

        if(addedParams != null) {
            addedParams = addedParams.trim();

        }

        if(removedParams != null) {
            removedParams = removedParams.trim();
        }

        if(params != null) {
            params = params.trim();
        }

        
        String entry = null;

        StringBuilder messageProcessorXml = new StringBuilder();

        if (provider == null || provider.equals("")) {
            throw new Exception("Provider can't be Empty");
        } else {

            if (store == null || "".equals(store.trim())) {
                    throw new Exception("Message Store can't be Empty");
            } else {
                messageProcessorXml.append("<ns1:messageProcessor name=\"");
                messageProcessorXml.append(name.trim()).append("\"" + " ").append("class=\"").append(provider.trim());
                        if (targetEndpoint != null) {
                            messageProcessorXml.append("\"" + " ").append("targetEndpoint=\"").append(targetEndpoint.trim());
                        }
                        messageProcessorXml.append("\"" + " ").append("messageStore=\"").append(store.trim()).append("\""+" ").
                        append(" xmlns:ns1=\"").append(SYNAPSE_NS).append("\">");
            }

        }

        if(params != null) {
            String[] paramParts = params.split("\\|");
            for(int i=1;i<paramParts.length;i++) {
                String part = paramParts[i];
                String[] pair = part.split("#");
                String pName = pair[0];
                String value = pair[1];
                
                if (pName.equalsIgnoreCase("axis2.repo") && value.length() > 0){
                    client.validateAxis2ClientRepo(value);
                }
                
                messageProcessorXml.append("<ns1:parameter name=\"").append(pName.trim()).append("\" >").
                        append(value.trim()).append("</ns1:parameter>");

            }
            
        }
        messageProcessorXml.append("</ns1:messageProcessor>");
        return messageProcessorXml.toString().trim();
    }

%>

<%
    String name = request.getParameter("Name");
    req = request;
    ses = session;
    int error = 0;
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new MessageProcessorAdminServiceClient(cookie, url, configContext);

    StringBuilder ss = new StringBuilder();
    try{
        ss.append(getMessageStoreXML());
    } catch (Exception e) {
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
        CARBON.showErrorDialog('Invalid value is given to the Axis2 Client Repo', gotoPage);
    });
    //}


</script>
<%
return;
    }
    
    
    if ( ((String) session.getAttribute("edit" + name)) != null) {
        try {
            client.modifyMessageProcessor(ss.toString());
            session.removeAttribute("edit" + name);
        }
        catch (Exception e) {
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
        CARBON.showErrorDialog(jsi18n["cannot.add.message.processor"] + '<%=errMsg%>', gotoPage);
    });
    //}


</script>

<%
        return;
    }
} else {
%>
<%
    try {
        client.addMessageProcessor(ss.toString());
    }
    catch (Exception e) {
        error = 1;
        String msg = e.getMessage();
        String errMsg = msg.replaceAll("\\'", " ");
        String pageName = request.getParameter("pageName");
%>
<script type="text/javascript">
    // function backtoForm(){
    //  javascript:history.go(-1);
    jQuery(document).ready(function() {
        function gotoPage() {

            history.go(-1);
        }
        CARBON.showErrorDialog(jsi18n["cannot.add.message.processor"] + '<%=errMsg%>', gotoPage);
    });

    //}


</script>
<%
            return;
        }
    }
%>

<%if (error == 0) {%>
<script type="text/javascript">
    forward();
</script>
<%}%>