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

        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers.
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        MediationFlowStatisticClient mediationFlowStatisticClient =
                new MediationFlowStatisticClient(configContext, serverURL, cookie);

        String messageId = request.getParameter("componentID");

        StatisticTreeWrapper statisticTreeWrapper = mediationFlowStatisticClient.getSequenceStatistic(messageId);

        EdgeData[] edges = statisticTreeWrapper.getTreeEdges();
        TreeNodeData[] treeNodeData = statisticTreeWrapper.getTreeNodes();


    %>
    <div id="middle">


        <!-- <link rel="stylesheet" href="demo.css"> -->
        <script src="dagre/d3.v3.min.js" charset="utf-8"></script>
        <script src="dagre/dagre-d3.js"></script>

        <!-- Pull in JQuery dependencies -->
        <link rel="stylesheet" href="dagre/tipsy.css">
        <script src="dagre/jquery-1.9.1.min.js"></script>
        <script src="dagre/tipsy.js"></script>

        <style id="css">
            text {
                font-weight: 300;
                font-family: "Helvetica Neue", Helvetica, Arial, sans-serf;
                font-size: 14px;
            }

            .node rect {
                stroke: #333;
                fill: #fff;
            }

            .edgePath path {
                stroke: #333;
                fill: #333;
                stroke-width: 1.5px;
            }

            .node text {
                pointer-events: none;
            }

            table#t01 {
                border: 1px solid black;
                border-collapse: collapse;
            }

            table#t01 th {
                border: 1px solid black;
                border-collapse: collapse;
            }

            table#t01 td {
                border: 1px solid black;
                border-collapse: collapse;
            }

            /* This styles the title of the tooltip */
            .tipsy .name {
                font-size: 1.5em;
                font-weight: bold;
                color: #000;
                margin: 0;
            }

            .tipsy .tipsy-inner {
                background-color: #FFF;
                color: #000;
                resize: horizontal;
                max-width: 900px;
                min-width: 100px;
            }

            /* This styles the body of the tooltip */
            .tipsy .description {
                font-size: 1.2em;
                color: #000;
            }

        </style>

        <svg width=960 height=1000></svg>

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
            zoom
                    .translate([(svg.attr("width") - g.graph().width * initialScale) / 2, 20])
                    .scale(initialScale)
                    .event(svg);
            svg.attr('height', g.graph().height * initialScale + 40);

        </script>
    </div>
</fmt:bundle>