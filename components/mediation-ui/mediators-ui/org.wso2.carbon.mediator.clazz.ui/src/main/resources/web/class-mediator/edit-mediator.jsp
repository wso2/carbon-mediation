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

<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.jaxen.JaxenException" %>
<%@ page import="org.wso2.carbon.mediator.clazz.ClassMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ClassMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ClassMediator classMediator = (ClassMediator) mediator;
   List<MediatorProperty> mediatorPropertyList = classMediator.getProperties();
   NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
       nameSpacesRegistrar.registerNameSpaces(mediatorPropertyList, "propertyValue", session);
    String propertyTableStyle = mediatorPropertyList.isEmpty() ? "display:none;" : "";

%>

<fmt:bundle basename="org.wso2.carbon.mediator.clazz.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.clazz.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="clazzi18n"/>
    <div>

        <script type="text/javascript" src="../class-mediator/js/mediator-util.js"></script>
        <script type="text/javascript">
            var val;
            jQuery('#actionID').click(function() {
                val = document.getElementById('mediatorInputId').value;
                var url = '../class-mediator/class-ajaxprocessor.jsp';
                jQuery('#attribDescription').load(url, {mediatorInput: val},
                        function(res, status, t) {
                            if (status != "success") {
                                CARBON.showErrorDialog('<fmt:message key="mediator.clazz.errmsg"/>');
                            }
                        })
                return false;
            });

        </script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.clazz.header"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td><fmt:message key="mediator.clazz.className"/><span class="required">*</span>
                            </td>
                            <td align="left"><input type="text" id="mediatorInputId" name="mediatorInput" size="40"
                                                    value="<%= classMediator.getMediator()!=null?classMediator.getMediator():""%>"/>&nbsp&nbsp&nbsp
                            </td>
                            <td><input id="actionID" type="button" value="<fmt:message key="mediator.clazz.loadClass"/>"
                                       class="button"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <div class="Mediator" id="attribDescription">
                            <%--<p id="propertyLabel" style="<%=propertyTableStyle%>"><fmt:message key="properties.defined.for.class.mediator"/></p>--%>
                        <h3 class="mediator" id="propertyLabel" style="<%=propertyTableStyle%>"><fmt:message
                                key="properties.defined.for.class.mediator"/></h3>

                            <%--<div style="margin-top:0px;">--%>
                            <table id="propertytable" style="<%=propertyTableStyle%>;" class="styledInner">
                            <thead>
                                <tr>
                                    <th width="15%"><fmt:message key="mediator.clazz.propName"/></th>
                                    <th width="10%"><fmt:message key="mediator.clazz.propValue"/></th>
                                    <th width="15%"><fmt:message key="mediator.clazz.propertyExp"/></th>
                                    <th id="ns-edior-th" style="display:none;" width="15%"><fmt:message
                                            key="mediator.clazz.nsEditor"/></th>
                                    <th><fmt:message key="mediator.clazz.action"/></th>
                                </tr>
                                <tbody>
                                    <%
                                        int i = 0;
                                        for (MediatorProperty mp : mediatorPropertyList) {
                                            if (mp != null) {
                                                String value = mp.getValue();
                                                boolean isLiteral = value != null && !value.isEmpty();

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
                                                    onchange="onPropertyTypeSelectionChange('<%=i%>','<fmt:message key="mediator.clazz.namespace"/>')">
                                                <% if (isLiteral) {%>
                                                <option value="literal">
                                                    <fmt:message key="mediator.clazz.value"/>
                                                </option>
                                                <option value="expression">
                                                    <fmt:message key="mediator.clazz.expression"/>
                                                </option>
                                                <%} else if (path != null) {%>
                                                <option value="expression">
                                                    <fmt:message key="mediator.clazz.expression"/>
                                                </option>
                                                <option value="literal">
                                                    <fmt:message key="mediator.clazz.value"/>
                                                </option>
                                                <%} else { %>
                                                <option value="literal">
                                                    <fmt:message key="mediator.clazz.value"/>
                                                </option>
                                                <option value="expression">
                                                    <fmt:message key="mediator.clazz.expression"/>
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
                                                <fmt:message key="mediator.clazz.namespace"/></a>
                                        </td>
                                        <%}%>
                                        <td><a href="#" class="delete-icon-link"
                                               onclick="deleteproperty(<%=i%>);return false;"><fmt:message
                                                key="mediator.clazz.delete"/></a></td>
                                    </tr>
                                    <% }
                                        i++;
                                    } %>
                                    <input type="hidden" name="propertyCount" id="propertyCount"
                                           value="<%=i%>"/>
                                </tbody>
                            </thead>
                        </table>
                        <!--</div>-->
                    </div>
                </td>
            </tr>
        </table>
    </div>
</fmt:bundle>










