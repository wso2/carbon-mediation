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

<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.priority.executors.ui.Executor" %>
<%@ page import="org.wso2.carbon.priority.executors.ui.Queue" %>
<%@ page import="org.wso2.carbon.priority.executors.ui.PriorityAdminClient" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%

    PriorityAdminClient client = new PriorityAdminClient(getServletConfig(), session);
    String executorName = request.getParameter("name");
    String action = request.getParameter("action");
    String mode = request.getParameter("mode");
    String forwardTo = request.getParameter("forwardTo");
    String sourceXML = request.getParameter("sourceXML");

    Executor executor = new Executor();
    
    OMElement elem = null;
    try {
        ByteArrayInputStream bais = new ByteArrayInputStream(sourceXML.getBytes());
        StAXOMBuilder builder = new StAXOMBuilder(bais);
        elem = builder.getDocumentElement();
    } catch (XMLStreamException e) {

    }
    executor.build(elem);

    if (action.equals("save")) {
        if (mode.equals("add")) {
            client.add(executor.getName(), executor);
        } else if (mode.equals("edit")) {
            client.update(executor.getName(), executor);
        }
    } else if (action.equals("source") || action.equals("design")) {
        session.setAttribute("source_executor", executor);
    }
%>

<script type="text/javascript">
    <% if (forwardTo == null) { %>
        document.location.href = 'list_executors.jsp?action=<%=action%>&mode=<%=mode%>';
    <% } else { %>
        document.location.href = '<%=forwardTo%>' + '?action=<%=action%>&mode=<%=mode%>';
    <% } %>
</script>