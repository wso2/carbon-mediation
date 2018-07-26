<%--
  ~  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@page import="org.wso2.carbon.mediator.kerberos.KerberosMediator"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.mediators.Value"%>

<%
    try {
        String loginContextName = request.getParameter("loginContextName");
        String loginConfig = request.getParameter("loginConfig");
        String krb5Config = request.getParameter("krb5Config");
        String spn = request.getParameter("spn");
        String clientPrincipal = request.getParameter("clientPrincipal");
        String clientPrincipalType = request.getParameter("clientPrincipalType");
        String password = request.getParameter("password");
        String passwordType = request.getParameter("passwordType");
        String keytabPath = request.getParameter("keytabPath");
        String keytabPathType = request.getParameter("keytabPathType");
        String krb5Option = request.getParameter("krb5Option");
        String spnOption = request.getParameter("spnOption");
        String loginOption = request.getParameter("loginOption");
        String keyTabOption = request.getParameter("keyTabOption");

        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof KerberosMediator)) {
            throw new RuntimeException("Unable to update the mediator");
        }
        KerberosMediator kerberosMediator = (KerberosMediator) mediator;
        XPathFactory xPathFactory = XPathFactory.getInstance();
        kerberosMediator.setLoginContextName(loginContextName);

        if (clientPrincipalType != null && "expression".equals(clientPrincipalType.trim())) {
            kerberosMediator.setClientPrincipal(new Value(xPathFactory.createSynapseXPath("clientPrincipalType",
                                                clientPrincipal.trim(), session)));
        } else {
            kerberosMediator.setClientPrincipal(new Value(clientPrincipal));
        }

        if ("selectFromRegistry".equals(krb5Option)) {
            String selectFromRegistry = request.getParameter("krb5ConfigKey");
            if (selectFromRegistry != null) {
                kerberosMediator.setKrb5ConfigKey(new Value(selectFromRegistry));
            }
        } else {
            kerberosMediator.setKrb5Config(krb5Config);
        }

        if ("spnSelectFromRegistry".equals(spnOption)) {
            String spnFromRegistry = request.getParameter("spnConfigKey");
            if (spnFromRegistry != null) {
                kerberosMediator.setSpnConfigKey(new Value(spnFromRegistry));
            }
        } else {
            kerberosMediator.setSpn(spn);;
        }

        if ("selectFromRegistryLoginConfig".equals(loginOption)) {
            String loginConfigFromRegistry = request.getParameter("loginConfigKey");
            if (loginConfigFromRegistry != null) {
                kerberosMediator.setLoginConfigKey(new Value(loginConfigFromRegistry));
            }
        } else {
            kerberosMediator.setLoginConfig(loginConfig);
        }

        if (passwordType != null && "expression".equals(passwordType.trim())) {
            kerberosMediator.setPassword(new Value(xPathFactory.createSynapseXPath("passwordType", password.trim(),
                                                session)));
        } else {
            kerberosMediator.setPassword(new Value(password));
        }

        if ("selectFromRegistryKeyTab".equals(keyTabOption)) {
            String keyTabConfigFromRegistry = request.getParameter("keyTabKey");
            if (keyTabConfigFromRegistry != null) {
                kerberosMediator.setRegistryKeyTabValue(new Value(keyTabConfigFromRegistry));
            }
        } else if (keytabPathType != null && "expression".equals(keytabPathType.trim())) {
            kerberosMediator.setKeytabPath(new Value(xPathFactory.createSynapseXPath("keytabPathType",
            keytabPath.trim(), session)));
        } else {
               kerberosMediator.setKeytabPath(new Value(keytabPath));
        }
    } catch (Exception e) {
        session.setAttribute("sequence.error.message", e.getMessage());
%>

<script type="text/javascript">
            document.location.href = "../sequences/design_sequence.jsp?ordinal=1";
        </script>
        <%
    }
%>


