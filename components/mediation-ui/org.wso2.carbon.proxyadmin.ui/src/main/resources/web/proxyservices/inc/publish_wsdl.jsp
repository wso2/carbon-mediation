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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../proxyservices/js/proxyservices.js"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"/>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"/>
<script type="text/javascript" src="../resources/js/resource_util.js"/>
<script type="text/javascript" src="../ajax/js/prototype.js"/>

<jsp:include page="../../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
<fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.JSResources"
        request="<%=request%>"
        i18nObjectName="proxyi18n"/>

<script type="text/javascript">
    function showPublishWSDLOptions() {
        var selectedIndex = document.getElementById('publishWsdlCombo').selectedIndex;
        if (selectedIndex == 0) {
            hideElem('wsdlInline');
            hideElem('wsdlUri');
            hideElem('wsdlReg');
            hideElem('wsdlEP');
            hideElem('wsdlResourceTr');
            
            jQuery("#wsdlInlineText").rules("remove");
            jQuery("#wsdlUriText").rules("remove");
            jQuery("#wsdlRegText").rules("remove");
            jQuery("#wsdlEPText").rules("remove");
        } else if (selectedIndex == 1) {
            showElem('wsdlInline');
            hideElem('wsdlUri');
            hideElem('wsdlReg');
            hideElem('wsdlEP');
            showElem('wsdlResourceTr');

            jQuery("#wsdlInlineText").rules("add",{
                required:true
            });
            jQuery("#wsdlUriText").rules("remove");
            jQuery("#wsdlRegText").rules("remove");
            jQuery("#wsdlEPText").rules("remove");
        } else if (selectedIndex == 2) {
            hideElem('wsdlInline');
            showElem('wsdlUri');
            hideElem('wsdlReg');
            hideElem('wsdlEP');
            showElem('wsdlResourceTr');

            jQuery("#wsdlInlineText").rules("remove");
            jQuery("#wsdlUriText").rules("add",{
                required:true
            });
            jQuery("#wsdlRegText").rules("remove");
            jQuery("#wsdlEPText").rules("remove");
        } else if (selectedIndex == 3) {
            showElem('wsdlReg');
            hideElem('wsdlInline');
            hideElem('wsdlUri');
            hideElem('wsdlEP');
            showElem('wsdlResourceTr');

            jQuery("#wsdlInlineText").rules("remove");
            jQuery("#wsdlUriText").rules("remove");
            jQuery("#wsdlEPText").rules("remove");
            jQuery("#wsdlRegText").rules("add",{
                required:true
            });
        } else if (selectedIndex ==4){
            hideElem('wsdlReg');
            hideElem('wsdlInline');
            hideElem('wsdlUri');
            showElem('wsdlEP');
            hideElem('wsdlResourceTr');

            jQuery("#wsdlInlineText").rules("remove");
            jQuery("#wsdlUriText").rules("remove");
            jQuery("#wsdlRegText").rules("remove");
            jQuery("#wsdlEPText").rules("add",{
                required:true
            });
        }

    }

    function testWsdlUri() {
        document.getElementById('testConnBtn').disabled = 'true';
        var wsdlUri = document.getElementById('wsdlUriText').value;
        if (wsdlUri == '') {
            CARBON.showWarningDialog(proxyi18n["invalid.wsdl.uri"]);
            document.getElementById('testConnBtn').removeAttribute('disabled');
        } else {
            jQuery.post("testConnection-ajaxprocessor.jsp", {'url' : wsdlUri},
                    function(data,status) {
                        if (data.replace(/^\s+|\s+$/g, '') != 'success') {
                            CARBON.showErrorDialog(proxyi18n["invalid.wsdl.uri2"]);
                        } else {
                            CARBON.showInfoDialog(proxyi18n["wsdl.uri.ok"]);
                        }
                        document.getElementById('testConnBtn').removeAttribute('disabled');
                    });
        }
    }

    var wsdlResources = Array();

    function addWsdlResources() {
        var location = getElement('locationText').value;
        var key = getElement('wsdl.resource.key').value;
        // trim the input values
        location = location.replace(/^\s*/, "").replace(/\s*$/, "");
        key = key.replace(/^\s*/, "").replace(/\s*$/, "");
        if (location != '' && key != '') {
            if (isWsdlResourceAlreadyExists(location)) {
                CARBON.showWarningDialog("<fmt:message key="resource.already.exists"/>");
                return;
            }
            addWsdlResourceRow(location, key);
            document.getElementById('locationText').value="";
            document.getElementById('wsdl.resource.key').value="";
        } else {
            CARBON.showWarningDialog("<fmt:message key="empty.location.or.key"/>");
        }
    }

    function getElement(id) {
        return document.getElementById(id);
    }

    function addWsdlResourceRow(location, key) {
        addRow(location, key, 'wsdlResourceTable', 'deleteWsdlResourceRow');

        var currentIndex = wsdlResources.push(new Array(2)) - 1;
        wsdlResources[currentIndex]['location'] = location;
        wsdlResources[currentIndex]['key'] = key;

        setWsdlResourceList();
    }

    function deleteWsdlResourceRow(index) {
        CARBON.showConfirmationDialog("<fmt:message key="confirm.wsdlresource.deletion"/>" , function() {
            document.getElementById('wsdlResourceTable').deleteRow(index);
            wsdlResources.splice(index-1, 1);
            if (wsdlResources.length == 0) {
                document.getElementById('wsdlResourceTable').style.display = 'none';
            }
            setWsdlResourceList();
        });
    }

    function setWsdlResourceList() {
        var i;
        var str = '';
        if (wsdlResources.length > 0) {
            str = wsdlResources[0]['location'] + ',' + wsdlResources[0]['key'];
            for (i = 1; i < wsdlResources.length; i++) {
                str += '::' + wsdlResources[i]['location'] + ',' + wsdlResources[i]['key'];
            }
        }
        document.getElementById('wsdlResourceList').value = str;
    }

    function isWsdlResourceAlreadyExists(location) {
        var i;
        for (i = 0; i < wsdlResources.length; i++) {
            if (wsdlResources[i]['location'] == location) {
                return true;
            }
        }
        return false;
    }

    function showWSDLOptionsPane() {
        var wsdlOptionsRow = document.getElementById('wsdlOptionsRow');
        var link = document.getElementById('wsdlOptionsExpandLink');
        if (wsdlOptionsRow.style.display == 'none') {
            wsdlOptionsRow.style.display = '';
            link.style.backgroundImage = 'url(images/up.gif)';
        } else {
            wsdlOptionsRow.style.display = 'none';
            link.style.backgroundImage = 'url(images/down.gif)';
        }
    }
