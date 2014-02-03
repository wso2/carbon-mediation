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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediation.statistics.ui.client.MediationStatisticsClient" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="org.wso2.carbon.mediation.statistics.stub.StatisticsRecord" %>
<%@ page import="org.wso2.carbon.mediation.statistics.stub.InOutStatisticsRecord" %>
<fmt:bundle basename="org.wso2.carbon.mediation.statistics.ui.i18n.Resources">

    <%
        response.setHeader("Cache-Control", "no-cache");

        String categoryStr = request.getParameter("category");
        int category = 0;
        try {
            category = Integer.parseInt(categoryStr);
        } catch (NumberFormatException e) {
           //TODO error handling
        }

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        MediationStatisticsClient client = new MediationStatisticsClient(configContext,backendServerURL,cookie);

        StatisticsRecord inRecord;
        StatisticsRecord outRecord;
        InOutStatisticsRecord r;

        try {
            if (category == MediationStatisticsClient.SERVER_STATISTICS) {
                r = client.getServerStatistics();
            } else {
                r = client.getCategoryStatistics(category);
            }
            inRecord = r.getInRecord();
            outRecord = r.getOutRecord();
        } catch (RemoteException e) {
    %>
    <jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>

    <%
            return;
        }
    %>

    <%
        if (inRecord != null || outRecord != null) {
    %>
   <table>
       <tr>
            <td width="50%">
                <%
                    if (inRecord != null) {
                %>
                <table class="styledLeft" id="sequenceStatsTable" width="100%">
                    <thead>
                    <tr>
                        <%
                            if (MediationStatisticsClient.ENDPOINT_STATISTICS == category) {
                        %>
                            <th colspan="2" align="left"><fmt:message key="statistics"/></th>
                        <%
                            } else {
                        %>
                            <th colspan="2" align="left"><fmt:message key="in.statistics"/></th>
                        <%
                            }
                        %>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="total.count"/></td>
                        <td><%=inRecord.getTotalCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td>
                            <% if (MediationStatisticsClient.SEQUENCE_STATISTICS == category) { %>
                                <fmt:message key="maximum.mediation.time"/>
                            <% } else { %>
                                <fmt:message key="maximum.response.time"/>
                            <% } %>
                        </td>
                        <td><%=inRecord.getMaxTime()%>ms</td>
                    </tr>
                    <tr class="tableOddRow">
                        <td>
                            <% if (MediationStatisticsClient.SEQUENCE_STATISTICS == category) { %>
                                <fmt:message key="minimum.mediation.time"/>
                            <% } else { %>
                                <fmt:message key="minimum.response.time"/>
                            <% } %>
                        </td>
                        <td>
                            <%if (inRecord.getMinTime() <= 0) { %>
                            &lt; 1.00 ms
                            <%} else {%>
                            <%=inRecord.getMinTime() %> ms
                            <%}%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td>
                            <% if (MediationStatisticsClient.SEQUENCE_STATISTICS == category) { %>
                                <fmt:message key="average.mediation.time"/>
                            <% } else { %>
                                <fmt:message key="average.time"/>
                            <% } %>
                        </td>
                        <%
                            double inAvg = Math.round(inRecord.getAvgTime() * 1000) / 1000;
                            String inAvgStr = String.valueOf(inAvg);
                            if (inAvg <= 0) {
                                inAvgStr = "&lt; 1.00";
                            }
                        %>
                        <td><%=inAvgStr%> ms</td>
                    </tr>
                </table>
                <br/>
                <%
                    }

                    if (outRecord != null) {
                %>
                <table class="styledLeft" id="sequenceStatsTable2" width="100%">
                    <thead>
                    <tr>
                        <th colspan="2" align="left"><fmt:message key="out.statistics"/></th>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="total.count"/></td>
                        <td><%=outRecord.getTotalCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td>
                            <% if (MediationStatisticsClient.SEQUENCE_STATISTICS == category) { %>
                                <fmt:message key="maximum.mediation.time"/>
                            <% } else { %>
                                <fmt:message key="maximum.response.time"/>
                            <% } %>
                        </td>
                        <td>
                            <%if (outRecord.getMaxTime() <= 0) { %>
                            &lt; 1.00 ms
                            <%} else {%>
                            <%=outRecord.getMaxTime() %> ms
                            <%}%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td>
                            <% if (MediationStatisticsClient.SEQUENCE_STATISTICS == category) { %>
                                <fmt:message key="minimum.mediation.time"/>
                            <% } else { %>
                                <fmt:message key="minimum.response.time"/>
                            <% } %>
                        </td>
                        <td>
                            <%if (outRecord.getMinTime() <= 0) { %>
                            &lt; 1.00 ms
                            <%} else {%>
                            <%=outRecord.getMinTime() %> ms
                            <%}%>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td>
                            <% if (MediationStatisticsClient.SEQUENCE_STATISTICS == category) { %>
                                <fmt:message key="average.mediation.time"/>
                            <% } else { %>
                                <fmt:message key="average.time"/>
                            <% } %>
                        </td>
                        <%
                            double outAvg = Math.round(outRecord.getAvgTime() * 1000) / 1000;
                            String outAvgStr = String.valueOf(outAvg);
                            if (outAvg <= 0) {
                                outAvgStr = "&lt; 1.00";
                            }
                        %>
                        <td><%=outAvgStr%> ms</td>
                    </tr>
                </table>
                <%
                    }
                %>
            </td>
            <td width="10px">&nbsp;</td>
            <td>
                <table>
                    <thead>
                        <tr>
                           <th align="left"><u>
                               <% if (MediationStatisticsClient.SEQUENCE_STATISTICS == category) { %>
                                    <fmt:message key="average.mediation.time.vs.time"/>
                               <% } else { %>
                                    <fmt:message key="average.time.vs.time"/>
                               <% } %>
                           </u></th>
                        </tr>
                    </thead>
                    <tr>
                        <td>
                            <div id="responseTimeGraph" style="width:500px;height:300px;"></div>
                        </td>
                    </tr>
                    <%
                        double avgTime = client.calculateAverageTime(r);
                    %>
                    <script type="text/javascript">
                        jQuery.noConflict();
                        function drawResponseTimeGraph() {
                            jQuery.plot(jQuery("#responseTimeGraph"), [
                                {
                                    data: graphAvgResponseTimeArrayObj.get(),
                                    lines: { show: true, fill: true }
                                }
                            ],  {
                                xaxis: {
                                    ticks: 10,
                                    min: 0
                                },
                                yaxis: {
                                    ticks: 10,
                                    min: 0
                                }
                            });
                        }
                        graphAvgResponseTimeArrayObj.add(<%= avgTime%>);
                        drawResponseTimeGraph();
                    </script>
                </table>
           </td>
       </tr>
    </table>
    <%
        } else {
    %>
            <p><fmt:message key="no.server.data"/></p>
    <%
        }
    %>
</fmt:bundle>