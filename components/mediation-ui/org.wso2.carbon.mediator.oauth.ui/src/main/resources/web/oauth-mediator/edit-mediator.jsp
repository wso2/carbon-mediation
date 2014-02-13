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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>

<%
		String remoteServiceUrl = null;
		String username = "";
		String password = "";

		try {
            Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
            if (!(mediator instanceof OAuthMediator)) {
                throw new RuntimeException("Unable to update the mediator");
            }
            OAuthMediator oauthMediator = (OAuthMediator)mediator;
            remoteServiceUrl = oauthMediator.getRemoteServiceUrl();
            username = oauthMediator.getUsername();
            password = oauthMediator.getPassword();
            
            if(remoteServiceUrl==null){
            	remoteServiceUrl ="";
            }
            if(username == null){
            	username = "";
            }
            if(password == null){
            	password = "";
            }
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
 %>
            
<%@page import="org.wso2.carbon.mediator.oauth.OAuthMediator"%><script type="text/javascript">
                   location.href = "../admin/error.jsp";
            </script>
    <%
            return;
        }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.oauth.ui.i18n.Resources">
    <carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.oauth.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="enti18n"/>    
<div>
    <script type="text/javascript" src="../oauth-mediator/js/mediator-util.js"></script>

    <table class="normal" width="100%">
        <tr>
            <td>
                <h2><fmt:message key="mediator.oauth.header"/></h2>
            </td>
        </tr>

        <tr>
            <td>
                <table style="width: 100%">
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.oauth.remoteservice"/>
                        </td>
                        <td class="text-box-big">
                        <input type="text" id="remoteServiceUrl" name="remoteServiceUrl" value="<%=remoteServiceUrl%>" />
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table style="width: 100%">
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.oauth.username"/>
                        </td>
                        <td class="text-box-big">
                        <input type="text" id="username" name="username" value="<%=username%>" />
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table style="width: 100%">
                    <tr>
                        <td class="leftCol-small">
                            <fmt:message key="mediator.oauth.password"/>
                        </td>
                        <td class="text-box-big">
                        <input type="password" id="password" name="password" value="<%=password%>" />
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>
</fmt:bundle>