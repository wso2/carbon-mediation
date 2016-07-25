<!--
 ~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.inbound.ui.internal.InboundClientConstants"%>
<%@ page import="org.wso2.carbon.inbound.ui.internal.InboundDescription"%>
<%@ page import="org.wso2.carbon.inbound.ui.internal.InboundManagementClient" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="inboundcommon.js"></script>

<fmt:bundle basename="org.wso2.carbon.inbound.ui.i18n.Resources">
    <carbon:jsi18n
        resourceBundle="org.wso2.carbon.inbound.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="taskjsi18n"/>
    <carbon:breadcrumb resourceBundle="org.wso2.carbon.inbound.ui.i18n.Resources"
                       topPage="true" request="<%=request%>" label="inbound.header"/>
    <div id="middle">
        <h2><fmt:message key="inbound.header"/></h2>

        <div id="workArea">
            <%
                InboundManagementClient client;
                try {
                    client = InboundManagementClient.getInstance(config, session);
                    List<InboundDescription> descriptions = client.getAllInboundDescriptions();
                    if (descriptions != null && !descriptions.isEmpty()) {

            %>
            <p><fmt:message key="available.defined.inbound.endpoints"/></p>
            <br/>
            <table id="myTable" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="inbound.name"/></th>
                    <th colspan="4"><fmt:message key="inbound.action"/></th>
                </tr>
                </thead>
                <tbody>

                <%
                    for (InboundDescription inboundDescription : descriptions) {
                        if (inboundDescription != null) {
                            String name = inboundDescription.getName();
                %>
                <tr id="tr_<%=name%>">

                    <td width="600px">
                        <% if (inboundDescription.getArtifactContainerName() != null) { %>
                            <img src="images/applications.gif">
                        <% } %>
                        <%=Encode.forHtmlContent(name)%>
                        <% if (inboundDescription.getIsEdited()) { %>
                            <span style="color:grey"> ( Edited )</span>
                        <% } %>
                    </td>

                    <% if (inboundDescription.getStatisticsEnable()) { %>
                    <td style="border-right:none;border-left:none;width:100px">
                        <div class="inlineDiv">
                            <div id="disableStat<%= inboundDescription.getName()%>">
                                <a href="#" onclick="disableStat('<%= inboundDescription.getName() %>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/static-icon.gif);">Disable Statistics</a>
                            </div>
                            <div id="enableStat<%= inboundDescription.getName()%>" style="display:none;">
                                <a href="#" onclick="enableStat('<%= inboundDescription.getName() %>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/static-icon-disabled.gif);">Enable
                                    Statistics</a>
                            </div>
                        </div>
                    </td>
                    <% } else { %>
                    <td style="border-right:none;border-left:none;width:100px">
                        <div class="inlineDiv">
                            <div id="enableStat<%= inboundDescription.getName()%>">
                                <a href="#" onclick="enableStat('<%=inboundDescription.getName()%>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/static-icon-disabled.gif);">Enable
                                    Statistics</a>
                            </div>
                            <div id="disableStat<%= inboundDescription.getName()%>" style="display:none">
                                <a href="#" onclick="disableStat('<%=inboundDescription.getName()%>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/static-icon.gif);">Disable Statistics</a>
                            </div>
                        </div>
                    </td>
                    <% } %>

                    <% if (inboundDescription.getTracingEnable()) { %>
                    <td style="border-right:none;border-left:none;width:100px">
                        <div class="inlineDiv">
                            <div id="disableTrace<%= inboundDescription.getName()%>">
                                <a href="#" onclick="disableTrace('<%= inboundDescription.getName() %>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/trace-icon.gif);">Disable Tracing</a>
                            </div>
                            <div id="enableTrace<%= inboundDescription.getName()%>" style="display:none;">
                                <a href="#" onclick="enableTrace('<%= inboundDescription.getName() %>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/trace-icon-disabled.gif);">Enable
                                    Tracing</a>
                            </div>
                        </div>
                    </td>
                    <% } else { %>
                    <td style="border-right:none;border-left:none;width:100px">
                        <div class="inlineDiv">
                            <div id="enableTrace<%= inboundDescription.getName()%>">
                                <a href="#" onclick="enableTrace('<%=inboundDescription.getName()%>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/trace-icon-disabled.gif);">Enable
                                    Tracing</a>
                            </div>
                            <div id="disableTrace<%= inboundDescription.getName()%>" style="display:none">
                                <a href="#" onclick="disableTrace('<%=inboundDescription.getName()%>')"
                                   class="icon-link"
                                   style="background-image:url(../admin/images/trace-icon.gif);">Disable Tracing</a>
                            </div>
                        </div>
                    </td>
                    <% } %>

                    <td style="border-right:none;border-left:none;width:100px">
                        <div class="inlineDiv">
                        <% if (inboundDescription.getArtifactContainerName() !=null) { %>
                            <a href="javascript:editCAppInbound('<%=name%>')" id="config_link"
                               class="edit-icon-link"><fmt:message key="inbound.edit"/></a>
                        <% } else { %>
                            <a href="javascript:editRecord('<%=name%>')" id="config_link"
                               class="edit-icon-link"><fmt:message key="inbound.edit"/></a>
                        <% } %>
                        </div>
                    </td>

                    <td style="border-left:none;width:100px">
                        <div class="inlineDiv">
                        <% if (inboundDescription.getArtifactContainerName() != null) { %>
                        <a href="#" id="delete_link" class="delete-icon-link" onclick="return false"><fmt:message
                                key="inbound.property.delete"/></a>
                        <% } else { %>
                            <a href="javascript:deleteRecord('<%=name%>')"
                               id="delete_link" class="delete-icon-link"><fmt:message
                                    key="inbound.property.delete"/></a>
                        <% } %>
                        </div>
                    </td>

                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
            <%} else {%>
            <p><fmt:message key="inbound.list.empty.text"/></p>
            <br/>
            <%}%>
                <div style="height:30px;">
                <a href="javascript:document.location.href='newInbound.jsp?ordinal=1'"
                   class="add-icon-link"><fmt:message key="inbound.button.add.text"/></a>
            </div>
            <%

            } catch (Throwable e) {
                request.getSession().setAttribute(InboundClientConstants.EXCEPTION, e);
            %>
            <script type="text/javascript">
                jQuery(document).ready(function() {
                    CARBON.showErrorDialog('<%=e.getMessage()%>');
                });
            </script>
            <%
                }
            %>
        </div>
    </div>
</fmt:bundle>
<script type="text/javascript">
    alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
</script>
