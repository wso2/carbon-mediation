<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.machineLearner.ui.MLMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.wso2.carbon.ml.commons.domain.Feature" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.mediator.machineLearner.ui.MLMediatorConstants" %>
<%@ page import="org.wso2.carbon.ml.core.exceptions.MLModelBuilderException" %>
<%@ page import="org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException" %>
<%@ page import="org.wso2.carbon.mediator.machineLearner.ui.util.MLMediatorUtils" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:bundle basename="org.wso2.carbon.mediator.machineLearner.ui.i18n.Resources">
<div>
    <script type="text/javascript" src="../rule-mediator/js/mediator-util.js"></script>
    <script type="text/javascript" src="../sequences/js/ns-editor.js"></script>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof org.wso2.carbon.mediator.machineLearner.ui.MLMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    org.wso2.carbon.mediator.machineLearner.ui.MLMediator mlMediator = (org.wso2.carbon.mediator.machineLearner.ui.MLMediator) mediator;
    if ("true".equals(request.getParameter("clearAll"))) {
        mlMediator.getFeatureMappings().clear();
    }
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    boolean modelNotFound = false;
    List<Feature> inputVariables = null;
    response.setHeader("Cache-Control", "no-cache");
    String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext context =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    // TODO
    //CommandMediatorAdminClient client = new CommandMediatorAdminClient(cookie,
    //        backEndServerURL, context, request.getLocale());
    String modelName = request.getParameter("mediatorInput");
    mlMediator.setModelName(modelName);
    //Map<String,Object> staticSetterProps = pojoCommandMediator.getStaticSetterProperties();
    //Map<String,SynapseXPath> messageSetterProps = pojoCommandMediator.getMessageSetterProperties();
    //Map<String, String> contextSetterProps = pojoCommandMediator.getContextSetterProperties();
    //Map<String, SynapseXPath> messageGetterProps = pojoCommandMediator.getMessageGetterProperties();
    //Map<String, String> contextGetterProps = pojoCommandMediator.getContextGetterProperties();

//    // TODO
//    inputVariables = new String[3];
//    inputVariables[0] = "height";
//    inputVariables[1] = "weight";
//    inputVariables[2] ="age";

    try {
//        inputVariables = client.getFeatureMappings(request.getParameter("mediatorInput"));
        inputVariables = org.wso2.carbon.mediator.machineLearner.ui.util.MLMediatorUtils.getFeaturesOfModel(request.getParameter("mediatorInput"));
    } catch (Exception e) {
        modelNotFound = true; // TODO:do nothing, set class not found exception, may be we should be more specific
    }
    if (inputVariables != null && inputVariables.size() > 0 && inputVariables.get(0) != null && !modelNotFound) {
%>
    <h3 id="titleLabel" class="mediator"><fmt:message key="mediator.ml.features"/></h3>

    <div style="margin-top:0px;">
        <table id="inputVariablesTable" class="styledInner">
            <thead>
                <tr>
                    <th><fmt:message key="mediator.ml.feature.name"/></th>
                    <th><fmt:message key="mediator.ml.expression"/></th>
                    <th><fmt:message key="mediator.ml.Action"/></th>
                </tr>
            </thead>
                <tbody>
                    <%
                        int index;
                        for (index = 0; index < inputVariables.size()-1; index++) {
                            String variableName = inputVariables.get(index).getName();
                            boolean hasValue = true;
                    %>
                    <tr>
                        <td style="vertical-align:top !important">
                            <input type="hidden" name="variableName<%= index%>" id="variableName<%= index%>"
                                   value="<%=variableName%>"/><%= variableName%>
                        </td>

                        <!--
                        <td style="vertical-align:top !important">
                            <%
                                String xPath = "expression";
                                //SynapseXPath xPath;
                                //hasValue = (xPath = messageSetterProps.get(variableName)) != null ||
                                //        (xPath = messageGetterProps.get(variableName)) != null;
                                //if (hasValue) {
                                //    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
                                //    nmspRegistrar.registerNameSpaces(xPath,
                                //            "mediator.command.prop.xpath" + index, session);
                                //}
                            %>
                            <input type="text" name="mediator.command.prop.xpath<%=index%>"
                                   id="mediator.command.prop.xpath.id<%=index%>"
                                   <%= hasValue ? " value=\"" + xPath.toString() + "\"" : " disabled=\"true\""%>/>
                        </td>
                        -->

                        <td><input type="text" name="inputXpath<%=index%>" id="inputXpath<%=index%>"
                                                   class="esb-edit small_textbox"/>
                        </td>
                        <td><a href="#" class="icon-link" style="background-image:url(../admin/images/delete.gif);"
                                               onclick="deleteRow(this)"><fmt:message key="mediator.ml.delete.variable"/></a>
                        </td>

                    </tr>
                    <%
                        }
                    %>
                    <input type="hidden" name="inputVariablesCount" id="inputVariablesCount" value="<%=index%>"/>
                </tbody>
        </table>
    </div>
    <div style="margin-top:0px;">
        <table>
            <tbody>
                <tr>
                    <td><input type="hidden" name="response" id="response"
                               value="response"/><fmt:message key="mediator.ml.prediction"/></td>
                    <td><input type="text" name="responseXpath" id="responseXpath"
                               class="esb-edit small_textbox"/></td>
                </tr>
            </tbody>
        </table>
    </div>
            <%

                //TODO: let the error be more specific,
            } else if (inputVariables == null && !modelNotFound) {  // no input variables found

            %>
            <script type="text/javascript">
                CARBON.showErrorDialog('<fmt:message key="mediator.ml.invalidModelMsg"/>');
            </script>
            <%

            } else if (modelNotFound) {

            %>
            <script type="text/javascript">
                CARBON.showErrorDialog('<fmt:message key="mediator.ml.modelNotFoundMsg"/>');
            </script>
            <%

            } else if (!modelNotFound) {
            %>
            <script type="text/javascript">
                CARBON.showInfoDialog('<fmt:message key="mediator.ml.modelStorageLocation.loaded.success"/>');
            </script>
            <%

            }

            %>
</div>
</fmt:bundle>