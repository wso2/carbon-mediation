<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.machineLearner.ui.MLMediator" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.jaxen.JaxenException" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof MLMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    MLMediator mlMediator = (MLMediator) mediator;
    mlMediator.getFeatureMappings().clear(); // to avoid duplicates
    String featureCountStr = request.getParameter("featureCount");

    if (featureCountStr != null && !"".equals(featureCountStr)) {
        int featureCount = Integer.parseInt(featureCountStr.trim());
        for (int i = 0; i < featureCount; i++) {
            String feature = request.getParameter("featureName" + i);
            String expression = request.getParameter("featureXpath" + i);
            mlMediator.addFeatureMapping(feature, expression);
        }
    }

    String predictionXpath = request.getParameter("predictionXpath");
    mlMediator.setPredictionExpression(predictionXpath);
%>
