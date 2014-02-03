<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.EditorUIClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.sequences.ui.i18n.Resources">
<carbon:jsi18n
                resourceBundle="org.wso2.carbon.sequences.ui.i18n.JSResources"
                request="<%=request%>" />
    <%
        String resourceConsumer = request.getParameter("resourceConsumer");
        EditorUIClient sequenceAdminClient
                = SequenceEditorHelper.getClientForEditor(getServletConfig(), session);//new SequenceAdminClient(this.getServletConfig(), session);
        String result = "error";
        try {
            result = sequenceAdminClient.getEntryNamesString();
        } catch (Exception e) {

    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog("<fmt:message key="local.registry.error.message"/>" + '<%=e.getMessage()%>');
        });
    </script>
    <%
        }
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers.
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
        if (result == null) {
            result = "";
        }
        String[] entries = result.split(" ");
        if (entries != null && entries.length > 0) {
    %>

    <div id="nsEditorContent" style="margin-top:10px;">
        <select id="local-registry-keys-selection" name="local-registry-keys-selection" onchange="onchangelocalregistrykeys('<%=resourceConsumer%>')">
            <option value="Select A Value"><fmt:message key="select.a.value"/></option>
            <% for (String value : entries) {
                if (value != null && !"".equals(value)) {%>

            <option value="<%=value%>"><%=value%>
            </option>

            <%
                    }
                }
            %>
        </select>
    </div>
    <%}%>
</fmt:bundle>

