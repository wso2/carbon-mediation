<%--
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
 --%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.xslt.XSLTMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%
    try {

        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof XSLTMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }
        XSLTMediator xsltMediator = (XSLTMediator) mediator;

        xsltMediator.setXsltKey(null);
        String keyVal;
        String keyExp;
        XPathFactory xPathFactory = XPathFactory.getInstance();

        String keyGroup = request.getParameter("keygroup");
        if (keyGroup != null && !keyGroup.equals("")) {
            if (keyGroup.equals("StaticKey")) {
                keyVal = request.getParameter("mediator.xslt.key.static_val");
                if (keyVal != null && !keyVal.equals("")) {
                    Value staticKey = new Value(keyVal);
                    xsltMediator.setXsltKey(staticKey);
                }
            } else if (keyGroup.equals("DynamicKey")) {
                keyExp = request.getParameter("mediator.xslt.key.dynamic_val");


                if (keyExp != null && !keyExp.equals("")) {
                    Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                            "mediator.xslt.key.dynamic_val", request.getParameter("mediator.xslt.key.dynamic_val"), session));
                    xsltMediator.setXsltKey(dynamicKey);
                }
            }
        }

        xsltMediator.setSource(xPathFactory.createSynapseXPath("mediator.xslt.source",
                request.getParameter("mediator.xslt.source"), session));

        String propertyCountParameter = request.getParameter("propertyCount");
        if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
            int propertyCount = 0;
            try {
                propertyCount = Integer.parseInt(propertyCountParameter.trim());
                xsltMediator.getProperties().clear();
                for (int i = 0; i <= propertyCount; i++) {
                    String name = request.getParameter("propertyName" + i);
                    if (name != null && !"".equals(name)) {
                        String valueId = "propertyValue" + i;
                        String value = request.getParameter(valueId);
                        String expression = request.getParameter("propertyTypeSelection" + i);
                        boolean isExpression = expression != null && "expression".equals(expression.trim());
                        MediatorProperty mp = new MediatorProperty();
                        mp.setName(name.trim());
                        if (value != null) {
                            if (isExpression) {
                                mp.setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                            } else if (value != null) {
                                mp.setValue(value.trim());
                            }
                        }
                        xsltMediator.addProperty(mp);
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String featureCountParameter = request.getParameter("featureCount");
        if (featureCountParameter != null && !"".equals(featureCountParameter)) {
            int featureCount = 0;
            try {
                featureCount = Integer.parseInt(featureCountParameter.trim());
                xsltMediator.getFeatures().clear();
                for (int i = 0; i <= featureCount; i++) {
                    String name = request.getParameter("featureName" + i);
                    if (name != null && !"".equals(name)) {
                        String value = request.getParameter("featureValue" + i);
                        if (value != null) {
                            xsltMediator.addFeature(name.trim(), Boolean.valueOf(value.trim()));                            
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        
        String resourceList = request.getParameter("resourceList");
		Map<String, String> resources = new HashMap<String, String>();
		Map<String, String> resourcesOld = xsltMediator.getResources(); //TODO need proper fix
		if (resourceList != null && !"".equals(resourceList)) {
			String[] resourceValues = resourceList.split("::");
			for (String resourceValue : resourceValues) {
				int index = resourceValue.indexOf(',');
				resources.put(resourceValue.substring(0, index),
				              resourceValue.substring(index + 1));
				resources.putAll(resourcesOld); //put already available resources also
			}
			xsltMediator.setResources(resources);
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
