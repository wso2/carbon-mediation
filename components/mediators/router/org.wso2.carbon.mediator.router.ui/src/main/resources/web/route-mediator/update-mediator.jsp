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

<%@ page import="org.wso2.carbon.mediator.router.RouteMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="java.util.regex.Pattern" %>
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

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof RouteMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    RouteMediator routeMediator = (RouteMediator) mediator;

    String expression = request.getParameter("mediator.route.expression");
    String pattern = request.getParameter("mediator.route.pattern");
    String routeBreak = request.getParameter("mediator.route.break");

    routeMediator.setExpression(null);
    routeMediator.setMatch(null);

    if (expression != null && !"".equals(expression)) {
        XPathFactory xPathFactory = XPathFactory.getInstance();
        routeMediator.setExpression(xPathFactory.createSynapseXPath("mediator.route.expression", request, session));       
    }

    if (pattern != null && !"".equals(pattern)) {
        routeMediator.setMatch(Pattern.compile(pattern));
    }

    if (routeBreak != null && "true".equalsIgnoreCase(routeBreak)) {
        routeMediator.setBreakRouter(true);
    } else {
        routeMediator.setBreakRouter(false);
    }

    session.removeAttribute("anonEpXML");
%>

<script type="text/javascript">
    document.location.href = "../sequences/design_sequence.jsp";
</script>

