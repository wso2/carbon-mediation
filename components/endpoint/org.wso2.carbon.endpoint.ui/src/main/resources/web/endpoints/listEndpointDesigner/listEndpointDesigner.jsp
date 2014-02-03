<!--
~ Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.Endpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointStore" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.ListEndpoint" %>
<%@ page import="org.wso2.carbon.endpoint.ui.util.ListEndpointDesignerHelper" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.endpoint.ui.i18n.Resources">

<%
    ListEndpoint endpoint = ListEndpointDesignerHelper.getEditingListEndpoint(session);
%>
<link type="text/css" rel="stylesheet" href="css/tree-styles.css"/>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<script type="text/javascript" src="js/listEndpoint-designer.js"></script>
<script type="text/javascript">

    var oMenu = new YAHOO.widget.Menu("basicmenu");
    var aMenuItems = [
        <% for (String[] menuItem : EndpointStore.getInstance().getMenuItems()) { %>
        { text: "<%=menuItem[0]%>" , id: "<%=menuItem[1]%>" } ,
        <%}%>
    ];
    oMenu.addItems(aMenuItems);

    jQuery(document).ready(function() {
        initEndpoints();
    });

    function updateChildEndpoint(childEndpointName, refresh) {
        if (!validateForm()) {
             return false;
        }

        document.getElementById('endpointProperties').value = populateServiceParams("headerTable");
        refreshTree = refresh;
        var options = {
            // dataType: 'text/xml',
            success:       afterChildEndpointUpdate  // post-submit callback
        };

        jQuery('#childEndpoint-editor-form').ajaxForm(options);
        jQuery('#childEndpoint-editor-form').submit();
    }

    function afterChildEndpointUpdate(src) {
        if (document.getElementById('whileUpload') != null && document.getElementById('whileUpload') != undefined) {
            document.getElementById('whileUpload').style.display = "none";
        }
        document.getElementById('childEndpoint-form-header').style.display = 'none';
        document.getElementById('childEndpoint-form-tab').style.display = 'none';
        hide("childEndpointDesign");
        focusRootEndpoint();
    }

</script>

<table class="normal" width="100%">
    <tr>
        <td colspan="3">
            <div class="treePane" id="treePane"
                 style="height: 300px; overflow: auto; width: auto; border: 1px solid rgb(204, 204, 204);position:relative;">
                <div style="position:absolute;padding:20px;">
                    <ul class="root-list" id="endpointTree">
                        <li>
                            <div class="minus-icon"
                                 onclick="treeColapse(this)"
                                 id="treeColapser"></div>
                            <div class="childEndpoints" id="childEndpoint-00">
                                <a class="root-endpoint">root</a>
                                <div class="endpointToolbar"
                                     style="width:100px;">
                                    <div>
                                        <a class="addChildStyle"><fmt:message key="listendpointdesigner.add.child"/></a>
                                    </div>
                                </div>
                            </div>
                            <%
                                int count = endpoint != null ? endpoint.getList().size() : 0;
                                if (count != 0) {
                            %>
                            <div class="branch-node"></div>
                            <ul class="child-list">
                                <%
                                    int position = 0;
                                    for (Endpoint tmpEndpoint : endpoint.getList()) {
                                        count--;
                                %>
                                <%=ListEndpointDesignerHelper.getEndpointHTML(tmpEndpoint, count == 0, String.valueOf(position), config, request.getLocale())%>
                                <%
                                        position++;
                                    }
                                %>
                            </ul>
                            <%
                                }
                            %>
                        </li>
                    </ul>
                </div>
            </div>
            <script type="text/javascript">
                jQuery(document).ready(function() {

                    jQuery(".toggle_container").hide();
                    jQuery("h2.trigger").click(function() {
                        if (jQuery(this).next().is(":visible")) {
                            this.className = "active trigger";
                        } else {
                            this.className = "trigger";
                        }
                        jQuery(this).next().slideToggle("fast");
                        return false; //Prevent the browser jump to the link anchor
                    });
                });
            </script>
        </td>
    </tr>
</table>
<table class="normal" width="100%" name="childEndpoint-form-table" id="childEndpoint-form-table">
    <tr>
        <td>
            <table class="styledLeft" cellspacing="0">
                <tr id="childEndpoint-form-header" style="display:none;">
                    <td class="middle-header">
                                <span style="float:left; position:relative; margin-top:2px;"><fmt:message
                                        key="design.view.of.the.endpoint"/></span>
                    </td>
                </tr>
                <tr id="childEndpoint-form-tab" style="display:none;">
                    <td style="padding: 0px !important;">
                        <div class="tabPaneContentMain forProxy"
                             id="childEndpointDesign"
                             name="childEndpointDesign"
                             style="display:none;width:auto;padding:0px;"></div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

</fmt:bundle>
