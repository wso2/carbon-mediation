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

<%@ page import="org.apache.axiom.om.OMAttribute" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.OMFactory" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="org.wso2.carbon.mediator.service.MediatorStore" %>
<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.sequences.ui.SequenceEditorConstants" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.EditorUIClient" %>
<%@ page import="org.wso2.carbon.mediation.service.templates.TemplateMediator" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.sequences.ui.i18n.Resources", request.getLocale());
    String name = request.getParameter("sequenceName");
    String sequenceXML = request.getParameter("sequenceXML");
    String action = request.getParameter("sequenceAction");
    SequenceMediator sequence;
    if (action != null) {
        if (name != null) {
            org.wso2.carbon.sequences.ui.client.EditorUIClient sequenceClient
                    = SequenceEditorHelper.getClientForEditor(getServletConfig(), session);//new SequenceAdminClient(getServletConfig(), session);
            sequence = sequenceClient.getSequenceMediator(name);
        } else {
            sequence = SequenceEditorHelper.getSequenceForEditor(session);//new SequenceMediator();
        }
        session.setAttribute("editingSequenceAction", action);
    } else if (sequenceXML != null && !"".equals(sequenceXML)) {
        if (SequenceEditorConstants.ACTION_PARAM_VALUE_ANONIFY.equals(
                SequenceEditorHelper.getEditingSequenceAction(session))) {
            OMElement elem = SequenceEditorHelper.parseStringToElement(sequenceXML);
            // changes the inSequence or outSequence or faultSequence to just sequence
            if ("sequence".equals(SequenceEditorHelper.getEditorMode(session))) {
                elem.setLocalName("sequence");
            }
            OMAttribute nameAttr = elem.getAttribute(new QName("name"));
            if (nameAttr != null) {
                nameAttr.setAttributeValue("__anonSequence__");
            } else {
                OMFactory fac = elem.getOMFactory();
                elem.addAttribute("name", "__anonSequence__", fac.createOMNamespace("", ""));
            }
            sequenceXML = elem.toString().trim();
        }
        try {
            sequence = SequenceEditorHelper.parseStringToSequence(sequenceXML);
        } catch (Exception e) {
            session.setAttribute("sequence.error.message", bundle.getString("sequence.source.invalid") + e.getMessage());
%>
<script type="text/javascript">
    document.location.href = "source_sequence.jsp?ordinal=1";
</script>
<%
            return;
        }
    } else {
        sequence = SequenceEditorHelper.getEditingSequence(session);
    }

    if (sequence == null) {
        session.setAttribute("sequence.error.message", bundle.getString("sequence.design.view.error"));
%>
<script type="text/javascript">
    document.location.href = "list_sequences.jsp";
</script>
<%
    } else {
    session.setAttribute("editingSequence", sequence);

    // gets the sequence, i.e. in/out/fault
    String seq = (String) session.getAttribute("sequence");
    HashMap<String, HashMap<String, String>> mediatorMap
            = MediatorStore.getInstance().getMediatorMenuItems();

%>

<link type="text/css" rel="stylesheet" href="css/tree-styles.css"/>
<link type="text/css" rel="stylesheet" href="css/menu-styles.css"/>

<style type="text/css">
    .mediator-links{
        position:absolute;
        margin-left:95px;
    }
    .mediator-link-top, .mediator-link-bottom {
        display:block;
        background-image:url(images/add.gif);
        background-repeat:no-repeat;
        background-position:0 0;
        font-size:9px !important;
        color:#000 !important;
        font-weight:bold;
        line-height:12px !important;  height:auto !important;
    }
    .mediator-link-top img, .mediator-link-bottom img {
        border:0px;
        vertical-align:middle;
    }
</style>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="js/tabs.js"></script>
<script type="text/javascript" src="js/mediator-menu.js"></script>
<script type="text/javascript" src="js/ns-editor.js"></script>
<script type="text/javascript" src="js/form.js"></script>
<%if (sequence instanceof TemplateMediator){%>
    <script type="text/javascript" src="../templates/js/template_param.js"></script>
<%}%>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<fmt:bundle basename="org.wso2.carbon.sequences.ui.i18n.Resources">
<carbon:jsi18n
                resourceBundle="org.wso2.carbon.sequences.ui.i18n.JSResources"
                request="<%=request%>"
                i18nObjectName="seqEditi18n" />

<script type="text/javascript">

    var currentMedTLN = "";

    // Creating the menu
    var oMenu = new YAHOO.widget.Menu("basicmenu");

    var aMenuItems = [
        <% for (String group : mediatorMap.keySet()) {
            HashMap<String, String> childMenu = mediatorMap.get(group);
        %>
            {
                text: "<%= group %>", submenu: {
                    id: "submenu<%= group %>",
                    itemdata: [
                        <% for (String logicalName : childMenu.keySet()) {%>
                            {text: "<%= childMenu.get(logicalName) %>", id: "<%= logicalName %>"},
                        <% } %>
                    ]
                }
            },
        <%}%>
        ];

    oMenu.addItems(aMenuItems);

    jQuery(document).ready(function() {
        initMediators();
        showHideName();
        <% if (session.getAttribute("mediator.position") != null) { %>
            showMediatorConfig('<%=session.getAttribute("mediator.position")%>')
        <% } %>
    });

    function showMediatorConfig(mediatorPosition) {
        var allDivs = document.getElementById("treePane").getElementsByTagName("*");
        var mediatorNodes = new Array();
        var toolbarNodes = new Array();

        for (var i = 0; i < allDivs.length; i++) {
            if (YAHOO.util.Dom.hasClass(allDivs[i], "mediatorLink")) {
                mediatorNodes.push(allDivs[i]);
            }
            if (YAHOO.util.Dom.hasClass(allDivs[i], "sequenceToolbar")) {
                toolbarNodes.push(allDivs[i]);
            }
        }

        for (i = 0; i < mediatorNodes.length; i++) {
            if (mediatorNodes[i].getAttribute("id") == mediatorPosition) {
                mediatorCallback(null, [mediatorNodes[i],mediatorNodes[i].id,mediatorNodes,toolbarNodes]);
            }
        }
    }

    function showHideName() {
        if ('<%=SequenceEditorHelper.getEditingSequenceAction(session)%>' == 'anonify' && '<%=session.getAttribute("sequenceAnonOriginator")%>' != "registry_sequence.jsp") {
            document.getElementById('sequenceNameSection').style.display ="none";
        }
    }

    function sourceView() {
        var mediatorSource = document.getElementById("mediatorSource");

        <%if (sequence instanceof TemplateMediator){%>
            handleParamAdd('template', getParamCount());
        <%
        }
        %>

        if (mediatorSource && mediatorSource.style.display != "none") {
            if (document.getElementById("mediator-source-form") != 'undefined' && document.getElementById("mediator-source-form") != undefined) {
                var options = {
                    beforeSubmit:  addCustomParam,  // pre-submit callback
                    success:       directToViewSource  // post-submit callback
                };

                var funcName = currentMedTLN + "MediatorValidate";
                if (eval("typeof " + funcName + " == 'function'")) {
                    if (eval(funcName + "()")) {
                        jQuery('#mediator-source-form').ajaxForm(options);
                    } else {
                        return;
                    }
                } else {
                    jQuery('#mediator-source-form').ajaxForm(options);
                }
                jQuery('#mediator-source-form').submit();
            } else {
                directToViewSource();
            }
        }
        var mediatorDesign = document.getElementById("mediatorDesign");
        if (mediatorDesign && mediatorDesign.style.display != "none") {
            if (document.getElementById("mediator-editor-form") != 'undefined' && document.getElementById("mediator-editor-form") != undefined) {
                var options = {
                    beforeSubmit:  addCustomParam,  // pre-submit callback
                    success:       directToViewSource  // post-submit callback
                };

                var funcName = currentMedTLN + "MediatorValidate";
                if (eval("typeof " + funcName + " == 'function'")) {
                    if (eval(funcName + "()")) {
                        jQuery('#mediator-editor-form').ajaxForm(options);
                    } else {
                        return;
                    }
                } else {
                    jQuery('#mediator-editor-form').ajaxForm(options);
                }
                jQuery('#mediator-editor-form').submit();
            } else {
                directToViewSource();
            }
        } else {
            directToViewSource();
        }
    }

    function directToViewSource() {
        document.location.href = "source_sequence.jsp?ordinal=1&sequenceName=" + document.getElementById("sequence.name").value + "&onErrorKey="
                + document.getElementById("sequence.onerror.key").value + "&seqDescription=" + document.getElementById("seqeunceDescription").value;
    }

    function cancelSequence() {
    <%
           String annonOriginator = (String) session.getAttribute("sequenceAnonOriginator");
           if (annonOriginator != null && !annonOriginator.equals("../sequences/design_sequence.jsp")) {%>
                window.location.href='<%=session.getAttribute("sequenceAnonOriginator")%>' + '?cancelled=true';
            <%} else {%>
                window.location.href = "<%=SequenceEditorHelper.getForwardToFrom(session)%>";
            <%}
        %>
    }

    function saveSequence() {
        var options = {
               // dataType: 'text/xml',
                success:       onUpdSuccess  // post-submit callback
            };

        <%if (sequence instanceof TemplateMediator){%>
            handleParamAdd('template', getParamCount());
        <%
        }
        %>
        var mediator_edit_tab = document.getElementById('mediator-edit-tab');
        var mediatorDesign = document.getElementById("mediatorDesign");
        var mediatorSource = document.getElementById("mediatorSource");
        if (mediator_edit_tab && mediator_edit_tab.style.display != "none") {
            if (mediatorDesign && mediatorDesign.style.display != "none") {
                var funcName = currentMedTLN + "MediatorValidate";
                if (eval("typeof " + funcName + " == 'function'")) {
                    if (eval(funcName + "()")) {
                        jQuery('#mediator-editor-form').ajaxForm(options);
                        jQuery('#mediator-editor-form').submit();
                    } else {
                        return false;
                    }
                } else {
                    jQuery('#mediator-editor-form').ajaxForm(options);
                    jQuery('#mediator-editor-form').submit();
                }
            } else if (mediatorSource && mediatorSource.style.display != "none") { 
	            YAHOO.util.Event.onAvailable("mediatorSrc", 
	            	function() {
	            		document.getElementById("mediatorSrc").value = editAreaLoader.getValue("mediatorSrc");
	            	}
	            );              
                
                    jQuery('#mediator-source-form').ajaxForm(options);
                    jQuery('#mediator-source-form').submit();
            }
        } else {
            onUpdSuccess();
        }
        return true;

    }

    function onUpdSuccess() {
        if ('<%=SequenceEditorHelper.getEditingSequenceAction(session)%>' == 'anonify') {
            var seqName = document.getElementById("sequence.name").value;
            var onErrorKey = document.getElementById("sequence.onerror.key").value;
            var seqDescription = document.getElementById("seqeunceDescription").value;
            if (onErrorKey != '') {
                document.location.href = "save_sequence.jsp?sequence=<%=seq%>&onErrorKey="
                        + onErrorKey;
            } else if (seqDescription != ''){
                 document.location.href = "save_sequence.jsp?sequence=<%=seq%>"
                        +"&seqDescription=" + seqDescription;
            } else {
                document.location.href = "save_sequence.jsp?sequence=<%=seq%>";
            }
        } else {
            var seqName = document.getElementById("sequence.name").value;
            var onErrorKey = document.getElementById("sequence.onerror.key").value;
            var seqDescription = document.getElementById("seqeunceDescription").value;
            if (seqName == "") {
                CARBON.showWarningDialog('<fmt:message key="sequence.name.required"/>');
                return;
            }
            document.location.href = "save_sequence.jsp?sequenceName=" + seqName
                    + "&onErrorKey=" + onErrorKey + "&seqDescription=" + seqDescription;
        }
    }



    function saveSequenceAs() {

        if ('<%=SequenceEditorHelper.getEditingSequenceAction(session)%>' == 'anonify') {
            CARBON.showErrorDialog('Unable to save the sequence to the synapse registry in current mode');
            return false;
        }

         <%if (sequence instanceof TemplateMediator){%>
            handleParamAdd('template', getParamCount());
        <%
        }
        %>
        var key = document.getElementById('synRegKey').value;
        if (key == '') {
            CARBON.showWarningDialog('The key value must not be empty when saving to the Synapse registry');
            return false;
        }

        var registry;
        if (document.getElementById("config_reg").checked == true) {
            registry = 'conf';
        } else {
            registry = 'gov';
        }
        var onErrorKey = document.getElementById("sequence.onerror.key").value;
        var seqDescription = document.getElementById("seqeunceDescription").value;
        document.location.href = "save_sequence_as.jsp?registry=" + registry + "&regKey=" + key + "&onErrorKey="
                + onErrorKey + "&seqDescription=" + seqDescription;
    }

    function applySequence() {

        var options = {
            success: onUpdateSucess // post-submit callback
        };

        var mediator_edit_tab = document.getElementById('mediator-edit-tab');
        var mediatorDesign = document.getElementById("mediatorDesign");
        var mediatorSource = document.getElementById("mediatorSource");
        if (mediator_edit_tab && mediator_edit_tab.style.display != "none") {
            if (mediatorDesign && mediatorDesign.style.display != "none") {
                var funcName = currentMedTLN + "MediatorValidate";
                if (eval("typeof " + funcName + " == 'function'")) {
                    if (eval(funcName + "()")) {

                        jQuery('#mediator-editor-form').ajaxForm(options);
                        jQuery('#mediator-editor-form').submit();

                    } else {
                        return false;
                    }
                } else {

                    jQuery('#mediator-editor-form').ajaxForm(options);
                    jQuery('#mediator-editor-form').submit();
                }
            } else if (mediatorSource && mediatorSource.style.display != "none") {

                jQuery('#mediator-editor-form').ajaxForm(options);
                jQuery('#mediator-source-form').submit();
            }
        }
        else {
            onUpdateSucess();
        }
    }

    function onUpdateSucess() {
        if ('<%=SequenceEditorHelper.getEditingSequenceAction(session)%>' == 'anonify') {
            document.location.href = "save_sequence.jsp?sequence=<%=seq%>&forwardTo=design_sequence.jsp";
        } else {
            var seqName = document.getElementById("sequence.name").value;
            var onErrorKey = document.getElementById("sequence.onerror.key").value;
            var seqDescription = document.getElementById("seqeunceDescription").value;
            if (seqName == "") {
                CARBON.showWarningDialog('<fmt:message key="sequence.name.required"/>');
                return;
            }
            document.location.href = "save_sequence.jsp?sequenceName=" + seqName
                    + "&onErrorKey=" + onErrorKey + "&forwardTo=design_sequence.jsp" + "&seqDescription=" + seqDescription;
        }
    }

    function addMediator(position, mediatorName, type) {
        document.location.href = "add_mediator.jsp?position=" + position
                + "&mediatorName=" + mediatorName + "&type=" + type;
    }

    var refreshTree = false;
    function updateMediator(mediatorName, refresh) {
        var funcName = mediatorName + "MediatorValidate";
        refreshTree = refresh;
        var options = {
                //dataType: 'text/xml',
                beforeSubmit:  beforeMediatorUpdate,  // pre-submit callback
                success:       afterMediatorUpdate  // post-submit callback
            };

        if (eval("typeof " + funcName + " == 'function'")) {
            if (!eval(funcName + "()")) {
                return false;
            }
        }
        jQuery('#mediator-editor-form').ajaxForm(options);
        jQuery('#mediator-editor-form').submit();
    }

    function afterMediatorUpdate(src) {
        var error  = src.trim();
        if(error != '' && (error.indexOf("error:") != -1)){
            CARBON.showErrorDialog(error.substring(6,error.length));
            if (document.getElementById('whileUpload') != null && document.getElementById('whileUpload') != undefined) {
                document.getElementById('whileUpload').style.display = "none";
            }
            return;
        }
        if (document.getElementById('whileUpload') != null && document.getElementById('whileUpload') != undefined) {
            document.getElementById('whileUpload').style.display = "none";
        }
        document.getElementById('mediator-designview-header').style.display = 'none';
        document.getElementById('mediator-sourceview-header').style.display = 'none';
        document.getElementById('mediator-edit-tab').style.display = 'none';
        hide("mediatorDesign");
        if (src != null && src != undefined) {
//            if (jQuery(src).find("script") != null) {
//                jQuery(src).find('script').each(function() {
//                    alert(jQuery(this).text());
//                    eval(jQuery(this).text());
//                });
//            }
            eval(src);
        }
        if (refreshTree) {
            var url = "design_sequence-ajaxprocessor.jsp?" + "&sequenceName="
                    + document.getElementById("sequence.name").value + "&onErrorKey="
                    + document.getElementById("sequence.onerror.key").value + "&mediatorAction=None";

            jQuery("#treePane").load(url, null, function (responseText, status, XMLHttpRequest) {
                if (status != "success") {
                    //CARBON.showErrorDialog(jsi18n["mediator.design.load.error"]);
                } else {
                    initMediators();
                }
            });
        }
        focusRootMediator();
    }

    function beforeMediatorUpdate(formData, jqForm, options) {
        document.getElementById('whileUpload').style.display = "";
        formData[formData.length] = {name : "followupAction", value : "source"};
    }

    function updateSource() {

        document.getElementById("mediatorSrc").value = editAreaLoader.getValue("mediatorSrc");
        var options = {
            beforeSubmit:  beforeSourceUpdate,  // pre-submit callback
            success:       afterSourceUpdate  // post-submit callback
        };
        jQuery.get("mediator-source-validate-ajaxprocessor.jsp",
        { mediatorSrc: document.getElementById("mediatorSrc").value},
                function(data, status) {
                    if (data.replace(/^\s+|\s+$/g, '') != 'valid') {
                        CARBON.showErrorDialog(jsi18n['invalid.mediator.source.syntax']);
                    } else {
                        jQuery('#mediator-source-form').ajaxForm(options);
                        jQuery('#mediator-source-form').submit();
                    }
                });
    }

    function afterSourceUpdate() {
        document.getElementById('whileUpload').style.display = "none";
        document.getElementById('mediator-designview-header').style.display = 'none';
        document.getElementById('mediator-sourceview-header').style.display = 'none';
        document.getElementById('mediator-edit-tab').style.display = 'none';
    }

    function beforeSourceUpdate(formData, jqForm, options) {
        document.getElementById('whileUpload').style.display = "";
    }

    function showSaveAsForm(show) {
        var formElem = document.getElementById('saveAsForm');
        if (show) {
            var seqName = document.getElementById("sequence.name").value;
            formElem.style.display = "";
            var keyField = document.getElementById('synRegKey');
//            if (keyField.value == '') {
                  keyField.value = document.getElementById("sequence.name").value;
//            }
        } else {
            formElem.style.display = "none";
        }
    }

    function updateEditingMediator() {
        var mediator_edit_tab = document.getElementById('mediator-edit-tab');
        var mediatorDesign = document.getElementById("mediatorDesign");
        var mediatorSource = document.getElementById("mediatorSource");
        if (mediator_edit_tab && mediator_edit_tab.style.display != "none") {
            if (mediatorDesign && mediatorDesign.style.display != "none") {
                var funcName = currentMedTLN + "MediatorValidate";
                if (eval("typeof " + funcName + " == 'function'")) {
                    if (eval(funcName + "()")) {
                        jQuery('#mediator-editor-form').submit();
                    } else {
                        return false;
                    }
                } else {
                    jQuery('#mediator-editor-form').submit();
                }
            } else if (mediatorSource && mediatorSource.style.display != "none") {
                jQuery('#mediator-source-form').submit();
            }
        }
        return true;
    }

</script>

    <carbon:breadcrumb
		label="<%= "edit".equals(SequenceEditorHelper.getEditingSequenceAction(session)) ? SequenceEditorHelper.getUIMetadataForEditor("sequence.edit.text",session) : SequenceEditorHelper.getUIMetadataForEditor("sequence.design.text",session) %>"
		resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />

    <div id="middle">

        <h2>
            <%
                if (request.getParameter("serviceName") != null) {
                    %><%=request.getParameter("serviceName")%>:&nbsp;<%
                }
                if("edit".equals(SequenceEditorHelper.getEditingSequenceAction(session))){
                    if("in".equals(seq)) {
                        %><fmt:message key="sequence.in.edit.header"/><%
                    } else if ("out".equals(seq)) {
                        %><fmt:message key="sequence.out.edit.header"/><%
                    } else if ("fault".equals(seq)) {
                        %><fmt:message key="sequence.fault.edit.header"/><%
                    } else {
                        %><fmt:message key="<%=SequenceEditorHelper.getUIMetadataForEditor("sequence.edit.header",session)%>"/><%
                    }
                }else{
                    if("in".equals(seq)) {
                        %><fmt:message key="sequence.in.design.header"/><%
                    } else if ("out".equals(seq)) {
                        %><fmt:message key="sequence.out.design.header"/><%
                    } else if ("fault".equals(seq)) {
                        %><fmt:message key="sequence.fault.design.header"/><%
                    } else {
                        %><fmt:message key="<%=SequenceEditorHelper.getUIMetadataForEditor("sequence.design.header",session)%>"/><%
                    }
                }
            %>
        </h2>
        <div id="workArea">
            <table class="styledLeft" cellspacing="0">
                <thead>
                    <tr>
                        <th>
                            <span style="float:left; position:relative; margin-top:2px;"><fmt:message key="<%=SequenceEditorHelper.getUIMetadataForEditor("sequence.design.view.text",session)%>"/></span><a href="#" onclick="sourceView()" class="icon-link" style="background-image:url(images/source-view.gif);"><fmt:message key="sequence.switchto.source.text"/></a>
                        </th>
                    </tr>
                </thead>
                <tbody>
		<tr>
		<td>
		<table class="normal" width="100%">
                    <tr id="sequenceNameSection">
                        <td width="5%" style="white-space:nowrap;">
                            <fmt:message key="<%=SequenceEditorHelper.getUIMetadataForEditor("sequence.name",session)%>"/><span class="required">*</span>
                        </td>
                        <td align="left" colspan="2">
                            <input type="text" id="sequence.name" value="<%= sequence.getName() != null ? sequence.getName() : (session.getAttribute("registrySequenceName") != null ? session.getAttribute("sequenceRegistryKey") : "") %>" <%= "edit".equals(SequenceEditorHelper.getEditingSequenceAction(session)) || session.getAttribute("registrySequenceName") != null ? "disabled=\"disabled\"" : "" %> onkeypress="return validateText(event);"/>
                        </td>
                    </tr>
                    <tr id="onErroSection">
                        <td width="5%" style="white-space:nowrap;">
                            <fmt:message key="sequence.onerror"/>
                        </td>
                        <td width="5%">
                            <input type="text" id="sequence.onerror.key" name="sequence.onerror.key" disabled="disabled" value="<%= sequence.getErrorHandler() != null ? sequence.getErrorHandler() : "" %>"/>
                        </td>
                        <td>
                            <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('sequence.onerror.key','/_system/config')"><fmt:message key="sequence.conf.registry.browser"/></a>
                            <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('sequence.onerror.key','/_system/governance')"><fmt:message key="sequence.gov.registry.browser"/></a>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="3">
                            <div class="treePane" id="treePane" style="height: 300px; overflow: auto; width: auto; border: 1px solid rgb(204, 204, 204);position:relative;">

                                    <div style="position:absolute;padding:20px;">

                                        <ul class="root-list" id="sequenceTree">

                                            <li>
                                                <div class="minus-icon" onclick="treeColapse(this)" id="treeColapser"></div>
                                                <div class="mediators" id="mediator-00">
                                                    <a class="root-mediator"><fmt:message key="<%=SequenceEditorHelper.getUIMetadataForEditor("sequence.root.text",session)%>"/></a>
                                                    <div class="sequenceToolbar" style="width:100px;">
                                                        <div>
                                                            <a class="addChildStyle"><fmt:message key="sequence.add.child.action"/></a>
                                                        </div>
                                                    </div>
                                                </div>
                                                <!--<div id="sequenceEditor">-->
                                                <%
                                                    int count = sequence.getList().size();
                                                    if (count != 0) {
                                                %>

                                                <div class="branch-node"></div>
                                                <ul class="child-list">
                                                     <!--<li>-->
                                                        <%
                                                            int position = 0;
                                                            for (Mediator mediator : sequence.getList()) {
                                                                count--;
                                                                Mediator beforeMed = position > 0 ? sequence.getList().get(position - 1) : null;
                                                                Mediator afterMed = position + 1 < sequence.getList().size() ? sequence.getList().get(position + 1) : null;
                                                        %>
                                                    <%=SequenceEditorHelper.getMediatorHTML(mediator, count==0, String.valueOf(position), config, beforeMed, afterMed, request.getLocale())%>
                                                        <%
                                                                position++;
                                                            }
                                                        %>
                                                    <!--</li>-->
                                                </ul>

                                                <%
                                                    }
                                                %>
                                                <!--</div>-->
                                            </li>
                                        </ul>

                                </div>
                            </div>

                            <script type="text/javascript">
                                jQuery(document).ready(function() {

                                    jQuery(".toggle_container").hide();
                                    jQuery("h2.trigger").click(function() {
                                        if (jQuery(this).next().is(":visible")) {
                                            this.className = "active trigger";
                                        } else {
                                            this.className = "trigger";
                                        }

                                        jQuery(this).next().slideToggle("fast");
                                        return false; //Prevent the browser jump to the link anchor
                                    });
                                });
                            </script>

                            <h2 class="trigger active"><a href="#">Sequence Description</a></h2>

                            <div class="toggle_container">
                                <textarea name="seqeunceDescription" id="seqeunceDescription" title="Sequence Description"
                                          cols="100" rows="3"><%= sequence.getDescription() != null ? sequence.getDescription() : ""%></textarea>
                            </div>


                        </td>
                    </tr>

        </table>

            <table class="normal" width="100%">
                <tr><td><table class="styledLeft" cellspacing="0">
    <tr id="mediator-designview-header" style="display:none;">
        <td class="middle-header" >
            <span style="float:left; position:relative; margin-top:2px;"><fmt:message key="mediator.design.view.text"/></span><a onclick="showSource()" class="icon-link" style="background-image:url(images/source-view.gif);"><fmt:message key="sequence.switchto.source.text"/></a>
        </td>
    </tr>
    <tr id="mediator-sourceview-header" style="display:none;">
        <td class="middle-header">
            <span style="float:left; position:relative; margin-top:2px;"><fmt:message key="mediator.source.view.text"/></span><a onclick="showDesign()" class="icon-link" style="background-image:url(images/design-view.gif);"><fmt:message key="sequence.switchto.design.text"/></a>
        </td>
    </tr>
    <tr id="mediator-edit-tab" style="display:none;">
        <td style="padding: 0px !important;">
            <div class="tabPaneContentMain forProxy" id="mediatorDesign" name="mediatorDesign" style="display:none;width:auto;padding:0px;"></div>
            <div id="mediatorSource" name="mediatorSource" style="display:none;"></div>
        </td>
    </tr>
</table>
</td></tr>
</table>
</td></tr>
        <%
                            if (sequence instanceof TemplateMediator) {
                                TemplateMediator template = (TemplateMediator) sequence;
                                String propertyTableStyle = template.getParameters().size() == 0 ? "display:none;" : "";
        %>
                <tr>

                                <div style="margin-top:0px;">

                            <table id="templatePropertyTable" style="<%=propertyTableStyle%>" class="styledInner">
                                <thead>
                                    <tr>
                                        <th width="75%"><fmt:message key="template.parameter.name"/></th>
                                        <th><fmt:message key="template.parameter.action"/></th>
                                    </tr>
                                    <tbody id="templatePropertyBody">
                                        <%
                                                int i = 0;

                                                Iterator<String> params=template.getParameters().iterator();
                                                while(params.hasNext()){
                                                    String paramName = params.next();

                                        %>
                                                    <tr id="templatePropertyRaw<%=i%>">
                                                        <td><input type="text" name="templatePropertyName<%=i%>" id="templatePropertyName<%=i%>"
                                                                   class="esb-edit small_textbox"
                                                                   value="<%=paramName%>"/>
                                                        </td>
                                                        <td><a href="#" class="delete-icon-link"
                                                                onclick="deleteProperty(<%=i%>)"><fmt:message key="template.parameter.delete"/></a></td>
                                                    </tr>


                                                <%
                                                    i++;
                                                }%>
                                                <input type="hidden" name="templatePropertyCount" id="templatePropertyCount" value="<%=i%>"/>

                                            </tbody>
                                        </thead>
                                   </table>
                                </div>


        </tr>
        <tr>
            <td>
                <div style="margin-top:10px;">
                    <a name="addParamLink"></a>
                    <a class="add-icon-link"
                       href="#addParamLink"
                       onclick="addTemplateParameter()"><fmt:message key="template.parameter.add"/></a>
                </div>
            </td>
        </tr>

        <%
            }
        %>

                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button" value="<fmt:message key="<%=SequenceEditorHelper.getUIMetadataForEditor("sequence.button.save.text",session)%>"/>" id="saveButton" onclick="javascript: saveSequence(); return false;"/>
                            <%
                                if (SequenceEditorHelper.getEditingSequenceAction(session) != "anonify") {
                            %>
                                <input type="button" class="button" value="<fmt:message key='<%=SequenceEditorHelper.getUIMetadataForEditor("sequence.button.saveas.text",session)%>'/>" id="saveAsButton" onclick="javascript: showSaveAsForm(true); return false;">
                            <%
                                }
                            %>
                            <input type="button" class="button" value="<fmt:message key="sequence.button.apply.text"/>" id="applyButton" onclick="javascript: applySequence(); return false;"/>
                            <input type="button" class="button" value="<fmt:message key="sequence.button.cancel.text"/>" onclick="javascript: cancelSequence(); return false;"/>

                        </td>
                    </tr>
                </tbody>
            </table>






            <div style="display:none;" id="saveAsForm">
                <p>&nbsp;</p>
                <table class="styledLeft">
                    <thead>
                        <tr>
                            <th colspan="2">
                                <span style="float:left; position:relative; margin-top:2px;"><fmt:message key="sequence.save.as.title"/></span>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>
                                <table class="normal">
                                    <tr>
                                        <td><fmt:message key="save.in"/></td>
                                        <td><fmt:message key="config.registry"/> <input type="radio" name="registry" id="config_reg"
                                                                       value="conf:" checked="checked"
                                                                       onclick="document.getElementById('reg').innerHTML='conf:';"/>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                            <fmt:message key="gov.registry"/> <input type="radio" name="registry" id="gov_reg"
                                                                       value="gov:"
                                                                       onclick="document.getElementById('reg').innerHTML='gov:';"/>

                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="sequence.save.as.key"/></td>
                                        <td><span id="reg">conf:</span><input type="text" size="75" id="synRegKey"/></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="buttonRow">
                                <input type="button" class="button" value="<fmt:message key="sequence.button.save.text"/>" id="saveSynRegButton" onclick="javascript: saveSequenceAs(); return false;"/>
                                <input type="button" class="button" value="Cancel" id="cancelSynRegButton" onclick="javascript: showSaveAsForm(false); return false;">
                            </td>
                        </tr>

                    </tbody>


                </table>
            </div>
        </div>



        <%
            if (session.getAttribute("sequence.error.message") != null) {
        %>
        <script type="text/javascript">
            <%
                String seqErrorMsg = (String) session.getAttribute("sequence.error.message");
            %>
            jQuery(document).ready(function() {
                CARBON.showErrorDialog("<%= StringEscapeUtils.escapeXml(seqErrorMsg) %>");
            });
        </script>
        <%
                session.removeAttribute("sequence.error.message");
            }

            if (session.getAttribute("sequence.error2.message") != null) {
        %>
        <script type="text/javascript">
            <%
                String seqErrorMsg2 = (String) session.getAttribute("sequence.error2.message");
            %>
            jQuery(document).ready(function() {
                CARBON.showErrorDialog("<%= StringEscapeUtils.escapeXml(seqErrorMsg2) %>");
            });
        </script>
        <%
                session.removeAttribute("sequence.error2.message");
            }

            if (session.getAttribute("sequence.warn.message") != null) {
        %>
        <script type="text/javascript">
            <%
                String seqWarnMsg = (String) session.getAttribute("sequence.warn.message");
            %>
            jQuery(document).ready(function() {
                CARBON.showWarningDialog("<%= StringEscapeUtils.escapeXml(seqWarnMsg) %>");
            });
        </script>
        <%
                session.removeAttribute("sequence.warn.message");
            }
        %>

    </div>


</fmt:bundle>
<%
    }
%>


