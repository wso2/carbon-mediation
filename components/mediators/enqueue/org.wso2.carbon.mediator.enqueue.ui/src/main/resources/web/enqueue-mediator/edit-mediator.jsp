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

<%@ page import="org.wso2.carbon.mediator.enqueue.EnqueueMediator" %>
<%@ page import="org.wso2.carbon.mediator.enqueue.PriorityAdminClient" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    PriorityAdminClient priorityAdminClient = new PriorityAdminClient(this.getServletConfig(), session);
    List<String> executorList = priorityAdminClient.getExecutors();
    Iterator<String> iterator = executorList.iterator();

    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    if (!(mediator instanceof EnqueueMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    EnqueueMediator enqueueMediator = (EnqueueMediator) mediator;
    String key = enqueueMediator.getSequence();

%>

<fmt:bundle basename="org.wso2.carbon.mediator.enqueue.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.enqueue.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="enqueueMediatorJsi18n"/>

    <div>
        <script type="text/javascript" src="../enqueue-mediator/js/mediator-util.js"></script>

        <table class="normal" width="100%">
            <tbody>
            <tr>
                <td colspan="3"><h2><fmt:message key="enqueue.mediator"/></h2></td>
            </tr>
            <tr>
                <td style="width:130px;">Executor<font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td colspan="2">
                    <select type="text" id="executor" name="executor"
                            style="width:300px;">
                        <%
                            while (iterator.hasNext()) {
                                String executor = iterator.next();
                                if (enqueueMediator.getExecutor() != null && executor.equals(enqueueMediator.getExecutor().trim())) { %>
                        <option selected="selected" value="<%=executor%>"><%=executor%>
                        </option>
                        <% } else {
                        %>
                        <option value="<%=executor%>"><%=executor%>
                        </option>
                        <%
                                }
                            }
                        %>

                    </select>
                </td>
            </tr>

            <tr>
                <td style="width:130px;">Priority<font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td colspan="2">
                    <input type="text" id="priority" name="priority"
                           style="width:300px;"
                           value='<%=enqueueMediator.getPriority()%>'/>
                </td>
            </tr>

            <tr>
                <td style="width:130px;">Sequence<font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td>
                    <input type="text" id="enqueue.mediator.sequence" name="enqueue.mediator.sequence"
                           style="width:300px;" readonly="readonly"
                           value='<%=enqueueMediator.getSequence() != null ? key : ""%>'/>
                </td>

                <td><a href="#registryBrowserLink" id="regEpLink_1"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('enqueue.mediator.sequence', '/_system/config')">
                    <fmt:message key="conf.registry.keys"/></a> <a
                        href="#registryBrowserLink" id="regEpLink_2"
                        class="registry-picker-icon-link"
                        onclick="showRegistryBrowser('enqueue.mediator.sequence', '/_system/governance')"><fmt:message
                        key="gov.registry.keys"/></a>
                </td>
            </tr>
            </tbody>
        </table>
        <a name="registryBrowserLink"/>

        <div id="registryBrowser" style="display:none;"/>
    </div>
</fmt:bundle>