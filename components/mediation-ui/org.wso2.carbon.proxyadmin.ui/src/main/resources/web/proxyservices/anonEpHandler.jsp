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
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%
    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.proxyadmin.ui.i18n.Resources",
            request.getLocale());
    String anonEpAction = request.getParameter("anonEpAction");
    String forwardTo = "";
    if (request.getParameter("cancelled") != null && "true".equals(request.getParameter("cancelled"))) {
        forwardTo = "index.jsp?header=" + session.getAttribute("header") + "&fromdesign=true";
        // removes common attributes
        removeCommonSessionAttributes(session);
    } else {
        if (anonEpAction != null && !"".equals(anonEpAction)) {
            // send path
            // sets the anonOriginator as anonEpHandler.jsp. This will be the page to which result should be returned
            session.setAttribute("anonOriginator", "../proxyservices/anonEpHandler.jsp");
            session.setAttribute("header", request.getParameter("header"));
            session.setAttribute("epMode", "anon");
            ProxyData pd = (ProxyData) session.getAttribute("proxy");
            session.setAttribute("proxy", pd);
            forwardTo = "../endpoints/anonymousEndpoint.jsp?serviceName=" + pd.getName() + "&toppage=false";
            if (bundle.getString("create").equals(anonEpAction)) {
                // going to add a new EP
                // remove anonEpXML attribute from session if exists
                if (session.getAttribute("anonEpXML") != null) {
                    session.removeAttribute("anonEpXML");
                }
            } else if (bundle.getString("anon.edit").equals(anonEpAction)) {
                // going to modify the existing EP
                String anonEpXML = pd.getEndpointXML();
                if (anonEpXML != null && !"".equals(anonEpXML)) {
                    session.setAttribute("anonEpXML", anonEpXML);
                }
            }
        } else {
            // return path
            ProxyData pd = (ProxyData) session.getAttribute("proxy");
            String anonEpXML = (String) session.getAttribute("anonEpXML");
            // the user may have cancelled the operation and therefore the anonEpXML may be null as well
            if (anonEpXML != null && !"".equals(anonEpXML)) {
                pd.setEndpointXML(anonEpXML);
            }
            forwardTo = "index.jsp?header=" + session.getAttribute("header") + "&fromdesign=true";
            removeCommonSessionAttributes(session);
        }
    }
%>

<%!
    void removeCommonSessionAttributes(HttpSession session) {
        session.removeAttribute("anonOriginator");
        session.removeAttribute("epMode");
        session.removeAttribute("anonEpXML");
        session.removeAttribute("header");
    }
%>

<script type="text/javascript">
    if (window.location.href.indexOf('originator') != -1 ||
            window.location.href.indexOf('cancelled') != -1) {
        window.location.href = '<%=forwardTo%>';
    } else {
        window.location.href = 'index.jsp';
    }
</script>