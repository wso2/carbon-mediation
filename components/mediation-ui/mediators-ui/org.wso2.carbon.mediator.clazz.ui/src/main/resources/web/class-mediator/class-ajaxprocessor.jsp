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
<%@ page import="org.wso2.carbon.mediator.clazz.ui.ClassMediatorAdminClient" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.clazz.ClassMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:bundle basename="org.wso2.carbon.mediator.clazz.ui.i18n.Resources">

    <div>
        <%
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
            if (!(mediator instanceof ClassMediator)) {
                // todo : proper error handling
                throw new RuntimeException("Unable to edit the mediator");
            }
            ClassMediator classMediator = (ClassMediator) mediator;

            boolean classNotFound = false;
            String[] classAttrib = null;
            response.setHeader("Cache-Control", "no-cache");
            String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext context =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            ClassMediatorAdminClient client = new ClassMediatorAdminClient(cookie,
                    backEndServerURL, context, request.getLocale());


            classMediator.setMediator(request.getParameter("mediatorInput"));

            try {
                classAttrib = client.getClassAttributes(request.getParameter("mediatorInput"));
            } catch (RemoteException e) {
                classNotFound = true; // TODO:do nothing, set class not found exception, may be we should be more specific
            }
            if (classAttrib != null && classAttrib.length > 0 && classAttrib[0] != null && !classNotFound) {
        %>
        <td>
            <h3 id="propertyLabel" class="mediator"><fmt:message key="properties.defined.for.class.mediator"/></h3>
        <%--<p id="propertyLabel"><fmt:message key="properties.defined.for.class.mediator"/></p>--%>
        <div style="margin-top:0px;">
        <table id="propertytable" class="styledInner" >
            <thead>
                <tr>
                    <th width="10%"><fmt:message key="mediator.clazz.PropName"/></th>
                    <th width="10%"><fmt:message key="mediator.clazz.PropValue"/></th>
                    <th ><fmt:message key="mediator.clazz.Action"/></th>
                </tr>
            <tbody>
                <%
                    int i;
                    for (i = 0; i < classAttrib.length; i++) {
                %>
                <tr>
                    <td align="left"><input type="hidden" name="propertyName<%=i%>" id="propertyName<%=i%>"
                                            value="<%= classAttrib[i]%>"/><%= classAttrib[i]%>
                    </td>
                    <td><input type="text" name="propertyValue<%=i%>" id="propertyValue<%=i%>"
                               class="esb-edit small_textbox"/>
                    </td>
                    <td><a href="#" class="icon-link" style="background-image:url(../admin/images/delete.gif);"
                           onclick="deleteRowClazz(this)"><fmt:message key="mediator.clazz.Delete"/></a>
                    </td>
                </tr>
                <%
                    }
                %>
            <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
            </tbody>
            </thead>
        </table>
        <%
            //TODO: let the error be more specific,
        } else if (classAttrib == null && !classNotFound) {  // no setters in the class
        %>
        <td>
            <script type="text/javascript">
                CARBON.showErrorDialog('Please enter a valid class');
            </script>
        </td>
        <%
        } else if (classNotFound) {
        %>
        <td>
            <script type="text/javascript">
                CARBON.showErrorDialog('Class not found in the path');
            </script>
        </td>
        <%
            }
        %>
      </td>
        <% if (!classNotFound) { %>
        <script type="text/javascript">
                CARBON.showInfoDialog('Class loaded successfully');
        </script>
        <% } %>
    </div>
</fmt:bundle>
    