<%--
 ~ Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageStoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.processor.ui.utils.MessageProcessorData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.processor.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.processor.ui.i18n.JSResources"
               request="<%=request%>" i18nObjectName="messageStorei18n"/>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="localentrycommons.js"></script>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../message_processor/js/registry-browser.js"></script>

<carbon:breadcrumb
        label="Message Processor"
        resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>


<script type="text/javascript">

    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, "");
    }

    String.prototype.ltrim = function() {
        return this.replace(/^\s+/, "");
    }

    String.prototype.rtrim = function() {
        return this.replace(/\s+$/, "");
    }

    function ValidateTextForm(form) {
        if (IsEmpty(form.Name)) {
            CARBON.showWarningDialog('<fmt:message key="name.field.cannot.be.empty"/>')
            form.Name.focus();
            return false;
        }

        if (IsEmpty(form.Provider)) {
            CARBON.showWarningDialog('<fmt:message key="provider.field.cannot.be.empty"/>')
            form.Name.focus();
            return false;
        }

        if (IsEmpty(form.MessageStore)) {
            CARBON.showWarningDialog('<fmt:message key="store.field.cannot.be.empty"/>')
            form.Name.focus();
            return false;
        }
        if (IsEmpty(form.Sequence)) {
            CARBON.showWarningDialog('<fmt:message key="sequence.field.cannot.be.empty"/>')
            form.Name.focus();
            return false;
        }

        return true;
    }

    function IsEmpty(aTextField) {
        if ((aTextField.value.trim().length == 0) ||
                (aTextField.value.trim() == null) || (aTextField.value.trim() == '')) {
            return true;
        }
        else {
            return false;
        }
    }


    function submitTextContent(value) {
    	if(validateSubmit()){
    		return true;
    	}
        addServiceParams();
        //document.Submit.submit();
        return true;
    }

    function validateSubmit(){
    	try{
	    	var iInterval = parseInt(document.getElementById('retry_interval').value);
	    	if(iInterval <= 0){
	    		CARBON.showErrorDialog('Invalid ' + '<fmt:message key="interval"/>');
	    		return true;
	    	}
    	}catch(e){
    		CARBON.showErrorDialog('Invalid ' + '<fmt:message key="interval"/>');
    		return true;    		
    	}
    	return false;
    }
    
    function addServiceParams() {
        addServiceParameter("interval", document.getElementById('retry_interval').value);
        addServiceParameter("concurrency", document.getElementById('sampling_concurrency').value);
        addServiceParameter("sequence", document.getElementById('Sequence').value);
        addServiceParameter("quartz.conf", document.getElementById('quartz_conf').value);
        addServiceParameter("cronExpression", document.getElementById('cron_expression').value);
        addServiceParameter("is.active", document.getElementById('mp_state').value);
    }

    function addServiceParameter(parameter, value) {
        var headerName = parameter;
        var headerValue = value;

        // trim the input values
        headerValue = headerValue.replace(/^\s*/, "").replace(/\s*$/, "");
        if (headerValue != '') {
            document.getElementById("tableParams").value = document.getElementById("tableParams").value + "|" + headerName + "#" + headerValue;
            document.getElementById("addedParams").value = document.getElementById("addedParams").value + "," + headerName + ":" + headerValue;
        }
    }

    function gotoPrevPage() {

        history.go(-1);
    }

    function showAdvancedOptions(id) {
        var formElem = document.getElementById(id + '_advancedForm');
        if (formElem.style.display == 'none') {
            formElem.style.display = '';
            document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                    'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/up.gif);">' + "<fmt:message key="hide.additional.parameters"/>" + '</a>';
        } else {
            formElem.style.display = 'none';
            document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                    'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/down.gif);">' + "<fmt:message key="show.additional.parameters"/>" + '</a>';
        }
    }

    function switchToSource() {
        if (!ValidateTextForm(document.Submit)) {
            return false;
        }
        addServiceParams();
        var messageStoreStr = {Name : document.getElementById("Name").value, Provider : document.getElementById("Provider").value, MessageStore : document.getElementById("MessageStore").value, tableParams : document.getElementById("tableParams").value};
        jQuery.ajax({
            type: 'POST',
            url: 'updatePages/messageProcessorUpdate.jsp',
            data: messageStoreStr,
            success: function(msg) {
                location.href = "sourceView.jsp";
            }
        });
    }
</script>

<div id="middle">
<h2><fmt:message key="message.sampling.processor"/></h2>

<div id="workArea">
<form name="Submit" action="ServiceCaller.jsp" method="POST"
      onsubmit="javascript:return ValidateTextForm(this)">
