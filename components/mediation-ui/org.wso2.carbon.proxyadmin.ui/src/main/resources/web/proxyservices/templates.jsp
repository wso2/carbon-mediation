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
<%@ page import="java.net.URL" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">
<script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
    <carbon:breadcrumb
            label="service.proxy.menu.text"
            resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="templates.title"/></h2>
        <div id="workArea">
            <p>
                <fmt:message key="templates.desc"/>
            </p>
            &nbsp;
            <table cellspacing="0" class="styledLeft" id="templatesTable">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="select.template"/></th>
                </tr>
                </thead>
                <tbody>
                    <%
                        //remove any sessions related to templates since template mode settings should not interfere
                        //with proxy admin editor mode settings
                        //in annonymous endpoint mode of proxy ui
                        session.removeAttribute("endpointTemplate");
                        session.removeAttribute("templateEdittingMode");
                        session.removeAttribute("templateRegKey");
                        //remove this attribute to avoid any sequence template related confilcts
                        //in annonymous sequence mode of proxy ui
                        session.removeAttribute("editorClientFactory");

                        Set<String> templates = application.getResourcePaths("/proxyservices");
                        Iterator<String> iter = templates.iterator();
                        while (iter.hasNext()) {
                            String template = iter.next();
                            template = template.substring(template.lastIndexOf("/") + 1);
                            String url = template;
                            if (template.startsWith("template_") && template.endsWith(".jsp")) {
                                template = template.substring(9, template.length() - 4);
                    %>
                                <tr>
                                    <td width="20%">
                                        <a href="<%=url%>"><fmt:message key="<%=template%>"/></a>
                                    </td>
                                    <td><fmt:message key="<%=template+".desc"%>"/></td>
                                </tr>
                    <%
                            }
                        }
                    %>
                    <tr>
                        <td width="20%">
                            <a href="./index.jsp?startwiz=true"><fmt:message key="custom.proxy"/></a>
                        </td>
                        <td><fmt:message key="custom.proxy.desc"/></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('templatesTable', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>