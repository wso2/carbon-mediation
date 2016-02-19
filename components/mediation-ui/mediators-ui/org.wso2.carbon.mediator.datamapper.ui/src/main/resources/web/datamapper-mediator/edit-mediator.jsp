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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.mediator.datamapper.ui.DataMapperMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof DataMapperMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    DataMapperMediator dataMapperMediator = (DataMapperMediator) mediator;

    String configKeyVal = "";
    String inputSchemaVal = "";
    String outputSchemaVal = "";

    Value configKey = null;
    if(dataMapperMediator.getConfigurationKey() != null) {
        System.out.println("getConfigurationKey is null");
        configKey = dataMapperMediator.getConfigurationKey();
        configKeyVal = configKey.getKeyValue();
    }

	Value inputSchema = null;
    if(dataMapperMediator.getInputSchemaKey() != null) {
        System.out.println("getInputSchemaKey is null");
        inputSchema = dataMapperMediator.getInputSchemaKey();
        inputSchemaVal = inputSchema.getKeyValue();
    }

	Value outputSchema = null;
    if(dataMapperMediator.getOutputSchemaKey() != null) {
        System.out.println("getOutputSchemaKey is null");
        outputSchema = dataMapperMediator.getOutputSchemaKey();
        outputSchemaVal = outputSchema.getKeyValue();
    }

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
            session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
%>

<fmt:bundle basename="org.wso2.carbon.mediator.datamapper.ui.i18n.Resources">
 <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.datamapper.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="logi18n"/>
<div>
<script type="text/javascript" src="../datamapper-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.datamapper.header"/></h2>
    </td>
</tr>
    <tr>
        <td>
            <table class="normal">
                <tr>
                    <td><fmt:message key="mediator.datamapper.configKey"/>
                    </td>
                    <td>
                        <input class="longInput" type="text" value="<%=configKeyVal%>"
                               name="configKey" id="configKey" readonly="true"/>
                    </td>
                    <td>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('configKey','/_system/config')"><fmt:message
                                key="mediator.datamapper.conf.registry.browser"/></a>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('configKey','/_system/governance')"><fmt:message
                                key="mediator.datamapper.gov.registry.browser"/></a>
                    </td>
                </tr>
		 <tr>
                    <td><fmt:message key="mediator.datamapper.inputSchema"/>
                    </td>
                    <td>
                        <input class="longInput" type="text" value="<%=inputSchemaVal%>"
                               name="inputSchema" id="inputSchema" readonly="true"/>
                    </td>
                    <td>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('inputSchema','/_system/config')"><fmt:message
                                key="mediator.datamapper.conf.registry.browser"/></a>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('inputSchema','/_system/governance')"><fmt:message
                                key="mediator.datamapper.gov.registry.browser"/></a>
                    </td>
                </tr>
	        <tr>
                    <td><fmt:message key="mediator.datamapper.outputSchema"/>
                    </td>
                    <td>
                        <input class="longInput" type="text" value="<%=outputSchemaVal%>"
                               name="outputSchema" id="outputSchema" readonly="true"/>
                    </td>
                    <td>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('outputSchema','/_system/config')"><fmt:message
                                key="mediator.datamapper.conf.registry.browser"/></a>
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('outputSchema','/_system/governance')"><fmt:message
                                key="mediator.datamapper.gov.registry.browser"/></a>
                    </td>
                </tr>
     		<tr>
                <td><fmt:message key="mediator.datamapper.inputType"/></td>
                <td>
                    <select id="mediator.datamapper.inputType" name="mediator.datamapper.inputType">
                        <%
                            System.out.println("dataMapperMediator.getInputType()" + dataMapperMediator.getInputType());
                            if(dataMapperMediator.getInputType() == DataMapperMediator.CSV_VALUE){
                        %>
                        <option value="<%=DataMapperMediator.CSV_VALUE%>" selected="true"><fmt:message key="mediator.datamapper.type.csv"/></option>
                        <option value="<%=DataMapperMediator.XML_VALUE%>"><fmt:message key="mediator.datamapper.type.xml"/></option>
                        <option value="<%=DataMapperMediator.JSON_VALUE%>"><fmt:message key="mediator.datamapper.type.json"/></option>
                        <%
                            } else if(dataMapperMediator.getInputType() == DataMapperMediator.XML_VALUE){
                        %>
                        <option value="<%=DataMapperMediator.CSV_VALUE%>" selected="true"><fmt:message key="mediator.datamapper.type.csv"/></option>
                        <option value="<%=DataMapperMediator.XML_VALUE%>"><fmt:message key="mediator.datamapper.type.xml"/></option>
                        <option value="<%=DataMapperMediator.JSON_VALUE%>"><fmt:message key="mediator.datamapper.type.json"/></option>
                        <%
                            } else if (dataMapperMediator.getInputType() == DataMapperMediator.JSON_VALUE) {
                        %>
                        <option value="<%=DataMapperMediator.CSV_VALUE%>" selected="true"><fmt:message key="mediator.datamapper.type.csv"/></option>
                        <option value="<%=DataMapperMediator.XML_VALUE%>"><fmt:message key="mediator.datamapper.type.xml"/></option>
                        <option value="<%=DataMapperMediator.JSON_VALUE%>"><fmt:message key="mediator.datamapper.type.json"/></option>
                        <%}%>
                    </select>
                </td>
                </tr>
	        <tr>
                <td><fmt:message key="mediator.datamapper.outputType"/></td>
                <td>
                    <select id="mediator.datamapper.outputType" name="mediator.datamapper.outputType">
                        <%
                            System.out.println("dataMapperMediator.getOutputType()" + dataMapperMediator.getOutputType());
                            if(dataMapperMediator.getOutputType() == DataMapperMediator.CSV_VALUE){
                        %>
                        <option value="<%=DataMapperMediator.CSV_VALUE%>" selected="true"><fmt:message key="mediator.datamapper.type.csv"/></option>
                        <option value="<%=DataMapperMediator.XML_VALUE%>"><fmt:message key="mediator.datamapper.type.xml"/></option>
                        <option value="<%=DataMapperMediator.JSON_VALUE%>"><fmt:message key="mediator.datamapper.type.json"/></option>
                        <%
                            } else if(dataMapperMediator.getOutputType() == DataMapperMediator.XML_VALUE){
                        %>
                         <option value="<%=DataMapperMediator.CSV_VALUE%>" selected="true"><fmt:message key="mediator.datamapper.type.csv"/></option>
                        <option value="<%=DataMapperMediator.XML_VALUE%>"><fmt:message key="mediator.datamapper.type.xml"/></option>
                        <option value="<%=DataMapperMediator.JSON_VALUE%>"><fmt:message key="mediator.datamapper.type.json"/></option>
                        <%
                            } else if (dataMapperMediator.getOutputType() == DataMapperMediator.JSON_VALUE) {
                        %>
                        <option value="<%=DataMapperMediator.CSV_VALUE%>" selected="true"><fmt:message key="mediator.datamapper.type.csv"/></option>
                        <option value="<%=DataMapperMediator.XML_VALUE%>"><fmt:message key="mediator.datamapper.type.xml"/></option>
                        <option value="<%=DataMapperMediator.JSON_VALUE%>"><fmt:message key="mediator.datamapper.type.json"/></option>
                        <%}%>
                    </select>
                </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>
