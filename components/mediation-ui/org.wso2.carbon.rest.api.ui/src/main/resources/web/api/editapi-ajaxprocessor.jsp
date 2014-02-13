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
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
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
	String apiContext = request.getParameter("apiContext");
	String filename = request.getParameter("filename");
	String hostname = request.getParameter("hostname");
	String port = request.getParameter("port");
	
	if(port == null || "".equals(port)){
		port = "-1";
	}
	
	List<ResourceData> resourceList = 
			(ArrayList<ResourceData>)session.getAttribute("apiResources");
	APIData apiData = new APIData();
	apiData.setName(apiName);
	apiData.setContext(apiContext);
	apiData.setHost(hostname);
	apiData.setPort(Integer.parseInt(port));
	apiData.setFileName(filename);
	ResourceData resources[] = new ResourceData[resourceList.size()];
	
	if(resourceList != null) {

        for (int i = 0; i < resourceList.size() - 1; ++i) {
            ResourceData a = resourceList.get(i);
            for (int j = i + 1; j < resourceList.size(); ++j) {
                ResourceData b = resourceList.get(j);
                if (a.getUrlMapping() != null &&
                    a.getUrlMapping().equals(b.getUrlMapping())) {
                    for (String aMethod : a.getMethods()) {
                        for (String bMethod : b.getMethods()) {
                            if (aMethod != null && aMethod.equals(bMethod)) {
                                response.setStatus(454);
                                return;
                            }
                        }
                    }
                }
            }
        }
	}
	
	apiData.setResources(resourceList.toArray(resources));
    try {
        String[] names = client.getApiNames();

        for (String name : names) {
            APIData data = client.getApiByNane(name);
            if (data.getContext().equals(apiContext)) {
                if (!name.equals(apiName)) {
                    response.setStatus(453);
                    return;
                }
            }
        }
        client.updateApi(apiData);
    } catch (Exception e) {
        response.setStatus(450);
    }
    session.removeAttribute("apiResources");
    session.removeAttribute("resourceData");
    session.removeAttribute("apiData");
    session.removeAttribute("inSeqXml");
    session.removeAttribute("outSeqXml");
    session.removeAttribute("faultSeqXml");
%>
