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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.message.store.ui.MessageStoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.store.ui.utils.MessageStoreData" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.store.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.message.store.ui.i18n.JSResources"
               request="<%=request%>" i18nObjectName="messageStorei18n"/>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="localentrycommons.js"></script>

<carbon:breadcrumb
        label="custom.message.store"
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
        if (IsEmpty(form.Provider)) {
            CARBON.showWarningDialog('<fmt:message key="provider.field.cannot.be.empty"/>')
            form.Provider.focus();
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

    function specialCharValidator() {
        var iChars = "!,-@#$%^&*()+=-[]\\\';,./{}|\":<>?";
        for (var i = 0; i < document.Submit.Value.value.length; i++) {
            if (iChars.indexOf(document.Submit.Value.value.charAt(i)) != -1) {
                //CARBON.showWarningDialog('The Value field has special characters. \nThese are not allowed.\n')
                return true;
            }
        }
        return false;
    }

    function submitTextContent(value) {
        var table = document.getElementById('headerTable');
        var numberOfRows = table.rows.length;
        //alert("number of rows "+numberOfRows);
        var i = 0;
        for (i = 1; i < numberOfRows; i++) {
            var row = table.rows[i];
            var nameCell = row.cells[0];
            var valueCell = row.cells[1];
            var name = nameCell.firstChild.nodeValue;
            var value = valueCell.firstChild.nodeValue;
            document.getElementById("tableParams").value = document.getElementById("tableParams").value + "|" + name + "#" + value;

        }

        return true;

    }

    function showHideBackOffMuliPlier(selectTable) {
        var i = selectTable.selectedIndex
        if (selectTable.selectedIndex == 0) {

            document.getElementById('BackOffMultiplier').style.display = "block";
            document.getElementById('BackOffMultiplier_T').style.display = "block"

        }

        else {

            document.getElementById('BackOffMultiplier').style.display = "none";
            document.getElementById('BackOffMultiplier_T').style.display = "none"

        }

    }

    function hideElem(objid) {
        var theObj = document.getElementById(objid);
        if (theObj) {
            theObj.style.display = "none";
        }
    }
    function showElem(objid) {
        var theObj = document.getElementById(objid);
        if (theObj) {
            theObj.style.display = "";
        }
    }

    var customHeaders = Array();
    var customHeadersCount = 0;

    function addServiceParamRow(key, value) {
        addRow(key, value, 'headerTable', 'deleteServiceParamRow');
        customHeaders[customHeadersCount] = new Array(2);
        customHeaders[customHeadersCount]['name'] = key;
        customHeaders[customHeadersCount]['value'] = value;
        customHeadersCount++;

        document.getElementById('headerName').value = "";
        document.getElementById('headerValue').value = "";
    }

    function addRow(param1, param2, table, delFunction) {
        var tableElement = document.getElementById(table);
        var param1Cell = document.createElement('td');
        param1Cell.appendChild(document.createTextNode(param1));

        var param2Cell = document.createElement('td');
        param2Cell.appendChild(document.createTextNode(param2));

        var delCell = document.createElement('td');
        delCell.innerHTML = '<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

        var rowtoAdd = document.createElement('tr');
        rowtoAdd.appendChild(param1Cell);
        rowtoAdd.appendChild(param2Cell);
        rowtoAdd.appendChild(delCell);

        tableElement.tBodies[0].appendChild(rowtoAdd);
        tableElement.style.display = "";

        alternateTableRows(tableElement, 'tableEvenRow', 'tableOddRow');
    }

    function isParamAlreadyExist(paramName) {
        var i;
        for (i = 0; i < customHeadersCount; i++) {
            if (customHeaders[i]['name'] == paramName) {
                return true;
            }
        }
        return false;
    }

    function addServiceParams() {
        var headerName = document.getElementById('headerName').value;
        var headerValue = document.getElementById('headerValue').value;

        // trim the input values
        headerName = headerName.replace(/^\s*/, "").replace(/\s*$/, "");
        headerValue = headerValue.replace(/^\s*/, "").replace(/\s*$/, "");
        if (headerName != '' && headerValue != '') {
            if (isParamAlreadyExist(headerName)) {
                CARBON.showWarningDialog("<fmt:message key="parameter.already.exists"/>");
                return;
            }
            addServiceParamRow(headerName, headerValue);
            document.getElementById("addedParams").value = document.getElementById("addedParams").value + "," + headerName + ":" + headerValue;
        } else {
            CARBON.showWarningDialog("<fmt:message key="empty.key.or.value"/>");
        }
    }

    function deleteServiceParamRow(index) {
        CARBON.showConfirmationDialog("<fmt:message key="confirm.parameter.deletion"/>", function() {
            var table = document.getElementById('headerTable');
            var row = table.rows[index];
            var cell = row.cells[0];
            var content = cell.firstChild.nodeValue;
            document.getElementById("removedParams").value = document.getElementById("removedParams").value + "," + content;
            document.getElementById('headerTable').deleteRow(index);

            customHeaders.splice(index - 1, 1);
            customHeadersCount--;
            if (customHeadersCount == 0) {
                document.getElementById('headerTable').style.display = 'none';
            }
        });
    }

    function switchToSource() {
        if(!ValidateTextForm(document.Submit))  {
            return false;
        }
        addServiceParams();
        var messageStoreStr = {Name : document.getElementById("Name").value,Provider : document.getElementById("Provider").value, tableParams : document.getElementById("tableParams").value};
        jQuery.ajax({
            type: 'POST',
            url: 'updatePages/customMessageStoreUpdate.jsp',
            data: messageStoreStr,
            success: function(msg) {
                location.href = "sourceView.jsp";
            }
        });
    }


</script>

<div id="middle">
<h2><fmt:message key="custom.message.store"/></h2>

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
                            <fmt:message key="custom.message.store"/></span>
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
                    <td><fmt:message key="provider"/></td>
                    <td>
                        <input name="Provider" id="Provider" type="hidden"
                               value="<%=messageStore.getClazz()%>"/>
                        <label id="Provider_label" for="Provider"><%=messageStore.getClazz()%>
                        </label>
                        <br/>
                    </td>
                </tr>
                <%} else {%>
                <tr style="display:none;">
                    <td><fmt:message key="provider"/></td>
                    <td><input type="text" size="60" name="customStore" value="custom.store"/></td>
                </tr>
                <tr id="custom_class_input">
                    <td><fmt:message key="provider.class"/><span class="required"> *</span></td>
                    <td><input id="Provider" name="Provider" size="60"
                               value=""/></td>
                </tr>
                <%}%>
                </body>
            </table>
        </td>
    </tr>
    </tbody>
