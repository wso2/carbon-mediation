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
        String authType = request.getParameter("authgroup");
        String password = request.getParameter("password");
        String spnOption = request.getParameter("spnOption");

        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof org.wso2.carbon.mediator.kerberos.ui.KerberosMediator)) {
            throw new RuntimeException("Unable to update the mediator");
        }
        org.wso2.carbon.mediator.kerberos.ui.KerberosMediator kerberosMediator =
                (org.wso2.carbon.mediator.kerberos.ui.KerberosMediator) mediator;
        XPathFactory xPathFactory = XPathFactory.getInstance();

        if ("spnSelectFromRegistry".equals(spnOption)) {
            String spnFromRegistry = request.getParameter("spnConfigKey");
            if (spnFromRegistry != null) {
                kerberosMediator.setSpnConfigKey(new Value(spnFromRegistry));
            }
        } else {
            kerberosMediator.setSpn(spn);;
        }

        if (authType != null && "keytabauth".equals(authType.trim())) {
            kerberosMediator.setLoginContextName(loginContextName);
            kerberosMediator.setLoginConfig(loginConfig);
            kerberosMediator.setKrb5Config(krb5Config);
            kerberosMediator.setPassword(new Value(""));
            kerberosMediator.setClientPrincipal(new Value(""));
        } else {
            kerberosMediator.setPassword(new Value(password));
            kerberosMediator.setClientPrincipal(new Value(clientPrincipal));
            kerberosMediator.setLoginContextName("");
            kerberosMediator.setLoginConfig("");
            kerberosMediator.setKrb5Config("");
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
