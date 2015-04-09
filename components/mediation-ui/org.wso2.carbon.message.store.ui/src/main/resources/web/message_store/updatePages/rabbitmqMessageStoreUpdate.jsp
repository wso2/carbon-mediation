<%@ page import="java.util.HashMap" %>
<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
%>

<%

    String name = request.getParameter("Name").trim();
    String params = request.getParameter("tableParams").trim();
    String provider = "org.apache.synapse.message.store.impl.rabbitmq.RabbitMQStore";

    if (params != null) {
        params = params.trim();
    }


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

    HashMap<String, String> paramList = new HashMap<String, String>();
    if (params != null) {
        String[] paramParts = params.split("\\|");
        for (int i = 1; i < paramParts.length; i++) {
            String part = paramParts[i];
            String[] pair = part.split("#");
            String pName = pair[0];
            String value = pair[1];
            paramList.put(pName.trim(), value.trim());
            messageStoreXml.append("<parameter name=\"").append(pName.trim()).append("\">").
                    append(value.trim()).append("</parameter>");

        }

    }

//    if (!paramList.containsKey("java.naming.factory.initial") ||
//            !paramList.containsKey("java.naming.provider.url")) {
//        throw new Exception();
//    }

    messageStoreXml.append("</messageStore>");
    String configuration = messageStoreXml.toString().trim();
    session.setAttribute("messageStoreConfiguration", configuration);
    session.setAttribute("provider", provider);
    session.setAttribute("name",name);


%>