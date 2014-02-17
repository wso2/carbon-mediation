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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.switchm.SwitchCaseMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SwitchCaseMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to update the mediator");
    }

    SwitchCaseMediator switchCaseMediator = (SwitchCaseMediator) mediator;

    String caseValue = request.getParameter("caseValue");
    caseValue = StringEscapeUtils.escapeXml(caseValue);
    switchCaseMediator.setRegex(Pattern.compile(caseValue));
%>
<script type="text/javascript">
    document.location.href = "../sequences/design_sequence.jsp?ordinal=1";
</script>
