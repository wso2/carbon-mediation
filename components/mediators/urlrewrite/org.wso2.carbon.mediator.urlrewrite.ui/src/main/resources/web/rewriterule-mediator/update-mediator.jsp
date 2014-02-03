
<%@page import="org.apache.synapse.commons.evaluators.config.EvaluatorFactory"%>
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

<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory"%>
<%@ page import="org.apache.synapse.util.AXIOMUtils"%>
<%@ page import="javax.xml.stream.XMLStreamException"%>
<%@page import="org.wso2.carbon.mediator.urlrewrite.URLRulesMediator"%>
<%@page import="org.wso2.carbon.mediator.urlrewrite.URLRewriteActions"%>
<%@page import="org.apache.synapse.util.xpath.SynapseXPath"%>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.synapse.commons.evaluators.EvaluatorException" %>
<%@ page import="org.apache.synapse.commons.evaluators.config.EvaluatorFactoryFinder" %>
<%
	Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

	if (!(mediator instanceof URLRulesMediator)) {

		throw new RuntimeException("Unable to edit the mediator");
	}
	URLRulesMediator urlRulesMediator = (URLRulesMediator) mediator;
	if (urlRulesMediator.getActions() != null) {
		urlRulesMediator.getActions().clear(); //clear at initial
	}
	String condition = null;
	if (request.getParameter("mediator.urlrewrite.condition") != null &&
	    !request.getParameter("mediator.urlrewrite.condition").trim().equals("")) {
		  try {
			    condition = request.getParameter("mediator.urlrewrite.condition");
	            OMElement evaluatorElem = org.apache.axiom.om.util.AXIOMUtil.stringToOM(condition);
	            urlRulesMediator.setEvaluator(EvaluatorFactoryFinder.getInstance().getEvaluator(evaluatorElem));
	        
	        } catch (XMLStreamException xse) {
	        	%>
		            <script type="text/javascript">
		          	  CARBON.showErrorDialog('<%=xse.getMessage()%>');
		            </script>
		        <%

	        } catch (EvaluatorException ee) {
	            %>
	          	  <script type="text/javascript">
	          		  CARBON.showErrorDialog('<%=ee.getMessage()%>');
	          	  </script>
	       		 <%	        
	        }
		} else {
			urlRulesMediator.setEvaluator();
		}

	// set rules
	String actionCountParameter = request.getParameter("actionCount");
	if (actionCountParameter != null && !"".equals(actionCountParameter)) {
		String value = null;
		String regex = null;

		String action = null;
		String fragment = null;
		int actionCount = 0;

		try {
			actionCount = Integer.parseInt(actionCountParameter);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid number format");
		}

		for (int i = 0; i < actionCount; i++) {
			URLRewriteActions urlRewriteActions = new URLRewriteActions();
			if (request.getParameter("action_select" + i) != null) {
				action = request.getParameter("action_select" + i);
				urlRewriteActions.setAction(action);
			}
			if (request.getParameter("fragment_select" + i) != null) {
				fragment = request.getParameter("fragment_select" + i);
				urlRewriteActions.setFragment(fragment);
			}
			if (request.getParameter("mediator.urlrewrite.regex" + i) != null &&
			    !request.getParameter("mediator.urlrewrite.regex" + i).trim().equals("")) {

				regex = request.getParameter("mediator.urlrewrite.regex" + i);
				urlRewriteActions.setRegex(regex);
			}
			XPathFactory xPathFactory = XPathFactory.getInstance();

			String valueId = "mediator.urlrewrite.valuetxt" + i;
			value = request.getParameter(valueId);
			String expression = request.getParameter("optionTypeSelection" + i);
			boolean isExpression =
			                       expression != null &&
			                               "expression".equals(expression.trim());

			if (value != null) {
				if (isExpression) {
					urlRewriteActions.setXpath(xPathFactory.createSynapseXPath(valueId,
					                                                           value.trim(),
					                                                           session));

				} else {
					urlRewriteActions.setValue(value.trim());
				}
			}

			urlRulesMediator.addActions(urlRewriteActions);
		}
	}
%>
<script type="text/javascript">
    document.location.href = "../sequences/design_sequence.jsp?ordinal=1";
</script>
