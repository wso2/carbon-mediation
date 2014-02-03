<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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

<%@ page import="org.wso2.carbon.mediator.clone.CloneMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.mediator.clone.ui.i18n.Resources">
    <%
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof CloneMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }
        CloneMediator cloneMediator = (CloneMediator) mediator;
    %>

    <div>
        <script type="text/javascript">
            function addCloneTarget() {
                if (!updateEditingMediator()) {
                    return;
                }
                document.location.href = "../clone-mediator/add_clone_target.jsp";
            }
        </script>
        <table class="normal" width="100%">
            <tr>
                <td>
                    <h2><fmt:message key="clone.mediator"/></h2>
                </td>
            </tr>
            <tr>
                <td>
                    <table class="normal">
                    <tr>
                    <td>
                    <fmt:message key="clone.id"/>
                    </td>
                    <td><input type="text" id="clone.id" name="clone.id"
						value='<%= cloneMediator.getId() != null ? 
								 cloneMediator.getId(): ""%>' />
					</td>
					<td></td>
                    </tr>
                     <tr>
                        <td>
                            <fmt:message key="mediator.clone.sequential"/>
                        </td>
                        <td>
                            <select name="sequentialMed" id="sequentialMed"
                                    class="esb-edit small_textbox">
                                <%
                                    if (cloneMediator.isSequential()) {
                                %>
                                <option selected="true" value="true"><fmt:message key="yes"/></option>
                                <option value="false"><fmt:message key="no"/></option>
                                <%
                                } else {
                                %>
                                <option value="true"><fmt:message key="yes"/></option>
                                <option selected="true" value="false"><fmt:message key="no"/></option>
                                <%
                                    }
                                %>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                        <tr>
                            <td>
                                <fmt:message key="continue.parent"/>
                            </td>
                            <td>
                                <select id="mediator.clone.continue" name="mediator.clone.continue">
                                    <option value="true" <%= cloneMediator.isContinueParent() ?
                                            "selected='selected'" : "" %>>
                                        <fmt:message key="yes"/>
                                    </option>
                                    <option value="false" <%= !cloneMediator.isContinueParent() ?
                                            "selected='selected'" : "" %>>
                                        <fmt:message key="no"/>
                                    </option>
                                </select>
                            </td>
                            <td></td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key="number.of.clones"/>
                            </td>
                            <td>
                                <%= cloneMediator.getList().size() %> <fmt:message key="clone.target"/><%= cloneMediator.getList().size() != 1 ? "s" : "" %>
                            </td>
                            <td><a href="#cloneTarget" onclick="addCloneTarget()"><fmt:message key="add.clone"/></a></td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>

        <a name="cloneTarget"></a>
    </div>
</fmt:bundle>