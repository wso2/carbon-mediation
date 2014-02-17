<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.apache.synapse.mediators.Value" %>
<%@page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.mediator.fastXSLT.FastXSLTMediator" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.fastXSLT.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.fastXSLT.i18n.JSResources"
        request="<%=request%>" i18nObjectName="fastXSLTjsi18n"/>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof FastXSLTMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    FastXSLTMediator fastXSLTMediator = (FastXSLTMediator) mediator;

    boolean isKeyDynamic = false;
    String keyVal = "";

    Value key = fastXSLTMediator.getXsltKey();

    if (key != null) {

        if (key.getKeyValue() != null) {
            isKeyDynamic = false;
            keyVal = key.getKeyValue();
        } else if (key.getExpression() != null) {
            isKeyDynamic = true;
            keyVal = key.getExpression().toString();
            NameSpacesRegistrar nameSpacesRegistrarKey = NameSpacesRegistrar.getInstance();
            nameSpacesRegistrarKey.registerNameSpaces(key.getExpression(), "mediator.fastXSLT.key.dynamic_val", session);
        }

    }
    SynapseXPath sourceXPath = fastXSLTMediator.getSource();
    String source = "";
    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    if (sourceXPath != null) {
        nameSpacesRegistrar.registerNameSpaces(sourceXPath, "mediator.fastXSLT.source", session);
        source = sourceXPath.toString();
    }
    List<MediatorProperty> mediatorPropertyList = fastXSLTMediator.getProperties();
    nameSpacesRegistrar.registerNameSpaces(mediatorPropertyList, "propertyValue", session);
    List<MediatorProperty> featureList = fastXSLTMediator.getFeatures();
    String propertyTableStyle = mediatorPropertyList.isEmpty() ? "display:none;" : "";
    String featureTableStyle = featureList.isEmpty() ? "display:none;" : "";
    Map<String, String> resources = fastXSLTMediator.getResources();
%>
<div>
<script type="text/javascript" src="../fastXSLT-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="fastXSLT.mediator.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">

            <tr>
                <td>
                    <fmt:message key="fastXSLT.key.type"/> :
                </td>
                <td>
                    <input type="radio"
                           onclick="javascript:displayElement('mediator.fastXSLT.key.dynamic', false); javascript:displayElement('mediator.fastXSLT.key.static', true); displayElement('mediator.fastXSLT.key.namespace.editor', false);"
                           name="keygroup" <%=!isKeyDynamic ? "checked=\"checked\" value=\"StaticKey\"" : "value=\"StaticKey\""%>/>
                    <fmt:message key="fastXSLT.key.static"/>
                    <input type="radio" id="keyGroupDynamic"
                           onclick="javascript:displayElement('mediator.fastXSLT.key.dynamic', true); displayElement('mediator.fastXSLT.key.namespace.editor', true); displayElement('mediator.fastXSLT.key.static', false);"
                           name="keygroup" <%=isKeyDynamic ? "checked=\"checked\" value=\"DynamicKey\"" : "value=\"DynamicKey\""%>/>
                    <fmt:message key="fastXSLT.key.dynamic"/>
                </td>
                <td></td>
            </tr>
            <tr id="mediator.fastXSLT.key.static" <%=isKeyDynamic ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.fastXSLT.key"/><span class="required">*</span></td>
                <td>
                    <input class="longInput" type="text" id="mediator.fastXSLT.key.static_val"
                           name="mediator.fastXSLT.key.static_val"
                           value="<%=keyVal%>" readonly="true"/>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('mediator.fastXSLT.key.static_val','/_system/config')"><fmt:message
                            key="conf.registry.keys"/></a>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('mediator.fastXSLT.key.static_val','/_system/governance')"><fmt:message
                            key="gov.registry.keys"/></a>
                </td>
            </tr>
            <tr id="mediator.fastXSLT.key.dynamic" <%=!isKeyDynamic ? "style=\"display:none\";" : ""%>>
                <td><fmt:message key="mediator.fastXSLT.key"/><span class="required">*</span></td>
                <td><input class="longInput" type="text" name="mediator.fastXSLT.key.dynamic_val"
                           id="mediator.fastXSLT.key.dynamic_val"
                           value="<%=keyVal%>"/></td>
                <td><a id="mediator.fastXSLT.key.dynamic_nmsp_button" href="#nsEditorLink"
                       class="nseditor-icon-link" style="padding-left:40px"
                       onclick="showNameSpaceEditor('mediator.fastXSLT.key.dynamic_val')">

                    <fmt:message key="namespaces"/></a>
                </td>
            </tr>

            <tr>
                <td>
                    <fmt:message key="mediator.fastXSLT.source"/>
                </td>
                <td>
                    <input class="longInput" type="text" id="mediator.fastXSLT.source" name="mediator.fastXSLT.source"
                           value="<%=source%>"/>
                </td>
                <td>
                    <a href="#nsEditorLink" class="nseditor-icon-link"
                       style="padding-left:40px"
                       onclick="showNameSpaceEditor('mediator.fastXSLT.source')"><fmt:message
                            key="namespaces"/></a>
                </td>
            </tr>
        </table>

    </td>
