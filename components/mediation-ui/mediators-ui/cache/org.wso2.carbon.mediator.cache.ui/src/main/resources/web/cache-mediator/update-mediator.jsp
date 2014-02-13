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

<%@ page import="org.wso2.caching.digest.DOMHASHGenerator" %>
<%@ page import="org.wso2.carbon.mediator.cache.CacheMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<%! public boolean notNullChecker(String strChecker){

    if(strChecker==null){
        return false;
    }
    else if(strChecker!=null){
        return true;
    }
    else if((!(strChecker.equalsIgnoreCase("")))){
        return true;
    }
   return false;
}%>
<%
    final int DEFAUTLINMEMSIZE = 1000;
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CacheMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CacheMediator cacheMediator = (CacheMediator) mediator;
    String cacheid = request.getParameter("cacheId");
    String cacheScope = request.getParameter("cacheScope");
    String cacheType = request.getParameter("cacheType");
    String cacheTimeout = request.getParameter("cacheTimeout");
    String maxMsgSize = request.getParameter("maxMsgSize");
    String impType = request.getParameter("impType");
    String maxSize = request.getParameter("maxSize");
    String sequenceOption = request.getParameter("sequenceOption");

    if(notNullChecker(cacheid)){
        cacheMediator.setId(cacheid);
    }
    if(notNullChecker(cacheScope)){
        cacheMediator.setScope(cacheScope.toLowerCase());
    }
    if(notNullChecker(cacheType)){
        if(cacheType.equalsIgnoreCase("Collector")){
            cacheMediator.setCollector(true);
        }
        else{
            cacheMediator.setCollector(false);
        }
    }
    if(notNullChecker(cacheTimeout)){
        try{
        cacheMediator.setTimeout(Long.parseLong(cacheTimeout));
        }
        catch(NumberFormatException e){

        }
    }
    if(notNullChecker(maxMsgSize)){
        try{
        cacheMediator.setMaxMessageSize(Integer.parseInt(maxMsgSize));
        }
        catch(NumberFormatException e){

        }
    }
    if(notNullChecker(impType)){
        try {
            cacheMediator.setInMemoryCacheSize(Integer.parseInt(maxSize));
        } catch (NumberFormatException e) {
            cacheMediator.setInMemoryCacheSize(DEFAUTLINMEMSIZE); //set the default
        }
    }


    DOMHASHGenerator domGen = new DOMHASHGenerator();
    cacheMediator.setDigestGenerator(domGen.getClass().getName());

    if ("selectFromRegistry".equals(sequenceOption)) {
        String selectFromRegistry = request.getParameter("mediator.sequence");
        cacheMediator.setOnCacheHitRef(selectFromRegistry);
        int size = cacheMediator.getList().size();
        for (int i = 0; i < size; i++) {
            cacheMediator.removeChild(i);
        }
    } else {
        cacheMediator.setOnCacheHitRef(null);
    }

    //String cacheScope = request.getParameter("")

    // todo : data collection from the edit jsp
%>

