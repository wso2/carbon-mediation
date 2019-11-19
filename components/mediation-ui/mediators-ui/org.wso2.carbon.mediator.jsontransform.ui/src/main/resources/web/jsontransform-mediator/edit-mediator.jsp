<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%--
  ~  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.mediator.jsontransform.JSONTransformMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof JSONTransformMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    JSONTransformMediator jsonTransformMediator = (JSONTransformMediator) mediator;
    List<MediatorProperty> mediatorPropertyList = jsonTransformMediator.getProperties();
    String propertyTableStyle = mediatorPropertyList.isEmpty() ? "display:none;" : "";
%>

<fmt:bundle basename="org.wso2.carbon.mediator.log.ui.i18n.Resources">
 <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.log.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="logi18n"/>
<div>
<script type="text/javascript" src="../log-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.log.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.log.category"/></td>
                <td>
                    <select id="mediator.log.category" name="mediator.log.category">
                        <%
                            if(logMediator.getLogCategory() == JSONTransforMediator.TRACE_VALUE){
                        %>
                        <option value="<%=JSONTransforMediator.TRACE_VALUE%>" selected="true"><fmt:message key="mediator.log.category.trace"/></option>
                        <option value="<%=JSONTransforMediator.DEBUG_VALUE%>"><fmt:message key="mediator.log.category.debug"/></option>
                        <option value="<%=LogMediator.INFO_VALUE%>"><fmt:message key="mediator.log.category.info"/></option>
                        <option value="<%=LogMediator.WARN_VALUE%>"><fmt:message key="mediator.log.category.warn"/></option>
                        <option value="<%=LogMediator.ERROR_VALUE%>"><fmt:message key="mediator.log.category.error"/></option>
                        <option value="<%=LogMediator.FATAL_VALUE%>"><fmt:message key="mediator.log.category.fatal"/></option>
                        <%
                            } else if (logMediator.getLogCategory() == LogMediator.DEBUG_VALUE) {
                        %>
                        <option value="<%=LogMediator.TRACE_VALUE%>"><fmt:message key="mediator.log.category.trace"/></option>
                        <option value="<%=LogMediator.DEBUG_VALUE%>" selected="true"><fmt:message key="mediator.log.category.debug"/></option>
                        <option value="<%=LogMediator.INFO_VALUE%>"><fmt:message key="mediator.log.category.info"/></option>
                        <option value="<%=LogMediator.WARN_VALUE%>"><fmt:message key="mediator.log.category.warn"/></option>
                        <option value="<%=LogMediator.ERROR_VALUE%>"><fmt:message key="mediator.log.category.error"/></option>
                        <option value="<%=LogMediator.FATAL_VALUE%>"><fmt:message key="mediator.log.category.fatal"/></option>
                        <%
                            } else if (logMediator.getLogCategory() == LogMediator.INFO_VALUE) {
                        %>
                        <option value="<%=LogMediator.TRACE_VALUE%>"><fmt:message key="mediator.log.category.trace"/></option>
                        <option value="<%=LogMediator.DEBUG_VALUE%>"><fmt:message key="mediator.log.category.debug"/></option>
                        <option value="<%=LogMediator.INFO_VALUE%>" selected="true"><fmt:message key="mediator.log.category.info"/></option>
                        <option value="<%=LogMediator.WARN_VALUE%>"><fmt:message key="mediator.log.category.warn"/></option>
                        <option value="<%=LogMediator.ERROR_VALUE%>"><fmt:message key="mediator.log.category.error"/></option>
                        <option value="<%=LogMediator.FATAL_VALUE%>"><fmt:message key="mediator.log.category.fatal"/></option>
                        <%
                            } else if (logMediator.getLogCategory() == LogMediator.WARN_VALUE) {
                        %>
                        <option value="<%=LogMediator.TRACE_VALUE%>"><fmt:message key="mediator.log.category.trace"/></option>
                        <option value="<%=LogMediator.DEBUG_VALUE%>"><fmt:message key="mediator.log.category.debug"/></option>
                        <option value="<%=LogMediator.INFO_VALUE%>"><fmt:message key="mediator.log.category.info"/></option>
                        <option value="<%=LogMediator.WARN_VALUE%>" selected="true"><fmt:message key="mediator.log.category.warn"/></option>
                        <option value="<%=LogMediator.ERROR_VALUE%>"><fmt:message key="mediator.log.category.error"/></option>
                        <option value="<%=LogMediator.FATAL_VALUE%>"><fmt:message key="mediator.log.category.fatal"/></option>
                        <%
                            } else if (logMediator.getLogCategory() == LogMediator.ERROR_VALUE) {
                        %>
                        <option value="<%=LogMediator.TRACE_VALUE%>"><fmt:message key="mediator.log.category.trace"/></option>
                        <option value="<%=LogMediator.DEBUG_VALUE%>"><fmt:message key="mediator.log.category.debug"/></option>
                        <option value="<%=LogMediator.INFO_VALUE%>"><fmt:message key="mediator.log.category.info"/></option>
                        <option value="<%=LogMediator.WARN_VALUE%>"><fmt:message key="mediator.log.category.warn"/></option>
                        <option value="<%=LogMediator.ERROR_VALUE%>" selected="true"><fmt:message key="mediator.log.category.error"/></option>
                        <option value="<%=LogMediator.FATAL_VALUE%>"><fmt:message key="mediator.log.category.fatal"/></option>
                        <%
                            } else if (logMediator.getLogCategory() == LogMediator.FATAL_VALUE) {
                        %>
                        <option value="<%=LogMediator.TRACE_VALUE%>"><fmt:message key="mediator.log.category.trace"/></option>
                        <option value="<%=LogMediator.DEBUG_VALUE%>"><fmt:message key="mediator.log.category.debug"/></option>
                        <option value="<%=LogMediator.INFO_VALUE%>"><fmt:message key="mediator.log.category.info"/></option>
                        <option value="<%=LogMediator.WARN_VALUE%>"><fmt:message key="mediator.log.category.warn"/></option>
                        <option value="<%=LogMediator.ERROR_VALUE%>"><fmt:message key="mediator.log.category.error"/></option>
                        <option value="<%=LogMediator.FATAL_VALUE%>" selected="true"><fmt:message key="mediator.log.category.fatal"/></option>
                        <%}%>
                    </select>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.log.logLevel"/></td>
                <td>
                    <select id="mediator.log.log_level" name="mediator.log.log_level">
                        <%
                            if (logMediator.getLogLevel() == LogMediator.FULL_VALUE) {
                        %>
                        <option value="<%=LogMediator.FULL_VALUE%>" selected="true"><fmt:message key="mediator.log.level.full"/></option>
                        <option value="<%=LogMediator.SIMPLE_VALUE%>"><fmt:message key="mediator.log.level.simple"/></option>
                        <option value="<%=LogMediator.HEADERS_VALUE%>"><fmt:message key="mediator.log.level.headers"/></option>
                        <option value="<%=LogMediator.CUSTOM_VALUE%>"><fmt:message key="mediator.log.level.custom"/></option>
                        <%
                        } else if (logMediator.getLogLevel() == LogMediator.SIMPLE_VALUE) {
                        %>
                        <option value="<%=LogMediator.FULL_VALUE%>"><fmt:message key="mediator.log.level.full"/></option>
                        <option value="<%=LogMediator.SIMPLE_VALUE%>" selected="true"><fmt:message key="mediator.log.level.simple"/></option>
                        <option value="<%=LogMediator.HEADERS_VALUE%>"><fmt:message key="mediator.log.level.headers"/></option>
                        <option value="<%=LogMediator.CUSTOM_VALUE%>"><fmt:message key="mediator.log.level.custom"/></option>
                        <%
                        } else if (logMediator.getLogLevel() == LogMediator.HEADERS_VALUE) {
                        %>
                        <option value="<%=LogMediator.FULL_VALUE%>"><fmt:message key="mediator.log.level.full"/></option>
                        <option value="<%=LogMediator.SIMPLE_VALUE%>"><fmt:message key="mediator.log.level.simple"/></option>
                        <option value="<%=LogMediator.HEADERS_VALUE%>" selected="true"><fmt:message key="mediator.log.level.headers"/></option>
                        <option value="<%=LogMediator.CUSTOM_VALUE%>"><fmt:message key="mediator.log.level.custom"/></option>
                        <%
                        } else if (logMediator.getLogLevel() == LogMediator.CUSTOM_VALUE) {
                        %>
                        <option value="<%=LogMediator.FULL_VALUE%>"><fmt:message key="mediator.log.level.full"/></option>
                        <option value="<%=LogMediator.SIMPLE_VALUE%>"><fmt:message key="mediator.log.level.simple"/></option>
                        <option value="<%=LogMediator.HEADERS_VALUE%>"><fmt:message key="mediator.log.level.headers"/></option>
                        <option value="<%=LogMediator.CUSTOM_VALUE%>" selected="true"><fmt:message key="mediator.log.level.custom"/></option>
                        <%
                        } else {
                        %>
                        <option value="<%=LogMediator.SIMPLE_VALUE%>" selected="true"><fmt:message key="mediator.log.level.select"/></option>
                        <option value="<%=LogMediator.FULL_VALUE%>"><fmt:message key="mediator.log.level.full"/></option>
                        <option value="<%=LogMediator.SIMPLE_VALUE%>"><fmt:message key="mediator.log.level.simple"/></option>
                        <option value="<%=LogMediator.HEADERS_VALUE%>"><fmt:message key="mediator.log.level.headers"/></option>
                        <option value="<%=LogMediator.CUSTOM_VALUE%>"><fmt:message key="mediator.log.level.custom"/></option>
                        <%
                            }
                        %>
                    </select>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.log.separator"/></td>
                <td>
                    <input type="text" id="mediator.log.log_separator" name="mediator.log.log_separator"
                           value="<%= logMediator.getSeparator() != null ?
                           logMediator.getSeparator() : LogMediator.DEFAULT_SEP %>"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td>
        <h3 class="mediator">
            <fmt:message key="mediator.log.properties"/></h3>

        <div style="margin-top:0px;">

            <table id="propertytable" style="<%=propertyTableStyle%>;" class="styledInner">
                <thead>
                    <tr>
                        <th width="15%"><fmt:message key="mediator.log.propertyName"/></th>
                        <th width="10%"><fmt:message key="mediator.log.propertyValue"/></th>
                        <th width="15%"><fmt:message key="mediator.log.propertyExp"/></th>
                        <th id="ns-edior-th" style="display:none;" width="15%"><fmt:message
                                key="mediator.log.nsEditor"/></th>
                        <th><fmt:message key="mediator.log.action"/></th>
                    </tr>
                    <tbody id="propertytbody">
                        <%
                            int i = 0;
                            for (MediatorProperty mp : mediatorPropertyList) {
                                if (mp != null) {
                                    String value = mp.getValue();
                                    boolean isLiteral = value != null && !"".equals(value);
                                    
                                    String pathValue = "";
                                    SynapsePath path = null;
                                    SynapseXPath synapseXPath = mp.getExpression();
                                    if(!isLiteral) {
                                    	if(synapseXPath == null) {
                                    		path = mp.getPathExpression();
                                    	    pathValue = path.toString();
                                    	} else {
                                    	    path = synapseXPath;
                                    	    pathValue = path.toString();
                                    	}
                                    }
                        %>
                        <tr id="propertyRaw<%=i%>">
                            <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                                       class="esb-edit small_textbox"
                                       value="<%=mp.getName()%>"/>
                            </td>
                            <td>
                                <select class="esb-edit small_textbox" name="propertyTypeSelection<%=i%>"
                                        id="propertyTypeSelection<%=i%>"
                                        onchange="onPropertyTypeSelectionChange('<%=i%>','<fmt:message key="mediator.log.namespace"/>')">
                                    <% if (isLiteral) {%>
                                    <option value="literal">
                                        <fmt:message key="mediator.log.value"/>
                                    </option>
                                    <option value="expression">
                                        <fmt:message key="mediator.log.expression"/>
                                    </option>
                                    <%} else if (path != null) {%>
                                    <option value="expression">
                                        <fmt:message key="mediator.log.expression"/>
                                    </option>
                                    <option value="literal">
                                        <fmt:message key="mediator.log.value"/>
                                    </option>
                                    <%} else { %>
                                    <option value="literal">
                                        <fmt:message key="mediator.log.value"/>
                                    </option>
                                    <option value="expression">
                                        <fmt:message key="mediator.log.expression"/>
                                    </option>
                                    <% }%>
                                </select>
                            </td>
                            <td>
                                <% if (value != null && !"".equals(value)) {%>
                                <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                                       value="<%=value%>"
                                       class="esb-edit"/>
                                <%} else if (path != null) {%>
                                <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                                       value="<%=pathValue%>" class="esb-edit"/>
                                <%} else { %>
                                <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                                       class="esb-edit"/>
                                <% }%>
                            </td>
                            <td id="nsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
                                <% if (!isLiteral && path != null) {%>
                                <script type="text/javascript">
                                    document.getElementById("ns-edior-th").style.display = "";
                                </script>
                                <a href="#nsEditorLink" class="nseditor-icon-link"
                                   style="padding-left:40px"
                                   onclick="showNameSpaceEditor('propertyValue<%=i%>')">
                                    <fmt:message key="mediator.log.namespace"/></a>
                            </td>
                            <%}%>
                            <td><a href="#" class="delete-icon-link" 
                                   onclick="deleteproperty(<%=i%>);return false;"><fmt:message
                                    key="mediator.log.delete"/></a></td>
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
<tr>
    <td>
        <div style="margin-top:10px;">
            <a name="addNameLink"></a>
            <a class="add-icon-link"
               href="#addNameLink"
               onclick="addproperty('<fmt:message key="mediator.log.namespace"/>','<fmt:message key="mediator.log.propemptyerror"/>','<fmt:message key="mediator.log.valueemptyerror"/>')"><fmt:message
                    key="mediator.log.addProperty"/></a>
        </div>
    </td>
</tr>
</table>
<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>