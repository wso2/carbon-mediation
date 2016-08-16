<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.stub.types.service.EndpointMetaData" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointService" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointStore" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">
<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/ressubmitEndpoint.jspource_util.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:breadcrumb
        label="endpoints"
        resourceBundle="org.wso2.carbon.newendpoint.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<%
    String sequenceSearchString = request.getParameter("sequenceSearchString");
    boolean isSuccessfulSearch = true;
    if (sequenceSearchString == null) {
        sequenceSearchString = "";
    }
String pageNumberStr = request.getParameter("pageNumber");
    String dynamicPageNumberStr = request.getParameter("dynamicPageNumber");

    int endpointPageNumber = 0;
    int dynamicEnpointPageNumber = 0;
    if (pageNumberStr != null) {
        endpointPageNumber = Integer.parseInt(pageNumberStr);
    }
    if(dynamicPageNumberStr!=null){
        dynamicEnpointPageNumber = Integer.parseInt(dynamicPageNumberStr);
    }
%>


<script type="text/javascript">

var allServicesSelected = false;
//Script for search Endpoints
function searchSequence() {
    document.searchForm.submit();
}

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

function enableStat(endpointName) {
    $.ajax({
               type: 'POST',
               url: 'ajaxprocessors/stat-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=enableStat',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleStatCallback('enableStat', endpointName);
                   }
               }
           });
}

function disableStat(endpointName) {
    $.ajax({
               type: 'POST',
               url: 'ajaxprocessors/stat-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=disableStat',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleStatCallback('disableStat', endpointName);
                   }
               }
           });
}

function handleStatCallback(action, endpointName) {
    var element;
    if (action == 'enableStat') {
        element = document.getElementById("disableStat" + endpointName);
        element.style.display = "";
        element = document.getElementById("enableStat" + endpointName);
        element.style.display = "none";
    } else {
        element = document.getElementById("disableStat" + endpointName);
        element.style.display = "none";
        element = document.getElementById("enableStat" + endpointName);
        element.style.display = "";
    }
}

function switchOn(endpointName) {
    $.ajax({
               type: 'POST',
               url: 'ajaxprocessors/switchOnOff-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=switchOn',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleSwitchOnOffCallback('switchOn', endpointName);
                   }
               }
           });
}

function switchOff(endpointName) {
    $.ajax({
               type: 'POST',
               url: 'ajaxprocessors/switchOnOff-ajaxprocessor.jsp',
               data: 'endpointName=' + endpointName + '&action=switchOff',
               success: function(msg) {
                   var index = msg.toString().trim().indexOf('<div>');
                   if (index != -1 && msg.toString().trim().indexOf('<div>Error:') == index) {
                       CARBON.showErrorDialog(msg.toString().trim().substring(index + 17));
                   } else {
                       handleSwitchOnOffCallback('switchOff', endpointName);
                   }
               }
           });
}

function handleSwitchOnOffCallback(action, endpointName) {
    var element;
    if (action == 'switchOn') {
        element = document.getElementById("switchOff" + endpointName);
        element.style.display = "";
        element = document.getElementById("switchOn" + endpointName);
        element.style.display = "none";
    } else {
        element = document.getElementById("switchOff" + endpointName);
        element.style.display = "none";
        element = document.getElementById("switchOn" + endpointName);
        element.style.display = "";
    }
}

function goBack(orginiator) {
    if (orginiator == null) {
        alert('Error: Origin not found');
        return false;
    }
    document.location.href = orginiator + '?cancelled=true';
    return true;
}

function deleteEndpoint(endpointName) {
    CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.endpoint"/> " + escape(endpointName) + " ?", function() {
        $.ajax({
                   type: 'POST',
                   url: 'ajaxprocessors/deleteEndpoint-ajaxprocessor.jsp',
                   data: 'endpointName=' + endpointName + '&force=false',
                   success: function(msg) {
                       var index = msg.toString().trim().indexOf('<div>Dep Error:</div>');
                       if (index != -1) {
                           confirmForceDelete(endpointName, msg.toString().trim().substring(index + 21));
                       } else {
                           loadEndpointsAfterDeletion();
                       }
                   }
               });
    });

}

