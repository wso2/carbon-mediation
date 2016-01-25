<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.service.data.publisher.stub.conf.EventingConfigData" %>
<%@ page import="org.wso2.carbon.bam.service.data.publisher.stub.conf.Property" %>
<%@ page import="org.wso2.carbon.bam.service.data.publisher.ui.ServiceDataPublisherAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%! public static final String PROPERTY_VALUES = "propertyValues";
    public static final String PROPERTY_KEYS = "propertyKeys";
%><fmt:bundle basename="org.wso2.carbon.bam.service.data.publisher.ui.i18n.Resources">

<carbon:breadcrumb
        label="bam.statistics"
        resourceBundle="org.wso2.carbon.bam.service.data.publisher.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
    String setConfig = request.getParameter("setConfig"); // hidden parameter to check if the form is being submitted
    String enableServiceStats = request.getParameter("enableServiceStats"); // String value is "on" of checkbox clicked, else null
    String url = request.getParameter("url");
    String userName = request.getParameter("user_name");
    String password = request.getParameter("password");

    String streamName = request.getParameter("stream_name");
    String version = request.getParameter("version");
    String nickName = request.getParameter("nick_name");
    String description = request.getParameter("description");

    String[] propertyKeys = request.getParameterValues(PROPERTY_KEYS);
    String[] propertyValues = request.getParameterValues(PROPERTY_VALUES);

    List<Property> properties = null;
    if (propertyKeys != null) {
        properties = new ArrayList<Property>();
        for (int i = 0; i < propertyKeys.length; i++) {
            Property property = new Property();
            String propertyKey = propertyKeys[i];
            String propertyValue = propertyValues[i];
            property.setKey(propertyKey);
            property.setValue(propertyValue);
            properties.add(property);
        }
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ServiceDataPublisherAdminClient client = new ServiceDataPublisherAdminClient(
            cookie, backendServerURL, configContext, request.getLocale());
    EventingConfigData eventingConfigData = null;

    if (setConfig != null) {    // form submitted request to set eventing config
        eventingConfigData = new EventingConfigData();
        eventingConfigData.setPublishingEnable(true);
        if (enableServiceStats != null) {
            eventingConfigData.setServiceStatsEnable(true);
        } else {
            eventingConfigData.setServiceStatsEnable(false);
        }
        if (url != null) {
            eventingConfigData.setUrl(url);
        }
        if (userName != null) {
            eventingConfigData.setUserName(userName);
        }
        if (password != null) {
            eventingConfigData.setPassword(password);
        }

        if (properties != null) {
            eventingConfigData.setProperties(properties.toArray(new Property[properties.size()]));
        }

        if (streamName != null) {
            eventingConfigData.setStreamName(streamName);
        }

        if (version != null) {
            eventingConfigData.setVersion(version);
        }

        if (nickName != null) {
            eventingConfigData.setNickName(nickName);
        }

        if (description != null) {
            eventingConfigData.setDescription(description);
        }

        try {
            client.setEventingConfigData(eventingConfigData);

%>
<script type="text/javascript">
    /*jQuery(document).init(function () {*/
        function handleOK() {

        }

        CARBON.showInfoDialog("Eventing Configuration Successfully Updated!", handleOK);
    /*});*/
</script>
<%
} catch (Exception e) {
    if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        }
    }
} else {
    try {
        eventingConfigData = client.getEventingConfigData();
    } catch (Exception e) {
        if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            }
        }
    }


    boolean isServiceStatsEnable = eventingConfigData.getServiceStatsEnable();

    if (url == null) {
        url = eventingConfigData.getUrl();
    }
    if (userName == null) {
        userName = eventingConfigData.getUserName();
    }
    if (password == null) {
        password = eventingConfigData.getPassword();
    }

    if (streamName == null) {
        streamName = eventingConfigData.getStreamName();
    }
    if (version == null) {
        version = eventingConfigData.getVersion();
    }
    if(nickName == null){
        nickName = eventingConfigData.getNickName();
    }
    if(description == null){
        description = eventingConfigData.getDescription();
    }

    if (properties == null) {
        Property[] propertiesDTO = eventingConfigData.getProperties();
        if (propertiesDTO != null) {
            properties = new ArrayList<Property>();
            for (int i = 0; i < propertiesDTO.length; i++) {
                Property property = propertiesDTO[i];
                properties.add(property);
            }
        }
    }

