<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData" %>
<%@page import="org.wso2.carbon.rest.api.ui.client.RestApiAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<script type="text/javascript" src="js/api-util.js"></script>

<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
        request="<%=request%>"/>
<carbon:breadcrumb
        label="manage.api"
        resourceBundle="org.wso2.carbon.rest.api.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<%
    ResourceBundle bundle = ResourceBundle.getBundle(
            "org.wso2.carbon.rest.api.ui.i18n.Resources",
            request.getLocale());
    String url = CarbonUIUtil.getServerURL(this.getServletConfig()
                                                   .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RestApiAdminClient client = new RestApiAdminClient(
            configContext, url, cookie, bundle.getLocale());

    String mode = request.getParameter("mode");
    boolean fromSourceView = session.getAttribute("fromSourceView") != null;
    session.removeAttribute("fromSourceView");

    boolean fromResourceSourceView = session.getAttribute("fromResourceSourceView") != null;
    session.removeAttribute("fromResourceSourceView");

    boolean fromApiDataSourceView = session.getAttribute("fromApiDataSourceView") != null;
    session.removeAttribute("fromApiDataSourceView");

    //clear resource, which added from sequence editor
    session.removeAttribute("resource");

    String apiName = "";
    String apiContext = "";
    String filename = "";
    String hostname = "";
    String port = "";

    APIData apiData = null;

    List<ResourceData> resourceList;
    session.removeAttribute("mode");
    session.setAttribute("mode", mode);
    if ("edit".equals(mode)) {
        //To apply changes that might have been made in the source view
        apiData = session.getAttribute("apiData") != null ? (APIData) session
					.getAttribute("apiData") : null;

			if (apiData == null) {
				apiName = request.getParameter("apiName");
				try {
					apiData = client.getApiByNane(apiName);
				} catch (Exception e) {
					String msg = "Unable to get API data: "
							+ e.getMessage();
					CarbonUIMessage.sendCarbonUIMessage(msg,
							CarbonUIMessage.ERROR, request);
				}
			// restrict previous API data load for new API request
			} else if (apiData.getName() == null || !apiData.getName().equals(
					request.getParameter("apiName"))) {

				apiName = request.getParameter("apiName");
				// remove previous session API data with the end-point data
				session.removeAttribute("anonEpXML");
				session.removeAttribute("apiData");

				try {
					apiData = client.getApiByNane(apiName);
				} catch (Exception e) {
					String msg = "Unable to get API data: "
							+ e.getMessage();
					CarbonUIMessage.sendCarbonUIMessage(msg,
							CarbonUIMessage.ERROR, request);
				}

			} else {
				apiName = apiData.getName();
				//if page loaded from API List view, new APIData should be loaded again.
				if(!fromSourceView && !fromResourceSourceView){
                                    try {
                                        apiData = client.getApiByNane(apiName);
                                    } catch (Exception e) {
                                        String msg = "Unable to get API data: "
                                                + e.getMessage();
                                        CarbonUIMessage.sendCarbonUIMessage(msg,
                                                CarbonUIMessage.ERROR, request);
                                    }
                                }
			}

        apiContext = apiData.getContext();
        //If api context contains a preceeding '/'
        if (apiContext.startsWith("/")) {
            //Remove preceeding '/' for displaying
            apiContext = apiContext.substring(1);
        }
        filename = apiData.getFileName();
        port = String.valueOf(apiData.getPort() != -1 ? apiData.getPort() : "");
        hostname = apiData.getHost() != null? apiData.getHost() : "";
        
        if(fromResourceSourceView){
        	hostname = request.getParameter("hostname");
        	port = request.getParameter("port");
        }
        
        if (apiData.getResources() != null) {
            resourceList = new ArrayList<ResourceData>(Arrays.asList(apiData.getResources()));
        } else {
            resourceList = new ArrayList<ResourceData>();
        }
    }
    //If not in edit mode, we are adding an API
    else {
        //To apply changes that might have been made in the source view
        if (fromSourceView || fromResourceSourceView) {
            apiData = (APIData) session.getAttribute("apiData");
            apiName = apiData.getName();
            apiContext = apiData.getContext();
            //If api context contains a preceeding '/'
            if (apiContext.startsWith("/")) {
                //Remove preceeding '/' for displaying
                apiContext = apiContext.substring(1);
            }
            if (apiData.getResources() != null) {
                resourceList = new ArrayList<ResourceData>(Arrays.asList(apiData.getResources()));
            } else {
                resourceList = new ArrayList<ResourceData>();
            }
            //session.removeAttribute("fromSourceView");
        } else {
            if (apiData == null) {
                apiData = new APIData();
                apiData.setPort(-1);
            } else {
            	apiData = (APIData) session.getAttribute("apiData");
            }
            resourceList = new ArrayList<ResourceData>();
        }
        
        port = String.valueOf(apiData.getPort() != -1 ? apiData.getPort() : "");
        hostname = apiData.getHost() != null? apiData.getHost() : "";
        
        if(fromResourceSourceView){
        	hostname = request.getParameter("hostname");
        	port = request.getParameter("port");
        }
    }
    String index = (String) session.getAttribute("index");
    int resourceIndex = -2;
    if (index != null) {
        resourceIndex = Integer.parseInt(index);
    }
    session.setAttribute("apiResources", resourceList);
    session.setAttribute("apiData", apiData);

    boolean isResourceUpdatePending = false;
    String rIndex = request.getParameter("resourceIndex");
    if(null != rIndex) {
        isResourceUpdatePending = true;
    }
%>

<script type="text/javascript">
YAHOO.util.Event.onDOMReady(init);

function init() {
<%if("edit".equals(mode)){%>
    document.getElementById("apiFileName").value = "<%=filename%>";
<%}%>
    buildResourceTree();
<%if (resourceIndex >= -1) {%>
    <%if (!fromApiDataSourceView) {%>
        loadResource('<%=resourceIndex%>');
    <%}%>
<%}%>
}

function treeColapse(icon) {

    var parentNode = icon.parentNode;
    var children = parentNode.childNodes;

    for (var i = 0; i < children.length; i++) {
        var child = children[i];

        if (child.className == "branch-node" || child.className == "child-list") {
            if (child.style.display == "") {
                child.style.display = "none";
                YAHOO.util.Dom.removeClass(icon, "minus-icon");
                YAHOO.util.Dom.addClass(icon, "plus-icon");
            }
            else if (child.style.display == "none") {
                child.style.display = "";
                YAHOO.util.Dom.removeClass(icon, "plus-icon");
                YAHOO.util.Dom.addClass(icon, "minus-icon");
            }
        }
    }
}

function buildResourceTree() {
    jQuery.ajax({
                    type: "POST",
                    <% if(isResourceUpdatePending) { %>
                        url: "treeBuilder-ajaxprocessor.jsp?updatePending=true",
                    <% } else { %>
                        url: "treeBuilder-ajaxprocessor.jsp?updatePending=false",
                    <% } %>
                    data:  "data=null",
                    success: function(data) {
                        jQuery("#parent").html(data);
                    }
                });
}

function addResource() {
    jQuery.ajax({
                    type: "POST",
                    url: "addResource-ajaxprocessor.jsp",
                    data:  "data=null",
                    success: function(data) {
                        jQuery("#info").html(data);
                    }
                });

    document.getElementById('resIndex').value = "-1";
    showResourceInfo();
}

function loadResource(index, isUpdatePending) {
    if(isUpdatePending) {
        CARBON.showConfirmationDialog('<fmt:message key="resource.update.pending"/>', function() {
            jQuery.ajax({
                      type: "POST",
                      url: "loadResource-ajaxprocessor.jsp?discardResourceData=true",
                      cache: false,
                      data: { index:index },
                      success: function(data) {
                          jQuery("#info").html(data);
                          jQuery.each(jQuery(".resources .resource"), function() { jQuery(this).attr("onClick", "loadResourceData(this, false)")});
                      }
                  });

            document.getElementById('resIndex').value = index;
            showResourceInfo();
        });
    } else {
        jQuery.ajax({
                    type: "POST",
                    url: "loadResource-ajaxprocessor.jsp",
                    cache: false,
                    data: { index:index },
                    success: function(data) {
                        jQuery("#info").html(data);
                    }
                });

        document.getElementById('resIndex').value = index;
        showResourceInfo();
    }
}

function loadResourceData(a, isUpdatePending) {

    var allNodes = document.getElementById("parent").getElementsByTagName("*");
    for (var i = 0; i < allNodes.length; i++) {
        if (YAHOO.util.Dom.hasClass(allNodes[i], "selected-node")) {
            YAHOO.util.Dom.removeClass(allNodes[i], "selected-node");
        }
    }
    YAHOO.util.Dom.addClass(a, "selected-node");

    var parentId = a.parentNode.id;
    var index = parentId.split('.')[1];
    loadResource(index, isUpdatePending);
}

function getResourceNode(idSuffix) {
    var html = "<div id=\"branch." + idSuffix + "\" class=\"branch-node\"></div>";
    html += "<ul id=\"ul." + idSuffix + "\" class=\"child-list\">" +
            "<li>" +
            "<div class=\"dot-icon\"></div>" +
            "<div id=\"resource." + idSuffix + "\" class=\"resources\">" +
            <% if (isResourceUpdatePending) { %>
                "<a class=\"resource\" onclick=\"loadResourceData(this, true)\">Resource</a>" +
            <% } else { %>
                "<a class=\"resource\" onclick=\"loadResourceData(this, false)\">Resource</a>" +
            <% } %>
            "<div style=\"width: 100px;\" class=\"sequenceToolbar\">" +
            "<div>" +
            "<a class=\"deleteStyle\" onclick=\"deleteResource(" + idSuffix + ")\">Delete</a>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "</li>" +
            "</ul>";

    return html;
}

function showResourceInfo() {
    //Show hidden form
    document.getElementById('resourceInfo').style.display = '';
}

function hideResourceInfo() {
    //Hide form.
    document.getElementById('resourceInfo').style.display = 'none';
}

function urlStyleChanged() {
    var urlStyle = document.getElementById('urlStyle');
    var newValue = urlStyle.value;

    if (newValue == 'none') {
        document.getElementById('urlRow').style.display = 'none';
    }
    else {
        document.getElementById('urlRow').style.display = '';
        if (newValue == 'uritemplate') {
            document.getElementById('uriTL').style.display = '';
            document.getElementById('urlML').style.display = 'none';
        }
        else if (newValue == 'urlmapping') {
            document.getElementById('urlML').style.display = '';
            document.getElementById('uriTL').style.display = 'none';
        }
    }
}

function getElement(id) {
    return document.getElementById(id);
}

function seqIsNone(name) {
    return document.getElementById(name + 'SeqOpNone').checked;
}

function seqIsDefinedInline(name) {
    return document.getElementById(name + 'SeqOpAnon').checked
            && (getElement(name + 'AnonAddEdit').innerHTML != '<fmt:message key="create"/>');
}

function getSeqDefinedInline(name) {
    return (getElement(name + 'AnonAddEdit')).value;  // todo
}

function seqIsFromRegistry(name) {
    return document.getElementById(name + 'SeqOpReg').checked && getElement('api.' + name + '.registry')
            && getElement('api.' + name + '.registry').value != "";
}

function getSeqFromRegistry(name) {
    return getElement('api.' + name + '.registry').value;
}

function seqIsExistingImport(name) {
    return document.getElementById(name + 'SeqOpImp').checked && getElement(name + 'ImportSeq').value != 'None';
}

function getSeqExistingImport(name) {
    return getElement(name + 'ImportSeq').value;
}

function getSequenceValue(name) {
    if (seqIsNone(name)) {
        return "none";
    } else if (seqIsDefinedInline(name)) {
        return getSeqDefinedInline(name);
    } else if (seqIsFromRegistry(name)) {
        return getSeqFromRegistry(name);
    } else if (seqIsExistingImport(name)) {
        return getSeqExistingImport(name);
    }
    return "none";
}

function updateResource(v) {
    var index = document.getElementById('resIndex').value;
    var methods = "";
    var urlStyle = "";
    var url = "";
    var inSequence = "";
    var outSequence = "";
    var faultSequence = "";
    var apiNameValue = document.getElementById('api.name').value;
    var apiContextValue = document.getElementById('api.context').value;
    var apiHostnameValue = document.getElementById('api.hostname').value;
    var apiPortValue = document.getElementById('api.port').value;

    var methodList = document.apiForm.methods;
    var isTemp;
    if (v) {
        isTemp = v;
    }
    var i;
    for (i = 0; i < methodList.length; i++) {
        //If method is checked.
        if (methodList[i].checked) {
            //If methods exist, append comma to the start of the next method.
            if (methods != "") {
                methods += "," + methodList[i].value;
            }
            else {
                methods += methodList[i].value;
            }
        }
    }
    if (methods == "") {
        CARBON.showWarningDialog('<fmt:message key="resource.methods.required"/>');
        return false;
    }

    var styleElement = document.getElementById('urlStyle');
    urlStyle = styleElement.options[styleElement.selectedIndex].value;

    var urlElement = document.getElementById('urlValue');
    if (urlElement != null) {
        url = urlElement.value;
    }

    inSequence = getSequenceValue('in');
    var inSeqIsInline = seqIsDefinedInline('in');

    outSequence = getSequenceValue('out');
    var outSeqIsInline = seqIsDefinedInline('out');

    faultSequence = getSequenceValue('fault');
    var faultSeqIsInline = seqIsDefinedInline('fault');
    jQuery.ajax({
                    type: "POST",
                    url: "updateResource-ajaxprocessor.jsp",
                    async: false,
                    data: { apiName:apiNameValue, apiContext:apiContextValue,apiHostname:apiHostnameValue, apiPort:apiPortValue,
                        index:index, methods:methods, urlStyle:urlStyle, url:url,
                        inSequence:inSequence, outSequence:outSequence, faultSequence:faultSequence,
                        isSeqIsInline:inSeqIsInline, outSeqIsInline:outSeqIsInline, faultSeqIsInline:faultSeqIsInline,
                        isTemp:isTemp},
                    success: function(data) {
                        hideResourceInfo();
                        //we are adding a new resource.
                        if (index == -1) {
                            //Add a new node to the resource tree.
                        <%
                        resourceList = (ArrayList<ResourceData>)session.getAttribute("apiResources");
                        %>
                            document.getElementById("resourceSizeVar").innerHTML = data;
                            var tempsize = document.getElementById("resourcesSize");
                            if(tempsize) {
                                size = tempsize.value;
                            } else {
                                size = 0;
                            }
                            var parentNode = document.getElementById("parent");
                            var innerHtml = parentNode.innerHTML;
                            innerHtml += getResourceNode(size-1);
                            parentNode.innerHTML = innerHtml;
                        }
                        jQuery.each(jQuery(".resources .resource"), function() { jQuery(this).attr("onClick", "loadResourceData(this, false)")});
                    },
                    error:function() {
                        if (v == null) {
                            CARBON.showErrorDialog("<fmt:message key="api.update.error.451"/> ");
                            return false;
                        }
                    }
                });
    return true;
}

function saveApi(apiNameValue, apiContextValue, hostname, port) {
    var apiFileName = document.getElementById("apiFileName").value;

    apiContextValue = "/" + apiContextValue;
<%
    resourceList =
            (ArrayList<ResourceData>)session.getAttribute("apiResources");


        %>
<%if("add".equals(mode)){%>
    jQuery.ajax({
                    type: "POST",
                    url: "addapi-ajaxprocessor.jsp",
                    data: { apiName:apiNameValue, apiContext:apiContextValue, hostname:hostname, port:port },
                    success: function(data) {
                        CARBON.showInfoDialog("<fmt:message key="api.add.success"/> ", function() {
                            document.location.href = "index.jsp";
                        });
                    },
                    error:function(status) {
                        if (status.status == '452') {
                            CARBON.showErrorDialog("<fmt:message key="api.update.error.452"/> : " + apiNameValue);
                        } else if (status.status == '453') {
                            CARBON.showErrorDialog("<fmt:message key="api.update.error.453"/> : " + apiContextValue);
                        } else if(status.status == '454'){
                        	CARBON.showErrorDialog("<fmt:message key="api.update.error.454"/>");
                        }
                    }
                });
<%}
        else if("edit".equals(mode)){%>
    jQuery.ajax({
                    type: "POST",
                    url: "editapi-ajaxprocessor.jsp",
                    data: { apiName:apiNameValue, apiContext:apiContextValue, filename:apiFileName, hostname:hostname, port:port },
                    success: function(data) {
                        CARBON.showInfoDialog("<fmt:message key="api.update.success"/> ", function() {
                            document.location.href = "index.jsp";
                        });
                    },
                    error:function(status) {
                    	if (status.status == '453'){
                    		CARBON.showErrorDialog("<fmt:message key="api.update.error.453"/> : " + apiContextValue);
                    	} else if(status.status == '454'){
                        	CARBON.showErrorDialog("<fmt:message key="api.update.error.454"/>");
                        }
                    }
                });
<%}%>
}

function validateAndSaveApi() {
    var apiNameValue = document.getElementById('api.name').value;
    var apiContextValue = document.getElementById('api.context').value;
    var hostname = document.getElementById('api.hostname').value;
    var port = document.getElementById('api.port').value;

    if (apiNameValue == null || apiNameValue == "") {
        CARBON.showWarningDialog('<fmt:message key="api.name.required"/>');
        return false;
    }
    else if (apiNameValue.indexOf(' ')>=0) {
        CARBON.showWarningDialog('<fmt:message key="api.name.whiteSpace"/>');
        return false;
    }
    else if (apiContextValue == null || apiContextValue == "") {
        CARBON.showWarningDialog('<fmt:message key="api.context.required"/>');
        return false;
    }
    else if (apiContextValue.indexOf(' ')>=0) {
        CARBON.showWarningDialog('<fmt:message key="api.context.whiteSpace"/>');
        return false;
    }
    else if (port != null && port != "") {
        if (!(/^\d{1,5}([ ]\d{1,5})*$/).test(port)) {
            CARBON.showWarningDialog('<fmt:message key="api.port.invalid"/>');
            return false;
        }
    }

    jQuery.ajax({
                    type: "POST",
                    url: "validateResources-ajaxprocessor.jsp",
                    data: "data=null" ,
                    success:function() {
                        return saveApi(apiNameValue, apiContextValue, hostname, port);
                    },
                    error:function() {
                        CARBON.showWarningDialog('<fmt:message key="api.resources.empty"/>');
                    }
                });
<%
%>
}

function deleteResource(index) {

    CARBON.showConfirmationDialog("<fmt:message key="resource.delete.confirmation"/> ", function() {

        jQuery.ajax({
                        type: "POST",
                        url: "deleteResource-ajaxprocessor.jsp",
                        data: { index:index },
                        success: function(data) {
                            //build whole tree to assign new ids.
                            buildResourceTree();
                            hideResourceInfo();
                        }
                    });
    });
}

function cancelSave() {
    jQuery.ajax({
                    type: "POST",
                    url: "cancel-ajaxprocessor.jsp",
                    success: function() {
                        document.location.href = "index.jsp";
                    }
                });
}

function resourceSourceView() {
    var index = document.getElementById('resIndex').value;
    var apiNameValue = document.getElementById('api.name').value;
    var apiContextValue = "/" + document.getElementById('api.context').value;
    var hostname = document.getElementById('api.hostname').value;
    var port = document.getElementById('api.port').value;
    
    if(port != null && port != ""){
    	if(!(/^\d{1,5}([ ]\d{1,5})*$/).test(port)){
    		CARBON.showWarningDialog('<fmt:message key="api.port.invalid"/>');
            return false;
    	}
    }
    
    var result = updateResource("true");
    if (result != false)  {
        document.location.href = "sourceView_resource.jsp?ordinal=1&mode=" + "<%=mode%>" +
                             "&apiName=" + apiNameValue +
                             "&apiContext=" + apiContextValue +
                             "&hostname=" + hostname + 
                             "&port=" + port +
                             "&index=" + index;
        goBack(1);

    }
}

function sourceView() {
    var apiNameValue = document.getElementById('api.name').value;
    var apiContextValue = "/" + document.getElementById('api.context').value;
    var hostname = document.getElementById('api.hostname').value;
    var port = document.getElementById('api.port').value;
    
    if(port != null && port != ""){
    	if(!(/^\d{1,5}([ ]\d{1,5})*$/).test(port)){
    		CARBON.showWarningDialog('<fmt:message key="api.port.invalid"/>');
            return false;
    	}
    }

    document.location.href = "sourceview_api.jsp?ordinal=1&mode=" + "<%=mode%>" +
                             "&apiName=" + apiNameValue +
                             "&apiContext=" + apiContextValue + 
                             "&hostname=" + hostname + 
                             "&port=" + port;

    goBack(1);
}
</script>

<div id="middle">
    <h2>
        <%
            if ("edit".equals(mode)) {
        %><fmt:message key="edit.api"/><%
    } else {
    %><fmt:message key="add.api"/><%
        }
    %>
    </h2>

    <div id="workArea">

        <form id="apiForm" name="apiForm" action="" method="POST">
            <input type="hidden" id="apiConf" name="apiConf" value=""/>

            <table class="styledLeft" cellspacing="0">
                <thead>
                <tr>
                    <th>
                	<span style="float: left; position: relative; margin-top: 2px;"><fmt:message
                            key="design.view.of.api"/></span>
                        <a style="background-image:url(images/source-view.gif);" class="icon-link"
                           onclick="sourceView()"><fmt:message
                                key="switch.to.source"/>
                        </a>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <table class="normal" width="100%">
                            <!-- API Name -->
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="api.name"/> <span
                                        class="required">*</span>
                                </td>
                                <td>
                                    <input type="text" id="api.name" value="<%=apiName%>"
                                            <%if (!"add".equals(mode)) { %>
                                           disabled="disabled" <%}%>/>
                                    <input type="hidden" name="apiName"
                                           value="<%=apiName%>"/>
                                </td>
                            </tr>
                            <!-- API Context Path -->
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="api.context"/> <span
                                        class="required">*</span>
                                </td>
                                <td>
                                    <div>
                                        /<input type="text" id="api.context"
                                                value="<%=apiContext%>"/>
                                    </div>
                                    <input type="hidden" name="apicontext"
                                           value="<%=apiContext%>"/>
                                </td>
                            </tr>
                            <!-- API Hostname -->
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="api.hostname"/>
                                </td>
                                <td>
                                    <input type="text" id="api.hostname" value="<%=hostname%>"/>
                                    <input type="hidden" name="api.hostname" value="<%=hostname%>"/>
                                </td>
                            </tr>
                            <!-- API Port -->
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="api.port"/>
                                </td>
                                <td>
                                    <input type="text" id="api.port" value="<%=port%>"/>
                                    <input type="hidden" name="api.port" value="<%=port%>"/>
                                </td>
                            </tr>
                            <!-- Resources -->
                            <tr>
                                <td colspan="3">
                                    <div class="treePane" id="treePane"
                                         style="height: 300px; overflow: auto; width: auto; border: 1px solid rgb(204, 204, 204);position:relative;">
                                        <div style="position:absolute;padding:20px;">
                                            <ul class="root-list" id="failoverTree">
                                                <li id="parent">
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </table>
                        <div id="resourceInfo" style="display:none">
                            <table class="normal" width="100%">
                                    <%--<tr id="resource-designview-header">
                                        <td class="middle-header" ></td>
                                    </tr>
                                    <tr id="resource-sourceview-header">
                                        <td class="middle-header" ></td>
                                    </tr>
                                    <tr id="resource-edit-tab">
                                        <td style="padding: 0px !important;"></td>
                                    </tr>--%>
                                <tr>
                                    <td>
                                        <table class="styledLeft" cellspacing="0">
                                            <tr>
                                                <td class="middle-header">
                                        <span style="float:left;position:relative; margin-top:2px;">
                                            <fmt:message key="design.view.of.the.resource"/></span>
                                                    <!--a style="background-image:url(images/source-view.gif);"
                                                       class="icon-link"
                                                       onclick="resourceSourceView()"
                                                      ><fmt:message
                                                            key="switch.to.source"/>
                                                    </a-->
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 0px !important;">
                                                    <div id="info" class="tabPaneContentMain"
                                                         style="width:auto;padding:0px;">
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <input type="hidden" id="resIndex" name="resIndex"/>
                        <input type="hidden" id="apiFileName" name="apiFileName"/>
                        <div id="resourceSizeVar" name="resourceSizeVar"/>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input type="button" value="<fmt:message key="save"/>"
                               class="button"
                               name="save"
                               onclick="validateAndSaveApi()"/>
                        <input type="button" value="<fmt:message key="cancel"/>"
                               name="cancel"
                               class="button"
                               onclick="cancelSave()"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>
    </div>
</div>


</fmt:bundle>