<input type="hidden" id="addedParams" name="addedParams" value=""/>
<input type="hidden" id="removedParams" name="removedParams" value=""/>
<input type="hidden" id="tableParams" name="tableParams" value="PARAMS:"/>
<% String origin = request.getParameter("origin");

    String messageStoreName = request.getParameter("messageProcessorName");
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MessageProcessorAdminServiceClient client = new MessageProcessorAdminServiceClient(cookie, url, configContext);
    String[] messageProcessorNames = client.getMessageProcessorNames();

    MessageProcessorData processorData = null;

    if (messageStoreName != null) {
        session.setAttribute("edit" + messageStoreName, "true");
        for (String name : messageProcessorNames) {
            if (name != null && name.equals(messageStoreName)) {
                processorData = client.getMessageProcessor(name);
            }
        }
    }  else if (origin != null && !"".equals(origin)) {
        String mpString = (String) session.getAttribute("messageProcessorConfiguration");
        String mpName = (String) session.getAttribute("mpName");
        String mpProvider = (String) session.getAttribute("mpProvider");
        String mpStore = (String) session.getAttribute("mpStore");

        session.removeAttribute("messageProcessorConfiguration");
        session.removeAttribute("mpName");
        session.removeAttribute("mpProvider");
        session.removeAttribute("mpStore");

        mpString = mpString.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
        OMElement messageProcessorElement = AXIOMUtil.stringToOM(mpString);
        processorData = new MessageProcessorData(messageProcessorElement.toString());
        processorData.setName(mpName);
        processorData.setClazz(mpProvider);
        processorData.setMessageStore(mpStore);
    }


    MessageStoreAdminServiceClient messageStoreClient =
            new MessageStoreAdminServiceClient(cookie, url, configContext);

    String[] messageStores = messageStoreClient.getMessageStoreNames();


%>

