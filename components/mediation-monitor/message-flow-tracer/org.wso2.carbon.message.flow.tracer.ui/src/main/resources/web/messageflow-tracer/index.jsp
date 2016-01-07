<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.message.flow.tracer.ui.MessageFlowTracerClient" %>
<%@ page import="org.apache.synapse.messageflowtracer.data.xsd.MessageFlowTraceEntry" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="flowtracercommon.js"></script>
<fmt:bundle basename="org.wso2.carbon.message.flow.tracer.ui.i18n.Resources">
    <carbon:breadcrumb label="messageflowtracer.menu"
                       resourceBundle="org.wso2.carbon.message.flow.tracer.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    MessageFlowTracerClient client;
    MessageFlowTraceEntry[] messageFlows;

    try {
        client = new MessageFlowTracerClient(configContext, serverURL, cookie);
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
        return;
    }

    boolean isOperation = request.getParameter("op") != null;
    String operation = request.getParameter("op");
    if (isOperation) {
        if(operation.equals("clear")){
            client.clearAll();
            %>
            <script type="text/javascript">
                location.href = "index.jsp";
            </script>
            <%
        }
    }

    messageFlows = client.getMessageFlows();
%>

<div id="middle">
    <h2><fmt:message key="message.flow.tracer"/></h2>

    <div class="buttonrow" style="padding-top:10px">
        <input type="button" class="button" value='<fmt:message key="clearAll"/>' onclick="clearAllNew();"/>
    </div>

    <div id="workArea">
        <table class="styledLeft" id="flowTable">
            <thead>
            <tr>
                <th width="30%"><fmt:message key="message.flow.id"/></th>
                <th width="15%"><fmt:message key="EntryType"/></th>
                <th width="15%"><fmt:message key="timestamp"/></th>
            </tr>
            </thead>
            <tbody>
            <%
                if(messageFlows[0]!=null){
                    for(MessageFlowTraceEntry flows:messageFlows){
            %>
            <tr>
                <td><%String messageID = flows.getMessageId();%>
                    <a href="javascript:flowDetails('<%=messageID%>')"><%=messageID%></a></td>
                <td><%=flows.getEntryType()%></td>
                <td><%=flows.getTimeStamp()%></td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>
</div>
</fmt:bundle>