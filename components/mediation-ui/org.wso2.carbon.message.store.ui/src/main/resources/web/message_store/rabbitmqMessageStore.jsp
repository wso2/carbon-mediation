<%--
 ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.message.store.ui.MessageStoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.store.ui.utils.MessageStoreData" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.store.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.store.ui.i18n.JSResources"
               request="<%=request%>" i18nObjectName="messageStorei18n"/>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="localentrycommons.js"></script>

<carbon:breadcrumb
        label="jms.message.store"
        resourceBundle="org.wso2.carbon.message.store.ui.i18n.Resources"
        topPage="true"
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
        else if (IsEmpty(form.host_name)) {
            CARBON.showWarningDialog('<fmt:message key="host.name.cannot.be.empty"/>')
            form.factory_initial.focus();
            return false;
        }
        else if (IsEmpty(form.host_port)) {
            CARBON.showWarningDialog('<fmt:message key="host.port.cannot.be.empty"/>')
            form.provider_url.focus();
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
        addServiceParams();
        return true;
    }

    function addServiceParams() {
        addServiceParameter("store.rabbitmq.host.name", document.getElementById('host_name').value);
        addServiceParameter("store.rabbitmq.host.port", document.getElementById('host_port').value);
        addServiceParameter("store.rabbitmq.queue.name", document.getElementById('queue_name').value);
        addServiceParameter("store.rabbitmq.exchange.name", document.getElementById('exchange_name').value);
        addServiceParameter("store.rabbitmq.route.key", document.getElementById('route_key').value);
        addServiceParameter("store.rabbitmq.virtual.host", document.getElementById('virtual_host').value);
        addServiceParameter("store.jms.username", document.getElementById('rabbitmq_username').value);
        addServiceParameter("store.jms.password", document.getElementById('rabbitmq_password').value);
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
        var messageStoreStr = {Name : document.getElementById("Name").value, tableParams : document.getElementById("tableParams").value};
        jQuery.ajax({
            type: 'POST',
            url: 'updatePages/jmsMessageStoreUpdate.jsp',
            data: messageStoreStr,
            success: function(msg) {
                location.href = "sourceView.jsp";
            }
        });
    }


</script>

<div id="middle">
<h2><fmt:message key="jms.message.store"/></h2>

<div id="workArea">
<form name="Submit" action="ServiceCaller.jsp" method="POST"
      onsubmit="javascript:return ValidateTextForm(this)">
<input type="hidden" id="addedParams" name="addedParams" value=""/>
<input type="hidden" id="removedParams" name="removedParams" value=""/>
<input type="hidden" id="tableParams" name="tableParams" value="PARAMS:"/>
<%
    String origin = request.getParameter("origin");

    String messageStoreName = request.getParameter("messageStoreName");
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MessageStoreAdminServiceClient client = new MessageStoreAdminServiceClient(cookie, url, configContext);
    String[] messageStores = client.getMessageStoreNames();

    MessageStoreData messageStore = null;

    if (messageStoreName != null) {
        session.setAttribute("edit" + messageStoreName, "true");
        for (String name : messageStores) {
            if (name != null && name.equals(messageStoreName)) {
                messageStore = client.getMessageStore(name);
            }
        }
    } else if (origin != null && !"".equals(origin)) {
        String msString = (String) session.getAttribute("messageStoreConfiguration");
        String msName = (String) session.getAttribute("msName");
        String msProvider = (String) session.getAttribute("msProvider");

        session.removeAttribute("messageStoreConfiguration");
        session.removeAttribute("msName");
        session.removeAttribute("msProvider");

        msString = msString.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
        OMElement messageStoreElement = AXIOMUtil.stringToOM(msString);
        messageStore = new MessageStoreData(messageStoreElement.toString());
        messageStore.setName(msName);
        messageStore.setClazz(msProvider);
    }

%>

