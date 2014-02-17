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
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="org.apache.axiom.om.impl.builder.StAXOMBuilder" %><%    
    String anonEpAction = request.getParameter("anonEpAction");
    String anonEpXML;
    String forwardTo = "";

    if (anonEpAction != null && !"".equals(anonEpAction)) {
        // send path
        session.setAttribute("epMode", "anon");
        // sets the anonOriginator to .jsp. This will be the page to which result should be returned
        session.setAttribute("anonOriginator", "../sequences/design_sequence.jsp");

        if ("add".equals(anonEpAction)) {
            // going to add a new EP
            forwardTo = "../endpoints/index.jsp";
            // remove anonEpXML attribute from session if exists
            if (session.getAttribute("anonEpXML") != null) {
                session.removeAttribute("anonEpXML");
            }
        } else if ("edit".equals(anonEpAction)) {
            // going to modify the existing EP
            anonEpXML = (String) session.getAttribute("endpointXML");
            session.removeAttribute("endpointXML");
            if (anonEpXML != null && !"".equals(anonEpXML)) {
                try {
                    StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(anonEpXML.getBytes()));
                    OMElement elem = builder.getDocumentElement();
                    if ((elem = elem.getFirstElement()) != null) {
                        String localName = elem.getLocalName();
                        if ("address".equals(localName)) {
                            // current one is an address EP
                            forwardTo = "../endpoints/addressEndpoint.jsp";
                        } else if("default".equals(localName)) {
                            // current one is an default EP
                            forwardTo = "../endpoints/defaultEndpoint.jsp";
                        } else if ("wsdl".equals(localName)) {
                            // current one is an wsdl EP
                            forwardTo = "../endpoints/WSDLEndpoint.jsp";
                        } else if ("failover".equals(localName)) {
                            // current one is an failover EP
                            forwardTo = "../endpoints/failOverEndpoint.jsp";
                        } else if ("loadbalance".equals(localName)) {
                            // current one is an loadBalance EP
                            forwardTo = "../endpoints/loadBalanceEndpoint.jsp";
                        } else if ("default".equals(localName)) {
                            // current one is an loadBalance EP
                            forwardTo = "../endpoints/defaultEndpoint.jsp";
                        }
                    }
                } catch (XMLStreamException e) {
                    // todo - handle error
                }
                forwardTo = forwardTo + "?toppage=false";
                session.setAttribute("anonEpXML", anonEpXML);
            }
        }
    }

    response.sendRedirect(forwardTo);
%>




