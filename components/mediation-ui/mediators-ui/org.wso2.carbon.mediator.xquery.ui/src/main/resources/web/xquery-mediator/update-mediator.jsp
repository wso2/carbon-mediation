<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
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


<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.xquery.Variable" %>
<%@ page import="org.wso2.carbon.mediator.xquery.XQueryMediator" %>
<%@ page import="org.wso2.carbon.mediator.xquery.internal.XQueryMediatorClientHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="javax.xml.namespace.QName" %>

<%
    try {
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

        if (!(mediator instanceof XQueryMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }
        XQueryMediator xqueryMediator = (XQueryMediator) mediator;

        XPathFactory xPathFactory = XPathFactory.getInstance();
        
        xqueryMediator.setQueryKey(null);
        String keyVal;
        String keyExp;

        String keyGroup = request.getParameter("keygroup");
        if (keyGroup != null && !keyGroup.equals("")) {
            if (keyGroup.equals("StaticKey")) {
                keyVal = request.getParameter("mediator.xquery.key.static_val");
                if (keyVal != null && !keyVal.equals("")) {
                    Value staticKey = new Value(keyVal);
                    xqueryMediator.setQueryKey(staticKey);
                }
            } else if (keyGroup.equals("DynamicKey")) {
                keyExp = request.getParameter("mediator.xquery.key.dynamic_val");


                if (keyExp != null && !keyExp.equals("")) {
                    Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                            "mediator.xquery.key.dynamic_val", request.getParameter("mediator.xquery.key.dynamic_val"), session));
                    xqueryMediator.setQueryKey(dynamicKey);
                }
            }
        }

        String target = request.getParameter("mediator.xquery.target");
        if (target != null && !"".equals(target)) {
            xqueryMediator.setTarget(
                    xPathFactory.createSynapseXPath("mediator.xquery.target", target.trim(), session));
        }

        String variableCountParameter = request.getParameter("variableCount");
        if (variableCountParameter != null && !"".equals(variableCountParameter)) {
            int variableCount = 0;
            try {
                variableCount = Integer.parseInt(variableCountParameter.trim());
                xqueryMediator.getVariables().clear();
                for (int i = 0; i < variableCount; i++) {
                    String name = request.getParameter("variableName" + i);
                    String type = request.getParameter("variableType" + i);

                    if (name != null && !"".equals(name) && type != null && !"".equals(type)) {
                        int typeValue = XQueryMediatorClientHelper.getType(type.trim());
                        if (typeValue == -1) {
                            continue;
                        }
                        String id = "variableValue" + i;
                        String value = request.getParameter(id);
                        String expression = request.getParameter("variableTypeSelection" + i);
                        boolean isExpression = expression != null && "expression".equals(expression.trim());
                        if (isExpression) {
                            Variable variable = new Variable((new QName(name.trim())));
                            variable.setType(typeValue);
                            if (value != null && !"".equals(value)) {
                                variable.setExpression(xPathFactory.createSynapseXPath(id, value.trim(), session));
                            }
                            String regKey = request.getParameter("registryKey" + i);
                            if (regKey != null && !"".equals(regKey)) {
                                variable.setRegKey(regKey.trim());
                            }
                            variable.setVariableType(Variable.CUSTOM_VARIABLE);
                            xqueryMediator.addVariable(variable);                            
                        } else {
                            if (value != null && !"".equals(value)) {
                                Variable variable = new Variable(new QName(name.trim()));
                                variable.setType(typeValue);
                                variable.setValue(value);
                                variable.setVariableType(Variable.BASE_VARIABLE);
                                xqueryMediator.addVariable(variable);
                            }
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
%>


<%
} catch (Exception e) {      

%>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog("An error has been occurred !. Error Message : " + '<%=e.getMessage()%>');
    });
</script>
<%
        return;
    }
%>
