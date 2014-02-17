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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon"%>
<%@ page import="org.wso2.carbon.mediator.cache.CacheMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CacheMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CacheMediator cacheMediator = (CacheMediator) mediator;

%>

<fmt:bundle basename="org.wso2.carbon.mediator.cache.ui.i18n.Resources">
<carbon:jsi18n resourceBundle="org.wso2.carbon.mediator.cache.ui.i18n.JSResources" request="<%=request%>" i18nObjectName="cachejsi18n" />
<div>
    <script type="text/javascript" src="../cache-mediator/js/mediator-util.js"></script>
    
    <table class="normal" width="100%">
	<tr>
	    <td>
	        <h2><fmt:message key="mediator.cache.header"/></h2>
	    </td>
	</tr>
	<tr>
	<td>
        <table class="normal">
            <tbody>
                <%if (cacheMediator.getId() != null) {%>
                <tr>
                    <td class="leftCol-small"><fmt:message key="mediator.cache.id"/></td>
                    <td><input type="text" name="cacheId" value="<%=cacheMediator.getId()%>"
                               size="40"/></td>
                </tr>
                <%} else {%>
                <tr>
                    <td class="leftCol-small"><fmt:message key="mediator.cache.id"/></td>
                    <td><input type="text" name="cacheId" value="" size="40"/></td>
                </tr>
                <%}%>
                <%
                    if (cacheMediator.getScope() != null &&
                            (cacheMediator.getScope().equalsIgnoreCase("Per-Mediator"))) {
                %>
                <tr>
                    <td><fmt:message key="mediator.cache.scope"/></td>
                    <td><select name="cacheScope">
                        <option value="per-mediator">Per-Mediator</option>
                        <option value="per-host">Per-Host</option>
                    </select></td>
                </tr>
                <%
                } else if (cacheMediator.getScope() != null &&
                        (cacheMediator.getScope().equalsIgnoreCase("Per-Host"))) {
                %>
                <tr>
                    <td><fmt:message key="mediator.cache.scope"/></td>
                    <td><select name="cacheScope">
                        <option value="per-host">Per-Host</option>
                        <option value="Per-Mediator">Per-Mediator</option>
                    </select></td>
                </tr>
                <%} else {%>
                <tr>
                    <td><fmt:message key="mediator.cache.scope"/></td>
                    <td><select name="cacheScope">
                        <option value="per-mediator">Per-Mediator</option>
                        <option value="Per-Host">Per-Host</option>
                    </select></td>
                </tr>
                <%}%>

                <%if (cacheMediator.isCollector()) {%>
                <tr>
                    <td><fmt:message key="mediator.cache.type"/></td>
                    <td><select name="cacheType" onchange="collectorSelector(this)">
                        <option value="Collector">Collector</option>
                        <option value="Finder">Finder</option>
                    </select></td>
                </tr>
                <%} else {%>
                <tr>
                    <td><fmt:message key="mediator.cache.type"/></td>
                    <td><select name="cacheType" onchange="collectorSelector(this)">
                        <option value="Finder">Finder</option>
                        <option value="Collector">Collector</option>
                    </select></td>
                </tr>
                <%}%>

               
                
                <tr>
                    <td><div id="hasGen"><fmt:message key="mediator.cache.hashgenerator"/></div></td>
                    <td><div id="hasGehVal"><input type="text" name="hashGen"
                               value="org.wso2.caching.digest.DOMHASHGenerator" readonly="readonly"
                               size="40"/></div></td>
                </tr>
                <%if (cacheMediator.getTimeout() > 0) {%>
                <tr>
                    <td><div id="timeout"><fmt:message key="mediator.cache.timeoutseconds"/></div></td>
                    <td><div id="timeoutVal"><input type="text" name="cacheTimeout"
                               value="<%=cacheMediator.getTimeout()%>" size="40"/></div></td>
                </tr>
                <%} else {%>
                <tr>
                    <td><div id="timeout"><fmt:message key="mediator.cache.timeoutseconds"/></div></td>
                    <td><div id="timeoutVal"><input type="text" name="cacheTimeout" value="" size="40"/></div></td>
                </tr>
                <%}%>
                <%if (cacheMediator.getMaxMessageSize() > 0) {%>
                <tr>
                    <td><div id="msgSize"><fmt:message key="mediator.cache.maxmessage"/></div></td>
                    <td><div id="msgSizeVal"><input type="text" name="maxMsgSize"
                               value="<%=cacheMediator.getMaxMessageSize()%>" size="40"/></div></td>
                </tr>
                <%} else {%>
                <tr>
                    <td><div id="msgSize"><fmt:message key="mediator.cache.maxmessage"/></div></td>
                    <td><div id="msgSizeVal"><input type="text" name="maxMsgSize" value="" size="40"/></div></td>
                </tr>
                <%}%>

            </tbody>
        </table>
    </td>
    </tr>
    <tr>
        <td>
            <div id="hideCacheDetails">
            <h3 class="mediator"><fmt:message key="mediator.cache.cashdetails"/></h3>
            <table class="normal">

                <tr>
                    <td class="leftCol-small"><fmt:message
                            key="mediator.cache.implementationType"/></td>
                    <td><select name="impType">
                        <option value="In-Memory">In-Memory</option>
                    </select></td>
                </tr>
                <%if (cacheMediator.getInMemoryCacheSize() > 0) {%>
                <tr>
                    <td><fmt:message key="mediator.cache.maxSize"/></td>
                    <td><input type="text" name="maxSize"
                               value="<%=cacheMediator.getInMemoryCacheSize()%>" size="40"/></td>
                </tr>
                <%} else {%>
                <tr>
                    <td><fmt:message key="mediator.cache.maxSize"/></td>
                    <td><input type="text" name="maxSize" value="" size="40"/></td>
                </tr>
                <%}%>

            </table>
             </div>
        </td>
    </tr>
    <tr>
        <td>
              <div id="hideCachHit">
            <h3 class="mediator"><fmt:message key="mediator.cache.cacheHit"/></h3>
            <table class="normal">

                <tr>
                    <td>
                        <input type="radio" id="sequenceOptionAnon" name="sequenceOption" value="annon" onclick="anonSelected()"/><fmt:message
                            key="mediator.cache.annon"/></td>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td>
                        <input type="radio" id="sequenceOptionReference" name="sequenceOption" value="selectFromRegistry" onclick="registrySelected()"/><fmt:message
                            key="mediator.cache.sequenceRef"/>
                    </td>
                    <td>
                        <div id="mediator.sequence.txt.div">
                        <input type="text" id="mediator.sequence" name="mediator.sequence" readonly="readonly"/>
                        </div>
                    </td>
                    <td>
                        <div id="mediator.sequence.link.div">
                        <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"                           
                           onclick="showRegistryBrowser('mediator.sequence','/_system/config')"><fmt:message
                                key="mediator.cache.conf.registry.browser"/></a>
                            <a href="#registryBrowserLink"
                           class="registry-picker-icon-link"
                           onclick="showRegistryBrowser('mediator.sequence','/_system/governance')"><fmt:message
                                key="mediator.cache.gov.registry.browser"/></a>
                        </div>
                    </td>
                </tr>

            </table>
                  </div>
        </td>
    </tr>
    </table>

<%if(cacheMediator.isCollector()){%>
    <script type="text/javascript">
        hideDivs();
    </script>
<%}%>
  
<script type="text/javascript">
    <%
    String ref = cacheMediator.getOnCacheHitRef();if (ref == null) {
    %>
    document.getElementById("sequenceOptionAnon").checked = true;
    anonSelected();
    <%

    } else {
   %>
    document.getElementById("sequenceOptionReference").checked = true;
    document.getElementById("mediator.sequence").value = "<%=ref%>";
    registrySelected();
    <%
    }
    %>

    function anonSelected() {
        document.getElementById("mediator.sequence.txt.div").style.display = "none";
        document.getElementById("mediator.sequence.link.div").style.display = "none";
    }

    function registrySelected() {
        document.getElementById("mediator.sequence.txt.div").style.display = "";
        document.getElementById("mediator.sequence.link.div").style.display = "";
    }
</script>

  <a name="registryBrowserLink"></a>

  <div id="registryBrowser" style="display:none;"></div>
</div>
</fmt:bundle>