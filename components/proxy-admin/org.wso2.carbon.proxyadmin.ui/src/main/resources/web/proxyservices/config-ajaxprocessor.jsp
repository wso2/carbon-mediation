<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="org.apache.axis2.AxisFault" %><%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
            request.getLocale());

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProxyServiceAdminClient client = new ProxyServiceAdminClient(
            configContext, backendServerURL, cookie, request.getLocale());

    String psName = request.getParameter("psName");
    String operation = request.getParameter("operation");
    String returnValue = null;
    try {
        if (operation.equals("redeploy")) {
            returnValue = client.redeployProxyService(psName);
        } else if (operation.equals("enableStat")) {
            returnValue = client.enableStatistics(psName);
        } else if (operation.equals("disableStat")) {
            returnValue = client.disableStatistics(psName);
        } else if (operation.equals("enableTrace")) {
            returnValue = client.enableTracing(psName);
        } else if (operation.equals("disableTrace")) {
            returnValue = client.disableTracing(psName);
        }
    } catch (AxisFault axisFault) {
        returnValue = "failed";
    }
%>
<%=returnValue%>