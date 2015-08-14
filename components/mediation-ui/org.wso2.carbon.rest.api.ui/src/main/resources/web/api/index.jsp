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
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData"%>
<%@page import="org.wso2.carbon.rest.api.ui.util.RestAPIConstants"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.rest.api.ui.client.RestApiAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonSecuredHttpContext" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<!-- This page is included to display messages which are set to request scope or session scope -->

<%
    response.setHeader("Cache-Control", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RestApiAdminClient client;
    
    String serviceContextPath = configContext.getServiceContextPath();
    
    String serverContext = "";
    APIData[] apis = null;

    int pageNumber;
    int numberOfPages;
    int apiCount;
    
    String pageNumberStr = request.getParameter("pageNumber");
    String apiSearchString = request.getParameter("apiSearchString");
    if (apiSearchString == null) {
        apiSearchString = "";
    }

    if (pageNumberStr == null) {
        pageNumber = 0;
    }
    else{
    	try {
    		pageNumber = Integer.parseInt(pageNumberStr);
        } catch (NumberFormatException e) {
        	response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, 
            		"pageNumber parameter is not an integer", e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
		    %>
		    <jsp:include page="../admin/error.jsp"/>
		    <%
            return;
        }
    }
    
    String serviceTypeFilter = request.getParameter("serviceTypeFilter");
    if (serviceTypeFilter == null) {
        serviceTypeFilter = "ALL";
    }
    String serviceSearchString = request.getParameter("serviceSearchString");
    if (serviceSearchString == null) {
        serviceSearchString = "";
    }
    boolean isAuthorizedToManage =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/mediation");
    try {
        client = new RestApiAdminClient(configContext, backendServerURL, cookie, request.getLocale());
        serverContext = client.getServerContext();
        apiCount = client.getAPICount();

        if (apiSearchString.equals("")) {
            apis = client.getAPIsForListing(pageNumber, RestAPIConstants.APIS_PER_PAGE);
        } else {
            apis = client.getAPIsForSearchListing(pageNumber, RestAPIConstants.APIS_PER_PAGE, apiSearchString);
            if (apis!=null) {
                apiCount=apis.length;
            }
        }

        if(apiCount % RestAPIConstants.APIS_PER_PAGE == 0){
        	numberOfPages = apiCount / RestAPIConstants.APIS_PER_PAGE;
        }
        else{
        	numberOfPages = (apiCount / RestAPIConstants.APIS_PER_PAGE) + 1;
        }



    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }

    session.removeAttribute("index");
    request.getSession().removeAttribute("resourceData");

    //int correctServiceGroups = servicesInfo.getNumberOfCorrectServiceGroups();
    //int faultyServiceGroups = servicesInfo.getNumberOfFaultyServiceGroups();
    
    boolean loggedIn = session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null;
    
    //boolean hasDownloadableServices = false;
%>

<carbon:breadcrumb
        label="deployed.apis"
        resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<%
    if (apiCount==0) {
%>
        <fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">
            <div id="middle">
                <h2><fmt:message key="deployed.apis"/></h2>
                <div id="workArea">
                	<a style="background-image: url(../admin/images/add.gif);" href="manageAPI.jsp?mode=add" class="icon-link">
                                <fmt:message key="add.api"/>
                    </a><br/><br/>
                    <%if (!apiSearchString.equals("")){
                    %>
                    <a style="background-image: url(images/search.gif);" href="index.jsp" class="icon-link">
                        <fmt:message key="no.search.apis.found"/>
                    </a><br/><br/>
                    <%}
                    else{
                    %>
                    <fmt:message key="no.deployed.apis.found"/>
                    <%

                    }%>


                </div>
            </div>
        </fmt:bundle>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">
<%
    if (session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null) {
%>
<script type="text/javascript">
    var allServicesSelected = false;

    function selectAllInThisPage(isSelected) {
        allServicesSelected = false;
        if (document.servicesForm.serviceGroups != null &&
            document.servicesForm.serviceGroups[0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.servicesForm.serviceGroups.length; j++) {
                    document.servicesForm.serviceGroups[j].checked = true;
                }
            } else {
                for (j = 0; j < document.servicesForm.serviceGroups.length; j++) {
                    document.servicesForm.serviceGroups[j].checked = false;
                }
            }
        } else if (document.servicesForm.serviceGroups != null) { // only 1 service
            document.servicesForm.serviceGroups.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allServicesSelected = true;
        return false;
    }

    function resetVars() {
        allServicesSelected = false;

        var isSelected = false;
        if (document.servicesForm.serviceGroups[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.servicesForm.serviceGroups.length; j++) {
                if (document.servicesForm.serviceGroups[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.servicesForm.serviceGroups != null) { // only 1 service
            if (document.servicesForm.serviceGroups.checked) {
                isSelected = true;
            }
        }
        return false;
    }
    
    function deleteApi(apiName) {
         CARBON.showConfirmationDialog("<fmt:message key="api.delete.confirmation"/> " + apiName + "?", function() {
             location.href = "delete_api.jsp?apiName=" + apiName;
         });
    }
    function searchSequence() {
        document.searchForm.submit();
    }

    function loadApiAfterBulkDeletion(){
        window.location.href = "index.jsp?pageNumber=<%=pageNumber%>";
    }
    function deleteServices() {
        var selected = false;
        if (document.servicesForm.apiGroups[0] != null) { // there is more than 1 APIs
            for (var j = 0; j < document.servicesForm.apiGroups.length; j++) {
                selected = document.servicesForm.apiGroups[j].checked;
                if (selected) break;
            }
        }

        else if (document.servicesForm.apiGroups != null) { // only 1 API
            selected = document.servicesForm.apiGroups.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.api.to.be.deleted"/>');
            return;
        }
        if (allServicesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.api.on.all.prompt"/>", function() {
                $.ajax({
                    type: 'POST',
                    url: 'delete_api_ajaxprocessor.jsp',
                    data: 'deleteAllApiGroups=true',
                    success: function(msg) {
                        loadApiAfterBulkDeletion();
                    }
                });
            });
        } else {

            var apiGroupsString = '';
            jQuery('.chkBox').each(function(index) {
                if(this.checked) {
                    apiGroupsString += this.value + ':';
                }
            });

            CARBON.showConfirmationDialog("<fmt:message key="delete.api.on.page.prompt"/>",function() {
                $.ajax({
                    type: 'POST',
                    url: 'delete_api_ajaxprocessor.jsp',
                    data: 'apiGroupsString='+ apiGroupsString,
                    success: function(msg) {
                        loadApiAfterBulkDeletion();
                    }
                });
            });
        }
    }

    function selectAllInThisPage(isSelected) {
        allServicesSelected = false;
        if (document.servicesForm.apiGroups != null &&
                document.servicesForm.apiGroups[0] != null) { // there is more than 1 API
            if (isSelected) {
                for (var j = 0; j < document.servicesForm.apiGroups.length; j++) {
                    document.servicesForm.apiGroups[j].checked = true;
                }
            } else {
                for (j = 0; j < document.servicesForm.apiGroups.length; j++) {
                    document.servicesForm.apiGroups[j].checked = false;
                }
            }
        } else if (document.servicesForm.apiGroups != null) { // only 1 API
            document.document.servicesForm.apiGroups.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allServicesSelected = true;
        return false;
    }

    function resetVars() {
        allServicesSelected = false;
        var isSelected = false;
        if (document.servicesForm.apiGroups[0] != null) { // there is more than 1 API
            for (var j = 0; j < document.servicesForm.apiGroups.length; j++) {
                if (document.servicesForm.apiGroups[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.servicesForm.apiGroups != null) { // only 1 API
            if (document.servicesForm.apiGroups.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function editApi(apiName) {
        document.location.href = "manageAPI.jsp?mode=edit&apiName="+apiName;
    }

    function editCAppApi(apiName) {
      CARBON.showConfirmationDialog('<fmt:message key="edit.cApp.api.on.page.prompt"/>', function() {
       $.ajax({
               type: 'POST',
               success: function() {
                     document.location.href = "manageAPI.jsp?mode=edit&apiName="+apiName;
               }
       });
       });
    }
</script>
<%
    }
%>

<div id="middle">
<h2><fmt:message key="deployed.apis"/></h2>

<div id="workArea">
<a style="background-image: url(../admin/images/add.gif);" href="manageAPI.jsp?mode=add" class="icon-link">
                                <fmt:message key="add.api"/>
</a>
<p>&nbsp;</p>
<%
    if (apis != null) {
        /*String parameters = "serviceTypeFilter=" + serviceTypeFilter +
                "&serviceSearchString=" + serviceSearchString;*/
%> 

<%
	if (loggedIn && isAuthorizedToManage) {
%>
<% } %>
<p>&nbsp;</p>

    <form action="index.jsp" name="searchForm">
        <table style="border:0; !important">
            <tbody>
            <tr style="border:0; !important">
                <td style="border:0; !important">
                    <nobr>
                        <fmt:message key="search.api"/>
                        <input type="text" name="apiSearchString"
                               value="<%= apiSearchString != null? apiSearchString : ""%>"/>&nbsp;
                    </nobr>
                </td>
                <td style="border:0; !important">
                    <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                       onclick="javascript:searchSequence(); return false;"
                       alt="<fmt:message key="search"/>"></a>
                </td>
            </tr>
            </tbody>
        </table>
    </form>

    <br/>
    <p><fmt:message key="api.synapse.text"/>
    <%
    	if (client!=null) {
    %>
	    <%=": " + client.getAPICount()%>
    <%
	}
    %>
    </p>
    <br/>

<form action="delete_service_groups.jsp" name="servicesForm" method="post">
    <input type="hidden" name="pageNumber" value="<%= pageNumber%>"/>
    <carbon:paginator pageNumber="<%=pageNumber%>"
                      numberOfPages="<%=numberOfPages%>"
                      page="index.jsp"
                      pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=\"\"%>"/>
    <br/>
    <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                              selectAllFunction="selectAllInAllPages()"
                              selectNoneFunction="selectAllInThisPage(false)"
                              addRemoveFunction="deleteServices()"
                              addRemoveButtonId="delete2"
                              resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                              selectAllInPageKey="selectAllInPage"
                              selectAllKey="selectAll"
                              selectNoneKey="selectNone"
                              addRemoveKey="delete"
                              numberOfPages="<%=numberOfPages%>"/>
    <br/>
    <table class="styledLeft" id="sgTable" width="100%">
        <thead>
        <tr>
            <th><fmt:message key="api.select"/></th>
        	<th><fmt:message key="api.name"/></th>
        	<th><fmt:message key="api.invocation.url"/></th>
        	<th colspan="2"><fmt:message key="apis.table.action.header"/></th>
        </tr>
        </thead>
        <tbody>

        <%
            int position = 0;
            for (APIData apiData : apis) {
                String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                position++;
                if (apiData == null) {
                    continue;
                }
        %>

        <tr bgcolor="<%=bgColor%>">
                    <% if (loggedIn) {%>
                    <% } %>
            <td width="10px" style="text-align:center; !important">
                <input type="checkbox" name="apiGroups"
                       value="<%=apiData.getName()%>"
                       onclick="resetVars()" class="chkBox"/>
                &nbsp;
            </td>
            <td width="100px">
                <nobr>
                 <% if(apiData.getDeployedFromCApp()) { %>
                    <img src="images/applications.gif">
                    <%=apiData.getName()%>
                    <% if(apiData.getEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                 <% } else { %>
                    <%=apiData.getName()%>
                 <% } %>
                </nobr>
            </td>
            <td width="100px">
                <nobr>
                    <%=serverContext + apiData.getContext()%>
                </nobr>
            </td>
            <td width="20px" style="text-align:left;border-left:none;border-right:none;width:100px;">
                <div class="inlineDiv">
                <% if(apiData.getDeployedFromCApp()) { %>
                    <a style="background-image:url(../admin/images/edit.gif);" class="icon-link"  onclick="editCAppApi('<%=apiData.getName()%>')">Edit</a>
                <% } else {%>
                    <a style="background-image:url(../admin/images/edit.gif);" class="icon-link"  onclick="editApi('<%=apiData.getName()%>')">Edit</a>
                <% } %>
                </div>
            </td>
            <td width="20px" style="text-align:left;border-left:none;width:100px;">
                <div class="inlineDiv">
                <% if(apiData.getDeployedFromCApp()) { %>
                    <a style="background-image:url(../admin/images/delete.gif);" class="icon-link" href="#"
                       onclick="#">Delete</a>
                <% } else {%>
                    <a style="background-image:url(../admin/images/delete.gif);" class="icon-link" href="#"
                    	onclick="deleteApi('<%=apiData.getName()%>')">Delete</a>
                <% } %>
                </div>
            </td>

        </tr>
        <%
            } // for each api
        %>
        </tbody>
    </table>
    <br/>
    <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                              selectAllFunction="selectAllInAllPages()"
                              selectNoneFunction="selectAllInThisPage(false)"
                              addRemoveFunction="deleteServices()"
                              addRemoveButtonId="delete2"
                              resourceBundle="org.wso2.carbon.service.mgt.ui.i18n.Resources"
                              selectAllInPageKey="selectAllInPage"
                              selectAllKey="selectAll"
                              selectNoneKey="selectNone"
                              addRemoveKey="delete"
                              numberOfPages="<%=numberOfPages%>"/>
    <br/>
    <carbon:paginator pageNumber="<%=pageNumber%>"
                      numberOfPages="<%=numberOfPages%>"
                      page="index.jsp"
                      pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=\"\"%>"/>
</form>
<p>&nbsp;</p>
<%
    if (loggedIn && isAuthorizedToManage) {
%>
<% } %>
<%
} else {
%>
<b><fmt:message key="no.deployed.services.found"/></b>
<%
    }
%>
</div>
</div>
</fmt:bundle>
