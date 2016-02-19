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
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.datamapper.ui.DataMapperMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof DataMapperMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    DataMapperMediator dataMapperMediator = (DataMapperMediator) mediator;

    Value configKey = new Value(request.getParameter("configKey"));
    if(configKey != null) {
        System.out.println("configKey" + configKey + "|" + new Value(request.getParameter("configKey")));
        dataMapperMediator.setConfigurationKey(configKey);
    }
    Value inputSchema = new Value(request.getParameter("inputSchema"));
    if(inputSchema != null) {
        System.out.println("inputSchema" + configKey);
        dataMapperMediator.setInputSchemaKey(inputSchema);
    }
    Value outputSchema = new Value(request.getParameter("outputSchema"));
    if(outputSchema != null) {
        System.out.println("inputSchema" + configKey);
        dataMapperMediator.setOutputSchemaKey(outputSchema);
    }

    System.out.println(request.getParameter("mediator.datamapper.inputType"));
    System.out.println(request.getParameter("mediator.datamapper.outputType"));
    dataMapperMediator.setInputType(Integer.parseInt(request.getParameter("mediator.datamapper.inputType")));
    dataMapperMediator.setOutputType(Integer.parseInt(request.getParameter("mediator.datamapper.outputType")));
    //dataMapperMediator.getProperties().clear(); // to avoid duplicates

%>

