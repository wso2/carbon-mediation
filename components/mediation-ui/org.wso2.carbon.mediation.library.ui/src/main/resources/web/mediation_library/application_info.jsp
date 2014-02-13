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
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css"
	rel="stylesheet" />
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page
	import="org.wso2.carbon.mediation.library.ui.LibraryAdminClient"%>
<%@ page
	import="org.wso2.carbon.mediation.library.stub.types.carbon.LibraryInfo"%>
<%@ page
	import="org.wso2.carbon.mediation.library.stub.types.carbon.LibraryArtifiactInfo"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>

<%
	String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

String BUNDLE = "org.wso2.carbon.mediation.library.ui.i18n.Resources";
ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

String importLibName = request.getParameter("libName");
String importPackageName = request.getParameter("pkgName");
LibraryInfo libraryInfo=null;
try {
    LibraryAdminClient client = new LibraryAdminClient(cookie,
                                                       backendServerURL, configContext, request.getLocale());
    String msg = "";
    if (importLibName != null && importPackageName != null && !"".equals(importLibName.trim())
            && !"".equals(importPackageName.trim())) {
    	libraryInfo = client.getLibraryInfo(importLibName, importPackageName);
        //msg = bundle.getString("successfully.imported.app") + " " + importLibName + ". " +
              //bundle.getString("refresh.capp.page");
    }
    //CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
} catch (Exception e) {
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
}
%>


<fmt:bundle
	basename="org.wso2.carbon.mediation.library.ui.i18n.Resources">
	<carbon:breadcrumb label="libs.application.dashboard"
		resourceBundle="org.wso2.carbon.mediation.library.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<script type="text/javascript">
		
	</script>

	<div id="middle">
		<div id="workArea">
			<table class="styledLeft" id="appTable" width="100%">
				<thead>
					<tr>
						<th><%=importLibName.trim() %></th>
					</tr>
					<tr>
						<th><fmt:message key="libs.table.operation" /></th>
						<th><fmt:message key="libs.table.description" /></th>
					</tr>
				</thead>
				<tbody>
					<%
					for (LibraryArtifiactInfo info : libraryInfo.getArtifacts()) {
				%>
					<tr>
						<td><%=info.getName()%></td>
						<td><%=info.getDescription()%></td>
					</tr>
					<%
					}
				%>
				
				</tbody>
			</table>
		</div>
	</div>

</fmt:bundle>