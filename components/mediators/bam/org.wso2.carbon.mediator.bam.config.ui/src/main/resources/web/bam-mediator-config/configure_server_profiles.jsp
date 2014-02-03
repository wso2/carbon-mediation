<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.BamServerProfileUtils" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.BamServerConfig" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    response.setHeader("Cache-Control", "no-cache");
    String labelName = request.getParameter("txtServerProfileLocation");
    if(!(labelName != null && !labelName.equals(""))){
        labelName = "New Profile";
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources">

<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources"
        request="<%=request%>" i18nObjectName="bamjsi18n"/>
<carbon:breadcrumb
        label="<%=labelName%>"
        resourceBundle="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<%! public static final String PROPERTY_VALUES = "propertyValues";
    public static final String PROPERTY_KEYS = "propertyKeys";
    public static final String STREAM_NAMES = "streamNames";
    public static final String STREAM_VERSIONS = "streamVersions";
    public static final String STREAM_NICKNAME = "streamNickname";
    public static final String STREAM_DESCRIPTION = "streamDescription";
    public static final String SERVER_PROFILE_LOCATION = "bamServerProfiles";
%>

<%
    String userName = "";
    String password = "";
    String urlSet = "";
    String ip = "";
    String authenticationPort = "";
    String receiverPort = "";
    String security = "true";
    String loadbalancer = "false";
    String serverProfileLocation = "";
    String serverProfileName = "";
    String action = "";
    String force = "false";
    String streamTable = "";

    BamServerConfig bamServerConfig = new BamServerConfig();
    List<StreamConfiguration> streamConfigurations;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    BamServerProfileUtils bamServerProfileUtils =
            new BamServerProfileUtils(cookie, backendServerURL, configContext, request.getLocale());

    String tmpUserName = request.getParameter("txtUsername");
    if(tmpUserName != null && !tmpUserName.equals("")){
        userName = tmpUserName;
    }


    String tmpPassword = request.getParameter("txtPassword");
    if(tmpPassword != null && !tmpPassword.equals("")){
        password = tmpPassword;
    }

    String tmpUrlSet = request.getParameter("urlSet");
    if(tmpUrlSet != null && !tmpUrlSet.equals("")){
        urlSet = tmpUrlSet;
    }

    String tmpIp = request.getParameter("txtIp");
    if(tmpIp != null && !tmpIp.equals("")){
        ip = tmpIp;
    }

    String tmpAuthPort = request.getParameter("authPort");
    if(tmpAuthPort != null && !tmpAuthPort.equals("")){
        authenticationPort = tmpAuthPort;
    }

    String tmpReceiverPort = request.getParameter("receiverPort");
    if(tmpReceiverPort != null && !tmpReceiverPort.equals("")){
        receiverPort = tmpReceiverPort;
    }

    String tmpSecurity = request.getParameter("security");
    if(tmpSecurity !=null && !tmpSecurity.equals("")){
        security = tmpSecurity;
    }
    
    String tmpLoadbalancer = request.getParameter("loadbalancer");
    if(tmpLoadbalancer != null && !tmpLoadbalancer.equals("")){
        loadbalancer = tmpLoadbalancer;
    }

    String tmpStreamTable = request.getParameter("hfStreamTableData");
    if(tmpStreamTable != null && !tmpStreamTable.equals("")){
        streamTable = tmpStreamTable;
    }
    
    String tmpForce = request.getParameter("force");
    if(tmpForce != null && !tmpForce.equals("")){
        force = tmpForce;
    }


    String tmpServerProfileName = request.getParameter("txtServerProfileLocation");
    if(bamServerProfileUtils.isNotNullOrEmpty(tmpServerProfileName)){
        serverProfileName = tmpServerProfileName;
        serverProfileLocation = SERVER_PROFILE_LOCATION + "/" + serverProfileName;
    }

    String tmpAction = request.getParameter("hfAction");
    if(bamServerProfileUtils.isNotNullOrEmpty(tmpAction)){
        action = tmpAction;
    }

    %>
        <style type="text/css">
            .no-border-all{
                border: none!important;
            }
            .no-border-all td{
                border: none!important;
            }
        </style>
        <script id="source" type="text/javascript">

            function onSecurityChanged(){
                var securityEnabled = document.getElementById("security");
                var receiverPortTr = document.getElementById("receiverPortTr");
                if(document.getElementById("isSecured").checked){
                    securityEnabled.value = "true";
                    receiverPortTr.style.display = "none";
                } else {
                    securityEnabled.value = "false";
                    receiverPortTr.style.display = "";
                }
            }

            function onLoadBalancingChanged(){
                var loadBalancingEnabled = document.getElementById("loadbalancer");
                if(document.getElementById("isLoadBalanced").checked){
                    loadBalancingEnabled.value = "true";
                    jQuery(".transportSingleUrlTr").hide();
                    jQuery(".transportMultipleUrlsTr").show();
                } else {
                    loadBalancingEnabled.value = "false";
                    jQuery(".transportSingleUrlTr").show();
                    jQuery(".transportMultipleUrlsTr").hide();
                }
                onSecurityChanged();
            }

            function onReceiverPortBlur(){
                var receiverPortInput = document.getElementById("receiverPort");
                var authPortInput = document.getElementById("authPort");
                if(authPortInput.value == ""){
                    authPortInput.value = (parseInt(receiverPortInput.value) + 100).toString();
                }
            }

            function loadServerProfiles(serverProfileLocationPath, serverProfilePath) {
                jQuery.ajax({
                                type:"GET",
                                url:"../bam-mediator-config/dropdown_ajaxprocessor.jsp",
                                data:{action:"getServerProfiles", serverProfilePath:serverProfileLocationPath},
                                success:function(data){
                                    document.getElementById("serverProfileList").innerHTML = "";
                                    jQuery("#serverProfileList").append("<option>- Select Server Profile -</option>");
                                    jQuery("#serverProfileList").append(data);
                                    if(serverProfilePath != null && serverProfilePath != ""){
                                        document.getElementById("serverProfileList").value = serverProfilePath;
                                    }
                                }
                            })
            }

            function onServerProfileSelected(parentPath){
                document.getElementById('txtServerProfileLocation').value = document.getElementById('serverProfileList').value;
            }

            function showConfigRegistryBrowser(id, path) {
                elementId = id;
                rootPath = path;
                showResourceTree(id, setValue , path);
            }


            var commonParameterString = "txtUsername=" + "<%=request.getParameter("txtUsername")%>" + "&"
                                                + "txtPassword=" + "<%=request.getParameter("txtPassword")%>" + "&"
                                                + "urlSet=" + "<%=request.getParameter("urlSet")%>" + "&"
                                                + "txtIp=" + "<%=request.getParameter("txtIp")%>" + "&"
                                                + "authPort=" + "<%=request.getParameter("authPort")%>" + "&"
                                                + "receiverPort=" + "<%=request.getParameter("receiverPort")%>" + "&"
                                                + "security=" + "<%=request.getParameter("security")%>" + "&"
                                                + "loadbalancer=" + "<%=request.getParameter("loadbalancer")%>" + "&"
                                                + "hfStreamTableData=" + "<%=request.getParameter("hfStreamTableData")%>" + "&"
                                                + "txtServerProfileLocation=" + "<%=request.getParameter("txtServerProfileLocation")%>";

            function saveOverwrite(){
                window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=save&force=true";
            }

            function removeOverwrite(){
                window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=remove&force=true";
            }

            function reloadPage(){
                window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=load";
            }

            function showHideDiv(divId) {
                var theDiv = document.getElementById(divId);
                if (theDiv.style.display == "none") {
                    theDiv.style.display = "";
                } else {
                    theDiv.style.display = "none";
                }
            }

            var streamRowNum = 1;
            var propertyRowNum = 1;

            function validatePropertyTable(){
                var propertyRowInputs = document.getElementById("propertyTable").getElementsByTagName("input");
                var inputName = "";
                for(var i=0; i<propertyRowInputs.length; i++){
                    inputName = propertyRowInputs[i].name;
                    if((inputName == "<%=PROPERTY_KEYS%>" || inputName == "<%=PROPERTY_VALUES%>") && propertyRowInputs[i].value == ""){
                        return "Property Name or Property Value cannot be empty.";
                    }
                }
                return "true";
            }

            function onAddPropertyClicked(){
                var result = validatePropertyTable();
                if(result == "true"){
                    addPropertyRow();
                } else {
                    CARBON.showInfoDialog(result);
                }
            }

            function addPropertyRow() {
                propertyRowNum++;
                var sId = "propertyTable_" + propertyRowNum;

                var tableContent = "<tr id=\"" + sId + "\">" +
                                   "<td>\n" +
                                   "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                                   "                    </td>\n" +
                                   "                    <td>\n" +
                                   "<table class=\"no-border-all\">" +
                                   "         <tr> " +
                                   "         <td> " +
                                   "         <table> " +
                                   "         <tr> " +
                                   "         <td><input type=\"radio\" name=\"fieldType_" + sId + "\" value=\"value\" checked=\"checked\"/></td> " +
                                   "          <td>Value</td> " +
                                   "         <tr> " +
                                   "         <tr> " +
                                   "         <td><input type=\"radio\" name=\"fieldType_" + sId + "\" value=\"expression\"/></td> " +
                                   "       <td>Expression</td> " +
                                   "         <tr> " +
                                   "       </table> " +
                                   "       </td> " +
                                   "         <td> " +
                                   "<input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\"/>" +
                                   "         </td> " +
                                   "         </tr> " +
                                   "         </table> " +
                                   "         </td> " +

                                   "<td>" +
                                   "<select id=\"propertyType_" + sId + "\">" +
                                   "<option value=\"STRING\" selected=\"selected\" >STRING</option>" +
                                   "<option value=\"INTEGER\">INTEGER</option>" +
                                   "<option value=\"BOOLEAN\">BOOLEAN</option>" +
                                   "<option value=\"DOUBLE\">DOUBLE</option>" +
                                   "<option value=\"FLOAT\">FLOAT</option>" +
                                   "<option value=\"LONG\">LONG</option>" +
                                   "</select>" +
                                   "</td>" +

                                   "<td> " +
                                   "<a onClick='javaScript:removePropertyColumn(\"" + sId + "\")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a> " +
                                   "</td> " +
                                   "</tr>;" ;

                jQuery("#propertyTable").append(tableContent);
                updatePropertyTableData();
            }

            function validateStreamsTable(){
                var streamRowInputs = document.getElementById("streamTable").getElementsByTagName("input");
                var inputName = "";
                for(var i=0; i<streamRowInputs.length; i++){
                    inputName = streamRowInputs[i].name;
                    if((inputName == "<%=STREAM_NAMES%>" || inputName == "<%=STREAM_VERSIONS%>"
                               || inputName == "<%=STREAM_NICKNAME%>" ||  inputName == "<%=STREAM_DESCRIPTION%>") && streamRowInputs[i].value == ""){
                        return "Stream Name, Stream Version, Nick Name or Description cannot be empty.";
                    }
                }
                return "true";
            }

            function addStreamRow() {
                var validationResult = validateStreamsTable();
                if(validationResult == "true"){
                    streamRowNum++;
                    var sId = "streamsTable_" + streamRowNum;
                    var tableContent = "<tr id=\"" + sId + "\">" +
                                       "<td>\n" +
                                       "<input type=\"text\" name=\"<%=STREAM_NAMES%>\" value=\"\">\n" +
                                       "</td>\n" +
                                       "<td>\n" +
                                       "<input type=\"text\" name=\"<%=STREAM_VERSIONS%>\" value=\"\">\n" +
                                       "</td>" +
                                       "<td>\n" +
                                       "<input type=\"text\" name=\"<%=STREAM_NICKNAME%>\" value=\"\">\n" +
                                       "</td>\n" +
                                       "<td>\n" +
                                       "<input type=\"text\" name=\"<%=STREAM_DESCRIPTION%>\" value=\"\">\n" +
                                       "</td>" +
                                       "<td>\n" +
                                       "<span><a onClick='javaScript:removeStreamColumn(\"" + sId + "\")'" +
                                       "style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>\n" +
                                       "<span><a onClick='javaScript:editStreamData(\"" + streamRowNum + "\")'" +
                                       "style='background-image: url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>\n" +
                                       "<input type=\"hidden\" id=\"hfStreamsTable_" + streamRowNum + "\" value=\"\"/>"
                    "</td>" +
                    "</tr>";

                    jQuery("#streamTable").append(tableContent);
                    updateStreamTableData();
                } else {
                    CARBON.showInfoDialog(validationResult);
                }
            }

            function removeStreamColumn(id) {
                jQuery("#" + id).remove();
            }

            function removePropertyColumn(id) {
                jQuery("#" + id).remove();
                updatePropertyTableData();
            }

            function updatePropertyTableData(){
                var tableData = "", inputs, lists, numOfInputs;
                inputs = document.getElementById("propertyTable").getElementsByTagName("input");
                lists = document.getElementById("propertyTable").getElementsByTagName("select");
                numOfInputs = inputs.length;
                for(var i=0; i<numOfInputs; i=i+4){
                    if(inputs[i].value != "" && inputs[i+3].value != ""){
                        tableData = tableData + inputs[i].value + "::" + inputs[i+3].value + "::" + lists[i/4].value;
                        if(inputs[i+1].checked){
                            tableData = tableData + "::" + "value";
                        } else {
                            tableData = tableData + "::" + "expression";
                        }
                        tableData = tableData + ";";
                    }
                }
                document.getElementById("hfPropertyTableData").value = tableData;
            }

            function savePropertyTableData(){
                updatePropertyTableData();
                var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
                document.getElementById("hfStreamsTable_" + streamRowNumber).value = document.getElementById("hfPropertyTableData").value;
                document.getElementById("propertiesTr").style.display = "none";
                jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
            }

            function saveDumpData(){
                var data = "";
                if(document.getElementById("mHeader").checked){
                    data = "dump";
                } else{
                    data = "notDump";
                }
                data = data + ";";
                if(document.getElementById("mBody").checked){
                    data = data + "dump";
                } else{
                    data = data + "notDump";
                }
                var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
                document.getElementById("hfStreamsTable_" + streamRowNumber).value = document.getElementById("hfStreamsTable_" + streamRowNumber).value + "^" + data;
                document.getElementById("mHeader").checked = "checked";
                document.getElementById("mBody").checked = "checked";
            }

            function savePropertiesData(){
                savePropertyTableData();
                saveDumpData();
            }

            function editStreamData(rowNumber){
                jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
                jQuery("#streamsTable_" + rowNumber).css("background-color","rgb(234,234,255)");
                document.getElementById("propertiesTr").style.display = "";
                document.getElementById("hfStreamTableRowNumber").value = rowNumber;
                loadPropertyDataTable();
                loadDumpData();
            }

            function loadPropertyDataTable(){
                emptyPropertyTable();
                var rowNumber =  document.getElementById("hfStreamTableRowNumber").value;
                var configDataString = document.getElementById("streamsTable_" + rowNumber).getElementsByTagName("input")[4].value;
                var propertyDataString = configDataString.split("^")[0];
                var propertyDataArray = propertyDataString.split(";");
                var numOfProperties = 0;
                for(var i=0; i<propertyDataArray.length; i++){
                    if(propertyDataArray[i] != ""){
                        addPropertyRow();
                        numOfProperties++;
                    }
                }

                for(var i=0; i<numOfProperties; i=i+1){
                    if(propertyDataArray[i].split("::").length == 4){
                        jQuery("#propertyTable").find("tr").find("input")[4*i].value = propertyDataArray[i].split("::")[0];
                        jQuery("#propertyTable").find("tr").find("input")[4*i+3].value = propertyDataArray[i].split("::")[1];
                        jQuery("#propertyTable").find("tr").find("select")[i].value = propertyDataArray[i].split("::")[2];
                        if(propertyDataArray[i].split("::")[3] == "value"){
                            jQuery("#propertyTable").find("tr").find("input")[4*i+1].checked = true;
                        } else if(propertyDataArray[i].split("::")[3] == "expression"){
                            jQuery("#propertyTable").find("tr").find("input")[4*i+2].checked = true;
                        }
                    }
                }
                updatePropertyTableData();
            }

            function loadDumpData(){
                cancelDumpData();
                var rowNumber =  document.getElementById("hfStreamTableRowNumber").value;
                var configDataString = document.getElementById("streamsTable_" + rowNumber).getElementsByTagName("input")[4].value;
                var dumpDataString = "";
                if(configDataString.split("^").length == 2){
                    dumpDataString = configDataString.split("^")[1];
                    var dumpDataArray = dumpDataString.split(";");
                    if(dumpDataArray.length == 2){
                        if(dumpDataArray[0] == "dump"){
                            document.getElementById("mHeader").checked = "checked";
                        } else {
                            document.getElementById("mHeader").checked = "";
                        }
                        if(dumpDataArray[1] == "dump"){
                            document.getElementById("mBody").checked = "checked";
                        } else {
                            document.getElementById("mBody").checked = "";
                        }
                    }
                }
            }

            function emptyPropertyTable(){
                document.getElementById("hfPropertyTableData").value = "";
                jQuery("#propertyTable").find("tr").find("input")[0].value = "";
                jQuery("#propertyTable").find("tr").find("input")[3].value = "";
                jQuery("#propertyTable").find("tr").find("select")[0].value = "STRING";
                jQuery("#propertyTable").find("tr").find("input")[1].checked = true;
                var tableRowNumber = jQuery("#propertyTable").find("tr").length;
                var isFirstRow = true;
                //var firstRowId = "";
                var currentRowId;
                var trArray = new Array();
                for(var i=0; i<tableRowNumber; i=i+1){
                    currentRowId = jQuery("#propertyTable").find("tr")[i].id;
                    if(currentRowId.split("_")[0] == "propertyTable"){
                        if(!isFirstRow){
                            //jQuery("#" + currentRowId).remove();
                            trArray.push(currentRowId);
                        }
                        isFirstRow = false;
                    }
                }
                for(var i=0; i<trArray.length; i++){
                    jQuery("#" + trArray[i]).remove();
                }

            }

            function cancelPropertyTableData(){
                emptyPropertyTable();
                document.getElementById("propertiesTr").style.display = "none";
                jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
            }

            function cancelDumpData(){
                document.getElementById("mHeader").checked = "checked";
                document.getElementById("mBody").checked = "checked";
            }

            function cancelStreamData(){
                cancelPropertyTableData();
                cancelDumpData();
            }

            function updateStreamTableData(){
                var tableData = "", inputs, numOfInputs;
                inputs = document.getElementById("streamTable").getElementsByTagName("input");
                numOfInputs = inputs.length;
                for(var i=0; i<numOfInputs; i=i+5){
                    if(inputs[i].value != "" && inputs[i+1].value != ""){
                        if(i != 0){
                            tableData = tableData + "~";
                        }
                        tableData = tableData + inputs[i].value + "^"
                                            + inputs[i+1].value + "^" + inputs[i+2].value + "^"
                                            + inputs[i+3].value + "^" + inputs[i+4].value;
                    }
                }
                document.getElementById("hfStreamTableData").value = tableData;
            }

            function submitPage(){
                updateStreamTableData();
                document.getElementById('hfAction').value='save';
            }

            function testServer(){
                var serverIp = document.getElementById('txtIp').value;
                var authPort = document.getElementById('authPort').value;

                if(!(serverIp != null && serverIp != "")){
                    CARBON.showInfoDialog("Please enter the IP address.");
                } else if(!(authPort != null && authPort != "")){
                    CARBON.showInfoDialog("Please enter the Authentication Port.");
                } else{
                    jQuery.ajax({
                                    type:"GET",
                                    url:"../bam-mediator-config/dropdown_ajaxprocessor.jsp",
                                    data:{action:"testServer", ip:serverIp, port:authPort},
                                    success:function(data){
                                        if(data != null && data != ""){
                                            var result = data.replace(/\n+/g, '');
                                            if(result == "true"){
                                                CARBON.showInfoDialog("Successfully connected to BAM Server.");
                                            } else if(result == "false"){
                                                CARBON.showErrorDialog("BAM Server cannot be connected!")
                                            }
                                        }
                                    }
                                });
                }
            }
        </script>


    <%

    if(!bamServerProfileUtils.resourceAlreadyExists(SERVER_PROFILE_LOCATION)){
        bamServerProfileUtils.addCollection(SERVER_PROFILE_LOCATION);
    }

    if("save".equals(action) && !"true".equals(force) && bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
        %>

            <script>
                CARBON.showConfirmationDialog("Are you sure you want to overwrite the existing Server Profile Configuration?", saveOverwrite, reloadPage, true);
            </script>

        <%
    }

    else if("remove".equals(action) && !"true".equals(force) && bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
        %>

            <script>
                CARBON.showConfirmationDialog("Are you sure you want to remove the existing Server Profile Configuration?", removeOverwrite, reloadPage, true);
            </script>

        <%
    }

    else if("load".equals(action)){  // loading an existing configuration
        if(bamServerProfileUtils.isNotNullOrEmpty(tmpServerProfileName)){
            serverProfileName = tmpServerProfileName;
            serverProfileLocation = SERVER_PROFILE_LOCATION + "/" + serverProfileName;
            if(bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
                bamServerConfig = bamServerProfileUtils.getResource(serverProfileLocation);

                userName = bamServerConfig.getUsername();
                password = bamServerProfileUtils.decryptPassword(bamServerConfig.getPassword());
                if(bamServerProfileUtils.isNotNullOrEmpty(bamServerConfig.getUrlSet())){
                    urlSet = bamServerConfig.getUrlSet();
                } else {
                    urlSet = "";
                }
                if(bamServerProfileUtils.isNotNullOrEmpty(bamServerConfig.getIp())){
                    ip = bamServerConfig.getIp();
                } else {
                    ip = "";
                }
                if(bamServerProfileUtils.isNotNullOrEmpty(bamServerConfig.getAuthenticationPort())){
                    authenticationPort = bamServerConfig.getAuthenticationPort();
                } else {
                    authenticationPort = "";
                }
                if(bamServerProfileUtils.isNotNullOrEmpty(bamServerConfig.getReceiverPort())){
                    receiverPort = bamServerConfig.getReceiverPort();
                } else {
                    receiverPort = "";
                }
                if(bamServerConfig.isSecure()){
                    security = "true";
                } else {
                    security = "false";
                }
                if(bamServerConfig.isLoadbalanced()){
                    loadbalancer = "true";
                } else {
                    loadbalancer = "false";
                }
            }
            else {
                %>

                <script type="text/javascript">
                    CARBON.showErrorDialog("Resource is not existing in the given location!");
                </script>

                <%
            }
        }
        else {
            %>

            <script type="text/javascript">
                CARBON.showInfoDialog("Enter the Server Profile Name.");
            </script>

            <%
        }
    }

    /*else if("stay".equals(action)){  // staying in the existing page

    }*/

    else if("remove".equals(action) && !"".equals(serverProfileLocation) && "true".equals(force)){  // staying in the existing page
        bamServerProfileUtils.removeResource(serverProfileLocation);

        %>

        <script type="text/javascript">
            CARBON.showInfoDialog("Server Profile was successfully deleted.");
        </script>

        <%
    }

    else if("save".equals(action) && !"".equals(serverProfileLocation)){ // Saving a configuration
        if("true".equals(force)){ // Saving after confirmation
            bamServerProfileUtils.addResource(urlSet, ip, authenticationPort, receiverPort, userName, password, "true".equals(security), "true".equals(loadbalancer),
                                              streamTable, serverProfileLocation);
            %>

            <script type="text/javascript">
                CARBON.showInfoDialog("Server Profile was successfully saved.", reloadPage);
            </script>

            <%
        }
        else if (!"true".equals(force)){ // Trying to save without confirmation
            if(!bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
                bamServerProfileUtils.addResource(urlSet, ip, authenticationPort, receiverPort, userName, password, "true".equals(security), "true".equals(loadbalancer),
                                                  streamTable, serverProfileLocation);
                %>

                <script type="text/javascript">
                    CARBON.showInfoDialog("Server Profile was successfully saved.", reloadPage);
                </script>

                <%
            }
            else {
                %>

                    <script type="text/javascript">
                        CARBON.showErrorDialog("Resource already exists!", reloadPage);
                    </script>

                <%
            }
        }

    }

%>


<div id="middle">
    <h2>
        <fmt:message key="bam.server.profile"/>
    </h2>

    <div id="workArea">
        <form action="configure_server_profiles.jsp" method="post">
        <table>
            <tr>
                <td colspan="2">
                    <h3>
                        <fmt:message key="server.profile"/>
                    </h3>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="profile.name"/><span class="required">*</span>
                </td>
                <td>
                    <table>
                        <tr>

                            <td>
                                <input class="longInput" type="text"
                                       value="<%=serverProfileName%>"
                                       id="txtServerProfileLocation" name="txtServerProfileLocation"/>
                            </td>
                            <td style="display: none;">
                                <select name="serverProfileList" id="serverProfileList" onchange="onServerProfileSelected('<%=SERVER_PROFILE_LOCATION%>')">
                                    <option>- Select Server Profile -</option>
                                </select>
                                <script type="text/javascript">
                                    loadServerProfiles("<%=SERVER_PROFILE_LOCATION%>", "<%=serverProfileLocation%>");
                                </script>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <input type="submit" value="Load Profile" onclick="document.getElementById('hfAction').value='load';" style="display: none;"/>
                    <input type="submit" value="Remove Profile" onclick="document.getElementById('hfAction').value='remove';" style="display: none;"/>
                    <input type="hidden" name="hfAction" id="hfAction" value=""/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <h3>
                        <fmt:message key="server.credential"/>
                    </h3>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="username"/><span class="required">*</span>
                </td>
                <td>
                    <input type="text" name="txtUsername" id="txtUsername" value="<%=userName%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="password"/><span class="required">*</span>
                </td>
                <td>
                    <input type="password" name="txtPassword" id="txtPassword" value="<%=password%>"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <h3>
                        <fmt:message key="server.transport"/>
                    </h3>
                </td>
            </tr>

            <tr>
                <td style="width:250px;" >
                    <fmt:message key="enable.loadbalancer"/>
                </td>
                <td>
                    <input type="checkbox" id="isLoadBalanced" name="isLoadBalanced" onchange="onLoadBalancingChanged()"/>
                    <input type="hidden" id="loadbalancer" name="loadbalancer" value="<%=loadbalancer%>"/>
                </td>
            </tr>

            <tr style="display: none;" class="transportMultipleUrlsTr">
                <td>
                    <fmt:message key="url.set"/><span class="required">*</span>
                </td>
                <td>
                    <input type="text" name="urlSet" id="urlSet" value="<%=urlSet%>"/>
                </td>
            </tr>

            <tr class="transportSingleUrlTr">
                <td>
                    <fmt:message key="protocol"/><span class="required">*</span>
                </td>
                <td>
                    <select name="transportProtocol" id="transportProtocol" onchange="">
                        <option>Thrift</option>
                    </select>
                    <script type="text/javascript">
                        document.getElementById("transportProtocol").value = "Thrift";
                    </script>
                </td>
            </tr>
            <tr class="transportSingleUrlTr">
                <td>
                    <fmt:message key="enable.security"/>
                </td>
                <td>
                    <input type="checkbox" id="isSecured" name="isSecured" checked="checked" onchange="onSecurityChanged()"/>
                    <input type="hidden" id="security" name="security" value="<%=security%>"/>
                    <script type="text/javascript">
                        if(document.getElementById("security").value == "false"){
                            document.getElementById("isSecured").checked = "";
                        }
                    </script>
                </td>
            </tr>
            <tr class="transportSingleUrlTr">
                <td>
                    <fmt:message key="ip"/><span class="required">*</span>
                </td>
                <td>
                    <input type="text" name="txtIp" id="txtIp" value="<%=ip%>"/>
                </td>
            </tr>
            <tr id="receiverPortTr" style="display: none;" class="transportSingleUrlTr">
                <td>
                    <fmt:message key="receiver.port"/><span class="required">*</span>
                </td>
                <td>
                    <input type="text" name="receiverPort" id="receiverPort" value="<%=receiverPort%>" onblur="onReceiverPortBlur()"/>
                </td>
            </tr>
            <script type="text/javascript">
                if(document.getElementById("security").value == "false"){
                    document.getElementById("receiverPortTr").style.display = "";
                }
            </script>
            <tr class="transportSingleUrlTr">
                <td>
                    <fmt:message key="authentication.port"/><span class="required">*</span>
                </td>
                <td>
                    <input type="text" name="authPort" id="authPort" value="<%=authenticationPort%>"/>
                    <input type="button" value="Test Server" onclick="testServer()"/>
                </td>
            </tr>
            <script type="text/javascript">
                document.getElementById("isLoadBalanced").checked = false;
                if(document.getElementById("loadbalancer").value == "true"){
                    document.getElementById("isLoadBalanced").checked = "checked";
                    onLoadBalancingChanged();
                }
            </script>
            <tr>
                <td colspan="2">
                    <h3>
                        <fmt:message key="streams.configuration"/>
                    </h3>
                </td>
            </tr>

            <tr id="streamsTr">
                <td colspan="2">
                    <input name="hfStreamTableData" id="hfStreamTableData" type="hidden" value="" />
                    <table id="streamTable" width="100%" class="styledLeft" style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th width="40%">
                                    <fmt:message key="stream.name"/>
                                </th>
                                <th width="40%">
                                    <fmt:message key="stream.version"/>
                                </th>
                                <th width="40%">
                                    <fmt:message key="stream.nickName"/>
                                </th>
                                <th width="40%">
                                    <fmt:message key="stream.description"/>
                                </th>
                                <th>

                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (bamServerConfig.getStreamConfigurations() != null && !bamServerConfig.getStreamConfigurations().isEmpty()) {
                                streamConfigurations = bamServerConfig.getStreamConfigurations();
                                int i = 1;
                                for (StreamConfiguration streamConfiguration : streamConfigurations) {
                            %>
                            <tr id="streamsTable_<%=i%>">
                                <td>
                                    <input id="streamName" type="text" name="<%=STREAM_NAMES%>" value="<%=streamConfiguration.getName()%>"/>
                                </td>
                                <td>
                                    <input id="streamVersion" type="text" name="<%=STREAM_VERSIONS%>" value="<%=streamConfiguration.getVersion()%>"/>
                                </td>
                                <td>
                                    <input id="streamNickname" type="text" name="<%=STREAM_NICKNAME%>" value="<%=streamConfiguration.getNickname()%>"/>
                                </td>
                                <td>
                                    <input id="streamDescription" type="text" name="<%=STREAM_DESCRIPTION%>" value="<%=streamConfiguration.getDescription()%>"/>
                                </td>
                                <% if (i == 1) { %>
                                <td><span><a onClick='javaScript:removeStreamColumn("streamsTable_<%=i%>")' style='background-image:
                                        url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("<%=i%>")' style='background-image:
                                        url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_<%=i%>" value="<%=bamServerProfileUtils.getStreamConfigurationListString(streamConfiguration)%>"/>
                                </td>
                                <% } else {  %>
                                <td>
                                    <span><a onClick='javaScript:removeStreamColumn("streamsTable_<%=i%>")' style='background-image:
                                        url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("<%=i%>")' style='background-image:
                                        url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_<%=i%>" value="<%=bamServerProfileUtils.getStreamConfigurationListString(streamConfiguration)%>"/>
                                </td>
                                <% } %>

                            </tr>
                            <script type="text/javascript">
                                streamRowNum++;
                            </script>
                            <%  i++;
                            }
                            } else { %>
                            <tr id="streamsTable_1">
                                <td>
                                    <input type="text" name="<%=STREAM_NAMES%>" value=""/>
                                </td>
                                <td>
                                    <input type="text" name="<%=STREAM_VERSIONS%>" value=""/>
                                </td>
                                <td>
                                    <input type="text" name="<%=STREAM_NICKNAME%>" value=""/>
                                </td>
                                <td>
                                    <input type="text" name="<%=STREAM_DESCRIPTION%>" value=""/>
                                </td>

                                <td>
                                    <span><a onClick='javaScript:removeStreamColumn("streamsTable_1")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("1")' style='background-image: url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_1" value=""/>
                                </td>
                            </tr>
                        </tbody>

                        <% } %>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <span><a onClick='javaScript:addStreamRow()' style='background-image:
                                        url(../admin/images/add.gif);'class='icon-link addIcon'>Add Stream</a></span>
                </td>
            </tr>

            <tr id="propertiesTr" style="display: none;">
                <td colspan="2">
                    <input name="hfPropertyTableData" id="hfPropertyTableData" type="hidden" value="" />
                    <input id="hfStreamTableRowNumber" type="hidden" value="1" />
                    <h3>
                        <fmt:message key="stream.configuration"/>
                    </h3>

                    <table>
                        <tr>
                            <td>
                                <h4>
                                    <fmt:message key="stream.payload"/>
                                </h4>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                            <fmt:message key="dump.header"/>
                                        </td>
                                        <td>
                                            <input type="checkbox" id="mHeader" name="mHeader" checked="checked" value="dump"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <fmt:message key="dump.body"/>
                                        </td>
                                        <td>
                                            <input type="checkbox" id="mBody" name="mBody" checked="checked" value="dump"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <h4>
                                    <fmt:message key="stream.properties"/>
                                </h4>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table id="propertyTable" width="100%" class="styledLeft" style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th width="30%">
                                            <fmt:message key="property.name"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="property.value"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="property.type"/>
                                        </th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr id="propertyTable_1">
                                        <td>
                                            <input type="text" name="<%=PROPERTY_KEYS%>" value=""/>
                                        </td>
                                        <td>
                                            <table class="no-border-all">
                                                <tr>
                                                    <td>
                                                        <table>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_1" value="value" checked="checked"/></td>
                                                                <td><fmt:message key="property.field.value"/></td>
                                                            </tr>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_1" value="expression"/></td>
                                                                <td><fmt:message key="property.field.expression"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                    <td>
                                                        <input type="text" name="<%=PROPERTY_VALUES%>" value=""/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>

                                        <td>
                                            <select id="propertyType_1">
                                                <option value="STRING" selected="selected" >STRING</option>
                                                <option value="INTEGER">INTEGER</option>
                                                <option value="BOOLEAN">BOOLEAN</option>
                                                <option value="DOUBLE">DOUBLE</option>
                                                <option value="FLOAT">FLOAT</option>
                                                <option value="LONG">LONG</option>
                                            </select>
                                        </td>

                                        <td>
                                            <a onClick='javaScript:removePropertyColumn("propertyTable_1")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:onAddPropertyClicked()' style='background-image: url(../admin/images/add.gif);'class='icon-link addIcon'>Add</a>
                                            </span>
                                        </td>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:savePropertiesData()' style='background-image: url(images/save-button.gif);'class='icon-link addIcon'>Update</a>
                                            </span>
                                        </td>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:cancelStreamData()' style='background-image: url(../admin/images/cancel.gif);'class='icon-link addIcon'>Cancel</a>
                                            </span>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Save" onclick="submitPage()"/>
                </td>
            </tr>
        </table>
        </form>
    </div>
</div>


</fmt:bundle>
