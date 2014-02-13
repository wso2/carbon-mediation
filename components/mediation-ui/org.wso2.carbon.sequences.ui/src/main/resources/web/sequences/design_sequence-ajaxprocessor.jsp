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


<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.mediator.service.builtin.SequenceMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.AbstractListMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.SequenceEditorConstants" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>

<fmt:bundle basename="org.wso2.carbon.sequences.ui.i18n.Resources">

<%
    String mediatorAction = request.getParameter("mediatorAction");
    SequenceMediator editingSequence = SequenceEditorHelper.getEditingSequence(session);
    String mediatoPosition = "";    
    if (mediatorAction.equals("add")) {        
        String sequenceName = request.getParameter("sequenceName");
        String onErrorKey = request.getParameter("onErrorKey");
        String mediatorName = request.getParameter("mediatorName");
        String position = request.getParameter("position");
        String type = request.getParameter("type");
        if (sequenceName != null && !"".equals(sequenceName)) {
            editingSequence.setName(sequenceName);
        }

        if (onErrorKey != null && !"".equals(onErrorKey)) {
            editingSequence.setErrorHandler(onErrorKey);
        }

        org.wso2.carbon.mediator.service.ui.Mediator mediator = SequenceEditorHelper.getNewMediator(mediatorName);
        if (SequenceEditorConstants.ADD_CHILD_TYPE.equals(type)) {
            AbstractListMediator parent = (AbstractListMediator) SequenceEditorHelper.getMediatorAt(
                    editingSequence, position.substring(9)
            );
            parent.addChild(mediator);
            if (position.equals("mediator-00")) {
                mediatoPosition = "mediator-" + (parent.getList().size() - 1);
            } else {
                mediatoPosition = position + "." + (parent.getList().size() - 1);
            }
        } else if (SequenceEditorConstants.ADD_SYBLING_TYPE.equals(type)) {
            String newPosition = position.substring(9);
            int index = newPosition.lastIndexOf(".");
            if (index != -1) {
                AbstractListMediator parent = (AbstractListMediator) SequenceEditorHelper.getMediatorAt(
                        editingSequence, newPosition.substring(0, index)
                );
                parent.addChild(mediator);
                mediatoPosition = position.substring(0, position.lastIndexOf(".")) + "." + (parent.getList().size() - 1);
            } else {
                editingSequence.addChild(mediator);
                mediatoPosition = "mediator-" + (editingSequence.getList().size() - 1);
            }
        } else {
            // TODO error handling
        }
    } else {
        String mediatorPosition = request.getParameter("mediatorID");
        if ("delete".equals(mediatorAction)) {
            SequenceEditorHelper.deleteMediatorAt(mediatorPosition, session);
            SequenceEditorHelper.clearSessionCache(session);
        } else if ("moveup".equals(mediatorAction)) {
            SequenceEditorHelper.moveMediatorUp(mediatorPosition, session);
        } else if ("movedown".equals(mediatorAction)) {
            SequenceEditorHelper.moveMediatorDown(mediatorPosition, session);
        }
    }
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
%>
<div style="position:absolute;padding:20px;">

    <ul class="root-list" id="sequenceTree">

        <li>
            <div class="minus-icon" onclick="treeColapse(this)" id="treeColapser"></div>
            <div class="mediators" id="mediator-00">
                <a class="root-mediator"><fmt:message key="sequence.root.text"/></a>

                <div class="sequenceToolbar" style="width:100px;">
                    <div>
                        <a class="addChildStyle"><fmt:message key="sequence.add.child.action"/></a>
                    </div>
                </div>
            </div>
<%
    int count = editingSequence.getList().size();
    if (count != 0) {
%>

<div class="branch-node"></div>
<ul class="child-list">
    <%
        int pos = 0;
        for (Mediator mediator : editingSequence.getList()) {
            count--;
            Mediator beforeMed = pos > 0 ? editingSequence.getList().get(pos - 1) : null;
            Mediator afterMed = pos + 1 < editingSequence.getList().size() ? editingSequence.getList().get(pos + 1) : null;
    %>
    <%=SequenceEditorHelper.getMediatorHTML(mediator, count == 0, String.valueOf(pos), config, beforeMed, afterMed, request.getLocale())%>
    <%
            pos++;
        }
    %>
</ul>
<%
    }
%>
        </li>

    </ul>
    <% if (mediatorAction.equals("add")) { %>
    <input id="addMediatorPosition" type="hidden" value="<%=mediatoPosition%>"/>
    <% } %>
</div>

</fmt:bundle>