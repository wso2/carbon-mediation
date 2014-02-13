<%
    String configuration = request.getParameter("messageProcessorString");
    String mpName = request.getParameter("mpName");
    String mpProvider = request.getParameter("mpProvider");
    String mpStore = request.getParameter("mpStore");
    configuration = configuration.replaceAll("\\s\\s+|\\n|\\r", ""); // remove the pretty printing from the string
    configuration = configuration.replace("&", "&amp;"); // this is to ensure that url is properly encoded

    session.setAttribute("messageProcessorConfiguration", configuration);
    session.setAttribute("mpName", mpName);
    session.setAttribute("mpProvider", mpProvider);
    session.setAttribute("mpStore", mpStore);

%>