<%
    if (messageStores == null || messageStores.length == 0) {
%>
<script type="text/javascript">

    function showZeroMSError() {

        CARBON.showErrorDialog('<fmt:message key="cannot.add.message.Processor"/>' + 'No Message Stores defined.', gotoPrevPage);

    }
    YAHOO.util.Event.onDOMReady(showZeroMSError);
    //                onload=;
</script>
<%
} else {
%>
<table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
    <thead>
    <tr>
        <th colspan="2"><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="message.sampling.processor"/></span>
            <a class="icon-link"
               style="background-image: url(images/source-view.gif);"
               onclick="switchToSource();"
               href="#"><fmt:message key="switch.to.source.view"/></a>
        </th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <table class="normal-nopadding">
                <body>
                <input type="hidden" name="pageName" value="manageMessageProcessor.jsp"/>
                <%if (processorData != null) {%>
                <tr>

                    <td width="276px"><fmt:message key="name"/><span class="required"> *</span></td>
                    <td>
                        <input id="Name" name="Name" type="hidden"
                               value="<%=processorData.getName()%>"/>
                        <label for="Name"><%=processorData.getName()%>
                        </label>
                    </td>
                </tr>
                <%} else { %>
                <tr>
                    <td width="276px"><fmt:message key="name"/><span class="required"> *</span></td>
                    <td><input id="Name" type="text" size="60" name="Name" value=""/></td>
                </tr>
                <%}%>
                <%if ((processorData != null)) { %>
                <tr>
                    <td><fmt:message key="provider"/><span class="required"> *</span></td>
                    <td>
                        <input name="Provider" id="Provider" type="hidden"
                               value="org.apache.synapse.message.processor.impl.sampler.SamplingProcessor"/>
                        <%
                            String providerLabel = "Message Sampling Processor";
                        %>
                        <label id="Provider_label" for="Provider"><%=providerLabel%>
                        </label>
                        <br/>
                    </td>
                </tr>
                <%} else {%>
                <input id="Provider" name="Provider" type="hidden"
                       value="org.apache.synapse.message.processor.impl.sampler.SamplingProcessor"/>

                <%}%>
                <tr>
                    <td><fmt:message key="sequence"/><span class="required"> *</span></td>
                    <td><input type="text" size="60" id="Sequence" name="Sequence"
                               value="<%=((null!=processorData)&& processorData.getParams() != null
                                        && !processorData.getParams().isEmpty()&&(processorData.getParams().get("sequence")!=null))?processorData.getParams().get("sequence"):""%>"/>
                    </td>
                    <td>
                       <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('Sequence','/_system/config')"><fmt:message key="processor.conf.registry.browser"/></a>
                       <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('Sequence','/_system/governance')"><fmt:message key="processor.gov.registry.browser"/></a>
                    </td>
                </tr>
                <%if ((processorData != null)) { %>
                <tr>
                    <td><fmt:message key="message.store"/><span class="required"> *</span></td>
                    <td>
                        <input name="MessageStore" id="MessageStore" type="hidden"
                               value="<%=processorData.getMessageStore()%>"/>
                        <label id="MessageStore_label"
                               for="MessageStore"><%=processorData.getMessageStore()%>
                        </label>
                        <br/>
                    </td>
                </tr>
                <%} else {%>
                <tr>
                    <td><fmt:message key="message.store"/><span class="required"> *</span></td>
                    <td>
                        <select id="MessageStore" name="MessageStore">
                            <%for (String msn : messageStores) {%>
                            <option selected="true" value="<%=msn%>"><%=msn%>
                            </option>
                            <%} %>
                        </select>
                    </td>
                </tr>
                <%}%>
                <tr>
                    <td>
                        <span id="_adv" style="float: left; position: relative;">
                           <a class="icon-link" onclick="javascript:showAdvancedOptions('');"
                              style="background-image: url(images/down.gif);"><fmt:message
                                   key="show.additional.parameters"/></a>
                        </span>
                    </td>
                </tr>
                </body>
            </table>

            <div id="_advancedForm" style="display:none">

                <table class="normal-nopadding">
                    <tbody>
                    <tr>
                        <td colspan="2" class="sub-header"><fmt:message
                                key="message.sampling.processor.parameters"/></td>
                    </tr>
                    <td><fmt:message key="message.processor.state"/><span class="required"> *</span></td>
                    <td>
                        <select id="mp_state" name="mp_state">
                            <% if (null!=processorData && processorData.getParams() != null &&
                                   !processorData.getParams().isEmpty() && (processorData.getParams().get("is.active") != null)) {
                                if (Boolean.valueOf(processorData.getParams().get("is.active"))) { %>
                                <option value="false">Deactivate</option>
                                <option value="true" selected>Activate</option>
                            <% } else { %>
                                <option value="false" selected>Deactivate</option>
                                <option value="true">Activate</option>
                            <% } %>
                            <% } else { %>
                                <option value="false">Deactivate</option>
                                <option value="true" selected>Activate</option>
                            <% } %>
                        </select>
                    </td>
                    <tr>
                        <td><fmt:message key="sampling.interval"/></td>
                        <td><input type="text" id="retry_interval" name="retry_interval"
                                   value="<%=((null!=processorData)&& processorData.getParams() != null
                                        && !processorData.getParams().isEmpty()&&(processorData.getParams().get("interval")!=null))?processorData.getParams().get("interval"):"1000"%>"
                                />
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="sampling.concurrency"/></td>
                        <td><input type="text" id="sampling_concurrency" name="sampling_concurrency"
                                   value="<%=((null!=processorData)&& processorData.getParams() != null
                                        && !processorData.getParams().isEmpty()&&(processorData.getParams().get("concurrency")!=null))?processorData.getParams().get("concurrency"):"1"%>"
                                />
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="quartz.conf"/></td>
                        <td><input type="text" id="quartz_conf" name="quartz_conf"
                                   value="<%=((null!=processorData)&& processorData.getParams() != null
                                        && !processorData.getParams().isEmpty()&&(processorData.getParams().get("quartz.conf")!=null))?processorData.getParams().get("quartz.conf"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="cronExpression"/></td>
                        <td><input type="text" id="cron_expression" name="cron_expression"
                                   value="<%=((null!=processorData)&& processorData.getParams() != null
                                        && !processorData.getParams().isEmpty()&&(processorData.getParams().get("cronExpression")!=null))?processorData.getParams().get("cronExpression"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </td>
    </tr>
    </tbody>
</table>

<table class="normal-nopadding">
    <tbody>
    <tr>
        <td colspan="2" class="buttonRow">
            <input type="submit" value="<fmt:message key="save"/>" class="button"
                   onclick="submitTextContent(document.Submit);"/>
            <input type="button" value="<fmt:message key="cancel"/>"
                   onclick="javascript:document.location.href='../message_processor/index.jsp?region=region1&item=messageProcessor_menu&ordinal=0'"
                   class="button"/>
        </td>
    </tr>
    </tbody>
</table>
<% } %>
</form>
</div>
</div>
<script type="text/javascript">
    editAreaLoader.init({
        id : "Value"        // textarea id
        ,syntax: "xml"            // syntax to be uses for highgliting
        ,start_highlight: true        // to display with highlight mode on start-up
    });
</script>
</fmt:bundle>