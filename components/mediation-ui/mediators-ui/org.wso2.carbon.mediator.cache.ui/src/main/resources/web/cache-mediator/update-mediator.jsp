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

<%@ page import="org.wso2.carbon.mediator.cache.ui.CacheMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.mediator.cache.CachingConstants" %>

<%! public boolean notNullChecker(String strChecker) {

    if (strChecker == null) {
        return false;
    } else {
        return !strChecker.isEmpty();
    }
}%>
<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof CacheMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CacheMediator cacheMediator = (CacheMediator) mediator;
    String cacheType = request.getParameter("cacheType");
    String protocolType = request.getParameter("protocolType").trim();
    String cacheTimeout = request.getParameter("cacheTimeout").trim();
    String maxMsgSize = request.getParameter("maxMsgSize").trim();
    String methods = request.getParameter("methods");
    String headersToExclude = request.getParameter("headersToExclude");
    String responseCodes = request.getParameter("responseCodes");
    String hashGen = request.getParameter("hashGen");
    String maxSize = request.getParameter("maxSize").trim();
    String sequenceOption = request.getParameter("sequenceOption");

    if (notNullChecker(cacheType)) {
        if (cacheType.equalsIgnoreCase("Collector")) {
            cacheMediator.setCollector(true);
        } else {
            cacheMediator.setCollector(false);
        }
    }

    if (notNullChecker(cacheTimeout)) {
        try {
            cacheMediator.setTimeout(Long.parseLong(cacheTimeout));
        } catch (NumberFormatException e) {
            //This is handled in the UI validation in mediator-util.js
        }
    } else {
        cacheMediator.setTimeout(CachingConstants.DEFAULT_TIMEOUT);
    }

    if (notNullChecker(maxMsgSize)) {
        try {
            cacheMediator.setMaxMessageSize(Integer.parseInt(maxMsgSize));
        } catch (NumberFormatException e) {
            //This is handled in the UI validation in mediator-util.js
        }
    } else {
        cacheMediator.setMaxMessageSize(CachingConstants.DEFAULT_SIZE);
    }

    if (notNullChecker(protocolType)) {
        cacheMediator.setProtocolType(protocolType);
    } else {
        cacheMediator.setProtocolType(CachingConstants.HTTP_PROTOCOL_TYPE);
    }

    if (notNullChecker(methods)) {
        cacheMediator.setHTTPMethodsToCache(methods);
    } else {
        cacheMediator.setHTTPMethodsToCache(CachingConstants.ALL);
    }

    if (notNullChecker(headersToExclude)) {
        cacheMediator.setHeadersToExcludeInHash(headersToExclude);
    } else {
        cacheMediator.setHeadersToExcludeInHash("");
    }

    if (notNullChecker(responseCodes)) {
        cacheMediator.setResponseCodes(responseCodes);
    } else {
        cacheMediator.setResponseCodes(CachingConstants.ANY_RESPONSE_CODE);
    }

    if (notNullChecker(hashGen)) {
        cacheMediator.setDigestGenerator(hashGen);
    } else {
        cacheMediator.setDigestGenerator(CachingConstants.DEFAULT_HASH_GENERATOR.getClass().toString());
    }

    if (notNullChecker(maxSize)) {
        try {
            cacheMediator.setInMemoryCacheSize(Integer.parseInt(maxSize));
        } catch (NumberFormatException e) {
            //This is handled in the UI validation in mediator-util.js
        }
    } else {
        cacheMediator.setInMemoryCacheSize(CachingConstants.DEFAULT_SIZE);
    }

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
%>

