<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData" %>
<%@ page import="org.wso2.carbon.rest.api.ui.client.RestApiAdminClient" %>
<%@ page import="org.wso2.carbon.rest.api.ui.util.ApiEditorHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">
    <%
        ResourceBundle bundle = ResourceBundle.getBundle(
                "org.wso2.carbon.rest.api.ui.i18n.Resources",
                request.getLocale());
        String url = CarbonUIUtil.getServerURL(this.getServletConfig()
                                                       .getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        RestApiAdminClient client = new RestApiAdminClient(
                configContext, url, cookie, bundle.getLocale());

        String apiName = "";
        String apiContext = "";
        String hostname = "";
        String port = "";
        String source = "";
        String sourceXml = "";

        String mode = request.getParameter("mode");
        List<ResourceData> resources = (List<ResourceData>) session.getAttribute("apiResources");
        ResourceData resourceArray[] = new ResourceData[resources.size()];
        String index = request.getParameter("index");
        int resourceIndex = Integer.valueOf(index);

        apiContext = request.getParameter("apiContext");
        apiName = request.getParameter("apiName");
        hostname = request.getParameter("hostname");
        port = request.getParameter("port");
        
        APIData apiData = new APIData();
        
        if ("edit".equals(mode)) {
            apiData.setName(apiName);
            apiData.setContext(apiContext);
            apiData.setHost(hostname == null || "".equals(hostname) ? null : hostname);
            apiData.setPort(Integer.parseInt(port != null && !"".equals(port) ? port : "-1"));
            apiData.setResources(resources.toArray(resourceArray));
        } else {
            apiData.setName(apiName != null ? apiName : "");
            apiData.setContext(apiContext != null ? apiContext : "/");
            apiData.setHost(hostname == null || "".equals(hostname) ? null : hostname);
            apiData.setPort(Integer.parseInt(port != null && !"".equals(port) ? port : "-1"));
            apiData.setResources(resources.toArray(resourceArray));
        }
        ResourceData resourceData;
        if (resourceIndex != -1) {
            resourceData = apiData.getResources()[resourceIndex];
        } else {
            resourceData = (ResourceData) session.getAttribute("resourceData");
        }
        if (resourceData == null) {
            sourceXml = "";
        } else {
            source = client.getResourceSource(resourceData);
            sourceXml = ApiEditorHelper.parseStringToPrettyfiedString(source);
        }
    %>

    <script type="text/javascript">

        function saveResource() {
            //document.location.href = "manageAPI.jsp?mode=" + "<%=mode%>" + "&apiName=" + "<%=apiName%>";

            var source = editAreaLoader.getValue("resource_source");
        <%session.setAttribute("index", index);%>
        <%if("edit".equals(mode)){%>
            jQuery.ajax({
                            type: "POST",
                            url: "savesource-ajaxprocessor.jsp",
                            data: { mode:"<%=mode%>", apiName:"<%=apiName%>", resourceString:source },
                            success: function(data) {
                                 <%session.setAttribute("index", index);%>
                                document.location.href = "manageAPI.jsp?mode=" + "<%=mode%>" + "&apiName=" + "<%=apiName%>" +
                                						 "&hostname=" + "<%=hostname%>" + "&port=" + "<%=port%>";

                            },
                            error: function() {
                                CARBON.showInfoDialog("Could not convert resource source to resource data. ", function() {
                                    <%session.setAttribute("index", index);%>
                                    document.location.href = "manageAPI.jsp?mode=" + "<%=mode%>" + "&apiName=" + "<%=apiName%>";
                                });
                            }
                        });
        <%}
      else{%>
            jQuery.ajax({
                            type: "POST",
                            url: "savesource-ajaxprocessor.jsp",
                            data: { mode:"<%=mode%>", resourceString:source },
                            success: function(data) {
                                 <%session.setAttribute("index", index);%>
                                document.location.href = "manageAPI.jsp?mode=" + "<%=mode%>" + "&apiName=" + "<%=apiName%>" +
                                						 "&hostname=" + "<%=hostname%>" + "&port=" + "<%=port%>";
                            },
                            error: function() {
                                CARBON.showInfoDialog("Could not convert resource source to resource data. ", function() {
                                    <%session.setAttribute("index", index);%>
                                    document.location.href = "manageAPI.jsp?mode=" + "<%=mode%>" + "&apiName=" + "<%=apiName%>";
                                });
                            }
                        });
        <%}
      %>
        }

        function cancelSequence() {
            jQuery.ajax({
                            type: "POST",
                            url: "cancel-ajaxprocessor.jsp",
                            success: function() {
                                document.location.href = "index.jsp";
                            }
                        });
        }

        function switchToDesignView() {
            var source = editAreaLoader.getValue("resource_source");

            jQuery.ajax({
                            type: "POST",
                            url: "switchtodesign-ajaxprocessor.jsp",
                            data: { resourceSource:source, index:'<%=index%>' },
                            success: function(data) {
                                <%session.setAttribute("index", index);%>
                                document.location.href = "manageAPI.jsp?mode=" + "<%=mode%>" + "&apiName=" + "<%=apiName%>" + 
                                						 "&hostname=" + "<%=hostname%>" + "&port=" + "<%=port%>";
                            },
                            error: function() {
                                  CARBON.showInfoDialog("Could not convert resource source to resource data. ", function() {
                                    <%session.setAttribute("index", index);%>
                                    document.location.href = "manageAPI.jsp?mode=" + "<%=mode%>" + "&apiName=" + "<%=apiName%>";
                                });
                            }
                        });
        }
    </script>

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
            request="<%=request%>"/>

    <carbon:breadcrumb
            label="api.source.header"
            resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="api.source.header"/></h2>

        <div id="workArea">
            <form action="" method="post" id="api.source.form" name="apiSrcForm">
                <table class="styledLeft" cellspacing="0" cellpadding="0">
                    <thead>
                    <tr>
                        <th>
							<span style="float:left; position:relative; margin-top:2px;">
								<fmt:message key="api.source.view.text"/>
							</span>
                            <a onclick="switchToDesignView()" class="icon-link"
                               style="background-image:url(images/design-view.gif);">
                                <fmt:message key="api.switchto.design.text"/>
                            </a>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><font style="color:#333333; font-size:small;">
                            <fmt:message key="api.source.name.warning"/>
                        </font>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <textarea id="resource_source" name="apiXML" style="border: 0px solid rgb(204, 204, 204);
                            	width: 99%; height: 400px; margin-top: 5px;"><%=sourceXml%>
                            </textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button"
                                   onclick="javascript: saveResource();"
                                   value="<fmt:message key="update"/>"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
    <script type="text/javascript">
        editAreaLoader.init({
                                id : "resource_source"        // textarea id
                                ,syntax: "xml"            // syntax to be uses for highgliting
                                ,start_highlight: true        // to display with highlight mode on start-up
                            });
    </script>
</fmt:bundle>
