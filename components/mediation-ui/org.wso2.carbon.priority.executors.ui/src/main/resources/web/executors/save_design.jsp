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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%

    PriorityAdminClient client = new PriorityAdminClient(getServletConfig(), session);
//    String executorName = request.getParameter("name");
    String action = request.getParameter("action");
    String mode = request.getParameter("mode");
    String forwardTo = request.getParameter("forwardTo");
//    String sourceXML = "";

    Executor executor = new Executor();

    String param = request.getParameter("executor.name");
    if (param != null && !param.equals("")) {
        executor.setName(param);
    }

    param = request.getParameter("isFixed");
    if (param != null && !param.equals("")) {
        if (param.equalsIgnoreCase("true")) {
            executor.setFixedSize(true);
        } else if (param.equalsIgnoreCase("false")) {
            executor.setFixedSize(false);
        }
    }

    int maxProp = 0;
        /* get the hidden value indicating the max property name */
    param = request.getParameter("hidden_queues");
    if (param != null && !param.equals("")) {
        maxProp = Integer.valueOf(param);
    }

    for (int i = 1; i < maxProp; i++) {
        int s = 0;
        int p = 0;
        param = request.getParameter("priority" + i);
        if (param != null && !param.equals("")) {
            p = Integer.parseInt(param);

            if (executor.isFixedSize()) {
                param = request.getParameter("capacity" + i);
                if (param != null && !param.equals("")) {
                    s = Integer.parseInt(param);
                }
                executor.getQueues().add(new Queue(p, s));
            } else {
                executor.getQueues().add(new Queue(p));
            }
        }
    }

    param = request.getParameter("nextQueue");
    if (param != null && !param.equals("")) {
        executor.setAlgorithm(param);
    }

    param = request.getParameter("core");
    if (param != null) {
        executor.setCore(Integer.parseInt(param));
    }

    param = request.getParameter("max");
    if (param != null) {
        executor.setMax(Integer.parseInt(param));
    }

    param = request.getParameter("keep_alive");
    if (param != null) {
        executor.setKeepAlive(Integer.parseInt(param));
    }

    if (action.equals("none")) {
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
        document.location.href = 'list_executors.jsp?action=<%=Encode.forHtml(action)%>&mode=<%=Encode.forHtml(mode)%>';
    <% } else { %>
        document.location.href = '<%=Encode.forHtml(forwardTo)%>' + '?action=<%=Encode.forHtml(action)%>&mode=<%=Encode.forHtml(mode)%>';
    <% } %>
</script>