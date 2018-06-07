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
<%@ page import="org.apache.axiom.om.OMElement"%>
<%@ page import="org.apache.synapse.task.TaskDescription"%>
<%@ page import="org.wso2.carbon.task.ui.internal.ResponseInformation"%>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskClientConstants"%>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementClient"%>
<%@ page import="org.wso2.carbon.task.ui.internal.TaskManagementHelper"%>
<%@ page import="javax.xml.namespace.QName"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Set"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>


<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="taskcommon.js"></script>
<fmt:bundle basename="org.wso2.carbon.task.ui.i18n.Resources">
	<%
		try {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
				String saveMode = request.getParameter("saveMode");
				boolean edit = "edit".equals(saveMode);
				TaskManagementClient client;
				ResponseInformation responseInformation;
				TaskDescription taskDescription = TaskManagementHelper
						.createTaskDescription(request);
				client = TaskManagementClient.getInstance(config, session);

				if (edit) {
					responseInformation = client
							.editTaskDescription(taskDescription);
				} else {
					responseInformation = client
							.addTaskDescription(taskDescription);
				}
				//        request.getSession().setAttribute(
				//                TaskClientConstants.TASK_KEY + taskDescription.getName().trim(), taskDescription);
				if (responseInformation != null
						&& responseInformation.isFault()) {
	%>
	<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog('<%=Encode.forHtmlAttribute(responseInformation.getMessage())%>', function () {
            goBackOnePage();
        }, function () {
            goBackOnePage();
        });
    });
</script>
	<%
		} else {
	%>
	<script type="text/javascript">
    forward("index.jsp");
</script>
	<%
		}
			} catch (Exception e) {
	%>
	<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog('<%=Encode.forHtmlAttribute(e.getMessage())%>
		', function() {
				goBackOnePage();
			}, function() {
				goBackOnePage();
			});
		});
	</script>
	<%
		}
	%>

</fmt:bundle>