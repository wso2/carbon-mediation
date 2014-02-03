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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediation.artifactuploader.ui.SynapseArtifactUploaderClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="global-params.js"></script>


<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>


<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<fmt:bundle basename="org.wso2.carbon.mediation.artifactuploader.ui.i18n.Resources">
<style type="text/css">
    .graylink {
        color: #aaaaaa !important;
    }
</style>
<script type="text/javascript">
    function removeArtifact(artifactName) {

         CARBON.showConfirmationDialog("<fmt:message key="do.you.want.to.delete.the.artifact"/> " + artifactName + " ?", function() {
            location.href = "remove-artifact.jsp?artifactName=" + artifactName;
        });
    }
</script>


    <carbon:breadcrumb resourceBundle="org.wso2.carbon.mediation.artifactadmin.ui.i18n.Resources"
                       topPage="false" label="list.artifacts" request="<%=request%>"/>
    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        SynapseArtifactUploaderClient client = new SynapseArtifactUploaderClient(
                cookie, serverURL, configContext, request.getLocale());
    %>


    <div id="middle">
        <h2><fmt:message key="artifact.list"/></h2>
        <div id="workArea">            
            <%
               String[] artifacts = client.getArtifacts();
            %>
            <table class="styledLeft" cellpadding="1" id="artifactTable">
                <thead>
                <tr>
                    <th><fmt:message key="artifact.name"/></th>
                    <th><fmt:message key="action"/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (artifacts != null && artifacts.length != 0 && artifacts[0] != null) {
                        for (String artifactName : artifacts) {
                %>
                        <tr>
                            <td>
                                <%=artifactName%>
                            </td>
                            <td>
                                <div class="inlineDiv">
                                    <a href="#" onclick="removeArtifact('<%=artifactName %>')"
                                       class="icon-link"
                                       style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                            key="delete"/></a>
                                </div>
                            </td>
                        </tr>
                <%
                        }
                    } else {
                %>
                        <tr><td colspan="2"><fmt:message key="no.artifacts"/></td></tr>
                <%
                    }
                %>
                </tbody>
            </table>

            <script type="text/javascript">
                alternateTableRows('artifactTable', 'tableEvenRow', 'tableOddRow');
            </script>
        </div>
    </div>
</fmt:bundle>
