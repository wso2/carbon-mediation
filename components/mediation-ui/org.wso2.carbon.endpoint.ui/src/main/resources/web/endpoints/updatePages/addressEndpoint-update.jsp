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
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.address.AddressEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>

<%
    boolean isEditingListEndpoint = session.getAttribute("isEditingListEndpoint") != null ? true : false;
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;

    String endpointName = request.getParameter("endpointName");
    String address = request.getParameter("url");
    String formatOption = request.getParameter("format");
    String optimizeOption = request.getParameter("optimize");
    String description = request.getParameter("endpointDescription").trim();
    String errorCode = request.getParameter("suspendErrorCode");
    String suspendDuration = request.getParameter("suspendDuration");
    String suspendMaxDuration = request.getParameter("suspendMaxDuration");
    String factor = request.getParameter("factor");
    String retryErrorCode = request.getParameter("retryErroCode");
    String enabledErrorCodes = request.getParameter("enableErrorCodes");
    String retryCodeRadio = request.getParameter("retryCode");
    String retriesOnTimeOut = request.getParameter("retryTimeOut");
    String retryDelay = request.getParameter("retryDelay");
    String disabledErrorCodes = request.getParameter("disabledErrorCodes");
    String action = request.getParameter("actionSelect");
    String actionDuration = null;
    String encoding = request.getParameter("encoding");
    if (action != null && !action.equals("neverTimeout")) {
        actionDuration = request.getParameter("actionDuration");
    }
    String wsAddressing = request.getParameter("wsAddressing");
    String useSeprateListner = null;
    if (wsAddressing != null) {
        useSeprateListner = request.getParameter("sepListener");
    }
    String wsSecurity = request.getParameter("wsSecurity");
    String useDifferentPolicies = request.getParameter("wsSecurityUseDifferentPolicies");
    String secOutboundPolicy = null;
    String secInboundPolicy = null;
    String secPolicy = null;
    if (wsSecurity != null) {
        if (useDifferentPolicies != null) {
            secOutboundPolicy = request.getParameter("wsSecOutboundPolicyKeyID");
            secInboundPolicy = request.getParameter("wsSecInboundPolicyKeyID");

        } else {

            secPolicy = request.getParameter("wsSecPolicyKeyID");
        }
    }
    String wsRM = request.getParameter("wsRM");
    String rmPolicy = null;
    if (wsRM != null) {
        rmPolicy = request.getParameter("wsrmPolicyKeyID");
    }
    String properties = request.getParameter("endpointProperties");

    AddressEndpoint addressEndpoint = new AddressEndpoint();

    if (isEditingListEndpoint) {
        addressEndpoint = (AddressEndpoint) ListEndpointDesignerHelper.getEditingEndpoint(request, session);
    }
    if (endpointName != null) {
        addressEndpoint.setEndpointName(endpointName);
    }
    if (address != null) {
        addressEndpoint.setAddress(address);
    }
    if ("soap11".equals(formatOption)) {
        addressEndpoint.setSoap11(true);
    } else if ("soap12".equals(formatOption)) {
        addressEndpoint.setSoap12(true);
    } else if ("POX".equals(formatOption)) {
        addressEndpoint.setPox(true);
    } else if ("REST".equals(formatOption)) {
        addressEndpoint.setRest(true);
    } else if ("GET".equals(formatOption)) {
        addressEndpoint.setGet(true);
    } else {
        addressEndpoint.setSoap11(false);
        addressEndpoint.setSoap12(false);
        addressEndpoint.setPox(false);
        addressEndpoint.setRest(false);
        addressEndpoint.setGet(false);
    }

    if ("SWA".equals(optimizeOption)) {
        addressEndpoint.setSwa(true);
    } else if ("MTOM".equals(optimizeOption)) {
        addressEndpoint.setMtom(true);
    } else {
        addressEndpoint.setSwa(false);
        addressEndpoint.setMtom(false);
    }
    if (encoding != null && !"".equals(encoding)) {
        addressEndpoint.setEncoding(encoding);
    }
    if (description != null && !"".equals(description)) {
        addressEndpoint.setDescription(description);
    } else {
        addressEndpoint.setDescription("");
    }
    if (errorCode != null) {
        addressEndpoint.setErrorCodes(errorCode);
    }
    if (suspendDuration != null) {
        addressEndpoint.setSuspendDurationOnFailure(suspendDuration);
    }
    if (suspendMaxDuration != null) {
        addressEndpoint.setMaxSusDuration(suspendMaxDuration);
    }
    if (factor != null) {
        addressEndpoint.setSusProgFactor(factor);
    }
    if (retryErrorCode != null) {
        addressEndpoint.setTimedOutErrorCodes(retryErrorCode);
    }
    if (retriesOnTimeOut != null) {
        addressEndpoint.setRetryTimeout(retriesOnTimeOut);
    }
    if (retryDelay != null) {
        addressEndpoint.setRetryDelay(retryDelay);
        addressEndpoint.setRetryDelay(retryDelay);
    }
    if (retryCodeRadio!=null && retryCodeRadio.equals("disabledErrorCode")) {
        if (disabledErrorCodes != null) {
            addressEndpoint.setRetryDisabledErrorCodes(disabledErrorCodes);
            addressEndpoint.setRetryEnabledErrorCodes(null);
        }
    }else if (retryCodeRadio!=null && retryCodeRadio.equals("enableErrorCode")) {
        if (enabledErrorCodes!=null) {
                addressEndpoint.setRetryEnabledErrorCodes(enabledErrorCodes);
        addressEndpoint.setRetryDisabledErrorCodes(null);
        }
    }
    if (action != null) {
        if (action.equals("discardMessage")) {
            addressEndpoint.setTimeoutAction("discard");
        } else if (action.equals("executeFaultSequence")) {
            addressEndpoint.setTimeoutAction("fault");
        } else {
            addressEndpoint.setTimeoutAction("100");
        }
    }
    if (actionDuration != null) {
        addressEndpoint.setTimeoutActionDur(actionDuration);
    }
    if (wsAddressing != null) {
        addressEndpoint.setWsadd(true);
    } else {
        addressEndpoint.setWsadd(false);
    }
    if (useSeprateListner != null) {
        addressEndpoint.setSepList(true);
    } else {
        addressEndpoint.setSepList(false);
    }
    if (wsSecurity != null) {
        addressEndpoint.setWssec(true);
    } else {
        addressEndpoint.setWssec(false);
    }
    if (secPolicy != null) {
        addressEndpoint.setSecPolKey(secPolicy);
    } else {
        addressEndpoint.setSecPolKey(null);
    }
    if (secOutboundPolicy != null) {
        addressEndpoint.setOutboundWsSecPolicyKey(secOutboundPolicy);
    } else {
        addressEndpoint.setOutboundWsSecPolicyKey(null);
    }
    if (secInboundPolicy != null) {
        addressEndpoint.setInboundWsSecPolicyKey(secInboundPolicy);
    } else {
        addressEndpoint.setInboundWsSecPolicyKey(null);
    }
    if (wsRM != null) {
        addressEndpoint.setWsrm(true);
    } else {
        addressEndpoint.setWsrm(false);
    }
    if (rmPolicy != null) {
        addressEndpoint.setRmPolKey(rmPolicy);
    } else {
        addressEndpoint.setRmPolKey(null);
    }
    if (properties != null) {
        addressEndpoint.setProperties(properties);
    } else {
        addressEndpoint.setProperties(null);
    }

    OMElement endpointElement = addressEndpoint.serialize(null);
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
            template.setElement(addressEndpoint.getEmptyEndpointElement());
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
