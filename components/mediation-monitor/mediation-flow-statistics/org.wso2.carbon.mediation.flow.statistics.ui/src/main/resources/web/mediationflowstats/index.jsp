<%--
  ~ *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~ *
  ~ *  WSO2 Inc. licenses this file to you under the Apache License,
  ~ *  Version 2.0 (the "License"); you may not use this file except
  ~ *  in compliance with the License.
  ~ *  You may obtain a copy of the License at
  ~ *
  ~ *    http://www.apache.org/licenses/LICENSE-2.0
  ~ *
  ~ * Unless required by applicable law or agreed to in writing,
  ~ * software distributed under the License is distributed on an
  ~ * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ * KIND, either express or implied.  See the License for the
  ~ * specific language governing permissions and limitations
  ~ * under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.mediation.flow.statistics.ui.i18n.Resources">
    <carbon:breadcrumb label="Mediation Flow Statistics"
                       resourceBundle="org.wso2.carbon.mediation.flow.statistics.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <%
        try {
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");
    %>
    <div id="middle">
        <h2>Mediation Flow Statistics</h2>

        <div id="workArea">
            <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                <thead>
                <tr>
                    <th colspan="3">Statistic Category</th>
                </tr>
                </thead>
                <tbody>

                <tr>
                    <td style="width:150px"><label for="statisticCategory">Select Static Category</label><span
                            class="required">*</span></td>
                    <td align="left">
                        <select id="statisticCategory" name="statisticCategory" class="longInput">
                            <option value="proxy">Proxy Service Statistic</option>
                            <option value="api">API Statistic</option>
                            <option value="inbound">Inbound Endpoint Statistics</option>
                            <option value="sequence">Sequence Statistics</option>
                            <option value="endpoint">Endpoint Statistics</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow" colspan="3">
                        <input class="button" type="button" value="Next" onclick="onClick()">
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <script>
        function onClick() {
            var selectedValue = document.getElementById("statisticCategory").value;
            window.location.href = "component_statistics.jsp?statisticCategory=" + selectedValue;
        }
    </script>
    <%
    } catch (Throwable e) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            CARBON.showErrorDialog('<%=e.getMessage()%>');
        });
    </script>
    <%
            return;
        }
    %>

</fmt:bundle>
