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
<%@ page import="java.util.Map" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    String ruleScriptID = request.getParameter("scriptID");
    if (ruleScriptID == null || "".equals(ruleScriptID)) {
        throw new RuntimeException("'scriptID' parameter cannot be found");
    }

    Map ruleScriptsMap = (Map) request.getSession().getAttribute("rulemediator_script_map");
    String ruleScript = "";
    if (ruleScriptsMap != null) {
        ruleScript = (String) ruleScriptsMap.get(ruleScriptID);
        if (ruleScript == null) {
            ruleScript = "";
        }
    }
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

%>

<fmt:bundle basename="org.wso2.carbon.mediator.rule.ui.i18n.Resources">

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.rule.ui.i18n.JSResources"
            request="<%=request%>"/>
    <div id="ruleScriptEditorContent" style="margin-top:10px;">
        <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">

            <tbody>
            <tr>
                <td>
                    <div style="margin-top:10px;">
                        <table border="0" cellpadding="0" cellspacing="0" width="600" id="nsTable"
                               class="styledInner">

                            <tbody id="ruleScriptTBody">
                            <tr>
                                <td>
                                    <textarea id="inlined_rule_script_source"
                                              name="inlined_rule_script_source"
                                              style="width:100%;height:400px"><%=ruleScript%>
                                    </textarea>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="buttonRow" colspan="3">

                    <input id="saveRuleScriptButton" class="button" name="saveRuleScriptButton"
                           type="button"
                           onclick="saveRuleScript('<%=ruleScriptID%>'); return false;"
                           href="#"
                           value="<fmt:message key="mediator.rule.editor.save"/>"/>
            </tr>
            </tbody>
        </table>
    </div>
</fmt:bundle>
