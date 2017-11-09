<%--
~  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~  Licensed to the Apache Software Foundation (ASF) under one or more
~  contributor license agreements.  See the NOTICE file distributed with
~  this work for additional information regarding copyright ownership.
~
~  The ASF licenses this file to You under the Apache License, Version 2.0
~
~  (the "License"); you may not use this file except in compliance with
~  the License.  You may obtain a copy of the License at
~       http://www.apache.org/licenses/LICENSE-2.0
~  Unless required by applicable law or agreed to in writing, software
~  distributed under the License is distributed on an "AS IS" BASIS,
~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~  See the License for the specific language governing permissions and
~  limitations under the License.
--%>
<%@ page import="org.apache.synapse.SynapseException" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.context.PrivilegedCarbonContext" %>
<%@ page import="org.wso2.carbon.event.sink.EventSink" %>
<%@ page import="org.wso2.carbon.event.sink.EventSinkService" %>
<%@ page import="org.wso2.carbon.mediator.publishevent.ui.Property" %>
<%@ page import="org.wso2.carbon.mediator.publishevent.ui.PublishEventMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof org.wso2.carbon.mediator.publishevent.ui.PublishEventMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;

    List<Property> mediatorMetaPropertyList = publishEventMediator.getMetaProperties();
    List<Property> mediatorCorrelationPropertyList = publishEventMediator.getCorrelationProperties();
    List<Property> mediatorPayloadPropertyList = publishEventMediator.getPayloadProperties();
    List<Property> mediatorArbitraryPropertyList = publishEventMediator.getArbitraryProperties();
    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();

    nameSpacesRegistrar.registerNameSpaces(convertPropertyList(mediatorMetaPropertyList), "metaPropertyValue", session);
    nameSpacesRegistrar
            .registerNameSpaces(convertPropertyList(mediatorCorrelationPropertyList), "correlationPropertyValue",
                                session);
    nameSpacesRegistrar
            .registerNameSpaces(convertPropertyList(mediatorPayloadPropertyList), "payloadPropertyValue", session);
    nameSpacesRegistrar
            .registerNameSpaces(convertPropertyList(mediatorArbitraryPropertyList), "arbitraryPropertyValue", session);
    String metaPropertyTableStyle = mediatorMetaPropertyList.isEmpty() ? "display:none;" : "";
    String correlationPropertyTableStyle = mediatorCorrelationPropertyList.isEmpty() ? "display:none;" : "";
    String payloadPropertyTableStyle = mediatorPayloadPropertyList.isEmpty() ? "display:none;" : "";
    String arbitraryPropertyTableStyle = mediatorArbitraryPropertyList.isEmpty() ? "display:none;" : "";

%>

<fmt:bundle basename="org.wso2.carbon.mediator.publishevent.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.publishevent.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="publishEventMediatorJsi18n"/>
<div>
<script type="text/javascript" src="../publishEvent-mediator/js/mediator-util.js"></script>

<table class="normal" width="100%">
<tbody>
<tr>
    <td colspan="3"><h2><fmt:message key="mediator.publishEvent.header"/></h2></td>
</tr>

