<%--
~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="js/registry-browser.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript" src="js/tabs.js"></script>

<%--<script type="text/javascript" src="js/form.js"></script>--%>
<script type="text/javascript" src="js/endpoint-util.js"></script>
<script type="text/javascript" src="js/template-param.js"></script>
<script type="text/javascript" src="js/endpoint-params.js"></script>
<script type="text/javascript" src="js/common-tasks.js"></script>

<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<link rel="stylesheet" type="text/css" href="css/errorcode-editor.css"/>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        request="<%=request%>"/>
<carbon:breadcrumb
        label="address.endpoint"
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    session.removeAttribute("isEditingListEndpoint");
    session.removeAttribute("endpointName");
    session.removeAttribute("endpointAction");
    session.removeAttribute("origin");
    session.removeAttribute("templateAdd");

    String endpointName = request.getParameter("endpointName");
    String endpointAction = request.getParameter("endpointAction");
    String origin = request.getParameter("origin");
    String templateAdd = request.getParameter("templateAdd");

    if (endpointName != null) {
        session.setAttribute("endpointName", endpointName);
    }
    if (endpointAction != null) {
        session.setAttribute("endpointAction", endpointAction);
    }
    if (origin != null) {
        session.setAttribute("origin", origin);
    }
    if (templateAdd != null) {
        session.setAttribute("templateAdd", templateAdd);
    }

    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;
    if (isFromTemplateEditor) {
        //template mode cant coexist with  anonymous mode
        //remove any anonymous mode related session attributes (still any session attributes may exist ie:- if
        // anonymous mode could nt exit properly)
        String epMode = (String) session.getAttribute("epMode");
        if (epMode != null && "anon".equals(epMode)) {
            session.removeAttribute("epMode");
        }
    }

    boolean isAnonymous = false;
    String endpointMode = (String) session.getAttribute("epMode");
    String anonymousOriginator = null;
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
        anonymousOriginator = (String) session.getAttribute("anonOriginator");
    }
%>

<div id="middle">
<h2>
    <% if (request.getParameter("serviceName") != null) {
    %><%=request.getParameter("serviceName")%>:&nbsp;<%
    }
    if ("edit".equals(endpointAction) && isFromTemplateEditor) {
%><fmt:message key="edit.endpoint.template"/><%
} else if ("edit".equals(endpointAction)) {
%><fmt:message key="edit.endpoint"/><%
} else if (isFromTemplateEditor) {
%><fmt:message key="address.endpoint.template"/><%
} else {
%><fmt:message key="address.endpoint"/><%
    }
%>
</h2>

<div id="workArea">
    <form action="updatePages/addressEndpoint-update.jsp" id="endpoint-editor-form"
          name="endpoint-editor-form">
        <table class="styledLeft">
            <thead>
            <tr>
                <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="design.view.of.the.address.endpoint"/></span>
                    <a class="icon-link"
                       style="background-image: url(images/source-view.gif);"
                       onclick="switchToSource('Address','<%=isAnonymous%>','<%=isFromTemplateEditor%>');"
                       href="#"><fmt:message key="switch.to.source.view"/></a>
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <div id="endpointForm">
                        <jsp:include page="forms/addressEndpoint-form.jsp" flush="true">
                            <jsp:param name="request" value="<%=request%>"/>
                        </jsp:include>
                    </div>
                    <div id="buttonRow">
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <input type="button" value="<fmt:message key="save"/>"
                                           class="button" name="save"
                                           onclick="javascript:submitEndpointData('Address','<%=isAnonymous%>','<%=isFromTemplateEditor%>');"/>
                                    <%
                                        if (!isAnonymous && !isFromTemplateEditor) {
                                    %>
                                    <input type="button" value="<fmt:message key="saveas"/>"
                                           class="button" name="save"
                                           onclick="javascript:showSaveAsForm(true,false);"/>
                                    <%
                                    } else if (isFromTemplateEditor && session.getAttribute("templateEdittingMode") == null) {
                                    %>
                                    <input type="button" value="<fmt:message key="saveas"/>"
                                           class="button" name="save"
                                           onclick="javascript:showSaveAsForm(true,true);"/>
                                    <%
                                        }
                                    %>
                                    <input type="button" value="<fmt:message key="cancel"/>"
                                           name="cancel" class="button"
                                           onclick="javascript:cancelEndpointData('<%=anonymousOriginator%>','<%=isFromTemplateEditor%>');"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div style="display:none;" id="saveAsForm">
                        <p>&nbsp;</p>
                        <table class="styledLeft">
                            <thead>
                            <tr>
                                <th colspan="2">
                                    <span style="float:left; position:relative; margin-top:2px;"><fmt:message
                                            key="save.as.title"/></span>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>
                                    <table class="normal">
                                        <tr>
                                            <td><fmt:message key="save.in"/></td>
                                            <td><fmt:message key="config.registry"/> <input
                                                    type="radio" name="registry" id="config_reg"
                                                    checked="checked" value="conf:"
                                                    onclick="document.getElementById('reg').innerHTML='conf:';"/>
                                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                                <fmt:message key="gov.registry"/> <input
                                                    type="radio" name="registry" id="gov_reg"
                                                    value="gov:"
                                                    onclick="document.getElementById('reg').innerHTML='gov:';"/>


                                            </td>
                                        </tr>
                                        <tr>
                                            <td><fmt:message key="registry.key"/>
                                                <span class="required">*</span>
                                            </td>
                                            <td><span id="reg">conf:</span><input type="text"
                                                                                 size="75"
                                                                                 id="synRegKey"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td class="buttonRow">
                                    <input type="button" class="button"
                                           value="<fmt:message key="save"/>" id="saveSynRegButton"
                                           onclick="javascript:submitDynamicEndpointData('Address','<%=isFromTemplateEditor%>'); return false;"/>
                                    <input type="button" class="button"
                                           value="<fmt:message key="cancel"/>"
                                           id="cancelSynRegButton"
                                           onclick="javascript:showSaveAsForm(false,'<%=isFromTemplateEditor%>'); return false;">
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
</div>

</fmt:bundle>

