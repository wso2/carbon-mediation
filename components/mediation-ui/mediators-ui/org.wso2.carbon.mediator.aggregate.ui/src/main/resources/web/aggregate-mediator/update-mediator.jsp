<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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

<%@ page import="org.wso2.carbon.mediator.aggregate.AggregateMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.mediators.Value"%>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory"%>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    XPathFactory xPathFactory = XPathFactory.getInstance();
    if (!(mediator instanceof AggregateMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    String aggregate_expr = request.getParameter("aggregate_expr");
    String complete_time = request.getParameter("complete_time");
    String complete_max = request.getParameter("complete_max");
    String complete_min = request.getParameter("complete_min");
    String correlate_exp = request.getParameter("correlate_expr");
    String sequenceOption = request.getParameter("sequenceOption");

    AggregateMediator aggregateMediator = (AggregateMediator) mediator;
  
    
    if ("selectFromRegistry".equals(sequenceOption)) {
        String selectFromRegistry = request.getParameter("mediator.sequence");
        aggregateMediator.setOnCompleteSequenceRef(selectFromRegistry);
        int size = aggregateMediator.getList().size();
        for (int i = 0; i < size; i++) {
            aggregateMediator.removeChild(i);                   
        }
    } else {
        aggregateMediator.setOnCompleteSequenceRef(null);
    }
    if(!complete_time.equals("")){
        long value = Long.parseLong(complete_time);
        aggregateMediator.setCompletionTimeoutSec(value);
    } else {
        aggregateMediator.setCompletionTimeoutSec(0);
    }

    String msgMinType = request.getParameter("msgMinType");
    boolean ismsgMinType = msgMinType != null && "expression".equals(msgMinType.trim());
    Value msgMinTypeEx =null;
     if(ismsgMinType){
          //aggregateMediator.setMaxMessagesToComplete(xPathFactory.createSynapseXPath("complete_max", request, session));
          msgMinTypeEx = new Value(xPathFactory.createSynapseXPath("msgMinType", complete_min.trim(), session));
      }else{
    	  msgMinTypeEx = new Value(complete_min);
      }
     aggregateMediator.setMinMessagesToComplete(msgMinTypeEx);
      
     String msgMaxType = request.getParameter("msgMaxType");
     boolean ismsgMaxType = msgMaxType != null && "expression".equals(msgMaxType.trim());
     Value msgMaxTypeEx =null;
     if(ismsgMaxType){
         //aggregateMediator.setMaxMessagesToComplete(xPathFactory.createSynapseXPath("complete_max", request, session));
         msgMaxTypeEx = new Value(xPathFactory.createSynapseXPath("msgMaxType", complete_max.trim(), session));
     }else{
    	 msgMaxTypeEx = new Value(complete_max);
     }
     aggregateMediator.setMaxMessagesToComplete(msgMaxTypeEx);
      
//    if(!complete_max.equals("")){
//    aggregateMediator.setMaxMessagesToComplete(Integer.parseInt(complete_max));
//} else {
//    aggregateMediator.setMaxMessagesToComplete(-1);
//}
//if(!complete_min.equals("")){
//    aggregateMediator.setMinMessagesToComplete(Integer.parseInt(complete_min));
//} else {
//    aggregateMediator.setMinMessagesToComplete(-1);
//}
      
     
       if(!aggregate_expr.equals("")){
             aggregateMediator.setAggregationExpression(xPathFactory.createSynapseXPath("aggregate_expr", request, session));
       }
       
      if(!correlate_exp.equals("")){
            aggregateMediator.setCorrelateExpression(xPathFactory.createSynapseXPath("correlate_expr", request, session));
      } else {
            aggregateMediator.setCorrelateExpression(null);
      }
      
      if (request.getParameter("mediator.aggregate.id") != null && 
    				  !request.getParameter("mediator.aggregate.id").trim().
						equals("")) {
    		aggregateMediator.setId(request.getParameter("mediator.aggregate.id"));
	  }

    if (request.getParameter("mediator.aggregate.enclose.element.property.name") != null &&
        !request.getParameter("mediator.aggregate.enclose.element.property.name").trim().
                equals("")) {
        aggregateMediator.setEnclosingElementPropertyName(request.getParameter("mediator.aggregate.enclose.element.property.name"));
    } else {
        aggregateMediator.setEnclosingElementPropertyName(null);
    }

%>

