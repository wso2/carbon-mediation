<%--
  ~  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~  http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.HandlerData" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@ page import="org.wso2.carbon.rest.api.ui.util.RestAPIConstants" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="js/api-util.js">
</script>

<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">

<%  List<HandlerData> handlers = (ArrayList<HandlerData>) session.getAttribute("apiHandlers");
    HandlerData selectedHandler;
    String index = request.getParameter("index");

    if (session.getAttribute("HandlerData") == null) {
        if (!"-1".equals(index)) {
            selectedHandler = handlers.get(Integer.parseInt(index));
        } else {
            index = (String) session.getAttribute("index");
            if ("-1".equals(index)) {
                return;
            }
            selectedHandler = handlers.get(Integer.parseInt(index));
        }
    } else {
        selectedHandler = (HandlerData)session.getAttribute("handlerData");
    }

    session.setAttribute("index", index);
%>

<table class="normal-nopadding" width="100%">
<!-- Methods-->
<tr>
    <td class="leftCol-small">
        <fmt:message key="handler.classpath"/>
    </td>
    <td style="display:inline; vertical-align:middle;">
        <input type="text" name="handler" id="handlerClasspath" value="<%=selectedHandler.getHandler()%>" style="width:300px; margin-top:2px;"/>
    </td>
</tr>
<%  String key= "";
    String value= "";
    String[] properties = selectedHandler.getProperties();
    if (properties != null) {%>
    <table width="100%" class="styledInner">
        <thead>
        <tr>
            <th>Properties</th>
        </tr>
        </thead>
        <tbody>
        <table id="propTable" class="styledLeft" cellspacing="0" cellpadding="0 "style="margin: 10px; width: 90%;">
        <thead>
            <tr>
                <th>Key</th>
                <th>Value</th>
                <th colspan="2">Action</th>
            </tr>
        </thead>
        <%  for(int i = 0; i < properties.length; i++) {
            if (properties[i] != null) {
                String[] entry = properties[i].split(RestAPIConstants.PROPERTY_KEY_VALUE_DELIMITER);
                key = entry[0].trim();
                value = entry[1].trim();
            }
        %>
        <%
        if (properties[0] != null) { %>
        <tr>
            <td style="border: solid 1px #cccccc;"><%=key%></td>
            <td style="border: solid 1px #cccccc;"><%=value%></td>
            <td style="border: solid 1px #cccccc; border-right:none;border-left:none;width:100px">
                <div class="inlineDiv">
                    <div>
                        <a href="#" onclick="editProperty(<%=i%>)" class="icon-link" style="background-image:url(../admin/images/edit.gif);">Edit</a>
                    </div>
                </div>
            </td>
            <td style="border: solid 1px #cccccc; border-left:none;width:100px">
                <div class="inlineDiv">
                    <div>
                        <a href="#" onclick="deleteProperty(<%=i%>)" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>
                    </div>
                </div>
            </td>
        </tr>
        <%}%>
        <%}%>
        </table>
         <div class="sequenceToolbar"
                 style="width:100px;margin-left:2px;" onclick="addProperty()">
                <div>
                    <a class="addChildStyle">Add Property</a>
                </div>
            </div>
            <br/>
        </tbody>
    </table>
    </tr>
    <%}%>
</table>
    <%if (properties == null) {%>
    <div class="sequenceToolbar" style="width:100px;margin-left:2px;" onclick="addProperty()">
        <div>
            <a class="addChildStyle">Add Property</a>
        </div>
    </div>
    <br/>
    <%}%>
<br/>
<input type="button" value="<fmt:message key="update"/>"
       class="button" name="updateBtn" onclick="updateHandler(false)"/>
</fmt:bundle>
