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

<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.SequenceEditorConstants" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.sequences.ui.i18n.Resources", request.getLocale());
    String sequenceName = request.getParameter("sequenceName");
    String onErrorKey = request.getParameter("onErrorKey");
    String sequenceXML = "";
    SequenceMediator sequence = SequenceEditorHelper.getEditingSequence(session);
    if (sequence == null) {
        session.setAttribute("sequence.error.message", bundle.getString("sequence.source.view.error"));
%>
<script type="text/javascript">
    document.location.href = "list_sequences.jsp";
</script>
<%
    } else {

    if (onErrorKey != null && !"".equals(onErrorKey)) {
        SequenceEditorHelper.getEditingSequence(session).setErrorHandler(onErrorKey);
    }

    try {
        if (SequenceEditorConstants.ACTION_PARAM_VALUE_ADD.equals(
                SequenceEditorHelper.getEditingSequenceAction(session))) {
            if (sequenceName != null) {
                SequenceEditorHelper.getEditingSequence(session).setName(sequenceName);
            }
            sequenceXML = SequenceEditorHelper.parseSequenceToPrettyfiedString(
                    SequenceEditorHelper.getEditingSequence(session));
        } else if (SequenceEditorConstants.ACTION_PARAM_VALUE_ANONIFY.equals(
                SequenceEditorHelper.getEditingSequenceAction(session))) {
            if((String)session.getAttribute("sequence")!=null){
            sequenceXML = SequenceEditorHelper.parseAnonSequenceToPrettyfiedString(
                    SequenceEditorHelper.getEditingSequence(session), (String)session.getAttribute("sequence"));
            } else {
                sequenceXML = SequenceEditorHelper.parseAnonSequenceToPrettyfiedString(
                        SequenceEditorHelper.getEditingSequence(session));    
            }
        } else {
            sequenceXML = SequenceEditorHelper.parseSequenceToPrettyfiedString(
                    SequenceEditorHelper.getEditingSequence(session));
        }

        sequenceXML =  StringEscapeUtils.escapeXml(sequenceXML);
    } catch (Exception e) {
        session.setAttribute("sequence.error2.message", "" + e.getMessage());
%>
<script type="text/javascript">
    document.location.href = "design_sequence.jsp?ordinal=1";
</script>
<%
        return;
    }
%>

<script type="text/javascript">
    	var xt="",h3OK=1;
	function checkErrorXML(x) {
	    xt = ""
	    h3OK = 1
	    checkXML(x)
	}
	
	function checkXML(n)
	{
	    var l,i,nam
	    nam = n.nodeName
	    if (nam == "h3")
	    {
	        if (h3OK == 0)
	        {
	            return;
	        }
	        h3OK = 0
	    }
	    if (nam == "#text")
	    {
	        xt = xt + n.nodeValue + "\n"
	    }
	    l = n.childNodes.length
	    for (i = 0; i < l; i++)
	    {
	        checkXML(n.childNodes[i])
	    }
	}
	function validateXML(txt)
	{
	    // code for IE
	    var error = "";
	    if (window.ActiveXObject)
	    {
	        var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
	        xmlDoc.async = "false";
	        xmlDoc.loadXML(txt);
	
	        if (xmlDoc.parseError.errorCode != 0)
	        {
	            txt = "Error Code: " + xmlDoc.parseError.errorCode + "\n";
	            txt = txt + "Error Reason: " + xmlDoc.parseError.reason;
	            txt = txt + "Error Line: " + xmlDoc.parseError.line;
	            error = txt;
	        }
	    }
	    // code for Mozilla, Firefox, Opera, etc.
	    else if (document.implementation.createDocument)
	    {
	        var parser = new DOMParser();
	        var text = txt;
	        var xmlDoc = parser.parseFromString(text, "text/xml");
	
	        if (xmlDoc.getElementsByTagName("parsererror").length > 0)
	        {
	            checkErrorXML(xmlDoc.getElementsByTagName("parsererror")[0]);
	            error = xt;
	        }
	
	    }
	    return error;
	
	}
    function cancelSequence() {
        <%
        String annonOriginator = (String) session.getAttribute("sequenceAnonOriginator");
        if (annonOriginator != null && annonOriginator.equals("registry_sequence.jsp")) {
        %>
            window.location.href='<%=annonOriginator%>' + '?cancelled=true';
        <%} else {
        %>
            window.location.href = "<%=SequenceEditorHelper.getForwardToFrom(session)%>";
        <%}
        %>
    }

    function saveSequence() {
        document.getElementById("sequence_source").value = editAreaLoader.getValue("sequence_source");        
        document.seqSrcForm.action = "save_sequence.jsp";
        document.seqSrcForm.submit();
    }

    function saveSequenceAs() {
        var key = document.getElementById('synRegKey').value;
        if (key == '') {
            CARBON.showWarningDialog('Registry key must not be empty');
            return false;
        }

        var registry;
        if (document.getElementById("config_reg").checked == true) {
            registry = 'conf';
        } else {
            registry = 'gov';
        }

        document.seqSrcForm.action = "save_sequence_as.jsp?regKey=" + key+ "&registry=" + registry;
        document.seqSrcForm.submit();
    }

    function designView() {
        var source_form = document.getElementById("sequence.source.form");
        var entryvalue = editAreaLoader.getValue("sequence_source");
        document.getElementById("sequence_source").value = entryvalue;
        var error = validateXML(entryvalue);
	if(error != "")
	{
		CARBON.showErrorDialog("<fmt:message key="invalid.value.error.parsing.xml"/><br />"+ error);
		return false;		
	}
        source_form.action = "design_sequence.jsp?ordinal=1";
        source_form.submit();
    }

    function showSaveAsForm(show) {
        var formElem = document.getElementById('saveAsForm');
        if (show) {
            formElem.style.display = "";

            var keyField = document.getElementById('synRegKey');
            if (keyField.value == '') {
                keyField.value =  document.getElementById('sequenceName').value;
            }
        } else {
            formElem.style.display = "none";
        }
    }
</script>

<fmt:bundle basename="org.wso2.carbon.sequences.ui.i18n.Resources">
<carbon:jsi18n
                resourceBundle="org.wso2.carbon.sequences.ui.i18n.JSResources"
                request="<%=request%>" />

    <carbon:breadcrumb
            label="sequence.source.text"
            resourceBundle="org.wso2.carbon.sequences.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>" />

    <div id="middle">
        <h2><fmt:message key="sequence.source.header"/></h2>
        <div id="workArea">
            <form action="" method="post" id="sequence.source.form" name="seqSrcForm">
            <table class="styledLeft" cellspacing="0" cellpadding="0">
                <thead>
                    <tr>
                        <th>
			<span style="float:left; position:relative; margin-top:2px;"><fmt:message key="sequence.source.view.text"/></span><a href="#" onclick="designView()" class="icon-link" style="background-image:url(images/design-view.gif);"><fmt:message key="sequence.switchto.design.text"/></a>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><font style="color:#333333; font-size:small;"><fmt:message key="sequence.source.name.warning"/></font></td>
                    </tr>
                    <tr>
                        <td>
                            <textarea id="sequence_source" name="sequenceXML" style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 400px; margin-top: 5px;"><%= sequenceXML %></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" class="button" onclick="javascript: saveSequence();" value="<fmt:message key="sequence.button.save.text"/>"/>
                            <%
                                if (SequenceEditorHelper.getEditingSequenceAction(session) != "anonify") {
                            %>
                            <input type="button" class="button" onclick="javascript: showSaveAsForm(true);" value="<fmt:message key="sequence.button.saveas.text"/>"/>
                            <%
                                }
                            %>
                             <input type="button" class="button" onclick="javascript: cancelSequence(); return false;" value="<fmt:message key="sequence.button.cancel.text"/>"/>
                        </td>
                    </tr>
                </tbody>
            </table>
                </form>
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
                                        <td><fmt:message key="config.registry"/>
                                            <input type="radio" name="registry" id="config_reg" value="conf:" checked="checked" onclick="document.getElementById('reg').innerHTML='conf:';"/>
                                             &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                            <fmt:message key="gov.registry"/> <input type="radio" name="registry" id="gov_reg" value="gov:" onclick="document.getElementById('reg').innerHTML='gov:';"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><fmt:message key="sequence.save.as.key"/></td>
                                        <td><input type="text" size="75" id="synRegKey"/><input type="hidden" id="sequenceName" value="<% out.println(sequenceName);%>"></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="buttonRow">
                                <input type="button" class="button" value="<fmt:message key='sequence.button.save.text'/>" id="saveSynRegButton" onclick="javascript: saveSequenceAs(); return false;"/>
                                <input type="button" class="button" value="<fmt:message key='sequence.button.cancel.text'/>" id="cancelSynRegButton" onclick="javascript: showSaveAsForm(false); return false;">
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
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
        %>

        <%
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
    <script type="text/javascript">
        editAreaLoader.init({
            id : "sequence_source"		// textarea id
            ,syntax: "xml"			// syntax to be uses for highgliting
            ,start_highlight: true		// to display with highlight mode on start-up
        });
    </script>
</fmt:bundle>
<%
    }
%>
