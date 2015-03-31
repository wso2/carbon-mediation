<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.machinelearner.ui.MLMediator" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.jaxen.JaxenException" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof org.wso2.carbon.mediator.machinelearner.ui.MLMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    org.wso2.carbon.mediator.machinelearner.ui.MLMediator mlMediator = (org.wso2.carbon.mediator.machinelearner.ui.MLMediator) mediator;
    mlMediator.getFeatures().clear(); // to avoid duplicates
    String featureCountStr = request.getParameter("featureCount");
    XPathFactory xPathFactory = XPathFactory.getInstance();

    if (featureCountStr != null && !"".equals(featureCountStr)) {
        int featureCount = Integer.parseInt(featureCountStr.trim());
        for (int i = 0; i < featureCount; i++) {
            String feature = request.getParameter("featureName" + i);
            MediatorProperty mediatorProperty = new MediatorProperty();
            mediatorProperty.setName(feature);
            String valueId = "featureXpath" + i;
            String value = request.getParameter(valueId);

            if (value.trim().startsWith("json-eval(")) {
                SynapseXPath jsonPath =
                        new SynapseXPath(value.trim().substring(10, value.length() - 1));
                mediatorProperty.setExpression(jsonPath);
            } else {
                mediatorProperty.setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
            }
            mlMediator.addFeature(mediatorProperty);
        }
    }

    String predictionXpath = request.getParameter("predictionXpath");
    mlMediator.setPredictionPropertyName(predictionXpath);
%>
