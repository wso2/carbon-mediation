<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~   WSO2 Inc. licenses this file to you under the Apache License,
  ~   Version 2.0 (the "License"); you may not use this file except
  ~   in compliance with the License.
  ~   You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  --%>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.predict.ui.PredictMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.ml.commons.domain.Feature" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.mediator.predict.ui.util.PredictMediatorUtils" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:bundle basename="org.wso2.carbon.mediator.predict.ui.i18n.Resources">
<div>
    <script type="text/javascript" src="../rule-mediator/js/mediator-util.js"></script>
    <script type="text/javascript" src="../sequences/js/ns-editor.js"></script>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof PredictMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    PredictMediator predictMediator = (PredictMediator) mediator;
    if ("true".equals(request.getParameter("clearAll"))) {
        predictMediator.getFeatures().clear();
    }
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    boolean modelNotFound = false;
    List<Feature> features = null;
    String responseVariable = null;
    response.setHeader("Cache-Control", "no-cache");
    String modelName = request.getParameter("mediatorInput");
    predictMediator.setModelStorageLocation(modelName);

    try {
        features = PredictMediatorUtils.getFeaturesOfModel(request.getParameter("mediatorInput"));
        responseVariable = PredictMediatorUtils.getResponseVariable(request.getParameter("mediatorInput"));
    } catch (Exception e) {
        modelNotFound = true;
    }
    if (features != null && features.size() > 0 && features.get(0) != null && !modelNotFound) {
%>
    <h3 id="titleLabel" class="mediator"><fmt:message key="mediator.predict.features"/></h3>

    <div style="margin-top:0px;">
        <table id="inputVariablesTable" class="styledInner">
            <thead>
                <tr>
                    <th><fmt:message key="mediator.predict.feature.name"/></th>
                    <th><fmt:message key="mediator.predict.expression"/></th>
                    <th><fmt:message key="mediator.predict.namespaces"/></th>
                </tr>
            </thead>
                <tbody>
                    <%
                        int index = 0;
                        for (Feature feature : features) {
                            String featureName = feature.getName();
                            if(!featureName.equals(responseVariable)) {
                                SynapsePath synapsePath = ((PredictMediator) mediator).getExpressionForFeature(featureName);
                                String expression = null;
                                if(synapsePath != null) {
                                    expression = synapsePath.getExpression();
                                }
                                if(expression == null) {
                                    expression = "";
                                }
                    %>
                    <tr>
                        <td style="vertical-align:top !important">
                            <input type="hidden" name="featureName<%= index%>" id="featureName<%= index%>"
                                   value="<%=featureName%>"/><%= featureName%>
                        </td>
                        <td>
                            <input type="text" name="featureXpath<%=index%>" id="featureXpath<%=index%>" size="35"
                                   value="<%=expression%>"    class="esb-edit small_textbox"/>
                        </td>

                        <%--namespace editor--%>
                        <td id="nsEditorButtonTD<%=index%>">
                            <script type="text/javascript">
                                document.getElementById("ns-edior-th").style.display = "";
                            </script>
                            <a href="#nsEditorLink" class="nseditor-icon-link"
                               style="padding-left:40px"
                               onclick="showNameSpaceEditor('featureXpath<%=index%>')">
                                <fmt:message key="mediator.predict.namespaces"/></a>
                        </td>
                    </tr>
                    <%
                                index++;
                            }
                        }
                    %>
                    <input type="hidden" name="featureCount" id="featureCount" value="<%=index%>"/>
                </tbody>
        </table>
    </div>
    <div style="margin-top:20px;">
        <h3 id="predictionLabel" class="mediator"><fmt:message key="mediator.predict.prediction.output"/></h3>
        <table class="normal">
            <tbody>
                <tr>
                    <td><input type="hidden" name="response" id="response"
                               value="response"/><fmt:message key="mediator.predict.prediction.property"/></td>
                    <%
                        String predictionExpression = ((PredictMediator) mediator).getPredictionPropertyName();
                        if(predictionExpression == null) {
                            predictionExpression = "";
                        }
                    %>
                    <td><input type="text" name="predictionProperty" id="predictionProperty"
                               value="<%=predictionExpression%>" class="esb-edit small_textbox"/></td>
                </tr>
            </tbody>
        </table>
    </div>
            <%

            } else if (features == null && !modelNotFound) {

            %>
            <script type="text/javascript">
                CARBON.showErrorDialog('<fmt:message key="mediator.predict.invalid.model.msg"/>');
            </script>
            <%

            } else if (modelNotFound) {

            %>
            <script type="text/javascript">
                CARBON.showErrorDialog('<fmt:message key="mediator.predict.model.not.found.msg"/>');
            </script>
            <%

            } else if (!modelNotFound) {
            %>
            <script type="text/javascript">
                CARBON.showInfoDialog('<fmt:message key="mediator.predict.model.loaded.success"/>');
            </script>
            <%

            }

            %>
</div>
</fmt:bundle>