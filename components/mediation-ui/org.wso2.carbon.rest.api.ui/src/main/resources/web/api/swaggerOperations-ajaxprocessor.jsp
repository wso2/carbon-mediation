<%--
~  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  Licensed under the Apache License, Version 2.0 (the "License");
~  you may not use this file except in compliance with the License.
~  You may obtain a copy of the License at
~
~        http://www.apache.org/licenses/LICENSE-2.0
~
~  Unless required by applicable law or agreed to in writing, software
~  distributed under the License is distributed on an "AS IS" BASIS,
~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~  See the License for the specific language governing permissions and
~  limitations under the License.
--%>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.apache.synapse.rest.API" %>
<%@ page import="org.apache.synapse.config.SynapseConfigUtils" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.integrator.core.rest.api.swagger.SwaggerConstants" %>
<%@ page import="org.wso2.carbon.integrator.core.rest.api.swagger.GenericApiObjectDefinition" %>
<%@ page import="org.wso2.carbon.context.PrivilegedCarbonContext" %>
<%@ page import="org.wso2.carbon.rest.api.ui.client.RestApiAdminClient" %>
<%@ page import="org.wso2.carbon.rest.api.ui.util.ApiEditorHelper" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@ page import="net.minidev.json.JSONObject" %>
<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.rest.api.ui.i18n.Resources",request.getLocale());
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
                                                                                CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RestApiAdminClient client = new RestApiAdminClient(configContext, url, cookie, bundle.getLocale());

    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

    String action = request.getParameter("action");
    String responsePayload = "";

    if("generateAPI".equals(action)) {
        //Generate API
        String swagJson= request.getParameter("swagJson");
        String genAPIStr = client.generateAPIFromSwagger(swagJson);

        genAPIStr = ApiEditorHelper.parseStringToPrettyfiedString(genAPIStr);
        APIData genAPI = ApiEditorHelper.convertStringToAPIData(genAPIStr);

        session.setAttribute("genAPIStr", genAPIStr);
        session.setAttribute("genAPI", genAPI);

        response.setContentType("application/xml");
        responsePayload = genAPIStr;

    } else if ("generateUpdatedAPI".equals(action)) {
        //Generate API
        String swagJson= request.getParameter("swagJson");
        String apiName= request.getParameter("apiName");
        String genAPIStr = client.generateUpdatedAPIFromSwagger(swagJson, apiName);

        genAPIStr = ApiEditorHelper.parseStringToPrettyfiedString(genAPIStr);
        APIData genAPI = ApiEditorHelper.convertStringToAPIData(genAPIStr);

        session.setAttribute("genUpdatedAPIStr", genAPIStr);
        session.setAttribute("apiData", genAPI);
        session.setAttribute("swagJson", swagJson);

        response.setContentType("application/xml");
        responsePayload = genAPIStr;
    } else if ("saveSwagger".equals(action)) {
        String swagJson= request.getParameter("swagJson");
        String apiName= request.getParameter("apiName");
        client.updateSwaggerDocument(apiName, swagJson,tenantId);
        responsePayload = "<response><msg>Swagger definition updated successfully<msg><status>SUCCESS</status></response>";
        response.setContentType("application/xml");
    } else {
        // Unidentified action
        responsePayload = "<response><msg>Unidentified action<msg><status>FAIL</status></response>";
        response.setContentType("application/xml");
    }
%>
<%=responsePayload%>