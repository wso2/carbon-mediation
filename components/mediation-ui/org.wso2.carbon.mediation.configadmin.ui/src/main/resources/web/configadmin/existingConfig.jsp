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

            ResponseInformation responseInformation = client.newConfiguration(name, description);
            boolean isFault = false;
            String errorMessage = "";
            if (responseInformation.isFault()) {
                isFault = true;
                errorMessage = responseInformation.getMessage();
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('Error occurred while creating the configuration. Error : <%=errorMessage%>');
    </script>
    <div id="workArea">
        <form method="post" name="configform" id="configform"
              action="new.jsp">
            <div>
                <label>Choose a new synape configuration</label>
                <br/><br/>

                <table class="styledLeft">
                    <tbody>
                    <tr class="tableEvenRow">
                        <td class="formRow">
                            <label>Synapse configuration file/directory<font
                                    color="red">*</font></label>
                        </td>
                        <td class="formRow"><input type="file" size="50" name="marFilename"/></td>
                    </tr>
                    <tr class="buttonRow">
                        <td colspan="2">
                            <input type="button" onclick="validate();" value="OK"
                                   class="button" name="upload"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </form>
    </div>
    <% } else { %>
    <script type="text/javascript">
        document.location.href = "index.jsp?tab=1";
    </script>
    <% }
    } else {
    %>
    <div id="workArea">
        <form method="post" name="configform" id="configform"
              action="new.jsp">
            <div>
                <label>Choose a new synape configuration</label>
                <br/><br/>

                <table class="styledLeft">
                    <tbody>
                    <tr class="tableEvenRow">
                        <td class="formRow">
                            <label>Synapse configuration file/directory<font
                                    color="red">*</font></label>
                        </td>
                        <td class="formRow"><input type="file" size="50" name="marFilename"/></td>
                    </tr>
                    <tr class="buttonRow">
                        <td colspan="2">
                            <input type="button" onclick="validate();" value="OK"
                                   class="button" name="upload"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </form>
    </div>
    <% } %>
</fmt:bundle>
