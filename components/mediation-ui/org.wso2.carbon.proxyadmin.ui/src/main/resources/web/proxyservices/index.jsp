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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.CarbonError" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.MetaData" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.Entry" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyServicePolicyInfo" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyAdminClientUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="inc/utils.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
            request.getLocale());

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ProxyServiceAdminClient client = new ProxyServiceAdminClient(
            configContext, backendServerURL, cookie, request.getLocale());

    String region = request.getParameter("region");
    String item = request.getParameter("item");
    String ordinal = request.getParameter("ordinal");
    Integer pageNum;
    // if we are coming from the menu that means we are creating a new proxy service
    if ("true".equals(request.getParameter("startwiz"))) {
        session.removeAttribute("proxy");
        session.removeAttribute("pageNum");
    }
    boolean switchimmed = "true".equals(request.getParameter("sourceView"));

    /* These four values should be populated from the registry values */
    String faultRegKey = "";
    String inRegKey = "";
    String outRegkey = "";
    String eprRegKey = "";

    ProxyData pd = (ProxyData)session.getAttribute("proxy");
    // error object
    CarbonError carbonError;
    String[] tps, definedEPs, definedSeqs;
    try {
        if (pd == null) {
            String name = request.getParameter("serviceName");
            if (name != null && !"".equals(name)) {
                pd = client.getProxy(name);
            }
        }
        MetaData metaData = client.getMetaData();
        // defined endpoints
        definedEPs = (metaData.getEndpointsAvailable()) ?
                ProxyAdminClientUtils.sortNames(metaData.getEndpoints()) : null;

        // available transports
        tps = (metaData.getTransportsAvailable()) ?
                ProxyAdminClientUtils.sortNames(metaData.getTransports()) : null;
        // defined sequences
        definedSeqs = (metaData.getSequencesAvailable()) ?
                ProxyAdminClientUtils.sortNames(metaData.getSequences()) : null;

    } catch (Exception e) {
        // if proxy attribute is present in the session then remove it
        if (session.getAttribute("proxy") != null) {
            session.removeAttribute("proxy");
        }
        CarbonUIMessage.sendCarbonUIMessage(bundle.getString("unable.to.get.metadata"),
                CarbonUIMessage.ERROR, request);
        %>
        <script type="text/javascript">
            window.location.href = '../service-mgt/index.jsp';
        </script>
        <%
        return;
    }

    boolean nameDisabled = false;
    pageNum = (Integer)session.getAttribute("pageNum");
    if (pageNum == null || pageNum < 0 || pageNum > 2) {
        pageNum = 0;
    }
    session.removeAttribute("pageNum");

    String name = (pd != null) ? pd.getName() : "";
    String securityEnabled = (pd != null && pd.getEnableSecurity()) ? "true" : "false";
    String policyKeys = "";
    if (pd != null && pd.getPolicies() != null) {
        ProxyServicePolicyInfo[] policies = pd.getPolicies();
        if (policies.length > 0 && policies[0] != null) {
            boolean first = true;
            for (ProxyServicePolicyInfo pi : policies) {
                if (!first) {
                    policyKeys +=",";
                }
                policyKeys += pi.getKey().trim();
                first = false;
            }
        }
    }


    String header = request.getParameter("header");
    if (header != null && header.toUpperCase().equals(bundle.getString("header.modify").toUpperCase())) {
        nameDisabled = true;
        /* if this is a modify request we set the pageNum to 0 */
        String fromDesign = request.getParameter("fromdesign");
        if (fromDesign == null || fromDesign.equals("")) {
            pageNum = 0;
        }
    }
    header = (header != null && !"".equals(header)) ? header : bundle.getString("header.add");

    // generates transports check boxes
    String tbody = "";
    String[] proxyTransports = null;
    if (pd != null) {
        proxyTransports = pd.getTransports();
        if (proxyTransports != null && proxyTransports.length > 0 && proxyTransports[0] != null) {
            for (String proxyTransport : proxyTransports) {
                tbody += "<tr><td>" + proxyTransport + "</td><td><input name=\"" + proxyTransport + "\" type=\"checkbox\" value=\"" + proxyTransport + "\" checked/></td></tr>";
            }
        }
    }
    if (tps != null && tps.length > 0) {
        for (String tp : tps) {
            if (proxyTransports != null && proxyTransports.length > 0 && proxyTransports[0] != null) {
                if (tbody.indexOf(("\"" + tp) + "\"") == -1) {
                    tbody += "<tr><td>" + tp + "</td><td><input name=\"" + tp + "\" type=\"checkbox\" value=\"" + tp + "\"unchecked/></td></tr>";
                }
            } else {
                // selects http/https ransports since no transport is specified in the proxy service
                tbody += "<tr><td>" + tp + "</td><td><input name=\"" + tp + "\" type=\"checkbox\" value=\"" + tp + "\"";
                        if(tp.startsWith("http")) {
                            tbody += "checked";
                        }
                tbody += "/></td></tr>";
            }
        }
    }

    // the variables holding which option of sequences or endpoints to be selected
    String whichEP, whichFaultSeq, whichInSeq, whichOutSeq;
    whichEP = whichFaultSeq = whichInSeq = whichOutSeq = "None";

    if (pd != null) {
        if (pd.getEndpointKey() != null && !"".equals(pd.getEndpointKey())) {
            whichEP = "Reg";
        } else if (pd.getEndpointXML() != null && !"".equals(pd.getEndpointXML())) {
            whichEP = "Anon";
        }

        if (pd.getFaultSeqKey() != null && !"".equals(pd.getFaultSeqKey())) {
            whichFaultSeq = "Reg";
        } else if (pd.getFaultSeqXML() != null && !"".equals(pd.getFaultSeqXML())) {
            whichFaultSeq = "Anon";
        }

        if (pd.getOutSeqKey() != null && !"".equals(pd.getOutSeqKey())) {
            whichOutSeq = "Reg";
        } else if (pd.getOutSeqXML() != null && !"".equals(pd.getOutSeqXML())) {
            whichOutSeq = "Anon";
        }

        if (pd.getInSeqKey() != null && !"".equals(pd.getInSeqKey())) {
            whichInSeq = "Reg";
        } else if (pd.getInSeqXML() != null && !"".equals(pd.getInSeqXML())) {
            whichInSeq = "Anon";
        }
    }

    // adds defined endpoints
    String epOptions = "<option name=\"None\" value=\"None\">" + bundle.getString("select.ep.none") + "</option>";
    if (definedEPs != null && definedEPs.length > 0) {
        for (String definedEP : definedEPs) {
            epOptions += "<option name=\"" + definedEP + "\" value=\"" + definedEP + "\">" + definedEP + "</option>";
        }
    }

    // if pd is given and its endpoint is in the list of available endpoints then make it selected
    String key;
    if (pd != null && ((key = pd.getEndpointKey()) != null && !"".equals(key))) {
        if (epOptions.indexOf("\"" + key + "\"") != -1) {
            whichEP = "Imp";
            key = "\"" + key + "\"";
            epOptions = epOptions.replaceFirst(key, key + " selected");
        }
    }

    // if pd is given and its endpoint is in the registry then set the key in eprRegKey text box
    if ("Reg".equals(whichEP)) {
        eprRegKey = pd.getEndpointKey();
    }

    // adds defined sequences
    String seqOptions = "";
    if (definedSeqs != null) {
        for (String definedSeq : definedSeqs) {
            seqOptions += "<option name=\"" + definedSeq + "\" value=\"" + definedSeq + "\">" + definedSeq + "</option>";
        }
    }

    // sets sequences lists of all flows to the defined set of sequences
    String faultSeqOptions, inSeqOptions, outSeqOptions;
    faultSeqOptions = inSeqOptions = outSeqOptions = seqOptions;

    // if pd is given and its fault sequence is in the list of available sequences then make it selected
    if (pd != null && ((key = pd.getFaultSeqKey()) != null && !"".equals(key))) {
        if (faultSeqOptions.indexOf("\"" + key + "\"") != -1) {
            whichFaultSeq = "Imp";
            key = "\"" + key + "\"";
            faultSeqOptions = faultSeqOptions.replaceFirst(key, key + " selected");
        }
    }
    faultSeqOptions = "<option name=\"None\" value=\"None\">" + bundle.getString("select.faultseq.none") + "</option>" + faultSeqOptions;

    // if pd is given and its fault sequence is in the registry then set the key in faultRegKey text box
    if ("Reg".equals(whichFaultSeq)){
        faultRegKey = pd.getFaultSeqKey();
    }

    // if pd is given and its out sequence is in the list of available sequences then make it selected
    if (pd != null && ((key = pd.getOutSeqKey()) != null && !"".equals(key))) {
        if (outSeqOptions.indexOf("\"" + key + "\"") != -1) {
            whichOutSeq = "Imp";
            key = "\"" + key + "\"";
            outSeqOptions = outSeqOptions.replaceFirst(key, key + " selected");
        }
    }
    outSeqOptions = "<option name=\"None\" value=\"None\">" + bundle.getString("select.outseq.none") + "</option>" + outSeqOptions;

    // if pd is given and its out sequence is in the registry then set the key in outRegKey text box
    if ("Reg".equals(whichOutSeq)){
        outRegkey = pd.getOutSeqKey();
    }

    // if pd is given and its in sequence is in the list of available sequences then make it selected
    if (pd != null && ((key = pd.getInSeqKey()) != null && !"".equals(key))) {
        if (inSeqOptions.indexOf("\"" + key + "\"") != -1) {
            whichInSeq = "Imp";
            key = "\"" + key + "\"";
            inSeqOptions = inSeqOptions.replaceFirst(key, key + " selected");
        }
    }
    inSeqOptions = "<option name=\"None\" value=\"None\">" + bundle.getString("select.inseq.none") + "</option>" + inSeqOptions;

    // if pd is given and its in sequence is in the registry then set the key in inRegKey text box
    if ("Reg".equals(whichInSeq)){
        inRegKey = pd.getInSeqKey();
    }

    // sets start on load option
    String startOnLoadBox = "<input type=\"checkbox\" name=\"startOnLoad\"/>";
    // if the given proxy data mentions not to start on load then add a checked attribute
    if (pd != null && !pd.getStartOnLoad()) {
        startOnLoadBox = "<input type=\"checkbox\" checked name=\"startOnLoad\"/>";
    }

    // sets additional service params on load if the given proxy data contains any
    Entry[] entries;
    String givenParams = "";
    if (pd != null && (entries = pd.getServiceParams()) != null && entries.length > 0 && entries[0] != null) {
        givenParams = entries[0].getKey() + "#" + entries[0].getValue().replace("\n","");
        for (int i = 1; i < entries.length; i++) {
            if (entries[i] != null) {
                givenParams += "::" + entries[i].getKey() + "#" + entries[i].getValue().replace("\n","");
            }
        }
    }

	givenParams = givenParams.replaceAll("\\\\", "\\\\\\\\");
	givenParams = givenParams.replaceAll("'","\\\\'");
    givenParams = givenParams.replaceAll(" xmlns=\"http://ws.apache.org/ns/synapse\"","");
	
    // sets pinned servers
    String pinnedServers = "";
    String [] servers;
    if (pd != null && (servers = pd.getPinnedServers()) != null && servers.length > 0 && servers[0] != null) {
        pinnedServers = servers[0];
        for (int i = 1; i < servers.length; i++) {
            pinnedServers += "," + servers[i];
        }
    }

    String description = "";
    if (pd != null && pd.getDescription() != null) {
        description = pd.getDescription();
    }

    String serviceGroup = "";
    if (pd != null && pd.getServiceGroup() != null) {
        serviceGroup = pd.getServiceGroup();
    }

    // sets anonymous endpoint options
    String anonEpAddEdit = bundle.getString("create");
    String anonEpXML;
    if (pd != null && (anonEpXML = pd.getEndpointXML()) != null && !"".equals(anonEpXML)) {
        anonEpAddEdit = bundle.getString("anon.edit");
    } else {
        anonEpXML = "";
    }
    session.setAttribute("anonEpXML", anonEpXML);

    // sets anonymous sequence options
    String anonInAddEdit, anonOutAddEdit, anonFaultAddEdit;
    anonInAddEdit = anonOutAddEdit = anonFaultAddEdit = bundle.getString("create");

    String anonInXML, anonOutXML, anonFaultXML;
    anonInXML = anonOutXML = anonFaultXML = "";
    if (pd != null && (anonInXML = pd.getInSeqXML()) != null && !"".equals(anonInXML)) {
        anonInAddEdit = bundle.getString("anon.edit");
        anonInXML = anonInXML.replaceAll(">", "&gt");
        anonInXML = anonInXML.replaceAll("<", "&lt");
    } else {
        anonInXML = "";
    }
    session.setAttribute("anonInXML", anonInXML);

    if (pd != null && (anonOutXML = pd.getOutSeqXML()) != null && !"".equals(anonOutXML)) {
        anonOutAddEdit = bundle.getString("anon.edit");
        anonOutXML = anonOutXML.replaceAll(">", "&gt");
        anonOutXML = anonOutXML.replaceAll("<", "&lt");
    } else {
        anonOutXML = "";
    }
    session.setAttribute("anonOutXML", anonOutXML);

    if (pd != null && (anonFaultXML = pd.getFaultSeqXML()) != null && !"".equals(anonFaultXML)) {
        anonFaultAddEdit = bundle.getString("anon.edit");
        anonFaultXML = anonFaultXML.replaceAll(">", "&gt");
        anonFaultXML = anonFaultXML.replaceAll("<", "&lt");
    } else {
        anonFaultXML = "";
    }
    session.setAttribute("anonFaultXML", anonFaultXML);

    String tracing, statistics;
    tracing = statistics = "off";
    if (pd != null) {
        statistics = (pd.getEnableStatistics()) ? "on" : "off";
        tracing = (pd.getEnableTracing()) ? "on" : "off";
    }

    String publishWsdl = "none";
    String wsdlText = "";
    if (pd != null) {

        if ((wsdlText = pd.getWsdlDef()) != null && !"".equals(wsdlText)) {
            publishWsdl = "inline";
            // create a one line string with no unnecessary whitespaces
            wsdlText = wsdlText.replaceAll("\n|\r|\\f|\\t", "");
            wsdlText = wsdlText.replaceAll("> +<", "><");
            InputStream xmlIn = new ByteArrayInputStream(wsdlText.getBytes());
            XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
            wsdlText = xmlPrettyPrinter.xmlFormat();
            wsdlText = wsdlText.replaceAll("\n", "<br>");
        } else if ((wsdlText = pd.getWsdlKey()) != null && !"".equals(wsdlText)) {
            publishWsdl = "reg";
        } else if ((wsdlText = pd.getWsdlURI()) != null && !"".equals(wsdlText)) {
            publishWsdl = "uri";
        } else if ((wsdlText = pd.getPublishWSDLEndpoint()) != null && !"".equals(wsdlText)) {
            publishWsdl = "ep";
        }
        if(wsdlText != null){        
            wsdlText = wsdlText.replaceAll("\\\\", "\\\\\\\\");
            wsdlText = wsdlText.replaceAll("'", "&#39;");    
        }
    }

    // sets additional WSDL resources on load if the given proxy data contains any
    Entry[] resources;
    String givenWsdlResources = "";
    if (pd != null && (resources = pd.getWsdlResources()) != null && resources.length > 0 && resources[0] != null) {
        givenWsdlResources = resources[0].getKey() + "," + resources[0].getValue();
        for (int i = 1; i < resources.length; i++) {
            givenWsdlResources += "::" + resources[i].getKey() + "," + resources[i].getValue();
        }
        givenWsdlResources = givenWsdlResources.replaceAll("\\\\", "\\\\\\\\" );
        givenWsdlResources = givenWsdlResources.replaceAll("'","\\\\'");
    }

    String saveOrModify = "add";
    boolean topPage = true;
    if (bundle.getString("header.add").equals(header)) {
        saveOrModify = "add";
        topPage = true;
    } else if (bundle.getString("header.modify").equals(header)) {
        saveOrModify = "modify";
        topPage = false;
    }

    removeSessionAttributes(session);