</script>
<%
   String templateType = request.getParameter("templateType");

%>

<div id="publishWSDLContent" align="center">
    <table id="wsdlOptionsTable" class="styledInner" cellspacing="0" width="80%">
        <thead>
        <tr>
            <th colspan="2">
                <a id="wsdlOptionsExpandLink" class="icon-link"
                   style="background-image: url(images/down.gif);"
                   onclick="showWSDLOptionsPane()"><%
                    if(templateType!=null){
                    if(templateType.equals("wsdl")){
                   %>
                    <fmt:message key="service.contract.publication.options"/>
                    <% }
                    }else { %>
                    <fmt:message key="publish.wsdl.options"/>
                    <% } %>
                </a>
            </th>
        </tr>
        </thead>
        <tbody>
        <tr id="wsdlOptionsRow" style="display:none;">
            <td style="padding: 0px !important;">
                <table cellpadding="0" cellspacing="0" class="styledInner" width="100%" style="margin-left:0px;">
                    <tr>
                        <td colspan="2">
                            <table class="normal-nopadding">
                                <tr>
                                    <td style="width:120px"><fmt:message key="publishing.wsdl"/></td>
                                    <td>
                                        <select id="publishWsdlCombo" name="publishWsdlCombo" onchange="showPublishWSDLOptions()">
                                            <option id="publishWsdlNone" selected="selected" value="None"><fmt:message key="select.inseq.none"/></option>
                                            <option id="publishWsdlInline" value="inline"><fmt:message key="specify.in.line"/></option>
                                            <option id="publishWsdlUri" value="uri"><fmt:message key="specify.source.url"/></option>
                                            <option id="publishWsdlReg" value="reg"><fmt:message key="pick.from.registry"/></option>
                                            <option id="publishWsdlEP" value="ep"><fmt:message key="pick.from.endpoint"/></option>
                                        </select>
                                    </td>

                                </tr>
                                <tr id="wsdlInline" style="display:none;">
                                    <td><fmt:message key="inline.wsdl"/></td>
                                    <td >
                                        <textarea name="wsdlInlineText" id="wsdlInlineText" rows="20" style="width: 99%;"></textarea>
                                    </td>
                                </tr>
                                <tr id="wsdlUri" style="display:none;">
                                    <td><fmt:message key="wsdl.uri"/></td>
                                    <td >
                                        <input type="text" name="wsdlUriText" id="wsdlUriText" size="60"/>
                                        <input type="button" id="testConnBtn" class="button" onclick="testWsdlUri()" value="Test URI"/>
                                    </td>
                                </tr>
                                <tr id="wsdlReg" style="display:none;">
                                    <td><fmt:message key="wsdl.refkey"/></td>
                                    <td >
                                        <table cellspacing="0">
                                            <tr>
                                            <td class="nopadding">
                                                <input type="text" name="wsdlRegText"
                                                       id="wsdlRegText" value="" size="40"
                                                       readonly="readonly"/>
                                            </td>
                                            <td>
                                                <a href="#" class="registry-picker-icon-link"
                                                   style="padding-left:30px" id="confRegBrowserLink"
                                                   onclick="showRegistryBrowser('wsdlRegText','/_system/config');"><fmt:message
                                                        key="conf.registry"/></a>
                                            </td>
                                            <td>
                                                <a href="#" class="registry-picker-icon-link"
                                                   style="padding-left:30px" id="govRegBrowserLink"
                                                   onclick="showRegistryBrowser('wsdlRegText','/_system/governance');"><fmt:message
                                                        key="gov.registry"/></a>
                                            </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr id="wsdlEP" style="display:none;">
                                    <td><fmt:message key="wsdl.epkey"/></td>
                                    <td >
                                        <table cellspacing="0">
                                            <tr>
                                                <td class="nopadding">
                                                    <input type="text" name="wsdlEPText"
                                                           id="wsdlEPText" value="" size="40"
                                                           readonly="readonly"/>
                                                </td>
                                                <td>
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:30px" id="confRegBrowserLink1"
                                                       onclick="showRegistryBrowser('wsdlEPText','/_system/config');"><fmt:message
                                                            key="conf.registry"/></a>
                                                </td>
                                                <td>
                                                    <a href="#" class="registry-picker-icon-link"
                                                       style="padding-left:30px" id="govRegBrowserLink1"
                                                       onclick="showRegistryBrowser('wsdlEPText','/_system/governance');"><fmt:message
                                                            key="gov.registry"/></a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr id="wsdlResourceTr" style="display:none;">
                                    <td style="vertical-align:top!important">
                                        <fmt:message key="wsdl.resource"/>
                                    </td>
                                    <td>
                                        <input type="hidden" id="wsdlResourceList" name="wsdlResourceList"/>
                                        <table class="styledInner">
                                            <tr>
                                                <td style="border: solid 1px #ccc !important;">
                                                    <div id="wsdlResourceAdd">
                                                        <table class="normal-nopadding" cellspacing="0">
                                                            <tr>
                                                                <td class="nopadding">
                                                                    <table>
                                                                        <tr>
                                                                            <td class="nopadding">
                                                                                <fmt:message key="wsdl.resource.location"/> <input type="text" id="locationText"/> </td><td class="nopadding">
                                                                            <fmt:message key="wsdl.resource.key"/> <input type="text" readonly="readonly" value="" id="wsdl.resource.key" name="wsdl.resource.key"/>
                                                                        </td>
                                                                        <td class="nopadding" style="padding-top: 10px !important">
                                                                            <a href="#" class="registry-picker-icon-link"
                                                                               onclick="showRegistryBrowser('wsdl.resource.key','/_system/config');"><fmt:message
                                                                                    key="conf.registry"/></a>
                                                                        </td>
                                                                        <td class="nopadding" style="padding-top: 10px !important">
                                                                            <a href="#" class="registry-picker-icon-link"
                                                                               onclick="showRegistryBrowser('wsdl.resource.key','/_system/governance');"><fmt:message
                                                                                    key="gov.registry"/></a>
                                                                        </td>
                                                                        </tr>
                                                                    </table>
                                                                </td>
                                                            </tr>
                                                            <tr><td class="nopadding">
                                                                <a class="icon-link"
                                                                   href="#addNameLink"
                                                                   onclick="addWsdlResources();"
                                                                   style="background-image: url(../admin/images/add.gif);"><fmt:message key="wsdl.resource.add"/>
                                                                </a>
                                                            </td>
                                                            </tr>
                                                        </table>
                                                    </div>
                                                    <div>
                                                        <table cellpadding="0" cellspacing="0" border="0" class="styledLeft"
                                                               id="wsdlResourceTable"
                                                               style="display:none;">
                                                            <thead>
                                                            <tr>
                                                                <th style="width:40%"><fmt:message key="location"/></th>
                                                                <th style="width:40%"><fmt:message key="key"/></th>
                                                                <th style="width:20%"><fmt:message key="param.action"/></th>
                                                            </tr>
                                                            </thead>
                                                            <tbody/>
                                                        </table>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>



<script type="text/javascript">
    jQuery(document).ready(function() {
        showPublishWSDLOptions();
    });
</script>

</fmt:bundle>
