<%--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 --%>
<%@page import="org.apache.synapse.mediators.Value"%>
<%@page import="org.wso2.carbon.mediator.fastXSLT.ui.FastXSLTMediator"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%
    try {
        Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
        if (!(mediator instanceof FastXSLTMediator)) {
            // todo : proper error handling
            System.out.println("Updating Error....");
            throw new RuntimeException("Unable to edit the mediator");
        }
        FastXSLTMediator xsltMediator = (FastXSLTMediator) mediator;
        xsltMediator.setXsltKey(null);
        String keyVal;
        String keyExp;
        XPathFactory xPathFactory = XPathFactory.getInstance();

        String keyGroup = request.getParameter("keygroup");
        if (keyGroup != null && !keyGroup.equals("")) {
            if (keyGroup.equals("StaticKey")) {
                keyVal = request.getParameter("mediator.fastXSLT.key.static_val");
                if (keyVal != null && !keyVal.equals("")) {
                    Value staticKey = new Value(keyVal);
                    xsltMediator.setXsltKey(staticKey);
                }
            } else if (keyGroup.equals("DynamicKey")) {
                keyExp = request.getParameter("mediator.fastXSLT.key.dynamic_val");


                if (keyExp != null && !keyExp.equals("")) {
                    Value dynamicKey = new Value(xPathFactory.createSynapseXPath(
                            "mediator.fastXSLT.key.dynamic_val", request.getParameter("mediator.fastXSLT.key.dynamic_val"), session));
                    xsltMediator.setXsltKey(dynamicKey);
                }
            }
        }
%>
<%
} catch (Exception e) {
     %>
<script type="text/javascript">
    jQuery(document).ready(function() {
        CARBON.showErrorDialog("An error has been occurred !. Error Message : " + '<%=e.getMessage()%>');
    });
</script>
<%
        return;
    }
%>
