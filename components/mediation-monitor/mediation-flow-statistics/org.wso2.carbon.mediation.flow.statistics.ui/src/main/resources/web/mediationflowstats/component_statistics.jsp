<%--
  ~ *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~ *
  ~ *  WSO2 Inc. licenses this file to you under the Apache License,
  ~ *  Version 2.0 (the "License"); you may not use this file except
  ~ *  in compliance with the License.
  ~ *  You may obtain a copy of the License at
  ~ *
  ~ *    http://www.apache.org/licenses/LICENSE-2.0
  ~ *
  ~ * Unless required by applicable law or agreed to in writing,
  ~ * software distributed under the License is distributed on an
  ~ * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ * KIND, either express or implied.  See the License for the
  ~ * specific language governing permissions and limitations
  ~ * under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.mediation.flow.statistics.ui.MediationFlowStatisticClient" %>
<%@ page import="org.wso2.carbon.mediation.flow.statistics.stub.AdminData" %>


<!--[if IE]><script language="javascript" type="text/javascript" src="js/excanvas.min.js"></script><![endif]-->
<script language="javascript" type="text/javascript" src="js/jquery.js"></script>
<script language="javascript" type="text/javascript" src="js/jquery.flot.js"></script>
<script language="javascript" type="text/javascript" src="js/jquery.flot.pie.js"></script>


<script type="text/javascript" src="js/statistics.js"></script>
<script type="text/javascript" src="js/graphs.js"></script>

<fmt:bundle basename="org.wso2.carbon.mediation.flow.statistics.ui.i18n.Resources">
    <carbon:breadcrumb label="Mediation Flow Statistics"
                       resourceBundle="org.wso2.carbon.mediation.flow.statistics.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <%
        try {
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            MediationFlowStatisticClient mediationFlowStatisticClient =
                    new MediationFlowStatisticClient(configContext, serverURL, cookie);

            String statisticCategory = request.getParameter("statisticCategory");
            String categoryName = null;
            AdminData[] collectedSequenceStatistic = null;
            if (statisticCategory.equals("proxy")) {
                categoryName = "Proxy Service";
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllProxyStatistics();

            } else if (statisticCategory.equals("api")) {
                categoryName = "API";
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllApiStatistics();

            } else if (statisticCategory.equals("inbound")) {
                categoryName = "Inbound Endpoint";
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllInboundEndpointStatistics();

            } else if (statisticCategory.equals("sequence")) {
                categoryName = "Sequence";
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllSequenceStatistics();
            }else if (statisticCategory.equals("endpoint")) {
                categoryName = "Endpoint";
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllEndpointStatistics();
            }
    %>
    <div id="middle">
        <h2><%=categoryName%> Mediation Flow Statistics</h2>

        <div id="workArea">

            <%
                if ((collectedSequenceStatistic != null) && (collectedSequenceStatistic.length > 0)) {
            %>

            <table>
                <tbody>
                <tr>
                    <td>
                        <a href="overall_stat.jsp?statisticCategory=<%=statisticCategory%>&categoryName=<%=categoryName%>&compare=1"
                           style="cursor:pointer">
                            Compare Proxy Service Statistics
                        </a>
                    </td>
                </tr>
                </tbody>
            </table>

            <table id="myTable" class="styledLeft">
                <thead>
                <tr>
                    <th><%=categoryName%> Name</th>
                    <th>Component Type</th>
                    <th>Count</th>
                    <th>Minimum Processing Time (ms)</th>
                    <th>Maximum Processing Time (ms)</th>
                    <th>Average Processing Time (ms)</th>
                    <th>Fault Count</th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (AdminData adminData : collectedSequenceStatistic) {
                %>
                <tr>
                    <td>
                        <a href="newtree.jsp?componentID=<%=adminData.getComponentID()%>&statisticCategory=<%=statisticCategory%>&categoryName=<%=categoryName%>">
                            <%=adminData.getComponentID()%>
                        </a>
                    </td>
                    <td>
                        <%=adminData.getTreeNodeData().getComponentType()%>
                    </td>
                    <td>
                        <%=adminData.getTreeNodeData().getCount()%>
                    </td>
                    <td>
                        <%=adminData.getTreeNodeData().getMinProcessingTime()%>
                    </td>
                    <td>
                        <%=adminData.getTreeNodeData().getMaxProcessingTime()%>
                    </td>
                    <td>
                        <%=adminData.getTreeNodeData().getAvgProcessingTime()%>
                    </td>
                    <td>
                        <%=adminData.getTreeNodeData().getFaultCount()%>
                    </td>
                </tr>
                <%
                    }
                %>

                </tbody>

            </table>

            <%
            } else {
            %>
            <p style="color: red">There are no <%=categoryName%> statistics collected for this category.</p>
            <%
                }
            %>
        </div>
    </div>
    <%
    } catch (Throwable e) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            CARBON.showErrorDialog('<%=e.getMessage()%>');
        });
    </script>
    <%
            return;
        }
    %>


</fmt:bundle>
