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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.mediator.validate.ValidateMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jaxen.JaxenException" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%

    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ValidateMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ValidateMediator validateMediator = (ValidateMediator) mediator;
    // schemakeys
    List<Value> keys = validateMediator.getSchemaKeys();

    // source
    SynapseXPath sourceXpth = validateMediator.getSource();
    String source = "";
    NameSpacesRegistrar namespacRegister = NameSpacesRegistrar.getInstance();
    if (sourceXpth != null) {
        namespacRegister.registerNameSpaces(sourceXpth, "mediator.validate.source", session);
        source = sourceXpth.toString();
    }

    // feature list
    List<MediatorProperty> featureList = validateMediator.getFeatures();
    String featureTableStyle = featureList.isEmpty() ? "display:none;" : "";
	Map<String, String> resources = validateMediator.getResources();
    int nKeys = 1;
%>

<fmt:bundle basename="org.wso2.carbon.mediator.validate.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.validate.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="validate18n"/>
    <div>
        <script type="text/javascript" src="../validate-mediator/js/mediator-util.js"></script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.validate.header"/></h2>
                </td>
            </tr>
			<tr>
                <td>
                    <h4><fmt:message key="mediator.validate.fault.note"/></h4>
                </td>
            </tr>
            <tr>
                <td>
                    <h3 class="mediator"><fmt:message key="mediator.validate.keys"/></h3>

                    <div style="margin-top:0px;">
                        <table id="keyTable" class="styledInner">
                            <thead>

                            <tbody id="keyTableBody">

                            <%
                                int i = 0;
                                boolean isStaticKey = true;
                                String showValue = "";
                                SynapseXPath synapseXPath = null;

                                if (!keys.isEmpty()) {

                                    for (Value schemaKey : keys) {

                                        if (schemaKey != null) {
                                            showValue = schemaKey.getKeyValue();
                                            synapseXPath = schemaKey.getExpression();

                                            isStaticKey = showValue != null && !"".equals(showValue);

                                            // If dynamic key : register namespace
                                            if (!isStaticKey && synapseXPath != null) {
                                                namespacRegister.registerNameSpaces(schemaKey.getExpression(), ("keyValue" + i), session);

                                            }
                            %>

                            <tr id="keyRaw<%=i%>">
                                <td>
                                    <select name="keyTypeSelection<%=i%>"
                                            id="keyTypeSelection<%=i%>"
                                            onchange="onKeyTypeSelectionChange('<%=i%>','<fmt:message key="mediator.validator.namespaces"/>')">

                                        <% if (isStaticKey) {%>

                                        <option value="static">
                                            <fmt:message key="mediator.validator.static.key"/>
                                        </option>
                                        <option value="dynamic">
                                            <fmt:message key="mediator.validator.dynamic.key"/>
                                        </option>

                                        <%} else {%>

                                        <option value="dynamic">
                                            <fmt:message key="mediator.validator.dynamic.key"/>
                                        </option>
                                        <option value="static">
                                            <fmt:message key="mediator.validator.static.key"/>
                                        </option>

                                        <% }%>

                                    </select>
                                </td>

                                <td>
                                    <% if (isStaticKey) {%>
                                    <input id="keyValue<%=i%>" name="keyValue<%=i%>" type="text"
                                           value="<%=showValue%>" readonly="true"
                                            />
                                    <%} else {%>
                                    <input id="keyValue<%=i%>" name="keyValue<%=i%>" type="text"
                                           value="<%=synapseXPath.toString()%>"/>
                                    <%} %>
                                </td>

                                <td id="nsEditorButtonTD<%=i%>" style="<%=isStaticKey? "display:none;" : ""%>">
                                    <% if (!isStaticKey && synapseXPath != null) {%>
                                    <a href="#nsEditorLink" class="nseditor-icon-link"
                                       style="padding-left:40px"
                                       onclick="showNameSpaceEditor('keyValue<%=i%>')"><fmt:message
                                            key="mediator.validator.namespaces"/></a>
                                    <%}%>
                                </td>

                                <td id="regBrowserTD<%=i%>" style="<%=!isStaticKey? "display:none;" : ""%>">
                                    <% if (isStaticKey && (synapseXPath == null || synapseXPath.toString() == "")) {%>
                                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('keyValue<%=i%>','/_system/config')"><fmt:message
                                            key="conf.registry.keys"/></a>
                                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('keyValue<%=i%>','/_system/governance')"><fmt:message
                                            key="gov.registry.keys"/></a>
                                    <%}%>
                                </td>

                                <td>
                                <a href="#" class="delete-icon-link"
                                       onclick="deleteKey('<%=i%>');return false;"><fmt:message
                                        key="mediator.validator.delete"/></a>                                    
                                </td>
                                
                            </tr>
                            <% }

                                i++;

                            }// end for

                            } else {  // if keys.isEmpty()

                            %>

                            <tr id="keyRaw<%=i%>">

                                <td>
                                    <select name="keyTypeSelection<%=i%>"
                                            id="keyTypeSelection<%=i%>"
                                            onchange="onKeyTypeSelectionChange('<%=i%>','<fmt:message key="mediator.validator.namespaces"/>')">

                                        <% if (isStaticKey) {%>

                                        <option value="static">
                                            <fmt:message key="mediator.validator.static.key"/>
                                        </option>
                                        <option value="dynamic">
                                            <fmt:message key="mediator.validator.dynamic.key"/>
                                        </option>

                                        <%} else {%>

                                        <option value="dynamic">
                                            <fmt:message key="mediator.validator.dynamic.key"/>
                                        </option>
                                        <option value="static">
                                            <fmt:message key="mediator.validator.static.key"/>
                                        </option>

                                        <% }%>

                                    </select>
                                </td>

                                <td>
                                    <% if (isStaticKey) {%>
                                    <input id="keyValue<%=i%>" name="keyValue<%=i%>" type="text"
                                           value="<%=showValue%>" readonly="true"
                                            />
                                    <%} else {%>
                                    <input id="keyValue<%=i%>" name="keyValue<%=i%>" type="text"
                                           value="<%=synapseXPath.toString()%>"/>
                                    <%} %>
                                </td>

                                <td id="nsEditorButtonTD<%=i%>" style="<%=isStaticKey? "display:none;" : ""%>">
                                    <% if (!isStaticKey && synapseXPath != null) {%>
                                    <a href="#nsEditorLink" class="nseditor-icon-link"
                                       style="padding-left:40px"
                                       onclick="showNameSpaceEditor('keyValue<%=i%>')"><fmt:message
                                            key="mediator.validator.namespaces"/></a>
                                    <%}%>
                                </td>

                                <td id="regBrowserTD<%=i%>" style="<%=!isStaticKey? "display:none;" : ""%>">
                                    <% if (isStaticKey && (synapseXPath == null || synapseXPath.toString() == "")) {%>
                                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('keyValue<%=i%>','/_system/config')"><fmt:message
                                            key="conf.registry.keys"/></a>
                                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('keyValue<%=i%>','/_system/governance')"><fmt:message
                                            key="gov.registry.keys"/></a>
                                    <%}%>
                                </td>


                                <td>
                                    <a href="#" class="delete-icon-link"
                                       onclick="deleteKey('<%=i%>');return false;"><fmt:message
                                        key="mediator.validator.delete"/></a></td>
                            </tr>

                            <%
                                    i++;
                                } // end else
                            %>
                            
                            <input type="hidden" name="keyCount" id="keyCount" value="<%=i%>"/>
                            <input type="hidden" name="nKeys" id="nKeys" value="<%=nKeys%>"/>
                            </tbody>
                            </thead>
                        </table>
                    </div>
                </td>
            </tr>

            <tr>
                <td>
                    <div style="margin-top:0px;">
                        <a name="addNameLink"></a>
                        <a class="add-icon-link"
                           href="#addNameLink"
                           onclick="addNewKey('<fmt:message key="mediator.validator.keyemptyerror"/>')"><fmt:message
                                key="mediator.validator.add.key"/><span class="required">*</span></a>
                    </div>
                </td>
            </tr>


            <tr>
                <td>
                    <table class="normal">
                        <thead>
                        <tbody>
                        <tr>
                            <td>
                                <fmt:message key="mediator.validate.source"/>
                            </td>
                            <td>
                                <input class="longInput" type="text" id="mediator.validate.source"
                                       name="mediator.validate.source"
                                       value="<%=source%>"/>
                            </td>
                            <td>
                                <a href="#nsEditorLink" class="nseditor-icon-link"
                                   style="padding-left:40px"
                                   onclick="showNameSpaceEditor('mediator.validate.source')"><fmt:message
                                        key="mediator.validate.namespace"/></a>
                            </td>
                        </tr>
                        </tbody>
                        </thead>
                    </table>
                </td>
            </tr>

            <tr>
                <td>
                    <h3 class="mediator"><fmt:message key="mediator.validator.features"/></h3>

                    <div style="margin-top:0px;">
                        <table id="featuretable" style="<%=featureTableStyle%>;" class="styledInner">
                            <thead>
                            <tr>
                                <th width="15%"><fmt:message key="mediator.validator.feature.name"/></th>
                                <th width="10%"><fmt:message key="mediator.validator.feature.value"/></th>
                                <th><fmt:message key="mediator.validator.action"/></th>
                            </tr>
                            <tbody id="featuretbody">
                            <%
                                int l = 0;
                                for (MediatorProperty property : featureList) {
                                    if (property != null) {
                                        String value = property.getValue();
                                        boolean isTrue = value != null && Boolean.valueOf(value.trim());
                            %>
                            <tr id="featureRaw<%=l%>">
                                <td><input type="text" name="featureName<%=l%>" id="featureName<%=l%>"
                                           value="<%=property.getName()%>"/>
                                </td>
                                <td>
                                    <select name="featureValue<%=l%>" id="featureValue<%=l%>">
                                        <% if (!isTrue) {%>
                                        <option value="false" selected="selected">
                                            <fmt:message key="mediator.validator.false"/>
                                        </option>
                                        <option value="true">
                                            <fmt:message key="mediator.validator.true"/>
                                        </option>
                                        <%} else { %>
                                        <option value="true" selected="selected">
                                            <fmt:message key="mediator.validator.true"/>
                                        </option>
                                        <option value="false">
                                            <fmt:message key="mediator.validator.false"/>
                                        </option>
                                        <% }%>
                                    </select>
                                </td>
                                <td><a href="#" href="#" class="delete-icon-link" 
                                       onclick="deletefeature('<%=l%>');return false;"><fmt:message
                                        key="mediator.validator.delete"/></a>
                                </td>
                            </tr>
                            <%
                                    }
                                    l++;
                                }
                            %>
                            <input type="hidden" name="featureCount" id="featureCount" value="<%=l%>"/>
                            </tbody>
                            </thead>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div style="margin-top:0px;">
                        <a name="addFeatureLink"></a>
                        <a class="add-icon-link"
                           href="#addFeatureLink"
                           onclick="addfeature('<fmt:message key="mediator.validator.emptyerror"/>')">
                            <fmt:message key="mediator.validator.addfeature"/></a>
                    </div>
                </td>
            </tr>
			 <tr id="resourceTr" >
			<td><input type="hidden" id="resourceList" name="resourceList" />
			<h3 class="mediator"><fmt:message key="mediator.validator.resources" /></h3>	
				<div id="resourceAdd">
					<table class="normal-nopadding" cellspacing="0">
						<tr>
							<td class="nopadding">
							<table>
								<tr>
									<td class="nopadding"><fmt:message
										key="mediator.validator.resource.location" /> <input type="text"
										id="locationText"  name="locationText" /></td>
									<td class="nopadding"><fmt:message
										key="mediator.validator.resource.key" /> <input type="text"
									 	 id="resourceKey"	name="resourceKey" /></td>
									<td class="nopadding" style="padding-top: 10px !important">
									<a href="#" class="registry-picker-icon-link"
										onclick="showRegistryBrowser('resourceKey','/_system/config');"><fmt:message
										key="conf.registry.keys" /></a></td>
									<td class="nopadding" style="padding-top: 10px !important">
									<a href="#" class="registry-picker-icon-link"
										onclick="showRegistryBrowser('resourceKey','/_system/governance');"><fmt:message
										key="gov.registry.keys" /></a></td>
								</tr>
							</table>
							</td>
						</tr>
						<tr>
							<td class="nopadding"><a class="icon-link"
								href="#addNameLink" onclick="addResources();"
								style="background-image: url(../admin/images/add.gif);"><fmt:message
								key="mediator.validator.resource.add" /> </a></td>
						</tr>
					</table>
					</div>
					<div>
						<table cellpadding="0" cellspacing="0" border="0"
						class="styledLeft" id="resourceTable" style="display: none;">
						<thead>
							<tr>
								<th style="width: 40%"><fmt:message key="mediator.validator.resource.location" /></th>
								<th style="width: 40%"><fmt:message key="mediator.validator.resource.key" /></th>
								<th style="width: 20%"><fmt:message key="mediator.validator.resource.action" /></th>
							</tr>
						</thead>
						<tbody/>
					<%
					Iterator itr = resources.keySet().iterator();
					if(itr.hasNext()){
						%>
							<table cellpadding="0" cellspacing="0" border="0"
						class="styledLeft" id="resourceTable2" >
						<thead>
							<tr>
								<th style="width: 40%"><fmt:message key="mediator.validator.resource.location" /></th>
								<th style="width: 40%"><fmt:message key="mediator.validator.resource.key" /></th>								
							</tr>
						</thead>
						<%
						for( itr = resources.keySet().iterator(); itr.hasNext();){
							int j=0;
						String location = (String)itr.next(); 
						if(location==null){
							location="";
						}
						
						String regKey = resources.get(location);
						if(regKey==null){
							regKey="";
						}
						
						%>						
						<tbody>
						<tr>
						<td class="nopadding"><%= location%></td>
						<td class="nopadding"><%= regKey%></td>					
						</tr>
						</tbody>				
						<%
						j++;
						}
					}					
					%>				
					</table>
					</div>				
			</td>
		</tr>
        </table>
        <a name="nsEditorLink"></a>

        <div id="nsEditor" style="display:none;"></div>

        <a name="registryBrowserLink"></a>

        <div id="registryBrowser" style="display:none;"></div>


    </div>
</fmt:bundle>
