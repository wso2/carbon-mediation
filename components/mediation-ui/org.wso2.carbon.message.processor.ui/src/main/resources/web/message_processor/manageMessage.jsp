<%--
 ~ Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageProcessorAdminServiceClient" %>
<%@ page import="org.wso2.carbon.message.processor.service.xsd.MessageProcessorMetaData" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>


<%@ page import="org.wso2.carbon.message.processor.ui.utils.MessageProcessorData" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageStoreAdminServiceClient" %>


<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<carbon:breadcrumb
        label="Message Processor"
        resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>


<fmt:bundle basename="org.wso2.carbon.message.processor.ui.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
    request="<%=request%>"/>
    
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript" src="localentrycommons.js"></script>
    <link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>

    <%

        String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),session);
        ConfigurationContext configContext = (ConfigurationContext)config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        MessageProcessorAdminServiceClient client = new MessageProcessorAdminServiceClient(cookie,url,configContext);
        String processorName = request.getParameter("messageProcessorName");
        String msg = null;
        String messageStoreName = request.getParameter("messageProcessorName");
        
        String[] messageProcessorNames = client.getMessageProcessorNames();
        String origin = request.getParameter("origin");
        MessageProcessorData processorData = null;

        if (messageStoreName != null) {
            session.setAttribute("edit" + messageStoreName, "true");
            for (String name : messageProcessorNames) {
                if (name != null && name.equals(messageStoreName)) {
                    processorData = client.getMessageProcessor(name);
                }
            }
        } else if (origin != null && !"".equals(origin)) {
            String mpString = (String) session.getAttribute("messageProcessorConfiguration");
            String mpName = (String) session.getAttribute("mpName");
            String mpProvider = (String) session.getAttribute("mpProvider");
            String mpStore = (String) session.getAttribute("mpStore");

            session.removeAttribute("messageProcessorConfiguration");
            session.removeAttribute("mpName");
            session.removeAttribute("mpProvider");
            session.removeAttribute("mpStore");

            mpString = mpString.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
            OMElement messageProcessorElement = AXIOMUtil.stringToOM(mpString);
            processorData = new MessageProcessorData(messageProcessorElement.toString());
            processorData.setName(mpName);
            processorData.setClazz(mpProvider);
            processorData.setMessageStore(mpStore);
        }
            
        MessageStoreAdminServiceClient messageStoreClient =
            new MessageStoreAdminServiceClient(cookie, url, configContext);
        String[] messageStores = messageStoreClient.getMessageStoreNames();

        if( processorName != null ) {
            try {
                msg = client.getMessage(processorName);
            } catch (Throwable e) {
                msg = "ERROR : " + e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(msg,CarbonUIMessage.ERROR, request);
            }
        }

        if(msg!=null) {
            msg = msg.replace("<","&lt");
            msg = msg.replace(">","&gt");
        }
    %>

    <div id = "middle">
        <h2><%=processorName%> : Message Management</h2>

        <% if(msg == null) { %>
        <div id="workArea" style="background-color:#F4F4F4;">
            <pre> <h3>No message in the subscribed Queue</h3> </pre> <br>
        </div>
        <input type="button" class="button" value = "Pop Message" disabled>
                
        <table>
            <%if (((null!=processorData)&& processorData.getParams() != null
                            && !processorData.getParams().isEmpty()
                            &&(processorData.getParams().get("message.processor.failMessagesStore")!=null))) { %>
                        <tr>
                            <td>
                                <p> Redirect Message to : </p>
                            </td>
                            <td>
                                <input name="FailMessagesStore" id="FailMessagesStore" type="hidden"
                                       value="<%=processorData.getParams().get("message.processor.failMessagesStore")%>"/>
                                <label id="FailMessagesStore_label" for="FailMessagesStore"><%=processorData.getParams()
                                       .get("message.processor.failMessagesStore")%>
                                </label>
                                <br/>
                            </td>
                            <td>
                                <input type="button" class="button" onclick="redirectMessage('<%=processorName%>')" value = "Redirect">
                            </td>
                        </tr>
                        <%} else {%>
                        <tr>
                            <td>
                                <p> Redirect Message to : <p>
                            </td>
                            <td>
                                <select id="FailMessagesStore" name="FailMessagesStore">
                                    <%for (String msn : messageStores) {%>
                                    <option selected="true" value="<%=msn%>"><%=msn%>
                                    </option>
                                    <%} %>
                                </select>
                            </td>
                            <td>
                                <input type="button" class="button" value = "Redirect" disabled>
                            </td>
                        </tr>
                    <%}%>  
        </table>
                
       
        <br><input type="button" value="<fmt:message key="cancel"/>"
                   onclick="javascript:document.location.href='../message_processor/index.jsp?region=region1&item=messageProcessor_menu&ordinal=0'"
                   class="button"/>
       
        
        
        <% } else if(msg.contains("ERROR")) { %>
        <div id="workArea" style="background-color:#F4F4F4;">
            <pre> 
                <h3><%=msg%></h3>
                <h4>Message Broker may be inactive</h4>
            </pre> <br>
        </div>
        <input type="button" class="button" value = "Pop Message" disabled>
                
        <table>
            <%if (((null!=processorData)&& processorData.getParams() != null
                            && !processorData.getParams().isEmpty()
                            &&(processorData.getParams().get("message.processor.failMessagesStore")!=null))) { %>
                        <tr>
                            <td>
                                <h3> Redirect Message to : </h3>
                            </td>
                            <td>
                                <input name="FailMessagesStore" id="FailMessagesStore" type="hidden"
                                       value="<%=processorData.getParams().get("message.processor.failMessagesStore")%>"/>
                                <label id="FailMessagesStore_label" for="FailMessagesStore"><%=processorData.getParams()
                                       .get("message.processor.failMessagesStore")%>
                                </label>
                                <br/>
                            </td>
                            <td>
                                <input type="button" class="button" onclick="redirectMessage('<%=processorName%>')" value = "Redirect">
                            </td>
                        </tr>
                        <%} else {%>
                        <tr>
                            <td>
                                <p> Redirect Message to : </p>
                            </td>
                            <td>
                                <select id="FailMessagesStore" name="FailMessagesStore">
                                    <%for (String msn : messageStores) {%>
                                    <option selected="true" value="<%=msn%>"><%=msn%>
                                    </option>
                                    <%} %>
                                </select>
                            </td>
                            <td>
                                <input type="button" class="button" value = "Redirect" disabled>
                            </td>
                        </tr>
                    <%}%>  
        </table>
        
        <br><input type="button" value="<fmt:message key="cancel"/>"
                   onclick="javascript:document.location.href='../message_processor/index.jsp?region=region1&item=messageProcessor_menu&ordinal=0'"
                   class="button"/>
          
        
        
        <% } else { %>
        <div id="workArea" style="background-color:#F4F4F4;">
            <pre> <h3><%=msg%></h3> </pre> <br>
        </div>
            <input type="button" class="button" onclick="popMessage('<%=processorName%>')" value = "Pop Message"> <br>     
    
        <table>
            <%if (((null!=processorData)&& processorData.getParams() != null
                            && !processorData.getParams().isEmpty()
                            &&(processorData.getParams().get("message.processor.failMessagesStore")!=null))) { %>
                        <tr>
                            <td>
                                <h3> Redirect Message to : </h3>
                            </td>
                            <td>
                                <input name="FailMessagesStore" id="FailMessagesStore" type="hidden"
                                       value="<%=processorData.getParams().get("message.processor.failMessagesStore")%>"/>
                                <label id="FailMessagesStore_label" for="FailMessagesStore"><%=processorData.getParams()
                                       .get("message.processor.failMessagesStore")%>
                                </label>
                                <br/>
                            </td>
                            <td>
                                <input type="button" class="button" onclick="redirectMessage('<%=processorName%>')" value = "Redirect">
                            </td>
                        </tr>
                        <%} else {%>
                        <tr>
                            <td>
                                <h3> Redirect Message to : </h3>
                            </td>
                            <td>
                                <select id="FailMessagesStore" name="FailMessagesStore">
                                    <%for (String msn : messageStores) {%>
                                    <option selected="true" value="<%=msn%>"><%=msn%>
                                    </option>
                                    <%} %>
                                </select>
                            </td>
                            <td>
                                <input type="button" class="button" onclick="redirectMessage('<%=processorName%>')" value = "Redirect">
                            </td>
                        </tr>
                    <%}%>  
        </table>
                        
        <input type="button" value="<fmt:message key="cancel"/>"
                   onclick="javascript:document.location.href='../message_processor/index.jsp?region=region1&item=messageProcessor_menu&ordinal=0'"
                   class="button"/>
        <% } %>
    </div>

    <script type="text/javascript">
        function popMessage(name)
        {
            
            CARBON.showConfirmationDialog("Do you want to pop the message?", function () {
                jQuery.ajax({
                    type: "POST",
                    url: "popMessageFromQueue.jsp",
                    data: {"processorName": name},
                    success: function (result, status, xhr) {
                        if(status == "success")
                        { CARBON.showInfoDialog("Message successfully popped from Queue"); }
                    }
                });
            });
        }
        
        function redirectMessage(name)
        {
            var store = document.getElementById('FailMessagesStore').value;
            CARBON.showConfirmationDialog("Do you want to redirect the message to Message Store "+store+"?", function () {
                jQuery.ajax({
                    type: "POST",
                    url: "enqueueMessageToQueue.jsp",
                    data: {"processorName": name, "storeName":store},
                    success: function (result, status, xhr) {
                        CARBON.showInfoDialog("Message successfully enqueued to " + store);
                    }
                });
            });
            
        }

    </script>


</div>

</fmt:bundle>

