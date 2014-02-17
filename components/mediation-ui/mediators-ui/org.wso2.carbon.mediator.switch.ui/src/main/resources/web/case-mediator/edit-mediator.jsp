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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.switchm.SwitchCaseMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.mediator.switchm.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.switchm.ui.i18n.JSResources"
		request="<%=request%>"
        i18nObjectName="casei18n" />
    <%
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof SwitchCaseMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }

        SwitchCaseMediator switchCaseMediator = (SwitchCaseMediator)mediator;
        Pattern pattern = switchCaseMediator.getRegex();
        String caseValueString = "";
        if (pattern != null) {
            caseValueString = pattern.toString();
        }
    %>

    <div>
        <script type="text/javascript" src="../case-mediator/js/mediator-util.js"></script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="case.mediator"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td>
                                <fmt:message key="case.value"/> <span class="required">*</span>
                            </td>
                            <td>
                                <input type="text" value='<%=caseValueString%>' id="caseValue" name="caseValue" size="40"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>
</fmt:bundle>
