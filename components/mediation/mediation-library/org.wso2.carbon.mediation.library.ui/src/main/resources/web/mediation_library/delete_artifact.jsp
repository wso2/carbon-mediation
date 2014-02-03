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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.mediation.library.ui.LibraryAdminClient" %>
<%@ page import="org.wso2.carbon.mediation.library.stub.types.carbon.LibraryInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String BUNDLE = "org.wso2.carbon.mediation.library.ui.i18n.Resources";
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        String artifactName = request.getParameter("artifactName");
        String artifactType = request.getParameter("type");
        if(artifactName == null || artifactType == null || "".equals(artifactName) ||
                "".equals(artifactType)){
            CarbonUIMessage.sendCarbonUIMessage(bundle.getString("warn.delete.app") + " : null " +
                                                "artifact Type/Name" ,
                                                CarbonUIMessage.WARNING, request);
            return;
        }

        try {
            LibraryAdminClient client = new LibraryAdminClient(cookie,
                                                               backendServerURL, configContext, request.getLocale());
            String msg = "";
            if (artifactName != null && "import".equals(artifactType.trim().toLowerCase())) {
                client.deleteImport(artifactName);
                msg = bundle.getString("successfully.deleted.import") + " " + artifactName + ". " +
                      bundle.getString("refresh.capp.page");
            }else if(artifactName != null && "library".equals(artifactType.trim().toLowerCase())){
                client.deleteLibrary(artifactName);
                msg = bundle.getString("successfully.deleted.app") + " " + artifactName + ". " +
                      bundle.getString("refresh.capp.page");

            }else{
                CarbonUIMessage.sendCarbonUIMessage(bundle.getString("warn.delete.app") + " : invalid " +
                                                "artifact Type/Name" ,
                                                CarbonUIMessage.WARNING, request);
                return;
            }
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
        }
%>

<script type="text/javascript">
    location.href = "index.jsp";
</script>