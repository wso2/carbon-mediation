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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="net.minidev.json.JSONObject" %>
<%@ page import="org.wso2.carbon.integrator.core.rest.api.swagger.GenericApiObjectDefinition" %>
<%@ page import="org.apache.synapse.rest.API" %>
<%@ page import="org.apache.synapse.config.SynapseConfigUtils" %>
<%@ page import="org.wso2.carbon.context.PrivilegedCarbonContext" %>
<%@ page import="org.wso2.carbon.rest.api.ui.client.RestApiAdminClient" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.integrator.core.rest.api.swagger.SwaggerConstants" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script src="swagger-editor/dist/swagger-editor-bundle.js"></script>
<script src="swagger-editor/dist/swagger-editor-standalone-preset.js"></script>
<script src="js/js-yaml.min.js"></script>
<link href="swagger-editor/dist/swagger-editor.css" rel="stylesheet">
<link href="swagger-editor/dist/custom/swagger-editor-custom.css" rel="stylesheet">

<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">
<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.rest.api.ui.i18n.Resources", request.getLocale());
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                                                                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RestApiAdminClient client = new RestApiAdminClient(configContext, url, cookie, bundle.getLocale());
    
    String apiName = request.getParameter("apiName");
    String action = request.getParameter("action");

    boolean enableEdit = true;

    if ("tryIt".equals(action)) {
        enableEdit = false;
    }

    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    String apiDocContent = client.getSwaggerDocument(apiName, tenantId);
%>

<script type="text/javascript">
    window.localStorage.setItem("swagger-editor-content", jsyaml.safeDump(<%=apiDocContent%>));

    function load_swagger_ui() {
        // Build a system
        const editor = SwaggerEditorBundle({
            dom_id: '#swagger-editor',
            layout: 'StandaloneLayout',
            presets: [
                SwaggerEditorStandalonePreset
            ]
        })

        window.editor = editor
    }

    $(document).ready(function () {
        load_swagger_ui();

    });
    
    function saveChanges() {

        var swagYaml = window.localStorage.getItem("swagger-editor-content");
        var swagJson = {};
        swagJson["swagJson"] = JSON.stringify(jsyaml.safeLoad(swagYaml));
        $.ajax({
            type: "post",
            data: swagJson,
            url: "swaggerOperations-ajaxprocessor.jsp?action=saveSwagger&apiName=<%=apiName%>",
            success: function (data) {

            }
        });
        document.location.href = "index.jsp";

    }

    function saveAndUpdateAPIChanges() {
        var swagYaml = window.localStorage.getItem("swagger-editor-content");
        var swagJson = {};
        swagJson["swagJson"] = JSON.stringify(jsyaml.safeLoad(swagYaml));
        $.ajax({
            type: "post",
            data: swagJson,
            url: "swaggerOperations-ajaxprocessor.jsp?action=generateUpdatedAPI&apiName=<%=apiName%>",
            success: function (data) {
                document.location.href = "manageAPI.jsp?mode=generatedUpdate&apiName=<%=apiName%>";
            }
        });
    }

    function discardChanges() {

        document.location.href = "manageAPI.jsp?mode=edit&apiName=<%=apiName%>";
    }
</script>

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
        request="<%=request%>"/>

<carbon:breadcrumb
        label="api.swagger.editor"
        resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<div id="swagger-editor-wrap">
    <% if (enableEdit) { %>
    <div class=swagger-editor-button id="swagger-editor-button">
        <button class="btn btn-primary" id="update_swagger" onclick="saveAndUpdateAPIChanges()">
            <fmt:message key="api.swagger.apply.changes"/>
        </button>
        <button class="btn btn-secondary" id="close_swagger_editor" onclick="discardChanges()">
            <fmt:message key="api.swagger.discard.changes"/>
        </button>
    </div>
    <% } %>
    <div id="swagger-editor">
    </div>
</div>
</fmt:bundle>