<tr>
    <td>
        <table class="normal">
            <tr>
                <td style="width:130px;"><fmt:message key="mediator.publishEvent.stream.name"/><font
                        style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td>
                    <input type="text" id="mediator.publishEvent.stream.name" name="mediator.publishEvent.stream.name"
                           style="width:300px;"
                           value='<%=publishEventMediator.getStreamName() != null ? publishEventMediator.getStreamName() : ""%>'/>
                </td>
                <td></td>
            </tr>
            <tr>
                <td style="width:130px;"><fmt:message key="mediator.publishEvent.stream.version"/><font
                        style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td>
                    <input type="text" id="mediator.publishEvent.stream.version"
                           name="mediator.publishEvent.stream.version"
                           style="width:300px;"
                           value='<%=publishEventMediator.getStreamVersion() != null ? publishEventMediator.getStreamVersion() : ""%>'/>
                </td>
                <td></td>
            </tr>

            <tr>
                <td style="width:130px;"><fmt:message key="mediator.publishEvent.eventSink.name"/><font
                        style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td>


                    <select class="esb-edit small_textbox" name="mediator.publishEvent.eventSink.select"
                            id="mediator.publishEvent.eventSink.select">

                        <%
                            List<EventSink> eventSinkList;
                            Object o = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                                              .getOSGiService(EventSinkService.class);
                            if (o instanceof EventSinkService) {
                                EventSinkService service = (EventSinkService) o;
                                eventSinkList = service.getEventSinks();
                            } else {
                                throw new SynapseException(
                                        "Internal error occurred. Failed to obtain EventSinkService");
                            }

                            for (EventSink sink : eventSinkList) {
                        %>
                        <option <%
                            if (publishEventMediator.getEventSink() != null &&
                                publishEventMediator.getEventSink().equals(sink.getName())) {
                                out.print("selected");
                            }
                        %> value="<%=sink.getName()%>">
                            <%=sink.getName()%>
                        </option>
                        <%
                            }
                        %>

                    </select>

                </td>
                <td></td>
            </tr>
            <tr>
                <td style="width:130px;"><fmt:message key="mediator.publishEvent.isAsync"/>
                </td>
                <td>
                    <select class="esb-edit small_textbox" name="mediator.publishEvent.async.select"
                            id="mediator.publishEvent.async.select">
                        <% if (!Boolean.parseBoolean(publishEventMediator.isAsync())) {%>
                        <option value="false" selected="selected">
                            <fmt:message key="mediator.publishEvent.async.false"/>
                        </option>
                        <option value="true">
                            <fmt:message key="mediator.publishEvent.async.true"/>
                        </option>
                        <%} else { %>
                        <option value="true" selected="selected">
                            <fmt:message key="mediator.publishEvent.async.true"/>
                        </option>
                        <option value="false">
                            <fmt:message key="mediator.publishEvent.async.false"/>
                        </option>
                        <% }%>
                    </select>
                <td></td>
            </tr>
            <tr>
                <td style="width:130px;"><fmt:message key="mediator.publishEvent.async.timeout"/>
                </td>
                <td>
                    <input type="text" id="mediator.publishEvent.async.timeout"
                           name="mediator.publishEvent.async.timeout"
                           style="width:300px;"
                           value='<%=publishEventMediator.getTimeout() != null ? publishEventMediator.getTimeout() : ""%>'/>
                </td>
                <td></td>
            </tr>
        </table>
    </td>
</tr>

<!--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++Meta Attributes++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

<tr>
    <td colspan="3"><h4><fmt:message key="mediator.publishEvent.meta.header"/></h4></td>
</tr>

