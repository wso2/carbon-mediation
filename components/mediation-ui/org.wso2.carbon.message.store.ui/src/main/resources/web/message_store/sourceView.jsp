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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.message.store.ui.MessageStoreAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.xml.XMLPrettyPrinter" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="js/messageStore-util.js"></script>

<%--<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<link rel="stylesheet" type="text/css" href="../yui/build/container/assets/skins/sam/container.css">--%>

<%--<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/element/element-min.js"></script>
<script type="text/javascript" src="../admin/js/widgets.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>--%>
<%--<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript" src="js/endpoint-util.js"></script>
<script type="text/javascript" src="js/form.js"></script>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>--%>

<fmt:bundle basename="org.wso2.carbon.message.store.ui.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.localentry.ui.i18n.Resources"
                   request="<%=request%>"/>

    <carbon:breadcrumb
            label="source.of.message.store"
            resourceBundle="org.wso2.carbon.message.store.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>


    <script type="text/javascript">

        function forward() {
            location.href = 'index.jsp'
        }

        function switchToDesign(url) {

            document.getElementById("messageStoreSource").value = editAreaLoader.getValue("messageStoreSource");
            var isValidXML = isValidXml(trim(document.getElementById('messageStoreSource').value));
            if (!isValidXML) {
                return false;
            }
            var messageStoreValue = trim(document.getElementById('messageStoreSource').value);
            var messageStoreStr = {messageStoreString : messageStoreValue, msName : document.getElementById("msName").value, msProvider : document.getElementById("msProvider").value};

            jQuery.ajax({
                type: 'POST',
                url: 'updatePages/sourceViewUpdate.jsp',
                data: messageStoreStr,
                success: function(msg) {
                    location.href = url;
                }
            });
        }

        function submitTextContent() {

            document.getElementById("messageStoreSource").value = editAreaLoader.getValue("messageStoreSource");
            var isValidXML = isValidXml(trim(document.getElementById('messageStoreSource').value));
            if (!isValidXML) {
                return false;
            }
            var messageStoreValue = trim(document.getElementById('messageStoreSource').value);
            var messageStoreStr = {messageStoreString : messageStoreValue, msName : document.getElementById("msName").value, msProvider : document.getElementById("msProvider").value};

            jQuery.ajax({
                type: 'POST',
                url: 'updatePages/sourceViewSubmit-ajaxprocessor.jsp',
                data: messageStoreStr,
                success: function(msg) {
                    document.Submit.innerHTML = msg;
                    postToServiceCaller();
                }
            });
        }

        function postToServiceCaller() {
            var messageStoreStr = {Name : document.getElementById("Name").value, tableParams : document.getElementById("tableParams").value, Provider : document.getElementById("Provider").value};
            jQuery.ajax({
                type: 'POST',
                url: 'ServiceCaller.jsp',
                data: messageStoreStr,
                success: function(msg) {
                    location.href = 'index.jsp';
                }
            });
        }

    </script>


    <%
        String prettyPrintPayload = "";
        String msString = (String) session.getAttribute("messageStoreConfiguration");
        String provider = (String) session.getAttribute("provider");
        String msName = (String) session.getAttribute("name");

        session.removeAttribute("messageStoreConfiguration");
        session.removeAttribute("provider");
        session.removeAttribute("name");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(msString.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(inputStream);
        prettyPrintPayload = xmlPrettyPrinter.xmlFormat();
        String designViewUrl = "";

        if (provider.equals("org.apache.synapse.message.store.impl.jms.JmsStore")) {
            designViewUrl = "jmsMessageStore.jsp?origin=source";
        } else if (provider.equals("org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore")) {
            designViewUrl = "rabbitmqMessageStore.jsp?origin=source";
        } else if (provider.equals("org.apache.synapse.message.store.impl.memory.InMemoryStore")) {
            designViewUrl = "inMemoryMessageStore.jsp?origin=source";
        } else {
            designViewUrl = "customMessageStore.jsp?origin=source";
        }

    %>


    <div id="middle">
        <h2><fmt:message key="source.of.message.store"/></h2>

        <div id="workArea">
            <form name="Submit" id="Submit" action="ServiceCaller.jsp" method="POST">
                <input type="hidden" id="msName" name="msName" value="<%=msName%>"/>
                <input type="hidden" id="msProvider" name="msProvider" value="<%=provider%>"/>
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th>
                            <span style="float: left; position: relative; margin-top: 2px;">
                                <fmt:message key="source.of.message.store"/>
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
                            <textarea id="messageStoreSource" name="design"
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
            id : "messageStoreSource"
            ,syntax: "xml"
            ,start_highlight: true
        });
    </script>
</fmt:bundle>