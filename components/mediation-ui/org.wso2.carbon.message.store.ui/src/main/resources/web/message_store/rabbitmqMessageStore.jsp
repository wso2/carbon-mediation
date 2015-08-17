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
        label="rabbitmq.message.store"
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
        else if (document.getElementById('enable_guaranteed_delivery').options[document.getElementById
             ('enable_guaranteed_delivery').selectedIndex].value == "true" && document.getElementById
             ('failover_message_store_name').selectedIndex == -1) {
              CARBON.showWarningDialog('<fmt:message key="failover.message.store.cannot.be.empty"/>')
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
        addServiceParameter("store.rabbitmq.username", document.getElementById('rabbitmq_username').value);
        addServiceParameter("store.rabbitmq.password", document.getElementById('rabbitmq_password').value);
        addServiceParameter("store.producer.guaranteed.delivery.enable", document.getElementById('enable_guaranteed_delivery').options[document.getElementById('enable_guaranteed_delivery').selectedIndex].value);
        addServiceParameter("store.failover.message.store.name", document.getElementById('failover_message_store_name').options[document.getElementById('failover_message_store_name').selectedIndex].value);

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

    function showGuaranteedDeliveryOptions(id) {
        var formElem = document.getElementById(id + '_guaranteedDeliveryForm');
        if (formElem.style.display == 'none') {
            formElem.style.display = '';
            document.getElementById(id + '_adv_gur_delivery').innerHTML = '<a class="icon-link" ' +
                'onclick="javascript:showGuaranteedDeliveryOptions(\'' + id + '\');" style="background-image: url(images/up.gif);">' + "<fmt:message key="hide.guaranteed.delivery.parameters"/>" + '</a>';
        } else {
            formElem.style.display = 'none';
            document.getElementById(id + '_adv_gur_delivery').innerHTML = '<a class="icon-link" ' +
                 'onclick="javascript:showGuaranteedDeliveryOptions(\'' + id + '\');" style="background-image: url(images/down.gif);">' + "<fmt:message key="show.guaranteed.delivery.parameters"/>" + '</a>';
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
            url: 'updatePages/rabbitmqMessageStoreUpdate.jsp',
            data: messageStoreStr,
            success: function(msg) {
                location.href = "sourceView.jsp";
            }
        });
    }


</script>

<div id="middle">
<h2><fmt:message key="rabbitmq.message.store"/></h2>

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
    String failoverMessageStoreName = "";

    if (messageStoreName != null) {
        session.setAttribute("edit" + messageStoreName, "true");
        for (String name : messageStores) {
            if (name != null && name.equals(messageStoreName)) {
                messageStore = client.getMessageStore(name);
                if(messageStore.getParams().get("store.failover.message.store.name") != null) {
                    failoverMessageStoreName = messageStore.getParams().get("store.failover.message.store.name");
                }
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
                            <fmt:message key="rabbitmq.message.store"/></span>
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
                               value="org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore"/>
                        <label id="Provider_label" for="Provider">org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore
                        </label>
                        <br/>
                    </td>
                </tr>
                <%} else {%>
                <input name="Provider" id="Provider" type="hidden"
                       value="org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore"/>
                <%}%>
                <tr>
                    <td><fmt:message key="rabbitmq.host.name"/><span class="required"> *</span></td>
                    <td><input type="text" size="60" id="host_name" name="host_name"
                               value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.host.name")!=null))?messageStore.getParams().get("store.rabbitmq.host.name"):""%>"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="rabbitmq.host.port"/><span class="required"> *</span></td>
                    <td><input type="text" size="60" id="host_port" name="host_port"
                               value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.host.port")!=null))?messageStore.getParams().get("store.rabbitmq.host.port"):""%>"/>
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
                <tr>
                    <td>
                         <span id="_adv_gur_delivery" style="float: left; position: relative;">
                         <a class="icon-link" onclick="javascript:showGuaranteedDeliveryOptions('');"
                            style="background-image: url(images/down.gif);">
                         <fmt:message key="show.guaranteed.delivery.parameters"/></a>
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
                        <td><fmt:message key="store.rabbitmq.queue.name"/></td>
                        <td><input type="text" id="queue_name" name="queue_name"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.queue.name")!=null))?messageStore.getParams().get("store.rabbitmq.queue.name"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.rabbitmq.exchange.name"/></td>
                        <td><input type="text" id="exchange_name" name="exchange_name"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.exchange.name")!=null))?messageStore.getParams().get("store.rabbitmq.exchange.name"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.rabbitmq.route.key"/></td>
                        <td><input type="text" id="route_key" name="route_key"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.route.key")!=null))?messageStore.getParams().get("store.rabbitmq.route.key"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.rabbitmq.username"/></td>
                        <td><input type="text" id="rabbitmq_username" name="rabbitmq_username"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.username")!=null))?messageStore.getParams().get("store.rabbitmq.username"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.rabbitmq.password"/></td>
                        <td><input type="password" id="rabbitmq_password" name="rabbitmq_password"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.password")!=null))?messageStore.getParams().get("store.rabbitmq.password"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.rabbitmq.virtual.host"/></td>
                        <td><input type="text" id="virtual_host" name="virtual_host"
                                   value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.rabbitmq.virtual.host")!=null))?messageStore.getParams().get("store.rabbitmq.virtual.host"):""%>"
                                   size="75"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div id="_guaranteedDeliveryForm" style="display:none">
                <table class="normal-nopadding">
                    <tbody>
                    <tr>
                        <td colspan="2" class="sub-header"><fmt:message key="failover.configuration.parameters"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="store.producer.guaranteed.enable"/></td>
                        <td>
                            <select id="enable_guaranteed_delivery">
                        <%
                            if ((null != messageStore) && (messageStore.getParams().get("store.producer.guaranteed.delivery.enable") != null) && (messageStore.getParams().get("store.producer.guaranteed.delivery.enable").equals("true"))) {
                        %>
                            <option selected="selected" value="true">True</option>
                            <option value="false">False</option>
                        <%} else {%>
                            <option value="true">True</option>
                            <option selected="selected" value="false">False</option>
                        <%}%>
                            </select>
                        </td>
                    </tr>

                    <%if (messageStore != null) {%>
                    <tr>
                        <td><fmt:message key="store.failover.messagestore.name"/></td>
                        <td>
                            <select id="failover_message_store_name" name="MessageStore">
                        <%
                            if(messageStores != null) {

                            for (String msn : messageStores) {

                            if(failoverMessageStoreName != null && msn.equals(failoverMessageStoreName)) {
                        %>
                            <option value="<%=msn%>" selected="true"><%=msn%></option>
                        <%
                           } else {
                        %>
                            <option  value="<%=msn%>"><%=msn%></option>
                        <%
                            }}}
                        %>
                             </select>
                        </td>
                    </tr>
                    <%} else { %>

                    <tr>
                       <td><fmt:message key="store.failover.messagestore.name"/></td>
                       <td>
                            <select id="failover_message_store_name" name="MessageStore">
                       <%
                            if(messageStores != null) {

                            for (String msn : messageStores)
                       {%>
                            <option selected="true" value="<%=msn%>"><%=msn%></option>
                       <%
                            }}
                        %>
                           </select>
                       </td>
                    </tr>
                    <%}%>
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