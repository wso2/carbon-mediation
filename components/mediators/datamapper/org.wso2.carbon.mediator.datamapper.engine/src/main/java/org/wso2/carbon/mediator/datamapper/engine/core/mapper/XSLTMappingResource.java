package org.wso2.carbon.mediator.datamapper.engine.core.mapper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .EMPTY_STRING;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .PARAMETER_FILE_ROOT;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants
        .PROPERTY_SEPERATOR;
import static org.wso2.carbon.mediator.datamapper.engine.utils.DataMapperEngineConstants.RUN_TIME_PROPERTIES;

public class XSLTMappingResource {

    private final Map<String,String> runTimeProperties;
    private String name;
    private String content;

    public XSLTMappingResource(String content) throws SAXException,
            IOException,
            ParserConfigurationException {
        this.content = content;
        this.runTimeProperties = new HashMap<>();
        Document document = getDocument();
        processOperators(document);
        processHeader(document);
    }

    InputStream getInputStream(){
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    InputSource getInputSource(){
        return new InputSource(new StringReader(content));
    }

    private Document getDocument() throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(getInputSource());
    }

    private void processOperators(Document document){
        Node rootNode = document.getElementsByTagName(PARAMETER_FILE_ROOT).item(0);
        for(int j=0;j<rootNode.getAttributes().getLength();j++){
            Node propertyNode = rootNode.getAttributes().item(j);
            if(propertyNode.getNodeName().equals(RUN_TIME_PROPERTIES)){
                String runTimePropertyString = propertyNode.getNodeValue();
                if(!EMPTY_STRING.equals(runTimePropertyString)) {
                    String[] properties = runTimePropertyString.split(PROPERTY_SEPERATOR);
                    int currentIndex = 0;
                    while (currentIndex < properties.length) {
                        runTimeProperties.put(properties[currentIndex], properties[currentIndex + 1]);
                        currentIndex += 2;
                    }
                }
                break;
            }
        }

    }

    private void processHeader(Document document){
        Node templateNode = document.getElementsByTagName("xsl:template").item(0);
        for (int i = 0; i < templateNode.getChildNodes().getLength(); i++) {
            this.name = templateNode.getChildNodes().item(i).getNodeName();
        }

    }

    public String getName() {
        return name;
    }

    Map<String, String> getRunTimeProperties() {
        return runTimeProperties;
    }
}
