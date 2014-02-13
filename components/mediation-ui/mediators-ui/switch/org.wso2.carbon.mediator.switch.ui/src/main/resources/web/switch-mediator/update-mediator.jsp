<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.switchm.SwitchMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseJsonPath" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%! private boolean nullChecker(String strChecker){

    if(strChecker!=null || (!(strChecker.equalsIgnoreCase("")))){
        return true;
    }
   return false;
}%>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof SwitchMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to update the mediator");
    }

    SwitchMediator switchMediator = (SwitchMediator) mediator;

    String source = request.getParameter("sourceXPath");
    
           boolean error = false;
         XPathFactory xPathFactory = XPathFactory.getInstance();
      if(!source.equals("")){
          try{
            if(request.getParameter("sourceXPath").trim().startsWith("json-eval(")) {
                SynapsePath path = new SynapseJsonPath(request.getParameter("sourceXPath").trim().substring(10, request.getParameter("sourceXPath").trim().length() - 1));
                switchMediator.setSource(path);
            } else {
                switchMediator.setSource(xPathFactory.createSynapseXPath("sourceXPath", request, session));
            }
          }
          catch(Exception e){
               error=true;
              %>
            CARBON.showErrorDialog('<%=e.getMessage()%>');
        <%
          }
      }
      



    // todo : data collection from the edit jsp
%>

