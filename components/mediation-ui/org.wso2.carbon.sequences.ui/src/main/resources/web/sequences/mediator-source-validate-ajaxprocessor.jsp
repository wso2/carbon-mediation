<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%
    try {
        SequenceEditorHelper.parseStringToMediator(request.getParameter("mediatorSrc"));
        out.write("valid");
    } catch (Exception e) {
        out.write("invalid");
    }                          
%>