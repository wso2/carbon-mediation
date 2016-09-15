<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%--
  ~  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.mediator.payloadfactory.PayloadFactoryMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.utils.CarbonUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.mediator.payloadfactory.util.Utils" %>
<%@ page import="java.io.FileWriter" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof PayloadFactoryMediator)) {
        CarbonUIMessage.sendCarbonUIMessage(
                "Unable to edit the mediator, expected: payloadFactoryMediator, found: " +
                        mediator.getTagLocalName(), CarbonUIMessage.ERROR, request);
        %><jsp:include page="../dialog/display_messages.jsp"/><%
        return;
    }

    PayloadFactoryMediator payloadFactoryMediator = (PayloadFactoryMediator) mediator;

    String format = payloadFactoryMediator.getFormat();
    String mediaType = payloadFactoryMediator.getType();
    String formatKey=(payloadFactoryMediator.getFormatKey()!=null)?payloadFactoryMediator.getFormatKey() : "";
    List<PayloadFactoryMediator.Argument> argumentList = payloadFactoryMediator.getArgumentList();
    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    String argumentTableStyle = argumentList.isEmpty() ? "display:none;" : "";



%>
<fmt:bundle basename="org.wso2.carbon.mediator.payloadfactory.ui.i18n.Resources" >
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.payloadfactory.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="payloadfactory_i18n"/>
<div>

<script type="text/javascript" src="../payloadfactory-mediator/js/mediator-util.js"></script>

<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.payloadFactory.header"/></h2>
    </td>
</tr>
<tr>
    <td>


        <table class="normal" >

            <tr>
                <h3><td><fmt:message key="mediator.payloadFactory.media"/></h3></td>
                <td>
                    <select class="esb-edit small_textbox" name="mediaType" id="mediaType">
                        <option value="xml" <%=(mediaType!=null && mediaType.contains("xml")) ? " selected=\"true\"" : ""%>>
                            <fmt:message key="mediator.payloadFactory.media.xml"/>
                        </option>
                        <option value="json" <%=(mediaType!=null && mediaType.contains("json")) ? " selected=\"true\"" : ""%>>
                            <fmt:message key="mediator.payloadFactory.media.json"/>
                        </option>
                        <option value="text" <%=(mediaType!=null && mediaType.contains("text")) ? " selected=\"true\"" : ""%>>
                                                    <fmt:message key="mediator.payloadFactory.media.text"/>
                                                </option>
                    </select>
                </td>

            </tr>
            <tr>
                <h3><td><fmt:message key="mediator.payloadFactory.format"/><font style="color: red;font-size: 8pt;">&nbsp;*</font></h3></td>

            </tr>

            <tr>

                   <td>
                           <%
                               String checked = "";
                               if((format!= null) && (format!="")) {
                                   if (!(payloadFactoryMediator.isDynamic())) {
                                       checked = "checked";
                                   }

                               }

                               %>

                                <input id="pfFormat.Inline" type="radio" name="pfFormat" value="inline"
                                        <%=checked%> onclick="showPfAddtionalOptions('inline');" /><fmt:message
                                    key="define.inline"/>


                            </td>

                   <td>
                        <% if (checked.equals("checked")){%>
                    <textarea id="payloadFactory.format" name="payloadFactory.format"  cols="50" rows="8" spellcheck="false"><%=format != null ?((mediaType!=null && mediaType.contains("json"))? format : Utils.prettyPrint(format)) : "" %></textarea>

                       <%
                          checked = "";

                       %>
                       <% } else { %>
                       <textarea id="payloadFactory.format" name="payloadFactory.format"  cols="50" rows="8" spellcheck="false" style="display:none;"><%=format != null ?((mediaType!=null && mediaType.contains("json"))? format : Utils.prettyPrint(format)) : ""  %></textarea>
                        <% } %>
                   </td>

              </tr>
            <tr>
                            <td>
                                   <%
                                  if((formatKey!= null) && (formatKey!="")) {
                                      if (payloadFactoryMediator.isDynamic()) {
                                          checked = "checked";
                                      }

                                  }

                                    %>


                                <input id="pfFormat.Reg" type="radio" name="pfFormat" value="registry"
                                         <%=checked%> onclick="showPfAddtionalOptions('registryPf');" /><fmt:message
                                    key="pick.from.registry"/>

                            </td>
                             <td>
                                 <% if (checked.equals("checked")){%>

                                <input type="text" id="registryKey" name="registryKey"
                                       value="<%=formatKey%>" readonly="readonly"/>

                            </td>
                            <td>
                                <a href="#registryBrowserLink" id="confRegPfLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/config');" >
                                    Configuration Registry</a>
                                <a href="#registryBrowserLink" id="govRegPfLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/governance');">
                                    Governance Registry</a>

                                <%
                                    checked = "";

                                %>

                            </td>

                            <% }else {%>
                                <input type="text" id="registryKey" name="registryKey"
                                       value="<%=formatKey%>" readonly="readonly" style="display:none;"/>

                            </td>
                            <td>
                                <a href="#registryBrowserLink" id="confRegPfLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/config');" style="display:none;">
                                    Configuration Registry</a>
                                <a href="#registryBrowserLink" id="govRegPfLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('registryKey','/_system/governance');" style="display:none;">
                                    Governance Registry</a>


                            </td>
                            <% } %>

             </tr>

        </table>
    </td>
