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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediation.statistics.ui.client.MediationStatisticsClient" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<script type="text/javascript" src="js/queue4graphs.js"></script>
<script type="text/javascript" src="js/statistics.js"></script>
<script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
<script type="text/javascript" src="../admin/js/excanvas.js"></script>
<script type="text/javascript" src="global-params.js"></script>
<fmt:bundle basename="org.wso2.carbon.mediation.statistics.ui.i18n.Resources">
<carbon:breadcrumb label="Endpoint Statistics"
		resourceBundle="org.wso2.carbon.mediation.statistics.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <%
        response.setHeader("Cache-Control", "no-cache");

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        MediationStatisticsClient client = new MediationStatisticsClient(configContext,backendServerURL,cookie);

        Map<String, List<String>> endpointOperations = new HashMap<String, List<String>>();
        String[] endpoints = new String[0];
        try {
            endpoints = client.listEndPoints();
            if (endpoints != null) {
                for (String e : endpoints) {
                    String key = e;
                    if (e.contains("__")) {
                        key = e.substring(0, e.lastIndexOf("__"));
                    }

                    if (!endpointOperations.containsKey(key)) {
                        endpointOperations.put(key, new ArrayList<String>());
                    }
                    endpointOperations.get(key).add(e);
                }
            }
        } catch (RemoteException e) {
           //TODO error handling
        }
    %>
    <script type="text/javascript">
        initResponseTimeGraph('50');
    </script>
    <div id="middle">
        <h2><fmt:message key="endpoint.statistics"/> (<fmt:message key="all.endpoints"/>)</h2>
        <div id="workArea">
            <table width="100%">
                <tr>
                    <td>
                       <div id="result"></div>
                            <script type="text/javascript">
                                jQuery.noConflict();
                                var refresh;
                                function refreshStats() {
                                    var url = "category_graph_ajaxprocessor.jsp?category=<%=MediationStatisticsClient.ENDPOINT_STATISTICS%>";
                                    jQuery("#result").load(url, null, function (responseText, status, XMLHttpRequest) {
                                        if (status != "success") {
                                            stopRefreshStats();
                                        }
                                    });
                                }
                                function stopRefreshStats() {
                                    if (refresh) {
                                        clearInterval(refresh);
                                    }
                                }
                                jQuery(document).ready(function() {
                                    refreshStats();
                                    refresh = setInterval("refreshStats()", 6000);
                                });
                            </script>
                    </td>
                </tr>
                <tr height="10"></tr>
                <tr>
                    <td>
                        <table class="styledLeft" id="endpointTable" style="width:100% !important;">
                            <thead>
                                <tr>
                                    <th colspan="2"><fmt:message key="endpoint.ops"/></th>
                                </tr>
                            </thead>
                            <% if (endpoints == null || endpoints.length == 0) { %>
                                <tr>
                                    <td colspan="2"><fmt:message key="no.endpoint.data"/></td>
                                </tr>
                            <% } else { %>
                            <%      for (String endPoint : endpointOperations.keySet()) { %>
                                    <tr>
                                        <td>
                                            <a href="endpoint.jsp?name=<%=endPoint%>"><%=endPoint%></a>
                                        </td>
                                        <td>
                                            <ul style="padding-left:10px;">
                                            <%
                                                for (String op : endpointOperations.get(endPoint)) {
                                                    if (op.lastIndexOf("__") > 0) {
                                            %>
                                                    <li style="list-style-type: circle ! important;"><a href="endpoint.jsp?name=<%=op%>"><%=op.substring(op.lastIndexOf("__") + 2)%></a></li>
                                            <%
                                                    }
                                                }
                                            %>
                                            </ul>
                                        </td>
                                    </tr>
                            <%      }
                                }%>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </div>
<script type="text/javascript">
    alternateTableRows('endpointTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>