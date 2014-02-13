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
<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.synapse.config.xml.endpoints.TemplateFactory" %>
<%@ page import="org.apache.synapse.endpoints.Template" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.ListEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.defaultendpoint.DefaultEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.factory.TemplateDefinitionFactory" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.EndpointConfigurationHelper" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.TemplateParameterContainer" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="java.util.Properties" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.endpoint.ui.i18n.Resources"
        request="<%=request%>"/>
<%

    boolean isEditingListEndpoint = session.getAttribute("isEditingListEndpoint") != null ? true : false;
    boolean isRetryAvailableInParentEndpoint = false;
    if (isEditingListEndpoint) {
        ListEndpoint listEndpoint = (ListEndpoint) session.getAttribute("editingListEndpoint");
        isRetryAvailableInParentEndpoint = listEndpoint.isRetryAvailable();
    }
    String endpointName = (String) session.getAttribute("endpointName");
    String endpointAction = (String) session.getAttribute("endpointAction");
    String origin = (String) session.getAttribute("origin");
    String templateAdd = (String) session.getAttribute("templateAdd");

    DefaultEndpoint endpoint = null;

    //initializing template specific parameters
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;
    boolean isTemplateAdd = templateAdd != null && "true".equals(templateAdd) ? true : false;

    String[] params = new String[0];
    Template templateObj = null;
    String templateName = "";
    if (isFromTemplateEditor) {
        templateObj = (Template) session.getAttribute("endpointTemplate");
        if (templateObj != null) {
            params = templateObj.getParameters().toArray(params);
            templateName = templateObj.getName();
        }
        //template mode cant coexist with  anonymous mode
        //remove any anonymous mode related session attributes (still any session attributes may exist ie:- if
        // anonymous mode could nt exit properly)
        String epMode = (String) session.getAttribute("epMode");
        if (epMode != null && "anon".equals(epMode)) {
            session.removeAttribute("epMode");
        }
    }

    //this factory will be used to populate/extract template specific parameters starting with '$'
    TemplateDefinitionFactory templateDefinitionFactory = new TemplateDefinitionFactory();
    TemplateParameterContainer templateMappings = templateDefinitionFactory.getParameterContainer();

    // Anonymous specific parameters
    boolean isAnonymous = false;
    String anonymousEndpointXML = null;
    String endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
    }

    if (!isEditingListEndpoint) {
        if (endpointAction != null && !"".equals(endpointAction) && endpointAction.equals("edit")) {
            try {
                session.setAttribute("action", "edit"); // uses when saving the endpoint
                if (isFromTemplateEditor) {
                    endpoint = new DefaultEndpoint();
                    endpoint.build(templateObj, templateDefinitionFactory);
                } else {
                    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

                    EndpointAdminClient client;
                    String endpointString;
                    try {
                        client = new EndpointAdminClient(cookie, serverURL, configContext);
                        endpointString = client.getEndpoint(endpointName);
                        OMElement endpointElement = AXIOMUtil.stringToOM(endpointString);
                        endpoint = new DefaultEndpoint();
                        endpoint.build(endpointElement, false);
                    } catch (Exception e) {
                        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
                return;
            }
        }
    } catch (Exception e) {
        String msg = "Unable to get default Endpoint data: " + e.getMessage();
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
    }
} else if (origin != null && !"".equals(origin)) {
    String epString = (String) session.getAttribute("endpointConfiguration");
    epString = epString.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
    OMElement endpointElement = AXIOMUtil.stringToOM(epString);

    if (isFromTemplateEditor) {
        templateObj = new TemplateFactory().createEndpointTemplate(endpointElement, new Properties());
        if (templateObj != null) {
            params = templateObj.getParameters().toArray(params);
            templateName = templateObj.getName();
        }
        endpoint = new DefaultEndpoint();
        endpoint.build(templateObj, templateDefinitionFactory);
    } else {
        endpoint = new DefaultEndpoint();
        endpoint.build(endpointElement, isAnonymous);
    }
} else if (isAnonymous && !isTemplateAdd) {
    //users should not be able to create templates on anonymous mode
    //always reset template editor mode for anonymous mode if a template session already exists
    // (and avoid be hitting this path when a template is added)
    isFromTemplateEditor = false;
    isTemplateAdd = false;

    // coming through using either send mediator or proxy services by adding an anonymous endpoint
    anonymousEndpointXML = (String) session.getAttribute("anonEpXML");
    if (anonymousEndpointXML != null && !"".equals(anonymousEndpointXML)) {
        try {
            OMElement endpointElement = AXIOMUtil.stringToOM(anonymousEndpointXML);
            endpoint = new DefaultEndpoint();
            endpoint.build(endpointElement, true);

        } catch (XMLStreamException e) {
            session.removeAttribute("anonEpXML");
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Unable to create endpoint with given data");
    window.location.href = "defaultEndpoint.jsp";
</script>
<%
                }
            }
        } else {
            session.setAttribute("action", "add");
        }
    } else {
        isAnonymous = false;
        endpoint = (DefaultEndpoint) ListEndpointDesignerHelper.getEditingEndpoint(request, session);
    }



    String[] formatOptions = {"soap11", "soap12", "POX", "REST", "GET", "leave-as-is"};
    String[] optimizeOptions = {"SWA", "MTOM", "leave-as-is"};
    String[] actionOptions = {"neverTimeout", "discardMessage", "executeFaultSequence"};
    String defaultEpName = "";
    boolean isPox = false, isRest = false, isSoap11 = false, isSoap12 = false, isGet = false;
    boolean isformatDefault = true;
    boolean isSWA = false, isMTOM = false, isOptimizeDefault = true;
    String errorCode = "";
    long suspendDurationOnFailure = 0;
    long maxDuration = 0;
    float factor = 1.0f; // default value
    String timeOutErrorCode = "";
    String retryDisabledErrorCodes = "";
    int retryTimeOut = 0;
    int retryDelayTimeOut = 0;
    boolean isNeverTimeout = true, isDiscardMessage = false, isFaultSequence = false;
    long actionDuration = 0;
    boolean isEnableAddressing = false;
    boolean isSepListener = false;
    boolean isEnableWSSec = false;
    String secPolicy = "";
    boolean isEnableRM = false;
    String rmPolicy = "";
    String description = "";
    String properties = "";

    if (endpoint != null) {
        // Endpoint Name
        if (endpoint.getEpName() != null) {
            defaultEpName = endpoint.getEpName();
        }
        // Description
        if (endpoint.getDescription() != null) {
            description = endpoint.getDescription();
        }
        // Format string
        if (endpoint.isPox()) {
            isPox = true;
        } else if (endpoint.isRest()) {
            isRest = true;
        } else if (endpoint.isSoap11()) {
            isSoap11 = true;
        } else if (endpoint.isSoap12()) {
            isSoap12 = true;
        } else if (endpoint.isGet()) {
            isGet = true;
        } else {
            isformatDefault = true;
        }
        if (isPox || isRest || isSoap11 || isSoap12 || isGet) {
            isformatDefault = false;
        }
        // Optimize string
        if (endpoint.isMtom()) {
            isMTOM = true;
        } else if (endpoint.isSwa()) {
            isSWA = true;
        } else {
            isOptimizeDefault = true;
        }
        if (isMTOM || isSWA) {
            isOptimizeDefault = false;
        }
        // Error codes
        if (endpoint.getErrorCodes() != null) {
            errorCode = endpoint.getErrorCodes().trim();
        }
        // Initial duration
        String tmpValue = endpoint.getSuspendDurationOnFailure();
        if (tmpValue != null & !"".equals(tmpValue)) {
            Long initialDuration = Long.valueOf(tmpValue);
            if (initialDuration >= 0) {
                suspendDurationOnFailure = initialDuration;
            }
        }
        // Max duration
        tmpValue = endpoint.getMaxSusDuration();
        if (tmpValue != null & !"".equals(tmpValue)) {
            Long maxSusDuration = Long.valueOf(tmpValue);
            if (0 <= maxSusDuration && maxSusDuration < Long.MAX_VALUE) {
                maxDuration = maxSusDuration;
            }
        }
        // Factor
        tmpValue = endpoint.getSusProgFactor();
        if (tmpValue != null & !"".equals(tmpValue)) {
            Float susProgFactor = Float.valueOf(tmpValue);
            if (0 <= susProgFactor && susProgFactor >= 0.0) {
                factor = susProgFactor;
            }
        }
        // TimeOut error code
        if (endpoint.getTimedOutErrorCodes() != null) {
            timeOutErrorCode = endpoint.getTimedOutErrorCodes().trim();
        }
        //non-retry error codes
        if (endpoint.getRetryDisabledErrorCodes() != null) {
            retryDisabledErrorCodes = endpoint.getRetryDisabledErrorCodes().trim();
        }
        // Retry time out
        //this is the delay between retries for a timeout-out on a endpoint
        tmpValue = endpoint.getRetryTimeout();
        if (tmpValue != null & !"".equals(tmpValue)) {
            if (Integer.parseInt(tmpValue) > 0) {
                retryTimeOut = Integer.parseInt(tmpValue);
            }
        }
        // Retry delay timeout
        //this is no of reties before suspend
        tmpValue = endpoint.getRetryDelay();
        if (tmpValue != null & !"".equals(tmpValue)) {
            if (Integer.parseInt(tmpValue) > 0) {
                retryDelayTimeOut = Integer.parseInt(endpoint.getRetryDelay());
            }
        }
        // Timeout Action
        if (endpoint.getTimeoutAct() != null) {
            if (endpoint.getTimeoutAct().equals("discard")) {
                isDiscardMessage = true;
                actionDuration = Integer.parseInt(endpoint.getTimeoutActionDur());
            } else if (endpoint.getTimeoutAct().equals("fault")) {
                isFaultSequence = true;
                actionDuration = Integer.parseInt(endpoint.getTimeoutActionDur());
            } else {
                isNeverTimeout = true;
            }
        } else {
            isNeverTimeout = true;
        }
        // Enable WS-Addressing?
        if (endpoint.isWsadd()) {
            isEnableAddressing = true;
        }
        // Use seperate listener ?
        if (endpoint.isSepList()) {
            isSepListener = true;
        }
        // Enable WS-Sec?
        if (endpoint.isWssec()) {
            isEnableWSSec = true;
            // Sec. policy
            if (endpoint.getSecPolKey() != null) {
                secPolicy = endpoint.getSecPolKey();
            }
        }
        // Enable rm ?
        if (endpoint.isWsrm()) {
            isEnableRM = true;
            // RM. policy
            if (endpoint.getRmPolKey() != null) {
                rmPolicy = endpoint.getRmPolKey();
            }
        }
        if (endpoint.getProperties() != null && endpoint.getProperties() != "") {
            properties = endpoint.getProperties();
        }
    } else {
        if (isFromTemplateEditor) { // set default variables to template fields
            if (defaultEpName.equals("")) {
                defaultEpName = "$name";
            }
        }
    }

