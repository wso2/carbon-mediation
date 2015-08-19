<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.mediation.throttle.stub.types.ThrottlePolicy" %>
<%@ page import="org.wso2.carbon.mediation.throttle.stub.types.InternalData" %>
<%@ page import="org.wso2.carbon.mediator.throttle.client.ThrottleClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%         
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    boolean popup = false;
    if ("true".equals(request.getParameter("popup"))) {
        popup = true;
    }
    
    String policyID = request.getParameter("policyID");
    Map policyXMLMap = (Map) request.getSession().getAttribute("throttle_policy_map");
    String policyXML = "";
    if (policyXMLMap != null) {
        policyXML = (String) policyXMLMap.get(policyID);
        if (policyXML == null) {
            policyXML = "";
        }
    }

    String param = "policyID=" + policyID;
    if (popup) {
        param = param + "&popup=true";
    }

    if (request.getParameter("backURL") != null) {
        session.setAttribute("backURL", request.getParameter("backURL"));
    }

    String BUNDLE = "org.wso2.carbon.mediator.throttle.ui.i18n.Resources";
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String header = bundle.getString("throttling.configuration.mediator");

    //Obtaining the client-side ConfigurationContext instance.
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    //Server URL which is defined in the server.xml
    String serverBackendURL = CarbonUIUtil.getServerURL(config.getServletContext(),
            session);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    ThrottlePolicy policy;

    try {
        ThrottleClient client = new ThrottleClient(cookie,
                serverBackendURL, configContext, request.getLocale());
        policy = client.toThrottlePolicy(policyXML, request.getParameter("loadDefault"));

    } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showWarningDialog("<%=e.getMessage()%>");
