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

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="configcommon.js"></script>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet" />
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>


<script type="text/javascript">
    var allowTabChange = true;
    var emtpyEntries = false;

//    $(function() {
//        var $myTabs = $("#tabs");
//
//        $myTabs.tabs({
//            select: function(event, ui) {
//                if (!allowTabChange) {
//                    alert("Tab selection is disabled, while you are in the middle of a workflow");
//                }
//                return allowTabChange;
//            },
//
//            show: function(event, ui) {
//                var selectedTab = $myTabs.tabs('option', 'selected');
//                allowTabChange = true;
//            }
//        });
//
//        $myTabs.tabs('select', 0);
//        if(emtpyEntries){
//           $myTabs.tabs('select', 1);
//        }
//    });

    <%--var tabIndex = 0;--%>
    <%--<%--%>
        <%--String tab = request.getParameter("tab");--%>
        <%--if(tab != null && tab.equals("0")) {--%>
    <%--%>--%>
        <%--tabIndex = 0;--%>
    <%--<%--%>
        <%--} else if (tab != null && tab.equals("1")) {--%>
    <%--%>--%>
        <%--tabIndex = 1;--%>
    <%--<%--%>
        <%--}--%>
    <%--%>--%>
//    $(document).ready(function() {
//        var $tabs = $('#tabs > ul').tabs({ cookie: { expires: 30 } });
//        $('a', $tabs).click(function() {
//            if ($(this).parent().hasClass('ui-tabs-selected')) {
//                $tabs.tabs('load', $('a', $tabs).index(this));
//            }
//        });
//        if (tabIndex == 0) {
//            $tabs.tabs('option', 'selected', 0);
//        } else if (tabIndex == 1) {
//            $tabs.tabs('option', 'selected', 1);
//        }
//    });
</script>

<script type="text/javascript">
    function addConfig() {
        window.location.href = "new.jsp";
    }

    function activateSequence(name) {
        window.location.href = "activate.jsp?name=" + name + "&action=activate";
    }

    function editSequence() {
        var $myTabs = $("#tabs");
        $myTabs.tabs('select', 0);
    }

    function addExistingConfig() {
        window.location.href = "existingConfig.jsp";
    }

    function deleteSequence(name) {
        window.location.href = "activate.jsp?name=" + name + "&action=delete";
    }
</script>

