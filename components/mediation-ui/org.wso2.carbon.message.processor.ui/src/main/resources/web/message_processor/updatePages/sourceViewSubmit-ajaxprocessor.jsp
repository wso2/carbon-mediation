<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.wso2.carbon.message.processor.ui.utils.MessageProcessorData" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.ui.CarbonSecuredHttpContext" %>

<%
    boolean loggedIn = session.getAttribute(CarbonSecuredHttpContext.LOGGED_USER) != null;
    if (!loggedIn) {
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
</script>
<%
    } else {
        String configuration = request.getParameter("messageProcessorString");
        String mpName = request.getParameter("mpName");
        String mpProvider = request.getParameter("mpProvider");
        String mpStore = request.getParameter("mpStore");
        configuration = configuration.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
        configuration = configuration.replace("&", "&amp;"); // this is to ensure that url is properly encoded
        OMElement messageProcessorElement = AXIOMUtil.stringToOM(configuration);
        MessageProcessorData processorData = new MessageProcessorData(messageProcessorElement.toString());
    
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("PARAMS:");
        for (String key : processorData.getParams().keySet()) {
            String paramName = key;
            String paramValue = processorData.getParams().get(key);
            if (paramValue != "") {
                stringBuffer.append("|" + paramName + "#" + paramValue);
            }
        }
%>
<input type="hidden" id="tableParams" name="tableParams" value="<%=stringBuffer.toString()%>"/>
<input id="Name" name="Name" type="hidden" value="<%=Encode.forHtml(mpName)%>"/>
<input name="Provider" id="Provider" type="hidden" value="<%=Encode.forHtml(mpProvider)%>"/>
<input name="MessageStore" id="MessageStore" type="hidden" value="<%=Encode.forHtml(mpStore)%>"/>
<input name="TargetEndpoint" id="TargetEndpoint" type="hidden" value="<%=processorData.getTargetEndpoint()%>"/>
<%
    }
%>
