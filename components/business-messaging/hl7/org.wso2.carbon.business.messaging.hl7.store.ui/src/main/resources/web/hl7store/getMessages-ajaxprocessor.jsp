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
<%@ page import="com.google.gson.JsonArray" %>
<%@ page import="com.google.gson.GsonBuilder" %>
<%@ page import="org.wso2.carbon.business.messaging.hl7.store.admin.HL7StoreAdminService" %>

<%

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    boolean success = true;
    String errorMsg = "";

    String name = request.getParameter("store").trim();
    String pageNumber = request.getParameter("page").trim();

    HL7StoreAdminServiceClient client = new HL7StoreAdminServiceClient(cookie, url, configContext);

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    JsonObject responseObject = new JsonObject();
    int size;

    try {
        size = client.getSize(name);
        TransferableHL7Message[] messages = client.getMessages(name, Integer.parseInt(pageNumber));

        JsonArray array = new JsonArray();

        String actions;
        JsonObject object = new JsonObject();

        for(TransferableHL7Message message: messages) {
            object = new JsonObject();

            actions = "<a class=\"editLink\" href=\"edit.jsp?store=" + message.getStoreName() + "&uuid=" + message.getMessageId() + "\">Resend</a>";

            object.addProperty("id", message.getId());
            object.addProperty("messageId", message.getMessageId());
            object.addProperty("controlId", message.getControlId());
            object.addProperty("rawMessage", message.getRawMessage());
            object.addProperty("actions", actions);
            object.addProperty("date", message.getDate());
            object.addProperty("timestamp", message.getTimestamp());

            array.add(object);
        }
        responseObject.add("resultsArray", array);
        responseObject.addProperty("resultsSize", size);
        responseObject.addProperty("success", true);

        double dSize = size;
        double dMaxPerPage = HL7StoreAdminService.MSGS_PER_PAGE;
        responseObject.addProperty("totalPages", Math.ceil(dSize/dMaxPerPage));
    } catch (Exception e) {
        errorMsg = "Search failed. " + e.getMessage();
        responseObject.addProperty("success", false);
        responseObject.addProperty("reason", errorMsg);
        size = client.getSize(name);
        responseObject.addProperty("resultsSize", size);
    }

    out.write(gson.toJson(responseObject));
%>
