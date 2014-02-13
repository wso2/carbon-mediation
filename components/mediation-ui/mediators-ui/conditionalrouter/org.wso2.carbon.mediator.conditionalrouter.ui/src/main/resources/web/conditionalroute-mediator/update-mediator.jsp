<!--
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
 -->

<%@ page import="org.wso2.carbon.mediator.router.RouteMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="org.wso2.carbon.mediator.conditionalrouter.ConditionalRouteMediator" %>
<%@ page import="org.wso2.carbon.mediator.conditionalrouter.ConditionalRouteMediatorService" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.apache.synapse.util.AXIOMUtils" %>
<%@ page import="org.apache.axiom.om.impl.llom.util.AXIOMUtil" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.synapse.commons.evaluators.EvaluatorException" %>
<%@ page import="org.apache.synapse.commons.evaluators.config.EvaluatorFactoryFinder" %>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof ConditionalRouteMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }

    ConditionalRouteMediator routeMediator = (ConditionalRouteMediator) mediator;

    String breakRoute = request.getParameter("mediator.conditionalroute.break");
    if (breakRoute != null && breakRoute.equals("true")) {
        routeMediator.setBreakAfter(true);
    } else {
        routeMediator.setBreakAfter(false);
    }

    String condition = request.getParameter("conditionalRouteConfig");
    if (condition != null) {
        try {
            OMElement evaluatorElem = org.apache.axiom.om.util.AXIOMUtil.stringToOM(condition);
            if (evaluatorElem != null) {
                routeMediator.setEvaluator(EvaluatorFactoryFinder.getInstance().
                        getEvaluator(evaluatorElem.getFirstElement()));
            }
        } catch (XMLStreamException xse) {
            throw new RuntimeException("Cannot Evaluate : Non xml content");

        } catch (EvaluatorException ee) {
            throw new RuntimeException("Cannot Evaluate : Evaluator Exception");
        }
    }

    String targetSeq = request.getParameter("seq.target");
    if (targetSeq != null && !targetSeq.equals("")) {
        routeMediator.setTargetSeq(targetSeq);
    }


%>

<script type="text/javascript">
    document.location.href = "../sequences/design_sequence.jsp";
</script>

