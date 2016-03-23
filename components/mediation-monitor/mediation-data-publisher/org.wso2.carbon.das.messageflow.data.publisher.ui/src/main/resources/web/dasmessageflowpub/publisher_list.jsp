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

	    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
	    ConfigurationContext configContext =
	            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	    DASMessageFlowPublisherAdminClient client = new DASMessageFlowPublisherAdminClient(cookie, backendServerURL, configContext, request.getLocale());
	    PublisherConfig[] publisherNameList = null;
	    
	    boolean collectingEnabled = client.isCollectingEnabled();

	    String action = request.getParameter("action");
		String serverId = request.getParameter("serverId");
		
		if (action != null && action.equals("remove") && serverId != null) {
			try {
		    	client.removeServer(serverId);
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
	    
	
	    try {
	    	publisherNameList = client.getAllPublisherNames();
	    	
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

    %>
	<div id="middle">
        <h2>
            <fmt:message key="das.server.list"/>
        </h2>
        <div id="workArea">
        
        <%
        	if (!collectingEnabled){
        		%>
        		<p  style="color: red;">
        			You need to enable mediation flow statistics in "synapse.properties" file for publishing data.
        		</p>
        		<br/>
        		<%
        	}
        %>
        
        <% if (publisherNameList != null && publisherNameList.length != 0) { %>
        
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                <thead>
                    <tr>
                        <th>
                            <fmt:message key="das.url"/>
                        </th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (PublisherConfig publisherName : publisherNameList) {
                            %>
                                 <tr>
                                     <td>
                                         <%=publisherName.getUrl()%>
                                     </td>
                                     <td>
                                         <span><a onClick='javaScript:removeServerProfile("<%=publisherName.getServerId()%>")' style='background-image:url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Server</a></span>
                                         <span><a onClick='javaScript:editServerProfile("<%=publisherName.getServerId()%>")' style='background-image:url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Server</a></span>
                                     </td>
                                 </tr>
                          <%
                        }
                    %>
                </tbody>
            </table>

           <% 
            } 
           %>
            
        </div>
        
        <span><a onClick='javaScript:addServerProfile()' style='background-image:
                                        url(../admin/images/add.gif);'class='icon-link addIcon'>Add Server</a></span>
	</div> 
		<script type="text/javascript">
	        function removeServerProfile(profileName){
	            window.location.href = "publisher_list.jsp?serverId=" + profileName + "&action=remove";
	        }
	        function editServerProfile(profileName){
	            window.location.href = "configure_publisher.jsp?serverId=" + profileName + "&action=load";
	        }
	        function removeForcefully(profileName){
	            window.location.href = "publisher_list.jsp?serverId=" + profileName + "&action=remove&force=true";
	        }
	        function reloadPage(){
	            window.location.href = "publisher_list.jsp";
	        }
	        function addServerProfile(){
	            window.location.href = "configure_publisher.jsp";
	        }
    	</script>

</fmt:bundle>

