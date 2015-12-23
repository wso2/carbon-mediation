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


<script src="dagre/d3.v3.min.js" charset="utf-8"></script>
<script src="dagre/dagre-d3.js"></script>

<!-- Pull in JQuery dependencies -->
<link rel="stylesheet" href="dagre/tipsy.css">
<link rel="stylesheet" href="css/tree.css">
<script src="dagre/jquery-1.9.1.min.js"></script>
<script src="dagre/tipsy.js"></script>

<link rel="stylesheet" href="vzGrammer/igviz.css">
<script src="vzGrammer/vega.js"></script>
<script src="vzGrammer/igviz.js"></script>


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
            String categoryName = request.getParameter("categoryName");
            AdminData[] collectedSequenceStatistic = null;
            if (statisticCategory.equals("proxy")) {
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllProxyStatistics();

            } else if (statisticCategory.equals("api")) {
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllApiStatistics();

            } else if (statisticCategory.equals("inbound")) {
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllInboundEndpointStatistics();

            } else if (statisticCategory.equals("sequence")) {
                collectedSequenceStatistic = mediationFlowStatisticClient.getAllSequenceStatistics();
            }
    %>


    <div id="middle">
        <h2>Comparing <%=categoryName%>  Flow Statistics</h2>

        <div id="workArea">

            <%
                if ((collectedSequenceStatistic != null) && (collectedSequenceStatistic.length > 0)) {
            %>


            <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                <thead>
                <tr>
                    <th colspan="3">Type of the Statistic</th>
                </tr>
                </thead>
                <tbody>

                <tr>
                    <td style="width:150px"><label for="compare">Select Statistic Parameter
                        to Compare</label><span
                            class="required">*</span></td>
                    <td align="left">
                        <select id="compare" name="compare" class="longInput" onchange="onChange()">
                            <option value="1" selected="selected">Hit Count</option>
                            <option value="2">Min. Processing Time</option>
                            <option value="3">Avg. Processing Time</option>
                            <option value="4">Max. Processing Time</option>
                            <option value="5">Fault Count</option>
                        </select>
                    </td>
                </tr>

                </tbody>
            </table>

            <br/>
            <br/>

            <h2 id="heading">
            </h2>
            <br/>
            <br/>

            <div id="staticBar"></div>
            <script>

                window.onload = function () {
                    onChange();
                };

                function onChange() {
                    var selectedValue = document.getElementById("compare").value;
                    var compareAttribute;
                    var compareInt = parseInt(selectedValue);
                    switch (compareInt) {
                        case 1:
                            compareAttribute = "Count";
                            break;
                        case 2:
                            compareAttribute = "Minimum Time";
                            break;
                        case 3:
                            compareAttribute = "Average Time";
                            break;
                        case 4:
                            compareAttribute = "Maximum Time";
                            break;
                        case 5:
                            compareAttribute = "Fault Count";
                            break;
                    }
                    document.getElementById("heading").innerHTML = "Comparing <%=statisticCategory%> by " + compareAttribute;
                    createJSONObject(compareAttribute, compareInt);
                }


                function createJSONObject(compareAttribute, value) {
                    jsonObject = {};


                    metaDataObject = {};
                    metaDataObject["names"] = ["<%=statisticCategory%> Name", compareAttribute];
                    metaDataObject["types"] = ['C', 'N'];

                    jsonObject ["metadata"] = metaDataObject;
                    jsonString = JSON.stringify(jsonObject);
                    jsonObject ["data"] = populateData(value);
                    jsonString = JSON.stringify(jsonObject);

                    var width = document.getElementById("staticBar").offsetWidth; //canvas width
                    var height = 270;   //canvas height

                    var config = {
                        "title": "Comparison By Count",
                        "yAxis": 1,
                        "xAxis": 0,
                        "width": width,
                        "height": height,
                        "chartType": "bar"
                    };


                    staticChart = igviz.setUp("#staticBar", config, jsonObject);
                    staticChart
                            .setXAxis(
                            {
                                "labelAngle": -35,
                                "labelAlign": "right",
                                "labelDy": 0,
                                "labelDx": 0,
                                "titleDy": 50
                            })
                            .setYAxis(
                            {
                                "titleDy": -30
                            })
                            .setDimension(
                            {
                                height: 270
                            });


                    staticChart.plot(jsonObject.data);

                }


                function populateData(type) {
                    dataObject = [];
                    <%
                    for (AdminData adminData : collectedSequenceStatistic) {
                    %>

                    switch (type) {
                        case 1:
                            dataObject.push(["<%=adminData.getComponentID()%>", "<%=adminData.getTreeNodeData().getCount()%>"]);
                            break;
                        case 2:
                            dataObject.push(["<%=adminData.getComponentID()%>",
                                "<%=adminData.getTreeNodeData().getAvgProcessingTime()%>"]);
                            break;
                        case 3:
                            dataObject.push(["<%=adminData.getComponentID()%>",
                                "<%=adminData.getTreeNodeData().getMinProcessingTime()%>"]);
                            break;
                        case 4:
                            dataObject.push(["<%=adminData.getComponentID()%>", "<%=adminData.getTreeNodeData().getMaxProcessingTime()%>"]);
                            break;
                        case 5:
                            dataObject.push(["<%=adminData.getComponentID()%>", "<%=adminData.getTreeNodeData().getFaultCount()%>"]);
                            break;
                        default:
                            dataObject.push(["<%=adminData.getComponentID()%>", "<%=adminData.getTreeNodeData().getCount()%>"]);
                            break;
                    }

                    <%
                        }
                    %>
                    return dataObject;
                }


            </script>

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
