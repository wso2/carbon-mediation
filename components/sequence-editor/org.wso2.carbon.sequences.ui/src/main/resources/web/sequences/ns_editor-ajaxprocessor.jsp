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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepository" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformation" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String currentID = request.getParameter("currentID");
    if (currentID == null || "".equals(currentID)) {
        //TODO
        throw new RuntimeException("'currentID' parameter cannot be found");
    }
    String divID = request.getParameter("divID");

    if (divID == null || "".equals(divID)) {
        //TODO
        throw new RuntimeException("'divID' parameter cannot be found");
    }

    String linkID = request.getParameter("linkID");
    if (linkID == null || "".equals(linkID)) {
        linkID = "null";
    }
    String editorMode = request.getParameter("editorMode");
    boolean isSingle = "single".equals(editorMode);
    String displayStyle = isSingle ? "display:none;" : "";

    NameSpacesInformationRepository repository = (NameSpacesInformationRepository) session.getAttribute(
            NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
    NameSpacesInformation information = null;
    if (repository == null) {
        repository = new NameSpacesInformationRepository();
        session.setAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
    } else {
        information = repository.getNameSpacesInformation(SequenceEditorHelper.getEditingMediatorPosition(session), currentID);
    }
    if (information == null) {
        information = new NameSpacesInformation();
        repository.addNameSpacesInformation(SequenceEditorHelper.getEditingMediatorPosition(session), currentID, information);
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
    <div id="nsEditorContent" style="margin-top:10px;">
        <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
           
            <tbody>
                <tr>
                    <td>
                        <div style="margin-top:10px;">
                            <table border="0" cellpadding="0" cellspacing="0" width="600" id="nsTable"
                                   class="styledInner">
                                <thead>
                                    <tr>
                                        <th width="25%"><fmt:message key="ns.prefix"/></th>
                                        <th width="50%"><fmt:message key="ns.uri"/></th>
                                        <th style="<%=displayStyle%>"><fmt:message key="ns.action"/></th>
                                    </tr>
                                </thead>

                                <tbody id="nsTBody">
                                    <%
                                        Iterator<String> prefixes = information.getPrefixes();
                                        int i = 0;
                                        if (prefixes.hasNext()) {
                                            while (prefixes.hasNext()) {
                                                String prefix = prefixes.next();
                                                String uri = information.getNameSpaceURI(prefix);
                                                if (prefix == null) {
                                                    prefix = "";
                                                }
                                                if (uri == null) {
                                                    uri = "";
                                                }

                                    %>
                                    <tr id="nsTR<%=i%>">
                                        <td align="left">
                                            <input type="text" style="width:100px" name="prefix<%=i%>" id="prefix<%=i%>"
                                                   value="<%=prefix%>"/>
                                        </td>
                                        <td>
                                            <input id="uri<%=i%>" class="longInput" name="uri<%=i%>" type="text" value="<%=uri%>"/>

                                        </td>
                                        <td style="<%=displayStyle%>"><a href="#" class="delete-icon-link"
                                                                         onclick="deletensraw('<%=i%>')"><fmt:message
                                                key="ns.delete"/></a>
                                        </td>

                                    </tr>
                                    <% i++;
                                        if (isSingle) {
                                            break;
                                        }
                                    }
                                    } else if (isSingle) {
                                        i++; %>
                                    <tr id="nsTR0">
                                        <td align="left">
                                            <input type="text" name="prefix0" id="prefix0" value=""/>
                                        </td>
                                        <td>
                                            <input id="uri0" name="uri0" type="text" value=""/>
                                        </td>
                                    </tr>
                                    <%}%>
                                    <input type="hidden" name="nsCount" id="nsCount" value="<%=i%>"/>
                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow" colspan="3">
                        <input style="<%=displayStyle%>" id="addNSButton" class="button" name="addNSButton"
                               type="button"
                               onclick="javascript: addNameSpace('<fmt:message key="ns.delete"/>','<fmt:message key="ns.prefixemptyerror"/>','<fmt:message key="ns.uriemptyerror"/>'); return false;"
                               href="#"
                               value="<fmt:message key="ns.add"/>"/>
                        <input id="saveNSButton" class="button" name="saveNSButton" type="button"
                               onclick="javascript: saveNameSpace('<%=divID%>', '<%=currentID%>', '<%=linkID%>','<fmt:message key="ns.prefixemptyerror.save"/>','<fmt:message key="ns.uriemptyerror.save"/>', <%=isSingle ? "true" : "false"%>); return false;"
                               href="#"
                               value="<fmt:message key="ns.save"/>"/>
                        <input id="cancelNSButton" class="button" name="cancelNSButton" type="button"
                               onclick="javascript: hideNameSpaceEditor('<%=divID%>', '<%=linkID%>'); return false;"
                               href="#"
                               value="<fmt:message key="ns.cancel"/>"/>
                        <input style="<%=isSingle ? "" : "display:none;"%>" id="clearNameSpace" class="button" name="clearNameSpace"
                               type="button"
                               onclick="javascript: clearSingleNameSpace(); return false;"
                               href="#"
                               value="<fmt:message key="ns.clear"/>"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</fmt:bundle>