%>
<script type="text/javascript" src="js/defaultEndpoint-validate.js"></script>
<script type="text/javascript">


    YAHOO.util.Event.onDOMReady(init);

    function init() {
        generateServiceParamTable();
    }

    function generateServiceParamTable() {
        var str = '<%=properties%>';
        if (str != '') {
            var params;
            params = str.split("::");
            var i, param;
            for (i = 0; i < params.length; i++) {
                param = params[i].split(",");
                addServiceParamRow(param[0], param[1], param[2], "headerTable");
            }
        }
    }

    function validateForm() {
        return validateDefaultEndpoint(false,false);
    }

</script>


<table class="normal-nopadding">
    <tbody>
    <%
        if (isFromTemplateEditor && isTemplateAdd) {
    %>
    <tr>
        <td width="180px"><fmt:message key="templateName"/> <span
                class="required">*</span></td>
        <td><input name="templateName" id="templateName"
                   value="<%=templateName%>"
                />
        </td>
    </tr>
    <%
    } else if (isFromTemplateEditor) {
    %>
    <tr>
        <td width="180px"><fmt:message key="templateName"/> <span
                class="required">*</span></td>
        <td><input name="templateNameBox" id="templateNameBox"
                   value="<%=templateName%>"
                   disabled="disabled"/>
            <input type="hidden" name="templateName" id="templateName"
                   value="<%=templateName%>"/>
        </td>
    </tr>
    <%
        }
    %>
    <tr style="<%=!isAnonymous?"":"display:none"%>">
        <td width="180px"><fmt:message key="endpointName"/> <span
                class="required">*</span></td>
        <td><input name="endpointName" id="endpointName"
                   value="<%=defaultEpName%>"
                <%=(!"".equals(defaultEpName)) ? "disabled=\"disabled\"" : ""%>
                <%=isFromTemplateEditor ? "" : "onkeypress=\"return validateText(event);\""%> />
            <input type="hidden" name="endpointName" value="<%=defaultEpName%>"/>
        </td>
    </tr>
    <%
        if (isFromTemplateEditor) {
            String propertyTableStyle = params.length == 0 ? "display:none;" : "";
            if (params.length == 2 && ((params[0].equals("name") && params[1].equals("uri")) || (params[1].equals("name") && params[0].equals("uri")))) {
                propertyTableStyle = "display:none;";
            }
    %>
    <tr>
        <td colspan="2">
            <div style="margin-top:0px;">
                <table id="propertytable" style="<%=propertyTableStyle%>" class="styledInner">
                    <thead>
                    <tr>
                        <th width="75%"><fmt:message key="template.parameter.name"/></th>
                        <th><fmt:message key="template.parameter.action"/></th>
                    </tr>
                    </thead>
                    <tbody id="propertytbody">
                    <%
                        int i = 0;
                        for (; i < params.length; i++) {
                            String paramName = params[i];
                            if (paramName.equals("name") || paramName.equals("uri")) { // hide default parameters
                                continue;
                            }
                    %>
                    <tr id="propertyRaw<%=i%>">
                        <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                                   class="esb-edit small_textbox"
                                   value="<%=paramName%>"/>
                        </td>
                        <td><a class="delete-icon-link"
                               onclick="deleteProperty(<%=i%>)"><fmt:message
                                key="template.parameter.delete"/></a></td>
                    </tr>
                    <%
                        }%>
                    <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
                    </tbody>
                </table>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <div style="margin-top:10px;">
                <a name="addNameLink"></a>
                <a class="add-icon-link"
                   onclick="addParameter()"><fmt:message key="template.parameter.add"/></a>
            </div>
        </td>
    </tr>
    <%
        }
    %>
    <tr>
        <td><span id="_adv" style="float: left; position: relative;">
            <a class="icon-link" onclick="javascript:showAdvancedOptions('');"
               style="background-image: url(images/down.gif);"><fmt:message
                    key="show.advanced.options"/></a>
        </span>
        </td>
    </tr>
    </tbody>