%>

<%!
    public void removeSessionAttributes(HttpSession session) {
        // if proxy attribute is present in the session then remove it
        if (session.getAttribute("proxy") != null) {
            session.removeAttribute("proxy");
        }
        // if epMode attribute is present in the session then remove it
        if (session.getAttribute("epMode") != null) {
            session.removeAttribute("epMode");
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">
<carbon:breadcrumb
        label="service.proxy.menu.text"
        resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.Resources"
        topPage="<%=topPage%>"
        request="<%=request%>"
        hidden="<%=switchimmed ? true : false%>"/>

<carbon:jsi18n
    resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.JSResources"
    request="<%=request%>"
    i18nObjectName="proxyi18n"
/>
<script type="text/javascript">
    //when the DOM is ready execute the methord
    YAHOO.util.Event.onDOMReady(init);
    var anonEpAction = '<%=anonEpAddEdit%>';
    var anonInAction = '<%=anonInAddEdit%>';
    var anonOutAction = '<%=anonOutAddEdit%>';
    var anonFaultAction = '<%=anonFaultAddEdit%>';
    var pageNum = <%=pageNum.intValue()%>;
    
    function init() {
        
        setSelected('faultSeqOp', '<%=whichFaultSeq%>');
        setSelected('inSeqOp', '<%=whichInSeq%>');
        setSelected('outSeqOp', '<%=whichOutSeq%>');
        setSelected('epOp', '<%=whichEP%>');
        generateServiceParamTable();
        generateWsdlResourceTable();
        hideSeqOps('in');
        hideSeqOps('out');
        hideSeqOps('fault');
        showHideSeqOpsOnLoad();
        hideEpOps();
        showHideEpOpsOnLoad();
        showPageOnLoad();
        showHidePublishWsdlOptionsOnLoad();
        if (<%=switchimmed%>) {
          sourceView();
          return;
        }
    }

    function showHidePublishWsdlOptionsOnLoad() {
        var publishWsdl = '<%=publishWsdl%>';
        if (publishWsdl == 'none') {
            getElement('publishWsdlCombo').selectedIndex = 0;
            showHidePublishWsdlOptions();
        } else if (publishWsdl == 'inline') {
            getElement('publishWsdlCombo').selectedIndex = 1;
            var text = '<%=wsdlText%>';
            text = text.replace(/^<br>/,"");
            text = text.replace(/<br>/g,"\r\n");
            getElement('wsdlInlineText').value = text;
            showHidePublishWsdlOptions();
        } else if (publishWsdl == 'reg') {
            getElement('publishWsdlCombo').selectedIndex = 3;
            getElement('wsdlRegText').value = '<%=wsdlText%>';
            showHidePublishWsdlOptions();
        } else if (publishWsdl == 'uri') {
            getElement('publishWsdlCombo').selectedIndex = 2;
            getElement('wsdlUriText').value = '<%=wsdlText%>';
            showHidePublishWsdlOptions();
        } else if (publishWsdl == 'ep') {
            getElement('publishWsdlCombo').selectedIndex = 4;
            getElement('wsdlEPText').value = '<%=wsdlText%>';
            showHidePublishWsdlOptions();
        }
    }

    function showPageOnLoad() {
        hideElem('saveBtn');
        if (pageNum == 0) {
            hideElem('backBtn');
        } else if (pageNum == 2) {
            hideElem('nextBtn');
            showElem('saveBtn');
            getElement('saveBtn').disabled = false;
        }
        hideElem('page0');
        hideElem('page1');
        hideElem('page2');
        hideElem('step0');
        hideElem('step1');
        hideElem('step2');
        showElem('step' + pageNum);
        showElem('page' + pageNum);
    }

    function showNextPage() {
        var result;
        if ((result = validatePage(pageNum)) != 'successful') {
            CARBON.showErrorDialog(result);
            return;
        }
        if (pageNum <= 1) {
            hideElem('page' + pageNum);
            hideElem('step' + pageNum);
            var n = ++pageNum;
            showElem('page' + n);
            showElem('step' + n);
            showElem('backBtn');
            if (pageNum == 2) {
                hideElem('nextBtn');
            }
        }
        if (pageNum == 2) {
            showElem('saveBtn');
            getElement('saveBtn').disabled = false;
        } else {
            hideElem('saveBtn');
        }
    }

    function validatePage(num) {
        var elem;
        var isOneSpecified = false;
        var proxyNameRegex = /[~!@#$%^&*()\\\/+=\:;<>'"?[\]{}|\s,]|^$/;

        if ((elem = getElement('psName')).value == null || proxyNameRegex.test(elem.value)) {
            return 'Proxy service name is empty or contains invalid characters';
        }
        if (num == 0) {
            var wsdl = getElement('publishWsdlCombo');
            if (wsdl != null) {
                var mode = wsdl[wsdl.selectedIndex].value;
                if (mode == 'uri') {
                    var wsdlUri = getElement('wsdlUriText');
                    if (wsdlUri && wsdlUri.value != "") {
                        var regx = RegExp("((jms|mailto|http|https|ftp|file):/.*)|file:.*");
                        if (!(wsdlUri.value.match(regx))) {
                            return proxyi18n["invalid.wsdl.uri"];
                        }
                    } else if (wsdlUri.value == '') {
                        return proxyi18n["invalid.wsdl.uri"];                        
                    }
                } else if (mode == 'reg') {
                    var wsdlKey = getElement('wsdlRegText');
                    if (wsdlKey && wsdlKey.value == '') {
                        return proxyi18n['wsdl.not.selected'];
                    }
                }
            }
        } else if (num == 1) {

            if (getElement('epOpImp').checked && getElement('importEp').value != 'None') {
                isOneSpecified = true;
            }
            if (getElement('inSeqOpImp').checked && getElement('inImportSeq').value != 'None') {
                isOneSpecified = true;
            }
            if (getElement('inSeqOpAnon').checked ) {
                if (getElement('inAnonAddEdit').innerHTML == '<fmt:message key="create"/>') {
                    return proxyi18n["anonymous.in.sequence.is.not.specified"];
                }
                isOneSpecified = true;
            } else if (getElement('inSeqOpReg').checked) {
                elem = getElement('proxy.in.registry');
                if (elem && elem.value == "") {
                    return proxyi18n["an.in.sequence.is.not.selected.from.the.registry"];
                }
                isOneSpecified = true;
            }

            if (getElement('epOpAnon').checked) {
                if (getElement('epAnonAddEdit').innerHTML == '<fmt:message key="create"/>') {
                    return proxyi18n["anonymous.endpoint.is.not.specified"];
                }
                isOneSpecified = true;
            } else if (getElement('epOpReg').checked) {
                elem = getElement('proxy.epr.registry');
                if (elem && elem.value == "") {
                    return proxyi18n["an.endpoint.is.not.selected.from.the.registry"];
                }
                isOneSpecified = true;
            }
            if (!isOneSpecified) {
                return proxyi18n["a.valid.in.sequence.or.a.valid.endpoint.is.needed.to.create.a.proxy.service"];
            }
        } else if (num == 2) {
            if (getElement('outSeqOpAnon').checked && getElement('outAnonAddEdit').innerHTML == '<fmt:message key="create"/>' ) {
                return proxyi18n["anonymous.out.sequence.is.not.added"];
            } else if (getElement('outSeqOpReg').checked) {
                elem = getElement('proxy.out.registry');
                if (elem && elem.value == "") {
                    return proxyi18n["an.out.sequence.is.not.selected.from.the.registry"];
                }
            }

            if (getElement('faultSeqOpAnon').checked && getElement('faultAnonAddEdit').innerHTML == '<fmt:message key="create"/>' ) {
                return proxyi18n["anonymous.fault.sequence.is.not.added"];
            }  else if (getElement('faultSeqOpReg').checked) {
                elem = getElement('proxy.fault.registry');
                if (elem && elem.value == "") {
                    return proxyi18n["a.fault.sequence.is.not.selected.from.the.registry"];
                }
            }
        }
        return 'successful';
    }

    function showBackPage() {
        hideElem('saveBtn');
        getElement('saveBtn').disabled = true;
        if (pageNum > 0) {
            hideElem('page' + pageNum);
            hideElem('step' + pageNum);
            var n = --pageNum;
            showElem('page' + n);
            showElem('step' + n);
            showElem('nextBtn');
            if  (pageNum == 0) {
                hideElem('backBtn');
            }
        }

    }

    function setSelected(type, option) {
        var element;
        element = getElement(type + option);
        element.setAttribute('checked', 'checked');
    }

    function saveData() {
        var result;
        if ((result = validatePage(2)) == 'successful') {
            populateServiceParams();
            populateWsdlResources();
            document.designForm.action = "designToData.jsp?submit=<%=saveOrModify%>&anonEpAction=" + anonEpAction + "&header=<%=Encode.forJavaScriptBlock(header)%>&forwardTo=../service-mgt/index.jsp&pageNum=" + pageNum + "&originator=index.jsp&anonInAction" + anonInAction + "&anonOutAction=" + anonOutAction + "&anonFaultAction=" + anonFaultAction;
            document.designForm.submit();
        } else {
            CARBON.showErrorDialog(result);
        }
    }

    function cancelData() {
        window.location.href="../service-mgt/index.jsp";
    }

    function sourceView() {
        populateServiceParams();
        populateWsdlResources();
        document.designForm.action = "designToData.jsp?return=source.jsp&header=<%=Encode.forJavaScriptBlock(header)%>&anonEpAction=" + anonEpAction + "&pageNum=" + pageNum + "&originator=index.jsp&anonInAction" + anonInAction + "&anonOutAction=" + anonOutAction + "&anonFaultAction=" + anonFaultAction;
        document.designForm.submit();
    }

    function populateWsdlResources() {
        var i;
        var str = '';
        if (wsdlResourcesCount > 0) {
            str += wsdlResources[0]['location'] + ',' + wsdlResources[0]['key'];
            for (i = 1; i < wsdlResources.length; i++) {
                str += '::' + wsdlResources[i]['location'] + ',' + wsdlResources[i]['key'];
            }
        }
        document.designForm.wsdlResourceList.value = str;

    }

    function generateWsdlResourceTable() {
        var str = '<%=givenWsdlResources%>';
        if (str != '') {
            var params;
            params = str.split("::");
            var i, param;
            for (i = 0; i < params.length; i++) {
                param = params[i].split(",");
                addWsdlResourceRow(param[0], param[1]);
            }
        }
    }

    // sets the values of the headerTable as the value of serviceParams (i.e. comma separated list of name:value pairs)
    function populateServiceParams() {
        var i;
        var str = '';
        var headerTable = document.getElementById("headerTable");
        for(var j= 1;j<headerTable.rows.length;j++){
            var parmName = headerTable.rows[j].getElementsByTagName("input")[0].value;
            var parmValue = headerTable.rows[j].getElementsByTagName("input")[1].value;
            if(parmName == "" || parmValue == ""){
                return;
            }
            if (j == 1) {
                str += parmName + '#' + parmValue;
            }else{
                str += '::' + parmName + '#' + parmValue;
            }
        }

        document.designForm.serviceParams.value = str;
    }

    function generateServiceParamTable() {
        var str = "<%=givenParams%>";
        if (str != "") {
            var params;
            params = str.split("::");
            var i, param;
            for (i = 0; i < params.length; i++) {
                param = params[i].split("#");
                addServiceParamRow(param[0], param[1]);
            }
        }
    }

    function  anonEpAddEdit() {
        populateServiceParams();
        populateWsdlResources();
        document.designForm.action = "designToData.jsp?return=anonEpHandler.jsp&header=<%=Encode.forJavaScriptBlock(header)%>&anonEpAction=" + anonEpAction + "&pageNum=" + pageNum + "&originator=index.jsp&anonInAction" + anonInAction + "&anonOutAction=" + anonOutAction + "&anonFaultAction=" + anonFaultAction;
        document.designForm.submit();
    }

    function anonEpClear() {
        anonEpAction = "<fmt:message key="create"/>";
        hideElem('epAnonClear');
        getElement('epAnonAddEdit').innerHTML = anonEpAction;
        getElement('epAnonAddEdit').style.backgroundImage="url(../admin/images/add.gif)";
    }

    function anonSeqClear(sequence) {
        if (sequence == 'in') {
            anonInAction = "<fmt:message key="create"/>";
            getElement('inAnonAddEdit').innerHTML = anonInAction;
        } else if (sequence == 'out') {
            anonOutAction = "<fmt:message key="create"/>";
            getElement('outAnonAddEdit').innerHTML = anonOutAction;
        } else if (sequence == 'fault') {
            anonFaultAction = "<fmt:message key="create"/>";
            getElement('faultAnonAddEdit').innerHTML = anonFaultAction;
        }
        hideElem(sequence + 'AnonClear');
        getElement(sequence + 'AnonAddEdit').style.backgroundImage="url(../admin/images/add.gif);";
    }

    function hideOtherEpOp(other) {
        hideElem(other);
        if (other == 'importEp') {
            showElem('epAnonAddEdit');
            if (anonEpAction == 'Edit') {
                showElem('epAnonClear');
            } else if (anonEpAction == 'Add') {
                hideElem('epAnonClear');
            }
        } else if (other == 'epAnonAddEdit') {
            hideElem('epAnonClear');
            showElem('importEp');
        }
        hideElem('eprRegistry');
    }

    function hideEpOps() {
        hideElem('epAnonAddEdit');
        hideElem('epAnonClear');
        hideElem('importEp');
        hideElem('eprRegistry');
    }

    function showHideEpOpsOnLoad() {
        if ('<%=whichEP%>' == 'Anon') {
            hideOtherEpOp('importEp');
        } else if ('<%=whichEP%>' == 'Imp') {
            hideOtherEpOp('epAnonAddEdit');
        } else if ('<%=whichEP%>' == 'Reg') {
            hideElem('importEp');
            hideElem('epAnonAddEdit');
            hideElem('epAnonClear');
            showElem('eprRegistry');
        }
    }

    function anonSeqAddEdit(sequence) {
        populateServiceParams();
        populateWsdlResources();
        document.designForm.action = "designToData.jsp?return=anonSequenceHandler.jsp&anonEpAction=" + anonEpAction + "&header=<%=Encode.forJavaScriptBlock(header)%>&pageNum=" + pageNum + "&originator=index.jsp&sequence=" + sequence + "&anonInAction" + anonInAction + "&anonOutAction=" + anonOutAction + "&anonFaultAction=" + anonFaultAction;
        document.designForm.submit();
    }

    function hideSeqOps(sequence) {
        hideElem(sequence + 'ImportSeq');
        hideElem(sequence + 'AnonAddEdit');
        hideElem(sequence + 'AnonClear');
        hideElem(sequence + 'Registry');
    }

    function hideOtherSeqOp(sequence, other) {
        hideElem(sequence + other);
        if (other == "ImportSeq") {
            showElem(sequence + 'AnonAddEdit');
        } else if (other == "AnonAddEdit") {
            showElem(sequence + 'ImportSeq');
        }
    }

    function showHideSeqOpsOnLoad() {
        if ('<%=whichInSeq%>' == 'Anon') {
            showElem('inAnonAddEdit');
            if (anonInAction == '<fmt:message key="anon.edit"/>') {
                showElem('inAnonClear');
            } else {
                hideElem('inAnonClear');
            }
        } else if ('<%=whichInSeq%>' == "Imp") {
            showElem('inImportSeq');
        } else if ('<%=whichInSeq%>' == "Reg") {
            showElem('inRegistry');
        }

        if ('<%=whichOutSeq%>' == 'Anon') {
            showElem('outAnonAddEdit');
            if (anonOutAction == '<fmt:message key="anon.edit"/>') {
                showElem('outAnonClear');
            } else {
                hideElem('outAnonClear');
            }
        } else if ('<%=whichOutSeq%>' == "Imp") {
            showElem('outImportSeq');
        } else if ('<%=whichOutSeq%>' == "Reg") {
            showElem('outRegistry');
        }

        if ('<%=whichFaultSeq%>' == 'Anon') {
            showElem('faultAnonAddEdit');
            if (anonFaultAction == '<fmt:message key="anon.edit"/>') {
                showElem('faultAnonClear');
            } else {
                hideElem('faultAnonClear');
            }
        } else if ('<%=whichFaultSeq%>' == "Imp") {
            showElem('faultImportSeq');
        } else if ('<%=whichFaultSeq%>' == "Reg") {
            showElem('faultRegistry');
        }
    }

    function getElement(id) {
        return document.getElementById(id);
    }

    function validate() {
        var result;
        if ((result = validatePage(2)) != 'successful') {
            pageNum = 2;
            return result;
        }
        return 'successful';
    }
    
    function addServiceParams() {
        //check to see if there are empty fields left
        var theTable = document.getElementById('headerTable');
        var inputs = theTable.getElementsByTagName('input');
        for(var i=0; i<inputs.length; i++){
            if(inputs[i].value == ""){
                CARBON.showErrorDialog("<fmt:message key="empty.key.or.value"/>");
                return;
            }
        }
        addServiceParamRow("", "");
        if(document.getElementById('headerTable').style.display == "none"){
            document.getElementById('headerTable').style.display = "";
        }
       /* var headerName = document.getElementById('headerName').value;
        var headerValue = document.getElementById('headerValue').value;
        // trim the input values
        headerName = headerName.replace(/^\s*//*, "").replace(/\s*$/, "");
        headerValue = headerValue.replace(/^\s*//*, "").replace(/\s*$/, "");
        if (headerName != '' && headerValue != '') {
            if (isParamAlreadyExist(headerName)) {
                CARBON.showWarningDialog("<fmt:message key="parameter.already.exists"/>");
                return;
            }
            addServiceParamRow(headerName, headerValue);
        } else {
            CARBON.showWarningDialog("<fmt:message key="empty.key.or.value"/>");
        }*/
    }

    function deleteServiceParamRow(index) {
        CARBON.showConfirmationDialog("<fmt:message key="confirm.parameter.deletion"/>" , function() {
            document.getElementById('headerTable').deleteRow(index);
            if (document.getElementById('headerTable').rows.length == 1) {
                document.getElementById('headerTable').style.display = 'none';
            }
        });
    }

    function deleteWsdlResourceRow(index) {
        CARBON.showConfirmationDialog("<fmt:message key="confirm.wsdlresource.deletion"/>" , function() {
            document.getElementById('wsdlResourceTable').deleteRow(index);
            wsdlResources.splice(index-1, 1);
            wsdlResourcesCount--;
            if (wsdlResourcesCount == 0) {
                document.getElementById('wsdlResourceTable').style.display = 'none';
            }
        });
    }

    function addWsdlResources() {
        // todo handle validation before adding resource, i.e. see if the above textbox or textarea is empty
        var location = getElement('locationText').value;
        var key = getElement('wsdl.resource.key').value;
        // trim the input values
        location = location.replace(/^\s*/, "").replace(/\s*$/, "");
        key = key.replace(/^\s*/, "").replace(/\s*$/, "");
        if (location != '' && key != '') {
            if (isWsdlResourceAlreadyExists(location)) {
                CARBON.showWarningDialog("<fmt:message key="resource.already.exists"/>");
                return;
            }
            addWsdlResourceRow(location, key);
        } else {
            CARBON.showWarningDialog("<fmt:message key="empty.location.or.key"/>");
        }
    }

    var wsdlResources = Array();
    var wsdlResourcesCount = 0;

    /**
     * Adds a new row to the <code>wsdlResourceTable</code>.
     * @param location
     * @param key
     */
    function addWsdlResourceRow(location, key) {
        addRow(location, key, 'wsdlResourceTable', 'deleteWsdlResourceRow');

        wsdlResources[wsdlResourcesCount] = new Array(2);
        wsdlResources[wsdlResourcesCount]['location'] = location;
        wsdlResources[wsdlResourcesCount]['key'] = key;

        wsdlResourcesCount++;

        document.getElementById('locationText').value="";
        document.getElementById('wsdl.resource.key').value="";
    }

    function isWsdlResourceAlreadyExists(location) {
        var i;
        for (i = 0; i < wsdlResourcesCount; i++) {
            if (wsdlResources[i]['location'] == location) {
                return true;
            }
        }
        return false;
    }

    function showHidePublishWsdlOptions() {
        var index;
        if ((index = document.getElementById('publishWsdlCombo').selectedIndex) == 0) {
            hideElem('wsdlInline');
            hideElem('wsdlUri');
            hideElem('wsdlReg');
            hideElem('wsdlEP');
            hideElem('wsdlResourceTr');
        } else if (index == 1) {
            showElem('wsdlInline');
            hideElem('wsdlUri');
            hideElem('wsdlReg');
            hideElem('wsdlEP');
            showElem('wsdlResourceTr');
        } else if (index == 2) {
            hideElem('wsdlInline');
            showElem('wsdlUri');
            hideElem('wsdlReg');
            hideElem('wsdlEP');
            showElem('wsdlResourceTr');
        } else if (index == 3) {
            hideElem('wsdlInline');
            hideElem('wsdlUri');
            hideElem('wsdlEP');
            showElem('wsdlReg');
            showElem('wsdlResourceTr');
        } else if (index == 4) {
            hideElem('wsdlInline');
            hideElem('wsdlUri');
            hideElem('wsdlReg');
            showElem('wsdlEP');
            hideElem('wsdlResourceTr');
        }
    }

    function radioClicked(seqence, name) {
        if (name != null) {
            showElem(seqence + name);
            if (name == "Registry") {
                hideElem(seqence + "ImportSeq");
                hideElem(seqence + "AnonAddEdit");
                hideElem(seqence + "AnonClear");
            } else if (name == "ImportSeq") {
                hideElem(seqence + "Registry");
                hideElem(seqence + "AnonAddEdit");
                hideElem(seqence + "AnonClear");
            } else if (name == "AnonAddEdit") {
                hideElem(seqence + "Registry");
                hideElem(seqence + "ImportSeq");
                if (seqence == 'in' && anonInAction == '<fmt:message key="anon.edit"/>') {
                    showElem('inAnonClear');
                } else if (seqence == 'out' && anonOutAction == '<fmt:message key="anon.edit"/>') {
                    showElem('outAnonClear');
                } else if (seqence == 'fault' && anonFaultAction == '<fmt:message key="anon.edit"/>') {
                    showElem('faultAnonClear');
                }
            }
        } else {
            hideElem(seqence + "Registry");
            hideElem(seqence + "ImportSeq");
            hideElem(seqence + "AnonAddEdit");
            hideElem(seqence + "AnonClear");
        }
    }

    function eprRadioClicked(name) {
        if (name == "importEp") {
            showElem("importEp");
            hideElem("eprRegistry");
            hideElem("epAnonAddEdit");
            hideElem('epAnonClear');
        } else if (name == "eprRegistry") {
            showElem("eprRegistry");
            hideElem("epAnonAddEdit");
            hideElem('epAnonClear');
            hideElem("importEp");
        } else if (name == "epAnonAddEdit") {
            showElem("epAnonAddEdit");
            if (anonEpAction == '<fmt:message key="anon.edit"/>') {
                showElem('epAnonClear');
            }
            hideElem("eprRegistry");
            hideElem("importEp");
        } else if (name == "epOpNone") {
            hideElem("epAnonAddEdit");
            hideElem('epAnonClear');
            hideElem("eprRegistry");
            hideElem("importEp");
        }
    }

    function testWsdlUri() {
        var inputBox = document.getElementById('wsdlUriText');
        var wsdlUri = inputBox.value;
        if (wsdlUri == '') {
            CARBON.showWarningDialog(proxyi18n["invalid.wsdl.uri"]);
        } else {
            jQuery.get("testConnection-ajaxprocessor.jsp", {'url' : wsdlUri},
                    function(data,status) {
                       if (data.replace(/^\s+|\s+$/g, '') != 'success') {
                           CARBON.showErrorDialog(proxyi18n["invalid.wsdl.uri2"]);
                       } else {
                           CARBON.showInfoDialog(proxyi18n["wsdl.uri.ok"]);
                       }
                    });
        }
    }
    function changePSN(){
        var proxyServiceName1 = document.getElementById("proxyServiceName1");
        var proxyServiceName2 = document.getElementById("proxyServiceName2");
        var psName = document.getElementById("psName").value;
        proxyServiceName1.innerHTML = psName;
        proxyServiceName2.innerHTML = psName;

    }
</script>

<div id="middle"  <%=switchimmed ? "style=visibility: hidden" : ""%>>
<h2><%=Encode.forHtmlContent(header)%> Proxy Service</h2>

<div id="workArea">
<link type="text/css" rel="stylesheet" href="../proxyservices/css/proxyservices.css"/>
<script type="text/javascript" src="../proxyservices/js/proxyservices.js"></script>
<form id="form1" name="designForm" method="post" action="">
<table cellspacing="0" class="styledLeft">
<thead>
    <tr>
        <th colspan="2">
            <span style="float: left; position: relative; margin-top: 2px;"><fmt:message key="design"/></span><a style="background-image: url(images/source-view.gif);" class="icon-link" onclick="sourceView()" href="#"><fmt:message key="switch.to.source.view"/></a>
        </th>
    </tr>
</thead>
<tbody>
<tr>
<td>
<table width="100%" class="normal">
<tr>
    <td colspan="3">
        <div id="step0">
            <h2><fmt:message key="proxy.service.page0"/></h2>
            <table class="normal">
                <tbody>
                <tr>
                <td class="leftCol-small"> <% if (nameDisabled) { %><strong><fmt:message key="proxy.service.name"/>:</strong><% } else { %><fmt:message key="proxy.service.name"/><span class="required">*</span><% } %> 
                </td>
                <td align="left">
                    <% if (!nameDisabled) { %>
                    <input id="psName" name="psName" type="text" value="<%=name%>" onchange="changePSN()"  onkeypress="return validateProxyNameText(event)"/>
                    <% } else { %>
                        <strong><%=name%></strong>
                    <% } %>
                </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div id="step1">
            <h2><fmt:message key="proxy.service.page1"/></h2>
            <strong><fmt:message key="proxy.service.name"/>: <span id="proxyServiceName1"><%=Encode.forHtmlContent(name)%></span></strong>
            <p>
                <fmt:message key="proxy.service.page1.desc"/>
            </p>
        </div>
        <div id="step2">
            <h2><fmt:message key="proxy.service.page2"/></h2>
            <strong><fmt:message key="proxy.service.name"/>: <span id="proxyServiceName2"><%=Encode.forHtmlContent(name)%></span></strong>
            <p>
                <fmt:message key="proxy.service.page2.desc"/>
            </p>
        </div>
        <input id="psName" name="psName" type="hidden" value="<%=Encode.forHtmlAttribute(name)%>">
        <input name="proxy.secured" type="hidden" value="<%=securityEnabled%>"/>
        <input name="proxy.policies" type="hidden" value="<%=policyKeys%>" />
    </td>
</tr>
<tr>
<td>
<%-- trace and statistics states are kept here in the hidden fields --%>
<div>
    <input type="hidden" name="statState" value="<%=statistics%>">
    <input type="hidden" name="traceState" value="<%=tracing%>">
</div>
<div id="page0">
    <table class="styledInner" cellspacing="0">
        <thead>
            <tr>
                <th><fmt:message key="general.settings"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td style="padding: 0px !important;">
                    <div id="generalContent">
                        <table cellpadding="0" cellspacing="0" class="styledInner" width="100%" style="margin-left:0px;">
                            <tr>
				<td colspan="3">
				<table class="normal-nopadding">
				<tr>
                                <td width="20%"><fmt:message key="publishing.wsdl"/></td>
                                <td >
                                    <select id="publishWsdlCombo" name="publishWsdlCombo" onchange="showHidePublishWsdlOptions();">
                                        <option id="publishWsdlNone" selected="selected" value="None"><fmt:message key="select.inseq.none"/></option>
                                        <option id="publishWsdlInline" value="inline"><fmt:message key="specify.in.line"/></option>
                                        <option id="publishWsdlUri" value="uri"><fmt:message key="specify.source.url"/></option>
                                        <option id="publishWsdlReg" value="reg"><fmt:message key="pick.from.registry"/></option>
                                        <option id="publishWsdlEP" value="ep"><fmt:message key="pick.from.endpoint"/></option>
                                    </select>
                                </td>

                            </tr>
                            <tr id="wsdlInline">
                                <td></td>
                                <td ><fmt:message key="wsdl.inline"/><br/><br/>
                                    <textarea name="wsdlInlineText" id="wsdlInlineText" rows="20" style="width: 99%;"></textarea>
                                </td>
                            </tr>
                            <tr id="wsdlUri">
                                <td></td>
                                <td >
				<fmt:message key="wsdl.uri"/><br/><br/>
                                    <input type="text" name="wsdlUriText" id="wsdlUriText">
                                    <input type="button" class="button" onclick="testWsdlUri()" value="Test URI"/>
                                </td>
                            </tr>
                            <tr id="wsdlReg">
                                <td><fmt:message key="wsdl.refkey"/></td>
				<td >
				<table cellspacing="0"><tr><td class="nopadding">
                                    <input type="text" name="wsdlRegText"
                                           id="wsdlRegText" value=""
                                           readonly="readonly"/>
				</td>
                <td class="nopadding">
                    <a href="#" class="registry-picker-icon-link"
                       style="padding-left:30px"
                       onclick="showRegistryBrowser('wsdlRegText','/_system/config');"><fmt:message
                            key="conf.registry"/></a>
				</td>
                <td class="nopadding">
                    <a href="#" class="registry-picker-icon-link"
                       style="padding-left:30px"
                       onclick="showRegistryBrowser('wsdlRegText','/_system/governance');"><fmt:message
                            key="gov.registry"/></a>
				</td>
				</tr>
				</table>
                                </td>
                            </tr>
                    <tr id="wsdlEP" style="display:none;">
                        <td><fmt:message key="wsdl.epkey"/></td>
                        <td >
                            <table cellspacing="0">
                                <tr>
                                    <td class="nopadding">
                                        <input type="text" name="wsdlEPText"
                                               id="wsdlEPText" value="" size="40"
                                               readonly="readonly"/>
                                    </td>
                                    <td>
                                        <a href="#" class="registry-picker-icon-link"
                                           style="padding-left:30px" id="confRegBrowserLink1"
                                           onclick="showRegistryBrowser('wsdlEPText','/_system/config');"><fmt:message
                                                key="conf.registry"/></a>
                                    </td>
                                    <td>
                                        <a href="#" class="registry-picker-icon-link"
                                           style="padding-left:30px" id="govRegBrowserLink1"
                                           onclick="showRegistryBrowser('wsdlEPText','/_system/governance');"><fmt:message
                                                key="gov.registry"/></a>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                            <tr id="wsdlResourceTr">

                                <%--todo fix resource table starts here--%>
                                <td style="vertical-align:top!important">
                                    
                                </td>
                                <td >
                                    <input type="hidden" id="wsdlResourceList" name="wsdlResourceList">
                                    <fmt:message key="wsdl.resource"/>
                                    <br/><br/>
                                    <table class="styledInner">
                                        <tr>
                                            <td style="border: solid 1px #ccc !important;">
                                                <div id="wsdlResourceAdd">
                                                    <table class="normal-nopadding" cellspacing="0">
                                                        <tr>
                                                            <td class="nopadding">
                                                                <table>
                                                                    <tr>
                                                                        <td class="nopadding">
                                                                            <fmt:message key="wsdl.resource.location"/>
                                                                            <input type="text" id="locationText"/></td>
                                                                        <td class="nopadding">

                                                                            <fmt:message key="wsdl.resource.key"/>
                                                                            <input type="text" readonly="readonly"
                                                                                   value="" id="wsdl.resource.key"
                                                                                   name="wsdl.resource.key"/>
                                                                        </td>
                                                                        <td class="nopadding"
                                                                            style="padding-top: 10px !important">
                                                                            <a href="#"
                                                                               class="registry-picker-icon-link"
                                                                               onclick="showRegistryBrowser('wsdl.resource.key','/_system/config');"><fmt:message
                                                                                    key="conf.registry"/></a>
                                                                        </td>
                                                                        <td class="nopadding"
                                                                            style="padding-top: 10px !important">
                                                                            <a href="#"
                                                                               class="registry-picker-icon-link"
                                                                               onclick="showRegistryBrowser('wsdl.resource.key','/_system/governance');"><fmt:message
                                                                                    key="gov.registry"/></a>
                                                                        </td>

                                                                    </tr>
                                                                </table>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="nopadding">
                                                                <a class="icon-link"
                                                                   href="#addNameLink"
                                                                   onclick="addWsdlResources();"
                                                                   style="background-image: url(../admin/images/add.gif);"><fmt:message
                                                                        key="wsdl.resource.add"/>
                                                                </a>
                                                            </td>
                                                        </tr>
                                                    </table>

                                                </div>
                                                <div>
                                                    <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
                                                           id="wsdlResourceTable"
                                                           style="display:none;">
                                                        <thead>
                                                        <tr>
                                                            <th style="width:40%"><fmt:message key="location"/></th>
                                                            <th style="width:40%"><fmt:message key="key"/></th>
                                                            <th style="width:20%"><fmt:message key="param.action"/></th>
                                                        </tr>
                                                        </thead>
                                                        <tbody></tbody>
                                                    </table>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                                        <%-- todo fix resource table ends here--%>
                            </tr>
                </table>
                </td>
                            </tr>
                            <tr>
                            <td colspan="3">
                                <table class="normal-nopadding">
                                <tr>
                                <td width="20%">
                                    <fmt:message key="service.parameters"/>
                                </td>
                                <td >
                                    <div id="nameValueAdd">
                                        <a class="icon-link"
                                                   href="#addNameLink"
                                                   onclick="addServiceParams();"
                                                   style="background-image: url(../admin/images/add.gif);"><fmt:message
                                                        key="service.parameters.add"/></a>
                                        <div style="clear:both;"></div>
                                    </div>
                                    <div>
                                        <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
                                               id="headerTable"
                                               style="display:none;">
                                            <thead>
                                                <tr>
                                                    <th style="width:40%"><fmt:message key="param.name"/></th>
                                                    <th style="width:40%"><fmt:message key="param.value"/></th>
                                                    <th style="width:20%"><fmt:message key="param.action"/></th>
                                                </tr>
                                            </thead>
                                            <tbody></tbody>
                                        </table>
                                    </div>
                                </td>
                                </tr>
                                </table>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <table class="normal-nopadding">
                                        <tr>
                                            <td width="20%"><fmt:message key="service.group"/></td>
                                            <td><input id="serviceGroup" name="serviceGroup" type="text"
                                                       value="<%=serviceGroup%>"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <table class="normal-nopadding">
                                        <tr>
                                            <td width="20%">
                                                <fmt:message key="dont.load.service.on.startup"/></td>
                                            <td><%=startOnLoadBox%>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <table class="normal-nopadding">
                                        <tr>
                                            <td width="20%"><fmt:message
                                                    key="pinned.servers.separated.by.comma.or.space"/></td>
                                            <td><input id="pnnedServers" name="pinnedServers" type="text"
                                                       value="<%=pinnedServers%>"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <table class="normal-nopadding">
                                        <tr>
                                            <td width="20%"><fmt:message
                                                    key="description"/></td>
                                            <td><input id="description" name="description" type="text"
                                                       value="<%=description%>"/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
    <br/>
    <table width="100%" class="styledInner" cellpadding="0" cellspacing="0" style="margin-left: 0px;">
        <thead>
            <tr>
                <th><fmt:message key="transport.settings"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td style="padding: 0px !important;">
                    <div id="transportContent">
                        <table id="transportsTable" cellpadding="0" cellspacing="0" border="0" class="styledInner" style="margin-left: 0px;">
			<tr><td><table class="normal-nopadding" style="width: auto">
                            <%=tbody%>
			</table>
			</td>
			</tr>
                        </table>
                       
                    </div>
                    <input type="hidden" name="serviceParams"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
<div id="page1">
    <table width="100%" class="styledInner">
        <thead>
            <tr>
                <th><fmt:message key="in.sequence.options"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <!-- in sequence -->
                    <div id="inSeq">
                        <div id="inSeqDesign">
                            <table id="inSeqOptionTable" class="normal">
                                <tr>
                                    <td class="nopadding">
                                        <input id="inSeqOpNone" type="radio" name="inSeqOp" value="none"
                                               onclick="radioClicked('in', null);"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="inSeqOpNone"><fmt:message key="select.inseq.none"/></label></td>
                                    <td class="nopadding"></td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="inSeqOpAnon" type="radio" name="inSeqOp" value="anon"
                                               onclick="radioClicked('in', 'AnonAddEdit');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="inSeqOpAnon"><fmt:message key="define.inline"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <% if (anonInAddEdit.equalsIgnoreCase(bundle.getString("create"))) {%>
                                        <a href="#" class="icon-link" id="inAnonAddEdit" style="background-image: url(../admin/images/add.gif);"
                                           onclick="anonSeqAddEdit('in');"><%=anonInAddEdit%></a>
                                        <% } else {%>
                                        <a href="#" class="icon-link" id="inAnonAddEdit" style="background-image: url(../admin/images/edit.gif);"
                                           onclick="anonSeqAddEdit('in');"><%=anonInAddEdit%></a>
                                        <% } %>
                                    </td>
                                    <td class="nopadding">&nbsp;</td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <a href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif);"
                                           id="inAnonClear" onclick="anonSeqClear('in');"><fmt:message key="clear"/></a>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="inSeqOpReg" type="radio" name="inSeqOp" value="registry"
                                               onclick="radioClicked('in', 'Registry');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="inSeqOpReg"><fmt:message key="pick.from.registry"/></label></td>
                                    <td class="nopadding">
                                        <table id="inRegistry">
                                            <tr>
                                                <td class="nopadding">
                                                    <input type="text" name="proxy.in.registry"
                                                           id="proxy.in.registry" value="<%=inRegKey%>"
                                                           style="width:300px"
                                                           readonly="readonly"/>
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.in.registry','/_system/config');"><fmt:message
                                                            key="conf.registry"/></a>
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.in.registry','/_system/governance');"><fmt:message
                                                            key="gov.registry"/></a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="inSeqOpImp" type="radio" name="inSeqOp" value="import"
                                               onclick="radioClicked('in', 'ImportSeq');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="inSeqOpImp"><fmt:message key="use.existing.sequence"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <select id="inImportSeq" name="inImportSeq">
                                            <%=inSeqOptions%>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
<br/>
    <table width="100%" class="styledInner">
        <thead>
            <tr>
                <th><fmt:message key="endpoint.options"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <!-- endpoint-->
                    <div id="endpoint">
                        <div id="endpointDesign">
                            <table id="epOptionTable" class="normal">
                                <tr>
                                    <td class="nopadding">
                                        <input id="epOpNone" type="radio" name="epOp" value="none" onclick="eprRadioClicked('epOpNone');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="epOpNone"><fmt:message key="select.inseq.none"/></label></td>
                                    <td class="nopadding"></td>
                                    <td class="nopadding"></td>
                                    <td class="nopadding"></td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="epOpAnon" type="radio" name="epOp" value="anon"
                                               onclick="eprRadioClicked('epAnonAddEdit');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="epOpAnon"><fmt:message key="define.inline"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                    <% if (anonEpAddEdit.equalsIgnoreCase(bundle.getString("create"))) { %>
                                        <a href="#" class="icon-link" id="epAnonAddEdit" style="background-image: url(../admin/images/add.gif);"
                                           onclick="anonEpAddEdit();"><%=anonEpAddEdit%></a>
                                    <% } else { %>
                                        <a href="#" class="icon-link" id="epAnonAddEdit" style="background-image: url(../admin/images/edit.gif);"
                                           onclick="anonEpAddEdit();"><%=anonEpAddEdit%></a>
                                    <% } %>    
                                    </td>
                                    <td class="nopadding">&nbsp;</td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <a href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif);"
                                           id="epAnonClear" onclick="anonEpClear();"><fmt:message key="clear"/></a>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="epOpReg" type="radio" name="epOp" value="registry"
                                               onclick="eprRadioClicked('eprRegistry');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="epOpReg"><fmt:message key="pick.from.registry"/></label></td>
                                    <td class="nopadding">
                                        <table id="eprRegistry">
                                            <tr>
                                                <td class="nopadding">
                                                    <input type="text" name="proxy.epr.registry"
                                                           id="proxy.epr.registry" value="<%=eprRegKey%>"
                                                           style="width:300px"
                                                           readonly="readonly" />
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.epr.registry','/_system/config')"><fmt:message
                                                            key="conf.registry"/></a>
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.epr.registry','/_system/governance')"><fmt:message
                                                            key="gov.registry"/></a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td class="nopadding"></td>
                                    <td class="nopadding"></td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="epOpImp" type="radio" name="epOp" value="import"
                                               onclick="eprRadioClicked('importEp');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="epOpImp"><fmt:message key="use.existing.endpoint"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <select id="importEp" name="importEp">
                                            <%=epOptions%>
                                        </select>
                                    </td>
                                    <td class="nopadding"></td>
                                    <td class="nopadding"></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