<style type="text/css">
    .graylink{
        color:#aaaaaa !important;
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
        String status = request.getParameter("status");
        ResourceBundle bundle = ResourceBundle.getBundle(
                "org.wso2.carbon.mediation.configadmin.ui.i18n.Resources", request.getLocale());

        if ("newConfigCreated".equals(status)) {
    %>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                CARBON.showInfoDialog('<%=bundle.getString("activated.configuration")%>');
            });
        </script>
    <%
        } else if ("activated".equals(status)) {
    %>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                CARBON.showInfoDialog('<%=bundle.getString("activated.configuration")%>');
            });
        </script>
    <%
        } else if ("updated".equals(status)) {
    %>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                CARBON.showInfoDialog('<%=bundle.getString("configuration.updated.successfully")%>');
            });
        </script>
    <%
        } else if ("updateFailed".equals(status)) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog('<%=bundle.getString("failed.to.update.configuration")%>');
        });
    </script>
    <%
    }
    else if ("session".equals(status)) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog('<%=bundle.getString("failed.to.update.configuration.session")%>');
        });
    </script>
    <%
    } else if ("deleted".equals(status)) {
    %>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                CARBON.showInfoDialog('<%=bundle.getString("deleted.configuration")%>');
            });
        </script>
    <%
        }
    %>

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
                msg += "Remove faulty items and continue?";

    %>
        <script type="text/javascript">
            jQuery(document).ready(function() {
                CARBON.showConfirmationDialog("<%=msg%>", function() {
                    forceUpdateConfiguration(document.configform);
                });
            });
        </script>
    <%
            } else {
                activeConfiguration = client.getConfiguration(session);
            }

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
			if(synapseConfig.length() < 50000){
				loadEditArea = true;
			}
			
            synapseConfigList = (List<ConfigurationInformation>) configurationList.getResult();
            if (synapseConfigList == null) {
                synapseConfigList = new ArrayList<ConfigurationInformation>();
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

	<% if(loadEditArea){ %>
	<script type="text/javascript">
    editAreaLoader.init({
        id : "rawConfig"		// text area id
        ,syntax: "xml"			// syntax to be uses for highlighting
        ,start_highlight: true  // to display with highlight mode on start-up
    });
    </script>
	<% } %>

    <div id="middle">
        <h2><fmt:message key="manage.synapse.config"/></h2>
        <div id="workArea">
            <%--<div id="tabs">--%>
                <%--<ul>--%>
                    <%--<li><a href="#tabs-1"><fmt:message key="tab.1.text"/></a></li>--%>
                    <%--<li><a href="#tabs-2"><fmt:message key="tab.2.text"/></a></li>--%>
                <%--</ul>--%>
                <%--<div id="tabs-1">--%>
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
                                                              style="border:solid 1px #cccccc; width: 99%; height: 400px; margin-top:5px;"><%=synapseConfig%></textarea>
															  
													<% if(!loadEditArea){ %>		  
													<div style="padding:10px;color:#444;">
														<fmt:message key="syntax.disabled"/>
													</div>
													<% } %>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="buttonRow">
                                        <button class="button" onclick="updateConfiguration(document.configform); return false;"><fmt:message key="update"/></button>
                                        <button class="button" onclick="resetConfiguration(); return false;"><fmt:message key="reset"/></button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </form>
                <%--</div>--%>
                <%--<div id="tabs-2">--%>
                    <%--<table class="styledLeft" cellspacing="1" id="configTable">--%>
                        <%--<thead>--%>
                            <%--<tr>--%>
                                <%--<th><fmt:message key="name"/></th>--%>
                                <%--<th><fmt:message key="description"/></th>--%>
                                <%--<th colspan="3"><fmt:message key="action"/></th>--%>
                            <%--</tr>--%>
                        <%--</thead>--%>
                        <%--<tbody>--%>
                            <%--<%--%>
                                <%--for (ConfigurationInformation info : synapseConfigList) {--%>
                            <%--%>--%>
                                <%--<tr>--%>
                                    <%--<td><%=info.getName()%></td>--%>
                                    <%--<td><%=info.getDescription() != null ? info.getDescription() : "" %></td>--%>
                                    <%--<td style="width:100px">--%>
                                        <%--<%--%>
                                            <%--if (info.getActive()) {--%>
                                        <%--%>--%>
                                        <%--<div class="inlineDiv">--%>
                                            <%--<a href="#" onclick="editSequence()"--%>
                                               <%--class="icon-link"--%>
                                               <%--style="background-image:url(../admin/images/edit.gif);"><fmt:message key="edit"/></a>--%>
                                        <%--</div>--%>
                                        <%--<%--%>
                                            <%--} else {--%>
                                        <%--%>--%>
                                        <%--<div class="inlineDiv">--%>
                                            <%--<a href="#" class="icon-link graylink"--%>
                                               <%--style="background-image:url(../configadmin/images/edit-gray.gif);"><fmt:message key="edit"/></a>--%>
                                        <%--</div>--%>
                                        <%--<%--%>
                                            <%--}--%>
                                        <%--%>--%>
                                    <%--</td>--%>
                                    <%--<td style="border-left:none;width:100px">--%>
                                        <%--<%--%>
                                            <%--if (!info.getActive()) {--%>
                                        <%--%>--%>
                                        <%--<div class="inlineDiv">--%>
                                            <%--<a href="#" onclick="activateSequence('<%=info.getName()%>')"--%>
                                               <%--class="icon-link"--%>
                                               <%--style="background-image:url(../configadmin/images/activate_new.gif);"><fmt:message key="activate"/></a>--%>
                                        <%--</div>--%>
                                        <%--<%--%>
                                            <%--} else {--%>
                                        <%--%>--%>
                                        <%--<div class="inlineDiv">--%>
                                            <%--<a href="#"--%>
                                               <%--class="icon-link"--%>
                                               <%--style="background-image:url(../configadmin/images/configuration.gif);"><fmt:message key="activate.conf"/></a>--%>
                                        <%--</div>--%>
                                        <%--<%--%>
                                            <%--}--%>
                                        <%--%>--%>
                                    <%--</td>--%>
                                    <%--<td style="border-left:none;width:100px">--%>
                                        <%--<%--%>
                                            <%--if (!info.getActive()) {--%>
                                        <%--%>--%>
                                        <%--<div class="inlineDiv">--%>
                                            <%--<a href="#" onclick="deleteSequence('<%=info.getName()%>')"--%>
                                               <%--class="icon-link"--%>
                                               <%--style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>--%>
                                        <%--</div>--%>
                                        <%--<%--%>
                                            <%--} else {--%>
                                        <%--%>--%>
                                        <%--<div class="inlineDiv">--%>
                                            <%--<a href="#"--%>
                                               <%--class="icon-link graylink"--%>
                                               <%--style="background-image:url(../configadmin/images/delete-gray.gif);"><fmt:message key="delete"/></a>--%>
                                        <%--</div>--%>
                                        <%--<%--%>
                                            <%--}--%>
                                        <%--%>--%>
                                    <%--</td>--%>
                                <%--</tr>--%>
                            <%--<%--%>
                                <%--}--%>
                            <%--%>--%>
                        <%--</tbody>--%>
                    <%--</table>--%>
                    <%--<div style="height:25px;">--%>
                        <%--<a class="icon-link" style="background-image: url(../admin/images/add.gif);"--%>
                           <%--onclick="addConfig()"><fmt:message key="add.configuration"/></a>--%>
                    <%--</div>--%>
                <%--</div>--%>
            <%--</div>--%>
        </div>
    </div>
</fmt:bundle>