<tr id="metapropertytable" style="<%=metaPropertyTableStyle%>;">
    <td>


        <div style="margin-top:0;">

            <table  class="styledInner">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyName"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValue"/></th>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyExp"/></th>
                    <th id="meta-ns-editor-th" style="display:none;" width="15%"><fmt:message
                            key="mediator.publishEvent.nsEditor"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValueType"/></th>
                    <th><fmt:message key="mediator.publishEvent.action"/></th>
                </tr>
                </thead>
                <tbody id="metapropertytbody">
                <%
                    int i = 0;
                    for (Property mp : mediatorMetaPropertyList) {
                        if (mp != null) {
                            String value = mp.getValue();
                            String type = mp.getType();
                            String pathValue;
                            SynapseXPath path = mp.getExpression();
                            if (path == null) {
                                pathValue = "";
                            } else {

                                pathValue = path.toString();
                            }
                            boolean isLiteral = value != null && !"".equals(value);
                %>
                <tr id="metaPropertyRaw<%=i%>">
                    <td><input type="text" name="metaPropertyName<%=i%>" id="metaPropertyName<%=i%>"
                               class="esb-edit small_textbox"
                               value="<%=mp.getName()%>"/>
                    </td>
                    <td>
                        <select class="esb-edit small_textbox" name="metaPropertyTypeSelection<%=i%>"
                                id="metaPropertyTypeSelection<%=i%>"
                                onchange="onMetaPropertyTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>')">
                            <% if (isLiteral) {%>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <%} else if (path != null) {%>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <%} else { %>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <% }%>
                        </select>
                    </td>
                    <td>
                        <% if (value != null && !"".equals(value)) {%>
                        <input id="metaPropertyValue<%=i%>" name="metaPropertyValue<%=i%>" type="text"
                               value="<%=value%>"
                               class="esb-edit"/>
                        <%} else if (path != null) {%>
                        <input id="metaPropertyValue<%=i%>" name="metaPropertyValue<%=i%>" type="text"
                               value="<%=pathValue%>" class="esb-edit"/>
                        <%} else { %>
                        <input id="metaPropertyValue<%=i%>" name="metaPropertyValue<%=i%>" type="text"
                               class="esb-edit"/>
                        <% }%>
                    </td>
                    <td id="metaNsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
                        <% if (!isLiteral && path != null) {%>
                        <script type="text/javascript">
                            document.getElementById("meta-ns-editor-th").style.display = "";
                        </script>
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('metaPropertyValue<%=i%>')">
                            <fmt:message key="mediator.publishEvent.namespace"/></a>
                    </td>
                    <%}%>
                    <td>
                        <select class="esb-edit small_textbox" name="metaPropertyValueTypeSelection<%=i%>"
                                id="metaPropertyValueTypeSelection<%=i%>"
                                onchange="onPropertyValueTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>', 'meta')">

                            <option <% if (type.equals("STRING")) {
                                out.print("selected");
                            } %> value="STRING">
                                <fmt:message key="mediator.publishEvent.type.string"/>
                            </option>
                            <option <% if (type.equals("INTEGER")) {
                                out.print("selected");
                            } %> value="INTEGER">
                                <fmt:message key="mediator.publishEvent.type.integer"/>
                            </option>

                            <option <% if (type.equals("BOOLEAN")) {
                                out.print("selected");
                            } %> value="BOOLEAN">
                                <fmt:message key="mediator.publishEvent.type.boolean"/>
                            </option>

                            <option <% if (type.equals("DOUBLE")) {
                                out.print("selected");
                            } %> value="DOUBLE">
                                <fmt:message key="mediator.publishEvent.type.double"/>
                            </option>
                            <option <% if (type.equals("FLOAT")) {
                                out.print("selected");
                            } %> value="FLOAT">
                                <fmt:message key="mediator.publishEvent.type.float"/>
                            </option>
                            <option <% if (type.equals("LONG")) {
                                out.print("selected");
                            } %> value="LONG">
                                <fmt:message key="mediator.publishEvent.type.long"/>
                            </option>

                        </select>
                    </td>
                    <td><a href="#" class="delete-icon-link"
                           onclick="deleteMetaProperty(<%=i%>);return false;"><fmt:message
                            key="mediator.publishEvent.delete"/></a></td>
                </tr>
                <% }
                    i++;
                } %>
                <input type="hidden" name="metaPropertyCount" id="metaPropertyCount" value="<%=i%>"/>
                <script type="text/javascript">
                    if (isRemainPropertyExpressions('meta')) {
                        resetDisplayStyle("", 'meta');
                    }
                </script>
                </tbody>
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
               onclick="addproperty('<fmt:message key="mediator.publishEvent.namespace"/>','<fmt:message
                       key="mediator.publishEvent.propemptyerror"/>','<fmt:message
                       key="mediator.publishEvent.valueemptyerror"/>','meta')"><fmt:message
                    key="mediator.publishEvent.addProperty"/></a>
        </div>
    </td>
</tr>


<!--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++Correlated Attributes++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

<tr>
    <td colspan="3"><h4><fmt:message key="mediator.publishEvent.correlated.header"/></h4></td>
</tr>

