<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.sequences.common.to.ConfigurationObject" %>
<%@ page import="org.wso2.carbon.sequences.ui.client.SequenceAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    String sequenceName = request.getParameter("sequenceName");
    String sequenceType = request.getParameter("type");
    boolean forceDelete = "true".equals(request.getParameter("force"));

    SequenceAdminClient sequenceAdminClient
            = new SequenceAdminClient(this.getServletConfig(), session);
    if (sequenceName != null && !"".equals(sequenceName) && sequenceType==null) {
        if (!forceDelete) {
            try {
                ConfigurationObject[] dependents = sequenceAdminClient.getDependents(sequenceName);
                if (dependents != null) {
                    String msg = "";
                    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.mediation.initializer.ui.i18n.Resources",
                            request.getLocale());
                    for (ConfigurationObject o : dependents) {
                        msg += "&ensp;&ensp;- " + o.getResourceId();
                        if (bundle != null) {
                            msg += " (" + bundle.getString("dependency.mgt." + o.getType()) + ")";
                        }
                        msg += "<br/>";
                    }
                    request.getSession().setAttribute("seq.d.mgt.error.msg", msg);
                    request.getSession().setAttribute("seq.d.mgt.error.name", sequenceName);
                } else {
                    doForceDelete(sequenceAdminClient, sequenceName, request);
                }
            } catch (Exception e) {
                String msg = "Could not delete sequence: " + e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
            }
        } else {
            doForceDelete(sequenceAdminClient, sequenceName, request);
        }
    } else {
        sequenceAdminClient.deleteDynamicSequence(sequenceName);
        // TODO: error handling
    }
%>

<script type="text/javascript">
    document.location.href = "list_sequences.jsp";
</script>

<%!
    private void doForceDelete(SequenceAdminClient adminClient, String sequenceName,
                             HttpServletRequest request) {
        try {
            adminClient.deleteSequence(sequenceName);
        } catch (Exception e) {
            String msg = "Could not delete sequence: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }
    }
%>
