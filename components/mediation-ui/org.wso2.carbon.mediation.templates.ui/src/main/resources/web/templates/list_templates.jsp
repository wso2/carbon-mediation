<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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
<%@ page import="org.wso2.carbon.sequences.common.to.SequenceInfo" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.TemplateAdminClientAdapter" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.TemplateEditorConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.factory.TemplateEditorClientFactory" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>
<%@ page import="org.wso2.carbon.mediation.templates.common.EndpointTemplateInfo" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.mediation.templates.ui.i18n.Resources">
    <carbon:jsi18n
                resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.JSResources"
                request="<%=request%>" />

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet" />
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<script type="text/javascript">
    function confirmForceDelete(sequenceName, msg) {
        CARBON.showConfirmationDialog('<fmt:message key="sequence.dependency.mgt.warning"/><br/><br/>'
                + msg + '<br/><fmt:message key="force.delete"/>', function() {
            jQuery.ajax({
                type: "POST",
                url: "../templates/delete_template-ajaxprocessor.jsp",
                data: {"sequenceName": sequenceName, "force": "true"},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("list_templates.jsp");
                    }
                }
            });
        });
    }
</script>

<%
    //remove session variables if user exited form design sequence of proxy admin
    session.removeAttribute("sequence");
    //createClient factory for editor
    session.setAttribute("editorClientFactory",new TemplateEditorClientFactory());


    TemplateAdminClientAdapter templateAdminClient
            = new TemplateAdminClientAdapter(this.getServletConfig(), session);
    SequenceInfo[] sequences = null;
    SequenceInfo[] dynamicSequences = null;
    String pageNumberStr = request.getParameter("pageNumber");
    String dynamicPageNumberStr = request.getParameter("dynamicPageNumber");
    int pageNumber = 0;
    int dynamicPageNumber = 0;
    if (pageNumberStr != null) {
        pageNumber = Integer.parseInt(pageNumberStr);
    }
    if(dynamicPageNumberStr!=null){
        dynamicPageNumber = Integer.parseInt(dynamicPageNumberStr);
    }
    int numberOfPages = 0;
    int numberOfDynamicPages = 0;
    try {
        sequences = templateAdminClient.getTemplates(pageNumber, TemplateEditorConstants.SEQUENCE_PER_PAGE);
        dynamicSequences = templateAdminClient.getDynamicSequences(dynamicPageNumber,
                TemplateEditorConstants.SEQUENCE_PER_PAGE);
        if(sequences == null){
            sequences = new SequenceInfo[0];
        }
        if(dynamicSequences == null){
            dynamicSequences = new SequenceInfo[0];
        }
        int seqCount = templateAdminClient.getSequencesCount();
        int dynamicSequenceCount = templateAdminClient.getDynamicSequenceCount();

        if (seqCount % TemplateEditorConstants.SEQUENCE_PER_PAGE == 0) {
            numberOfPages = seqCount / TemplateEditorConstants.SEQUENCE_PER_PAGE;
        } else {
            numberOfPages = seqCount / TemplateEditorConstants.SEQUENCE_PER_PAGE + 1;
        }

        if (dynamicSequenceCount % TemplateEditorConstants.SEQUENCE_PER_PAGE == 0) {
            numberOfDynamicPages = dynamicSequenceCount / TemplateEditorConstants.SEQUENCE_PER_PAGE;
        } else {
            numberOfDynamicPages = dynamicSequenceCount / TemplateEditorConstants.SEQUENCE_PER_PAGE + 1;
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

    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.mediation.templates.ui.i18n.Resources",
            request.getLocale());
    if("fail".equals(session.getAttribute("dynamic_edit"))){
        %>
            <script type="text/javascript">
                CARBON.showErrorDialog("<%= bundle.getString(
                    "unable.to.build.sequence.object.from.the.given.sequence.information") %>");
            </script>
        <%
                session.removeAttribute("dynamic_edit");
    }
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    session.removeAttribute("mediator.position");

    String dependencyMgtError = (String) session.getAttribute("seq.d.mgt.error.msg");
    if (dependencyMgtError != null) {
        String seqToDelete = (String) session.getAttribute("seq.d.mgt.error.name");
%>
    <script type="text/javascript">
        confirmForceDelete('<%=seqToDelete%>', '<%=dependencyMgtError%>');
    </script>
<%
        session.removeAttribute("seq.d.mgt.error.msg");
        session.removeAttribute("seq.d.mgt.error.name");
    }

    try {
        if (((String) session.getAttribute("hasDuplicateTemplateEndpoint")).equals("true")) {
%>
    <script type="text/javascript">
        CARBON.showErrorDialog('<fmt:message key="endpoint.already.exists"/>');
    </script>
    <%
                session.removeAttribute("hasDuplicateTemplateEndpoint");
            }
        } catch (Exception e) {
        }
%>


<script type="text/javascript" src="../carbon/global-params.js"></script>

<script type="text/javascript">

    wso2.wsf.Util.initURLs();
    var ENABLE = "enable";
    var DISABLE = "disable";
    var STAT = "statistics";
    var TRACE = "Tracing";

    var frondendURL = wso2.wsf.Util.getServerURL() + "/";

    function addSequence() {
        document.location.href = "../sequences/design_sequence.jsp?sequenceAction=add&seqEditor=template";
    }

    function addEndpoint() {
        jQuery.ajax({
                     type: 'POST',
                     url: '../templates/endpoint_template-ajaxprocessor.jsp',
                     data: 'data=null',
                     success: function(msg) {
                                 document.location.href = "../endpoints/index.jsp?templateAdd=true&endpointOriginator=template";
                     },
                     error: function(msg) {
//                        CARBON.showErrorDialog('<fmt:message key="template.trace.enable.link"/>' +
//                                               ' ' + templateName);
                     }
                 })

    }

    function disableStat(sequenceName) {
        $.ajax({
            type: 'POST',
            url: '../sequences/stat_tracing-ajaxprocessor.jsp',
            data: 'sequenceName=' + sequenceName + '&action=disableStat',
            success: function(msg) {
                handleCallback(sequenceName, DISABLE, STAT);
            },
            error: function(msg) {
                CARBON.showErrorDialog('<fmt:message key="sequence.stat.disable.error"/>' +
                                       ' ' + sequenceName);
            }
        });
    }

    function enableStat(sequenceName) {
        $.ajax({
            type: 'POST',
            url: '../sequences/stat_tracing-ajaxprocessor.jsp',
            data: 'sequenceName=' + sequenceName + '&action=enableStat',
            success: function(msg) {
                handleCallback(sequenceName, ENABLE, STAT);
            },
            error: function(msg) {
                CARBON.showErrorDialog('<fmt:message key="sequence.stat.enable.error"/>' +
                                       ' ' + sequenceName);
            }
        });
    }

    function handleCallback(seq, action, type) {
        var element;
        if (action == "enable") {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + seq);
                element.style.display = "";
                element = document.getElementById("enableStat" + seq);
                element.style.display = "none";
            } else {
                element = document.getElementById("disableTracing" + seq);
                element.style.display = "";
                element = document.getElementById("enableTracing" + seq);
                element.style.display = "none";
            }
        } else {
            if (type == "statistics") {
                element = document.getElementById("disableStat" + seq);
                element.style.display = "none";
                element = document.getElementById("enableStat" + seq);
                element.style.display = "";
            } else {
                element = document.getElementById("disableTracing" + seq);
                element.style.display = "none";
                element = document.getElementById("enableTracing" + seq);
                element.style.display = "";
            }
        }
    }

    function enableTracing(sequenceName) {
        $.ajax({
            type: 'POST',
            url: '../sequences/stat_tracing-ajaxprocessor.jsp',
            data: 'sequenceName=' + sequenceName + '&action=enableTracing',
            success: function(msg) {
                handleCallback(sequenceName, ENABLE, TRACE);
            },
            error: function(msg) {
                CARBON.showErrorDialog('<fmt:message key="sequence.trace.enable.link"/>' +
                                       ' ' + sequenceName);
            }
        });
    }

    function disableTracing(sequenceName) {
        $.ajax({
            type: 'POST',
            url: '../sequences/stat_tracing-ajaxprocessor.jsp',
            data: 'sequenceName=' + sequenceName + '&action=disableTracing',
            success: function(msg) {
                handleCallback(sequenceName, DISABLE, TRACE);
            },
            error: function(msg) {
                CARBON.showErrorDialog('<fmt:message key="sequence.trace.disable.error"/>' +
                                       ' ' + sequenceName);
            }
        });
    }

    function editSequence() {
        document.location.href = "../sequences/design_sequence.jsp?sequenceAction=edit&seqEditor=template&sequenceName=" + arguments[0];
    }

    function editCAppSequence(sequenceName) {
        CARBON.showConfirmationDialog('<fmt:message key="edit.artifactContainer.sequences.template.on.page.prompt"/>', function() {
            $.ajax({
                type: 'POST',
                success: function() {
                    document.location.href = "../sequences/design_sequence.jsp?sequenceAction=edit&seqEditor=template&sequenceName=" + sequenceName;
                }
            });
        });
    }

    function editEndpoint(endPointName, endPointType ,paramCollectionStr) {

       jQuery.ajax({
                    type: 'POST',
                    url: '../templates/endpoint_template-ajaxprocessor.jsp',
                    data: 'templateName=' + endPointName,
                    success: function(msg) {
                        editEndpointSuccess(endPointName, endPointType);
                    },
                    error: function(msg) {
//                        CARBON.showErrorDialog('<fmt:message key="template.trace.enable.link"/>' +
//                                               ' ' + templateName);
                    }
                })
    }

    function editCAppEndpoint(endPointName, endPointType ,paramCollectionStr) {
        CARBON.showConfirmationDialog('<fmt:message key="edit.artifactContainer.sequences.template.on.page.prompt"/>', function() {
            jQuery.ajax({
                type: 'POST',
                url: '../templates/endpoint_template-ajaxprocessor.jsp',
                data: 'templateName=' + endPointName,
                success: function() {
                    editEndpointSuccess(endPointName, endPointType);
                }
            });
        });
    }

    function editEndpointSuccess(endPointName, endPointType){
        if (endPointType == 'address') {
            document.location.href = '../endpoints/addressEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit&endpointOriginator=template';
        } else if (endPointType == 'wsdl') {
            document.location.href = '../endpoints/wsdlEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit&endpointOriginator=template';
        } else if (endPointType == 'failover') {
            document.location.href = '../endpoints/failOverEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit&endpointOriginator=template';
        } else if (endPointType == 'loadbalance') {
            document.location.href = '../endpoints/loadBalanceEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit&endpointOriginator=template';
        } else if (endPointType == 'default') {
            document.location.href = '../endpoints/defaultEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit&endpointOriginator=template';
        } else if(endPointType == 'http') {
            document.location.href = '../endpoints/httpEndpoint.jsp?endpointName=' + endPointName + '&endpointAction=edit&endpointOriginator=template';
        }

    }

    function deleteSequence(sequenceName) {
        if (sequenceName == "main" || sequenceName == "fault") {
           CARBON.showWarningDialog('<fmt:message key="sequence.main.fault.cannot.delete"/>');
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="sequence.delete.confirmation"/> " + escape(sequenceName) + "?", function(){
                jQuery.ajax({
                    type: "POST",
                    url: "../templates/delete_template-ajaxprocessor.jsp",
                    data: {"sequenceName": sequenceName},
                    async: false,
                    success: function (result, status, xhr) {
                        if (status == "success") {
                            location.assign("list_templates.jsp");
                        }
                    }
                });
            });
        }
    }

    function deleteEndpoint(sequenceName) {
        CARBON.showConfirmationDialog("<fmt:message key="endpoint.delete.confirmation"/> " + escape(sequenceName) + "?", function() {;
            jQuery.ajax({
                type: "POST",
                url: "../templates/delete_template-ajaxprocessor.jsp",
                data: {"sequenceName": sequenceName, "templateType": "endpoint"},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("list_templates.jsp");
                    }
                }
            });
        });
    }

    function getResponseValue(responseXML) {
        var returnElementList = responseXML.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
        if (returnElementList.length == 0)
            returnElementList = responseXML.getElementsByTagName("return");
        var returnElement = returnElementList[0];

        return returnElement.firstChild.nodeValue;
    }
    function editRegistrySequence(key) {
        if (key != null && key != undefined && key != "") {
            location.href = "../sequences/registry_sequence.jsp?action=edit&key=" + key;
        } else {
            CARBON.showErrorDialog("Specify the key of the Sequence to be edited");
        }
    }

    function editRegistryEndpoint(endPointName, endPointType ,paramCollectionStr) {
        jQuery.ajax({
                     type: 'POST',
                     url: '../templates/endpoint_template-ajaxprocessor.jsp',
                     data: 'templateName=' + endPointName + "&edittingType=registry",
                     success: function(msg) {
                         var parameters = msg.trim().split(";");
                         var epType = parameters[0].trim();
                         var epName = parameters[1].trim();
                         editEndpointSuccess(epName, epType);
                     },
                     error: function(msg) {
//                        CARBON.showErrorDialog('<fmt:message key="template.trace.enable.link"/>' +
//                                               ' ' + templateName);
                     }
                 })
    }

    function deleteRegistrySequence(sequenceName) {
        if (sequenceName == "main" || sequenceName == "fault") {
           CARBON.showWarningDialog('<fmt:message key="sequence.main.fault.cannot.delete"/>');
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="sequence.delete.confirmation"/> " + escape(sequenceName) + "?", function(){
                jQuery.ajax({
                    type: "POST",
                    url: "../templates/delete_template-ajaxprocessor.jsp",
                    data: {"type": "registry", "sequenceName": sequenceName},
                    async: false,
                    success: function (result, status, xhr) {
                        if (status == "success") {
                            location.assign("list_templates.jsp");
                        }
                    }
                });
            });
        }
    }

    function deleteRegistryEndpoint(sequenceName) {
        CARBON.showConfirmationDialog("<fmt:message key="endpoint.delete.confirmation"/> " + escape(sequenceName) + "?", function() {
            jQuery.ajax({
                type: "POST",
                url: "../templates/delete_template-ajaxprocessor.jsp",
                data: {"type": "registry", "sequenceName": sequenceName, "templateType": "endpoint"},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("list_templates.jsp");
                    }
                }
            });
        });
    }

    function minMaxReg() {
        var minMaxRegBox = $('minMaxRegBox');
        if (minMaxRegBox.style.display == "none") {
            minMaxRegBox.style.display = "";
        } else {
            minMaxRegBox.style.display = "none";
        }
    }
    $(function() {
        $("#tabs").tabs();
    });

