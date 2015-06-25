<!--
 ~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@page import="org.wso2.carbon.inbound.ui.internal.InboundManagementClient"%>
<%@page import="org.wso2.carbon.inbound.ui.internal.InboundClientConstants"%>
<%@page import="java.util.List"%>
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
var classRequired = false;
var sequenceRequired=false;
var onErrorRequired=false;
var requiredParams = null;
</script>
    <carbon:jsi18n
        resourceBundle="org.wso2.carbon.inbound.ui.i18n.Resources"
        request="<%=request%>" i18nObjectName="taskjsi18n"/>
<fmt:bundle basename="org.wso2.carbon.inbound.ui.i18n.Resources">
    <carbon:breadcrumb label="inbound.header.new"
                       resourceBundle="org.wso2.carbon.inbound.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <% InboundManagementClient client;
        try {
            client = InboundManagementClient.getInstance(config, session);
            List<String>defaultParams = client.getDefaultParameters(request.getParameter("inboundType"));            
            List<String>advParams = client.getAdvParameters(request.getParameter("inboundType"));
    %>
    <form method="post" name="inboundcreationform" id="inboundcreationform"
          action="saveInbound.jsp">

        <div id="middle">
            <h2><fmt:message key="inbound.header.new"/></h2>

            <div id="workArea">

                <table id="tblInput" name="tblInput" class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message key="inbound.header.new"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.name"/></td>
                        <td align="left">
                            <%=request.getParameter("inboundName")%>      
                            <input name="inboundName" id="inboundName" type="hidden" value="<%=request.getParameter("inboundName")%>"/>                      
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.type"/></td>
                        <td align="left">
                            <%=request.getParameter("inboundType")%>
                            <input name="inboundType" id="inboundType" type="hidden" value="<%=request.getParameter("inboundType")%>"/>                      
                        </td>
                        <td></td>
                    </tr>
                    <%
                       if(!(InboundClientConstants.TYPE_HTTP.equals(request.getParameter("inboundType")) || InboundClientConstants.TYPE_HTTPS.equals(request.getParameter("inboundType"))) ) { %>
                    <script type="text/javascript">sequenceRequired = true;</script>
                    <script type="text/javascript">onErrorRequired = true;</script>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.sequence"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="inboundSequence" name="inboundSequence" class="longInput" type="text"/>
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
                            <input id="inboundErrorSequence" name="inboundErrorSequence" class="longInput" type="text"/>
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
                                <option value="true">true</option>     
                                <option value="false" selected>false</option>           
                            </select>                            
                        </td>                      
                    </tr>
                     <% } else { %>
                        <tr>
                                                <td style="width:150px"><fmt:message key="inbound.sequence"/></td>
                                                <td align="left">
                                                    <input id="inboundSequence" name="inboundSequence" class="longInput" type="text"/>
                                                </td>
                                                <td align="left">
                        	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundSequence','/_system/config')"><fmt:message key="inbound.sequence.registry.con"/></a>
                        	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundSequence','/_system/governance')"><fmt:message key="inbound.sequence.registry.gov"/></a>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="width:150px"><fmt:message key="inbound.error.sequence"/></td>
                                                <td align="left">
                                                    <input id="inboundErrorSequence" name="inboundErrorSequence" class="longInput" type="text"/>
                                                </td>
                                                <td align="left">
                        	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundErrorSequence','/_system/config')"><fmt:message key="inbound.sequence.registry.con"/></a>
                        	                        <a href="#" class="registry-picker-icon-link"  onclick="showRegistryBrowser('inboundErrorSequence','/_system/governance')"><fmt:message key="inbound.sequence.registry.gov"/></a>
                                                </td>
                                            </tr>

                               <% } %>
                    <% if(InboundClientConstants.TYPE_CLASS.equals(request.getParameter("inboundType"))){ %>
					<script type="text/javascript">classRequired = true;</script>                    
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.class"/></td>
                        <td align="left">                        
                            <input name="inboundClass" id="inboundClass" class="longInput" type="text"/>                                         
                        </td>
                        <td></td>
                    </tr>
                    <% } %>             
                    <%if(!defaultParams.isEmpty()){                     
                    %>
                    <script type="text/javascript">var requiredParams = new Array(<%=defaultParams.size()%>);</script>
                    <%} %>       
					<%  int ctr = -1;
					    for(String defaultParamOri : defaultParams) {
						String [] arrParamOri = defaultParamOri.split(InboundClientConstants.STRING_SPLITTER);
						String defaultParam = arrParamOri[0].trim();
                        String defaultVal = "";
                        if (arrParamOri.length == 2) { defaultVal = arrParamOri[1].trim(); }
						ctr++;
					%> 	
					<script type="text/javascript">requiredParams[<%=ctr%>] = '<%=defaultParam%>';</script>				                     
	                    <tr>
	                        <td style="width:150px"><%=defaultParam %><span class="required">*</span></td>
	                        <td align="left">
	                        <%if(arrParamOri.length > 2){%>
	                            <select id="<%=defaultParam%>" name="<%=defaultParam%>">
	                            <%for(int i = 1;i<arrParamOri.length;i++){%>
	                                <option value="<%=arrParamOri[i].trim()%>"><%=arrParamOri[i].trim()%></option>
	                            <%}%>                                
                                </select>
							<%}else{ %>
							        <%if(InboundClientConstants.TYPE_HTTPS.equals(request.getParameter("inboundType")) && defaultParam.equals("keystore")){%>
							        <textarea name="<%=defaultParam%>" id="<%=defaultParam%>" form="inboundcreationform" rows="8" cols="35">
							        </textarea>
							        <%}else{ %>

                                    <input id="<%=defaultParam%>" name="<%=defaultParam%>" class="longInput" type="text" value="<%=defaultVal%>"/>
                              <%} %>
                            <%} %>                       
	                        </td>
	                        <td></td>
	                    </tr>                        
                     <% } %>
                     <% if(InboundClientConstants.TYPE_CLASS.equals(request.getParameter("inboundType"))){ %>
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
                                    String defaultVal = "";
                                    if (arrParamOri.length == 2) { defaultVal = arrParamOri[1].trim(); }
								%> 					                       
				                    <tr>
				                        <td style="width:150px"><%=defaultParam %></td>
				                        <td align="left">
				                        <%if(arrParamOri.length > 2){%>
				                            <select id="<%=defaultParam%>" name="<%=defaultParam%>">
				                            <%for(int i = 1;i<arrParamOri.length;i++){%>
				                                <option value="<%=arrParamOri[i].trim()%>"><%=arrParamOri[i].trim()%></option>
				                            <%}%>                                
			                                </select>
										<%} else{%>
                                        <%if(InboundClientConstants.TYPE_HTTPS.equals(request.getParameter("inboundType")) && (defaultParam.equals("truststore") || defaultParam.equals("CertificateRevocationVerifier"))){%>
                                        <textarea name="<%=defaultParam%>" id="<%=defaultParam%>" form="inboundcreationform" rows="8" cols="35">
                                        </textarea>
                                        <%}else{ %>

			                                <input id="<%=defaultParam%>" name="<%=defaultParam%>" class="longInput" type="text" value="<%=defaultVal%>"/>
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
                                   value="<fmt:message key="inbound.save.button.text"/>"
                                   onclick="inboundsave2('<fmt:message key="inbound.seq.cannotfound.msg"/>','<fmt:message key="inbound.err.cannotfound.msg"/>','<fmt:message key="inbound.interval.cannotfound.msg"/>','<fmt:message key="inbound.class.cannotfound.msg"/>','<fmt:message key="inbound.required.msg"/>',document.inboundcreationform); return false;"/>
                            <input class="button" type="button"
                                   value="<fmt:message key="inbound.cancel.button.text"/>"
                                   onclick="document.location.href='index.jsp?ordinal=0';"/>                                                                 
                        </td>
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
            CARBON.showErrorDialog('<%=e.getMessage()%>');
        });
    </script>
    <%
            return;
        }
    %>
</fmt:bundle>

