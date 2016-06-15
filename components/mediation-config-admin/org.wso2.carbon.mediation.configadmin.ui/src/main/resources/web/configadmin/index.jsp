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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.mediation.configadmin.stub.types.carbon.ConfigurationInformation" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.mediation.configadmin.stub.types.carbon.ValidationError" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="global-parMCITams.js"></script>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>


<script type="text/javascript">
    function lo() {
        var iFrame = document.getElementById('frame_rawConfig');
        var innerDoc = iFrame.contentDocument || iFrame.contentWindow.document;
        innerDoc.getElementById("toolbar_1").style.display = "block";

        var searchBox = innerDoc.getElementById("area_search_replace");
        var links = searchBox.getElementsByTagName("a");
        links[2].style.display = "none";
        links[3].style.display = "none";

        var tableRows = searchBox.getElementsByTagName("tr");
        var replaceRow = tableRows[1];
        var replaceColumns = replaceRow.getElementsByTagName("td");
        replaceColumns[0].innerHTML = "";
        replaceColumns[1].innerHTML = "";
    }
</script>

<style type="text/css">
    .graylink {
        color: #aaaaaa !important;
    }
</style>

<fmt:bundle basename="org.wso2.carbon.mediation.configadmin.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediation.configadmin.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="configjsi18n"/>
    <carbon:breadcrumb
            resourceBundle="org.wso2.carbon.mediation.configadmin.ui.i18n.Resources"
            topPage="false" label="manage.synapse.config" request="<%=request%>"/>

    <%
        String synapseConfig = null;
        Boolean loadEditArea = false;
        List<ConfigurationInformation> synapseConfigList = null;

        ResponseInformation configurationList;
        ResponseInformation activeConfiguration;

        try {
            ConfigManagementClient client = ConfigManagementClient.getInstance(config, session);
            boolean validationFailed = Boolean.parseBoolean(request.getParameter("configInvalid"));
            if (validationFailed) {
                activeConfiguration = new ResponseInformation();
                activeConfiguration.setResult(session.getAttribute("input.config"));
                ValidationError[] errors = (ValidationError[]) session.getAttribute("validation.errors");
                session.removeAttribute("input.config");
                session.removeAttribute("validation.errors");

                String msg = "Problems detected in the provided configuration:<br/><br/>";
                for (ValidationError error : errors) {
                    msg += " - " + error.getItemName() + ": " + error.getMessage().replaceAll("\"", "'") +
                            "<br/><br/>";
                }
                msg += "Faulty Configuration Detected!";
                synapseConfig = "Faulty Configuration";

    %>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            CARBON.showInfoDialog("<%=msg%>");
        });
    </script>
    <%
        } else {
            activeConfiguration = client.getConfiguration(session);


            configurationList = client.getConfigurations();

            if (activeConfiguration.isFault() || configurationList.isFault()) {
                throw new Exception("Error while retrieving Synapse configuration details");
            }

            synapseConfig = (String) activeConfiguration.getResult();
            if (synapseConfig != null) {
                synapseConfig = synapseConfig.trim().replace("&", "&amp;");
            } else {
                synapseConfig = "";
            }
        }
        if (synapseConfig.length() < 50000) {
            loadEditArea = true;
        }

    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
        }
    %>

    <% if (loadEditArea) { %>
    <script type="text/javascript">


        editAreaLoader.init({
            id: "rawConfig"		// text area id
            ,syntax: "xml"			// syntax to be uses for highlighting
            ,start_highlight: true  // to display with highlight mode on start-up
            ,toolbar: "search, go_to_line, fullscreen, |, select_font,|, change_smooth_selection, highlight, reset_highlight, word_wrap"
            ,is_editable: false
            ,EA_load_callback: 'lo'
        });

    </script>
    <% } %>

    <div id="middle">
        <h2><fmt:message key="manage.synapse.config"/></h2>
        <div id="workArea">
            <form method="post" name="configform" id="configform" action="index.jsp">
                <div id="saveConfiguration">
                            <span style="margin-top:10px;margin-bottom:10px; display:block;_margin-top:0px;">
                                <fmt:message key="save.advice"/>
                            </span>
                </div>
                <table class="styledLeft" style="width:100%">
                    <thead>
                    <tr>
                        <th>
                            <fmt:message key="esb.configuration"/>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal" style="width:100%">
                                <tr>
                                    <td id="rawConfigTD">
                                                    <textarea name="rawConfig" id="rawConfig"
                                                              style="border:solid 1px #cccccc; width: 99%; height:
                                                              400px; margin-top:5px;"
                                                              disabled><%=synapseConfig%></textarea>

                                        <% if (!loadEditArea) { %>'
                                        <div style="padding:10px;color:#444;">
                                            <fmt:message key="syntax.disabled"/>
                                        </div>
                                        <% } %>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>