</script>
<%
        return;
    }

    String maxAccesses = "";
    if (policy.getMaxConcurrentAccesses() != 0) {
        maxAccesses = String.valueOf(policy.getMaxConcurrentAccesses());
    }

    String htmlString = "";
    String disableFields;
    int newRows = 0;

    //If the incoming internal configs are not empty, load them
    if (policy.getMaxConcurrentAccesses() != 0 ||
            policy.getInternalConfigs() != null && policy.getInternalConfigs()[0] != null) {

        InternalData[] data = policy.getInternalConfigs();
        if (data != null) {
            newRows = data.length;

            String opt1, opt2, opt3, opt4, opt5;
            int f2, f3, f4;
            for (int i = 0; i < data.length; i++) {
                opt1 = "IP";
                opt2 = "DOMAIN";
                opt3 = "Control";
                opt4 = "Deny";
                opt5 = "Allow";
                disableFields = "";
                if (data[i] != null) {
                    if (data[i].getAccessLevel() == 1) {
                        opt3 = "Deny";
                        opt4 = "Control";
                        disableFields = "disabled=\'true\'";
                    } else if (data[i].getAccessLevel() == 2) {
                        opt3 = "Allow";
                        opt5 = "Control";
                        disableFields = "disabled=\'true\'";
                    }
                    if (data[i].getRangeType().equals("DOMAIN")) {
                        opt1 = "DOMAIN";
                        opt2 = "IP";
                    }
                    f2 = data[i].getMaxRequestCount();
                    f3 = data[i].getUnitTime();
                    f4 = data[i].getProhibitTimePeriod();
                    htmlString += "<tr>\n" +
                            "            <td><input name=\"data" + (i + 1) + "1\" type=\"text\" " +
                            "size=\"15\" value=\"" + data[i].getRange() + "\"/></td>\n" +
                            "            <td><select name=\"data" + (i + 1) + "6\">\n" +
                            "                <option value=\"" + opt1 + "\">" + bundle.getString(opt1) + "</option>\n" +
                            "                <option value=\"" + opt2 + "\">" + bundle.getString(opt2) + "</option>\n" +
                            "            </select></td>\n" +
                            "            <td><input size=\"13\" name=\"data" + (i + 1) + "2\" " + disableFields + " type=\"text\" " +
                            "value=\"" + ((f2 == 0) ? "" : f2) + "\"/></td>\n" +
                            "            <td><input size=\"13\" name=\"data" + (i + 1) + "3\" " + disableFields + " type=\"text\" " +
                            "value=\"" + ((f3 == 0) ? "" : f3) + "\"/></td>\n" +
                            "            <td><input size=\"14\" name=\"data" + (i + 1) + "4\" " + disableFields + " type=\"text\" " +
                            "value=\"" + ((f4 == 0) ? "" : f4) + "\"/></td>\n" +
                            "            <td><select onchange=\"checkForAllow(this);\" name=\"data" + (i + 1) + "5\">\n" +
                            "                <option value=\"" + opt3 + "\">" + bundle.getString(opt3) + "</option>\n" +
                            "                <option value=\"" + opt4 + "\">" + bundle.getString(opt4) + "</option>\n" +
                            "                <option value=\"" + opt5 + "\">" + bundle.getString(opt5) + "</option>\n" +
                            "            </select></td>\n" +
                            "            <td><a href=\"#\" class=\"delete-icon-link\" onclick=\"removeRow(this);\" title=\"" + bundle.getString("throttling.delete.this.row") + "\">" + bundle.getString("throttling.delete") + "</a></td>\n" +
                            "        </tr>\n";
                } else {
                    newRows--;
                }
            }
        }
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.throttle.ui.i18n.Resources">
<%
    if (!popup) {
%>
<carbon:breadcrumb
        label="throttling.breadcrumbtext"
        resourceBundle="org.wso2.carbon.mediator.throttle.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    }
%>

<script type="text/javascript">

//set number of rows of the table at the back end
var rows = <%= newRows%>;

//Reset the names of the elements according to the order when needed
function resetIds(rowNumber) {
    var i;
    var j;
    var temp;
    for (i = rowNumber; i < rows; i++) {
        for (j = 1; j < 7; j++) {
            temp = document.getElementsByName("data" + (i + 1) + "" + j);
            temp[0].name = "data" + i + "" + j;
        }
    }
}

//add a new row to the table
function addRow() {
    rows = rows + 1;

    //add a row to the rows collection and get a reference to the newly added row
    var newRow = document.getElementById("dataTable").insertRow(-1);

    newRow.className = "even";

    //add 6 cells (<th>) to the new row and set the innerHTML to contain text boxes
    var oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input type='text' name='data" + rows + "1' size='15'/>";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<select name='data" + rows + "6'>" +
                      "<option value='IP'><fmt:message key="IP"/></option>" +
                      "<option value='DOMAIN'><fmt:message key="DOMAIN"/></option></select>";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input size='13' type='text' name='data" + rows + "2' disabled='true'/>";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input size='13' type='text' name='data" + rows + "3' disabled='true'/>";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<input size='14' type='text' name='data" + rows + "4' disabled='true'/>";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<select onchange=\'checkForAllow(this);\' name='data" + rows + "5'>" +
                      "<option value='Allow'><fmt:message key="Allow"/></option>" +
                      "<option value='Deny'><fmt:message key="Deny"/></option>" +
                      "<option value='Control'><fmt:message key="Control"/></option></select>";

    oCell = newRow.insertCell(-1);
    oCell.innerHTML = "<a href='#' class='delete-icon-link' onclick='removeRow(this);' title='<fmt:message key="throttling.delete.this.row"/>'><fmt:message key="throttling.delete"/></a>";
}

//deletes the specified row from the table
function removeRow(src) {
    var oRow = src.parentNode.parentNode;
    var index = oRow.rowIndex;

    //once the row reference is obtained, delete it passing in its rowIndex
    document.getElementById("dataTable").deleteRow(oRow.rowIndex);

    //Reset the names as one row is deleted
    resetIds(index);
    rows = rows - 1;
}

function submitForm() {
    var i;
    var temp1, temp2, temp3;
    var rangeEmpty = false, controlError = false;
    for (i = 1; i < rows + 1; i++) {
        temp1 = document.getElementsByName("data" + i + "" + 1);
        if (temp1[0].value == "") {
            rangeEmpty = true;
            break;
        }
        temp1 = document.getElementsByName("data" + i + "" + 5);
        temp2 = document.getElementsByName("data" + i + "" + 2);
        temp3 = document.getElementsByName("data" + i + "" + 3);
        if (temp1[0].value == "Control" && (temp2[0].value == "" || temp3[0].value == "")) {
            controlError = true;
            break;
        }
    }
    if (rows == 0 && document.getElementById("maxAccess").value == "") {
        CARBON.showErrorDialog("<fmt:message key="throttling.no.config"/>");
    } else if (rangeEmpty) {
        CARBON.showErrorDialog("<fmt:message key="throttling.range.empty"/>");
    } else if (controlError) {
        CARBON.showErrorDialog("<fmt:message key="throttling.no.max.req.count"/>");
    } else {
        var referenceString = collectionFormData();
        var url = "../throttling/trottle-policy-update_ajaxprocessor.jsp?enable=Yes&<%= param%>" + referenceString;
        jQuery.post(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showWarningDialog("Error Occurred!");
                    }
                });
    }
    hideEditor();
    return false;
}

