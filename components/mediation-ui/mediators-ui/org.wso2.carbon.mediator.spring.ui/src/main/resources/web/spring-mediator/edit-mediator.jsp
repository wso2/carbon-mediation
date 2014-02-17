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

<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.spring.SpringMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SpringMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SpringMediator springMediator = (SpringMediator) mediator;
    NameSpacesRegistrar namespaceRegister = NameSpacesRegistrar.getInstance();
    // bean
    String beanName = "";
    if (springMediator.getBeanName() != null) {
        beanName = springMediator.getBeanName();
    }
    // key
    String keyName = "";
    SynapseXPath sourceXpath = null;
    if (springMediator.getConfigKey() != null) {
        keyName = springMediator.getConfigKey();
    }

%>

<fmt:bundle basename="org.wso2.carbon.mediator.spring.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.spring.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="springi18n"/>
    <div>
        <script type="text/javascript" src="../spring-mediator/js/mediator-util.js"></script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.spring.header"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                        <tr>
                            <td><fmt:message key="mediator.spring.bean"/> <span class="required">*</span></td>
                            <td>
                                <input class="longInput" type="text" value="<%=beanName%>"
                                       name="beanName" id="beanName"/>

                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="mediator.spring.key"/> <span class="required">*</span></td>
                            <td>
                                <input class="longInput" type="text" value="<%=keyName%>"
                                       name="beanKey" id="beanKey" readonly="true"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"                                   
                                   onclick="showRegistryBrowser('beanKey','/_system/config')"><fmt:message
                                        key="conf.registry.browser"/></a>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showRegistryBrowser('beanKey','/_system/governance')"><fmt:message
                                        key="gov.registry.browser"/></a>                                
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </div>
</fmt:bundle>