</div>                                      
<div id="page2">
    <table width="100%" class="styledInner">
        <thead>
            <tr>
                <th><fmt:message key="out.sequence.options"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <div id="outSeq">
                        <div id="outSeqDesign">
                            <table id="outSeqOptionTable" class="normal">
                                <tr>
                                    <td class="nopadding">
                                        <input id="outSeqOpNone" type="radio" name="outSeqOp" value="none"
                                               onclick="radioClicked('out', null);"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="epOpImp"><fmt:message key="select.inseq.none"/></label></td>
                                    <td class="nopadding"></td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="outSeqOpAnon" type="radio" name="outSeqOp" value="anon"
                                               onclick="radioClicked('out', 'AnonAddEdit');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="outSeqOpAnon"><fmt:message key="define.inline"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <% if (anonOutAddEdit.equalsIgnoreCase(bundle.getString("create"))) { %>
                                        <a href="#" class="icon-link" id="outAnonAddEdit" style="background-image: url(../admin/images/add.gif);"
                                           onclick="anonSeqAddEdit('out');"><%=anonOutAddEdit%></a>
                                        <% } else { %>
                                        <a href="#" class="icon-link" id="outAnonAddEdit" style="background-image: url(../admin/images/edit.gif);"
                                           onclick="anonSeqAddEdit('out');"><%=anonOutAddEdit%></a>
                                        <% } %>
                                    </td>
                                    <td class="nopadding">&nbsp;</td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <a href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif);"
                                           id="outAnonClear" onclick="anonSeqClear('out');"><fmt:message key="clear"/></a>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="outSeqOpReg" type="radio" name="outSeqOp" value="registry"
                                               onclick="radioClicked('out', 'Registry');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="outSeqOpReg"><fmt:message key="pick.from.registry"/></label></td>
                                    <td class="nopadding">
                                        <table id="outRegistry" class="normal-nopadding">
                                            <tr>
                                                <td class="nopadding">
                                                    <input type="text" name="proxy.out.registry"
                                                           id="proxy.out.registry" value="<%=outRegkey%>"
                                                           style="width:300px"
                                                           readonly="readonly"/>
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.out.registry','/_system/config')"><fmt:message
                                                            key="conf.registry"/></a>
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.out.registry','/_system/governance')"><fmt:message
                                                            key="gov.registry"/></a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="outSeqOpImp" type="radio" name="outSeqOp" value="import"
                                               onclick="radioClicked('out', 'ImportSeq');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="outSeqOpImp"><fmt:message key="use.existing.sequence"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <select id="outImportSeq" name="outImportSeq">
                                            <%=outSeqOptions%>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
