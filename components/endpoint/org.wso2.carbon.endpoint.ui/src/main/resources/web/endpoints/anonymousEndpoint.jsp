<%--
  ~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointService" %>
<%@ page import="org.wso2.carbon.endpoint.ui.endpoints.EndpointStore" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%
    String forwardTo = "";
    String anonymousEndpointXML = (String) session.getAttribute("anonEpXML");
    if (anonymousEndpointXML != null && !"".equals(anonymousEndpointXML)) {
        try {
            OMElement endpointElement = AXIOMUtil.stringToOM(anonymousEndpointXML);
            EndpointService epService = EndpointStore.getInstance().getEndpointService(endpointElement);
            forwardTo = epService.getUIPageName() + "Endpoint.jsp";
        } catch (XMLStreamException e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Invalid Endpoint Configuration");
</script>
<%
            forwardTo = (String) session.getAttribute("anonOriginator");
        }
        forwardTo = forwardTo + "?toppage=false&" + request.getQueryString();
    } else {
        forwardTo = "../endpoints/index.jsp" + "?toppage=false&" + request.getQueryString();
    }

%>

<script type="text/javascript">
    window.location.href = '<%=forwardTo%>';
</script>