//tab handling logic
    var tabIndex = -1;
    <%
    String tab = request.getParameter("tab");
    if(tab!=null && tab.equals("0")){
    %>
    tabIndex = 0;
    <%
    } else if (tab!=null && tab.equals("1")) {
    %>
    tabIndex = 1;
    <%}%>
    $(document).ready(function() {
        var $tabs = $('#tabs > ul').tabs({ cookie: { expires: 30 } });
        $('a', $tabs).click(function() {
            if ($(this).parent().hasClass('ui-tabs-selected')) {
                $tabs.tabs('load', $('a', $tabs).index(this));
            }
        });
        if (tabIndex == 0) {
            $tabs.tabs('option', 'selected', 0);
        } else if (tabIndex == 1) {
            $tabs.tabs('option', 'selected', 1);
        }
    });
</script>

<style type="text/css">
    .inlineDiv div {
        float: left;
    }
</style>

<carbon:breadcrumb
        label="sequence.menu.text"
        resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>" />

<div id="middle">

    <h2>
        <fmt:message key="mediation.templates.header"/>
    </h2>

<div id="workArea" style="background-color:#F4F4F4;">
<div style="height:25px;">
    <a class="icon-link" style="background-image: url(../admin/images/add.gif);"
       href="javascript:addSequence()"><fmt:message key="sequence.button.add.text"/></a>
