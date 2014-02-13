<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

<%
    String errorCodes = request.getParameter("codes");
    String inputID = request.getParameter("inputID");

    String[] tmpErrorCodes = errorCodes.split(",");
    ArrayList<String> previousErrorCodes = new ArrayList<String>();
    for (String errCode : tmpErrorCodes) {
        previousErrorCodes.add(errCode);
    }

    Map<String, String> availableErrorCodes = new HashMap<String, String>();
    // Define available synapse error codes
    availableErrorCodes.put("101000", "Receiver IO error sending");
    availableErrorCodes.put("101001", "Receiver IO error receiving");
    availableErrorCodes.put("101500", "Sender IO error sending");
    availableErrorCodes.put("101501", "Sender IO error receiving");
    availableErrorCodes.put("101503", "Connection failed");
    availableErrorCodes.put("101504", "Connection timed out");
    availableErrorCodes.put("101505", "Connection closed");
    availableErrorCodes.put("101506", "HTTP protocol violation");
    availableErrorCodes.put("101507", "Connect cancel");
    availableErrorCodes.put("101508", "Connect timeout");
    availableErrorCodes.put("101509", "Send abort");
    availableErrorCodes.put("101510", "Response processing failure");
%>

<script type="text/javascript">

    function errorCodeEditorCallBack(inputID) {
        var output = "";

        for (var i = 0; i < document.getElementById('errorCodesCount').value; i++) {
            var checkboxID = "errorCodeCheckbox" + i;
            if (document.getElementById(checkboxID).checked) {
                var errorCodeID = "errorCode" + i;
                if (output == "") {
                    output = document.getElementById(errorCodeID).value;
                } else {
                    output = output + "," + document.getElementById(errorCodeID).value;
                }
            }
        }

        document.getElementById(inputID).value = output;
        CARBON.closeWindow();
    }

</script>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">

    <div id="errorCodesEditorContent" style="margin-top:10px; margin-left: 5px; margin-right: 5px;">
        <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
            <tbody>
            <tr>
                <td>
                    <div style="margin-top:10px;">
                        <table border="0" cellpadding="0" cellspacing="0" id="errorCodesTable"
                               class="styledInner">
                            <thead>
                            <tr>
                                <th width="25%"><fmt:message key="errorcode.editor.code"/></th>
                                <th width="50%"><fmt:message
                                        key="errorcode.editor.description"/></th>
                                <th width="5%"><fmt:message key="errorcode.editor.select"/></th>
                            </tr>
                            </thead>
                            <tbody id="errorCodesTBody">
                            <%
                                int i = 0;
                                for (String key : availableErrorCodes.keySet()) { %>
                            <tr id="errorCodeEditorRow<%=i%>">
                                <td align="left">
                                    <input type="hidden" id="errorCode<%=i%>" name="errorCode<%=i%>"
                                           value="<%=key%>"/>
                                    <%=key%>
                                </td>
                                <td>
                                    <%=availableErrorCodes.get(key)%>
                                </td>
                                <td><input type="checkbox" id="errorCodeCheckbox<%=i%>"
                                           name="errorCode<%=i%>"
                                           value="errorCode<%=i%>"
                                        <%=previousErrorCodes.contains(key) ? "checked=\"checked\"" : ""%> />
                                </td>
                            </tr>
                            <%
                                    i++;
                                }
                            %>
                            <input type="hidden" name="errorCodesCount" id="errorCodesCount"
                                   value="<%=i%>"/>
                            </tbody>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td align="right" class="buttonRow" colspan="3">
                    <input id="errorCodesEditorOK" class="button"
                           name="errorCodesEditorOK"
                           type="button"
                           onclick="javascript: errorCodeEditorCallBack('<%=inputID%>'); return false;"
                           href="#"
                           value="<fmt:message key="errorcode.editor.ok"/>"/>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

</fmt:bundle>