</tr>
<tr>
    <td>
        <h3 class="mediator"><fmt:message key="mediator.payloadFactory.arguments"/></h3>

        <div style="margin-top:0px;">
            <table id="argumentTable" style="<%=argumentTableStyle%>;" class="styledInner">
                <thead>
                    <tr>
                        <th width="10%"><fmt:message key="mediator.payloadFactory.arg.index"/></th>
                        <th width="10%"><fmt:message key="mediator.payloadFactory.arg.type"/></th>
                        <th width="10%"><fmt:message key="mediator.payloadFactory.arg.eval"/></th>
                        <th width="10%"><fmt:message key="mediator.payloadFactory.arg.deep.check"/></th>
                        <th width="10%"><fmt:message key="mediator.payloadFactory.arg.literal"/></th>
                        <th width="15%"><fmt:message key="mediator.payloadFactory.arg.value"/></th>
                        <th id="ns-edior-th" style="display:none;" width="15%"><fmt:message
                                key="mediator.payloadFactory.ns.editor"/></th>
                        <th><fmt:message key="mediator.payloadFactory.action"/></th>
                    </tr>
                    </thead>
                    <tbody id="argumentTableBody">
                    <%
                    int i = 1;
                    for (PayloadFactoryMediator.Argument arg : argumentList) {
                        if (arg != null) {
                            boolean isXPath = arg.getExpression() != null;
                            boolean isDeepCheck = arg.isDeepCheck();
                            boolean isLiteral = arg.isLiteral();
                            boolean isJson  = arg.getJsonPath() != null;
                            boolean isValue = arg.getValue() != null;
                            if (isXPath) {
                                nameSpacesRegistrar.registerNameSpaces(arg.getExpression(),
                                        "payloadFactory.argValue" + i, session);
                            }
                    %>
                        <tr id="argRaw<%=i%>">
                            <td id="argIndex<%=i%>"><%=i%></td>
                            <td>
                                <select class="esb-edit small_textbox" name="argType<%=i%>"
                                        id="argType<%=i%>"
                                        onchange="onArgTypeChange('<%=i%>')">
                                    <option value="value"
                                            <%=!isXPath ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.value"/>
                                    </option>
                                    <option value="expression"
                                            <%=isXPath || isJson ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.expression"/>
                                    </option>
                                </select>
                            </td>
                            <td>
                                <select class="esb-edit small_textbox" name="payloadFactory.argEval<%=i%>"
                                        id="payloadFactory.argEval<%=i%>" style="<%=isValue? "display:none;" : ""%>"
                                        onchange="onEvalTypeChange('<%=i%>')"
                                        >
                                    <option value="xml"
                                            <%=isXPath ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.media.xml"/>
                                    </option>
                                    <option value="json"
                                            <%=isJson ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.media.json"/>
                                    </option>

                                </select>
                            </td>
                            <td>
                                <select class="esb-edit small_textbox" name="payloadFactory.argDeepCheck<%=i%>"
                                        id="payloadFactory.argDeepCheck<%=i%>" style="">
                                    <option value="true"
                                            <%=isDeepCheck ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.true"/>
                                    </option>
                                    <option value="false"
                                            <%=!isDeepCheck ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.false"/>
                                    </option>

                                </select>
                            </td>
                            <td>
                                <select class="esb-edit small_textbox" name="payloadFactory.argLiteral<%=i%>"
                                        id="payloadFactory.argLiteral<%=i%>" style="">
                                    <option value="true"
                                            <%=isLiteral ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.true"/>
                                    </option>
                                    <option value="false"
                                            <%=!isLiteral ? " selected=\"true\"" : "" %>>
                                        <fmt:message key="mediator.payloadFactory.false"/>
                                    </option>

                                </select>
                            </td>
                            <td>
                                <input id="payloadFactory.argValue<%=i%>"
                                       name="payloadFactory.argValue<%=i%>" type="text"
                                       value="<%= isXPath ? arg.getExpression().toString() : ( isJson ? arg.getJsonPath().getExpression() : arg.getValue())%>"

                                       class="esb-edit"/>
                            </td>
                            <td id="nsEditorButtonTD<%=i%>" style="<%=isXPath? "display:none;" : ""%>">
                                <% if (isXPath) { %>
                                    <script type="text/javascript">
                                        document.getElementById("ns-edior-th").style.display = "";
                                    </script>
                                    <a href="#nsEditorLink" class="nseditor-icon-link"
                                        style="padding-left:40px"
                                        onclick="showNameSpaceEditor('payloadFactory.argValue<%=i%>')">
                                        <fmt:message key="mediator.payloadFactory.namespaces"/></a>
                                <% } %>
                            </td>
                            <td>
                                <a href="#" class="delete-icon-link"
                                   onclick="deleteArg(<%=i%>);return false;"><fmt:message
                                    key="mediator.payloadFactory.delete"/></a>
                            </td>
                        </tr>
                    <%
                        }
                        i++;
                    }
                    %>
                    <input type="hidden" name="argCount" id="argCount" value="<%=i%>"/>
                    <script type="text/javascript">
                        if (isRemainPropertyExpressions()) {
                            resetDisplayStyle("");
                        }
                    </script>
                    </tbody>
            </table>
        </div>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:10px;">
            <a name="addNameLink"></a>
            <a class="add-icon-link"
               href="#addNameLink"
               onclick="addArgument('<fmt:message key="mediator.payloadFactory.namespaces"/>')">
                <fmt:message key="mediator.payloadFactory.add.argument"/>
            </a>
        </div>
    </td>
</tr>
</table>
<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>
