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
<%@ page import="org.wso2.carbon.rest.api.ui.util.RestAPIConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script src="swagger-editor/dist/swagger-editor-bundle.js"></script>
<script src="swagger-editor/dist/swagger-editor-standalone-preset.js"></script>
<script src="js/js-yaml.min.js"></script>
<script src="js/api-util.js"></script>
<link href="swagger-editor/dist/swagger-editor.css" rel="stylesheet">
<link href="swagger-editor/dist/custom/swagger-editor-custom.css" rel="stylesheet">
<link href="resources/defaultJsonSwagger.json">
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">
    <%
        String defaultSwagger = RestAPIConstants.DEFAULT_JSON_SWAGGER;
        String sourceXml = "";

        String action = request.getParameter("action");
        boolean actionBack = "back".equals(action);
    %>
    <script type="text/javascript">

        <% if (!actionBack) { %>
        window.localStorage.setItem("swagger-editor-content", jsyaml.safeDump(<%=defaultSwagger%>));
        <% } %>

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
            //Load swagger UI
            load_swagger_ui();
        });
        
        function generateApi() {
            var swagYaml = window.localStorage.getItem("swagger-editor-content");
            var swagJson = {};
            swagJson["swagJson"] = JSON.stringify(jsyaml.safeLoad(swagYaml));
            $.ajax({
                type: "post",
                data: swagJson,
                url: "swaggerOperations-ajaxprocessor.jsp?action=generateAPI",
                success: function (data) {
                    document.location.href = "generateAPIWizard2.jsp";
                }
            });
        }

        function discardChanges() {
            document.location.href = "index.jsp";
        }
    </script>

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
            request="<%=request%>"/>
    <carbon:breadcrumb
            label="generate.api.wizard"
            resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="generate.api.wizard"/></h2>
        <div id="swagger-editor-wrap">
            <div class=swagger-editor-button id="swagger-editor-button">
                <button class="btn btn-primary" id="update_swagger" onclick="generateApi()">
                    <fmt:message key="generate.api"/>
                </button>
                <button class="btn btn-secondary" id="close_swagger_editor" onclick="discardChanges()">
                    <fmt:message key="cancel"/>
                </button>
            </div>
            <div id="swagger-editor"></div>
        </div>
    </div>
</fmt:bundle>