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

<%@ page import="org.wso2.carbon.mediation.service.templates.TemplateMediator" %>
<%@ page import="java.util.ArrayList" %>

<%

    //System.out.println("/tempaltes param processor.....");
    if (session.getAttribute("editingSequence") instanceof TemplateMediator) {
        //System.out.println("including param .....");

        TemplateMediator template = (TemplateMediator) session.getAttribute("editingSequence");
        String paramCount = request.getParameter("paramCount");
        String paramAction = request.getParameter("paramAction");
        //System.out.println("param count ....." + paramCount + " param action ....." + paramAction);

        if (paramAction == null || (paramAction != null && "add".equals(paramAction))) {
            int count = Integer.parseInt(paramCount);
            ArrayList paramList = new ArrayList();
            for (int i = 0; i < count; i++) {
                String paramName = request.getParameter("param" + i);
                if (paramName != null && !"".equals(paramName.trim())) {
                    if (!(paramList.contains(paramName.toString())))
                    {
                        paramList.add(paramName);
                    }

                }
                //System.out.println("param name ....." + paramName);
            }
            template.setParameters(paramList);
        }
    }


%>