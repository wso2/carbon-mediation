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

<%@page contentType="text/html" pageEncoding="UTF-8"
        import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="js/messageStore-util.js"></script>

<fmt:bundle basename="org.wso2.carbon.message.processor.ui.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
                   request="<%=request%>"/>

    <carbon:breadcrumb
            label="source.of.message.processor"
            resourceBundle="org.wso2.carbon.message.processor.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>


    <script type="text/javascript">

        function forward() {
            location.href = 'index.jsp'
        }

        function switchToDesign(url) {

            document.getElementById("messageProcessorSource").value = editAreaLoader.getValue("messageProcessorSource");
            var isValidXML = isValidXml(trim(document.getElementById('messageProcessorSource').value));
            if (!isValidXML) {
                return false;
            }
            var messageProcessorValue = trim(document.getElementById('messageProcessorSource').value);
            var messageProcessorStr = {messageProcessorString : messageProcessorValue, mpName : document.getElementById("mpName").value, mpProvider : document.getElementById("mpProvider").value, mpStore : document.getElementById("mpStore").value};

            jQuery.ajax({
                type: 'POST',
                url: 'updatePages/sourceViewUpdate.jsp',
                data: messageProcessorStr,
                success: function(msg) {
                    location.href = url;
                }
            });
        }

        function submitTextContent() {

            document.getElementById("messageProcessorSource").value = editAreaLoader.getValue("messageProcessorSource");
            var isValidXML = isValidXml(trim(document.getElementById('messageProcessorSource').value));
            if (!isValidXML) {
                return false;
            }
            var messageProcessorValue = trim(document.getElementById('messageProcessorSource').value);
            var messageProcessorStr = {messageProcessorString : messageProcessorValue, mpName : document.getElementById("mpName").value, mpProvider : document.getElementById("mpProvider").value, mpStore : document.getElementById("mpStore").value};

            jQuery.ajax({
                type: 'POST',
                url: 'updatePages/sourceViewSubmit-ajaxprocessor.jsp',
                data: messageProcessorStr,
                success: function(msg) {
                    document.Submit.innerHTML = msg;
                    postToServiceCaller();
                }
            });
        }

        function postToServiceCaller() {
            var messageProcessorStr = {Name : document.getElementById("Name").value, tableParams : document.getElementById("tableParams").value, Provider : document.getElementById("Provider").value, MessageStore : document.getElementById("MessageStore").value, TargetEndpoint : document.getElementById("TargetEndpoint").value};
            jQuery.ajax({
                type: 'POST',
                url: 'ServiceCaller.jsp',
                data: messageProcessorStr,
                success: function(msg) {
                    location.href = 'index.jsp';
                }
            });
        }

    </script>


    <%
        String prettyPrintPayload = "";
        String mpString = (String) session.getAttribute("messageProcessorConfiguration");
        String provider = (String) session.getAttribute("provider");
        String mpName = (String) session.getAttribute("name");
        String mpStore = (String) session.getAttribute("store");

        session.removeAttribute("messageProcessorConfiguration");
        session.removeAttribute("provider");
        session.removeAttribute("name");
        session.removeAttribute("store");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(mpString.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(inputStream);
        prettyPrintPayload = xmlPrettyPrinter.xmlFormat();
        String designViewUrl = "";

        if (provider.equals("org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor")) {
            designViewUrl = "manageMessageForwardingProcessor.jsp?origin=source";
        } else if(provider.equals("org.apache.synapse.message.processor.impl.failover.FailoverScheduledMessageForwardingProcessor")) {
            designViewUrl = "manageFailoverMessageForwardingProcessor.jsp?origin=source";
        } else if (provider.equals("org.apache.synapse.message.processor.impl.sampler.SamplingProcessor")) {
            designViewUrl = "manageMessageSamplingProcessor.jsp?origin=source";
        } else {
            designViewUrl = "manageCustomMessageProcessor.jsp?origin=source";
        }

    %>


    <div id="middle">
        <h2><fmt:message key="source.of.message.processor"/></h2>

        <div id="workArea">
            <form name="Submit" id="Submit" action="ServiceCaller.jsp" method="POST">
                <input type="hidden" id="mpName" name="mpName" value="<%=mpName%>"/>
                <input type="hidden" id="mpProvider" name="mpProvider" value="<%=provider%>"/>
                <input type="hidden" id="mpStore" name="mpStore" value="<%=mpStore%>"/>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>
                            <span style="float: left; position: relative; margin-top: 2px;">
                                <fmt:message key="source.of.message.processor"/>
                            </span>
                            <a href="#" class="icon-link"
                               style="background-image: url(images/design-view.gif);"
                               onclick="switchToDesign('<%=designViewUrl%>')"> <fmt:message
                                    key="switch.to.design.view"/></a>
                        </th>
                    </tr>
                    </thead>
                    <tr>
                        <td>
                            <textarea id="messageProcessorSource" name="design"
                                      style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                                      rows="30"><%=prettyPrintPayload%>
                            </textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="save"/>" class="button"
                                   name="save"
                                   onclick="submitTextContent();"/>
                            <input type="button" value="<fmt:message key="cancel"/>" name="cancel"
                                   class="button"
                                   onclick="javascript:document.location.href='index.jsp'"/>
                        </td>
                    </tr>
                </table>

            </form>
        </div>
    </div>

    <script type="text/javascript">
        editAreaLoader.init({
            id : "messageProcessorSource"
            ,syntax: "xml"
            ,start_highlight: true
        });
    </script>
</fmt:bundle>