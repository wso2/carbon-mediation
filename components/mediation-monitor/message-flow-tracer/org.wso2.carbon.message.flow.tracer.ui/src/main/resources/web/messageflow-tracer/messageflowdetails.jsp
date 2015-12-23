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

    table#t01 th{
        border: 1px solid black;
        border-collapse: collapse;
    }

    table#t01 td{
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
        resize:horizontal; max-width:900px; min-width:100px;
    }

    /* This styles the body of the tooltip */
    .tipsy .description {
        font-size: 1.2em;
        color: #fff;
    }

    </style>

    <svg width=960 height=1000></svg>

    <script id="js">
    // Create a new directed graph
    var g = new dagreD3.graphlib.Graph().setGraph({});

    var states = <%=nodesMap%>;

    // Add states to the graph, set labels, and style
    Object.keys(states).forEach(function(state) {
      var value = states[state];
//      value.label = state;
      value.rx = value.ry = 5;
      g.setNode(state, value);
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

    // Simple function to style the tooltip for the given node.
    var styleTooltip = function(beforepayload, afterpayload, beforeproperties, afterproperties) {

        var x = (beforepayload+"").split("\n").join("<br>");
        var y = (afterpayload+"").split("\n").join("<br>");

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

        return "<h2>Before Payload</h2>"+x+"<br><hr>"+"<h2>Before Properties</h2>"+beforePropTable+"<br><hr>"+"<h2>After Payload</h2>"+y+"<br><hr>"+"<h2>After Properties</h2>"+afterPropTable;
    };

    // Run the renderer. This is what draws the final graph.
    render(inner, g);

    inner.selectAll("g.node")
            .attr("title", function(v) { return styleTooltip(g.node(v).beforepayload, g.node(v).afterpayload, g.node(v).beforeproperties, g.node(v).afterproperties) })
            .each(function(v) {
                $(this).tipsy({ trigger: 'focus', fade:false, gravity: 'n' , opacity: 1, html: true, offset: 10 });
            });

//    inner.selectAll("g.node")
//      .attr("title", function(v) { return styleTooltip(g.node(v).label, g.node(v).description) })
//      .each(function(v) { $(this).tipsy({ gravity: "w", opacity: 1, html: true }); });

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