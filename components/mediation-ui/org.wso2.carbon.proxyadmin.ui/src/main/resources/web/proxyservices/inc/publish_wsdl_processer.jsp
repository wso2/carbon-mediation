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
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.Entry" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.MalformedURLException" %>
<%
    Object proxyObj = request.getAttribute("proxyDataObject");
    if (proxyObj != null) {
        ProxyData proxy = (ProxyData) proxyObj;
        String wsdlMode = request.getParameter("publishWsdlCombo");
        Object resourcesOnly = request.getAttribute("processResourcesOnly");

        if (!"true".equals(resourcesOnly)) {
            
            if ("inline".equals(wsdlMode)) {
                String inlineTxt = request.getParameter("wsdlInlineText");
                if (inlineTxt != null && !"".equals(inlineTxt)) {
                    inlineTxt = inlineTxt.replaceAll("\n|\\r|\\f|\\t", "");
                    inlineTxt = inlineTxt.replaceAll("> +<", "><");
                    proxy.setWsdlAvailable(true);
                    proxy.setWsdlDef(inlineTxt);
                } else {
                    request.setAttribute("proxyCreationError",
                            "The Inline WSDL content has not been provided");
                }
            } else if ("uri".equals(wsdlMode)) {
                String uri = request.getParameter("wsdlUriText");
                if (uri != null && !"".equals(uri)) {
                    try {
                        URL url = new URL(uri);
                        proxy.setWsdlAvailable(true);
                        proxy.setWsdlURI(url.toString());
                    } catch (MalformedURLException e) {
                        request.setAttribute("proxyCreationError",
                            "The WSDL URI provided is malformed");
                    }

                } else {
                    request.setAttribute("proxyCreationError",
                            "The WSDL URI has not been provided");
                }
            } else if ("reg".equals(wsdlMode)) {
                String registryKey = request.getParameter("wsdlRegText");
                if (registryKey != null && !"".equals(registryKey)) {
                    proxy.setWsdlAvailable(true);
                    proxy.setWsdlKey(registryKey);
                } else {
                    request.setAttribute("proxyCreationError",
                            "The registry key for the WSDL has not been provided");
                }
            } else if ("ep".equals(wsdlMode)){
                String epKey = request.getParameter("wsdlEPText");
                if(epKey != null && !"".equals(epKey)){
                    proxy.setWsdlAvailable(true);
                    proxy.setPublishWSDLEndpoint(epKey);
                }
            }
        }

        if (proxy.getWsdlAvailable() && request.getAttribute("proxyCreationError") == null) {
            String resourceList = request.getParameter("wsdlResourceList");
            if (resourceList != null && !"".equals(resourceList)) {
                String[] resourceValues = resourceList.split("::");
                for (String resourceValue : resourceValues) {
                    Entry resourceEntry = new Entry();
                    int index = resourceValue.indexOf(',');
                    resourceEntry.setKey(resourceValue.substring(0, index));
                    resourceEntry.setValue(resourceValue.substring(index + 1));
                    proxy.addWsdlResources(resourceEntry);
                }
            }
        }
    }
%>