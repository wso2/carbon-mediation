<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.Entry" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyServicePolicyInfo" %>
<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
            request.getLocale());
    ProxyData pd = new ProxyData();

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProxyServiceAdminClient client = new ProxyServiceAdminClient(
            configContext, backendServerURL, cookie, request.getLocale());
    // error object
    String[] tps;
    try {
        // available transports
        tps = client.getAvailableTransports();
    } catch (RemoteException e) {
        CarbonUIMessage.sendCarbonUIMessage(bundle.getString("unable.to.get.metadata.on.transports"),
                CarbonUIMessage.ERROR, request);
        return;
    }

    session.setAttribute("pageNum", new Integer(request.getParameter("pageNum")));

    // iterates through all the transports and see which one is checked by the user
    String selectedTransport;
    ArrayList<String> selectedTransports = new ArrayList<String>();
    if (tps != null && tps.length > 0 && tps[0] != null) {
        for (String transport : tps) {
            if ((selectedTransport = request.getParameter(transport)) != null){
                selectedTransports.add(selectedTransport);
            }
        }
    }
    // if any of the transports is selected then set such transports in the proxy data object
    if (!selectedTransports.isEmpty()) {
        pd.setTransports(selectedTransports.toArray(new String[selectedTransports.size()]));
    }

    // sets proxy service name
    pd.setName(request.getParameter("psName"));

    // sets start on load option
    pd.setStartOnLoad((request.getParameter("startOnLoad") == null));

    // sets service parameters
    String serviceParams = request.getParameter("serviceParams");
    if (serviceParams != null && !"".equals(serviceParams)) {
        String [] params = serviceParams.split("::");
        Entry[] entries = new Entry[params.length];
        String [] pair;
        String param;
        for (int i = 0; i < params.length; i++) {
            param = params[i];
            pair = param.split(",");
            entries[i] = new Entry();
            entries[i].setKey(pair[0]);
            entries[i].setValue(pair[1]);
        }
        pd.setServiceParams(entries);
    }

    // sets wsdl resources
    String publishWsdlCombo = request.getParameter("publishWsdlCombo");
    if (!"None".equals(publishWsdlCombo)) {
        if ("inline".equals(publishWsdlCombo)) {
            String wsdlDef = request.getParameter("wsdlInlineText");
            wsdlDef = wsdlDef.replaceAll("\n|\\r|\\f|\\t", "");
            wsdlDef = wsdlDef.replaceAll("> +<", "><");
            pd.setWsdlDef(wsdlDef);

        } else if ("uri".equals(publishWsdlCombo)) {
            pd.setWsdlURI(request.getParameter("wsdlUriText"));
        } else if ("reg".equals(publishWsdlCombo)) {
            pd.setWsdlKey(request.getParameter("wsdlRegText"));
        } else if ("ep".equals(publishWsdlCombo)) {
            pd.setPublishWSDLEndpoint(request.getParameter("wsdlEPText"));
        }
        String wsdlResources = request.getParameter("wsdlResourceList");
        if (wsdlResources != null && !"".equals(wsdlResources)) {
            String [] resources = wsdlResources.split("::");
            Entry [] entries = new Entry[resources.length];
            String [] pair;
            String param;
            for (int i = 0; i < resources.length; i++) {
                param = resources[i];
                pair = param.split(",");
                entries[i] = new Entry();
                entries[i].setKey(pair[0]);
                entries[i].setValue(pair[1]);
            }
            pd.setWsdlResources(entries);
        }
    }

    // sets pinned servers
    String pinnedServers = request.getParameter("pinnedServers");
    if (pinnedServers != null && !"".equals(pinnedServers)) {
        pd.setPinnedServers(pinnedServers.split(","));
    }

    String serviceGroup = request.getParameter("serviceGroup");
    if (serviceGroup != null && !"".equals(serviceGroup)) {
        pd.setServiceGroup(serviceGroup);
    }

    // sets service description
    String description = request.getParameter("description");
    if (description != null) {
        pd.setDescription(description);
    }

    // sets in sequence information
    String option = request.getParameter("inSeqOp");
    if ("none".equals(option)) {
        pd.setInSeqKey(null);
        pd.setInSeqXML(null);
    } else if ("anon".equals(option)) {
        String anonInXML = (String)session.getAttribute("anonInXML");
        if (anonInXML != null && !"".equals(anonInXML) && !bundle.getString("anon.add").equals(request.getParameter("anonInAction"))) {
            anonInXML = anonInXML.replaceAll("&gt", ">");
            anonInXML = anonInXML.replaceAll("&lt", "<");

            anonInXML = anonInXML.replaceAll(">;", "&gt;");
            anonInXML = anonInXML.replaceAll("<;", "&lt;");

            pd.setInSeqXML(anonInXML);
            pd.setInSeqKey(null);
            // removes session attribute after using it
            session.removeAttribute("anonInXML");
        } else {
            // set both in sequence key and xml to null meaning that no sequence is defined
            pd.setInSeqKey(null);
            pd.setInSeqXML(null);
        }
    } else if ("registry".equals(option)) {
        pd.setInSeqKey(request.getParameter("proxy.in.registry"));
    } else if ("import".equals(option)) {
        String inImportSeq = request.getParameter("inImportSeq");
        if (!"None".equals(inImportSeq)) {
            pd.setInSeqKey(inImportSeq);
            pd.setInSeqXML(null);
        }
    }

    // sets out sequence information
    option = request.getParameter("outSeqOp");
    if ("none".equals(option)) {
        pd.setOutSeqKey(null);
        pd.setOutSeqXML(null);
    } else if ("anon".equals(option)) {
        String anonOutXML = (String)session.getAttribute("anonOutXML");
        if (anonOutXML != null && !"".equals(anonOutXML) && !bundle.getString("anon.add").equals(request.getParameter("anonOutAction"))) {
            anonOutXML = anonOutXML.replaceAll("&gt", ">");
            anonOutXML = anonOutXML.replaceAll("&lt", "<");
            pd.setOutSeqXML(anonOutXML);
            pd.setOutSeqKey(null);
            // removes session attribute after using it
            session.removeAttribute("anonOutXML");
        } else {
            // set both out sequence key and xml to null meaning that no sequence is defined
            pd.setOutSeqKey(null);
            pd.setOutSeqXML(null);
        }
    } else if ("registry".equals(option)) {
        pd.setOutSeqKey(request.getParameter("proxy.out.registry"));
    } else if ("import".equals(option)) {
        String outImportSeq = request.getParameter("outImportSeq");
        if (!"None".equals(outImportSeq)) {
            pd.setOutSeqKey(outImportSeq);
            pd.setOutSeqXML(null);
        }
    }

    // sets fault sequence information
    option = request.getParameter("faultSeqOp");
    if ("none".equals(option)) {
        pd.setFaultSeqKey(null);
        pd.setFaultSeqXML(null);
    } else if ("anon".equals(option)) {
       String anonFaultXML = (String)session.getAttribute("anonFaultXML");
        if (anonFaultXML != null && !"".equals(anonFaultXML) && !bundle.getString("anon.add").equals(request.getParameter("anonFaultAction"))) {
            anonFaultXML = anonFaultXML.replaceAll("&gt", ">");
            anonFaultXML = anonFaultXML.replaceAll("&lt", "<");
            pd.setFaultSeqXML(anonFaultXML);
            pd.setFaultSeqKey(null);
            // removes session attribute after using it
            session.removeAttribute("anonFaultXML");
        } else {
            // set both fault sequence key and xml to null meaning that no sequence is defined
            pd.setFaultSeqKey(null);
            pd.setFaultSeqXML(null);
        }
    } else if ("registry".equals(option)) {
        pd.setFaultSeqKey(request.getParameter("proxy.fault.registry"));
    } else if ("import".equals(option)) {
        String faultImportSeq = request.getParameter("faultImportSeq");
        if (!"None".equals(faultImportSeq)) {
            pd.setFaultSeqKey(faultImportSeq);
            pd.setFaultSeqXML(null);
        }
    }

    // sets endpoint information
    option = request.getParameter("epOp");
    if ("none".equals(option)) {
        pd.setEndpointKey(null);
        pd.setEndpointXML(null);
    } else if ("anon".equals(option)) {
       String anonEpXML = (String)session.getAttribute("anonEpXML");
        if (anonEpXML != null && !"".equals(anonEpXML) && !bundle.getString("anon.add").equals(request.getParameter("anonEpAction"))) {
            pd.setEndpointXML(anonEpXML);
            pd.setEndpointKey(null);
            // removes session attribute after using it
            session.removeAttribute("anonEpXML");
        } else {
            // set both fault sequence key and xml to null meaning that no endpoint is defined
            pd.setEndpointKey(null);
            pd.setEndpointXML(null);
        }
    } else if ("registry".equals(option)) {
        pd.setEndpointKey(request.getParameter("proxy.epr.registry"));
    } else if ("import".equals(option)) {
        String importEp = request.getParameter("importEp");
        if (!"None".equals(importEp)) {
            pd.setEndpointKey(importEp);
            pd.setEndpointXML(null);
        }
    }

    if (Boolean.parseBoolean(request.getParameter("proxy.secured"))) {
        pd.setEnableSecurity(true);
    }

    String policies = request.getParameter("proxy.policies");
    if (policies != null && !"".equals(policies)) {
        String[] keys = policies.split(",");
        ProxyServicePolicyInfo[] policyInfo = new ProxyServicePolicyInfo[keys.length];
        for (int i = 0; i < policyInfo.length; i++) {
            policyInfo[i] = new ProxyServicePolicyInfo();
            policyInfo[i].setKey(keys[i].trim());
        }
        pd.setPolicies(policyInfo);
    }

    String state = request.getParameter("statState");
    pd.setEnableStatistics("on".equals(state));
    state = request.getParameter("traceState");
    pd.setEnableTracing("on".equals(state));

    // sets proxy data object to the request scope
    session.setAttribute("proxy", pd);

    String param = request.getParameter("submit");
    String op, forwardTo = "";
    if (param != null) {
        forwardTo = "submit.jsp?forwardTo=" + request.getParameter("forwardTo") + "&submit=" + param + "&header=" + request.getParameter("header");
    } else if ((param = request.getParameter("return")) != null) {
        if ((op = request.getParameter("sequence")) != null && !"".equals(op)) {
            forwardTo = param + "?sequence=" + op + "&header=" + request.getParameter("header");
        } else if ((op = request.getParameter("anonEpAction")) != null && !"".equals(op)) {
            forwardTo = param + "?anonEpAction=" + op + "&header=" + request.getParameter("header");
        } else if ((op = request.getParameter("header")) != null && !"".equals(op)){
            forwardTo = param + "header=" + op;
        }
    }
    // originator parameter is not required for the source.jsp transistion. Anyway it won't do any harm either :)
    forwardTo += "&originator=designToData.jsp&ordinal=1";
%>
<script type="text/javascript">
    if (window.location.href.indexOf("originator") != -1) {
        window.location.href='<%=forwardTo%>';
    } else {
        window.location.href='index.jsp';
    }
</script>
<%
    return;
%>