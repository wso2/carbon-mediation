<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.message.flow.tracer.ui.MessageFlowTracerClient" %>
<%@ page import="org.wso2.carbon.message.flow.tracer.data.xsd.Edge" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.message.flow.tracer.ui.i18n.Resources">
    <carbon:breadcrumb label="message.flow.chart"
                       resourceBundle="org.wso2.carbon.message.flow.tracer.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    %>
    <%
        MessageFlowTracerClient client;
        String messageId = request.getParameter("messageid");

        String nodesMap;
        Edge[] edges;

        try {
            client = new MessageFlowTracerClient(configContext, serverURL, cookie);
            nodesMap = client.getAllComponents(messageId);
            edges = client.getAllEdges(messageId);
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
    </script>
    <%
            return;
        }
    %>
    <div id="middle">
        <h2><fmt:message key="message.flow.chart"/></h2>


    <script src="dagre/d3.v3.min.js" charset="utf-8"></script>
    <script src="dagre/dagre-d3.js"></script>

    <link rel="stylesheet" href="css/tree.css">

    <div style="height: 800px;">
        <div class="live map">
            <svg width=960 height=1000></svg>
        </div>
    </div>

    <div id="node_properties" style="display: inline-block;"><i>Select a node to view properties</i></div>

    <script id="js">
    // Create a new directed graph
    var g = new dagreD3.graphlib.Graph().setGraph({});

    var states = <%=nodesMap%>;

    // Add states to the graph, set labels, and style
    Object.keys(states).forEach(function(state) {
      var value = states[state];

    var html = "<div class=small>";
      html += "<span class=type></span>";
      html += "<span class=name>"+value.label.split(":")[1]+"</span>";
      html += "</div>";

    var nodeLabel = {
            labelType : "html",
            label : html,
            rx : 5,
            ry : 5,
            padding: 0,
            class: nodeStyleClass(value.label)
    };



      g.setNode(state, nodeLabel);
    });

    <%for(Edge edge:edges){%>
        g.setEdge("<%=edge.getNode1()%>",     "<%=edge.getNode2()%>",     { label: "" });
    <%}%>

    // Create the renderer
    var render = new dagreD3.render();

    // Set up an SVG group so that we can translate the final graph.
    var svg = d3.select("svg"),
        inner = svg.append("g");

    // Set up zoom support
    var zoom = d3.behavior.zoom().on("zoom", function() {
        inner.attr("transform", "translate(" + d3.event.translate + ")" +
                                    "scale(" + d3.event.scale + ")");
      });
    svg.call(zoom);

    function nodeStyleClass(name) {
        var type = name.split(":")[0];
        var result = "mediator";

        switch (type) {
            case "MEDIATOR":
                result =  "mediator"; break;
            case "ENDPOINT":
                result =  "endpoint"; break;
            case "SEQUENCE":
                {
                    var nameValue = name.split(":")[1];

                    if (nameValue == "PROXY_FAULTSEQ" || nameValue == "API_FAULTSEQ")
                        result =  "fault";
                    else
                        return "sequence";
                    break;
                }
            case "REST_API":
                result =  "api"; break;
            case "Inbound Endpoint":
                result =  "inbound"; break;
            case "Proxy":
                result =  "proxy"; break;

        }

        return result;

    };



    // Simple function to style the tooltip for the given node.
    var styleTooltip = function(data) {

        var beforepayload = data.beforepayload;
        var afterpayload = data.afterpayload;
        var beforeproperties = data.beforeproperties;
        var afterproperties = data.afterproperties;

        var x = (beforepayload+"").split("\n").join("<br>");
        x = "<div style='height:200px;overflow: auto;'>" + x + "</div>";
        var y = (afterpayload+"").split("\n").join("<br>");
        y = "<div style='height:200px;overflow: auto;'>" + y + "</div>";

        var a = (beforeproperties+"").split(",");
        var b = (afterproperties+"").split(",");

        var beforePropTable = "<table id = \"t01\" style=\"width:100%\">";

        for (var i in a) {
            var property = a[i].split("=");
            beforePropTable = beforePropTable + "<tr><td>"+property[0]+"</td><td>"+property[1]+"</td><tr>";
        }

        beforePropTable = beforePropTable + "</table>";

        var afterPropTable = "<table id = \"t01\" style=\"width:100%\">";

        for (var i in b) {
            var property = b[i].split("=");
            afterPropTable = afterPropTable + "<tr><td>"+property[0]+"</td><td>"+property[1]+"</td><tr>";
        }

        afterPropTable = afterPropTable + "</table>";

        var topic = "<br/><h2><b>" + data.label + "</b></h2><br/>"

        return topic + "<table style='width:100%'>"
                        + "<thead><tr><th style='width:50%'>Before Payload<hr/></th><th style='width:50%'>After Payload<hr/></th></tr></thead>"
                        + "<tbody><tr><td>" + x + "</td><td>" + y + "</td></tr></tbody>"
                        + "<thead><tr><th style='width:50%'>Before Properties<hr/></th><th style='width:50%'>After Properties<hr/></th></tr></thead>"
                        + "<tbody><tr><td>" + beforePropTable + "</td><td>" + afterPropTable + "</td></tr></tbody>"
                        + "</table>";
    };

    // Run the renderer. This is what draws the final graph.
    render(inner, g);

    inner.selectAll("g.node")
            .on("click", function(d) {
                document.getElementById("node_properties").innerHTML = styleTooltip(states[d]);
                window.location.href="#node_properties";
            });


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