function loadEndpointAfterBulkDeletion(){
    location.assign("index.jsp?pageNumber=<%=endpointPageNumber%>");
}

function loadEndpointsAfterDeletion() {
    jQuery.ajax({
        type: "POST",
        url: "ajaxprocessors/deleteEndpoint-ajaxprocessor.jsp",
        data: {"loadpage": true, "pageNumber": <%=endpointPageNumber%>},
        async: false,
        success: function (result, status, xhr) {
            if (status != "success") {
                CARBON.showErrorDialog(jsi18n["endpoint.design.load.error"]);
            }
            location.assign("index.jsp?pageNumber=<%=endpointPageNumber%>");
        }
    });
}

function deleteDynamicEndpoint(key) {
    CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.endpoint"/> " + escape(key) + "?", function () {
        jQuery.ajax({
            type: "POST",
            url: "ajaxprocessors/deleteDynamicEndpoint-ajaxprocessor.jsp",
            data: {"endpointName": key, "dynamicPageNumber": <%=dynamicEnpointPageNumber%>},
            async: false,
            success: function (result, status, xhr) {
                if (status != "success") {
                    CARBON.showErrorDialog(jsi18n["endpoint.design.load.error"]);
                }
                location.assign("index.jsp?pageNumber=<%=endpointPageNumber%>");
            }
        });
    });
}

function confirmForceDelete(endpointName, msg) {
    CARBON.showConfirmationDialog('This endpoint is a dependency for following items!<br/><br/>'
                                          + msg + '<br/>Force delete?', function() {
        $.ajax({
                   type: 'POST',
                   url: 'ajaxprocessors/deleteEndpoint-ajaxprocessor.jsp',
                   data: 'endpointName=' + endpointName + '&force=true',

                   success: function(msg) {
                       loadEndpointsAfterDeletion();
                   }
               });
    });
}

function editEndpoint(endpointType, endPointName) {
    document.location.href = endpointType + 'Endpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit';
}

function editCAppEndpoint(endpointType, endPointName) {
    CARBON.showConfirmationDialog("The changes will not persist to the CAPP after restart or redeploy. " +
            "Do you want to Edit?", function() {
        $.ajax({
            type: 'POST',
            success: function() {
                document.location.href = endpointType + 'Endpoint.jsp?endpointName='
                        + endPointName + '&endpointAction=edit';
            }
        });
    });
}

function editDynamicEndpoint(key) {
    if (key != null && key != undefined && key != "") {
        location.href = "dynamicEndpoint.jsp?anonEpAction=edit&key=" + key;
    } else {
        CARBON.showErrorDialog("Specify the key of the Endpoint to be edited");
    }
}

function deleteServices() {
    var selected = false;
    if (document.endpointForm.endpointGroups[0] != null) { // there is more than 1 endpoints
        for (var j = 0; j < document.endpointForm.endpointGroups.length; j++) {
            selected = document.endpointForm.endpointGroups[j].checked;
            if (selected) break;
        }
    }

    else if (document.endpointForm.endpointGroups != null) { // only 1 endpoint
        selected = document.endpointForm.endpointGroups.checked;
    }
    if (!selected) {
        CARBON.showInfoDialog('<fmt:message key="select.endpoint.to.be.deleted"/>');
        return;
    }
    if (allServicesSelected) {
        CARBON.showConfirmationDialog("<fmt:message key="delete.endpoint.on.all.prompt"/>", function() {
            $.ajax({
                type: 'POST',
                url: 'ajaxprocessors/delete_endpoint_groups-ajaxprocessor.jsp',
                data: 'deleteAllEndpointGroups=true',
                success: function(msg) {
                    loadEndpointAfterBulkDeletion();
                }
            });
        });
    } else {

        var endpointGroupsString = '';
        jQuery('.chkBox').each(function(index) {
            if(this.checked) {
                endpointGroupsString += this.value + ':';
            }
        });

        CARBON.showConfirmationDialog("<fmt:message key="delete.endpoint.on.page.prompt"/>",function() {
            $.ajax({
                type: 'POST',
                url: 'ajaxprocessors/delete_endpoint_groups-ajaxprocessor.jsp',
                data: 'endpointGroupsString='+ endpointGroupsString,
                success: function(msg) {
                    loadEndpointAfterBulkDeletion();
                }
            });
        });
    }
}

