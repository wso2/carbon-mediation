<%--

  Copyright (c) 20010-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
  Version 2.0 (the "License"); you may not use this file except
  in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.

--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.cloud.gateway.common.CGConstant" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<fmt:bundle basename="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources">
    <carbon:breadcrumb
            label="csg.menu.text"
            resourceBundle="org.wso2.carbon.cloud.gateway.agent.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="cloud.services.gateway"/></h2>

        <div id="workArea">
            <table width="100%">
            <tr>
                <td>
                    <table class="styledLeft" id="internal" width="100%">
                        <thead>
                        <tr>
                            <th><fmt:message key="csg.configuration"/></th>
                        </tr>
                        </thead>
                        <% if (CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PERMISSION_STRING)) {%>
                        <tr>
                            <td>
                                <a class="icon-link" style="background-image:url(images/add-edit-service.png);"
                                   href="server-list.jsp"><fmt:message key="csg.add.edit.server"/></a>
                            </td>
                        </tr>
                        <% } %>
                        <% if (CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_PUBLISH_SERVICE_PERMISSION_STRING) ||
                                CarbonUIUtil.isUserAuthorized(request, CGConstant.ADMIN_UN_PUBLISH_SERVICE_PERMISSION_STRING)){ %>
                        <tr>
                            <td>
                                <a class="icon-link"
                                   style="background-image:url(images/publish-unpublish.png);"
                                   href="service-list.jsp"><fmt:message key="csg.publish.unpublish"/></a>
                            </td>
                        </tr>
                        <% } %>
                    </table>
                </td>
            </tr>
            </table>
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('internal', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('external', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>
