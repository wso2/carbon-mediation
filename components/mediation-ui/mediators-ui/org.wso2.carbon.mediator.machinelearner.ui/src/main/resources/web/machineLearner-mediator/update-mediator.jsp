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
    String featureCount = request.getParameter("inputVariablesCount");

    if (featureCount != null && !"".equals(featureCount)) {
        int variableCount = Integer.parseInt(featureCount.trim());
        for (int i = 0; i < variableCount; i++) {
            String feature = request.getParameter("variableName" + i);
            String expression = request.getParameter("inputXpath" + i);
            mlMediator.addFeatureMapping(feature, expression);
        }
    }

    String responseXpath = request.getParameter("responseXpath");
    mlMediator.setPredictionExpression(responseXpath);
%>
