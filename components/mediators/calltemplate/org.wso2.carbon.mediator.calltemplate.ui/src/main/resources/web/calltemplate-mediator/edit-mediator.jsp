<%--
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
 --%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.CallTemplateMediator" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.util.Value" %>
<%@ page import="org.wso2.carbon.mediator.calltemplate.client.TemplateAdminClient" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.calltemplate.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.calltemplate.i18n.JSResources"
        request="<%=request%>" i18nObjectName="xsltjsi18n"/>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CallTemplateMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CallTemplateMediator callMediator = (CallTemplateMediator) mediator;

    String target = "";
    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    if (callMediator.getTargetTemplate() != null) {
        target = callMediator.getTargetTemplate();
    }
    Set<String> callTemplateParamList = callMediator.getpName2ExpressionMap().keySet();
    int j = 0;
    for (String param : callTemplateParamList) {
        Value val = callMediator.getpName2ExpressionMap().get(param);
        if (val != null && val.getExpression() != null) {
            nameSpacesRegistrar.registerNameSpaces(val.getExpression(), "propertyValue"+j, session);
        }
        j++;
    }
    String propertyTableStyle = callTemplateParamList.isEmpty() ? "display:none;" : "";

    TemplateAdminClient templateAdminClient
            = new TemplateAdminClient(this.getServletConfig(), session);
    String[] templateNameList = templateAdminClient.getAllTempalateNames();

%>
<div>
<script type="text/javascript" src="../calltemplate-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="call.mediator.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">

           <tr>
                <td>
                    <fmt:message key="mediator.call.target"/><span class="required">*</span>
                </td>
                <td>
 		    <input type="hidden" id="mediator.call.target" name="mediator.call.target"
                           value="<%=target%>"/>
                    <input class="longInput" type="text" id="mediator.call.target.visible" name="mediator.call.target.visble"
                           value="<%=target%>" disabled="true"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="mediator.call.target.avaliable"/>
                </td>
                <td>
                    <select name="templateSelector" id="templateSelector" onchange="onTemplateSelectionChange()">
                        <option value="default">Select From Templates</option>
                        <%
                            for (String templateName : templateNameList) {%>
                                <option value="<%=templateName%>"><%=templateName%></option>
                            <%}%>
                    </select>
                </td>
            </tr>
        </table>
        
    </td>
</tr>
<tr>
    <td>
        <h3 class="mediator">
            <fmt:message key="parameters"/></h3>

        <div style="margin-top:0px;">
            <table id="propertytable" style="<%=propertyTableStyle%>;" class="styledInner">
                <thead>
                    <tr>
                        <th width="15%"><fmt:message key="th.parameter.name"/></th>
                        <th width="10%"><fmt:message key="th.parameter.type"/></th>
                        <th width="15%"><fmt:message key="th.parameter.value.expression"/></th>
                        <th id="dynamic-xpath-th" style="display:none;" width="10%"><fmt:message key="dynamicxpath"/></th>
                        <th id="ns-edior-th" style="display:none;" width="15%"><fmt:message key="namespaceeditor"/></th>
                        <th><fmt:message key="th.action"/></th>
                    </tr>
                    <tbody name="propertytbody" id="propertytbody">
                        <%
                            int i = 0;
                            for (String  mp : callTemplateParamList) {
                                if (mp != null) {
                                    Value value = callMediator.getpName2ExpressionMap().get(mp);
                                    SynapseXPath synapseXPath = value.getExpression();
                                    boolean isLiteral = synapseXPath == null;
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
                        <% }
                            i++;
                        } %>
                        <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
                        <script type="text/javascript">
                            if (isRemainPropertyExpressions()) {
                                resetDisplayStyle("");
                            }
                        </script>
                    </tbody>
                </thead>
            </table>
        </div>
    </td>
</tr>
<%--<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addNameLink"></a>
            <a class="add-icon-link"
               href="#addNameLink"
               onclick="addproperty('<fmt:message key="namespaces"/>','<fmt:message key="nameemptyerror"/>','<fmt:message key="valueemptyerror"/>')"><fmt:message
                    key="add.property"/></a>
        </div>
    </td>
</tr> --%>

</table>
<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>

<a name="registryBrowserLink"></a>

<div id="registryBrowser" style="display:none;"></div>
</div>
</fmt:bundle> 