</table>
<div id="_advancedForm" style="display:none">
<table class="normal-nopadding">
<tbody>
<tr>
    <td colspan="2" class="sub-header"><fmt:message key="message.content"/></td>
</tr>
<tr>
    <td width="180px"><fmt:message key="format"/></td>
    <td><select name="format">
        <option value="<%=formatOptions[0]%>" <%=isSoap11 ? "selected=\"selected\"" : ""%>>
            <fmt:message key="soap.1.1"/></option>
        <option value="<%=formatOptions[1]%>" <%=isSoap12 ? "selected=\"selected\"" : ""%>>
            <fmt:message key="soap.1.2"/></option>
        <option value="<%=formatOptions[2]%>" <%=isPox ? "selected=\"selected\"" : ""%>>
            <fmt:message key="plain.old.xml.pox"/></option>
        <option value="<%=formatOptions[3]%>" <%=isRest ? "selected=\"selected\"" : ""%>>
            <fmt:message key="representational.state.transer.rest.get"/></option>
        <option value="<%=formatOptions[4]%>" <%=isGet ? "selected=\"selected\"" : ""%>>
            <fmt:message key="get"/></option>
        <option value="<%=formatOptions[5]%>" <%=isformatDefault ? "selected=\"selected\"" : ""%>>
            <fmt:message
                    key="leave.as.is"/></option>
    </select>
    </td>
