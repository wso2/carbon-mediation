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
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ page import="java.net.URI" %>
<%@ page import="java.net.URISyntaxException" %>

<%
    Object proxyObj = request.getAttribute("proxyDataObject");
    if (proxyObj != null) {
        ProxyData proxy = (ProxyData) proxyObj;
        String mode = request.getParameter("targetEndpointMode");
        if ("url".equals(mode)) {
            String targetURL = request.getParameter("targetURL");
            if (targetURL != null && !"".equals(targetURL)) {
                try {
                	targetURL = targetURL.replaceAll("&", "&amp;");
                    URI url = new URI(targetURL);
                    proxy.setEndpointXML("<endpoint xmlns=\"http://ws.apache.org/ns/synapse\"><address uri=\"" + url.toString() + "\"/></endpoint>");
                } catch (URISyntaxException e) {
                    request.setAttribute("proxyCreationError", "The target URL provided is malformed");
                }
            } else {
                request.setAttribute("proxyCreationError", "The target URL has not been provided");
            }
            
        } else if ("predef".equals(mode)) {
            String endpointName = request.getParameter("predefEndpoint");
            if (endpointName != null && !"".equals(endpointName)) {
                proxy.setEndpointKey(endpointName);
            } else {
                request.setAttribute("proxyCreationError", "The target endpoint name is not provided");
            }

        } else if ("reg".equals(mode)) {
            String regKey = request.getParameter("endpointRegKey");
            if (regKey != null && !"".equals(regKey)) {
                proxy.setEndpointKey(regKey);
            } else {
                request.setAttribute("proxyCreationError", "The registry key for the dynamic endpoint is not provided");
            }

        }
    }
%>