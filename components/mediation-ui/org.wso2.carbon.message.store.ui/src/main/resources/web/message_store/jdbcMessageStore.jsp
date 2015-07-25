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
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.store.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.store.ui.i18n.JSResources"
               request="<%=request%>" i18nObjectName="messageStorei18n"/>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="localentrycommons.js"></script>

<script type="text/javascript" src="js/messageStore-util.js"></script>

<carbon:breadcrumb
        label="jdbc.message.store"
        resourceBundle="org.wso2.carbon.message.store.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<%
    boolean isPool = true;
    boolean isInline = false;
    boolean displayCommonProps = false;
    boolean displayExisistingDs = false;



    String origin = request.getParameter("origin");

    String messageStoreName = request.getParameter("messageStoreName");
    String serverUrl = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MessageStoreAdminServiceClient client = new MessageStoreAdminServiceClient(cookie, serverUrl, configContext);
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

    // Fetch carbon ndatasource list
    List<String> sourceList =null;

    try {
        sourceList = client.getAllDataSourceInformations();
    }catch (Exception e){
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }

    // If already created using datasource, get make datasource radio-button selected
    if ((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.dsName")!=null)) isPool = false;

    if (!isPool && !isInline) displayExisistingDs = true;
    if (isInline || isPool) displayCommonProps = true;
%>

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
        var isPool = document.getElementById('radio_pool').checked;

        if (IsEmpty(form.Name)) {
            CARBON.showWarningDialog('<fmt:message key="name.field.cannot.be.empty"/>')
            form.Name.focus();
            return false;
        }

        if (IsEmpty(form.store_table)) {
            CARBON.showWarningDialog('<fmt:message key="table.field.cannot.be.empty"/>')
            form.store_table.focus();
            return false;
        }

        if(isPool){
            if (IsEmpty(form.driver)) {
                CARBON.showWarningDialog('<fmt:message key="driver.field.cannot.be.empty"/>')
                form.driver.focus();
                return false;
            }
            if (IsEmpty(form.url)) {
                CARBON.showWarningDialog('<fmt:message key="url.field.cannot.be.empty"/>')
                form.url.focus();
                return false;
            }
            if (IsEmpty(form.user)) {
                CARBON.showWarningDialog('<fmt:message key="user.field.cannot.be.empty"/>')
                form.user.focus();
                return false;
            }
        }  else {
            if (IsEmpty(form.data_source)) {
                CARBON.showWarningDialog('<fmt:message key="data.source.field.cannot.be.empty"/>')
                form.data_source.focus();
                return false;
            }
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
        if (document.getElementById('radio_pool').checked) {
            addServiceParameter("store.jdbc.driver", document.getElementById('driver').value);
            addServiceParameter("store.jdbc.connection.url", document.getElementById('url').value);
            addServiceParameter("store.jdbc.username", document.getElementById('user').value);
            addServiceParameter("store.jdbc.password", document.getElementById('password').value);
        } else {
            addServiceParameter("store.jdbc.dsName", document.getElementById('data_source').options[document.getElementById('data_source').selectedIndex].value);
        }

        addServiceParameter("store.jdbc.table", document.getElementById('store_table').value);
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

    function switchToSource() {
        if(!ValidateTextForm(document.Submit))  {
            return false;
        }
        addServiceParams();
        var messageStoreStr = {Name : document.getElementById("Name").value, tableParams : document.getElementById("tableParams").value};
        jQuery.ajax({
            type: 'POST',
            url: 'updatePages/jdbcMessageStoreUpdate.jsp',
            data: messageStoreStr,
            success: function(msg) {
                location.href = "sourceView.jsp";
            }
        });
    }

</script>

<div id="middle">
    <h2><fmt:message key="jdbc.message.store"/></h2>

    <div id="workArea">
        <form name="Submit" action="ServiceCaller.jsp" method="POST"
              onsubmit="javascript:return ValidateTextForm(this)">
            <input type="hidden" id="addedParams" name="addedParams" value=""/>
            <input type="hidden" id="removedParams" name="removedParams" value=""/>
            <input type="hidden" id="tableParams" name="tableParams" value="PARAMS:"/>

            <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                <thead>
                <tr>
                    <th colspan="2"><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="jdbc.message.store"/></span>
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
                                <td><input id="Name" type="text" size="60" name="Name" value=""/></td>
                            </tr>
                            <%}%>
                            <%if ((messageStore != null)) { %>
                            <tr>
                                <td><fmt:message key="provider"/><span class="required"> *</span></td>
                                <td>
                                    <input name="Provider" id="Provider" type="hidden"
                                           value="org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore"/>
                                    <label id="Provider_label"
                                           for="Provider">org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore
                                    </label>
                                    <br/>
                                </td>
                            </tr>
                            <%} else {%>
                            <input id="Provider" name="Provider" type="hidden"
                                   value="org.apache.synapse.message.store.impl.jdbc.JDBCMessageStore"/>
                            </tr>
                            <%}%>

                            <tr>
                                <td><fmt:message key="store.jdbc.table"/><span class="required"> *</span></td>
                                <td><input type="text" size="60" id="store_table" name="store_table"
                                           value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.table")!=null))?messageStore.getParams().get("store.jdbc.table"):""%>"/>
                                </td>
                            </tr>

                            <tr>
                                <td style="width:150px"><fmt:message key="store.jdbc.connInfo"/></td>
                                <td><input type="radio" id="radio_pool" onclick="poolOnClick()"
                                           name="connectiongroup" value="poolgroup"
                                        <%=isPool ? "checked=\"checked\"" : ""%>/>
                                    <label><fmt:message key="store.jdbc.pool"/></label>
                                    <input type="radio" id="radio_datasource" onclick="sourceOnClick()"
                                           name="connectiongroup" value="datasourceprop" <%=!isPool ? "checked=\"checked\"" : ""%>/>
                                    <label><fmt:message key="store.jdbc.source.exist"/></label>
                                </td>
                            </tr>

                            <!--
                            <tr id="sourceGroup" <%=isPool ? "style=\"display:none\";" : ""%>>
                                <td style="width:150px"><fmt:message key="store.jdbc.source.type"/></td>
                                <td>
                                    <input type="radio" id="sourceTypeInline"
                                           onclick="inlineOnClick()" value="inline"
                                           name="sourceType"
                                        <%=isInline ? "checked=\"checked\"" : ""%>/>
                                    <label><fmt:message key="store.jdbc.source.inline"/></label>

                                    <input type="radio" id="sourceTypeExisting"
                                           onclick="existingOnClick()" value="existing"
                                           name="sourceType" <%=!isInline ? "checked=\"checked\"" : ""%>/>
                                    <label><fmt:message key="store.jdbc.source.exist"/></label>
                                </td>
                            </tr>
                            -->

                            <tr id="dataSourceSelect" <%=!displayExisistingDs ? "style=\"display:none\";" : ""%>>
                                <td style="width:150px"><fmt:message key="store.dsName"/></td>
                                <td>
                                    <table>
                                        <tbody>
                                        <tr>
                                            <td>
                                                <select name="data_source" id="data_source">
                                                    <option value=""
                                                            <%=((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.dsName")==null))?"selected":""%>
                                                                        >--SELECT--</option>
                                                    <%
                                                        if (sourceList != null) {

                                                            for (String name : sourceList) {
                                                    %>
                                                                <option
                                                                    value=<%=name%>
                                                                    <%=((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.dsName")!=null)&&(messageStore.getParams().get("store.jdbc.dsName").equals(name)))?"selected":""%>
                                                                        ><%=name%> </option>
                                                    <%
                                                            }
                                                        }
                                                    %>
                                                </select>

                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                            </tr>

                            <tr id="store.jdbc.driver_row" <%=!isPool ? "style=\"display:none;\"" : ""%>>
                                <td style="width:150px">
                                    <fmt:message key="store.driver"/>
                                    <font style="color: red; font-size: 8pt;"> *</font>
                                </td>
                                <td><input style="width:300px" type="text" name="driver" id="driver" class="longTextField"
                                           value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.driver")!=null))?messageStore.getParams().get("store.jdbc.driver"):""%>"/></td>
                            </tr>
                            <%--<tr id="store.jdbc.inictx_row" <%=!isPool && isInline ? "" : "style=\"display:none;\""%>>
                                <td style="width:150px">
                                    <fmt:message key="store.contex"/>
                                </td>
                                <td><input type="text" style="width:300px" name="init_ctx" id="init_ctx" value="<%=initCtx%>"/></td>
                            </tr>
                            <tr id="store.jdbc.ds_row" <%=!isPool && isInline ? "" : "style=\"display:none;\""%>>
                                <td>
                                    <fmt:message key="store.dsName"/>
                                    <font style="color: red; font-size: 8pt;"> *</font>
                                </td>
                                <td><input type="text" style="width:300px" name="ext_data_source" id="ext_data_source" value="<%=dsName%>"/></td>
                            </tr>--%>
                            <tr id='store.jdbc.url' <%=!displayCommonProps ? "style=\"display:none;\"" : ""%>>
                                <td>
                                    <fmt:message key="store.url"/>
                                    <font style="color: red; font-size: 8pt;"> *</font>
                                </td>
                                <td><input type="text" style="width:300px" name="url" id="url"
                                           value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.connection.url")!=null))?messageStore.getParams().get("store.jdbc.connection.url"):""%>"/></td>
                            </tr>
                            <tr id="store.jdbc.user" <%=!displayCommonProps ? "style=\"display:none;\"" : ""%>>
                                <td>
                                    <fmt:message key="store.user"/>
                                    <font style="color: red; font-size: 8pt;"> *</font>
                                </td>
                                <td><input type="text" style="width:300px"  name="user" id="user"
                                           value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.username")!=null))?messageStore.getParams().get("store.jdbc.username"):""%>"/></td>
                            </tr>
                            <tr id="store.jdbc.passwd" <%=!displayCommonProps ? "style=\"display:none;\"" : ""%>>
                                <td>
                                    <fmt:message key="store.password"/>
                                    <%--<font style="color: red; font-size: 8pt;"> *</font>--%>
                                </td>
                                <td><input type="password" name="password" id="password" style="width:300px"
                                           value="<%=((null!=messageStore)&&(messageStore.getParams().get("store.jdbc.password")!=null))?messageStore.getParams().get("store.jdbc.password"):""%>"/></td>
                            </tr>

                            </tbody>
                        </table>

                        <div id="_advancedForm" style="display:none"></div>


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
        ,syntax: "xml"            // syntax to be uses for highlighting
        ,start_highlight: true        // to display with highlight mode on start-up
    });
</script>
</fmt:bundle>