<tr id="correlationpropertytable" style="<%=correlationPropertyTableStyle%>;">
    <td>


        <div style="margin-top:0;">

            <table class="styledInner">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyName"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValue"/></th>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyExp"/></th>
                    <th id="correlation-ns-editor-th" style="display:none;" width="15%"><fmt:message
                            key="mediator.publishEvent.nsEditor"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValueType"/></th>
                    <th><fmt:message key="mediator.publishEvent.action"/></th>
                </tr>
                </thead>
                <tbody id="correlationpropertytbody">
                <%
                    i = 0;
                    for (Property mp : mediatorCorrelationPropertyList) {
                        if (mp != null) {
                            String value = mp.getValue();
                            String type = mp.getType();
                            String pathValue;
                            SynapseXPath path = mp.getExpression();
                            if (path == null) {
                                pathValue = "";
                            } else {

                                pathValue = path.toString();
                            }
                            boolean isLiteral = value != null && !"".equals(value);
                %>
                <tr id="correlationPropertyRaw<%=i%>">
                    <td><input type="text" name="correlationPropertyName<%=i%>" id="correlationPropertyName<%=i%>"
                               class="esb-edit small_textbox"
                               value="<%=mp.getName()%>"/>
                    </td>
                    <td>
                        <select class="esb-edit small_textbox" name="correlationPropertyTypeSelection<%=i%>"
                                id="correlationPropertyTypeSelection<%=i%>"
                                onchange="onCorrelationPropertyTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>')">
                            <% if (isLiteral) {%>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <%} else if (path != null) {%>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <%} else { %>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <% }%>
                        </select>
                    </td>
                    <td>
                        <% if (value != null && !"".equals(value)) {%>
                        <input id="correlationPropertyValue<%=i%>" name="correlationPropertyValue<%=i%>" type="text"
                               value="<%=value%>"
                               class="esb-edit"/>
                        <%} else if (path != null) {%>
                        <input id="correlationPropertyValue<%=i%>" name="correlationPropertyValue<%=i%>" type="text"
                               value="<%=pathValue%>" class="esb-edit"/>
                        <%} else { %>
                        <input id="correlationPropertyValue<%=i%>" name="correlationPropertyValue<%=i%>" type="text"
                               class="esb-edit"/>
                        <% }%>
                    </td>
                    <td id="correlationNsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
                        <% if (!isLiteral && path != null) {%>
                        <script type="text/javascript">
                            document.getElementById("correlation-ns-editor-th").style.display = "";
                        </script>
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('correlationPropertyValue<%=i%>')">
                            <fmt:message key="mediator.publishEvent.namespace"/></a>
                    </td>
                    <%}%>
                    <td>
                        <select class="esb-edit small_textbox" name="correlationPropertyValueTypeSelection<%=i%>"
                                id="correlationPropertyValueTypeSelection<%=i%>"
                                onchange="onPropertyValueTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>','correlation')">

                            <option <% if (type.equals("STRING")) {
                                out.print("selected");
                            } %> value="STRING">
                                <fmt:message key="mediator.publishEvent.type.string"/>
                            </option>
                            <option <% if (type.equals("INTEGER")) {
                                out.print("selected");
                            } %> value="INTEGER">
                                <fmt:message key="mediator.publishEvent.type.integer"/>
                            </option>

                            <option <% if (type.equals("BOOLEAN")) {
                                out.print("selected");
                            } %> value="BOOLEAN">
                                <fmt:message key="mediator.publishEvent.type.boolean"/>
                            </option>

                            <option <% if (type.equals("DOUBLE")) {
                                out.print("selected");
                            } %> value="DOUBLE">
                                <fmt:message key="mediator.publishEvent.type.double"/>
                            </option>
                            <option <% if (type.equals("FLOAT")) {
                                out.print("selected");
                            } %> value="FLOAT">
                                <fmt:message key="mediator.publishEvent.type.float"/>
                            </option>
                            <option <% if (type.equals("LONG")) {
                                out.print("selected");
                            } %> value="LONG">
                                <fmt:message key="mediator.publishEvent.type.long"/>
                            </option>

                        </select>
                    </td>
                    <td><a href="#" class="delete-icon-link"
                           onclick="deleteCorrelationProperty(<%=i%>);return false;"><fmt:message
                            key="mediator.publishEvent.delete"/></a></td>
                </tr>
                <% }
                    i++;
                } %>
                <input type="hidden" name="correlationPropertyCount" id="correlationPropertyCount" value="<%=i%>"/>
                <script type="text/javascript">
                    if (isRemainPropertyExpressions('correlation')) {
                        resetDisplayStyle("", 'correlation');
                    }
                </script>
                </tbody>
            </table>
        </div>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:10px;">
            <a name="addCorrelationNameLink"></a>
            <a class="add-icon-link"
               href="#addCorrelationNameLink"
               onclick="addproperty('<fmt:message key="mediator.publishEvent.namespace"/>','<fmt:message
                       key="mediator.publishEvent.propemptyerror"/>','<fmt:message
                       key="mediator.publishEvent.valueemptyerror"/>','correlation')"><fmt:message
                    key="mediator.publishEvent.addProperty"/></a>
        </div>
    </td>
