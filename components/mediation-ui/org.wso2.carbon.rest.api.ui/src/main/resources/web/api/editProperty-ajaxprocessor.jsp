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
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.HandlerData" %>
<%@ page import="org.wso2.carbon.rest.api.ui.util.RestAPIConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    int index = Integer.parseInt(request.getParameter("index"));
    int propertyIndex = Integer.parseInt(request.getParameter("propIndex"));
    List<HandlerData> handlerList = (ArrayList<HandlerData>) session.getAttribute("apiHandlers");
    HandlerData handler = handlerList.get(index);
    String property = handler.getProperties()[propertyIndex];
    String propertyKey = property.split(RestAPIConstants.PROPERTY_KEY_VALUE_DELIMITER)[0];
    String propertyVal = property.split(RestAPIConstants.PROPERTY_KEY_VALUE_DELIMITER)[1];
    session.setAttribute("propertyIndex", Integer.toString(propertyIndex));
%>

<fmt:bundle basename="org.wso2.carbon.rest.api.ui.i18n.Resources">


<table class="normal-nopadding" width="100%">
<!-- Methods-->
<tr>
    <td style="display:inline; vertical-align:middle;">
        <fmt:message key="handler.property.name"/>
        <input type="text" id="propertyKey" value="<%=propertyKey%>" style="width:300px; margin-top:2px;"/>
    </td>
    <td style="display:inline; vertical-align:middle;">
        <fmt:message key="handler.property.value"/>
        <input type="text" id="propertyVal" value="<%=propertyVal%>" style="width:300px; margin-top:2px;"/>
    </td>
</tr>
</table>
<br/>
<input type="button" value="<fmt:message key="update"/>"
       class="button" name="updateBtn" onclick="updateHandler(true)"/>
</fmt:bundle>
