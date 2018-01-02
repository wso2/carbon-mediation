<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.wso2.carbon.message.store.ui.utils.MessageStoreData" %>

<%
    String configuration = request.getParameter("messageStoreString");
    String msName = request.getParameter("msName");
    String msProvider = request.getParameter("msProvider");
    String parameterResequenceIdPath = "store.resequence.id.path";
    configuration = configuration.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
    configuration = configuration.replace("&", "&amp;"); // this is to ensure that url is properly encoded
    OMElement messageStoreElement = AXIOMUtil.stringToOM(configuration);
    MessageStoreData messageStore = new MessageStoreData(messageStoreElement.toString());

    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("PARAMS:");
    for (String key : messageStore.getParams().keySet()) {
        String paramName = key;
        String paramValue = messageStore.getParams().get(key);
        if (paramValue != "") {
            stringBuffer.append("|" + paramName + "#" + paramValue);
        }
    }
%>

    <input type="hidden" id="tableParams" name="tableParams" value="<%=stringBuffer.toString()%>"/>
    <input id="Name" name="Name" type="hidden"
           value="<%=msName%>"/>
    <input name="Provider" id="Provider" type="hidden" value="<%=msProvider%>"/>
