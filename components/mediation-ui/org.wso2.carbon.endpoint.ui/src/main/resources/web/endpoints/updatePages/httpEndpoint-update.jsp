<%--
~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.TemplateSerializer" %>
<%@ page import="org.apache.synapse.endpoints.Template" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.http.HttpEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>

<%
    boolean isEditingListEndpoint = session.getAttribute("isEditingListEndpoint") != null ? true : false;
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;

    String endpointName = request.getParameter("endpointName");
    String uriTemplate = request.getParameter("uriTemplate");
    String methodOption = request.getParameter("httpMethod");
  
    String description = request.getParameter("endpointDescription").trim();
    String errorCode = request.getParameter("suspendErrorCode");
    String suspendDuration = request.getParameter("suspendDuration");
    String suspendMaxDuration = request.getParameter("suspendMaxDuration");
    String factor = request.getParameter("factor");
    String retryErrorCode = request.getParameter("retryErroCode");
    String retriesOnTimeOut = request.getParameter("retryTimeOut");
    String retryDelay = request.getParameter("retryDelay");
    String disabledErrorCodes = request.getParameter("disabledErrorCodes");
    String action = request.getParameter("actionSelect");
    String actionDuration = null;

    String clientId = request.getParameter("clientId");
    String clientSecret = request.getParameter("clientSecret");
    String authMode = request.getParameter("authMode");
    String refreshToken = request.getParameter("refreshToken");
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String tokenURL = request.getParameter("tokenURL");
    String requestParametersMap = request.getParameter("requestParametersMap");

    String basicAuthUsername = request.getParameter("basicAuthUsername");
    String basicAuthPassword = request.getParameter("basicAuthPassword");

    if (action != null && !action.equals("neverTimeout")) {
        actionDuration = request.getParameter("actionDuration");
    }
    
    String properties = request.getParameter("endpointProperties");

    HttpEndpoint httpEndpoint = new HttpEndpoint();

    if (isEditingListEndpoint) {
        httpEndpoint = (HttpEndpoint) ListEndpointDesignerHelper.getEditingEndpoint(request, session);
    }
    if (endpointName != null) {
    	httpEndpoint.setEndpointName(endpointName);
    }
    if (uriTemplate != null) {
    	httpEndpoint.setUriTemplate(uriTemplate);
    }
    if ("get".equals(methodOption)) {
    	httpEndpoint.setHttpGet(true);
    } else if ("post".equals(methodOption)) {
    	httpEndpoint.setHttpPost(true);
    } else if ("patch".equals(methodOption)) {
    	httpEndpoint.setHttpPatch(true);
    } else if ("put".equals(methodOption)) {
    	httpEndpoint.setHttpPut(true);
    } else if ("delete".equals(methodOption)) {
        httpEndpoint.setHttpDelete(true);
    } else if ("head".equals(methodOption)) {
    	httpEndpoint.setHttpHead(true);
    } else if ("options".equals(methodOption)) {
        httpEndpoint.setHttpOptions(true);
    } else {
    	httpEndpoint.setHttpGet(false);
    	httpEndpoint.setHttpPost(false);
    	httpEndpoint.setHttpPatch(false);
    	httpEndpoint.setHttpPut(false);
    	httpEndpoint.setHttpDelete(false);
    	httpEndpoint.setHttpHead(false);
    	httpEndpoint.setHttpOptions(false);
    }

    if (description != null && !"".equals(description)) {
    	httpEndpoint.setDescription(description);
    } else {
    	httpEndpoint.setDescription("");
    }
    if (errorCode != null) {
    	httpEndpoint.setErrorCodes(errorCode);
    }
    if (suspendDuration != null) {
    	httpEndpoint.setSuspendDurationOnFailure(suspendDuration);
    }
    if (suspendMaxDuration != null) {
    	httpEndpoint.setMaxSusDuration(suspendMaxDuration);
    }
    if (factor != null) {
    	httpEndpoint.setSusProgFactor(factor);
    }
    if (retryErrorCode != null) {
    	httpEndpoint.setTimedOutErrorCodes(retryErrorCode);
    }
    if (retriesOnTimeOut != null) {
    	httpEndpoint.setRetryTimeout(retriesOnTimeOut);
    }
    if (retryDelay != null) {
    	httpEndpoint.setRetryDelay(retryDelay);
    	httpEndpoint.setRetryDelay(retryDelay);
    }
    if (disabledErrorCodes != null) {
    	httpEndpoint.setRetryDisabledErrorCodes(disabledErrorCodes);
    }
    if (action != null) {
        if (action.equals("discardMessage")) {
        	httpEndpoint.setTimeoutAction("discard");
        } else if (action.equals("executeFaultSequence")) {
        	httpEndpoint.setTimeoutAction("fault");
        } else {
        	httpEndpoint.setTimeoutAction("100");
        }
    }
    if (actionDuration != null) {
    	httpEndpoint.setTimeoutActionDur(actionDuration);
    }
    if (properties != null) {
    	httpEndpoint.setProperties(properties);
    } else {
    	httpEndpoint.setProperties(null);
    }

    if (clientId != null) {
        httpEndpoint.setClientId(clientId);
    }
    if (clientSecret != null) {
        httpEndpoint.setClientSecret(clientSecret);
    }
    if (authMode != null && !authMode.equals("null")) {
        httpEndpoint.setAuthMode(authMode);
    }
    if (refreshToken != null) {
        httpEndpoint.setRefreshToken(refreshToken);
    }
    if (username != null) {
        httpEndpoint.setUsername(username);
    }
    if (password != null) {
        httpEndpoint.setPassword(password);
    }
    if (tokenURL != null) {
        httpEndpoint.setTokenURL(tokenURL);
    }
    if (requestParametersMap !=null && requestParametersMap.length() != 0 && !requestParametersMap.equals("null")) {
       // remove enclosing braces
       requestParametersMap = requestParametersMap.substring( 1, requestParametersMap.length() - 1 );
       Map<String, String> map = Arrays.stream(requestParametersMap.split(","))
          .map(entry -> entry.split("="))
          .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
       httpEndpoint.setRequestParametersMap(map);
    }

    if (basicAuthUsername != null) {
        httpEndpoint.setBasicAuthUsername(basicAuthUsername);
    }
    if (basicAuthPassword != null) {
        httpEndpoint.setBasicAuthPassword(basicAuthPassword);
    }

    OMElement endpointElement = httpEndpoint.serialize(null);
    String epConfig = endpointElement.toString();
    String configuration = null;

    if (isFromTemplateEditor) {
        String templateName = request.getParameter("templateName");
        Template template = new Template();
        if (templateName != null) {
            template.setName(templateName);
        }
        if (epConfig != null) {
            template.setElement(endpointElement);
        } else {
            template.setElement(httpEndpoint.getEmptyEndpointElement());
        }

        String paramCount = request.getParameter("propertyCount");
        int count = Integer.parseInt(paramCount);
        for (int i = 0; i < count; i++) {
            String paramName = request.getParameter("propertyName" + i);
            if (paramName != null && !"".equals(paramName.trim()) &&
                (!"name".equals(paramName.trim()) && !"uri".equals(paramName.trim()))) {
                template.addParameter(paramName);
            }
        }

        OMElement serializedTemplateElement = new TemplateSerializer().serializeEndpointTemplate(template,
                                                                                            null);
        configuration = serializedTemplateElement.toString();
    } else {
        configuration = epConfig;
    }

    if (!isEditingListEndpoint) {
        session.setAttribute("endpointConfiguration", configuration);
    }

%>
