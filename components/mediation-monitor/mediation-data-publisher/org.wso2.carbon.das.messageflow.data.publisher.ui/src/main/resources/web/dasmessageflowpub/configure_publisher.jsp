<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.das.messageflow.data.publisher.stub.conf.MediationStatConfig" %>
<%@ page import="org.wso2.carbon.das.messageflow.data.publisher.stub.conf.Property" %>
<%@ page import="org.wso2.carbon.das.messageflow.data.publisher.ui.DASMessageFlowPublisherAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%! public static final String PROPERTY_VALUES = "propertyValues";
    public static final String PROPERTY_KEYS = "propertyKeys";
%>
<fmt:bundle basename="org.wso2.carbon.das.messageflow.data.publisher.ui.i18n.Resources">

<carbon:breadcrumb
        label="system.statistics"
        resourceBundle="org.wso2.carbon.das.messageflow.data.publisher.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
	String action = request.getParameter("action");
	String serverId = request.getParameter("serverId");
	
    String url = request.getParameter("url");
    String userName = request.getParameter("user_name");
    String password = request.getParameter("password");
    
    String traceState = request.getParameter("traceState");
    String statsState = request.getParameter("statsState");

    String[] propertyKeys = request.getParameterValues(PROPERTY_KEYS);
    String[] propertyValues = request.getParameterValues(PROPERTY_VALUES);

    List<Property> properties = null;
    if (propertyKeys != null) {
        properties = new ArrayList<Property>();
        for (int i = 0; i < propertyKeys.length; i++) {
            Property property = new Property();
            String propertyKey = propertyKeys[i];
            String propertyValue = propertyValues[i];
            property.setKey(propertyKey);
            property.setValue(propertyValue);
            properties.add(property);
        }
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    DASMessageFlowPublisherAdminClient client = new DASMessageFlowPublisherAdminClient(
            cookie, backendServerURL, configContext, request.getLocale());
    MediationStatConfig mediationStatConfig = new MediationStatConfig();


    if (action != null && action.equals("load") && serverId != null) {
    	// Load existing
    	try {
            mediationStatConfig = client.getEventingConfigData(serverId);
        } catch (Exception e) {
            if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
                response.setStatus(500);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
			    %>
			    <jsp:include page="../admin/error.jsp"/>
			    <%
                }
            }
    	
    }

    if (action != null && action.equals("save")) {

        if (url != null) {
            mediationStatConfig.setUrl(url);
        }
        if (userName != null) {
            mediationStatConfig.setUserName(userName);
        }
        if (password != null) {
            mediationStatConfig.setPassword(password);
        }
        System.out.println("STATE-"+traceState);System.out.println(statsState);System.out.println(userName);
        if (traceState != null) {
        	mediationStatConfig.setMessageFlowTracePublishingEnabled(true);
        }
        if (statsState != null) {
        	mediationStatConfig.setMessageFlowStatsPublishingEnabled(true);
        }

        if (properties != null) {
            mediationStatConfig.setProperties(properties.toArray(new Property[properties.size()]));
        }

        if (serverId != null) {
        	mediationStatConfig.setServerId(serverId);
        } else {
        	serverId = String.valueOf(url.hashCode());
        	mediationStatConfig.setServerId(serverId);
        }


        try {
            client.setEventingConfigData(mediationStatConfig);// TODO : temp for testing

		%>
		<script type="text/javascript">
		    jQuery(document).init(function() {
		        function handleOK() {
		
		        }
		
		        CARBON.showInfoDialog("Eventing Configuration Successfully Updated!", handleOK);
		    });
		</script>
		<%
		} catch (Exception e) {
	    if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
	        response.setStatus(500);
	        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
	        session.setAttribute(CarbonUIMessage.ID, uiMsg);
		%>
		<jsp:include page="../admin/error.jsp"/>
		<%
	        }
	    }
    	
    	
    	
    }

%>


