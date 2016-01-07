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

            <table border="0" class="styledLeft">
                <tbody>
                <tr>
                    <td>
                        Show Full Statistics Tree :
                        <button type="button" onclick="showFullTree()">Show Full Tree</button>
                    </td>
                    <td>
                        Show Root Node :
                        <button type="button" onclick="showRootNode()">Show Root</button>
                    </td>
                </tr>
                </tbody>
            </table>

            <br />

            <%
                if (edges == null || treeNodeData == null) {
            %>
            <p style="color: red">Something went wrong when populating data for message flow.</p>
            <%
            } else {
            %>

            <div class="live map">
                <svg width=960 height=1000></svg>
            </div>

            <script>
                // Create a new directed graph
                var render = dagreD3.render();
                g = new dagreD3.graphlib.Graph().setGraph({});
                var nodeCount = 0;
                var predecessors = {};
                var successor = {};
                var inEdges = {};
                var stateOfChildren = [];
                var showMoreNodeData = [];
                var clickedOnce = false;
                var timer;

                var simpleNodes = [];
                var fullNodes = [];


                var PROXY_SERVICE = "Proxy Service";
                var API = "API";
                var INBOUND_ENDPOINT = "Inbound EndPoint";
                var MEDIATOR = "Mediator";
                var ENDPOINT = "Endpoint";
                var RESOURCE = "API Resource";
                var SEQUENCE = "Sequence";
                var FAULT_HANDLER = "Fault Handler";

                var PROXY_SERVICE_NODE_CLASS = "proxy";
                var API_NODE_CLASS = "api";
                var INBOUND_ENDPOINT_NODE_CLASS = "inbound";
                var MEDIATOR_NODE_CLASS = "mediator";
                var ENDPOINT_NODE_CLASS = "endpoint";
                var RESOURCE_NODE_CLASS = "resource";
                var SEQUENCE_NODE_CLASS = "sequence";
                var FAULT_HANDLER_NODE_CLASS = "fault";

                //Starting initializing the statistic tree----------------------------------
                <%
                    for(int i = 0; i<treeNodeData.length;i++){
                        TreeNodeData nodeData = treeNodeData[i];
                        String nodeName = nodeData.getComponentId();
                %>
                var nodeClass = getNodeClass("<%=nodeData.getComponentType()%>");
                var html = "<div class=small>";
                html += "<span class=type></span>";
                html += "<table>";
                html += "<tr><td><span class=name><%=nodeName%></span></tr></td>";
                html += "</table>";
                html += "</div>";
                var nodeLabel = {
                    labelType: "html",
                    label: html,
                    rx: 5,
                    ry: 5,
                    padding: 0,
                    class: nodeClass
                };
                simpleNodes.push(nodeLabel);

                html = "<div class=large>";
                html += "<span class=type></span>";
                html += "<table>";
                html += "<tr><td><span class=name_underlined><%=nodeName%></span></tr></td>";
                html += "<tr><td><span class=description>Component Type: <%=nodeData.getComponentType()%></span></tr></td>";
                html += "<tr><td><span class=description>Hit Count: <%=nodeData.getCount()%></span></tr></td>";
                html += "<tr><td><span class=description>Maximum Processing Time (ms) : <%=nodeData.getMaxProcessingTime()%></span></tr></td>";
                html += "<tr><td><span class=description>Average Processing Time (ms) :<%=nodeData.getAvgProcessingTime()%></span></tr></td>";
                html += "<tr><td><span class=description>Minimum ProcessingTime (ms) : <%=nodeData.getMinProcessingTime()%></span></tr></td>";
                html += "<tr><td><span class=description>Fault Count: <%=nodeData.getFaultCount()%></span></tr></td>";
                html += "</table>";
                html += "</div>";
                nodeLabel = {
                    labelType: "html",
                    label: html,
                    rx: 5,
                    ry: 5,
                    padding: 0,
                    class: nodeClass
                };
                fullNodes.push(nodeLabel);
                <%
                }
                %>

                //Getting necessary node details by creating full tree

                <%
                for(int i = 0; i < treeNodeData.length; i++) {
                %>
                g.setNode("<%=i%>", {});
                <%
                }
                for(EdgeData edge:edges){
                %>
                g.setEdge("<%=edge.getParentNodeIndex()%>", "<%=edge.getNodeIndex()%>", {label: ""});
                <%
                }
                %>
                predecessors = g._preds;
                successor = g._sucs;
                inEdges = g._in;
                nodeCount = g._nodeCount;

                for (var i = 0, len = nodeCount; i < len; i++) {
                    stateOfChildren[i] = showMoreNodeData[i] = false;
                }

                //Finished initializing the statistic tree ------------------------------------------------


                window.onload = function () {
                    g = new dagreD3.graphlib.Graph().setGraph({});
                    g.setNode(0, JSON.parse(JSON.stringify(simpleNodes[0])));
                    draw();
                };

                function getNodeClass(componentType) {
                    var nodeClass;
                    switch (componentType) {
                        case MEDIATOR:
                            nodeClass = MEDIATOR_NODE_CLASS;
                            break;
                        case SEQUENCE:
                            nodeClass = SEQUENCE_NODE_CLASS;
                            break;
                        case ENDPOINT:
                            nodeClass = ENDPOINT_NODE_CLASS;
                            break;
                        case PROXY_SERVICE:
                            nodeClass = PROXY_SERVICE_NODE_CLASS;
                            break;
                        case RESOURCE:
                            nodeClass = RESOURCE_NODE_CLASS;
                            break;
                        case API:
                            nodeClass = API_NODE_CLASS;
                            break;
                        case INBOUND_ENDPOINT:
                            nodeClass = INBOUND_ENDPOINT_NODE_CLASS;
                            break;
                        case FAULT_HANDLER_NODE_CLASS:
                            nodeClass = FAULT_HANDLER_NODE_CLASS;
                            break;
                    }
                    return nodeClass;
                }

                function showFullTree(){
                    for (var i = 0; i < simpleNodes.length; i++) {
                        if(stateOfChildren[i] == false){
                            showImmediateSuccessors(i);
                        }
                    }
                    draw();
                }

                function showRootNode(){
                    destroySuccessors(0);
                    draw();
                }

                function destroySuccessors(i) {
                    stateOfChildren[i] = false;
                    Object.keys(successor[i]).forEach(function (key) {
                        destroySuccessors(key);
                        g.removeNode(key);
                        stateOfChildren[key] = showMoreNodeData[key] = false;
                    });
                }

                function showImmediateSuccessors(i) {
                    stateOfChildren[i] = true;
                    Object.keys(successor[i]).forEach(function (key) {
                        showAllPredecessors(key);
                        if (!(key in g._nodes)) {
                            showInEdgesAndNode(key);
                        }
                    });
                }

                function showAllPredecessors(i) {
                    Object.keys(predecessors[i]).forEach(function (key) {
                        if (!(key in g._nodes)) {
                            showAllPredecessors(key);
                            showInEdgesAndNode(key);
                        }
                        if (Object.keys(successor[key]).length == 1) {
                            stateOfChildren[key] = true;
                        }
                    });
                }

                function showInEdgesAndNode(i) {
                    g.setNode(i, JSON.parse(JSON.stringify(simpleNodes[i])));
                    Object.keys(inEdges[i]).forEach(function (key) {
                        g.setEdge(this[key]["v"], this[key]["w"], {label: ""});
                    }, inEdges[i]);
                }


                // Toggle children on click.
                function nodeClick(d) {
                    if (stateOfChildren[d]) {
                        destroySuccessors(d);
                    } else {
                        showImmediateSuccessors(d);
                    }
                    draw();
                    clickedOnce = false;
                }

                function clickEvent(d) {
                    if (clickedOnce) {
                        clickedOnce = false;
                        clearTimeout(timer);
                    } else {
                        timer = setTimeout(function () {
                            nodeClick(d);
                        }, 500);
                        clickedOnce = true;
                    }
                }

                function nodeDoubleClick(d) {
                    if (showMoreNodeData[d]) {
                        g._nodes[d].label = JSON.parse(JSON.stringify(simpleNodes[d].label));
                    } else {
                        g._nodes[d].label = JSON.parse(JSON.stringify(fullNodes[d].label));
                    }
                    showMoreNodeData[d] = !showMoreNodeData[d];
                    draw();
                }

                // Create a new directed graph
                function draw() {

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
                    g.graph().transition = function (selection) {
                        return selection.transition().duration(500);
                    };

                    // Render the graph into svg g
                    d3.select("svg g").call(render, g);
                    // Center the graph
                    var initialScale = 1;
                    zoom.translate([(svg.attr("width") - g.graph().width * initialScale) / 2, 20])
                            .scale(initialScale)
                            .event(svg);
                    svg.attr('height', g.graph().height * initialScale + 40);

                    inn = svg.select("g");

                    var selections = inn.selectAll("g.node");
                    selections
                            .on("click", function (d) {
                                clickEvent(d);
                            })
                            .on("dblclick", function (d) {
                                nodeDoubleClick(d);
                            });
                }

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