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
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Random" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.store.ui.i18n.JSResources"
               request="<%=request%>"/>

<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
    private HttpServletRequest req = null;
    private HttpSession ses = null;
    private static final String gt = ">";
    private static final String lt = "<";
    private String failoverMessageStoreName;
    private boolean isGuaranteedDeliveryEnable = false;
    private String failoverMessageStoreType = null;
    private int randomNum;
    private String factoryInitializer;
    private String providerUrl;
    private String failoverEndpointName;
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
        randomNum = randInt();
        failoverMessageStoreName = req.getParameter("Name").trim() + "_" + randomNum + "_failover";
        params = params + "|store.failover.message.store.name#"+failoverMessageStoreName;


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

                if("store.producer.guaranteed.delivery.enable".equals(pName) && "true".equals(value)) {
                    isGuaranteedDeliveryEnable = true;
                }

                if("store.failover.message.store.type".equals(pName) && "jms".equals(value)) {
                    failoverMessageStoreType = "JMS";
                }

                if("store.failover.message.store.type".equals(pName) && "jdbc".equals(value)) {
                    failoverMessageStoreType = "JDBC";
                }

                if("java.naming.factory.initial".equals(pName)) {
                   factoryInitializer = value;
                }

                if("java.naming.provider.url".equals(pName)) {
                   providerUrl = value;
                }

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
        if ("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore".
                                                                        equals(provider.trim())) {
            if (!paramList.containsKey("store.rabbitmq.host.name") ||
                !paramList.containsKey("store.rabbitmq.host.port")) {
                throw new Exception();
            }
        }
        messageStoreXml.append("</ns1:messageStore>");
        return messageStoreXml.toString().trim();
    }


    private String getFailoverMessageStoreXML() throws Exception {

        String provider = null;

        if(failoverMessageStoreType.equals("JMS")) {
            provider = "org.apache.synapse.message.store.impl.jms.JmsStore";
        } else {
            provider = "org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore";
        }

        String params = req.getParameter("failoverMessageStoreProperties");

        if (params != null) {
            params = params.trim();
        }

        String entry = null;

        StringBuilder messageStoreXml = new StringBuilder();

        if (provider == null || provider.equals("")) {
            messageStoreXml.append("<ns1:messageStore name=\"");
            messageStoreXml.append(failoverMessageStoreName.trim()).append("\"" + " ").append(" xmlns:ns1=\"")
                        .append(SYNAPSE_NS).append("\">");
        } else {
            messageStoreXml.append("<ns1:messageStore name=\"");
            messageStoreXml.append(failoverMessageStoreName.trim()).append("\"" + " ").append("class=\"").append(provider).append("\"" + " ").append(" xmlns:ns1=\"")
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

            if ("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore".
                                                                            equals(provider.trim())) {
                if (!paramList.containsKey("store.rabbitmq.host.name") ||
                    !paramList.containsKey("store.rabbitmq.host.port")) {
                    throw new Exception();
                }
            }
            messageStoreXml.append("</ns1:messageStore>");
            return messageStoreXml.toString().trim();
        }


    private String getFailoverMessageProcessorXML() throws Exception {

        String name = req.getParameter("Name").trim() + "_" + randomNum + "_failover_Processor";
        String provider = "org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor";
        String params = "PARAMS:|interval#1000|client.retry.interval#1000|is.active#true|max.delivery.drop#Disabled|member.count#1";
        failoverEndpointName = req.getParameter("Name").trim() + "_" + randomNum + "_failover_Endpoint";

        if(params != null) {
            params = params.trim();
        }

        String entry = null;
        StringBuilder messageProcessorXml = new StringBuilder();

        if (provider == null || provider.equals("")) {
            throw new Exception("Provider can't be Empty");
        } else {

            if (failoverMessageStoreName == null || "".equals(failoverMessageStoreName.trim())) {
                throw new Exception("Message Store can't be Empty");
            } else {
                messageProcessorXml.append("<ns1:messageProcessor name=\"");
                messageProcessorXml.append(name.trim()).append("\"" + " ").append("class=\"").append(provider.trim());
                    if (failoverEndpointName != null) {
                        messageProcessorXml.append("\"" + " ").append("targetEndpoint=\"").append(failoverEndpointName.trim());
                    }
                    messageProcessorXml.append("\"" + " ").append("messageStore=\"").append(failoverMessageStoreName.trim()).append("\""+" ").
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
                messageProcessorXml.append("<ns1:parameter name=\"").append(pName.trim()).append("\" >").
                append(value.trim()).append("</ns1:parameter>");

            }

        }

        messageProcessorXml.append("</ns1:messageProcessor>");

        return messageProcessorXml.toString().trim();

    }


    private String getFailoverEndPointXML()  {

        return "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" + failoverEndpointName + "\">\n" +
                          "<address uri=\"jms:/" + req.getParameter("Name").trim() + "_Queue?transport.jms.ConnectionFactoryJNDIName=QueueConnectionFactory&amp;" +
                          "java.naming.factory.initial=" + factoryInitializer + "&amp;" +
                          "java.naming.provider.url=" + providerUrl + "&amp;transport.jms.DestinationType=queue\">\n" +
                          "</address>\n" +
                          "</endpoint>";

    }


    public static int randInt() {

        Random rand = new Random();
        int randomNum = rand.nextInt((100000 - 1000) + 1) + 1000;

        return randomNum;
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
    MessageStoreAdminServiceClient messageStoreClient = new MessageStoreAdminServiceClient(cookie, url, configContext);
    MessageProcessorAdminServiceClient messageProcessorClient = new MessageProcessorAdminServiceClient(cookie, url, configContext);
    EndpointAdminClient endpointClient = new EndpointAdminClient(cookie, url, configContext);

    int error = 0;
    StringBuilder messageStore = new StringBuilder();
    StringBuilder failoverMessageStore = new StringBuilder();
    StringBuilder failoverMessageStoreProcessor = new StringBuilder();
    StringBuilder failoverMessageEndpoint = new StringBuilder();

    try {

        messageStore.append(getMessageStoreXML());

        if(isGuaranteedDeliveryEnable && failoverMessageStoreType != null) {

            failoverMessageStore.append(getFailoverMessageStoreXML());
            failoverMessageStoreProcessor.append(getFailoverMessageProcessorXML());
            failoverMessageEndpoint.append(getFailoverEndPointXML());

        }

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
            messageStoreClient.modifyMessageStore(messageStore.toString());
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

        messageStoreClient.addMessageStore(messageStore.toString());

        if(isGuaranteedDeliveryEnable && failoverMessageStoreType != null) {

            messageStoreClient.addMessageStore(failoverMessageStore.toString());
            messageProcessorClient.addMessageProcessor(failoverMessageStoreProcessor.toString());
            endpointClient.saveEndpoint(failoverMessageEndpoint.toString());

       }

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