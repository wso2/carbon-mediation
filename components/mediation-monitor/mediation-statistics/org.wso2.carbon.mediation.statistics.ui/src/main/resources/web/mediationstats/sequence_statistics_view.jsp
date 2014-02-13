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
<script type="text/javascript" src="js/queue4graphs.js"></script>
<script type="text/javascript" src="js/statistics.js"></script>
<script type="text/javascript" src="../admin/js/jquery.flot.js"></script>
<script type="text/javascript" src="../admin/js/excanvas.js"></script>
<script type="text/javascript" src="global-params.js"></script>
<fmt:bundle basename="org.wso2.carbon.mediation.statistics.ui.i18n.Resources">
<carbon:breadcrumb label="Sequence Statistics"
		resourceBundle="org.wso2.carbon.mediation.statistics.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <%
        response.setHeader("Cache-Control", "no-cache");

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        MediationStatisticsClient client = new MediationStatisticsClient(configContext,backendServerURL,cookie);

        String[] sequences = new String[0];
        try {
            sequences = client.listSequence();
        } catch (RemoteException e) {
           //TODO error handling
        }
    %>
    <script type="text/javascript">
        initResponseTimeGraph('50');
    </script>
    <div id="middle">
        <h2><fmt:message key="sequence.statistics"/> (<fmt:message key="all.sequences"/>)</h2>
        <div id="workArea">
            <table width="100%">
                <tr>
                    <td>
                       <div id="result"></div>
                            <script type="text/javascript">
                                jQuery.noConflict();
                                var refresh;
                                function refreshStats() {
                                    var url = "category_graph_ajaxprocessor.jsp?category=<%=MediationStatisticsClient.SEQUENCE_STATISTICS%>";
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
                <tr height="10"/>
                <tr>
                    <td>
                        <table class="styledLeft" id="sequenceTable" style="width:100% !important;">
                            <thead>
                                <tr>
                                    <th><fmt:message key="sequences"/></th>
                                </tr>
                            </thead>
                            <% if (sequences == null || sequences.length == 0) { %>
                                <tr>
                                    <td><fmt:message key="no.sequence.data"/></td>
                                </tr>
                            <% } else { %>
                            <%      for (String sequence : sequences) { %>
                                    <tr>
                                        <td>
                                            <a href="sequence.jsp?name=<%=sequence%>"><%=sequence%></a>
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
    alternateTableRows('sequenceTable', 'tableEvenRow', 'tableOddRow');
</script>
</fmt:bundle>