<!--
 ~ Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformation" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepository" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
%>

<%
    String currentID = request.getParameter("currentID");
    String name = request.getParameter("name");
    if (currentID == null || "".equals(currentID)) {
        throw new RuntimeException("'currentID' parameter cannot be found");
    }

    NameSpacesInformationRepository repository = (NameSpacesInformationRepository) session.getAttribute(
            NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
    NameSpacesInformation information = null;
    if (repository == null) {
        repository = new NameSpacesInformationRepository();
        session.setAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
    } else {
        information = repository.getNameSpacesInformation(name, currentID);
    }
    if (information == null) {
        information = new NameSpacesInformation();
        repository.addNameSpacesInformation(name, currentID, information);
    }
    information.removeAllNameSpaces();
    String CountParameter = request.getParameter("nsCount");
    if (CountParameter != null && !"".equals(CountParameter)) {
        try {
            int count = Integer.parseInt(CountParameter.trim());
            for (int i = 0; i < count; i++) {
                String prefix = request.getParameter("prefix" + i);
                String uri = request.getParameter("uri" + i);
                if (prefix != null && uri != null && !"".equals(uri)) {
                    uri = uri.trim();
                    prefix = prefix.trim();
                    if (!SYNAPSE_NS.equals(uri)) {
                        information.addNameSpace(prefix, uri);
                    }
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }
%>

