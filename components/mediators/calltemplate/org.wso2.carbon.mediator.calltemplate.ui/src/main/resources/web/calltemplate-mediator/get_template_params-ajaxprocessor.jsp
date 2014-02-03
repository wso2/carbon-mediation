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

<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.client.TemplateAdminClient" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.util.CallUtil" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.util.Value" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.mediator.calltemplate.i18n.Resources">

<%
    TemplateAdminClient templateAdminClient
            = new TemplateAdminClient(this.getServletConfig(), session);

    String templateName = request.getParameter("templateSelect");
    String paramStr = templateAdminClient.getParameterStringForTemplate(templateName);
/*
    System.out.println("get Template params processor....." + " templ name : " + templateName +
                       "   paramStr : " + paramStr);
*/

    String[] callTemplateParamList = CallUtil.extractParamNames(paramStr);
%>


<%
    int i = 0;
    for (String  mp : callTemplateParamList) {
        if (!(mp.isEmpty())) {
            Value value = null;
            SynapseXPath synapseXPath = null;
            boolean isLiteral = true;
%>
<tr id="propertyRaw<%=i%>">
    <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
               value="<%=mp%>"/>
    </td>
    <td>
        <select name="propertyTypeSelection<%=i%>"
                id="propertyTypeSelection<%=i%>"
                onchange="onPropertyTypeSelectionChange('<%=i%>','<fmt:message key="namespaces"/>')">
            <% if (isLiteral) {%>
            <option value="literal">
                <fmt:message key="value"/>
            </option>
            <option value="expression">
                <fmt:message key="expression"/>
            </option>
            <%} else if (synapseXPath != null) {%>
            <option value="expression">
                <fmt:message key="expression"/>
            </option>
            <option value="literal">
                <fmt:message key="value"/>
            </option>
            <%} else { %>
            <option value="literal">
                <fmt:message key="value"/>
            </option>
            <option value="expression">
                <fmt:message key="expression"/>
            </option>
            <% }%>
        </select>
    </td>
    <td>
        <% if (value != null && isLiteral) {%>
        <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
               value="<%=value.getKeyValue()%>"
                />
        <%} else if (synapseXPath != null) {%>
        <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
               value="<%=synapseXPath.toString()%>"/>
        <%} else { %>
        <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"/>
        <% }%>
    </td>

    <td id="dynamicXpathCol<%=i%>" style="<%=isLiteral? "display:none;" : ""%>">
        <% if (value!=null && !isLiteral && synapseXPath != null) {%>
        <script type="text/javascript">
            document.getElementById("dynamic-xpath-th").style.display = "";
        </script>
        <input id="dynamicCheckbox<%=i%>" name="dynamicCheckbox<%=i%>" type="checkbox"
               value="true" <%=value.hasExprTypeKey()?"CHECKED":""%>/>
          <%}%>
    </td>
    <td id="nsEditorButtonTD<%=i%>" style="<%=isLiteral? "display:none;" : ""%>">
        <% if (!isLiteral && synapseXPath != null) {%>
        <script type="text/javascript">
            document.getElementById("ns-edior-th").style.display = "";
        </script>
        <a href="#nsEditorLink" class="nseditor-icon-link"
           style="padding-left:40px"
           onclick="showNameSpaceEditor('propertyValue<%=i%>')"><fmt:message
                key="namespaces"/></a>
          <%}%>
    </td>


    <td><a href="#" class="delete-icon-link" onclick="deleteproperty('<%=i%>');return false;"><fmt:message
            key="delete"/></a></td>
</tr>
    <%
            i++;
        }

    }
    %>
<input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
<script type="text/javascript">
    if (isRemainPropertyExpressions()) {
        resetDisplayStyle("");
    }else{
        resetDisplayStyle("none");
    }
    var paramCount = 0;
    paramCount = <%=i%>;
    var propertytable = document.getElementById("propertytable");
    if(paramCount>0){
        propertytable.style.display = "";
    }
    else{
        propertytable.style.display = "none";
    }
</script>
</fmt:bundle>