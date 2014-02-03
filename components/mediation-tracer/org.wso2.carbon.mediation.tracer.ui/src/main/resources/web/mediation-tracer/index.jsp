<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediation.tracer.ui.client.MediationTracerServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.mediation.tracer.ui.i18n.Resources">
<carbon:breadcrumb
		label="mediationtracermenutext"
		resourceBundle="org.wso2.carbon.mediation.tracer.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />
<script src="../carbon/global-params.js" type="text/javascript"></script>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MediationTracerServiceClient client;
    String[] logs = new String[0];
    try {
        client = new MediationTracerServiceClient(cookie, backendServerURL, configContext,
                                                  request.getLocale());

        boolean isOperation = request.getParameter("op") != null;
        String opeation = request.getParameter("op");
        if (!isOperation) {
            String []returnLogs = client.getTraceLogs();
            if (!(returnLogs != null && returnLogs.length == 1 && (returnLogs[0] == null ||
                    returnLogs[0].equals("")))) {
                logs = returnLogs;
            }
        } else {
            if (opeation.equals("search")) {
                String key = request.getParameter("key");
                String ignoreCase = request.getParameter("ignoreCase");

                if (key == null) {
                    key = "";
                }

                boolean igCase = false;
                if (ignoreCase != null) {
                    igCase = Boolean.parseBoolean(ignoreCase);
                }
                String []returnLogs = client.searchLogs(key, igCase);
                if (!(returnLogs != null && returnLogs.length == 1 && (returnLogs[0] == null ||
                        returnLogs[0].equals("")))) {
                    logs = returnLogs;
                }
            } else if (opeation.equals("clear")) {
                client.clearLogs();
                String []returnLogs = client.getTraceLogs();
                if (!(returnLogs != null && returnLogs.length == 1 && (returnLogs[0] == null ||
                        returnLogs[0].equals("")))) {
                    logs = returnLogs;
                }
            }
        }
    } catch (Exception e) {
%>
<script type="text/javascript">
   window.location.href = "../admin/error.jsp";
</script>
<%
    }
%>

<script type="text/javascript">

wso2.wsf.Util.initURLs();

var frondendURL = wso2.wsf.Util.getServerURL() + "/";

function getResponseValue(responseXML) {
    var returnElementList = responseXML.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
    if (returnElementList.length == 0)
        returnElementList = responseXML.getElementsByTagName("return");
    var returnElement = returnElementList[0];

    return returnElement.firstChild.nodeValue;
}

function clearAll() {
    var bodyXML = '<ns1:clearTraceLogs xmlns:ns1="http://tracer.mediation.carbon.wso2.org">' +
                  '</ns1:clearTraceLogs>';
    var callURL = wso2.wsf.Util.getBackendServerURL(frondendURL, "<%=backendServerURL%>") +
                  "MediationTracerService/clearTraceLogs";
    new wso2.wsf.WSRequest(callURL, "urn:clearTraceLogs", bodyXML, clearTraceLogsCallback, undefined, undefined, getProxyAddress(), false);
}

function clearTraceLogsCallback() {
    var data = this.req.responseXML;
    var responseTextValue = getResponseValue(data);
    if (responseTextValue == "true") {
        var tbody = document.getElementById("traceLogsTableBody");
        var table = tbody.parentNode;
        table.removeChild(tbody);
        var newTBody = document.createElement("tbody");
        var idAttr = document.createAttribute('id');
        idAttr.value = "traceLogsTableBody";
        newTBody.attributes.setNamedItem(idAttr);
        table.appendChild(newTBody);
        var tr = document.createElement("tr");
        var td = document.createElement("td");
        tr.appendChild(td);
        td.innerHTML = '<fmt:message key="no.trace.entries"/>';
        newTBody.appendChild(tr);
        alternateTableRows('traceLogsTable', 'tableEvenRow', 'tableOddRow');
    } else {
        CARBON.showWarningDialog('<fmt:message key="unable.to.clear.logs"/>');
    }
}

function searchTrace() {
    var keyword = document.getElementById("tracekeyword");
    if (keyword != null && keyword != undefined && keyword.value != null &&
        keyword.value != undefined) {
        var ignore_case = document.getElementById("ignore_case");
        var ignoreCase = false;
        if (ignore_case != null) {
            if (ignore_case.checked) {
                ignoreCase = true;
            }
        }
        var bodyXML = '<searchTraceLog xmlns="http://tracer.mediation.carbon.wso2.org">' +
                      '<keyword>' + keyword.value + '</keyword>' +
                      '<ignoreCase>' + ignoreCase.toString() + '</ignoreCase>' +
                      '</searchTraceLog>';
        var callURL = wso2.wsf.Util.getBackendServerURL(frondendURL, "<%=backendServerURL%>") +
                      "MediationTracerService/searchTraceLog";
        new wso2.wsf.WSRequest(callURL, "urn:searchTraceLog", bodyXML, searchTraceLogsCallback, undefined, undefined, getProxyAddress(), false);
    }
}

function searchTraceLogsCallback() {
    var data = this.req.responseXML;
    var rows = data.getElementsByTagName("ns:return");
    // Older browsers might not recognize namespaces (e.g. FF2)
    if (rows.length == 0) {
        rows = data.getElementsByTagName("return");
    }

    var tbody = document.getElementById("traceLogsTableBody");
    var table = tbody.parentNode;
    table.removeChild(tbody);
    var newTBody = document.createElement("tbody");
    var idAttr = document.createAttribute('id');
    idAttr.value = "traceLogsTableBody";
    newTBody.attributes.setNamedItem(idAttr);
    table.appendChild(newTBody);
    if (rows != null || rows != undefined) {
        for (var i=0; i< rows.length;i++) {
            var tr = document.createElement("tr");
            var td = document.createElement("td");
            tr.appendChild(td);
            td.innerHTML = rows[i].firstChild.nodeValue;
            newTBody.appendChild(tr);
        }
    }
    alternateTableRows('traceLogsTable', 'tableEvenRow', 'tableOddRow');
}

    function searchTraceNew() {
        var key = document.getElementById('tracekeyword').value;
        var ignoreCase = document.getElementById('ignore_case');
        var isIgonreCases = false;
        if (ignoreCase.checked) {
            isIgonreCases = true;
        }
        document.location.href = "index.jsp?op=search&key=" + key + "&ignoreCase=" + isIgonreCases;
    }

    function clearAllNew() {
        document.location.href = "index.jsp?op=clear";
    }
</script>

<div id="middle">
    <h2><fmt:message key="mediation.message.tracer"/></h2>

    <div id="workArea">

        <table class="normal">
            <tbody>
                <tr>
                    <td><input type="text" id="tracekeyword" value=""/>
                        <input type="button" class="button" onclick="searchTraceNew();" value="<fmt:message key="search"/>"/>
                    </td>
                    <td><input type="checkbox" id="ignore_case"/><fmt:message key="ignore.case"/></td>
                </tr>
            </tbody>
        </table>
        <div id="tracelogdiv" style="overflow-y:auto;overflow-x:auto;height:500px;margin-top:10px">
            <table class="styledLeft" id="traceLogsTable" style="width:99% !important">
                <tbody id="traceLogsTableBody">
                    <%
                        for (String log : logs) {

                    %>
                    <tr>
                        <td><%=log%>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>

            <script type="text/javascript">
                alternateTableRows('traceLogsTable', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
        <div class="buttonrow" style="padding-top:10px">
            <input type="button" class="button" value='<fmt:message key="clearAll"/>' onclick="clearAllNew();"/>
        </div>
    </div>
</div>
</fmt:bundle>
