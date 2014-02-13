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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<%
    String serviceName = request.getParameter("serviceName");
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProxyServiceAdminClient client = new ProxyServiceAdminClient(
            configContext, backendServerURL, cookie, request.getLocale());
    ProxyData pd = client.getProxy(serviceName);
    String name = pd.getName();
%>
<carbon:jsi18n
    resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.JSResources"
    request="<%=request%>"
    i18nObjectName="proxyi18n"
/>

    <tr>
        <td id="edit<%=name%>">
            <a href="#" onclick="editPS();return false;"><img src="../admin/images/edit.gif" alt="" border="0"> Edit</a>
        </td>
        <td id="enableStat<%=name%>">
            <a href="#" onclick="statOnOffPS('<%=name%>', 'enable');return false;"><img src="../admin/images/static-icon-disabled.gif" alt="" border="0"> Enable Statistics</a>
        </td>
        <td id="disableStat<%=name%>">
            <a href="#" onclick="statOnOffPS('<%=name%>', 'disable');return false;"><img src="../admin/images/static-icon.gif" alt="" border="0"> Disable Statistics</a>
        </td>
    </tr>

    <tr>
        <td id="redeploy<%=name%>">
            <a href="#" onclick="redeployPS('<%=name%>');return false;"><img src="../proxyservices/images/core/icon-redeploy.gif" alt="" border="0"> Redeploy</a>
        </td>
        <td id="enableTracing<%=name%>">
            <a href="#" onclick="traceOnOffPS('<%=name%>', 'enable');return false;"><img src="../admin/images/trace-icon-disabled.gif" alt="" border="0"> Enable Tracing</a>
        </td>
        <td id="disableTracing<%=name%>">
            <a href="#" onclick="traceOnOffPS('<%=name%>', 'disable');return false;"><img src="../admin/images/trace-icon.gif" alt="" border="0"> Disable Tracing</a>
        </td>
    </tr>

<script type="text/javascript" src="../carbon/global-params.js"></script>
<script type="text/javascript">
    <fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">
    jQuery(document).ready(function() {
        showHideOptionsOnLoad();
    });

    function showHideOptionsOnLoad() {
        if (<%=pd.getEnableStatistics()%>) {
            hideElem('enableStat' + '<%=name%>')
            showElem('disableStat' + '<%=name%>')
        } else {
            hideElem('disableStat' + '<%=name%>')
            showElem('enableStat' + '<%=name%>')
        }
        if (<%=pd.getEnableTracing()%>) {
            hideElem('enableTracing' + '<%=name%>');
            showElem('disableTracing' + '<%=name%>');
        } else {
            hideElem('disableTracing' + '<%=name%>');
            showElem('enableTracing' + '<%=name%>');
        }
    }

    function showElem(id) {
        document.getElementById(id).style.display = "";
    }

    function hideElem(id) {
        document.getElementById(id).style.display = "none";
    }

    wso2.wsf.Util.initURLs();
    var frontdendURL = wso2.wsf.Util.getServerURL() + "/";

    function editPS() {
        window.location.href='../proxyservices/index.jsp?header=' + '<fmt:message key="header.modify"/>' + '&serviceName=<%=name%>&startwiz=true';
    }

    function redeployPS() {
        jQuery.get("../proxyservices/config-ajaxprocessor.jsp", {'psName' : arguments[0], 'operation' : 'redeploy'},
                    function(data,status) {
                        handleProxyCallBack(data, status,
                                "successfully.redeployed.proxy.service", "unable.to.redeploy.proxy.service", null, null);
                    });
    }

    function statOnOffPS() {
        var command = arguments[1];
        if ("enable" == command) {
            jQuery.get("../proxyservices/config-ajaxprocessor.jsp", {'psName' : arguments[0], 'operation' : 'enableStat'},
                    function(data,status) {
                        handleProxyCallBack(data,status, "successfully.enabled.statistics", "unable.to.enable.statistics",
                                "disableStat", "enableStat");
                    });
        } else if ("disable" == command) {
            jQuery.get("../proxyservices/config-ajaxprocessor.jsp", {'psName' : arguments[0], 'operation' : 'disableStat'},
                    function(data,status) {
                        handleProxyCallBack(data,status, "successfully.disabled.statistics", "unable.to.disable.statistics",
                                "enableStat", "disableStat");
                    });
        }
    }

    function traceOnOffPS() {
        var command = arguments[1];
        if ("enable" == command) {
            jQuery.get("../proxyservices/config-ajaxprocessor.jsp", {'psName' : arguments[0], 'operation' : 'enableTrace'},
                    function(data,status) {
                        handleProxyCallBack(data,status, "successfully.enabled.tracing", "unable.to.enable.tracing",
                                "disableTracing", "enableTracing");
                    });
        } else if ("disable" == command) {
            jQuery.get("../proxyservices/config-ajaxprocessor.jsp", {'psName' : arguments[0], 'operation' : 'disableTrace'},
                    function(data,status) {
                        handleProxyCallBack(data,status, "successfully.disabled.tracing", "unable.to.disable.tracing",
                                "enableTracing", "disableTracing");
                    });
        }
    }

    function handleProxyCallBack(data, status, textOnSuccess, textOnFail, show, hide) {
        if (data.replace(/^\s+|\s+$/g, '') != 'successful') {
            CARBON.showErrorDialog(proxyi18n[textOnFail]);
        } else {
            if (show != null && hide != null) {
                showHideIcon(show,hide);
            }
            CARBON.showInfoDialog(proxyi18n[textOnSuccess]);
        }
    }

    function showHideIcon(show, hide) {
        hideElem(hide + '<%=name%>');
        showElem(show + '<%=name%>');
    }
    </fmt:bundle>
</script>