<br/>
    <table width="100%" class="styledInner">
        <thead>
            <tr>
                <th><fmt:message key="fault.sequence.options"/></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <div id="faultSeq">
                        <div id="faultSeqDesign">
                            <table id="faultSeqOptionTable" class="normal">
                                <tr>
                                    <td class="nopadding">
                                        <input id="faultSeqOpNone" type="radio" name="faultSeqOp" value="none"
                                               onclick="radioClicked('fault', null);"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="faultSeqOpNone"> <fmt:message key="select.inseq.none"/></label></td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="faultSeqOpAnon" type="radio" name="faultSeqOp" value="anon"
                                               onclick="hideOtherSeqOp('fault', 'ImportSeq');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="faultSeqOpAnon"><fmt:message key="define.inline"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <% if (anonFaultAddEdit.equalsIgnoreCase(bundle.getString("create"))) { %>
                                        <a href="#" class="icon-link" id="faultAnonAddEdit" style="background-image: url(../admin/images/add.gif);"
                                           onclick="anonSeqAddEdit('fault');"><%=anonFaultAddEdit%></a>
                                        <% } else { %>
                                        <a href="#" class="icon-link" id="faultAnonAddEdit" style="background-image: url(../admin/images/edit.gif);"
                                           onclick="anonSeqAddEdit('fault');"><%=anonFaultAddEdit%></a>
                                        <% }%>
                                    </td>
                                    <td class="nopadding">&nbsp;</td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <a href="#" class="icon-link" style="background-image: url(../admin/images/delete.gif);"
                                           id="faultAnonClear" onclick="anonSeqClear('fault');"><fmt:message key="clear"/></a>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="faultSeqOpReg" type="radio" name="faultSeqOp" value="registry"
                                               onclick="radioClicked('fault', 'Registry');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="faultSeqOpReg"><fmt:message key="pick.from.registry"/></label></td>
                                    <td class="nopadding">
                                        <table id="faultRegistry">
                                            <tr>
                                                <td class="nopadding">
                                                    <input type="text" name="proxy.fault.registry"
                                                           id="proxy.fault.registry" value="<%=faultRegKey%>"
                                                           style="width:300px"
                                                           readonly="readonly"/>
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.fault.registry','/_system/config')"><fmt:message
                                                            key="conf.registry"/></a>
                                                </td>
                                                <td class="nopadding">
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:40px"
                                                       onclick="showRegistryBrowserWithoutLocalEntries('proxy.fault.registry','/_system/governance')"><fmt:message
                                                            key="gov.registry"/></a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="nopadding">
                                        <input id="faultSeqOpImp" type="radio" name="faultSeqOp" value="import"
                                               onclick="radioClicked('fault', 'ImportSeq');"/>
                                    </td>
                                    <td style="vertical-align:middle;" class="nopadding"><label for="faultSeqOpImp"><fmt:message key="use.existing.sequence"/></label></td>
                                    <td style="vertical-align:middle;" class="nopadding">
                                        <select id="faultImportSeq" name="faultImportSeq">
                                            <%=faultSeqOptions%>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
</div>
</td></tr></table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input id="backBtn" type="button" value="<<fmt:message key="back"/>" class="button" onclick="showBackPage();"/>
        <input id="saveBtn" type="submit" value="<fmt:message key="finish"/>" class="button" onclick="saveData();return false;" disabled="disabled"/>
        <input id="nextBtn" type="button" value="<fmt:message key="next"/>>" class="button" onclick="showNextPage();"/>
        <input id="cancelBtn" type="button" value="<fmt:message key="cancel"/>" class="button" onclick="cancelData();return false"/>
    </td>
</tr>
</tbody>
</table>
</form>
</div>
</div>

</fmt:bundle>
