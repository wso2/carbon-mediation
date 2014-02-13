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
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.MetaData" %>
<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyAdminClientUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<script type="text/javascript" src="../js/proxyservices.js"></script>

<link rel="stylesheet" type="text/css" href="../yui/build/fonts/fonts-min.css" />
<script type="text/javascript" src="../yui/build/selector/selector-min.js"></script>

<script type="text/javascript" src="../yui/build/event-delegate/event-delegate-min.js"></script>



<script type="text/javascript">
    var onEndpointTypeChange = function (event, matchedEl, container) {
        showEndpointOptions(matchedEl);
    };
    YAHOO.util.Event.delegate("targetEndpointOpts", "click", onEndpointTypeChange, "input");

    jQuery(document).ready(function() {
        initEPValidation("url");    
    });
    function showEndpointOptions(select) {
        var i = select.value;
        initEPValidation(i);
    }
    function initEPValidation(i){
        if (i == 'url') {
            showElem('targetURL');
            hideElem('predefEndpoints');
            hideElem('endpointReg');
            jQuery("#endpointRegKey").rules("remove");
            jQuery("#targetURLTxt").rules("add",{
                required:true,
                esbURL:true
            });
        } else if (i == 'predef') {
            hideElem('targetURL');
            showElem('predefEndpoints');
            hideElem('endpointReg');
            jQuery("#targetURLTxt").rules("remove");
            jQuery("#endpointRegKey").rules("remove");            
        } else if (i == 'reg') {
            hideElem('targetURL');
            hideElem('predefEndpoints');
            showElem('endpointReg');
            jQuery("#endpointRegKey").rules("add",{
                required:true
            });
            jQuery("#targetURLTxt").rules("remove");
        }
    }

    jQuery.validator.addMethod("esbURL", function(value, element) {
      return this.optional(element) || /((fix|jms|http|https|local|ftp|file|hl7|idoc|bapi|vfs):\/.*)|file:.*|mailto:.*/.test(value);
    }, "Please specify a correct URL");


</script>

<fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="proxyi18n"/>

    <%
        MetaData md = (MetaData) request.getAttribute("proxyMetadata");
    %>

    <tr>
        <td><fmt:message key="target.endpoint"/><span class="required">*</span></td>
        <td id="targetEndpointOpts">
            <input type="radio" name="targetEndpointMode" value="url" id="url_targetEndpointMode" checked="checked" /><label for="url_targetEndpointMode"><fmt:message key="endpoint.op.enter.url"/></label>
            <% if (md.getEndpointsAvailable()) { %>
            <input type="radio" name="targetEndpointMode" value="predef" id="predef_targetEndpointMode" /><label for="predef_targetEndpointMode"><fmt:message key="endpoint.op.predef"/></label>
            <% } %>
            <input type="radio" name="targetEndpointMode" value="reg" id="reg_targetEndpointMode" /><label for="reg_targetEndpointMode"><fmt:message key="pick.from.registry"/></label>
        </td>
    </tr>
    <tr id="targetURL">
        <td><fmt:message key="target.url"/><span class="required">*</span></td>
        <td><input type="text" name="targetURL" id="targetURLTxt" size="60" /><%--<label class="form-help">Help text</label>--%></td>
    </tr>
    <%
        if (md.getEndpointsAvailable()) {
            String[] endpoints = ProxyAdminClientUtils.sortNames(md.getEndpoints());
    %>
    <tr id="predefEndpoints" style="display:none;">
        <td><fmt:message key="endpoint.op.predef"/><span class="required">*</span></td>
        <td>
            <select name="predefEndpoint" id="predefEndpointsCombo">
                <%
                    for (int i = 0; i < endpoints.length; i++) {
                %>
                <option value="<%=endpoints[i]%>"><%=endpoints[i]%></option>
                <%
                    }
                %>
            </select>
        </td>
    </tr>
    <%
        }
    %>
    <tr id="endpointReg" style="display:none;">
        <td><fmt:message key="endpoint.op.regkey"/><span class="required">*</span></td>
        <td >
            <table class="normal">
                <tr>
                    <td style="padding-left:0px !important">
                        <input type="text" id="endpointRegKey"
                               name="endpointRegKey" size="40"
                               readonly="true" />
                    </td>
                    <td>
                        <a href="#"
                           class="registry-picker-icon-link"
                           style="padding-left:20px"
                           onclick="showRegistryBrowser('endpointRegKey', '/_system/config');"><fmt:message key="conf.registry"/></a>
                    </td>
                    <td>
                        <a href="#"
                           class="registry-picker-icon-link"
                           style="padding-left:20px"
                           onclick="showRegistryBrowser('endpointRegKey', '/_system/governance');"><fmt:message key="gov.registry"/></a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>

    <%
        if ("true".equals(request.getParameter("formSubmitted"))) {
            String endpointMode = request.getParameter("targetEndpointMode");
            int selectedIndex = 0;
            if ("url".equals(endpointMode)) {
                if (request.getParameter("targetURL") != null) {
    %>
            <script type="text/javascript">
                 YAHOO.util.Event.onDOMReady(function(){
                     document.getElementById('targetURLTxt').value = '<%=request.getParameter("targetURL").trim()%>';
                    document.getElementById('url_targetEndpointMode').checked = true;
                });
            </script>
    <%
                }
            } else if ("predef".equals(endpointMode)) {
                selectedIndex = 1;
    %>
        <script type="text/javascript">
                YAHOO.util.Event.onDOMReady(function(){
                    document.getElementById('predefEndpointsCombo').value = '<%=request.getParameter("predefEndpoint")%>';
                    document.getElementById('predef_targetEndpointMode').checked = true;
                });
        </script>
    <%
            } else if ("reg".equals(endpointMode)) {
                selectedIndex = 2;
            }
    %>
        <script type="text/javascript">
           /* var select = document.getElementById('targetEndpointMode');
            select.selectedIndex = <%=selectedIndex%>;
            showEndpointOptions(select);*/
        </script>
    <%
        }
    %>    
    
</fmt:bundle>