//clear all the field on the UI
function clearAll() {
    document.getElementById("maxAccess").value = "";

    var i;
    var j;
    var temp;
    for (i = 1; i < rows + 1; i++) {
        for (j = 1; j < 7; j++) {
            temp = document.getElementsByName("data" + i + "" + j);
            if (j == 5) {
                temp[0].value = "Allow";
            } else if (j == 6) {
                temp[0].value = "IP";
            } else {
                temp[0].value = "";
            }
        }
    }
}

function collectionFormData() {

    var parameterString = "";

    var maxConAccess = document.getElementById("maxAccess").value
    if (maxConAccess && maxConAccess != "") {
        parameterString = "&maxAccess=" + maxConAccess;
    }
    var i;
    var j;
    for (i = 1; i < rows + 1; i++) {
        for (j = 1; j < 7; j++) {
            var dataID = "data" + i + "" + j;
            var temp = document.getElementsByName(dataID);
            if (temp != null && temp != undefined && temp.length >= 1) {
                var dataValue = temp[0].value ;
                if (dataValue != null && dataValue != undefined && dataValue != "") {
                    parameterString += "&" + dataID + "=" + dataValue;
                }
            }
        }
    }
    return parameterString;
}

function checkForAllow(element) {
    var elName = element.name;
    var row = elName.substring(4, 5);
    var i;
    var temp;
    for (i = 2; i < 5; i++) {
        temp = document.getElementsByName("data" + row + "" + i);
        if (element.value == "Allow" || element.value == "Deny") {
            temp[0].disabled = true;
        } else {
            temp[0].disabled = false;
        }
    }
}

function resetForm() {
    var url = "../throttling/trottle-policy-editor_ajaxprocessor.jsp?<%= param%>";
    jQuery("#popupContent").load(url, null,
            function(res, status, t) {
                if (status != "success") {
                    CARBON.showWarningDialog("Error Occurred!");
                }
            });
    return false;
}

function loadDefault() {
    var url = "../throttling/trottle-policy-editor_ajaxprocessor.jsp?loadDefault=true&<%= param%>";
    jQuery("#popupContent").load(url, null,
            function(res, status, t) {
                if (status != "success") {
                    CARBON.showWarningDialog("Error Occurred!");
                }
            });
    return false;
}

function hideEditor() {
    CARBON.closeWindow();
    return false;
}

</script>

<div id="middle">
    <h2><%= header%>
    </h2>

    <div id="workArea">

        <div id="formset">
            <form id="form3" name="dataForm" method="post" action="">
                <div id="policyConfiguration">
                    <table class="styledLeft">
                        <tr>
                            <td class="formRow">
                                <table width="100%" border="0" cellpadding="0" cellspacing="0" class="normal">
                                    <tr>
                                        <td nowrap="nowrap"><fmt:message key="thottling.maximum.concurrent.accesses"/>&nbsp;<input
                                                id="maxAccess" name="maxAccess" type="text" size="14"
                                                value="<%=maxAccesses%>"/></td>
                                    </tr>

                                </table>
                                <br/>
                                <table class="styledLeft" id="dataTable">
                                    <thead>
                                        <tr>
                                            <th><fmt:message key="throttling.range"/></th>
                                            <th><fmt:message key="throttling.type"/></th>
                                            <th><fmt:message key="throttling.max.request.count"/></th>
                                            <th><fmt:message key="throttling.unit.time.ms"/></th>
                                            <th><fmt:message key="throttling.prohibit.time.period.ms"/></th>
                                            <th><fmt:message key="throttling.access"/></th>
                                            <th><fmt:message key="throttling.actions"/></th>
                                        </tr>
                                    </thead>
                                    <%= htmlString%>
                                </table>
                                <a href="#" onclick="addRow();" class="add-icon-link"><fmt:message
                                        key="throttling.add.new.entry"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td class="buttonRow">
                                <input type="button" value="<fmt:message key="throttling.finish"/>"
                                       onclick="submitForm();" class="button"/>
                                <input type="button" value="<fmt:message key="throttling.reset"/>"
                                       onclick="resetForm();" class="button"/>
                                <input type="button" value="<fmt:message key="throttling.default"/>"
                                       onclick="loadDefault();" class="button"/>
                                <input type="button" value="<fmt:message key="throttling.clear"/>" onclick="clearAll();"
                                       class="button"/>
                                <input type="button" id="cancelButton" onclick="hideEditor();return false;"
                                       value="<fmt:message key="throttling.cancel" />" class="button"/>
                            </td>
                        </tr>
                    </table>
                </div>
            </form>

        </div>
    </div>
</div>
</fmt:bundle>