</tr>
<tr>
    <td><fmt:message key="optimize"/></td>
    <td><select name="optimize">
        <option value="<%=optimizeOptions[0]%>" <%=isSWA ? "selected" : ""%>>
            <fmt:message key="swa"/></option>
        <option value="<%=optimizeOptions[1]%>" <%=isMTOM ? "selected" : ""%>>
            <fmt:message key="mtom"/></option>
        <option value="<%=optimizeOptions[2]%>" <%=isOptimizeDefault ? "selected" : ""%>>
            <fmt:message key="leave.as.is"/></option>
        <option value="<%=templateMappings.getTemplateMapping(TemplateParameterContainer.EndpointDefKey.optimize)%>" <%=templateMappings.contains(TemplateParameterContainer.EndpointDefKey.optimize) ? "selected" : ""%>>
            <fmt:message key="mtom"/></option>
    </select></td>
</tr>
<tr>
    <td colspan="2" class="sub-header"><fmt:message
            key="suspend"/></td>
</tr>
<tr id="tr_supspend_error_codes">
    <td>
        <div class="indented"><fmt:message key="error.codes"/></div>
    </td>
    <td>
        <table class="normal">
            <tr>
                <td><input type="text" id="suspendErrorCode" name="suspendErrorCode"
                           class="longInput"
                           value="<%="".equals(errorCode.trim())?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.suspendErrorCodes):errorCode%>"
                           size="75" />
                </td>
                <td>
                    <a class="errorcode-picker-icon-link"
                       style="padding-left:20px;padding-right:20px"
                       onclick="showErrorCodeEditor('suspendErrorCode')"><fmt:message
                            key="errorcode.editor.link"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td><fmt:message key="intial.duration.millis"/></td>
    <td><input type="text" id="suspendDuration" name="suspendDuration"
               value="<%=suspendDurationOnFailure==0?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.initialSuspendDuration):suspendDurationOnFailure %>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="max.duration.millis"/></td>
    <td><input type="text" id="suspendMaxDuration" name="suspendMaxDuration"
               value="<%=maxDuration==0?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.suspendMaximumDuration):maxDuration%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="factor"/></td>
    <td><input type="text" id="factor" name="factor"
               value="<%=(factor==1.0)?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.suspendProgressionFactor):factor%>"/>
    </td>
