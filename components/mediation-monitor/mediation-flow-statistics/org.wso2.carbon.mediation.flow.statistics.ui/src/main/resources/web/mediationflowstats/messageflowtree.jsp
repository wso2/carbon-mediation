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
<%@ page import="org.wso2.carbon.mediation.flow.statistics.ui.MediationFlowStatisticClient" %>

<script src="dagre/d3.v3.min.js" charset="utf-8"></script>
<script src="dagre/dagre-d3.js"></script>

<!-- Pull in JQuery dependencies -->
<link rel="stylesheet" href="dagre/tipsy.css">
<link rel="stylesheet" href="css/messageflowtree.css">
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
            String categoryName = request.getParameter("categoryName");
            String categoryId = request.getParameter("categoryId");
            String flowId = request.getParameter("flowId");
            String requestToBE = categoryId + ":" + componentID + ":" + flowId;
            String jsonTree = mediationFlowStatisticClient.getMessageFlowDetails(requestToBE);
    %>
    <div id="middle">
        <h2>
            Mediation Flow for <%=componentID%> <%=categoryName%> : <%=flowId%>
        </h2>

        <div id="workArea">

            <%
                if (jsonTree == null) {
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
                var tree = <%=jsonTree%>;
                var clickedOnce = false;
                var timer;
                var simpleNodes = [];
                var fullNodes = [];
                var showMoreNodeData = [];


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
                function getLabels(){
                    for(var i = 0; i<tree.nodeList.length;i++)
                    {
                        var nodeClass = getNodeClass(tree.nodeList[i].componentType);
                        var html = "<div class=small>";
                        html += "<span class=type></span>";
                        html += "<table>";
                        html += "<tr><td><span class=name>" + tree.nodeList[i].componentId + "</span></tr></td>";
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
                        html += "<tr><td><span class=name_underlined>" + tree.nodeList[i].componentId + "</span></tr></td>";
                        html += "<tr><td><span class=description>Component Type : " + tree.nodeList[i].componentType +  "</span></tr></td>";
                        html += "<tr><td><span class=description>ProcessingTime (ms) : " + tree.nodeList[i].processingTime + "</span></tr></td>";
                        html += "<tr><td><span class=description>Fault Count : " + tree.nodeList[i].faultCount + "</span></tr></td>";
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

                        showMoreNodeData[i] = false;
                    }

                }

                window.onload = function () {
                    g = new dagreD3.graphlib.Graph().setGraph({});
                    getLabels();
                    for (var i = 0; i < tree.nodeList.length ;i++) {
                        g.setNode(i, JSON.parse(JSON.stringify(simpleNodes[i])));
                    }
                    for (var j = 0; j < tree.edgeList.length; j++) {
                        g.setEdge(tree.edgeList[j].parentNode, tree.edgeList[j].childNode, {label: ""});
                    }
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