</tr>


<!--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++Payload Attributes++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<tr>
    <td colspan="3"><h4><fmt:message key="mediator.publishEvent.payload.header"/></h4></td>
</tr>

<tr id="payloadpropertytable" style="<%=payloadPropertyTableStyle%>;">
    <td>


        <div style="margin-top:0;">

            <table class="styledInner">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyName"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValue"/></th>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyExp"/></th>
                    <th id="payload-ns-editor-th" style="display:none;" width="15%"><fmt:message
                            key="mediator.publishEvent.nsEditor"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValueType"/></th>
                    <th><fmt:message key="mediator.publishEvent.action"/></th>
                </tr>
                </thead>
                <tbody id="payloadpropertytbody">
                <%
                    i = 0;
                    for (Property mp : mediatorPayloadPropertyList) {
                        if (mp != null) {
                            String value = mp.getValue();
                            String type = mp.getType();
                            String pathValue;
                            SynapseXPath path = mp.getExpression();
                            if (path == null) {
                                pathValue = "";
                            } else {

                                pathValue = path.toString();
                            }
                            boolean isLiteral = value != null && !"".equals(value);
                %>
                <tr id="payloadPropertyRaw<%=i%>">
                    <td><input type="text" name="payloadPropertyName<%=i%>" id="payloadPropertyName<%=i%>"
                               class="esb-edit small_textbox"
                               value="<%=mp.getName()%>"/>
                    </td>
                    <td>
                        <select class="esb-edit small_textbox" name="payloadPropertyTypeSelection<%=i%>"
                                id="payloadPropertyTypeSelection<%=i%>"
                                onchange="onPayloadPropertyTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>', 'payload')">
                            <% if (isLiteral) {%>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <%} else if (path != null) {%>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <%} else { %>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <% }%>
                        </select>
                    </td>
                    <td>
                        <% if (value != null && !"".equals(value)) {%>
                        <input id="payloadPropertyValue<%=i%>" name="payloadPropertyValue<%=i%>" type="text"
                               value="<%=value%>"
                               class="esb-edit"/>
                        <%} else if (path != null) {%>
                        <input id="payloadPropertyValue<%=i%>" name="payloadPropertyValue<%=i%>" type="text"
                               value="<%=pathValue%>" class="esb-edit"/>
                        <%} else { %>
                        <input id="payloadPropertyValue<%=i%>" name="payloadPropertyValue<%=i%>" type="text"
                               class="esb-edit"/>
                        <% }%>
                    </td>
                    <td id="payloadNsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
                        <% if (!isLiteral && path != null) {%>
                        <script type="text/javascript">
                            document.getElementById("payload-ns-editor-th").style.display = "";
                        </script>
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('payloadPropertyValue<%=i%>')">
                            <fmt:message key="mediator.publishEvent.namespace"/></a>
                    </td>
                    <%}%>
                    <td>
                        <select class="esb-edit small_textbox" name="payloadPropertyValueTypeSelection<%=i%>"
                                id="payloadPropertyValueTypeSelection<%=i%>"
                                onchange="onPropertyValueTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>','payload')">

                            <option <% if (type.equals("STRING")) {
                                out.print("selected");
                            } %> value="STRING">
                                <fmt:message key="mediator.publishEvent.type.string"/>
                            </option>
                            <option <% if (type.equals("INTEGER")) {
                                out.print("selected");
                            } %> value="INTEGER">
                                <fmt:message key="mediator.publishEvent.type.integer"/>
                            </option>

                            <option <% if (type.equals("BOOLEAN")) {
                                out.print("selected");
                            } %> value="BOOLEAN">
                                <fmt:message key="mediator.publishEvent.type.boolean"/>
                            </option>

                            <option <% if (type.equals("DOUBLE")) {
                                out.print("selected");
                            } %> value="DOUBLE">
                                <fmt:message key="mediator.publishEvent.type.double"/>
                            </option>
                            <option <% if (type.equals("FLOAT")) {
                                out.print("selected");
                            } %> value="FLOAT">
                                <fmt:message key="mediator.publishEvent.type.float"/>
                            </option>
                            <option <% if (type.equals("LONG")) {
                                out.print("selected");
                            } %> value="LONG">
                                <fmt:message key="mediator.publishEvent.type.long"/>
                            </option>

                        </select>
                    </td>
                    <td><a href="#" class="delete-icon-link"
                           onclick="deletePayloadProperty(<%=i%>);return false;"><fmt:message
                            key="mediator.publishEvent.delete"/></a></td>
                </tr>
                <% }
                    i++;
                } %>
                <input type="hidden" name="payloadPropertyCount" id="payloadPropertyCount" value="<%=i%>"/>
                <script type="text/javascript">
                    if (isRemainPropertyExpressions('payload')) {
                        resetDisplayStyle("", 'payload');
                    }
                </script>
                </tbody>
            </table>
        </div>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:10px;">
            <a name="addPayloadNameLink"></a>
            <a class="add-icon-link"
               href="#addPayloadNameLink"
               onclick="addproperty('<fmt:message key="mediator.publishEvent.namespace"/>','<fmt:message
                       key="mediator.publishEvent.propemptyerror"/>','<fmt:message
                       key="mediator.publishEvent.valueemptyerror"/>','payload')"><fmt:message
                    key="mediator.publishEvent.addProperty"/></a>
        </div>
    </td>
