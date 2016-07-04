<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.validate.ValidateMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.synapse.mediators.Value" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ValidateMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    ValidateMediator validateMediator = (ValidateMediator) mediator;

    // set keys
    List<Value> schemaKeyList = new ArrayList<Value>();
    String keyCountParameter = request.getParameter("keyCount");
    XPathFactory xPathFactory = XPathFactory.getInstance();

    if (keyCountParameter != null && !"".equals(keyCountParameter)) {
        int keyCount = 0;
        try {
            keyCount = Integer.parseInt(keyCountParameter.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format");
        }
        for (int i = 0; i < keyCount; i++) {

            String keyType = request.getParameter("keyTypeSelection" + i);
            String keyValue = null;

            //Key validatorKey = null;
            if ("static".equals(keyType)) {
                keyValue = request.getParameter("keyValue" + i);
                if (keyValue != null && !keyValue.equals("")) {
                    Value staticKey = new Value(keyValue);
                    schemaKeyList.add(staticKey);
                }
            } else if ("dynamic".equals(keyType)) {
                keyValue = request.getParameter("keyValue" + i);
                if (keyValue != null && !keyValue.equals("")) {
                    Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                            "keyValue" + i, request.getParameter("keyValue" + i), session));
                    schemaKeyList.add(dynamicKey);
                }
            }
        }
        validateMediator.setSchemaKeys(schemaKeyList);
    }

    // set source
    String sourceParameter = request.getParameter("mediator.validate.source");

    if (sourceParameter != null && !"".equals(sourceParameter)) {
        if(sourceParameter.trim().startsWith("json-eval(")) {
             SynapsePath path = new SynapseJsonPath(sourceParameter.trim()
                                    .substring(10, sourceParameter.trim().length() - 1));
             validateMediator.setSource(path);
         } else {
             validateMediator.setSource(xPathFactory.createSynapseXPath("mediator.validate.source", sourceParameter.trim(), session));
         }
    } else if ("".equals(sourceParameter)) {
        validateMediator.setSource(null);
    }

    // set features
    String featureCountParameter = request.getParameter("featureCount");
    if (featureCountParameter != null && !"".equals(featureCountParameter)) {
        int featureCount = 0;
        try {
            featureCount = Integer.parseInt(featureCountParameter);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format");
        }
        // avoid duplications
        validateMediator.getFeatures().clear();
        for (int i = 0; i <= featureCount; i++) {
            String featureName = request.getParameter("featureName" + i);
            if (featureName != null && !"".equals(featureName)) {
                String value = request.getParameter("featureValue" + i);
                if (value != null) {
                    validateMediator.addFeature(featureName, Boolean.parseBoolean(value.trim()));
                }
            }
        }
    }
	
	String resourceList = request.getParameter("resourceList");
	Map<String, String> resources = new HashMap<String, String>();
	Map<String, String> resourcesOld =  validateMediator.getResources(); //TODO need proper fix
	if (resourceList != null && !"".equals(resourceList)) {
		String[] resourceValues = resourceList.split("::");
		for (String resourceValue : resourceValues) {
			int index = resourceValue.indexOf(',');
			resources.put(resourceValue.substring(0, index),
			              resourceValue.substring(index + 1));
			resources.putAll(resourcesOld); //put already available resources also
		}
		 validateMediator.setResources(resources);
	}

%>

