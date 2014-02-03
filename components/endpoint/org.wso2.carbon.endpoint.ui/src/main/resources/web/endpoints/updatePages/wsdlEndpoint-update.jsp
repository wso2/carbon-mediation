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
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.wsdl.WsdlEndpoint" %>

<%
    boolean isEditingListEndpoint = session.getAttribute("isEditingListEndpoint") != null ? true : false;
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;

    String endpointName = request.getParameter("endpointName");
    String uriWsdl = request.getParameter("uriWSDLVal");
    String service = request.getParameter("wsdlendpointService");
    String port = request.getParameter("wsdlendpointPort");
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

    WsdlEndpoint wsdlEndpoint = new WsdlEndpoint();

    if (isEditingListEndpoint) {
        wsdlEndpoint = (WsdlEndpoint) ListEndpointDesignerHelper.getEditingEndpoint(request, session);
    }
    if (endpointName != null) {
        wsdlEndpoint.setEpName(endpointName);
    }
    if (description != null && !"".equals(description)) {
        wsdlEndpoint.setDescription(description);
    } else {
        wsdlEndpoint.setDescription("");
    }
    if (uriWsdl != null && !"".equals(uriWsdl)) {
        wsdlEndpoint.setUri(uriWsdl);
    }
    if (service != null) {
        wsdlEndpoint.setService(service);
    }
    if (port != null) {
        wsdlEndpoint.setPort(port);
    }
    if (errorCode != null) {
        wsdlEndpoint.setErrorCodes(errorCode);
    }
    if (suspendDuration != null) {
        wsdlEndpoint.setSuspendDurationOnFailure(suspendDuration);
    }
    if (suspendMaxDuration != null) {
        wsdlEndpoint.setMaxSusDuration(suspendMaxDuration);
    }
    if (factor != null) {
        wsdlEndpoint.setSusProgFactor(factor);
    }
    if (retryErrorCode != null) {
        wsdlEndpoint.setTimedOutErrorCodes(retryErrorCode);
    }
    if (retriesOnTimeOut != null) {
        wsdlEndpoint.setRetryTimeout(retriesOnTimeOut);
    }
    if (retryDelay != null) {
        wsdlEndpoint.setRetryDelay(retryDelay);
    }
    if (disabledErrorCodes != null) {
        wsdlEndpoint.setRetryDisabledErrorCodes(disabledErrorCodes);
    }
    if (action != null) {
        if (action.equals("discardMessage")) {
            wsdlEndpoint.setTimeoutAct("discard");
        } else if (action.equals("executeFaultSequence")) {
            wsdlEndpoint.setTimeoutAct("fault");
        } else {
            wsdlEndpoint.setTimeoutAct("100");
        }
    }
    if (actionDuration != null) {
        wsdlEndpoint.setTimeoutActionDuration(actionDuration);
    }
    if (wsAddressing != null) {
        wsdlEndpoint.setWsadd(true);
    } else {
        wsdlEndpoint.setWsadd(false);
    }
    if (useSeprateListner != null) {
        wsdlEndpoint.setSepList(true);
    } else {
        wsdlEndpoint.setSepList(false);
    }
    if (wsSecurity != null) {
        wsdlEndpoint.setWssec(true);
    } else {
        wsdlEndpoint.setWssec(false);
    }
    if (secPolicy != null) {
        wsdlEndpoint.setSecPolKey(secPolicy);
    } else {
        wsdlEndpoint.setSecPolKey(null);
    }
    if (wsRM != null) {
        wsdlEndpoint.setWsrm(true);
    } else {
        wsdlEndpoint.setWsrm(false);
    }
    if (rmPolicy != null) {
        wsdlEndpoint.setRmPolKey(rmPolicy);
    } else {
        wsdlEndpoint.setRmPolKey(null);
    }
    if (properties != null) {
        wsdlEndpoint.setProperties(properties);
    } else {
        wsdlEndpoint.setProperties(null);
    }

    OMElement endpointElement = wsdlEndpoint.serialize(null);
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
            template.setElement(wsdlEndpoint.getEmptyEndpointElement());
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