<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
%>

<%
    String name = request.getParameter("Name").trim();
    String provider = request.getParameter("Provider");
    String store = request.getParameter("MessageStore");
    String params = request.getParameter("tableParams");
    String targetEndpoint = request.getParameter("TargetEndpoint");

    if (params != null) {
        params = params.trim();
    }

    StringBuilder messageProcessorXml = new StringBuilder();

    if (provider == null || provider.equals("")) {
        throw new Exception("Provider can't be Empty");
    } else {

        if (store == null || "".equals(store.trim())) {
            throw new Exception("Message Store can't be Empty");
        } else {
            messageProcessorXml.append("<messageProcessor name=\"");
            messageProcessorXml.append(name.trim()).append("\"" + " ").append("class=\"").append(provider.trim());
                    if (targetEndpoint != null) {
                        messageProcessorXml.append("\"" + " ").append("targetEndpoint=\"").append(targetEndpoint.trim());
                    }
                    messageProcessorXml.append("\"" + " ").append("messageStore=\"").append(store.trim()).append("\"" + " ").
                    append("xmlns=\"").append(SYNAPSE_NS).append("\">");
        }

    }

    if (params != null) {
        String[] paramParts = params.split("\\|");
        for (int i = 1; i < paramParts.length; i++) {
            String part = paramParts[i];
            String[] pair = part.split("#");
            String pName = pair[0];
            String value = pair[1];
            messageProcessorXml.append("<parameter name=\"").append(pName.trim()).append("\">").
                    append(value.trim()).append("</parameter>");

        }

    }
    messageProcessorXml.append("</messageProcessor>");
    String configuration = messageProcessorXml.toString().trim();
    session.setAttribute("messageProcessorConfiguration", configuration);
    session.setAttribute("name", name);
    session.setAttribute("provider", provider);
    session.setAttribute("store", store);
    session.setAttribute("targetEndpoint", targetEndpoint);

%>