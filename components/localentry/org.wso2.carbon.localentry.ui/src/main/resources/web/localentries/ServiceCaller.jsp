<%--
 ~ Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.localentry.ui.client.LocalEntryAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.localentry.stub.types.EntryData" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<carbon:jsi18n resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
               request="<%=request%>" />
                               
<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
    private HttpServletRequest req = null;
    private HttpSession ses = null;
    private static final String gt = ">";
    private static final String lt = "<";
%>
<script type="text/javascript">

    function forward() {
        location.href = 'index.jsp?region=region1&item=localentries_menu'
    }

</script>

<%!
   
    

    private String getServiceCallXML() {

    String name = req.getParameter("Name").trim();
    String value = req.getParameter("Value");
    String description = req.getParameter("eventDescription").trim();
    String entryCheck = req.getParameter("pageName");
    String entry = null;
    StringBuilder serviceCallXML = new StringBuilder();

    boolean sourceUrlCheck = false;
    if (entryCheck.equalsIgnoreCase("inlinedText.jsp")) {
        entry = "type=\"0\"";
    } else if (entryCheck.equalsIgnoreCase("inlinedXML.jsp")) {
        entry = "type=\"1\"";
        OMElement elem;
        try {
            // Omiting XML declarations etc. e.g. : <?xml version="1.0"?>
            elem = LocalEntryAdminClient.nonCoalescingStringToOm(value);
            value = elem.toString();
        }
        catch (XMLStreamException e) {
            return "";
        }
    } else {
        entry = "type=\"2\" src=\"" + value + "\"";
        sourceUrlCheck = true;
    }

    serviceCallXML.append("<localEntry key=\"");
    if (sourceUrlCheck) {
        serviceCallXML.append(name).append("\"" + " ").append(entry).append(" xmlns=\"")
                .append(SYNAPSE_NS).append("\">");
    } else {
        serviceCallXML.append(name).append("\"" + " ").append(entry).append(" xmlns=\"")
                .append(SYNAPSE_NS).append("\">").append(value);
    }

        if(description!=null){
           serviceCallXML.append("<description>"+description+"</description>");
        }


        serviceCallXML.append("</localEntry>");

    return serviceCallXML.toString().trim();
}

%>

<%
    String name = request.getParameter("Name");
    if (name == null) { // user has hit this page just after a session timeout.
        // he has to start over as session attributes are lost.
        %>
        <script type="text/javascript">
            document.location.href = '../localentries/index.jsp?region=region1&item=localentries_menu';
        </script>
        <%
        return;
    }
    req = request;
    ses = session;
    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    LocalEntryAdminClient client = new LocalEntryAdminClient(cookie, url, configContext);

    StringBuilder ss = new StringBuilder();
    ss.append(getServiceCallXML());

    // OMElement payload = AXIOMUtil.stringToOM(ss.toString());
    //payload.serialize(new PrintWriter(new PrintWriter(new File("/home/dinuka/Desktop/tt.txt"))));

    boolean entCheck = false;
    EntryData[] data = client.getEntryData();
    if (data != null) {
        for (EntryData da : data) {
            if (da.getName().equalsIgnoreCase(name)) {
                entCheck = true;
                break;
            }
        }
    }

    int error = 0;
    if (entCheck && (((String) session.getAttribute("edit" + name)) != null)) {
        try {
            client.saveEntry(ss.toString());
            session.removeAttribute("edit" + name);
        }
        catch (Exception e) {
            error = 1;
            String msg = e.getMessage();
            String errMsg = msg.replaceAll("\\'", " ");
            //session.setAttribute("locerrMsg",errMsg);
            String pageName = request.getParameter("pageName");
            //  session.removeAttribute("pageName");

%>
<script type="text/javascript">
    //function backtoForm(){

    jQuery(document).ready(function() {
        function gotoPage() {

            history.go(-1);
        }
        CARBON.showErrorDialog(jsi18n["cannot.add.local.entry"] + '<%=errMsg%>', gotoPage);        
    });
    //}


</script>

<%
        return;
    }
} else {
%>
<%
    try {
        client.addEntry(ss.toString());
    }
    catch (Exception e) {
        error = 1;
        String msg = e.getMessage();
        String errMsg = msg.replaceAll("\\'", " ");
        // session.setAttribute("locerrMsg",errMsg);
        String pageName = request.getParameter("pageName");
        //session.removeAttribute("pageName");
%>
<script type="text/javascript">
    // function backtoForm(){
    //  javascript:history.go(-1);
    jQuery(document).ready(function() {
        function gotoPage() {

            history.go(-1);
        }
        CARBON.showErrorDialog(jsi18n["cannot.add.local.entry"] + '<%=errMsg%>', gotoPage);
    });

    //}


</script>
<%
            return;
        }
    }
%>

<%if (error == 0) {%>
<script type="text/javascript">
    forward();
</script>
<%}%>