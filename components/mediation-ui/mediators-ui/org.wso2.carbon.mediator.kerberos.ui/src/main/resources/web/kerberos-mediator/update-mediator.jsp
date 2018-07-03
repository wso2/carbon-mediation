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

        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof KerberosMediator)) {
            throw new RuntimeException("Unable to update the mediator");
        }
        KerberosMediator kerberosMediator = (KerberosMediator) mediator;
        XPathFactory xPathFactory = XPathFactory.getInstance();
        kerberosMediator.setLoginContextName(loginContextName);
        kerberosMediator.setLoginConfig(loginConfig);
        kerberosMediator.setKrb5Config(krb5Config);
        kerberosMediator.setSpn(spn);

        if (clientPrincipalType != null && "expression".equals(clientPrincipalType.trim())) {
            kerberosMediator.setClientPrincipal(new Value(xPathFactory.createSynapseXPath("clientPrincipalType", 
                                                clientPrincipal.trim(), session)));
        } else {
            kerberosMediator.setClientPrincipal(new Value(clientPrincipal));
        }
        
        if (passwordType != null && "expression".equals(passwordType.trim())) {
            kerberosMediator.setPassword(new Value(xPathFactory.createSynapseXPath("passwordType", password.trim(),
                                                session)));
        } else {
            kerberosMediator.setPassword(new Value(password));
        }
        
        if (keytabPathType != null && "expression".equals(keytabPathType.trim())) {
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


