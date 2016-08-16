<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
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
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.sequences.ui.SequenceEditorConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.sequences.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.sequences.ui.i18n.JSResources"
        request="<%=request%>"/>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<script type="text/javascript">
    function confirmForceDelete(sequenceName, msg) {
        CARBON.showConfirmationDialog('<fmt:message key="sequence.dependency.mgt.warning"/><br/><br/>'
                + msg + '<br/><fmt:message key="force.delete"/>', function() {
            jQuery.ajax({
                type: "POST",
                url: "delete_sequence-ajaxprocessor.jsp",
                data: {"sequenceName": sequenceName, "force": "true"},
                async: false,
                success: function (result, status, xhr) {
                    if (status == "success") {
                        location.assign("list_sequences.jsp");
                    }
                }
            });
        });
    }
</script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<%
    //remove session variables if user exited form design sequence of proxy admin
    session.removeAttribute("sequence");

    //remove any sessions related to templates since template mode settings should not interfere
    //with sequence editor mode settings

    //remove attribute to restate sequence-editor mode
    session.removeAttribute("editorClientFactory");
    session.removeAttribute("sequenceAnonOriginator");
    //remove any endpoint template related session attribs to avoid any confilcts
    session.removeAttribute("endpointTemplate");
    session.removeAttribute("templateEdittingMode");
    session.removeAttribute("templateRegKey");

    SequenceAdminClient sequenceAdminClient
            = new SequenceAdminClient(this.getServletConfig(), session);
    SequenceInfo[] sequences = null;
    SequenceInfo[] dynamicSequences = null;
    String pageNumberStr = request.getParameter("pageNumber");
    String dynamicPageNumberStr = request.getParameter("dynamicPageNumber");
    String sequenceSearchString = request.getParameter("sequenceSearchString");
    boolean isSuccessfulSearch = true;
    if (sequenceSearchString == null) {
        sequenceSearchString = "";
    }
    int pageNumber = 0;
    int dynamicPageNumber = 0;
    if (pageNumberStr != null) {
        pageNumber = Integer.parseInt(pageNumberStr);
    }
    if (dynamicPageNumberStr != null) {
        dynamicPageNumber = Integer.parseInt(dynamicPageNumberStr);
    }
    int numberOfPages = 0;
    int numberOfDynamicPages = 0;
    try {
        int seqCount = 0;
        int dynamicSequenceCount = 0;
        if (sequenceSearchString.equals("")) {

            sequences = sequenceAdminClient.getSequences(pageNumber, SequenceEditorConstants.SEQUENCE_PER_PAGE);
            dynamicSequences = sequenceAdminClient.getDynamicSequences(dynamicPageNumber,
                    SequenceEditorConstants.SEQUENCE_PER_PAGE);
            seqCount = sequenceAdminClient.getSequencesCount();
            dynamicSequenceCount = sequenceAdminClient.getDynamicSequenceCount();
        }


        else{

            sequences = sequenceAdminClient.getSequencesSearch(pageNumber, SequenceEditorConstants.SEQUENCE_PER_PAGE, sequenceSearchString);
            if (sequences == null) {
                seqCount = sequenceAdminClient.getSequencesCount();
            } else {
                seqCount = sequences.length;
            }
            dynamicSequences = sequenceAdminClient.getDynamicSequencesSearch(dynamicPageNumber, SequenceEditorConstants.SEQUENCE_PER_PAGE, sequenceSearchString);
            if (dynamicSequences == null) {
                dynamicSequenceCount = sequenceAdminClient.getDynamicSequenceCount();

            } else {
                dynamicSequenceCount = dynamicSequences.length;
            }

            if (sequences == null && dynamicSequences == null) {
                isSuccessfulSearch = false;
            }
        }

        if (!isSuccessfulSearch) {
            sequences = sequenceAdminClient.getSequences(pageNumber, SequenceEditorConstants.SEQUENCE_PER_PAGE);
            dynamicSequences = sequenceAdminClient.getDynamicSequences(dynamicPageNumber,
                    SequenceEditorConstants.SEQUENCE_PER_PAGE);
            seqCount = sequenceAdminClient.getSequencesCount();
            dynamicSequenceCount = sequenceAdminClient.getDynamicSequenceCount();
        }


        if (seqCount % SequenceEditorConstants.SEQUENCE_PER_PAGE == 0) {
            numberOfPages = seqCount / SequenceEditorConstants.SEQUENCE_PER_PAGE;
        } else {
            numberOfPages = seqCount / SequenceEditorConstants.SEQUENCE_PER_PAGE + 1;
        }

        if (dynamicSequenceCount % SequenceEditorConstants.SEQUENCE_PER_PAGE == 0) {
            numberOfDynamicPages = dynamicSequenceCount / SequenceEditorConstants.SEQUENCE_PER_PAGE;
        } else {
            numberOfDynamicPages = dynamicSequenceCount / SequenceEditorConstants.SEQUENCE_PER_PAGE + 1;
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

    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.sequences.ui.i18n.Resources",
            request.getLocale());
    if ("fail".equals(session.getAttribute("dynamic_edit"))) {
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

%>


<script type="text/javascript" src="../carbon/global-params.js"></script>

<script type="text/javascript">

    wso2.wsf.Util.initURLs();
    var ENABLE = "enable";
    var DISABLE = "disable";
    var STAT = "statistics";
    var TRACE = "Tracing";
    var allSequencesSelected = false;
    var frondendURL = wso2.wsf.Util.getServerURL() + "/";

    function searchSequence() {
        document.searchForm.submit();
    }

    function addSequence() {
        document.location.href = "design_sequence.jsp?sequenceAction=add";
    }

    function disableStat(sequenceName) {
        $.ajax({
            type: 'POST',
            url: 'stat_tracing-ajaxprocessor.jsp',
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
            url: 'stat_tracing-ajaxprocessor.jsp',
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
            url: 'stat_tracing-ajaxprocessor.jsp',
            data: 'sequenceName=' + sequenceName + '&action=enableTracing',
            success: function(msg) {
                handleCallback(sequenceName, ENABLE, TRACE);
                handleCallback(sequenceName, ENABLE, STAT);
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
            url: 'stat_tracing-ajaxprocessor.jsp',
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
        document.location.href = "design_sequence.jsp?sequenceAction=edit&sequenceName=" + arguments[0];
    }

    function editCAppSequence(sequenceName) {
        CARBON.showConfirmationDialog('<fmt:message key="edit.artifactContainer.sequences.on.page.prompt"/>', function() {
            $.ajax({
                type: 'POST',
                success: function() {
                    location.href = "design_sequence.jsp?sequenceAction=edit&sequenceName=" + sequenceName;
                }
            });
        });
    }

    function deleteSequence(sequenceName) {
        if (sequenceName == "main" || sequenceName == "fault") {
            CARBON.showWarningDialog('<fmt:message key="sequence.main.fault.cannot.delete"/>');
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="sequence.delete.confirmation"/> " + escape(sequenceName) + "?", function() {
                jQuery.ajax({
                    type: "POST",
                    url: "delete_sequence-ajaxprocessor.jsp",
                    data: {"sequenceName": sequenceName},
                    async: false,
                    success: function (result, status, xhr) {
                        if (status == "success") {
                            location.assign("list_sequences.jsp");
                        }
                    }
                });
            });
        }
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
            location.href = "registry_sequence.jsp?action=edit&key=" + key;
        } else {
            CARBON.showErrorDialog("Specify the key of the Sequence to be edited");
        }
    }

    function deleteRegistrySequence(sequenceName) {
        if (sequenceName == "main" || sequenceName == "fault") {
            CARBON.showWarningDialog('<fmt:message key="sequence.main.fault.cannot.delete"/>');
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="sequence.delete.confirmation"/> " + escape(sequenceName) + "?", function() {
                jQuery.ajax({
                    type: "POST",
                    url: "delete_sequence-ajaxprocessor.jsp",
                    data: {"type": "registry", "sequenceName": sequenceName},
                    async: false,
                    success: function (result, status, xhr) {
                        if (status == "success") {
                            location.assign("list_sequences.jsp");
                        }
                    }
                });
            });
        }
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

    function loadSequenceAfterBulkDeletion() {
        window.location.href = "list_sequences.jsp";
    }

    function deleteServices() {
        var selected = false;
        if (document.sequenceForm.sequenceGroups[0] != null) { // there is more than 1 sequences
            for (var j = 0; j < document.sequenceForm.sequenceGroups.length; j++) {
                selected = document.sequenceForm.sequenceGroups[j].checked;
                if (selected) break;
            }
        }
        else if (document.sequenceForm.sequenceGroups != null) { // only 1 sequence
            selected = document.sequenceForm.sequenceGroups.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.sequences.to.be.deleted"/>');
            return;
        }
        if (allSequencesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.sequences.on.all.prompt"/>", function () {
                $.ajax({
                    type: 'POST',
                    url: 'delete_sequence_groups-ajaxprocessor.jsp',
                    data: 'deleteAllSequenceGroups=true',
                    success: function (msg) {
                        loadSequenceAfterBulkDeletion();
                    }
                });
            });
        } else {

            var sequenceGroupsString = '';
            jQuery('.chkBox').each(function (index) {
                if (this.checked) {
                    sequenceGroupsString += this.value + ':';
                }
            });


            CARBON.showConfirmationDialog("<fmt:message key="delete.sequences.on.all.prompt"/>", function () {
                $.ajax({
                    type: 'POST',
                    url: 'delete_sequence_groups-ajaxprocessor.jsp',
                    data: 'sequenceGroupsString=' + sequenceGroupsString,
                    success: function (msg) {
                        loadSequenceAfterBulkDeletion();
                    }
                });
            });
        }
    }

    function selectAllInThisPage(isSelected) {
        allSequencesSelected = false;
        if (document.sequenceForm.sequenceGroups != null &&
                document.sequenceForm.sequenceGroups[0] != null) { // there is more than 1 sequence
            if (isSelected) {
                for (var j = 0; j < document.sequenceForm.sequenceGroups.length; j++) {
                    document.sequenceForm.sequenceGroups[j].checked = true;
                }
            } else {
                for (j = 0; j < document.sequenceForm.sequenceGroups.length; j++) {
                    document.sequenceForm.sequenceGroups[j].checked = false;
                }
            }
        } else if (document.sequenceForm.sequenceGroups != null) { // only 1 sequence
            document.sequenceForm.sequenceGroups.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allSequencesSelected = true;
        return false;
    }

    function resetVars() {
        allSequencesSelected = false;
        var isSelected = false;
        if (document.sequenceForm.sequenceGroups[0] != null) { // there is more than 1 sequence
            for (var j = 0; j < document.sequenceForm.sequenceGroups.length; j++) {
                if (document.sequenceForm.sequenceGroups[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.sequenceForm.sequenceGroups != null) { // only 1 sequence
            if (document.sequenceForm.sequenceGroups.checked) {
                isSelected = true;
            }
        }
        return false;
    }
</script>

<style type="text/css">
    .inlineDiv div {
        float: left;
    }
</style>

<carbon:breadcrumb
        label="sequence.menu.text"
        resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<div id="middle">

<h2>
    <fmt:message key="mediation.sequences.header"/>
</h2>

<div id="workArea" style="background-color:#F4F4F4;">
<div style="height:25px;">
    <a class="icon-link" style="background-image: url(../admin/images/add.gif);"
       href="javascript:addSequence()"><fmt:message key="sequence.button.add.text"/></a>
</div>
<div id="tabs">
<ul>
    <li><a href="#tabs-1"><fmt:message key="defined.sequences"/></a></li>
    <li><a href="#tabs-2"><fmt:message key="dynamic.sequencs"/></a></li>
</ul>
<div id="tabs-1">
    <p><fmt:message key="sequences.defined.text"/>
    <%
        if ( sequenceAdminClient!=null) {
    %>
    <%=" :- " + sequenceAdminClient.getSequencesCount()%>
    <%
      }
    %>
    </p>
    <br/>
    <form action="list_sequences.jsp" name="searchForm">
        <table style="border:0; !important">
            <tbody>
            <tr style="border:0; !important">
                <td style="border:0; !important">
                    <nobr>
                        <fmt:message key="search.sequence"/>
                        <label>
                            <input type="text" name="sequenceSearchString"
                                   value="<%= sequenceSearchString != null? Encode.forHtmlAttribute(sequenceSearchString) : ""%>"/>
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
                    <fmt:message key="search.unable.sequence"/>
                </td>
                <%
                    }
                %>
            </tr>
            </tbody>
        </table>
    </form>
    <br/>
    <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                      page="list_sequences.jsp" pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=\"\"%>"/>
    <br>
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
    <form name="sequenceForm" action="delete_sequence_groups-ajaxprocessor.jsp" method="post">
        <input type="hidden" name="pageNumberSequence" value="<%= pageNumber%>"/>
        <table class="styledLeft" cellspacing="1" id="sequencesTable">
            <thead>
            <tr>
                <th>
                    <fmt:message key="sequence.select"/>
                </th>
                <th>
                    <fmt:message key="sequence.name"/>
                </th>
                <th colspan="4">
                    <fmt:message key="sequence.actions"/>
                </th>
            </tr>
            </thead>
            <tbody>
            <%
            if(sequences != null) {
            for (SequenceInfo sequence : sequences) { %>
            <tr>
                <td width="10px" style="text-align:center; !important">
                    <input type="checkbox" name="sequenceGroups"
                           value="<%=Encode.forHtmlAttribute(sequence.getName())%>"
                           onclick="resetVars()" class="chkBox"/>
                    &nbsp;
                </td>
                <td>
                    <span href="#"
                        <% if(sequence.getDescription()!= null){ %>
                            onmouseover="showTooltip(this,'<%=sequence.getDescription()%>')"
                        <% } %>>
                        <% if (sequence.getArtifactContainerName() != null) { %>
                            <img src="images/applications.gif">
                            <%=Encode.forHtmlContent(sequence.getName())%>
                            <% if(sequence.getIsEdited()) { %>
                                <span style="color:grey"> ( Edited )</span>
                            <% } %>
                        <% } else { %>
                            <%=Encode.forHtmlContent(sequence.getName())%>
                        <% } %>
                    </span>
                </td>

                <% if (sequence.isEnableStatistics()) { %>
                <td style="border-right:none;border-left:none;width:200px">
                    <div class="inlineDiv">
                        <div id="disableStat<%= Encode.forHtmlAttribute(sequence.getName())%>">
                            <a href="#" onclick="disableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="sequence.stat.disable.link"/></a>
                        </div>
                        <div id="enableStat<%= Encode.forHtmlAttribute(sequence.getName())%>" style="display:none;">
                            <a href="#" onclick="enableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="sequence.stat.enable.link"/></a>
                        </div>
                    </div>
                </td>
                <% } else { %>
                <td style="border-right:none;border-left:none;width:200px">
                    <div class="inlineDiv">
                        <div id="enableStat<%= Encode.forHtmlAttribute(sequence.getName())%>">
                            <a href="#" onclick="enableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="sequence.stat.enable.link"/></a>
                        </div>
                        <div id="disableStat<%= Encode.forHtmlAttribute(sequence.getName())%>" style="display:none">
                            <a href="#" onclick="disableStat('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="sequence.stat.disable.link"/></a>
                        </div>
                    </div>
                </td>
                <% } %>
                <% if (sequence.isEnableTracing()) { %>
                <td style="border-right:none;border-left:none;width:200px">
                    <div class="inlineDiv">
                        <div id="disableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>">
                            <a href="#"
                               onclick="disableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="sequence.trace.disable.link"/></a>
                        </div>
                        <div id="enableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>"
                             style="display:none;">
                            <a href="#" onclick="enableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="sequence.trace.enable.link"/></a>
                        </div>
                    </div>
                </td>
                <% } else { %>
                <td style="border-right:none;border-left:none;width:200px">
                    <div class="inlineDiv">
                        <div id="enableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>">
                            <a href="#" onclick="enableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="sequence.trace.enable.link"/></a>
                        </div>
                        <div id="disableTracing<%= Encode.forHtmlAttribute(sequence.getName())%>"
                             style="display:none">
                            <a href="#"
                               onclick="disableTracing('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="sequence.trace.disable.link"/></a>
                        </div>
                    </div>
                </td>
                <% } %>

                <% if (sequence.getArtifactContainerName() != null) { %>
                    <td style="border-left:none;border-right:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="editCAppSequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                                class="icon-link"
                                style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                    key="sequence.edit.action"/></a>
                        </div>
                    </td>

                    <td style="border-left:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="#"
                                class="icon-link"
                                style="color:grey;background-image:url(../admin/images/delete.gif);"><fmt:message
                                    key="sequence.delete.action"/></a>
                        </div>
                    </td>
                <% } else { %>
                    <td style="border-left:none;border-right:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="editSequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                                class="icon-link"
                                style="background-image:url(../admin/images/edit.gif);"><fmt:message
                                    key="sequence.edit.action"/></a>
                        </div>
                    </td>
                    <td style="border-left:none;width:100px">
                        <div class="inlineDiv">
                            <a href="#" onclick="deleteSequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                                class="icon-link"
                                style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                    key="sequence.delete.action"/></a>
                        </div>
                    </td>
                <% } %>
            </tr>
            <% } }%>
            </tbody>
        </table>
    </form>

    <script type="text/javascript">
        alternateTableRows('sequencesTable', 'tableEvenRow', 'tableOddRow');
    </script>
    <p>&nbsp;</p>
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
    <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                      page="list_sequences.jsp" pageNumberParameterName="pageNumber"
                      resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=\"\"%>"/>
</div>
<div id="tabs-2">
    <% if ((dynamicSequences != null) && (dynamicSequences.length > 0)) { %>
    <p><fmt:message key="sequences.dynamic.text"/></p>
    <br/>
    <carbon:paginator pageNumber="<%=dynamicPageNumber%>"
                      numberOfPages="<%=numberOfDynamicPages%>"
                      page="list_sequences.jsp"
                      pageNumberParameterName="dynamicPageNumber"
                      resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=\"\"%>"/>
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
                    <a href="#"
                       onclick="editRegistrySequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/edit.gif);"><fmt:message
                            key="sequence.edit.action"/></a>
                </div>
            </td>
            <td class="registryWriteOperation" style="border-left:none;width:100px">
                <div class="inlineDiv">
                    <a href="#"
                       onclick="deleteRegistrySequence('<%= Encode.forJavaScriptAttribute(sequence.getName()) %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/delete.gif);"><fmt:message
                            key="sequence.delete.action"/></a>
                </div>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <br>
    <carbon:paginator pageNumber="<%=dynamicPageNumber%>"
                      numberOfPages="<%=numberOfDynamicPages%>"
                      page="list_sequences.jsp"
                      pageNumberParameterName="dynamicPageNumber"
                      resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
                      prevKey="prev" nextKey="next"
                      parameters="<%=\"\"%>"/>
    <script type="text/javascript">
        alternateTableRows('dynamicSequencesTable', 'tableEvenRow', 'tableOddRow');
    </script>

    <% } else { %>
    <p><fmt:message key="no.sequences.dynamic.text"/></p>
    <% } %>
</div>
</div>
</div>
</div>
</fmt:bundle>