<script id="source" type="text/javascript">
    function showHideDiv(divId) {
        var theDiv = document.getElementById(divId);
        if (theDiv.style.display == "none") {
            theDiv.style.display = "";
        } else {
            theDiv.style.display = "none";
        }
    }
    
    function goBackToServerList() {
    	window.location.href = "publisher_list.jsp";
    }

    var rowNum = 1;

    function addColumn() {
        rowNum++;
        
        var sId = "propertyTable_" + rowNum;
       
        var tableContent = "<tr id=\"" + sId + "\">" +
                           "<td>\n" +
                           "                        <fmt:message key='property.name'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                           "                    </td>\n" +
                           "                    <td>\n" +
                           "                        <fmt:message key='property.value'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\">\n" +
                           "                    </td>" +
                           "<td>\n" +
                           "<a onClick='javaScript:removeColumn(\"" + sId + "\")'" +
                           "style='background-image: url(../daspubsvcstat/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>\n" +
                           "                    </td>" +
                           "</tr>";

        $("#propertyTable").append(tableContent);
    }

    function addMetaData(){
        var sId = "propertyTable_" + rowNum;
        var propertyTable = "<table id=\"propertyTable\" width=\"100%\" class=\"styledLeft\""+
                           " style=\"margin-left: 0px;\"><tr id=\"" + sId + "\">" +
                           "<td>\n" +
                           "                        <fmt:message key='property.name'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                           "                    </td>\n" +
                           "                    <td>\n" +
                           "                        <fmt:message key='property.value'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\">\n" +
                           "                    </td>" +
                           "<td>\n" +
                           "<a onClick='javaScript:removeColumn(\"" + sId + "\")'" +
                           "style='background-image: url(../daspubsvcstat/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>\n" +
                           "                    </td>" +
                           "</tr></table>";

        $("#propertyTablePlaceHolder").append(propertyTable);
    }

    function removeColumn(id) {
        $("#" + id).remove();
    }

    function testServer(){

        var serverUrl = document.getElementById('url').value;
        var serverIp = serverUrl.split("://")[1].split(":")[0];
        var authPort = serverUrl.split("://")[1].split(":")[1];

        if(serverIp == null || authPort == null || serverIp == "" || authPort == ""){
            CARBON.showInfoDialog("Please enter the URL correctly.");
        } else{
            jQuery.ajax({
                            type:"GET",
                            url:"../dasmessageflowpub/test_server_ajaxprocessor.jsp",
                            data:{action:"testServer", ip:serverIp, port:authPort},
                            success:function(data){
                                if(data != null && data != ""){
                                    var result = data.replace(/\n+/g, '');
                                    if(result == "true"){
                                        CARBON.showInfoDialog("Successfully connected to DAS Server");
                                    } else if(result == "false"){
                                        CARBON.showErrorDialog("DAS Server cannot be connected!")
                                    }
                                }
                            }
                        });
        }
    }
</script>

<div id="middle">
    <h2>
        <fmt:message key="das.stat.publisher.config"/>
    </h2>

    <div id="workArea">
        <div id="result"></div>
        <p>&nbsp;</p>

        <form action="configure_publisher.jsp" method="post">
            <input type="hidden" name="action" value="save"/>
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                 
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="das.credential"/>
                    </th>
                </tr>
                </thead>

                <tr>
                    <td><fmt:message key="das.url"/></td>
                    <td>
                        <input type="text" id="url" name="url" value="<%= (url != null) ? url : "" %>"/>
                        <input type="button" value="Test Server" onclick="testServer()"/>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="username"/></td>
                    <td><input type="text" name="user_name" value="<%= (userName != null) ? userName : "" %>"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="password"/></td>
                    <td><input type="password" name="password" value="<%= (password != null) ? password : "" %>"/></td>
                </tr>
                
                <tr>
                	<td><fmt:message key="publishing"/></td>
                    <td>
	                    <input type="checkbox" name="traceState" value="traceEnabled" <%=mediationStatConfig.getMessageFlowTracePublishingEnabled() ? "checked='checked'" : "" %> />
	                    <fmt:message key="publishing.traceData"/>
	                    <input type="checkbox" name="statsState" value="statsEnabled" <%=mediationStatConfig.getMessageFlowStatsPublishingEnabled() ? "checked='checked'" : "" %> />
	                    <fmt:message key="publishing.statsData"/>
                    </td>
                </tr>

                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="properties"/>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td id="propertyTablePlaceHolder" colspan="2">

                        <% if (properties != null) { %>
                        <table id="propertyTable" width="100%" class="styledLeft"
                           style="margin-left: 0px;">
                            <tr>
                                <td colspan="3">
                                    <a onClick='javaScript:addColumn()' style='background-image:
                                    url(../dasmessageflowpub/images/add.gif);' class='icon-link addIcon'>Add Property</a>
                                </td>
                            </tr>
                            <% int i = 1;
                            for (Property property : properties) {

                            %>
                            <tr id="propertyTable_<%=i%>">
                                <td>
                                    <fmt:message key="property.name"/>
                                    <input type="text" name="<%=PROPERTY_KEYS%>"
                                           value="<%=property.getKey()%>">
                                </td>
                                <td>
                                    <fmt:message key="property.value"/>
                                    <input type="text" name="<%=PROPERTY_VALUES%>"
                                           value="<%=property.getValue()%>">
                                </td>

                                <td>
                                    <a onClick='javaScript:removeColumn("propertyTable_<%=i%>")' style='background-image:
                                    url(../dasmessageflowpub/images/delete.gif);' class='icon-link addIcon'>Remove Property</a>
                                </td>


                            </tr>
                            <script type="text/javascript">
                                rowNum++;
                            </script>
                            <% i++;
                            }
                            %>

                        </table>
                        <%
                        } else { %>
                            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                                <tr>
                                    <td colspan="3">
                                    <a onClick='javaScript:addMetaData()'
                                    style='background-image: url(../dasmessageflowpub/images/add.gif);'
                                    class='icon-link addIcon'>Add Property</a>
                                    </td>
                                </tr>
                            </table>

                        <% } %>
                    </td>
                </tr>
                </tbody>

                  


                <tr>
                    <td colspan="4" class="buttonRow">
                        <input type="submit" class="button" value="<fmt:message key="save"/>"
                               id="updateStats"/>&nbsp;&nbsp;
                        <input type="button" class="button" onclick="goBackToServerList()" value="<fmt:message key="close"/>"
                               id="closeStats"/>&nbsp;&nbsp;
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>


</fmt:bundle>

