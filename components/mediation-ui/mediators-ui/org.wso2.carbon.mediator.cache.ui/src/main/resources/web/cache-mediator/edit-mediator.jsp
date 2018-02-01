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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.mediator.cache.ui.CacheMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp" />

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CacheMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CacheMediator cacheMediator = (CacheMediator) mediator;

%>

<fmt:bundle basename="org.wso2.carbon.mediator.cache.ui.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.mediator.cache.ui.i18n.JSResources" request="<%=request%>"
                   i18nObjectName="cachejsi18n" />
    <div>
        <script type="text/javascript" src="../cache-mediator/js/mediator-util.js"></script>

        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="mediator.cache.header" /></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                        <%if (cacheMediator.isCollector()) {%>
                        <tr>
                            <td><fmt:message key="mediator.cache.type" /><font
                                    style="color: red; font-size: 8pt;"> *</font></td>
                            <td><select name="cacheType" onchange="collectorSelector(this)">
                                <option value="Collector">Collector</option>
                                <option value="Finder">Finder</option>
                            </select></td>
                        </tr>
                        <%} else {%>
                        <tr>
                            <td><fmt:message key="mediator.cache.type" /><font
                                    style="color: red; font-size: 8pt;"> *</font></td>
                            <td><select name="cacheType" onchange="collectorSelector(this)">
                                <option value="Finder">Finder</option>
                                <option value="Collector">Collector</option>
                            </select></td>
                        </tr>
                        <%}%>

                        <%if (cacheMediator.getTimeout() > 0) {%>
                        <tr>
                            <td>
                                <div id="timeoutName"><fmt:message key="mediator.cache.timeoutseconds" /></div>
                            </td>
                            <td>
                                <div id="timeout"><input type="text" name="cacheTimeout"
                                                         value="<%=cacheMediator.getTimeout()%>" size="40" /></div>
                            </td>
                        </tr>
                        <%} else {%>
                        <tr>
                            <td>
                                <div id="timeoutVal"><fmt:message key="mediator.cache.timeoutseconds" /></div>
                            </td>
                            <td>
                                <div id="timeout"><input type="text" name="cacheTimeout" value="" size="40" /></div>
                            </td>
                        </tr>
                        <%}%>
                        <%if (cacheMediator.getMaxMessageSize() > 0) {%>
                        <tr>
                            <td>
                                <div id="msgSize"><fmt:message key="mediator.cache.maxmessage" /></div>
                            </td>
                            <td>
                                <div id="msgSizeVal"><input type="text" name="maxMsgSize"
                                                            value="<%=cacheMediator.getMaxMessageSize()%>" size="40" />
                                </div>
                            </td>
                        </tr>
                        <%} else {%>
                        <tr>
                            <td>
                                <div id="msgSize"><fmt:message key="mediator.cache.maxmessage" /></div>
                            </td>
                            <td>
                                <div id="msgSizeVal"><input type="text" name="maxMsgSize" value="" size="40" /></div>
                            </td>
                        </tr>
                        <%}%>

                        </tbody>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="protocolDetails">
                        <h3 class="mediator"><fmt:message key="mediator.cache.protocoldetails" /></h3>
                        <table class="normal">
                            <tr>
                                <td class="leftCol-med"><fmt:message
                                        key="mediator.cache.protocol" /></td>
                                <td><select name="protocolType">
                                    <option value="HTTP">HTTP</option>
                                </select></td>
                            </tr>
                            <%if (cacheMediator.getHTTPMethodsToCache() != null) {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.httpmethods" /></td>
                                <td><input type="text" name="methods"
                                           value="<%=cacheMediator.getHTTPMethodsToCache()%>" size="40" /></td>
                            </tr>
                            <%} else {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.httpmethods" /></td>
                                <td><input type="text" name="methods" value="" size="40" /></td>
                            </tr>
                            <%}%>
                            <%if (cacheMediator.getHeadersToExcludeInHash() != null) {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.headersToExclude" /></td>
                                <td><input type="text" name="headersToExclude"
                                           value="<%=cacheMediator.getHeadersToExcludeInHash()%>" size="40" /></td>
                            </tr>
                            <%} else {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.headersToExclude" /></td>
                                <td><input type="text" name="headersToExclude" value="" size="40" /></td>
                            </tr>
                            <%}%>
                            <%if (cacheMediator.getResponseCodes() != null) {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.responsecodes" /></td>
                                <td><input type="text" name="responseCodes"
                                           value="<%=cacheMediator.getResponseCodes()%>" size="40" /></td>
                            </tr>
                            <%} else {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.responsecodes" /></td>
                                <td><input type="text" name="responseCodes" value="" size="40" /></td>
                            </tr>
                            <%}%>
                            <tr>
                                <td>
                                    <div id="hashGen"><fmt:message key="mediator.cache.hashgenerator" /><font
                                            style="color: red; font-size: 8pt;"> *</font></div>
                                </td>
                                <td>
                                    <div id="hashGenVal"><input type="text" name="hashGen"
                                                                value="org.wso2.carbon.mediator.cache.digest.HttpRequestHashGenerator"
                                                                readonly="readonly"
                                                                size="40" /></div>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="mediator.cache.cacheControlEnabled"/></td>
                                <td>
                                    <select name="enableCacheControl" id="enableCacheControl"
                                            class="esb-edit small_textbox">
                                        <%
                                            if (cacheMediator.isCacheControlEnabled()) {
                                        %>
                                        <option selected="true" value="true">True</option>
                                        <option value="false">False</option>
                                        <%
                                        } else {
                                        %>
                                        <option value="true">True</option>
                                        <option selected="true" value="false">False</option>
                                        <%
                                            }
                                        %>
                                    </select>
                                </td>
                                <td></td>
                            </tr>
                            <tr>
                                <td><fmt:message key="mediator.cache.includeAgeHeader"/></td>
                                <td>
                                    <select name="includeAgeHeader" id="includeAgeHeader"
                                            class="esb-edit small_textbox">
                                        <%
                                            if (cacheMediator.isAddAgeHeaderEnabled()) {
                                        %>
                                        <option selected="true" value="true">True</option>
                                        <option value="false">False</option>
                                        <%
                                        } else {
                                        %>
                                        <option value="true">True</option>
                                        <option selected="true" value="false">False</option>
                                        <%
                                            }
                                        %>
                                    </select>
                                </td>
                                <td></td>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="hideCacheDetails">
                        <h3 class="mediator"><fmt:message key="mediator.cache.cachedetails" /></h3>
                        <table class="normal">
                            <%if (cacheMediator.getInMemoryCacheSize() > 0) {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.maxSize" /></td>
                                <td><input type="text" name="maxSize"
                                           value="<%=cacheMediator.getInMemoryCacheSize()%>" size="40" /></td>
                            </tr>
                            <%} else {%>
                            <tr>
                                <td><fmt:message key="mediator.cache.maxSize" /></td>
                                <td><input type="text" name="maxSize" value="" size="40" /></td>
                            </tr>
                            <%}%>

                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div id="hideCachHit">
                        <h3 class="mediator"><fmt:message key="mediator.cache.cacheHit" /></h3>
                        <table class="normal">

                            <tr>
                                <td>
                                    <input type="radio" id="sequenceOptionAnon" name="sequenceOption" value="annon"
                                           onclick="anonSelected()" /><fmt:message
                                        key="mediator.cache.annon" /></td>
                                <td></td>
                                <td></td>
                            </tr>
                            <tr>
                                <td>
                                    <input type="radio" id="sequenceOptionReference" name="sequenceOption"
                                           value="selectFromRegistry" onclick="registrySelected()" /><fmt:message
                                        key="mediator.cache.sequenceRef" />
                                </td>
                                <td>
                                    <div id="mediator.sequence.txt.div">
                                        <input type="text" id="mediator.sequence" name="mediator.sequence"
                                               readonly="readonly" />
                                    </div>
                                </td>
                                <td>
                                    <div id="mediator.sequence.link.div">
                                        <a href="#registryBrowserLink"
                                           class="registry-picker-icon-link"
                                           onclick="showRegistryBrowser('mediator.sequence','/_system/config')"><fmt:message
                                                key="mediator.cache.conf.registry.browser" /></a>
                                        <a href="#registryBrowserLink"
                                           class="registry-picker-icon-link"
                                           onclick="showRegistryBrowser('mediator.sequence','/_system/governance')"><fmt:message
                                                key="mediator.cache.gov.registry.browser" /></a>
                                    </div>
                                </td>
                            </tr>

                        </table>
                    </div>
                </td>
            </tr>
        </table>

        <%if (cacheMediator.isCollector()) {%>
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