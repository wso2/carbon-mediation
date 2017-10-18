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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page
	import="org.wso2.carbon.mediation.library.ui.LibraryAdminClient"%>
<%@ page
	import="org.wso2.carbon.mediation.library.stub.types.carbon.LibraryInfo"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css"
	rel="stylesheet" />
<script type="text/javascript"
	src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
	src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript"
	src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp" />

<%
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            String BUNDLE = "org.wso2.carbon.mediation.library.ui.i18n.Resources";
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

            LibraryInfo[] libsList = null;
            String[] importList = null;
            LibraryAdminClient client = null;

            try {
                client = new LibraryAdminClient(cookie,
                                                backendServerURL, configContext, request.getLocale());
                libsList = client.getAllLibraryInfo();
                importList = client.getAllImports();
            } catch (Exception e) {
                response.setStatus(500);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
            }


%>

<fmt:bundle
	basename="org.wso2.carbon.mediation.library.ui.i18n.Resources">
	<carbon:breadcrumb label="libs.list.headertext"
		resourceBundle="org.wso2.carbon.mediation.library.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.application.mgt.ui.i18n.JSResources"
		request="<%=request%>" />

	<script type="text/javascript">

    // script for tab handling
$(function() {
    $("#tabs").tabs();
});

$(document).ready(function() {
    var $tabs = $('#tabs > ul').tabs({ cookie: { expires: 30 } });
    $('a', $tabs).click(function() {
        if ($(this).parent().hasClass('ui-tabs-selected')) {
            $tabs.tabs('load', $('a', $tabs).index(this));
        }
    });
    <%
String tabs = request.getParameter("tabs");
if(tabs!=null && tabs.equals("0")) {
    %>$tabs.tabs('option', 'selected', 0);
    <%
}else if(tabs!=null && tabs.equals("1")){
    %>$tabs.tabs('option', 'selected', 1);
    <%
    }
    %>
    if (!isDefinedSequenceFound && !isDynamicSequenceFound) {
        $tabs.tabs('option', 'selected', 2);
    }
});

    function deleteApplication(libQName) {
        CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.app"/>" , function(){
            jQuery.ajax({
                type: "POST",
                url: "delete_artifact-ajaxprocessor.jsp",
                data: {"artifactName": libQName, "type": "library"},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("index.jsp");
                    }
                }
            });
        });
    }

    function updateStatus(libQName, libName, packageName, status) {
        // Show confirmation only when disabling the connector
        if (status == "disabled") {
            CARBON.showConfirmationDialog("<fmt:message key="confirm.disable.connector"/>", function () {
                document.applicationsForm.action = "import_lib.jsp?libQName=" + encodeURI(libQName) + "&status=" + status + "&libName=" + libName + "&packageName=" + packageName;
                document.applicationsForm.submit();
            });
        } else {
            document.applicationsForm.action = "import_lib.jsp?libQName=" + encodeURI(libQName) + "&status=" + status + "&libName=" + libName + "&packageName=" + packageName;
            document.applicationsForm.submit();
        }
    }

    function deleteImport(importName) {
        CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.app"/>" , function(){
            jQuery.ajax({
                type: "POST",
                url: "delete_artifact-ajaxprocessor.jsp",
                data: {"artifactName": importName, "type": "import"},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("index.jsp");
                    }
                }
            });
        });
    }

    function restartServerCallback() {
        var url = "../server-admin/proxy_ajaxprocessor.jsp?action=restart";
        jQuery.noConflict();
        jQuery("#output").load(url, null, function (responseText, status, XMLHttpRequest) {
            if (jQuery.trim(responseText) != '') {
                CARBON.showWarningDialog(responseText);
                return;
            }
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["restart.error"]);
            } else {
                CARBON.showInfoDialog(jsi18n["restart.in.progress.message"]);
            }
        });
    }

    function restartServer() {
        jQuery(document).ready(function() {
            CARBON.showConfirmationDialog(jsi18n["restart.message"], restartServerCallback, null);
        });
    }

</script>


	<div id="middle">


				<h2>
					<fmt:message key="libs.list.headertext" />
				</h2>

				<div id="workArea">
					<form action="" name="applicationsForm" method="post">
						<%
                   if (libsList != null && libsList.length > 0) {
                %>
						<table class="styledLeft" id="appTable" width="100%">
							<thead>
								<tr>
                                    <th><fmt:message key="libs.application" />
									</th>
									<th><fmt:message key="libs.package" />
									</th>
									<th><fmt:message key="libs.description" />
									</th>
                                    <th><fmt:message key="libs.status" />
                                    </th>
									<th colspan="2"><fmt:message key="libs.actions" />
									</th>
								</tr>
							</thead>
							<tbody>
								<%
                         for (LibraryInfo libraryInfo : libsList) {
                             String libName = libraryInfo.getLibName();
                             String pkgName = libraryInfo.getPackageName();
                             String libDesc = libraryInfo.getDescription();
                             String libQName = libraryInfo.getQName();
                             boolean libStatus = libraryInfo.getStatus();
                         
                    %>
								<tr>
									<td><a
										href="./application_info.jsp?libName=<%= libName%>&pkgName=<%= pkgName%>"><%= libName%></a>
									</td>
									<%
                            if (pkgName != null) {
                        %>
									<td><%=pkgName%></td> 
									<%
                            }
                        %>
									<%
                                        if (libDesc != null) {
                                    %>
                                    <td><%=libDesc%>
                                    </td>
                                    <%
                                        } else {
                                    %>
                                    <td></td>
                                    <%
                                        }
                                    %>
									<td> <%if(libStatus){ %>
									   <a href="#" class="icon-link-nofloat"
										style="background-image: url(images/activate.gif);"
										onclick="updateStatus('<%=libQName%>','<%=libName%>','<%=pkgName%>','disabled');"
										title="<%= bundle.getString("libs.status.disable.connector")%>">
                                           <%= bundle.getString("libs.status.enabled")%></a>
										  <%}else{%>
										     <a href="#" class="icon-link-nofloat"
										style="background-image: url(images/deactivate.gif);"
										onclick="updateStatus('<%=libQName%>','<%=libName%>','<%=pkgName%>','enabled');"
										title="<%= bundle.getString("libs.status.enable.connector")%>">
                                                 <%= bundle.getString("libs.status.disabled")%></a>
										  <%} %>
									</td>

                                    <td><a href="#" class="icon-link-nofloat"
                                           style="background-image: url(images/delete.gif);"
                                           onclick="deleteApplication('<%= libQName%>');"
                                           title="<%= bundle.getString("libs.delete.this.row")%>"><%= bundle.getString("libs.delete")%>
                                    </a>
                                    </td>
									<td><a
										href="download-ajaxprocessor.jsp?cappName=<%= libName%>"
										class="icon-link-nofloat"
										style="background-image: url(images/download.gif);"
										title="<%= bundle.getString("download.capp")%>"><%= bundle.getString("download")%></a>
									</td>

								</tr>
								<%
                        }
                    %>
							</tbody>
						</table>
						<%
                } else {
                %>
						<label><fmt:message key="libs.no.apps" />
						</label>
						<%
                    }
                %>
					</form>
				</div>
	</div>

	<%--<%--%>
	<%--if (request.getParameter("restart") != null && request.getParameter("restart").equals("true")) {--%>
	<%--%>--%>
	<%--<script type="text/javascript">--%>
	<%--restartServer();--%>
	<%--</script>--%>
	<%--<%--%>
	<%--}--%>
	<%--%>--%>

</fmt:bundle>