%>

 <style type="text/css">
    .alert-error {
        padding: 8px 35px 8px 14px;
        margin-bottom: 20px;
        text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
        border: 1px solid #eed3d7;
        -webkit-border-radius: 4px;
        -moz-border-radius: 4px;
        border-radius: 4px;
        color: #b94a48;
        background-color: #f2dede;
    }
</style>

<script id="source" type="text/javascript">
    $(document).ready(function(){
        if($(".serviceConfigurationCheckBox").is(":checked")){
                $(".serviceConfigurationInput").each(function(){
                    $(this).removeAttr('disabled');
                });
           }else{
                $(".serviceConfigurationInput").each(function(){
                    $(this).attr('disabled','disabled');
                })
        }
    });
    function showHideDiv(divId) {
        var theDiv = document.getElementById(divId);
        if (theDiv.style.display == "none") {
            theDiv.style.display = "";
        } else {
            theDiv.style.display = "none";
        }
    }

    var rowNum = 1;

    function addColumn() {
        rowNum++;
        /*var n =  + parseInt(trId.charAt(trId.length-1))+1;
         jQuery("#"+trId+" td div.addIcon").remove();*/
        //alert(n);
        var sId = "propertyTable_" + rowNum;
        //alert(sId);
        var tableContent = "<tr id=\"" + sId + "\">" +
                           "<td>\n" +
                           "                        <fmt:message key='property.name'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                           "                    </td>\n" +
                           "                    <td>\n" +
                           "                        <fmt:message key='property.value'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\">\n" +
                           "                    </td>" +
                           "<td>\n" +
                           "<a onClick='javaScript:removeColumn(\"" + sId + "\")'" +
                           "style='background-image: url(../daspubsvcstat/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>\n" +
                           "                    </td>" +
                           "</tr>";

        $("#propertyTable").append(tableContent);
    }

    function addMetaData(){
        var sId = "propertyTable_" + rowNum;
        var propertyTable = "<table id=\"propertyTable\" width=\"100%\" class=\"styledLeft\""+
                           " style=\"margin-left: 0px;\"><tr id=\"" + sId + "\">" +
                           "<td>\n" +
                           "                        <fmt:message key='property.name'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                           "                    </td>\n" +
                           "                    <td>\n" +
                           "                        <fmt:message key='property.value'/>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\">\n" +
                           "                    </td>" +
                           "<td>\n" +
                           "<a onClick='javaScript:removeColumn(\"" + sId + "\")'" +
                           "style='background-image: url(../daspubsvcstat/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>\n" +
                           "                    </td>" +
                           "</tr></table>";

        $("#propertyTablePlaceHolder").append(propertyTable);
    }

    function removeColumn(id) {
        $("#" + id).remove();
    }

    function testServer(){

        var serverUrl = document.getElementById('url').value;
        var serverIp = serverUrl.split("://")[1].split(":")[0];
        var authPort = serverUrl.split("://")[1].split(":")[1];

        if(serverIp == null || authPort == null || serverIp == "" || authPort == ""){
            CARBON.showInfoDialog("Please enter the URL correctly.");
        } else{
            jQuery.ajax({
                            type:"GET",
                            url:"../daspubsvcstat/test_server_ajaxprocessor.jsp",
                            data:{action:"testServer", ip:serverIp, port:authPort},
                            success:function(data){
                                if(data != null && data != ""){
                                    var result = data.replace(/\n+/g, '');
                                    if(result == "true"){
                                        CARBON.showInfoDialog("Successfully connected to BAM Server");
                                    } else if(result == "false"){
                                        CARBON.showErrorDialog("BAM Server cannot be connected!")
                                    }
                                }
                            }
                        });
        }
    }



    function enableStatisticsStreamFields(){
           if($(".serviceConfigurationCheckBox").is(":checked")){
                $(".serviceConfigurationInput").each(function(){
                    $(this).removeAttr('disabled');
                });
           }else{
                $(".serviceConfigurationInput").each(function(){
                    $(this).attr('disabled','disabled');
                })
           }
    }

    /*function enableActivityStreamFields() {
            if (document.getElementsByName()[0]('enableActivityService').checked) {
                document.getElementById('activity_stream_name').removeAttribute('disabled');
                document.getElementById('activity_version').removeAttribute('disabled');
                document.getElementById('activity_nick_name').removeAttribute('disabled');
                document.getElementById('activity_description').removeAttribute('disabled');
            } else {
                document.getElementById('activity_stream_name').disabled = 'disabled';
                document.getElementById('activity_version').disabled = 'disabled';
                document.getElementById('activity_nick_name').disabled = 'disabled';
                document.getElementById('activity_description').disabled = 'disabled';
            }
        }*/
    /*function enableStatisticsStreamFields() {
        if (document.getElementsByName()[0]('enableServiceStats').checked) {
            document.getElementById('stream_name').removeAttribute('disabled');
            document.getElementById('version').removeAttribute('disabled');
            document.getElementById('nick_name').removeAttribute('disabled');
            document.getElementById('description').removeAttribute('disabled');
        } else {
            document.getElementById('stream_name').disabled = 'disabled';
            document.getElementById('version').disabled = 'disabled';
            document.getElementById('nick_name').disabled = 'disabled';
            document.getElementById('description').disabled = 'disabled';            
        }
    }*/
