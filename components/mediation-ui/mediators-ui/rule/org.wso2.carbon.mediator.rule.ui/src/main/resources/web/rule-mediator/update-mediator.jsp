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

<%@ page import="org.wso2.carbon.mediator.rule.RuleMediator" %>
<%@ page import="org.wso2.carbon.mediator.rule.ui.internal.RuleMediatorClientHelper" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.rule.mediator.config.RuleMediatorConfig" %>
<%@ page import="org.wso2.carbon.rule.common.RuleSet" %>
<%@ page import="org.wso2.carbon.rule.common.Rule" %>

<%
    try {
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

        if (!(mediator instanceof RuleMediator)) {
            // todo : proper error handling
            throw new RuntimeException("Unable to edit the mediator");
        }

        RuleMediator ruleMediator = (RuleMediator) mediator;

        RuleMediatorConfig ruleMediatorConfig = ruleMediator.getRuleMediatorConfig();
        if (ruleMediatorConfig == null) {
            ruleMediatorConfig = new RuleMediatorConfig();
            ruleMediator.setRuleMediatorConfig(ruleMediatorConfig);
        }

        //Initialize RuleMediatorClientHelper
        RuleMediatorClientHelper.init(request);
        // set the source details
        RuleMediatorClientHelper.populateSource(request, ruleMediatorConfig);
        RuleMediatorClientHelper.populateTarget(request, ruleMediatorConfig);


        Rule rule;
        if (ruleMediatorConfig.getRuleSet().getRules().isEmpty()){
            rule = new Rule();
            ruleMediatorConfig.getRuleSet().getRules().add(rule);
        } else {
            rule = ruleMediatorConfig.getRuleSet().getRules().get(0);
        }

        String ruleType = request.getParameter("rule.script.type");
        if ((ruleType != null) && !ruleType.trim().equals("")){
            rule.setResourceType(ruleType);
        } else {
            rule.setResourceType("regular");
        }


        boolean isKey = "key".equals(request.getParameter("ruleScriptType"));
        boolean isURL = "url".equals(request.getParameter("ruleScriptType"));
        if (isKey) {
            String registryKey = request.getParameter("mediator.rule.key");
            rule.setSourceType("registry");
            rule.setValue(registryKey);

        } else if (isURL) {
            String ruleScriptURL = request.getParameter("mediator.rule.url");
            rule.setSourceType("url");
            rule.setValue(ruleScriptURL);

        } else {
            String ruleScriptID = SequenceEditorHelper.getEditingMediatorPosition(session) + "_rulescript";
            Map ruleScriptsMap = (Map) request.getSession().getAttribute("rulemediator_script_map");
            if (ruleScriptsMap != null) {
                String ruleScript = (String) ruleScriptsMap.get(ruleScriptID);
                rule.setSourceType("inline");
                rule.setValue(ruleScript);
            }
        }

        RuleMediatorClientHelper.setProperty(request, ruleMediatorConfig.getRuleSet(), "addCreationProperty", "creation");
//        RuleMediatorClientHelper.setProperty(request, session, setDescription,
//                "addRegistrationProperty", "registration");
//        RuleMediatorClientHelper.setProperty(request, session, setDescription,
//                "addDeregistrationProperty", "deregistration");

        RuleMediatorClientHelper.updateInputFacts(request, ruleMediatorConfig, "fact");
        RuleMediatorClientHelper.updateOutputFacts(request, ruleMediatorConfig, "result");
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
