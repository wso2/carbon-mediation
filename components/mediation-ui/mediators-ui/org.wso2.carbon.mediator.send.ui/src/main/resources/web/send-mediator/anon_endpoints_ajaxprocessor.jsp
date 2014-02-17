<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%
    String anonEpAction = request.getParameter("anonEpAction");
    String forwardTo = "../endpoints/anonymousEndpoint.jsp";

    if (anonEpAction != null && !"".equals(anonEpAction)) {
        session.setAttribute("epMode", "anon");
        // sets the anonOriginator to .jsp. This will be the page to which result should be returned
        session.setAttribute("anonOriginator", "../sequences/design_sequence.jsp");
        if ("add".equals(anonEpAction)) {
            // going to add a new EP
            // remove anonEpXML attribute from session if exists
            session.removeAttribute("anonEpXML");
            session.removeAttribute("endpointXML");
        } else if ("edit".equals(anonEpAction)) {
            // going to modify the existing EP
            String anonEpXML = (String) session.getAttribute("endpointXML");
            session.removeAttribute("endpointXML");
            if (anonEpXML != null && !"".equals(anonEpXML)) {
                session.setAttribute("anonEpXML", anonEpXML);
            }
        }
    }
    response.sendRedirect(forwardTo);
%>




