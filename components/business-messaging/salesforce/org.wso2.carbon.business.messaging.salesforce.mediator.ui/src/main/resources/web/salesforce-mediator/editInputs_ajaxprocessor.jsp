<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@page import="org.apache.synapse.util.xpath.SynapseXPath" %>

<%@page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.factory.OperationBuilder" %>

<%@page import="java.util.List" %>
<%@page import="java.util.Queue" %>
<%@page import="java.util.LinkedList" %>


<%@page import="java.util.ArrayList" %>
<%--<%@ page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui." %>--%>
<%@ page
        import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.config.SalesforceUIHandler" %>
<%@ page import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.*" %>
<%@ page
        import="org.wso2.carbon.business.messaging.salesforce.mediator.ui.exception.SalesforceUIException" %>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<%


    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    if (!(mediator instanceof SalesforceMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SalesforceMediator salesforceMediator = (SalesforceMediator) mediator;
    SalesforceUIHandler handler = salesforceMediator.getHandler();
    OperationType operation = salesforceMediator.getOperation();
    OperationBuilder builder;
    if (null == operation || !request.getParameter("operationName").equals(operation.getName())) {
        //build an operation from scratch - ui + model parameters
        try {
            builder = new OperationBuilder(request.getParameter("operationName"), handler);
            operation = builder.createUIMappedOperation();
        } catch (SalesforceUIException e) {
            //TODO put a carbon UI error message here
        }
    } else if (operation != null) {
        //populates ui parameters of an operation from an existing operation
        try {
            builder = new OperationBuilder(operation, handler);
            builder.createUIMappedOperationFromExistingModel();
        } catch (SalesforceUIException e) {
            //TODO put a carbon UI error message here
        }
    }

    List<InputType> inputs = new ArrayList<InputType>();
    List<OutputType> outputs = operation.getOutputs();

    if (!operation.getInputs().isEmpty()) {
        inputs = operation.getInputs();
    }
    if (!operation.getOutputs().isEmpty()) {
        outputs = operation.getOutputs();
    }

    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    //register namespaces for input xpath in the session
    for (int i = 0; i < inputs.size(); i++) {

        if (null != inputs.get(i).getSourceXPath()) {
            nameSpacesRegistrar.registerNameSpaces(inputs.get(i).getSourceXPath(),
                                                   "inputValue" + i, session);
        }

    }
    //register namespaces for output xpath in the session 
    for (int i = 0; i < outputs.size(); i++) {

        OutputType outputType = outputs.get(i);
        if (null != outputType.getSourceXPath()) {
            nameSpacesRegistrar.registerNameSpaces(outputs.get(i).getSourceXPath(),
                                                   "outputSrcValue" + i, session);
        }
        if (null != outputType.getTargetXPath()) {
            nameSpacesRegistrar.registerNameSpaces(outputs.get(i).getTargetXPath(),
                                                   "outputTargetValue" + i, session);
        }

    }

    String outputTableStyle = outputs.isEmpty() ? "display:none;" : "";

    String inputTableStyle = inputs.isEmpty() ? "display:none;" : "";
    System.out.println("init o.k");

%>
<fmt:bundle basename="org.wso2.carbon.business.messaging.salesforce.mediator.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.business.messaging.salesforce.mediator.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="salesforceMediatorJsi18n"/>
<div>
    <script type="text/javascript"
            src="../salesforce-mediator/js/mediator-util.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <table id="inputstable" style="<%=inputTableStyle%>;"
           class="styledInner">
        <thead>
        <tr>
            <th width="15%"><fmt:message key="mediator.salesforce.inputName"/></th>
            <th width="15%"><fmt:message key="mediator.salesforce.inputValue"/></th>
            <th><fmt:message key="mediator.salesforce.inputExp"/></th>
            <th width="15%" id="ns-edior-th" style="display: none;"><fmt:message
                    key="mediator.salesforce.nsEditor"/></th>
        </tr>
        </thead>
        <tbody id="inputtbody">
        <%
            int i = 0;
            //populate and display UI if inputs are available
            for (InputType input : inputs) {

                if (input != null) {
                    InputTypeUI uiType = handler.getInputUIObject(input);
                    String inputName = input.getName();
                    boolean isRequired = uiType.isRequired();
                    String value = input.getSourceValue();
                    SynapseXPath synapseXPath = input.getSourceXPath();
                    boolean isLiteral = value != null && !"".equals(value);
        %>
        <tr id="inputRaw<%=i%>">
            <td>
                <%=uiType.getDisplayName()%>
                <% if (isRequired) { %>
                <span class="required">*</span>
                <% } %>
                <input type="hidden" name="inputName_hidden<%=i%>" id="inputName_hidden<%=i%>"
                       value="<%=inputName%>"/>
                <input type="hidden" name="inputRequired_hidden<%=i%>"
                       id="inputRequired_hidden<%=i%>" value="<%=isRequired%>"/>
            </td>
            <td>
                <select class="esb-edit small_textbox"
                        name="inputTypeSelection<%=i%>" id="inputTypeSelection<%=i%>"
                        onchange="onInputTypeSelectionChange('<%=i%>','<fmt:message key="mediator.salesforce.namespace"/>')">
                    <% if (isLiteral) {%>
                    <option value="literal"><fmt:message
                            key="mediator.salesforce.value"/></option>
                    <option value="expression"><fmt:message
                            key="mediator.salesforce.expression"/></option>
                    <%} else if (synapseXPath != null) {%>
                    <option value="expression"><fmt:message
                            key="mediator.salesforce.expression"/></option>
                    <option value="literal"><fmt:message
                            key="mediator.salesforce.value"/></option>
                    <%} else { %>
                    <option value="literal"><fmt:message
                            key="mediator.salesforce.value"/></option>
                    <option value="expression"><fmt:message
                            key="mediator.salesforce.expression"/></option>
                    <% }%>
                </select>
            </td>
            <td>
                <% if (isLiteral) {%>
                <input id="inputValue<%=i%>" name="inputValue<%=i%>" type="text"
                       value="<%=value%>" class="esb-edit"/>
                <% } else if (synapseXPath != null) {%>
                <input id="inputValue<%=i%>" name="inputValue<%=i%>" type="text"
                       value="<%=synapseXPath.toString()%>" class="esb-edit"/>
                <% } else { %>
                <input id="inputValue<%=i%>" name="inputValue<%=i%>" type="text"
                       class="esb-edit"/>
                <% }%>
            </td>
            <td id="nsEditorButtonTD<%=i%>"
                style="<%=synapseXPath == null?"display:none;":""%>">
                <% if (!isLiteral && synapseXPath != null) {
                    System.out.println("Executed inside...");
                %>
                <script type="text/javascript">
                    document.getElementById("ns-edior-th").style.display = "";
                </script>
                <a href="#nsEditorLink" class="nseditor-icon-link" style="padding-left: 40px"
                   onclick="showNameSpaceEditor('inputValue<%=i%>')">
                    <fmt:message key="mediator.salesforce.namespace"/>
                </a>
                <% }%>
            </td>
        </tr>
        <%
                    i++;
                }//end if
            } // end for
        %>
        <tr style="display: none;">
            <td colspan="4">
                <input type="hidden" name="inputCount" id="inputCount" value="<%=i%>"/>
                <script type="text/javascript">
                    if (isRemainPropertyExpressions()) {
                        resetDisplayStyle("");
                    }
                </script>
            </td>
        </tr>
        </tbody>
    </table>

    <!--  x-->
    <br></br>
    <table id="outputstable" style="<%=outputTableStyle%>;" class="normal" border="0">
        <thead>
        <tr>
            <th width="15%"><fmt:message key="mediator.salesforce.outputName"/></th>
            <th width="15%"><fmt:message key="mediator.salesforce.outputValue"/></th>
            <th><fmt:message key="mediator.salesforce.outputSourceExp"/></th>
            <th width="15%" id="ns-edior-th" style=""><fmt:message
                    key="mediator.salesforce.nsEditor"/></th>
            <th><fmt:message key="mediator.salesforce.outputTargetExp"/></th>
            <th width="15%" id="ns-edior-th" style=""><fmt:message
                    key="mediator.salesforce.nsEditor"/></th>
        </tr>
        </thead>
        <tbody id="outputbody">
        <%
            //populate and display UI if outputs are available
            i = 0;
            for (OutputType output : outputs) {
                OutputTypeUI uiType = handler.getOutputUIObject(output);
                String targetKey = output.getTargetKey() == null ? "" : output.getTargetKey();
                String outputName = output.getName();
                SynapseXPath sourceXapth = output.getSourceXPath();
                SynapseXPath targetXapth = output.getTargetXPath();
                if (output != null) {
        %>
        <tr id="outputRaw<%=i%>">
            <td>
                <%=uiType.getDisplayName()%>
                <input type="hidden" name="outputName_hidden<%=i%>" id="outputName_hidden<%=i%>"
                       value="<%=outputName%>"/>
            </td>

            <td>
                <input type="text" name="outputKeyValue<%=i%>" id="outputKeyValue<%=i%>"
                       class="esb-edit small_textbox" value="<%=targetKey%>"/>
            </td>

                <%--source xpath expr--%>
            <td>
                <input id="outputSrcValue<%=i%>" name="outputSrcValue<%=i%>" type="text"
                       value="<%=sourceXapth==null?"":sourceXapth.toString()%>"
                       class="esb-edit"/>
            </td>
            <td id="nsEditorSrcButtonTD<%=i%>"
                style="">
                <script type="text/javascript">
                    document.getElementById("ns-edior-th").style.display = "";
                </script>
                <a href="#nsEditorLink" class="nseditor-icon-link" style="padding-left: 40px"
                   onclick="showNameSpaceEditor('outputSrcValue<%=i%>')">
                    <fmt:message key="mediator.salesforce.namespace"/>
                </a>
            </td>

                <%--target xpath expr--%>
            <td>
                <input id="outputTargetValue<%=i%>" name="outputTargetValue<%=i%>" type="text"
                       value="<%=targetXapth==null?"":targetXapth.toString()%>"
                       class="esb-edit"/>
            </td>
            <td id="nsEditorTargetButtonTD<%=i%>"
                style="">
                <script type="text/javascript">
                    document.getElementById("ns-edior-th").style.display = "";
                </script>
                <a href="#nsEditorLink" class="nseditor-icon-link" style="padding-left: 40px"
                   onclick="showNameSpaceEditor('outputTargetValue<%=i%>')">
                    <fmt:message key="mediator.salesforce.namespace"/>
                </a>
            </td>
        </tr>
        <%
                }
                i++;
            }
        %>

        <tr style="display: none;">
            <td colspan="4">
                <input type="hidden" name="outputCount" id="outputCount" value="<%=i%>"/>
            </td>
        </tr>
        </tbody>
    </table>

    <!-- x -->
    <a name="nsEditorLink"></a>

    <div id="nsEditor" style="display:none;"></div>
</div>
</fmt:bundle>