function selectAllInThisPage(isSelected) {
    allServicesSelected = false;
    if (document.endpointForm.endpointGroups != null &&
            document.endpointForm.endpointGroups[0] != null) { // there is more than 1 endpoint
        if (isSelected) {
            for (var j = 0; j < document.endpointForm.endpointGroups.length; j++) {
                document.endpointForm.endpointGroups[j].checked = true;
            }
        } else {
            for (j = 0; j < document.endpointForm.endpointGroups.length; j++) {
                document.endpointForm.endpointGroups[j].checked = false;
            }
        }
    } else if (document.endpointForm.endpointGroups != null) { // only 1 endpoint
        document.endpointForm.endpointGroups.checked = isSelected;
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
    if (document.endpointForm.endpointGroups[0] != null) { // there is more than 1 endpoint
        for (var j = 0; j < document.endpointForm.endpointGroups.length; j++) {
            if (document.endpointForm.endpointGroups[j].checked) {
                isSelected = true;
            }
        }
    } else if (document.endpointForm.endpointGroups != null) { // only 1 endpoint
        if (document.endpointForm.endpointGroups.checked) {
            isSelected = true;
        }
    }
    return false;
}
</script>
<%
    int numberOfPages = 0;
    int numberOfDynamicPages = 0;
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient client;
    EndpointMetaData[] ePMetaData = null;
    String[] dynamicEndpoints = null;
    try {
        client = new EndpointAdminClient(cookie, serverURL, configContext);
        int epCount = 0;
        int dynamicEpCount = 0;
        if (sequenceSearchString.equals("")) {
            ePMetaData = client.getEndpointMetaData(endpointPageNumber, EndpointAdminClient.ENDPOINT_PER_PAGE);
            dynamicEndpoints = client.getDynamicEndpoints(dynamicEnpointPageNumber, EndpointAdminClient.ENDPOINT_PER_PAGE);
            epCount = client.getEndpointCount();
            dynamicEpCount = client.getDynamicEndpointCount();
        }
        else {
             ePMetaData = client.getEndpointMetaDataSearch(endpointPageNumber, EndpointAdminClient.ENDPOINT_PER_PAGE,sequenceSearchString);
             if (ePMetaData == null) {
                isSuccessfulSearch =false;
                ePMetaData = client.getEndpointMetaData(endpointPageNumber, EndpointAdminClient.ENDPOINT_PER_PAGE);
                epCount = client.getEndpointCount();
             } else {
                epCount = ePMetaData.length;
             }
             dynamicEndpoints = client.getDynamicEndpoints(dynamicEnpointPageNumber, EndpointAdminClient.ENDPOINT_PER_PAGE);
             dynamicEpCount = client.getDynamicEndpointCount();
        }

        if (epCount % EndpointAdminClient.ENDPOINT_PER_PAGE == 0) {
            numberOfPages = epCount / EndpointAdminClient.ENDPOINT_PER_PAGE;
        } else {
            numberOfPages = epCount / EndpointAdminClient.ENDPOINT_PER_PAGE + 1;
        }

        if (dynamicEpCount % EndpointAdminClient.ENDPOINT_PER_PAGE == 0) {
            numberOfDynamicPages = dynamicEpCount / EndpointAdminClient.ENDPOINT_PER_PAGE;
        } else {
            numberOfDynamicPages = dynamicEpCount / EndpointAdminClient.ENDPOINT_PER_PAGE + 1;
        }

    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }

    //Template specific parameters
    String templateAdd = request.getParameter("templateAdd");
    boolean isTemplateAdd = templateAdd != null && "true".equals(templateAdd) ? true : false;

    // Anonymous Endpoint specific parameters
    String endpointMode = null; // this holds an anonymous endpoint which can come trough proxy and send mediator
    String anonymousOriginator = null;
    boolean isAnonymous = false;
    String anonymousEndpointXML = null;
    // came through clicking menu on the left
    if (request.getParameter("region") != null && request.getParameter("item") != null) {
        session.removeAttribute("epMode");
        session.removeAttribute("anonEpXML");
        session.removeAttribute("proxy");
        session.removeAttribute("header");
        session.removeAttribute("endpointTemplate");
        session.removeAttribute("templateEdittingMode");
        session.removeAttribute("templateRegKey");
    } else {
        // a user is adding an anonymous endpoint
        endpointMode = (String) session.getAttribute("epMode");
        if (endpointMode != null && endpointMode.equals("anon")) {
            // if the user is here that means the user is going to add (not edit an existing endpoint)
            // a new endpoint
            isAnonymous = true;
            anonymousOriginator = (String) session.getAttribute("anonOriginator");
        }
    }
    String proxyServiceName = request.getParameter("serviceName");
%>

<div id="middle">
<h2>
    <%
        if (!isAnonymous) {
            if (isTemplateAdd) {
    %><fmt:message key="manage.endpoints.template"/><%
} else {
%><fmt:message key="manage.endpoints"/><%
    }
} else {
    if (proxyServiceName != null) {
%><%=proxyServiceName%>:&nbsp;<%
    }
%><fmt:message key="create.anon.endpoint"/><%
    }
%>
</h2>

<div id="workArea" style="background-color:#F4F4F4;">
<%
    if (!isAnonymous) { //hide tabs during anonymous mode
%>
<div id="tabs">
<ul>
    <% if (!isTemplateAdd) { %>
    <li><a href="#tabs-1"><fmt:message key="defined.endpoints"/></a></li>
    <li><a href="#tabs-2"><fmt:message key="dynamic.endpoints"/></a></li>
    <%}%>
    <li><a href="#tabs-3"><fmt:message
            key="<%=isTemplateAdd ?"add.endpoint.template":"add.endpoint"%>"/></a></li>
</ul>

<!--Tab 1: Endpoint List-->
<% if (!isTemplateAdd) { %>
<div id="tabs-1">
    <br/>
    <form action="index.jsp" name="searchForm">
        <table style="border:0; !important">
            <tbody>
            <tr style="border:0; !important">
                <td style="border:0; !important">
                    <nobr>
                        <fmt:message key="search.endPoint"/>
                        <label>
                            <input type="text" name="sequenceSearchString"
                                   value="<%= sequenceSearchString != null? Encode.forHtml(sequenceSearchString) : ""%>"/>
                        </label>&nbsp;
                    </nobr>
                </td>
                <td style="border:0; !important">
                    <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                       onclick="javascript:searchSequence(); return false;"
                       alt="<fmt:message key="search"/>"></a>
                </td>
                <%
                    if (!isSuccessfulSearch) {
                %>
                <td style="border:0; !important">
                    <fmt:message key="search.unable.endPoint"/>
                </td>
                <%
                    }
                %>
            </tr>
            </tbody>
        </table>
    </form>
    <br/>
    <div id="noEpDiv" style="<%=ePMetaData!=null ?"display:none":""%>">
        <fmt:message
                key="no.endpoints.in.synapse.config"></fmt:message>
    </div>

    <% if (ePMetaData != null) {%>
    <script type="text/javascript">
        isDefinedSequenceFound = true;
    </script>
    <p><fmt:message key="endpoints.synapse.text"/>
    <%
        if (client!=null) {
    %>
    <%=" :- " + client.getEndpointCount()%>
    <%
        }
    %>
    </p>
    <br/>
    <carbon:paginator pageNumber="<%=endpointPageNumber%>" numberOfPages="<%=numberOfPages%>"
                  page="index.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=""%>" />
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
    <form name="endpointForm" action="ajaxprocessors/delete_endpoint_groups-ajaxprocessor.jsp" method="post">
        <input type="hidden" name="pageNumberEndpoint" value="<%= pageNumberStr%>"/>
        <table class="styledLeft" cellpadding="1" id="endpointListTable">
        <thead>
        <tr>
            <th style="width:4%"><fmt:message key="endpoint.select"/></th>
            <th style="width:20%"><fmt:message key="endpoint.name"/></th>
            <th style="width:20%"><fmt:message key="type"/></th>
            <th colspan="4"><fmt:message key="action"/></th>
        </tr>
        </thead>

        <tbody>
        <%for (EndpointMetaData endpoint : ePMetaData) {%>
        <tr>
            <td width="10px" style="text-align:center; !important">
                <input type="checkbox" name="endpointGroups"
                       value="<%= Encode.forHtmlAttribute(endpoint.getName()) %>"
                       onclick="resetVars()" class="chkBox"/>
                &nbsp;
            </td>
            <td><% if (endpoint.getDescription() != null) { %>
                <% if (endpoint.getArtifactContainerName() != null) { %>
                    <span href="#">
                        <img src="images/applications.gif">
                        <%= Encode.forHtmlContent(endpoint.getName())%>
                        <% if(endpoint.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                    </span>
                <% } else { %>
                    span href="#"><%= Encode.forHtmlContent(endpoint.getName())%></span>
                <% } %>
                <% } else { %>
                <% if (endpoint.getArtifactContainerName() != null) { %>
                    <span href="#">
                        <img src="images/applications.gif">
                            <%= Encode.forHtmlContent(endpoint.getName())%>
                            <% if(endpoint.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                    </span>
                <% } else { %>
                    <span href="#"><%= Encode.forHtmlContent(endpoint.getName())%></span>
                <% } %>
                <% } %>


            </td>
            <td>
                <%
                    EndpointService ePService = client.getEndpointService(endpoint);
                %>
                <%=Encode.forHtmlContent(ePService.getDisplayName())%>
            </td>
            <td style="border-right:none;border-left:none;width:100px">
                <div class="inlineDiv">
                    <% if (endpoint.getSwitchOn()) { %>
                    <div id="switchOff<%= Encode.forHtmlAttribute(endpoint.getName()) %>">
                        <a href="#" onclick="switchOff('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-on.gif);"><fmt:message
                                key="switch.off"/></a>
                    </div>
                    <div id="switchOn<%= Encode.forHtmlAttribute(endpoint.getName()) %>" style="display:none;">
                        <a href="#" onclick="switchOn('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-off.gif);"><fmt:message
                                key="switch.on"/></a>
                    </div>
                    <%} else {%>
                    <div id="switchOff<%= Encode.forHtmlAttribute(endpoint.getName()) %>" style="display:none;">
                        <a href="#" onclick="switchOff('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-on.gif);"><fmt:message
                                key="switch.off"/></a>
                    </div>
                    <div id="switchOn<%= Encode.forHtmlAttribute(endpoint.getName()) %>" style="">
                        <a href="#" onclick="switchOn('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           class="icon-link"
                           style="background-image:url(images/endpoint-off.gif);"><fmt:message
                                key="switch.on"/></a>
                    </div>
                    <% }
                        if (ePService.isStatisticsAvailable()) {
                            if (endpoint.getEnableStatistics()) { %>
                    <td style="border-right:none;border-left:none;width:100px">
                        <div id="disableStat<%= Encode.forHtmlAttribute(endpoint.getName()) %>">
                            <a href="#" onclick="disableStat('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="disable.statistics"/></a>
                        </div>
                        <div id="enableStat<%= Encode.forHtmlAttribute(endpoint.getName()) %>" style="display:none;">
                            <a href="#" onclick="enableStat('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="enable.statistics"/></a>
                        </div>
                    </td>
                </div>
            </td>
            <%
            } else { %>
            <td style="border-right:none;border-left:none;width:100px">
                <div class="inlineDiv">
                    <div id="enableStat<%= Encode.forHtmlAttribute(endpoint.getName()) %>">
                        <a href="#" onclick="enableStat('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                key="enable.statistics"/></a>
                    </div>
                    <div id="disableStat<%= Encode.forHtmlAttribute(endpoint.getName()) %>" style="display:none">
                        <a href="#" onclick="disableStat('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                key="disable.statistics"/></a>
                    </div>
                </div>
            </td>

            <% }
            } else {%>
            <td style="border-right:none;border-left:none;width:100px"></td>
            <%
                }
            %>

            <% if (endpoint.getArtifactContainerName() != null) { %>
                <td style="border-left:none;border-right:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#"
                           class="icon-link"
                           onclick="editCAppEndpoint('<%=ePService.getUIPageName()%>','<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="edit"/></a>
                    </div>
                </td>
            <% } else {%>
                <td style="border-left:none;border-right:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#"
                           class="icon-link"
                           onclick="editEndpoint('<%=ePService.getUIPageName()%>','<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="edit"/>
                        </a>
                    </div>
                </td>
            <% } %>

            <% if (endpoint.getArtifactContainerName() != null) { %>
                <td style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#"
                           onclick="#"
                           class="icon-link"
                           style="color:grey;background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="delete"/>
                        </a>
                    </div>
                </td>
            <% } else {%>
                <td style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#"
                           onclick="deleteEndpoint('<%= Encode.forJavaScriptAttribute(endpoint.getName()) %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="delete"/></a>
                    </div>
                </td>
            <% } %>
        </tr>
        <%}%>
        </tbody>
    </table>
    </form>
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
    <carbon:paginator pageNumber="<%=endpointPageNumber%>" numberOfPages="<%=numberOfPages%>"
                          page="index.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=""%>" />
    <% } %>
</div>

<!--Tab 2: Dynamic Endpoints-->
<div id="tabs-2">
    <%
        if (client.isRegisterNull() && dynamicEndpoints != null) {
            dynamicEndpoints = null;
            dynamicEnpointPageNumber = 0;
    %>
    <div id="noEpDiv"
         style="<%=dynamicEndpoints!=null || isAnonymous?"display:none":""%>">
        <fmt:message
                key="enable.endpoints.in.registry"></fmt:message>
    </div>
    <%
    } else {
    %>
    <div id="noEpDiv"
         style="<%=dynamicEndpoints!=null || isAnonymous?"display:none":""%>">
        <fmt:message
                key="no.endpoints.in.registry"></fmt:message>
    </div>
    <%
        }
        if ((dynamicEndpoints != null) && (dynamicEndpoints.length > 0) && (!isAnonymous)) {
    %>
    <script type="text/javascript">
        isDynamicSequenceFound = true;
    </script>
    <p><fmt:message key="endpoints.dynamic.text"/></p>
    <br/>
    <carbon:paginator pageNumber="<%=dynamicEnpointPageNumber%>"
                      numberOfPages="<%=numberOfDynamicPages%>"
                      page="index.jsp" pageNumberParameterName="dynamicPageNumber"
                      resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=\"\"%>"/>
    <br/>
    <table class="styledLeft" cellspacing="1" id="dynamicEndpointsTable">
        <thead>
        <tr>
            <th style="width:30%">
                <fmt:message key="endpoint.name"/>
            </th>
            <th style="width:20%">
                <fmt:message key="type"/>
            </th>
            <th class="registryWriteOperation" colspan="2">
                <fmt:message key="action"/>
            </th>
        </tr>
        </thead>
        <tbody>
        <% for (String endpoint : dynamicEndpoints) { 
        	String epXML = client.getDynamicEndpoint(endpoint);
        	if(epXML == null){
        		continue;
        	}%>
        <tr>
            <td>
                <%=endpoint %>
            </td>
            <td>
                <%  //String epXML = client.getDynamicEndpoint(endpoint);
                    EndpointService epService = client.getEndpointService(epXML);
                %>
                <%=Encode.forHtmlContent(epService.getDisplayName())%>
            </td>
            <td class="registryWriteOperation" style="border-right:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="editDynamicEndpoint('<%=endpoint%>')" class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"><fmt:message
                            key="edit"/></a>
                </div>
            </td>
            <td class="registryWriteOperation" style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#" onclick="deleteDynamicEndpoint('<%= endpoint %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"><fmt:message
                            key="delete"/></a>
                </div>
            </td>
        </tr>
        <%}%>
        </tbody>
    </table>
    <br/>
    <carbon:paginator pageNumber="<%=dynamicEnpointPageNumber%>" numberOfPages="<%=numberOfDynamicPages%>"
                          page="index.jsp" pageNumberParameterName="dynamicPageNumber"
                          resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=\"\"%>"/>
    <%
        }
    %>
    <br/>
</div>

<%}%>
<%} //hide tabs during anonymous mode%>

<!-- Tab 3: Add new Endpoints -->
<div id="tabs-3">
    <table id="endpointOptionTable" class="styledLeft" cellpadding="1">
        <thead>
        <tr>
            <th colspan="2">
                <fmt:message key="select.endpoint.type"/>
            </th>
        </tr>
        </thead>
        <%
            String proxyServiceParam = proxyServiceName != null ?
                                       "serviceName=" + proxyServiceName : "";
            String fullQueryString = (isTemplateAdd ? "templateAdd=true&" : "") +
                                     proxyServiceParam;
            Collection<EndpointService> endpointServices = EndpointStore.getInstance().getRegisteredEndpoints();
            for (EndpointService endpointService : endpointServices) {
                if (isTemplateAdd && endpointService.canAddAsTemplate()) {
        %>
        <tr>
            <td width="155px">
                <a class="icon-link"
                   href="<%=endpointService.getUIPageName()%>Endpoint.jsp?<%=fullQueryString%>"
                   style="background-image: url(../admin/images/add.gif);">
                    <%=Encode.forHtmlContent(endpointService.getDisplayName())%> Template
                </a>
            </td>
            <td>
                <%=endpointService.getDescription()%>
            </td>
        </tr>
        <% } else if (!isTemplateAdd) { %>
        <tr>
            <td width="155px">
                <a class="icon-link"
                   href="<%=endpointService.getUIPageName()%>Endpoint.jsp?<%=fullQueryString%>"
                   style="background-image: url(../admin/images/add.gif);">
                    <%=Encode.forHtmlContent(endpointService.getDisplayName()) %>
                </a>
            </td>
            <td>
                <%=endpointService.getDescription()%>
            </td>
        </tr>
        <% }
        }
        %>
        <tr id="btnRow" style="<%=isAnonymous?"":"display:none"%>">
            <td colspan="2" class="buttonRow">
                <input id="cancelBtn" type="button" value="<fmt:message key="back"/>"
                       class="button"
                       onclick="goBack('<%=anonymousOriginator%>');return false"/>
            </td>
        </tr>
    </table>
</div>
<% if (!isAnonymous) { //hide tabs during anonymous mode%>
</div>
<%}%>
</div>
</div>

</fmt:bundle>
