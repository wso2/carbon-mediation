<%@ page import="java.util.HashMap" %>
<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
%>

<%

    String name = request.getParameter("Name").trim();
    String provider = request.getParameter("Provider").trim();

    String entry = null;

    StringBuilder messageStoreXml = new StringBuilder();

    if (provider == null || provider.equals("")) {
        messageStoreXml.append("<messageStore name=\"");
        messageStoreXml.append(name.trim()).append("\"" + " ").append("xmlns=\"")
                .append(SYNAPSE_NS).append("\">");
    } else {
        messageStoreXml.append("<messageStore name=\"");
        messageStoreXml.append(name.trim()).append("\"" + " ").append("class=\"").append(provider).append("\"" + " ").append("xmlns=\"")
                .append(SYNAPSE_NS).append("\">");
    }


    messageStoreXml.append("</messageStore>");
    String configuration = messageStoreXml.toString().trim();
    session.setAttribute("messageStoreConfiguration", configuration);
    session.setAttribute("provider", provider);
    session.setAttribute("name",name);


%>