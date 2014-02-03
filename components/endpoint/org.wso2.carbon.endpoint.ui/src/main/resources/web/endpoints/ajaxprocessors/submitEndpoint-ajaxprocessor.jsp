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
    boolean isAnonymous = false;
    String forwardTo = "../endpoints/index.jsp?region=region1&item=endpoints_menu&tabs=0";
    boolean isError = false;

    // template
    boolean isFromTemplateEditor = session.getAttribute("endpointTemplate") != null ? true : false;
    boolean isTemplateRegMode = false;
    if (isFromTemplateEditor) {
        forwardTo = "../templates/list_templates.jsp?region=region1&item=templates_menu#tabs-3";
        String epMode = (String) session.getAttribute("epMode");
        isTemplateRegMode = session.getAttribute("templateEdittingMode") != null ? true : false;
        if (epMode != null && "anon".equals(epMode)) {
            session.removeAttribute("epMode");
        }
    }

    // isAnonymous
    String anonymousOriginator = null;
    String endpointMode = (String) session.getAttribute("epMode");
    if (endpointMode != null && "anon".equals(endpointMode)) {
        isAnonymous = true;
        isFromTemplateEditor = false;
        anonymousOriginator = (String) session.getAttribute("anonOriginator");
    }
    String configuration = (String) session.getAttribute("endpointConfiguration");
    String endpointAction = (String) session.getAttribute("action");

    if (endpointMode == null && !isTemplateRegMode) {
        try {
            if (endpointAction != null && endpointAction.equals("edit") && !"".equals(endpointAction)) {
                if (!isFromTemplateEditor) {
                    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext;
                    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    EndpointAdminClient client = new EndpointAdminClient(cookie, serverURL, configContext);
                    // save an existing one
                    client.saveEndpoint(configuration);
                } else {
                    //editing a endpoint template
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);
                    templateClient.saveTemplate(configuration);
                }
            } else {
                if (!isFromTemplateEditor) {
                    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext;
                    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    EndpointAdminClient client = new EndpointAdminClient(cookie, serverURL, configContext);
                    //add a new endpoint
                    client.addEndpoint(configuration);
                } else {
                    //add new endpoint template
                    session.removeAttribute("hasDuplicateTemplateEndpoint");
                    EndpointTemplateAdminClient templateClient = new EndpointTemplateAdminClient(config, session);

                    if (templateClient.hasDuplicateTemplateEndpoint(configuration)) {
                            session.setAttribute("hasDuplicateTemplateEndpoint","true");
                    } else {
                        templateClient.addTemplate(configuration);
                        session.setAttribute("hasDuplicateTemplateEndpoint","false");
                    }
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
    } else if (isAnonymous) {
        // coming through using either send mediator or proxy services by adding an anonymous endpoint
        session.setAttribute("anonEpXML", configuration);
        forwardTo = anonymousOriginator + "?originator=../endpoints/submitEndpoint.jsp";

        if (anonymousOriginator.startsWith("../sequences")) {
            forwardTo = forwardTo + "&region=region1&item=sequences_menu";
        } else if (anonymousOriginator.startsWith("../proxy")) {
            forwardTo = forwardTo + "&region=region1&item=proxy_services_menu";
        }

    } else if (isTemplateRegMode) {
        session.setAttribute("anonEpXML", configuration);
        forwardTo = "dynamicEndpoint.jsp" + "?originator=../endpoints/submitEndpoint.jsp";
    }
    session.removeAttribute("endpointConfiguration");
    session.removeAttribute("action");
%>

<div>FwdTo:</div><%=forwardTo%>