</script>

<div id="middle">
    <h2>
        <fmt:message key="bam.service.data.publisher.config"/>
    </h2>

    <div id="workArea">
        <div id="result"></div>
        <p>&nbsp;</p>

        <% if (eventingConfigData != null && !eventingConfigData.getPublishingEnable()) { %>
            <div class="alert-error">
                Data publishing currently disabled. To enable it, configure ServiceDataPublishing to
                'enable' in $CARBON_HOME/repository/conf/etc/bam.xml.
            </div>
        <%
        }
        %>

        <form action="configure_service_publisher.jsp" method="post">
            <input type="hidden" name="setConfig" value="on"/>
            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="service.configuration"/>
                    </th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <% if (isServiceStatsEnable) { %>
                        <input type="checkbox" name="enableServiceStats"
                               checked="true"  class="serviceConfigurationCheckBox" onchange="enableStatisticsStreamFields()">&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } else { %>
                        <input type="checkbox" name="enableServiceStats" class="serviceConfigurationCheckBox" onchange="enableStatisticsStreamFields()">&nbsp;&nbsp;&nbsp;&nbsp;
                        <% } %>
                        <fmt:message key="enable.service.stats"/>

                    </td>
                </tr>
               <%-- <% if (isServiceStatsEnable) { %>--%>
                    <tr>
                        <td><fmt:message key="stream.name"/></td>
                        <td><input id="stream_name" class="serviceConfigurationInput" type="text" name="stream_name" value="<%=streamName%>"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="version"/></td>
                        <td><input id="version" type="text" class="serviceConfigurationInput" name="version" value="<%=version%>"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="nick.name"/></td>
                        <td><input id="nick_name" type="text" class="serviceConfigurationInput" name="nick_name" value="<%=nickName%>"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="description"/></td>
                        <td><input id="description" type="text" class="serviceConfigurationInput" name="description" value="<%=description%>"/></td>
                    </tr>
<%--                <% } else { %>
                    <tr>
                        <td><fmt:message key="stream.name"/></td>
                        <td><input id="stream_name" type="text" name="stream_name" value="<%=streamName%>" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="version"/></td>
                        <td><input id="version" type="text" name="version" value="<%=version%>" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="nick.name"/></td>
                        <td><input id="nick_name" type="text" name="nick_name" value="<%=nickName%>" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="description"/></td>
                        <td><input id="description" type="text" name="description" value="<%=description%>" disabled="disabled"/></td>
                    </tr>
                <% } %>--%>
