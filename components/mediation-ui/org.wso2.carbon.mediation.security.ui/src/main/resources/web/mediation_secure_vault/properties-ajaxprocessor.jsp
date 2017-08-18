<%--
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
 --%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ page
	import="org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean"%>
<%@ page
	import="org.wso2.carbon.registry.properties.stub.utils.xsd.Property"%>
<%@ page
	import="org.wso2.carbon.mediation.security.vault.ui.PropertiesServiceClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants"%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="carbon"
	uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<%
	PropertiesServiceClient client_ = new PropertiesServiceClient(config, session);
	try {
		if (request.getParameter("name") != null) {
			if (request.getParameter("oldName") != null) {
				client_.updateProperty(request);
			} else if (request.getParameter("remove") != null) {
				//ignore methods other than post
				if (!request.getMethod().equalsIgnoreCase("POST")) {
					response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
					return;
				}
				client_.removeProperty(request);
			} else {
				client_.setProperty(request);
			}
		}
	} catch (Exception e) {
		response.setStatus(500);
%>
<%=e.getMessage()%>
<%
        return;
    }
    String requestedPage = request.getParameter("dynamicPageNumber");
    PropertiesBean propertiesBean_ = client_.getProperties(request,Integer.parseInt(requestedPage ==null?"0":requestedPage));
    if (propertiesBean_ == null) {
        return;
    }
%>
<%--<script type="text/javascript">
   function retentionError(){
       CARBON.showWarningDialog('<fmt:message key="retention.warn"/>' );
       return;
   }
</script>--%>


<fmt:bundle
	basename="org.wso2.carbon.mediation.security.vault.ui.i18n.Resources">
	<div id="middle">
		<div id="workArea" style="background-color: #F4F4F4;">



			<div id="propertiesList">
				<%
            HashMap properties = new HashMap();
            if (!(propertiesBean_.getSysProperties() == null ||
                    propertiesBean_.getSysProperties().length == 0)) {
                Property[] propArray = propertiesBean_.getProperties();
                for (Property aPropArray : propArray) {
                    properties.put(aPropArray.getKey(), aPropArray.getValue());
                }
        %>
				<table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
					style="width: 800px">
					<thead>
						<tr>
							<th style="width: 20%; border-right: none !important"
								align="left"><fmt:message key="name" /></th>
							<th align="left" style="border-left: none !important"></th>
							<th align="left"><fmt:message key="action" />
							</th>
						</tr>
					</thead>
					<%
                    String[] sysProperties = propertiesBean_.getSysProperties();
                    int itemsPerPage = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);

                    int pageNumber;
                    if (requestedPage != null && requestedPage.length() > 0) {
                        pageNumber = new Integer(requestedPage);
                    } else {
                        pageNumber = 0;
                    }

                    int rowCount = client_.getPropertiesLenght();
                    int numberOfPages;
                    if (rowCount % itemsPerPage == 0) {
                        numberOfPages = rowCount / itemsPerPage;
                    } else {
                        numberOfPages = rowCount / itemsPerPage + 1;
                    }

                for (int i = 0; i < sysProperties.length; i++) {
                    String name = sysProperties[i];
                    String value = "";


            %>

					<tr id="propEditPanel_<%=i%>" style="display: none;">
						<td style="border-right: none !important"><input
							id="propRPath_<%=i%>" type="hidden"
							value="<%=propertiesBean_.getPathWithVersion()%>" /><input
							id="oldPropName_<%=i%>" type="hidden" value="<%=Encode.forHtmlAttribute(name)%>" /><input
							value="<%=Encode.forHtmlAttribute(name)%>" type="text" id="propName_<%=i%>"
							class="propEditNameSelector" />
						</td>
						<td style="border-left: none !important"><table
								cellpadding="0" cellspacing="0" border="0" class="styledLeft">
								<tr>
									<td>Enter New Password:</td>
									<td><input value="<%=value%>" id="propValue_<%=i%>"
										type="password" /></td>
								</tr>
								<tr>
									<td>Re-enter Password:</td>
									<td><input value="<%=value%>" id="propValueConfirm_<%=i%>"
										type="password" /></td>

								</tr>
							</table>
						</td>
						<td><a class="icon-link"
							style="background-image: url(../properties/images/save-button.gif);"
							id="propSaveButton_<%=i%>"
							onclick="showHideCommon('propViewPanel_<%=i%>');showHideCommon('propEditPanel_<%=i%>'); editProperty('<%=i%>')">
								<fmt:message key="save" /> </a> <a class="icon-link"
							style="background-image: url(../admin/images/cancel.gif);"
							onclick="showHideCommon('propViewPanel_<%=i%>');showHideCommon('propEditPanel_<%=i%>');">
								<fmt:message key="cancel" /> </a></td>
					</tr>


					<tr id="propViewPanel_<%=i%>">
						<%
            	String tmpName = name.replaceAll("<","&lt;");
            	tmpName = tmpName.replaceAll(">","&gt;");
            	
            	String tmpValue = value.replaceAll("<","&lt;");
            	tmpValue = tmpValue.replaceAll(">","&gt;");
            	%>
						<td style="border-right: none !important"><span
							class="__propName"><%=tmpName%></span><span
							class="__propNameRef propViewNameSelector" style="display: none;"><%=Encode.forHtmlContent(name)%></span>
						</td>
						<td style="border-left: none !important"></td>


						<% if (propertiesBean_.getPutAllowed() && !propertiesBean_.getVersionView()) { %>
						<td style="width: 150px">
							<%
                        if(Boolean.parseBoolean(propertiesBean_.getWriteLocked())){
                    %> <a class="icon-link registryWriteOperation"
							style="background-image: url(../admin/images/edit.gif);"
							onclick="retentionError();"> <fmt:message key="edit" /> </a> <%}else {%>
							<a class="icon-link registryWriteOperation"
							style="background-image: url(../admin/images/edit.gif);"
							onclick="showHideCommon('propViewPanel_<%=i%>');showHideCommon('propEditPanel_<%=i%>');$('propName_<%=i%>').focus();">
								<fmt:message key="edit" /> </a> <%}%> <%if(Boolean.parseBoolean(propertiesBean_.getDeleteLocked())){%>

							<a class="icon-link registryWriteOperation"
							style="background-image: url(../admin/images/delete.gif);"
							onclick="retentionError();"
							style="margin-left:5px;cursor:pointer;"><fmt:message
									key="delete" /> </a> <%}else {%> <a
							class="icon-link registryWriteOperation"
							style="background-image: url(../admin/images/delete.gif);"
							onclick="removeProperty('<%=name.replace("\\", "\\\\")%>');"
							style="margin-left:5px;cursor:pointer;"><fmt:message
									key="delete" /> </a> <%}%>
						</td>
						<% } else {%>
						<td>&nbsp;</td>
						<% } %>

					</tr>

					<%}%>
				</table>

				<carbon:paginator pageNumber="<%=pageNumber%>"
					numberOfPages="<%=numberOfPages%>" page="manageSecureVault.jsp"
					pageNumberParameterName="dynamicPageNumber"
					resourceBundle="org.wso2.carbon.mediation.security.vault.ui.i18n.Resources"
					prevKey="prev" nextKey="next" parameters="<%=""%>" />

				<%}%>


			</div>
		</div>
	</div>
	<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css"
    	rel="stylesheet" />
    <script type="text/javascript"
    	src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
    <script type="text/javascript"
    	src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
    <script type="text/javascript"
    	src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
</fmt:bundle>
