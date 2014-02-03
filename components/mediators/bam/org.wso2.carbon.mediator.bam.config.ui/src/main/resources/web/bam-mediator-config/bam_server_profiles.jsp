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
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.BamServerProfilesHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    response.setHeader("Cache-Control", "no-cache");
%>

<fmt:bundle basename="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources">

    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources"
            request="<%=request%>" i18nObjectName="bamjsi18n"/>
    <carbon:breadcrumb
            label="bam.server.profiles"
            resourceBundle="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <%!
        public static final String SERVER_PROFILE_LOCATION = "bamServerProfiles";
    %>

    <script type="text/javascript">
        function removeServerProfile(profileName){
            window.location.href = "bam_server_profiles.jsp?serverProfileName=" + profileName + "&action=remove";
        }
        function editServerProfile(profileName){
            window.location.href = "configure_server_profiles.jsp?txtServerProfileLocation=" + profileName + "&hfAction=load";
        }
        function removeForcefully(profileName){
            window.location.href = "bam_server_profiles.jsp?serverProfileName=" + profileName + "&action=remove&force=true";
        }
        function reloadPage(){
            window.location.href = "bam_server_profiles.jsp";
        }
        function addServerProfile(){
            window.location.href = "configure_server_profiles.jsp";
        }
    </script>

    <div id="middle">
        <%
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

            BamServerProfilesHelper bamServerProfilesHelper =
                    new BamServerProfilesHelper(cookie, backendServerURL, configContext, request.getLocale());

            if(!bamServerProfilesHelper.resourceAlreadyExists(SERVER_PROFILE_LOCATION)){
                bamServerProfilesHelper.addCollection(SERVER_PROFILE_LOCATION);
            }

            String[] serverNameList = bamServerProfilesHelper.getServerProfileList(SERVER_PROFILE_LOCATION);
            String action = request.getParameter("action");
            String profileName = request.getParameter("serverProfileName");
            String force = request.getParameter("force");
            if(bamServerProfilesHelper.isNotNullOrEmpty(action) && action.equals("remove") &&
               bamServerProfilesHelper.isNotNullOrEmpty(profileName)){
                if(bamServerProfilesHelper.isNotNullOrEmpty(force) && force.equals("true")){
                    bamServerProfilesHelper.removeResource(SERVER_PROFILE_LOCATION + "/" + profileName);
            %>

            <script type="text/javascript">
                CARBON.showInfoDialog("Server Profile was successfully deleted.", reloadPage);
            </script>

            <%
                } else {
                    %>

                    <script type="text/javascript">
                        //CARBON.showConfirmationDialog("Are you sure you want to remove the existing Server Profile?", removeForcefully, reloadPage, true);
                        var remove = confirm("Are you sure you want to remove the existing Server Profile?");
                        if(remove){
                            removeForcefully("<%=profileName%>");
                        } else {
                            reloadPage();
                        }
                    </script>

                    <%
                }
            } else {

        %>

        <h2>
            <fmt:message key="bam.server.profiles"/>
        </h2>
        <div id="workArea">
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                <thead>
                    <tr>
                        <th>
                            <fmt:message key="server.profile.name"/>
                        </th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (String serverName : serverNameList) {
                            %>
                                 <tr>
                                     <td>
                                         <%=serverName%>
                                     </td>
                                     <td>
                                         <span><a onClick='javaScript:removeServerProfile("<%=serverName%>")' style='background-image:url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Profile</a></span>
                                         <span><a onClick='javaScript:editServerProfile("<%=serverName%>")' style='background-image:url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Profile</a></span>
                                     </td>
                                 </tr>
                            <%
                        }
                    }
                    %>
                </tbody>
            </table>
        </div>
        <span><a onClick='javaScript:addServerProfile()' style='background-image:
                                        url(../admin/images/add.gif);'class='icon-link addIcon'>Add Profile</a></span>
    </div>

</fmt:bundle>