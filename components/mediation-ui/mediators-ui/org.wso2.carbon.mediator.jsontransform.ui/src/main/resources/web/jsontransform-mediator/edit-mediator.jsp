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
<%@ page import="java.util.List" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof JSONTransformMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    JSONTransformMediator jsonTransformMediator = (JSONTransformMediator) mediator;
    List<MediatorProperty> mediatorPropertyList = jsonTransformMediator.getProperties();
    String propertyTableStyle = mediatorPropertyList.isEmpty() ? "display:none;" : "";
    
    String keyVal = "";
    Value key = jsonTransformMediator.getSchemaKey();
    
    if (key != null) {
        if (key.getKeyValue() != null) {
            keyVal = key.getKeyValue();
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.jsontransform.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.jsontransform.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="logi18n"/>
    <div>
        <script type="text/javascript" src="../jsontransform-mediator/js/mediator-util.js"></script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.jsontransform.header"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                        <tr id="mediator.jsontransform.key.static">
                            <td><fmt:message key="mediator.jsontransform.key"/></td>
                            <td>
                                <input class="longInput" type="text" id="mediator.jsontransform.key.static_val"
                                       name="mediator.jsontransform.key.static_val"
                                       value="<%=keyVal%>" readonly="true"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.jsontransform.key.static_val','/_system/config')"><fmt:message
                                        key="conf.registry.keys"/></a>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('mediator.jsontransform.key.static_val','/_system/governance')"><fmt:message
                                        key="gov.registry.keys"/></a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <h3 class="mediator">
                        <fmt:message key="mediator.jsontransform.properties"/></h3>
                    
                    <div style="margin-top:0px;">
                        
                        <table id="propertytable" style="<%=propertyTableStyle%>;" class="styledInner">
                            <thead>
                            <tr>
                                <th width="15%"><fmt:message key="mediator.jsontransform.propertyName"/></th>
                                <th width="15%"><fmt:message key="mediator.jsontransform.propertyValue"/></th>
                                <th><fmt:message key="mediator.jsontransform.action"/></th>
                            </tr>
                            <tbody id="propertytbody">
                            <%
                                int i = 0;
                                for (MediatorProperty mp : mediatorPropertyList) {
                                    if (mp != null) {
                                        String value = mp.getValue();
                            %>
                            <tr id="propertyRaw<%=i%>">
                                <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                                           class="esb-edit small_textbox"
                                           value="<%=mp.getName()%>"/>
                                </td>
                                <td>
                                    <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                                           value="<%=value%>"
                                           class="esb-edit"/>
                                </td>
                                <td><a href="#" class="delete-icon-link"
                                       onclick="deleteproperty(<%=i%>);return false;"><fmt:message
                                        key="mediator.jsontransform.delete"/></a></td>
                            </tr>
                            <% }
                                i++;
                            } %>
                            <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
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
                           onclick="addproperty('<fmt:message key="mediator.jsontransform.propemptyerror"/>',
                                   '<fmt:message key="mediator.jsontransform.valueemptyerror"/>')"><fmt:message
                                key="mediator.jsontransform.addProperty"/></a>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</fmt:bundle>