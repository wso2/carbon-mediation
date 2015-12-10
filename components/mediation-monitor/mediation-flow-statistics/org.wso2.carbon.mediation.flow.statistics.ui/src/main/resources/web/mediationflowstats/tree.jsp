<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.mediation.flow.statistics.ui.MediationFlowStatisticClient" %>
<%@ page import="org.wso2.carbon.mediation.flow.statistics.stub.TreeNodeData" %>
<%@ page import="org.wso2.carbon.mediation.flow.statistics.stub.StatisticTreeWrapper" %>
<%@ page import="org.wso2.carbon.mediation.flow.statistics.stub.EdgeData" %>
<%@ page import="org.wso2.carbon.mediation.flow.statistics.stub.EdgeData" %>

<script src="dagre/d3.v3.min.js" charset="utf-8"></script>
<script src="dagre/dagre-d3.js"></script>

<!-- Pull in JQuery dependencies -->
<link rel="stylesheet" href="dagre/tipsy.css">
<link rel="stylesheet" href="css/tree.css">
<script src="dagre/jquery-1.9.1.min.js"></script>
<script src="dagre/tipsy.js"></script>

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

            String statisticCategory = request.getParameter("statisticCategory");
            StatisticTreeWrapper statisticTreeWrapper = null;
            String categoryName = request.getParameter("categoryName");

            if (statisticCategory.equals("proxy")) {
                statisticTreeWrapper = mediationFlowStatisticClient.getProxyStatistic(componentID);

            } else if (statisticCategory.equals("api")) {
                statisticTreeWrapper = mediationFlowStatisticClient.getApiStatistic(componentID);

            } else if (statisticCategory.equals("inbound")) {
                statisticTreeWrapper = mediationFlowStatisticClient.getInboundEndpointStatistic(componentID);

            } else if (statisticCategory.equals("sequence")) {
                statisticTreeWrapper = mediationFlowStatisticClient.getSequenceStatistic(componentID);
            }
            EdgeData[] edges = null;
            TreeNodeData[] treeNodeData = null;
            if (statisticTreeWrapper != null) {
                edges = statisticTreeWrapper.getTreeEdges();
                treeNodeData = statisticTreeWrapper.getTreeNodes();
            }
    %>
    <div id="middle">
        <h2>
            Mediation Flow for <%=componentID%> <%=categoryName%>
        </h2>

        <div id="workArea">
            <%
                if (edges == null || treeNodeData == null) {
            %>
            <p style="color: red">Something went wrong when populating data for message flow.</p>
            <%
            } else {
            %>
            <svg width=960 height=1000>></svg>
            <script id="js">
                // Create a new directed graph
                var g = new dagreD3.graphlib.Graph().setGraph({});
                <%
                    for(int i = 0; i < treeNodeData.length; i++) {
                %>
                g.setNode("<%=i%>", {
                    labelType: "html",
                    label: "<b style='text-align: center'><%=treeNodeData[i].getComponentId()%></b><br/>" +
                    "Component Type: <%=treeNodeData[i].getComponentType()%><br/>" +
                    "Hit Count: <%=treeNodeData[i].getCount()%><br/>" +
                    "Maximum Processing Time: <%=treeNodeData[i].getMaxProcessingTime()%><br/>" +
                    "Average Processing Time:<%=treeNodeData[i].getAvgProcessingTime()%><br/>" +
                    "Minimum ProcessingTime: <%=treeNodeData[i].getMinProcessingTime()%><br/>" +
                    "Fault Count: <%=treeNodeData[i].getFaultCount()%>",
                    style: "fill: " +
                    <%
                    //TODO method
                    if(treeNodeData[i].getResponse()){
                    %>
                    "red"
                    <%
                    }else{
                    %>
                    "green"
                    <%
                    }
                    %>
                    ,
                });
                <%
                    }
                %>
                <%for(EdgeData edge:edges){%>
                g.setEdge("<%=edge.getParentNodeIndex()%>", "<%=edge.getNodeIndex()%>", {label: ""});
                <%}%>
                // Create the renderer
                var render = new dagreD3.render();

                render.draw

                // Set up an SVG group so that we can translate the final graph.
                var svg = d3.select("svg");
                var inner = svg.append("g");

                // Set up zoom support
                var zoom = d3.behavior.zoom().on("zoom", function () {
                    inner.attr("transform", "translate(" + d3.event.translate + ")" +
                            "scale(" + d3.event.scale + ")");
                });
                svg.call(zoom);
                // Run the renderer. This is what draws the final graph.
                render(inner, g);
                // Center the graph
                var initialScale = 1;
                zoom.translate([(svg.attr("width") - g.graph().width * initialScale) / 2, 20])
                        .scale(initialScale)
                        .event(svg);
                svg.attr('height', g.graph().height * initialScale + 40);

            </script>
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