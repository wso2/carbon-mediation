<%--
 ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>

<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ page import="org.wso2.carbon.business.messaging.hl7.store.ui.HL7StoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.business.messaging.hl7.store.entity.xsd.TransferableHL7Message" %>

<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.JsonObject" %>
<%@ page import="com.google.gson.GsonBuilder" %>

<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<%
    String dtd = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String name = request.getParameter("store").trim();
    String messageId = request.getParameter("uuid").trim();

    HL7StoreAdminServiceClient client = new HL7StoreAdminServiceClient(cookie, url, configContext);

    TransferableHL7Message message = client.getMessage(name, messageId);

    String actions;
    JsonObject object;

    object = new JsonObject();

    object.addProperty("id", message.getId());
    object.addProperty("messageId", StringEscapeUtils.escapeXml(message.getMessageId()));
    object.addProperty("controlId", StringEscapeUtils.escapeXml(message.getControlId()));
    object.addProperty("rawMessage", StringEscapeUtils.escapeXml(message.getRawMessage()));
    object.addProperty("xmlMessage", dtd + "\n" + StringEscapeUtils.escapeXml(message.getEnvelope()));
    object.addProperty("date", message.getDate());
    object.addProperty("timestamp", message.getTimestamp());

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    // Convert the object to a JSON string
    String json = gson.toJson(object);
    out.write(json);
%>
