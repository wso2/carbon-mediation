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
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%
    String ruleScriptID = request.getParameter("scriptID");
    if (ruleScriptID == null || "".equals(ruleScriptID)) {
        throw new RuntimeException("'scriptID' parameter cannot be found");
    }
    String ruleScript = request.getParameter("scriptxml");

    Map ruleScriptsMap = (Map) request.getSession().getAttribute("rulemediator_script_map");

    if (ruleScriptsMap == null) {
        ruleScriptsMap = new HashMap();
        request.getSession().setAttribute("rulemediator_script_map", ruleScriptsMap);
    }

    ruleScriptsMap.put(ruleScriptID, ruleScript);

%>

