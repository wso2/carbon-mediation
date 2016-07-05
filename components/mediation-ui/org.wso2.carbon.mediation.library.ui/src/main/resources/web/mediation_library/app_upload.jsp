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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediation.library.ui.i18n.Resources">
    <carbon:breadcrumb label="libs.headertext"
                       resourceBundle="org.wso2.carbon.mediation.library.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript">

        function validate() {
            var fileName = document.appsUpload.filename.value;
            if (fileName == '') {
                CARBON.showWarningDialog('<fmt:message key="select.car.file"/>');
            } else if (fileName.lastIndexOf(".zip") != -1) {
                document.appsUpload.submit();
            } else {
                CARBON.showWarningDialog('<fmt:message key="select.car.file"/>');
            }
        }

    </script>


    <div id="middle">
        <h2><fmt:message key="libs.headertext"/></h2>

        <div id="workArea">
.            <form method="post" name="appsUpload" action="../../fileupload/mediationlib?<csrf:tokenname/>=<csrf:tokenvalue/>"
                  enctype="multipart/form-data" target="_self">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message
                                key="libs.upload.car.legend"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tr>
                                    <td>

                                        <label><fmt:message
                                                key="libs.upload.car.label"/></label>
                                    </td>
                                    <td>
                                        <input type="file" id="filename" name="filename" size="50"/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input name="upload" type="button" class="button"
                                   value=" <fmt:message key="libs.upload"/> "
                                   onclick="validate();"/>
                            <input type="button" class="button" onclick="javascript:location.href='../mediation_library/index.jsp'"
                                   value=" <fmt:message key="libs.cancel"/> "/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>