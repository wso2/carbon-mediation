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


<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.factory.OperationBuilder" %>
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

<%@page import="java.io.FileOutputStream" %>
<%@page import="java.io.OutputStream" %>
<%@page import="java.io.OutputStreamWriter" %>
<%@page import="java.io.Writer" %>
<%@page import="java.io.File" %>
<%@page import="java.io.PrintWriter" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page
        import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.config.SalesforceUIHandler" %>
<%@ page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.*" %>
<%@ page
        import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.exception.SalesforceUIException" %>


<%
    /** This jsp is called whenever sequence Editor UI is updated or saved
     **/

    //select editing mediator stored in the current session
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String DEFAULT_AXIS2_CONF = "";//"./repository/conf/axis2.xml"
    String DEFAULT_CLIENT_REPO = "";//"./repository/deployment/client"
    String param = null;
    // SynapseXPath xpath = null;
    XPathFactory xPathFactory = XPathFactory.getInstance();

    if (!(mediator instanceof SalesforceMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SalesforceMediator salesforceMediator = (SalesforceMediator) mediator;
    SalesforceUIHandler handler = salesforceMediator.getHandler();

    OperationBuilder builder = null;
    OperationType operation = null;
    try {
        builder = new OperationBuilder(request.getParameter("mediator.salesforce.operation_name"), handler);
        operation = builder.createUIMappedOperation();
    } catch (SalesforceUIException e) {
        //TODO put a carbon UI error message here and handle errors
    }

    salesforceMediator.setOperation(operation);

    //populate configuration parameters
    param = request.getParameter("mediator.salesforce.repo");
    if (param != null && !param.equals("")) {
        salesforceMediator.setClientRepository(param);
    } else {
        salesforceMediator.setClientRepository(DEFAULT_CLIENT_REPO);
    }
    param = request.getParameter("mediator.salesforce.axis2XML");
    if (param != null && !param.equals("")) {
        salesforceMediator.setAxis2xml(param);
    } else {
        salesforceMediator.setAxis2xml(DEFAULT_AXIS2_CONF);
    }
%>

<%
    //populate inputs
    String inputCountParameter = request.getParameter("inputCount");
    List<Type> inputsList = new ArrayList<Type>();
    if (inputCountParameter != null && !"".equals(inputCountParameter)) {
        int inputCount = 0;
        try {
            inputCount = Integer.parseInt(inputCountParameter.trim());
            for (int i = 0; i < inputCount; i++) {
                String name = request.getParameter("inputName_hidden" + i);

                if (name != null && !"".equals(name)) {
                    String value = request.getParameter("inputValue" + i);
                    String expression = request.getParameter("inputTypeSelection" + i);
                    boolean isExpression = expression != null && "expression".equals(expression.trim());

                    InputType input = new InputType();
                    input.setName(name.trim());

                    if (value != null) {
                        if ("".equals(value)) {
                            value = "?";
                        }
                        if (isExpression) {
                            //xpath is constructed from namespace session information
                            input.setSourceXPath(xPathFactory.createSynapseXPath("inputValue" + i, value.trim(), session));
                        } else {
                            input.setSourceValue(value.trim());
                        }
                    }
                    inputsList.add(input);
                }
            }
            //we populate operation model with collected input information
            builder.populateInputs(operation, inputsList);
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        }
    }

%>

<%
    //populate outputs
    String outputCountParameter = request.getParameter("outputCount");
    List<Type> outputList = new ArrayList<Type>();
    if (outputCountParameter != null && !"".equals(outputCountParameter.trim())) {
        int outputCount = 0;
        try {
            outputCount = Integer.parseInt(outputCountParameter.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format");
        }

        for (int i = 0; i < outputCount; i++) {
            String name = request.getParameter("outputName_hidden" + i);
            System.out.println("name : " + name);

            if (name != null && !"".equals(name.trim())) {
                String targetKeyValue = request.getParameter("outputKeyValue" + i);
                String srcExpression = request.getParameter("outputSrcValue" + i);
                String targetExpression = request.getParameter("outputTargetValue" + i);
                System.out.println("Target Key : " + targetKeyValue);
                OutputType output = new OutputType();
                output.setName(name.trim());

                if (targetKeyValue != null && !"".equals(targetKeyValue.trim())) {
                    output.setTargetKey(targetKeyValue.trim());
                }

                if (srcExpression != null && !"".equals(srcExpression.trim())) {
                    //xpath is constructed from namespace session information
                    output.setSourceXpath(xPathFactory.createSynapseXPath("outputSrcValue" + i,
                                                                          srcExpression.trim(), session));
                }

                if (targetExpression != null && !"".equals(targetExpression.trim())) {
                    //xpath is constructed from namespace session information
                    output.setTargetXpath(xPathFactory.createSynapseXPath("outputTargetValue" + i,
                                                                          targetExpression.trim(), session));
                }
                outputList.add(output);
            }
        }
        //we populate operation model with collected input information
        builder.populateOutputs(operation, outputList);


    }
%>
