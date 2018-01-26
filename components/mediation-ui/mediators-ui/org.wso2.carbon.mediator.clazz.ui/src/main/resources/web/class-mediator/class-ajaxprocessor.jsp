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
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.jaxen.JaxenException" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.clazz.ClassMediator" %>
<%@ page import="org.wso2.carbon.mediator.clazz.ui.ClassMediatorAdminClient" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<%@ page import="java.rmi.RemoteException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:bundle basename="org.wso2.carbon.mediator.clazz.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.clazz.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="clazzi18n"/>

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
                    <th width="15%"><fmt:message key="mediator.clazz.propName"/></th>
                    <th width="10%"><fmt:message key="mediator.clazz.propValue"/></th>
                    <th width="15%"><fmt:message key="mediator.clazz.propertyExp"/></th>
                    <th id="ns-edior-th" style="display:none;" width="15%"><fmt:message
                            key="mediator.clazz.nsEditor"/></th>
                    <th ><fmt:message key="mediator.clazz.action"/></th>
                </tr>
            <tbody>
                <%
                    List<MediatorProperty> mediatorPropertyList = new ArrayList<MediatorProperty>();
                    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
                    nameSpacesRegistrar.registerNameSpaces(mediatorPropertyList, "propertyValue", session);

                    for (int j = 0; j < classAttrib.length; j++) {
                        MediatorProperty mediatorProperty = new MediatorProperty();
                        mediatorProperty.setName(classAttrib[j]);
                        mediatorProperty.setValue("");
                        mediatorPropertyList.add(mediatorProperty);
                    }

                   int i = 0;
                   for (MediatorProperty mp : mediatorPropertyList) {
                       if (mp != null) {
                           String value = mp.getValue();
                           boolean isLiteral = value != null;

                           String pathValue = "";
                           SynapsePath path = null;
                           SynapseXPath synapseXPath = mp.getExpression();
                           if(!isLiteral && mp.getPathExpression() != null) {
                               if(synapseXPath == null) {
                                   path = mp.getPathExpression();
                                   pathValue = path.toString();
                               } else {
                                   path = synapseXPath;
                                   pathValue = path.toString();
                               }
                           }
               %>
               <tr id="propertyRaw<%=i%>">
                   <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                              class="esb-edit small_textbox"
                              value="<%=mp.getName()%>"/>
                   </td>
                   <td>
                       <select class="esb-edit small_textbox" name="propertyTypeSelection<%=i%>"
                               id="propertyTypeSelection<%=i%>"
                               onchange="onPropertyTypeSelectionChange('<%=i%>','<fmt:message key="mediator.clazz.namespace"/>')">
                           <% if (isLiteral) {%>
                           <option value="literal">
                               <fmt:message key="mediator.clazz.value"/>
                           </option>
                           <option value="expression">
                               <fmt:message key="mediator.clazz.expression"/>
                           </option>
                           <%} else if (path != null) {%>
                           <option value="expression">
                               <fmt:message key="mediator.clazz.expression"/>
                           </option>
                           <option value="literal">
                               <fmt:message key="mediator.clazz.value"/>
                           </option>
                           <%} else { %>
                           <option value="literal">
                               <fmt:message key="mediator.clazz.value"/>
                           </option>
                           <option value="expression">
                               <fmt:message key="mediator.clazz.expression"/>
                           </option>
                           <% }%>
                       </select>
                   </td>
                   <td>
                       <% if (value != null && !"".equals(value)) {%>
                       <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                              value="<%=value%>"
                              class="esb-edit"/>
                       <%} else if (path != null) {%>
                       <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                              value="<%=pathValue%>" class="esb-edit"/>
                       <%} else { %>
                       <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                              class="esb-edit"/>
                       <% }%>
                   </td>
                   <td id="nsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
                       <% if (!isLiteral && mp.getPathExpression() != null && path != null) {%>
                       <script type="text/javascript">
                           document.getElementById("ns-edior-th").style.display = "";
                       </script>
                       <a href="#nsEditorLink" class="nseditor-icon-link"
                          style="padding-left:40px"
                          onclick="showNameSpaceEditor('propertyValue<%=i%>')">
                           <fmt:message key="mediator.clazz.namespace"/></a>
                   </td>
                   <%}%>
                   <td><a href="#" class="delete-icon-link"
                          onclick="deleteproperty(<%=i%>);return false;"><fmt:message
                           key="mediator.clazz.delete"/></a></td>
               </tr>
               <% }
                   i++;
               } %>
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
    