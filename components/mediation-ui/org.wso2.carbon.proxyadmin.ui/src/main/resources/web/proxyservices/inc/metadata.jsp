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
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.MetaData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>

<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
            request.getLocale());

    try {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ProxyServiceAdminClient client = new ProxyServiceAdminClient(
                configContext, backendServerURL, cookie, request.getLocale());
        MetaData metaData = client.getMetaData();
        request.setAttribute("proxyMetadata", metaData);

    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(bundle.getString("unable.to.get.metadata"),
                CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    window.location.href = '../service-mgt/index.jsp';
</script>
<%
        return;
    }
%>