<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.wso2.carbon.mediator.rule.RuleMediator" %>
<%@ page import="org.wso2.carbon.mediator.rule.ui.internal.RuleMediatorClientHelper" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.rule.common.*" %>
<%@ page import="org.wso2.carbon.rule.mediator.config.RuleMediatorConfig" %>
<%@ page import="org.wso2.carbon.rule.mediator.config.Source" %>
<%@ page import="org.wso2.carbon.rule.mediator.config.Target" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformation" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepository" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.rule.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.rule.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="rulejsi18n"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof RuleMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    RuleMediator ruleMediator = (RuleMediator) mediator;
    RuleMediatorConfig ruleMediatorConfig = ruleMediator.getRuleMediatorConfig();
    if (ruleMediatorConfig == null) {
        ruleMediatorConfig = new RuleMediatorConfig();
    }

    String ownerID = SequenceEditorHelper.getEditingMediatorPosition(request.getSession());

    Source source = ruleMediatorConfig.getSource();
    String sourceValue = source.getValue() != null ? source.getValue() : "";
    String sourceXpath = source.getXpath() != null ? source.getXpath() : "";
    Map<String, String> sourceNSMap = source.getPrefixToNamespaceMap();

    if (sourceNSMap != null) {
        NameSpacesInformationRepository repository = (NameSpacesInformationRepository) session.getAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
        if (repository == null) {
            repository = new NameSpacesInformationRepository();
        }
        NameSpacesInformation nameSpacesInformation = new NameSpacesInformation();
        nameSpacesInformation.setNameSpaces(sourceNSMap);
        repository.addNameSpacesInformation(ownerID, "sourceValue", nameSpacesInformation);
        session.setAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);

    }


    Target target = ruleMediatorConfig.getTarget();
    String targetValue = target.getValue() != null ? target.getValue() : "";
    String targetResultXpath = target.getResultXpath() != null ? target.getResultXpath() : "";
    String targetXpath = target.getXpath() != null ? target.getXpath() : "";
    String targetAction = target.getAction() != null ? target.getAction() : "";
    Map<String, String> resultNSMap = target.getPrefixToNamespaceMap();
  //  Map<String, String> targetNSMap = target.getPrefixToTargetNamespaceMap();
    if (resultNSMap != null) {

        NameSpacesInformationRepository repository = (NameSpacesInformationRepository) session.getAttribute(
                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
        if (repository == null) {
            repository = new NameSpacesInformationRepository();
        }
        NameSpacesInformation nameSpacesInformation = new NameSpacesInformation();
        nameSpacesInformation.setNameSpaces(resultNSMap);
        repository.addNameSpacesInformation(ownerID, "resultValue", nameSpacesInformation);
        session.setAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
    }
//    if(targetNSMap != null){
//           NameSpacesInformationRepository repository = (NameSpacesInformationRepository) session.getAttribute(
//                NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
//        if (repository == null) {
//            repository = new NameSpacesInformationRepository();
//        }
//        NameSpacesInformation nameSpacesInformation = new NameSpacesInformation();
//        nameSpacesInformation.setNameSpaces(targetNSMap);
//        repository.addNameSpacesInformation(ownerID, "targetNS", nameSpacesInformation);
//        session.setAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
//
//    }

    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    //Execution Set Metadata
    RuleSet ruleSet = ruleMediatorConfig.getRuleSet();
    if (ruleSet == null) {
        ruleSet = new RuleSet();
    }

    if (ruleSet.getRules().isEmpty()) {
        ruleSet.addRule(new Rule());
    }

    Rule rule = ruleSet.getRules().get(0); //Todo to support many rules

    boolean isInline = (rule.getSourceType() == null)
            || (rule.getSourceType().equals(""))
            || (rule.getSourceType().equals("inline"));
    boolean isURL = (rule.getSourceType() != null) && (rule.getSourceType().equals("url"));

    // if the type is in line rule value is the script value
    // if the type is registry key, then value is regsitry key
    String ruleValue = rule.getValue() != null ? rule.getValue().toString() : "";

    String ruleSourceValue = isInline ? ruleValue : "";
    String ruleRegistryKeyValue = (isInline || isURL) ? "" : ruleValue;
    String ruleURLValue = isURL ? ruleValue : "";

    String ruleType = (rule.getResourceType() != null) ? rule.getResourceType() : "";

    String ruleScriptID = SequenceEditorHelper.getEditingMediatorPosition(session) + "_rulescript";
    Map ruleScriptsMap = (Map) request.getSession().getAttribute("rulemediator_script_map");
    if (ruleScriptsMap == null) {
        ruleScriptsMap = new HashMap();
        request.getSession().setAttribute("rulemediator_script_map", ruleScriptsMap);
    }
    ruleScriptsMap.put(ruleScriptID, ruleSourceValue);


    Map<String, String> creationProperties = ruleSet.getProperties();
    RuleMediatorClientHelper.registerNameSpaces(creationProperties, "creationpropertyValue", session);

//        Collection<PropertyDescription> registrationIterator = setDescription.getRegistrationProperties();
//        RuleMediatorClientHelper.registerNameSpaces(registrationIterator, "registrationpropertyValue", session);
//        Collection<PropertyDescription> deRegistrationIterator = setDescription.getDeregistrationProperties();
//        RuleMediatorClientHelper.registerNameSpaces(deRegistrationIterator, "deregistrationpropertyValue", session);

    //Inputs
    Input input = ruleMediatorConfig.getInput();
    String inputWrapperName = input.getWrapperElementName();
    inputWrapperName = inputWrapperName == null ? "" : inputWrapperName;
    String inputNameSpace = input.getNameSpace();
    inputNameSpace = inputNameSpace == null ? "" : inputNameSpace;
    List<Fact> inputFacts = input.getFacts();


    //Outputs
    Output output = ruleMediatorConfig.getOutput();
    String outputWrapperName = output.getWrapperElementName();
    outputWrapperName = outputWrapperName == null ? "" : outputWrapperName;
    String outputNameSpace = output.getNameSpace();
    outputNameSpace = outputNameSpace == null ? "" : outputNameSpace;
    List<Fact> outputFacts = ruleMediatorConfig.getOutput().getFacts();

    String inputTableStyle = inputFacts.isEmpty() ? "display:none;" : "";
    String outputTableStyle = outputFacts.isEmpty() ? "display:none;" : "";

%>

<div>
<script type="text/javascript" src="../rule-mediator/js/mediator-util.js"></script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="rule.mediator.header"/></h2>
    </td>
</tr>

<tr>
    <td><h3 class="mediator"><fmt:message key="mediator.rule.source"/></h3></td>
</tr>
<tr>

    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.rule.source.value"/>
                    <font style="color: red; font-size: 8pt;">*</font>
                </td>
                <td>
                    <input title="<fmt:message key="mediator.rule.source.value.tip"/>" type="text" id="mediator.rule.source.value"
                           name="mediator.rule.source.value"
                           style="width:300px;" value='<%=sourceValue%>'/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.rule.source.xpath"/>
                </td>
                <td>
                    <input type="text" id="mediator.rule.source.xpath"
                           name="mediator.rule.source.xpath"
                           style="width:300px;" value='<%=sourceXpath%>'/>
                </td>
                <td id="sourceNsEditorButtonTD">
                    <a href="#nsEditorLink" class="nseditor-icon-link"
                       style="padding-left:40px"
                       onclick="showNameSpaceEditor('sourceValue')"><fmt:message
                            key="namespaces"/></a>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr>
    <td><h3 class="mediator"><fmt:message key="mediator.rule.target"/></h3></td>
</tr>
<tr>
    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.rule.source.value"/>
                    <font style="color: #ff0000; font-size: 8pt;">*</font>
                </td>
                <td>
                    <input title="<fmt:message key="mediator.rule.target.value.tip"/>" type="text" id="mediator.rule.target.value"
                           name="mediator.rule.target.value"
                           style="width:300px;" value='<%=targetValue%>'/>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.rule.target.resultXpath"/>
                </td>
                <td>
                    <input type="text" id="mediator.rule.target.resultXpath"
                           name="mediator.rule.target.resultXpath"
                           style="width:300px;" value='<%=targetResultXpath%>'/>
                </td>
                <td id="resultNsEditorButtonTD">
                    <a href="#nsEditorLink" class="nseditor-icon-link"
                       style="padding-left:40px"
                       onclick="showNameSpaceEditor('resultValue')"><fmt:message
                            key="namespaces"/></a>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.rule.target.xpath"/>
                </td>
                <td>
                    <input type="text" id="mediator.rule.target.xpath"
                           name="mediator.rule.target.xpath"
                           style="width:300px;" value='<%=targetXpath%>'/>
                </td>
                          <td id="targetNsEditorButtonTD">
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('resultValue')"><fmt:message
                                key="namespaces"/></a>
                    </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.rule.target.action"/>
                </td>
                <td>
                    <select id="mediator.rule.target.action" name="mediator.rule.target.action">
                        <option id="request" value="replace" <% if ("replace".equals(targetAction)){ %> selected="true" <%} %> >
                            <fmt:message key="mediator.rule.target.replace"/>
                        </option>
                        <option id="application" value="child" <% if ("child".equals(targetAction)){ %> selected="true" <%} %>>
                            <fmt:message key="mediator.rule.target.child"/>
                        </option>
                        <option id="soapSession" value="sibling" <% if ("sibling".equals(targetAction)){ %> selected="true" <%} %>>
                            <fmt:message key="mediator.rule.target.sibling"/>
                        </option>
                    </select>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr>
    <td><h3 class="mediator"><fmt:message key="mediator.rule.executionset"/></h3></td>
</tr>

    <%-- Add RuleSet with radio buttons  --%>
<tr>
    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.rule.rule.script.as"/><span class="required">*</span></td>
                <td>
                    <%
                        if (isInline) {
                    %>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypeinlined"
                           value="inlined"
                           onclick="setRuleScriptType('inlined');"
                           checked="checked"/>
                    <fmt:message key="in-lined"/>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypekey"
                           value="key"
                           onclick="setRuleScriptType('key');"/>
                    <fmt:message key="key"/>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypeurl"
                           value="url"
                           onclick="setRuleScriptType('url');"/>
                    <fmt:message key="reg.url"/>
                    <% } else if (isURL) { %>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypeinlined"
                           value="inlined"
                           onclick="setRuleScriptType('inlined');"/>
                    <fmt:message key="in-lined"/>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypekey"
                           value="key"
                           onclick="setRuleScriptType('key');"/>
                    <fmt:message key="key"/>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypeurl"
                           value="url"
                           onclick="setRuleScriptType('url');"
                           checked="checked"/>
                    <fmt:message key="reg.url"/>

                    <%} else { %>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypeinlined"
                           value="inlined"
                           onclick="setRuleScriptType('inlined');"/>
                    <fmt:message key="in-lined"/>
                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypekey"
                           value="key"
                           onclick="setRuleScriptType('key');"
                           checked="checked"/>
                    <fmt:message key="key"/>

                    <input type="radio" name="ruleScriptType"
                           id="ruleScriptTypeurl"
                           value="url"
                           onclick="setRuleScriptType('url');"/>
                    <fmt:message key="reg.url"/>

                    <%} %>
                </td>
            </tr>
            <tr>
                <td></td>
                <td id="inline_rulescript" style="<%=!isInline?"display:none" : ""%>">
                    <a
                            href="#ruleScriptBrowserLink" class="policie-icon-link"
                            style="padding-left:40px"
                            onclick="showInLinedRuleScriptPolicyEditor('<%=ruleScriptID%>');"><fmt:message
                            key="ruleScript.policy.editor"/></a>
                </td>
                <td id="url_rulescript" style="<%=!isURL ? "display:none;" : ""%>" >
                    <input type="text" class="longInput" id="mediator.rule.url"
                           name="mediator.rule.url" value="<%=ruleURLValue%>"/>
                </td>

                <td id="regkey_rulescript" style="<%=(isInline || isURL) ? "display:none;" : ""%>">
                    <input type="text" class="longInput" id="mediator.rule.key"
                           name="mediator.rule.key" value="<%=ruleRegistryKeyValue%>"
                           readonly="true"/>
                </td>
                <td id="regbrowser_rulescript"
                    style="<%=(isInline || isURL) ? "display:none;" : ""%>">
                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                       style="padding-left:20px;padding-left:20px"
                       onclick="showRegistryBrowser('mediator.rule.key','/_system/config');"><fmt:message
                            key="registry.conf.keys"/></a>
                    <a href="#registryBrowserLink" class="registry-picker-icon-link"
                       style="padding-left:20px"
                       onclick="showRegistryBrowser('mediator.rule.key','/_system/governance');"><fmt:message
                            key="registry.gov.keys"/></a>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="mediator.rule.type"/></td>
                <td>
                    <select id="rule.script.type.id" name="rule.script.type">
                        <option value="regular" <% if ("regular".equals(ruleType)){ %> selected="true" <%} %> ><fmt:message key="mediator.rule.type.regular"/></option>
                        <option value="dtable" <% if ("dtable".equals(ruleType)){ %> selected="true" <%} %>><fmt:message key="mediator.rule.type.dtable"/></option>
                    </select>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <a name="ruleScriptBrowserLink"></a>

                    <div id="ruleScriptBrowser" style="display:none;"></div>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr>
    <td><h3 class="mediator"><fmt:message key="mediator.rule.inputs"/></h3></td>
</tr>
<tr>
    <td class="formRaw">
        <table class="normal">
            <tr>
                <td><fmt:message key="wrapper"/>
                </td>

                <td>
                    <input type="text" name="inputWrapperName" id="inputWrapperName"
                           value="<%=inputWrapperName%>"/>
                </td>
                <td><fmt:message key="namespace"/>
                </td>
                <td>
                    <input type="text" name="inputNameSpace" id="inputNameSpace"
                           value="<%=inputNameSpace%>"/>
                </td>
            </tr>
        </table>
    </td>


</tr>
<tr>
    <td>
        <table id="facttable" class="styledInner" style="<%=inputTableStyle%>;">
            <thead>
            <tr>
                <th width="10%"><fmt:message key="th.parameter.type"/></th>
                    <%--<th width="10%"><fmt:message key="th.fact.selector"/></th>--%>
                <th width="10%"><fmt:message key="th.parameter.elementName"/></th>
                <th width="10%"><fmt:message key="th.parameter.namespace"/></th>
                <th width="10%"><fmt:message key="th.parameter.xpath"/></th>
                <th id="ns-edior-th"><fmt:message key="namespaceeditor"/></th>
                <th><fmt:message key="th.action"/></th>
            </tr>
            <tbody id="facttbody">
            <%
                int k = 0;
                for (Fact fact : inputFacts) {
                    if (fact != null) {
                        String factType = fact.getType() != null ? fact.getType() : "";
                        String factElementName = fact.getElementName() != null ? fact.getElementName() : "";
                        String factNamespace = fact.getNamespace() != null ? fact.getNamespace() : "";
                        String factXpath = fact.getXpath() != null ? fact.getXpath() : "";
                        Map<String, String> factNSMap = fact.getPrefixToNamespaceMap();
                        if (factNSMap != null) {
                            NameSpacesInformationRepository repository = (NameSpacesInformationRepository) session.getAttribute(
                                    NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
                            if (repository == null) {
                                repository = new NameSpacesInformationRepository();
                            }
                            NameSpacesInformation nameSpacesInformation = new NameSpacesInformation();
                            nameSpacesInformation.setNameSpaces(factNSMap);
                            repository.addNameSpacesInformation(ownerID, "factValue" + k, nameSpacesInformation);
                            session.setAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
                        }

            %>
            <tr id="factRaw<%=k%>">
                <td>
                    <input name="factType<%=k%>"
                           id="factType<%=k%>" value="<%=factType%>"
                           type="text"/>
                </td>
                    <%--              <td>
                        <a class="fact-selector-icon-link" href="#factEditorLink"
                           style="padding-left:40px"
                           onclick="showFactEditor('fact','<%=k%>')"><fmt:message
                                key="fact.type"/></a>
                    </td>--%>
                <td>
                    <input name="factElementName<%=k%>"
                           id="factElementName<%=k%>" value="<%=factElementName%>"
                           type="text"/>
                </td>

                <td>
                    <input name="factNamespace<%=k%>"
                           id="factNamespace<%=k%>" value="<%=factNamespace%>"
                           type="text"/>
                </td>

                <td>
                    <input name="factXpath<%=k%>"
                           id="factXpath<%=k%>" value="<%=factXpath%>"
                           type="text"/>
                </td>

                <td id="factNsEditorButtonTD<%=k%>">
                    <a href="#nsEditorLink" class="nseditor-icon-link"
                       style="padding-left:40px"
                       onclick="showNameSpaceEditor('factValue<%=k%>')"><fmt:message
                            key="namespaces"/></a>
                </td>

                <td><a href="#" href="#" class="delete-icon-link" style="padding-left:40px"
                       onclick="deleteFact('fact','<%=k%>')"><fmt:message
                        key="delete"/></a></td>
            </tr>
            <% }
                k++;
            } %>
            <input type="hidden" name="factCount" id="factCount"
                   value="<%=k%>"/>
            </tbody>
        </table>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addfactLink"></a>
            <a class="add-icon-link"
               href="#addfactLink"
               onclick="addFact('fact')">
                <fmt:message key="add.fact"/></a>
        </div>
    </td>
</tr>
<tr>
    <td><h3 class="mediator"><fmt:message key="mediator.rule.outputs"/></h3></td>
</tr>
<tr>
    <td class="formRaw">
        <table class="normal">
            <tr>
                <td><fmt:message key="wrapper"/>
                </td>

                <td>
                    <input type="text" name="outputWrapperName" id="outputWrapperName"
                           value="<%=outputWrapperName%>"/>
                </td>
                <td><fmt:message key="namespace"/>
                </td>
                <td>
                    <input type="text" name="outputNameSpace" id="outputNameSpace"
                           value="<%=outputNameSpace%>"/>
                </td>
            </tr>
        </table>
    </td>


</tr>
<tr>
    <td>
        <table id="resulttable" class="styledInner" style="<%=outputTableStyle%>;">
            <thead>
            <tr>
                <th width="10%"><fmt:message key="th.parameter.type"/></th>
                    <%--<th width="10%"><fmt:message key="th.result.selector"/></th>--%>
                <th width="10%"><fmt:message key="th.parameter.elementName"/></th>
                <th width="10%"><fmt:message key="th.parameter.namespace"/></th>
                    <%--<th width="10%"><fmt:message key="th.parameter.xpath"/></th>--%>
                    <%--<th id="resultns-edior-th"><fmt:message key="namespaceeditor"/></th>--%>
                <th><fmt:message key="th.action"/></th>
            </tr>
            <tbody id="resulttbody">
            <%
                int j = 0;
                for (Fact result : outputFacts) {
                    if (result != null) {
                        String resultType = result.getType();
                        String resultElementName = result.getElementName() != null ? result.getElementName() : "";
                        String resultNamespace = result.getNamespace() != null ? result.getNamespace() : "";
                        String resultXpath = result.getXpath() != null ? result.getXpath() : "";

            %>
            <tr id="resultRaw<%=j%>">
                <td>
                    <input name="resultType<%=j%>"
                           id="resultType<%=j%>" value="<%=resultType%>"
                           type="text"/>
                </td>
                    <%--         <td>
                        <a class="fact-selector-icon-link" href="#factEditorLink"
                           style="padding-left:40px"
                           onclick="showFactEditor('result','<%=j%>')"><fmt:message
                                key="result.type"/></a>
                    </td>--%>

                <td>
                    <input name="resultElementName<%=j%>"
                           id="resultElementName<%=j%>" value="<%=resultElementName%>"
                           type="text"/>
                </td>

                <td>
                    <input name="resultNamespace<%=j%>"
                           id="resultNamespace<%=j%>" value="<%=resultNamespace%>"
                           type="text"/>
                </td>

                    <%--<td>--%>
                    <%--<input name="resultXpath<%=j%>"--%>
                    <%--id="resultXpath<%=j%>" value="<%=resultXpath%>"--%>
                    <%--type="text"/>--%>
                    <%--</td>--%>

                    <%--<td id="resultNsEditorButtonTD<%=j%>">--%>
                    <%--<a href="#nsEditorLink" class="nseditor-icon-link"--%>
                    <%--style="padding-left:40px"--%>
                    <%--onclick="showNameSpaceEditor('resultValue<%=j%>')"><fmt:message--%>
                    <%--key="namespaces"/></a>--%>
                    <%--</td>--%>

                <td><a href="#" href="#" class="delete-icon-link" style="padding-left:40px"
                       onclick="deleteFact('result','<%=j%>')"><fmt:message
                        key="delete"/></a></td>
            </tr>
            <% }
                j++;
            } %>
            <input type="hidden" name="resultCount" id="resultCount"
                   value="<%=j%>"/>
            </tbody>
        </table>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addresultLink"></a>
            <a class="add-icon-link"
               href="#addresultLink"
               onclick="addFact('result')">
                <fmt:message key="add.fact"/></a>
        </div>
    </td>
</tr>
</table>

<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>

<a name="factEditorLink"></a>

<div id="factEditor" style="display:none;"></div>

<a name="registryBrowserLink"></a>

<div id="registryBrowser" style="display:none;"></div>
</div>
</fmt:bundle>
