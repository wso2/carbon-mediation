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
<%@page import="org.wso2.carbon.rest.api.ui.util.ApiEditorHelper" %>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%
    String apiSource = request.getParameter("apiSource");
    String resourceSource = request.getParameter("resourceSource");
    String index = request.getParameter("index");

    if (apiSource != null && !"".equals(apiSource.trim())) {
        APIData apiData = ApiEditorHelper.convertStringToAPIData(apiSource);
        if (apiData == null) {
            response.setStatus(455);
            return;
        }
        session.setAttribute("apiData", apiData);
        session.setAttribute("fromApiDataSourceView", "true");
    } else if (resourceSource != null && !"".equals(resourceSource.trim())) {
        ResourceData resourceData = ApiEditorHelper.convertStringToResourceData(resourceSource);
        if (resourceData == null) {
            response.setStatus(455);
            return;
        }
        if (index == null || "".equals(index)) {
            response.setStatus(455);
            return;
        }
        if ("-1".equals(index)) {
            session.setAttribute("resourceData", resourceData);
        } else {
            APIData apiData = (APIData) session.getAttribute("apiData");
            int i = Integer.parseInt(index);
            apiData.getResources()[i] = resourceData;
            List<ResourceData> resourceList = (ArrayList<ResourceData>) session.getAttribute("apiResources");
            resourceList.add(i, resourceData);
        }
        session.setAttribute("fromResourceSourceView", "true");
    }
    session.setAttribute("fromSourceView", "true");
%>
