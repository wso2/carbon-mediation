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
	    DASMessageFlowPublisherAdminClient client = new DASMessageFlowPublisherAdminClient(
	            cookie, backendServerURL, configContext, request.getLocale());
	    MediationStatConfig[] publisherNameList = null;
	
	    try {
	    	publisherNameList = client.getAllPublisherNames(); // TODO: Implement the actual operation
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

        <h2>
            <fmt:message key="das.server.list"/>
        </h2>
        <div id="workArea">
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                <thead>
                    <tr>
                        <th>
                            <fmt:message key="das.server.name"/>
                        </th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        //for (MediationStatConfig publisherName : publisherNameList) {
                            %>
                                 <tr>
                                     <td>
                                         
                                     </td>
                                     <td>
                                         <span><a onClick='javaScript:removeServerProfile("")' style='background-image:url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Server</a></span>
                                         <span><a onClick='javaScript:editServerProfile("")' style='background-image:url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Server</a></span>
                                     </td>
                                 </tr>
                          <%
                      //  }
                    %>
                </tbody>
            </table>
        </div>
        <span><a onClick='javaScript:addServerProfile()' style='background-image:
                                        url(../admin/images/add.gif);'class='icon-link addIcon'>Add Server</a></span>

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

