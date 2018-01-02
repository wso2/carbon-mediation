<!--
 ~ Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="org.wso2.carbon.inbound.ui.internal.InboundDescription"%>
<%@page import="org.wso2.carbon.inbound.ui.internal.InboundManagementClient"%>
<%@page import="org.wso2.carbon.inbound.ui.internal.InboundClientConstants"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="localentrycommons.js"></script>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="registry-browser.js"></script>


<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="inboundcommon.js"></script>
<script type="text/javascript">
var iParamCount = 0;
var iParamMax = 0;
var classRequired = false;
var sequenceRequired=false;
var onErrorRequired=false;
var requiredParams = null;
var kafkaSpecialParameters = null;
</script>
<fmt:bundle basename="org.wso2.carbon.inbound.ui.i18n.Resources">
    <carbon:breadcrumb label="inbound.header.update"
                       resourceBundle="org.wso2.carbon.inbound.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <% InboundManagementClient client;
        try {
            client = InboundManagementClient.getInstance(config, session);
            InboundDescription inboundDescription = client.getInboundDescription(request.getParameter("name"));
            String inboundTypeValue = inboundDescription.getType();
            boolean isMBbased = false;
            
            if (InboundClientConstants.TYPE_JMS.equalsIgnoreCase(inboundTypeValue)) {
            	if (inboundDescription.getParameters().containsKey("connectionfactory.QueueConnectionFactory")) {
            		isMBbased = true;
            		inboundTypeValue = InboundClientConstants.TYPE_WSO2MB;
            		String connectionURL = inboundDescription.getParameters().get("connectionfactory.QueueConnectionFactory");
            		inboundDescription.getParameters().put("wso2mb.connection.url", connectionURL);
            	} else if (inboundDescription.getParameters().containsKey("connectionfactory.TopicConnectionFactory")) {
            		isMBbased = true;
            		inboundTypeValue = InboundClientConstants.TYPE_WSO2MB;
            		String connectionURL = inboundDescription.getParameters().get("connectionfactory.TopicConnectionFactory");
            		inboundDescription.getParameters().put("wso2mb.connection.url", connectionURL);
            	}
            }
            
            List<String>defaultParams = client.getDefaultParameters(inboundTypeValue);            
            List<String>advParams = client.getAdvParameters(inboundTypeValue); 
            Set<String>defaultParamNames = new HashSet<String>();
            String specialParams = client.getKAFKASpecialParameters();
            String topicListParams = client.getKAFKATopicListParameters();
            String firstSpecialParam = "";
    %>       
    
    <form method="post" name="inboundupdateform" id="inboundupdateform"
          action="updateInbound-ajaxprocessor.jsp">

        <div id="middle">
            <h2><fmt:message key="inbound.header.update"/></h2>

            <div id="workArea">

                <table id="tblInput" name="tblInput" class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message key="inbound.header.update"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.name"/></td>
                        <td align="left">
                            <%=inboundDescription.getName()%>      
                            <input name="inboundName" id="inboundName" type="hidden" value="<%=Encode.forHtmlAttribute(inboundDescription.getName())%>"/>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.type"/></td>
                        <td align="left">
                            <%=inboundTypeValue%>
                            <input name="inboundType" id="inboundType" type="hidden" value="<%=Encode.forHtmlAttribute(inboundDescription.getType())%>"/>
                            <input name="inboundMode" id="inboundMode" type="hidden" value="edit"/>                                                  
                        </td>
                        <td></td>
                    </tr>

                   <%
                   if(!(InboundClientConstants.TYPE_HTTP.equals(inboundDescription.getType()) || InboundClientConstants.TYPE_HTTPS.equals(inboundDescription.getType())) ) { %>
                    <script type="text/javascript">sequenceRequired = true;</script>
                    <script type="text/javascript">onErrorRequired = true;</script>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.sequence"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="inboundSequence" name="inboundSequence" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(inboundDescription.getInjectingSeq())%>"/>
                        </td>
                        <td align="left">
	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundSequence','/_system/config')"><fmt:message key="inbound.sequence.registry.con"/></a>
	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundSequence','/_system/governance')"><fmt:message key="inbound.sequence.registry.gov"/></a>
                        </td>
                    </tr>                    
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.error.sequence"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="inboundErrorSequence" name="inboundErrorSequence" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(inboundDescription.getOnErrorSeq())%>"/>
                        </td>
                        <td align="left">
	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundErrorSequence','/_system/config')"><fmt:message key="inbound.sequence.registry.con"/></a>
	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundErrorSequence','/_system/governance')"><fmt:message key="inbound.sequence.registry.gov"/></a>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.error.suspend"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <select id="inboundSuspend" name="inboundSuspend" class="longInput">
                               <%if(inboundDescription.isSuspend()){%>                                
                                <option value="true" selected>true</option>  
                                <option value="false">false</option>
                                <%} else {%>
                                <option value="true">true</option>  
                                <option value="false" selected>false</option>                                
                                <%}%>              
                            </select>                            
                        </td>                      
                    </tr>
                     <% } else { %>
                   <tr>
                        <td style="width:150px"><fmt:message key="inbound.sequence"/></td>
                        <td align="left">
                        <input id="inboundSequence" name="inboundSequence" class="longInput" type="text" value="<%=inboundDescription.getInjectingSeq()!= null ? Encode.forHtmlAttribute(inboundDescription.getInjectingSeq()):"" %>"/>
                        </td>
                         <td align="left">
                         <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundSequence','/_system/config')"><fmt:message key="inbound.sequence.registry.con"/></a>
                         <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundSequence','/_system/governance')"><fmt:message key="inbound.sequence.registry.gov"/></a>
                         </td>
                   </tr>
                   <tr>
                       <td style="width:150px"><fmt:message key="inbound.error.sequence"/></td>
                       <td align="left">
                       <input id="inboundErrorSequence" name="inboundErrorSequence" class="longInput" type="text" value="<%=inboundDescription.getOnErrorSeq() != null ? Encode.forHtmlAttribute(inboundDescription.getOnErrorSeq()):""%>"/>
                       </td>
                       <td align="left">
                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundErrorSequence','/_system/config')"><fmt:message key="inbound.sequence.registry.con"/></a>
                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundErrorSequence','/_system/governance')"><fmt:message key="inbound.sequence.registry.gov"/></a>
                        </td>
                   </tr>
                   <% } %>
                    <% if(InboundClientConstants.TYPE_CLASS.equals(inboundDescription.getType())){ %>
                    <script type="text/javascript">classRequired = true;</script>       
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.class"/><span class="required">*</span></td>
                        <td align="left">                        
                            <input name="inboundClass" id="inboundClass" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(inboundDescription.getClassImpl())%>"/>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.isListening"/></td>
                        <td>
                            <input type="radio" id="inbound.behavior.polling" name="inbound.behavior" value="polling" onclick="toggleInboundInterval('polling')"><fmt:message key="inbound.polling"/>
                            <input type="radio" id="inbound.behavior.listening" name="inbound.behavior" value="listening" onclick="toggleInboundInterval('listening')" ><fmt:message key="inbound.listening"/>
                            <input type="radio" id="inbound.behavior.waiting" name="inbound.behavior" value="eventBased" onclick="toggleInboundInterval('waiting')" ><fmt:message key="inbound.waiting"/>
                        </td>
                        <td></td>
                    </tr>

                    <tr id="inboundIntervalRow">
                        <td style="width:150px"><fmt:message key="inbound.interval"/><span class="required">*</span></td>
                        <td align="left">
                            <input name="interval" id="interval" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(inboundDescription.getInterval())%>"/>
                        </td>
                        <td></td>
                    </tr>
                    <tr id="inboundSequentialRow">
                        <td style="width:150px"><fmt:message key="inbound.sequential"/><span class="required">*</span></td>
                        <td align="left">
                            <select name="sequential" id="sequential">
                                <%if("true".equals(inboundDescription.getSequential())){%>
	                            	<option value="true" selected>true</option>
	                            	<option value="false">false</option>
	                            <% } else {%>
	                            	<option value="true">true</option>
	                            	<option value="false" selected>false</option>	                            
	                            <% } %>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                    <tr id="inboundCoordinationRow">
                        <td style="width:150px"><fmt:message key="inbound.coordination"/><span class="required">*</span></td>
                        <td align="left">
                            <select name="coordination" id="coordination">
                                <%if("true".equals(inboundDescription.getCoordination())){%>
	                            	<option value="true" selected>true</option>
	                            	<option value="false">false</option>
	                            <% } else {%>
	                            	<option value="true">true</option>
	                            	<option value="false" selected>false</option>	                            
	                            <% } %>
                            </select>                            
                        </td>
                        <td></td>
                    </tr>                        
                    <script language="javascript">
                        function toggleInboundInterval(event){
                            if (event == "listening"){
                                document.getElementById("inboundIntervalRow").style.display="none";
                                document.getElementById("inboundSequentialRow").style.display="none";
                                document.getElementById("inboundCoordinationRow").style.display="none";
                                intervalRequired = false;
                            }else if (event == "waiting"){
                                document.getElementById("inboundIntervalRow").style.display="none";
                                document.getElementById("inboundSequentialRow").style.display="table-row";
                                document.getElementById("inboundCoordinationRow").style.display="table-row";
                                intervalRequired = false;
                            } else {
                                document.getElementById("inboundIntervalRow").style.display="table-row";
                                document.getElementById("inboundSequentialRow").style.display="table-row";
                                document.getElementById("inboundCoordinationRow").style.display="table-row";
                                intervalRequired = true;
                            }
                        }
                    </script>

                    <% } %>
                    <%if(!defaultParams.isEmpty()){                     
                    %>
                    <script type="text/javascript">var requiredParams = new Array(<%=defaultParams.size()%>);</script>
                    <%} %>       
					<%  int ctr = -1;
					    for(String defaultParamOri : defaultParams) {
						String [] arrParamOri = defaultParamOri.split(InboundClientConstants.STRING_SPLITTER);
						String defaultParam = arrParamOri[0].trim();
						defaultParamNames.add(defaultParam);
						ctr++;
					%> 	
					<script type="text/javascript">requiredParams[<%=ctr%>] = '<%=Encode.forHtml(defaultParam)%>';</script>
	                    <tr>
	                        <td style="width:150px"><%=Encode.forHtmlContent(defaultParam) %><span class="required">*</span></td>
	                        <td align="left">
	                        <%if(arrParamOri.length > 2){%>
	                            <%if(InboundClientConstants.TYPE_KAFKA.equals(inboundDescription.getType())){
                                    firstSpecialParam = arrParamOri[1].trim();
                                %>
                                    <select id="<%=Encode.forHtmlAttribute(defaultParam)%>" name="<%=Encode.forHtmlAttribute(defaultParam)%>" onchange="javascript:showSpecialFields('<%=Encode.forJavaScriptAttribute(specialParams)%>','<%=inboundDescription.getParameters()%>','<%=Encode.forJavaScriptAttribute(topicListParams)%>');">
                                <%} else{%>
                                    <select id="<%=Encode.forHtmlAttribute(defaultParam)%>" name="<%=Encode.forHtmlAttribute(defaultParam)%>">
                                <%}%>
	                            <%for(int i = 1;i<arrParamOri.length;i++){
	                                String eleValue = arrParamOri[i].trim();
	                            %>
	                            <%if(eleValue.equals(inboundDescription.getParameters().get(defaultParam))){
	                                firstSpecialParam = eleValue;
	                            %>
	                                <option value="<%=Encode.forHtmlAttribute(eleValue)%>" selected><%=Encode.forHtmlContent(eleValue)%></option>
	                            <%}else{%>
	                                <option value="<%=Encode.forHtmlAttribute(eleValue)%>"><%=Encode.forHtmlContent(eleValue)%></option>
	                            <%}%>  	  
	                            <%}%>
                                </select>
							<%} else{ %>
                             <%if(InboundClientConstants.TYPE_HTTPS.equals(inboundDescription.getType()) && defaultParam.equals("keystore")){%>
                             <textarea name="<%=Encode.forHtmlAttribute(defaultParam)%>" id="<%=Encode.forHtmlAttribute(defaultParam)%>" form="inboundupdateform" rows="8" cols="35">
                             <%=Encode.forHtmlContent(inboundDescription.getParameters().get(defaultParam))%>
                              </textarea>
                             <%}else if (isMBbased && (defaultParam.equals("wso2mb.connection.url"))) {%>
                                <input id="<%=Encode.forHtmlAttribute(defaultParam)%>" name="<%=Encode.forHtmlAttribute(defaultParam)%>" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(inboundDescription.getParameters().get(defaultParam))%>"/>
                                (eg: amqp://admin:admin@clientID/carbon?brokerlist='tcp://localhost:5673' )
                             <%} else{ %>
                                <input id="<%=Encode.forHtmlAttribute(defaultParam)%>" name="<%=Encode.forHtmlAttribute(defaultParam)%>" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(inboundDescription.getParameters().get(defaultParam))%>"/>
                             <%} %>
                            <%} %>                       
	                        </td>
	                        <td></td>
	                    </tr>                        
                     <% } %>
                    <% if(InboundClientConstants.TYPE_KAFKA.equals(inboundDescription.getType())){%>
                        <tr><td colspan="3"><div id="specialFieldsForm"><table id="tblSpeInput" name="tblSpeInput" cellspacing="0" cellpadding="0" border="0">
                            <%
                                String[] allSpecialParams = specialParams.split(",");
                                String[] parentLevelParameters = allSpecialParams[0].split(InboundClientConstants.STRING_SPLITTER);
                            %>
                            <script type="text/javascript">var kafkaSpecialParameters = new Array(<%=allSpecialParams.length + parentLevelParameters.length - 1%>);</script>
                            <%
                                int specialParameterCount = 0;
                                for (int s = 0; s < parentLevelParameters.length; s++) {
                            %>
                            <script type="text/javascript">kafkaSpecialParameters[<%=specialParameterCount%>] = '<%=parentLevelParameters[s]%>';</script>
                            <%
                                    specialParameterCount++;
                                }
                                for (int s = 1; s < allSpecialParams.length; s++) {
                            %>
                            <script type="text/javascript">kafkaSpecialParameters[<%=specialParameterCount%>] = '<%=allSpecialParams[s]%>';</script>
                            <%
                                    specialParameterCount++;
                                }

                                for(int s = 0;s<allSpecialParams.length;s++){
                                    String topicsOrTopicFilterEle = "";
                                    String eleVal = "";
                                    String[] fLists = topicListParams.split(InboundClientConstants.STRING_SPLITTER);
                                    String listval = fLists[1].trim();
                                    String specialParam = allSpecialParams[s];
                                    if(firstSpecialParam.equals("highlevel") && (specialParam.indexOf(InboundClientConstants.STRING_SPLITTER) > -1 && specialParam.split(InboundClientConstants.STRING_SPLITTER)[0].trim().equals("topics/topic.filter"))) {
                                        String[] tLists = specialParam.split(InboundClientConstants.STRING_SPLITTER);
                                        if(inboundDescription.getParameters().get(tLists[1].trim())==null && inboundDescription.getParameters().get(tLists[2].trim())==null){
                                            eleVal = "";
                                            topicsOrTopicFilterEle = tLists[1].trim();
                                        } else{
                                            eleVal = ((inboundDescription.getParameters().get(tLists[1].trim())==null)?inboundDescription.getParameters().get(tLists[2].trim()):inboundDescription.getParameters().get(tLists[1].trim()));
                                            topicsOrTopicFilterEle = ((inboundDescription.getParameters().get(tLists[1].trim())==null)?tLists[2].trim():tLists[1].trim());
                                        }
                                        String inboundDescriptionOfParams = inboundDescription.getParameters().toString();
                                        %>
                                        <tr><td style="width:167px"><%=Encode.forHtmlContent(specialParam.split(InboundClientConstants.STRING_SPLITTER)[0].trim())%><span class="required">*</span></td><td align="left"><select id="topicsOrTopicFilter" name="topicsOrTopicFilter" onchange="javascript:showTopicsOrTopicFilterFields('<%=Encode.forJavaScriptAttribute(inboundDescriptionOfParams)%>','<%=Encode.forJavaScriptAttribute(topicListParams)%>')">
                                        <%
                                        for(int t = 1; t < tLists.length; t++){
                                            if(topicsOrTopicFilterEle.equals(tLists[t])){%>
                                                <option value="<%=Encode.forHtmlAttribute(tLists[t].trim())%>" selected><%=Encode.forHtmlContent(tLists[t].trim())%></option>
                                            <%} else{%>
                                                <option value="<%=Encode.forHtmlAttribute(tLists[t].trim())%>"><%=Encode.forHtmlContent(tLists[t].trim())%></option>
                                            <%}
                                        }%>
                                        </select></td><td></td></tr>
                                        <tr><td colspan="3"><div id="tDiv"><table>
                                        <%
                                        if(topicsOrTopicFilterEle.equals("topic.filter")){
                                            String[] splitedInboundDescription = inboundDescriptionOfParams.replace("{","").replace("}","").split(",");
                                            if(!inboundDescriptionOfParams.equals("")){
                                                for(int j=0; j<splitedInboundDescription.length; j++){
                                                    if((splitedInboundDescription[j].split("=")[0].trim().equals("filter.from.whitelist") || splitedInboundDescription[j].split("=")[0].trim().equals("filter.from.blacklist")) && Boolean.parseBoolean(splitedInboundDescription[j].split("=")[1])){
                                                        listval = splitedInboundDescription[j].split("=")[0].trim();
                                                        break;
                                                    }
                                                }
                                            }
                                        %>
                                        <tr><td style="width:157px"><%=Encode.forHtmlContent(specialParam.split(InboundClientConstants.STRING_SPLITTER)[0].trim())%><span class="required">*</span></td><td align="left"><select id="<%=Encode.forHtmlAttribute(specialParam.split(InboundClientConstants.STRING_SPLITTER)[0].trim())%>" name="<%=Encode.forHtmlAttribute(specialParam.split(InboundClientConstants.STRING_SPLITTER)[0].trim())%>" onchange="javascript:showTopicsOrTopicFilterFields('<%=inboundDescription.getParameters()%>','<%=Encode.forJavaScriptAttribute(topicListParams)%>');">
                                        <%for(int l = 1; l < fLists.length; l++){
                                            if(listval.equals(fLists[l])){%>
                                                 <option value="<%=Encode.forHtmlAttribute(fLists[l].trim())%>" selected><%=Encode.forHtmlContent(fLists[l].trim())%></option>
                                            <%} else{%>
                                                 <option value="<%=Encode.forHtmlAttribute(fLists[l].trim())%>"><%=Encode.forHtmlContent(fLists[l].trim())%></option>
                                            <%}
                                        }%>
                                        </td><td></td></tr>
                                        <%
                                        }
                                        %>
                                        <tr><td style="width:155px"><%=Encode.forHtmlContent(specialParam.split(InboundClientConstants.STRING_SPLITTER)[1].trim())%> name<span class="required">*</span></td><td align="left"><input id="<%=Encode.forHtmlAttribute(specialParam.split(InboundClientConstants.STRING_SPLITTER)[1].trim())%>" name="<%=Encode.forHtmlAttribute(specialParam.split(InboundClientConstants.STRING_SPLITTER)[1].trim())%>" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(eleVal)%>"/></td><td></td></tr></table></div></td></tr>
                                    <%} else{
                                        if(allSpecialParams[s].startsWith(firstSpecialParam+".")){
                                            if(inboundDescription.getParameters().get(allSpecialParams[s]) != null){
                                                eleVal = inboundDescription.getParameters().get(allSpecialParams[s]);
                                            }%>
                                            <tr><td style="width:167px"><%=Encode.forHtmlContent(allSpecialParams[s].replace(firstSpecialParam+".", ""))%><span class="required">*</span></td><td align="left"><input id="<%=Encode.forHtmlAttribute(allSpecialParams[s])%>" name="<%=Encode.forHtmlAttribute(allSpecialParams[s])%>" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(eleVal)%>"/></td><td></td></tr>
                                        <%}
                                    }
                                }%>
                            </table></div></td>
                        </tr>
                    <%}%>
                     <% if(InboundClientConstants.TYPE_CLASS.equals(inboundDescription.getType())){ %>
                    <tr>
                        <td class="buttonRow" colspan="3">
	                            <input class="button" type="button"
	                                   value='<fmt:message key="inbound.add.param"/>'
	                                   onclick="addRow('tblInput');"/>
	                            <input class="button" type="button"
	                                   value='<fmt:message key="inbound.remove.param"/>'
	                                   onclick="deleteRow('tblInput');"/>
                        </td>
                    </tr>
					<%  int i = 0;
					    for(String strKey : inboundDescription.getParameters().keySet()) {
					    if(!defaultParamNames.contains(strKey)){

                            if (strKey.equalsIgnoreCase("inbound.behavior")) {
                                if(inboundDescription.getParameters().get(strKey).equalsIgnoreCase("listening")){
                                    %>
                                        <script language="javascript">
                                            document.getElementById("inbound.behavior.listening").checked = true;
                                            document.getElementById("inboundIntervalRow").style.display="none";
                                            document.getElementById("inboundSequentialRow").style.display="none";
                                            document.getElementById("inboundCoordinationRow").style.display="none";
                                        </script>

                                    <%
                                }else if(inboundDescription.getParameters().get(strKey).equalsIgnoreCase("eventBased")){
                                    %>
                                        <script language="javascript">
                                            document.getElementById("inbound.behavior.waiting").checked = true;
                                            document.getElementById("inboundIntervalRow").style.display="none";
                                        </script>

                                    <%
                                }else if(inboundDescription.getParameters().get(strKey).equalsIgnoreCase("polling")){
                                    %>
                                        <script language="javascript">
                                            document.getElementById("inbound.behavior.polling").checked = true;
                                        </script>

                                    <%
                                }
                                continue;
                            }

					    i++;
					    %>                        
	                    <tr>
	                        <td style="width:150px">
	                            <input id="paramkey<%=i%>" name="paramkey<%=i%>" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(strKey)%>"/>
	                        </td>
	                        <td align="left">	                            	                      
                                <input id="paramval<%=i%>" name="paramval<%=i%>" class="longInput" type="text" value="<%=Encode.forHtmlAttribute(inboundDescription.getParameters().get(strKey))%>"/>
	                        </td>
	                        <td></td>
	                    </tr>                        
                     <% } %> 
                     <% } %> 
                     <script type="text/javascript">iParamCount=<%=i%>;iParamMax=<%=i%>;</script>
                     <%}else{
                     if(!advParams.isEmpty()){%>
				    <tr>
				        <td><span id="_adv" style="float: left; position: relative;">
				            <a class="icon-link" onclick="javascript:showAdvancedOptions('');"
				               style="background-image: url(images/down.gif);"><fmt:message
				                    key="show.advanced.options"/></a>
				        </span>
				        </td>
				    </tr> 
				    <%} }%>
				    <tr>
					    <td colspan="3">
						    <div id="_advancedForm" style="display:none">
						    <table id="tblAdvInput" name="tblAdvInput" class="normal-nopadding" cellspacing="0" cellpadding="0" border="0">
								<% for(String defaultParamOri : advParams) {
									String [] arrParamOri = defaultParamOri.split(InboundClientConstants.STRING_SPLITTER);
									String defaultParam = arrParamOri[0].trim();						
								%> 					                       
				                    <tr>
				                        <td style="width:150px"><%=Encode.forHtmlContent(defaultParam) %></td>
				                        <td align="left">
				                        <%if(arrParamOri.length > 2){%>
				                            <select id="<%=Encode.forHtmlAttribute(defaultParam)%>" name="<%=Encode.forHtmlAttribute(defaultParam)%>">
				                            <%for(int i = 1;i<arrParamOri.length;i++){
				                                String eleValue = arrParamOri[i].trim();
				                                %>
				                                <%if(eleValue.equals(inboundDescription.getParameters().get(defaultParam))){%>
				                                <option value="<%=Encode.forHtmlAttribute(eleValue)%>" selected><%=Encode.forHtmlContent(eleValue)%></option>
				                                <%} else {%>
				                                <option value="<%=Encode.forHtmlAttribute(eleValue)%>"><%=Encode.forHtmlContent(eleValue)%></option>
				                                <%} %>					                            				                                
				                            <%}%>                                
			                                </select>
										<%}else{%>
                                        <%if(InboundClientConstants.TYPE_HTTPS.equals(inboundDescription.getType()) && (defaultParam.equals("truststore") || defaultParam.equals("CertificateRevocationVerifier"))){%>
                                        <textarea name="<%=Encode.forHtmlAttribute(defaultParam)%>" id="<%=Encode.forHtmlAttribute(defaultParam)%>" form="inboundupdateform" rows="8" cols="35">
                                        <%=((inboundDescription.getParameters().get(defaultParam)==null)?"":Encode.forHtmlContent(inboundDescription.getParameters().get(defaultParam)))%>
                                        </textarea>
                                        <%}else{ %>
			                                <input id="<%=Encode.forHtmlAttribute(defaultParam)%>" name="<%=Encode.forHtmlAttribute(defaultParam)%>" class="longInput" type="text"  value="<%=((inboundDescription.getParameters().get(defaultParam)==null)?"":Encode.forHtmlAttribute(inboundDescription.getParameters().get(defaultParam)))%>"/>
			                            <%} %>
			                            <%} %>
				                        </td>
				                        <td></td>
				                    </tr>                        
			                     <% } %>						    
						    	</table>
						    </div> 			
					    </td>
				    </tr>                     
                    <tr>
                        <td class="buttonRow" colspan="3">
                            <input class="button" type="button"
                                   value="<fmt:message key="inbound.update.button.text"/>"
                                   onclick="inboundUpdate('<fmt:message key="inbound.seq.cannotfound.msg"/>','<fmt:message key="inbound.err.cannotfound.msg"/>','<fmt:message key="inbound.interval.cannotfound.msg"/>','<fmt:message key="inbound.class.cannotfound.msg"/>','<fmt:message key="inbound.required.msg"/>','<fmt:message key="inbound.jms.durableSubscriberName.cannotfound.msg"/>',document.inboundupdateform); return false;"/>
                            <input class="button" type="button"
                                   value="<fmt:message key="inbound.cancel.button.text"/>"
                                   onclick="document.location.href='index.jsp?ordinal=0';"/>                                                                   
                        </td>
                        <td></td>
                    </tr>
                    </tbody>
                </table>
                                               
                <script type="text/javascript">
                    autoredioselect();
                </script>
            </div>
        </div>

    </form>
    <%
    } catch (Throwable e) {
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            CARBON.showErrorDialog('<%=Encode.forHtml(e.getMessage())%>');
        });
    </script>
    <%
            return;
        }
    %>
</fmt:bundle>

