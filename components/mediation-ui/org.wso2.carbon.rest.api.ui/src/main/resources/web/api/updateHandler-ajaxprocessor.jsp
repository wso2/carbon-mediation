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
<%@page import="java.util.ArrayList"%>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.HandlerData"%>
<%@page import="java.util.List"%>
<%@ page import="org.wso2.carbon.rest.api.ui.util.RestAPIConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    int index = Integer.parseInt(request.getParameter("index"));
    List<HandlerData> handlerList = (ArrayList<HandlerData>) session.getAttribute("apiHandlers");
    HandlerData data = (HandlerData) session.getAttribute("handlerData");
    String propertyIndex = (String) session.getAttribute("propertyIndex");
    if (data == null) {
        data = new HandlerData();
    }
    String isProperty = request.getParameter("isProperty");
    if(isProperty != null && isProperty.equals("true")) {
        HandlerData handler = handlerList.get(index);
        String[] oldProperties = handler.getProperties();
        if (oldProperties == null) {
            oldProperties = new String[0];
        }
        String[] newProperties = new String[oldProperties.length + 1];
        String propertyKey = request.getParameter("propertyKey");
        String propertyVal = request.getParameter("propertyVal");
        if (propertyKey != null && !propertyKey.isEmpty() && propertyVal != null && !propertyVal.isEmpty()) {
            int i = 0;
            if (propertyIndex != null) {
                newProperties = oldProperties;
                newProperties[Integer.parseInt(propertyIndex)] = propertyKey +
                        RestAPIConstants.PROPERTY_KEY_VALUE_DELIMITER + propertyVal;
            } else {
                for (String prop : oldProperties) {
                    newProperties[i] = prop;
                    i++;
                }
                newProperties[i] = propertyKey + RestAPIConstants.PROPERTY_KEY_VALUE_DELIMITER + propertyVal;
            }
        }
        data.setProperties(newProperties);
    }
    String classPath = request.getParameter("classPath");
    if (classPath != null) {
        data.setHandler(classPath);
    }
    String mode = (String) session.getAttribute("mode");
    if (mode != null && !"".equals(mode)) {
        if ("add".equals(mode)) {
            String name = request.getParameter("apiName");
            String context = request.getParameter("apiContext");
            String hostname = request.getParameter("apiHostname");
            String sPort = request.getParameter("apiPort");
            String version = request.getParameter("version");
            String versionType = request.getParameter("versionType");
            APIData apiData = (APIData) session.getAttribute("apiData");
            if (apiData != null) {
                apiData.setName(name);
                apiData.setContext(context);
                apiData.setHost(hostname);
                apiData.setVersion(version);
                apiData.setVersionType(versionType);
                int iPort = -1;
                if(null != sPort && !"".equals(sPort)) {
                    try {
                    	iPort = Integer.valueOf(sPort);
                    } catch(NumberFormatException nfe) {
                    	log("Invalid port",nfe);
                    }
                }
                apiData.setPort(iPort);
            }
        }
    }

    if (index == -1) {
        handlerList.add(data);
    } else {
        if (handlerList.isEmpty()) {
            handlerList = new ArrayList<HandlerData>();
            handlerList.add(data);
        } else {
            handlerList.set(index, data);
        }
    }
    session.setAttribute("apiHandlers", handlerList);
    /// finally this is a real update.
    APIData apiData = (APIData) session.getAttribute("apiData");
    apiData.setHandlers(handlerList.toArray(new HandlerData[handlerList.size()]));
    session.removeAttribute("index");
    String size = String.valueOf(handlerList.size());
    request.getSession().removeAttribute("handlerData");
%>
<input id="handlersSize" name="handlersSize" type="hidden" value="<%=size%>"/>
