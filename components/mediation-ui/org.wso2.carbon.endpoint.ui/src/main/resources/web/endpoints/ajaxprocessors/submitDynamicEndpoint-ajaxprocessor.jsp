<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.mediation.templates.ui.EndpointTemplateAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%
    String forwardTo = "index.jsp?tabs=1";
    boolean isError = false;

    String keyName = request.getParameter("regKey");
    String registry = request.getParameter("registry");
    if ("conf".equals(registry)) {
        keyName = "conf:" + request.getParameter("regKey");
    } else if ("gov".equals(registry)) {
        keyName = "gov:" + request.getParameter("regKey");
    }
    boolean updateSynapseReg = false;
    if ("true".equals(request.getParameter("updateSynapseReg"))) {
        updateSynapseReg = true;
    }

    // template
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;
    if (isFromTemplateEditor) {
        forwardTo = "../templates/list_templates.jsp?region=region1&item=templates_menu#tabs-4";
        String epMode = (String) session.getAttribute("epMode");
        if (epMode != null && "anon".equals(epMode)) {
            session.removeAttribute("epMode");
        }
    }

    // isAnonymous
    String endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
        String errMsg = "Unable to save to the Synapse registry in the current mode";
%>
<div>Error:</div><%=errMsg%>
<%
        return;
    }

    if (keyName == null) {
        String errMsg = "Registry key must not be empty";
%>
<div>Error:</div><%=errMsg%>
<%
        return;
    }

    String configuration = (String) session.getAttribute("endpointConfiguration");

    if (endpointMode == null) {
        try {
            if (updateSynapseReg) {
                if (!isFromTemplateEditor) {
                    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext;
                    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    EndpointAdminClient client = new EndpointAdminClient(cookie, serverURL, configContext);

                    // save an existing one
                    client.saveDynamicEndpoint(keyName, configuration);
                } else {
                    //editing a endpoint template
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
                    templateClient.saveDynamicTemplate(keyName, configuration);
                }
            } else {
                //add a new endpoint
                if (!isFromTemplateEditor) {
                    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext;
                    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    EndpointAdminClient client = new EndpointAdminClient(cookie, serverURL, configContext);

                    //add a new endpoint
                    client.addDynamicEndpoint(keyName, configuration);
                } else {
                    //add new endpoint template
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
                    templateClient.addDynamicTemplate(keyName, configuration);
                }
            }
        } catch (Exception e) {
            isError = true;
            String errMsg = e.getMessage();
            errMsg = errMsg.replace("\'", ""); // this is to ensure that error message doesn't have ' or " marking unterminated strings
            errMsg = errMsg.replace("\"", "");
            errMsg = errMsg.replace("\n", "");
%>
<div>Error:</div><%=errMsg%>
<%
            return;
        }
    }
%>
<div>FwdTo:</div><%=forwardTo%>