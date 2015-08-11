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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<link href="css/task.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="inboundcommon.js"></script>
<script type="text/javascript">
var existingInbounds = null;
</script>
<fmt:bundle basename="org.wso2.carbon.inbound.ui.i18n.Resources">
    <carbon:breadcrumb label="inbound.header.new"
                       resourceBundle="org.wso2.carbon.inbound.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <% InboundManagementClient client;
        try {
            client = InboundManagementClient.getInstance(config, session);
            String[] msg = client.getAllInboundNames();
            int length =0 ;
            if(msg != null){
             length = msg.length;
             %>
              <script type="text/javascript">var existingInbounds = new Array(<%=length%>);</script>

         <%
            for(int i=0;i< length;i++){
               String nameOfInboundEndpoint = msg[i];
               %>
             <script type="text/javascript">existingInbounds[<%=i%>]='<%=nameOfInboundEndpoint%>';</script>
            <%
            }
            }
    %>
    <form method="post" name="inboundcreationform" id="inboundcreationform"
          action="newInbound1.jsp">

        <div id="middle">
            <h2><fmt:message key="inbound.header.new"/></h2>

            <div id="workArea">

                <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message key="inbound.header.new"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.name"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <input id="inboundName" name="inboundName" class="longInput" type="text"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="width:150px"><fmt:message key="inbound.type"/><span
                                class="required">*</span></td>
                        <td align="left">
                            <select id="inboundType" name="inboundType" class="longInput" on>
                                <option value="<%=InboundClientConstants.TYPE_HTTP%>">HTTP</option>
                                <option value="<%=InboundClientConstants.TYPE_HTTPS%>">HTTPS</option>
                                <option value="<%=InboundClientConstants.TYPE_FILE%>">File</option>
                                <option value="<%=InboundClientConstants.TYPE_JMS%>">JMS</option>
                                <option value="<%=InboundClientConstants.TYPE_HL7%>">HL7</option>
                                <option value="<%=InboundClientConstants.TYPE_KAFKA%>">KAFKA</option>
                                <option value="<%=InboundClientConstants.TYPE_CLASS%>">Custom</option>
                                <option value="<%=InboundClientConstants.TYPE_MQTT%>">MQTT</option>
                            </select>
                        </td>
                    </tr>



                    <tr>
                        <td class="buttonRow" colspan="3">
                            <input class="button" type="button"
                                   value="<fmt:message key="inbound.next.button.text"/>"
                                   onclick="inboundsave1('<fmt:message key="inbound.name.cannotfound.msg"/>','<fmt:message key="task.classname.cannotfound.msg"/>',document.inboundcreationform); return false;"/>
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

