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
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@ page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%
    List<ResourceData> resources = (ArrayList<ResourceData>) session.getAttribute("apiResources");
    String index = request.getParameter("index");
    ResourceData resourceData;

    String forwardTo = "";
    if (session.getAttribute("resource") == null) { // sending to anonSequenceHandler.jsp
        resourceData = (ResourceData) session.getAttribute("resourceData");
        if (resourceData != null) {
            if ("in".equals(request.getParameter("sequence"))) {
                resourceData.setInSequenceKey(null);
            } else if ("out".equals(request.getParameter("sequence"))) {
                resourceData.setOutSequenceKey(null);
            } else if ("fault".equals(request.getParameter("sequence"))) {
                resourceData.setFaultSequenceKey(null);
            }

            String param, op;
            if ((param = request.getParameter("return")) != null) {
                if ((op = request.getParameter("sequence")) != null && !"".equals(op)) {
                    forwardTo = param + "?sequence=" + op + "&index=" + request.getParameter("index");
                }
            }
            forwardTo += "&originator=designToData.jsp&ordinal=1";
            session.setAttribute("resource", resourceData);
        }

    } else {  // returning from anonSequenceHandler.jsp
        resourceData = (ResourceData) session.getAttribute("resource");
        APIData apiData = (APIData) session.getAttribute("apiData");
        if (resourceData != null && apiData != null) {
            apiData.setResources(resources.toArray(new ResourceData[resources.size()]));
            forwardTo = "manageAPI.jsp?" + "mode=" + session.getAttribute("mode") + "&apiName=" + apiData.getName() + "&resourceIndex=" + index;
            ///
            session.setAttribute("resourceData", resourceData);
            session.removeAttribute("resource");
        }
    }
    session.setAttribute("fromSourceView", true);
    session.setAttribute("index", index);
    session.setAttribute("apiResources", resources);
%>
<script type="text/javascript">
    if (window.location.href.indexOf("originator") != -1
            || window.location.href.indexOf("sequence") != -1) {
        window.location.href = '<%=forwardTo%>';
    } else {
        window.location.href = 'manageAPI.jsp';
    }
</script>
