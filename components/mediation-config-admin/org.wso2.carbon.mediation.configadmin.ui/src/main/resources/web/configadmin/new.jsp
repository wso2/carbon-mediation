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
<%@ page import="org.wso2.carbon.mediation.configadmin.ui.ConfigManagementClient" %>
<%@ page import="org.wso2.carbon.mediation.configadmin.ui.ResponseInformation" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="configcommon.js"></script>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<script type="text/javascript">
    function createConfig() {
        document.configform.actionInput.value = "create";
        document.configform.submit();
    }

    function onConfigurationTypeChange() {
        var elem = document.getElementById('configurationType');
        if (elem != null) {
            if (elem.value == 'new') {
                var row = document.getElementById('descriptionRow');
                if (row != null) {
                    row.style.display = '';
                }
            } else if (elem.value == 'existing') {
                var row = document.getElementById('descriptionRow');
                if (row != null) {
                    row.style.display = 'none';
                }
            }
        }
    }
</script>

<fmt:bundle basename="org.wso2.carbon.mediation.configadmin.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediation.configadmin.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="configjsi18n"/>
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.mediation.configadmin.ui.i18n.Resources"
                       topPage="false" label="new.synapse.config" request="<%=request%>"/>
    <div id="middle">
    <h2><fmt:message key="new.synapse.config"/></h2>

    <%
        String action = request.getParameter("actionInput");
        if (action != null && action.equals("create")) {
            ConfigManagementClient client = null;
            client = ConfigManagementClient.getInstance(config, session);

            String name = request.getParameter("name");
            String description = request.getParameter("desc");
            String configurationType = request.getParameter("configurationType");

            ResponseInformation responseInformation =null;
            if (configurationType != null) {
                if (configurationType.equals("new")) {
                    responseInformation = client.newConfiguration(name, description);
                } else {
                    responseInformation = client.addConfiguration(name);
                }
            } else {
                responseInformation = client.newConfiguration(name, description);
            }
            String errorMessage = "";
            if (responseInformation.isFault()) {
                errorMessage = responseInformation.getMessage();
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('Error occurred while creating the configuration. Error : <%=errorMessage%>');
    </script>
    <div id="workArea">
        <form method="post" name="configform" id="configform"
              action="new.jsp">
            <label><fmt:message key="new.configuration.details"/></label>
            <br/><br/>
            <table class="styledLeft" width="100%">                
                <tbody>
                <tr class="tableOddRow">
                    <td style="width:200px"><fmt:message key="type.of.configuration"/></td>
                    <td><select name="configurationType" id="configurationType" onchange="onConfigurationTypeChange()">
                        <option value="new"><fmt:message key="new.configuration"/></option>
                        <option value="existing"><fmt:message key="existing.configuration"/></option>
                    </select>
                    </td>
                </tr>
                <tr class="tableOddRow">
                    <td><fmt:message key="name"/><font style="color: red; font-size: 8pt;"> *</font></td>
                    <td><input value="<%=name%>" name="name" id="name"/></td>
                </tr>
                <tr class="tableOddRow" id="descriptionRow">
                    <td><fmt:message key="description"/></td>
                    <td><textarea name="desc" id="desc" rows="2" cols="40">desc</textarea></td>
                </tr>
                <tr>
                    <td class="buttonRow" colspan="2">
                        <input name="updateConfig" type="button"
                               class="button"
                               value="<fmt:message key="add"/>"
                               onclick="createConfig()"
                               title="Create"/>
                    </td>
                </tr>
                </tbody>
            </table>
            <input type="hidden" value="" name="actionInput"/>
        </form>
    </div>
    <% } else { %>
    <script type="text/javascript">
        window.location.href = "index.jsp?status=newConfigCreated&tab=1";
    </script>
    <% }
    } else {
    %>
    <div id="workArea">
        <form method="post" name="configform" id="configform"
              action="new.jsp">
            <label><fmt:message key="new.configuration.details"/></label>
            <br/><br/>
            <table class="styledLeft" width="100%">
                <tbody>
                <tr class="tableOddRow">
                    <td style="width:200px"><fmt:message key="type.of.configuration"/></td>
                    <td><select name="configurationType" id="configurationType" onchange="onConfigurationTypeChange()">
                        <option value="new"><fmt:message key="new.configuration"/></option>
                        <option value="existing"><fmt:message key="existing.configuration"/></option>
                    </select>
                    </td>
                </tr>
                <tr class="tableOddRow">
                    <td><fmt:message key="name"/></td>
                    <td><input value="" name="name" id="name"/></td>
                </tr>
                <tr class="tableOddRow" id="descriptionRow">
                    <td><fmt:message key="description"/></td>
                    <td><textarea name="desc" id="desc" rows="2" cols="40"></textarea></td>
                </tr>
                <tr class="buttonRow">
                    <td colspan="2">
                        <input name="updateConfig" type="button"
                               class="button"
                               value="<fmt:message key="add"/>"
                               onclick="createConfig()"
                               title="Create"/>
                    </td>
                </tr>
                </tbody>
            </table>
            <input type="hidden" value="" name="actionInput"/>
        </form>
    </div>
    <% } %>
</fmt:bundle>