<%--                <% } else { %>
                    <tr>
                        <td><fmt:message key="stream.name"/></td>
                        <td><input id="activity_stream_name" type="text" name="activity_stream_name" value="<%=activityStreamName%>" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="version"/></td>
                        <td><input id="activity_version" type="text" name="activity_version" value="<%=activityVersion%>" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="nick.name"/></td>
                        <td><input id="activity_nick_name" type="text" name="activity_nick_name" value="<%=activityNickName%>" disabled="disabled"/></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="description"/></td>
                        <td><input id="activity_description" type="text" name="activity_description" value="<%=activityDescription%>" disabled="disabled"/></td>
                    </tr>
                <% } %>--%>

                    <%--                    <% if (isServiceStatsEnable || isMsgDumpingEnable) { %>--%>
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="bam.credential"/>
                    </th>
                </tr>
                </thead>
                <%
                    if(!client.isCloudDeployment()){
                %>
                <tr>
                    <td><fmt:message key="bam.url"/></td>
                    <td>
                        <input type="text" id="url" name="url" value="<%=url%>"/>
                        <input type="button" value="Test Server" onclick="testServer()"/>
                    </td>
                </tr>
                <%
                    }else {
                %>
                   <input type="hidden" id="url" name="url" value="<%=client.getBAMServerURL()%>"/>
                <%
                    }
                %>
                <tr>
                    <td><fmt:message key="username"/></td>
                    <td><input type="text" name="user_name" value="<%=userName%>"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="password"/></td>
                    <td><input type="password" name="password" value="<%=password%>"/></td>
                </tr>

                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="properties"/>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td id="propertyTablePlaceHolder" colspan="2">

                        <% if (properties != null) { %>
                        <table id="propertyTable" width="100%" class="styledLeft"
                           style="margin-left: 0px;">
                            <tr>
                                <td colspan="3">
                                    <a onClick='javaScript:addColumn()' style='background-image:
                                    url(../daspubsvcstat/images/add.gif);' class='icon-link addIcon'>Add
                                                                                                     Property</a>
                                </td>
                            </tr>
                            <% int i = 1;
                            for (Property property : properties) {

                            %>
                            <tr id="propertyTable_<%=i%>">
                                <td>
                                    <fmt:message key="property.name"/>
                                    <input type="text" name="<%=PROPERTY_KEYS%>"
                                           value="<%=property.getKey()%>">
                                </td>
                                <td>
                                    <fmt:message key="property.value"/>
                                    <input type="text" name="<%=PROPERTY_VALUES%>"
                                           value="<%=property.getValue()%>">
                                </td>

                                <td>
                                    <a onClick='javaScript:removeColumn("propertyTable_<%=i%>")' style='background-image:
                                    url(../daspubsvcstat/images/delete.gif);' class='icon-link addIcon'>Remove
                                                                                                        Property</a>
                                </td>


                            </tr>
                            <script type="text/javascript">
                                rowNum++;
                            </script>
                            <% i++;
                            }
                            %>

                        </table>
                        <%
                        } else { %>
                            <table width="100%" class="styledLeft" style="margin-left: 0px;">
                                <tr>
                                    <td colspan="3">
                                    <a onClick='javaScript:addMetaData()'
                                    style='background-image: url(../daspubsvcstat/images/add.gif);'
                                    class='icon-link addIcon'>Add Property</a>
                                    </td>
                                </tr>
                            </table>

                        <% } %>
                    </td>
                </tr>
                </tbody>
                <tr>
                    <td colspan="4" class="buttonRow">
                    <% if (eventingConfigData != null && eventingConfigData.getPublishingEnable()) {%>
                        <input type="submit" class="button" value="<fmt:message key="update"/>"
                               id="updateStats"/>&nbsp;&nbsp;
                    <%    } else {  %>
                        <input type="submit" class="button" value="<fmt:message key="update"/>"
                               id="updateStats" disabled="disabled"/>&nbsp;&nbsp;
                    <% } %>
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>


</fmt:bundle>

