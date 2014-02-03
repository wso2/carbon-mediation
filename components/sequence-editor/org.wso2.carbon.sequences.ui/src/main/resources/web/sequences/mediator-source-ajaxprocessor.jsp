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

<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.common.SequenceEditorException" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%--<script src="../codepress/codepress.js" type="text/javascript"></script>--%>

<%
    ResourceBundle bundle = ResourceBundle.getBundle(SequenceEditorHelper.BUNDLE, request.getLocale());
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String mediatorXML = null;
    try {
        mediatorXML = SequenceEditorHelper.serializeMediator(mediator);
        mediatorXML = StringEscapeUtils.escapeXml(mediatorXML);
    } catch (SequenceEditorException e) {
        session.setAttribute("sequence.warn.message", MessageFormat.format(bundle.getString("mediator.view.source.error"), e.getMessage()));
%>
<script type="text/javascript">
    document.location.href = "design_sequence.jsp";
</script>
<%
    }

    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
%>

<fmt:bundle basename="org.wso2.carbon.sequences.ui.i18n.Resources">
<carbon:jsi18n
                resourceBundle="org.wso2.carbon.sequences.ui.i18n.JSResources"
                request="<%=request%>" />

    <div>
        <form action="mediator-source-update.jsp" id="mediator-source-form" name="mediator-source-form">
            <table class="styledLeft" cellspacing="0" cellpadding="0" style="margin-left: 0px;">
                <tbody>
                    <tr>
                        <td style="padding: 0px !important; border: solid 1px #ccc; border-top: 0px;" colspan="2" id="mediatorSrcTD">
                            <textarea id="mediatorSrc" name="mediatorSrc" style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"><%= mediatorXML %>
                            </textarea>
                        </td>
                    </tr>
                    <tr class="buttonRow" style="border: solid 1px #ccc; border-top: 0px;">
                        <td>
                            <input type="submit" class="button" onclick="updateSource(); return false;"
                                   value="<fmt:message key="sequence.button.update.text"/>"/>
                            <input type="hidden" name="random" value="<%=Math.random()%>"/>
                        </td>
                        <td id="whileUpload" style="display:none">
                            <img align="top" src="../resources/images/ajax-loader.gif"/>
                            <span><fmt:message key="sequence.update.message"/></span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </form>
    </div>
    <script type="text/javascript">
         editAreaLoader.delete_instance("mediatorSrc");

    </script>
</fmt:bundle>
