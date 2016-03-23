<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.das.messageflow.data.publisher.stub.conf.PublisherConfig" %>
<%@ page import="org.wso2.carbon.das.messageflow.data.publisher.stub.conf.Property" %>
<%@ page import="org.wso2.carbon.das.messageflow.data.publisher.ui.DASMessageFlowPublisherAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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
    
    String publishingState = request.getParameter("publishingState");


    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    DASMessageFlowPublisherAdminClient client = new DASMessageFlowPublisherAdminClient(
            cookie, backendServerURL, configContext, request.getLocale());
    PublisherConfig mediationStatConfig = new PublisherConfig();


    if (action != null && action.equals("load") && serverId != null) {
    	// Load existing
    	try {
            mediationStatConfig = client.getEventingConfigData(serverId);
            
            url = mediationStatConfig.getUrl();
            userName = mediationStatConfig.getUserName();
            password = mediationStatConfig.getPassword();
            
            
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

        if (url == "" || userName == "" || password == "") {

        %>
            <script type="text/javascript">
               CARBON.showErrorDialog('Configuration Incomplete!');
            </script>

        <%

        }
        else {

            if (url != null) {
                mediationStatConfig.setUrl(url);
            }
            if (userName != null) {
                mediationStatConfig.setUserName(userName);
            }
            if (password != null) {
                mediationStatConfig.setPassword(password);
            }
            if (publishingState != null) {
                mediationStatConfig.setMessageFlowPublishingEnabled(true);
            }

            if (serverId != null) {
                mediationStatConfig.setServerId(serverId);
            } else {
                serverId = String.valueOf("server_id_" + url.hashCode());
                mediationStatConfig.setServerId(serverId);
            }


            try {
                client.setEventingConfigData(mediationStatConfig);// TODO : temp for testing

            %>
            <script type="text/javascript">

                    CARBON.showInfoDialog("DAS Configuration Successfully Saved!");

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

    function showSaveSuccessful() {}

    function showSavingFailure(){
        CARBON.showErrorDialog('Configuration Incomplete!');
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
            <% if (serverId != null) { %>
            	<input type="hidden" name="serverId" value="<%= mediationStatConfig.getServerId() %>"/>
            <% } %>
            
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                 
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="das.credential"/>
                    </th>
                </tr>
                </thead>

                <tr>
                    <td>
                        <fmt:message key="das.url"/>
                        <span class="required">*</span>
                    </td>
                    <td>
                        <input type="text" id="url" name="url" value="<%= (url != null) ? url : "" %>"/>
                        <input type="button" value="Test Server" onclick="testServer()"/>
                        <i>&nbsp; (eg: tcp://127.0.0.1:7611)</i>
                    </td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="username"/>
                        <span class="required">*</span>
                    </td>
                    <td><input type="text" name="user_name" value="<%= (userName != null) ? userName : "" %>"/></td>
                </tr>
                <tr>
                    <td>
                        <fmt:message key="password"/>
                        <span class="required">*</span>
                    </td>
                    <td><input type="password" name="password" value="<%= (password != null) ? password : "" %>"/></td>
                </tr>
                
                <tr>
                	<td><fmt:message key="publishing"/></td>
                    <td>
	                    <input type="checkbox" name="publishingState" value="publishingEnabled" <%=mediationStatConfig.getMessageFlowPublishingEnabled() ? "checked='checked'" : "" %> />
                    </td>
                </tr>

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

