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
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    //Todo - Call a admin service and get fact types
    Collection<String> facts = new ArrayList<String>();
    facts.add("mediator");
    String type = request.getParameter("type");
    String category = request.getParameter("category");
    boolean isNew = (type == null || "".equals(type));
    if (type == null) {
        type = "";
    }

    int index = Integer.parseInt(request.getParameter("index"));
    boolean isFact = "fact".equals(category);
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
    String newFactDisplay = isNew ? "" : "display:none;";
    String existingFactDisplay = isNew ? "display:none;" : "";

%>
<script type="text/javascript">
    function hideEditor() {
        CARBON.closeWindow();
    }
    function saveFacts(category, i) {
        var factTypeTD = document.getElementById(category + "Type" + i);
        var newFactTR = document.getElementById("factTypeValueNewType");
        if (newFactTR.checked) {
            var newFact = document.getElementById("newFactType");
            factTypeTD.value = newFact.value;
        } else {
            factTypeTD.value = getSelectedValue("factType_Selector");
        }
        hideEditor();
    }

    function setFactType(type) {
        var existingFactTR = document.getElementById("factTypeSelectionTR");
        var newFactTR = document.getElementById("newFactTypeTR");
        if ('new.type' == type) {
            newFactTR.style.display = "";
            existingFactTR.style.display = "none";
        } else {
            existingFactTR.style.display = "";
            newFactTR.style.display = "none";
        }
        return true;
    }

</script>
<fmt:bundle basename="org.wso2.carbon.mediator.rule.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.rule.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="rulejsi18n"/>
    <%--<carbon:breadcrumb--%>
    <%--label="step2.msg"--%>
    <%--resourceBundle="org.wso2.carbon.rule.service.ui.i18n.Resources"--%>
    <%--topPage="false"--%>
    <%--request="<%=request%>"/>--%>

    <div id="middle">
        <div id="workArea">
            <table class="styledLeft">
                    <%--<thead>--%>
                    <%--<tr>--%>
                    <%--<th><fmt:message key="facts.selector"/></th>--%>
                    <%--</tr>--%>
                    <%--</thead>--%>
                <tr>
                    <td class="formRaw">
                        <table class="normal">
                            <tr>
                                <td>
                                    <% if (isFact) {%>
                                    <fmt:message key="fact.type.as"/>
                                    <%} else {%>
                                    <fmt:message key="result.type.as"/>
                                    <%}%></td>
                                <td>
                                    <%
                                        if (isNew) {
                                    %>
                                    <input type="radio" name="factTypeValue"
                                           id="factTypeValueNewType"
                                           value="new.type"
                                           onclick="setFactType('new.type');"
                                           checked="checked"/>
                                    <fmt:message key="new.value"/>
                                    <input type="radio" name="factTypeValue"
                                           id="factTypeValueExistingType"
                                           value="existing.type"
                                           onclick="setFactType('existing.type');"/>
                                    <fmt:message key="existing.type"/>
                                    <% } else { %>
                                    <input type="radio" name="factTypeValue"
                                           id="factTypeValueNewType"
                                           value="new.type"
                                           onclick="setFactType('new.type');"/>
                                    <fmt:message key="new.value"/>
                                    <input type="radio" name="factTypeValue"
                                           id="factTypeValueExistingType"
                                           value="existing.type"
                                           onclick="setFactType('existing.type');"
                                           checked="checked"/>
                                    <fmt:message key="existing.type"/>
                                    <%} %>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="formRaw">
                        <table class="normal">
                            <tr id="factTypeSelectionTR" style="<%=existingFactDisplay%>">
                                <td><% if (isFact) {%>
                                    <fmt:message key="fact.type"/>
                                    <%} else {%>
                                    <fmt:message key="result.type"/>
                                    <%}%><font
                                            color="red">*</font>
                                </td>
                                <td>
                                    <select class="longInput" name="factType_Selector"
                                            id="factType_Selector">
                                        <%
                                            for (String fact : facts) {
                                                if (type.equals(fact)) {
                                        %>
                                        <option value="<%=fact%>" selected="selected"><%=fact%>
                                        </option>
                                        <%
                                        } else {
                                        %>
                                        <option value="<%=fact%>"><%=fact%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                            </tr>
                            <tr id="newFactTypeTR" style="<%=newFactDisplay%>">
                                <td><% if (isFact) {%>
                                    <fmt:message key="fact.type"/>
                                    <%} else {%>
                                    <fmt:message key="result.type"/>
                                    <%}%><font
                                            color="red">*</font>
                                </td>
                                <td><input class="longInput" type="text" name="newFactType"
                                           id="newFactType"
                                           value="<%=type.trim()%>"/></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <input class="button" type="button" value="<fmt:message key="save"/>"
                               onclick="saveFacts('<%=category%>','<%=index%>')"/>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>"
                               onclick="hideEditor()"/>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>