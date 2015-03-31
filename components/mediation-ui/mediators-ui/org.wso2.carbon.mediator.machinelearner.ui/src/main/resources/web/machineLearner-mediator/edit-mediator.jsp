<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.machinelearner.ui.MLMediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof MLMediator)) {
        throw new RuntimeException("Unable to edit the mediator");
    }
    MLMediator mlMediator = (MLMediator) mediator;
    String modelName = mlMediator.getModelName();
%>

<fmt:bundle basename="org.wso2.carbon.mediator.machinelearner.ui.i18n.Resources">
<carbon:jsi18n
            resourceBundle="org.wso2.carbon.mediator.machinelearner.ui.i18n.JSResources"
            request="<%=request%>"
            i18nObjectName="mli18n"/>
<div>
<script type="text/javascript" src="../machineLearner-mediator/js/mediator-util.js"></script>
<script type="text/javascript">
    var val;
    jQuery('#actionID').click(function() {
        val = document.getElementById('mediatorInputId').value;
        if(mlMediatorValidate()) {
            var url = '../machineLearner-mediator/machineLearner-ajaxprocessor.jsp';
            jQuery('#attribDescription').load(url, {mediatorInput: val, clearAll : 'true'},
                    function(res, status, t) {
                        if (status != "success") {
                            CARBON.showErrorDialog('<fmt:message key="mediator.ml.error.msg"/>');
                        }
                    })
            return false;
        }
    });
</script>
<table class="normal" width="100%">
<tr>
    <td>
        <h2><fmt:message key="mediator.ml.header"/></h2>
    </td>
</tr>
<tr>
    <td>
        <table class="normal">
            <tr>
                <td><fmt:message key="mediator.ml.model.name"/><span class="required">*</span>
                </td>
                <td align="left">
                    <input type="text" id="mediatorInputId" name="mediatorInput" size="35"
                           value="<%= modelName !=null ? modelName : ""%>"/>
                </td>
                <td>
                    <input id="actionID" type="button" value="<fmt:message key="mediator.ml.load.model"/>"
                           class="button"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
<td>
<div class="Mediator" id="attribDescription">
    <% if (modelName != null && modelName.length() > 0) { %>
        <jsp:include page="machineLearner-ajaxprocessor.jsp">
            <jsp:param name="mediatorInput" value="<%= modelName%>"/>
        </jsp:include>
    <% } %>
</div>
</td>
</tr>
</table>
</div>
</fmt:bundle>
