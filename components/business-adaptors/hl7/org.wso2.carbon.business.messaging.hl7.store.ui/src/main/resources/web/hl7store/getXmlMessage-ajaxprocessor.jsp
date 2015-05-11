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
<%@ page import="ca.uhn.hl7v2.parser.PipeParser" %>
<%@ page import="ca.uhn.hl7v2.model.Message" %>
<%@ page import="ca.uhn.hl7v2.parser.EncodingNotSupportedException" %>
<%@ page import="ca.uhn.hl7v2.parser.Parser" %>
<%@ page import="ca.uhn.hl7v2.parser.DefaultXMLParser" %>
<%@ page import="ca.uhn.hl7v2.HL7Exception" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<%
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String errorMessage = "";

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    JsonObject object;

    String rawMessage = request.getParameter("rawMessage").trim();

    object = new JsonObject();

    try {
        Message message = new PipeParser().parse(rawMessage);
        Parser xmlParser = new DefaultXMLParser();
        String xmlDoc = xmlParser.encode(message);
        object.addProperty("success", true);
        object.addProperty("xmlMessage", xmlDoc);
    } catch (EncodingNotSupportedException e) {
        errorMessage = "Could not parse ER7 Encoding message into XML. " + e.getMessage();
        object.addProperty("success", false);
        object.addProperty("reason", StringEscapeUtils.escapeXml(errorMessage));
    } catch (HL7Exception e) {
        errorMessage = "Could not parse ER7 Encoding message into XML. " + e.getMessage();
        object.addProperty("success", false);
        object.addProperty("reason", StringEscapeUtils.escapeXml(errorMessage));
    }

    out.write(gson.toJson(object));
%>