<table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
    <thead>
    <tr>
        <th colspan="2"><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="jms.message.store"/></span>
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
                <tbody>
                <input type="hidden" name="pageName" value="manageMessageStore.jsp"/>
                <%if (messageStore != null) {%>
                <tr>

                    <td width="271px"><fmt:message key="name"/><span class="required"> *</span></td>
                    <td>
                        <input id="Name" name="Name" type="hidden"
                               value="<%=messageStore.getName()%>"/>
                        <label for="Name"><%=messageStore.getName()%>
                        </label>
                    </td>
                </tr>
                <%} else { %>
                <tr>
                    <td width="271px"><fmt:message key="name"/><span class="required"> *</span></td>
                    <td><input type="text" size="60" id="Name" name="Name" value=""/></td>
                </tr>
                <%}%>
                <%if ((messageStore != null)) { %>
                <tr>
                    <td><fmt:message key="provider"/><span class="required"> *</span></td>
                    <td>
                        <input name="Provider" id="Provider" type="hidden"
                               value="org.apache.synapse.message.store.impl.jms.JmsStore"/>
                        <label id="Provider_label" for="Provider">org.apache.synapse.message.store.impl.jms.JmsStore
                        </label>
                        <br/>
                    </td>
                </tr>
                <%} else {%>
                <input name="Provider" id="Provider" type="hidden"
                       value="org.apache.synapse.message.store.impl.jms.JmsStore"/>
                <%}%>
                <tr>
                    <td><fmt:message key="factory.initial"/><span class="required"> *</span></td>
                    <td><input type="text" size="60" id="factory_initial" name="factory_initial"
                               value="<%=((null!=messageStore)&&(messageStore.getParams().get("java.naming.factory.initial")!=null))?messageStore.getParams().get("java.naming.factory.initial"):""%>"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="provider.url"/><span class="required"> *</span></td>
                    <td><input type="text" size="60" id="provider_url" name="provider_url"
                               value="<%=((null!=messageStore)&&(messageStore.getParams().get("java.naming.provider.url")!=null))?messageStore.getParams().get("java.naming.provider.url"):""%>"/>
                    </td>
                </tr>

                <tr>
                    <td>
                                 <span id="_adv" style="float: left; position: relative;">
                                <a class="icon-link" onclick="javascript:showAdvancedOptions('');"
                                   style="background-image: url(images/down.gif);"><fmt:message
                                        key="show.additional.parameters"/></a>
                                </span>
                    </td>
                </tr>
                </tbody>
            </table>

            <div id="_advancedForm" style="display:none">

                <table class="normal-nopadding">
                    <tbody>
                    <tr>
                        <td colspan="2" class="sub-header"><fmt:message key="additional.parameters"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.jms.destination"/></td>
                        <td><input type="text" id="jms_destination" name="jms_destination"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jms.destination")!=null))?messageStore.getParams().get("store.jms.destination"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.jms.connection.factory"/></td>
                        <td><input type="text" id="jms_connection_factory" name="jms_connection_factory"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jms.connection.factory")!=null))?messageStore.getParams().get("store.jms.connection.factory"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.jms.username"/></td>
                        <td><input type="text" id="jms_username" name="jms_username"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jms.username")!=null))?messageStore.getParams().get("store.jms.username"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.jms.password"/></td>
                        <td><input type="password" id="jms_password" name="jms_password"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jms.password")!=null))?messageStore.getParams().get("store.jms.password"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.jms.JMSSpecVersion"/></td>
                        <td>
                            <select id="jms_spec_version">
                                <%
                                    if ((null != messageStore) && (messageStore.getParams().get("store.jms.JMSSpecVersion") != null) && (messageStore.getParams().get("store.jms.JMSSpecVersion").equals("1.0"))) {
                                %>
                                <option value="1.1">1.1</option>
                                <option selected="selected" value="1.0">1.0</option>

                                <%} else {%>

                                <option selected="selected" value="1.1">1.1</option>
                                <option value="1.0">1.0</option>
                                <%}%>
                            </select>
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
                   onclick="javascript:document.location.href='index.jsp'"
                   class="button"/>
        </td>
    </tr>
    </tbody>
</table>
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