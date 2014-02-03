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

        String name = request.getParameter("name");

        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        MediationStatisticsClient client = new MediationStatisticsClient(configContext,backendServerURL,cookie);

        InOutStatisticsRecord record;
        StatisticsRecord inRecord, outRecord;
        try {
            record = client.getServerStatistics();
            inRecord = record.getInRecord();
            outRecord = record.getOutRecord();
        } catch (RemoteException e) {
    %>
    <jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>

    <%
            return;
        }
    %>
   <table>
       <tr>
            <td width="50%">
                <%
                    if (inRecord != null) {
                %>
                <table class="styledLeft" id="serverStatsTable" width="100%">
                    <thead>
                    <tr>
                        <th colspan="2" align="left"><%=name%> <fmt:message key="in.statistics"/></th>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="total.count"/></td>
                        <td><%=inRecord.getTotalCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="fault.count"/></td>
                        <td><%=inRecord.getFaultCount() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="maximum.response.time"/></td>
                        <td><%=inRecord.getMaxTime()%>ms</td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="minimum.response.time"/></td>
                        <td>
                            <%if (inRecord.getTotalCount() > 0 && inRecord.getMinTime() <= 0) { %>
                            &lt; 1 ms
                            <%} else {%>
                            <%=inRecord.getMinTime() %> ms
                            <%}%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="average.time"/></td>
                        <%
                            double inAvg = Math.round(inRecord.getAvgTime() * 1000) / 1000;
                            String inAvgStr = String.valueOf(inAvg);
                            if (inRecord.getTotalCount() > 0 && inAvg <= 0) {
                                inAvgStr = "&lt; 1.00";
                            }
                        %>
                        <td><%=inAvgStr%> ms</td>
                    </tr>
                </table>
                <%
                    }
                    if (outRecord != null) {
                %>
                <table class="styledLeft" id="serverStatsTable" width="100%">
                    <thead>
                    <tr>
                        <th colspan="2" align="left"><%=name%> <fmt:message key="out.statistics"/></th>
                    </tr>
                    </thead>
                    <tr class="tableOddRow">
                        <td width="40%"><fmt:message key="total.count"/></td>
                        <td><%=outRecord.getTotalCount() %>
                        </td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="fault.count"/></td>
                        <td><%=outRecord.getFaultCount() %>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="maximum.response.time"/></td>
                        <td><%=outRecord.getMaxTime()%>ms</td>
                    </tr>
                    <tr class="tableEvenRow">
                        <td><fmt:message key="minimum.response.time"/></td>
                        <td>
                            <%if (outRecord.getTotalCount() > 0 && outRecord.getMinTime() <= 0) { %>
                            &lt; 1 ms
                            <%} else {%>
                            <%=outRecord.getMinTime() %> ms
                            <%}%>
                        </td>
                    </tr>
                    <tr class="tableOddRow">
                        <td><fmt:message key="average.time"/></td>
                        <%
                            double outAvg = Math.round(outRecord.getAvgTime() * 1000) / 1000;
                            String outAvgStr = String.valueOf(outAvg);
                            if (outRecord.getTotalCount() > 0 && outAvg <= 0) {
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
                           <th align="left"><u><fmt:message key="average.time.vs.time"/></u></th>
                        </tr>
                    </thead>
                    <tr>
                        <td>
                            <div id="responseTimeGraph" style="width:500px;height:300px;"></div>
                        </td>
                    </tr>
                    <%
                        double avgTime = client.calculateAverageTime(record);
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
</fmt:bundle>