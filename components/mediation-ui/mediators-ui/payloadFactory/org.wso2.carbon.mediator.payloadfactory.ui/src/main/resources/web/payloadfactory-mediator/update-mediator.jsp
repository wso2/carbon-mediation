<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.payloadfactory.PayloadFactoryMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.apache.synapse.config.xml.SynapseJsonPathFactory" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof PayloadFactoryMediator)) {
        CarbonUIMessage.sendCarbonUIMessage(
                "Unable to edit the mediator, expected: payloadFactoryMediator, found: " +
                mediator.getTagLocalName(), CarbonUIMessage.ERROR, request);
%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
        return;
    }
    PayloadFactoryMediator payloadFactoryMediator = (PayloadFactoryMediator) mediator;
    payloadFactoryMediator.getArgumentList().clear();

    // payloadFactoryMediator.setFormat(request.getParameter("payloadFactory.format"));
    String option = request.getParameter("pfFormat");
    if ("inline".equals(option)) {
        payloadFactoryMediator.setDynamic(false);
        payloadFactoryMediator.setFormat(request.getParameter("payloadFactory.format"));

    } else if ("registry".equals(option)) {
        payloadFactoryMediator.setDynamic(true);
        payloadFactoryMediator.setFormatKey(request.getParameter("registryKey"));
    }
    String type = request.getParameter("mediaType");
    if(type!=null){
        payloadFactoryMediator.setType(type);
    }

    int maxArgCount = Integer.parseInt(request.getParameter("argCount"));
    XPathFactory xPathFactory = XPathFactory.getInstance();

    for (int i = 0; i < maxArgCount; ++i) {
        PayloadFactoryMediator.Argument arg = new PayloadFactoryMediator.Argument();
        String argType = request.getParameter("argType" + i);
        if (argType == null) {
            continue;
        }
        if ("value".equals(argType)) {
            arg.setValue(request.getParameter("payloadFactory.argValue" + i).trim());
        } else if ("expression".equals(argType)) {
            String evaluator=request.getParameter("payloadFactory.argEval"+i).trim();
            if(evaluator!=null && "json".equals(evaluator)){
                arg.setJsonPath(SynapseJsonPathFactory.getSynapseJsonPath(request.getParameter("payloadFactory.argValue" + i).trim()));
            } else{
                try{
                arg.setExpression(xPathFactory.createSynapseXPath("payloadFactory.argValue" + i,
                                                           request, session));
                }catch (Exception e){
                    %>error:<%=e.getMessage()%><%
                }
            }

            arg.setEvaluator(evaluator);
        } else {
            CarbonUIMessage.sendCarbonUIMessage("Invalid argument type is found for payloadFactory " +
                                                "mediator", CarbonUIMessage.ERROR, request);
%>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
            return;
        }
        payloadFactoryMediator.addArgument(arg);
    }
%>
