<%--
 ~ Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.localentry.ui.client.LocalEntryAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.localentry.stub.types.EntryData" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.localentry.ui.i18n.Resources">
    <carbon:breadcrumb
            label="source.url.entry"
            resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript">

        function ValidatSourceEntryForm(form) {
            if (IsEmpty(form.Name)) {
                CARBON.showWarningDialog('<fmt:message key="name.field.cannot.be.empty"/>')
                form.Name.focus();
                return false;
            }

            if (IsEmpty(form.Value)) {
                CARBON.showWarningDialog('<fmt:message key="value.field.cannot.be.empty"/>')
                form.Value.focus();
                return false;
            }
        }

        function IsEmpty(aTextField) {
            if ((aTextField.value.length == 0) ||
                    (aTextField.value == null)) {
                return true;
            }
            else {
                return false;
            }
        }
    </script>

    <div id="middle">
        <h2><fmt:message key="source.url.entry"/></h2>

        <div id="workArea">
            <form name="Submit" action="ServiceCaller.jsp" method="POST"
                  onsubmit="javascript:return ValidatSourceEntryForm(this)">
                <% String entryName = request.getParameter("entryName");
                    if (entryName != null) {
                        session.setAttribute("edit" + entryName, "true");
                    }
                    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),
                            session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    LocalEntryAdminClient client = new LocalEntryAdminClient(cookie, url, configContext);
                    EntryData entry = null;
                    if (entryName != null) {
                        EntryData[] entData = client.getEntryData();
                        for (EntryData data : entData) {
                            if (data.getName().equalsIgnoreCase(entryName)) {
                                entry = data;
                                break;
                            }
                        }
                    }
                %>
                <table cellpadding="0" cellspacing="0" border="0" class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key="Localentry.design.view.text"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <table class="normal" width="100%">
                                <input type="hidden" name="pageName" value="sourceURL.jsp"/>
                                <%if ((entry != null) && entry.getName() != null) {%>
                                <tr>
                                    <td>
                                        <fmt:message key="name"/>
                                        <span class="required">*</span>
                                    </td>
                                    <td>
                                        <input id="Name" type="hidden" name="Name" value="<%=entry.getName()%>"
                                               class="longInput"/>
                                        <label for="Name"><%=entry.getName()%>
                                        </label>
                                    </td>
                                </tr>
                                <%} else {%>
                                <tr>
                                    <td><fmt:message key="name"/>
                                        <span class="required">*</span>
                                    </td>
                                    <td><input type="text" name="Name" value="" class="longInput"/></td>
                                </tr>
                                <%}%>
                                <tr>
                                    <td style="width:100px"><fmt:message key="url"/>
                                        <span class="required">*</span>
                                    </td>
                                    <td>
                                        <%if ((entry != null) && entry.getValue() != null) {%>
                                        <input type="text" name="Value" value="<%=entry.getValue()%>"
                                               class="longInput"/>
                                        <%} else {%>
                                        <input type="text" name="Value" value="" class="longInput"/>
                                        <%}%>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td>
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

                            <h2 class="trigger active"><a href="#"><fmt:message key="Localentry.description"/></a></h2>

                            <div class="toggle_container">
                                <textarea name="eventDescription" id="eventDescription"
                                          title="Sequence Description"
                                          cols="100"
                                          rows="3"><%= ((entry != null) && (entry.getDescription() != null)) ? entry.getDescription() : ""%></textarea>
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="submit" value="<fmt:message key="save"/>" class="button"/>
                            <input type="button" value="<fmt:message key="cancel"/>"
                                   class="button"
                                   onclick="javascript:document.location.href='index.jsp?region=region1&item=localentries_menu'"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>