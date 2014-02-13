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
<%@ page import="java.util.Enumeration" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.proxyadmin.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.proxyadmin.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="proxyi18n"/>

    <script type="text/javascript">
        function showTransportOptionsPane() {
            var wsdlOptionsRow = document.getElementById('transportOptionsRow');
            var link = document.getElementById('transportOptionsExpandLink');
            if (wsdlOptionsRow.style.display == 'none') {
                wsdlOptionsRow.style.display = '';
                link.style.backgroundImage = 'url(images/up.gif)';
            } else {
                wsdlOptionsRow.style.display = 'none';
                link.style.backgroundImage = 'url(images/down.gif)';
            }
        }

        jQuery(document).ready(function() {
            jQuery.validator.addMethod("notransports", function() {
                var valid = validateTransports();
                var transportOptionsRow = document.getElementById('transportOptionsRow');
                if(!valid && transportOptionsRow.style.display=="none"){
                    showTransportOptionsPane();
                }
                return validateTransports();
            }, proxyi18n['no.transports']);
        });
    </script>

    <%
        String[] transports = null;
        String trpList = "";
        MetaData metadata = (MetaData) request.getAttribute("proxyMetadata");
        transports = metadata.getTransports();
    %>

    <div id="transportsContent" align="center">
        <table id="transportOptionsTable" class="styledInner" cellspacing="0" width="80%">
            <thead>
            <tr>
                <th colspan="2">
                    <a id="transportOptionsExpandLink" class="icon-link"
                       style="background-image: url(images/down.gif);"
                       onclick="showTransportOptionsPane()"><fmt:message key="transports"/></a>
                </th>
            </tr>
            </thead>
            <tbody>
            <tr id="transportOptionsRow" style="display:none;">
                <td style="padding: 0px !important;">
                    <input name="availableTransportsList" id="availableTransportsList" type="hidden" value=""/>
                    <table cellpadding="0" cellspacing="0" class="styledInner" width="100%" style="margin-left:0px;">
                        <tr>
                            <td>
                                <table id="availableTransports" class="normal-nopadding" style="width: auto">
                                    <%
                                        for (int i = 0; i < transports.length; i++) {
                                            if (i == 0) {
                                                trpList += transports[i];
                                            } else {
                                                trpList += "," + transports[i];                                                               
                                            }
                                    %>
                                    <tr>
                                        <td><%=transports[i]%></td>
                                        <td><input id="trp__<%=transports[i]%>" name="trp__<%=transports[i]%>" type="checkbox"
                                                   value="<%=transports[i]%>" <%if(transports[i].startsWith("http")){%>checked="true"<%}%>/> </td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                </table>
                                <input type="hidden" class="notransports" id="notransports" name="notransports" />                                
                                <script type="text/javascript">
                                    document.getElementById('availableTransportsList').value = '<%=trpList%>';
                                </script>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <%
        if ("true".equals(request.getParameter("formSubmitted"))) {
            for (String transportName : transports) {
    %>
        <script type="text/javascript">
            var chk = document.getElementById('trp__' + '<%=transportName%>');
            if (chk) {
                chk.removeAttribute('checked');
            }
        </script>
    <%
            }

            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String paramName = params.nextElement();
                if (paramName.startsWith("trp__")) {
    %>
        <script type="text/javascript">
            var chk = document.getElementById('<%=paramName%>');
            if (chk) {
                chk.checked = 'true';
            }
        </script>
    <%
                }
            }
        }
    %>
</fmt:bundle>