</div>

<div style="height:25px;">
    <a class="icon-link" style="background-image: url(../admin/images/add.gif);"
       href="javascript:addEndpoint()"><fmt:message key="endpoint.button.add.text"/></a>
</div>
<div id="tabs">
    <ul>
        <li><a href="#tabs-1"><fmt:message key="defined.templates"/></a></li>
        <li><a href="#tabs-2"><fmt:message key="dynamic.templates"/></a></li>
        <li><a href="#tabs-3"><fmt:message key="endpoint.templates"/></a></li>
        <li><a href="#tabs-4"><fmt:message key="dynamic.endpoint.templates"/></a></li>
    </ul>
    <div id="tabs-1">
        <% if ((sequences != null) && (sequences.length > 0)) { %>
        <p><fmt:message key="sequences.defined.text"/></p>
        <br/>
<carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                  page="list_templates.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=""%>" />
<br>
        <table class="styledLeft" cellspacing="1" id="sequencesTable">
            <thead>
                <tr>
                    <th>
                        <fmt:message key="sequence.name"/>
                    </th>
                    <%--<th>
                        <fmt:message key="sequence.description"/>
                    </th>--%>
                    <th colspan="4">
                        <fmt:message key="sequence.actions"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <% for (SequenceInfo sequence : sequences) { %>
                <tr>
                    <td>
                        <% if (sequence.getArtifactContainerName() != null) { %>
                            <img src="images/applications.gif">
                            <%= Encode.forHtmlContent(sequence.getName()) %>
                            <% if(sequence.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                        <% } else {%>
                            <%= Encode.forHtmlContent(sequence.getName()) %>
                        <% } %>
                    </td>
                    <%--<td>
                        <%= sequence.getDescription() != null ? sequence.getDescription() : "" %>
                    </td>--%>
                            <% if (sequence.isEnableStatistics()) { %>
                            <td style="border-right:none;border-left:none;width:200px">
                                <div class="inlineDiv">
                            <div id="disableStat<%= Encode.forHtmlAttribute(sequence.getName())%>">
                                <a href="#" onclick="disableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/static-icon.gif);"><fmt:message key="sequence.stat.disable.link"/></a>
                            </div>
                            <div id="enableStat<%= Encode.forHtmlAttribute(sequence.getName())%>" style="display:none;">
                                <a href="#" onclick="enableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message key="sequence.stat.enable.link"/></a>
                            </div>
                                    </div>
                                </td>
                            <% } else { %>
                                    <td style="border-right:none;border-left:none;width:200px">
                                <div class="inlineDiv">
                            <div id="enableStat<%= Encode.forHtmlAttribute(sequence.getName())%>">
                                <a href="#" onclick="enableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message key="sequence.stat.enable.link"/></a>
                            </div>
                            <div id="disableStat<%= Encode.forHtmlAttribute(sequence.getName())%>" style="display:none">
                                <a href="#" onclick="disableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/static-icon.gif);"><fmt:message key="sequence.stat.disable.link"/></a>
                            </div>
                                    </div>
                                </td>
                            <% } %>
                            <% if (sequence.isEnableTracing()) { %>
                                    <td style="border-right:none;border-left:none;width:200px">
                                <div class="inlineDiv">
                            <div id="disableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>">
                                <a href="#" onclick="disableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message key="sequence.trace.disable.link"/></a>
                            </div>
                            <div id="enableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>" style="display:none;">
                                <a href="#" onclick="enableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message key="sequence.trace.enable.link"/></a>
                            </div>
                                    </div>
                                </td>
                            <% } else { %>
                                    <td style="border-right:none;border-left:none;width:200px">
                                <div class="inlineDiv">
                            <div id="enableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>">
                                <a href="#" onclick="enableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message key="sequence.trace.enable.link"/></a>
                            </div>
                            <div id="disableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>" style="display:none">
                                <a href="#" onclick="disableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message key="sequence.trace.disable.link"/></a>
                            </div>
                                    </div>
                                </td>
                            <% } %>
                            <td style="border-left:none;border-right:none;width:100px">
                                <div class="inlineDiv">
                                    <% if (sequence.getArtifactContainerName() != null) { %>
                                        <a href="#" onclick="editCAppSequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/edit.gif);"><fmt:message key="sequence.edit.action"/></a>
                                    <% } else { %>
                                        <a href="#" onclick="editSequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/edit.gif);"><fmt:message key="sequence.edit.action"/></a>
                                    <% } %>
                                </div>
                            </td>
                            <td style="border-left:none;width:100px">
                                <div class="inlineDiv">
                                    <% if (sequence.getArtifactContainerName() != null) { %>
                                        <a href="#" onclick="#" class="icon-link" style="color:gray;background-image:url(../admin/images/delete.gif);"><fmt:message key="sequence.delete.action"/></a>
                                    <% } else { %>
                                        <a href="#" onclick="deleteSequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="sequence.delete.action"/></a>
                                    <% } %>
                                </div>
                            </td>
                </tr>
                <% } %>
            </tbody>
        </table>

        <script type="text/javascript">
            alternateTableRows('sequencesTable', 'tableEvenRow', 'tableOddRow');
        </script>
<p>&nbsp;</p>
<carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                  page="list_templates.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=\"\"%>"/>
        <% } else { %>
            <p><fmt:message key="no.templates.static.text"/></p>
        <% } %>
        </div>
    <div id="tabs-2">
        <% if ((dynamicSequences != null) && (dynamicSequences.length > 0)) { %>
        <p><fmt:message key="sequences.dynamic.text"/></p>
        <br/>
<carbon:paginator pageNumber="<%=dynamicPageNumber%>" numberOfPages="<%=numberOfDynamicPages%>"
                  page="list_templates.jsp" pageNumberParameterName="dynamicPageNumber"
                  resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=""%>" />
        <br/>
        <table class="styledLeft" cellspacing="1" id="dynamicSequencesTable">
            <thead>
                <tr>
                    <th>
                        <fmt:message key="sequence.name"/>
                    </th>
                    <th class="registryWriteOperation" style="width:200px" colspan="2">
                        <fmt:message key="sequence.actions"/>
                    </th>
                </tr>
            </thead>
            <tbody>
            <% for (SequenceInfo sequence : dynamicSequences) { %>
            <tr>
                <td style="width:200px">
                    <%= Encode.forHtmlContent(sequence.getName()) %>
                </td>
                <td class="registryWriteOperation" style="border-right:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="editRegistrySequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="sequence.edit.action"/></a>
                    </div>
                </td>
                <td class="registryWriteOperation" style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="deleteRegistrySequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')" class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="sequence.delete.action"/></a>
                    </div>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <br>
<carbon:paginator pageNumber="<%=dynamicPageNumber%>" numberOfPages="<%=numberOfDynamicPages%>"
                  page="list_templates.jsp" pageNumberParameterName="dynamicPageNumber"
                  resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=""%>" />
        <script type="text/javascript">
            alternateTableRows('dynamicSequencesTable', 'tableEvenRow', 'tableOddRow');
        </script>

        <% } else { %>
            <p><fmt:message key="no.templates.dynamic.text"/></p>
        <% } %>
        </div>


    <%
        EndpointTemplateAdminClient endpointClient
                = new EndpointTemplateAdminClient(this.getServletConfig(), session);
        EndpointTemplateInfo[] endpointTemplates = null;
        EndpointTemplateInfo[] dynamicEndpointTemplates = null;
        String endpointPageNumberStr = request.getParameter("endpointPageNumber");
        String dynamicEndpointPageNumberStr = request.getParameter("dynamicPageNumberForEndpoint");
        int endpointPageNumber = 0;
        int dynamicEndpointPageNumber = 0;
        if (endpointPageNumberStr != null) {
            endpointPageNumber = Integer.parseInt(endpointPageNumberStr);
        }
        if (dynamicEndpointPageNumberStr != null) {
            dynamicEndpointPageNumber = Integer.parseInt(dynamicEndpointPageNumberStr);
        }
        int numberOfPagesEndpoint = 0;
        int numberOfDynamicPagesEndpoint = 0;

        endpointTemplates = endpointClient.getEndpointTemplates(endpointPageNumber, TemplateEditorConstants.SEQUENCE_PER_PAGE);
        dynamicEndpointTemplates = endpointClient.getDynamicEndpointTemplates(dynamicPageNumber,
                TemplateEditorConstants.SEQUENCE_PER_PAGE);
        if(endpointTemplates == null){
            endpointTemplates = new EndpointTemplateInfo[0];
        }
        if(dynamicEndpointTemplates == null){
            dynamicEndpointTemplates = new EndpointTemplateInfo[0];
        }
        int epTemplateCount = endpointClient.getEndpointTemplatesCount();
        int dynamicTemplateCount = endpointClient.getDynamicEndpointTemplatesCount();

        if (epTemplateCount % TemplateEditorConstants.SEQUENCE_PER_PAGE == 0) {
            numberOfPagesEndpoint = epTemplateCount / TemplateEditorConstants.SEQUENCE_PER_PAGE;
        } else {
            numberOfPagesEndpoint = epTemplateCount / TemplateEditorConstants.SEQUENCE_PER_PAGE + 1;
        }

        if (dynamicTemplateCount % TemplateEditorConstants.SEQUENCE_PER_PAGE == 0) {
            numberOfDynamicPagesEndpoint = dynamicTemplateCount / TemplateEditorConstants.SEQUENCE_PER_PAGE;
        } else {
            numberOfDynamicPagesEndpoint = dynamicTemplateCount / TemplateEditorConstants.SEQUENCE_PER_PAGE + 1;
        }
    %>

    <div id="tabs-3">
        <% if ((endpointTemplates != null) && (endpointTemplates.length > 0)) { %>
        <p><fmt:message key="endpoint.defined.text"/></p>
        <br/>
        <carbon:paginator pageNumber="<%=endpointPageNumber%>" numberOfPages="<%=numberOfPagesEndpoint%>"
                          page="list_templates.jsp" pageNumberParameterName="endpointPageNumber"
                          resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=\"\"%>"/>
        <br>
        <table class="styledLeft" cellspacing="1" id="endpointsTable">
            <thead>
            <tr>
                <th>
                    <fmt:message key="endpoint.name"/>
                </th>
                <%--<th>
                    <fmt:message key="endpoint.description"/>
                </th>--%>
                <th colspan="4">
                    <fmt:message key="endpoint.actions"/>
                </th>
            </tr>
            </thead>
            <tbody>
            <% for (EndpointTemplateInfo endpoint : endpointTemplates) { %>
            <tr>
                <td>
                    <% if (endpoint.getArtifactContainerName() != null) { %>
                        <img src="images/applications.gif">
                        <%= Encode.forHtmlContent(endpoint.getTemplateName()) %>
                        <% if(endpoint.getIsEdited()) { %> <span style="color:grey"> ( Edited )</span><% } %>
                    <% } else {%>
                        <%= Encode.forHtmlContent(endpoint.getTemplateName()) %>
                    <% } %>
                </td>
                <%--<td>
                    <%= endpoint.getDescription() != null ? endpoint.getDescription() : "" %>
                </td>--%>

                <% if (endpoint.getArtifactContainerName() != null) { %>
                    <td style="border-left:none;border-right:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="editCAppEndpoint('<%= Encode.forJavaScriptAttribute(endpoint.getTemplateName()) %>','<%= endpoint.getEndpointType() %>','<%= endpoint.getParamColelctionString() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                    key="endpoint.edit.action"/></a>
                        </div>
                    </td>
                    <td style="border-left:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="#%>','<%= endpoint.getEndpointType() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                    key="endpoint.delete.action"/></a>
                        </div>
                    </td>
                <% } else { %>
                    <td style="border-left:none;border-right:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="editEndpoint('<%= Encode.forJavaScriptAttribute(endpoint.getTemplateName()) %>','<%= endpoint.getEndpointType() %>','<%= endpoint.getParamColelctionString() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                    key="endpoint.edit.action"/></a>
                        </div>
                    </td>
                    <td style="border-left:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="deleteEndpoint('<%= Encode.forJavaScriptAttribute(endpoint.getTemplateName()) %>','<%= endpoint.getEndpointType() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                    key="endpoint.delete.action"/></a>
                        </div>
                    </td>
                <% } %>
            </tr>
            <% } %>
            </tbody>
        </table>

        <script type="text/javascript">
            alternateTableRows('endpointsTable', 'tableEvenRow', 'tableOddRow');
        </script>
        <p>&nbsp;</p>
        <carbon:paginator pageNumber="<%=endpointPageNumber%>" numberOfPages="<%=numberOfPagesEndpoint%>"
                          page="list_templates.jsp" pageNumberParameterName="endpointPageNumber"
                          resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=\"\"%>"/>
    <% } else { %>
            <p><fmt:message key="no.templates.static.text"/></p>
        <% } %>
    </div>

    <div id="tabs-4">
        <% if ((dynamicEndpointTemplates != null) && (dynamicEndpointTemplates.length > 0)) { %>
        <p><fmt:message key="endpoints.dynamic.text"/></p>
        <br/>
        <carbon:paginator pageNumber="<%=dynamicEndpointPageNumber%>" numberOfPages="<%=numberOfDynamicPagesEndpoint%>"
                          page="list_templates.jsp" pageNumberParameterName="dynamicEndpointPageNumber"
                          resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=\"\"%>"/>
        <br>
        <table class="styledLeft" cellspacing="1" id="dynamicEndpointsTable">
            <thead>
            <tr>
                <th>
                    <fmt:message key="endpoint.name"/>
                </th>
                <th>
                    <fmt:message key="endpoint.description"/>
                </th>
                <th class="registryWriteOperation" colspan="4">
                    <fmt:message key="endpoint.actions"/>
                </th>
            </tr>
            </thead>
            <tbody>
            <% for (EndpointTemplateInfo endpoint : dynamicEndpointTemplates) { %>
            <tr>
                <td>
                    <%= Encode.forHtmlContent(endpoint.getTemplateName()) %>
                </td>
                <td>
                    <%= endpoint.getDescription() != null ? Encode.forHtmlContent(endpoint.getDescription()) : "" %>
                </td>

                <td class="registryWriteOperation" style="border-left:none;border-right:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="editRegistryEndpoint('<%= Encode.forJavaScriptAttribute(endpoint.getTemplateName()) %>','<%= endpoint.getEndpointType() %>','<%= endpoint.getParamColelctionString() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                key="endpoint.edit.action"/></a>
                    </div>
                </td>
                <td class="registryWriteOperation" style="border-left:none;width:100px">
                    <div class="inlineDiv">
                        <a href="#" onclick="deleteRegistryEndpoint('<%= Encode.forJavaScriptAttribute(endpoint.getTemplateName()) %>','<%= endpoint.getEndpointType() %>')"
                           class="icon-link"
                           style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="endpoint.delete.action"/></a>
                    </div>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>

        <script type="text/javascript">
            alternateTableRows('dynamicEndpointsTable', 'tableEvenRow', 'tableOddRow');
        </script>
        <p>&nbsp;</p>
        <carbon:paginator pageNumber="<%=dynamicEndpointPageNumber%>" numberOfPages="<%=numberOfDynamicPagesEndpoint%>"
                          page="list_templates.jsp" pageNumberParameterName="dynamicEndpointPageNumber"
                          resourceBundle="org.wso2.carbon.mediation.templates.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=\"\"%>"/>
    <% } else { %>
            <p><fmt:message key="no.templates.dynamic.text"/></p>
        <% } %>
    </div>

</div>
</div>
</div>
</fmt:bundle>
