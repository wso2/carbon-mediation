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
<%@page import="org.apache.axis2.AxisFault"%>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData"%>
<%@page import="java.util.List"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rest.api.ui.client.RestApiAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="javax.xml.stream.XMLStreamReader" %>
<%@ page import="javax.xml.stream.XMLInputFactory" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.synapse.config.xml.rest.ResourceFactory" %>
<%@ page import="org.apache.synapse.rest.Resource" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.apache.axiom.om.OMAttribute" %>
<%@ page import="org.apache.synapse.rest.dispatch.URLMappingHelper" %>
<%@ page import="org.apache.synapse.rest.dispatch.URITemplateHelper" %>
<%@ page import="org.apache.synapse.config.xml.XMLConfigConstants" %>
<%@ page import="org.apache.synapse.config.xml.SequenceMediatorFactory" %>
<%@ page import="org.apache.synapse.mediators.base.SequenceMediator" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.apache.axis2.Constants" %>
<%@ page import="org.apache.synapse.rest.RESTConstants" %>
<%@ page import="org.apache.synapse.SynapseException" %>
<%@ page import="org.wso2.carbon.rest.api.ui.util.ApiEditorHelper" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    ResourceBundle bundle = ResourceBundle.getBundle(
			"org.wso2.carbon.rest.api.ui.i18n.Resources",
			request.getLocale());
    String url = CarbonUIUtil.getServerURL(this.getServletConfig()
		.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
		.getServletContext().getAttribute(
				CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
		.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    RestApiAdminClient client = new RestApiAdminClient(
		configContext, url, cookie, bundle.getLocale());

    String apiName = request.getParameter("apiName");
    String apiString = request.getParameter("apiString");
    String resourceString = request.getParameter("resourceString");
    String mode = request.getParameter("mode");
    String strError = null;
    
    if ("edit".equals(mode)) {
        if (apiString != null && !"".equals(apiString)) {
        	try{
            	client.updateApiFromString(apiName, apiString);
        	}catch(AxisFault af){
        		strError = af.getMessage();%>
        		error::<%=strError%>::error
        	<%}
        } else if (resourceString != null && !"".equals(resourceString)) {
            ResourceData resourceData = ApiEditorHelper.convertStringToResourceData(resourceString);
            String index = "0";
            APIData apiData = (APIData) session.getAttribute("apiData");
            int i = Integer.parseInt(index);
            apiData.getResources()[i] = resourceData;
            List<ResourceData> resourceList = (ArrayList<ResourceData>) session.getAttribute("apiResources");
            resourceList.add(i, resourceData);
            session.setAttribute("fromResourceSourceView", "true");
            session.setAttribute("fromSourceView", "true");
            session.setAttribute("resourceData", resourceData);
            session.setAttribute("apiData", apiData);
            session.setAttribute("apiResources", resourceList);
        }
    } else {        
    	try{
    		client.addApiFromString(apiString);
    	}catch(AxisFault af){
    		strError = af.getMessage();%>
    		error::<%=strError%>::error
    	<%}        
    }
%>
