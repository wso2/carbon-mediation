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

<%@ page import="org.wso2.carbon.mediator.script.ScriptMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.mediators.Value" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.TreeMap" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ScriptMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator, not a valid ScriptMediator");

    }
    ScriptMediator scriptMediator =
            (ScriptMediator) mediator;
    scriptMediator.setFunction(null);
    scriptMediator.setKey(null);
    scriptMediator.setLanguage(null);
    scriptMediator.setScriptSourceCode(null);
    Map includes = scriptMediator.getIncludes();
    if (includes != null) {
        includes.clear();
    }
    String scriptType = request.getParameter("script_type");
    //language
    String language = request.getParameter("mediator.script.language");
    scriptMediator.setLanguage(language);
    //script type and registry keys
    if (scriptType.equals("inline")) {
        String sourceScipt = request.getParameter("mediator.script.source_script");
        scriptMediator.setScriptSourceCode(sourceScipt);
        scriptMediator.setKey(null);

    } else if (scriptType.equals("regKey")) {

        XPathFactory xPathFactory = XPathFactory.getInstance();

        scriptMediator.setKey(null);
        String keyVal;
        String keyExp;

        String keyGroup = request.getParameter("keygroup");
        if (keyGroup != null && !keyGroup.equals("")) {
            if (keyGroup.equals("StaticKey")) {
                keyVal = request.getParameter("mediator.script.key.static_val");
                if (keyVal != null && !keyVal.equals("")) {
                    Value staticKey = new Value(keyVal);
                    scriptMediator.setKey(staticKey);
                }
            } else if (keyGroup.equals("DynamicKey")) {
                keyExp = request.getParameter("mediator.script.key.dynamic_val");


                if (keyExp != null && !keyExp.equals("")) {
                    Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                            "mediator.script.key.dynamic_val", request.getParameter(
                            "mediator.script.key.dynamic_val"), session));
                    scriptMediator.setKey(dynamicKey);
                }
            }
        }


        String function = request.getParameter("mediator.script.function");
        scriptMediator.setFunction(function);
        // add the include keys
        String includeKeyCountParameter = request.getParameter("includeKeyCount");
        Map includeKeyMap = new TreeMap();
        if(includeKeyCountParameter!=null && !"".equals(includeKeyCountParameter)){
            int includeKeyCount = Integer.parseInt(includeKeyCountParameter);
            for(int i = 0;i < includeKeyCount;i++){
                String includeKeyVal = request.getParameter("includeKey" + i);
                if(includeKeyVal!=null){
                    includeKeyMap.put(includeKeyVal,null);
                    
                }
            }
            scriptMediator.setIncludes(includeKeyMap);
        }
    }
%>