</tr>


<!--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++Arbitrary Attributes++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->

<tr>
    <td colspan="3"><h4><fmt:message key="mediator.publishEvent.arbitrary.header"/></h4></td>
</tr>

<tr id="arbitrarypropertytable" style="<%=arbitraryPropertyTableStyle%>;">
    <td>


        <div style="margin-top:0;">

            <table class="styledInner">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyName"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValue"/></th>
                    <th width="15%"><fmt:message key="mediator.publishEvent.propertyExp"/></th>
                    <th id="arbitrary-ns-editor-th" style="display:none;" width="15%"><fmt:message
                            key="mediator.publishEvent.nsEditor"/></th>
                    <th width="10%"><fmt:message key="mediator.publishEvent.propertyValueType"/></th>
                    <th><fmt:message key="mediator.publishEvent.action"/></th>
                </tr>
                </thead>
                <tbody id="arbitrarypropertytbody">
                <%
                    i = 0;
                    for (Property mp : mediatorArbitraryPropertyList) {
                        if (mp != null) {
                            String value = mp.getValue();
                            String type = mp.getType();
                            String pathValue;
                            SynapseXPath path = mp.getExpression();
                            if (path == null) {
                                pathValue = "";
                            } else {

                                pathValue = path.toString();
                            }
                            boolean isLiteral = value != null && !"".equals(value);
                %>
                <tr id="arbitraryPropertyRaw<%=i%>">
                    <td><input type="text" name="arbitraryPropertyName<%=i%>" id="arbitraryPropertyName<%=i%>"
                               class="esb-edit small_textbox"
                               value="<%=mp.getName()%>"/>
                    </td>
                    <td>
                        <select class="esb-edit small_textbox" name="arbitraryPropertyTypeSelection<%=i%>"
                                id="arbitraryPropertyTypeSelection<%=i%>"
                                onchange="onArbitraryPropertyTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>')">
                            <% if (isLiteral) {%>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <%} else if (path != null) {%>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <%} else { %>
                            <option value="literal">
                                <fmt:message key="mediator.publishEvent.value"/>
                            </option>
                            <option value="expression">
                                <fmt:message key="mediator.publishEvent.expression"/>
                            </option>
                            <% }%>
                        </select>
                    </td>
                    <td>
                        <% if (value != null && !"".equals(value)) {%>
                        <input id="arbitraryPropertyValue<%=i%>" name="arbitraryPropertyValue<%=i%>" type="text"
                               value="<%=value%>"
                               class="esb-edit"/>
                        <%} else if (path != null) {%>
                        <input id="arbitraryPropertyValue<%=i%>" name="arbitraryPropertyValue<%=i%>" type="text"
                               value="<%=pathValue%>" class="esb-edit"/>
                        <%} else { %>
                        <input id="arbitraryPropertyValue<%=i%>" name="arbitraryPropertyValue<%=i%>" type="text"
                               class="esb-edit"/>
                        <% }%>
                    </td>
                    <td id="arbitraryNsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
                        <% if (!isLiteral && path != null) {%>
                        <script type="text/javascript">
                            document.getElementById("arbitrary-ns-editor-th").style.display = "";
                        </script>
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('arbitraryPropertyValue<%=i%>')">
                            <fmt:message key="mediator.publishEvent.namespace"/></a>
                    </td>
                    <%}%>
                    <td>
                        <select class="esb-edit small_textbox" name="arbitraryPropertyValueTypeSelection<%=i%>"
                                id="arbitraryPropertyValueTypeSelection<%=i%>"
                                onchange="onPropertyValueTypeSelectionChange('<%=i%>','<fmt:message
                                        key="mediator.publishEvent.namespace"/>','arbitrary')">

                            <option <% if (type.equals("STRING")) {
                                out.print("selected");
                            } %> value="STRING">
                                <fmt:message key="mediator.publishEvent.type.string"/>
                            </option>

                        </select>
                    </td>
                    <td><a href="#" class="delete-icon-link"
                           onclick="deleteArbitraryProperty(<%=i%>);return false;"><fmt:message
                            key="mediator.publishEvent.delete"/></a></td>
                </tr>
                <% }
                    i++;
                } %>
                <input type="hidden" name="arbitraryPropertyCount" id="arbitraryPropertyCount" value="<%=i%>"/>
                <script type="text/javascript">
                    if (isRemainPropertyExpressions('arbitrary')) {
                        resetDisplayStyle("", 'arbitrary');
                    }
                </script>
                </tbody>
            </table>
        </div>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:10px;">
            <a name="addArbitraryNameLink"></a>
            <a class="add-icon-link"
               href="#addArbitraryNameLink"
               onclick="addproperty('<fmt:message key="mediator.publishEvent.namespace"/>','<fmt:message
                       key="mediator.publishEvent.propemptyerror"/>','<fmt:message
                       key="mediator.publishEvent.valueemptyerror"/>','arbitrary')"><fmt:message
                    key="mediator.publishEvent.addProperty"/></a>
        </div>
    </td>
</tr>

</tbody>
</table>

<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>
<%!
    List<MediatorProperty> convertPropertyList(List<Property> list) {
        List<MediatorProperty> newList = new ArrayList<MediatorProperty>();
        for (Property p : list) {
            newList.add(p);
        }
        return newList;
    }

%>