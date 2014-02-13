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

<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.clazz.ClassMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ClassMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ClassMediator classMediator = (ClassMediator) mediator;
    Map propMap = classMediator.getProperties();
    String propertyTableStyle = propMap.isEmpty() ? "display:none;" : "";

%>

<fmt:bundle basename="org.wso2.carbon.mediator.clazz.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.clazz.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="classi18n"/>
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
                            <td><input id="actionID" type="button" value="<fmt:message key="mediator.clazz.LoadClass"/>"
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
                                    <th width="10%"><fmt:message key="mediator.clazz.PropName"/></th>
                                    <th width="10%"><fmt:message key="mediator.clazz.PropValue"/></th>
                                    <th><fmt:message key="mediator.clazz.Action"/></th>
                                </tr>
                                <tbody>
                                    <%
                                        int i = 0;
                                        Set keys = propMap.keySet();
                                        Iterator keyItr = keys.iterator();
                                        while (keyItr.hasNext()) {
                                            Object key = keyItr.next();
                                            Object value = propMap.get(key);
                                    %>
                                    <tr id="propertyRaw<%=i%>">
                                        <td align="left"><input type="hidden" name="propertyName<%=i%>"
                                                                id="propertyName<%=i%>"
                                                                value="<%= key%>"/><%= key%>
                                        </td>
                                        <td><input type="text" name="propertyValue<%=i%>"
                                                   id="propertyValue<%=i%>"
                                                   class="esb-edit small_textbox"
                                                   value="<%= value%>"/>
                                        </td>
                                        <td><a href="#" class="icon-link"
                                               style="background-image:url(../admin/images/delete.gif);"
                                               onclick="deleteRowClazz(this)"><fmt:message
                                                key="mediator.clazz.Delete"/></a></td>
                                    </tr>
                                    <%
                                            i++;
                                        }
                                    %>
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