</tr>
<tr>
    <td colspan="2" class="sub-header"><fmt:message
            key="on.timedout"/></td>
</tr>
<tr id="retry_error_codes">
    <td>
        <div class="indented"><fmt:message key="error.codes"/></div>
    </td>
    <td>
        <table class="normal">
            <tr>
                <td><input type="text" id="retryErroCode" name="retryErroCode" class="longInput"
                           value="<%="".equals(timeOutErrorCode.trim())?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.timeoutErrorCodes):timeOutErrorCode%>"
                           size="75"/>
                </td>
                <td>
                    <a class="errorcode-picker-icon-link"
                       style="padding-left:20px;padding-right:20px"
                       onclick="showErrorCodeEditor('retryErroCode')"><fmt:message
                            key="errorcode.editor.link"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td><fmt:message key="retry"/></td>
    <td><input type="text" id="retryTimeOut" name="retryTimeOut"
               value="<%=retryTimeOut==0?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.retriesOnTimeoutBeforeSuspend):retryTimeOut%>"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="retry.delay.millis"/></td>
    <td><input type="text" id="retryDelay" name="retryDelay"
               value="<%=(retryDelayTimeOut==0)?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.retryDurationOnTimeout):retryDelayTimeOut%>"/>
    </td>
</tr>

<% if (isRetryAvailableInParentEndpoint) {
%>
<tr>
    <td colspan="2" class="sub-header"><fmt:message key="failover.retry"/></td>
</tr>
<tr id="disabled_error_codes">
    <td>
        <div class="indented"><fmt:message key="disabled.error.codes"/></div>
    </td>
    <td>
        <table class="normal">
            <tr>
                <td><input type="text" id="disabledErrorCodes" name="disabledErrorCodes" class="longInput"
                           value="<%=retryDisabledErrorCodes%>" size="75"></td>
                <td>
                    <a class="errorcode-picker-icon-link"
                       style="padding-left:20px;padding-right:20px"
                       onclick="showErrorCodeEditor('disabledErrorCodes')"><fmt:message
                            key="errorcode.editor.link"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>
<%
    }
%>
<tr>
    <td colspan="2" class="sub-header"><fmt:message key="timeout"/></td>
</tr>
<tr>
    <td>
        <div class="indented"><fmt:message key="action"/></div>
    </td>
    <td><select name="actionSelect" onchange="activateDurationField(this)">
        <option value="<%=actionOptions[0]%>" <%=isNeverTimeout ? "selected=\"selected\"" : ""%>>
            <fmt:message key="action.never.timeout"/></option>
        <option value="<%=actionOptions[1]%>" <%=isDiscardMessage ? "selected=\"selected\"" : ""%>>
            <fmt:message key="action.discard.message"/></option>
        <option value="<%=actionOptions[2]%>" <%=isFaultSequence ? "selected=\"selected\"" : ""%>>
            <fmt:message key="action.execute.fault.sequence"/></option>
    </select>
    </td>
</tr>
<tr>
    <td>
        <div class="indented"><fmt:message key="duration.millis"/>
        </div>
    </td>
    <td><input id="actionDuration" type="text" name="actionDuration"
               value="<%=(actionDuration==0)?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.timeoutDuration):actionDuration%>"
        <%=actionDuration == 0 ? "disabled=\"disabled\"" : ""%>
    </td>
</tr>

<tr>
    <td colspan="2" class="sub-header"><fmt:message key="qos"/></td>
</tr>
<tr>
    <td><fmt:message key="ws.addressing"/></td>
    <td><input type="checkbox" onchange="showHideOnSelect('wsAddressing','tr_separate_listener')"
               id="wsAddressing" name="wsAddressing" value="wsAddressing"
        <%=isEnableAddressing ? "checked" : ""%>
    </td>
