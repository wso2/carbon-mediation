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
<%@ page import="org.apache.synapse.config.xml.endpoints.TemplateSerializer" %>
<%@ page import="org.apache.synapse.endpoints.Template" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.defaultendpoint.DefaultEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>

<%
    boolean isEditingListEndpoint = session.getAttribute("isEditingListEndpoint") != null ? true : false;
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;

    String endpointName = request.getParameter("endpointName");
    String formatOption = request.getParameter("format");
    String optimizeOption = request.getParameter("optimize");
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
    if (action != null && !action.equals("neverTimeout")) {
        actionDuration = request.getParameter("actionDuration");
    }
    String wsAddressing = request.getParameter("wsAddressing");
    String useSeprateListner = null;
    if (wsAddressing != null) {
        useSeprateListner = request.getParameter("sepListener");
    }
    String wsSecurity = request.getParameter("wsSecurity");
    String secPolicy = null;
    if (wsSecurity != null) {
        secPolicy = request.getParameter("wsSecPolicyKeyID");
    }
    String wsRM = request.getParameter("wsRM");
    String rmPolicy = null;
    if (wsRM != null) {
        rmPolicy = request.getParameter("wsrmPolicyKeyID");
    }
    String properties = request.getParameter("endpointProperties");

    DefaultEndpoint defaultEndpoint = new DefaultEndpoint();

    if (isEditingListEndpoint) {
        defaultEndpoint = (DefaultEndpoint) ListEndpointDesignerHelper.getEditingEndpoint(request, session);
    }
    if (endpointName != null) {
        defaultEndpoint.setEpName(endpointName);
    }
    if ("soap11".equals(formatOption)) {
        defaultEndpoint.setSoap11(true);
    } else if ("soap12".equals(formatOption)) {
        defaultEndpoint.setSoap12(true);
    } else if ("POX".equals(formatOption)) {
        defaultEndpoint.setPox(true);
    } else if ("REST".equals(formatOption)) {
        defaultEndpoint.setRest(true);
    } else if ("GET".equals(formatOption)) {
        defaultEndpoint.setGet(true);
    } else {
        defaultEndpoint.setSoap11(false);
        defaultEndpoint.setSoap12(false);
        defaultEndpoint.setPox(false);
        defaultEndpoint.setRest(false);
        defaultEndpoint.setGet(false);
    }

    if ("SWA".equals(optimizeOption)) {
        defaultEndpoint.setSwa(true);
    } else if ("MTOM".equals(optimizeOption)) {
        defaultEndpoint.setMtom(true);
    }else {
        defaultEndpoint.setSwa(false);
        defaultEndpoint.setMtom(false);
    }

    if (description != null && !"".equals(description)) {
        defaultEndpoint.setDescription(description);
    } else {
        defaultEndpoint.setDescription("");
    }
    if (errorCode != null) {
        defaultEndpoint.setErrorCodes(errorCode);
    }
    if (suspendDuration != null) {
        defaultEndpoint.setSuspendDurationOnFailure(suspendDuration);
    }
    if (suspendMaxDuration != null) {
        defaultEndpoint.setMaxSusDuration(suspendMaxDuration);
    }
    if (factor != null) {
        defaultEndpoint.setSusProgFactor(factor);
    }
    if (retryErrorCode != null) {
        defaultEndpoint.setTimedOutErrorCodes(retryErrorCode);
    }
    if (retriesOnTimeOut != null) {
        defaultEndpoint.setRetryTimeout(retriesOnTimeOut);
    }
    if (retryDelay != null) {
        defaultEndpoint.setRetryDelay(retryDelay);
    }
    if (disabledErrorCodes != null) {
        defaultEndpoint.setRetryDisabledErrorCodes(disabledErrorCodes);
    }
    if (action != null) {
        if (action.equals("discardMessage")) {
            defaultEndpoint.setTimeoutAct("discard");
        } else if (action.equals("executeFaultSequence")) {
            defaultEndpoint.setTimeoutAct("fault");
        } else {
            defaultEndpoint.setTimeoutAct("100");
        }
    }
    if (actionDuration != null) {
        defaultEndpoint.setTimeoutActionDur(actionDuration);
    }
    if (wsAddressing != null) {
        defaultEndpoint.setWsadd(true);
    } else {
        defaultEndpoint.setWsadd(false);
    }
    if (useSeprateListner != null) {
        defaultEndpoint.setSepList(true);
    } else {
        defaultEndpoint.setSepList(false);
    }
    if (wsSecurity != null) {
        defaultEndpoint.setWssec(true);
    } else {
        defaultEndpoint.setWssec(false);
    }
    if (secPolicy != null) {
        defaultEndpoint.setSecPolKey(secPolicy);
    } else {
        defaultEndpoint.setSecPolKey(null);
    }
    if (wsRM != null) {
        defaultEndpoint.setWsrm(true);
    } else {
        defaultEndpoint.setWsrm(false);
    }
    if (rmPolicy != null) {
        defaultEndpoint.setRmPolKey(rmPolicy);
    } else {
        defaultEndpoint.setRmPolKey(null);
    }
    if (properties != null) {
        defaultEndpoint.setProperties(properties);
    } else {
        defaultEndpoint.setProperties(null);
    }

    OMElement endpointElement = defaultEndpoint.serialize(null);
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
            template.setElement(defaultEndpoint.getEmptyEndpointElement());
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
