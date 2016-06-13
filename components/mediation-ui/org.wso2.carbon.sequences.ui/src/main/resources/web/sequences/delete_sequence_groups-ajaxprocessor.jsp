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

<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>


<%
    //ignore methods other than post
    if (!request.getMethod().equalsIgnoreCase("POST")) {
        response.sendError(405);
        return;
    }
    String[] sequenceGroups = null;
    String sequenceString = request.getParameter("sequenceGroupsString");
    if (sequenceString != null && !sequenceString.equalsIgnoreCase("")) {
        sequenceGroups = sequenceString.split(":");
    }
    String pageNumber = request.getParameter("pageNumberSequence");
    String deleteAllSequenceGroups = request.getParameter("deleteAllSequenceGroups");
    int pageNumberInt = 0;
    if (pageNumber != null && !pageNumber.equals("")) {
        try {
            pageNumberInt = Integer.parseInt(pageNumber);
        } catch (Exception e) {

        }
    }

    SequenceAdminClient sequenceAdminClient;

    try {
        sequenceAdminClient = new SequenceAdminClient(this.getServletConfig(), session);
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<script type="text/javascript">
    location.href = "../../admin/error.jsp";
</script>
<%
        return;
    }
    try {
        if (deleteAllSequenceGroups != null && deleteAllSequenceGroups.equalsIgnoreCase("true")) {
            sequenceAdminClient.deleteAllSequenceGroups();
        } else if (sequenceGroups != null && sequenceGroups.length > 0) {
            sequenceAdminClient.deleteSelectedSequence(sequenceGroups);
        }

    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
    }
%>









