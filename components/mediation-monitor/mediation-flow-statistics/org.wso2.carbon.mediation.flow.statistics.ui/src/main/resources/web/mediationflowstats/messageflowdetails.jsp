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
<%@ page import="org.wso2.carbon.mediation.flow.statistics.stub.StatisticDataHolder" %>


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

            String componentID = request.getParameter("componentID");
            String categoryName = request.getParameter("categoryName");
            String categoryId = request.getParameter("categoryId");

            String requestToBE = categoryId + ":" + componentID;
            StatisticDataHolder[] statisticDataHolders = mediationFlowStatisticClient.getAllMessageFlows(requestToBE);

    %>
    <div id="middle">
        <h2>Mediation Flows of <%=componentID%></h2>

        <div id="workArea">

            <%
                if ((statisticDataHolders != null) && (statisticDataHolders.length > 0)) {
            %>

            <table>
                <tbody>
                <tr>
                    <td><%--correct--%>
                        <a href="flowcomparisson.jsp?categoryId=<%=categoryId%>&componentID=<%=componentID%>&categoryName=<%=categoryName%>"
                           style="cursor:pointer">
                            Compare <%=componentID%> Message Flows
                        </a>
                    </td>
                </tr>
                </tbody>
            </table>

            <table id="statTable" class="styledLeft">
                <thead>
                <tr>
                    <th>Time Stamp</th>
                    <th>Message Flow Id</th>
                    <th>Processing Time</th>
                    <th>Fault Count</th>
                </tr>
                </thead>
                <tbody>
                <%
                    for (StatisticDataHolder statisticDataHolder : statisticDataHolders) {
                %>
                <tr>
                    <td>
                        <a href="messageflowtree.jsp?flowId=<%=statisticDataHolder.getMessageFlowId()%>&categoryId=<%=categoryId%>&componentID=<%=componentID%>&categoryName=<%=categoryName%>">
                            <%=statisticDataHolder.getTimeStamp()%>
                        </a>
                    </td>

                    <td>
                        <%=statisticDataHolder.getMessageFlowId()%>
                    </td>
                    <td>
                        <%=statisticDataHolder.getProcessingTime()%>
                    </td>
                    <td>
                        <%=statisticDataHolder.getFaultCount()%>
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
