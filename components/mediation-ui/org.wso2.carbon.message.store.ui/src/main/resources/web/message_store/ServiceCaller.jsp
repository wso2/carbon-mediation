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
<%@ page import="org.wso2.carbon.message.store.ui.MessageStoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.store.ui.i18n.JSResources"
               request="<%=request%>"/>

<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
    private HttpServletRequest req = null;
    private HttpSession ses = null;
    private static final String gt = ">";
    private static final String lt = "<";
%>
<script type="text/javascript">

    function forward() {
        location.href = 'index.jsp'
    }

</script>

<%!


    private String getMessageStoreXML() throws Exception {

        String name = req.getParameter("Name").trim();
        String provider = req.getParameter("Provider");
        String addedParams = req.getParameter("addedParams");
        String removedParams = req.getParameter("removedParams");
        String params = req.getParameter("tableParams");


        if (addedParams != null) {
            addedParams = addedParams.trim();

        }

        if (removedParams != null) {
            removedParams = removedParams.trim();
        }

        if (params != null) {
            params = params.trim();
        }


        String entry = null;

        StringBuilder messageStoreXml = new StringBuilder();

        if (provider == null || provider.equals("")) {
            messageStoreXml.append("<ns1:messageStore name=\"");
            messageStoreXml.append(name.trim()).append("\"" + " ").append(" xmlns:ns1=\"")
                    .append(SYNAPSE_NS).append("\">");
        } else {
            messageStoreXml.append("<ns1:messageStore name=\"");
            messageStoreXml.append(name.trim()).append("\"" + " ").append("class=\"").append(provider).append("\"" + " ").append(" xmlns:ns1=\"")
                    .append(SYNAPSE_NS).append("\">");
        }

        HashMap<String, String> paramList = new HashMap<String, String>();
        if (params != null) {
            String[] paramParts = params.split("\\|");
            for (int i = 1; i < paramParts.length; i++) {
                String part = paramParts[i];
                String[] pair = part.split("#");
                String pName = pair[0];
                String value = pair[1];
                paramList.put(pName.trim(), value.trim());
                messageStoreXml.append("<ns1:parameter name=\"").append(pName.trim()).append("\" >").
                        append(value.trim()).append("</ns1:parameter>");

            }

        }
        if ("org.apache.synapse.message.store.impl.jms.JmsStore".
                equals(provider.trim())) {
            if (!paramList.containsKey("java.naming.factory.initial") ||
                    !paramList.containsKey("java.naming.provider.url")) {
                throw new Exception();
            }
        }
        messageStoreXml.append("</ns1:messageStore>");
        return messageStoreXml.toString().trim();
    }

%>

<%
    String name = request.getParameter("Name");
    req = request;
    ses = session;
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MessageStoreAdminServiceClient client = new MessageStoreAdminServiceClient(cookie, url, configContext);
    int error = 0;
    StringBuilder ss = new StringBuilder();
    try {
        ss.append(getMessageStoreXML());
    } catch (Exception e) {
        error = 1;
%>
<script type="text/javascript">
    // function backtoForm(){
    //  javascript:history.go(-1);
    jQuery(document).ready(function() {

        CARBON.showErrorDialog(jsi18n["cannot.add.message.store"] + 'Requred parameters missing : java.naming.factory.initial and java.naming.provider.url', function() {
            history.go(-1);
        });
    });

    //}


</script>
<%
    }


    if (((String) session.getAttribute("edit" + name)) != null) {
        try {
            client.modifyMessageStore(ss.toString());
            session.removeAttribute("edit" + name);
        } catch (Exception e) {
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

        CARBON.showErrorDialog(jsi18n["cannot.add.message.store"] + '<%=errMsg%>', gotoPage);
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

        client.addMessageStore(ss.toString());
    } catch (Exception e) {
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

        CARBON.showErrorDialog(jsi18n["cannot.add.message.store"] + '<%=errMsg%>', gotoPage);
    });


</script>
<%
            return;
        }
    }
%>

<%if (error == 0) {%>
<script type="text/javascript">
    jQuery(document).ready(forward());
</script>
<%}%>