<%@ page import="org.wso2.carbon.task.ui.internal.TaskClientConstants" %>
<%--
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>
<%--<%@ page isErrorPage="true" %>--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    Throwable cause = (Throwable) request.getSession().getAttribute(TaskClientConstants.EXCEPTION);

%>
<fmt:bundle basename="org.wso2.carbon.task.ui.i18n.Resources">

    <div id="content">


        <div id="simple-content">
            <div class="page_title"><fmt:message key="task.error.main.text"/></div>
            <% if (cause != null) {
                request.removeAttribute(TaskClientConstants.EXCEPTION);
                String errorMsg = cause.getMessage();

                StackTraceElement[] trace = cause.getStackTrace();
            %>

            <div> Error : <%=errorMsg%>
            </div>
            <div>
                <% if (trace.length > 0) {
                %>
                <fmt:message key="task.error.tecnical.info.text"/>
                <br><br>
                <%
                    for (int x = 0; x < trace.length; x++) {
                %>
                <%=trace[x].toString()%><br>
                <%
                        }
                    }
                } else {

                    String errorMsg = request.getParameter(TaskClientConstants.ERROR_MSG);
                    if (errorMsg != null) {
                %>
                <div> Error : <%=errorMsg%>
                </div>
                <%
                } else {
                %>

                <fmt:message key="task.moreinfo.not.text"/>
                <% }
                }%>
            </div>
        </div>

    </div>
</fmt:bundle>