</tr>
<tr id="tr_separate_listener" style="display:<%=isEnableAddressing?"":"none" %>">
    <td>
        <div class="indented"><fmt:message
                key="seperate.listener"/></div>
    </td>
    <td><input type="checkbox" id="sepListener" name="sepListener" value="sepListener"
        <%=isSepListener ? "checked=\"checked\"" : ""%>
    </td>
</tr>
<tr>
    <td><fmt:message key="ws.security"/></td>
    <td><input type="checkbox" onclick="showHideOnSelect('wsSecurity','tr_ws_sec_policy_key')"
               id="wsSecurity" name="wsSecurity" value="wsSecurity"
            <%=isEnableWSSec ? "checked=\"checked\"" : ""%> />
    </td>
</tr>
<tr id="tr_ws_sec_policy_key" style="display:<%=isEnableWSSec?"":"none" %>">
    <td>
        <div class="indented"><fmt:message key="policy.key"/></div>
    </td>
    <td>
        <table class="normal">
            <tr>
                <td>
                    <input class="longInput" type="text" id="wsSecPolicyKeyID"
                           name="wsSecPolicyKeyID"
                           value="<%="".equals(secPolicy.trim())?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.policy):secPolicy%>"   <%=isFromTemplateEditor ? "" : "readonly=\"true\""%> />
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px;padding-right:20px"
                       onclick="showRegistryBrowser('wsSecPolicyKeyID', '/_system/config')"><fmt:message
                            key="registry.conf.keys"/></a>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px"
                       onclick="showRegistryBrowser('wsSecPolicyKeyID', '/_system/governance')"><fmt:message
                            key="registry.gov.keys"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td><fmt:message key="ws.rm"/></td>
    <td><input type="checkbox" onclick="showHideOnSelect('wsRM','tr_ws_rm_policy_key')" id="wsRM"
               name="wsRM" value="wsRM"
            <%=isEnableRM ? "checked=\"checked\"" : ""%> />
    </td>
</tr>
<tr id='tr_ws_rm_policy_key' style="display:<%=isEnableRM?"":"none" %>">
    <td>
        <div class="indented"><fmt:message
                key="policy.key"/></div>
    </td>
    <td>
        <table class="normal">
            <tr>
                <td><input class="longInput" type="text" id="wsrmPolicyKeyID"
                           name="wsrmPolicyKeyID"
                           value="<%="".equals(rmPolicy.trim())?EndpointConfigurationHelper.getMappingFrom(templateMappings, TemplateParameterContainer.EndpointDefKey.wsRMPolicyKey):rmPolicy%>" <%=isFromTemplateEditor ? "" : "readonly=\"true\""%> />
                </td>
                <td><a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px;padding-right:20px"
                       onclick="showRegistryBrowser('wsrmPolicyKeyID', '/_system/config')"><fmt:message
                        key="registry.conf.keys"/></a>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       style="padding-left:20px"
                       onclick="showRegistryBrowser('wsrmPolicyKeyID', '/_system/governance')"><fmt:message
                            key="registry.gov.keys"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
<tr>
    <td colspan="2" class="sub-header"><fmt:message key="endpoint.description.hader"/></td>
</tr>
<tr>
    <td>
        <div class="indented"><fmt:message key="endpoint.description"/></div>
    </td>
    <td>
        <textarea name="endpointDescription" id="endpointDescription" title="Endpoint Description"
                  cols="100" rows="3"><%=description%>
        </textarea>
    </td>
</tr>
</tr>
</tbody>
</table>
</div>
<table class="normal-nopadding">
    <tbody>
    <tr>
        <td colspan="2" class="sub-header"><fmt:message key="endpoint.property.header"/></td>
    </tr>
    <tr>
        <td colspan="2">
            <a onclick="addServiceParams('headerTable')"
               style="background-image: url('../admin/images/add.gif');" class="icon-link">Add
                                                                                           Property</a><input
                type="hidden" name="endpointProperties" id="endpointProperties"/>
        </td>
    </tr>
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
                   id="headerTable"
                   style="display:none;">
                <thead>
                <tr>
                    <th style="width:25%"><fmt:message key="param.name"/></th>
                    <th style="width:25%"><fmt:message key="param.value"/></th>
                    <th style="width:25%"><fmt:message key="param.scope"/></th>
                    <th style="width:25%"><fmt:message key="param.action"/></th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </td>
    </tr>
    </tbody>
</table>

</fmt:bundle>
