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

<%@ page import="org.wso2.carbon.mediator.service.MediatorService" %>
<%@ page import="org.wso2.carbon.mediator.service.MediatorStore" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.Set" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String mediatorPosition = request.getParameter("mediatorID");    
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String logicalName = mediator.getTagLocalName();
    MediatorStore store = MediatorStore.getInstance();
    MediatorService mediatorInfo = store.getMediatorService(logicalName);
    String editPage = "../" + mediatorInfo.getUIFolderName() + "-mediator/edit-mediator.jsp";
    String updatePage = "../" + mediatorInfo.getUIFolderName() + "-mediator/update-mediator.jsp";
    String helpPage = "../" + mediatorInfo.getUIFolderName() + "-mediator/docs/userguide.html";
    // set the mediator position to the session
    session.setAttribute("mediator.position", mediatorPosition);

    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    //session.removeAttribute("anonEpXML");
%>

<fmt:bundle basename="org.wso2.carbon.sequences.ui.i18n.Resources">
<carbon:jsi18n
                resourceBundle="org.wso2.carbon.sequences.ui.i18n.JSResources"
                request="<%=request%>" />
<div>
    <%
        Set resourcePaths = config.getServletContext().getResourcePaths(
                "/" + mediatorInfo.getUIFolderName() + "-mediator");
        if (resourcePaths != null && resourcePaths.contains("/"
                + mediatorInfo.getUIFolderName() + "-mediator/edit-mediator.jsp")) {
    %>

    <script type="text/javascript">
        currentMedTLN = '<%= mediatorInfo.getUIFolderName() %>';
    </script>

            <div class="page-header-help">
                <a target="_blank" href="<%= helpPage %>"><fmt:message key="sequence.mediator.help"/></a>
            </div>

    <form action="mediator-update-ajaxprocessor.jsp" id="mediator-editor-form" name="mediator-editor-form">
        <jsp:include page="<%= editPage %>" flush="true">
            <jsp:param name="mediatorPosition" value="<%=mediatorPosition%>"/>
        </jsp:include>
        
        <%
            if (mediatorInfo.isEditable()) {
        %>
        <table class="styledLeft" cellspacing="0" style="margin-left: 0px !important;">
<tr class="buttonRow" style="border-top: solid 1px #ccc;">
<td>
    <input type="hidden" id="mediatorID" name="mediatorID" value="<%=mediatorPosition%>"/>
    <input type="hidden" id="updatePage" name="updatePage" value="<%=updatePage%>"/>
    <input type="hidden" name="random" value="<%=Math.random()%>"/>
    <% if (mediatorInfo.isSequenceRefreshRequired()) { %>
    <input type="button" class="button"
           onclick="javascript: updateMediator('<%= mediatorInfo.getUIFolderName() %>', true); return false;"
           value="<fmt:message key="sequence.button.update.text"/>"/>
    <% } else { %>
    <input type="button" class="button"
           onclick="javascript: updateMediator('<%= mediatorInfo.getUIFolderName() %>', false); return false;"
           value="<fmt:message key="sequence.button.update.text"/>"/>
    <% } %>
</td>
<td id="whileUpload" style="display:none">
    <img align="top" src="../resources/images/ajax-loader.gif"/>
    <span><fmt:message key="sequence.update.message"/></span>
</td>

</tr>
    </table>
        <%
            }
        %>
    </form>

    <%
        } else {
    %>
            <script type="text/javascript">
                hide("mediator-designview-header");
                hide("mediator-edit-tab");
                hide("mediatorDesign");
            </script>
    <%
        }
    %>

</div>
</fmt:bundle>