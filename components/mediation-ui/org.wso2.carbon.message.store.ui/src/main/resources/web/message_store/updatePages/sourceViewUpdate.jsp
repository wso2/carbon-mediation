<%
    String configuration = request.getParameter("messageStoreString");
    String msName = request.getParameter("msName");
    String msProvider = request.getParameter("msProvider");
    configuration = configuration.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
    configuration = configuration.replace("&", "&amp;"); // this is to ensure that url is properly encoded

    session.setAttribute("messageStoreConfiguration", configuration);
    session.setAttribute("msName", msName);
    session.setAttribute("msProvider", msProvider);

%>