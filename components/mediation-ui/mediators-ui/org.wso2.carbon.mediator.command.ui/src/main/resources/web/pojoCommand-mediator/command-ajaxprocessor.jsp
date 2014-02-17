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
<%@ page import="org.wso2.carbon.mediator.command.ui.CommandMediatorAdminClient" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.command.CommandMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="java.util.Set" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:bundle basename="org.wso2.carbon.mediator.command.ui.i18n.Resources">
<div>
    <script type="text/javascript" src="../rule-mediator/js/mediator-util.js"></script>
    <script type="text/javascript" src="../sequences/js/ns-editor.js"></script>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CommandMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CommandMediator pojoCommandMediator = (CommandMediator) mediator;
    if ("true".equals(request.getParameter("clearAll"))) {
        pojoCommandMediator.getStaticSetterProperties().clear();
        pojoCommandMediator.getMessageSetterProperties().clear();
        pojoCommandMediator.getContextSetterProperties().clear();
        pojoCommandMediator.getMessageGetterProperties().clear();
        pojoCommandMediator.getContextGetterProperties().clear();
    }
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    boolean classNotFound = false;
    String[] classAttrib = null;
    response.setHeader("Cache-Control", "no-cache");
    String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext context =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    CommandMediatorAdminClient client = new CommandMediatorAdminClient(cookie,
            backEndServerURL, context, request.getLocale());
    String className = request.getParameter("mediatorInput");
    pojoCommandMediator.setCommand(className);
    Map<String,Object> staticSetterProps = pojoCommandMediator.getStaticSetterProperties();
    Map<String,SynapseXPath> messageSetterProps = pojoCommandMediator.getMessageSetterProperties();
    Map<String, String> contextSetterProps = pojoCommandMediator.getContextSetterProperties();
    Map<String, SynapseXPath> messageGetterProps = pojoCommandMediator.getMessageGetterProperties();
    Map<String, String> contextGetterProps = pojoCommandMediator.getContextGetterProperties();

    try {
        classAttrib = client.getClassAttributes(request.getParameter("mediatorInput"));
    } catch (RemoteException e) {
        classNotFound = true; // TODO:do nothing, set class not found exception, may be we should be more specific
    }
    if (classAttrib != null && classAttrib.length > 0 && classAttrib[0] != null && !classNotFound) {
%>
    <h3 id="propertyLabel" class="mediator"><fmt:message key="mediator.command.properties"/></h3>

    <div style="margin-top:0px;">
        <table id="commandPropTable" class="styledInner">
            <thead>
                <tr>
                    <th><fmt:message key="mediator.command.propName"/></th>
                    <th><fmt:message key="mediator.command.readInfo"/></th>
                    <th><fmt:message key="mediator.command.updateInfo"/></th>
                    <th><fmt:message key="mediator.command.expression"/></th>
                    <th><fmt:message key="mediator.command.ns.editor"/>&nbsp;</th>
                    <th><fmt:message key="mediator.command.property.name"/></th>
                </tr>
            </thead>
                <tbody>
                    <%
                        int index;
                        for (index = 0; index < classAttrib.length; index++) {
                            String attributeName = classAttrib[index];
                            boolean hasValue;
                    %>
                    <tr>
                        <td style="vertical-align:top !important">
                            <input type="hidden" name="propertyName<%= index%>" id="propertyName<%= index%>"
                                   value="<%=attributeName%>"/><%= attributeName%>
                        </td>
                        <td style="vertical-align:top !important"><fmt:message key="mediator.command.from"/>:
                            <select class="esb-edit small_textbox" id="propertySelectReadType<%=index%>"
                                    name="propertySelectReadType<%=index%>"
                                    onchange="enableDisableInputs(<%= index%>)">
                                <option value="none">- <fmt:message key="mediator.command.selectValueNone"/> -</option>
                                <option value="value" <%=staticSetterProps.containsKey(attributeName) ? " selected=\"true\"" : ""%>>
                                    <fmt:message key="mediator.command.selectValueValue"/></option>
                                <option value="message" <%=messageSetterProps.containsKey(attributeName) ? " selected=\"true\"" : ""%>>
                                    <fmt:message key="mediator.command.selectValueMsg"/></option>
                                <option value="context" <%=contextSetterProps.containsKey(attributeName) ? " selected=\"true\"" : ""%>>
                                    <fmt:message key="mediator.command.selectValueContxt"/></option>
                            </select>
                            <%
                                Object staticValue = staticSetterProps.get(attributeName);
                                hasValue = (staticValue != null);
                            %>
                            <div id="staticValueDiv<%=index%>"
                                    <%=hasValue ? "style=\"white-space:nowrap\"" : " style=\"display:none;white-space:nowrap\""%>>
                                <br/>
                                <fmt:message key="mediator.command.value"/>:
                                <input type="text" name="mediator.command.prop.value<%=index%>"
                                       id="mediator.command.prop.value.id<%=index%>"
                                   <%=hasValue ? " value=\"" + staticValue.toString() + "\"" : ""%>/>
                            </div>
                        </td>
                        <td style="white-space:nowrap;vertical-align:top !important"><fmt:message key="mediator.command.to"/>:
                            <select class="esb-edit small_textbox" id="propertySelectUpdateType<%=index%>"
                                    name="propertySelectUpdateType<%=index%>"
                                    onchange="enableDisableInputs(<%= index%>)">
                                <option value="none">- <fmt:message key="mediator.command.selectValueNone"/> -</option>
                                <option value="message" <%=messageGetterProps.containsKey(attributeName) ? " selected=\"true\"" : ""%>>
                                    <fmt:message key="mediator.command.selectValueMsg"/></option>
                                <option value="context" <%=contextGetterProps.containsKey(attributeName) ? " selected=\"true\"" : ""%>>
                                    <fmt:message key="mediator.command.selectValueContxt"/></option>
                            </select>
                        </td>
                        <td style="vertical-align:top !important">
                            <%
                                SynapseXPath xPath;
                                hasValue = (xPath = messageSetterProps.get(attributeName)) != null ||
                                        (xPath = messageGetterProps.get(attributeName)) != null;
                                if (hasValue) {
                                    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
                                    nmspRegistrar.registerNameSpaces(xPath,
                                            "mediator.command.prop.xpath" + index, session);
                                }
                            %>
                            <input type="text" name="mediator.command.prop.xpath<%=index%>"
                                   id="mediator.command.prop.xpath.id<%=index%>"
                                   <%= hasValue ? " value=\"" + xPath.toString() + "\"" : " disabled=\"true\""%>/>
                        </td>
                        <td style="vertical-align:top !important">
                            <a href="#nsEditorLink" class="nseditor-icon-link"
                               style="padding-left:40px;<%=hasValue ? "" : "display:none"%>"
                               onclick="showNameSpaceEditor('mediator.command.prop.xpath<%=index%>')"
                               id="mediator.command.prop.ns.link.id<%=index%>">
                                <fmt:message key="mediator.command.namespaces"/>
                            </a>
                            <div id="mediator.command.prop.ns.dummy.id<%=index%>" <%= hasValue ?
                                    " style=\"display:none\"" : ""%>>
                            </div>
                        </td>
                        <td style="vertical-align:top !important">
                            <%
                                String contextPropName;
                                hasValue = (contextPropName = contextSetterProps.get(attributeName)) != null ||
                                        (contextPropName = contextGetterProps.get(attributeName)) != null;
                            %>
                            <input type="text" name="mediator.command.prop.context<%=index%>"
                                   id="mediator.command.prop.context.id<%=index%>"
                                   <%= hasValue ? " value=\"" + contextPropName + "\"" : " disabled=\"true\""%>/>
                        </td>
                        <%--td style="vertical-align:top !important"><a href="#" class="icon-link" style="background-image:url(../admin/images/delete.gif);"
                               onclick="javascript:deleteRowCommand(this)"><fmt:message
                                key="mediator.command.delete"/></a>
                        </td--%>
                    </tr>
                    <%
                        }
                    %>
                    <input type="hidden" name="propertyCount" id="propertyCount" value="<%=index%>"/>
                </tbody>
        </table>
    </div>
            <%

                //TODO: let the error be more specific,
            } else if (!classNotFound) {  // no execute method in the class

            %>
            <script type="text/javascript">
                CARBON.showErrorDialog('<fmt:message key="mediator.command.invalidClassMsg"/>');
            </script>
            <%

            } else if (classNotFound) {

            %>
            <script type="text/javascript">
                CARBON.showErrorDialog('<fmt:message key="mediator.command.classnotFoundMsg"/>');
            </script>
            <%

                }

            %>
</div>
</fmt:bundle>