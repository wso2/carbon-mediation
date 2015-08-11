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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.switchm.SwitchCaseMediator" %>
<%@ page import="org.wso2.carbon.mediator.switchm.SwitchMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="java.net.URLDecoder" %>

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

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SwitchMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    SwitchMediator switchMediator = (SwitchMediator) mediator;
    switchMediator.addChild(new SwitchCaseMediator());

    String source = URLDecoder.decode(request.getParameter("src"), "UTF-8");

    if (source != null) {
        boolean error = false;
        XPathFactory xPathFactory = XPathFactory.getInstance();
        if(!source.equals("")){
            try{
                if(request.getParameter("src").trim().startsWith("json-eval(")) {
                    SynapsePath path = new SynapseJsonPath(request.getParameter("src").trim()
                            .substring(10, request.getParameter("src").trim().length() - 1));
                    switchMediator.setSource(path);
                } else {
                    switchMediator.setSource(xPathFactory.createSynapseXPath("src", request, session));
                }
            }
            catch(Exception e){
                error=true;
            }
        }
    }
%>

<script type="text/javascript">
    document.location.href = "../sequences/design_sequence.jsp?ordinal=1";
</script>

