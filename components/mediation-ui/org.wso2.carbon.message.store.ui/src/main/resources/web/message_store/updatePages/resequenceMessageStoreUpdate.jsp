<%@ page import="java.util.HashMap" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformationRepository" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesInformation" %>
<%@ page import="java.util.Iterator" %>
<%!
    private static final String SYNAPSE_NS = "http://ws.apache.org/ns/synapse";
%>

<%

    String name = request.getParameter("Name").trim();
    String id = "resequencer.argValue";
    String type = "json";
    String parameterResequenceIdPath = "store.resequence.id.path";
    String params = request.getParameter("tableParams").trim();
    String provider = "org.apache.synapse.message.store.impl.resequencer.ResequenceMessageStore";
    NameSpacesInformationRepository repository = (NameSpacesInformationRepository) session.getAttribute(
            NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY);
    NameSpacesInformation information = null;
    if (repository == null) {
        repository = new NameSpacesInformationRepository();
        session.setAttribute(NameSpacesInformationRepository.NAMESPACES_INFORMATION_REPOSITORY, repository);
    } else {
        information = repository.getNameSpacesInformation(name, id);
    }
    StringBuilder messageStoreXml = new StringBuilder();

    messageStoreXml.append("<messageStore name=\"");
    messageStoreXml.append(name.trim()).append("\"" + " ").append("class=\"").append(provider).append("\"" + " ")
            .append("xmlns=\"").append(SYNAPSE_NS).append("\">");

    HashMap<String, String> paramList = new HashMap<String, String>();
    if (params != null) {
        String[] paramParts = params.split("\\|");
        for (int i = 1; i < paramParts.length; i++) {
            String part = paramParts[i];
            String[] pair = part.split("#");
            String pName = pair[0];
            String value = pair[1];
            paramList.put(pName.trim(), value.trim());

            if (pName.equals(parameterResequenceIdPath)) {
                messageStoreXml.append("<parameter name=\"").append(pName.trim()).append("\"").append(" ");
                if (!value.startsWith(type) && information != null && information.getPrefixes() != null) {
                    for (Iterator<String> it = information.getPrefixes(); it.hasNext(); ) {
                        String prefix = it.next();
                        if (!SYNAPSE_NS.equals(information.getNameSpaceURI(prefix))) {
                            messageStoreXml.append("xmlns:").append(prefix).append("=\"")
                                    .append(information.getNameSpaceURI(prefix)).append("\" ");
                        }
                    }
                }
                messageStoreXml.append("expression=\"").append(value.trim()).append("\" />");
            } else {
                messageStoreXml.append("<parameter name=\"").append(pName.trim()).append("\">").
                        append(value.trim()).append("</parameter>");
            }
        }
    }

    messageStoreXml.append("</messageStore>");
    String configuration = messageStoreXml.toString().trim();
    session.setAttribute("messageStoreConfiguration", configuration);
    session.setAttribute("provider", provider);
    session.setAttribute("name", name);


%>