</table>

<table class="styledInner" cellpadding="0" cellspacing="0" style="margin-left: 0px;">
    <tr>
        <td>
            <table class="normal-nopadding">
                <tr>
                    <td width="20%">
                        <fmt:message key="messageStore.parameters"/>
                    </td>
                    <td>
                        <div id="nameValueAdd">
                            <table id="parametersTable">
                                <tbody>
                                <tr>
                                    <td>
                                        <fmt:message key="name"/>: <input type="text"
                                                                          id="headerName"/>
                                    </td>
                                    <td class="nopadding">
                                        <fmt:message key="value"/>: <input type="text"
                                                                           id="headerValue"/>
                                    </td>
                                    <td class="nopadding">
                                            <%--<input type="button" onclick="addServiceParams();" value="<fmt:message key="add.new"/>" class="button"/>--%>
                                        <a class="icon-link"
                                           href="#addNameLink"
                                           onclick="addServiceParams();"
                                           style="background-image: url(../admin/images/add.gif);"><fmt:message
                                                key="messageStore.parameters.add"/>
                                    </td>
                                    </a>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div>
                            <%
                                if (messageStore != null && messageStore.getParams() != null &&
                                        !messageStore.getParams().isEmpty()) {

                            %>

                            <table cellpadding="0" cellspacing="0" border="0"
                                   class="styledLeft"
                                   id="headerTable"
                                   style="display:block;">
                                <thead>
                                <tr>
                                    <th style="width:40%"><fmt:message
                                            key="param.name"/></th>
                                    <th style="width:40%"><fmt:message
                                            key="param.value"/></th>
                                    <th style="width:20%"><fmt:message
                                            key="param.action"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                <%
                                    Iterator<String> it = messageStore.getParams().
                                            keySet().iterator();
                                    while (it.hasNext()) {
                                        String key = it.next();
                                        String value = messageStore.getParams().get(key);
                                %>

                                <tr>
                                    <td>
                                        <%=key%>
                                    </td>
                                    <td>
                                        <%=value%>
                                    </td>
                                    <td><a id="deleteLink" href="#"
                                           onClick="deleteServiceParamRow(this.parentNode.parentNode.rowIndex)"
                                           alt="Delete" class="icon-link"
                                           style="background-image:url(../admin/images/delete.gif);">Delete</a>
                                    </td>
                                </tr>

                                <%
                                    }
                                %>
                                </tbody>
                            </table>

                            <%} else { %>
                            <table cellpadding="0" cellspacing="0" border="0"
                                   class="styledLeft"
                                   id="headerTable"
                                   style="display:none;">
                                <thead>
                                <tr>
                                    <th style="width:40%"><fmt:message
                                            key="param.name"/></th>
                                    <th style="width:40%"><fmt:message
                                            key="param.value"/></th>
                                    <th style="width:20%"><fmt:message
                                            key="param.action"/></th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                            <% }
                            %>
                        </div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
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
