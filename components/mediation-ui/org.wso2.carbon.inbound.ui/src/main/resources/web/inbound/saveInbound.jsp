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
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.wso2.carbon.inbound.ui.internal.InboundManagementClient"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>


<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="inboundcommon.js"></script>
<fmt:bundle basename="org.wso2.carbon.inbound.ui.i18n.Resources">
	<carbon:breadcrumb label="task.edit.header"
		resourceBundle="org.wso2.carbon.inbound.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />
	<%
		try {
			InboundManagementClient client = InboundManagementClient.getInstance(config, session);
			String classImpl = null;
			String protocol = null;
			if((request.getParameter("inboundClass") == null || request.getParameter("inboundClass").equals(""))){
				protocol = request.getParameter("inboundType");
			}else{
				classImpl = request.getParameter("inboundClass");
			}
			List<String>sParams = new ArrayList<String>();
			Map<String,String[]>paramMap = request.getParameterMap();
			for(String strKey:paramMap.keySet()){
				if(strKey.startsWith("transport.")){
					sParams.add(strKey + "~:~" + request.getParameter(strKey));
				}else if(strKey.startsWith("paramkey")){
					sParams.add(request.getParameter("paramkey" + strKey.replaceAll("paramkey","")) + "~:~" + request.getParameter("paramval" + strKey.replaceAll("paramkey","")));
				}	
			}		
			client.addInboundEndpoint(request.getParameter("inboundName"), request.getParameter("inboundSequence"),request.getParameter("inboundErrorSequence"),request.getParameter("inboundInterval"),protocol, classImpl, sParams);
	%>
	<script type="text/javascript">
    forward("index.jsp");
</script>
	<%
			} catch (Exception e) {
	%>
	<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog('<%=e.getMessage()%>
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