</tr>
<tr>
    <td>
        <h3 class="mediator">
            <fmt:message key="properties"/></h3>

        <div style="margin-top:0px;">
            <table id="propertytable" style="<%=propertyTableStyle%>;" class="styledInner">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="th.property.name"/></th>
                    <th width="10%"><fmt:message key="th.property.type"/></th>
                    <th width="15%"><fmt:message key="th.value.expression"/></th>
                    <th id="ns-edior-th" style="display:none;" width="15%"><fmt:message key="namespaceeditor"/></th>
                    <th><fmt:message key="th.action"/></th>
                </tr>
                <tbody id="propertytbody">
                <%
                    int i = 0;
                    for (MediatorProperty mp : mediatorPropertyList) {
                        if (mp != null) {
                            String value = mp.getValue();
                            SynapseXPath synapseXPath = mp.getExpression();
                            boolean isLiteral = value != null && !"".equals(value);
                %>
                <tr id="propertyRaw<%=i%>">
                    <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                               value="<%=mp.getName()%>"/>
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
                        <% if (value != null && !"".equals(value)) {%>
                        <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                               value="<%=value%>"
                                />
                        <%} else if (synapseXPath != null) {%>
                        <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                               value="<%=synapseXPath.toString()%>"/>
                        <%} else { %>
                        <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"/>
                        <% }%>
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


                    <td><a href="#" class="delete-icon-link"
                           onclick="deleteproperty('<%=i%>');return false;"><fmt:message
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
<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addNameLink"></a>
            <a class="add-icon-link"
               href="#addNameLink"
               onclick="addproperty('<fmt:message key="namespaces"/>','<fmt:message key="nameemptyerror"/>','<fmt:message key="valueemptyerror"/>')"><fmt:message
                    key="add.property"/></a>
        </div>
    </td>
</tr>
<tr>
    <td>
        <h3 class="mediator"><fmt:message key="features"/></h3>

        <div style="margin-top:0px;">
            <table id="featuretable" style="<%=featureTableStyle%>;" class="styledInner">
                <thead>
                <tr>
                    <th width="15%"><fmt:message key="th.feature.name"/></th>
                    <th width="10%"><fmt:message key="th.feature.value"/></th>
                    <th><fmt:message key="th.action"/></th>
                </tr>
                <tbody id="featuretbody">
                <%
                    int k = 0;
                    for (MediatorProperty property : featureList) {
                        if (property != null) {
                            String value = property.getValue();
                            boolean isTrue = value != null && Boolean.valueOf(value.trim());
                %>
                <tr id="featureRaw<%=k%>">
                    <td><input type="text" name="featureName<%=k%>" id="featureName<%=k%>"

                               value="<%=property.getName()%>"/>
                    </td>
                    <td>
                        <select name="featureValue<%=k%>"
                                id="featureValue<%=k%>">
                            <% if (!isTrue) {%>
                            <option value="false" selected="selected">
                                <fmt:message key="false"/>
                            </option>
                            <option value="true">
                                <fmt:message key="true"/>
                            </option>
                            <%} else { %>
                            <option value="true" selected="selected">
                                <fmt:message key="true"/>
                            </option>
                            <option value="false">
                                <fmt:message key="false"/>
                            </option>
                            <% }%>
                        </select>
                    </td>
                    <td><a href="#" href="#" class="delete-icon-link"
                           onclick="deletefeature('<%=k%>')"><fmt:message key="delete"/></a></td>
                </tr>
                <% }
                    k++;
                } %>
                <input type="hidden" name="featureCount" id="featureCount" value="<%=k%>"/>
                </tbody>
                </thead>
            </table>
        </div>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addFeatureLink"></a>
            <a class="add-icon-link"
               href="#addFeatureLink"
               onclick="addfeature('<fmt:message key="nameemptyerror.feature"/>')">
                <fmt:message key="add.feature"/></a>
        </div>
    </td>
</tr>
<tr id="resourceTr">
    <td><input type="hidden" id="resourceList" name="resourceList"/>

        <h3 class="mediator"><fmt:message key="fastXSLT.mediator.resources"/></h3>

        <div id="resourceAdd">
            <table class="normal-nopadding" cellspacing="0">
                <tr>
                    <td class="nopadding">
                        <table>
                            <tr>
                                <td class="nopadding"><fmt:message
                                        key="fastXSLT.mediator.resource.location"/> <input type="text"
                                                                                       id="locationText"
                                                                                       name="locationText"/></td>
                                <td class="nopadding"><fmt:message
                                        key="fastXSLT.mediator.resource.key"/> <input type="text"
                                                                                  readonly="readonly" value=""
                                                                                  id="resourceKey"
                                                                                  name="resourceKey"/></td>
                                <td class="nopadding" style="padding-top: 10px !important">
                                    <a href="#" class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('resourceKey','/_system/config');"><fmt:message
                                            key="conf.registry.keys"/></a></td>
                                <td class="nopadding" style="padding-top: 10px !important">
                                    <a href="#" class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('resourceKey','/_system/governance');"><fmt:message
                                            key="gov.registry.keys"/></a></td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="nopadding"><a class="icon-link"
                                             href="#addNameLink" onclick="addResources();"
                                             style="background-image: url(../admin/images/add.gif);"><fmt:message
                            key="fastXSLT.mediator.resource.add"/> </a></td>
                </tr>
            </table>
        </div>
        <div>
            <table cellpadding="0" cellspacing="0" border="0"
                   class="styledLeft" id="resourceTable" style="display: none;">
                <thead>
                <tr>
                    <th style="width: 40%"><fmt:message key="fastXSLT.mediator.resource.location"/></th>
                    <th style="width: 40%"><fmt:message key="fastXSLT.mediator.resource.key"/></th>
                    <th style="width: 20%"><fmt:message key="fastXSLT.mediator.resource.action"/></th>
                </tr>
                </thead>
                <tbody/>
                        <%
					Iterator itr = resources.keySet().iterator();
					if(itr.hasNext()){
						%>
                <table cellpadding="0" cellspacing="0" border="0"
                       class="styledLeft" id="resourceTable2">
                    <thead>
                    <tr>
                        <th style="width: 40%"><fmt:message key="fastXSLT.mediator.resource.location"/></th>
                        <th style="width: 40%"><fmt:message key="fastXSLT.mediator.resource.key"/></th>
                    </tr>
                    </thead>
                    <%
                        for (itr = resources.keySet().iterator(); itr.hasNext(); ) {
                            int j = 0;
                            String location = (String) itr.next();
                            if (location == null) {
                                location = "";
                            }

                            String regKey = resources.get(location);
                            if (regKey == null) {
                                regKey = "";
                            }

                    %>
                    <tbody>
                    <tr>
                        <td class="nopadding"><%= location%>
                        </td>
                        <td class="nopadding"><%= regKey%>
                        </td>
                    </tr>
                    </tbody>
                    <%
                                j++;
                            }
                        }
                    %>
                </table>
        </div>
    </td>
</tr>
</table>
<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>

<a name="registryBrowserLink"></a>

<div id="registryBrowser" style="display:none;"></div>
</div>
</fmt:bundle> 