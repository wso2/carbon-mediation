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
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

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

<style type="text/css">
    .graylink {
        color: #aaaaaa !important;
    }
</style>

<fmt:bundle basename="org.wso2.carbon.mediation.artifactuploader.ui.i18n.Resources">
    <script type="text/javascript">
        function submitUploadForm() {
            if (document.uploadForm.synapseArtifactName.value != '') {
                if (document.uploadForm.synapseArtifactName.value.lastIndexOf(".jar") != -1 ||
                        document.uploadForm.synapseArtifactName.value.lastIndexOf(".xar") != -1) {
                    document.uploadForm.submit();
                } else {
                    CARBON.showErrorDialog("<fmt:message key="only.xar.jar"/>");
                }
            } else {
                CARBON.showInfoDialog("<fmt:message key="please.select.artifact"/>");
            }
        }

        function cancelUpload() {
            window.location.href = "index.jsp?ordinal=1";
        }
    </script>

    <carbon:breadcrumb
            resourceBundle="org.wso2.carbon.mediation.artifactadmin.ui.i18n.Resources"
            topPage="false" label="add.artifacts" request="<%=request%>"/>


    <div id="middle">
        <h2><fmt:message key="artifacts.add"/></h2>
        <div id="workArea">
            <p><fmt:message key="artifact.list.desc"/></p>
            <p>&nbsp;</p>
            <table class="styledLeft" cellpadding="1" id="artifactTable">
                <thead>
                <tr>
                    <th><fmt:message key="upload.artifact"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <form action="../../fileupload/synapseArtifact?<csrf:tokenname/>=<csrf:tokenvalue/>"
                        method="post"
                              enctype="multipart/form-data" name="uploadForm">
                            <table class="normal">
                                <tbody>
                                <tr>
                                    <td><label><fmt:message key="upload.text"/></label></td>
                                    <td><input type="file" name="synapseArtifactName"
                                               id="synapseArtifactName"/></td>
                                </tr>
                                </tbody>
                            </table>
                        </form>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="button" value="<fmt:message key="upload"/>" class="button"
                               name="upload" onclick="submitUploadForm()"/>
                        <input type="button" value="<fmt:message key="cancel"/>" class="button"
                               name="upload" onclick="cancelUpload()"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>
