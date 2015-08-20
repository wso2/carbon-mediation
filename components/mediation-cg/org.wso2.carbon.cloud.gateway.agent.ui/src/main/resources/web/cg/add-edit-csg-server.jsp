<%--

  Copyright (c) 20010-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
  Version 2.0 (the "License"); you may not use this file except
  in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.

--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.ui.CGAgentAdminClient" %>
<%@ page import="org.wso2.carbon.cloud.gateway.agent.stub.types.carbon.CGServerBean" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGUtils" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGConstant" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%
    String forwardTo = request.getParameter("forwardTo");
    boolean isEditMode = false;
    String host = "", port = "", userName = "", passWord = "", domain = "", serverURL = "";
    String serverName = request.getParameter("serverName");
    if (request.getParameter("mode") != null && request.getParameter("mode").equals("edit")) {
        session.setAttribute("mode", "edit");
        isEditMode = true;
    }
    try {
        if (serverName != null) {
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            CGAgentAdminClient cgAdminClient = new CGAgentAdminClient(
                    cookie, backendServerURL, configContext, request.getLocale());

            CGServerBean bean = cgAdminClient.getCGServer(serverName);
            host = bean.getHost();
            port = bean.getPort();
            domain = bean.getDomainName();
            userName = bean.getUserName();
            passWord = bean.getPassWord();
            if (domain != null && !"".equals(domain)) {
                userName = userName + "@" + domain;
            }
            serverURL = "https://" + host + ":" + port;

        } else {
            serverName = "";
            userName = CGUtils.getStringProperty(CGConstant.CG_USER_NAME, CGConstant.DEFAULT_CG_USER);
        }
    } catch (AxisFault axisFault) {
        CarbonUIMessage.sendCarbonUIMessage("Cloud not retrieve the CSG server" + serverName + "info",
                CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>

<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources">

<script type="text/javascript">
    function cancel() {
        location.href = 'index.jsp';
    }

    function showWhatIsThisHelp() {
        CARBON.showInfoDialog('<fmt:message key="csg.whatis.this.text"/>');
    }

    function saveChanges() {
        var forWardString;
    <%
    if(forwardTo !=null && !"null".equals(forwardTo)) {
    %>
        forWardString = '?forwardTo=<%=forwardTo%>';
    <%
    } else {
    %>
        forWardString = '';
    <%
        }
    %>
        document.csgServerForm.action = 'save-csg-server.jsp' + forWardString;
        if (doValidation()) {
            document.csgServerForm.submit();
        }
    }

    function doValidation() {
        var serverName = document.getElementById('csg_server_name_id').value;
        if (isEmpty(serverName)) {
            CARBON.showWarningDialog("<fmt:message key="csg.error.empty.servername"/>");
            return false;
        }

        var serverURL = document.getElementById('csg_server_url_id').value;
        if (isEmpty(serverURL)) {
            CARBON.showWarningDialog("<fmt:message key="csg.error.empty.serverurl"/>");
            return false;
        } else {
            var httpsPrefix = serverURL.substring(0, 8); // string https://
            if (httpsPrefix != "https://") {
                CARBON.showWarningDialog("<fmt:message key="csg.error.invalid.https.url"/>");
                return false;
            }
        }

    <%--var domainName = document.getElementById('csg_domain_name_id').value;--%>
    <%--if (!isEmpty(domainName) && !isValidDomain(domainName)) {--%>
    <%--CARBON.showWarningDialog("<fmt:message key="csg.error.invalid.domain"/>");--%>
    <%--return false;--%>
    <%--}--%>

    <%--var csgHostPort = document.getElementById('csg_host_port_id').value;--%>
    <%--if(isEmpty(csgHostPort)){--%>
    <%--CARBON.showWarningDialog("<fmt:message key="csg.error.empty.port"/>");--%>
    <%--return false;--%>
    <%--}--%>
    <%--if(isNaN(csgHostPort)){--%>
    <%--CARBON.showWarningDialog("<fmt:message key="csg.error.not.a.number"/>");--%>
    <%--return false;--%>
    <%--}--%>

        var userName = document.getElementById('csg_user_name_id').value;
        if (isEmpty(userName)) {
            CARBON.showWarningDialog("<fmt:message key="csg.error.empty.username"/>");
            return false;
        }

        var password = document.getElementById('csg_user_password_id').value;
        if (isEmpty(password)) {
            CARBON.showWarningDialog("<fmt:message key="csg.error.empty.password"/>");
            return false;
        }

        return true;
    }

    function isEmpty(fieldName) {
        return fieldName == null || fieldName == undefined || fieldName == 'undefined' || fieldName.length == 0;
    }

    function isValidDomain(nname) {

        var val = true;
        var dot = nname.lastIndexOf(".");
        var dname = nname.substring(0, dot);
        var ext = nname.substring(dot, nname.length);
        //alert(ext);

        if (ext.indexOf("-trial") >= 0 || ext.indexOf("-unverified") >= 0) {
            // we are not allowing to create a domain with -trial or -unverified is in the extension
            CARBON.showWarningDialog("<fmt:message key="csg.invalid.domain"/>");
            return false;
        }

        if (ext.indexOf("/") >= 0 || ext.indexOf("\\") >= 0) {
            CARBON.showWarningDialog("<fmt:message key="csg.invalid.encoding"/>");
            return false;
        }

        if (dot > 1 && dot < 57) {

            if (!val) {
                CARBON.showWarningDialog("<fmt:message key="csg.invalid.extension"/>");
                return false;
            }
            else {
                for (var j = 0; j < dname.length; j++) {
                    var dh = dname.charAt(j);
                    var hh = dh.charCodeAt(0);
                    if ((hh > 47 && hh < 59) || (hh > 64 && hh < 91) || (hh > 96 && hh < 123) || hh == 45 || hh == 46) {
                        if ((j == 0 || j == dname.length - 1) && hh == 45) {
                            CARBON.showWarningDialog("<fmt:message key="csg.invalid.name"/>");
                            ;
                            return false;
                        }
                    }
                    else {
                        CARBON.showWarningDialog("<fmt:message key="csg.has.special.characters"/>");
                        return false;
                    }
                }
            }
        }
        else {
            CARBON.showWarningDialog("<fmt:message key="csg.long.domain"/>");
            ;
            return false;
        }
        return true;
    }

</script>

<carbon:breadcrumb
        label="csg.add.server"
        resourceBundle="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<div id="middle">
    <h2>
        <%
            if (isEditMode) {
        %>
        <fmt:message key="csg.edit.server"/>
        <%
        } else {
        %>
        <fmt:message key="csg.add.server"/>
        <%
            }
        %>
    </h2>

    <div id="workArea">
        <%
            if(CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PERMISSION_STRING)){
        %>
        <form method="post" action="save-csg-server.jsp" name="csgServerForm">
            <table class="styledLeft" id="csgServerInfoTableId" width="60%">
                <thead>
                <tr>
                    <th><fmt:message key="csg.server.info"/></th>
                </tr>
                </thead>
                <tr>
                    <td class="formRaw">
                        <table class="normal">
                            <tr>
                                <td width="5%" style="white-space:nowrap;">
                                    <fmt:message key="csg.server.name"/> <span
                                        class="required">*</span>
                                </td>
                                <td align="left" colspan="2">
                                    <label for="csg_server_name_id"></label>
                                    <input type="text" name="csg_server_name_id"
                                           id="csg_server_name_id" value="<%=serverName%>"
                                            <%=isEditMode ? "disabled=\"disabled\"" : ""%>
                                           onkeypress="return validateText(event);"/>
                                    <%
                                        if (isEditMode) {
                                    %>
                                    <input type="hidden" name="csg_server_name_id_hidden"
                                           id="csg_server_name_id_hidden" value="<%=serverName%>"/>
                                    <%
                                        }
                                    %>
                                    <label><fmt:message key="csg.new.server.label.name"/></label>
                                </td>
                            </tr>
                            <tr>
                                <td width="5%" style="white-space:nowrap;">
                                    <fmt:message key="csg.server.url"/> <span
                                        class="required">*</span>
                                </td>
                                <td align="left" colspan="2">
                                    <label for="csg_server_url_id"></label><input type="text"
                                                                                  name="csg_server_url_id"
                                                                                  id="csg_server_url_id"
                                                                                  value="<%=serverURL%>"/>
                                    <label><fmt:message
                                            key="csg.new.server.label.serverurl"/></label>
                                    <label><a href="#" onclick="showWhatIsThisHelp();"
                                              class="icon-link"
                                              style="background-image:url(images/help.png);"><fmt:message
                                            key="csg.help.what"/></a></label>
                                </td>
                            </tr>
                            <tr>
                                <td width="5%" style="white-space:nowrap;">
                                    <fmt:message key="csg.user.name"/> <span
                                        class="required">*</span>
                                </td>
                                <td align="left" colspan="2">
                                    <label for="csg_user_name_id"></label><input type="text"
                                                                                 name="csg_user_name_id"
                                                                                 id="csg_user_name_id"
                                                                                 value="<%=userName%>"/>
                                    <label><fmt:message
                                            key="csg.new.server.label.username"/></label>
                                </td>
                            </tr>
                            <tr>
                                <td width="5%" style="white-space:nowrap;">
                                    <fmt:message key="csg.user.password"/> <span
                                        class="required">*</span>
                                </td>
                                <td align="left" colspan="2">
                                    <label for="csg_user_password_id"></label>
                                    <input type="password" name="csg_user_password_id"
                                           id="csg_user_password_id" value="<%=passWord%>"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <label>
                            <input type="button" class="button" value="<fmt:message key="save"/>"
                                   onclick="saveChanges();"/>
                        </label>
                        <label>
                            <input type="button" class="button" value="<fmt:message key="cancel"/>"
                                   onclick="cancel();"/>
                        </label>
                    </td>
                </tr>
            </table>
        </form>
        <%
            }
        %>
    </div>
</div>
</fmt:bundle>