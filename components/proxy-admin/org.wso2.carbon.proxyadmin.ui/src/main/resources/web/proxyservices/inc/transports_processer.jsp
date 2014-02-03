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
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%
    Object proxyObj = request.getAttribute("proxyDataObject");
    if (proxyObj != null) {
        ProxyData proxy = (ProxyData) proxyObj;
        Enumeration<String> params = request.getParameterNames();
        String prefix = "trp__";
        boolean transportsFound = false;
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            if (param.startsWith(prefix)) {
                proxy.addTransports(param.substring(prefix.length()));
                transportsFound = true;
            }
        }

        if (!transportsFound) {
            request.setAttribute("proxyCreationError",
                        "At least one transport must be specified for a proxy service");   
        }
    }
%>