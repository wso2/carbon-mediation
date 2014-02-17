<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

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

<%@ page import="org.wso2.carbon.mediator.urlrewrite.URLRewriteMediator"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath"%>
<%@ page
	import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar"%>
<%@ page import="java.util.List"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>


<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp" />

<fmt:bundle	basename="org.wso2.carbon.mediator.urlrewrite.ui.i18n.Resources">
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.urlrewrite.ui.i18n.JSResources"
		request="<%=request%>" 
		i18nObjectName="urlrewritejsi18n" />

	<%
			    
		Mediator mediator =  SequenceEditorHelper.getEditingMediator(request,
		                                                            session);
		if (!(mediator instanceof URLRewriteMediator)) {
				throw new RuntimeException("Unable to edit the mediator");
		}
		URLRewriteMediator urlRewriteMediator = (URLRewriteMediator) mediator;
		
		 List<Mediator> mediatorList = urlRewriteMediator.getList();
		 int noOfRules = mediatorList.size();
%>


	<div> <script type="text/javascript">
            function addRules() {
                if (!updateEditingMediator()) {
                    return;
                }
                document.location.href = "../rewrite-mediator/add-rule.jsp";
            }
            
        </script>
	<table class="normal" width="100%">

		<tr>
			<td>
			<h2><fmt:message key="mediator.urlrewrite.header" /></h2>
			</td>
		</tr>
		<tr>
			<td>
			<table class="normal">
				<tr>
					<td><fmt:message key="in.property" /></td>
					<td><input type="text" id="in.property" name="in.property"
						value='<%=urlRewriteMediator.getInProperty() != null ? 
					          urlRewriteMediator.getInProperty(): ""%>' />
					</td>
					<td></td>
				</tr>
				<tr>
					<td><fmt:message key="out.property" /></td>
					<td><input type="text" id="out.property" name="out.property"
						value='<%=urlRewriteMediator.getOutProperty() != null ? 
					          urlRewriteMediator.getOutProperty(): ""%>' />
					</td>
					<td></td>
				</tr>
				
			<tr>
                <td>
                    <div style="margin-top:0px;">
                        <a name="addRulesLink"></a>
                        <a class="add-icon-link"
                           href="#addRulesLink"
                           onclick="addRules()">
                            <fmt:message key="mediator.urlrewrite.addrule"/></a>
                    </div>
                </td>
            </tr>
			</table>
			</td>
		</tr>
	</table>

	</div>
</fmt:bundle>