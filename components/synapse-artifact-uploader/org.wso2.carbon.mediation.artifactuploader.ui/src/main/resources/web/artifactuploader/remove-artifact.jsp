<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediation.artifactuploader.ui.SynapseArtifactUploaderClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    SynapseArtifactUploaderClient client = new SynapseArtifactUploaderClient(
            cookie, serverURL, configContext, request.getLocale());
    client.removeArtifact(request.getParameter("artifactName"));

%>
<script type="text/javascript">
    window.location.href = "index.jsp?ordinal=1";
</script>