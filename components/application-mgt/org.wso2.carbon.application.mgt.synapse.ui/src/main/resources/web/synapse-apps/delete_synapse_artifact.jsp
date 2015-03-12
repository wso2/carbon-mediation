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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.application.mgt.synapse.ui.SynapseAppAdminClient" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String BUNDLE = "org.wso2.carbon.application.mgt.synapse.ui.i18n.Resources";
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    String artifactName = CharacterEncoder.getSafeText(request.getParameter("artifactName"));
    String artifactType = CharacterEncoder.getSafeText(request.getParameter("artifactType"));
    String appName = CharacterEncoder.getSafeText(request.getParameter("appName"));

    try {
        SynapseAppAdminClient client = new SynapseAppAdminClient(cookie,
                backendServerURL, configContext, request.getLocale());
        String msg = bundle.getString("successfully.deleted.artifact") + artifactName;
//        if ("sequence".equals(artifactType) && artifactName != null) {
//            client.deleteSequence(artifactName);
//        } else if ("endpoint".equals(artifactType) && artifactName != null) {
//            client.deleteEndpoint(artifactName);
//        } else if ("proxyservice".equals(artifactType) && artifactName != null) {
//            client.deleteProxyService(artifactName);
//        } else if ("localentry".equals(artifactType) && artifactName != null) {
//            client.deleteLocalEntry(artifactName);
//        } else if ("event".equals(artifactType) && artifactName != null) {
//            client.deleteEvent(artifactName);
//        } else if ("task".equals(artifactType) && artifactName != null) {
//            client.deleteTask(artifactName);
//        }
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    }
%>

<script type="text/javascript">
    location.href = "../carbonapps/application_info.jsp?appName=<%= appName%>";
</script>
