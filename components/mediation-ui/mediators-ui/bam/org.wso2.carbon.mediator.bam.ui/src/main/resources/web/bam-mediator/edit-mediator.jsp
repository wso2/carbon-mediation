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

<%@ page import="org.wso2.carbon.mediator.bam.ui.BamMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%!
    public static final String SERVER_PROFILE_LOCATION = "bamServerProfiles";
%>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    
    if (!(mediator instanceof BamMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    BamMediator bamMediator = (BamMediator) mediator;

    String serverProfilePath = "";
    String streamName = "";
    String streamVersion = "";


    if(bamMediator.getServerProfile() != null){
        serverProfilePath = bamMediator.getServerProfile();
    }

    if(bamMediator.getStreamName() != null){
        streamName = bamMediator.getStreamName();
    }

    if(bamMediator.getStreamVersion() != null){
        streamVersion = bamMediator.getStreamVersion();
    }

%>

<fmt:bundle basename="org.wso2.carbon.mediator.bam.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.bam.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="propertyMediatorJsi18n"/>
    <div>
        <script type="text/javascript" src="../bam-mediator/js/mediator-util.js"></script>
        <script type="text/javascript">

            function loadServerProfiles(serverProfileLocationPath, serverProfilePath) {
                jQuery.ajax({
                                type:"GET",
                                url:"../bam-mediator/dropdown_ajaxprocessor.jsp",
                                data:{action:"getServerProfiles", serverProfilePath:serverProfileLocationPath},
                                success:function(data){
                                    document.getElementById("serverProfileList").innerHTML = "";
                                    jQuery("#serverProfileList").append("<option>- Select Server Profile -</option>");
                                    jQuery("#serverProfileList").append(data);
                                    if(serverProfilePath != null && serverProfilePath != ""){
                                        document.getElementById("serverProfileList").value = serverProfilePath;
                                    }
                                }
                            })
            }

            function loadStreamNames(serverProfilePath, streamName) {
                jQuery.ajax({
                                type:"GET",
                                url:"../bam-mediator/dropdown_ajaxprocessor.jsp",
                                data:{action:"getStreamNames", serverProfilePath:serverProfilePath},
                                success:function(data){
                                    document.getElementById("streamNameList").innerHTML = "";
                                    jQuery("#streamNameList").append("<option>- Select Stream Name -</option>");
                                    jQuery("#streamNameList").append(data);
                                    if(streamName != null && streamName != ""){
                                        document.getElementById("streamNameList").value = streamName;
                                    }
                                    document.getElementById('streamNameList').disabled = "";
                                }
                            })
            }

            function loadStreamVersions(serverProfilePath, streamName, streamVersion) {
                jQuery.ajax({
                                type:"GET",
                                url:"../bam-mediator/dropdown_ajaxprocessor.jsp",
                                data:{action:"getStreamVersions", serverProfilePath:serverProfilePath, streamName:streamName},
                                success:function(data){
                                    document.getElementById("streamVersionList").innerHTML = "";
                                    jQuery("#streamVersionList").append("<option>- Select Stream Version -</option>");
                                    jQuery("#streamVersionList").append(data);
                                    if(streamVersion != null && streamVersion != "" && document.getElementById("streamVersionList").value != null){
                                        document.getElementById("streamVersionList").value = streamVersion;
                                    }
                                    document.getElementById('streamVersionList').disabled = "";
                                }
                            })
            }

            function onServerProfileSelected(parentPath){
                loadStreamNames(parentPath + "/" + document.getElementById('serverProfileList').value, "");
                document.getElementById('streamNameList').disabled = "";
            }

            function selectStreamVersionList(parentPath){
                loadStreamVersions(parentPath + "/" + document.getElementById('serverProfileList').value, document.getElementById('streamNameList').value);
                document.getElementById('streamVersionList').disabled = "";
            }

        </script>


        <table class="normal" width="100%">
            <tbody>
            <tr>
                <td colspan="4">
                    <h2><fmt:message key="bam.mediator"/></h2>
                </td>
            </tr>


            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="server.profile.header"/>
                    </h3>
                </td>
            </tr>

            <tr>
                <td>
                    <fmt:message key="server.profile"/><span class="required">*</span>
                </td>
                <td>
                    <select name="serverProfileList" id="serverProfileList" onchange="onServerProfileSelected('<%=SERVER_PROFILE_LOCATION%>')">
                        <option>- Select Server Profile -</option>
                    </select>
                    <script type="text/javascript">
                        loadServerProfiles("<%=SERVER_PROFILE_LOCATION%>", "<%=serverProfilePath%>");
                    </script>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="stream.configuration"/>
                    </h3>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="stream.name"/><span class="required">*</span>
                </td>
                <td>
                    <select name="streamNameList" id="streamNameList" disabled="disabled" onchange="selectStreamVersionList('<%=SERVER_PROFILE_LOCATION%>')">
                        <option>- Select Stream Name -</option>
                    </select>
                    <script type="text/javascript">
                        if("" != "<%=serverProfilePath%>"){
                            loadStreamNames("<%=SERVER_PROFILE_LOCATION%>" + "/" + "<%=serverProfilePath%>", "<%=streamName%>");
                        }
                    </script>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="stream.version"/><span class="required">*</span>
                </td>
                <td>
                    <select name="streamVersionList" id="streamVersionList" disabled="disabled">
                        <option>- Select Stream Version -</option>
                    </select>
                    <script type="text/javascript">
                        if("" != "<%=serverProfilePath%>"){
                            loadStreamVersions("<%=SERVER_PROFILE_LOCATION%>" + "/" + "<%=serverProfilePath%>", "<%=streamName%>", "<%=streamVersion%>");
                        }
                    </script>
                </td>
            </tr>
        </table>
    </div>
</fmt:bundle>