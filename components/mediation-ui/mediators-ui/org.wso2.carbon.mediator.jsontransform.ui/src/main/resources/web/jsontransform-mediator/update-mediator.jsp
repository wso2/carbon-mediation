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
<%@ page import="org.wso2.carbon.mediator.jsontransform.JSONTransformMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.jaxen.JaxenException" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    MediatorProperty meditorProp = null;
    if (!(mediator instanceof JSONTransformMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    JSONTransformMediator jsonTransformMed = (JSONTransformMediator) mediator;
    jsonTransformMed.getProperties().clear(); // to avoid duplicates
    
    String propertyCountParameter = request.getParameter("propertyCount");
    if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
        int propertyCount = 0;
        try {
            propertyCount = Integer.parseInt(propertyCountParameter.trim());
            for (int i = 0; i <= propertyCount; i++) {
                String name = request.getParameter("propertyName" + i);
                if (name != null && !"".equals(name)) {
                    String valueId = "propertyValue" + i;
                    String value = request.getParameter(valueId);
                    MediatorProperty mp = new MediatorProperty();
                    mp.setName(name.trim());
                    if (value != null) {
                        mp.setValue(value.trim());
                    }
                    jsonTransformMed.addProperty(mp);
                }
            }
        } catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        }
    }
    
    String keyVal = request.getParameter("mediator.jsontransform.key.static_val");
    if (keyVal != null && !StringUtils.isEmpty(keyVal)) {
        Value staticKey = new Value(keyVal);
        jsonTransformMed.setSchemaKey(staticKey);
    }
%>

