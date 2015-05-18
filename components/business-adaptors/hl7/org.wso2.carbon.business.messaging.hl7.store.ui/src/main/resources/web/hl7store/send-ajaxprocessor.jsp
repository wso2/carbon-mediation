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

<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.JsonObject" %>
<%@ page import="com.google.gson.GsonBuilder" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<%

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    boolean success = true;
    String errorMsg = "";

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    JsonObject object;

    String storeName = request.getParameter("store").trim();
    String proxyName = request.getParameter("proxy").trim();
    String message = StringEscapeUtils.unescapeXml(request.getParameter("er7").trim());

    try {
        HL7StoreAdminServiceClient client = new HL7StoreAdminServiceClient(cookie, url, configContext);
        success = client.sendMessage(message, storeName, proxyName);
    } catch(XMLStreamException e) {
        errorMsg = "There was an error with the XML Payload trying to be sent. " + e.getMessage();
        success = false;
    } catch (Exception e) {
        errorMsg = "There was an error while communicating with the backend. " + e.getMessage();
        success = false;
    }

    object = new JsonObject();

    if(success) {
        object.addProperty("success", true);
    } else {
        object.addProperty("success", false);
        object.addProperty("reason", StringEscapeUtils.escapeXml(errorMsg));
    }

    // Convert the object to a JSON string
    String json = gson.toJson(object);
    out.write(json);
%>
