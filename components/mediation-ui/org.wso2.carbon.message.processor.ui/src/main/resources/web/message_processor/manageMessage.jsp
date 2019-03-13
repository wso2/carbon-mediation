<%--
 ~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<%@ page import="org.wso2.carbon.message.processor.ui.utils.MessageProcessorData" %>
<%@ page import="org.wso2.carbon.message.processor.ui.MessageStoreAdminServiceClient" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="js/messageStore-util.js"></script>
<script type="text/javascript" src="js/customModal.js"></script>

<fmt:bundle basename="org.wso2.carbon.message.processor.ui.i18n.Resources">

    <carbon:jsi18n resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
    request="<%=request%>"/>

    <carbon:breadcrumb
        label="Message Management"
        resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
        topPage="false"
    request="<%=request%>"/>

    <script type="text/javascript" src="localentrycommons.js"></script>
    <link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>

    <%

        String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),session);
        ConfigurationContext configContext =
            (ConfigurationContext)config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        MessageProcessorAdminServiceClient client = new MessageProcessorAdminServiceClient(cookie,url,configContext);
        String processorName = request.getParameter("messageProcessorName");
        String msg = null;
        String messageStoreName = request.getParameter("messageProcessorName");

        String[] messageProcessorNames = client.getMessageProcessorNames();
        String origin = request.getParameter("origin");
        MessageProcessorData processorData = null;
        Boolean loadEditArea = true;

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
        
        String storeList = "";
        
        // Creating a string with all the stores with a delimiter in between
        for(String storeName : messageStores) {
            storeList += storeName + "#";
        }
    
        //Get rid of that last #
        if(storeList.length() > 0) {
            storeList = storeList.substring(0, storeList.length() - 1);
        }   
        
        if( processorName != null ) {
            try {
                msg = client.browseMessage(processorName);
            } catch (Throwable e) {
                msg = "ERROR : " + e.browseMessage();
                CarbonUIMessage.sendCarbonUIMessage(msg,CarbonUIMessage.ERROR, request);
            }
        }

        if(msg!=null) {
            msg = msg.replace("<","&lt");
            msg = msg.replace(">","&gt");
        }
    %>

    <style>
        pre {
            overflow-x: auto;
            white-space: pre-wrap;
            white-space: -moz-pre-wrap;
            white-space: -pre-wrap;
            white-space: -o-pre-wrap;
            word-wrap: break-word;
        }
    </style>

    <div id = "middle">
        <h2><%=processorName%> : Message Management</h2>

        <!-- No messages in the Queue -->
        <% if(msg == null) { %>
        <!-- Empty Queue -->
        <div id="workArea">
            <form name="Submit" id="Submit" action="ServiceCaller.jsp" method="POST">
                <table class="styledLeft">
                    <thead>
                        <tr>
                            <th>
                                <span style="float: left; position: relative; margin-top: 2px;">
                                    Message Processor Payload
                                </span>
                            </th>
                        </tr>
                    </thead>
                    <tr>
                        <td>
                            <textarea id="Payload" name="Payload"
                               style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                               rows="30"> Subscribed Queue is Empty
                            </textarea>
                        </td>
                    </tr>

                    <!-- Operations -->
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="Pop Message" class="button"
                                   disabled/>
                            <input type="button" value="Redirect Message" class="button"
                                   disabled/>
                            <input type="button" value="<fmt:message key="cancel"/>"
                                   class="button" onclick="javascript:document.location.href = 'index.jsp'"/>
                        </td>
                    </tr>
                </table>

            </form>
        </div>

        <!-- Error occurs -->
        <% } else if(msg.contains("ERROR")) { %>

        <!-- Payload -->
        <div id="workArea">
            <form name="Submit" id="Submit" action="ServiceCaller.jsp" method="POST">
                <table class="styledLeft">
                    <thead>
                        <tr>
                            <th>
                                <span style="float: left; position: relative; margin-top: 2px;">
                                    Message Processor Payload
                                </span>
                            </th>
                        </tr>
                    </thead>
                    <tr>
                        <td>
                            <textarea id="Payload" name="Payload"
                               style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                               rows="30"><%=msg%>. Message Broker may be inactive
                            </textarea>
                        </td>
                    </tr>  

                    <!-- Operations -->
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="Pop Message" class="button"
                                   disabled/>
                            <input type="button" value="Redirect Message" class="button"
                                   disabled/>
                            <input type="button" value="<fmt:message key="cancel"/>"
                                   class="button" onclick="javascript:document.location.href = 'index.jsp'"/>
                        </td>
                    </tr>
                </table>

            </form>
        </div>

        <!-- Display message -->
        <% } else { %>

        <!-- Payload -->
        <div id="workArea">
            <form name="Submit" id="Submit" action="ServiceCaller.jsp" method="POST">
                <table class="styledLeft">
                    <thead>
                        <tr>
                            <th>
                                <span style="float: left; position: relative; margin-top: 2px;">
                                    Message Processor Payload
                                </span>
                            </th>
                        </tr>
                    </thead>
                    <tr>
                        <td>
                            <textarea id="Payload" name="Payload"
                               style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                               rows="30"><%=msg%>
                            </textarea>
                        </td>
                    </tr>

                    <!-- Operations -->
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="Pop Message" class="button"
                                   onclick="popMessage('<%=processorName%>')"/>
                            <input type="button" value="Redirect Message" class="button"
                                   onclick="redirectMessage('<%=processorName%>')"/>
                            <input type="button" value="<fmt:message key="cancel"/>"
                                   class="button" onclick="javascript:document.location.href = 'index.jsp'"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>


        <% } %>



        <script type="text/javascript">
            function popMessage(name) {
                CARBON.showConfirmationDialog("Do you want to pop the message?", function () {
                    jQuery.ajax({
                        type: "POST",
                        url: "popMessageFromQueue.jsp",
                        data: {"processorName": name},
                        success: function (result, status, xhr) {
                            if (status == "success") {
                                CARBON.showConfirmationDialog("Message successfully popped from Queue",
                                function() {
                                    javascript:document.location.href = 'index.jsp';
                                });
                            }
                        }
                    });
                });
            }

            function redirectMessage(processorName) {

                data = '<%=storeList%>'; 
                data = jQuery.trim(data);
                
                data = data.split("#");
               
                storeList = '<div id="target" style="margin: 0 auto; width: 80%;">Redirect to : <select id="storeName" style="margin-top: 10px;">';
                for (var i = 0; i < data.length; i++) {
                    storeList += '<option value="' + data[i] + '">' + data[i] + '</option>';
                }
       
                storeList += '</select><P style="margin-top: 5px;">Message will be redirected to the chosen message store</P></div>';

                CARBON.showCustomModal("", "Select a store to redirect the message ", 130, true,
                        function () {
                            //Getting the selected store
                            var storeName = jQuery('#storeName').val();

                            jQuery.ajax({
                                type: "POST",
                                url: "redirectMessage.jsp",
                                data: {"processorName": processorName, "storeName": storeName},
                                success: function (result, status, xhr) {
                                    CARBON.showConfirmationDialog("Message successfully redirected to " + storeName,
                                    function() {
                                        javascript:document.location.href = 'index.jsp';
                                    });
                                }
                            });
                        }, 500
                );
                $("#popupDialog").after(storeList);
            } //End of showPopUp Function


            editAreaLoader.init({
                id: "Payload"		// text area id
                , syntax: "xml"	       // syntax to be uses for highlighting
                , start_highlight: true    // to display with highlight mode on start-up
                , toolbar: "search, go_to_line, fullscreen, |, select_font,|, change_smooth_selection, highlight, reset_highlight, word_wrap"
                , is_editable: false
                , EA_load_callback: 'modifyToolbar'
            });
        </script>
    </div>

</fmt:bundle>

