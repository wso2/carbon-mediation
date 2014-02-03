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
<%@page import="java.util.ArrayList"%>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData"%>
<%@page import="java.util.List"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
	List<ResourceData> resourceList =
			(ArrayList<ResourceData>)session.getAttribute("apiResources");

    boolean isResourceUpdatePending = false;
    String rIndex = request.getParameter("updatePending");
    if(rIndex.equals("true")) {
        isResourceUpdatePending = true;
    } else {
        isResourceUpdatePending = false;
    }

%>

<div id="treeColapser"
	 class="minus-icon"
     onclick="treeColapse(this)">
</div>
<div id="api.root" class="resources">
    <a class="root-endpoint">Root</a>

    <div class="sequenceToolbar"
         style="width:100px;" onclick="addResource()">
        <div>
            <a class="addChildStyle">Add Resource</a>
        </div>
    </div>
</div>

<%
	for(int i=0; i<resourceList.size(); i++){%>
		<div id="<%="branch." + i%>" class="branch-node"></div>
		<ul id="<%="ul." + i%>" class="child-list"> 
	  	<li>
			<div class="dot-icon"></div>
			<div id="<%="resource." + i%>" class="resources">
                <% if (isResourceUpdatePending) { %>
                    <a class="resource" onclick="loadResourceData(this, true)">Resource</a>
                <% } else { %>
                    <a class="resource" onclick="loadResourceData(this, false)">Resource</a>
                <% } %>
				<div style="width: 100px;" class="sequenceToolbar">
					<div>
						<a class="deleteStyle" onclick="<%="deleteResource(" + i + ")"%>">Delete</a>
					</div>
				</div>
			</div>
		</li>
	    </ul>
	<%}
%>
